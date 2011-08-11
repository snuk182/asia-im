package ua.snuk182.asia.view;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public interface ITabContent{
	
	public int getMainMenuId();

	boolean onPrepareOptionsMenu(Menu menu);
	
	boolean onOptionsItemSelected(MenuItem item);
	
	boolean onKeyDown(int i, KeyEvent event);
	
	EntryPoint getEntryPoint();
	
	TabWidgetLayout getTabWidgetLayout();
	
	void visualStyleUpdated();

	public void onResume();
}
