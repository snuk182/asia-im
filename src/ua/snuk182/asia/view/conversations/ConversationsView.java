package ua.snuk182.asia.view.conversations;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.Message;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.HistorySaver;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.plus.ImageOrTextGridAdapter;
import ua.snuk182.asia.view.IHasBuddy;
import ua.snuk182.asia.view.IHasMessages;
import ua.snuk182.asia.view.IHasServiceMessages;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.cl.grid.ContactListGridItem;
import ua.snuk182.asia.view.more.StatusTextView;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import ua.snuk182.asia.view.more.fileexplorer.FileExplorer.FileExplorerAction;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ConversationsView extends RelativeLayout implements ITabContent, IHasBuddy, IHasMessages, IHasServiceMessages {

	public final Buddy buddy;
	private short accountConnectionState = AccountService.STATE_DISCONNECTED;
	//public AccountView account;
	private List<TextMessage> messages;
	private final ImageButton sendBtn;
	private final ImageButton smileyBtn;
	private final EditText textEditor;
	private final LinearLayout historyView;
	private final ScrollView scroller;
	
	private final ExpandableListView participantsView;
	private ConversationsViewParticipantsAdapter participantsAdapter;
	
	public String chatId = null;
	private InputMethodManager kbManager;
	
	private Bitmap icon = null;

	private final ImageView statusIcon;
	private final ImageView xstatusIcon;
	private final StatusTextView statusText;

	private TabWidgetLayout tabWidgetLayout;

	private boolean sendWithEnter = true;
	private boolean sendTyping = false;
	private boolean sendTypingGo = true;
	private boolean isImeFullScreen = true;
	private String printDateMode = null;
	
	private static final Random idGenerator = new Random();
	private final Handler handler = new Handler();
	private ImageOrTextGridAdapter smileAdapter;
	
	private final FileExplorerAction sendFileAction = new FileExplorerAction() {
		
		@Override
		public void action(File file) {
			Bundle bu = new Bundle();
			bu.putSerializable(File.class.getName(), file);
			try {
				getEntryPoint().runtimeService.sendFile(bu, buddy);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
		}
	};
	
	private static final FileFilter fileSendFilter = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return true;
		}
		
	};
	
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
	
	private Runnable freeToSendTypingRunable = new Runnable(){

		@Override
		public void run() {
			sendTypingGo = true;
		}
		
	};
	
	private Runnable endTypingRunnable = new Runnable() {
		
		@Override
		public void run() {
			removeTyping();
		}
	};
	
	private Runnable typingRunnable = new Runnable(){

		@Override
		public void run() {
			handler.post(endTypingRunnable);
		}
		
	};
	
	private int textColor = 0;
	private float textSize;

	private OnTabChangeListener tabChangeListener = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			if (!tabId.equals(chatId)) {
				if (kbManager != null){
					kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(), 0);
				}
			} else {
				buddy.unread = 0;
				try {
					getEntryPoint().setUnread(buddy, null);					
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					getEntryPoint().onRemoteCallFailed(e);
				}
				tabWidgetLayout.setScaledBitmap(icon);		
				updateBuddyState(buddy);
				scroller.post(scrollToEnd);	
			}
		}
	};

	private final Runnable refreshHistoryCallback = new Runnable() {
		public void run() {
			historyView.removeViewAt(0);
			
			for (int i = messages.size()-1; i>=0; i--){
				historyView.addView(getListItem(messages.get(i)), 0);
			}
			
			handler.post(scrollToEnd);			
		}
	};
	private final Runnable messageSentCallback = new Runnable() {

		@Override
		public void run() {
			sendTypingGo = false;
			textEditor.setText("");
			historyView.addView(getListItem(messages.get(messages.size()-1)));
			//historyAdapter.notifyDataSetChanged();
			//historyView.setSelection(historyView.getChildCount() - 1);
			sendTypingGo = true;
			handler.post(scrollToEnd);		
		}
	};
	private Runnable scrollToEnd = new Runnable() {
		
		@Override
		public void run() {
			scroller.scrollBy(0, 4*getEntryPoint().metrics.heightPixels);			
		}
	};
	private final Runnable bitmapGot = new Runnable(){

		@Override
		public void run() {
			tabWidgetLayout.setScaledBitmap(icon);						
		}
		
	};
	private Runnable getOccupantsRunnable = new Runnable() {
		
		@Override
		public void run() {
			try {				
				fillGroupChatView(getEntryPoint().runtimeService.getChatRoomOccupants(getServiceId(), buddy.protocolUid));
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e1) {
				getEntryPoint().onRemoteCallFailed(e1);
			}
		}
	};

	public ConversationsView(final EntryPoint entryPoint, Buddy buddy, AccountView account) {
		super(entryPoint);
		
		this.buddy = buddy;
		updated(account, false);

		kbManager = (InputMethodManager) getEntryPoint().getSystemService(Context.INPUT_METHOD_SERVICE);
		entryPoint.mainScreen.addOnTabChangeListener(tabChangeListener);

		LayoutInflater inflate = LayoutInflater.from(getContext());
		inflate.inflate(R.layout.conversation, this);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setGravity(Gravity.BOTTOM);
		
		tabWidgetLayout = new TabWidgetLayout(entryPoint);

		tabWidgetLayout.setText(buddy.getName());
		sendBtn = (ImageButton) findViewById(R.id.convsendbtn);
		smileyBtn = (ImageButton) findViewById(R.id.convsmileybtn);

		statusIcon = (ImageView) findViewById(R.id.statusicon);
		xstatusIcon = (ImageView) findViewById(R.id.xstatusicon);
		statusText = (StatusTextView) findViewById(R.id.statustext);
		
		textEditor = (EditText) findViewById(R.id.convtext);
		participantsView = (ExpandableListView) findViewById(R.id.buddiesScrollView);

		historyView = (LinearLayout) findViewById(R.id.historyScrollView);
		scroller = (ScrollView) findViewById(R.id.scroller);
		
		int maxLength = 2047;
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(maxLength);
		textEditor.setFilters(filterArray);
		textEditor.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (sendTyping && sendTypingGo){
					new Thread(){
						
						@Override
						public void run(){
							sendTypingNotification();
						}
						
					}.start();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {}
			
		});
		textEditor.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (sendWithEnter && actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN){
					sendMessage(textEditor.getText().toString());
					return true;
				} 
				return false;
			}
		});
		
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessage(textEditor.getText().toString());
			}
		});

		chatId = ConversationsView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid;
		requestIcon();
		printDateMode = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_chat_date));

		checkGroupChatView();
		checkoutHistory();
		updateBuddyState(buddy);
		
		visualStyleUpdated();	
		
		smileyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final Dialog dialog = new Dialog(entryPoint, android.R.style.Theme_Translucent_NoTitleBar);
				GridView grid = new GridView(entryPoint);
				grid.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				grid.setBackgroundColor(0xd0000000);
				
				int numColumns = 5;
				
				if (entryPoint.dontDrawSmileys){
					numColumns = (int)((getEntryPoint().metrics.widthPixels-60) / (80 * getEntryPoint().metrics.density));
				} else {
					numColumns = (int)(((getEntryPoint().metrics.widthPixels-60) / smileAdapter.size) * getEntryPoint().metrics.density);
				}
				
				grid.setNumColumns(numColumns);
				grid.setAdapter(smileAdapter);
				grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
				grid.setGravity(Gravity.CENTER_HORIZONTAL);
				grid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						final int cursorPos = textEditor.getSelectionStart();

						TypedArray smileys = getResources().obtainTypedArray(R.array.smiley_pick_names);
						String smiley = smileys.getString(position);
						textEditor.setText(textEditor.getText().insert(cursorPos, smiley));
						textEditor.setSelection(cursorPos + smiley.length());
						smileys.recycle();
						dialog.dismiss();
					}
				});
				dialog.setContentView(grid);
				dialog.show();
			}
		});
	}

	private void checkGroupChatView() {
		if (buddy.visibility == Buddy.VIS_GROUPCHAT && buddy.status != Buddy.ST_OFFLINE){
			handler.post(getOccupantsRunnable);
		} else {
			participantsView.setVisibility(View.GONE);
		}
	}

	public void fillGroupChatView(MultiChatRoomOccupants occupants) {
		String showIconsStr = null;
		try {
			showIconsStr = getEntryPoint().runtimeService.getAccountView(occupants.serviceId).options.getString(getEntryPoint().getResources().getString(R.string.key_show_icons));
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			getEntryPoint().onRemoteCallFailed(e);
		} catch (NotFoundException e1) {
			ServiceUtils.log(e1);
		}
		boolean showIcons  = showIconsStr != null ? Boolean.parseBoolean(showIconsStr) : true;		
		
		participantsView.setVisibility(View.VISIBLE);
		
		RelativeLayout.LayoutParams layout;
		if (showIcons){
			layout = new RelativeLayout.LayoutParams(ContactListGridItem.itemSize, LayoutParams.FILL_PARENT);
		} else {
			layout = new RelativeLayout.LayoutParams((int) ((getEntryPoint().metrics.widthPixels / 4) + (getEntryPoint().metrics.density)), LayoutParams.FILL_PARENT);
		}
		
		layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		participantsView.setLayoutParams(layout);
		
		if (participantsAdapter == null){
			participantsAdapter = new ConversationsViewParticipantsAdapter(getEntryPoint(), occupants, showIcons);
			participantsView.setAdapter(participantsAdapter); 
			participantsView.setGroupIndicator(getResources().getDrawable(R.drawable.dummy));
			participantsView.setDividerHeight(0);			
		} else {
			participantsAdapter.refreshOccupants(occupants);
		}
		
		for (int i=0; i<occupants.groups.size(); i++){
			BuddyGroup gr = occupants.groups.get(i);
			if (gr.buddyList.size() > 0){
				participantsView.setSelectedChild(i, 0, true);	
			}
		}	
	}

	private synchronized void sendTypingNotification() {
		try {
				sendTypingGo = false;
				executor.schedule(freeToSendTypingRunable, 2, TimeUnit.SECONDS);
				getEntryPoint().runtimeService.sendTyping(buddy.serviceId, buddy.protocolUid);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e1) {
				getEntryPoint().onRemoteCallFailed(e1);
			}
		
	}

	private void sendMessage(final String text) {
		if (text == null || text.length() < 1)
			return;

		if (accountConnectionState != AccountService.STATE_CONNECTED) {
			Toast.makeText(getEntryPoint(), "Please enter network first", Toast.LENGTH_SHORT).show();
			return;
		}

		new Thread("Send message") {
			@Override
			public void run() {
				try {
					TextMessage message = new TextMessage(buddy.ownerUid);
					message.to = buddy.protocolUid;
					message.time = new Date();
					message.text = text;
					message.messageId = idGenerator.nextInt();
					
					if (buddy.secureOptions == Buddy.SECURE_CONNECTED){
						message.options = TextMessage.OPT_SECURE;
					}
					getEntryPoint().runtimeService.sendMessage(message, buddy.serviceId);
					
					messages.add(HistorySaver.formatMessageForHistory(message, buddy, getResources().getString(R.string.label_me)));
					handler.post(messageSentCallback);
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (Exception e1) {
					Toast.makeText(getEntryPoint(), "Error sending message " + e1.getLocalizedMessage(), Toast.LENGTH_LONG).show();
					ServiceUtils.log(e1);
				}
			}
		}.start();
	}

	private void checkoutHistory() {
		ServiceMessage waitMsg = new ServiceMessage(buddy.protocolUid);
		waitMsg.text = getResources().getString(R.string.label_wait);
		final View waitView = getListItem(waitMsg);
		historyView.addView(waitView);
		
		new Thread("Get history") {
			@Override
			public void run() {
				messages = buddy.getLastHistory(getEntryPoint(), false);					
				handler.post(refreshHistoryCallback);
			}
		}.start();

		// kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(), 0);
	}

	@Override
	public void messageReceived(TextMessage message, boolean tabActive) {
		if (buddy.protocolUid.equals(message.from) || buddy.protocolUid.equals(message.to)) {
			removeTyping();
			historyView.addView(getListItem(message));
			if (tabActive){
				scroller.post(scrollToEnd);	
				buddy.unread = 0;				
			} else {
				//if (!getEntryPoint().getTabHost().getCurrentTabTag().equals(ContactList.class.getSimpleName()+" "+getServiceId())){
					tabWidgetLayout.setImageResource(R.drawable.message_medium);
					buddy.unread++;
				//}
			}
			try {
				getEntryPoint().setUnread(buddy, message);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
		}
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public String getChatId() {
		return chatId;
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

		MenuItem askXStatusItem = menu.findItem(R.id.menuitem_askxstatus);
		if (buddy.xstatus > -1 && buddy.serviceName.equalsIgnoreCase(getResources().getString(R.string.icq_service_name))) {
			askXStatusItem.setIcon(ServiceUtils.getXStatusArray32(getContext(), buddy.serviceName).getDrawable(buddy.xstatus));
			askXStatusItem.setVisible(true);
		} else {
			askXStatusItem.setIcon(R.drawable.xstatus_none);
			askXStatusItem.setVisible(false);
		}
		MenuItem sendFileItem = menu.findItem(R.id.menuitem_send_file);
		sendFileItem.setVisible(buddy.status != Buddy.ST_OFFLINE && buddy.canFileShare);
		
		MenuItem securitizeItem = menu.findItem(R.id.menuitem_securitize);
		switch (buddy.secureOptions){
		case Buddy.SECURE_NOSUPPORT:
		case Buddy.SECURE_SUPPORTS:
			securitizeItem.setVisible(false);
			break;
		case Buddy.SECURE_ENABLED:
			securitizeItem.setVisible(true);
			securitizeItem.setIcon(R.drawable.lock_closed_medium);
			securitizeItem.setTitle(R.string.label_securitize);
			break;
		case Buddy.SECURE_CONNECTED:
			securitizeItem.setVisible(true);
			securitizeItem.setIcon(R.drawable.lock_open_medium);
			securitizeItem.setTitle(R.string.label_desecuritize);
			break;
		}
		
		MenuItem joinChatItem = menu.findItem(R.id.menuitem_join_chat);
		MenuItem leaveChatItem = menu.findItem(R.id.menuitem_leave_chat);
		MenuItem participantsItem = menu.findItem(R.id.menuitem_participants);
		if (accountConnectionState == AccountService.STATE_CONNECTED && buddy.visibility == Buddy.VIS_GROUPCHAT){
			joinChatItem.setVisible(buddy.status == Buddy.ST_OFFLINE);
			leaveChatItem.setVisible(buddy.status != Buddy.ST_OFFLINE);
			participantsItem.setVisible(buddy.status != Buddy.ST_OFFLINE);
			participantsItem.setTitle(participantsView.getVisibility() == View.VISIBLE ? R.string.label_hide_participants : R.string.label_show_participants);
		} else {
			joinChatItem.setVisible(false);
			leaveChatItem.setVisible(false);
			participantsItem.setVisible(false);
		}

		LocationManager lm = (LocationManager) getEntryPoint().getSystemService(Context.LOCATION_SERVICE);
		MenuItem sendLocationItem = menu.findItem(R.id.menuitem_send_location);
		sendLocationItem.setVisible(lm != null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_securitize:
			toggleSecurity();
			break;
		case R.id.menuitem_close:
			// kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(),
			// 0);
			getEntryPoint().mainScreen.removeTabByTag(chatId);
			returnToBuddyList();
			break;
		case R.id.menuitem_send_file:
			ViewUtils.showPickFileDialog(getEntryPoint(), buddy, sendFileAction, fileSendFilter);
			break;
		case R.id.menuitem_send_location:
			new LocationLoader().getAndSendLocation();
			break;
		case R.id.menuitem_showtabs:
			ViewUtils.showTabChangeMenu(getEntryPoint());
			break;
		case R.id.menuitem_history:
			getEntryPoint().getHistoryTab(buddy);
			break;
		case R.id.menuitem_askxstatus:
			try {
				getEntryPoint().runtimeService.askForXStatus(buddy);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
			break;
		case R.id.menuitem_join_chat:
			try {
				getEntryPoint().runtimeService.joinExistingChat(buddy.serviceId, buddy.protocolUid);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
			break;
		case R.id.menuitem_leave_chat:
			try {
				getEntryPoint().runtimeService.leaveChat(buddy.serviceId, buddy.protocolUid);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
			break;
		case R.id.menuitem_participants:
			participantsView.setVisibility(participantsView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
			break;
		}
		return false;
	}

	private void toggleSecurity() {
		if (buddy.secureOptions == Buddy.SECURE_ENABLED){
			buddy.secureOptions = Buddy.SECURE_CONNECTED;
		} else if (buddy.secureOptions == Buddy.SECURE_CONNECTED){
			buddy.secureOptions = Buddy.SECURE_ENABLED;
		}
		
		try {
			getEntryPoint().runtimeService.editBuddy(buddy);
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			getEntryPoint().onRemoteCallFailed(e);
		}
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			returnToBuddyList();
			return true;
		}

		return false;
	}

	private void returnToBuddyList() {
		kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(), 0);
		getEntryPoint().mainScreen.checkAndSetCurrentTabByTag(ContactList.class.getSimpleName() + " " + buddy.serviceId);
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	public void updateBuddyState(final Buddy buddy) {
		if (!this.buddy.protocolUid.equals(buddy.protocolUid)) {
			return;
		} else {
			this.buddy.merge(buddy);
		}
		
		tabWidgetLayout.setText(buddy.getName());
		statusIcon.setImageResource(ServiceUtils.getStatusResIdByBuddyTiny(getContext(), buddy));

		switch (buddy.status) {
		case Buddy.ST_ONLINE:
			statusText.setText(R.string.label_st_online);
			break;
		case Buddy.ST_FREE4CHAT:
			statusText.setText(R.string.label_st_free4chat);
			break;
		case Buddy.ST_AWAY:
			statusText.setText(R.string.label_st_away);
			break;
		case Buddy.ST_BUSY:
			statusText.setText(R.string.label_st_busy);
			break;
		case Buddy.ST_DND:
			statusText.setText(R.string.label_st_dnd);
			break;
		case Buddy.ST_INVISIBLE:
			statusText.setText(R.string.label_st_invisible);
			break;
		case Buddy.ST_NA:
			statusText.setText(R.string.label_st_na);
			break;
		case Buddy.ST_OFFLINE:
			statusText.setText(R.string.label_st_offline);
			break;
		case Buddy.ST_ANGRY:
			statusText.setText(R.string.label_st_angry);
			break;
		case Buddy.ST_DEPRESS:
			statusText.setText(R.string.label_st_depress);
			break;
		case Buddy.ST_DINNER:
			statusText.setText(R.string.label_st_dinner);
			break;
		case Buddy.ST_HOME:
			statusText.setText(R.string.label_st_home);
			break;
		case Buddy.ST_WORK:
			statusText.setText(R.string.label_st_work);
			break;
		}

		if (buddy.xstatus > -1) {
			TypedArray xNames = ServiceUtils.getXStatusArray(getContext(), buddy.serviceName);
			int xImageId = xNames.getResourceId(buddy.xstatus, 0);
			if (xImageId != 0) {
				xstatusIcon.setVisibility(View.VISIBLE);
				xstatusIcon.setImageResource(xImageId);
				statusText.setText(getResources().obtainTypedArray(R.array.icq_xstatus_descr).getText(buddy.xstatus));
			}
		} else {
			xstatusIcon.setVisibility(View.GONE);
		}
		
		if (buddy.xstatusName != null || buddy.xstatusDescription != null) {
			statusText.setTextAndFormat(
					(buddy.xstatusName != null && buddy.xstatusName.length()>0 ? (buddy.xstatusName + " :") : "") + 
					(buddy.xstatusDescription != null && buddy.xstatusDescription.length()>0 ? (" " + buddy.xstatusDescription) : ""));
		} 
		
		if (accountConnectionState == AccountService.STATE_DISCONNECTED || buddy.visibility != Buddy.VIS_GROUPCHAT || buddy.status == Buddy.ST_OFFLINE){
			participantsView.setVisibility(View.GONE);
		} else {
			checkGroupChatView();
		}
	}
	
	private void requestIcon(){
		new Thread("Chat icon request"){
			@Override
			public void run(){
				icon = Buddy.getIcon(getEntryPoint(), buddy.getFilename());
				handler.post(bitmapGot);									
			}
		}.start();
	}

	class LocationLoader implements LocationListener {
		boolean requested = true;
		LocationManager lm = null;

		public void getAndSendLocation() {
			if (lm == null) {
				lm = (LocationManager) getEntryPoint().getSystemService(Context.LOCATION_SERVICE);
			}
			if (lm != null) {
				if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					getEntryPoint().startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
				} else {
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
				}
			} else {
				Toast.makeText(getEntryPoint(), "Sorry, your device does not support location resolving", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			if (requested) {
				String url = "http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude() + "&z=16";
				sendMessage(getResources().getString(R.string.label_im_here) + url);
				requested = false;
				if (lm != null) {
					lm.removeUpdates(this);
				}
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {
			if (requested) {
				getAndSendLocation();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

	}

	@Override
	public int getMainMenuId() {
		return R.menu.conversation_menu;
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		return tabWidgetLayout;
	}

	@Override
	public byte getServiceId() {
		return buddy.serviceId;
	}

	@Override
	public void updated(AccountView account, boolean refreshContacts) {
		accountConnectionState = account.getConnectionState();
		sendTyping = Boolean.parseBoolean((String) account.options.get(getContext().getString(R.string.key_send_typing)));
	}

	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public void stateChanged(AccountView account, boolean refreshContacts) {
		if ((accountConnectionState = account.getConnectionState()) == AccountService.STATE_DISCONNECTED){
			buddy.status = Buddy.ST_OFFLINE;
			updateBuddyState(buddy);
		}
	}

	@Override
	public void visualStyleUpdated() {
		int textSize;
		try {
			textSize = Integer.parseInt(getEntryPoint().getApplicationOptions().getString((getResources().getString(R.string.key_text_size))));
			try {
				isImeFullScreen = Boolean.parseBoolean(getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_fullscreen_landscape_kb)));
				int imeOpts = textEditor.getImeOptions();
				if (isImeFullScreen){
					imeOpts ^= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
				} else {
					imeOpts |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
				}
				textEditor.setImeOptions(imeOpts);
			} catch (Exception e) {
			}
			try {
				sendWithEnter = Boolean.parseBoolean(getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_send_by_enter)));
			} catch (Exception e) {
			}
		} catch (Exception npe) {
			textSize = 16;
			ServiceUtils.log(npe);
		} 
		
		if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER) {
			setBackgroundColor(0x60000000);
			this.textColor = 0xffffffff;
			historyView.setBackgroundColor(0x60000000);
			statusText.setTextColor(0xffffffff);
		} else {
			try {
				int color = EntryPoint.bgColor;
				historyView.setBackgroundColor(0);
				this.textColor = (color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff;
				statusText.setTextColor((color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff);
				setBackgroundColor(0);
			} catch (NumberFormatException e) {
				ServiceUtils.log(e);
			}
		}

		this.textSize = textSize * getEntryPoint().metrics.density;
			
		for (int i=0; i<historyView.getChildCount(); i++){
			View v = historyView.getChildAt(i);
			if (v instanceof HistoryRecordView){
				((HistoryRecordView)v).setTextColor(this.textColor);
				((HistoryRecordView)v).setTextSize(this.textSize);
			}
		}
		
		if (getEntryPoint().dontDrawSmileys){
			smileAdapter = new ImageOrTextGridAdapter(getEntryPoint(), getResources().obtainTypedArray(R.array.smiley_pick_names), (int) (60*getEntryPoint().metrics.density), TextView.class);
		} else {
			smileAdapter = new ImageOrTextGridAdapter(getEntryPoint(), getResources().obtainTypedArray(R.array.smiley_pick_values), (int) (60*getEntryPoint().metrics.density), ImageView.class);
		}
		
	}
	
	private View getListItem(Message message){
		
		if (message instanceof TextMessage){
			HistoryRecordView v = new HistoryRecordView(getContext(), null, textSize);
				v.recordId = message.messageId;
				final String msg = message.text.trim();
				v.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View arg0) {
						return historyRecordMenu(msg);
					}
					
				});
				
				v.setTextAndFormat(msg, printDateMode, getEntryPoint().dontDrawSmileys, ((TextMessage) message).options == TextMessage.OPT_SECURE);
				
				v.setTextColor(0xff000000+textColor);
				return v;
			}		
		if (message instanceof ServiceMessage){ //TODO 
			TextView tv = new TextView(getContext());
				tv.setTextColor(0xff00ff00);
				tv.setGravity(Gravity.CENTER);
				tv.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
			tv.setText(message.text);
			return tv;
		}
		
		return null;
	}
	
	protected boolean historyRecordMenu(final String msg) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getEntryPoint());
		final TypedArray values = getEntryPoint().getResources().obtainTypedArray(R.array.conversation_menu_values);
		builder.setItems(R.array.conversation_menu_values, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = values.getString(which);
				if (value.equals(getEntryPoint().getResources().getString(R.string.menu_value_copy))) {
					ViewUtils.replaceClipboard(getEntryPoint(), msg, false);
				}
				if (value.equals(getEntryPoint().getResources().getString(R.string.menu_value_add_to_copied))) {
					ViewUtils.replaceClipboard(getEntryPoint(), msg, true);
				}
				if (value.equals(getEntryPoint().getResources().getString(R.string.menu_value_copy_all))) {
					if (historyView.getChildAt(0) instanceof HistoryRecordView){
						ViewUtils.replaceClipboard(getEntryPoint(), ((HistoryRecordView)historyView.getChildAt(0)).getText().toString(), false);
					}
					
					if (historyView.getChildCount() < 2){
						return;
					}
					
					for (int i=1; i<historyView.getChildCount(); i++){
						if (historyView.getChildAt(i) instanceof HistoryRecordView){
							ViewUtils.replaceClipboard(getEntryPoint(), ((HistoryRecordView)historyView.getChildAt(i)).getText().toString(), true);
						}
					}
				}
			}
		});
		builder.create().show();
		
		return false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		scroller.post(scrollToEnd);	
	}

	@Override
	public void connectionState(int state) {
	}

	@Override
	public void onStart() {
		tabChangeListener.onTabChanged(chatId);	
	}

	@Override
	public void bitmap(String uid) {
		if (uid.equals(buddy.protocolUid)){
			requestIcon();	
		} else if (participantsAdapter != null){
			participantsAdapter.bitmap(uid);
		}
	}

	public void messageAck(long messageId, int level) {
		for (int i=historyView.getChildCount()-1; i>=0; i--){
			if (historyView.getChildAt(i) instanceof HistoryRecordView
					&& ((HistoryRecordView)historyView.getChildAt(i)).recordId == messageId){
				((HistoryRecordView)historyView.getChildAt(i)).messageAck(level);
				handler.post(scrollToEnd);
			}
		}		
	}
	
	private void removeTyping(){
		findViewById(R.id.convstatuspanel).setVisibility(View.VISIBLE);
		findViewById(R.id.convtypingpanel).setVisibility(View.GONE);
	}

	public void typing(boolean tabActive) {
		findViewById(R.id.convtypingpanel).setVisibility(View.VISIBLE);
		findViewById(R.id.convstatuspanel).setVisibility(View.GONE);
		executor.schedule(typingRunnable, 4, TimeUnit.SECONDS);
	}
	
	@Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs){
		if (isImeFullScreen){
			outAttrs.imeOptions ^= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
		} else {
			outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
		}
		return super.onCreateInputConnection(outAttrs);
	}

	@Override
	public void configChanged() {}

	@Override
	public void serviceMessageReceived(ServiceMessage message, boolean tabActive) {
		if (message.serviceId == getServiceId() && message.from.equals(buddy.protocolUid) && buddy.visibility == Buddy.VIS_GROUPCHAT){
			historyView.addView(getListItem(message));
			scroller.post(scrollToEnd);	
		}
	}
}
