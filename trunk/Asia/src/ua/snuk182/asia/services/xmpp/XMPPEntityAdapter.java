package ua.snuk182.asia.services.xmpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;

public final class XMPPEntityAdapter {
	
	public static final TextMessage xmppMessage2TextMessage(Message message, byte serviceId){
		if (message == null){
			return null;
		}
		
		TextMessage txtMessage = new TextMessage(normalizeJID(message.getFrom()));
		txtMessage.serviceId = serviceId;
		txtMessage.messageId = message.getPacketID() != null ? message.getPacketID().hashCode() : message.hashCode();
		txtMessage.time = new Date();
		txtMessage.text = message.getBody();
		txtMessage.to = normalizeJID(message.getTo());
		
		return txtMessage;
	}

	public static final Presence userStatus2XMPPPresence(Byte status) {
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
	
	public static final OnlineInfo presence2OnlineInfo(Presence presence){
		if (presence == null){
			return null;
		}
		OnlineInfo info = new OnlineInfo();
		info.protocolUid = normalizeJID(presence.getFrom());
		
		info.userStatus = xmppPresence2UserStatus(presence);
		info.xstatusName = presence.getStatus();
		
		return info;
	}
	
	public static String normalizeJID(String jid){
		if (jid == null){
			return null;
		}
		if (jid.indexOf("/")>-1){
			return jid.split("/")[0];
		}
		
		return jid;
	}

	public static final Buddy rosterEntry2Buddy(XMPPService service, RosterEntry entry, String ownerUid, byte serviceId){
		if (entry == null){
			return null;
		}
		Buddy buddy = new Buddy(normalizeJID(entry.getUser()), ownerUid, service.getServiceName(), serviceId);
		buddy.name = entry.getName();
		buddy.id = entry.getUser().hashCode();
		
		return buddy;
	}
	
	public static final BuddyGroup rosterGroup2BuddyGroup(RosterGroup entry, String ownerUid, byte serviceId){
		if (entry == null){
			return null;
		}
		BuddyGroup group = new BuddyGroup(serviceId, ownerUid, serviceId);
		group.id = entry.hashCode();
		group.name = entry.getName();
		for (RosterEntry buddy: entry.getEntries()){
			group.buddyList.add(buddy.getUser().hashCode());
		}
		return group;
	}
	
	public static final List<Buddy> rosterEntryCollection2BuddyList(XMPPService service, Collection<RosterEntry> entries, String ownerUid, byte serviceId){
		if (entries == null){
			return null;
		}
		List<Buddy> buddies = new ArrayList<Buddy>(entries.size());
		for (RosterEntry entry: entries){
			buddies.add(rosterEntry2Buddy(service, entry, ownerUid, serviceId));
		}
		return buddies;
	}
	
	public static final List<BuddyGroup> rosterGroupCollection2BuddyGroupList(Collection<RosterGroup> entries, String ownerUid, byte serviceId){
		if (entries == null){
			return null;
		}
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>(entries.size());
		for (RosterGroup entry: entries){
			groups.add(rosterGroup2BuddyGroup(entry, ownerUid, serviceId));
		}
		return groups;
	}
}
