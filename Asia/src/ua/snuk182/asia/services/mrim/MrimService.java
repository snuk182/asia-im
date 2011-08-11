package ua.snuk182.asia.services.mrim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import ua.snuk182.asia.services.api.ProtocolException;
import ua.snuk182.asia.services.api.ProtocolUtils;
import ua.snuk182.asia.services.mrim.inner.MrimConstants;
import ua.snuk182.asia.services.mrim.inner.MrimException;
import ua.snuk182.asia.services.mrim.inner.MrimServiceInternal;
import ua.snuk182.asia.services.mrim.inner.MrimServiceResponse;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimBuddy;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimGroup;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimMessage;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimOnlineInfo;
import android.content.Context;
import android.content.res.TypedArray;

public class MrimService extends AccountService {
	
	public static final String SERVICE_NAME = "Mail.Ru";
	private static final String LOGIN_PORT = "loginport";
	private static final String LOGIN_HOST = "loginhost";
	private static final String PASSWORD = "password";
	private static final String MRID = "mrid";
	
	@Override
	public Object request(short action, Object... args) throws ProtocolException {
		try {
			switch (action) {
			case AccountService.REQ_GETFULLBUDDYINFO:
				internal.request(MrimServiceInternal.REQ_GETFULLBUDDYINFO, args[0]);
				break;
			case AccountService.REQ_GETBUDDYINFO:
				internal.request(MrimServiceInternal.REQ_GETSHORTBUDDYINFO, args[0]);
				break;
			/*case AccountService.REQ_ADDGROUP:
				internal.request(MrimServiceInternal.REQ_ADDGROUP, MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[0]), MrimEntityAdapter.buddyGroupList2MrimBuddyGroupList((List<BuddyGroup>) args[1]));
				break;
			case AccountService.REQ_ADDBUDDY:
				internal.request(MrimServiceInternal.REQ_ADDBUDDY, MrimEntityAdapter.buddy2MrimBuddy((Buddy) args[0]), MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[1]), false);
				break;
			case AccountService.REQ_REMOVEBUDDY:
				internal.request(MrimServiceInternal.REQ_REMOVEBUDDY, MrimEntityAdapter.buddy2MrimBuddy((Buddy) args[0]));
				break;
			case AccountService.REQ_MOVEBUDDIES:
				internal.request(MrimServiceInternal.REQ_MOVEBUDDIES, MrimEntityAdapter.buddyList2MrimBuddyList((List<Buddy>) args[0]), MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[1]), MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[2]));
				break;
			case AccountService.REQ_REMOVEBUDDIES:
				internal.request(MrimServiceInternal.REQ_REMOVEBUDDIES, MrimEntityAdapter.buddyList2MrimBuddyList((List<Buddy>) args[0]));
				break;*/
			case AccountService.REQ_SETEXTENDEDSTATUS:
			case AccountService.REQ_SETSTATUS:
				boolean isChat = Buddy.ST_FREE4CHAT == (Byte)args[0];
				int status = (isChat || ((Byte)args[1] > -1)) ? MrimConstants.STATUS_OTHER : MrimEntityAdapter.userStatus2MrimUserStatus((Byte) args[0]);
				String xstatus = isChat ? "STATUS_CHAT" : MrimEntityAdapter.userXStatus2MrimXStatus((Byte) args[1]);
				internal.request(MrimServiceInternal.REQ_SETSTATUS, status, xstatus, args[2], args[3]);
				break;
			/*case AccountService.REQ_AUTHREQUEST:
				internal.request(MrimServiceInternal.REQ_AUTHREQUEST, MrimEntityAdapter.buddy2MrimBuddy((Buddy) args[0]), (String) args[1]);
				break;*/
			case AccountService.REQ_AUTHRESPONSE:
				internal.request(MrimServiceInternal.REQ_AUTHRESPONSE, (String) args[0], (Boolean) args[1]);
				break;
			case AccountService.REQ_SEARCHFORBUDDY_BY_UID:
				internal.request(MrimServiceInternal.REQ_SEARCHFORBUDDY_BY_UID, (String) args[0]);
				break;
			case AccountService.REQ_DISCONNECT:
				internal.request(MrimServiceInternal.REQ_DISCONNECT);
				break;
			case AccountService.REQ_CONNECT:
				run(args);
				break;
			case AccountService.REQ_GETCONTACTLIST:
				break;
			case AccountService.REQ_GETEXTENDEDSTATUS:
				internal.request(MrimServiceInternal.REQ_GETEXTENDEDSTATUS, (String) args[0]);
				break;
			/*case AccountService.REQ_RENAMEBUDDY:
				internal.request(MrimServiceInternal.REQ_RENAMEBUDDY, MrimEntityAdapter.buddy2MrimBuddy((Buddy) args[0]));
				break;
			case AccountService.REQ_RENAMEGROUP:
				internal.request(MrimServiceInternal.REQ_RENAMEGROUP, MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[0]));
				break;
			case AccountService.REQ_MOVEBUDDY:
				internal.request(MrimServiceInternal.REQ_MOVEBUDDY, MrimEntityAdapter.buddy2MrimBuddy((Buddy) args[0]), MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[1]), MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[2]));
				break;*/
			case AccountService.REQ_GETGROUPLIST:
				break;
			case AccountService.REQ_SENDMESSAGE:
				if (internal.getCurrentState() == MrimServiceInternal.STATE_CONNECTED){
					internal.request(MrimServiceInternal.REQ_SENDMESSAGE, MrimEntityAdapter.textMessage2MrimMessage((TextMessage) args[0]));
				}
				break;
			case AccountService.REQ_GETICON:
				internal.request(MrimServiceInternal.REQ_GETICON, args[0]);
				break;
			/*case AccountService.REQ_REMOVEGROUP:
				internal.request(MrimServiceInternal.REQ_REMOVEGROUP, MrimEntityAdapter.buddyGroup2MrimBuddyGroup((BuddyGroup) args[0]));
				break;
			case AccountService.REQ_FILERESPOND:
				internal.request(MrimServiceInternal.REQ_FILERESPOND, ((FileMessage)args[0]).messageId, args[1], args[2]);
				break;*/
			case AccountService.REQ_SENDFILE:
				List<File> files = new ArrayList<File>();
				files.add((File) args[1]);
				
				//return ProtocolUtils.bytes2LongBE((byte[]) internal.request(MrimServiceInternal.REQ_SENDFILE, MrimEntityAdapter.buddy2MrimBuddy((Buddy) args[0]), files, args[2]), 0);
			case AccountService.REQ_FILECANCEL:
				internal.request(MrimServiceInternal.REQ_FILECANCEL, args[0]);
				break;
			case AccountService.REQ_SENDTYPING:
				internal.request(MrimServiceInternal.REQ_SENDTYPING, args[0]);
				break;
			}
		} catch (MrimException e) {
			throw new ProtocolException(e.getMessage());
		}
		return null;
		
	}

