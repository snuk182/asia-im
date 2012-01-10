package ua.snuk182.asia.view.more;

import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Message;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.IHasServiceMessages;
import ua.snuk182.asia.view.ITabContent;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class AccountActivityView extends ScrollView implements ITabContent, IHasServiceMessages {

	private AccountView account;
	private LinearLayout layout;

	private Runnable scrollToEnd = new Runnable() {

		@Override
		public void run() {
			scrollBy(0, getEntryPoint().metrics.heightPixels);
		}
	};

	public AccountActivityView(EntryPoint entryPoint, AccountView account) {
		super(entryPoint);

		this.account = account;
		layout = new LinearLayout(entryPoint);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		int padding = (int) (2 * getEntryPoint().metrics.density);
		layout.setPadding(padding, padding, padding, padding);
		layout.setOrientation(LinearLayout.VERTICAL);
		addView(layout);
		
		List<ServiceMessage> messages = ServiceUtils.getAccountActivity(account);
		for (ServiceMessage msg : messages) {
			serviceMessageReceived(msg, true);
		}

		visualStyleUpdated();
	}

	@Override
	public synchronized void serviceMessageReceived(ServiceMessage message, boolean tabActive) {
		if (message.serviceId == account.serviceId && message.from.equals(account.getAccountId())) {
			layout.addView(getListItem(message));
			if (tabActive) {
				post(scrollToEnd);
			}
		}
	}

	@Override
	public int getMainMenuId() {
		return R.menu.default_menu;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_close:
			close();
			break;
		}
		return false;
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());

		tabWidgetLayout.getTabName().setText(R.string.label_account_activity_log);
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
		if (bgType == null || bgType.equals("wallpaper")) {
			setBackgroundColor(0x60000000);
			for (int i = 0; i < layout.getChildCount(); i++) {
				TextView tv = (TextView) layout.getChildAt(i);
				tv.setBackgroundColor(0x60000000);
				tv.setTextColor(ColorStateList.valueOf(0xffffffff));
			}

		} else {
			try {
				int color = (int) Long.parseLong(bgType);
				setBackgroundColor(0);
				ColorStateList colorState = ColorStateList.valueOf((color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff);
				for (int i = 0; i < layout.getChildCount(); i++) {
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
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			close();
			return true;
		}

		return false;
	}

	private void close() {
		getEntryPoint().mainScreen.removeTabByTag(AccountActivityView.class.getSimpleName() + " " + account.getAccountId());
	}

	@Override
	public void onStart() {}

	@Override
	public void configChanged() {
	}

	private View getListItem(Message message) {
		TextView tv = new TextView(getContext());
		tv.setTextColor(0xff00ff00);
		tv.setGravity(Gravity.CENTER);
		tv.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
		tv.setText(message.text);
		return tv;
	}
}
