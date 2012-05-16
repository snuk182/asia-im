package ua.snuk182.asia;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.IRuntimeService;
import ua.snuk182.asia.services.IRuntimeServiceCallback;
import ua.snuk182.asia.services.ServiceStoredPreferences;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.TabInfoFactory;
import ua.snuk182.asia.view.IMainScreen;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.conversations.ConversationsView;
import ua.snuk182.asia.view.groupchats.GroupChatsView;
import ua.snuk182.asia.view.mainscreen.SmartphoneScreen;
import ua.snuk182.asia.view.mainscreen.TabletScreen;
import ua.snuk182.asia.view.more.AccountActivityView;
import ua.snuk182.asia.view.more.AccountManagerView;
import ua.snuk182.asia.view.more.AsiaCoreException;
import ua.snuk182.asia.view.more.HistoryView;
import ua.snuk182.asia.view.more.MasterPasswordView;
import ua.snuk182.asia.view.more.PreferencesView;
import ua.snuk182.asia.view.more.SearchUsersView;
import android.app.ActivityGroup;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * The main and only activity for an application.
 * 
 * @author Sergiy Plygun
 *
 * TODO rewrite into Fragments instead of ActivityGroup
 */
public class EntryPoint extends ActivityGroup {
	
	//various parameter keys for saving state
	private static final String SAVEDSTATE_SERVICE_INTENT = "serviceIntent";
	private static final String SAVEDSTATE_SELECTED_CHAT = "selectedChat";
	private static final String SAVEDSTATE_SELECTED_ACC = "selectedAcc";
	private static final String SAVEDSTATE_TABS = "tabs";	
	
	//the background color for wallpaper mode. also acts as wallpaper mode marker.
	public static final int BGCOLOR_WALLPAPER = 0xff7f7f80;
	
	public IRuntimeService runtimeService = null;
	private Intent serviceIntent = null;
	
	public IMainScreen mainScreen;
	public ProgressDialog progressDialog;
	
	private Bundle appOptions;
	
	public static String tabStyle = "slim";
	
	public static int bgColor = BGCOLOR_WALLPAPER;
	
	public DisplayMetrics metrics = new DisplayMetrics();
	
	public boolean dontDrawSmileys = false;
	public boolean menuOnTabLongclick = false;

	public BitmapDrawable wallpaper = null;
	
	private Method invalidateOptionsMenuMethod = null;
	
	private Bundle savedState;
	private ServiceConnection serviceConnection = new ServiceConnection(){
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (runtimeService == null){
				runtimeService = IRuntimeService.Stub.asInterface(service);
    			try {
					runtimeService.registerCallback(serviceCallback);
					continueCreating();
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
			finish();
		}
	};
	
	private final Runnable startRunnable = new Runnable() {
		
		@Override
		public void run() {
			updateStyle();
			
			String view;
			
			//detecting view type, smartphone or tablet
			try {
				view = ServiceStoredPreferences.getOption(EntryPoint.this, getString(R.string.key_view_type));
			} catch (NullPointerException npe) {	
				view = null;
			} 
			
			if (view != null && view.equals(getString(R.string.value_view_type_tablet))){
				mainScreen = new TabletScreen(EntryPoint.this, null);
			} else {
				mainScreen = new SmartphoneScreen(EntryPoint.this, null);
			}
			
			setContentView((View)mainScreen);
			
			//detecting if master password for start activity is required
			String masterPw = ServiceStoredPreferences.getOption(getApplicationContext(), getResources().getString(R.string.key_master_password));
			Boolean needPassword = masterPw != null && masterPw.length()>0;
			if (needPassword){
				addMasterPasswordRequestTab();
				toggleWaitscreen(false);				
			} else {
				getRuntimeService();	
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
			
			String dontDrawSmileysStr = getApplicationOptions().getString(getResources().getString(R.string.key_dont_draw_smileys));
			dontDrawSmileys = dontDrawSmileysStr!=null ? Boolean.parseBoolean(dontDrawSmileysStr) : false;
			
			updateWallpaper();	
			
			mainScreen.visualStyleUpdated();
		}
		
	};
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig); 
        getMetrics();
        threadMsgHandler.post(new Runnable(){

			@Override
			public void run() {
				mainScreen.configChanged();
			}
        	
        });
    }
	
	private void addMasterPasswordRequestTab() {
		String tag = MasterPasswordView.class.getSimpleName();
		
		if (mainScreen.checkAndSetCurrentTabByTag(tag)){
			return;
		} 
		
		TabInfo info = TabInfoFactory.createMasterPasswordTab(this);		
		mainScreen.addTab(info, true);
	}