	private MrimServiceResponse mrimResponse = new MrimServiceResponse(){

		@SuppressWarnings("unchecked")
		@Override
		public Object respond(short action, Object... args) {
			try{
				switch (action) {
				case MrimServiceResponse.RES_LOG:
					return serviceResponse.respond(IAccountServiceResponse.RES_LOG, getServiceId(), args[0]);
				case MrimServiceResponse.RES_CONNECTED:
					serviceResponse.respond(IAccountServiceResponse.RES_CONNECTED, getServiceId());
					sendKeepalive();
					break;
				case MrimServiceResponse.RES_DISCONNECTED:
					closeKeepaliveThread();
					System.gc();
					if (args.length > 0){
						return serviceResponse.respond(IAccountServiceResponse.RES_DISCONNECTED, getServiceId(), args[0]);
					} else {
						return serviceResponse.respond(IAccountServiceResponse.RES_DISCONNECTED, getServiceId());
					}
				case MrimServiceResponse.RES_SAVEIMAGEFILE:
					return serviceResponse.respond(IAccountServiceResponse.RES_SAVEIMAGEFILE, getServiceId(), args[0], args[1], args[2]);
				case MrimServiceResponse.RES_CLUPDATED:
					List<MrimBuddy> mrimBuddies = (List<MrimBuddy>) args[0];
					return serviceResponse.respond(IAccountServiceResponse.RES_CLUPDATED, getServiceId(), MrimEntityAdapter.mrimBuddyList2Buddylist(MrimService.this, mrimBuddies, internal.getMrid(), getServiceId()), MrimEntityAdapter.mrimBuddyGroupList2BuddyGroupList((List<MrimGroup>) args[1], internal.getMrid(), getServiceId(), mrimBuddies));
				case MrimServiceResponse.RES_MESSAGE:
					TextMessage txtmessage = MrimEntityAdapter.mrimMessage2TextMessage((MrimMessage) args[0], getServiceId());
					
					if (txtmessage.from.equals(getUserID())) {
						resetHeartbeat();
						return null;
					}
					
					txtmessage.to = internal.getMrid()+" "+getServiceName();
					return serviceResponse.respond(IAccountServiceResponse.RES_MESSAGE, getServiceId(), txtmessage);
				case MrimServiceResponse.RES_BUDDYSTATECHANGED:	
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYSTATECHANGED, getServiceId(), MrimEntityAdapter.mrimOnlineInfo2OnlineInfo((MrimOnlineInfo) args[0]));
				case MrimServiceResponse.RES_CONNECTING:
					return serviceResponse.respond(IAccountServiceResponse.RES_CONNECTING, getServiceId(), args[0]);
					
				case MrimServiceResponse.RES_FILEMESSAGE:
					//return serviceResponse.respond(IAccountServiceResponse.RES_FILEMESSAGE, getServiceId(), MrimEntityAdapter.icbmMessage2FileMessage((ICBMMessage)args[0], getServiceId()));
				case MrimServiceResponse.RES_NOTIFICATION:
					if (args.length > 1){
						return serviceResponse.respond(IAccountServiceResponse.RES_NOTIFICATION, getServiceId(), args[0], args[1]);
					} else {
						return serviceResponse.respond(IAccountServiceResponse.RES_NOTIFICATION, getServiceId(), args[0]);
					}
				case MrimServiceResponse.RES_ACCOUNTUPDATED:
					//return serviceResponse.respond(IAccountServiceResponse.RES_ACCOUNTUPDATED, getServiceId(), MrimEntityAdapter.icqOnlineInfo2OnlineInfo((MrimOnlineInfo) args[0]));
				case MrimServiceResponse.RES_USERINFO:
					//return serviceResponse.respond(IAccountServiceResponse.RES_USERINFO, getServiceId(), MrimEntityAdapter.icqPersonalInfo2PersonalInfo((MrimPersonalInfo) args[0], context));
				case MrimServiceResponse.RES_AUTHREQUEST:
					return serviceResponse.respond(IAccountServiceResponse.RES_AUTHREQUEST, getServiceId(), args[0], args[1]);
				/*case MrimServiceResponse.RES_SEARCHRESULT:
					return serviceResponse.respond(IAccountServiceResponse.RES_SEARCHRESULT, getServiceId(), MrimEntityAdapter.icqPersonalInfos2PersonalInfos((List<MrimPersonalInfo>) args[0], context));
				case MrimServiceResponse.RES_GROUPADDED:
					return serviceResponse.respond(IAccountServiceResponse.RES_GROUPADDED, getServiceId(), MrimEntityAdapter.MrimBuddyGroup2BuddyGroup((MrimBuddyGroup) args[0], internal.getMrid(), getServiceId()));
				case MrimServiceResponse.RES_BUDDYADDED:
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYADDED, getServiceId(), MrimEntityAdapter.MrimBuddy2Buddy((MrimBuddy) args[0], internal.getMrid(), getServiceId()));
				case MrimServiceResponse.RES_BUDDYDELETED:
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYDELETED, getServiceId(), MrimEntityAdapter.MrimBuddy2Buddy((MrimBuddy) args[0], internal.getMrid(), getServiceId()));
				case MrimServiceResponse.RES_GROUPDELETED:
					return serviceResponse.respond(IAccountServiceResponse.RES_GROUPDELETED, getServiceId(), MrimEntityAdapter.MrimBuddyGroup2BuddyGroup((MrimBuddyGroup) args[0], internal.getMrid(), getServiceId()));
				case MrimServiceResponse.RES_BUDDYMODIFIED:
					return serviceResponse.respond(IAccountServiceResponse.RES_BUDDYMODIFIED, getServiceId(), MrimEntityAdapter.MrimBuddy2Buddy((MrimBuddy) args[0], internal.getMrid(), getServiceId()));
				case MrimServiceResponse.RES_GROUPMODIFIED:
					return serviceResponse.respond(IAccountServiceResponse.RES_GROUPMODIFIED, getServiceId(), MrimEntityAdapter.MrimBuddyGroup2BuddyGroup((MrimBuddyGroup) args[0], internal.getMrid(), getServiceId()));
				*/case MrimServiceResponse.RES_FILEPROGRESS:
					return serviceResponse.respond(IAccountServiceResponse.RES_FILEPROGRESS, getServiceId(), ProtocolUtils.bytes2LongBE((byte[]) args[0], 0), args[1], args[2], args[3], args[4], args[5], (args.length > 6)?args[6] : null);
				case MrimServiceResponse.RES_MESSAGEACK:
					return serviceResponse.respond(IAccountServiceResponse.RES_MESSAGEACK, getServiceId(), args[0], args[1], args[2]);
				case MrimServiceResponse.RES_TYPING:
					return serviceResponse.respond(IAccountServiceResponse.RES_TYPING, getServiceId(), args[0]);
				}			
			
			}catch(ProtocolException e){
				try {
					return serviceResponse.respond(RES_LOG, getServiceId(), e.getLocalizedMessage());
				} catch (ProtocolException e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}
		
	};
	
	@SuppressWarnings("unchecked")
	private void run(Object[] args) throws ProtocolException{
		
		Map<String, String> sharedPreferences = (Map<String, String>) serviceResponse.respond(IAccountServiceResponse.RES_GETFROMSTORAGE, getServiceId(), IAccountServiceResponse.SHARED_PREFERENCES, options.keySet());
		if (sharedPreferences==null){
			throw new ProtocolException("Error getting preferences");
		}
		String un = sharedPreferences.get(MRID);
		String pw = sharedPreferences.get(PASSWORD);		
		String host = sharedPreferences.get(LOGIN_HOST);
		String port = sharedPreferences.get(LOGIN_PORT);

		/*String ping = sharedPreferences.get(AccountService.PING_TIMEOUT);
		if (ping != null){
			try {
				pingTimeout = Integer.parseInt(ping);
			} catch (Exception e) {}
		}*/
		
		if (un==null || pw==null){
			throw new ProtocolException("Error: no auth data");
		}
		
		int status = MrimEntityAdapter.userStatus2MrimUserStatus((Byte) args[0]);
		try {
			internal.request(MrimServiceInternal.REQ_CONNECT, un, pw, host, port, /*ping, */status, args[1], args[2], args[3]);
		} catch (MrimException e) {
			throw new ProtocolException(e.getMessage());
		}
	}
	
	private MrimServiceInternal internal = new MrimServiceInternal(mrimResponse);

	public static byte[] statusValues = new byte[]{ Buddy.ST_ONLINE, Buddy.ST_AWAY, Buddy.ST_OTHER, Buddy.ST_FREE4CHAT, Buddy.ST_INVISIBLE};

	public MrimService(Context context, IAccountServiceResponse serviceResponse, byte serviceId) {
		super(context, serviceResponse, serviceId);
		
		options.put(MRID, null);
		options.put(PASSWORD, null);
		options.put(LOGIN_HOST, null);
		options.put(LOGIN_PORT, null + "");
		pingTimeout = 0;
	}

	public MrimService(Context context) {
		super(context, null, (byte) -1);
	}

	@Override
	protected short getCurrentState() {
		return internal.getCurrentState();
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	protected String getUserID() {
		return internal.getMrid();
	}

	

	public Map<String, String> getOptions() {
		return options;
	}	

	@Override
	public int getProtocolOptionNames() {
		return R.array.mrim_preference_names;
	}

	@Override
	public int getProtocolOptionDefaults() {
		return R.array.mrim_preference_defaults;
	}

	@Override
	public int getProtocolOptionStrings() {
		return R.array.mrim_preference_strings;
	}
	
	

	public static int getStatusResIdByAccountTiny(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.mrim_offline_tiny;
			case AccountService.STATE_CONNECTING:
				return R.drawable.mrim_connecting_tiny;
			}
		}
		
		return getStatusResIdByStatusIdTiny(account.status);
	}

