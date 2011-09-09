package ua.snuk182.asia.services.mrim;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.api.ProtocolUtils;
import ua.snuk182.asia.services.mrim.inner.MrimConstants;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimBuddy;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimGroup;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimMessage;
import ua.snuk182.asia.services.mrim.inner.dataentity.MrimOnlineInfo;

public final class MrimEntityAdapter {

	public static final String[] xstatuses = { "status_4", "status_5", "status_6", "status_7", "status_8", "status_9", "status_10", "status_11", "status_12", "status_13", "status_14", "status_15", "status_16", "status_18", "status_19", "status_20", "status_21", "status_22", "status_23", "status_24", "status_26", "status_27", "status_28", "status_51", "status_52", "status_46", "status_48", "status_47" };

	public static final int userStatus2MrimUserStatus(Byte status) {
		switch (status) {
		case Buddy.ST_OFFLINE:
			return MrimConstants.STATUS_OFFLINE;
		case Buddy.ST_AWAY:
			return MrimConstants.STATUS_AWAY;
		case Buddy.ST_INVISIBLE:
			return MrimConstants.STATUS_OFFLINE | MrimConstants.STATUS_FLAG_INVISIBLE;
		case Buddy.ST_OTHER:
		case Buddy.ST_FREE4CHAT:
			return MrimConstants.STATUS_OTHER;
		default:
			return MrimConstants.STATUS_ONLINE;
		}
	}
	
	public static final String userXStatus2MrimXStatus(byte xstatus){
		try {
			return xstatuses[xstatus];
		} catch (Exception e) {
			return "";
		}
	}

	public static final int skipFormatted(byte[] dump, String format, int pos, int processed) {
		int i = pos;
		while (processed < format.length()) {
			if (format.charAt(processed) == 's') {
				int strLen = (int) ul2Long(dump, pos);
				pos += 4 + strLen;
			} else if (format.charAt(processed) == 'z') {
				while (dump[pos] != 0) {
					pos++;
				}
			} else {
				pos += 4;
			}
			processed++;
		}

		return pos - i;
	}

	public static final byte[] string2lpsa(String string) {
		if (string == null || string.length() < 1) {
			return ProtocolUtils.int2ByteLE(0);
		}

		byte[] strBytes;
		try {
			strBytes = string.getBytes("windows-1251");
		} catch (UnsupportedEncodingException e) {
			strBytes = string.getBytes();
		}

		byte[] out = new byte[4 + strBytes.length];
		System.arraycopy(ProtocolUtils.int2ByteLE(strBytes.length), 0, out, 0, 4);
		System.arraycopy(strBytes, 0, out, 4, strBytes.length);

		return out;
	}

