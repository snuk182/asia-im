package ua.snuk182.asia.services.icq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileInfo;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.api.ProtocolUtils;
import ua.snuk182.asia.services.icq.inner.ICQConstants;
import ua.snuk182.asia.services.icq.inner.dataentity.ICBMMessage;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQBuddy;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQBuddyGroup;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQFileInfo;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQOnlineInfo;
import ua.snuk182.asia.services.icq.inner.dataentity.ICQPersonalInfo;
import ua.snuk182.asia.services.utils.Base64;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public final class ICQEntityAdapter {
	public static final ICQBuddy buddy2ICQBuddy(Buddy buddy){
		ICQBuddy icqBuddy = new ICQBuddy();
		icqBuddy.itemId = buddy.id;
		icqBuddy.groupId = buddy.groupId;
		icqBuddy.screenName = buddy.name;
		icqBuddy.uin = buddy.protocolUid;
		icqBuddy.visibility = buddy.visibility;
		
		return icqBuddy;
	}
	
	public static final ICQBuddyGroup buddyGroup2ICQBuddyGroup(BuddyGroup ggroup){
		ICQBuddyGroup group = new ICQBuddyGroup();
		group.name = ggroup.name;
		group.groupId = ggroup.id;
		group.buddies = ggroup.buddyList;
		
		return group;
	}
	
	public static final Buddy ICQBuddy2Buddy(ICQService service, ICQBuddy icqBuddy, String ownerUid, byte serviceId){
		Buddy buddy = new Buddy(icqBuddy.uin, ownerUid, service.getServiceName(), serviceId);
		buddy.name = icqBuddy.screenName;
		buddy.id = icqBuddy.itemId;
		buddy.groupId = icqBuddy.groupId;
		
		buddy.visibility = icqBuddy.visibility;
		
		if ((icqBuddy.onlineInfo.userStatus & ICQConstants.STATUS_AWAY)>0){
			buddy.status = Buddy.ST_AWAY;			
		} else if ((icqBuddy.onlineInfo.userStatus & ICQConstants.STATUS_NA)>0){
			buddy.status = Buddy.ST_NA;			
		} else if ((icqBuddy.onlineInfo.userStatus & ICQConstants.STATUS_OCCUPIED)>0){
			buddy.status = Buddy.ST_BUSY;			
		} else if ((icqBuddy.onlineInfo.userStatus & ICQConstants.STATUS_DND)>0){
			buddy.status = Buddy.ST_DND;			
		} else if ((icqBuddy.onlineInfo.userStatus & ICQConstants.STATUS_FREE4CHAT)>0){
			buddy.status = Buddy.ST_FREE4CHAT;	
		} else if ((icqBuddy.onlineInfo.userStatus & ICQConstants.STATUS_INVISIBLE)>0){
			buddy.status = Buddy.ST_INVISIBLE;	
		} else if ((icqBuddy.onlineInfo.userStatus == ICQConstants.STATUS_OFFLINE)){
			buddy.status = Buddy.ST_OFFLINE;			
		} else {
			buddy.status = Buddy.ST_ONLINE;			
		}
		boolean xstatusFound = false;
		if(icqBuddy.onlineInfo.capabilities!=null){
			for (int j=icqBuddy.onlineInfo.capabilities.size()-1; j>-1; j--){
				String cap = icqBuddy.onlineInfo.capabilities.get(j);
				
				if(!xstatusFound){
					for (int i=0; i<ICQConstants.XSTATUS_CLSIDS.length; i++){
						String xClsid = ProtocolUtils.getHexString(ICQConstants.XSTATUS_CLSIDS[i]);
						if (xClsid.equalsIgnoreCase(cap)){
							buddy.xstatus = (byte) i;
							xstatusFound = true;
							break;
						}					
					}
				}
				
				if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_ANGRY))){
					buddy.status = Buddy.ST_ANGRY;
					break;
				}
				if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_FREE4CHAT))){
					buddy.status = Buddy.ST_FREE4CHAT;
					break;
				}
				if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_DEPRESSION))){
					buddy.status = Buddy.ST_DEPRESS;
					break;
				}
				if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_HOME))){
					buddy.status = Buddy.ST_HOME;
					break;
				}
				if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_LUNCH))){
					buddy.status = Buddy.ST_DINNER;
					break;
				}
				if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_WORK))){
					buddy.status = Buddy.ST_WORK;
					break;
				}
			}
		}
		return buddy;
	}
	
	public static final BuddyGroup ICQBuddyGroup2BuddyGroup(ICQBuddyGroup icqGroup, String ownerUid, byte serviceId){
		BuddyGroup group = new BuddyGroup(icqGroup.groupId, ownerUid, serviceId);
		group.name = icqGroup.name;
		group.buddyList = icqGroup.buddies;
		
		return group;
	}

	public static final List<Buddy> ICQBuddyList2Buddylist(ICQService service, List<ICQBuddy> buddyList, String ownerUid, byte serviceId){
		List<Buddy> buddies = new ArrayList<Buddy>();
		for (ICQBuddy icqBuddy:buddyList){
			buddies.add(ICQBuddy2Buddy(service, icqBuddy, ownerUid, serviceId));
		}
		return buddies;
	}
	
	public static final List<ICQBuddy> buddyList2ICQBuddyList(List<Buddy> buddies){
		if (buddies == null){
			return null;
		}
		List<ICQBuddy> icqBuddies = new ArrayList<ICQBuddy>(buddies.size());
		for (Buddy buddy:buddies){
			icqBuddies.add(buddy2ICQBuddy(buddy));
		}
		return icqBuddies;
	}
	
	public static final List<ICQBuddyGroup> buddyGroupList2ICQBuddyGroupList(List<BuddyGroup> groups){
		if(groups == null){
			return null;
		}
		List<ICQBuddyGroup> icqGroups = new ArrayList<ICQBuddyGroup>(groups.size());
		for (BuddyGroup group: groups){
			icqGroups.add(buddyGroup2ICQBuddyGroup(group));
		}
		return icqGroups;
	}
	
	public static final List<BuddyGroup> ICQBuddyGroupList2BuddyGroupList(List<ICQBuddyGroup> groupList, String ownerId, byte serviceId){
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>();
		for (ICQBuddyGroup icqGroup:groupList){
			groups.add(ICQEntityAdapter.ICQBuddyGroup2BuddyGroup(icqGroup, ownerId, serviceId));
		}
		return groups;
	}
	
	public static final ICBMMessage textMessage2ICBMMessage(TextMessage txtMessage){
		if (txtMessage == null) return null;
		ICBMMessage msg = new ICBMMessage();
		msg.text = txtMessage.text;
		msg.receiverId = txtMessage.to;
		msg.messageType = ICQConstants.MTYPE_PLAIN;
		msg.messageId = ProtocolUtils.long2ByteBE(txtMessage.messageId);
		return msg;
	}
	
	public static final TextMessage icbmMessage2TextMessage(ICBMMessage msg, byte serviceId){
		if (msg == null) return null;
		TextMessage txtMsg = new TextMessage(msg.senderId);
		txtMsg.text = msg.text;
		if (msg.receivingTime!=null){
			txtMsg.time = msg.receivingTime;
		} else {
			txtMsg.time = new Date();
		}
		txtMsg.serviceId = serviceId;
		txtMsg.messageId = ProtocolUtils.bytes2LongBE(msg.messageId, 0);
		return txtMsg;
	}
	
	public static final OnlineInfo icqOnlineInfo2OnlineInfo(ICQOnlineInfo in){
		if (in==null)return null;
		
		OnlineInfo out = new OnlineInfo();
		//out.capabilities = in.capabilities;
		out.createTime = in.createTime;
		out.extIP = in.extIP;
		out.idleTime = in.idleTime;
		out.memberSinceTime = in.memberSinceTime;
		out.name = in.name;
		out.onlineTime = in.onlineTime;
		out.signonTime = in.signonTime;
		out.typingNotification = in.typingNotification;
		out.protocolUid = in.uin;
		out.xstatusName = in.personalText;
		out.xstatusDescription = in.extendedStatus;
		
		if (in.iconData!=null && in.iconData.iconId ==1 && in.iconData.flags==1){
			out.iconHash = Base64.encodeBytes(in.iconData.hash);
		}
		
		if ((in.userStatus & ICQConstants.STATUS_AWAY)>0){
			out.userStatus = Buddy.ST_AWAY;			
		} else if ((in.userStatus & ICQConstants.STATUS_NA)>0){
			out.userStatus = Buddy.ST_NA;			
		} else if ((in.userStatus & ICQConstants.STATUS_OCCUPIED)>0){
			out.userStatus = Buddy.ST_BUSY;			
		} else if ((in.userStatus & ICQConstants.STATUS_DND)>0){
			out.userStatus = Buddy.ST_DND;			
		} else if ((in.userStatus & ICQConstants.STATUS_INVISIBLE)>0){
			out.userStatus = Buddy.ST_INVISIBLE;	
		} else if ((in.userStatus & ICQConstants.STATUS_FREE4CHAT)>0){
			out.userStatus = Buddy.ST_FREE4CHAT;
		} else if ((in.userStatus == ICQConstants.STATUS_OFFLINE)){
			out.userStatus = Buddy.ST_OFFLINE;			
		} else {
			out.userStatus = Buddy.ST_ONLINE;			
		}
		
		boolean xstatusFound = false;
		boolean statusFound = false;
		String canFileShare = ProtocolUtils.getHexString(ICQConstants.CLSID_AIM_FILESEND);
		
		if(in.capabilities!=null){
			for (int j=in.capabilities.size()-1; j>-1; j--){
				String cap = in.capabilities.get(j);
				
				if (cap.equals(canFileShare)){
					out.canFileShare = true;
					continue;
				}
				
				if (statusFound && xstatusFound){
					continue;
				}
				
				if(!xstatusFound){
					for (int i=0; i<ICQConstants.XSTATUS_CLSIDS.length; i++){
						String xClsid = ProtocolUtils.getHexString(ICQConstants.XSTATUS_CLSIDS[i]);
						if (xClsid.equalsIgnoreCase(cap)){
							out.xstatus = (byte) i;
							xstatusFound = true;
							break;
						}
					}
				}
				
				if (!statusFound){
					if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_ANGRY))){
						out.userStatus = Buddy.ST_ANGRY;
						statusFound = true;
					}
					if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_FREE4CHAT))){
						out.userStatus = Buddy.ST_FREE4CHAT;
						statusFound = true;
					}
					if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_DEPRESSION))){
						out.userStatus = Buddy.ST_DEPRESS;
						statusFound = true;
					}
					if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_HOME))){
						out.userStatus = Buddy.ST_HOME;
						statusFound = true;
					}
					if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_LUNCH))){
						out.userStatus = Buddy.ST_DINNER;
						statusFound = true;
					}
					if (cap.equals(ProtocolUtils.getHexString(ICQConstants.CLSID_STATUS_WORK))){
						out.userStatus = Buddy.ST_WORK;
						statusFound = true;
					}
				}
			}
		}
		
		out.visibility = in.visibility;
		
		return out;
	}
	
	public static final int userStatus2ICQUserStatus(Byte status){
		switch(status){
		case Buddy.ST_AWAY:
			return ICQConstants.STATUS_AWAY;
		case Buddy.ST_BUSY:
			return ICQConstants.STATUS_OCCUPIED;
		case Buddy.ST_DND:
			return ICQConstants.STATUS_DND;
		case Buddy.ST_INVISIBLE:
			return ICQConstants.STATUS_INVISIBLE;
		case Buddy.ST_NA:
			return ICQConstants.STATUS_NA;
		case Buddy.ST_ONLINE:
			return ICQConstants.STATUS_ONLINE;
		/*case Buddy.ST_FREE4CHAT:
			return ICQConstants.STATUS_FREE4CHAT;*/
		default:
			return -1;		
		}
	}
	
	public static final byte[] userQipStatus2ICQQipStatus(Byte status) {
		switch(status){
		case Buddy.ST_FREE4CHAT:
			return ICQConstants.CLSID_STATUS_FREE4CHAT;
		case Buddy.ST_ANGRY:
			return ICQConstants.CLSID_STATUS_ANGRY;
		case Buddy.ST_DEPRESS:
			return ICQConstants.CLSID_STATUS_DEPRESSION;
		case Buddy.ST_DINNER:
			return ICQConstants.CLSID_STATUS_LUNCH;
		case Buddy.ST_HOME:
			return ICQConstants.CLSID_STATUS_HOME;
		case Buddy.ST_WORK:
			return ICQConstants.CLSID_STATUS_WORK;
		default:
			return null;
		}
	}
	
	public static final PersonalInfo icqPersonalInfo2PersonalInfo(ICQPersonalInfo icqInfo, Context context){
		PersonalInfo info = new PersonalInfo();
		info.protocolUid = icqInfo.uin;
		
		//ICQ is 1(female), 2(male)
		byte gender = (byte) (icqInfo.gender == 1 ? 0 : 1);
		
		Bundle bundle = new Bundle();
		bundle.putString(PersonalInfo.INFO_EMAIL, icqInfo.email);
		bundle.putString(PersonalInfo.INFO_FIRST_NAME, icqInfo.firstName);
		bundle.putString(PersonalInfo.INFO_LAST_NAME, icqInfo.lastName);
		bundle.putString(PersonalInfo.INFO_NICK, icqInfo.nickname);
		bundle.putByte(PersonalInfo.INFO_GENDER, gender);
		bundle.putShort(PersonalInfo.INFO_AGE, icqInfo.age);
		bundle.putShort(PersonalInfo.INFO_STATUS, icqInfo.status);
		bundle.putByte(PersonalInfo.INFO_REQUIRES_AUTH, icqInfo.authRequired);
		
		Set<String> names = icqInfo.params.keySet();
		for (String name: names){
			if (name.indexOf("ountry") > -1){
				String[] countryNames = context.getResources().getStringArray(R.array.icq_country_names);
				int[] countryCodes = context.getResources().getIntArray(R.array.icq_country_codes);
				
				try {
					int code = (Integer) icqInfo.params.get(name);
					for (int i=0; i<countryCodes.length; i++){
						if (countryCodes[i] == code){
							bundle.putString(name, countryNames[i]);
							break;
						}
					}
				} catch (Exception e) {		
					e.printStackTrace();
				}
			} else if (name.indexOf("ccupation") > -1){
				String[] occuNames = context.getResources().getStringArray(R.array.icq_occupation_names);
				int[] occuCodes = context.getResources().getIntArray(R.array.icq_occupation_codes);
				
				try {
					int code = (Integer) icqInfo.params.get(name);
					for (int i=0; i<occuCodes.length; i++){
						if (occuCodes[i] == code){
							bundle.putString(name, occuNames[i]);
							break;
						}
					}
				} catch (Exception e) {		
					e.printStackTrace();
				}
			} else if (name.indexOf("anguage") > -1){
				String[] langNames = context.getResources().getStringArray(R.array.icq_language_names);
				int[] langCodes = context.getResources().getIntArray(R.array.icq_language_codes);
				
				try {
					int code = (Integer) icqInfo.params.get(name);
					for (int i=0; i<langCodes.length; i++){
						if (langCodes[i] == code){
							bundle.putString(name, langNames[i]);
							break;
						}
					}
				} catch (Exception e) {		
					e.printStackTrace();
				}
			} else if (name.indexOf("GMT") > -1){
				String[] gmtNames = context.getResources().getStringArray(R.array.icq_gmt_names);
				int[] gmtCodes = context.getResources().getIntArray(R.array.icq_gmt_codes);
				
				try {
					int code = (Integer) icqInfo.params.get(name);
					for (int i=0; i<gmtCodes.length; i++){
						if (gmtCodes[i] == code){
							bundle.putString(name, gmtNames[i]);
							break;
						}
					}
				} catch (Exception e) {			
					e.printStackTrace();
				}
			} else if (name.indexOf("Family status") > -1){
				String[] maritalNames = context.getResources().getStringArray(R.array.icq_marital_names);
				int[] maritalCodes = context.getResources().getIntArray(R.array.icq_marital_codes);
				
				try {
					int code = (Integer) icqInfo.params.get(name);
					for (int i=0; i<maritalCodes.length; i++){
						if (maritalCodes[i] == code){
							bundle.putString(name, maritalNames[i]);
							break;
						}
					}
				} catch (Exception e) {		
					e.printStackTrace();
				}
			} else if (name.indexOf("past ") > -1){
				String past = name.split("past ")[1];
				try {
					int pastId = Integer.parseInt(past);
					String[] pastNames = context.getResources().getStringArray(R.array.icq_past_names);
					int[] pastCodes = context.getResources().getIntArray(R.array.icq_past_codes);
					
					for (int i=0; i<pastCodes.length; i++){
						if (pastCodes[i] == pastId){
							bundle.putString(pastNames[i], (String) icqInfo.params.get(name));
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
			} else if (name.indexOf("affiliation ") > -1){
				String aff = name.split("affiliation ")[1];
				try {
					int affId = Integer.parseInt(aff);
					String[] affNames = context.getResources().getStringArray(R.array.icq_affiliation_names);
					int[] affCodes = context.getResources().getIntArray(R.array.icq_affiliation_codes);
					
					for (int i=0; i<affCodes.length; i++){
						if (affCodes[i] == affId){
							bundle.putString(affNames[i], (String) icqInfo.params.get(name));
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
			} else {
				bundle.putString(name, (String) icqInfo.params.get(name));
			}
		}
		
		Log.d("", bundle.toString());
		
		info.properties = bundle;
		return info;
	}
	
	public static final List<PersonalInfo> icqPersonalInfos2PersonalInfos(List<ICQPersonalInfo> icqinfos, Context context){
		if (icqinfos == null){
			return null;
		}
		List<PersonalInfo> infos = new ArrayList<PersonalInfo>(icqinfos.size());
		for (ICQPersonalInfo info:icqinfos){
			infos.add(icqPersonalInfo2PersonalInfo(info, context));
		}
		return infos;
	}

	public static final FileMessage icbmMessage2FileMessage(ICBMMessage icbmMessage, byte serviceId) {
		if (icbmMessage == null) return null;
		
		FileMessage message = new FileMessage(icbmMessage.senderId);
		message.messageId = ProtocolUtils.bytes2LongBE(icbmMessage.messageId, 0);
		message.serviceId = serviceId;
		message.files.addAll(icqFileInfoList2FileInfoList(icbmMessage.files));
		message.time = icbmMessage.receivingTime;
		return message;
	}

	private static List<FileInfo> icqFileInfoList2FileInfoList(List<ICQFileInfo> files) {
		if (files == null){
			return null;
		}
		
		List<FileInfo> infos = new ArrayList<FileInfo>(files.size());
		for (ICQFileInfo file: files){
			FileInfo info = new FileInfo();
			info.filename = file.filename;
			info.size = file.size;
			
			infos.add(info);
		}
		return infos;
	}
}
