package ua.snuk182.asia.core.dataentity;

import java.io.FileInputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ua.snuk182.asia.services.HistorySaver;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.view.conversations.ConversationsView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Buddy entity.
 * 
 * @author Sergiy Plygun
 *
 */
public class Buddy implements Parcelable, Comparable<Buddy> {

	//Status values
	public static final byte ST_OFFLINE = 0;
	public static final byte ST_ONLINE = 1;
	public static final byte ST_AWAY = 2;
	public static final byte ST_NA = 3;
	public static final byte ST_BUSY = 4;
	public static final byte ST_DND = 5;
	public static final byte ST_FREE4CHAT = 6;
	public static final byte ST_INVISIBLE = 7;
	public static final byte ST_HOME = 8;
	public static final byte ST_WORK = 9;
	public static final byte ST_DINNER = 10;
	public static final byte ST_DEPRESS = 11;
	public static final byte ST_ANGRY = 12;
	public static final byte ST_OTHER = 13;

	//Visibility values
	public static final byte VIS_PERMITTED = 1;
	public static final byte VIS_DENIED = 2;
	public static final byte VIS_IGNORED = 3;
	public static final byte VIS_REGULAR = 0;
	public static final byte VIS_NOT_AUTHORIZED = 4;
	public static final byte VIS_GROUPCHAT = 5;
	
	//Secure chat values
	public static final byte SECURE_NOSUPPORT = 0;
	public static final byte SECURE_SUPPORTS = 1;
	public static final byte SECURE_ENABLED = 2;
	public static final byte SECURE_CONNECTED = 3;
	
	/**
	 * Buddy internal ID
	 */
	public int id;
	
	/**
	 * Service name of buddy's account
	 */
	public String serviceName;
	
	/**
	 * Buddy name/nick
	 */
	public String name;
	
	/**
	 * Buddy protocol-specific identifier (444555666 for ICQ, user@server.com for XMPP and so on)
	 */
	public String protocolUid;
	
	/**
	 * Buddy's account protocol-specific identifier (444555666 for ICQ, user@server.com for XMPP and so on)
	 */
	public String ownerUid;
	
	/**
	 * Icon hash value (unusable for now, v0.8.x)
	 */
	public String iconHash;
	
	/**
	 * Buddy's account service ID
	 */
	public byte serviceId;
	
	/**
	 * Buddy's status (see ST_* fields for values)
	 */
	public byte status;
	
	/**
	 * Buddy's extended status. The different account types (ICQ, XMPP...) operate with different extended status sets. Value = -1 means no xstatus.
	 */
	public byte xstatus = -1;
	
	/**
	 * Extended status title
	 */
	public String xstatusName;
	
	/**
	 * Extended status text.
	 */
	public String xstatusDescription;
	
	/**
	 * Buddy's IP. Note that not all buddies make their IP visible, so this field can be empty.
	 */
	public String externalIP;
	
	/**
	 * Buddy's online time in seconds since last login.
	 */
	public int onlineTime;
	
	/**
	 * Buddy's signon time
	 */
	public Date signonTime;
	//public List<String> capabilities = new ArrayList<String>();
	
	/**
	 * Buddy's visibility (see VIS_* fields for values)
	 */
	public byte visibility;
	
	/**
	 * Buddy's unread messages counter
	 */
	public byte unread = 0;
	
	/**
	 * Buddy's group ID
	 */
	public int groupId = AccountService.NO_GROUP_ID;
	
	/**
	 * File sharing ability flag
	 */
	public boolean canFileShare = false;
	
	/**
	 * Buddy's client application ID. Depends on holder account type.
	 */
	public String clientId = null;
	
	public byte secureOptions = SECURE_NOSUPPORT;
	
	//non-serializable field
	public boolean waitsForInfo = false;
	
	public String authRequest = null;
	
	private HistorySaver historySaver;

	public static final String BUDDYICON_FILEEXT = ".ico";
	
	public String getName() {
		return name!=null?name:protocolUid;
	}
	
