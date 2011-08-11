package ua.snuk182.asia.view.conversations;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.Message;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.HistorySaver;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.plus.ImageGridAdapter;
import ua.snuk182.asia.view.IHasBuddy;
import ua.snuk182.asia.view.IHasMessages;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.more.StatusTextView;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class ConversationsView extends RelativeLayout implements ITabContent, IHasBuddy, IHasMessages {

	public final List<Buddy> buddies;
	public AccountView account;
	private List<TextMessage> messages;
	private final ImageButton sendBtn;
	private final ImageButton smileyBtn;
	private final EditText textEditor;
	private final LinearLayout historyView;
	private final ScrollView scroller;
	private final ListView participantsView;
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
	private final ImageGridAdapter smileAdapter;
	
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
				kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(), 0);
			} else {
				buddies.get(0).unread = 0;
				try {
					getEntryPoint().runtimeService.setUnread(buddies.get(0), null);
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					getEntryPoint().onRemoteCallFailed(e);
				}
				tabWidgetLayout.setScaledBitmap(icon);		
				updateBuddyState(buddies.get(0));
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
			scroller.scrollBy(0, getEntryPoint().metrics.heightPixels);			
		}
	};
	private final Runnable bitmapGot = new Runnable(){

		@Override
		public void run() {
			tabWidgetLayout.setScaledBitmap(icon);						
		}
		
	};

	public ConversationsView(final EntryPoint entryPoint, List<Buddy> buddies, AccountView account) {
		super(entryPoint);
		
		this.buddies = buddies;
		this.account = account;
		updated(account);

		kbManager = (InputMethodManager) getEntryPoint().getSystemService(Context.INPUT_METHOD_SERVICE);
		entryPoint.addOnTabChangeListener(tabChangeListener);

		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.conversation, this);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setGravity(Gravity.BOTTOM);
		
		tabWidgetLayout = new TabWidgetLayout(entryPoint);

		if (buddies.size() == 1) {
			tabWidgetLayout.getTabName().setText(buddies.get(0).getName());
		} else {
			tabWidgetLayout.getTabIcon().setImageResource(R.drawable.accounts);
			tabWidgetLayout.getTabName().setText("Group chat");
		}

		sendBtn = (ImageButton) findViewById(R.id.convsendbtn);
		smileyBtn = (ImageButton) findViewById(R.id.convsmileybtn);

		statusIcon = (ImageView) findViewById(R.id.statusicon);
		xstatusIcon = (ImageView) findViewById(R.id.xstatusicon);
		statusText = (StatusTextView) findViewById(R.id.statustext);
		
		textEditor = (EditText) findViewById(R.id.convtext);
		participantsView = (ListView) findViewById(R.id.buddiesScrollView);

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
		
		smileAdapter = new ImageGridAdapter(entryPoint, getResources().obtainTypedArray(R.array.smiley_pick_values), (int) (60*getEntryPoint().metrics.density));

		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessage(textEditor.getText().toString());
			}
		});

		smileyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final Dialog dialog = new Dialog(entryPoint, android.R.style.Theme_Translucent_NoTitleBar);
				GridView grid = new GridView(entryPoint);
				grid.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				grid.setBackgroundColor(0xd0000000);
				
				int numColumns = (int)(((getEntryPoint().metrics.widthPixels-60) / smileAdapter.size) * getEntryPoint().metrics.density) ;
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

		if (buddies.size() == 1) {
			participantsView.setVisibility(View.GONE);
			chatId = ConversationsView.class.getSimpleName() + " " + buddies.get(0).serviceId + " " + buddies.get(0).protocolUid;
			requestIcon();
		} else {
			chatId = "multichat ";
		}
		
		printDateMode = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_chat_date));

		// TODO fix for multichat
		if (buddies.size() == 1) {
			checkoutHistory();
			updateBuddyState(buddies.get(0));
		}

		visualStyleUpdated();		
	}

	private synchronized void sendTypingNotification() {
		try {
				sendTypingGo = false;
				executor.schedule(freeToSendTypingRunable, 2, TimeUnit.SECONDS);
				getEntryPoint().runtimeService.sendTyping(account.serviceId, buddies.get(0).protocolUid);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e1) {
				getEntryPoint().onRemoteCallFailed(e1);
			}
		
	}

	private void sendMessage(final String text) {
		if (text == null || text.length() < 1)
			return;

		if (account.getConnectionState() != AccountService.STATE_CONNECTED) {
			Toast.makeText(getEntryPoint(), "Please enter network first", Toast.LENGTH_SHORT).show();
			return;
		}

		new Thread("Send message") {
			@Override
			public void run() {
				if (buddies.size() == 1) {
					try {
						Buddy buddy = buddies.get(0);

						TextMessage message = new TextMessage(buddy.ownerUid);
						message.to = buddy.protocolUid;
						message.time = new Date();
						message.text = text;
						message.messageId = idGenerator.nextInt();
						getEntryPoint().runtimeService.sendMessage(message, buddy.serviceId);
						
						messages.add(HistorySaver.formatMessageForHistory(message, buddy, getResources().getString(R.string.label_me)));
						handler.post(messageSentCallback);
					} catch (NullPointerException npe) {	
						ServiceUtils.log(npe);
					} catch (Exception e1) {
						Toast.makeText(getEntryPoint(), "Error sending message " + e1.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						ServiceUtils.log(e1);
					}
				} else {
					// TODO multichat
				}
			}
		}.start();
	}

	private void checkoutHistory() {
		ServiceMessage waitMsg = new ServiceMessage(buddies.get(0).protocolUid);
		waitMsg.text = getResources().getString(R.string.label_wait);
		final View waitView = getListItem(waitMsg);
		historyView.addView(waitView);
		
		new Thread("Get history") {
			@Override
			public void run() {
				messages = buddies.get(0).getLastHistory(getEntryPoint(), false);					
				handler.post(refreshHistoryCallback);
			}
		}.start();

		// kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(), 0);
	}

	@Override
	public void messageReceived(TextMessage message, boolean tabActive) {
		if (buddies.size() == 1) {
			if (buddies.get(0).protocolUid.equals(message.from) || buddies.get(0).protocolUid.equals(message.to)) {
				removeTyping();
				historyView.addView(getListItem(message));
				if (tabActive){
					scroller.post(scrollToEnd);	
				} else {
					//if (!getEntryPoint().getTabHost().getCurrentTabTag().equals(ContactList.class.getSimpleName()+" "+getServiceId())){
						tabWidgetLayout.getTabIcon().setImageResource(R.drawable.message_medium);
					//}
				}
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
			hideTabsStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_hide_tabs));
			if (hideTabsStr != null) {
				boolean hideTabs = Boolean.parseBoolean(hideTabsStr);
				showTabsItem.setVisible(hideTabs);
			} else {
				showTabsItem.setVisible(false);
			}
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} 

		if (buddies.size() == 1) {
			MenuItem askXStatusItem = menu.findItem(R.id.menuitem_askxstatus);
			if (buddies.get(0).xstatus > -1 && buddies.get(0).serviceName.equalsIgnoreCase(getResources().getString(R.string.icq_service_name))) {
				askXStatusItem.setIcon(ServiceUtils.getXStatusArray32(getContext(), account.protocolName).getDrawable(buddies.get(0).xstatus));
				askXStatusItem.setVisible(true);
			} else {
				askXStatusItem.setIcon(R.drawable.xstatus_none);
				askXStatusItem.setVisible(false);
			}
			MenuItem sendFileItem = menu.findItem(R.id.menuitem_send_file);
			sendFileItem.setVisible(buddies.get(0).canFileShare);
		}

		LocationManager lm = (LocationManager) getEntryPoint().getSystemService(Context.LOCATION_SERVICE);
		MenuItem sendLocationItem = menu.findItem(R.id.menuitem_send_location);
		sendLocationItem.setVisible(lm != null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_close:
			// kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(),
			// 0);
			getEntryPoint().removeTabByTag(chatId);
			returnToBuddyList();
			break;
		case R.id.menuitem_send_file:
			ViewUtils.showSendFileDialog(getEntryPoint(), buddies.get(0));
			break;
		case R.id.menuitem_send_location:
			new LocationLoader().getAndSendLocation();
			break;
		case R.id.menuitem_showtabs:
			ViewUtils.showTabChangeMenu(getEntryPoint());
			break;
		case R.id.menuitem_history:
			getEntryPoint().addHistoryTab(buddies.get(0));
			break;
		case R.id.menuitem_askxstatus:
			try {
				getEntryPoint().runtimeService.askForXStatus(buddies.get(0));
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
			break;
		}
		return false;
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
		getEntryPoint().getTabHost().setCurrentTabByTag(ContactList.class.getSimpleName() + " " + account.serviceId);
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	public void updateBuddyState(final Buddy buddy) {
		if (buddies.size() == 1) {
			if (!buddies.get(0).protocolUid.equals(buddy.protocolUid)) {
				return;
			} else {
				buddies.get(0).merge(buddy);
			}
		}
		
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
	}
	
	private void requestIcon(){
		if (buddies.size() == 1){
			new Thread("Chat icon request"){
				@Override
				public void run(){
					icon = buddies.get(0).getIcon(getEntryPoint(), (int) (32*getEntryPoint().metrics.density));
					handler.post(bitmapGot);									
				}
			}.start();
		}
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
	public int getServiceId() {
		return account.serviceId;
	}

	@Override
	public void updated(AccountView account) {
		this.account.options = account.options;
		sendTyping = Boolean.parseBoolean((String) account.options.get(getContext().getString(R.string.key_send_typing)));
	}

	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public void stateChanged(AccountView account) {
		this.account.merge(account);
		if (this.account.getConnectionState() == AccountService.STATE_DISCONNECTED){
			for (Buddy bu :buddies){
				bu.status = Buddy.ST_OFFLINE;
				updateBuddyState(bu);
			}
		}
	}

	@Override
	public void visualStyleUpdated() {
		String bgType;
		String textSize;
		try {
			bgType = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));
			textSize = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_text_size));
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
		} catch (NullPointerException npe) {
			bgType = null;
			textSize = null;
			ServiceUtils.log(npe);
		} if (bgType == null || bgType.equals("wallpaper")) {
			setBackgroundColor(0x60000000);
			this.textColor = 0xffffffff;
			historyView.setBackgroundColor(0x60000000);
			statusText.setTextColor(0xffffffff);
		} else {
			try {
				int color = (int) Long.parseLong(bgType);
				historyView.setBackgroundColor(0);
				this.textColor = (color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff;
				statusText.setTextColor((color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff);
				setBackgroundColor(0);
			} catch (NumberFormatException e) {
				ServiceUtils.log(e);
			}
		}

		if (textSize == null || textSize.equals(getResources().getString(R.string.value_text_size_medium))) {
			this.textSize = 16 * getEntryPoint().metrics.density;
		} else if (textSize.equals(getResources().getString(R.string.value_text_size_big))) {
			this.textSize = 20 * getEntryPoint().metrics.density;
		} else if (textSize.equals(getResources().getString(R.string.value_text_size_small))) {
			this.textSize = 12 * getEntryPoint().metrics.density;
		} else {
			this.textSize = 8 * getEntryPoint().metrics.density;
		}
		
		for (int i=0; i<historyView.getChildCount(); i++){
			View v = historyView.getChildAt(i);
			if (v instanceof HistoryRecordView){
				((HistoryRecordView)v).setTextColor(this.textColor);
				((HistoryRecordView)v).setTextSize(this.textSize);
			}
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
				
				v.setTextAndFormat(msg, printDateMode);
				
				v.setTextColor(0xff000000+textColor);
				return v;
			}		
		if (message instanceof ServiceMessage){ //TODO 
			TextView tv = new TextView(getContext());
				tv.setTextColor(0xff000000+textColor);
				tv.setGravity(Gravity.CENTER);
				tv.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
			
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
	public void onResume() {
		tabChangeListener.onTabChanged(chatId);	
	}

	@Override
	public void bitmap(String uid) {
		requestIcon();		
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
}
