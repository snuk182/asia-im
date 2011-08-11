package ua.snuk182.asia;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.IRuntimeService;
import ua.snuk182.asia.services.IRuntimeServiceCallback;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.TabInfoFactory;
import ua.snuk182.asia.view.IHasAccount;
import ua.snuk182.asia.view.IHasBuddy;
import ua.snuk182.asia.view.IHasFileTransfer;
import ua.snuk182.asia.view.IHasMessages;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.conversations.ConversationsView;
import ua.snuk182.asia.view.more.AccountManagerView;
import ua.snuk182.asia.view.more.AsiaCoreException;
import ua.snuk182.asia.view.more.HistoryView;
import ua.snuk182.asia.view.more.PersonalInfoView;
import ua.snuk182.asia.view.more.PreferencesView;
import ua.snuk182.asia.view.more.SearchUsersView;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

public class EntryPoint extends TabActivity {
	
	public IRuntimeService runtimeService = null;
	public ArrayList<TabInfo> tabs = null;
	private Intent serviceIntent = null;
	
	private Bundle appOptions;
	
	public int bgColor = 0xff7f7f80;
	public DisplayMetrics metrics = new DisplayMetrics();
	
	private HorizontalScrollView tabScroller;
	
	private final List<OnTabChangeListener> tabChangeListeners = new ArrayList<OnTabChangeListener>();
	
