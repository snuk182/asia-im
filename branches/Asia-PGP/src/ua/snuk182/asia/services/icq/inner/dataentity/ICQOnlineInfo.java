package ua.snuk182.asia.services.icq.inner.dataentity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.snuk182.asia.services.icq.inner.ICQConstants;

public class ICQOnlineInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8238204242585177130L;
	
	public short itemId;
	public String uin;
	public short warnLevel;
	public List<TLV> tlvs = new ArrayList<TLV>();
	
	public short userClass;
	public int userStatus = ICQConstants.STATUS_OFFLINE;
	public ICQDCInfo dcInfo;
	public String extIP;
	public int onlineTime;
	public Date signonTime;
	public Date memberSinceTime;
	public short distribNumber;
	public String personalText;
	public String extendedStatus;
	public byte visibility;
	public boolean typingNotification = true;
	public boolean idleTimeNotification = true;
	public boolean handheldNotification = true;
	public List<String> capabilities;
	public int idleTime;
	public Date createTime;
	public ICQIconData iconData;
	public String name;
	public byte[] qipStatus;
	public byte extendedStatusId = -1;
	
	public ICQOnlineInfo() {
	}
}
