package ua.snuk182.asia.services.xmpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.packet.EncryptedMessage;
import org.jivesoftware.smackx.packet.SignedPresence;
import org.jivesoftware.smackx.provider.EncryptedDataProvider;

import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;

public final class XMPPEntityAdapter {
	
	public static final TextMessage xmppMessage2TextMessage(Message message, byte serviceId, boolean resourceAsWriterId, EncryptedDataProvider edp){
		if (message == null){
			return null;
		}
		
		TextMessage txtMessage;
		if (resourceAsWriterId){
			String[] reqisites = message.getFrom().split("/");		
			txtMessage = new TextMessage(reqisites[1], reqisites[0]);			
		} else {
			String from	= normalizeJID(message.getFrom());
			txtMessage = new TextMessage(from, from);
		}
		txtMessage.serviceId = serviceId;
		txtMessage.messageId = message.getPacketID() != null ? message.getPacketID().hashCode() : message.hashCode();
		txtMessage.time = new Date();
		txtMessage.text = message.getBody();
		txtMessage.to = normalizeJID(message.getTo());
		
		if (edp != null){
			PacketExtension ext = message.getExtension("x", "jabber:x:encrypted");
			if (ext != null){
				EncryptedMessage ems = (EncryptedMessage) ext;
				try {
					txtMessage.text = ems.decryptAndGet();	
					txtMessage.options = TextMessage.OPT_SECURE;
				} catch (Exception e) {
					ServiceUtils.log(e);
				}
			}
		}
		
		return txtMessage;
	}

	public static final Presence userStatus2XMPPPresence(Byte status, EncryptedDataProvider edp) {
		Presence presence = new Presence(Type.available);
		switch (status) {
		case Buddy.ST_AWAY:
			presence.setMode(Mode.away);
			break;
		case Buddy.ST_DND:
			presence.setMode(Mode.dnd);
			break;
		case Buddy.ST_NA:
			presence.setMode(Mode.xa);
			break;
		case Buddy.ST_ONLINE:
			presence.setMode(Mode.available);
			break;
		case Buddy.ST_FREE4CHAT:
			presence.setMode(Mode.chat);
			break;
		}
		
		if (edp != null){
			SignedPresence spr = new SignedPresence(edp.myKey, edp.myKeyPw);
			try {
				spr.signAndSet(presence.getStatus());
				presence.addExtension(spr);
			} catch (XMPPException e) {
				ServiceUtils.log(e);
			}
		}
		
		return presence;
	}
	
	public static byte xmppPresence2UserStatus(Presence presence) {
		if (presence == null || presence.getType() != Type.available){
			return Buddy.ST_OFFLINE;
		}
		
		if (presence.getMode() == null || presence.getMode() == Mode.available){
			return Buddy.ST_ONLINE;
		}
		
		if (presence.getMode() == Mode.dnd){
			return Buddy.ST_DND;
		}
		if (presence.getMode() == Mode.xa){
			return Buddy.ST_NA;
		}
		if (presence.getMode() == Mode.chat){
			return Buddy.ST_FREE4CHAT;
		}
		if (presence.getMode() == Mode.away){
			return Buddy.ST_AWAY;
		}
				
		return Buddy.ST_OFFLINE;
	}
	
	@SuppressWarnings("unchecked")
	public static final OnlineInfo presence2OnlineInfo(Presence presence, EncryptedDataProvider edp, IAccountServiceResponse response, byte serviceId){
		if (presence == null){
			return null;
		}
		OnlineInfo info = new OnlineInfo();
		info.protocolUid = normalizeJID(presence.getFrom());
		
		info.userStatus = xmppPresence2UserStatus(presence);
		info.xstatusName = presence.getStatus();
		if (info.userStatus != Buddy.ST_OFFLINE){
			info.canFileShare = true;
		}
		
		if (edp != null){
			PacketExtension ext = presence.getExtension("x", "jabber:x:signed");
			if (ext != null){
				SignedPresence spr = (SignedPresence) ext;
				String result;
				try {
					Set<String> keys = new HashSet<String>();
					keys.add(info.protocolUid);
					String buddyPGPKey = (String) ((HashMap<String, String>) response.respond(IAccountServiceResponse.RES_GETFROMSTORAGE, serviceId, AccountService.BUDDY_KEY_STORAGE, keys)).get(info.protocolUid);
					result = spr.verifyAndGet(buddyPGPKey);
					edp.keyStorage.put(info.protocolUid, buddyPGPKey);
					//result = spr.verifyAndGet("//sdcard/snuk182p.asc");
				} catch (Exception e) {
					ServiceUtils.log(e);
					result = null;
				}
				
				info.secureOptions = result != null ? Buddy.SECURE_ENABLED : Buddy.SECURE_SUPPORTS;
			} else {
				info.secureOptions = Buddy.SECURE_NOSUPPORT;
			}
		} else {
			info.secureOptions = Buddy.SECURE_NOSUPPORT;
		}
		
		return info;
	}
	
