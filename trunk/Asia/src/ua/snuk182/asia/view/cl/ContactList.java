package ua.snuk182.asia.view.cl;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.view.IHasBuddy;
import ua.snuk182.asia.view.IHasMessages;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.grid.ContactListGridDrawer;
import ua.snuk182.asia.view.cl.list.ContactListListDrawer;
import ua.snuk182.asia.view.cl.twocolumn.DoubleContactListListDrawer;
import ua.snuk182.asia.view.conversations.ConversationsView;
import ua.snuk182.asia.view.more.StatusTextView;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class ContactList extends LinearLayout implements ITabContent, IHasMessages, IHasBuddy  {
	
	private AccountView account;
	
	private IContactListDrawer contactList;
	private ImageView ownIcon;
	private ImageView xStatusIcon;
	private ImageView statusIcon;
	private StatusTextView notificationText;
	private TabWidgetLayout tabWidgetLayout;
	private LinearLayout statusPanel;
	public boolean sort = true;
	
	private ProgressBar progressBar;
	
	private Bitmap icon = null;
	
	private final Runnable bitmapGot = new Runnable(){

		@Override
		public void run() {
			setOwnIcon(icon);							
		}
		
	};
	
	private final Handler handler = new Handler();

	/*private OnTabChangeListener tabChangeListener = new OnTabChangeListener(){

		@Override
		public void onTabChanged(String tabId) {
			if (tabId.equals(ContactList.class.getSimpleName()+" "+account.protocolServiceId)){
				tabWidgetLayout.getTabIcon().setImageResource(ServiceUtils.getStatusResIdByAccount(account, 32, false));
			}			
		}
		
	};*/
	
	public ContactList(EntryPoint entryPoint, AccountView account) {
		super(entryPoint);
		
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.contact_list_grid, this);	
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setOrientation(LinearLayout.VERTICAL);
		
		tabWidgetLayout = new TabWidgetLayout(entryPoint);
	
		this.account = account;
		
		tabWidgetLayout.getTabName().setText(account.ownName == null ? account.protocolUid : account.ownName);
		
		statusPanel = (LinearLayout) findViewById(R.id.statuspanel);
		
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		progressBar.setMax(10);
		
		notificationText = (StatusTextView)findViewById(R.id.statusText);
		
		xStatusIcon = (ImageView) findViewById(R.id.xstatusicon);
		ownIcon = (ImageView) findViewById(R.id.ownericon);
		ownIcon.setVisibility(View.GONE);
		statusIcon = (ImageView) findViewById(R.id.statusicon);
		statusIcon.setVisibility(View.GONE);
		
		//entryPoint.addOnTabChangeListener(tabChangeListener);
		
		requestIcon();
		
		stateChanged(null);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		MenuItem connectItem = menu.findItem(R.id.menuitem_connect);
		if (account.getConnectionState() != AccountService.STATE_DISCONNECTED){
			connectItem.setTitle(getResources().getString(R.string.label_disconnect));
			connectItem.setIcon(R.drawable.logout);
		} else {
			connectItem.setTitle(getResources().getString(R.string.label_connect));
			connectItem.setIcon(R.drawable.login);
		}
		
		MenuItem searchItem = menu.findItem(R.id.menuitem_search);
		if (searchItem != null){
			if (account!=null && account.getConnectionState() == AccountService.STATE_CONNECTED){
				searchItem.setVisible(true);
			} else {
				searchItem.setVisible(false);
			}
		}
		
		MenuItem editXStatusItem = menu.findItem(R.id.menuitem_editxstatus);
		if (editXStatusItem != null){
			if (account.xStatus>-1){
				editXStatusItem.setIcon(ServiceUtils.getXStatusArray32(getContext(), account.protocolName).getDrawable(account.xStatus));
				//editXStatusItem.setTitle(account.xStatusName);
			} else {
				editXStatusItem.setIcon(R.drawable.xstatus_none);
			}
		}
		
		MenuItem visibilityItem = menu.findItem(R.id.menuitem_visibility);
		if (visibilityItem != null){
			visibilityItem.setVisible(account.getConnectionState() != Buddy.ST_OFFLINE);
			if (account.getConnectionState() != Buddy.ST_OFFLINE){
				int arrayId = ServiceUtils.getVisibilityArrayIdByAccountVisibilityId(account.visibility);
				visibilityItem.setIcon(ServiceUtils.getVisibilityIcons(getContext(), account.protocolName).getDrawable(arrayId));
				visibilityItem.setTitle(ServiceUtils.getVisibilityNames(getContext(), account.protocolName).getString(arrayId));
			} 
		}
		
		MenuItem editStatusItem = menu.findItem(R.id.menuitem_editstatus);
		editStatusItem.setIcon(ServiceUtils.getStatusResIdByAccountMedium(getContext(), account, true));
		
		MenuItem showTabsItem = menu.findItem(R.id.menuitem_showtabs);
		String hideTabsStr;
		try {
			hideTabsStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_view_type));
			if (hideTabsStr!=null){
				showTabsItem.setVisible(hideTabsStr.equals(getResources().getString(R.string.value_view_type_notabs)));
			} else {
				showTabsItem.setVisible(false);
			}
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} 
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_search:
			getEntryPoint().addSearchTab(account);
			break;
	    case R.id.menuitem_exit:
	    	getEntryPoint().exit();			
	    	return true;
	    case R.id.menuitem_showtabs:
	    	ViewUtils.showTabChangeMenu(getEntryPoint());
	    	break;
	    case R.id.menuitem_account:
	    	getEntryPoint().addPreferencesTab(account);
	        return true;
	    case R.id.menuitem_prefs:
	    	getEntryPoint().addPreferencesTab(null);
	    	return true;
	    case R.id.menuitem_visibility:
	    	ViewUtils.showVisibilityMenu(getEntryPoint(), account);
	    	return true;
	    case R.id.menuitem_connect:	
	    	new Thread(){
	    		@Override
	    		public void run(){
	    			if (account.getConnectionState() == AccountService.STATE_DISCONNECTED){
	    				try {
	    					getEntryPoint().runtimeService.connect(account.serviceId);
	    				} catch (NullPointerException npe) {
	    					ServiceUtils.log(npe);
	    				} catch (RemoteException e) {
	    					getEntryPoint().onRemoteCallFailed(e);
	    				}  
	    			} else {
	    				try {
	    					getEntryPoint().runtimeService.disconnect(account.serviceId);
	    				} catch (NullPointerException npe) {	
	    					ServiceUtils.log(npe);
	    				} catch (RemoteException e) {
	    					getEntryPoint().onRemoteCallFailed(e);
	    				}
	    			}	 
	    		}
	    	}.start();   	
	    	return true;
	    case R.id.menuitem_editstatus:
	    	ViewUtils.menuEditStatus(getEntryPoint(), account);
	    	return true;
	    case R.id.menuitem_editxstatus:
	    	ViewUtils.menuEditXStatus(getEntryPoint(), account);
	    	return true;
	    case R.id.menuitem_about:
	    	AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
	    	alert.setMessage(getResources().getString(R.string.app_name)+" "+getResources().getString(R.string.version)+"."+getResources().getString(R.string.label_about_text)).setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       }).create().show();
	    	return true;
	    case R.id.menuitem_accounts:
	    	getEntryPoint().addAccountsManagerTab();
	    	break;
	    }
	    return false;
	}
	
	public void updateView() {
		try {
			account.merge(getEntryPoint().runtimeService.getAccountView(account.serviceId));
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e1) {
			getEntryPoint().onRemoteCallFailed(e1);
		}
		
		try {
			String doSort = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_sort_lists));
			if (doSort !=null){
				sort = Boolean.parseBoolean(doSort);
			}
		} catch (NullPointerException npe) {		
			ServiceUtils.log(npe);
		} 
		
		String clType = account.options.getString(getResources().getString(R.string.key_list_type));
    	if (clType == null){
    		clType = getResources().getString(R.string.value_list_type_grid);
    	}
		if (contactList == null || (!clType.equals(contactList.getType()))){
			if (contactList!=null){
				removeView((View) contactList);
			}
			
	    	if ((clType == null || clType.equals(getResources().getString(R.string.value_list_type_grid)))){
	    		contactList = new ContactListGridDrawer(getEntryPoint(), account, this);
	    	} else if (clType.equals(getResources().getString(R.string.value_list_type_list))){
	    		//contactList = new ContactListListDrawer(getEntryPoint(), account);
	    		contactList = new ContactListListDrawer(getEntryPoint(), account, this);
	    	} else {
	    		contactList = new DoubleContactListListDrawer(getEntryPoint(), account, this);
	    	}
	    	
	    	addView((View)contactList, 0);
		}
		
		String hideTabsStr;
		try {
			hideTabsStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_view_type));
			if (hideTabsStr!=null){
				if (hideTabsStr.equals(getResources().getString(R.string.value_view_type_notabs))){
					statusIcon.setVisibility(View.VISIBLE);
					statusIcon.setImageDrawable(getResources().getDrawable(ServiceUtils.getStatusResIdByAccountMedium(getContext(), account, false)));
				} else {
					statusIcon.setVisibility(View.GONE);
				}
			} else {
				statusIcon.setVisibility(View.GONE);
			}
			
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} 
		
		updated(null);
	}
	
	public void updated(AccountView origin){
		if (origin!=null){
			this.account.merge(origin);
		}
		if (account.getConnectionState() != AccountService.STATE_CONNECTING && account.getConnectionState() != AccountService.STATE_DISCONNECTING){
			if (account.xStatus>-1){
				xStatusIcon.setVisibility(View.VISIBLE);
				xStatusIcon.setImageDrawable(ServiceUtils.getXStatusArray32(getEntryPoint(), account.protocolName).getDrawable(account.xStatus));
				notificationText.setTextAndFormat(
						(account.xStatusName!= null && account.xStatusName.length()>0 ? account.xStatusName+": " : "")+
						(account.xStatusText!=null && account.xStatusText.length()>0 ? account.xStatusText : ""));
			} else {
				xStatusIcon.setVisibility(View.GONE);
				notificationText.setText("");
			}
		}		
		
		tabWidgetLayout.getTabName().setText(account.ownName == null ? account.protocolUid : account.ownName);
		
		contactList.updateView();
	}

	private void requestIcon() {
		new Thread("Contact list icon request "+getAccount().getAccountId()){
			@Override
			public void run(){
				icon = account.getIcon(getEntryPoint());
				handler.post(bitmapGot);								
			}
		}.start();		
	}

	/*@Override
	public void onFocusChange(View v, boolean hasFocus) {
		contactList.getChildAt(0).requestFocus();			
	}
	*/
	@Override
	public void messageReceived(TextMessage message, boolean activeTab){
		if (message==null || message.from==null) return;
		
		if (activeTab || (!getEntryPoint().mainScreen.getCurrentChatsTabTag().equals(ConversationsView.class.getSimpleName() + " " + getServiceId() + " " + message.from))){
			tabWidgetLayout.getTabIcon().setImageResource(R.drawable.message_medium);
			contactList.messageReceived(message);
		}
	}
	
	@Override
	public void updateBuddyState(Buddy buddy){
		contactList.updateBuddyState(buddy);
		if (contactList.hasUnreadMessages()){
			tabWidgetLayout.getTabIcon().setImageResource(R.drawable.message_medium);
		} else {
			tabWidgetLayout.getTabIcon().setImageResource(ServiceUtils.getStatusResIdByAccountMedium(getContext(), account, false));
		}
	}

	public ImageView getOwnIcon() {
		return ownIcon;
	}
	
	private void setOwnIcon(Bitmap icon) {
		if (icon==null) {
			ownIcon.setVisibility(View.GONE);
			return;
		}
		
		ownIcon.setImageBitmap(ViewUtils.scaleBitmap(icon, (int) (32*getEntryPoint().metrics.density), true));
		ownIcon.setVisibility(View.VISIBLE);
	}
	
	@Override 
	public boolean onKeyDown(int i, KeyEvent event) {

		  /*if (i == KeyEvent.KEYCODE_BACK) {
		    Toast.makeText(getContext(), getResources().getString(R.string.label_sorry_back_button), Toast.LENGTH_SHORT).show();
		    return true; 
		  }*/
		  if (i == KeyEvent.KEYCODE_SEARCH){
			  return true;
		  }

		  return false;
	}

	@Override
	public int getMainMenuId() {
		return ServiceUtils.getMenuResIdByAccount(getContext(), account);
	}

	@Override
	public int getServiceId() {
		if (account == null){
			return -1;
		}
		return account.serviceId;
	}

	public AccountView getAccount() {
		return account;
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		return tabWidgetLayout;
	}

	@Override
	public void stateChanged(AccountView account) {
		this.account.merge(account);
		statusPanel.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		tabWidgetLayout.getTabIcon().setImageResource(ServiceUtils.getStatusResIdByAccountMedium(getContext(), this.account, false));
		updateView();		
	}

	public EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}

	@Override
	public void visualStyleUpdated() {
		updateView();	
		
		String bgType;
		
		try {
			bgType = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));
		} catch (NullPointerException npe) {
			bgType = null;
			ServiceUtils.log(npe);
		} if (bgType == null || bgType.equals("wallpaper")){
			notificationText.setBackgroundColor(0x60000000);
			notificationText.setTextColor(0xffffffff);
			((View) contactList).setBackgroundColor(0x60000000);
		}else {
			try {
				int color = (int) Long.parseLong(bgType);
				notificationText.setBackgroundColor(0);
				notificationText.setTextColor((color-0xff000000)>0x777777?0xff000000:0xffffffff);
				((View) contactList).setBackgroundColor(0);
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}		
	}
	
	@Override
	public void connectionState(int state) {
		statusPanel.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(state);
		account.setConnectionState(AccountService.STATE_CONNECTING);
		tabWidgetLayout.getTabIcon().setImageResource(ServiceUtils.getStatusResIdByAccountMedium(getContext(), account, false));
	}

	@Override
	public void onStart() {
			
	}

	@Override
	public void bitmap(String uid) {
		if (uid.equals(account.protocolUid)){
			requestIcon();	
		} else {
			contactList.bitmap(uid);
		}
	}

	@Override
	public void configChanged() {
		contactList.configChanged();
	}	
}
