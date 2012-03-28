package ua.snuk182.asia.services;

import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.core.dataentity.ContactListViewGroup;
import ua.snuk182.asia.services.IRuntimeServiceCallback;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;

interface IRuntimeService{
	String sendMessage(in TextMessage message, byte serviceId);
	
	byte createAccount(in AccountView account);
	void deleteAccount(in AccountView account);
	void editAccount(in AccountView account);
	
	List<AccountView> getAccounts(boolean disabledToo);
	Buddy getBuddy(byte serviceId, String buddyProtocolUid);
	List<Buddy> getBuddies(byte serviceId, in List<String> buddyProtocolUid);
	List<TabInfo> getSavedTabs();
	void saveTabs(in List<TabInfo> tabInfos);
	AccountView getAccountView(byte serviceId);
	void connect(byte serviceId);
	//void setAppVisible(boolean visible);
	boolean isDataSetInvalid(byte serviceId, long lastUpdateTime);
	void connectAll();
	void disconnectAll();
	void disconnect(byte serviceId);
	void registerCallback(IRuntimeServiceCallback callback);
	void setUnread(in Buddy buddy, in TextMessage message);
	void setServiceMessageUnread(byte serviceId, boolean unread, in ServiceMessage message);
	void savePreference(String key, String value, byte serviceId);
	Bundle getApplicationOptions();
	Bundle getProtocolServiceOptions(byte serviceId);
	void saveProtocolServiceOptions(byte serviceId, in Bundle options);
	void askForXStatus(in Buddy buddy);
	
	void addBuddy(in Buddy buddy);
	void removeBuddy(in Buddy buddy);
	void renameBuddy(in Buddy buddy);
	void moveBuddy(in Buddy buddy, in BuddyGroup oldGroup, in BuddyGroup newGroup);
	
	void addGroup(in BuddyGroup group);
	void removeGroup(in BuddyGroup group, in List<Buddy> buddy, in BuddyGroup newGroupForBuddies);
	void renameGroup(in BuddyGroup group);
	void setGroupCollapsed(byte serviceId, int groupId, boolean collapsed);
	List<Buddy> getBuddiesFromGroup(in BuddyGroup group);
	
	void requestBuddyShortInfo(byte serviceId, String uid);
	void requestBuddyFullInfo(byte serviceId, String uid);
	
	void requestAuthorization(in Buddy buddy, String reason);
	void respondAuthorization(in Buddy buddy, boolean authorized);
	void searchUsersByUid(byte serviceId, String buddyUid);
	void respondFileMessage(in FileMessage msg, boolean accept);
	void sendFile(in Bundle bu, in Buddy buddy);
	void cancelFileTransfer(byte serviceId, long messageId);
	
	List<ServiceMessage> getServiceMessages(byte serviceId, String uid);
	
	void setStatus(byte serviceId, byte status);
	void setXStatus(in AccountView acccount);
	void prepareExit();
	//void log(String log);
	void sendTyping(byte serviceId, String buddyUid);
	void editBuddyVisibility(in Buddy buddy);
	void editMyVisibility(byte serviceId, byte visibility);
	
	void requestAvailableChatRooms(byte serviceId);
	
	byte createChat(byte serviceId, String chatId, String chatNickname, String chatName, String chatPassword);
	byte joinExistingChat(byte serviceId, String chatId);
	byte leaveChat(byte serviceId, String chatId);
	byte joinChat(byte serviceId, String chatId, String chatNickname, String chatPassword);
	boolean checkGroupChatsAvailability(byte serviceId);
	MultiChatRoomOccupants getChatRoomOccupants(byte serviceId, String chatId);
	PersonalInfo getChatInfo(byte serviceId, String chatId);
	void setCurrentTabs(in List<String> tabs);
}