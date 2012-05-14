package ua.snuk182.asia.core.dataentity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Multi-user chat room entity. Recognized as {@link Buddy}.
 * 
 * @author SergiyP
 *
 */
public class MultiChatRoom extends Buddy{

	public void readFromParcel(Parcel in){
		super.readFromParcel(in);
	}
	
	public MultiChatRoom(String protocolUid, AccountView account){
		super(protocolUid, account);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
	}
	
	private MultiChatRoom(Parcel in){
		readFromParcel(in);
	}
	
	public MultiChatRoom(String protocolUid, String ownerUid, String serviceName, byte serviceId) {
		super(protocolUid, ownerUid, serviceName, serviceId);
	}

	public static final Parcelable.Creator<MultiChatRoom> CREATOR = new Parcelable.Creator<MultiChatRoom>(){

		@Override
		public MultiChatRoom createFromParcel(Parcel source) {
			return new MultiChatRoom(source);
		}

		@Override
		public MultiChatRoom[] newArray(int size) {
			return new MultiChatRoom[size];
		}
		
	};
	
	@Override
	public String toString(){
		return protocolUid+"\n"+name+"\n\n";
	}
}