	public static String normalizeJID(String jid){
		/*if (jid == null){
			return null;
		}
		if (jid.indexOf("/")>-1){
			return jid.split("/")[0];
		}
		
		return jid;*/
		return StringUtils.parseBareAddress(jid);
	}
	
	public static String getClientId(String jid){
		if (jid == null){
			return null;
		}
		if (jid.indexOf("/")>-1){
			return jid.split("/")[1];
		}
		
		return jid;
	}

	public static final Buddy rosterEntry2Buddy(XMPPService service, RosterEntry entry){
		if (entry == null){
			return null;
		}
		Buddy buddy = new Buddy(normalizeJID(entry.getUser()), service.getUserID(), service.getServiceName(), service.getServiceId());
		buddy.name = entry.getName();
		buddy.id = entry.getUser().hashCode();
		buddy.clientId = getClientId(entry.getUser());
		if (entry.getStatus()!=null && entry.getStatus().equals(RosterPacket.ItemStatus.SUBSCRIPTION_PENDING)){
			buddy.visibility = Buddy.VIS_NOT_AUTHORIZED;
		}
		
		return buddy;
	}
	
	public static final BuddyGroup rosterGroup2BuddyGroup(RosterGroup entry, String ownerUid, byte serviceId, List<Buddy> buddies){
		if (entry == null){
			return null;
		}
		BuddyGroup group = new BuddyGroup(serviceId, ownerUid, serviceId);
		group.id = entry.hashCode();
		group.name = entry.getName();
		for (RosterEntry buddy: entry.getEntries()){
			for (Buddy buu : buddies){
				if (buddy.getUser().hashCode() == buu.id){
					buu.groupId = group.id;
					group.buddyList.add(buu.id);
				}
			}			
		}
		return group;
	}
	
	public static final List<Buddy> rosterEntryCollection2BuddyList(XMPPService service, Collection<RosterEntry> entries){
		if (entries == null){
			return null;
		}
		List<Buddy> buddies = new ArrayList<Buddy>(entries.size());
		for (RosterEntry entry: entries){
			buddies.add(rosterEntry2Buddy(service, entry));
		}
		return buddies;
	}
	
	public static final List<BuddyGroup> rosterGroupCollection2BuddyGroupList(Collection<RosterGroup> entries, String ownerUid, byte serviceId, List<Buddy> buddies){
		if (entries == null){
			return null;
		}
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>(entries.size());
		for (RosterGroup entry: entries){
			groups.add(rosterGroup2BuddyGroup(entry, ownerUid, serviceId, buddies));
		}
		return Collections.unmodifiableList(groups);
	}

	public static final List<MultiChatRoom> xmppHostedRooms2MultiChatRooms(XMPPService service, Collection<HostedRoom> hostedRooms) {
		if (hostedRooms == null){
			return new ArrayList<MultiChatRoom>(0);
		}
		
		List<MultiChatRoom> chats = new ArrayList<MultiChatRoom>(hostedRooms.size());
		for (HostedRoom room: hostedRooms){
			chats.add(xmppHostedRoom2MultiChatRoom(service, room));
		}
		return chats;
	}

	public static final MultiChatRoom xmppHostedRoom2MultiChatRoom(XMPPService service, HostedRoom room) {
		MultiChatRoom chat = new MultiChatRoom(room.getJid(), service.getUserID(), service.getServiceName(), service.getServiceId());
		chat.name = room.getName();
		return chat;
	}

	public static MultiChatRoom xmppRoomInfo2MultiChatRoom(XMPPService service, RoomInfo info) {
		MultiChatRoom chat = new MultiChatRoom(info.getRoom(), service.getUserID(), service.getServiceName(), service.getServiceId());
		chat.name = info.getDescription();
		return chat;
	}

	public static Buddy chatRoomInfo2Buddy(XMPPService service, RoomInfo info, String ownerUid, byte serviceId, boolean joined) {
		Buddy buddy = new Buddy(normalizeJID(info.getRoom()), ownerUid, service.getServiceName(), serviceId);
		buddy.name = (info.getSubject()!= null && info.getSubject().length() > 0) ? info.getSubject() : info.getRoom();
		buddy.xstatusName = info.getDescription();
		buddy.id = buddy.protocolUid.hashCode();
		buddy.visibility = Buddy.VIS_GROUPCHAT;
		if (joined){
			buddy.status = Buddy.ST_ONLINE;
		}
		
		return buddy;
	}

	public static Buddy chatInfo2Buddy(XMPPService service, String chatId, String chatName, boolean joined) {
		Buddy buddy = new Buddy(chatId, service.getUserID(), service.getServiceName(), service.getServiceId());
		buddy.name = chatName;
		buddy.id = buddy.protocolUid.hashCode();
		buddy.visibility = Buddy.VIS_GROUPCHAT;
		if (joined){
			buddy.status = Buddy.ST_ONLINE;
		}
		
		return buddy;
	}

