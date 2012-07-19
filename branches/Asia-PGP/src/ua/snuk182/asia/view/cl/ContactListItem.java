package ua.snuk182.asia.view.cl;

import ua.snuk182.asia.core.dataentity.Buddy;
import android.view.View.OnFocusChangeListener;

public interface ContactListItem extends OnFocusChangeListener{

	public void populate(Buddy buddy);
	public void color();
	public void requestIcon(final Buddy buddy);
	public void setTag(String tag);
}
