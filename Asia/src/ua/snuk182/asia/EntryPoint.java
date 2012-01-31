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

public class EntryPoint extends ActivityGroup {
	
	private static final String SAVEDSTATE_SERVICE_INTENT = "serviceIntent";
	private static final String SAVEDSTATE_SELECTED_CHAT = "selectedChat";
	private static final String SAVEDSTATE_SELECTED_ACC = "selectedAcc";
	private static final String SAVEDSTATE_TABS = "tabs";
	public static final int BGCOLOR_WALLPAPER = 0xff7f7f80;
	public IRuntimeService runtimeService = null;
	private Intent serviceIntent = null;
	
	public IMainScreen mainScreen;
	public ProgressDialog progressDialog;
	
	private Bundle appOptions;
	
	public static String tabStyle = "slim";
	
	public static int bgColor = 0xff7f7f80;
	
	public DisplayMetrics metrics = new DisplayMetrics();
	
	public boolean dontDrawSmileys = false;
	
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
			/*runtimeService = null;    
			serviceIntent = null;
			if (serviceConnection != null){
				getRuntimeService();
			}*/
			
			finish();
		}
	};
	
	private final Runnable startRunnable = new Runnable() {
		
		@Override
		public void run() {
			updateStyle();
			
			String view;
			
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
			
	    	//addTab(TabInfoFactory.createSplashscreenTab(this));  
	    	
	        getRuntimeService();	
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
				//updateBackground();
			}
        	
        });
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
			tabStyle = "slim";
		}
    	
    	getMetrics();    	
	}
	
    private void updateWallpaper() {
    	if (bgColor == BGCOLOR_WALLPAPER){
			try {
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

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	//getSharedPreferences("AsiaTotalParams", 0).edit().clear().commit();    
    	
    	super.onCreate(savedInstanceState);  		
    	if (!ServiceUtils.isTablet(getApplicationContext())){
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    	}
    	
    	//Log.w("asia", Resources.getSystem().getConfiguration().toString());
    	
    	getNonVersionedMethods();
    	
    	setContentView(R.layout.dummy);
    	
    	savedState = savedInstanceState;
    	
    	toggleWaitscreen(true);
        
    	threadMsgHandler.post(startRunnable);	
    }
    
    private void getNonVersionedMethods() {
		try {
			invalidateOptionsMenuMethod = getClass().getMethod("invalidateOptionsMenu", new Class[] {});
		} catch (NoSuchMethodException e) {}
	}

	public void toggleWaitscreen(final boolean show) {
    	
    	if (show){
    		try {
				progressDialog = ProgressDialog.show(EntryPoint.this, "", getResources().getString(R.string.label_wait), true);
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
    	
    	/*if (progressDialog!=null){
			if (show){
				progressDialog.show();
			} else {
				progressDialog.hide();
				
				progressDialog = null;
			}
		} else {
			if (show){
				try {
					progressDialog = ProgressDialog.show(EntryPoint.this, "", getResources().getString(R.string.label_wait), true);
					progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.logo_96px));
					progressDialog.setCancelable(true);
				} catch (Exception e) {
					//TODO may be Window Leaked
					ServiceUtils.log(e);
				}
			}
		}*/
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
    
    private void continueCreating() {
    	try {
    		runtimeService.setAppVisible(true);
    		appOptions = runtimeService.getApplicationOptions();
    		
    		ArrayList<TabInfo> savedTabs;
        	int selectedAccTab = 0;
        	int selectedChatTab = 0;
        	if(savedState!=null){
        		savedTabs = savedState.getParcelableArrayList(SAVEDSTATE_TABS);        
            	/*if (mainScreen instanceof TabletScreen){
            		selectedAccTab = savedState.getInt(SAVEDSTATE_SELECTED_ACC);
            		selectedChatTab = savedState.getInt(SAVEDSTATE_SELECTED_CHAT);
            	} else {
            		selectedChatTab = savedState.getInt(SAVEDSTATE_SELECTED_CHAT);
            	}*/
        		selectedAccTab = savedState.getInt(SAVEDSTATE_SELECTED_ACC);
        		selectedChatTab = savedState.getInt(SAVEDSTATE_SELECTED_CHAT);
            	serviceIntent = savedState.getParcelable(SAVEDSTATE_SERVICE_INTENT);
        	} else {
        		savedTabs = null;
        	}
    		
    		if (savedTabs == null){
    			savedTabs = (ArrayList<TabInfo>) runtimeService.getSavedTabs();
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
			
			//checkShowTabs();
			
			serviceCallback.visualStyleUpdated();			
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
    
    public void getConversationTab(Buddy buddy){
    	String tag = ConversationsView.class.getSimpleName()+" "+buddy.serviceId+" "+buddy.protocolUid;
    	
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
    
    public void addAccountActivityTab(AccountView account) {
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
    
    public void addHistoryTab(Buddy buddy){
    	String tag = HistoryView.class.getSimpleName()+" "+buddy.serviceId+" "+buddy.protocolUid;    	
    	
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	TabInfo info = TabInfoFactory.createHistoryTab(this, buddy);
    	mainScreen.addTab(info, true);    	
    }
    
    public void addSearchTab(AccountView account){
    	String tag = SearchUsersView.class.getSimpleName()+" "+account.serviceId;    	
    	   
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	TabInfo tab = TabInfoFactory.createSearchTab(this, account);
    	mainScreen.addTab(tab, true);
    }
    
    public void addMyGroupChatsTab(AccountView account){
    	String tag = GroupChatsView.class.getSimpleName()+" "+account.serviceId;    	
    	   
    	if (mainScreen.checkAndSetCurrentTabByTag(tag)){
    		return;
    	}
    	
    	TabInfo tab = TabInfoFactory.createGroupChatsTab(this, account);
    	mainScreen.addTab(tab, true);
    }
    
    public void removeAccount(AccountView account){
    	mainScreen.removeAccount(account);
    }

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
					
					mainScreen.visualStyleUpdated();
				}
				
			});
			
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
			/*threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					//int current = getTabHost().getCurrentTab();
					
					//removeTabAt(current);
				}
				
			});		*/	
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
			return super.onKeyDown(i, event);
		}
	}
	
	/*@Override
	public void onStart(){
		super.onStart();	
		
		try {
			serviceCallback.visualStyleUpdated();
		} catch (NullPointerException npe) {					
		} catch (RemoteException e) {
			ServiceUtils.log(e);
		}
		
		if (mainScreen != null){
			mainScreen.onStart();
		}
	}*/
	
	@Override
	public void onRestart(){
		super.onRestart();
		updateWallpaper();
		if (runtimeService!=null){
			try {
				runtimeService.setAppVisible(true);
			} catch (RemoteException e) {
				ServiceUtils.log(e);
				runtimeService = null;
				serviceIntent = null;
				
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
				if (tabs.get(i).tag.indexOf(ContactList.class.getSimpleName())<0 &&
						tabs.get(i).tag.indexOf(ConversationsView.class.getSimpleName())<0 &&
						tabs.get(i).tag.indexOf(HistoryView.class.getSimpleName())<0){
					tabs.remove(i);
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
		mainScreen.onDestroy();
	}
	
	public void exit() {
		new Thread(){
			@Override
			public void run(){
				try {
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

	public void addAccountsManagerTab() {		
		String tag = AccountManagerView.class.getSimpleName();		
		if (mainScreen.checkAndSetCurrentTabByTag(tag)){
			return;
		} 
		
		TabInfo tab = TabInfoFactory.createAccountManager(this);
		mainScreen.addTab(tab, true);
	}
	
	public void addPreferencesTab(AccountView account){
		String tag = account != null ? PreferencesView.class.getSimpleName()+" "+account.serviceId : PreferencesView.class.getSimpleName();
		
		if (mainScreen.checkAndSetCurrentTabByTag(tag)){
			return;
		} 
		
		TabInfo info = TabInfoFactory.createPreferencesTab(this, account);		
		mainScreen.addTab(info, true);
	}

	public void setXStatus(AccountView account) {
		try {
			runtimeService.setXStatus(account);
			mainScreen.accountStateChanged(account, false);
			/*for (TabInfo tab: tabs){
				if ((tab.content instanceof IHasAccount) && ((IHasAccount)tab.content).getServiceId()==account.serviceId){
					((IHasAccount)tab.content).stateChanged(account);
				}
			}*/
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
	
	/*public TabInfo getSelectedTab(){
		return tabs.get(getTabHost().getCurrentTab());
	}
	
	public TabInfo getTabByTag(String tag){
		for (TabInfo tab:tabs){
			if (tab.tag.equals(tag)){
				return tab;
			}
		}
		return null;
	}*/
	
	public void onRemoteCallFailed(Throwable e){
		ServiceUtils.log(e);
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
	
	public void refreshMenu() {
		if (Build.VERSION.SDK_INT > 10 && invalidateOptionsMenuMethod != null){
			try {
				invalidateOptionsMenuMethod.invoke(this);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}
	}
}