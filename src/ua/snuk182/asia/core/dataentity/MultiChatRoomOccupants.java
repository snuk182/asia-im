package ua.snuk182.asia.core.dataentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class MultiChatRoomOccupants implements Parcelable {
	
	public final List<BuddyGroup> groups = Collections.synchronizedList(new ArrayList<BuddyGroup>());
	public final List<Buddy> buddies = Collections.synchronizedList(new ArrayList<Buddy>());
	public final byte serviceId;

	public MultiChatRoomOccupants(byte serviceId, List<BuddyGroup> groups, List<Buddy> buddies) {
		this.groups.addAll(groups);
		this.buddies.addAll(buddies);
		this.serviceId = serviceId;
	}
	
	public MultiChatRoomOccupants(Parcel source) {
		serviceId = source.readByte();
		readFromParcel(source);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(serviceId);
		dest.writeList(buddies);
		dest.writeList(groups);
	}
	
	@SuppressWarnings("unchecked")
	private void readFromParcel(Parcel in){
		buddies.addAll(in.readArrayList(Buddy.class.getClassLoader()));
		groups.addAll(in.readArrayList(BuddyGroup.class.getClassLoader()));
	}

	public static final Parcelable.Creator<MultiChatRoomOccupants> CREATOR = new Creator<MultiChatRoomOccupants>() {
		
		@Override
		public MultiChatRoomOccupants[] newArray(int size) {
			return new MultiChatRoomOccupants[size];
		}
		
		@Override
		public MultiChatRoomOccupants createFromParcel(Parcel source) {
			return new MultiChatRoomOccupants(source);
		}
	};
}
