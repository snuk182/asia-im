package ua.snuk182.asia.services.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.icq.inner.ICQServiceInternal;
import android.content.Context;

public abstract class AccountService {
	protected byte serviceId = -1;	
	
	protected IAccountServiceResponse serviceResponse;
	
	protected Map<String, String> options = new HashMap<String, String>();
	
	public abstract Object request(short action, Object ... args) throws ProtocolException;
	public abstract String getServiceName();
	public abstract Map<String, String> getOptions();

	public void setServiceId(byte serviceId) {
		this.serviceId = serviceId;
	}
	public byte getServiceId() {
		return serviceId;
	}
	
	protected final Context context; 
	
	public static final short STATE_DISCONNECTED = 0;
	public static final short STATE_CONNECTING = 1;
	public static final short STATE_CONNECTED = 4;
	public static final short STATE_DISCONNECTING = 3;
	
	public static final short REQ_NOP = 0;
	public static final short REQ_CONNECT = 1;
	public static final short REQ_DISCONNECT = 2;
	public static final short REQ_SETSTATUS = 3;
	public static final short REQ_SETEXTENDEDSTATUS = 4;
	public static final short REQ_GETSTATUS = 5;
	public static final short REQ_GETEXTENDEDSTATUS = 6;
	public static final short REQ_SENDMESSAGE = 7;
	public static final short REQ_SENDFILE = 8;
	public static final short REQ_GETBUDDYINFO = 9;
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
	
	public static final short REQ_GET_CHAT_ROOMS = 34;
	public static final short REQ_CREATE_CHAT_ROOM = 35;
	public static final short REQ_JOIN_CHAT_ROOM = 36;
	public static final short REQ_LEAVE_CHAT_ROOM = 37;
	public static final short REQ_CHECK_GROUPCHATS_AVAILABLE = 38;
	public static final short REQ_GET_CHAT_ROOM_OCCUPANTS = 39;
	
	public static final String ERR_HOST_NOT_FOUND = "host not found";
	public static final int NOT_IN_LIST_GROUP_ID = -666;
	
	public AccountService(Context context, IAccountServiceResponse serviceResponse, byte serviceId){
		this.context = context;
		this.serviceResponse = serviceResponse;
		this.serviceId = serviceId;
	}

	public void log(String log) {
		ServiceUtils.log(log);
	}
	
	public void log(Throwable e){
		ServiceUtils.log(e);
	}

	public abstract int getProtocolOptionNames();
	public abstract int getProtocolOptionDefaults();
	public abstract int getProtocolOptionStrings();
	
	//=========================================================
	
	public static final String PING_TIMEOUT = "pingtimeout";

	private KeepaliveTimer keepaliveTimer = new KeepaliveTimer();
	
	private ScheduledFuture<?> task;
	
	public ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);

	private Runnable timeoutRunnable = new Runnable(){
		
		@Override
		public void run() {
			if (getCurrentState() == ICQServiceInternal.STATE_CONNECTED
					&& keepaliveTimer.running){
				keepaliveTimer.running = false;
				try {
					log("heartbeat timeout "+getUserID());
					timeoutDisconnect();
				} catch (Exception e1) {
					log(e1);
				}
			}			
		}
		
	};

	public long pingTimeout = 20;
	
	public void resetHeartbeat() {
		log("phew..."+getUserID());
		
		if (task!=null){
			task.cancel(false);
		}	
		if (pingTimeout < 1){
			return;
		}
		keepaliveTimer.running = true;
		task = executor.schedule(keepaliveTimer, 120 , TimeUnit.SECONDS);
	}
	
	protected abstract void timeoutDisconnect(); 
	
	protected abstract short getCurrentState();
	
	public void closeKeepaliveThread() {
		resetHeartbeat();
	}

	private void bugogaAttacks(){
		if (pingTimeout < 1){
			return;
		}
		log("bugoga attacks..."+getCurrentState());
		try {
			if (getCurrentState() == AccountService.STATE_CONNECTED){
				keepaliveRequest();
				task = executor.schedule(timeoutRunnable, pingTimeout , TimeUnit.SECONDS);
				keepaliveTimer.running = true;	
				log("whoops..."+getUserID());
			}
		} catch (Exception e) {
			log(e);
		}
	}
	
	protected abstract void keepaliveRequest();
	
	protected abstract String getUserID();
	
	public void sendKeepalive(){
		if (pingTimeout > 0){
			log("start keepalive "+getUserID());
			resetHeartbeat();
		} else {
			log("skip keepalive "+getUserID());
		}
	}
	
	class KeepaliveTimer extends Thread{
		
		public volatile boolean running = true;
		
		@Override
		public void run(){
			if (running){
				if (task!=null){
					task.cancel(false);
				}
				bugogaAttacks();
			}			
		}		
	}
	
	public void setProtocolResponse(IAccountServiceResponse response) {
		serviceResponse = response;		
	}

	public IAccountServiceResponse getServiceResponse() {
		return serviceResponse;
	}
}
