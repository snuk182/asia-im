package ua.snuk182.asia.view.mainscreen;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.Splashscreen;
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
import ua.snuk182.asia.view.more.HistoryView;
import ua.snuk182.asia.view.more.PersonalInfoView;
import ua.snuk182.asia.view.more.PreferencesView;
import ua.snuk182.asia.view.more.SearchUsersView;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.Gravity;
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
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class TabletScreen extends LinearLayout implements IMainScreen {

	public boolean isChatMenu = false;

	public final ArrayList<TabInfo> tabsAccount;
	private HorizontalScrollView tabScrollerAccount;
	private final TabHost tabHostAccount;
	public final ArrayList<TabInfo> tabsChat;
	private HorizontalScrollView tabScrollerChat;
	private final TabHost tabHostChat;

	private final TabInfo splash;

	private TabInfo getTabByTag(String tag) {
		for (TabInfo tab : tabsAccount) {
			if (tab.tag.equals(tag)) {
				return tab;
			}
		}
		for (TabInfo tab : tabsChat) {
			if (tab.tag.equals(tag)) {
				return tab;
			}
		}
		return null;
	}

	private final List<OnTabChangeListener> tabChangeListenersAccount = new ArrayList<OnTabChangeListener>();
	private final List<OnTabChangeListener> tabChangeListenersChat = new ArrayList<OnTabChangeListener>();

	private final OnTabChangeListener tabChangeListenerAccount = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			for (OnTabChangeListener listener : tabChangeListenersAccount) {
				listener.onTabChanged(tabId);
				getEntryPoint().refreshMenu();
			}
		}
	};
	private final OnTabChangeListener tabChangeListenerChat = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			for (OnTabChangeListener listener : tabChangeListenersChat) {
				listener.onTabChanged(tabId);
				getEntryPoint().refreshMenu();
			}
		}
	};

	private final OnTabChangeListener scrollToSelectedListenerAccount = new OnTabChangeListener() {

		@Override
		public void onTabChanged(final String tabId) {
			tabScrollerAccount.post(new Runnable() {

				@Override
				public void run() {
					TabInfo desiredTab = getTabByTag(tabId);
					if (desiredTab == null) {
						return;
					}
					View tabWidget = desiredTab.tabWidgetLayout;
					Rect rect = new Rect();
					tabScrollerAccount.getDrawingRect(rect);
					if (rect.left > tabWidget.getLeft()) {
						tabScrollerAccount.scrollTo(tabWidget.getLeft(), 0);
						return;
					}
					if (rect.right < tabWidget.getRight()) {
						tabScrollerAccount.scrollTo(tabWidget.getRight(), 0);
						return;
					}
				}
			});
		}
	};

	private final OnTabChangeListener scrollToSelectedListenerChat = new OnTabChangeListener() {

		@Override
		public void onTabChanged(final String tabId) {
			tabScrollerChat.post(new Runnable() {

				@Override
				public void run() {
					TabInfo desiredTab = getTabByTag(tabId);
					if (desiredTab == null) {
						return;
					}
					View tabWidget = desiredTab.tabWidgetLayout;
					Rect rect = new Rect();
					tabScrollerChat.getDrawingRect(rect);
					if (rect.left > tabWidget.getLeft()) {
						tabScrollerChat.scrollTo(tabWidget.getLeft(), 0);
						return;
					}
					if (rect.right < tabWidget.getRight()) {
						tabScrollerChat.scrollTo(tabWidget.getRight(), 0);
						return;
					}
				}
			});
		}
	};

	private Runnable callMenuRunnable = new Runnable() {

		@Override
		public void run() {
			getEntryPoint().getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
		}

	};

	public TabletScreen(EntryPoint entryPoint, AttributeSet attrs) {
		super(entryPoint, attrs);

		tabsAccount = new ArrayList<TabInfo>();
		tabsChat = new ArrayList<TabInfo>();

		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.tablet_layout, this);
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		tabHostAccount = (TabHost) findViewById(R.id.tabhost_acc);
		tabHostChat = (TabHost) findViewById(R.id.tabhost_chat);

		inflate.inflate(R.layout.tab_layout, tabHostAccount);
		inflate.inflate(R.layout.tab_layout, tabHostChat);

		tabHostAccount.setup(entryPoint.getLocalActivityManager());
		tabHostChat.setup(entryPoint.getLocalActivityManager());

		tabHostChat.getTabWidget().setGravity(Gravity.RIGHT);

		setOrientation();

		tabScrollerAccount = (HorizontalScrollView) tabHostAccount.findViewById(R.id.tabContainer);
		tabScrollerChat = (HorizontalScrollView) tabHostChat.findViewById(R.id.tabContainer);

		tabHostAccount.setOnTabChangedListener(tabChangeListenerAccount);
		tabHostChat.setOnTabChangedListener(tabChangeListenerChat);

		tabChangeListenersAccount.add(scrollToSelectedListenerAccount);
		tabChangeListenersChat.add(scrollToSelectedListenerChat);

		splash = TabInfoFactory.createSplashscreenTab(entryPoint, tabHostChat);

		addTab(splash, true);
	}

	private void setOrientation() {
		removeAllViews();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setOrientation(HORIZONTAL);
			tabHostChat.setPadding(5, 0, 0, 0);
		} else {
			setOrientation(VERTICAL);
			tabHostChat.setPadding(0, 5, 0, 0);
		}
		addView(tabHostAccount);
		addView(tabHostChat);
		// tabHostAccount.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT, 0.65f));
		// tabHostChat.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT, 0.35f));
	}

	@Override
	public void addOnTabChangeListener(OnTabChangeListener listener) {
		tabChangeListenersAccount.add(listener);
		tabChangeListenersChat.add(listener);
	}

	@Override
	public void removeOnTabChangeListener(OnTabChangeListener listener) {
		tabChangeListenersAccount.remove(listener);
		tabChangeListenersChat.remove(listener);
	}

	@Override
	public void visualStyleUpdated() {
		if (EntryPoint.bgColor == 0xff7f7f80) {
			tabScrollerAccount.setBackgroundColor(0x0);
			tabScrollerChat.setBackgroundColor(0x0);
		} else if (EntryPoint.bgColor < 0xff7f7f80) {
			tabScrollerAccount.setBackgroundColor(0xff202020);
			tabScrollerChat.setBackgroundColor(0xff202020);
		} else {
			tabScrollerAccount.setBackgroundColor(0xffd0d0d0);
			tabScrollerChat.setBackgroundColor(0xffd0d0d0);
		}

		for (TabInfo tab : tabsAccount) {
			if (tab.content != null) {
				tab.content.visualStyleUpdated();
				getEntryPoint();
				tab.tabWidgetLayout.color();
			}
		}
		for (TabInfo tab : tabsChat) {
			if (tab.content != null) {
				tab.content.visualStyleUpdated();
				getEntryPoint();
				tab.tabWidgetLayout.color();
			}
		}

		if (tabHostAccount.getCurrentView() != null && tabHostAccount.getCurrentView().getContext() instanceof PreferencesView) {
			((PreferencesView) tabHostAccount.getCurrentView().getContext()).visualStyleUpdated();
		}

		scrollToSelectedListenerAccount.onTabChanged(tabHostAccount.getCurrentTabTag());
		scrollToSelectedListenerChat.onTabChanged(tabHostChat.getCurrentTabTag());
	}

	@Override
	public boolean checkAndSetCurrentTabByTag(String tag) {
		for (int i = 0; i < tabsAccount.size(); i++) {
			if (tabsAccount.get(i).tag.equals(tag)) {
				tabHostAccount.setCurrentTab(i);
				return true;
			}
		}
		for (int i = 0; i < tabsChat.size(); i++) {
			if (tabsChat.get(i).tag.equals(tag)) {
				tabHostChat.setCurrentTab(i);
				return true;
			}
		}
		tabHostAccount.setCurrentTab(0);
		return false;
	}

	@Override
	public void addTab(final TabInfo info, boolean setAsCurrent) {
		if (info == null)
			return;

		boolean exists = getTabByTag(info.tag) != null;
		if (exists) {
			return;
		}

		if (info.tabWidgetLayout != null && info.tabWidgetLayout.getParent() != null) {
			((ViewGroup) info.tabWidgetLayout.getParent()).removeView(info.tabWidgetLayout);
		}

		if (isChat(info)) {
			tabsChat.add(info);
			if (tabHostChat.getCurrentTabTag() != null && tabHostChat.getCurrentTabTag().equals(Splashscreen.class.getSimpleName())) {
				removeTabAt(0, tabsChat, tabHostChat);
			} else {
				tabHostChat.addTab(info.tabSpec);
				if (EntryPoint.tabStyle.equals("system")) {
					info.tabWidgetLayout.setFromView(tabHostChat.getTabWidget().getChildTabViewAt(tabHostChat.getTabWidget().getChildCount() - 1), tabHostChat);
				}

				if (setAsCurrent) {
					tabHostChat.setCurrentTabByTag(info.tag);
				}
			}

			if (getEntryPoint().menuOnTabLongclick) {
				putOnLongClickListener(tabHostChat, info);
			}
		} else {
			tabsAccount.add(info);
			tabHostAccount.addTab(info.tabSpec);
			if (EntryPoint.tabStyle.equals("system")) {
				info.tabWidgetLayout.setFromView(tabHostAccount.getTabWidget().getChildTabViewAt(tabHostAccount.getTabWidget().getChildCount() - 1), tabHostAccount);
			}
			if (setAsCurrent) {
				tabHostAccount.setCurrentTabByTag(info.tag);
			}
			
			if (getEntryPoint().menuOnTabLongclick) {
				putOnLongClickListener(tabHostAccount, info);
			}
		}
	}

	private void putOnLongClickListener(TabHost tabHost, final TabInfo info) {
		tabHost.getTabWidget().getChildTabViewAt(tabHost.getTabWidget().getChildCount() - 1).setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				boolean isChat = isChat(info);

				if (info.tag.equals(isChat ? getCurrentChatsTabTag() : getCurrentAccountsTabTag())) {
					isChatMenu = isChat;
					getEntryPoint().getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
					return true;
				} else {
					return false;
				}
			}
		});
	}

	private static final boolean isChat(TabInfo info) {
		return info.tag.indexOf(PreferencesView.class.getSimpleName()) > -1 || info.content instanceof Splashscreen || info.content instanceof ConversationsView || info.content instanceof HistoryView
				|| info.content instanceof PersonalInfoView;
	}

	@Override
	public void removeTabByTag(String tag) {

		for (int i = 0; i < tabsAccount.size(); i++) {
			if (tabsAccount.get(i).tag.equals(tag)) {
				removeTabAt(i, tabsAccount, tabHostAccount);
				return;
			}
		}
		for (int i = 0; i < tabsChat.size(); i++) {
			if (tabsChat.get(i).tag.equals(tag)) {
				removeTabAt(i, tabsChat, tabHostChat);
				return;
			}
		}

		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	private void removeTabAt(int pos, ArrayList<TabInfo> tabs, TabHost host) {
		try {
			host.setCurrentTab(0);
			host.getTabWidget().setFocusable(false);
			host.clearAllTabs();
			tabs.remove(pos);
		} catch (Exception e) {
			ServiceUtils.log(e);
		}

		if (tabs.size() < 1) {
			if (tabs == tabsAccount) {
				getEntryPoint().finish();
			} else {
				tabs.add(splash);
			}
		}

		for (TabInfo info : tabs) {
			try {
				host.addTab(info.tabSpec);
				if (EntryPoint.tabStyle.equals("system")) {
					info.tabWidgetLayout.setFromView(host.getTabWidget().getChildTabViewAt(host.getTabWidget().getChildCount() - 1), host);
				}
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}

		host.getTabWidget().setFocusable(true);
	}

	@Override
	public ArrayList<TabInfo> getTabs() {
		ArrayList<TabInfo> all = new ArrayList<TabInfo>(tabsAccount.size() + tabsChat.size());
		all.addAll(tabsAccount);
		all.addAll(tabsChat);
		return all;
	}

	@Override
	public int getCurrentAccountsTab() {
		return tabHostAccount.getCurrentTab();
	}

	@Override
	public String getCurrentAccountsTabTag() {
		return tabHostAccount.getCurrentTabTag();
	}

	@Override
	public int getCurrentChatsTab() {
		return tabHostChat.getCurrentTab();
	}

	@Override
	public String getCurrentChatsTabTag() {
		return tabHostChat.getCurrentTabTag();
	}

	@Override
	public TabHost getAccountsTabHost() {
		return tabHostAccount;
	}

	@Override
	public TabHost getChatsTabHost() {
		return tabHostChat;
	}

	@Override
	public void onStart() {
		if (tabsAccount.size() > 0 && tabsAccount.get(tabHostAccount.getCurrentTab()).content != null) {
			tabsAccount.get(tabHostAccount.getCurrentTab()).content.onStart();
		}
		if (tabsChat.size() > 0 && tabsChat.get(tabHostChat.getCurrentTab()).content != null) {
			tabsChat.get(tabHostChat.getCurrentTab()).content.onStart();
		}
	}

	@Override
	public void onDestroy() {
		/*
		 * try { for (int i=tabsChat.size()-1; i>=0; i--){ if
		 * (tabsChat.get(i).tag
		 * .indexOf(ConversationsView.class.getSimpleName())<0 &&
		 * tabsChat.get(i).tag.indexOf(HistoryView.class.getSimpleName())<0){
		 * tabsChat.remove(i); } }
		 * getEntryPoint().runtimeService.saveTabs(getTabs()); } catch
		 * (NullPointerException npe) { ServiceUtils.log(npe); } catch
		 * (RemoteException e){ getEntryPoint().onRemoteCallFailed(e); }
		 */
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		TabInfo info;
		menu.clear();

		MenuInflater inflater = getEntryPoint().getMenuInflater();

		int currentTab = -1;
		if (!isChatMenu) {
			currentTab = tabHostAccount.getCurrentTab();

			if (currentTab < 0) {
				return false;
			}

			info = tabsAccount.get(currentTab);
			boolean addChatMenu = false;

			TabInfo chatInfo = tabsChat.get(tabHostChat.getCurrentTab());
			if (chatInfo.content != null && chatInfo.content.getMainMenuId() != 0) {
				addChatMenu = true;
			}

			if (info.content.getMainMenuId() < 1) {
				return false;
			}

			inflater.inflate(info.content.getMainMenuId(), menu);

			if (addChatMenu) {
				menu.add(Menu.NONE, R.string.label_chat_menu, 0, R.string.label_chat_menu);
			}
		} else {
			currentTab = tabHostChat.getCurrentTab();

			if (currentTab < 0) {
				return false;
			}
			info = tabsChat.get(currentTab);

			if (info.content.getMainMenuId() < 1) {
				return false;
			}
			inflater.inflate(info.content.getMainMenuId(), menu);

			if (ServiceUtils.isTablet(getContext())) {
				menu.add(Menu.NONE, R.string.label_account_menu, 0, R.string.label_account_menu);
			}
		}

		return info.content.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.string.label_chat_menu) {
			isChatMenu = true;
			getEntryPoint().threadMsgHandler.post(callMenuRunnable);
			return true;
		}

		if (item.getItemId() == R.string.label_account_menu) {
			isChatMenu = false;
			getEntryPoint().threadMsgHandler.post(callMenuRunnable);
			return true;
		}

		if (isChatMenu) {
			isChatMenu = false;
			return tabsChat.get(tabHostChat.getCurrentTab()).content.onOptionsItemSelected(item);
		} else {
			return tabsAccount.get(tabHostAccount.getCurrentTab()).content.onOptionsItemSelected(item);
		}
	}

	@Override
	public void accountStateChanged(AccountView account, boolean refreshContacts) {
		for (TabInfo tab : tabsAccount) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == account.serviceId) {
				((IHasAccount) tab.content).stateChanged(account, refreshContacts);
			}
		}
		for (TabInfo tab : tabsChat) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == account.serviceId) {
				((IHasAccount) tab.content).stateChanged(account, refreshContacts);
			}
		}
	}

	@Override
	public void icon(byte serviceId, String uid) {
		for (TabInfo tab : tabsAccount) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == serviceId) {
				ServiceUtils.log(uid);
				((IHasAccount) tab.content).bitmap(uid);
			}
		}
		for (TabInfo tab : tabsChat) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == serviceId) {
				ServiceUtils.log(uid);
				((IHasAccount) tab.content).bitmap(uid);
			}
		}
	}

	@Override
	public void buddyStateChanged(Buddy buddy) {
		for (TabInfo tab : tabsAccount) {
			if ((tab.content instanceof IHasBuddy) && ((IHasBuddy) tab.content).getServiceId() == buddy.serviceId) {
				((IHasBuddy) tab.content).updateBuddyState(buddy);
			}
		}
		for (TabInfo tab : tabsChat) {
			if ((tab.content instanceof IHasBuddy) && ((IHasBuddy) tab.content).getServiceId() == buddy.serviceId) {
				((IHasBuddy) tab.content).updateBuddyState(buddy);
			}
		}
	}

	@Override
	public void connecting(byte serviceId, int progress) {
		for (TabInfo tab : tabsAccount) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == serviceId) {
				((IHasAccount) tab.content).connectionState(progress);
			}
		}
		for (TabInfo tab : tabsChat) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == serviceId) {
				((IHasAccount) tab.content).connectionState(progress);
			}
		}
	}

	@Override
	public void accountUpdated(AccountView account, boolean refreshContacts) {
		for (TabInfo tab : tabsAccount) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == account.serviceId) {
				((IHasAccount) tab.content).updated(account, refreshContacts);
			}
		}
		for (TabInfo tab : tabsChat) {
			if ((tab.content instanceof IHasAccount) && ((IHasAccount) tab.content).getServiceId() == account.serviceId) {
				((IHasAccount) tab.content).updated(account, refreshContacts);
			}
		}
	}

	@Override
	public void searchResult(byte serviceId, List<PersonalInfo> infos) {
		for (TabInfo tab : tabsAccount) {
			if ((tab.content instanceof SearchUsersView) && ((SearchUsersView) tab.content).getServiceId() == serviceId) {
				((SearchUsersView) tab.content).searchResult(infos);
			}
		}
		for (TabInfo tab : tabsChat) {
			if ((tab.content instanceof SearchUsersView) && ((SearchUsersView) tab.content).getServiceId() == serviceId) {
				((SearchUsersView) tab.content).searchResult(infos);
			}
		}
	}

	@Override
	public void textMessage(TextMessage message) {
		for (int i = 0; i < tabsChat.size(); i++) {
			TabInfo tab = tabsChat.get(i);
			if ((tab.content instanceof IHasAccount) && (tab.content instanceof IHasMessages) && ((IHasAccount) tab.content).getServiceId() == message.serviceId) {
				((IHasMessages) tab.content).messageReceived(message, i == tabHostChat.getCurrentTab());
			}
		}

		if ((!(tabHostChat.getCurrentView() instanceof IHasAccount) || ((IHasAccount) tabHostChat.getCurrentView()).getServiceId() != message.serviceId)
				&& (!(tabHostAccount.getCurrentView() instanceof IHasAccount) || ((IHasAccount) tabHostAccount.getCurrentView()).getServiceId() != message.serviceId)) {
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

		for (int i = 0; i < tabsAccount.size(); i++) {
			TabInfo tab = tabsAccount.get(i);
			if ((tab.content instanceof IHasAccount) && (tab.content instanceof IHasMessages) && ((IHasAccount) tab.content).getServiceId() == message.serviceId) {
				((IHasMessages) tab.content).messageReceived(message, false);
			}
		}
	}

	@Override
	public void fileProgress(long messageId, Buddy buddy, String filename, long totalSize, long sizeTransferred, boolean isReceive, String error) {
		IHasFileTransfer ft = null;

		for (TabInfo tab : tabsChat) {
			if (tab.content != null && tab.content instanceof IHasFileTransfer && ((IHasFileTransfer) tab.content).getServiceId() == buddy.serviceId) {
				ft = (IHasFileTransfer) tab.content;
			}
		}

		if (ft == null) {
			TabInfo tab = TabInfoFactory.createFileTransferTab(getEntryPoint(), buddy.serviceId);
			ft = (IHasFileTransfer) tab.content;
			addTab(tab, true);
			tabHostChat.setCurrentTabByTag(tab.tag);
		}

		ft.notifyFileProgress(messageId, buddy, filename, totalSize, sizeTransferred, isReceive, error);
	}

	@Override
	public void messageAck(Buddy buddy, long messageId, int level) {
		for (TabInfo tab : tabsChat) {
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid)) {
				((ConversationsView) tab.content).messageAck(messageId, level);
			}
		}
	}

	@Override
	public void typing(byte serviceId, String buddyUid) {
		for (int i = 0; i < tabsChat.size(); i++) {
			TabInfo tab = tabsChat.get(i);
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + serviceId + " " + buddyUid)) {
				((ConversationsView) tab.content).typing(tabHostChat.getCurrentTab() == i);
				break;
			}
		}
	}

	@Override
	public void removeAccount(AccountView account) {
		tabHostAccount.setCurrentTab(0);
		tabHostChat.setCurrentTab(0);

		for (int i = tabsChat.size() - 1; i >= 0; i--) {
			if (tabsChat.get(i).tag.indexOf(ConversationsView.class.getSimpleName() + " " + account.serviceId) > -1 || tabsChat.get(i).tag.equals(ContactList.class.getSimpleName() + " " + account.serviceId)) {
				tabsChat.remove(i);
			}
		}
		for (int i = tabsAccount.size() - 1; i >= 0; i--) {
			if (tabsAccount.get(i).tag.indexOf(ConversationsView.class.getSimpleName() + " " + account.serviceId) > -1 || tabsAccount.get(i).tag.equals(ContactList.class.getSimpleName() + " " + account.serviceId)) {
				tabsAccount.remove(i);
			}
		}

		if (tabsAccount.size() < 1) {
			getEntryPoint().addAccountEditorTab(null);
		}

		try {
			tabHostAccount.getTabWidget().setFocusable(false);
			tabHostAccount.clearAllTabs();
			tabHostChat.getTabWidget().setFocusable(false);
			tabHostChat.clearAllTabs();
		} catch (Exception e) {
			ServiceUtils.log(e);
		}

		for (TabInfo info : tabsAccount) {
			try {
				tabHostAccount.addTab(info.tabSpec);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}

		tabHostAccount.getTabWidget().setFocusable(true);

		for (TabInfo info : tabsChat) {
			try {
				tabHostChat.addTab(info.tabSpec);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}

		tabHostChat.getTabWidget().setFocusable(true);
	}

	private EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public void setCurrentChatsTab(int tab) {
		if (tab < tabsChat.size()) {
			tabHostChat.setCurrentTab(tab);
		}
	}

	@Override
	public void setCurrentAccountsTab(int tab) {
		if (tab < tabsAccount.size()) {
			tabHostAccount.setCurrentTab(tab);
		}
	}

	@Override
	public void configChanged() {
		setOrientation();

		for (TabInfo tab : tabsAccount) {
			if (tab.content != null) {
				tab.content.configChanged();
			}
		}
		for (TabInfo tab : tabsChat) {
			if (tab.content != null) {
				tab.content.configChanged();
			}
		}
		scrollToSelectedListenerAccount.onTabChanged(tabHostAccount.getCurrentTabTag());
		scrollToSelectedListenerChat.onTabChanged(tabHostChat.getCurrentTabTag());
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {
		if (tabHostChat.getCurrentTabTag().indexOf(PreferencesView.class.getSimpleName()) > -1) {
			return tabsChat.get(tabHostChat.getCurrentTab()).content.onKeyDown(i, event);
		}

		try {
			if (tabsAccount.get(tabHostAccount.getCurrentTab()).content.onKeyDown(i, event)) {
				return true;
			} else {
				return super.onKeyDown(i, event);
			}
		} catch (Exception e) {
			return super.onKeyDown(i, event);
		}
	}

	@Override
	public void availableChatsList(byte serviceId, List<MultiChatRoom> chats) {
		for (TabInfo tab : tabsChat) {
			if (tab.tag != null && tab.tag.equals(GroupChatsView.class.getSimpleName() + " " + serviceId)) {
				((GroupChatsView) tab.content).chatsList(chats);
			}
		}
	}

	@Override
	public void chatRoomOccupants(byte serviceId, String chatId, MultiChatRoomOccupants occupants) {
		for (TabInfo tab : tabsChat) {
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + serviceId + " " + chatId)) {
				((ConversationsView) tab.content).fillGroupChatView(occupants);
			}
		}
	}

	@Override
	public void serviceMessage(ServiceMessage message) {
		for (int i = 0; i < tabsChat.size(); i++) {
			TabInfo tab = tabsChat.get(i);
			if (tab.content != null && (tab.content instanceof IHasServiceMessages)) {
				((IHasServiceMessages) tab.content).serviceMessageReceived(message, i == tabHostChat.getCurrentTab());
			}
		}
		for (int i = 0; i < tabsAccount.size(); i++) {
			TabInfo tab = tabsAccount.get(i);
			if (tab.content != null && (tab.content instanceof IHasServiceMessages)) {
				((IHasServiceMessages) tab.content).serviceMessageReceived(message, i == tabHostChat.getCurrentTab());
			}
		}
	}

	@Override
	public void refreshAccounts() {
		try {
			List<AccountView> accounts = getEntryPoint().runtimeService.getAccounts(false);

			tabsAccount.clear();
			tabsChat.clear();

			tabHostChat.clearAllTabs();
			addTab(splash, true);

			for (int i = accounts.size() - 1; i >= 0; i--) {
				AccountView account = accounts.get(i);
				TabInfo info = TabInfoFactory.createContactList(getEntryPoint(), account);
				tabsAccount.add(0, info);
			}

			try {
				tabHostAccount.setCurrentTab(0);
				tabHostAccount.getTabWidget().setFocusable(false);
				tabHostAccount.clearAllTabs();
			} catch (Exception e) {
				ServiceUtils.log(e);
			}

			for (TabInfo info : tabsAccount) {
				try {
					tabHostAccount.addTab(info.tabSpec);
				} catch (Exception e) {
					ServiceUtils.log(e);
				}
			}

			tabHostAccount.getTabWidget().setFocusable(true);
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
		list.add(tabHostAccount.getCurrentTabTag());
		list.add(tabHostChat.getCurrentTabTag());
		return list;
	}
}
