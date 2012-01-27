package ua.snuk182.asia.view.groupchats;

import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.app.ProgressDialog;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class GroupChatsView extends RelativeLayout implements ITabContent {
	
	protected final AccountView account;
	
	private final TabWidgetLayout tabWidgetLayout;
	private final LinearLayout layout;
	
	private final EditText searcher;
	private final ImageButton searchBtn;
	
	private int size = 32;
	
	ProgressDialog progressDialog;

	private final Runnable getAvailableRoomsRunnable = new Runnable() {
		
		@Override
		public void run() {
			try {				
				getEntryPoint().runtimeService.requestAvailableChatRooms(account.serviceId);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e1) {
				getEntryPoint().onRemoteCallFailed(e1);
			}
		}
	};
	
	public GroupChatsView (EntryPoint entryPoint, AccountView account) {
		super(entryPoint);
		this.account = account;
		
		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.group_chats, this);
		setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout = (LinearLayout) findViewById(R.id.chatslist);
		searcher = (EditText) findViewById(R.id.searchtext);
		searchBtn = (ImageButton) findViewById(R.id.searchbtn);
		
		tabWidgetLayout = new TabWidgetLayout(entryPoint);
		setFocusable(false);
		
		int size = (int) (32*entryPoint.metrics.density);
		tabWidgetLayout.setLayoutParams(new LinearLayout.LayoutParams(size, size));
		tabWidgetLayout.setImageResource(R.drawable.logo_32px);
		//tabWidgetLayout.setScaleType(ScaleType.CENTER_INSIDE);
		tabWidgetLayout.setText(account.getSafeName());
		
		searchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				filterResult();
			}
		});
		
		visualStyleUpdated();
		requestAvailableChatRooms();
	}

	protected void filterResult() {
		for (int i=0; i<layout.getChildCount(); i++){
			View item = layout.getChildAt(i);
			if (item instanceof GroupChatsViewItem){
				GroupChatsViewItem chatItem = (GroupChatsViewItem) item;
				chatItem.checkSearchStringMatch(searcher.getText().toString());
			}
		}
	}

	@Override
	public int getMainMenuId() {
		return R.menu.group_chats_menu;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem showTabsItem = menu.findItem(R.id.menuitem_showtabs);
		String hideTabsStr;
		try {
			hideTabsStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_view_type));
			if (hideTabsStr != null) {
				boolean hideTabs = hideTabsStr.equals(getResources().getString(R.string.value_view_type_notabs));
				showTabsItem.setVisible(hideTabs);
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
		case R.id.menuitem_close:
			// kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(),
			// 0);
			closeMe();
			returnToBuddyList();
			break;
		case R.id.menuitem_showtabs:
			ViewUtils.showTabChangeMenu(getEntryPoint());
			break;
		case R.id.menuitem_refresh_chat_list:
			requestAvailableChatRooms();
			break;
		case R.id.menuitem_create_groupchat:
			ViewUtils.newGroupChatDialog(getEntryPoint(), account);
			break;
		}
		return false;
	}
	
	protected void requestAvailableChatRooms(){
		getEntryPoint().threadMsgHandler.post(getAvailableRoomsRunnable );
	}
	
	protected void closeMe() {
		getEntryPoint().mainScreen.removeTabByTag(GroupChatsView.class.getSimpleName()+" "+account.serviceId);
		
		//fix for a bug of loosing FILL_PARENT
		((View)getEntryPoint().mainScreen).setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			returnToBuddyList();
			return true;
		}
		if (i == KeyEvent.KEYCODE_SEARCH){
			filterResult();
			return true;
		}

		return false;
	}

	private void returnToBuddyList() {
		getEntryPoint().mainScreen.checkAndSetCurrentTabByTag(ContactList.class.getSimpleName() + " " + account.serviceId);
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		return tabWidgetLayout;
	}

	@Override
	public void visualStyleUpdated() {
		if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER){
			setBackgroundColor(0x60000000);
		 } else {
			try {
				setBackgroundColor(0);
			 } catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}
		
		String itemSizeStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_cl_item_size));
		if (itemSizeStr == null || itemSizeStr.equals(getResources().getString(R.string.value_size_medium))){
			size = 48;
		} else if (itemSizeStr.equals(getResources().getString(R.string.value_size_big))){
			size = 64;
		} else if (itemSizeStr.equals(getResources().getString(R.string.value_size_small))){
			size = 32;
		} else {
			size = 24;
		}
	}

	@Override
	public void onStart() {}

	@Override
	public void configChanged() {}

	public void chatsList(List<MultiChatRoom> chats) {
		layout.removeAllViews();
		for (final MultiChatRoom chat : chats){
			getEntryPoint().threadMsgHandler.post(new Runnable(){

				@Override
				public void run() {
					addAvailableChat(chat);
				}
				
			});
		}		
	}

	private void addAvailableChat(final MultiChatRoom chat) {
		GroupChatsViewItem item = new GroupChatsViewItem(getEntryPoint(), chat.name, chat.protocolUid, searcher.getText().toString(), size);
		item.color();
		item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getChatInfo(chat);
			}
		});
		
		item.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				ViewUtils.showManualGroupChatOptions(getEntryPoint(), account, chat);
				return false;
			}
		});
		layout.addView(item);
	}

	private void getChatInfo(MultiChatRoom chat) {
		try {
			PersonalInfo info = getEntryPoint().runtimeService.getChatInfo(account.serviceId, chat.protocolUid);
			ViewUtils.showChatInfo(account, chat, info, getEntryPoint());
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e1) {
			getEntryPoint().onRemoteCallFailed(e1);
		}
	}

}
