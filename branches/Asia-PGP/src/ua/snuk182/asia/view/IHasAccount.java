package ua.snuk182.asia.view;

import ua.snuk182.asia.core.dataentity.AccountView;

public interface IHasAccount extends IHasBitmap {

	public byte getServiceId();
	public void stateChanged(AccountView account, boolean refreshContacts);
	public void updated(AccountView account, boolean refreshContacts);
	public void connectionState(int state);
}
