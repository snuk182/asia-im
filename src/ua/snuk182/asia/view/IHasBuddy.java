package ua.snuk182.asia.view;

import ua.snuk182.asia.core.dataentity.Buddy;

public interface IHasBuddy extends IHasAccount{

	public void updateBuddyState(Buddy buddy);
}
