package ua.snuk182.asia.view.more;

import java.util.Set;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
		layout = new LinearLayout(context);
		
		this.buddy = buddy;
		this.info = info;
		
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		int padding = (int) (2*getEntryPoint().metrics.density);
		layout.setPadding(padding, padding, padding, padding);
		layout.setOrientation(LinearLayout.VERTICAL);
		addView(layout);
		
		visualStyleUpdated();
		
		updateInfo(info);
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
		ViewUtils.updatePersonalInfoLayout(info, layout, longClickListener);
		
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
		
		tabWidgetLayout.setText(R.string.label_personal_info);
		tabWidgetLayout.setImageResource(R.drawable.contact_24px);
		//tabWidgetLayout.setScaleType(ScaleType.FIT_XY);
		
		return tabWidgetLayout;
	}

	@Override
	public void visualStyleUpdated() {
		if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER){
			setBackgroundColor(0x60000000);
			for (int i=0; i<layout.getChildCount(); i++){
				TextView tv = (TextView) layout.getChildAt(i);
				tv.setBackgroundColor(0x60000000);
				tv.setTextColor(ColorStateList.valueOf(0xffffffff));
			}
			
		}else {
			try {
				int color = EntryPoint.bgColor;
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
	public void onStart() {}

	@Override
	public void configChanged() {}
}
