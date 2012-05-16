package ua.snuk182.asia.core.dataentity;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Buddy group entity.
 * 
 * @author Sergiy Plygun.
 *
 */
public class BuddyGroup implements Parcelable, Comparable<BuddyGroup> {
	
	/**
	 * Group internal id
	 */
	public int id;
	
	/**
	 * Holder account's service id
	 */
	public byte serviceId;
	
	/**
	 * Group name, human-readable
	 */
	public String name;
	
	/**
	 * Holder account's protocol UID
	 */
	public String ownerUid;
	
	/**
	 * Shows whether group should be collapsed on showing.
	 */
	public boolean isCollapsed = false;
	
	/**
	 * List of contained buddies' IDs
	 */
	public List<Integer> buddyList = new ArrayList<Integer>();

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeByte(serviceId);
		dest.writeString(name);
		dest.writeString(ownerUid);
		dest.writeByte((byte) (isCollapsed? 1: 0));
		dest.writeList(buddyList);		
	}
	
	@SuppressWarnings("unchecked")
	public void readFromParcel(Parcel in){
		id = in.readInt();
		serviceId = in.readByte();
		name = in.readString();
		ownerUid = in.readString();
		isCollapsed = in.readByte() != 0;
		buddyList = in.readArrayList(Integer.class.getClassLoader());
	}
	
	private BuddyGroup(Parcel in){
		readFromParcel(in);
	}
	
	public BuddyGroup(int id, String accountId, Byte serviceId) {
		this.id = id;
		this.ownerUid = accountId;
		this.serviceId = serviceId;
	}

	public static final Parcelable.Creator<BuddyGroup> CREATOR = new Parcelable.Creator<BuddyGroup>(){

		@Override
		public BuddyGroup createFromParcel(Parcel source) {
			return new BuddyGroup(source);
		}

		@Override
		public BuddyGroup[] newArray(int size) {
			return new BuddyGroup[size];
		}
		
	};
	
	/**
	 * Safe name getter. If no human-readable name found, the empty string is returned.
	 */
	@Override
	public String toString(){
		return name!=null?name:"";
	}

	@Override
	public int compareTo(BuddyGroup another) {
		return name.compareToIgnoreCase(another.name);
	}
}
