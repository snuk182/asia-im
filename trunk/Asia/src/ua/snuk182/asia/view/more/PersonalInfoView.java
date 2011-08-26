package ua.snuk182.asia.view.more;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.cl.ContactList;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalInfoView extends ScrollView implements ITabContent{
	
	private PersonalInfo info;
	private Buddy buddy;
	
	private final LinearLayout layout;
	
	private OnLongClickListener longClickListener = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			ClipboardManager clipMan = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			clipMan.setText(((TextView)v).getText());
			Toast.makeText(getContext(), R.string.label_copied_to_clipboard, Toast.LENGTH_SHORT).show();
			return true;
		}
	};

	public PersonalInfoView(EntryPoint context, Buddy buddy, PersonalInfo info) {
		super(context);
		layout = new LinearLayout(getContext());
		
		this.buddy = buddy;
		this.info = info;
		
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		int padding = (int) (2*getEntryPoint().metrics.density);
		layout.setPadding(padding, padding, padding, padding);
		layout.setOrientation(LinearLayout.VERTICAL);
		addView(layout);
		
		visualStyleUpdated();
		
		TextView uidView = new TextView(getContext());
		uidView.setText("UID: "+buddy.protocolUid, TextView.BufferType.EDITABLE);
		colorView(uidView);
		uidView.setOnLongClickListener(longClickListener);
		layout.addView(uidView);
		
		updateInfo(info);
	}
	
	private void colorView(TextView view){
		Spannable s = view.getEditableText();
		if (s == null){
			return;
		}
		
		if (view.getText().toString().indexOf("UID: ") > -1){
			s.setSpan(new ForegroundColorSpan(0xffff0000), 0, view.getText().toString().indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {
			s.setSpan(new ForegroundColorSpan(0xff00a5ff), 0, view.getText().toString().indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
	}

	@Override
	public int getMainMenuId() {
		return R.menu.personal_info_view_menu;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			getEntryPoint().mainScreen.checkAndSetCurrentTabByTag(ContactList.class.getSimpleName() + " " + buddy.serviceId);
			return true;
		}

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.menuitem_copy_all:
	    	copyAll();
	    	return true;
	    case R.id.menuitem_close:
			close();
			break;
	    }
	    return false;
	}
	
	private void close() {
		getEntryPoint().mainScreen.removeTabByTag(PersonalInfoView.class.getSimpleName()+" "+buddy.serviceId+" "+buddy.protocolUid);		
	}

	public void updateInfo(PersonalInfo info){
		if (info == null || info.properties == null){
			return;
		}
		
		List<String> keys = new ArrayList<String>();
		keys.addAll(info.properties.keySet());
		
		Collections.sort(keys);
		
		for (String key: keys){
			if (info.properties.get(key).toString().equals("-1")){
				continue;
			}
			
			TextView iew = new TextView(getContext());
			
			if (key.equals(PersonalInfo.INFO_GENDER)){
				info.properties.putString(key, ((Byte)info.properties.get(key)) == 1 ? getResources().getString(R.string.label_male) : getResources().getString(R.string.label_female));
			}
			
			iew.setText(key+": "+info.properties.get(key), TextView.BufferType.EDITABLE);
			colorView(iew);
			iew.setOnLongClickListener(longClickListener);
			layout.addView(iew);
		}
		
		visualStyleUpdated();
	}

	private void copyAll() {
		StringBuilder str = new StringBuilder();
		str.append("UID: ");
		str.append(info.protocolUid);
		str.append("\n");
		
		Set<String> keys = info.properties.keySet();
		for (String key: keys){
			str.append(key);
			str.append(": ");
			str.append(info.properties.get(key));
			str.append("\n");
		}
		
		ClipboardManager clipMan = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		clipMan.setText(str);
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());
		
		tabWidgetLayout.getTabName().setText(R.string.label_personal_info);
		tabWidgetLayout.getTabIcon().setImageResource(R.drawable.contact_24px);
		tabWidgetLayout.getTabIcon().setScaleType(ScaleType.FIT_XY);
		
		return tabWidgetLayout;
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
			for (int i=0; i<layout.getChildCount(); i++){
				TextView tv = (TextView) layout.getChildAt(i);
				tv.setBackgroundColor(0x60000000);
				tv.setTextColor(ColorStateList.valueOf(0xffffffff));
			}
			
		}else {
			try {
				int color = (int) Long.parseLong(bgType);
				setBackgroundColor(0);
				ColorStateList colorState = ColorStateList.valueOf((color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff);
				for (int i=0; i<layout.getChildCount(); i++){
					TextView tv = (TextView) layout.getChildAt(i);
					tv.setBackgroundColor(0);
					tv.setTextColor(colorState);
				}
				
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
		}
	}

	@Override
	public void onResume() {}

	@Override
	public void configChanged() {}
}
