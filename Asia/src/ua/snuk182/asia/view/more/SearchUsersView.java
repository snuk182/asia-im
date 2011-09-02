package ua.snuk182.asia.view.more;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.view.IHasAccount;
import ua.snuk182.asia.view.ITabContent;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.RemoteException;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class SearchUsersView extends LinearLayout implements ITabContent, IHasAccount {
	
	private EntryPoint entryPoint;
	private AccountView account;

	EditText uinEditor;
	Button searchBtn;
	ListView searchResultsView;
	List<PersonalInfo> infoList = new ArrayList<PersonalInfo>();

	ProgressDialog progressDialog;

	private SearchUsersAdapter searchAdapter;
	
	public SearchUsersView(final EntryPoint entryPoint, final AccountView account){
		super(entryPoint);
		this.entryPoint = entryPoint;
		this.account = account;
		
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.search_users, this);
		setOrientation(VERTICAL);
		setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		setPadding(10, 10, 10, 10);
		uinEditor = (EditText) findViewById(R.id.buddy_uin);
		int maxLength = 9;
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(maxLength);
		uinEditor.setFilters(filterArray);
		searchBtn = (Button) findViewById(R.id.button_search);
		searchResultsView = (ListView) findViewById(R.id.search_box_found_list);
		searchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (account.getConnectionState() != AccountService.STATE_CONNECTED){
					Toast.makeText(entryPoint, "connect network first", Toast.LENGTH_LONG).show();
					return;
				}
				
				if (uinEditor.getText() == null || uinEditor.getText().length() < 1) {
					Toast.makeText(entryPoint, R.string.error_fill_uid, Toast.LENGTH_LONG).show();
					return;
				}

				try {
					progressDialog = ProgressDialog.show(entryPoint, "", getResources().getString(R.string.label_wait), true);
					progressDialog.setCancelable(true);
					entryPoint.runtimeService.searchUsersByUid(account.serviceId, uinEditor.getText().toString());
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					getEntryPoint().onRemoteCallFailed(e);
				}
			}

		});
		searchAdapter = new SearchUsersAdapter(entryPoint, account.serviceId, infoList);
		searchResultsView.setAdapter(searchAdapter);
		
		visualStyleUpdated();
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
				return true;
			}
			entryPoint.mainScreen.removeTabByTag(SearchUsersView.class.getSimpleName()+ " " + account.serviceId);
			return true;
		}

		return false;
	}

	public void searchResult(List<PersonalInfo> infos) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		infoList.clear();
		infoList.addAll(infos);
		searchAdapter.notifyDataSetChanged();
	}

	@Override
	public int getMainMenuId() {
		return 0;
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(entryPoint);
		
		tabWidgetLayout.getTabName().setText(R.string.label_search_buddies);
		tabWidgetLayout.getTabIcon().setImageResource(android.R.drawable.ic_menu_search);
		
		return tabWidgetLayout;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
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
			searchAdapter.setTextColor(0xff000000);			
		}else {
			try {
				int color = (int) Long.parseLong(bgType);
				setBackgroundColor(0);
				searchAdapter.setTextColor((color-0xff000000)>0x777777?0xff000000:0xffffffff);						
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}				
	}
	
	public EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}

	@Override
	public void bitmap(String uid) {
				
	}

	@Override
	public int getServiceId() {
		return account.serviceId;
	}

	@Override
	public void stateChanged(AccountView account) {
		this.account.merge(account);		
	}

	@Override
	public void updated(AccountView account) {
		
	}

	@Override
	public void connectionState(int state) {
		
	}

	@Override
	public void onStart() {}
	
	@Override
	public void configChanged() {}
}
