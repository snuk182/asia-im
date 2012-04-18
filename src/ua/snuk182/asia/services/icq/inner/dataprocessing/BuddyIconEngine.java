package ua.snuk182.asia.services.icq.inner.dataprocessing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import ua.snuk182.asia.services.api.ProtocolUtils;
import ua.snuk182.asia.services.icq.inner.ICQConstants;
import ua.snuk182.asia.services.icq.inner.ICQException;
import ua.snuk182.asia.services.icq.inner.ICQServiceInternal;
import ua.snuk182.asia.services.icq.inner.ICQServiceResponse;
import ua.snuk182.asia.services.icq.inner.dataentity.Flap;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQOnlineInfo;
import ua.snuk182.asia.services.icq.inner.dataentity.ServiceRedirect;
import ua.snuk182.asia.services.icq.inner.dataentity.Snac;
import ua.snuk182.asia.services.icq.inner.dataentity.TLV;

public class BuddyIconEngine {

	private ICQServiceInternal service;
	private ServiceRedirect serviceInfo;
	private IconRunnableService runnableService;
	private List<ICQOnlineInfo> queue = new LinkedList<ICQOnlineInfo>();
	private short flapSeqNumber = 0;
	
	private static final byte CONNSTATE_DISCONNECTED = 0;
	private static final byte CONNSTATE_CONNECTING = 1;
	private static final byte CONNSTATE_CONNECTED = 2;
	private volatile byte connectState = CONNSTATE_DISCONNECTED;
	
	public short getFlapSeqNumber() {
		if (flapSeqNumber>=0x8000){
			flapSeqNumber = 0;
		};
		return flapSeqNumber++;
	}
	
	private final List<Flap> packets = new LinkedList<Flap>();
	public Flap buffer = null;
	
	
	public BuddyIconEngine(ICQServiceInternal icqServiceInternal){
		this.service = icqServiceInternal;
	}
	
	protected void forceFlapProcess(){
		synchronized(packets){
			while(packets.size()>0){
				Flap flap = packets.remove(0);
				internalFlapMap(flap);
			}
		}
	}
	
	public void requestServiceServerUrl(){
		Flap flap = new Flap();
		flap.channel = ICQConstants.FLAP_CHANNELL_DATA;
		
		Snac data = new Snac();
		data.serviceId = ICQConstants.SNAC_FAMILY_GENERIC;
		data.subtypeId = ICQConstants.SNAC_GENERIC_NEWSERVICEREQUEST;
		data.requestId = ICQConstants.SNAC_GENERIC_NEWSERVICEREQUEST;
		
		byte[] familyId = ProtocolUtils.short2ByteBE(ICQConstants.SNAC_FAMILY_SERVERSTOREDBUDDYICON);
		
		data.plainData = familyId;
		flap.data = data;
		
		service.getRunnableService().sendToSocket(flap);
	}
	
	public void serviceServerURLResponse(ServiceRedirect serviceInfo){
		this.setServiceInfo(serviceInfo);		
		service.log("got icon service "+serviceInfo.serviceServerURL);
		startRunnableService();
	}
	
	private void startRunnableService(){
		runnableService = new IconRunnableService(serviceInfo.serviceServerURL);
		runnableService.start();
	}

	private void internalFlapMap(Flap flap){
		if (flap == null) return;
		switch (flap.channel){
		case ICQConstants.FLAP_CHANNELL_START:
			runnableService.sendToSocket(sendCookie());
			break;
		case ICQConstants.FLAP_CHANNELL_DATA:
			internalSnacMap(flap.data);
			break;
		}
	}
	
	private void internalSnacMap(Snac data){
		switch(data.getServiceId()){
		case ICQConstants.SNAC_FAMILY_GENERIC:
			switch (data.subtypeId){
			case ICQConstants.SNAC_GENERIC_SERVERSUPPORTEDFAMILIES:
				connectState = CONNSTATE_CONNECTED;
				if (buffer!=null){
					runnableService.sendToSocket(buffer);
					buffer = null;
				}
				forceIconRequests();
				break;
			}
			break;
		case ICQConstants.SNAC_FAMILY_SERVERSTOREDBUDDYICON:
			switch (data.subtypeId){
			case ICQConstants.SNAC_SERVERSTOREDBUDDYICON_BUDDYICONRES2:
				forceIconRequests();
				parseIconData(data.plainData);
				break;
			}
			break;
		}
		
	}

