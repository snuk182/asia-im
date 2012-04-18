package ua.snuk182.asia.core.dataentity;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


public class ContactListViewGroup implements Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5982121287314450208L;

	public String name;
	public String id;
	public List<Buddy> buddyList;

	
	@Override
	public String toString(){
		if (name == null) return null;
		return name+": "+buddyList.size()+" items";
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(id);
		arg0.writeString(name);
		arg0.writeList(buddyList);		
	}
	
	private ContactListViewGroup(Parcel in){
		readFromParcel(in);
	}
	
	public ContactListViewGroup(List<Buddy> buddies){
		this.buddyList = buddies;
	}
	
	@SuppressWarnings("unchecked")
	private void readFromParcel(Parcel in) {
		id = in.readString();
		name = in.readString();
		buddyList = in.readArrayList(Buddy.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<ContactListViewGroup> CREATOR = new Parcelable.Creator<ContactListViewGroup>(){

		@Override
		public ContactListViewGroup createFromParcel(Parcel arg0) {
			return new ContactListViewGroup(arg0);
		}

		@Override
		public ContactListViewGroup[] newArray(int arg0) {
			return new ContactListViewGroup[arg0];
		}		
	};
}
