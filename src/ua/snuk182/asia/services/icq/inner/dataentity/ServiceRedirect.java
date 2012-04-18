package ua.snuk182.asia.services.icq.inner.dataentity;

import java.io.Serializable;

import ua.snuk182.asia.services.api.ProtocolUtils;

public class ServiceRedirect implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4761405668642668407L;
	
	public String serviceServerURL = null;
	public byte[] authCookie = null;
	public short family = 0;
	
	public ServiceRedirect(TLV[] tlvs){
		if (tlvs == null) return;
		
		for (TLV tlv:tlvs){
			switch(tlv.type){
			case 0xd:
				family = ProtocolUtils.bytes2ShortBE(tlv.value);
				break;
			case 0x5:
				serviceServerURL = ProtocolUtils.getEncodedString(tlv.value);
				break;
			case 0x6:
				authCookie = tlv.value;
				break;
			}
		}			
	}
}
