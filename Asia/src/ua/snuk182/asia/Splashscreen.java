/**
 * 
 */
package ua.snuk182.asia;

import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

/**
 * Splashscreen for tablet mode. Shown if no conversation tabs opened.
 * 
 * @author Sergiy P
 *
 */
public class Splashscreen extends RelativeLayout implements ITabContent{
	
	public Splashscreen(EntryPoint entryPoint){
		super(entryPoint);
		
		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.splashscreen, this);
		
		setGravity(Gravity.CENTER);
		
		visualStyleUpdated();
	}
	
	@Override 
	public boolean onKeyDown(int i, KeyEvent event) {

		  return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_exit:
			getEntryPoint().exit();
		}
		return false;
	}

	@Override
	public int getMainMenuId() {
		//return R.menu.splashscreen_menu;
		return 0;
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());
		
		tabWidgetLayout.setText(R.string.app_name);
		return tabWidgetLayout;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void visualStyleUpdated() {
		if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER){
			setBackgroundColor(0x60000000);
		} else {
			try {
				setBackgroundColor(0);
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}		
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint)getContext();
	}

	@Override
	public void onStart() {		
	}

	@Override
	public void configChanged() {}
}
