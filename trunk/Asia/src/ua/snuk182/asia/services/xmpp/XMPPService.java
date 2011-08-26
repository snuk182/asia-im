package ua.snuk182.asia.services.xmpp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.ChatStateListener;
import org.jivesoftware.smackx.DefaultMessageEventRequestListener;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.MessageEventNotificationListener;
import org.jivesoftware.smackx.packet.VCard;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import ua.snuk182.asia.services.api.ProtocolException;
import ua.snuk182.asia.services.icq.inner.ICQServiceResponse;
import android.content.Context;
import android.content.res.TypedArray;

public class XMPPService extends AccountService implements ConnectionListener, MessageListener, ChatManagerListener, RosterListener, MessageEventNotificationListener, ChatStateListener {

	public static final byte[] statusValues = new byte[] { Buddy.ST_ONLINE, Buddy.ST_AWAY, Buddy.ST_NA, Buddy.ST_BUSY, Buddy.ST_FREE4CHAT, Buddy.ST_INVISIBLE };

	private static final String LOGIN_PORT = "loginport";

	private static final String LOGIN_HOST = "loginhost";

	private static final String PASSWORD = "password";

	private static final String JID = "jid";
	
	private static final Random random = new Random();

	private OnlineInfo onlineInfo;

	XMPPConnection connection;

	final Map<String, Chat> chats = new HashMap<String, Chat>();
	
	private volatile boolean isContactListReady = false;
	
	private List<OnlineInfo> infos = Collections.synchronizedList(new ArrayList<OnlineInfo>());

	private final DefaultMessageEventRequestListener messageEventListener = new DefaultMessageEventRequestListener();
	MessageEventManager messageEventManager;

	@Override
	public void processMessage(Chat chat, final Message message) {
		final String log = "message " + chat.getParticipant();
		log(log);
		/*new Thread(log){
			@Override 
			public void run(){
				try {
					TextMessage txtmessage = XMPPEntityAdapter.xmppMessage2TextMessage(message, serviceId);
					if (txtmessage.from.equals(getUserID())) {
						resetHeartbeat();
						return;
					}
					serviceResponse.respond(IAccountServiceResponse.RES_MESSAGE, getServiceId(), txtmessage);
				} catch (ProtocolException e) {
					log(e);
				}
			}
		}.start();*/
		
		try {
			TextMessage txtmessage = XMPPEntityAdapter.xmppMessage2TextMessage(message, serviceId);
			if (txtmessage.from.equals(getUserID())) {
				resetHeartbeat();
			}
			serviceResponse.respond(IAccountServiceResponse.RES_MESSAGE, getServiceId(), txtmessage);
		} catch (ProtocolException e) {
			log(e);
		}
	}

	@Override
	public void chatCreated(final Chat chat, boolean createdLocally) {
		final String log = "chat " + chat.getParticipant();
		log(log);
		/*new Thread(log){
			@Override
			public void run(){
				if (chats.get(XMPPEntityAdapter.normalizeJID(chat.getParticipant())) == null) {
					chat.addMessageListener(XMPPService.this);
					chats.put(XMPPEntityAdapter.normalizeJID(chat.getParticipant()), chat);
				}
			}
		}.start();*/
		
		if (chats.get(XMPPEntityAdapter.normalizeJID(chat.getParticipant())) == null) {
			chat.addMessageListener(this);
			chats.put(XMPPEntityAdapter.normalizeJID(chat.getParticipant()), chat);
		}
	}

	@Override
	public void presenceChanged(final Presence presence) {
		final String log = "presence "+presence.getFrom();
		log(log);
		/*new Thread(log){
			@Override
			public void run(){
				try {
					serviceResponse.respond(IAccountServiceResponse.RES_BUDDYSTATECHANGED, serviceId, XMPPEntityAdapter.presence2OnlineInfo(presence));
				} catch (ProtocolException e) {
					log(e);
				}
			}
		}.start();*/
		
		OnlineInfo info = XMPPEntityAdapter.presence2OnlineInfo(presence);
		synchronized (infos) {
			infos.add(info);
		}
		
		if (isContactListReady){
			checkCachedInfos();
		} 
	}

	private void checkCachedInfos() {
		synchronized (infos) {
			if (infos.size() > 0) {
				for (int i = infos.size() - 1; i >= 0; i--) {
					OnlineInfo info = infos.remove(i);
					try {
						serviceResponse.respond(IAccountServiceResponse.RES_BUDDYSTATECHANGED, serviceId, info);
					} catch (ProtocolException e) {
						log(e);
					}
				}
			}
		}		
	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		// TODO Auto-generated method stub

	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		// TODO Auto-generated method stub

	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		// TODO Auto-generated method stub

	}

