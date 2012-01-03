package ua.snuk182.asia.services.icq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import ua.snuk182.asia.services.api.ProtocolException;
import ua.snuk182.asia.services.api.ProtocolUtils;
import ua.snuk182.asia.services.icq.inner.ICQConstants;
import ua.snuk182.asia.services.icq.inner.ICQException;
import ua.snuk182.asia.services.icq.inner.ICQServiceInternal;
import ua.snuk182.asia.services.icq.inner.ICQServiceResponse;
import ua.snuk182.asia.services.icq.inner.dataentity.ICBMMessage;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQBuddy;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQBuddyGroup;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQOnlineInfo;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQPersonalInfo;
import ua.snuk182.asia.services.utils.Base64;
import android.content.Context;

public class ICQService extends AccountService {
	
	private static final String PING_TIMEOUT = "pingtimeout";
	private static final String LOGIN_PORT = "loginport";
	private static final String LOGIN_HOST = "loginhost";
	private static final String PASSWORD = "password";
	private static final String UID = "uid";

	private ICQServiceResponse icqResponse = new ICQServiceResponse(){

		@SuppressWarnings("unchecked")
		@Override
		public Object respond(short action, Object... args) {
			
			try{
				switch (action) {
				case ICQServiceResponse.RES_LOG:
					ServiceUtils.log((String)args[0]);
					break;
				case ICQServiceResponse.RES_CONNECTED:
					serviceResponse.respond(IAccountServiceResponse.RES_CONNECTED, getServiceId());
					sendKeepalive();
					break;
				case ICQServiceResponse.RES_DISCONNECTED:
					closeKeepaliveThread();
					System.gc();
					if (args.length > 0){
						return serviceResponse.respond(IAccountServiceResponse.RES_DISCONNECTED, getServiceId(), args[0]);
					} else {
						return serviceResponse.respond(IAccountServiceResponse.RES_DISCONNECTED, getServiceId());
					}
				case ICQServiceResponse.RES_SAVEIMAGEFILE:

					return serviceResponse.respond(IAccountServiceResponse.RES_SAVEIMAGEFILE, getServiceId(), args[0], args[1], Base64.encodeBytes((byte[]) args[2]));
				case ICQServiceResponse.RES_CLUPDATED:
					return serviceResponse.respond(IAccountServiceResponse.RES_CLUPDATED, getServiceId(), ICQEntityAdapter.ICQBuddyList2Buddylist(ICQService.this, (List<ICQBuddy>) args[0], internal.getUn(), getServiceId()), ICQEntityAdapter.ICQBuddyGroupList2BuddyGroupList((List<ICQBuddyGroup>) args[1], internal.getUn(), getServiceId()));
				case ICQServiceResponse.RES_KEEPALIVE:
					resetHeartbeat();
					break;
				case ICQServiceResponse.RES_MESSAGE:
					TextMessage txtmessage = ICQEntityAdapter.icbmMessage2TextMessage((ICBMMessage) args[0], getServiceId());
					
					if (txtmessage.from.equals(getUserID())) {
						resetHeartbeat();
						return null;
					}
					
					txtmessage.to = internal.getUn()+" "+getServiceName();
					return serviceResponse.respond(IAccountServiceResponse.RES_MESSAGE, getServiceId(), txtmessage);
				case ICQServiceResponse.RES_BUDDYSTATECHANGED:	
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYSTATECHANGED, getServiceId(), ICQEntityAdapter.icqOnlineInfo2OnlineInfo((ICQOnlineInfo) args[0]));
				case ICQServiceResponse.RES_CONNECTING:
					return serviceResponse.respond(IAccountServiceResponse.RES_CONNECTING, getServiceId(), args[0]);
					
				case ICQServiceResponse.RES_FILEMESSAGE:
					return serviceResponse.respond(IAccountServiceResponse.RES_FILEMESSAGE, getServiceId(), ICQEntityAdapter.icbmMessage2FileMessage((ICBMMessage)args[0], getServiceId()));
				case ICQServiceResponse.RES_NOTIFICATION:
					if (args.length > 1){
						return serviceResponse.respond(IAccountServiceResponse.RES_NOTIFICATION, getServiceId(), args[0], args[1]);
					} else {
						return serviceResponse.respond(IAccountServiceResponse.RES_NOTIFICATION, getServiceId(), args[0]);
					}
				case ICQServiceResponse.RES_ACCOUNTUPDATED:
					return serviceResponse.respond(IAccountServiceResponse.RES_ACCOUNTUPDATED, getServiceId(), ICQEntityAdapter.icqOnlineInfo2OnlineInfo((ICQOnlineInfo) args[0]));
				case ICQServiceResponse.RES_USERINFO:
					serviceResponse.respond(IAccountServiceResponse.RES_USERINFO, getServiceId(), ICQEntityAdapter.icqPersonalInfo2PersonalInfo((ICQPersonalInfo) args[0], context));
					break;
				case ICQServiceResponse.RES_AUTHREQUEST:
					return serviceResponse.respond(IAccountServiceResponse.RES_AUTHREQUEST, getServiceId(), args[0], args[1]);
				case ICQServiceResponse.RES_SEARCHRESULT:
					return serviceResponse.respond(IAccountServiceResponse.RES_SEARCHRESULT, getServiceId(), ICQEntityAdapter.icqPersonalInfos2PersonalInfos((List<ICQPersonalInfo>) args[0], context));
				case ICQServiceResponse.RES_GROUPADDED:
					return serviceResponse.respond(IAccountServiceResponse.RES_GROUPADDED, getServiceId(), ICQEntityAdapter.ICQBuddyGroup2BuddyGroup((ICQBuddyGroup) args[0], internal.getUn(), getServiceId()));
				case ICQServiceResponse.RES_BUDDYADDED:
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYADDED, getServiceId(), ICQEntityAdapter.ICQBuddy2Buddy(ICQService.this, (ICQBuddy) args[0], internal.getUn(), getServiceId()));
				case ICQServiceResponse.RES_BUDDYDELETED:
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYDELETED, getServiceId(), ICQEntityAdapter.ICQBuddy2Buddy(ICQService.this, (ICQBuddy) args[0], internal.getUn(), getServiceId()));
				case ICQServiceResponse.RES_GROUPDELETED:
					return serviceResponse.respond(IAccountServiceResponse.RES_GROUPDELETED, getServiceId(), ICQEntityAdapter.ICQBuddyGroup2BuddyGroup((ICQBuddyGroup) args[0], internal.getUn(), getServiceId()));
				case ICQServiceResponse.RES_BUDDYMODIFIED:
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYMODIFIED, getServiceId(), ICQEntityAdapter.ICQBuddy2Buddy(ICQService.this, (ICQBuddy) args[0], internal.getUn(), getServiceId()));
				case ICQServiceResponse.RES_GROUPMODIFIED:
					return serviceResponse.respond(IAccountServiceResponse.RES_GROUPMODIFIED, getServiceId(), ICQEntityAdapter.ICQBuddyGroup2BuddyGroup((ICQBuddyGroup) args[0], internal.getUn(), getServiceId()));
				case ICQServiceResponse.RES_FILEPROGRESS:
					return serviceResponse.respond(IAccountServiceResponse.RES_FILEPROGRESS, getServiceId(), ProtocolUtils.bytes2LongBE((byte[]) args[0], 0), args[1], args[2], args[3], args[4], args[5], (args.length > 6)?args[6] : null);
				case ICQServiceResponse.RES_MESSAGEACK:
					return serviceResponse.respond(IAccountServiceResponse.RES_MESSAGEACK, getServiceId(), args[0], args[1], args[2]);
				case ICQServiceResponse.RES_TYPING:
					return serviceResponse.respond(IAccountServiceResponse.RES_TYPING, getServiceId(), args[0]);
				}			
			
			} catch(ProtocolException e){
				ServiceUtils.log(e);
			}
			return null;
		}
		
	};
	
	private ICQServiceInternal internal = new ICQServiceInternal(icqResponse);
	
	public ICQService(Context context, IAccountServiceResponse serviceResponse, byte serviceId) {
		super(context, serviceResponse, serviceId);
		
		options.put(UID, null);
		options.put(PASSWORD, null);
		options.put(LOGIN_HOST, null);
		options.put(LOGIN_PORT, null);
		options.put(PING_TIMEOUT, null);
	}

	public ICQService(Context context) {
		super(context, null, (byte) -1);
	}

	@Override
	public String getServiceName(){
		return context.getResources().getString(R.string.icq_service_name);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object request(short action, Object... args) throws ProtocolException{
		try {
			switch (action) {
			case AccountService.REQ_GETFULLBUDDYINFO:
				internal.request(ICQServiceInternal.REQ_GETFULLBUDDYINFO, args[0]);
				break;
			case AccountService.REQ_GETBUDDYINFO:
				internal.request(ICQServiceInternal.REQ_GETSHORTBUDDYINFO, args[0]);
				break;
			case AccountService.REQ_ADDGROUP:
				internal.request(ICQServiceInternal.REQ_ADDGROUP, ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[0]), ICQEntityAdapter.buddyGroupList2ICQBuddyGroupList((List<BuddyGroup>) args[1]));
				break;
			case AccountService.REQ_ADDBUDDY:
				internal.request(ICQServiceInternal.REQ_ADDBUDDY, ICQEntityAdapter.buddy2ICQBuddy((Buddy) args[0]), ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[1]), false);
				break;
			case AccountService.REQ_REMOVEBUDDY:
				internal.request(ICQServiceInternal.REQ_REMOVEBUDDY, ICQEntityAdapter.buddy2ICQBuddy((Buddy) args[0]));
				break;
			case AccountService.REQ_MOVEBUDDIES:
				internal.request(ICQServiceInternal.REQ_MOVEBUDDIES, ICQEntityAdapter.buddyList2ICQBuddyList((List<Buddy>) args[0]), ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[1]), ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[2]));
				break;
			case AccountService.REQ_REMOVEBUDDIES:
				internal.request(ICQServiceInternal.REQ_REMOVEBUDDIES, ICQEntityAdapter.buddyList2ICQBuddyList((List<Buddy>) args[0]));
				break;
			case AccountService.REQ_SETSTATUS:
				if (internal.getCurrentState() == STATE_CONNECTED) {
					int status = ICQEntityAdapter.userStatus2ICQUserStatus((Byte) args[0]);
					if (status < 0) {
						byte[] qipStatus = ICQEntityAdapter.userQipStatus2ICQQipStatus((Byte) args[0]);
						if (qipStatus != null) {
							internal.request(ICQServiceInternal.REQ_SETSTATUS, ICQConstants.STATUS_ONLINE, (Byte) args[1], qipStatus);
						} else {
							throw new ProtocolException("Wrong status - " + args[0]);
						}
					} else {
						internal.request(ICQServiceInternal.REQ_SETSTATUS, status, (Byte) args[1]);
					}
				}

				break;
			case AccountService.REQ_SETEXTENDEDSTATUS:
				byte[] qipStatus = ICQEntityAdapter.userQipStatus2ICQQipStatus((Byte) args[0]);
				internal.request(ICQServiceInternal.REQ_SETEXTENDEDSTATUS, qipStatus, (Byte) args[1], (String) args[2], (String) args[3]);
				break;
			case AccountService.REQ_AUTHREQUEST:
				internal.request(ICQServiceInternal.REQ_AUTHREQUEST, ICQEntityAdapter.buddy2ICQBuddy((Buddy) args[0]), (String) args[1]);
				break;
			case AccountService.REQ_AUTHRESPONSE:
				internal.request(ICQServiceInternal.REQ_AUTHRESPONSE, (String) args[0], (Boolean) args[1]);
				break;
			case AccountService.REQ_SEARCHFORBUDDY_BY_UID:
				internal.request(ICQServiceInternal.REQ_SEARCHFORBUDDY_BY_UID, (String) args[0]);
				break;
			case AccountService.REQ_DISCONNECT:
				internal.request(ICQServiceInternal.REQ_DISCONNECT);
				break;
			case AccountService.REQ_CONNECT:
				run(args);
				break;
			case AccountService.REQ_GETCONTACTLIST:
				break;
			case AccountService.REQ_GETEXTENDEDSTATUS:
				internal.request(ICQServiceInternal.REQ_GETEXTENDEDSTATUS, (String) args[0]);
				break;
			case AccountService.REQ_RENAMEBUDDY:
				internal.request(ICQServiceInternal.REQ_RENAMEBUDDY, ICQEntityAdapter.buddy2ICQBuddy((Buddy) args[0]));
				break;
			case AccountService.REQ_RENAMEGROUP:
				internal.request(ICQServiceInternal.REQ_RENAMEGROUP, ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[0]));
				break;
			case AccountService.REQ_MOVEBUDDY:
				internal.request(ICQServiceInternal.REQ_MOVEBUDDY, ICQEntityAdapter.buddy2ICQBuddy((Buddy) args[0]), ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[1]), ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[2]));
				break;
			case AccountService.REQ_GETGROUPLIST:
				break;
			case AccountService.REQ_SENDMESSAGE:
				internal.request(ICQServiceInternal.REQ_SENDMESSAGE, ICQEntityAdapter.textMessage2ICBMMessage((TextMessage) args[0]));
				break;
			case AccountService.REQ_GETICON:
				internal.request(ICQServiceInternal.REQ_GETICON, args[0]);
				break;
			case AccountService.REQ_REMOVEGROUP:
				internal.request(ICQServiceInternal.REQ_REMOVEGROUP, ICQEntityAdapter.buddyGroup2ICQBuddyGroup((BuddyGroup) args[0]));
				break;
			case AccountService.REQ_FILERESPOND:
				internal.request(ICQServiceInternal.REQ_FILERESPOND, ((FileMessage)args[0]).messageId, args[1], args[2]);
				break;
			case AccountService.REQ_SENDFILE:
				List<File> files = new ArrayList<File>();
				files.add((File) args[1]);
				
				return ProtocolUtils.bytes2LongBE((byte[]) internal.request(ICQServiceInternal.REQ_SENDFILE, ICQEntityAdapter.buddy2ICQBuddy((Buddy) args[0]), files, args[2]), 0);
			case AccountService.REQ_FILECANCEL:
				internal.request(ICQServiceInternal.REQ_FILECANCEL, args[0]);
				break;
			case AccountService.REQ_SENDTYPING:
				internal.request(ICQServiceInternal.REQ_SENDTYPING, args[0]);
				break;
			case AccountService.REQ_VISIBILITY:
				if (args[0] instanceof Buddy){
					internal.request(ICQServiceInternal.REQ_VISIBILITY, ICQEntityAdapter.buddy2ICQBuddy((Buddy) args[0]));
				} else if (args[0] instanceof Byte){
					internal.request(ICQServiceInternal.REQ_VISIBILITY, args[0]);
				} 
				break;
			}
		} catch (ICQException e) {
			throw new ProtocolException(e.getMessage());
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void run(Object[] args) throws ProtocolException{
		
		Map<String, String> sharedPreferences = (Map<String, String>) serviceResponse.respond(IAccountServiceResponse.RES_GETFROMSTORAGE, getServiceId(), IAccountServiceResponse.SHARED_PREFERENCES, options.keySet());
		if (sharedPreferences==null){
			throw new ProtocolException("Error getting preferences");
		}
		String un = sharedPreferences.get(UID);
		String pw = sharedPreferences.get(PASSWORD);		
		String host = sharedPreferences.get(LOGIN_HOST);
		String port = sharedPreferences.get(LOGIN_PORT);

		String ping = sharedPreferences.get(AccountService.PING_TIMEOUT);
		if (ping != null){
			try {
				pingTimeout = Integer.parseInt(ping);
			} catch (Exception e) {}
		}
		
		if (un==null || pw==null || args.length<4){
			throw new ProtocolException("Error: no auth data");
		}
		
		int status = 0;
		byte[] qipStatus = null;
		status = ICQEntityAdapter.userStatus2ICQUserStatus((Byte) args[0]);
		if (status<0){
			status = ICQConstants.STATUS_ONLINE;
			qipStatus = ICQEntityAdapter.userQipStatus2ICQQipStatus((Byte) args[0]);					
		} else {
			qipStatus = null;
		}
		
		try {
			if (args.length < 5){
				internal.request(ICQServiceInternal.REQ_CONNECT, un, pw, host, port, /*ping, */status, args[1], args[2], args[3], qipStatus);
			} else {
				internal.request(ICQServiceInternal.REQ_CONNECT, un, pw, host, port, /*ping, */status, args[1], args[2], args[3], qipStatus, args[4]);
			}
		} catch (ICQException e) {
			throw new ProtocolException(e.getMessage());
		}		
	}

	public Map<String, String> getOptions() {
		return options;
	}	

	@Override
	public int getProtocolOptionNames() {
		return R.array.icq_preference_names;
	}

	@Override
	public int getProtocolOptionDefaults() {
		return R.array.icq_preference_defaults;
	}

	@Override
	public int getProtocolOptionStrings() {
		return R.array.icq_preference_strings;
	}
	
	

	@Override
	protected void timeoutDisconnect() {
		internal.getRunnableService().disconnect();		
	}

	@Override
	protected short getCurrentState() {
		return internal.getCurrentState();
	}

	@Override
	protected String getUserID() {
		return internal.getUn();
	}

	@Override
	protected void keepaliveRequest() {
		try {
			internal.request(ICQServiceInternal.REQ_KEEPALIVE_CHECK);
		} catch (ICQException e) {
			ServiceUtils.log(e);
		}
	}
}
