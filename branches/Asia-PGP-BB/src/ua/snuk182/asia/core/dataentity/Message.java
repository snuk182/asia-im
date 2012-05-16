package ua.snuk182.asia.core.dataentity;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Abstract message entity
 * 
 * @author SergiyP
 *
 */
public class Message implements Parcelable {
	
	/**
	 * Holder account's service id
	 */
	public byte serviceId;
	
	/**
	 * Message text
	 */
	public String text;
	
	/**
	 * Sender's protocol UID. 
	 */
	public String from;
	
	/**
	 * The time message is sent/received
	 */
	public Date time;
	
	/**
	 * Message ID
	 */
	public long messageId = 0;

	public Message(Parcel arg0) {
		//readFromParcel(arg0);
		time = new Date();
	}
	
	public Message(String from){
		this.from = from;
		time = new Date();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(serviceId);
		dest.writeString(text);
		dest.writeString(from);
		dest.writeLong(messageId);
		dest.writeLong(time.getTime());
	}
	
	protected void readFromParcel(Parcel in){
		serviceId = in.readByte();
		text = in.readString();
		from = in.readString();
		messageId = in.readLong();
		time = new Date(in.readLong());
	}

	public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>(){

		@Override
		public Message createFromParcel(Parcel arg0) {
			return new Message(arg0);
		}

		@Override
		public Message[] newArray(int size) {
			return new Message[size];
		}
		
	};
}