	private String un = null;
	private String pw = null;
	private String serviceName = null;

	private String loginHost = "jabber.ru";
	private int loginPort = 5222;

	public XMPPService(Context context, IAccountServiceResponse serviceResponse, byte serviceId) {
		super(context, serviceResponse, serviceId);
		
		options.put(JID, null);
		options.put(PASSWORD, null);
		options.put(LOGIN_HOST, loginHost);
		options.put(LOGIN_PORT, loginPort + "");
		options.put(PING_TIMEOUT, pingTimeout + "");
	}

	public XMPPService(Context context) {
		super(context, null, (byte) -1);
	}

	@Override
	public Object request(short action, final Object... args) throws ProtocolException {
		switch (action) {
		case AccountService.REQ_GETBUDDYINFO:
			break;
		case AccountService.REQ_ADDGROUP:
			break;
		case AccountService.REQ_ADDBUDDY:
			break;
		case AccountService.REQ_REMOVEBUDDY:
			break;
		case AccountService.REQ_MOVEBUDDIES:
			break;
		case AccountService.REQ_REMOVEBUDDIES:
			break;
		case AccountService.REQ_SETSTATUS:
			if (connection != null) {
				onlineInfo.userStatus = (Byte) args[0];
				connection.sendPacket(XMPPEntityAdapter.userStatus2XMPPPresence(onlineInfo.userStatus));
			}
			break;
		case AccountService.REQ_SETEXTENDEDSTATUS:
			break;
		case AccountService.REQ_AUTHREQUEST:
			break;
		case AccountService.REQ_AUTHRESPONSE:
			break;
		case AccountService.REQ_SEARCHFORBUDDY_BY_UID:
			break;
		case AccountService.REQ_DISCONNECT:
			// closeKeepaliveThread();
			if (connection != null){
				connection.disconnect();
			}
			break;
		case AccountService.REQ_CONNECT:
			if (args.length > 0) {
				onlineInfo = new OnlineInfo();
				onlineInfo.protocolUid = getJID();
				onlineInfo.userStatus = (Byte) args[0];
			}
			connect();
			break;
		case AccountService.REQ_GETCONTACTLIST:
			break;
		case AccountService.REQ_GETEXTENDEDSTATUS:
			break;
		case AccountService.REQ_RENAMEBUDDY:
			break;
		case AccountService.REQ_RENAMEGROUP:
			break;
		case AccountService.REQ_MOVEBUDDY:
			break;
		case AccountService.REQ_GETGROUPLIST:
			break;
		case AccountService.REQ_SENDMESSAGE:
			try {
				sendMessage((TextMessage) args[0]);
			} catch (Exception e) {
				log(e);
				/*
				 * try {
				 * serviceResponse.respond(IAccountServiceResponse.RES_DISCONNECTED
				 * , getServiceId()); } catch (ProtocolException e1) { log(e1);
				 * }
				 */
			}
			break;
		case AccountService.REQ_GETICON:
			if (connection == null){
				return null;
			}
			getPersonalInfo((String) args[0]);
			break;
		case AccountService.REQ_REMOVEGROUP:
			break;
		case AccountService.REQ_SENDTYPING:
			if (connection == null){
				return null;
			}
			sendTyping((String)args[0]);
			break;
		}
		return null;
	}

	private void sendTyping(String jid) {
		messageEventManager.sendComposingNotification(jid, random.nextLong()+"");
	}

	private void sendMessage(TextMessage textMessage) throws XMPPException {
		Chat chat = chats.get(textMessage.to);
		if (chat == null) {
			chat = connection.getChatManager().createChat(textMessage.to, this);
			chats.put(textMessage.to, chat);
		}

		Message message = new Message(chat.getParticipant(), Message.Type.chat);
		message.setThread(chat.getThreadID());
		message.setPacketID(textMessage.messageId + "");
		message.setBody(textMessage.text);
		MessageEventManager.addNotificationsRequests(message, true, true, true, true);
		chat.sendMessage(message);

	}

	private String getJID() {
		return un + "@" + serviceName;
	}

