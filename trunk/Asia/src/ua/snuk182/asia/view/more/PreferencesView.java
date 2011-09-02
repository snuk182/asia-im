package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.services.ServiceConstants;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ITabContent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.TextView;

public class PreferencesView extends PreferenceActivity implements ITabContent {
	
	Bundle options;
	AccountView account;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		getListView().setBackgroundColor(0x0);
		final EntryPoint entryPoint = (EntryPoint) getParent();
		
		account = getIntent().getParcelableExtra(ServiceConstants.INTENTEXTRA_SERVICEID);
		if (account!= null){
			addPreferencesFromResource(ServiceUtils.getPreferencesIdByAccount(entryPoint, account));
			options = account.options;
		}else {
			
			addPreferencesFromResource(R.xml.preferences);
			try {
				options = entryPoint.getApplicationOptions();
			} catch (NullPointerException npe) {	
				return;
			} 
		}
		
		PreferenceScreen screen = getPreferenceScreen();
		
		for (int k=0; k<screen.getPreferenceCount(); k++){
			PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().getPreference(k);
			for (int i=0; i<category.getPreferenceCount(); i++){
				Preference pref = category.getPreference(i);
				pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

					@Override
					public boolean onPreferenceChange(Preference arg0, Object arg1) {
						options.putString(arg0.getKey(), arg1.toString());
						try {
							entryPoint.runtimeService.savePreference(arg0.getKey(), arg1.toString(), account!=null ? account.serviceId : -1);
						} catch (NullPointerException npe) {	
							ServiceUtils.log(npe);
						} catch (RemoteException e) {
							getEntryPoint().onRemoteCallFailed(e);
						}
						return true;
					}
				});
				Object value = options.get(pref.getKey());
				if (value!=null){
					if (pref instanceof CheckBoxPreference){
						((CheckBoxPreference)pref).setChecked(Boolean.parseBoolean((String) value));
					}
					if (pref instanceof EditTextPreference){
						((EditTextPreference)pref).setText((String) value);
					}
					if (pref instanceof ListPreference){
						((ListPreference)pref).setValue((String) value);
					}
				}
			}
		}
		
		final String bgType = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));;
		
		getListView().setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			
			@Override
			public void onChildViewRemoved(View parent, View child) {}
			
			@Override
			public void onChildViewAdded(View parent, View child) {
				updateStyleForTitle(child, bgType);				
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			String tag = account!=null ? PreferencesView.class.getSimpleName()+" "+account.serviceId : PreferencesView.class.getSimpleName();
			
			getEntryPoint().mainScreen.removeTabByTag(tag);
			return true;
		}

		return false;
	}

	@Override
	public int getMainMenuId() {
		return 0;
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getParent();
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		return null;
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
		
		for (int i=0; i<getListView().getChildCount(); i++){
			View pre = getListView().getChildAt(i);
			updateStyleForTitle(pre, bgType);
		}
	}
	
	private void updateStyleForTitle(View pre, String bgType) {
		if (bgType == null || bgType.equals("wallpaper")){
			getListView().setBackgroundColor(0x60000000);
			TextView title = (TextView) pre.findViewById(android.R.id.title);
				if (title != null){
					title.setTextColor(ColorStateList.valueOf(0xffffffff));
				}
			
			
		} else {
			getListView().setBackgroundColor(0);
			try {
				int color = (int) Long.parseLong(bgType);
				TextView title = (TextView) pre.findViewById(android.R.id.title);
					if (title != null){
						title.setTextColor(ColorStateList.valueOf((color-0xff000000)>0x777777?0xff000000:0xffffffff));
					}
						
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}
		
	}
	
	@Override
	public void onStart(){
		super.onResume();
	}
	
	@Override
	public void configChanged() {}
}
