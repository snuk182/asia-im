package ua.snuk182.asia.core.dataentity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Service message entity.
 * 
 * @author SergiyP
 *
 */
public class ServiceMessage extends Message implements Parcelable {
	
	public static final String TYPE_AUTHREQUEST = "Type.Authrequest";
	public static final String TYPE_FILE = "Type.File";
	public static final String TYPE_CHAT_MESSAGE = "Type.Chat";
	
	/**
	 * Service message type
	 */
	public String type;
	
	public ServiceMessage(Parcel arg0) {
		super(arg0);
		readFromParcel(arg0);
	}
	
	public ServiceMessage(String from){
		super(from);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(type);
	}
	
	public void readFromParcel(Parcel in){
		super.readFromParcel(in);
		type = in.readString();
	}

	public static final Parcelable.Creator<ServiceMessage> CREATOR = new Parcelable.Creator<ServiceMessage>(){

		@Override
		public ServiceMessage createFromParcel(Parcel arg0) {
			return new ServiceMessage(arg0);
		}

		@Override
		public ServiceMessage[] newArray(int size) {
			return new ServiceMessage[size];
		}
		
	};
}
