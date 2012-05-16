package ua.snuk182.asia.services.icq.inner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import ua.snuk182.asia.services.api.ProtocolUtils;
import ua.snuk182.asia.services.icq.inner.dataentity.Flap;
import ua.snuk182.asia.services.icq.inner.dataentity.ICBMMessage;
import ua.snuk182.asia.services.icq.inner.dataentity.ICBMParams;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQBuddy;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQBuddyGroup;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQBuddyList;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQOnlineInfo;
import ua.snuk182.asia.services.icq.inner.dataentity.RateLimit;
import ua.snuk182.asia.services.icq.inner.dataentity.TLV;
import ua.snuk182.asia.services.icq.inner.dataprocessing.AuthenticationProcessor;
import ua.snuk182.asia.services.icq.inner.dataprocessing.BuddyIconEngine;
import ua.snuk182.asia.services.icq.inner.dataprocessing.FileTransferEngine;
import ua.snuk182.asia.services.icq.inner.dataprocessing.ICBMMessagingEngine;
import ua.snuk182.asia.services.icq.inner.dataprocessing.IFlapProcessor;
import ua.snuk182.asia.services.icq.inner.dataprocessing.MainProcessor;
import ua.snuk182.asia.services.icq.inner.dataprocessing.OnlineInfoEngine;
import ua.snuk182.asia.services.icq.inner.dataprocessing.PersonalInfoEngine;
import ua.snuk182.asia.services.icq.inner.dataprocessing.SSIProcessor;

public class ICQServiceInternal {
	
	
	public static final short STATE_DISCONNECTED = 0;
	public static final short STATE_CONNECTING_LOGIN = 1;
	public static final short STATE_CONNECTING_BOS = 2;
	public static final short STATE_AUTHENTICATING = 3;
	public static final short STATE_CONNECTED = 4;
	
	public static final short REQ_NOP = 0;
	public static final short REQ_CONNECT = 1;
	public static final short REQ_DISCONNECT = 2;
	public static final short REQ_SETSTATUS = 3;
	public static final short REQ_SETEXTENDEDSTATUS = 4;
	public static final short REQ_GETSTATUS = 5;
	public static final short REQ_GETEXTENDEDSTATUS = 6;
	public static final short REQ_SENDMESSAGE = 7;
	public static final short REQ_SENDFILE = 8;
	public static final short REQ_GETSHORTBUDDYINFO = 9;
	public static final short REQ_GETOWNINFO = 10;
	public static final short REQ_SETOWNINFO = 11;
	public static final short REQ_ADDBUDDY = 12;
	public static final short REQ_REMOVEBUDDY = 13;
	public static final short REQ_EDITCONTACTLIST = 14;
	public static final short REQ_SEARCHFORBUDDY_BY_UID = 15;
	public static final short REQ_SAVEPARAMS = 16;
	public static final short REQ_GETCONTACTLIST = 17;
	public static final short REQ_GETGROUPLIST = 18;
	public static final short REQ_GETICON = 19;
	public static final short REQ_AUTHREQUEST = 20;
	public static final short REQ_AUTHRESPONSE = 21;
	public static final short REQ_RENAMEBUDDY = 22;
	public static final short REQ_MOVEBUDDY = 23;
	public static final short REQ_RENAMEGROUP = 24;
	public static final short REQ_ADDGROUP = 25;
	public static final short REQ_REMOVEGROUP = 26;
	public static final short REQ_MOVEBUDDIES = 27;
	public static final short REQ_REMOVEBUDDIES = 28;
	public static final short REQ_FILERESPOND = 29;
	public static final short REQ_FILECANCEL = 30;
	public static final short REQ_GETFULLBUDDYINFO = 31;
	public static final short REQ_SENDTYPING = 32;
	public static final short REQ_VISIBILITY = 33;
	
	public static final short REQ_KEEPALIVE_CHECK = 34;	

