package ua.snuk182.asia.view.cl;

import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.TextMessage;
import android.view.ViewParent;


public interface IContactListDrawer {

	public void removeAllViews();

	public ViewParent getParent();

	public void updateView(ContactList parent);

	public void messageReceived(TextMessage message);

	public void updateBuddyState(Buddy buddy);

	public String getType();

	public void bitmap(String uid);
	
	public boolean hasUnreadMessages();	
}