	private void parseIconData(byte[] plainData){
		if (plainData==null) return;
		
		byte uinLength = plainData[0];
		byte[] uinBytes = new byte[uinLength];
		System.arraycopy(plainData, 1, uinBytes, 0, uinLength);
		
		String uin;
		uin = ProtocolUtils.getEncodedString(uinBytes);
		
		byte hashLength = plainData[uinLength+4];
		
		byte[] hash = new byte[hashLength];
		System.arraycopy(plainData, uinLength+5, hash, 0, hashLength);
		
		int toSkip = hashLength*2 + 5 + 5 + uinLength;
		
		int iconLength = ProtocolUtils.unsignedShort2Int(ProtocolUtils.bytes2ShortBE(plainData, toSkip));
		byte[] iconBytes = new byte[iconLength];
		
		System.arraycopy(plainData, toSkip+2, iconBytes, 0, iconBytes.length);
		service.getServiceResponse().respond(ICQServiceResponse.RES_SAVEIMAGEFILE, iconBytes, uin, hash);
	}

	private void forceIconRequests(){
		if (queue.size()>0){
			runnableService.sendToSocket(makeIconRequest(queue.remove(0)));			
		} else {
			disconnect();
		}		
	}
	
	public void requestIcon(ICQOnlineInfo info){
		service.log(" attempt icon request for "+info.uin + ((info !=null && info.iconData != null) ? ProtocolUtils.getSpacedHexString(info.iconData.hash)+" id "+ info.iconData.iconId +" flags "+info.iconData.flags: ""));
		
		if (info == null || info.iconData == null || info.iconData.iconId!=1 || info.iconData.flags!=1) return;
		
		service.log("icon request for "+info.uin);
		
		queue.add(info);
		if (connectState == CONNSTATE_DISCONNECTED){
			service.log("icon service get server");
			requestServiceServerUrl();
			connectState = CONNSTATE_CONNECTING;
			return;
		}
		
		if (connectState == CONNSTATE_CONNECTED){
			forceIconRequests();
		}
	}

	private Flap sendCookie(){
		service.log("--- send icon cookie");
		
		Flap flap = new Flap();
		flap.channel = ICQConstants.FLAP_CHANNELL_START;
		flap.sequenceNumber = getFlapSeqNumber();
		
		TLV[] tlvs = new TLV[2];
		
		tlvs[0] = new TLV((short)0x0, new byte[]{0x00, 0x01});
		
		tlvs[1] = new TLV(ICQConstants.TLV_AUTHCOOKIE, serviceInfo.authCookie);
		
		flap.tlvData = tlvs;
		return flap;
		//service.getRunnableService().sendToSocket(flap);
	}

	private Flap makeIconRequest(ICQOnlineInfo info){		
		Flap flap = new Flap();
		flap.channel = ICQConstants.FLAP_CHANNELL_DATA;
		flap.sequenceNumber = getFlapSeqNumber();
		
		Snac data = new Snac();
		data.serviceId = ICQConstants.SNAC_FAMILY_SERVERSTOREDBUDDYICON;
		data.subtypeId = ICQConstants.SNAC_SERVERSTOREDBUDDYICON_BUDDYICONREQ2;
		data.requestId = ICQConstants.SNAC_SERVERSTOREDBUDDYICON_BUDDYICONREQ2;
		
		byte[] uinBytes;
		try {
			uinBytes = info.uin.getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			uinBytes = info.uin.getBytes();
		}
		
		byte[] plainData = new byte[6+uinBytes.length+info.iconData.hash.length];
		plainData[0]= (byte) uinBytes.length;
		System.arraycopy(uinBytes, 0, plainData, 1, uinBytes.length);
		plainData[uinBytes.length+1] = 0x01;
		
		/*System.arraycopy(Utils.short2ByteBE((short) 0x01), 0, plainData, uinBytes.length+2, 2);
		plainData[uinBytes.length+4] = 0x01;*/
		
		System.arraycopy(ProtocolUtils.short2ByteBE(info.iconData.iconId), 0, plainData, uinBytes.length+2, 2);
		plainData[uinBytes.length+4] = info.iconData.flags;
		
		plainData[uinBytes.length+5] = (byte) info.iconData.hash.length;
		System.arraycopy(info.iconData.hash, 0, plainData, uinBytes.length+6, info.iconData.hash.length);
		
		data.plainData = plainData;
		flap.data = data;
		
		return flap;
	}
	