	private String loginHost = "login.icq.com";
	private int loginPort = 800;
	//private int pingTimeout = 10;// seconds
	
	private String un;
	private String pw;
	private byte[] internalIp = new byte[]{0,0,0,0};
	
	private ICQServiceResponse serviceResponse;
	
	private ICQRunnableService runnableService;
	private final List<Flap> packets = Collections.synchronizedList(new ArrayList<Flap>());
	private ICQDataParser dataParser = new ICQDataParser();
	private IFlapProcessor processor = null;
	private ICBMMessagingEngine messagingEngine = new ICBMMessagingEngine(this);
	private OnlineInfoEngine onlineInfoEngine = new OnlineInfoEngine(this);
	private BuddyIconEngine buddyIconEngine = new BuddyIconEngine(this);
	private PersonalInfoEngine personalInfoEngine = null;
	private FileTransferEngine fileTransferEngine = null;
	private SSIProcessor ssiEngine = null;
	
	private final AtomicInteger intCounter = new AtomicInteger();
	
	private final ICQBuddyList buddyList = new ICQBuddyList();
	
	private ICQOnlineInfo onlineInfo;
	private ICBMParams messageParams;
	private int maxVisibleListLength;
	private int maxInvisibleListLength;
	private TLV[] ssiLimits;
	private short[] serverSupportedFamilies = null;
	private RateLimit[] rateLimits = null;
	private short currentState = STATE_DISCONNECTED;
	public String lastConnectionError = null;
	
	public ICQServiceInternal(ICQServiceResponse icqResponse) {
		serviceResponse = icqResponse;
	}

	public void startMainProcessor() throws ICQException{
		processor = new MainProcessor();
		try {
			processor.init(this);
		} catch (Exception e) {
			log(e);
			throw new ICQException("Error starting main processor");
		}
	}
	
	public void runService(String host, int port){
		if (runnableService!=null){
			runnableService.connected = false;
		}
		runnableService = new ICQRunnableService(host, port);
		runnableService.start();
	}
	
	protected void forceFlapProcess() throws Exception{
		while(packets.size()>0){
			synchronized (packets) {
				Flap flap = packets.remove(0);
				processor.process(flap);
			}
		}
	}
	
	public class ICQRunnableService extends Thread{
		private Socket socket = new Socket();
		private String host;
		private int port;
		
		private volatile boolean connected = true;
		private short flapSeqNumber = 0;
		
		public void setFlapSeqNumber(short number){
			this.flapSeqNumber = number;
		}
		
		public short getFlapSeqNumber() {
			if (flapSeqNumber >=0x8000){
				flapSeqNumber = 0;
			};
			return flapSeqNumber++;
		}

		public ICQRunnableService(String host, int port){
			this.host = host;
			this.port = port;
			setName("ICQ runnable "+un);
		}

		@Override
		public void run() {
			flapSeqNumber = ProtocolUtils.getAtomicShort();
			try {
				//socket = new Socket();
				//socket.setSoTimeout(300000);
				socket.connect(new InetSocketAddress(InetAddress.getByName(host), port));
				connected = true;
				getDataFromSocket();
			} catch (UnknownHostException e) {
				serviceResponse.respond(ICQServiceResponse.RES_NOTIFICATION, "host not found!");
				new Timer().schedule(new ErrorTimer(), 5000);				
				log(e);
			} catch (IOException e) {
				serviceResponse.respond(ICQServiceResponse.RES_NOTIFICATION, "connection error!");
				new Timer().schedule(new ErrorTimer(), 5000);				
				log(e);
			} 
		}
		