	public static final int getStatusResIdByAccountMedium(AccountView account, boolean withoutConnectionState){
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.mrim_offline_medium;
			case AccountService.STATE_CONNECTING:
				return R.drawable.mrim_connecting_medium;
			}
		}
		
		return getStatusResIdByStatusIdMedium(account.status);
	}

	public static final int getStatusResIdByAccountSmall(AccountView account, boolean withoutConnectionState){
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.mrim_offline_small;
			case AccountService.STATE_CONNECTING:
				return R.drawable.mrim_connecting_small;
			}
		}
		
		return getStatusResIdByStatusIdSmall(account.status);
	}
	
	/*public static int getStatusResIdByStatusId(int statusId, int size) {
		if (size <= 16){
			return getStatusResIdByStatusId16(statusId);
		} else if (size <= 32){
			return getStatusResIdByStatusId32(statusId);
		} else if (size <= 48){
			return getStatusResIdByStatusId48(statusId);
		} else if (size <= 64){
			return getStatusResIdByStatusId64(statusId);
		} else {
			return getStatusResIdByStatusId80(statusId);
		}
	}*/

	public static int getStatusResIdByStatusIdBig(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_big;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_big;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_big;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_big;
			break;
		default:
			statusResId = R.drawable.mrim_offline_big;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_big;
			break;
		}
		return statusResId;
	}

	/*private static int getStatusResIdByStatusId64(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_64;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_64;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_64;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_64;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_64;
			break;
		default:
			statusResId = R.drawable.mrim_offline_64;
			break;
		}
		return statusResId;
	}*/

	public static int getStatusResIdByStatusIdSmall(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_small;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_small;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_small;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_small;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_small;
			break;
		default:
			statusResId = R.drawable.mrim_offline_small;
			break;
		}
		
		return statusResId;
	}

	public static int getStatusResIdByStatusIdMedium(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_medium;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_medium;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_medium;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_medium;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_medium;
			break;
		default:
			statusResId = R.drawable.mrim_offline_medium;
			break;
		}
		
		return statusResId;
	}

	public static int getStatusResIdByStatusIdTiny(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_tiny;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_tiny;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_tiny;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_tiny;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_tiny;
			break;
		default:
			statusResId = R.drawable.mrim_offline_tiny;
			break;
		}
		
		return statusResId;
	}

	/*public static int getStatusResIdByAccount(AccountView account, int size, boolean withoutConnectionState) {
		
		if (size <= 16){
			return getStatusResIdByAccount16(account, withoutConnectionState);
		} else if (size <= 32){
			return getStatusResIdByAccount32(account, withoutConnectionState);
		} else {
			return getStatusResIdByAccount48(account, withoutConnectionState);
		}
	}*/
	
	public static int getMenuResIdByAccount(AccountView account) {
		return R.menu.mrim_cl_menu;
	}	
	
	public static final String[] getStatusListNames(Context context){
		return context.getResources().getStringArray(R.array.mrim_status_strings);
	}
	
	public static final byte getStatusValueByCount(int count){
		return statusValues[count];
	}

	@Override
	protected void timeoutDisconnect() {
		internal.getRunnableService().disconnect();		
	}

	public static TypedArray getStatusResIds(Context context) {
		return context.getResources().obtainTypedArray(R.array.mrim_status_icons);
	}

}