	public static Message textMessage2XMPPMessage(TextMessage textMessage, String thread, String to, Message.Type messageType, EncryptedDataProvider edp) throws Exception {
		Message message = new Message(to, messageType);
		message.setThread(thread);
		message.setPacketID(textMessage.messageId + "");
		MessageEventManager.addNotificationsRequests(message, true, true, true, true);
		
		if (textMessage.options == TextMessage.OPT_SECURE && edp != null){
			EncryptedMessage ems = new EncryptedMessage(edp.myKey, edp.myKeyPw);
			ems.setAndEncrypt(textMessage.text, edp.keyStorage.get(to));
			message.setBody("Encrypted message");			
			message.addExtension(ems);
		} else {
			message.setBody(textMessage.text);			
		}
		
		return message;
	}
	
	public static final MultiChatRoomOccupants xmppMUCOccupants2mcrOccupants(MultiUserChat muc, XMPPService service, boolean loadIcons) throws XMPPException {
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>();
		BuddyGroup moderators = new BuddyGroup(2, service.getUserID(), service.getServiceId());
		BuddyGroup participants = new BuddyGroup(5, service.getUserID(), service.getServiceId());
		BuddyGroup other = new BuddyGroup(7, service.getUserID(), service.getServiceId());
		BuddyGroup all = new BuddyGroup(8, service.getUserID(), service.getServiceId());
		moderators.name = "Moderators";
		participants.name = "Participants";
		other.name = "Other";
		all.name = "All";
		
		Map<String, Buddy> buddies = new HashMap<String, Buddy>();
		
		Iterator<String> it = muc.getOccupants(); 
		
		for (;it.hasNext();){
			String occupant = it.next();
			String buddyId;
			Occupant occu = muc.getOccupant(occupant);
			if (occu != null && occu.getJid() != null){
				buddyId = normalizeJID(occu.getJid());
				if (loadIcons){
					try {
						service.loadCard(buddyId);
					} catch (Exception e) {
						service.log(e);
					}
				}
			} else {
				buddyId = occupant;
			}
			Buddy buddy = new Buddy(buddyId, service.getUserID(), service.getServiceName(), service.getServiceId());
			buddy.name = buddyId.equals(occupant) ? StringUtils.parseResource(occupant) : occu.getNick();
			buddy.status = xmppPresence2UserStatus(muc.getOccupantPresence(occupant));
			
			buddies.put(buddy.protocolUid, buddy);
			buddy.id = buddyId.hashCode();
			all.buddyList.add(buddy.id);
		}
		
		try {
			fillMUCGroup(muc, service, muc.getParticipants(), participants, loadIcons, buddies);
			fillMUCGroup(muc, service, muc.getModerators(), moderators, loadIcons, buddies);
			groups.add(moderators);
			groups.add(participants);
			groups.add(other);
		} catch (Exception e1) {
			service.log(e1);
		}
		
		if (groups.size() < 1){
			groups.add(all);			
		}
		
		MultiChatRoomOccupants mcro = new MultiChatRoomOccupants(service.getServiceId(), groups, Collections.unmodifiableList(new ArrayList<Buddy>(buddies.values())));
		
		return mcro;
	}

	private static void fillMUCGroup(MultiUserChat muc, XMPPService service, Collection<Occupant> occupants, BuddyGroup group, boolean loadIcons, Map<String, Buddy> map) {
		for (Occupant occu : occupants){
			group.buddyList.add(normalizeJID(occu.getJid()).hashCode());
			String buddyId = normalizeJID(occu.getJid());
			if (loadIcons){
				try {
					service.loadCard(buddyId);
				} catch (Exception e) {
					service.log(e);
				}
			}
			Buddy buddy = new Buddy(buddyId, service.getUserID(), service.getServiceName(), service.getServiceId());
			buddy.name = occu.getNick();
			
			byte status = xmppPresence2UserStatus(muc.getOccupantPresence(muc.getRoom()+"/"+occu.getNick()));
			buddy.status = status != Buddy.ST_OFFLINE ? status : Buddy.ST_ONLINE;
		
			map.put(buddy.protocolUid, buddy);
			buddy.id = buddyId.hashCode();
		}		
	}

	public static RosterEntry buddy2RosterEntry(XMPPConnection connection, Buddy buddy) {		
		return connection.getRoster().getEntry(buddy.protocolUid);
	}

	public static RosterGroup buddyGroup2RosterEntry(XMPPConnection connection, BuddyGroup buddyGroup) {
		for (RosterGroup group: connection.getRoster().getGroups()){
			if (group.hashCode() == buddyGroup.id){
				return group;
			}
		}
		return null;
	}
}
