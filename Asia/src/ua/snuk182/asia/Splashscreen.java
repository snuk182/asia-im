/**
 * 
 */
package ua.snuk182.asia;

import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

/**
 * @author Sergiy P
 *
 */
public class Splashscreen extends RelativeLayout implements ITabContent{
	
	public Splashscreen(EntryPoint entryPoint){
		super(entryPoint);
		
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.splashscreen, this);
		
		setGravity(Gravity.CENTER);
	}
	
	@Override 
	public boolean onKeyDown(int i, KeyEvent event) {

		  /*if (i == KeyEvent.KEYCODE_BACK) {
		    Toast.makeText(getContext(), getResources().getString(R.string.label_sorry_back_button), Toast.LENGTH_LONG).show();
		    return true; 
		  }*/

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
		return R.menu.splashscreen_menu;
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());
		
		tabWidgetLayout.getTabName().setText(R.string.label_wait_starting);
		return tabWidgetLayout;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void visualStyleUpdated() {
				
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint)getContext();
	}

	@Override
	public void onResume() {		
	}
}
