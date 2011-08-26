package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.IHasFileTransfer;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class FileTransferView extends ScrollView implements ITabContent, IHasFileTransfer{
	
	private int serviceId = -1;
	
	private final TabWidgetLayout tabWidgetLayout;
	private final LinearLayout layout;

	public FileTransferView(EntryPoint entryPoint, AccountView account) {
		super(entryPoint);
		this.serviceId = account.serviceId;
		
		setLayoutParams(new ScrollView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout = new LinearLayout(entryPoint);
		layout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		addView(layout);
		
		tabWidgetLayout = new TabWidgetLayout(entryPoint);
		
		int size = (int) (32*entryPoint.metrics.density);
		tabWidgetLayout.getTabIcon().setLayoutParams(new LinearLayout.LayoutParams(size, size));
		tabWidgetLayout.getTabIcon().setImageResource(android.R.drawable.ic_menu_save);
		tabWidgetLayout.getTabIcon().setScaleType(ScaleType.FIT_XY);
		tabWidgetLayout.getTabName().setText(account.getSafeName());
		
		visualStyleUpdated();
	}

	@Override
	public int getServiceId() {
		return serviceId;
	}

	@Override
	public void stateChanged(AccountView account) {
				
	}

	@Override
	public void updated(AccountView account) {
				
	}

	@Override
	public void connectionState(int state) {
				
	}

	@Override
	public void bitmap(String uid) {
				
	}

	@Override
	public int getMainMenuId() {
		return R.menu.ft_menu;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem showTabsItem = menu.findItem(R.id.menuitem_showtabs);
		String hideTabsStr;
		try {
			hideTabsStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_view_type));
			if (hideTabsStr != null) {
				boolean hideTabs = hideTabsStr.equals(getResources().getString(R.string.value_view_type_notabs));
				showTabsItem.setVisible(hideTabs);
			} else {
				showTabsItem.setVisible(false);
			}
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} 

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_close:
			// kbManager.hideSoftInputFromWindow(textEditor.getWindowToken(),
			// 0);
			closeMe();
			returnToBuddyList();
			break;
		case R.id.menuitem_showtabs:
			ViewUtils.showTabChangeMenu(getEntryPoint());
			break;
		}
		return false;
	}

	private void closeMe() {
		getEntryPoint().mainScreen.removeTabByTag(FileTransferView.class.getSimpleName()+" "+serviceId);
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			returnToBuddyList();
			return true;
		}

		return false;
	}

	private void returnToBuddyList() {
		getEntryPoint().mainScreen.checkAndSetCurrentTabByTag(ContactList.class.getSimpleName() + " " + serviceId);
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		return tabWidgetLayout;
	}

	@Override
	public void visualStyleUpdated() {
		String bgType;
		int textColor = 0xffff;
		try {
			bgType = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));
		} catch (NullPointerException npe) {	
			bgType = null;
			ServiceUtils.log(npe);
		} 
		if (bgType == null || bgType.equals("wallpaper")) {
			setBackgroundColor(0x60000000);
			textColor = 0xffffffff;
		} else {
			try {
				int color = (int) Long.parseLong(bgType);
				setBackgroundColor(0);
				textColor = (color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff;
			} catch (NumberFormatException e) {
				ServiceUtils.log(e);
			}
		}	
		for (int i = 0; i<layout.getChildCount(); i++){
			FileTransferItem ft = (FileTransferItem) layout.getChildAt(i);
			ft.title.setTextColor(textColor);
		}
	}

	@Override
	public void onResume() {
				
	}

	@Override
	public void notifyFileProgress(long messageId, Buddy buddy, String filename, long totalSize, long sizeTransferred, Boolean isReceive, String error) {
		FileTransferItem item = null;
		for (int i = 0; i<layout.getChildCount(); i++){
			FileTransferItem ft = (FileTransferItem) layout.getChildAt(i);
			if (ft.messageId == messageId){
				item = ft;
				break;
			}
		}
		
		if (item == null){
			item = new FileTransferItem(this, messageId, filename, buddy, isReceive, (int)sizeTransferred, (int)totalSize, error);
			layout.addView(item);
		}
		
		item.populate((int) sizeTransferred, error);
	}

	public void removeTask(FileTransferItem item) {
		layout.removeView(item);
		if (layout.getChildCount()<1){
			closeMe();
		}
	}

	@Override
	public void configChanged() {}
}