	private void updateStyle() {
		String bgType = ServiceStoredPreferences.getOption(getApplicationContext(), getResources().getString(R.string.key_bg_type));	
    	
		if (bgType == null || bgType.equals("wallpaper")){
			bgColor = BGCOLOR_WALLPAPER;
		}else {
			try {
				bgColor = (int) Long.parseLong(bgType);
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}
		
		if (bgColor < BGCOLOR_WALLPAPER){
			setTheme(R.style.DarkTheme);	  				
		}
		
		if (bgColor > BGCOLOR_WALLPAPER){
			setTheme(R.style.LightTheme);
		}
		
		if (bgColor == BGCOLOR_WALLPAPER){
			setTheme(R.style.TransparentTheme);	    	
		}
		
		tabStyle = ServiceStoredPreferences.getOption(getApplicationContext(), getResources().getString(R.string.key_tab_style));		
    	if (tabStyle == null) {	
			tabStyle = getString(R.string.value_tab_style_slim);
		}
    	
    	menuOnTabLongclick = Boolean.parseBoolean(ServiceStoredPreferences.getOption(getApplicationContext(), getResources().getString(R.string.key_toggle_menu_on_tab_longclick)));		
    	
    	getMetrics();    	
	}
	
    private void updateWallpaper() {
    	if (bgColor == BGCOLOR_WALLPAPER){
			try {
				//calculating correct wallpaper dimentions - the less wp side should be equal to bigger screen side
				int heightPx = (int) (metrics.heightPixels * metrics.density);
				int widthPx = (int) (metrics.widthPixels * metrics.density);
				
				Bitmap original = ViewUtils.scaleBitmap(((BitmapDrawable)getWallpaper()).getBitmap(),   
						(heightPx > widthPx) ? heightPx : widthPx, 
								true);	
				wallpaper = new BitmapDrawable(getResources(), original);
				wallpaper.setGravity(Gravity.CENTER);
				wallpaper.setFilterBitmap(false);
				wallpaper.setDither(false);
				mainScreen.setBackgroundDrawable(wallpaper);
			} catch (Exception e) {
				mainScreen.setBackgroundColor(bgColor);		
			}
		} else {
			mainScreen.setBackgroundColor(bgColor);					
		}
	}

	public Bundle getApplicationOptions() {
		return appOptions;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);  
    	
    	//do not load title bar on small screens. this looks like a hack, but no official api for detecting tablet mode is available
    	if (!ServiceUtils.isTablet(getApplicationContext())){
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    	}
    	
    	getNonVersionedMethods();
    	
    	setContentView(R.layout.dummy);
    	
    	savedState = savedInstanceState;
    	
    	toggleWaitscreen(true);
        
    	threadMsgHandler.post(startRunnable);	
    }
    
	//request older api methods, if available
    private void getNonVersionedMethods() {
		try {
			invalidateOptionsMenuMethod = getClass().getMethod("invalidateOptionsMenu", new Class[] {});
		} catch (NoSuchMethodException e) {}
	}