		private void getDataFromSocket(){
			byte[] tail = null;
			int read = 0;
			int tailLength = 0;
			
			while (connected && socket!=null && socket.isConnected() && !socket.isClosed()){
				try {
					InputStream is = socket.getInputStream();
					if (is.available()>0){
						Thread.sleep(200);
					
						byte[] head = new byte[6];
						
						is.read(head, 0, 6);
						
						tailLength = ProtocolUtils.unsignedShort2Int(ProtocolUtils.bytes2ShortBE(head, 4));					
					
						tail = new byte[6+tailLength];
						System.arraycopy(head, 0, tail, 0, 6);
						read = 0;
						while (read < tailLength){
							read += is.read(tail, 6+read, tailLength-read);
						}
						log("Got "+ProtocolUtils.getSpacedHexString(tail));
						
						try {
							Flap flap = dataParser.parseFlap(tail);
							
							synchronized(packets){
								packets.add(flap);
							}
						} catch (Exception e) {
							log(e);
						}		
						new Thread("ICQ packet processor"){
							@Override
							public void run(){
								try {
									forceFlapProcess();
								} catch (Exception e) {
									log(e);
								}
							}
						}.start();
						tail = null;
					} else {
						Thread.sleep(1000);
					}				
				}catch(IOException e){
					log(e);
					new Thread("icq disconnection"){
						@Override
						public void run(){
							disconnect();	
						}
					}.start();			
				}catch (Exception e) {
					log(e);
				} 
			}
			log("disconnected");			
			connected = false;						
		}
		
		public synchronized boolean sendToSocket(Flap flap){
			try {
				if (flap == null){
					throw new ICQException("Flap to send is Null!");
				}
				
				OutputStream os = socket.getOutputStream();
				flap.sequenceNumber = getFlapSeqNumber();
				byte[] out = dataParser.flap2Bytes(flap);
				
				if (flap.channel!=1){
					log("To be sent "+ProtocolUtils.getSpacedHexString(out));
				} else {
					log("smth secret to be sent");
				}
				
				os.write(out);
				
				//checkForKeepaliveTimer();
			} catch (NullPointerException e) {
				log(e);
				return false;
			} catch (IOException e) {
				log(e);
				disconnect();
				return false;
			} catch (ICQException e) {
				log(e);
				return false;
			} 
			return true;
		}
		
		public synchronized boolean sendMultipleToSocket(Flap[] flaps){
			
			try {
				OutputStream os = socket.getOutputStream();
				for (Flap p:flaps){
					if (p == null){
						continue;
					}
					p.sequenceNumber = getFlapSeqNumber();
				}
				byte[] out = dataParser.flaps2Bytes(flaps);
				log("To be sent "+ProtocolUtils.getSpacedHexString(out));
				
				os.write(out);
				
				//checkForKeepaliveTimer();
			} catch (IOException e) {
				connected = false;
				log(e);
			} catch (ICQException e) {
				log(e);
			}
			return true;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public void disconnect(){
			log("attempt disconnect "+currentState+ ((lastConnectionError!= null) ? lastConnectionError : ""));
			//closeKeepaliveTimerThread();
			if (processor != null){
				processor.onDisconnect();
			}
			if (socket!=null && !socket.isClosed()){
				try {
					socket.close();
					connected = false;
				} catch (IOException e) {
					log(e);
				}
			}
			if (fileTransferEngine != null){
				fileTransferEngine.cancelAll();
			}
			if (currentState != STATE_CONNECTING_BOS){
				if (lastConnectionError != null){
					serviceResponse.respond(ICQServiceResponse.RES_DISCONNECTED, lastConnectionError);
					lastConnectionError = null;
				} else {
					serviceResponse.respond(ICQServiceResponse.RES_DISCONNECTED);
				}
				setCurrentState(STATE_DISCONNECTED);
			}
		}	
		
		class ErrorTimer extends TimerTask{

			@Override
			public void run() {
				disconnect();			
			}		
		}
	}

	public ICQRunnableService getRunnableService() {
		return runnableService;
	}

	public void log(String string) {
		serviceResponse.respond(ICQServiceResponse.RES_LOG, string);				
	}
	