	public static final byte[] string2lpsw(String string) {
		if (string == null || string.length() < 1) {
			return ProtocolUtils.int2ByteLE(0);
		}

		byte[] strBytes;
		try {
			strBytes = string.getBytes("UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			strBytes = string.getBytes();
		}

		byte[] out = new byte[4 + strBytes.length];
		System.arraycopy(ProtocolUtils.int2ByteLE(strBytes.length), 0, out, 0, 4);
		System.arraycopy(strBytes, 0, out, 4, strBytes.length);

		return out;
	}

	public static final String lpsa2String(byte[] dump, int pos) {
		int len = ProtocolUtils.bytes2IntLE(dump, pos);
		pos += 4;
		if (len < 1) {
			return "";
		}
		String str;
		try {
			str = new String(dump, pos, len, "windows-1251");
		} catch (UnsupportedEncodingException e) {
			str = new String(dump, pos, len);
		} catch (Exception ee){
			byte[] dummy = new byte[len];
			Arrays.fill(dummy, (byte) ' ');
			try {
				str = new String(dummy, "windows-1251");
			} catch (UnsupportedEncodingException e) {
				str = new String(dummy);
			}
		}
		return str;
	}

	public static final long ul2Long(byte[] dump, int pos) {
		return ProtocolUtils.unsignedInt2Long(ProtocolUtils.bytes2IntLE(dump, pos));
	}
	
	public static final byte[] spacedHexString2Bytes(String str){
		String[] strings = str.split(" ");
		byte[] array = new byte[strings.length];
		Arrays.fill(array, (byte) 0);
		for (int i=0; i<strings.length; i++){
			String hex = strings[i];
			array[i] = (byte) Integer.parseInt(hex, 16);
		}
		
		return array;
	}

	public static final String lpsw2String(byte[] dump, int pos) {
		int len = ProtocolUtils.bytes2IntLE(dump, pos);
		pos += 4;
		if (len < 1) {
			return "";
		}
		if ((len % 2) > 0){
			len--;
		}
		String str;
		try {
			str = new String(dump, pos, len, "UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			str = new String(dump, pos, len);
		} catch (Exception ee){
			byte[] dummy = new byte[len];
			Arrays.fill(dummy, (byte) ' ');
			try {
				str = new String(dummy, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				str = new String(dummy);
			}
		}
		return str;
	}

	private static String long2hex(long val) {
		String result = Long.toString(val, 16);
		while (result.length() < 8) {
			result = "0" + result;
		}
		return result;
	}

	public static final Buddy mrimBuddy2Buddy(MrimService service, MrimBuddy in, String ownerUid, byte serviceId) {
		Buddy out = new Buddy(in.uin, ownerUid, service.getServiceName(), serviceId);
		out.id = in.id;
		out.name = in.name;
		out.groupId = in.groupId;
		out.status = mrimUserStatus2UserStatus(in.onlineInfo.status, in.onlineInfo.xstatusId);
		out.xstatus = mrimXStatus2XStatus(in.onlineInfo.xstatusId);
		out.xstatusName = in.onlineInfo.xstatusName;
		out.xstatusDescription = in.onlineInfo.xstatusText;
		out.visibility = mrimVisibility2Visibility(in);

		return out;
	}

	public static final BuddyGroup mrimBuddyGroup2BuddyGroup(MrimGroup in, String ownerUid, byte serviceId, List<MrimBuddy> buddies) {
		BuddyGroup group = new BuddyGroup(in.groupId, ownerUid, serviceId);
		group.name = in.name;

		for (MrimBuddy buddy : buddies) {
			if (buddy.groupId == in.groupId) {
				group.buddyList.add(buddy.id);
			}
		}

		return group;
	}

	public static final List<BuddyGroup> mrimBuddyGroupList2BuddyGroupList(List<MrimGroup> groupList, String ownerId, byte serviceId, List<MrimBuddy> buddies) {
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>(groupList.size());
		for (MrimGroup group : groupList) {
			groups.add(mrimBuddyGroup2BuddyGroup(group, ownerId, serviceId, buddies));
		}

		return groups;
	}

	public static final List<Buddy> mrimBuddyList2Buddylist(MrimService service, List<MrimBuddy> buddyList, String ownerUid, byte serviceId) {
		List<Buddy> buddies = new ArrayList<Buddy>();
		for (MrimBuddy buddy : buddyList) {
			buddies.add(mrimBuddy2Buddy(service, buddy, ownerUid, serviceId));
		}
		return buddies;
	}

	private static final byte mrimVisibility2Visibility(MrimBuddy in) {
		if ((in.flags & MrimConstants.CONTACT_INTFLAG_NOT_AUTHORIZED) != 0) {
			return Buddy.VIS_NOT_AUTHORIZED;
		}

		return Buddy.VIS_REGULAR;
	}

	private static final byte mrimXStatus2XStatus(String xstatusId) {
		for (byte i=0; i<xstatuses.length; i++){
			if (xstatuses[i].equalsIgnoreCase(xstatusId)){
				return i;
			}
		}
		return -1;
	}

	private static final byte mrimUserStatus2UserStatus(int status, String xstatusName) {
		if ((status & MrimConstants.STATUS_FLAG_INVISIBLE) != 0) {
			return Buddy.ST_INVISIBLE;
		}
		
		if (xstatusName.equalsIgnoreCase("status_chat")){
			return Buddy.ST_FREE4CHAT;
		}

		switch (status) {
		case MrimConstants.STATUS_OFFLINE:
			return Buddy.ST_OFFLINE;
		case MrimConstants.STATUS_AWAY:
			return Buddy.ST_AWAY;
		case MrimConstants.STATUS_UNDETERMINATED:
			return Buddy.ST_OTHER;
		default:
			return Buddy.ST_ONLINE;
		}
	}

	public static final String getHexLong(byte[] dump, int pos) {
		long x1 = ul2Long(dump, pos) & 0xFFFFFFFFL;
		long x2 = ul2Long(dump, pos) & 0xFFFFFFFFL;
		return (long2hex(x2) + long2hex(x1)).toUpperCase();
	}

	public static TextMessage mrimMessage2TextMessage(MrimMessage msg, byte serviceId) {
		if (msg == null)
			return null;
		TextMessage txtMsg = new TextMessage(msg.from);
		txtMsg.text = msg.text;
		txtMsg.time = new Date();
		txtMsg.serviceId = serviceId;
		txtMsg.messageId = msg.messageId;
		return txtMsg;
	}

	public static MrimMessage textMessage2MrimMessage(TextMessage textMessage) {
		if (textMessage == null) {
			return null;
		}
		MrimMessage msg = new MrimMessage();
		msg.from = textMessage.from;
		msg.text = textMessage.text;
		msg.messageId = (int) textMessage.messageId;
		msg.to = textMessage.to;

		return msg;
	}

	public static OnlineInfo mrimOnlineInfo2OnlineInfo(MrimOnlineInfo in) {
		if (in == null) {
			return null;
		}
		OnlineInfo out = new OnlineInfo();
		out.userStatus = mrimUserStatus2UserStatus(in.status, in.xstatusId);
		out.xstatus = mrimXStatus2XStatus(in.xstatusId);
		out.canFileShare = false;
		out.xstatusName = in.xstatusName;
		out.xstatusDescription = in.xstatusText;
		out.protocolUid = in.uin;

		return out;
	}
}
