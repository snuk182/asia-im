package ua.snuk182.asia.view.more;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ITabContent;
import android.content.res.Resources.NotFoundException;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class AccountManagerView extends ListView implements ITabContent {

	private AccountAdapter adapter;
	
	public AccountManagerView(EntryPoint entryPoint){
		super(entryPoint);
		List<AccountView> accounts;
		try {
			accounts = entryPoint.runtimeService.getProtocolServices();
		} catch (NullPointerException npe) {	
			accounts = new ArrayList<AccountView>(0);
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			accounts = new ArrayList<AccountView>(0);
			ServiceUtils.log(e);
		}

		adapter = new AccountAdapter(this, accounts);
		setAdapter(adapter);
		
		visualStyleUpdated();
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_close:
			getEntryPoint().mainScreen.removeTabByTag(AccountManagerView.class.getSimpleName());
			break;
		case R.id.menuitem_add:
			try {
				if (getEntryPoint().runtimeService.getProtocolServices().size() < 126){
					getEntryPoint().addAccountEditorTab(null);
					getEntryPoint().mainScreen.removeTabByTag(AccountManagerView.class.getSimpleName());
				} else {
					Toast.makeText(getEntryPoint(), R.string.label_too_many_accounts, Toast.LENGTH_LONG);
				}
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			} catch (NotFoundException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
			break;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			if (getEntryPoint().mainScreen.getTabs().size()<2){
				getEntryPoint().exit();
			}else {
				getEntryPoint().mainScreen.removeTabByTag(AccountManagerView.class.getSimpleName());
			}
			
			return true;
		}

		return false;
	}


	@Override
	public int getMainMenuId() {
		return R.menu.account_manager_menu;
	}


	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());
		
		tabWidgetLayout.getTabName().setText(R.string.label_accounts);
		tabWidgetLayout.getTabIcon().setImageResource(R.drawable.accounts);
		
		return tabWidgetLayout;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}


	@Override
	public void visualStyleUpdated() {
		String bgType;
		
		try {
			bgType = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));
		} catch (NullPointerException npe) {	
			bgType = null;
			ServiceUtils.log(npe);
		} 
		if (bgType == null || bgType.equals("wallpaper")){
			setBackgroundColor(0x60000000);
			adapter.setTextColor(0xff000000);			
		}else {
			try {
				int color = (int) Long.parseLong(bgType);
				setBackgroundColor(0);
				adapter.setTextColor((color-0xff000000)>0x777777?0xff000000:0xffffffff);						
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}					
	}
	
	public EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}

	@Override
	public void onStart() {
	}


	@Override
	public void configChanged() {}
}
