package ua.snuk182.asia.services.api;

import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;

public interface IAccountServiceResponse {
	public Object respond(short action, byte serviceId, Object ... args) throws ProtocolException;
	
	/**
	 * Dummy operation, just in case.
	 */
	public static final short RES_NOP = 0;
	
	//public static final short RES_SAVEPARAMS = 1;
	
	/**
	 * Notify service that account has entered the system.
	 * Args: none
	 */
	public static final short RES_CONNECTED = 2;
	
	/**
	 * Notify service that account has been thrown off the system.
	 * Args: none or String error. If error is present, it will be shown in a toast with no reconnection.
	 * 
	 */	
	public static final short RES_DISCONNECTED = 3;
	
	//public static final short RES_STATUSSET = 4;
	
	//public static final short RES_EXTENDEDSTATUSSET = 5;
	
	/**
	 * Notify service that the message is received.
	 * Args: {@link TextMessage}
	 */
	public static final short RES_MESSAGE = 6;
	
	/**
	 * Notify service that file transfer request is received.
	 * Args: {@link FileMessage}
	 */
	public static final short RES_FILEMESSAGE = 7;
	
	/**
	 * Notify service that user info is received.
	 */
	public static final short RES_USERINFO = 8;
	
	/**
	 * Notify service that own info is received.
	 * Args: {@link PersonalInfo}
	 */
	public static final short RES_OWNINFO = 9;
	
	//public static final short RES_OWNINFOSET = 10;	
	
	/**
	 * Notify service that a buddy is added to account.
	 * Args: {@link Buddy}
	 */
	public static final short RES_BUDDYADDED = 11;
	
	/**
	 * Notify service that a buddy is removed from account.
	 * Args: {@link Buddy}
	 */	
	public static final short RES_BUDDYDELETED = 12;
		
	/**
	 * Notify service that contact list has been updated.
	 * Args: List<Buddy>, List<BuddyGroup>
	 */
	public static final short RES_CLUPDATED = 13;
	
	/**
	 * Notify service that buddy's state is changed.
	 * Args: {@link OnlineInfo}
	 */
	public static final short RES_BUDDYSTATECHANGED = 14;
	
	/**
	 * Ask for storing icon file.
	 * Args: byte[] - file data, String - file data owner uid
	 * 
	 */
	public static final short RES_SAVEIMAGEFILE = 15;
	
	/**
	 * Notification message from a service.
	 * Args: String message
	 */
	public static final short RES_NOTIFICATION = 16;
	
	/**
	 * Notify service that account has been updated.
	 * Args: {@link OnlineInfo} with new account data.
	 */
	public static final short RES_ACCOUNTUPDATED = 17;
	
	/**
	 * Ask service to store service-specific preferences.
	 * Args: String - info owner uid, Map<String, String> - info map to save.
	 */
	public static final short RES_SAVETOSTORAGE = 18;
	
	/**
	 * Ask service to retrieve service-specific preferences.
	 * Args: String - info owner uid, Set<String> - info keys to get.
	 */
	public static final short RES_GETFROMSTORAGE = 19; // String storageSuffix, Set<String> keys
	//public static final short RES_LOG = 20;
	
	/**
	 * Notify service that authorization request arrived.
	 * Args: String - request sender uid, String - request text.
	 */
	public static final short RES_AUTHREQUEST = 21;
	
	/**
	 * Notify service that result of previous search arrived.
	 * Args: ArrayList<PersonalInfo> - a list of buddy infos, may be empty
	 * @see AccountService#REQ_SEARCHFORBUDDY_BY_UID 
	 */
	public static final short RES_SEARCHRESULT = 22;
	
	/**
	 * Notify service that a buddy info is changed.
	 * Args: {@link Buddy}
	 */
	public static final short RES_BUDDYMODIFIED = 23;
	
	/**
	 * Notify service that a buddy group is changed.
	 * Args: {@link BuddyGroup}
	 */
	public static final short RES_GROUPMODIFIED = 24;
	
	/**
	 * Notify service that a buddy group is added to account.
	 * Args: {@link BuddyGroup}
	 */
	public static final short RES_GROUPADDED = 25;
	
	/**
	 * Notify service that a buddy group is removed from account.
	 * Args: {@link BuddyGroup}
	 */
	public static final short RES_GROUPDELETED = 26;
	
	/**
	 * Notify service about account's connection state.
	 * Args: Integer - connection state, from 0 to 10
	 */
	public static final short RES_CONNECTING = 27;
	
	/**
	 * Notify service about file transfer progress.
	 * Args: Long - message id, String - file path (for outgoings) or file name (for incomings), Long - total file size (bytes), Long - bytes sent, Boolean - true if incoming transfer, String - data error (or null, if no error), String - participant uid
	 */
	public static final short RES_FILEPROGRESS = 28;
	
	//public static final short RES_VISUALSTYLE = 29;	
	//public static final short RES_ACCOUNTREMOVED = 30;
	//public static final short RES_ACCOUNTADDED = 31;
	
	/**
	 * Notify service that message delivery notification arrived.
	 * Args: String - message recipient uid, Long - message id, Integer - notification level (1 - delivered to server, 2 - delivered to recipient)
	 */
	public static final short RES_MESSAGEACK = 32;
	
	/**
	 * Typing notification arrived. 
	 * Args: String - sender uid
	 */
	public static final short RES_TYPING = 33;
	
	/**
	 * Notify service that account service's available group chats list arrived. Only for account types that supports group chats.
	 * Args: List<MultiChatRoom>
	 */
	public static final short RES_AVAILABLE_CHATS = 34;
	
	/**
	 * Notify service that group chat occupants list arrived. Only for account types that support group chats.
	 * Args: String - chat id, MultiChatRoomOccupants - occupants info
	 */
	public static final short RES_CHAT_PARTICIPANTS = 35;
	
	/**
	 * Service message arrived (for now only chat service messages are implemented).
	 * Args: String - sender uid, String - message.
	 */
	public static final short RES_SERVICEMESSAGE = 36;
	
	/**
	 * Data for storing into account's activity log arrived.
	 * Args: String - text data.
	 */
	public static final short RES_ACCOUNT_ACTIVITY = 37;	
	
	
	/**
	 * Account service storage name suffix, for distinguishing common preferences.
	 */
	public static final String SHARED_PREFERENCES = "ProtocolSharedPrefs";		
}