	@Override
	public String toString(){
		return getName()+" ("+protocolUid+")";
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private Buddy(Parcel in){
		readFromParcel(in);
	}
	
	public Buddy(String protocolUid, AccountView account) {
		this(protocolUid, account.protocolUid, account.protocolName, account.serviceId);
	}
	public Buddy(String protocolUid, String ownerUid, String serviceName, byte serviceId) {
		this.protocolUid = protocolUid;
		this.ownerUid = ownerUid;
		this.serviceName = serviceName;
		this.serviceId = serviceId;
	}

	public void readFromParcel(Parcel in) {
		id = in.readInt();
		serviceName = in.readString();
		name = in.readString();
		protocolUid = in.readString();
		ownerUid = in.readString();
		serviceId = in.readByte();
		status = in.readByte();
		xstatus = in.readByte();
		xstatusName = in.readString();
		xstatusDescription = in.readString();
		externalIP = in.readString();
		onlineTime = in.readInt();
		
		long sig = in.readLong();
		signonTime = sig>-1 ? new Date(sig) : null;
		visibility = in.readByte();
		unread = in.readByte();
		//in.readStringList(capabilities);
		canFileShare = in.readByte() != 0;
		groupId = in.readInt();
		iconHash = in.readString();
		clientId = in.readString();
		secureOptions = in.readByte();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(serviceName);
		dest.writeString(name);
		dest.writeString(protocolUid);
		dest.writeString(ownerUid);
		dest.writeByte(serviceId);
		dest.writeByte(status);
		dest.writeByte(xstatus);
		dest.writeString(xstatusName);
		dest.writeString(xstatusDescription);
		dest.writeString(externalIP);
		dest.writeInt(onlineTime);
		dest.writeLong(signonTime!=null ? signonTime.getTime() : -1);
		dest.writeByte(visibility);
		dest.writeByte(unread);
		//dest.writeStringList(capabilities);
		dest.writeByte((byte) (canFileShare? 1: 0));
		dest.writeInt(groupId);
		dest.writeString(iconHash);
		dest.writeString(clientId);
		dest.writeByte(secureOptions);
	}
	
	/**
	 * Obtain history saver for buddy
	 * 
	 * @return saver
	 */
	public HistorySaver getHistorySaver() {
		if (historySaver == null){
			historySaver = new HistorySaver(this);
		}
		return historySaver;
	}

	public static final Parcelable.Creator<Buddy> CREATOR = new Parcelable.Creator<Buddy>(){

		@Override
		public Buddy createFromParcel(Parcel source) {
			return new Buddy(source);
		}

		@Override
		public Buddy[] newArray(int size) {
			return new Buddy[size];
		}
		
	};

	/**
	 * Get history for buddy.
	 * 
	 * @param context application context to operate in
	 * @param getAll true if all history should be taken.
	 * @return a list of history messages. May be empty.
	 */
	public List<TextMessage> getLastHistory(Context context, boolean getAll) {
		unread = 0;		
		return getHistorySaver().getLastHistory(context, getAll);
	}
	
	/**
	 * Merge buddy with new data.
	 * 
	 * @param origin the new data holder.
	 */
	public void merge(Buddy origin){
		if (origin == null || origin == this){
			return;
		}
		
		serviceName = origin.serviceName;
		name = origin.name;
		protocolUid = origin.protocolUid;
		ownerUid = origin.ownerUid;
		serviceId = origin.serviceId;
		status = origin.status;
		xstatus = origin.xstatus;
		xstatusName = origin.xstatusName;
		xstatusDescription = origin.xstatusDescription;
		externalIP = origin.externalIP;
		onlineTime = origin.onlineTime;
		signonTime = origin.signonTime;
		visibility = origin.visibility;
		unread = origin.unread;
		canFileShare = origin.canFileShare;
		//capabilities = origin.capabilities;
		groupId = origin.groupId;
		iconHash = origin.iconHash;
		secureOptions = origin.secureOptions;
	}
	
	/**
	 * Returns holder account id, in form of "123456789 ICQ"
	 * 
	 * @see AccountView#getAccountId()
	 * @return id
	 */
	public String getOwnerAccountId(){
		return ownerUid+" "+serviceName;
	}
	
	public Buddy(){}

	/**
	 * Comparator. First checks status, then name, ignoring case.
	 * 
	 * @see Comparator
	 */
	@Override
	public int compareTo(Buddy another) {
		if (status != another.status){
			if (status == Buddy.ST_OFFLINE){
				return -1;
			}
			if (another.status == Buddy.ST_OFFLINE){
				return 1;
			}
		}
		return name.compareToIgnoreCase(another.name);
	}

	/**
	 * Obtain filename for buddy's additional data (history, icon etc) 
	 * 
	 * @return
	 */
	public String getFilename() {
		return getOwnerAccountId()+" "+protocolUid;
	}
	
	/**
	 * Get buddy's userpic.
	 * 
	 * @param context context to operate in
	 * @param filename icon file name
	 * @return icon bitmap as {@link Bitmap.Config.ARGB_8888}, if found, or null
	 */
	public static synchronized Bitmap getIcon(Context context, String filename){
		
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(filename+BUDDYICON_FILEEXT);
		} catch (Exception e) {
		}
		
		if (fis == null) return null;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inScaled = false;
		options.inPurgeable=true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		return BitmapFactory.decodeStream(fis, null, options);
	}

	/**
	 * Get chat tag for buddy.
	 * 
	 * @return
	 */
	public String getChatTag() {
		return ConversationsView.class.getSimpleName()+" "+serviceId+" "+protocolUid;
	}
}
