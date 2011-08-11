package ua.snuk182.asia.view;

import ua.snuk182.asia.core.dataentity.TextMessage;

public interface IHasMessages {
	
	public void messageReceived(TextMessage message, boolean activeTab);

	//public void serviceMessageReceived(ServiceMessage msg);

}
