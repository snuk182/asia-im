package ua.snuk182.asia.view;

import ua.snuk182.asia.core.dataentity.ServiceMessage;

public interface IHasServiceMessages {

	public void serviceMessageReceived(ServiceMessage msg, boolean tabActive);
}