	private void connect() throws ProtocolException {
		@SuppressWarnings("unchecked")
		Map<String, String> sharedPreferences = (Map<String, String>) serviceResponse.respond(IAccountServiceResponse.RES_GETFROMSTORAGE, getServiceId(), IAccountServiceResponse.SHARED_PREFERENCES, options.keySet());
		if (sharedPreferences == null) {
			throw new ProtocolException("Error getting preferences");
		}

		try {
			String jid = sharedPreferences.get(JID);
			String[] jidParams = jid.split("@");
			un = jidParams[0];
			serviceName = jidParams[1];
		} catch (Exception e2) {
			log(e2);
		}

		pw = sharedPreferences.get(PASSWORD);
		try {
			loginPort = Integer.parseInt(sharedPreferences.get(LOGIN_PORT));
		} catch (Exception e2) {
		}

		String host = sharedPreferences.get(LOGIN_HOST);
		if (host != null) {
			loginHost = host;
		}

		if (loginHost == null || loginPort < 1 || un == null || pw == null) {
			throw new ProtocolException("Error: no auth data");
		}

		String ping = sharedPreferences.get(AccountService.PING_TIMEOUT);
		if (ping != null) {
			try {
				pingTimeout = Integer.parseInt(ping);
			} catch (Exception e) {
			}
		}

		new Thread("XMPP connector " + getJID()) {
			@Override
			public void run() {
				SmackConfiguration.setPacketReplyTimeout(10000);
				ConnectionConfiguration config = new ConnectionConfiguration(loginHost, loginPort);
				config.setServiceName(serviceName);

				// config.setSASLAuthenticationEnabled(false);

				connection = new XMPPConnection(config);
				try {
					isContactListReady = false;
					serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, serviceId, 1);
					connection.connect();
					serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, serviceId, 2);
					connection.login(un, pw);
					/*ServiceDiscoveryManager discoManager = new ServiceDiscoveryManager(connection);
					discoManager.addFeature("http://jabber.org/protocol/disco#info");*/
					connection.addConnectionListener(XMPPService.this);
					serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, serviceId, 3);
					connection.sendPacket(XMPPEntityAdapter.userStatus2XMPPPresence(onlineInfo.userStatus));
					serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, serviceId, 5);
					Roster roster = connection.getRoster();
					roster.addRosterListener(XMPPService.this);
					List<Buddy> buddies = XMPPEntityAdapter.rosterEntryCollection2BuddyList(XMPPService.this, roster.getEntries(), getJID(), serviceId);
					List<BuddyGroup> groups = XMPPEntityAdapter.rosterGroupCollection2BuddyGroupList(roster.getGroups(), getJID(), serviceId);
					serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, serviceId, 7);
					connection.getChatManager().addChatListener(XMPPService.this);
					serviceResponse.respond(IAccountServiceResponse.RES_CLUPDATED, getServiceId(), buddies, groups);
					isContactListReady = true;
					serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, serviceId, 9);
					serviceResponse.respond(IAccountServiceResponse.RES_ACCOUNTUPDATED, serviceId, onlineInfo);
					serviceResponse.respond(IAccountServiceResponse.RES_CONNECTED, getServiceId());
					getPersonalInfo(getJID());
					checkCachedInfos();
					messageEventManager = new MessageEventManager(connection);
					if (onlineInfo.userStatus != Buddy.ST_INVISIBLE) {
						messageEventManager.addMessageEventRequestListener(messageEventListener);
					}
					messageEventManager.addMessageEventNotificationListener(XMPPService.this);
					sendKeepalive();
				} catch (Exception e) {
					log(e);
					connection = null;
					connectionClosedOnError(e);
					
				}
			}
		}.start();
	}

	private void respondInfo(VCard card, String jid, PersonalInfo personalInfo) throws ProtocolException {
		personalInfo.protocolUid = jid;
		String fn;
		if (card.getNickName() != null && card.getNickName().length() > 0) {
			personalInfo.properties.putString(PersonalInfo.INFO_NICK, card.getNickName());
			serviceResponse.respond(IAccountServiceResponse.RES_USERINFO, getServiceId(), personalInfo);
		} else if ((fn = card.getField("FN")) != null && fn.length() > 0){
			personalInfo.properties.putString(PersonalInfo.INFO_NICK, fn);
			serviceResponse.respond(IAccountServiceResponse.RES_USERINFO, getServiceId(), personalInfo);
		}
		byte[] icon = card.getAvatar();
		if (icon != null) {
			serviceResponse.respond(IAccountServiceResponse.RES_SAVEIMAGEFILE, getServiceId(), icon, jid, new String(card.getAvatarHash().hashCode() + ""));
		}
	}

	@Override
	public String getServiceName() {
		return context.getString(R.string.xmpp_service_name);
	}

	@Override
	public Map<String, String> getOptions() {
		return options;
	}

	@Override
	public int getProtocolOptionNames() {
		return R.array.xmpp_preference_names;
	}

	@Override
	public int getProtocolOptionDefaults() {
		return R.array.xmpp_preference_defaults;
	}

	@Override
	public int getProtocolOptionStrings() {
		return R.array.xmpp_preference_strings;
	}

	/*public static int getStatusResIdByStatusId(int statusId, int size) {

		if (size <= 16) {
			return getStatusResIdByStatusIdTiny(statusId);
		} else if (size <= 24) {
			return getStatusResIdByStatusIdSmall(statusId);
		} else if (size <= 32) {
			return getStatusResIdByStatusIdMedium(statusId);
		} else {
			return getStatusResIdByStatusIdBig(statusId);
		}
	}*/
	
	public static int getStatusResIdByStatusIdBigger(int statusId) {
		int statusResId;

		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_bigger;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_bigger;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_bigger;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_bigger;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_bigger;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_bigger;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_bigger;
			break;
		}

		return statusResId;
	}

	public static int getStatusResIdByStatusIdBig(int statusId) {
		int statusResId;

		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_big;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_big;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_big;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_big;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_big;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_big;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_big;
			break;
		}

		return statusResId;
	}

	/*private static int getStatusResIdByStatusId64(int statusId) {
		int statusResId;

		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_64;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_64;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_64;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_64;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_64;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_64;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_64;
			break;
		}

		return statusResId;
	}*/

	public static int getStatusResIdByStatusIdSmall(int statusId) {
		int statusResId;

		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_small;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_small;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_small;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_small;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_small;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_small;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_small;
			break;
		}

		return statusResId;
	}

	public static int getStatusResIdByStatusIdMedium(int statusId) {
		int statusResId;

		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_medium;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_medium;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_medium;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_medium;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_medium;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_medium;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_medium;
			break;
		}

		return statusResId;
	}

	public static int getStatusResIdByStatusIdTiny(int statusId) {
		int statusResId;

		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_tiny;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_tiny;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_tiny;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_tiny;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_tiny;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_tiny;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_tiny;
			break;
		}

		return statusResId;
	}

	/*public static int getStatusResIdByAccount(AccountView account, int size, boolean withoutConnectionState) {

		if (size <= 16) {
			return getStatusResIdByAccountTiny(account, withoutConnectionState);
		} else {
			return getStatusResIdByAccountMedium(account, withoutConnectionState);
		}
	}
*/
	public static int getStatusResIdByAccountTiny(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState) {
			switch (account.getConnectionState()) {
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.xmpp_offline_tiny;
			case AccountService.STATE_CONNECTING:
				return R.drawable.xmpp_connecting_tiny;
			}
		}

		return getStatusResIdByStatusIdTiny(account.status);
	}
	
	public static final int getStatusResIdByAccountSmall(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState) {
			switch (account.getConnectionState()) {
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.xmpp_offline_small;
			case AccountService.STATE_CONNECTING:
				return R.drawable.xmpp_connecting_small;
			}
		}

		return getStatusResIdByStatusIdSmall(account.status);
	}

	public static final int getStatusResIdByAccountMedium(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState) {
			switch (account.getConnectionState()) {
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.xmpp_offline_medium;
			case AccountService.STATE_CONNECTING:
				return R.drawable.xmpp_connecting_medium;
			}
		}

		return getStatusResIdByStatusIdMedium(account.status);
	}

	/*private static final int getStatusResIdByAccount48(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState) {
			switch (account.getConnectionState()) {
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.xmpp_offline_48;
			case AccountService.STATE_CONNECTING:
				return R.drawable.xmpp_connecting_48;
			}
		}

		return getStatusResIdByStatusId48(account.status);
	}*/

	public static int getMenuResIdByAccount(AccountView account) {
		return R.menu.xmpp_cl_menu;
	}

	public static final String[] getStatusListNames(Context context) {
		return context.getResources().getStringArray(R.array.xmpp_status_strings);
	}

	public static final byte getStatusValueByCount(int count) {
		return statusValues[count];
	}

	private final void getPersonalInfo(final String jid) {
		new Thread("XMPP icon getter " + jid) {
			@Override
			public void run() {
				VCard card = new VCard();
				try {
					SmackConfiguration.setPacketReplyTimeout(25000);
					card.load(connection, jid);
					log("got card " + jid + " " + card.getNickName() + " " + card.getAvatar());
					respondInfo(card, jid, new PersonalInfo());
				} catch (Exception e) {
					log(e);
					return;
				}

			}
		}.start();
	}

	@Override
	protected void timeoutDisconnect() {
		closeKeepaliveThread();
		connection.disconnect();
		/*
		 * try { serviceResponse.respond(ICQServiceResponse.RES_DISCONNECTED,
		 * serviceId); } catch (ProtocolException e) { log(e); }
		 */
	}

	@Override
	protected short getCurrentState() {
		return connection.isConnected() ? AccountService.STATE_CONNECTED : AccountService.STATE_DISCONNECTED;
	}

	@Override
	protected String getUserID() {
		return getJID();
	}

	public static TypedArray getStatusResIds(Context context) {
		return context.getResources().obtainTypedArray(R.array.xmpp_status_icons);
	}

	@Override
	public void connectionClosed() {
		log("Connection closed " + getJID());
		closeKeepaliveThread();
		isContactListReady = false;
		try {
			serviceResponse.respond(ICQServiceResponse.RES_DISCONNECTED, serviceId);
		} catch (ProtocolException e) {
			log(e);
		}
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		log("Connection closed " + getJID() + ": " + e.getLocalizedMessage());
		closeKeepaliveThread();
		isContactListReady = false;
		try {
			if ((e instanceof IOException) || (e instanceof XMPPException && ((XMPPException) e).getXMPPError() != null && ((XMPPException) e).getXMPPError().getCondition().equals("remote-server-timeout"))) {
				serviceResponse.respond(IAccountServiceResponse.RES_DISCONNECTED, getServiceId());
			} else {
				serviceResponse.respond(IAccountServiceResponse.RES_DISCONNECTED, getServiceId(), e.getLocalizedMessage());
			}
		} catch (ProtocolException e1) {
			log(e1);
		}
	}

	@Override
	public void reconnectingIn(int seconds) {
		log("Connection reconnect " + getJID() + " in " + seconds);
		try {
			serviceResponse.respond(ICQServiceResponse.RES_CONNECTING, serviceId, 1);
		} catch (ProtocolException e) {
			log(e);
		}
	}

	@Override
	public void reconnectionSuccessful() {
		log("Reconnected " + getJID());
		try {
			serviceResponse.respond(IAccountServiceResponse.RES_CONNECTED, getServiceId());
		} catch (ProtocolException e) {
			log(e);
		}
	}

	@Override
	public void reconnectionFailed(Exception e) {
		log("Reconnection failed " + getJID());
		connectionClosedOnError(e);
	}

	@Override
	public void cancelledNotification(String from, String packetID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void composingNotification(String from, String packetID) {
		try {
			serviceResponse.respond(IAccountServiceResponse.RES_TYPING, getServiceId(), XMPPEntityAdapter.normalizeJID(from));
		} catch (ProtocolException e) {
			log(e);
		}
	}

	@Override
	public void deliveredNotification(String from, String packetID) {
		long messageId = Long.parseLong(packetID);
		log(getJID() + " - " + from + " delivered " + messageId);
		try {
			serviceResponse.respond(IAccountServiceResponse.RES_MESSAGEACK, getServiceId(), XMPPEntityAdapter.normalizeJID(from), messageId, 2);
		} catch (ProtocolException e) {
			log(e);
		}
	}

	@Override
	public void displayedNotification(String from, String packetID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void offlineNotification(String from, String packetID) {
		long messageId = Long.parseLong(packetID);
		log(getJID() + " - " + from + " delivered " + messageId);
		try {
			serviceResponse.respond(IAccountServiceResponse.RES_MESSAGEACK, getServiceId(), XMPPEntityAdapter.normalizeJID(from), messageId, 1);
		} catch (ProtocolException e) {
			log(e);
		}
	}

	@Override
	public void stateChanged(Chat chat, ChatState state) {
		if (state == ChatState.composing){
			try {
				serviceResponse.respond(IAccountServiceResponse.RES_TYPING, getServiceId(), XMPPEntityAdapter.normalizeJID(chat.getParticipant()));
			} catch (ProtocolException e) {
				log(e);
			}
		}
	}
}
