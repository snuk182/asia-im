package ua.snuk182.asia.view;

import ua.snuk182.asia.core.dataentity.AccountView;

public interface IHasAccount extends IHasBitmap {

	public int getServiceId();
	public void stateChanged(AccountView account);
	public void updated(AccountView account);
	public void connectionState(int state);
}
