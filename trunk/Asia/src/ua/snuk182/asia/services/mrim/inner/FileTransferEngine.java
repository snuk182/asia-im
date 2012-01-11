package ua.snuk182.asia.services.mrim.inner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.ProtocolUtils;
import ua.snuk182.asia.services.icq.inner.ICQServiceResponse;
import ua.snuk182.asia.services.mrim.MrimEntityAdapter;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimFileTransfer;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimIncomingFile;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimPacket;

public class FileTransferEngine {

	private static final String MRA_FT_HELLO = "MRA_FT_HELLO ";
	private static final String MRA_GET_FILE = "MRA_FT_GET_FILE";
	private static final String LIST_DATA_DIVIDER = ";";
	private static final String IN_DATA_DIVIDER = ":";

	private static final int SERVER_SOCKET_TIMEOUT = 600000;

	private byte[] localIp = new byte[] { 0, 0, 0, 0 };

	private final MrimServiceInternal service;
	private final List<MrimFileTransfer> transfers = new ArrayList<MrimFileTransfer>();
	private final Map<Long, FileRunnableService> activeTransfers = new HashMap<Long, FileRunnableService>();

	private List<NotificationData> notifications = new LinkedList<NotificationData>();

	public FileTransferEngine(MrimServiceInternal service) {
		this.service = service;
	}

	public long sendFiles(String buddyMrid, final List<File> files, byte[] localIp) {
		final MrimFileTransfer transfer = getFileTransferRequest(buddyMrid, files);
		this.localIp = localIp;

		transfers.add(transfer);
		new Thread("File sender " + buddyMrid) {

			@Override
			public void run() {
				try {
					sendFileTransferRequest(createPeer(transfer));
				} catch (IOException e) {
					// todo ask mirror?
				}
			}

		}.start();
		return transfer.messageId;
	}

	private FileRunnableService createPeer(MrimFileTransfer transfer) throws IOException {
		service.log("ft: creating own peer");
		FileRunnableService frs = activeTransfers.get(transfer.messageId);

		if (frs == null) {
			service.log("ft: new runnable for " + transfer.buddyMrid);
			frs = new FileRunnableService(transfer);
			frs.connectionState = FileRunnableService.CONNSTATE_FILE_HEADER;
			activeTransfers.put((long) transfer.messageId, frs);
		} else {
			service.log("ft: existing runnable for " + transfer.buddyMrid);
			frs.transfer = transfer;
		}
		frs.connectionState = FileRunnableService.CONNSTATE_FILE_HEADER;

		frs.server = createLocalSocket(frs);
		/*
		 * message.externalPort = frs.server.getLocalPort(); message.rvIp =
		 * ProtocolUtils.getIPString(service.getInternalIp());
		 * message.rvMessageType = 0;
		 */
		return frs;
	}

	private void sendFileTransferRequest(FileRunnableService frs) {
		MrimPacket packet = new MrimPacket();
		packet.type = MrimConstants.MRIM_CS_FILE_TRANSFER;
		byte[] to = MrimEntityAdapter.string2lpsa(frs.transfer.buddyMrid);
		byte[] sessionId = ProtocolUtils.int2ByteLE(packet.type);

		long ln = 0;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < frs.transfer.files.size(); i++) {
			File file = frs.transfer.files.get(i);
			ln += file.length();

			sb.append(file.getName().replace(':', '_').replace(';', '_'));
			sb.append(IN_DATA_DIVIDER);
			sb.append(file.length());
			if (i < frs.transfer.files.size() - 1) {
				sb.append(';');
			}
		}
		byte[] lengthSum = ProtocolUtils.long2ByteLE(ln);
		byte[] filenames = MrimEntityAdapter.string2lpsa(sb.toString());
		byte[] unk1 = MrimEntityAdapter.string2lpsa("");
		byte[] myIpPort = MrimEntityAdapter.string2lpsa(ProtocolUtils.getIPString(localIp) + IN_DATA_DIVIDER + frs.server.getLocalPort());