	public void setServiceInfo(ServiceRedirect serviceInfo){
		this.serviceInfo = serviceInfo;
	}

	public ServiceRedirect getServiceInfo() {
		return serviceInfo;
	}

	class IconRunnableService extends Thread {
		String host;
		int port;
		Socket socket;
		
		//public volatile boolean connected = true;
		
		public IconRunnableService(String url){
			if (url.indexOf(":")>-1){
				String[] ur = url.split(":");
				host = ur[0];
				port = Integer.parseInt(ur[1]);
			} else {
				host = url;
				port = service.getLoginPort();
			}	
			setName("ICQ icon service");
		}
		
		@Override
		public void run() {
			try {
				socket = new Socket(host, port);
			} catch (UnknownHostException e) {
				service.log(e.getLocalizedMessage());
			} catch (IOException e) {
				service.log(e.getLocalizedMessage());
			}
			
			getDataFromSocket();
		}
		
		private boolean getDataFromSocket(){
			byte[] tail = null;
			int read = 0;
			int tailLength = 0;
			
			while (socket!=null && socket.isConnected() && connectState!=CONNSTATE_DISCONNECTED){
				try {
					InputStream is = socket.getInputStream();
					if (is.available()>0){
						Thread.sleep(200);
					
						if (tail == null){
							byte[] head = new byte[6];
							
							is.read(head, 0, 6);
							
							read = 0;
							tailLength = ProtocolUtils.unsignedShort2Int(ProtocolUtils.bytes2ShortBE(head, 4));					
						
							tail = new byte[6+tailLength];
							System.arraycopy(head, 0, tail, 0, 6);
							read += is.read(tail, 6, tailLength);
							service.log("icon engine get "+ProtocolUtils.getSpacedHexString(tail));
							if (read<tailLength){
								continue;
							}			
						}else{
							read += is.read(tail, 6+read, tailLength-read);
							if (read<tailLength){
								continue;
							}
						}
						Flap flap = service.getDataParser().parseFlap(tail);
						
						synchronized(packets){
							packets.add(flap);
						}		
						try {
							forceFlapProcess();
						} catch (Exception e) {
							packets.remove(flap);
							service.log(e);
						}
						tail = null;
					} else {
						Thread.sleep(300);
					}				
				} catch (IOException e) {
					service.log(e.getLocalizedMessage());
				} catch (InterruptedException e) {
					service.log(e.getLocalizedMessage());
				} catch (ICQException e) {
					service.log(e.getLocalizedMessage());
				}
			}
			connectState = CONNSTATE_DISCONNECTED;
			service.log("icon service disconnected");
			
			return true;
		}
		
		public boolean sendToSocket(Flap flap){
			
			try {
				if (socket == null){
					throw new IOException("Socket is null");
				}
				OutputStream os = socket.getOutputStream();
				final byte[] out = service.getDataParser().flap2Bytes(flap);
				service.log("icon engine sent "+ProtocolUtils.getSpacedHexString(out));
				synchronized(os){
					os.write(out);
				}
			} catch (IOException e) {
				service.log(e);
				disconnect();
				buffer = flap;
				try {
					requestServiceServerUrl();
				} catch (Exception e1) {
					service.log(e1.toString());
				}
			} catch (ICQException e) {
				service.log(e.getLocalizedMessage());
			}
			return true;
		}
		
		/*public boolean sendMultipleToSocket(Flap[] flaps){
			
			try {
				OutputStream os = socket.getOutputStream();
				byte[] out = service.getDataParser().flaps2Bytes(flaps);
				service.log("icon engine sent "+ProtocolUtils.getSpacedHexString(out));
				synchronized(os){
					os.write(out);
				}
			} catch (IOException e) {
				service.log(e.getLocalizedMessage());
			} catch (ICQException e) {
				service.log(e.getLocalizedMessage());
			}
			return true;
		}*/		
	}

	public void disconnect() {
		connectState = CONNSTATE_DISCONNECTED;	
	}
}
