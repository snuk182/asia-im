package ua.snuk182.asia.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.xmlpull.v1.XmlPullParserException;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Account;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import ua.snuk182.asia.services.api.ProtocolException;
import ua.snuk182.asia.services.plus.Notificator;
import ua.snuk182.asia.view.more.AsiaCoreException;
import ua.snuk182.asia.view.more.musiccontrol.AbstractPlayerStateListener;
import ua.snuk182.asia.view.more.musiccontrol.IPlayerStateListener;
import ua.snuk182.asia.view.more.musiccontrol.androidmusic.AndroidMusicServiceStateListener;
import ua.snuk182.asia.view.more.musiccontrol.poweramp.PowerAmpStateListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class RuntimeService extends Service {

	private final List<Account> accounts = new ArrayList<Account>();

	private ProtocolServiceResponse protocolResponse;
	private IRuntimeServiceCallback uiCallback;
	private ServiceStoredPreferences storage;
	private List<TabInfo> tabInfos = null;
	private Notificator notificator = null;
	private boolean isAppVisible = true;
	private Handler handler = new Handler();
	private Bundle appOptions;
	private Map<String, AbstractPlayerStateListener> playerStateListeners = new HashMap<String, AbstractPlayerStateListener>();
	
	private int startId;
	
	private WifiManager.WifiLock wifiLock = null;
	private PowerManager.WakeLock powerLock = null;
	
	private boolean finished = false;
	
	private final Thread exitThread = new Thread(){
		
		@Override
		public void run(){
			finished = true;
			try {
				serviceBinder.disconnectAll();
			} catch (RemoteException e) {
				ServiceUtils.log(e);
			}
			setForeground(false);
			removeStatusbarNotification();
			stopSelfResult(startId);
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
	@Override 
	public void onStart(Intent intent, int startId){
		this.startId = startId;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    try {
	        mStartForeground = getClass().getMethod("startForeground",
	                mStartForegroundSignature);
	        mStopForeground = getClass().getMethod("stopForeground",
	                mStopForegroundSignature);
	    } catch (Exception e) {
	        // Running on an older platform.
	        mStartForeground = mStopForeground = null;
	    }
	    try {
	        mSetForeground = getClass().getMethod("setForeground",
	                mSetForegroundSignature);
	    } catch (Exception e) {
	    	mSetForeground = null;
	    }
		//android.os.Debug.waitForDebugger();
		startForegroundCompat(R.string.label_wait, new Notification());
	    
		protocolResponse = new ProtocolServiceResponse();
		//setForeground(true);
		storage = new ServiceStoredPreferences(getApplicationContext());
		notificator = new Notificator(getApplicationContext());

		List<AccountView> acViews;
		try {
			acViews = storage.getAccounts();
		} catch (Exception e1) {
			ServiceUtils.log(e1);
			acViews = new ArrayList<AccountView>();
		}
		accounts.clear();
		for (AccountView aView : acViews) {
			Account a = new Account(getApplicationContext(), aView, protocolResponse);
			accounts.add(a);
		}
		appOptions = storage.getApplicationOptions();
		String logToFile = appOptions.getString(getResources().getString(R.string.key_log_to_file));
		if (logToFile != null) {
			try {
				ServiceUtils.logToFile = Boolean.parseBoolean(logToFile);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}

		statusbarNotifyAccountChanged();

		new Thread("Runtime service startup") {
			@Override
			public void run() {
				PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
				powerLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "asia power lock");
				powerLock.acquire();

				WifiManager wlanManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
				if (wlanManager != null) {
					wifiLock = wlanManager.createWifiLock("asia wifi lock");
					wifiLock.acquire();
				}
			}
		}.start();
		
		String autoconnect = appOptions.getString(getResources().getString(R.string.key_autoconnect));
		for (Account a: accounts){
			if (a.accountView.getConnectionState() != AccountService.STATE_DISCONNECTED || (autoconnect!=null && autoconnect.indexOf("true") > -1)){
				try {
					a.accountView.setConnectionState(AccountService.STATE_CONNECTING);
					serviceBinder.connect(a.accountView.serviceId);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
			}
			
			String powerampPlayerStatusKey = getResources().getString(R.string.key_poweramp_playing_to_status);
			String playerStatus = a.accountView.options.getString(powerampPlayerStatusKey);
			if (Boolean.parseBoolean(playerStatus)){				
				putPlayerStateListener(getPlayerStateListener(powerampPlayerStatusKey), a.accountView);
			}
			
			String androidmusicPlayerStatusKey = getResources().getString(R.string.key_androidmusic_playing_to_status);
			playerStatus = a.accountView.options.getString(androidmusicPlayerStatusKey);
			if (Boolean.parseBoolean(playerStatus)){				
				putPlayerStateListener(getPlayerStateListener(androidmusicPlayerStatusKey), a.accountView);
			}
		}
	}
	
	private AbstractPlayerStateListener getPlayerStateListener(String key) {
		AbstractPlayerStateListener listener = playerStateListeners.get(key);
		if (listener == null){
			if (key.equals(getResources().getString(R.string.key_poweramp_playing_to_status))){
				listener = new PowerAmpStateListener(this);				
			}
			
			if (key.equals(getResources().getString(R.string.key_androidmusic_playing_to_status))){
				listener = new AndroidMusicServiceStateListener(this);				
			}
			
			playerStateListeners.put(key, listener);
		}
		
		return listener;
	}

	@Override
	public void onLowMemory(){
		storage.saveAccounts(accounts);
		System.gc();
		super.onLowMemory();
	}

	@Override
	public void onDestroy() {
		ServiceUtils.log("on destroy");
		wipe();
		//accounts.clear();
		super.onDestroy();
		
		stopForegroundCompat(R.string.label_wait);
	}
	
	private void wipe(){
		//removeStatusbarNotification();
		ServiceUtils.log("wipe service data");
		if (wifiLock != null) {
			wifiLock.release();
			wifiLock = null;
		}
		if (powerLock != null) {
			powerLock.release();
			powerLock = null;
		}
		storage.saveAccounts(accounts);
	}

	public RuntimeService() {}
	
	public void requestIcon(byte serviceId, String uid){
		try {
			getAccountInternal(serviceId).accountService.request(AccountService.REQ_GETICON, uid);
		} catch (ProtocolException e) {
			ServiceUtils.log(e);
		} 
	}

	public class ProtocolServiceResponse implements IAccountServiceResponse {

		@SuppressWarnings({ "unchecked" })
		@Override
		public synchronized Object respond(final short action, final byte serviceId, final Object... args) throws ProtocolException {
			final Account a = getAccountInternal(serviceId);
			if (a == null) {
				throw new ProtocolException("Account not found");
			}

			final AccountView account = a.accountView;
			account.updateTime();			
			switch (action) {
			/*case IAccountServiceResponse.RES_LOG:
				synchronized (this) {
					ServiceUtils.log((String) args[0], account, false);
				}
				break;*/
			case IAccountServiceResponse.RES_GETFROMSTORAGE:
				return storage.getMap((Set<String>) args[1], account.getAccountId() + " " + (String) args[0]);
			case IAccountServiceResponse.RES_SAVETOSTORAGE:
				storage.saveMap((Map<String, String>) args[1], account.getAccountId() + " " + (String) args[0]);
				break;
			case IAccountServiceResponse.RES_TYPING:
				try {
					uiCallback.typing(account.serviceId, (String)args[0]);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e2) {
					ServiceUtils.log(e2);
				}
				break;
			case IAccountServiceResponse.RES_CONNECTED:

				try {
					account.setConnectionState(AccountService.STATE_CONNECTED);
					uiCallback.accountConnected(account);
					
					storage.saveServiceState(accounts);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e2) {
					ServiceUtils.log(e2);
				}

				statusbarNotifyAccountChanged();

				if (account.xStatus > -1) {
					try {
						getAccountById(serviceId).accountService.request(AccountService.REQ_SETEXTENDEDSTATUS, account.status, account.xStatus, account.xStatusName, account.xStatusText);
					} catch (ProtocolException e) {
						ServiceUtils.log(e, account);
					} catch (AsiaCoreException e) {
						ServiceUtils.log(e);
					}
				}

				break;
			case IAccountServiceResponse.RES_DISCONNECTED:
				if (finished){
					notificator.cancel(a.accountView);
					break;
				}
				
				boolean reconnect = false;

				if (account.getConnectionState() != AccountService.STATE_DISCONNECTED && args.length < 1) {
					reconnect = true;
				}
				disconnected(account);

				if (args.length > 0) {
					notificationToast((String)args[0]);		
					break;
				}

				if (reconnect) {
					new ScheduledThreadPoolExecutor(1).schedule(new Runnable() {

						@Override
						public void run() {
							if (account.getConnectionState() == AccountService.STATE_DISCONNECTED) {
								/*account.setConnectionState(AccountService.STATE_CONNECTING);
								try {
									getAccountById(serviceId).accountService.request(AccountService.REQ_CONNECT, account.status, account.xStatus, account.xStatusName, account.xStatusText);
								} catch (ProtocolException e) {
									ServiceUtils.log(e);
								} catch (AsiaCoreException e) {
									ServiceUtils.log(e);
								}*/
								try {
									connect(getAccountById(serviceId));
								} catch (AsiaCoreException e) {
									ServiceUtils.log(e);
								}
							}
						}

					}, 2, TimeUnit.SECONDS);
				} else {
					storage.saveServiceState(accounts);
				}

				break;
			case IAccountServiceResponse.RES_SAVEIMAGEFILE:
				byte[] iconData = (byte[]) args[0];
				//Bitmap bitmap = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);

				String filename = account.getAccountId() + " " + args[1];
				log("icon for " + filename);
				storage.saveIcon(filename, iconData, new Runnable(){

					@Override
					public void run() {
						try {
							uiCallback.icon(serviceId, (String) args[1]);
						} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e2) {
							ServiceUtils.log(e2);
						}
					}
					
				});
				
				// account.editBuddy(buddy, true);

				if (!args[1].equals(account.protocolUid) && args[2] != null){
					final Buddy buddy = account.getBuddyByProtocolUid((String) args[1]);
					buddy.iconHash = (String) args[2];					
				}
				
				
				//storage.saveAccount(account);		
				
				break;
			case IAccountServiceResponse.RES_SAVEPARAMS:
				new Thread("Save account parameters") {
					@Override
					public void run() {
						Map<String, String> map = (Map<String, String>) args[0];
						SharedPreferences.Editor settings = getSharedPreferences(account.getAccountId(), 0).edit();
						for (String key : map.keySet()) {
							String value = map.get(key);
							settings.putString(key, value);
						}
						settings.commit();
					}
				}.start();
				break;
			case IAccountServiceResponse.RES_CLUPDATED:
				Boolean saveNotInList = Boolean.parseBoolean(account.options.getString(getApplicationContext().getResources().getString(R.string.key_notinlist_save)));
				
				String loadIconsStr = account.options.getString(getResources().getString(R.string.key_load_icons));
				boolean loadIcons;
				if (loadIconsStr != null) {
					loadIcons = Boolean.parseBoolean(loadIconsStr);
				} else {
					loadIcons = true;
				}

				account.removeAllBuddies(saveNotInList);
				account.setBuddyGroupList((List<BuddyGroup>) args[1]);
				account.setBuddyList((List<Buddy>) args[0], RuntimeService.this, loadIcons);
				try {
					uiCallback.contactListUpdated(account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e2) {
					ServiceUtils.log(e2);
				}
				
				storage.saveAccount(account);
				break;
			case IAccountServiceResponse.RES_MESSAGE:
				final TextMessage message = (TextMessage) args[0];
				textMessage(account, message);
				break;
			case IAccountServiceResponse.RES_BUDDYSTATECHANGED:
				final OnlineInfo info = (OnlineInfo) args[0];
				Buddy buddy = account.getBuddyByProtocolUid(info.protocolUid);
				if (buddy == null) {
					// TODO wtf
					break;
				}
				
				String playMessageOnly = appOptions.getString(getResources().getString(R.string.key_message_sound_only));
				boolean msgOnlyValue = false;
				try {
					msgOnlyValue = Boolean.parseBoolean(playMessageOnly);
				} catch (Exception e3) {
				}

				if (!msgOnlyValue && buddy.status == Buddy.ST_OFFLINE && info.userStatus != Buddy.ST_OFFLINE) {
					String soundNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_sound_type));
					if (soundNotification == null) {
						soundNotification = getApplicationContext().getResources().getString(R.string.value_sound_type_profile);
					}

					if (soundNotification.equals(getApplicationContext().getResources().getString(R.string.value_sound_type_profile))) {
						notificator.playOnlineBasedOnProfile();
					}
					if (soundNotification.equals(getApplicationContext().getResources().getString(R.string.value_sound_type_all_on))
							|| soundNotification.equals(getApplicationContext().getResources().getString(R.string.value_sound_type_sound))) {
						notificator.playOnline();
					}
				}

				ServiceUtils.mergeBuddyWithOnlineInfo(buddy, info);
				
				loadIconsStr = account.options.getString(getResources().getString(R.string.key_load_icons));
				if (loadIconsStr == null){
					loadIcons = true;
				} else {
					loadIcons = Boolean.parseBoolean(loadIconsStr);
				}
				
				if (loadIcons/*
							 * && (oldBuddy.icon == null ||
							 * (oldBuddy.iconHash!=null &&
							 * !oldBuddy.iconHash.equals(info.iconHash)))
							 */) {
					final String buddyUid = buddy.protocolUid;
					ServiceUtils.log("icon request "+buddy.getFilename());
					new Thread("Runtime icon request") {
						@Override
						public void run() {
							requestIcon(serviceId, buddyUid);
						}
					}.start();
				}

				//if (account.getConnectionState() != AccountService.STATE_CONNECTED && info.userStatus != Buddy.ST_OFFLINE) break;

				try {
					uiCallback.buddyStateChanged(buddy);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e2) {
					ServiceUtils.log(e2);
				}
				break;
			case IAccountServiceResponse.RES_NOTIFICATION:
				notificationToast((String)args[0]);		
				break;
			case IAccountServiceResponse.RES_CONNECTING:
				try {
					uiCallback.connecting(serviceId, (Integer)args[0]);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e2) {
					ServiceUtils.log(e2);
				}
				
				

				break;
			case IAccountServiceResponse.RES_ACCOUNTUPDATED:
				OnlineInfo nfo = (OnlineInfo) args[0];
				try {
					loadIcons = Boolean.parseBoolean(account.options.getString(getResources().getString(R.string.key_load_icons)));
				} catch (Exception e1) {
					loadIcons = true;
				}
				if (loadIcons) {
					requestIcon(serviceId, account.protocolUid);
				}

				if (nfo != null) {
					account.ownName = nfo.name;
					if (nfo.userStatus != Buddy.ST_OFFLINE){
						account.status = nfo.userStatus;
					}
					if (nfo.visibility != -1){
						account.visibility = nfo.visibility;
					}
					try {
						uiCallback.accountUpdated(account);
					} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
						ServiceUtils.log(e);
					}
				}

				statusbarNotifyAccountChanged();

				break;
			case IAccountServiceResponse.RES_USERINFO:
				PersonalInfo pinfo = (PersonalInfo) args[0];
				if (pinfo.protocolUid == null)
					return null;
				if (pinfo.protocolUid.equals(account.protocolUid)) {
					account.ownName = pinfo.properties.getString(PersonalInfo.INFO_NICK);
					try {
						uiCallback.accountUpdated(account);
					} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
						ServiceUtils.log(e);
					}
				} else {
					buddy = account.getBuddyByProtocolUid(pinfo.protocolUid);
					
					//WTF
					if (buddy == null){
						log("no buddy "+pinfo.protocolUid+" found");
						return null;
					}
					
					if (buddy.getName().equals(buddy.protocolUid)) {
						String nick = pinfo.properties.getString(PersonalInfo.INFO_NICK);
						if (nick != null) {
							buddy.name = nick;
						}
					}
					if (!buddy.waitsForInfo){
						try {
							uiCallback.buddyStateChanged(buddy);
						} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
							ServiceUtils.log(e);
						}
					} else {
						buddy.waitsForInfo = false;
						try {
							uiCallback.personalInfo(buddy, pinfo);
						} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
							ServiceUtils.log(e);
						}
					}
				}
				storage.saveAccount(account);
				break;
			case IAccountServiceResponse.RES_AUTHREQUEST:
				ServiceMessage sm = new ServiceMessage((String) args[0]);
				sm.type = ServiceMessage.TYPE_AUTHREQUEST;
				sm.text = (String) args[1];
				sm.serviceId = serviceId;
				Buddy buddddy = account.getBuddyByProtocolUid(sm.from);
				if (buddddy == null) {
					Boolean noAuthFromAliens = Boolean.parseBoolean(account.options.getString(getApplicationContext().getResources().getString(R.string.key_deny_messages_not_from_list)));
					if (noAuthFromAliens != null && noAuthFromAliens) {
						return null;
					}
					buddddy = buddyNotFromList(sm, account);
				}
				try {
					uiCallback.serviceMessage(sm);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_SEARCHRESULT:
				final ArrayList<PersonalInfo> infos = (ArrayList<PersonalInfo>) args[0];
				try {
					uiCallback.searchResult(serviceId, infos);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_GROUPADDED:
				final BuddyGroup group = (BuddyGroup) args[0];
				account.getBuddyGroupList().add(group);
				storage.saveAccount(account);
				
				try {
					uiCallback.groupAdded(group, account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_BUDDYADDED:
				final Buddy uddy = (Buddy) args[0];
				account.addBuddyToList(uddy);
				storage.saveAccount(account);
				try {
					uiCallback.buddyAdded(uddy, account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_BUDDYDELETED:
				final Buddy ddy = (Buddy) args[0];
				account.removeBuddyByUid(ddy);
				storage.saveAccount(account);

				try {
					uiCallback.buddyRemoved(ddy, account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_GROUPDELETED:
				final BuddyGroup roup = (BuddyGroup) args[0];
				account.removeGroup(roup);
				storage.saveAccount(account);

				try {
					uiCallback.groupRemoved(roup, account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_BUDDYMODIFIED:
				final Buddy dy = (Buddy) args[0];
				account.editBuddy(dy, false);
				
				storage.saveAccount(account);
				try {
					uiCallback.buddyEdited(dy, account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_GROUPMODIFIED:
				final BuddyGroup oup = (BuddyGroup) args[0];
				account.editGroup(oup);
				
				storage.saveAccount(account);
				try {
					uiCallback.groupEdited(oup, account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_FILEMESSAGE:
				final FileMessage fm = (FileMessage) args[0];
				Buddy bud = account.getBuddyByProtocolUid(fm.from);
				if (bud == null) {
					return null;
				}
				try {
					uiCallback.fileMessage(fm);
				} catch (NullPointerException npe) {					
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_FILEPROGRESS:
				String statusbarNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_statusbar_type));
				if (statusbarNotification != null && !statusbarNotification.equals(getResources().getString(R.string.value_statusbar_type_none))){
					notificator.notifyFileProgress((Long)args[0], (String)args[1], (Long)args[2], (Long)args[3], (Boolean)args[4], (String)args[5]);
				}
				
				buddy = account.getBuddyByProtocolUid((String) args[6]);
				
				try {
					uiCallback.fileProgress((Long)args[0], buddy, (String)args[1], (Long)args[2], (Long)args[3], (Boolean)args[4], (String)args[5]);
				} catch (NullPointerException npe) {					
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				
				break;
			case IAccountServiceResponse.RES_MESSAGEACK:
				Buddy buu = account.getBuddyByProtocolUid((String) args[0]);
				
				if (buu == null){
					break;
				}
				
				try{
					uiCallback.messageAck(buu, (Long) args[1], (Integer) args[2]);
				} catch (NullPointerException npe) {					
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_AVAILABLE_CHATS:
				try {
					uiCallback.availableChatsList(serviceId, (List<MultiChatRoom>) args[0]);
				} catch (NullPointerException npe) {					
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_CHAT_PARTICIPANTS:
				try {
					uiCallback.chatRoomOccupants(serviceId, (String) args[0], (MultiChatRoomOccupants)args[1]);
				} catch (NullPointerException npe) {					
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			case IAccountServiceResponse.RES_SERVICEMESSAGE:
				sm = new ServiceMessage((String) args[0]);
				sm.type = ServiceMessage.TYPE_CHAT_MESSAGE;
				sm.text = (String) args[1];
				sm.serviceId = serviceId;
				try {
					uiCallback.serviceMessage(sm);
				} catch (NullPointerException npe) {					
				} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				break;
			}

			return null;
		}
	}

	public ProtocolServiceResponse getProtocolResponse() {
		return protocolResponse;
	}

	public void disconnected(AccountView account) {		
		
		account.disconnected();

		try {
			uiCallback.disconnected(account);
		} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
			ServiceUtils.log(e);
		}

		statusbarNotifyAccountChanged(false);		
	}

	private void textMessage(AccountView account, TextMessage message) {
		if (message.text == null || message.text.length() < 1) {
			return;
		}
		Buddy budddy = account.getBuddyByProtocolUid(message.from);
		
		if (budddy == null && message.from.equals(account.protocolUid)){
			budddy = account.getBuddyByProtocolUid(message.to);
		}
		
		if (budddy == null){
			Boolean noAuthFromAliens = Boolean.parseBoolean(account.options.getString(getApplicationContext().getResources().getString(R.string.key_deny_messages_not_from_list)));
			if (noAuthFromAliens != null && noAuthFromAliens) {
				return;
			}
			budddy = buddyNotFromList(message, account);
		}
		
		if (!isAppVisible){
			budddy.unread++;
		}
		
		budddy.getHistorySaver().saveHistoryRecord(HistorySaver.formatMessageForHistory(message, budddy, account.getSafeName()), getApplicationContext());
		
		try {
			uiCallback.textMessage(message);
		} catch (NullPointerException npe) { 
			isAppVisible = false;
			budddy.unread++;
		} catch (DeadObjectException de) { 
			isAppVisible = false;
			budddy.unread++;
		} catch (RemoteException e) {
			ServiceUtils.log(e);
		}
		
		String soundNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_sound_type));
		if (soundNotification == null) {
			soundNotification = getApplicationContext().getResources().getString(R.string.value_sound_type_profile);
		}

		if (soundNotification.equals(getApplicationContext().getResources().getString(R.string.value_sound_type_profile))) {
			notificator.playMessageBasedOnProfile();
		}
		if (soundNotification.equals(getApplicationContext().getResources().getString(R.string.value_sound_type_all_on))) {
			notificator.playMessage(true, true);
		}
		if (soundNotification.equals(getApplicationContext().getResources().getString(R.string.value_sound_type_vibra))) {
			notificator.playMessage(false, true);
		}
		if (soundNotification.equals(getApplicationContext().getResources().getString(R.string.value_sound_type_sound))) {
			notificator.playMessage(true, false);
		}

		if (!isAppVisible) {
			String statusbarNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_statusbar_type));
			if (statusbarNotification == null) {
				statusbarNotification = getApplicationContext().getResources().getString(R.string.value_statusbar_type_messages);
			}
			
			boolean blinkLed = false;
			
			try {
				blinkLed = Boolean.parseBoolean(appOptions.getString(getApplicationContext().getResources().getString(R.string.key_blink_led)));
			} catch (Exception e) {}

			if (statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_messages))) {
				notificator.notifyMessageReceived(message, budddy, false, blinkLed);
			}
			if (statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_icon))) {
				notificator.notifyMessageReceived(message, budddy, true, blinkLed);
			}
			statusbarNotifyAccountChanged(budddy.serviceId, budddy.getName()+": "+message.text.substring(message.text.indexOf("):")+2), true);
		}
		
	}

	public void setProtocolResponse(ProtocolServiceResponse protocolResponse) {
		this.protocolResponse = protocolResponse;
	}

	public Account getAccountById(byte serviceId) throws AsiaCoreException {
		for (Account a : accounts) {
			if (a.accountView.serviceId == serviceId) {
				return a;
			}
		}

		throw new AsiaCoreException("No account found for id=" + serviceId);
	}

	void log(String string) {
		ServiceUtils.log(string, null, false);
	}
	
	private byte[] getMyIp(){
		NetworkInfo info = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		
		byte[] ip = null;
		
		switch(info.getType()){
		case (ConnectivityManager.TYPE_WIFI):
			WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			//ip = ServiceUtils.getIPBytesFromSystemIp(wifiManager.getConnectionInfo().getIpAddress());
			ip = ServiceUtils.ipString2ByteBE(ServiceUtils.ipAddressToString(wifiManager.getConnectionInfo().getIpAddress())) ;
			break;
		case (ConnectivityManager.TYPE_MOBILE):
			Enumeration<NetworkInterface> face;
			try {
				face = NetworkInterface.getNetworkInterfaces();
				while(face.hasMoreElements()){
					NetworkInterface f = face.nextElement();
					if(f.getName().equals("pdp0")){
						ip = f.getInetAddresses().nextElement().getAddress();
						break;
					}
				}
			} catch (SocketException e) {
				ServiceUtils.log(e);
			}
		
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			telephonyManager.getDataState();
			
			break;
		}
		return ip;
	}
	
	private void connect(final Account a) {
		new Runnable() {

			@Override
			public void run() {
				a.accountView.setConnectionState(AccountService.STATE_CONNECTING);
				statusbarNotifyAccountChanged();
				String secure = a.accountView.options.getString(getResources().getString(R.string.key_secure_login));
				try {
					if (secure != null && secure.equalsIgnoreCase("true")){
						a.accountService.request(AccountService.REQ_CONNECT, a.accountView.status, a.accountView.xStatus, a.accountView.xStatusName, a.accountView.xStatusText, true);
					} else {
						a.accountService.request(AccountService.REQ_CONNECT, a.accountView.status, a.accountView.xStatus, a.accountView.xStatusName, a.accountView.xStatusText);
					}
				} catch (ProtocolException e) {
					ServiceUtils.log(e);
				}
			}

		}.run();
	}

	private final IRuntimeService.Stub serviceBinder = new IRuntimeService.Stub() {

		private void disconnect(Account a) {
			try {
				disconnected(a.accountView);
				a.accountService.request(AccountService.REQ_DISCONNECT);
				storage.saveServiceState(accounts);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void connect(byte serviceId) throws RemoteException {
			try {
				RuntimeService.this.connect(getAccountById(serviceId));
			} catch (AsiaCoreException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void connectAll() throws RemoteException {
			for (Account account : accounts) {
				RuntimeService.this.connect(account);
			}
		}

		@Override
		public void disconnect(byte serviceId) throws RemoteException {
			disconnect(getAccountInternal(serviceId));
		}

		@Override
		public List<AccountView> getProtocolServices() throws RemoteException {
			List<AccountView> views = new ArrayList<AccountView>();
			for (Account a : accounts) {
				views.add(a.accountView);
			}
			return views;
		}

		@Override
		public String sendMessage(TextMessage message, byte serviceId) throws RemoteException {
			Account a = getAccountInternal(serviceId);
			try {
				a.accountService.request(AccountService.REQ_SENDMESSAGE, message);
				Buddy buddy = a.accountView.getBuddyByProtocolUid(message.to);
				buddy.getHistorySaver().saveHistoryRecord(HistorySaver.formatMessageForHistory(message, buddy, getApplicationContext().getResources().getString(R.string.label_me)), getApplicationContext());
			} catch (ProtocolException e) {
				Toast.makeText(getApplicationContext(), "Error sending message", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		public void disconnectAll() throws RemoteException {
			for (Account account : accounts) {
				disconnect(account);
			}
		}

		@Override
		public void registerCallback(IRuntimeServiceCallback callback) throws RemoteException {
			uiCallback = callback;
		}

		@Override
		public byte createAccount(AccountView account) throws RemoteException {
			account.serviceId = (byte) accounts.size();
			account.status = Buddy.ST_ONLINE;
			storage.saveAccount(account, true);
			accounts.add(new Account(getApplicationContext(), account, protocolResponse));
			uiCallback.accountAdded(account);

			return account.serviceId;
		}

		@Override
		public AccountView getAccountView(byte serviceId) throws RemoteException {
			return getAccountInternal(serviceId).accountView;
		}

		@Override
		public void setUnread(final Buddy buddy, final TextMessage message) throws RemoteException {
			final AccountView account = getAccountInternal(buddy.serviceId).accountView;
			Buddy serviceBuddy = account.getBuddyByProtocolUid(buddy.protocolUid);
			serviceBuddy.unread = buddy.unread;
			final String statusbarNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_statusbar_type));
			if (buddy.unread > 0) {
				if (message != null) {
					
					boolean blinkLed = false;
					
					try {
						blinkLed = Boolean.parseBoolean(appOptions.getString(getApplicationContext().getResources().getString(R.string.key_blink_led)));
					} catch (Exception e) {}

					if (statusbarNotification == null || statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_messages))) {
						if (buddy != null) {
							notificator.notifyMessageReceived(message, buddy, false, blinkLed);
						}
					}
					if (statusbarNotification != null && !isAppVisible && statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_icon))) {
						if (buddy != null) {
							notificator.notifyMessageReceived(message, buddy, true, blinkLed);
						}
					}
					statusbarNotifyAccountChanged();
				}
			} else {

				if (statusbarNotification == null || statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_messages))) {
					notificator.cancel(buddy);
				}
				statusbarNotifyAccountChanged();
				uiCallback.buddyStateChanged(serviceBuddy);

			}
			storage.saveAccount(account);
		}

		@Override
		public Buddy getBuddy(byte serviceId, String buddyProtocolUid) throws RemoteException {
			return getAccountInternal(serviceId).accountView.getBuddyByProtocolUid(buddyProtocolUid);
		}

		@Override
		public List<TabInfo> getSavedTabs() throws RemoteException {
			isAppVisible = true;
			return tabInfos;
		}

		@Override
		public void saveTabs(List<TabInfo> tabs) throws RemoteException {
			tabInfos = tabs;
		}

		@Override
		public List<Buddy> getBuddies(byte serviceId, List<String> buddyProtocolUids) throws RemoteException {
			AccountView account = getAccountInternal(serviceId).accountView;
			return account.getBuddyList();
		}

		@Override
		public void setStatus(final byte serviceId, byte status) throws RemoteException {
			Account account = getAccountInternal(serviceId);
			account.accountView.status = status;
			storage.saveAccount(account.accountView);
			if (account.accountView.getConnectionState() == AccountService.STATE_CONNECTED) {
				try {
					account.accountService.request(AccountService.REQ_SETSTATUS, status, account.accountView.xStatus, account.accountView.xStatusName, account.accountView.xStatusText);
					
					uiCallback.status(account.accountView);
					
					statusbarNotifyAccountChanged();
				} catch (ProtocolException e) {
					ServiceUtils.log(e);
				}
			}
		}

		@Override
		public void setXStatus(AccountView ac) throws RemoteException {
			Account a = getAccountInternal(ac.serviceId);
			AccountView account = a.accountView;
			account.xStatus = ac.xStatus;
			account.xStatusName = ac.xStatusName;
			account.xStatusText = ac.xStatusText;
			storage.saveAccount(account);

			setXStatusInternal(a);
		}

		@Override
		public void deleteAccount(AccountView account) throws RemoteException {
			for (int i = 0; i < accounts.size(); i++) {
				if (account.serviceId == accounts.get(i).accountView.serviceId) {
					accounts.remove(i);
					break;
				}
			}
			notificator.cancel(account);
			try {
				storage.removeAccount(account);
				uiCallback.accountRemoved(account);
			} catch (Exception e) {
				ServiceUtils.log(e);
			}			
		}

		@Override
		public void editAccount(AccountView account) throws RemoteException {
			storage.saveAccount(account);
			uiCallback.accountUpdated(account);
		}

		@Override
		public void prepareExit() throws RemoteException {
			exitThread.start();
		}

		@Override
		public void savePreference(String key, String value, final byte serviceId) throws RemoteException {
			AccountView account;
			if (serviceId > -1) {
				account = getAccountInternal(serviceId).accountView;
				account.options.putString(key, value);
				account.updateTime();
				
				if (key.equals(getResources().getString(R.string.key_send_typing))) {
					uiCallback.accountUpdated(account);
				}
				
				String powerampKey = getResources().getString(R.string.key_poweramp_playing_to_status);
				if (key.equals(powerampKey)){
					if (Boolean.parseBoolean(value)){
						putPlayerStateListener(getPlayerStateListener(powerampKey), account);
					} else {
						removePlayerStateListener(getPlayerStateListener(powerampKey), account);
					}
				} 
				
				String musicKey = getResources().getString(R.string.key_androidmusic_playing_to_status);
				if (key.equals(musicKey)){
					if (Boolean.parseBoolean(value)){
						putPlayerStateListener(getPlayerStateListener(musicKey), account);
					} else {
						removePlayerStateListener(getPlayerStateListener(musicKey), account);
					}
				}
			} else {
				account = null;
				appOptions.putString(key, value);

				if (key.equals(getResources().getString(R.string.key_log_to_file))) {
					try {
						ServiceUtils.logToFile = Boolean.parseBoolean(value);
					} catch (Exception e) {
						ServiceUtils.log(e);
					}
				}
			}

			storage.savePreference(key, value, account);
			uiCallback.visualStyleUpdated();

			if (key.equals(getApplicationContext().getResources().getString(R.string.key_statusbar_type))) {
				String statusbarNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_statusbar_type));
				for (Account acc : accounts) {

					if (statusbarNotification != null && statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_account_status))) {

						statusbarNotifyAccountChanged();
					} else {
						notificator.cancel(acc.accountView);
					}
				}

				if (statusbarNotification != null && statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_icon))){
					notificator.showAppIcon();
				} else {
					notificator.removeAppIcon();
				}
			}
		}

		@Override
		public Bundle getProtocolServiceOptions(byte serviceId) throws RemoteException {
			Account account = getAccountInternal(serviceId);
			Map<String, String> nativeMap = account.accountService.getOptions();
			Map<String, String> map = storage.getMap(nativeMap.keySet(), account.accountView.getAccountId() + " " + IAccountServiceResponse.SHARED_PREFERENCES);
			Bundle bu = new Bundle();
			for (String key : nativeMap.keySet()) {
				String val = map.get(key);
				if (val == null) {
					val = nativeMap.get(key);
				}
				bu.putString(key, val);
			}
			return bu;
		}

		@Override
		public void saveProtocolServiceOptions(byte serviceId, Bundle options) throws RemoteException {
			if (options == null) {
				return;
			}
			Account account = getAccountInternal(serviceId);
			Map<String, String> map = new HashMap<String, String>();

			for (String key : options.keySet()) {
				map.put(key, options.getString(key));
			}
			storage.saveMap(map, account.accountView.getAccountId() + " " + IAccountServiceResponse.SHARED_PREFERENCES);
		}

		@Override
		public void askForXStatus(Buddy buddy) throws RemoteException {
			Account account = getAccountInternal(buddy.serviceId);
			try {
				account.accountService.request(AccountService.REQ_GETEXTENDEDSTATUS, buddy.protocolUid);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public boolean isDataSetInvalid(byte serviceId, long lastUpdateTime) throws RemoteException {
			AccountView account = getAccountInternal(serviceId).accountView;
			return account.lastUpdateTime != lastUpdateTime;
		}

		@Override
		public void setAppVisible(boolean visible) throws RemoteException {
			isAppVisible = visible;
			if (isAppVisible){
				String statusbarNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_statusbar_type)); 
				if (statusbarNotification != null && statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_icon))){
					notificator.showAppIcon();
				} else {
					notificator.removeAppIcon();
				}
			}
		}

		@Override
		public void addBuddy(Buddy buddy) throws RemoteException {
			if (buddy == null) {
				return;
			}
			Account account = getAccountInternal(buddy.serviceId);
			BuddyGroup group = account.accountView.getBuddyGroupByGroupId(buddy.groupId);
			short id = 0;
			do {
				id = (short) new Random().nextInt(0x7fff);
			} while (account.accountView.getBuddyByBuddyId(id) != null);
			buddy.id = id;
			try {
				account.accountService.request(AccountService.REQ_ADDBUDDY, buddy, group);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void removeBuddy(Buddy buddy) throws RemoteException {
			if (buddy == null) {
				return;
			}
			Account account = getAccountInternal(buddy.serviceId);
			if (buddy.groupId != AccountService.NOT_IN_LIST_GROUP_ID && buddy.visibility != Buddy.VIS_GROUPCHAT) {
				try {
					account.accountService.request(AccountService.REQ_REMOVEBUDDY, buddy);
				} catch (ProtocolException e) {
					ServiceUtils.log(e);
				}
			}
			
			if (buddy.visibility == Buddy.VIS_GROUPCHAT){
				storage.delete(getGroupchatStorageName(account.accountView, buddy.protocolUid));
			}
			
			account.accountView.removeBuddyByUid(buddy);
			storage.saveAccount(account.accountView);
			uiCallback.contactListUpdated(account.accountView);
		}

		@Override
		public void requestBuddyShortInfo(byte serviceId, String uid) throws RemoteException {
			if (uid == null) {
				return;
			}
			try {
				getAccountInternal(serviceId).accountService.request(AccountService.REQ_GETBUDDYINFO, uid);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void requestAuthorization(Buddy buddy, String reason) throws RemoteException {
			if (buddy == null) {
				return;
			}
			try {
				getAccountInternal(buddy.serviceId).accountService.request(AccountService.REQ_AUTHREQUEST, buddy, reason);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void respondAuthorization(Buddy buddy, boolean authorized) throws RemoteException {
			if (buddy == null) {
				return;
			}
			try {
				getAccountInternal(buddy.serviceId).accountService.request(AccountService.REQ_AUTHRESPONSE, buddy.protocolUid, authorized);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void setServiceMessageUnread(byte serviceId, boolean unread, ServiceMessage message) throws RemoteException {
			Account account = getAccountInternal(serviceId);
			if (account.accountView.protocolUid.equals(message.from)) {

			} else {
				Buddy buddy = account.accountView.getBuddyByProtocolUid(message.from);
				if (unread) {
					if (message != null) {
						// serviceMessages.add(message);
						notificator.notifyServiceMessageReceived(message, buddy);
					}
				} else {
					buddy.unread = (byte) 0;
					notificator.cancel(buddy);
				}
				storage.saveAccount(account.accountView);
			}
		}

		@Override
		public List<ServiceMessage> getServiceMessages(byte serviceId, String uid) throws RemoteException {
			List<ServiceMessage> messages = new ArrayList<ServiceMessage>();
			/*
			 * for (int i = serviceMessages.size() - 1; i >= 0; i--) { if
			 * (serviceMessages.get(i).from.equals(uid)) {
			 * messages.add(serviceMessages.remove(i)); } }
			 */
			return messages;
		}

		@Override
		public void searchUsersByUid(byte serviceId, String buddyUid) throws RemoteException {
			if (buddyUid == null) {
				return;
			}
			try {
				getAccountInternal(serviceId).accountService.request(AccountService.REQ_SEARCHFORBUDDY_BY_UID, buddyUid);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}

		}

		@Override
		public void addGroup(BuddyGroup group) throws RemoteException {
			if (group == null)
				return;

			Account account = getAccountInternal(group.serviceId);
			group.ownerUid = account.accountView.protocolUid;
			try {
				account.accountService.request(AccountService.REQ_ADDGROUP, group, account.accountView.getBuddyGroupList());
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void renameBuddy(Buddy buddy) throws RemoteException {
			if (buddy == null) {
				return;
			}
			Account account = getAccountInternal(buddy.serviceId);
			if (buddy.groupId != AccountService.NOT_IN_LIST_GROUP_ID) {
				try {
					account.accountService.request(AccountService.REQ_RENAMEBUDDY, buddy);
				} catch (ProtocolException e) {
					ServiceUtils.log(e);
				}
			}
		}

		@Override
		public void renameGroup(BuddyGroup group) throws RemoteException {
			if (group == null) {
				return;
			}
			try {
				getAccountInternal(group.serviceId).accountService.request(AccountService.REQ_RENAMEGROUP, group);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void moveBuddy(Buddy buddy, BuddyGroup oldGroup, BuddyGroup newGroup) throws RemoteException {
			if (buddy == null || oldGroup == null || newGroup == null) {
				return;
			}
			Account account = getAccountInternal(buddy.serviceId);
			if (buddy.groupId != AccountService.NOT_IN_LIST_GROUP_ID) {
				try {
					account.accountService.request(AccountService.REQ_MOVEBUDDY, buddy, oldGroup, newGroup);
				} catch (ProtocolException e) {
					ServiceUtils.log(e);
				}
			}
		}

		@SuppressWarnings("unused")
		public void moveBuddies(byte serviceId, List<Buddy> buddies, BuddyGroup oldGroup, BuddyGroup newGroup) throws RemoteException {
			if (buddies == null || oldGroup == null || newGroup == null) {
				return;
			}
			try {
				getAccountInternal(serviceId).accountService.request(AccountService.REQ_MOVEBUDDIES, buddies, oldGroup, newGroup);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		public void removeBuddies(final byte serviceId, List<Buddy> buddies) throws RemoteException {
			if (buddies == null) {
				return;
			}
			Account account = getAccountInternal(serviceId);
			try {
				account.accountService.request(AccountService.REQ_REMOVEBUDDIES, buddies);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
			for (Buddy buddy : buddies) {
				account.accountView.removeBuddyByUid(buddy);
			}
			storage.saveAccount(account.accountView);
			uiCallback.contactListUpdated(account.accountView);
		}

		@Override
		public List<Buddy> getBuddiesFromGroup(BuddyGroup group) throws RemoteException {
			AccountView account = getAccountInternal(group.serviceId).accountView;
			List<Buddy> buddies = account.getBuddiesForGroup(group);
			return buddies;
		}

		@Override
		public void removeGroup(BuddyGroup group, List<Buddy> buddies, BuddyGroup newGroupForBuddies) throws RemoteException {
			if (buddies != null) {
				if (newGroupForBuddies != null) {
					for (Buddy bu : buddies) {
						moveBuddy(bu, group, newGroupForBuddies);
					}
				} else {
					removeBuddies(group.serviceId, buddies);
				}
			}
			try {
				getAccountInternal(group.serviceId).accountService.request(AccountService.REQ_REMOVEGROUP, group);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void respondFileMessage(FileMessage msg, boolean accept) throws RemoteException {
			Account account = getAccountInternal(msg.serviceId);	
			try {
				account.accountService.request(AccountService.REQ_FILERESPOND, msg, accept, getMyIp());
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void sendFile(Bundle bu, Buddy buddy) throws RemoteException {
			File fi = (File) bu.getSerializable(File.class.getName());
			if (fi != null && buddy != null){
				try {
					
					Account account = getAccountInternal(buddy.serviceId);
					
					long messageId = (Long) account.accountService.request(AccountService.REQ_SENDFILE, buddy, fi, getMyIp());
					uiCallback.fileProgress(messageId, buddy, fi.getAbsolutePath(), fi.length(), -1, false, null);
				} catch (ProtocolException e) {
					ServiceUtils.log(e);
				}
			}
		}

		@Override
		public void cancelFileTransfer(byte serviceId, long messageId) throws RemoteException {
			try {
				
				Account account = getAccountInternal(serviceId);
				
				notificator.cancelFileNotification(messageId);
				
				account.accountService.request(AccountService.REQ_FILECANCEL, messageId);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}			
		}

		@Override
		public void requestBuddyFullInfo(byte serviceId, String uid) throws RemoteException {
			if (uid == null) {
				return;
			}
			getAccountInternal(serviceId).accountView.getBuddyByProtocolUid(uid).waitsForInfo = true;
			try {				
				getAccountInternal(serviceId).accountService.request(AccountService.REQ_GETFULLBUDDYINFO, uid);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public void sendTyping(byte serviceId, String buddyUid) throws RemoteException {
			if (buddyUid == null) {
				return;
			}
			Account acc = getAccountInternal(serviceId);
			if (acc.accountView.status == Buddy.ST_INVISIBLE){
				return;
			}
			try {				
				acc.accountService.request(AccountService.REQ_SENDTYPING, buddyUid);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public Bundle getApplicationOptions() throws RemoteException {
			return appOptions;
		}

		@Override
		public void setGroupCollapsed(byte serviceId, int groupId, boolean collapsed) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			BuddyGroup group = acc.accountView.getBuddyGroupByGroupId(groupId);
			group.isCollapsed = collapsed;
			
			storage.saveAccount(acc.accountView);
		}

		@Override
		public void editBuddyVisibility(Buddy buddy) throws RemoteException {
			if (buddy == null) {
				return;
			}
			Account acc = getAccountInternal(buddy.serviceId);
			try {				
				acc.accountService.request(AccountService.REQ_VISIBILITY, buddy);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
			acc.accountView.getBuddyByBuddyId(buddy.id).merge(buddy);
			uiCallback.buddyStateChanged(buddy);
		}

		@Override
		public void editMyVisibility(byte serviceId, byte visibility) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			acc.accountView.visibility = visibility;
			try {				
				acc.accountService.request(AccountService.REQ_VISIBILITY, visibility);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
			uiCallback.accountUpdated(acc.accountView);
		}

		@Override
		public void requestAvailableChatRooms(byte serviceId) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			try {				
				acc.accountService.request(AccountService.REQ_GET_CHAT_ROOMS);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
			}
		}

		@Override
		public byte createChat(byte serviceId, String chatId, String chatNickname, String chatName, String chatPassword) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			try {				
				Buddy chat = (Buddy) acc.accountService.request(AccountService.REQ_CREATE_CHAT_ROOM, chatId, chatName, (chatPassword == null || chatPassword.length()<1) ? null : chatPassword, chatNickname);
				
				AccountView account = acc.accountView;
				account.addBuddyToList(chat);
				storage.saveAccount(account);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put(ServiceConstants.GROUPCHAT_PREFERENCE_NICKNAME, chatNickname);
				map.put(ServiceConstants.GROUPCHAT_PREFERENCE_PASSWORD, chatPassword);
				storage.saveMap(map, getGroupchatStorageName(account, chat.protocolUid));
				
				try {
					uiCallback.buddyAdded(chat, account);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				return ProtocolException.ERROR_NONE;
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
				return e.errorCode;
			}
		}
		
		private String getGroupchatStorageName(AccountView account, String chatId){
			return ServiceConstants.GROUPCHAT_PREFERENCES_PREFIX+" "+account.protocolUid+" "+chatId;
		}
		
		@Override
		public byte joinExistingChat(byte serviceId, String chatId) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			
			Map<String, String> map = storage.getMap(ServiceUtils.GROUPCHAT_PREFERENCE_MAP, getGroupchatStorageName(acc.accountView, chatId));
			return joinChatInternal(acc, chatId, map.get(ServiceConstants.GROUPCHAT_PREFERENCE_NICKNAME), map.get(ServiceConstants.GROUPCHAT_PREFERENCE_PASSWORD), false);
		}

		@Override
		public byte joinChat(byte serviceId, String chatId, String chatNickname, String chatPassword) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			return joinChatInternal(acc, chatId, chatNickname, chatPassword, true);
		}

		private byte joinChatInternal(Account acc, String chatId, String chatNickname, String chatPassword, boolean saveChatData) {
			try {				
				Buddy chat = (Buddy) acc.accountService.request(AccountService.REQ_JOIN_CHAT_ROOM, chatId, (chatPassword == null || chatPassword.length()<1) ? null : chatPassword, chatNickname);
				
				AccountView account = acc.accountView;
				account.addBuddyToList(chat);
				storage.saveAccount(account);
				
				if (saveChatData){
					Map<String, String> map = new HashMap<String, String>();
					map.put(ServiceConstants.GROUPCHAT_PREFERENCE_NICKNAME, chatNickname);
					map.put(ServiceConstants.GROUPCHAT_PREFERENCE_PASSWORD, chatPassword);
					storage.saveMap(map, getGroupchatStorageName(account, chat.protocolUid));
				}
				try {					
					if (acc.accountView.getBuddyByProtocolUid(chatId) != null){
						uiCallback.buddyStateChanged(chat);
					} else {
						uiCallback.buddyAdded(chat, account);
					}					
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				return ProtocolException.ERROR_NONE;
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
				return e.errorCode;
			}
		}

		@Override
		public boolean checkGroupChatsAvailability(byte serviceId) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			try {
				return (Boolean) acc.accountService.request(AccountService.REQ_CHECK_GROUPCHATS_AVAILABLE);
			} catch (Exception e) {}
			return false;
		}

		@Override
		public byte leaveChat(byte serviceId, String chatId) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			try {
				acc.accountService.request(AccountService.REQ_LEAVE_CHAT_ROOM, chatId);
				Buddy buddy = acc.accountView.getBuddyByProtocolUid(chatId);
				buddy.status = Buddy.ST_OFFLINE;
				try {
					uiCallback.buddyStateChanged(buddy);
				} catch (NullPointerException npe) { isAppVisible = false;} catch (DeadObjectException de) { isAppVisible = false;} catch (RemoteException e) {
					ServiceUtils.log(e);
				}
				return ProtocolException.ERROR_NONE;
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
				return e.errorCode;
			}
		}

		@Override
		public MultiChatRoomOccupants getChatRoomOccupants(byte serviceId, String chatId) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			try {
				boolean loadIcons;
				String loadIconsStr = acc.accountView.options.getString(getResources().getString(R.string.key_load_icons));
				if (loadIconsStr == null){
					loadIcons = true;
				} else {
					loadIcons = Boolean.parseBoolean(loadIconsStr);
				}
				
				return (MultiChatRoomOccupants) acc.accountService.request(AccountService.REQ_GET_CHAT_ROOM_OCCUPANTS, chatId, loadIcons);				
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
				return null;
			}
		}

		@Override
		public PersonalInfo getChatInfo(byte serviceId, String chatId) throws RemoteException {
			Account acc = getAccountInternal(serviceId);
			try {
				return (PersonalInfo) acc.accountService.request(AccountService.REQ_GETFULLBUDDYINFO, chatId);
			} catch (ProtocolException e) {
				ServiceUtils.log(e);
				return null;
			}
		}
	};

	protected Account getAccountInternal(byte serviceId) {
		for (Account account : accounts) {
			if (account.accountView.serviceId == serviceId) {
				return account;
			}
		}
		return null;
	}

	private void removePlayerStateListener(AbstractPlayerStateListener playerStateListener, AccountView account) {
		playerStateListener.removePlayerStateListener(account.serviceId);
		try {
			restoreXStatus(getAccountInternal(account.serviceId));
		} catch (Exception e) {
			ServiceUtils.log(e);
		}
	}

	private void putPlayerStateListener(AbstractPlayerStateListener playerStateListener, final AccountView account) {
		playerStateListener.addPlayerStateListener(account.serviceId, new IPlayerStateListener() {
			
			@Override
			public void onStateChanged(int status, Object... properties) {
				Account a = getAccountInternal(account.serviceId);
				switch (status) {
					case IPlayerStateListener.TRACK:
					case IPlayerStateListener.STARTED:
					a.accountView = ServiceUtils.editXStatusWithPlayed(getApplicationContext(), account, properties);
					try {
						setXStatusInternal(a);
					} catch (RemoteException e1) {
						ServiceUtils.log(e1);
					}
					break;
				case IPlayerStateListener.STOPPED:
					try {
						restoreXStatus(a);
					} catch (Exception e) {
						ServiceUtils.log(e);
					}
					break;
				}
				
			}
		});
	}

	private void restoreXStatus(Account account) throws XmlPullParserException, IOException {
		storage.getAccount(account.accountView, false);
		
		try {
			setXStatusInternal(account);
		} catch (RemoteException e) {
			ServiceUtils.log(e);
		}
	}

	private void setXStatusInternal(Account a) throws RemoteException {
		if (uiCallback == null){
			return;
		}
		AccountView account = a.accountView;
		uiCallback.accountUpdated(account);
		if (account.getConnectionState() == AccountService.STATE_CONNECTED) {
			try {
				a.accountService.request(AccountService.REQ_SETEXTENDEDSTATUS, account.status, account.xStatus, account.xStatusName, account.xStatusText);
			} catch (ProtocolException e) {
				ServiceUtils.log(e, account);
			}
		}
	}

	private void removeStatusbarNotification() {
		new Thread("Remove statusbar notification"){
			@Override
			public void run(){
				String statusbarNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_statusbar_type));
				if (statusbarNotification != null && statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_account_status))) {
					synchronized(notificator){
						for (Account account : accounts) {
							notificator.cancel(account.accountView);
						}
					}
				}
				if (statusbarNotification != null && statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_icon))){
					synchronized(notificator){
						notificator.removeAppIcon();
					}
				}
			}
		}.start();		
	}

	protected Buddy buddyNotFromList(ua.snuk182.asia.core.dataentity.Message message, final AccountView account) {
		final Buddy newBuddy = new Buddy(message.from, account);
		newBuddy.groupId = AccountService.NOT_IN_LIST_GROUP_ID;
		account.addBuddyToList(newBuddy);

		try {
			uiCallback.contactListUpdated(account);
			serviceBinder.requestBuddyShortInfo(account.serviceId, newBuddy.protocolUid);
		} catch (NullPointerException npe) {			
		} catch (RemoteException e) {
			ServiceUtils.log(e);
		}

		return newBuddy;
	}
	
	private void statusbarNotifyAccountChanged(boolean runSeparateThread){
		statusbarNotifyAccountChanged((byte) -1, null, runSeparateThread);
	}
	
	private void statusbarNotifyAccountChanged(){
		statusbarNotifyAccountChanged((byte) -1, null, true);
	}
	
	public void notificationToast(final String text){
		handler.post(new Runnable(){

			@Override
			public void run() {
				Toast toast = Toast.makeText(RuntimeService.this, text, Toast.LENGTH_LONG);
				//toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, 4);
				toast.show();
			}
			
		});
	}

	private void statusbarNotifyAccountChanged(final byte serviceId, final String message, boolean runSeparateThread) {
		Thread thread = new Thread("Statusbar account notificator") {

			@Override
			public void run() {
				String statusbarNotification = appOptions.getString(getApplicationContext().getResources().getString(R.string.key_statusbar_type));
				if (statusbarNotification != null && statusbarNotification.equals(getApplicationContext().getResources().getString(R.string.value_statusbar_type_account_status))) {
					synchronized(notificator){
						//order of application bar icons is changed in Android 2.3
						if (Build.VERSION.SDK_INT < 9){
							for (Account a : accounts) {
								if (serviceId > -1 && a.accountView.serviceId == serviceId){
									notificator.notifyAccountChanged(a.accountView, message);
								} else {
									notificator.notifyAccountChanged(a.accountView, null);
								}
							}
						} else {
							for (int i=accounts.size()-1; i>=0; i--) {
								Account a = accounts.get(i);
								if (serviceId > -1 && a.accountView.serviceId == serviceId){
									notificator.notifyAccountChanged(a.accountView, message);
								} else {
									notificator.notifyAccountChanged(a.accountView, null);
								}
							}
						}
					}
				}
			}

		};
		
		if (runSeparateThread){
			thread.start();
		} else {
			thread.run();
		}
	}
	
	private static final Class<?>[] mSetForegroundSignature = new Class[] {
	    boolean.class};
	private static final Class<?>[] mStartForegroundSignature = new Class[] {
	    int.class, Notification.class};
	private static final Class<?>[] mStopForegroundSignature = new Class[] {
	    boolean.class};

	private NotificationManager mNM;
	private Method mSetForeground;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mSetForegroundArgs = new Object[1];
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	void invokeMethod(Method method, Object[] args) {
	    try {
	        method.invoke(this, args);
	    } catch (InvocationTargetException e) {
	        // Should not happen.
	        Log.w("ApiDemos", "Unable to invoke method", e);
	    } catch (IllegalAccessException e) {
	        // Should not happen.
	        Log.w("ApiDemos", "Unable to invoke method", e);
	    }
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
	    // If we have the new startForeground API, then use it.
	    if (mStartForeground != null) {
	        mStartForegroundArgs[0] = Integer.valueOf(id);
	        mStartForegroundArgs[1] = notification;
	        invokeMethod(mStartForeground, mStartForegroundArgs);
	        return;
	    }

	    // Fall back on the old API.
	    mSetForegroundArgs[0] = Boolean.TRUE;
	    invokeMethod(mSetForeground, mSetForegroundArgs);
	    mNM.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat(int id) {
	    // If we have the new stopForeground API, then use it.
	    if (mStopForeground != null) {
	        mStopForegroundArgs[0] = Boolean.TRUE;
	        invokeMethod(mStopForeground, mStopForegroundArgs);
	        return;
	    }

	    // Fall back on the old API.  Note to cancel BEFORE changing the
	    // foreground state, since we could be killed at that point.
	    mNM.cancel(id);
	    mSetForegroundArgs[0] = Boolean.FALSE;
	    invokeMethod(mSetForeground, mSetForegroundArgs);
	}
}
