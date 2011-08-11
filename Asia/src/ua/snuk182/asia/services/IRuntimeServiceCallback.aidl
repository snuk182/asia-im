package ua.snuk182.asia.services;

import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.PersonalInfo;

interface IRuntimeServiceCallback{
	/*
	public static final short RES_NOP = 0;
	public static final short RES_SAVEPARAMS = 1;
	public static final short RES_CONNECTED = 2;
	public static final short RES_DISCONNECTED = 3;
	public static final short RES_STATUSSET = 4;
	public static final short RES_EXTENDEDSTATUSSET = 5;
	public static final short RES_MESSAGE = 6;
	public static final short RES_FILEMESSAGE = 7;
	public static final short RES_USERINFO = 8;
	public static final short RES_OWNINFO = 9;
	public static final short RES_OWNINFOSET = 10;
	public static final short RES_BUDDYADDED = 11;
	public static final short RES_BUDDYDELETED = 12;
	public static final short RES_CLUPDATED = 13;
	*/
	
	/*void accountAdded(in AccountView account);
	void accountInfoUpdated(in AccountView account);
	void accountRemoved(in AccountView account);
	void accountStateChanged(in AccountView account);
	void textMessage(in TextMessage message);
	void buddyStateChanged(in Buddy buddy);
	void buddyGroupAdded(in BuddyGroup group);
	void buddyGroupRemoved(in BuddyGroup group);
	void serviceNotification(byte serviceId, String text, byte type);
	void serviceMessage(in ServiceMessage msg);
	void buddySearchResult(byte serviceId, in List<PersonalInfo> infos);
	void visualStyleUpdated();
	void connectionState(byte serviceId, int state);
	void bitmap(byte serviceId, String uid);*/
	

	void accountConnected(in AccountView account);
	void icon(byte serviceId, String uid);
	void contactListUpdated(in AccountView account);
	void buddyStateChanged(in Buddy buddy);
	//void notification(String text, int type);
	void connecting(byte serviceId, int progress);
	void accountUpdated(in AccountView account);
	void serviceMessage(in ServiceMessage message);
	void fileMessage(in FileMessage message);
	void searchResult(byte serviceId, in List<PersonalInfo> infos);
	void groupAdded(in BuddyGroup group, in AccountView account);
	void buddyAdded(in Buddy buddy, in AccountView account);
	void buddyRemoved(in Buddy buddy, in AccountView account);
	void groupRemoved(in BuddyGroup group, in AccountView account);
	void buddyEdited(in Buddy buddy, in AccountView account);
	void groupEdited(in BuddyGroup group, in AccountView account);
	void disconnected(in AccountView account);
	void textMessage(in TextMessage message);
	void accountAdded(in AccountView account);
	void status(in AccountView account);
	void accountRemoved(in AccountView account);
	void visualStyleUpdated();
	
	void fileProgress(long messageId, in Buddy buddy, String filename, long totalSize, long sizeTransferred, boolean isReceive, String error);	
	void messageAck(in Buddy buddy, long messageId, int level);
	void personalInfo(in Buddy buddy, in PersonalInfo info);
	void typing(byte serviceId, String buddyUid);
}