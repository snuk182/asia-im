package ua.snuk182.asia.core.dataentity;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Online info entity. Carries buddy/account online info update.
 * 
 * @author Sergiy Plygun
 *
 */
public class OnlineInfo implements Parcelable {
	
	/**
	 * Status
	 * 
	 * @see Buddy#status
	 */
	public Byte userStatus = Buddy.ST_OFFLINE;
	
	/**
	 * Extended status
	 * 
	 * @see Buddy#xstatus
	 */
	public byte xstatus = -1;
	
	/**
	 * Extended status title.
	 * 
	 * @see Buddy#xstatusName
	 */
	public String xstatusName = null;
	
	/**
	 * Extended status description
	 * 
	 * @see Buddy#xstatusDescription
	 */
	public String xstatusDescription = null;
	
	/**
	 * Buddy's IP address
	 * 
	 * @see Buddy#externalIP
	 */
	public String extIP;
	
	/**
	 * Buddy's online time in seconds
	 */
	public int onlineTime;
	
	/**
	 * Buddy's signon date
	 */
	public Date signonTime;
	
	/**
	 * Buddy's membership start date
	 */
	public Date memberSinceTime;
	
	/**
	 * Visibility
	 * 
	 * @see Buddy#visibility
	 */
	public byte visibility;
	
	/**
	 * Buddy's typing notification capability
	 */
	public boolean typingNotification = true;
	//public List<String> capabilities;
	
	/**
	 * Buddy's file sharing capability
	 */
	public boolean canFileShare = false;
	
	/**
	 * Buddy's idle time
	 */
	public int idleTime;
	
	/**
	 * Buddy's account create time
	 */
	public Date createTime;
	
	/**
	 * Buddy's name/nick
	 */
	public String name;
	
	/**
	 * Buddy's protocol UID
	 */
	public String protocolUid;
	
	/**
	 * Buddy's userpic hash
	 */
	public String iconHash;
	public byte secureOptions = Buddy.SECURE_NOSUPPORT;
	
	public static final Parcelable.Creator<OnlineInfo> CREATOR = new Parcelable.Creator<OnlineInfo>(){

		@Override
		public OnlineInfo createFromParcel(Parcel source) {
			return new OnlineInfo(source);
		}

		@Override
		public OnlineInfo[] newArray(int size) {
			return new OnlineInfo[size];
		}
		
	};
	
	private OnlineInfo(Parcel source) {
		readFromParcel(source);
	}

	public OnlineInfo() {
	}

	private void readFromParcel(Parcel in){
		userStatus = in.readByte();
		extIP = in.readString();
		onlineTime = in.readInt();
		signonTime = new Date(in.readLong());
		memberSinceTime = new Date(in.readLong());
		visibility = in.readByte();
		typingNotification = in.readByte()==1;
		//in.readStringList(capabilities);
		canFileShare = in.readByte() != 0;
		idleTime = in.readInt();
		createTime = new Date(in.readLong());
		name = in.readString();
		protocolUid = in.readString();
		iconHash = in.readString();
		secureOptions = in.readByte();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(userStatus);
		dest.writeString(extIP);
		dest.writeInt(onlineTime);
		dest.writeLong(signonTime.getTime());
		dest.writeLong(memberSinceTime.getTime());
		dest.writeByte(visibility);
		dest.writeByte((byte) (typingNotification?1:0));
		//dest.writeStringList(capabilities);
		dest.writeByte((byte) (canFileShare ? 1: 0));
		dest.writeInt(idleTime);
		dest.writeLong(createTime.getTime());
		dest.writeString(name);
		dest.writeString(protocolUid);
		dest.writeString(iconHash);
		dest.writeByte(secureOptions);
	}
}
