package ua.snuk182.asia.view;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Tab content interface. All tabs should implement it.
 * 
 * @author SergiyP
 *
 */
public interface ITabContent{
	
	/**
	 * Get menu resource id for tab implementation
	 * 
	 * @return menu resource id
	 */
	public int getMainMenuId();

	/**
	 * Action for preparing options menu for tab implementation. See also {@link Activity#onPrepareOptionsMenu(Menu)} for details.
	 * 
	 * @param menu menu object to be filled
	 * @return true if menu should be displayed
	 */
	boolean onPrepareOptionsMenu(Menu menu);
	
	/**
	 * Action on menu item selection for the tab implementation. See also {@link Activity#onOptionsItemSelected(MenuItem)} for details.
	 * 
	 * @param item selected menu item
	 */
	boolean onOptionsItemSelected(MenuItem item);
	
	/**
	 * Action on key press for the tab. See also {@link Activity#onKeyDown(int, KeyEvent)} for details.
	 * 
	 * @param keyCode key code from {@link KeyEvent}
	 * @param event the key event from {@link KeyEvent}
	 * @return
	 */
	boolean onKeyDown(int keyCode, KeyEvent event);
	
	/**
	 * Get {@link EntryPoint} instance, usually casted from context.
	 * 
	 * @return EntryPoint instance
	 */
	EntryPoint getEntryPoint();
	
	/**
	 * get {@link TabWidgetLayout} for the tab. 
	 * 
	 * @return
	 */
	TabWidgetLayout getTabWidgetLayout();
	
	/**
	 * Action on visual style updated
	 */
	void visualStyleUpdated();

	/**
	 * Action on application start. See also {@link Activity#onStart()}.
	 */
	public void onStart();

	/**
	 * Action on configuration changed. See also {@link Activity#onConfigurationChanged(android.content.res.Configuration)}.
	 */
	public void configChanged();
}
