package ua.snuk182.asia.view.mainscreen;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.TabInfoFactory;
import ua.snuk182.asia.view.IHasAccount;
import ua.snuk182.asia.view.IHasBuddy;
import ua.snuk182.asia.view.IHasFileTransfer;
import ua.snuk182.asia.view.IHasMessages;
import ua.snuk182.asia.view.IHasServiceMessages;
import ua.snuk182.asia.view.IMainScreen;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.conversations.ConversationsView;
import ua.snuk182.asia.view.groupchats.GroupChatsView;
import ua.snuk182.asia.view.more.AsiaCoreException;
import ua.snuk182.asia.view.more.PreferencesView;
import ua.snuk182.asia.view.more.SearchUsersView;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.graphics.Rect;
import android.os.Build;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;

public class SmartphoneScreen extends TabHost implements IMainScreen {

	private final ArrayList<TabInfo> tabs;

	private HorizontalScrollView tabScroller;

	private final List<OnTabChangeListener> tabChangeListeners = new ArrayList<OnTabChangeListener>();

	private final OnTabChangeListener tabChangeListener = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			for (OnTabChangeListener listener : tabChangeListeners) {
				listener.onTabChanged(tabId);
				getEntryPoint().refreshMenu();
			}
		}
	};

	private final OnTabChangeListener scrollToSelectedListener = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			scrollToSelected(tabId);
		}
	};

	public SmartphoneScreen(EntryPoint entryPoint, AttributeSet attrs) {
		super(entryPoint, attrs);

		tabs = new ArrayList<TabInfo>();

		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.tab_layout, this);
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setup(entryPoint.getLocalActivityManager());
		tabScroller = (HorizontalScrollView) findViewById(R.id.tabContainer);

		setOnTabChangedListener(tabChangeListener);

		addOnTabChangeListener(scrollToSelectedListener);
	}

	private void scrollToSelected(final String tabId) {
		tabScroller.post(new Runnable() {

			@Override
			public void run() {
				TabInfo desiredTab = getTabByTag(tabId);
				if (desiredTab == null) {
					return;
				}
				TabWidgetLayout tabWidget = desiredTab.tabWidgetLayout;
				Rect rect = new Rect();
				tabScroller.getDrawingRect(rect);

				int leftBound = tabWidget.getLeftBound();
				int rightBound = tabWidget.getRightBound();

				if (rect.left > leftBound) {
					tabScroller.scrollTo(leftBound, 0);
					return;
				}
				if (rect.right < rightBound) {
					tabScroller.scrollTo(rightBound, 0);
					return;
				}
			}
		});
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			scrollToSelected(getCurrentTabTag());
		}
	}

	public HorizontalScrollView getTabScroller() {
		return tabScroller;
	}

	public void addOnTabChangeListener(OnTabChangeListener listener) {
		tabChangeListeners.add(listener);
	}

	public void removeOnTabChangeListener(OnTabChangeListener listener) {
		tabChangeListeners.remove(listener);
	}

	protected EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	private boolean checkShowTabs() {
		if (Build.VERSION.SDK_INT == 11) {
			findViewById(R.id.tabWidgetContainer).setVisibility(View.GONE);
			return false;
		}
		String hideTabsStr;
		try {
			hideTabsStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_view_type));
			if (hideTabsStr != null) {
				boolean hideTabs = hideTabsStr.equals(getResources().getString(R.string.value_view_type_notabs));
				if (hideTabs) {
					findViewById(R.id.tabWidgetContainer).setVisibility(View.GONE);
				} else {
					findViewById(R.id.tabWidgetContainer).setVisibility(View.VISIBLE);
				}
				return hideTabs;
			} else {
				findViewById(R.id.tabWidgetContainer).setVisibility(View.VISIBLE);
			}
		} catch (NullPointerException npe) {
			findViewById(R.id.tabWidgetContainer).setVisibility(View.VISIBLE);
		}
		return true;
	}

	@Override
	public void configChanged() {
		for (TabInfo tab : tabs) {
			if (tab.content != null) {
				tab.content.configChanged();
			}
		}
		// scrollToSelectedListener.onTabChanged(getCurrentTabTag());
	}

	@Override
	public void visualStyleUpdated() {
		/*
		 * if (checkShowTabs() && EntryPoint.bgColor !=
		 * EntryPoint.BGCOLOR_WALLPAPER){
		 * findViewById(R.id.divider).setVisibility(View.VISIBLE);
		 * findViewById(R.id.divider).setBackgroundColor(0x60202020); } else {
		 * findViewById(R.id.divider).setVisibility(View.GONE); }
		 */
		checkShowTabs();

		if (EntryPoint.bgColor == 0xff7f7f80) {
			tabScroller.setBackgroundColor(0x0);
		} else {
			if (EntryPoint.tabStyle.equals("system")) {
				tabScroller.setBackgroundColor(0);
			} else {
				if (EntryPoint.bgColor < 0xff7f7f80) {
					tabScroller.setBackgroundColor(0xff808080);
				} else {
					tabScroller.setBackgroundColor(0xffd0d0d0);
				}
			}
		}

		for (TabInfo tab : tabs) {
			if (tab.content != null) {
				tab.content.visualStyleUpdated();
				tab.tabWidgetLayout.color();
			}
		}

		if (getCurrentView() != null && getCurrentView().getContext() instanceof PreferencesView) {
			((PreferencesView) getCurrentView().getContext()).visualStyleUpdated();
		}
	}

	@Override
	public void addTab(final TabInfo info, boolean setAsCurrent) {
		if (info == null || getTabByTag(info.tag) != null)
			return;

		tabs.add(info);
		if (info.tabWidgetLayout != null && info.tabWidgetLayout.getParent() != null) {
			((ViewGroup) info.tabWidgetLayout.getParent()).removeView(info.tabWidgetLayout);
		}

		addTab(info.tabSpec);
		if (info.tabWidgetLayout.spec != null) {
			info.tabWidgetLayout.setFromView(getTabWidget().getChildTabViewAt(getTabWidget().getChildCount() - 1), this);
		}

		if (setAsCurrent) {
			setCurrentTabByTag(info.tag);
		}

		getTabWidget().getChildTabViewAt(getTabWidget().getChildCount() - 1).setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if (info.tag.equals(getCurrentTabTag())) {
					getEntryPoint().getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
					return true;
				} else {
					return false;
				}
			}
		});
	}

	@Override
	public void onStart() {
		try {
			if (tabs.size() > 0 && getSelectedTab().content != null) {
				getSelectedTab().content.onStart();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkAndSetCurrentTabByTag(String tag) {
		for (int i = 0; i < tabs.size(); i++) {
			if (tabs.get(i).tag.equals(tag)) {
				setCurrentTab(i);
				return true;
			}
		}

		return false;
	}

	@Override
	public void removeTabByTag(String tag) {
		String currentTag = getCurrentTabTag();

		if (currentTag != null && currentTag.equals(tag)) {
			setCurrentTab(0);
		}

		for (int i = 0; i < tabs.size(); i++) {
			if (tabs.get(i).tag.equals(tag)) {
				removeTabAt(i);
				break;
			}
		}

		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	private void removeTabAt(int pos) {
		try {
			setCurrentTab(0);
			getTabWidget().setFocusable(false);
			clearAllTabs();
			tabs.remove(pos);
		} catch (Exception e) {
			ServiceUtils.log(e);
		}

		if (tabs.size() < 1) {
			getEntryPoint().finish();
		}

		for (TabInfo info : tabs) {
			try {
				addTab(info.tabSpec);
				if (info.tabWidgetLayout.spec != null) {
					info.tabWidgetLayout.setFromView(getTabWidget().getChildTabViewAt(getTabWidget().getChildCount() - 1), this);
				}
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}

		getTabWidget().setFocusable(true);
	}

	@Override
	public ArrayList<TabInfo> getTabs() {
		return tabs;
	}

	@Override
	public void accountStateChanged(AccountView account, boolean refreshContacts) {
		for (TabInfo tab : tabs) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == account.serviceId) {
				((IHasAccount) tab.content).stateChanged(account, refreshContacts);
			}
		}
	}

	@Override
	public void accountUpdated(AccountView account, boolean refreshContacts) {
		for (TabInfo tab : tabs) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == account.serviceId) {
				((IHasAccount) tab.content).updated(account, refreshContacts);
			}
		}
	}

	@Override
	public void icon(byte serviceId, String uid) {
		for (TabInfo tab : tabs) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == serviceId) {
				ServiceUtils.log(uid);
				((IHasAccount) tab.content).bitmap(uid);
			}
		}
	}

	@Override
	public void buddyStateChanged(Buddy buddy) {
		for (TabInfo tab : tabs) {
			if ((tab.content instanceof IHasBuddy) && ((IHasBuddy) tab.content).getServiceId() == buddy.serviceId) {
				((IHasBuddy) tab.content).updateBuddyState(buddy);
			}
		}
	}

	@Override
	public void connecting(byte serviceId, int progress) {
		for (TabInfo tab : tabs) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == serviceId) {
				((IHasAccount) tab.content).connectionState(progress);
			}
		}
	}

	@Override
	public void searchResult(byte serviceId, List<PersonalInfo> infos) {
		for (TabInfo tab : tabs) {
			if ((tab.content instanceof SearchUsersView) && ((SearchUsersView) tab.content).getServiceId() == serviceId) {
				((SearchUsersView) tab.content).searchResult(infos);
			}
		}
	}

	@Override
	public void textMessage(TextMessage message) {
		for (int i = 0; i < tabs.size(); i++) {
			TabInfo tab = tabs.get(i);
			if ((tab.content instanceof IHasAccount) && (tab.content instanceof IHasMessages) && ((IHasAccount) tab.content).getServiceId() == message.serviceId) {
				((IHasMessages) tab.content).messageReceived(message, i == getCurrentTab());
			}
		}

		if (!(getCurrentView() instanceof IHasAccount) || ((IHasAccount) getCurrentView()).getServiceId() != message.serviceId) {
			try {
				Buddy budddy = getEntryPoint().runtimeService.getBuddy(message.serviceId, message.from);
				budddy.unread++;
				getEntryPoint().setUnread(budddy, message);
			} catch (NullPointerException npe) {
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
		}
	}

	@Override
	public void fileProgress(long messageId, Buddy buddy, String filename, long totalSize, long sizeTransferred, boolean isReceive, String error) {
		IHasFileTransfer ft = null;

		for (TabInfo tab : tabs) {
			if (tab.content != null && tab.content instanceof IHasFileTransfer && ((IHasFileTransfer) tab.content).getServiceId() == buddy.serviceId) {
				ft = (IHasFileTransfer) tab.content;
			}
		}

		if (ft == null) {
			TabInfo tab = TabInfoFactory.createFileTransferTab(getEntryPoint(), buddy.serviceId);
			ft = (IHasFileTransfer) tab.content;
			addTab(tab, true);
			setCurrentTabByTag(tab.tag);
		}

		ft.notifyFileProgress(messageId, buddy, filename, totalSize, sizeTransferred, isReceive, error);
	}

	@Override
	public void messageAck(Buddy buddy, long messageId, int level) {
		for (TabInfo tab : tabs) {
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid)) {
				((ConversationsView) tab.content).messageAck(messageId, level);
			}
		}
	}

	@Override
	public void typing(byte serviceId, String buddyUid) {
		for (int i = 0; i < tabs.size(); i++) {
			TabInfo tab = tabs.get(i);
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + serviceId + " " + buddyUid)) {
				((ConversationsView) tab.content).typing(getCurrentTab() == i);
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		/*
		 * try { for (int i=tabs.size()-1; i>=0; i--){ if
		 * (tabs.get(i).tag.indexOf(ContactList.class.getSimpleName())<0 &&
		 * tabs.get(i).tag.indexOf(ConversationsView.class.getSimpleName())<0 &&
		 * tabs.get(i).tag.indexOf(HistoryView.class.getSimpleName())<0){
		 * tabs.remove(i); } } getEntryPoint().runtimeService.saveTabs(tabs);
		 * //unbindService(serviceConnection); } catch (NullPointerException
		 * npe) { ServiceUtils.log(npe); } catch (RemoteException e){
		 * getEntryPoint().onRemoteCallFailed(e); }
		 */
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (getSelectedTab() != null && getSelectedTab().content != null && getSelectedTab().content.getMainMenuId() > 0) {
			menu.clear();

			MenuInflater inflater = getEntryPoint().getMenuInflater();
			inflater.inflate(getSelectedTab().content.getMainMenuId(), menu);

			return getSelectedTab().content.onPrepareOptionsMenu(menu);
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return getSelectedTab().content.onOptionsItemSelected(item);
	}

	private TabInfo getSelectedTab() {
		if (getCurrentTab() >= tabs.size()) {
			ServiceUtils.log(getCurrentTabTag() + " has no tabinfo!!");
		}
		try {
			return tabs.get(getCurrentTab());
		} catch (Exception e) {
			ServiceUtils.log(e);
			return null;
		}
	}

	private TabInfo getTabByTag(String tag) {
		for (TabInfo tab : tabs) {
			if (tab.tag.equals(tag)) {
				return tab;
			}
		}
		return null;
	}

	@Override
	public void removeAccount(AccountView account) {
		setCurrentTab(0);

		for (int i = tabs.size() - 1; i >= 0; i--) {
			if (tabs.get(i).tag.indexOf(ConversationsView.class.getSimpleName() + " " + account.serviceId) > -1 || tabs.get(i).tag.equals(ContactList.class.getSimpleName() + " " + account.serviceId)) {
				tabs.remove(i);
			}
		}

		if (tabs.size() < 1) {
			getEntryPoint().addAccountEditorTab(null);
		}

		try {
			getTabWidget().setFocusable(false);
			clearAllTabs();
		} catch (Exception e) {
			ServiceUtils.log(e);
		}

		for (TabInfo info : tabs) {
			try {
				addTab(info.tabSpec);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}

		getTabWidget().setFocusable(true);
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {
		try {
			if (getSelectedTab().content.onKeyDown(i, event)) {
				return true;
			} else {
				return super.onKeyDown(i, event);
			}
		} catch (Exception e) {
			return super.onKeyDown(i, event);
		}
	}

	@Override
	public int getCurrentAccountsTab() {
		return getCurrentTab();
	}

	@Override
	public String getCurrentAccountsTabTag() {
		return getCurrentTabTag();
	}

	@Override
	public int getCurrentChatsTab() {
		return getCurrentTab();
	}

	@Override
	public String getCurrentChatsTabTag() {
		return getCurrentTabTag();
	}

	@Override
	public TabHost getAccountsTabHost() {
		return this;
	}

	@Override
	public TabHost getChatsTabHost() {
		return this;
	}

	@Override
	public void setCurrentChatsTab(int tab) {
		if (tab < getTabs().size()) {
			setCurrentTab(tab);
		}
	}

	@Override
	public void setCurrentAccountsTab(int tab) {
		if (tab < tabs.size()) {
			setCurrentTab(tab);
		}
	}

	@Override
	public void availableChatsList(byte serviceId, List<MultiChatRoom> chats) {
		for (TabInfo tab : tabs) {
			if (tab.tag != null && tab.tag.equals(GroupChatsView.class.getSimpleName() + " " + serviceId)) {
				((GroupChatsView) tab.content).chatsList(chats);
			}
		}
	}

	@Override
	public void chatRoomOccupants(byte serviceId, String chatId, MultiChatRoomOccupants occupants) {
		for (TabInfo tab : tabs) {
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + serviceId + " " + chatId)) {
				((ConversationsView) tab.content).fillGroupChatView(occupants);
			}
		}
	}

	@Override
	public void serviceMessage(ServiceMessage message) {
		for (int i = 0; i < tabs.size(); i++) {
			TabInfo tab = tabs.get(i);
			if (tab.content != null && (tab.content instanceof IHasServiceMessages)) {
				((IHasServiceMessages) tab.content).serviceMessageReceived(message, i == getCurrentTab());
			}
		}
	}

	@Override
	public void refreshAccounts() {
		try {
			List<AccountView> accounts = getEntryPoint().runtimeService.getAccounts(false);

			tabs.clear();

			for (int i = accounts.size() - 1; i >= 0; i--) {
				AccountView account = accounts.get(i);
				TabInfo info = TabInfoFactory.createContactList(getEntryPoint(), account);
				tabs.add(0, info);
			}

			try {
				setCurrentTab(0);
				getTabWidget().setFocusable(false);
				clearAllTabs();
			} catch (Exception e) {
				ServiceUtils.log(e);
			}

			for (TabInfo info : tabs) {
				try {
					addTab(info.tabSpec);
				} catch (Exception e) {
					ServiceUtils.log(e);
				}
			}

			getTabWidget().setFocusable(true);
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			getEntryPoint().onRemoteCallFailed(e);
		} catch (AsiaCoreException e) {
			ServiceUtils.log(e);
		}
	}

	@Override
	public List<String> getCurrentTabs() {
		List<String> list = new ArrayList<String>();
		list.add(getCurrentTabTag());
		return list;
	}
}
