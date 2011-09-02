package ua.snuk182.asia.view.mainscreen;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.TabInfoFactory;
import ua.snuk182.asia.view.IHasAccount;
import ua.snuk182.asia.view.IHasBuddy;
import ua.snuk182.asia.view.IHasFileTransfer;
import ua.snuk182.asia.view.IHasMessages;
import ua.snuk182.asia.view.IMainScreen;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.conversations.ConversationsView;
import ua.snuk182.asia.view.more.PersonalInfoView;
import ua.snuk182.asia.view.more.PreferencesView;
import ua.snuk182.asia.view.more.SearchUsersView;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;

public class SmartphoneScreen extends TabHost implements IMainScreen {
	
	public final ArrayList<TabInfo> tabs;
	
	private HorizontalScrollView tabScroller;
	
	private final List<OnTabChangeListener> tabChangeListeners = new ArrayList<OnTabChangeListener>();
	
	private final OnTabChangeListener tabChangeListener = new OnTabChangeListener() {
		
		@Override
		public void onTabChanged(String tabId) {
			for (OnTabChangeListener listener:tabChangeListeners){
				listener.onTabChanged(tabId);
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
		
		tabs =  new ArrayList<TabInfo>();
        
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.tab_layout, this);	
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setup(entryPoint.getLocalActivityManager());
		tabScroller = (HorizontalScrollView) findViewById(R.id.tabContainer);
    	
    	setOnTabChangedListener(tabChangeListener);
    	
    	addOnTabChangeListener(scrollToSelectedListener);    	
	}
	
	private void scrollToSelected(final String tabId) {
		tabScroller.post(new Runnable(){

			@Override
			public void run() {
				TabInfo desiredTab = getTabByTag(tabId);
				if (desiredTab == null){
					return;
				}
				View tabWidget = desiredTab.tabWidgetLayout;
				Rect rect = new Rect();
				tabScroller.getDrawingRect(rect);
				if (rect.left > tabWidget.getLeft()){
					tabScroller.scrollTo(tabWidget.getLeft(),0);	
					return;
				}
				if (rect.right < tabWidget.getRight()){
					tabScroller.scrollTo(tabWidget.getRight(),0);	
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
	
	public void addOnTabChangeListener(OnTabChangeListener listener){
		tabChangeListeners.add(listener);
	}
	public void removeOnTabChangeListener(OnTabChangeListener listener){
		tabChangeListeners.remove(listener);
	}
	
	protected EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}
	
	private boolean checkShowTabs() {
    	if (Build.VERSION.SDK_INT == 11){
    		getTabWidget().setVisibility(View.GONE);
    		return false;
    	}
    	String hideTabsStr;
		try {
			hideTabsStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_view_type));
			if (hideTabsStr!=null){
				boolean hideTabs = hideTabsStr.equals(getResources().getString(R.string.value_view_type_notabs));
				if (hideTabs){
					getTabWidget().setVisibility(View.GONE);
				} else {
					getTabWidget().setVisibility(View.VISIBLE);
				}
				return hideTabs;
			} else {
				getTabWidget().setVisibility(View.VISIBLE);				
			}		
		} catch (NullPointerException npe) {		
			getTabWidget().setVisibility(View.VISIBLE);	
		}
		return true;
	}
	
	@Override
	public void configChanged() {
		for (TabInfo tab:tabs){
			if (tab.content != null){
				tab.content.configChanged();
			}
		}
		//scrollToSelectedListener.onTabChanged(getCurrentTabTag());
	}
	
	@Override
	public void visualStyleUpdated() {
		if (checkShowTabs() && getEntryPoint().bgColor != EntryPoint.BGCOLOR_WALLPAPER){
			findViewById(R.id.divider).setVisibility(View.VISIBLE);
			findViewById(R.id.divider).setBackgroundColor(0x60202020);
		} else {
			findViewById(R.id.divider).setVisibility(View.GONE);
		}
		
		for (TabInfo tab:tabs){
			if (tab.content != null){
				tab.content.visualStyleUpdated();
				tab.tabWidgetLayout.color(getEntryPoint().bgColor);
			}
		}
		
		if (getCurrentView() != null && getCurrentView().getContext() instanceof PreferencesView){
			((PreferencesView)getCurrentView().getContext()).visualStyleUpdated();
		}
	}

	@Override
	public void addTab(TabInfo info, boolean setAsCurrent){
		if (info==null) return;
		
		boolean exists = getTabByTag(info.tag) != null;
		if (exists){
			return;
		}
		
		tabs.add(info);
		if (info.tabWidgetLayout!= null && info.tabWidgetLayout.getParent() != null){
			((ViewGroup)info.tabWidgetLayout.getParent()).removeView(info.tabWidgetLayout);
		}
		
		addTab(info.tabSpec);
		
		if (setAsCurrent){
			setCurrentTabByTag(info.tag);
		}
	}

	@Override
	public void onStart() {
		if (tabs.size() > 0 && tabs.get(getCurrentTab()).content != null){
			tabs.get(getCurrentTab()).content.onStart();
		}
	}
	
	@Override 
	public boolean checkAndSetCurrentTabByTag(String tag){
		for (int i=0; i<tabs.size(); i++){
    		if (tabs.get(i).tag.equals(tag)){
    			setCurrentTab(i);
    			return true;
    		}
    	}
		
		return false;
	}
	
	@Override
	public void removeTabByTag(String tag){
    	String currentTag = getCurrentTabTag();
    	
    	if (currentTag.equals(tag)){
    		setCurrentTab(0);
    	} 
    	
    	for (int i=0; i<tabs.size(); i++){
    		if (tabs.get(i).tag.equals(tag)){
    			removeTabAt(i);
    			break;
    		}
    	}   	
    }
    
	private void removeTabAt(int pos){
    	try {
    		setCurrentTab(0);
    		getTabWidget().setFocusable(false);
    		clearAllTabs();
    		tabs.remove(pos);	
    	} catch (Exception e) {
			ServiceUtils.log(e);
		}	
		
    	if (tabs.size()<1){
    		getEntryPoint().finish();
    	}
    	
    	for (TabInfo info:tabs){
    		try {
    			addTab(info.tabSpec);
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
	public void accountStateChanged(AccountView account) {
		for (TabInfo tab: tabs){
			if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==account.serviceId){
				((IHasAccount)tab.content).stateChanged(account);
			}
		}
	}

	@Override
	public void accountUpdated(AccountView account) {
		for (TabInfo tab: tabs){
			if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==account.serviceId){
				((IHasAccount)tab.content).updated(account);
			}
		}
	}

	@Override
	public void icon(byte serviceId, String uid) {
		for (TabInfo tab: tabs){
			if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==serviceId){
				ServiceUtils.log(uid);
				((IHasAccount)tab.content).bitmap(uid);
			}
		}
	}

	@Override
	public void buddyStateChanged(Buddy buddy) {
		for (TabInfo tab: tabs){
			if ((tab.content instanceof IHasBuddy) && ((IHasBuddy)tab.content).getServiceId()==buddy.serviceId){
				((IHasBuddy)tab.content).updateBuddyState(buddy);				
			}
		}
	}

	@Override
	public void connecting(byte serviceId, int progress) {
		for (TabInfo tab: tabs){
			if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==serviceId){
				((IHasAccount)tab.content).connectionState(progress);
			}
		}
	}

	@Override
	public void searchResult(byte serviceId, List<PersonalInfo> infos) {
		for (TabInfo tab: tabs){
			if ((tab.content instanceof SearchUsersView) && ((SearchUsersView)tab.content).getServiceId()==serviceId){
				((SearchUsersView)tab.content).searchResult(infos);
			}
		}
	}

	@Override
	public void textMessage(TextMessage message) {
		for (int i=0; i<tabs.size(); i++){
			TabInfo tab = tabs.get(i);
			if ((tab.content instanceof IHasAccount) && (tab.content instanceof IHasMessages) && ((IHasAccount)tab.content).getServiceId()==message.serviceId){
				((IHasMessages)tab.content).messageReceived(message, i==getCurrentTab());					
			}
		}
		
		if (!(getCurrentView() instanceof IHasAccount) || ((IHasAccount)getCurrentView()).getServiceId()!=message.serviceId){
			try {
				Buddy budddy = getEntryPoint().runtimeService.getBuddy(message.serviceId, message.from);
				budddy.unread++;
				getEntryPoint().runtimeService.setUnread(budddy, message);
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
		
		for (TabInfo tab: tabs){
			if (tab.content!= null && tab.content instanceof IHasFileTransfer && ((IHasFileTransfer) tab.content).getServiceId() == buddy.serviceId){
				ft = (IHasFileTransfer) tab.content;
			}
		}	
		
		if (ft == null){
			TabInfo tab = TabInfoFactory.createFileTransferTab(getEntryPoint(), buddy.serviceId);
			ft = (IHasFileTransfer) tab.content;
			addTab(tab, true);
			setCurrentTabByTag(tab.tag);
		}
		
		ft.notifyFileProgress(messageId, buddy, filename, totalSize, sizeTransferred, isReceive, error);
	}

	@Override
	public void messageAck(Buddy buddy, long messageId, int level) {
		for (TabInfo tab: tabs){
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid)){
				((ConversationsView) tab.content).messageAck(messageId, level);
			}
		}
	}

	@Override
	public void typing(byte serviceId, String buddyUid) {
		for (int i=0; i<tabs.size(); i++){
			TabInfo tab = tabs.get(i);
			if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + serviceId + " " + buddyUid)){
				((ConversationsView) tab.content).typing(getCurrentTab() == i);
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		try {
			for (int i=tabs.size()-1; i>=0; i--){
				if (tabs.get(i).tag.indexOf(PersonalInfoView.class.getSimpleName())>-1){
					tabs.remove(i);
				}
			}
			getEntryPoint().runtimeService.saveTabs(tabs);
			//unbindService(serviceConnection);
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} catch (RemoteException e){
			getEntryPoint().onRemoteCallFailed(e);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (getSelectedTab().content != null && getSelectedTab().content.getMainMenuId() > 0){
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
	
	private TabInfo getSelectedTab(){
		return tabs.get(getCurrentTab());
	}
	
	private TabInfo getTabByTag(String tag){
		for (TabInfo tab:tabs){
			if (tab.tag.equals(tag)){
				return tab;
			}
		}
		return null;
	}

	@Override
	public void removeAccount(AccountView account) {
		setCurrentTab(0);
    	
    	for (int i=tabs.size()-1; i>=0; i--){
			if (tabs.get(i).tag.indexOf(ConversationsView.class.getSimpleName()+" "+account.serviceId)>-1 ||
					tabs.get(i).tag.equals(ContactList.class.getSimpleName()+" "+account.serviceId)){
				tabs.remove(i);
			}
		}
    	
    	if (tabs.size()<1){
    		getEntryPoint().addAccountEditorTab(null);
    	}
    	
    	try {
    		getTabWidget().setFocusable(false);
    		clearAllTabs();
    	} catch (Exception e) {
			ServiceUtils.log(e);
		}	
		
    	for (TabInfo info:tabs){
    		try {
				addTab(info.tabSpec);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
    	}
		
		getTabWidget().setFocusable(true);
	}
	
	@Override
	public boolean onKeyDown(int i, KeyEvent event){
		if (getSelectedTab().content.onKeyDown(i, event)){
			return true;
		} else {
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
		setCurrentTab(tab);
	}

	@Override
	public void setCurrentAccountsTab(int tab) {
		setCurrentTab(tab);
	}

	
}