	public void log(Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.toString());
		for (StackTraceElement el:e.getStackTrace()){
			sb.append("\n"+el);
		}
		log(sb.toString());		
	}	

	public void setServerSupportedFamilies(short[] serverSupportedFamilies) {
		this.serverSupportedFamilies = serverSupportedFamilies;
	}

	public short[] getServerSupportedFamilies() {
		return serverSupportedFamilies;
	}

	public void setRunnableService(ICQRunnableService runnableService) {
		this.runnableService = runnableService;
	}

	public void setRateLimits(RateLimit[] rateLimits) {
		this.rateLimits = rateLimits;
	}

	public RateLimit[] getRateLimits() {
		return rateLimits;
	}

	public ICQDataParser getDataParser() {
		return dataParser;
	}

	public void setDataParser(ICQDataParser dataParser) {
		this.dataParser = dataParser;
	}

	public void setOnlineInfo(ICQOnlineInfo onlineInfo) {
		this.onlineInfo = onlineInfo;
	}

	public ICQOnlineInfo getOnlineInfo() {
		return onlineInfo;
	}

	public void setMessageParams(ICBMParams messageParams) {
		this.messageParams = messageParams;
	}

	public ICBMParams getMessageParams() {
		return messageParams;
	}

	public void setMaxVisibleListLength(int maxVisibleListLength) {
		this.maxVisibleListLength = maxVisibleListLength;
	}

	public int getMaxVisibleListLength() {
		return maxVisibleListLength;
	}

	public void setMaxInvisibleListLength(int maxInvisibleListLength) {
		this.maxInvisibleListLength = maxInvisibleListLength;
	}

	public int getMaxInvisibleListLength() {
		return maxInvisibleListLength;
	}

	public void setSSILimits(TLV[] ssiLimits) {
		this.ssiLimits = ssiLimits;
	}

	public TLV[] getSSILimits() {
		return ssiLimits;
	}

	public void setBuddyList(List<ICQBuddy> buddies, List<ICQBuddyGroup> buddyGroups, Map<String, Short> permitList, Map<String, Short> denyList, List<ICQBuddy> notAuthList) {
		//List<Buddy> buddiez = ICQEntityAdapter.ICQBuddyList2Buddylist(buddies, un+" "+getServiceName(), getServiceId());
		//List<BuddyGroup> groups = ICQEntityAdapter.ICQBuddyGroupList2BuddyGroupList(buddyGroups, un, getServiceId());
		buddyList.buddyGroupList.clear();
		for (ICQBuddyGroup group:buddyGroups){
			buddyList.buddyGroupList.add(group.groupId);
		}
		buddyList.permitList = permitList;
		buddyList.denyList = denyList;
		buddyList.notAuthList = notAuthList;
		serviceResponse.respond(ICQServiceResponse.RES_CLUPDATED, buddies, buddyGroups);
	}
	
	public ICQBuddyList getBuddyList() {
		return buddyList;
	}

	public PersonalInfoEngine getPersonalInfoEngine() {
		if (personalInfoEngine == null){
			personalInfoEngine = new PersonalInfoEngine(this);
		}
		return personalInfoEngine;		
	}
	
	public boolean checkFileTransferEngineCreated(){
		return fileTransferEngine != null;
	}
	
	public FileTransferEngine getFileTransferEngine() {
		if (fileTransferEngine == null){
			fileTransferEngine = new FileTransferEngine(this);
		}
		return fileTransferEngine;		
	}

	public AtomicInteger getIntCounter() {
		return intCounter;
	}

	public SSIProcessor getSSIEngine() {
		if (ssiEngine == null){
			ssiEngine = new SSIProcessor(this);
		}
		return ssiEngine;
	}

	
	public void setMessagingEngine(ICBMMessagingEngine messagingEngine) {
		this.messagingEngine = messagingEngine;
	}

	public ICBMMessagingEngine getMessagingEngine() {
		if (messagingEngine==null){
			messagingEngine = new ICBMMessagingEngine(this);
		}
		return messagingEngine;
	}

