package ua.snuk182.asia.core.dataentity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Personal info entity
 * 
 * @author Sergiy Plygun
 *
 */
public class PersonalInfo implements Parcelable{
	
	/**
	 * Info owner's protocol UID, either buddy or account
	 */
	public String protocolUid;
	
	/**
	 * Info bundle.
	 */
	public Bundle properties;
	
	//A list of common info keys
	public static final String INFO_CHAT_DESCRIPTION = "Description";
	public static final String INFO_CHAT_OCCUPANTS = "Occupants";
	public static final String INFO_CHAT_SUBJECT = "Subject";
	
	public static final String INFO_NICK = "nick";	
	public static final String INFO_FIRST_NAME = "first-name";
	public static final String INFO_LAST_NAME = "last-name";
	public static final String INFO_EMAIL = "email";
	public static final String INFO_GENDER = "gender";
	public static final String INFO_AGE = "age";
	public static final String INFO_STATUS = "status";
	public static final String INFO_REQUIRES_AUTH = "auth-required";

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(protocolUid);
		dest.writeBundle(properties);
	}
	
	private PersonalInfo(Parcel in){
		readFromParcel(in);
	}

	public PersonalInfo() {
		properties = new Bundle();
	}

	private void readFromParcel(Parcel in) {
		protocolUid = in.readString();
		properties = in.readBundle();		
	}

	public static final Parcelable.Creator<PersonalInfo> CREATOR = new Parcelable.Creator<PersonalInfo>(){

		@Override
		public PersonalInfo createFromParcel(Parcel source) {
			return new PersonalInfo(source);
		}

		@Override
		public PersonalInfo[] newArray(int size) {
			return new PersonalInfo[size];
		}
		
	};
}