    /**
     * Shows or hides waitscreen.
     * 
     * @param show true if screen needs to be shown, false - hidden.
     */
	public void toggleWaitscreen(final boolean show) {
    	
    	if (show){
    		try {
				progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.label_wait), true);
				progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.logo_96px));
				progressDialog.setCancelable(true);
			} catch (Exception e) {
				//TODO may be Window Leaked
				ServiceUtils.log(e);
			}
    	} else {
    		if (progressDialog != null){
    			progressDialog.hide();
    			progressDialog = null;
    		}
    	}
	}

	private void getRuntimeService() {
    	threadMsgHandler.post(new Runnable(){

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
    	if (mainScreen != null){
    		boolean result = mainScreen.onPrepareOptionsMenu(menu);
    		return result;
    	} else {
    		return false;
    	}
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item){
    	return mainScreen.onOptionsItemSelected(item);
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
				mainScreen.checkAndSetCurrentTabByTag(notificationTag);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
    	}		    	
    }
    
    /**
     * Continue UI creation after the service has been connected.
     * 
     */
    private void continueCreating() {
    	try {
    		appOptions = runtimeService.getApplicationOptions();
    		
    		ArrayList<TabInfo> savedTabs;
        	int selectedAccTab = 0;
        	int selectedChatTab = 0;
        	if(savedState!=null){
        		savedTabs = savedState.getParcelableArrayList(SAVEDSTATE_TABS);        
            	selectedAccTab = savedState.getInt(SAVEDSTATE_SELECTED_ACC);
        		selectedChatTab = savedState.getInt(SAVEDSTATE_SELECTED_CHAT);
            	serviceIntent = savedState.getParcelable(SAVEDSTATE_SERVICE_INTENT);
        	} else {
        		savedTabs = null;
        	}
    		
    		if (savedTabs!=null){
    			for (TabInfo tab:savedTabs){    				
    				mainScreen.addTab(TabInfoFactory.recreateTabContent(this, tab), false);    	
    			}
    		} else {
    			List<AccountView> protocols = runtimeService.getAccounts(false);
    			    			
    			if (protocols.size()<1){
    				addAccountEditorTab(null);
    			}
    			
    			for (AccountView protocolView:protocols){				
    				addAccountTab(protocolView);
    			}
    		}
			mainScreen.setCurrentAccountsTab(selectedAccTab);
			mainScreen.setCurrentChatsTab(selectedChatTab);
			
			runtimeService.setCurrentTabs(mainScreen.getCurrentTabs());    		
			
			serviceCallback.visualStyleUpdated();	
			
			//if master password was asked, remove its tab 
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.removeTabByTag(MasterPasswordView.class.getSimpleName());
				}
				
			});
		} catch (NullPointerException npe) {	
			npe.printStackTrace();
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		} catch (AsiaCoreException e) {
			ServiceUtils.log(e);
		} 		
		toggleWaitscreen(false);
	}

	public void addAccountEditorTab(AccountView account){
		TabInfo tab = TabInfoFactory.createAccountEditTab(this, account);
    	mainScreen.addTab(tab, true);    	
    }
    
    public TabInfo addAccountTab(AccountView account) {
    	try {
    		TabInfo info = TabInfoFactory.createContactList(this, account);
			mainScreen.addTab(info, false);
			return info;
		} catch (AsiaCoreException e) {
			ServiceUtils.log(e);
			return null;
		}		
	}
    
    /**
     * Brings conversation tab to front for a buddy. If no such tab opened, opens it.
     * 
     * @param buddy
     */
    public void getConversationTab(Buddy buddy){
    	String tag = buddy.getChatTag();
    	
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	try {
    		AccountView account = runtimeService.getAccountView(buddy.serviceId);
			TabInfo info = TabInfoFactory.createConversation(this, account, buddy);
			mainScreen.addTab(info, true);
    	} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (AsiaCoreException e) {
			ServiceUtils.log(e);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		}    	
    }
    
    /**
     * Brings activity tab to front for an account. If no such tab opened, opens it.
     * 
     * @param account
     */
    public void getAccountActivityTab(AccountView account) {
    	String tag = AccountActivityView.class.getSimpleName()+" "+account.getAccountId();
    	
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	try {
    		TabInfo info = TabInfoFactory.createAccountActivityTab(this, account);
			mainScreen.addTab(info, true);
    	} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} 
    }
    
    /**
     * Brings history tab to front for a buddy. If no such tab opened, opens it.
     * 
     * @param buddy
     */
    public void getHistoryTab(Buddy buddy){
    	String tag = HistoryView.class.getSimpleName()+" "+buddy.serviceId+" "+buddy.protocolUid;    	
    	
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	TabInfo info = TabInfoFactory.createHistoryTab(this, buddy);
    	mainScreen.addTab(info, true);    	
    }
    
    /**
     * Brings buddy search tab to front for an account. If no such tab opened, opens it.
     * 
     * @param account
     */
    public void getSearchTab(AccountView account){
    	String tag = SearchUsersView.class.getSimpleName()+" "+account.serviceId;    	
    	   
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	TabInfo tab = TabInfoFactory.createSearchTab(this, account);
    	mainScreen.addTab(tab, true);
    }
    
    /**
     * Brings group chats tab to front for an account. If no such tab opened, opens it.
     * 
     * @param account
     */
    public void getMyGroupChatsTab(AccountView account){
    	String tag = GroupChatsView.class.getSimpleName()+" "+account.serviceId;    	
    	   
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	TabInfo tab = TabInfoFactory.createGroupChatsTab(this, account);
    	mainScreen.addTab(tab, true);
    }
    
    /**
     * Brings account manager tab to front for an account. If no such tab opened, opens it.
     */
    public void addAccountsManagerTab() {		
		String tag = AccountManagerView.class.getSimpleName();		
		if (mainScreen.checkAndSetCurrentTabByTag(tag)){
			return;
		} 
		
		TabInfo tab = TabInfoFactory.createAccountManager(this);
		mainScreen.addTab(tab, true);
	}
	
    /**
     * Brings preferences tab to front. If no such tab opened, opens it.
     * 
     * @param account if set, searches for account settings, otherwise application preferences will be the target.
     */
    public void addPreferencesTab(AccountView account){
		String tag = account != null ? PreferencesView.class.getSimpleName()+" "+account.serviceId : PreferencesView.class.getSimpleName();
		
		if (mainScreen.checkAndSetCurrentTabByTag(tag)){
			return;
		} 
		
		TabInfo info = TabInfoFactory.createPreferencesTab(this, account);		
		mainScreen.addTab(info, true);
	}

	public void removeAccount(AccountView account){
    	mainScreen.removeAccount(account);
    }

	/**
	 * Service callback.
	 */
    private final IRuntimeServiceCallback serviceCallback = new IRuntimeServiceCallback.Stub() {
		
		private void accountStateChanged(AccountView account, boolean refreshContacts){
			mainScreen.accountStateChanged(account, refreshContacts);
		}
		

		@Override
		public void accountConnected(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountStateChanged(account, false);		
				}
				
			});
		}

		@Override
		public void icon(final byte serviceId, final String uid) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.icon(serviceId, uid);					
				}
				
			});
			
		}

		@Override
		public void contactListUpdated(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.accountUpdated(account, true);		
				}
				
			});		
		}

		@Override
		public void buddyStateChanged(final Buddy buddy) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.buddyStateChanged(buddy);
				}
				
			});
			
		}

		@Override
		public void connecting(final byte serviceId, final int progress) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.connecting(serviceId, progress);						
				}
				
			});
			
		}

		@Override
		public void accountUpdated(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.accountUpdated(account, false);		
				}
				
			});				
		}

		@Override
		public void serviceMessage(final ServiceMessage msg) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					if (msg == null) return;
					if (msg.type.equals(ServiceMessage.TYPE_AUTHREQUEST)){						
						ViewUtils.showAuthRequestDialog(msg, EntryPoint.this);						
					} else {
						mainScreen.serviceMessage(msg);
					}
				}
				
			});
			
		}

		@Override
		public void searchResult(final byte serviceId, final List<PersonalInfo> infos) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.searchResult(serviceId, infos);	
					
				}
				
			});
		}

		@Override
		public void groupAdded(final BuddyGroup group, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_group) + group.name + getApplicationContext().getResources().getString(R.string.label_added), Toast.LENGTH_LONG).show();
					mainScreen.visualStyleUpdated();
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
					mainScreen.visualStyleUpdated();
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
					mainScreen.visualStyleUpdated();
				}
				
			});
			
		}

		@Override
		public void groupRemoved(final BuddyGroup group, AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), getApplicationContext().getResources().getString(R.string.label_group) + group.name + getApplicationContext().getResources().getString(R.string.label_deleted), Toast.LENGTH_LONG).show();
					
					mainScreen.visualStyleUpdated();
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
					
					//mainScreen.visualStyleUpdated();
				}				
			});
			mainScreen.accountUpdated(account, true);
		}

		@Override
		public void disconnected(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountStateChanged(account, true);
				}
				
			});
			
		}

		@Override
		public void textMessage(final TextMessage message) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.textMessage(message);
				}
				
			});
			
		}

		@Override
		public void accountAdded(final AccountView account) throws RemoteException {
			TabInfo tab = addAccountTab(account);		
			tab.content.visualStyleUpdated();
			tab.tabWidgetLayout.color();
		}

		@Override
		public void status(final AccountView account) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					accountStateChanged(account, false);		
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
			getMetrics();
			
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
					mainScreen.fileProgress(messageId, buddy, filename, totalSize, sizeTransferred, isReceive, error);
				}
				
			});
		}

		@Override
		public void messageAck(final Buddy buddy, final long messageId, final int level) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.messageAck(buddy, messageId, level);	
				}
				
			});		
		}

		@Override
		public void personalInfo(final Buddy buddy, final PersonalInfo info) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					TabInfo tab = TabInfoFactory.createPersonalInfoTab(EntryPoint.this, buddy, info);	
					mainScreen.addTab(tab, true);
				}
				
			});	
		}

		@Override
		public void typing(final byte serviceId, final String buddyUid) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.typing(serviceId, buddyUid);	
				}
				
			});	
		}


		@Override
		public void availableChatsList(final byte serviceId, final List<MultiChatRoom> chats) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.availableChatsList(serviceId, chats);	
				}
				
			});	
		}


		@Override
		public void chatRoomOccupants(final byte serviceId, final String chatId, final MultiChatRoomOccupants occupants) throws RemoteException {
			threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					mainScreen.chatRoomOccupants(serviceId, chatId, occupants);	
				}
				
			});	
		}		
	};
	private Runnable endActivityRunnable = new Runnable(){

		@Override
		public void run() {
			finish();
			
			//just in case
			System.gc();
		}};

	public IRuntimeServiceCallback getServiceCallback() {
		return serviceCallback;
	}
	
	private void getMetrics() {
		final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		display.getMetrics(metrics);
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event){
		if (mainScreen.onKeyDown(i, event)){
			return true;
		} else {			
			if (i == KeyEvent.KEYCODE_BACK){
				
				//if master password is required, then force destroying activity for password being asked during next activity start
				String masterPw = ServiceStoredPreferences.getOption(getApplicationContext(), getResources().getString(R.string.key_master_password));
				Boolean needPassword = masterPw != null && masterPw.length()>0;
				
				if (needPassword){
					finish();
					return true;
				} else {
					return super.onKeyDown(i, event);
				}
			} else {
				return super.onKeyDown(i, event);
			}
		}
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		updateWallpaper();
		if (runtimeService!=null){
			try {
				
				//tell the service that we're watching the activity now
				runtimeService.setCurrentTabs(mainScreen.getCurrentTabs());
			} catch (RemoteException e) {
				ServiceUtils.log(e);
				runtimeService = null;
				serviceIntent = null;
				
				//mb service connection is dead? reconnect it
				getRuntimeService();
			}
		} else {
			getRuntimeService();
		}
		mainScreen.onStart();
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle){
		try {
			ArrayList<TabInfo> tabs = mainScreen.getTabs();
			for (int i=tabs.size()-1; i>=0; i--){
				TabInfo tab = tabs.get(i);
				
				//do not save tabs other than contact list, chats and history tabs
				if (tab.tag.indexOf(ContactList.class.getSimpleName())<0 &&
						tab.tag.indexOf(ConversationsView.class.getSimpleName())<0 &&
						tab.tag.indexOf(HistoryView.class.getSimpleName())<0){
					mainScreen.removeTabByTag(tab.tag);
				}
			}
			
			bundle.putParcelableArrayList(SAVEDSTATE_TABS, tabs);
			bundle.putInt(SAVEDSTATE_SELECTED_ACC, mainScreen.getCurrentAccountsTab());
			bundle.putInt(SAVEDSTATE_SELECTED_CHAT, mainScreen.getCurrentChatsTab());
			bundle.putParcelable(SAVEDSTATE_SERVICE_INTENT, serviceIntent);
		} catch (Exception e) {
			ServiceUtils.log(e);
		}
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
				
				//tell the service that we're moved off from Asia
				runtimeService.setCurrentTabs(new ArrayList<String>());
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
		mainScreen.onDestroy();
	}
	
	public void exit() {
		new Thread(){
			@Override
			public void run(){
				try {					
					//TODO fix the annoying WindowLeak
					runtimeService.prepareExit();
					unbindService(serviceConnection);
					serviceConnection = null;
					/*stopService(serviceIntent);	*/				
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					onRemoteCallFailed(e);
				}	
			}
		}.start();	
		toggleWaitscreen(false);
		finish();
	}

	public void setXStatus(AccountView account) {
		try {
			runtimeService.setXStatus(account);
			mainScreen.accountStateChanged(account, false);
			refreshMenu();
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		}		
	}

	public void removeBuddy(Buddy buddy) {
		try {
			runtimeService.removeBuddy(buddy);
			mainScreen.removeTabByTag("conversation "+buddy.serviceId+" "+buddy.protocolUid);
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			onRemoteCallFailed(e);
		}		
	}
	
	public void onRemoteCallFailed(Throwable e){
		ServiceUtils.log(e);
		//destroy activity. TODO fix to more appropriate action
		threadMsgHandler.post(endActivityRunnable );
	}
		
	public final Handler threadMsgHandler = new Handler();

	public void refreshAccounts() {
		mainScreen.refreshAccounts();
	}

	public void restart() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}
	
	/**
	 * Android versions >= 3.0 needs menu to be refreshed.
	 */
	public void refreshMenu() {
		if (Build.VERSION.SDK_INT > 10 && invalidateOptionsMenuMethod != null){
			try {
				invalidateOptionsMenuMethod.invoke(this);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}
	}

	public void proceedLoading() {
		toggleWaitscreen(true);
		getRuntimeService();	
	}

	public void setUnread(Buddy buddy, TextMessage message) throws RemoteException {
		runtimeService.setUnread(buddy, null);
		mainScreen.buddyStateChanged(buddy);
	}
}