	protected ArrayList<TabInfo> savedTabs = null;
	protected int selectedTab = 0;
	private ServiceConnection serviceConnection = new ServiceConnection(){
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (runtimeService == null){
				runtimeService = IRuntimeService.Stub.asInterface(service);
    			try {
					runtimeService.registerCallback(serviceCallback);
					continueCreating(savedTabs, selectedTab);
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
			} else {
				ServiceUtils.log("onService connected run again");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			runtimeService = null;    
			serviceIntent = null;
			if (serviceConnection != null){
				getRuntimeService();
			}
		}
	};
	
	private final OnTabChangeListener tabChangeListener = new OnTabChangeListener() {
		
		@Override
		public void onTabChanged(String tabId) {
			for (OnTabChangeListener listener:tabChangeListeners){
				listener.onTabChanged(tabId);
			}		
		}
	};
	
	private final Runnable visualStyleUpdatedRunnable = new Runnable(){

		@Override
		public void run() {
			String orientation = getResources().getString(R.string.value_screen_orientation_system);
			
			try {
				orientation = getApplicationOptions().getString(getResources().getString(R.string.key_screen_orientation));
			} catch (NullPointerException npe) {	
				orientation = getResources().getString(R.string.value_screen_orientation_system);
				ServiceUtils.log(npe);
			} 
			if (getResources().getString(R.string.value_screen_orientation_sensor).equalsIgnoreCase(orientation)){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);	
			} else if (getResources().getString(R.string.value_screen_orientation_landscape).equalsIgnoreCase(orientation)){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);	
			} else if (getResources().getString(R.string.value_screen_orientation_portrait).equalsIgnoreCase(orientation)){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}					
			
			String bgType;
			
			try {
				bgType = getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));
			} catch (NullPointerException npe) {	
				bgType = null;
				ServiceUtils.log(npe);
			}
			if (bgType == null || bgType.equals("wallpaper")){
				bgColor = 0xff7f7f80;
				Bitmap original = ((BitmapDrawable)getWallpaper()).getBitmap();	
				BitmapDrawable wallpaper = new BitmapDrawable(original);
				//wallpaper.setGravity(Gravity.CENTER);
				
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
					wallpaper.setGravity(Gravity.CENTER|Gravity.FILL_VERTICAL);
				} else {
					wallpaper.setGravity(Gravity.CENTER|Gravity.FILL_HORIZONTAL);
				}
				getTabHost().setBackgroundDrawable(wallpaper);
				findViewById(R.id.divider).setVisibility(View.GONE);
			}else {
				try {
					bgColor = (int) Long.parseLong(bgType);
					getTabHost().setBackgroundColor(bgColor);
					
					if (checkShowTabs()){
						findViewById(R.id.divider).setVisibility(View.VISIBLE);
						//findViewById(R.id.divider).setBackgroundColor((bgColor ^ 0xffffff));
						findViewById(R.id.divider).setBackgroundColor(0x60202020);
					} else {
						findViewById(R.id.divider).setVisibility(View.GONE);
					}							
				} catch (NumberFormatException e) {				
					ServiceUtils.log(e);
				}
			}	
			
			for (TabInfo tab:tabs){
				if (tab.content != null){
					tab.content.visualStyleUpdated();
					tab.tabWidgetLayout.color(bgColor);
				}
			}
			
			if (getTabHost().getCurrentView() != null && getTabHost().getCurrentView().getContext() instanceof PreferencesView){
				((PreferencesView)getTabHost().getCurrentView().getContext()).visualStyleUpdated();
			}
		}
		
	};
	
	private final OnTabChangeListener scrollToSelectedListener = new OnTabChangeListener() {
		
		@Override
		public void onTabChanged(final String tabId) {
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
	};
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);   
        threadMsgHandler.post(new Runnable(){

			@Override
			public void run() {
				try {
					serviceCallback.visualStyleUpdated();
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				scrollToSelectedListener.onTabChanged(getTabHost().getCurrentTabTag());
			}
        	
        });
    }
	
    public Bundle getApplicationOptions() {
		return appOptions;
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	//getSharedPreferences("AsiaTotalParams", 0).edit().clear().commit();    
    	
    	super.onCreate(savedInstanceState);  
    	
    	setContentView(R.layout.tab_layout);
    	
    	tabScroller = (HorizontalScrollView) findViewById(R.id.tabContainer);
    	
    	getTabHost().setOnTabChangedListener(tabChangeListener);
    	
    	addOnTabChangeListener(scrollToSelectedListener);
    	
    	final ArrayList<TabInfo> savedTabs;
    	final int selectedTab;
    	if(savedInstanceState!=null){
    		savedTabs = savedInstanceState.getParcelableArrayList("tabs");        
        	selectedTab = savedInstanceState.getInt("selected");
        	serviceIntent = savedInstanceState.getParcelable("serviceIntent");
    	} else {
    		savedTabs = null;
    		selectedTab = 0;
    	}
    	tabs =  new ArrayList<TabInfo>();
        addTab(TabInfoFactory.createSplashscreenTab(this));  
        
        this.savedTabs = savedTabs;
        this.selectedTab = selectedTab;
        getRuntimeService();		
    }
    
    private void getRuntimeService() {
    	getTabHost().post(new Runnable(){

			@Override
			public void run() {
				
				if (serviceIntent == null){
		    		serviceIntent = new Intent("ua.snuk182.asia.services.RuntimeService");
		    	}
				startService(serviceIntent);
				bindService(serviceIntent, serviceConnection, 0);    					
			}});		
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
    	if (getSelectedTab().content != null && getSelectedTab().content.getMainMenuId() > 0){
    		menu.clear();
        	
        	MenuInflater inflater = getMenuInflater();
    		inflater.inflate(getSelectedTab().content.getMainMenuId(), menu);
    		
        	return getSelectedTab().content.onPrepareOptionsMenu(menu);
    	}
    	return false;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item){
    	return getSelectedTab().content.onOptionsItemSelected(item);
    }
    
    @Override
    public void onNewIntent(Intent intent){
    	if (intent==null || intent.getData()==null){
    		return;
    	}
    	String notificationTag = intent.getData().getHost();
    	
    	if (notificationTag.startsWith(ConversationsView.class.getSimpleName())){
    		String[] splits = notificationTag.split(" ");
    			
    		byte serviceId = Byte.parseByte(splits[1]);
			try {				
				getConversationTab(runtimeService.getBuddy(serviceId, splits[2]));
			} catch (NullPointerException npe) {			
				ServiceUtils.log(npe);
			} catch (Exception e) {
				onRemoteCallFailed(e);
			}
    	}
    	
    	if (notificationTag.startsWith(ContactList.class.getSimpleName())){
    		try {
				getTabHost().setCurrentTabByTag(notificationTag);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
    	}		    	
    }
    
    private void continueCreating(ArrayList<TabInfo> savedTabs, int selectedTab) {
    	try {
    		runtimeService.setAppVisible(true);
    		appOptions = runtimeService.getApplicationOptions();
    		if (savedTabs == null){
    			savedTabs = (ArrayList<TabInfo>) runtimeService.getSavedTabs();
    		}
    		if (savedTabs!=null){
    			for (TabInfo tab:savedTabs){    				
    				addTab(TabInfoFactory.recreateTabContent(this, tab));    	
    			}
    		} else {
    			List<AccountView> protocols = runtimeService.getProtocolServices();
    			    			
    			if (protocols.size()<1){
    				addAccountEditorTab(null);
    			}
    			
    			for (AccountView protocolView:protocols){				
    				addAccountTab(protocolView);
    			}
    			selectedTab = 0;
    		}
			if (tabs.get(0).content instanceof Splashscreen){
				removeTabAt(0);
			}
			
			getTabHost().setCurrentTab(selectedTab);					
			
			checkShowTabs();
			
			serviceCallback.visualStyleUpdated();
			
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		} catch (AsiaCoreException e) {
			ServiceUtils.log(e);
		} 		
		
	}
    
    public boolean checkShowTabs() {
    	if (Build.VERSION.SDK_INT == 11){
    		getTabWidget().setVisibility(View.GONE);
    		return false;
    	}
    	String hideTabsStr;
		try {
			hideTabsStr = getApplicationOptions().getString(getResources().getString(R.string.key_hide_tabs));
			if (hideTabsStr!=null){
				boolean hideTabs = Boolean.parseBoolean(hideTabsStr);
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

	public void addAccountEditorTab(AccountView account){
		TabInfo tab = TabInfoFactory.createAccountEditTab(this, account);
    	addTab(tab);
    	getTabHost().setCurrentTab(tabs.size()-1);
    }
    
    public TabInfo addAccountTab(AccountView account) {
    	try {
    		TabInfo info = TabInfoFactory.createContactList(this, account);
			addTab(info);
			return info;
		} catch (AsiaCoreException e) {
			ServiceUtils.log(e);
			return null;
		}		
	}
    
    public void getConversationTab(Buddy buddy){
    	for (int i=0; i<tabs.size(); i++){
    		String tag = ConversationsView.class.getSimpleName()+" "+buddy.serviceId+" "+buddy.protocolUid;
    		if (tabs.get(i).tag.equals(tag)){
    			getTabHost().setCurrentTab(i);
    			return;
    		}
    	}
    	
    	try {
    		AccountView account = runtimeService.getAccountView(buddy.serviceId);
			ArrayList<Buddy> buddyList = new ArrayList<Buddy>(1);
	    	buddyList.add(buddy);
	    	TabInfo info;
			info = TabInfoFactory.createConversation(this, account, buddyList);
			addTab(info);
	    	
	    	getTabHost().setCurrentTabByTag(info.tag);
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (AsiaCoreException e) {
			ServiceUtils.log(e);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		}
    	
    }
    
    public void addHistoryTab(Buddy buddy){
    	String tag = HistoryView.class.getSimpleName()+" "+buddy.serviceId+" "+buddy.protocolUid;    	
    	String currentTag = getTabHost().getCurrentTabTag();    	
    	getTabHost().setCurrentTabByTag(tag);
    	
    	if (!currentTag.equals(getTabHost().getCurrentTabTag())){
    		return;
    	}
    	
    	TabInfo info = TabInfoFactory.createHistoryTab(this, buddy);
    	addTab(info);
    	
    	getTabHost().setCurrentTabByTag(info.tag);
    }
    
    public void addSearchTab(AccountView account){
    	String tag = SearchUsersView.class.getSimpleName()+" "+account.serviceId;    	
    	String currentTag = getTabHost().getCurrentTabTag();    	
    	getTabHost().setCurrentTabByTag(tag);
    	
    	if (!currentTag.equals(getTabHost().getCurrentTabTag())){
    		return;
    	}    
    	
    	TabInfo tab = TabInfoFactory.createSearchTab(this, account);
    	addTab(tab);
    	getTabHost().setCurrentTabByTag(tab.tag);
    }
    
    public void removeTabByTag(String tag){
    	String currentTag = getTabHost().getCurrentTabTag();
    	
    	if (currentTag.equals(tag)){
    		getTabHost().setCurrentTab(0);
    	} 
    	
    	for (int i=0; i<tabs.size(); i++){
    		if (tabs.get(i).tag.equals(tag)){
    			removeTabAt(i);
    			break;
    		}
    	}   	
    }
    
    public void removeTabAt(int pos){
    	TabHost tabHost = getTabHost();
    	
    	try {
    		tabHost.setCurrentTab(0);
    		tabHost.getTabWidget().setFocusable(false);
    		tabHost.clearAllTabs();
    		tabs.remove(pos);	
    	} catch (Exception e) {
			ServiceUtils.log(e);
		}	
		
    	if (tabs.size()<1){
    		finish();
    	}
    	
    	for (TabInfo info:tabs){
    		try {
    			tabHost.addTab(info.tabSpec);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
    	}
		
    	tabHost.getTabWidget().setFocusable(true);					
    }
    
    public void removeAccount(AccountView account){
    	TabHost tabHost = getTabHost();
    	tabHost.setCurrentTab(0);
    	
    	for (int i=tabs.size()-1; i>=0; i--){
			if (tabs.get(i).tag.indexOf(ConversationsView.class.getSimpleName()+" "+account.serviceId)>-1 ||
					tabs.get(i).tag.equals(ContactList.class.getSimpleName()+" "+account.serviceId)){
				tabs.remove(i);
			}
		}
    	
    	if (tabs.size()<1){
    		addAccountEditorTab(null);
    	}
    	
    	try {
    		tabHost.getTabWidget().setFocusable(false);
    		tabHost.clearAllTabs();
    	} catch (Exception e) {
			ServiceUtils.log(e);
		}	
		
    	for (TabInfo info:tabs){
    		try {
				tabHost.addTab(info.tabSpec);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
    	}
		
		tabHost.getTabWidget().setFocusable(true);
    }

	private final IRuntimeServiceCallback serviceCallback = new IRuntimeServiceCallback.Stub() {
		
		private void accountStateChanged(AccountView account){
			for (TabInfo tab: tabs){
				if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==account.serviceId){
					((IHasAccount)tab.content).stateChanged(account);
				}
			}
			
			/*if (!(getTabHost().getCurrentView() instanceof IHasAccount) || ((IHasAccount)getTabHost().getCurrentView()).getServiceId()!=account.protocolServiceId){
				try {
					notificationToast(account.ownName+getResources().getString(R.string.label_offline));
				} catch (Exception e) {
				}
			}*/
		}
		
		private void accountContactListUpdated(AccountView account){
			for (TabInfo tab: tabs){
				if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==account.serviceId){
					((IHasAccount)tab.content).updated(account);
				}
			}
		}

		@Override
		public void accountConnected(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountStateChanged(account);		
				}
				
			});
		}

		@Override
		public void icon(final byte serviceId, final String uid) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					for (TabInfo tab: tabs){
						if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==serviceId){
							ServiceUtils.log(uid);
							((IHasAccount)tab.content).bitmap(uid);
						}
					}					
				}
				
			});
			
		}

		@Override
		public void contactListUpdated(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountContactListUpdated(account);					
				}
				
			});		
		}

		@Override
		public void buddyStateChanged(final Buddy buddy) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					for (TabInfo tab: tabs){
						if ((tab.content instanceof IHasBuddy) && ((IHasBuddy)tab.content).getServiceId()==buddy.serviceId){
							((IHasBuddy)tab.content).updateBuddyState(buddy);				
						}
					}
				}
				
			});
			
		}

		@Override
		public void connecting(final byte serviceId, final int progress) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					for (TabInfo tab: tabs){
						if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==serviceId){
							((IHasAccount)tab.content).connectionState(progress);
						}
					}						
				}
				
			});
			
		}

		@Override
		public void accountUpdated(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountContactListUpdated(account);					
				}
				
			});				
		}

		@Override
		public void serviceMessage(final ServiceMessage msg) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					if (msg == null) return;
					//Context context = getTabHost().getCurrentView().getContext();
					if (msg.type.equals(ServiceMessage.TYPE_AUTHREQUEST)){
						
						ViewUtils.showAuthRequestDialog(msg, EntryPoint.this);
						
						/*if (context instanceof IHasAccount && ((IHasAccount)context).getServiceId() == msg.serviceId && context instanceof IHasMessages){
							((IHasMessages)context).serviceMessageReceived(msg);
						} else {
							runtimeService.setServiceMessageUnread(msg.serviceId, true, msg);
						}*/
					}	
				}
				
			});
			
		}

		@Override
		public void searchResult(final byte serviceId, final List<PersonalInfo> infos) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					for (TabInfo tab: tabs){
						if ((tab.content instanceof SearchUsersView) && ((SearchUsersView)tab.content).getServiceId()==serviceId){
							((SearchUsersView)tab.content).searchResult(infos);
						}
					}	
					
				}
				
			});
		}

		@Override
		public void groupAdded(final BuddyGroup group, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_group) + group.name + getApplicationContext().getResources().getString(R.string.label_added), Toast.LENGTH_LONG).show();
				}
				
			});
		}

		@Override
		public void buddyAdded(final Buddy buddy, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_buddy) + buddy.name + " (" + buddy.protocolUid + ")" + getApplicationContext().getResources().getString(R.string.label_added),
							Toast.LENGTH_LONG).show();
					
					for (TabInfo tab:tabs){
						if (tab.content != null){
							tab.content.visualStyleUpdated();
						}
					}
				}
				
			});
		}

		@Override
		public void buddyRemoved(final Buddy buddy, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_buddy) + buddy.name + " (" + buddy.protocolUid + ")" + getApplicationContext().getResources().getString(R.string.label_deleted),
							Toast.LENGTH_LONG).show();
					for (TabInfo tab:tabs){
						if (tab.content != null){
							tab.content.visualStyleUpdated();
							tab.tabWidgetLayout.color(bgColor);
						}
					}
				}
				
			});
			
		}

		@Override
		public void groupRemoved(final BuddyGroup group, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_group) + group.name + getApplicationContext().getResources().getString(R.string.label_deleted), Toast.LENGTH_LONG).show();
					
					for (TabInfo tab:tabs){
						if (tab.content != null){
							tab.content.visualStyleUpdated();
							tab.tabWidgetLayout.color(bgColor);
						}
					}
				}
				
			});
		}

		@Override
		public void buddyEdited(final Buddy buddy, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_buddy) + buddy.name + " (" + buddy.protocolUid + ")" + getApplicationContext().getResources().getString(R.string.label_modified),
							Toast.LENGTH_LONG).show();
				}
				
			});
			buddyStateChanged(buddy);
		}

		@Override
		public void groupEdited(final BuddyGroup group, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_group) + group.name + getApplicationContext().getResources().getString(R.string.label_modified), Toast.LENGTH_LONG).show();
					
					for (TabInfo tab:tabs){
						if (tab.content != null){
							tab.content.visualStyleUpdated();
							tab.tabWidgetLayout.color(bgColor);
						}
					}
				}
				
			});
			
		}

		@Override
		public void disconnected(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountStateChanged(account);
				}
				
			});
			
		}

		@Override
		public void textMessage(final TextMessage message) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					for (int i=0; i<tabs.size(); i++){
						TabInfo tab = tabs.get(i);
						if ((tab.content instanceof IHasAccount) && (tab.content instanceof IHasMessages) && ((IHasAccount)tab.content).getServiceId()==message.serviceId){
							((IHasMessages)tab.content).messageReceived(message, i==getTabHost().getCurrentTab());					
						}
					}
					
					if (!(getTabHost().getCurrentView() instanceof IHasAccount) || ((IHasAccount)getTabHost().getCurrentView()).getServiceId()!=message.serviceId){
						try {
							runtimeService.setUnread(runtimeService.getBuddy(message.serviceId, message.from), message);
						} catch (NullPointerException npe) {
							ServiceUtils.log(npe);
						} catch (RemoteException e) {
							onRemoteCallFailed(e);
						}
					}
				}
				
			});
			
		}

		@Override
		public void accountAdded(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					int current = getTabHost().getCurrentTab();
					TabInfo tab = addAccountTab(account);		
					tab.content.visualStyleUpdated();
					tab.tabWidgetLayout.color(bgColor);
					removeTabAt(current);
				}
				
			});			
		}

		@Override
		public void status(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountStateChanged(account);		
				}
				
			});
		}

		@Override
		public void accountRemoved(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					removeAccount(account);	
				}
				
			});
		}

		@Override
		public void visualStyleUpdated() throws RemoteException {
			final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			display.getMetrics(metrics);
			
			threadMsgHandler.post(visualStyleUpdatedRunnable);
		}

		@Override
		public void fileMessage(final FileMessage message) throws RemoteException {
			if (message == null) return;			
			
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					ViewUtils.showFileRequestDialog(message, EntryPoint.this);					
				}
				
			});
		}

		@Override
		public void fileProgress(final long messageId, final Buddy buddy, final String filename, final long totalSize, final long sizeTransferred, final boolean isReceive, final String error) throws RemoteException {
			
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					IHasFileTransfer ft = null;
					
					for (TabInfo tab: tabs){
						if (tab.content!= null && tab.content instanceof IHasFileTransfer && ((IHasFileTransfer) tab.content).getServiceId() == buddy.serviceId){
							ft = (IHasFileTransfer) tab.content;
						}
					}	
					
					if (ft == null){
						TabInfo tab = TabInfoFactory.createFileTransferTab(EntryPoint.this, buddy.serviceId);
						ft = (IHasFileTransfer) tab.content;
						addTab(tab);
						getTabHost().setCurrentTabByTag(tab.tag);
					}
					
					ft.notifyFileProgress(messageId, buddy, filename, totalSize, sizeTransferred, isReceive, error);
				}
				
			});
		}

		@Override
		public void messageAck(final Buddy buddy, final long messageId, final int level) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					for (TabInfo tab: tabs){
						if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid)){
							((ConversationsView) tab.content).messageAck(messageId, level);
						}
					}	
				}
				
			});		
		}

		@Override
		public void personalInfo(final Buddy buddy, final PersonalInfo info) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					TabInfo tab = TabInfoFactory.createPersonalInfoTab(EntryPoint.this, buddy, info);	
					addTab(tab);
					getTabHost().setCurrentTabByTag(tab.tag);
				}
				
			});	
		}

		@Override
		public void typing(final byte serviceId, final String buddyUid) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					for (int i=0; i<tabs.size(); i++){
						TabInfo tab = tabs.get(i);
						if (tab.tag != null && tab.tag.equals(ConversationsView.class.getSimpleName() + " " + serviceId + " " + buddyUid)){
							((ConversationsView) tab.content).typing(getTabHost().getCurrentTab() == i);
							break;
						}
					}	
				}
				
			});	
		}		
	};
	private Runnable endActivityRunnable = new Runnable(){

		@Override
		public void run() {
			finish();
			System.gc();
		}};
	
	public void addTab(TabInfo info){
		if (info==null) return;
		
		boolean exists = getTabByTag(info.tag) != null;
		if (exists){
			return;
		}
		
		tabs.add(info);
		if (info.tabWidgetLayout!= null && info.tabWidgetLayout.getParent() != null){
			((ViewGroup)info.tabWidgetLayout.getParent()).removeView(info.tabWidgetLayout);
		}
		getTabHost().addTab(info.tabSpec);
	}

	public IRuntimeServiceCallback getServiceCallback() {
		return serviceCallback;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		try {
			serviceCallback.visualStyleUpdated();
		} catch (NullPointerException npe) {					
		} catch (RemoteException e) {
			ServiceUtils.log(e);
		}
		
		if (tabs.get(getTabHost().getCurrentTab()).content != null){
			tabs.get(getTabHost().getCurrentTab()).content.onResume();
		}
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
	public void onStart(){
		super.onStart();	
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		
		if (runtimeService!=null){
			try {
				runtimeService.setAppVisible(true);
			} catch (RemoteException e) {
				ServiceUtils.log(e);
				runtimeService = null;
				serviceIntent = null;
				
				savedTabs = null;
		        selectedTab = 0;
		       
				getRuntimeService();
			}
		} else {
			savedTabs = null;
	        selectedTab = 0;
			getRuntimeService();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle){
		bundle.putParcelableArrayList("tabs", tabs);
		bundle.putInt("selected", getTabHost().getCurrentTab());
		bundle.putParcelable("serviceIntent", serviceIntent);
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		if(runtimeService!=null){
			try {
				runtimeService.setAppVisible(false);
			} catch (NullPointerException npe) {
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				ServiceUtils.log(e);
			}
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		ServiceUtils.log("entry point destroyed");
		try {
			for (int i=tabs.size()-1; i>=0; i--){
				if (tabs.get(i).tag.indexOf(PersonalInfoView.class.getSimpleName())>-1){
					tabs.remove(i);
				}
			}
			runtimeService.saveTabs(tabs);
			//unbindService(serviceConnection);
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} catch (RemoteException e){
			onRemoteCallFailed(e);
		}
	}
	
	public void exit() {
		new Thread(){
			@Override
			public void run(){
				try {
					runtimeService.prepareExit();
					serviceConnection = null;
					/*unbindService(serviceConnection);
					stopService(serviceIntent);	*/				
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					onRemoteCallFailed(e);
				}	
			}
		}.start();	
		finish();
	}

	public void addAccountsManagerTab() {		
		String tag = AccountManagerView.class.getSimpleName();		
		String currentTag = getTabHost().getCurrentTabTag();    	
    	getTabHost().setCurrentTabByTag(tag);
    	
    	if (!currentTag.equals(getTabHost().getCurrentTabTag())){
    		return;
    	} 
		
		addTab(TabInfoFactory.createAccountManager(this));
		getTabHost().setCurrentTabByTag(AccountManagerView.class.getSimpleName());
	}
	
	public void addPreferencesTab(AccountView account){
		String tag = account != null ? PreferencesView.class.getSimpleName()+" "+account.serviceId : PreferencesView.class.getSimpleName();
		
		String currentTag = getTabHost().getCurrentTabTag();
    	
    	getTabHost().setCurrentTabByTag(tag);
    	
    	if (!currentTag.equals(getTabHost().getCurrentTabTag())){
    		return;
    	}    	
    	
		TabInfo info = TabInfoFactory.createPreferencesTab(this, account);		
		addTab(info);
		getTabHost().setCurrentTabByTag(tag);
	}

	public void setXStatus(AccountView account) {
		try {
			runtimeService.setXStatus(account);
			for (TabInfo tab: tabs){
				if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==account.serviceId){
					((IHasAccount)tab.content).stateChanged(account);
				}
			}
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		}		
	}

	public ArrayList<TabInfo> getTabs() {
		return tabs;
	}

	public void removeBuddy(Buddy buddy) {
		try {
			runtimeService.removeBuddy(buddy);
			removeTabByTag("conversation "+buddy.serviceId+" "+buddy.protocolUid);
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		}		
	}
	
	public TabInfo getSelectedTab(){
		return tabs.get(getTabHost().getCurrentTab());
	}
	
	public TabInfo getTabByTag(String tag){
		for (TabInfo tab:tabs){
			if (tab.tag.equals(tag)){
				return tab;
			}
		}
		return null;
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
	
	public void onRemoteCallFailed(Throwable e){
		ServiceUtils.log(e);
		threadMsgHandler.post(endActivityRunnable );
	}
		
	private final Handler threadMsgHandler = new Handler();
}