package ua.snuk182.asia.services.icq.inner.dataentity;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ICBMMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7486438674845449876L;

	public String text;
	public String senderId;
	public String receiverId;
	public short channel = 0;
	public byte[] messageId = new byte[8];
	public TLV[] tlvs;
	public String capability;
	public Date sendingTime;
	public Date receivingTime;
	public byte messageType;
	public byte[] pluginSpecificData;
	public String rvIp;
	public String internalIp;
	public String externalIp;
	public int externalPort;
	public String invitation;
	public short rvMessageType = 0;
	
	public final List<ICQFileInfo> files = new LinkedList<ICQFileInfo>();

	public boolean connectFTProxy = false;
	public boolean connectFTPeer = false;
}
