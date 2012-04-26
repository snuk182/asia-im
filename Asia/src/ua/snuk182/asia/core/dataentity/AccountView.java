package ua.snuk182.asia.core.dataentity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ua.snuk182.asia.services.RuntimeService;
import ua.snuk182.asia.services.api.AccountService;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Account view part, for using in views.
 * 
 * @author SergiyP
 *
 */
public class AccountView implements Parcelable {
	
	public static final byte VIS_TO_PERMITTED = 3;
	public static final byte VIS_EXCEPT_DENIED = 4;
	public static final byte VIS_TO_BUDDIES = 5;
	public static final byte VIS_TO_ALL = 1;
	public static final byte VIS_INVISIBLE = 2;
	
	/**
	 * "Account enabled" flag
	 */
	public boolean isEnabled = true;
	
	/**
	 * Service ID (0 to 255)
	 */
	public byte serviceId = -1;
	
	/**
	 * Protocol name (ICQ, XMPP etc)
	 */
	public String protocolName;
	
	/**
	 * Protocol-specific identifier (444555666 for ICQ, user@server.com for XMPP and so on)
	 */
	public String protocolUid;
	
	/**
	 * User name, human friendly
	 */
	public String ownName;
	
	/**
	 * Account availability status.
	 */
	public byte status = Buddy.ST_ONLINE;
	
	/**
	 * Account extended status.
	 */
	public byte xStatus = -1;
	
	/**
	 * Account extended status name
	 */
	public String xStatusName = "";
	
	/**
	 * Account extended status text.
	 */
	public String xStatusText = "";
	
	/**
	 * Account visibility.
	 */
	public byte visibility = VIS_TO_BUDDIES;
	
	/**
	 * Buddy list.
	 */
	private List<Buddy> buddyList = Collections.synchronizedList(new ArrayList<Buddy>());
	
	/**
	 * Buddy group list.
	 */
	private List<BuddyGroup> buddyGroupList = Collections.synchronizedList(new ArrayList<BuddyGroup>());
	
	/**
	 * Buddies with unread messages temporary storage. Buddy uid - number of unread messages. Non-serializable.
	 */
	private Map<String, Byte> unreadsMap = new HashMap<String, Byte>();
	
	/**
	 * Undeletable buddies temporary storage. Non-serializable.
	 */
	private List<Buddy> undeletable;
	
	/**
	 * Account options storage.
	 */
	public Bundle options = new Bundle();
	
	/**
	 * Account connection state.
	 */
	private short connectionState = AccountService.STATE_DISCONNECTED;
	
