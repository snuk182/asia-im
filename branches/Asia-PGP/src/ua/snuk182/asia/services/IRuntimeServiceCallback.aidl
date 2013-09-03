package ua.snuk182.asia.services;

import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;

interface IRuntimeServiceCallback{
	
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
	
	void availableChatsList(byte serviceId, in List<MultiChatRoom> chats);
	void chatRoomOccupants(byte serviceId, String chatId, in MultiChatRoomOccupants occupants);
}