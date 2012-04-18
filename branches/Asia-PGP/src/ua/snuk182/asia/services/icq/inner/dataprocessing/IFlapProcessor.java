package ua.snuk182.asia.services.icq.inner.dataprocessing;

import ua.snuk182.asia.services.icq.inner.ICQException;
import ua.snuk182.asia.services.icq.inner.ICQServiceInternal;
import ua.snuk182.asia.services.icq.inner.dataentity.Flap;

public interface IFlapProcessor {
	public void process(Flap flap) throws ICQException;
	public void init(ICQServiceInternal icqServiceInternal) throws ICQException;
	
	public void onDisconnect();	
}