		byte[] internalData = new byte[to.length + sessionId.length + 4 + 4 + filenames.length + unk1.length + myIpPort.length];

		int i = 0;
		System.arraycopy(to, 0, internalData, i, to.length);
		i += to.length;
		System.arraycopy(sessionId, 0, internalData, i, sessionId.length);
		i += sessionId.length;
		System.arraycopy(lengthSum, 0, internalData, i, 4);
		i += 4;
		System.arraycopy(ProtocolUtils.int2ByteLE(filenames.length + unk1.length + myIpPort.length), 0, internalData, i, 4);
		i += 4;
		System.arraycopy(filenames, 0, internalData, i, filenames.length);
		i += filenames.length;
		System.arraycopy(unk1, 0, internalData, i, unk1.length);
		i += unk1.length;
		System.arraycopy(myIpPort, 0, internalData, i, myIpPort.length);
		i += myIpPort.length;

		packet.rawData = internalData;

		service.getRunnableService().sendToSocket(packet);
	}

	private byte[] getHandshakeData(MrimFileTransfer transfer) {
		service.log("get handshake for " + transfer.host + " id " + transfer.messageId);

		String str = new String(MRA_FT_HELLO + service.getMrid());
		byte[] boo = new byte[str.length() + 1];
		System.arraycopy(str.getBytes(), 0, boo, 0, str.length());
		boo[boo.length - 1] = 0;
		return boo;
	}

	private ServerSocket createLocalSocket(final FileRunnableService frs) throws IOException {
		final ServerSocket server = new ServerSocket(0);

		new Thread("FT Server socket listener") {

			@Override
			public void run() {
				try {
					server.setSoTimeout(SERVER_SOCKET_TIMEOUT);
					Socket socket = server.accept();

					frs.socket = socket;
					service.log("client connected");
					frs.start();
				} catch (Exception e) {
					service.log(e);
				}
			}

		}.start();

		return server;
	}

	private MrimFileTransfer getFileTransferRequest(String buddyMrid, List<File> files) {
		MrimFileTransfer transfer = new MrimFileTransfer();
		transfer.buddyMrid = buddyMrid;
		transfer.files = files;
		transfer.messageId = files.hashCode();
		return transfer;
	}

	class FileRunnableService extends Thread {
		public static final int CONNSTATE_CONNECTED = 0;
		public static final int CONNSTATE_HANDSHAKE = 1;
		public static final int CONNSTATE_FILE_HEADER = 2;
		public static final int CONNSTATE_FILE_BODY = 3;
		public static final int CONNSTATE_FILE_SENT = 4;
		public static final int CONNSTATE_DISCONNECTED = 5;

		ServerSocket server = null;
		Socket socket;
		int connectionState = CONNSTATE_CONNECTED;
		MrimFileTransfer transfer;
		String participantUid = null;
		List<byte[]> blobs = new LinkedList<byte[]>();

		long currentFileSizeLeft = 0;
		long currentFileSize = 0;
		byte[] currentFileInfo = null;
		int totalFiles = 1;
		private ExtendedBufferedOutputStream currentFileStream;
		private String currentFileName;

		byte[] buffer = null;

		public FileRunnableService(MrimFileTransfer transfer) {
			this(null, transfer);
		}

		public FileRunnableService(Socket socket, MrimFileTransfer transfer) {
			this.socket = socket;
			this.transfer = transfer;
			if (transfer.files != null) {
				totalFiles = transfer.files.size();
				currentFileSize = 0;
				for (File f : transfer.files) {
					currentFileSize += f.length();
				}
			} else {
				totalFiles = transfer.incomingFiles.size();
			}

			participantUid = transfer.buddyMrid;

			setName("File transfer " + transfer.buddyMrid);
		}

		@Override
		public void run() {
			if (socket == null) {
				return;
			}

			getDataFromSocket();
		}

		private void sendFileRequest(MrimIncomingFile file) {
			byte[] infoBlob;

			String str = MRA_GET_FILE + " " + file.filename;
			infoBlob = new byte[str.length() + 1];
			System.arraycopy(str.getBytes(), 0, infoBlob, 0, str.length() - 1);
			infoBlob[infoBlob.length - 1] = 0;

			sendToSocket(infoBlob);
		}

		private void sendHandshake() {
			sendToSocket(getHandshakeData(transfer));
		}

		private boolean getDataFromSocket() {
			int read = 0;
			boolean fullPacket = true;
			final List<byte[]> tmpBlobs = new LinkedList<byte[]>();

			while (connectionState != CONNSTATE_DISCONNECTED && socket != null && socket.isConnected() && !socket.isClosed()) {
				InputStream is;
				try {
					is = socket.getInputStream();

					if (is.available() < 1) {
						Thread.sleep(300);
					} else {
						Thread.sleep(500);

						switch (connectionState) {
						case CONNSTATE_CONNECTED:
						case CONNSTATE_HANDSHAKE:
						case CONNSTATE_FILE_HEADER:
							byte[] blob = new byte[is.available()];
							is.read(blob, 0, blob.length);

							fullPacket = false;
							for (byte bu : blob) {
								if (bu == 0) {
									fullPacket = true;
									break;
								}
							}
							tmpBlobs.add(blob);
							break;
						case CONNSTATE_FILE_BODY:
							if (buffer == null) {
								buffer = new byte[88000];
							}

							read = is.read(buffer, 0, buffer.length);
							service.log("read " + read + "| bytes left " + currentFileSizeLeft);
							currentFileSizeLeft -= read;
							fileData(buffer, read, currentFileSizeLeft);

							if (currentFileSizeLeft < 1) {
								// sendFileAck();
								connectionState = CONNSTATE_FILE_HEADER;
								totalFiles--;
								buffer = null;
								if (totalFiles < 1) {
									cleanup();
								}
							}
							continue;
						}

						if (fullPacket) {
							byte[] boo = new byte[0];
							for (byte[] i : tmpBlobs) {
								boo = ProtocolUtils.concatByteArrays(boo, i);
							}

							tmpBlobs.clear();
							blobs.add(boo);
							new Thread("File transfer processor") {
								@Override
								public void run() {
									try {
										forceBlobProcess();
									} catch (Exception e) {
										service.log(e);
									}
								}
							}.start();
						}
					}
				} catch (IOException e) {
					service.log(e);
				} catch (InterruptedException e) {
					service.log(e);
				}
			}
			cleanup();
			return false;
		}

		private synchronized void fileData(byte[] blob, int read, final long bytesLeft) {
			if (currentFileStream != null) {
				try {
					/*
					 * if (connectionState == CONNSTATE_FILE_HEADER){
					 * currentFileStream.close(); currentFileStream = null; }
					 * else if (connectionState == CONNSTATE_FILE_BODY){
					 * currentFileStream.write(blob); }
					 */

					if (connectionState == CONNSTATE_FILE_BODY) {
						currentFileStream.write(blob, 0, read);
						currentFileStream.flush();

						if (bytesLeft < 1) {
							connectionState = CONNSTATE_FILE_HEADER;
							final String filename = currentFileStream.file.getAbsolutePath();
							currentFileStream.close();
							currentFileStream = null;
							service.log(currentFileName + " got");

							sendNotification(transfer.messageId, filename, currentFileSize, currentFileSize - bytesLeft, true, null, participantUid);
						} else {
							sendNotification(transfer.messageId, currentFileStream.getFile().getAbsolutePath(), currentFileSize, currentFileSize - bytesLeft, true, null, participantUid);
						}
					}
					// messageId, filename, totalSize, sizeTransferred,
					// isReceive, error

				} catch (IOException e) {
					ServiceUtils.log(e);
					try {
						currentFileStream.close();
					} catch (IOException e1) {
						ServiceUtils.log(e);
					}
					currentFileStream = null;
				}
			}
		}

		private void cleanup() {
			try {
				socket.close();
				transfers.remove(transfer);
				activeTransfers.remove(transfer.messageId);
				if (server != null) {
					server.close();
					server = null;
				}
			} catch (IOException e) {
			}
		}

		public synchronized boolean sendToSocket(byte[] out) {
			try {
				OutputStream os = socket.getOutputStream();

				service.log("-- FT To be sent " + ProtocolUtils.getSpacedHexString(out));
				os.write(out);

			} catch (IOException e) {
				connectionState = CONNSTATE_DISCONNECTED;
				service.log(e);
			}
			return true;
		}

		protected void forceBlobProcess() throws Exception {
			synchronized (blobs) {
				while (blobs.size() > 0) {
					byte[] blob = blobs.remove(0);
					process(blob);
				}
			}
		}

		private void process(byte[] blob) {
			String str = ProtocolUtils.getEncodedString(blob, 0, blob.length - 1);

			switch (connectionState) {
			case CONNSTATE_CONNECTED:
			case CONNSTATE_HANDSHAKE:
				try {
					String from = str.substring(str.indexOf(MRA_FT_HELLO));
					service.log(from + " connected to peer");
					if (transfer.files != null) { // if i'm the sender
						if (transfer.mirror) {
							sendHandshake();
						}
					} else {
						if (transfer.mirror) {
							sendFileRequest(transfer.incomingFiles.remove(0));
						} else {
							sendHandshake();
						}
					}
				} catch (Exception e) {
					service.log(e);
					transferFailed(transfer);
				}
				// parseRendezvous(blob);
				break;
			case CONNSTATE_FILE_HEADER:
				service.log("got header");
				currentFileInfo = blob;

				try {
					String fileInfo = str.substring(str.indexOf(MRA_GET_FILE));
					service.log(transfer.buddyMrid + " asks for file " + fileInfo);

					for (File fi : transfer.files) {
						if (fi.getName().equals(fileInfo)) {
							sendFileToSocket(fi);
							return;
						}
					}
					transferFailed(transfer);
				} catch (Exception e) {
					service.log(e);
					transferFailed(transfer);
				}
				break;
			case CONNSTATE_FILE_BODY:
				// fileData(blob);
				break;
			}
		}

		private void sendFileToSocket(final File file) {
			OutputStream os;
			try {
				os = socket.getOutputStream();
			} catch (IOException e) {
				service.log(e);
				transferFailed(transfer);
				cleanup();
				return;
			}
			long length = file.length();
			if (length > 8000) {
				buffer = new byte[8000];
			} else {
				buffer = new byte[(int) length];
			}

			currentFileSizeLeft = 0;
			int read = 0;
			service.log("sending " + file.getName() + " to " + participantUid);

			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis, 8000);
				while (currentFileSizeLeft < length) {
					read = bis.read(buffer, 0, buffer.length);
					if (read < 0) {
						break;
					}
					os.write(buffer, 0, read);
					os.flush();
					currentFileSizeLeft += read;
					service.log("sent " + currentFileSizeLeft + " bytes");

					sendNotification(transfer.messageId, file.getAbsolutePath(), length, currentFileSizeLeft, false, null, participantUid);

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						service.log(e);
					}
				}
			} catch (IOException e) {
				service.log(e);
				transferFailed(transfer);
				cleanup();
				return;
			}
			connectionState = CONNSTATE_FILE_HEADER;

			service.log(file.getName() + " sent");
		}

		private class ExtendedBufferedOutputStream extends BufferedOutputStream {

			private final File file;

			public ExtendedBufferedOutputStream(File file, OutputStream os) {
				super(os, 88000);
				this.file = file;
			}

			public File getFile() {
				return file;
			}
		}	
	}
	
	public void parseFTRequest(MrimPacket packet) {
		MrimFileTransfer transfer = new MrimFileTransfer();

		int i = 44;
		transfer.buddyMrid = MrimEntityAdapter.lpsa2String(packet.rawData, i);
		i += 4 + transfer.buddyMrid.length();
		transfer.messageId = ProtocolUtils.bytes2IntLE(packet.rawData, i);
		i += 4;
		i += 4; // total filesize - useless
		i += 4; // total file info size - useless

		String fileStr = MrimEntityAdapter.lpsa2String(packet.rawData, i);

		String[] strFiles = fileStr.split(LIST_DATA_DIVIDER);
		transfer.incomingFiles = new LinkedList<MrimIncomingFile>();
		for (String file : strFiles) {
			String[] attrs = file.split(IN_DATA_DIVIDER);
			MrimIncomingFile ifile = new MrimIncomingFile();
			ifile.filename = attrs[0];
			ifile.filesize = Long.parseLong(attrs[1]);
			transfer.incomingFiles.add(ifile);
		}

		i += 4 + fileStr.length();

		i += 4; // data divider

		String ipData = MrimEntityAdapter.lpsa2String(packet.rawData, i);
		String[] ipDataParts = ipData.split(LIST_DATA_DIVIDER);
		for (String data : ipDataParts) {
			String[] connection = data.split(IN_DATA_DIVIDER);

			transfer.host = connection[0];
			transfer.port = Integer.parseInt(connection[1]);

			break; // TODO do we need more?
		}

		transfers.add(transfer);

		service.getServiceResponse().respond(MrimServiceResponse.RES_FILEMESSAGE, transfer);
	}

	public void parseFTResponse(MrimPacket packet) {
		int i = 44;
		int status = ProtocolUtils.bytes2IntLE(packet.rawData, i);
		i += 4;
		String from = MrimEntityAdapter.lpsa2String(packet.rawData, i);
		i += from.length() + 4;
		int msgId = ProtocolUtils.bytes2IntLE(packet.rawData, i);
		i += 4;

		MrimFileTransfer transfer = null;
		for (MrimFileTransfer tr : transfers) {
			if (tr.messageId == msgId) {
				transfer = tr;
				break;
			}
		}

		if (transfer == null)
			return;

		switch (status) {
		case MrimConstants.FILE_TRANSFER_STATUS_OK:
			break;
		case MrimConstants.FILE_TRANSFER_STATUS_DECLINE: // here so far
		case MrimConstants.FILE_TRANSFER_STATUS_INCOMPATIBLE_VERS:
		case MrimConstants.FILE_TRANSFER_STATUS_ERROR:
			transferFailed(transfer);
			break;
		case MrimConstants.FILE_TRANSFER_MIRROR:
			String ipData = MrimEntityAdapter.lpsa2String(packet.rawData, i);
			String[] ipDataParts = ipData.split(LIST_DATA_DIVIDER);
			for (String data : ipDataParts) {
				String[] connection = data.split(IN_DATA_DIVIDER);

				transfer.host = connection[0];
				transfer.port = Integer.parseInt(connection[1]);
				transfer.mirror = true;

				connectPeer(transfer, null);
				break; // TODO do we need more?
			}
			break;
		}
	}

	private void connectPeer(MrimFileTransfer transfer, FileRunnableService runnable) {
		service.log("connecting peer " + transfer.host + ":" + transfer.port + " for " + transfer.messageId + "//receiver ");

		Socket socket;
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(InetAddress.getByAddress(ProtocolUtils.ipString2ByteBE(transfer.host)), transfer.port), 10000);
		} catch (Exception e) {
			service.log(e);
			socket = null;
		}

		if (socket != null && socket.isConnected()) {
			service.log("ft: direct socket connected for " + transfer.messageId);
			if (runnable == null) {
				service.log("ft: new runnable for " + transfer.messageId);
				runnable = new FileRunnableService(socket, transfer);
				activeTransfers.put((long) transfer.messageId, runnable);
			} else {
				service.log("ft: existing runnable for " + transfer.messageId);
				if (runnable.server != null) {
					try {
						runnable.server.close();
						runnable.server = null;
					} catch (IOException e) {
						service.log(e);
					}
				}
				runnable.socket = socket;

			}
			runnable.connectionState = FileRunnableService.CONNSTATE_FILE_HEADER;
			runnable.start();
			runnable.sendHandshake();
			service.getRunnableService().sendToSocket(getAcceptMessage(transfer));
		} else {
			service.log("ft: no direct connection");
			if (transfer.files == null) {
				createMirror(transfer);
			} else {
				transferFailed(transfer);
			}
		}
	}

	private void createMirror(MrimFileTransfer transfer) {
		try {
			MrimPacket packet = getAnswerMessage(transfer, MrimConstants.FILE_TRANSFER_MIRROR);

			FileRunnableService frs = createPeer(transfer);

			String ipData = ProtocolUtils.getIPString(localIp) + IN_DATA_DIVIDER + frs.socket.getLocalPort();

			packet.rawData = ProtocolUtils.concatByteArrays(packet.rawData, ipData.getBytes());

			transfer.mirror = true;

			service.getRunnableService().sendToSocket(packet);

		} catch (IOException e) {
			transferFailed(transfer);
		}
	}

	private void transferFailed(MrimFileTransfer transfer) {
		service.getRunnableService().sendToSocket(getAnswerMessage(transfer, MrimConstants.FILE_TRANSFER_STATUS_ERROR));
	}

	private MrimPacket getAcceptMessage(MrimFileTransfer transfer) {
		return getAnswerMessage(transfer, MrimConstants.FILE_TRANSFER_STATUS_OK);
	}

	private MrimPacket getAnswerMessage(MrimFileTransfer transfer, int ftStatus) {
		MrimPacket packet = new MrimPacket();
		packet.type = MrimConstants.MRIM_CS_FILE_TRANSFER_ACK;

		byte[] blob = new byte[8 + transfer.buddyMrid.length()];

		int i = 0;
		System.arraycopy(ProtocolUtils.int2ByteLE(ftStatus), 0, blob, i, 4);
		i += 4;

		System.arraycopy(transfer.buddyMrid.getBytes(), 0, blob, i, transfer.buddyMrid.length());
		i += transfer.buddyMrid.length();
		System.arraycopy(ProtocolUtils.int2ByteLE(transfer.messageId), 0, blob, i, 4);

		packet.rawData = blob;
		return packet;
	}

	private synchronized void sendNotification(int messageId, String filename, long totalSize, long sizeSent, boolean incoming, String error, String participantUid) {
		NotificationData data = new NotificationData(messageId, filename, totalSize, sizeSent, incoming, error, participantUid);
		notifications.add(data);

		new Thread("Notification") {

			@Override
			public void run() {
				sendNotifications();
			}

		}.start();
	}

	private void sendNotifications() {
		synchronized (notifications) {
			while (notifications.size() > 0) {
				NotificationData data = notifications.remove(0);
				service.getServiceResponse().respond(ICQServiceResponse.RES_FILEPROGRESS, data.messageId, data.filePath, data.totalSize, data.sent, data.incoming, data.error, data.participantUid);
			}
		}
	}

	private class NotificationData {

		public int messageId;
		public String filePath;
		public long totalSize;
		public long sent;
		public boolean incoming;
		public String error;
		public String participantUid;

		public NotificationData(int messageId, String filePath, long totalSize, long sent, boolean incoming, String error, String participantUid) {
			this.messageId = messageId;
			this.filePath = filePath;
			this.totalSize = totalSize;
			this.sent = sent;
			this.incoming = incoming;
			this.error = error;
			this.participantUid = participantUid;
		}
	}

	public void cancelAll() {
		for (FileRunnableService runnable : activeTransfers.values()) {
			if (runnable.socket != null && !runnable.socket.isClosed()) {
				try {
					runnable.socket.close();
				} catch (IOException e) {
					service.log(e);
				}
			}
		}
	}
}