	public void setOnlineInfoEngine(OnlineInfoEngine onlineInfoEngine) {
		this.onlineInfoEngine = onlineInfoEngine;
	}

	public OnlineInfoEngine getOnlineInfoEngine() {
		if (onlineInfoEngine==null){
			onlineInfoEngine = new OnlineInfoEngine(this);
		}
		return onlineInfoEngine;
	}

	public void setBuddyIconEngine(BuddyIconEngine buddyIconEngine) {
		this.buddyIconEngine = buddyIconEngine;
	}

	public BuddyIconEngine getBuddyIconEngine() {
		if (buddyIconEngine == null){
			buddyIconEngine = new BuddyIconEngine(this);
		}
		return buddyIconEngine;
	}
	
	
	@SuppressWarnings("unchecked")
	public Object request(short action, final Object... args) throws ICQException{
		switch(action){
		case REQ_KEEPALIVE_CHECK:
			if (getCurrentState() == STATE_CONNECTED){
				((MainProcessor)processor).checkServerConnection();
			}
			break;
		case REQ_GETSHORTBUDDYINFO:
			if (getCurrentState() == STATE_CONNECTED && args.length>0){
				getPersonalInfoEngine().getShortPersonalMetainfo((String) args[0]);
			}
			break;
		case REQ_GETFULLBUDDYINFO:
			if (getCurrentState() == STATE_CONNECTED && args.length>0){
				try {
					getPersonalInfoEngine().getFullPersonalMetainfo((String) args[0]);
				} catch (Exception e) {
					serviceResponse.respond(ICQServiceResponse.RES_NOTIFICATION, "Error getting info");
				}
			}
			break;
		case REQ_ADDGROUP:
			if (getCurrentState() == STATE_CONNECTED && args.length>0){
				ICQBuddyGroup group = (ICQBuddyGroup) args[0];
				List<ICQBuddyGroup> existingGroups = (List<ICQBuddyGroup>) args[1];
				short id = 0;
				do {
					id = (short) new Random().nextInt(0x7fff);
				} while (ProtocolUtils.getBuddyGroupByGroupId(id, existingGroups) != null);
				group.groupId = id;
				
				getSSIEngine().addGroup((ICQBuddyGroup) args[0]);
			}
			break;
		case REQ_ADDBUDDY:
			if (getCurrentState() == STATE_CONNECTED && args.length>0){
				getSSIEngine().addBuddyToContactList((ICQBuddy) args[0], (ICQBuddyGroup) args[1], false);
			}
			break;
		case REQ_REMOVEBUDDY:
			if (getCurrentState() == STATE_CONNECTED && args.length>0){
				getSSIEngine().removeBuddyFromContactList((ICQBuddy) args[0]);
			}
			break;
		case REQ_MOVEBUDDIES:
			if (getCurrentState() == STATE_CONNECTED && args.length>0){
				getSSIEngine().moveBuddies((List<ICQBuddy>) args[0], (ICQBuddyGroup)args[1], (ICQBuddyGroup)args[2]);
			}
			break;
		case REQ_REMOVEBUDDIES:
			if (getCurrentState() == STATE_CONNECTED && args.length>0){
				getSSIEngine().removeBuddies((List<ICQBuddy>) args[0]);
			}
			break;
		case REQ_SETSTATUS:
			if (getCurrentState() == STATE_CONNECTED){
				int status = (Integer) args[0];
				if (args.length<3){
					onlineInfo.userStatus = status;
					((MainProcessor)processor).sendXStatusChange(null, (Byte) args[1], onlineInfo.personalText, onlineInfo.extendedStatus);
				} else {
					byte[] qipStatus = (byte[]) args[2];
					if (qipStatus!=null){
						((MainProcessor)processor).sendXStatusChange(qipStatus, (Byte) args[1], onlineInfo.personalText, onlineInfo.extendedStatus);
					} 
				}
			}
			break;
		case REQ_SETEXTENDEDSTATUS:
			if (getCurrentState() == STATE_CONNECTED){
				byte[] qipStatus = (byte[]) args[0];
				((MainProcessor)processor).sendXStatusChange(qipStatus, (Byte) args[1], (String)args[2], (String)args[3]);
			}
			break;
		case REQ_AUTHREQUEST:
			if (getCurrentState() != STATE_CONNECTED){
				throw new ICQException("Enter the network first");
			}
			getSSIEngine().sendAuthorizationRequest((ICQBuddy) args[0], (String)args[1]);
			break;
		case REQ_AUTHRESPONSE:
			if (getCurrentState() != STATE_CONNECTED){
				throw new ICQException("Enter the network first");
			}
			getSSIEngine().sendAuthorizationReply((String)args[0], (Boolean)args[1]);
			break;
		case REQ_SEARCHFORBUDDY_BY_UID:
			if (getCurrentState() != STATE_CONNECTED){
				throw new ICQException("Enter the network first");
			}
			getPersonalInfoEngine().sendSearchByUinRequest((String)args[0]);
			break;
		case REQ_DISCONNECT:
			log("disconnect direct request");
			if (buddyIconEngine!=null){
				buddyIconEngine.disconnect();
			}
			if (runnableService != null){
				runnableService.disconnect();
			}
			break;
		case REQ_CONNECT:
			un = (String) args[0];
			pw = (String) args[1];
			
			if (args[2] !=null){
				loginHost = (String) args[2];
			}
			
			if (args[3] != null){
				loginPort = Integer.parseInt(((String) args[3]).trim().replace("\n", ""));
			}
			
			/*Set<String> nameSet = new HashSet<String>();
			nameSet.add(ICQConstants.SAVEDPREFERENCES_SSI_ITEM_COUNT);
			nameSet.add(ICQConstants.SAVEDPREFERENCES_SSI_UPDATE_DATE);
			
			Map<String, String> privatePreferences = (Map<String, String>) serviceResponse.respond(serviceResponse.RES_GETFROMSTORAGE, ICQConstants.SAVEDPREFERENCES_NAME, nameSet);
			String ssiItemCount = privatePreferences.get(ICQConstants.SAVEDPREFERENCES_SSI_ITEM_COUNT);
			String ssiLastUpdate = privatePreferences.get(ICQConstants.SAVEDPREFERENCES_SSI_UPDATE_DATE);
			if (ssiItemCount!=null && ssiLastUpdate!=null){
				buddyList.itemNumber = Integer.parseInt(ssiItemCount);
				buddyList.lastUpdateTime = new Date(Long.parseLong(ssiLastUpdate));
			}*/			
			
			onlineInfo = new ICQOnlineInfo();
			
			onlineInfo.userStatus = (Integer) args[4];
			onlineInfo.qipStatus = (byte[]) args[8];
			onlineInfo.extendedStatusId = (Byte) args[5];
			onlineInfo.personalText = (String) args[6];
			onlineInfo.extendedStatus = (String) args[7];
			
			processor = new AuthenticationProcessor();
			processor.init(this);
			((AuthenticationProcessor)processor).isSecureLogin = args.length > 9;
			
			setCurrentState(STATE_CONNECTING_LOGIN);
			serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, 1);
			runService(loginHost, loginPort);			
			break;
		case REQ_GETCONTACTLIST:
			break;	
		case REQ_GETEXTENDEDSTATUS:
			getMessagingEngine().askForXStatus((String) args[0]);
			break;
		case REQ_RENAMEBUDDY:
			getSSIEngine().modifyBuddy((ICQBuddy)args[0]);
			break;
		case REQ_RENAMEGROUP:
			getSSIEngine().modifyGroup((ICQBuddyGroup) args[0]);
			break;
		case REQ_MOVEBUDDY:
			getSSIEngine().moveBuddy((ICQBuddy)args[0], (ICQBuddyGroup) args[1], (ICQBuddyGroup) args[2]);
			break;
		case REQ_GETGROUPLIST:
			break;
		case REQ_SENDMESSAGE:
			if (getCurrentState() == STATE_CONNECTED){
				try {
					getMessagingEngine().sendMessage((ICBMMessage) args[0]);
				} catch (Exception e) {
					serviceResponse.respond(ICQServiceResponse.RES_NOTIFICATION, "Error sending message");
				}
			} else {
				throw new ICQException("You should enter the network first");
			}
			break;
		case REQ_GETICON:
			if (args[0].equals(un)){
				try {
					getBuddyIconEngine().requestIcon(onlineInfo);
				} catch (Exception e) {
					log(e);
				}
				break;
			}
			
			synchronized (buddyList.buddyInfos) {
				for (ICQOnlineInfo buddy : buddyList.buddyInfos) {
					if (buddy.uin.equals(args[0])) {
						if (buddy.iconData != null) {
							try {
								getBuddyIconEngine().requestIcon(buddy);
							} catch (Exception e) {
								log(e);
							}
						}
						break;
					}
				}
			}
			break;
		case REQ_REMOVEGROUP:
			getSSIEngine().removeGroup((ICQBuddyGroup) args[0]);
			break;
		case REQ_FILERESPOND:
			if (args.length > 2){
				internalIp = (byte[]) args[2];
			} else {
				internalIp = new byte[]{127,0,0,1};
			}
			
			new Thread("File accept response"){
				@Override
				public void run(){
					getFileTransferEngine().fileReceiveResponse((Long)args[0], (Boolean)args[1]);
				}
			}.start();
			break;
		case REQ_SENDFILE:
			final ICQBuddy buddy = (ICQBuddy) args[0];
			final List<File> files = (List<File>) args[1];
			
			if (args.length > 2){
				internalIp = (byte[]) args[2];
			} else {
				internalIp = new byte[]{127,0,0,1};
			}
			
			return getFileTransferEngine().sendFiles(buddy, files, getInternalIp());
		case REQ_FILECANCEL:
			getFileTransferEngine().cancel((Long)args[0]);
			break;
		case REQ_SENDTYPING:
			getMessagingEngine().sendTyping((String) args[0]);
			break;
		case REQ_VISIBILITY:
			if (args[0] instanceof ICQBuddy){
				getSSIEngine().modifyVisibility((ICQBuddy)args[0]);
			} else if (args[0] instanceof Byte){
				byte vis = (Byte) args[0];
				onlineInfo.visibility = vis;
				getSSIEngine().modifyMyVisibility(onlineInfo);
			}
			break;
		}
		return null;
	}

	public ICQServiceResponse getServiceResponse() {
		return serviceResponse;
	}

	public String getLoginHost() {
		return loginHost;
	}

	public void setLoginHost(String loginHost) {
		this.loginHost = loginHost;
	}

	public int getLoginPort() {
		return loginPort;
	}

	public void setLoginPort(int loginPort) {
		this.loginPort = loginPort;
	}

	/*public int getPingTimeout() {
		return pingTimeout;
	}

	public void setPingTimeout(int pingTimeout) {
		this.pingTimeout = pingTimeout;
	}*/

	public String getUn() {
		return un;
	}

	public void setUn(String un) {
		this.un = un;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public void setCurrentState(short currentState) {
		this.currentState = currentState;
	}

	public short getCurrentState() {
		return currentState;
	}

	public IFlapProcessor getProcessor() {
		return processor;
	}

	public byte[] getInternalIp() {
		if (internalIp == null){
			try {
				internalIp = InetAddress.getLocalHost().getAddress();
			} catch (UnknownHostException e) {
				log(e);
				internalIp = new byte[]{0,0,0,0};
			}
		}
		return internalIp;
	}
}