	/**
	 * Account last update time.
	 */
	public long lastUpdateTime = new Date().getTime();

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(serviceId);
		//dest.writeString(protocolId);
		dest.writeString(protocolName);
		dest.writeString(protocolUid);
		dest.writeString(ownName);
		dest.writeByte(status);
		dest.writeByte(xStatus);
		dest.writeByte(visibility);
		dest.writeString(xStatusName);
		dest.writeString(xStatusText);
		dest.writeList(buddyList);
		dest.writeList(buddyGroupList);
		dest.writeBundle(options);
		dest.writeInt(connectionState);
		dest.writeLong(lastUpdateTime);
		dest.writeByte((byte) (isEnabled ? 1: 0));
	}
	
	@SuppressWarnings("unchecked")
	public void readFromParcel(Parcel in){
		serviceId = in.readByte();
		//protocolId = in.readString();
		protocolName = in.readString();
		protocolUid = in.readString();
		ownName = in.readString();
		status = in.readByte();
		xStatus = in.readByte();
		visibility = in.readByte();
		xStatusName = in.readString();
		xStatusText = in.readString();
		buddyList.clear();
		buddyGroupList.clear();
		buddyList.addAll(in.readArrayList(Buddy.class.getClassLoader()));
		buddyGroupList.addAll(in.readArrayList(BuddyGroup.class.getClassLoader()));
		options = in.readBundle();
		connectionState = (short) in.readInt();
		lastUpdateTime = in.readLong();
		isEnabled = in.readByte() != 0;
	}
	
	public static final Parcelable.Creator<AccountView> CREATOR = new Parcelable.Creator<AccountView>(){

		@Override
		public AccountView createFromParcel(Parcel source) {
			return new AccountView(source);
		}

		@Override
		public AccountView[] newArray(int size) {
			return new AccountView[size];
		}
		
	};
	
	private AccountView(Parcel in){
		readFromParcel(in);
	}

	public AccountView(String protocolUid, String protocolName) {
		this.protocolUid = protocolUid.trim();
		this.protocolName = protocolName.trim();
	}	

	public Buddy getBuddyByProtocolUid(String uid){
		synchronized (buddyList) {
			for (Buddy buddy : buddyList) {
				if (uid.equals(buddy.protocolUid)) {
					return buddy;
				}
			}
		}
		return null;
	}
	
	public BuddyGroup getBuddyGroupByGroupId(int id){
		synchronized (buddyGroupList) {
			for (BuddyGroup group : buddyGroupList) {
				if (group.id == id) {
					return group;
				}
			}
		}
		return null;
	}
	
	public boolean hasUnreadMessages(){
		synchronized (buddyList) {
			for (Buddy bu : buddyList) {
				if (bu.unread > 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Buddy editBuddy(Buddy newBuddy, boolean updateStatus){
		synchronized (buddyList) {
			for (Buddy buddy : buddyList) {
				if (newBuddy.protocolUid.equals(buddy.protocolUid)) {
					buddy.name = newBuddy.name;
					//buddy.capabilities = newBuddy.capabilities;
					buddy.canFileShare = newBuddy.canFileShare;
					if (updateStatus) {
						buddy.externalIP = newBuddy.externalIP;
						buddy.onlineTime = newBuddy.onlineTime;
						buddy.signonTime = newBuddy.signonTime;

						buddy.status = newBuddy.status;
						buddy.xstatus = newBuddy.xstatus;
						buddy.xstatusDescription = newBuddy.xstatusDescription;
						buddy.xstatusName = newBuddy.xstatusName;

						if (newBuddy.status != Buddy.ST_OFFLINE) {
							buddy.visibility = Buddy.VIS_REGULAR;
						} else {
							buddy.visibility = newBuddy.visibility;
						}
					}
					if (buddy.groupId != newBuddy.groupId) {
						buddy.groupId = newBuddy.groupId;
					}
					return buddy;
				}
			}
		}
		return null;
	}
	
	/**
	 * example "123456789 ICQ"
	 * @return protocolUid+" "+protocolName
	 */
	public String getAccountId(){
		return protocolUid+" "+protocolName;
	}

	public void disconnected() {
		synchronized (buddyList) {
			for (Buddy buddy : buddyList) {
				buddy.status = Buddy.ST_OFFLINE;
				buddy.xstatus = (byte) -1;
			}
		}
		connectionState = AccountService.STATE_DISCONNECTED;
	}
	
	public void updateTime(){
		this.lastUpdateTime = new Date().getTime();
	}

	public void removeAllBuddies(boolean keepNotInList) {
		undeletable = new LinkedList<Buddy>();
		synchronized (buddyList) {
			for (int i = buddyList.size() - 1; i >= 0; i--) {
				Buddy bu = buddyList.get(i);
				if (bu.unread > 0){
					unreadsMap.put(bu.protocolUid, bu.unread);
				}
				if ((bu.groupId == AccountService.NOT_IN_LIST_GROUP_ID && keepNotInList) || bu.visibility == Buddy.VIS_GROUPCHAT) {
					undeletable.add(bu);
				}						
			}
			
			buddyList.clear();
		}
	}

	public void removeBuddyByUid(Buddy buddy) {
		synchronized (buddyList) {
			for (int i = 0; i < buddyList.size(); i++) {
				if (buddyList.get(i).protocolUid.equals(buddy.protocolUid)) {
					buddyList.remove(i);
					break;
				}
			}
			for (BuddyGroup group : buddyGroupList) {
				if (group.id == buddy.groupId) {
					for (int i = group.buddyList.size() - 1; i >= 0; i--) {
						if (group.buddyList.get(i) == buddy.id) {
							group.buddyList.remove(i);
							//break;
						}
					}
					//break;
				}
			}
		}
	}
	
	public Buddy getBuddyByBuddyId(int id) {
		for (Buddy buddy:buddyList){
			if (buddy.id == id){
				return buddy;
			}
		}		
		return null;
	}

	public void addBuddyToList(Buddy buddy) {
		removeBuddyByUid(buddy);
		buddyList.add(buddy);
		if (buddy.groupId!=AccountService.NOT_IN_LIST_GROUP_ID){
			for (BuddyGroup group:buddyGroupList){
				if (group.id == buddy.groupId){
					group.buddyList.add(buddy.id);
					break;
				}
			}
		}
	}

	public void editGroup(BuddyGroup newGroup) {
		for (BuddyGroup group:buddyGroupList){
			if (group.id == newGroup.id){
				group.name = newGroup.name;
				break;
			}
		}		
	}

	public List<Buddy> getBuddiesForGroup(BuddyGroup group) {
		List<Buddy> res = new ArrayList<Buddy>(group.buddyList.size());
		synchronized (buddyList) {
			for (Buddy buddy : buddyList) {
				for (int i : group.buddyList) {
					if (buddy.id == i && buddy.groupId == group.id) {
						res.add(buddy);
						break;
					}
				}
			}
		}
		return res;
	}

	public void removeGroup(BuddyGroup group) {
		for (int i=0; i<buddyGroupList.size(); i++){
			if (buddyGroupList.get(i).id == group.id){
				buddyGroupList.remove(i);
				break;
			}
		}		
	}

	public short getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(short connectionState) {
		this.connectionState = connectionState;
	}
	
	public void merge(AccountView origin){
		if (origin == null || origin == this){
			return;
		}
		serviceId = origin.serviceId;
		protocolName = origin.protocolName;
		protocolUid = origin.protocolUid;
		ownName = origin.ownName;
		status = origin.status;
		xStatus = origin.xStatus;
		xStatusName = origin.xStatusName;
		xStatusText = origin.xStatusText;
		visibility = origin.visibility;
		
		synchronized (buddyList) {
			buddyList.clear();
			buddyList.addAll(origin.buddyList);
		}
		synchronized (buddyGroupList) {
			buddyGroupList.clear();
			buddyGroupList.addAll(origin.buddyGroupList);
		}
		options = origin.options;
		connectionState = origin.connectionState;
		lastUpdateTime = origin.lastUpdateTime;
	}
	
	public AccountView(){
		
	}

	public String getFilename() {
		return getAccountId()+" "+protocolUid;
	}
	
	public String getSafeName(){
		return ownName != null ? ownName : protocolUid;
	}
	
	public Bitmap getIcon(Context context){
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(getFilename()+Buddy.BUDDYICON_FILEEXT);
		} catch (FileNotFoundException e) {
		}
		
		if (fis == null) return null;
		
		return BitmapFactory.decodeStream(fis);
	}

	public List<Buddy> getBuddyList() {
		synchronized (buddyList) {
			return buddyList;
		}
	}

	public void setBuddyList(List<Buddy> buddyList, RuntimeService runtimeService, boolean getIcons) {
		synchronized (buddyList) {
			this.buddyList.addAll(buddyList);
			if (undeletable != null){
				this.buddyList.addAll(undeletable);
			}
		}
		for (Iterator<String> unreads = unreadsMap.keySet().iterator(); unreads.hasNext();){
			String unreadKey = unreads.next();
			for (Buddy bu : buddyList){
				if (bu.protocolUid.equals(unreadKey)){
					bu.unread = unreadsMap.get(unreadKey);
				}
			}
		}
		undeletable = null;
		unreadsMap.clear();
		if (getIcons){
			for (Buddy b:buddyList){
				//if (b.status != Buddy.ST_OFFLINE){
					runtimeService.requestIcon(b.serviceId, b.protocolUid);
				//}
			}
		}
	}

	public List<BuddyGroup> getBuddyGroupList() {
		synchronized (buddyGroupList) {
			return buddyGroupList;
		}
	}
	
	public void setBuddyGroupList(List<BuddyGroup> buddyGroupList) {
		synchronized (buddyGroupList) {
			List<BuddyGroup> old = new ArrayList<BuddyGroup>();
			old.addAll(this.buddyGroupList);
			this.buddyGroupList.clear();
			this.buddyGroupList.addAll(buddyGroupList);
			for (BuddyGroup bg : this.buddyGroupList){
				boolean done = false;
				for (BuddyGroup obg : old){
					if (bg.id == obg.id){
						bg.isCollapsed = obg.isCollapsed;
						done = true;
						break;
					}
				}
				if (done){
					continue;
				}
			}
		}
	}
}
