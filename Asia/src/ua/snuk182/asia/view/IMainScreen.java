package ua.snuk182.asia.view;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public interface IMainScreen {

	public void setBackgroundDrawable(Drawable drawable);
	public void setBackgroundColor(int color);
	
	public void addOnTabChangeListener(OnTabChangeListener listener);
	public void removeOnTabChangeListener(OnTabChangeListener listener);
	
	public void visualStyleUpdated();
	public void configChanged();
	
	public boolean checkAndSetCurrentTabByTag(String tag);
	public void addTab(TabInfo tab, boolean setAsCurrent);
	public void removeTabByTag(String tag);
	public ArrayList<TabInfo> getTabs();
	
	public int getCurrentAccountsTab();
	public String getCurrentAccountsTabTag();
	public int getCurrentChatsTab();
	public String getCurrentChatsTabTag();
	public TabHost getAccountsTabHost();
	public TabHost getChatsTabHost();
	public void setCurrentChatsTab(int tab);
	public void setCurrentAccountsTab(int tab);
	
	public void onStart();
	public void onDestroy();
	public boolean onPrepareOptionsMenu(Menu menu);
	public boolean onOptionsItemSelected(MenuItem item);
	public boolean onKeyDown(int i, KeyEvent event);
	
	void accountStateChanged(AccountView account, boolean updateContactList);
	void icon(final byte serviceId, final String uid);
	void buddyStateChanged(final Buddy buddy);
	void connecting(final byte serviceId, final int progress);
	void accountUpdated(final AccountView account, boolean refreshContacts);
	void searchResult(final byte serviceId, final List<PersonalInfo> infos);	
	void textMessage(final TextMessage message);
	void fileProgress(final long messageId, final Buddy buddy, final String filename, final long totalSize, final long sizeTransferred, final boolean isReceive, final String error);
	void messageAck(final Buddy buddy, final long messageId, final int level);
	void typing(final byte serviceId, final String buddyUid);
	void removeAccount(AccountView account);
	void availableChatsList(byte serviceId, List<MultiChatRoom> chats);
	void chatRoomOccupants(byte serviceId, String chatId, MultiChatRoomOccupants occupants);
	void serviceMessage(ServiceMessage msg);
	void refreshAccounts();
}
