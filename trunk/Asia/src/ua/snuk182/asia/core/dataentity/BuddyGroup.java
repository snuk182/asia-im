package ua.snuk182.asia.core.dataentity;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class BuddyGroup implements Parcelable, Comparable<BuddyGroup> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9101729129950262311L;
	
	public int id;
	
	public byte serviceId;
	public String name;
	public String ownerUid;
	public boolean isCollapsed = false;
	
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
	
	@Override
	public String toString(){
		return name!=null?name:"";
	}

	@Override
	public int compareTo(BuddyGroup another) {
		return name.compareToIgnoreCase(another.name);
	}
}
