package ua.snuk182.asia.view.more;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.Message;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.IHasMessages;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.conversations.HistoryViewAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

public class HistoryView extends ListView implements ITabContent, IHasMessages {

	private final Buddy buddy;
	private final List<Message> messages = new ArrayList<Message>();
	
	private String historyId = null;
	private String exportResult = null;
	
	private final Handler handler = new Handler();
	
	private final Runnable exportFinishedRunnable = new Runnable(){

		@Override
		public void run() {
			if (exportResult != null){
				Toast.makeText(getContext(), exportResult, Toast.LENGTH_LONG).show();
				exportResult = null;
			}
		}
		
	};

	public HistoryView(EntryPoint entryPoint, Buddy buddy, String tag) {
		super(entryPoint);

		this.buddy = buddy;
		historyId = tag;

		setAdapter(new HistoryViewAdapter(entryPoint, messages, 16, getEntryPoint().dontDrawSmileys));
		setDividerHeight(0);
		
		visualStyleUpdated();

		messages.clear();
		messages.addAll(buddy.getHistorySaver().getLastHistory(entryPoint, true));
		((HistoryViewAdapter) getAdapter()).notifyDataSetChanged();
		setSelection(messages.size() - 1);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_close:
			closeHistory();
			break;
		case R.id.menuitem_return:
			getEntryPoint().mainScreen.checkAndSetCurrentTabByTag(ContactList.class.getSimpleName() + " " + buddy.getOwnerAccountId());
			break;
		case R.id.menuitem_delete:
			messages.clear();
			buddy.getHistorySaver().deleteHistory(getEntryPoint());
			((HistoryViewAdapter) getAdapter()).notifyDataSetChanged();
			break;
		case R.id.menuitem_export:
			exportHistory();
			break;
		}
		return false;
	}

	private void exportHistory() {
		new Thread(){
			@Override
			public void run(){
				exportResult = buddy.getHistorySaver().exportHistory(getEntryPoint());
				handler.post(exportFinishedRunnable);
			}
		}.start();
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		if (i == KeyEvent.KEYCODE_BACK) {
			closeHistory();
			return true;
		}

		return false;
	}

	private void closeHistory() {
		getEntryPoint().mainScreen.removeTabByTag(historyId);
	}

	@Override
	public void messageReceived(TextMessage message, boolean activeTab) {
		if (message.from.equals(buddy.protocolUid)){
			messages.add(message);
			((HistoryViewAdapter) getAdapter()).notifyDataSetChanged();
			setSelection(messages.size() - 1);
		}
	}

	@Override
	public int getMainMenuId() {
		return R.menu.history_view_menu;
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());
		
		tabWidgetLayout.getTabName().setText(getEntryPoint().getResources().getString(R.string.label_history)+" - "+buddy.name);
		tabWidgetLayout.getTabIcon().setImageResource(R.drawable.history);
		return tabWidgetLayout;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void visualStyleUpdated() {
		HistoryViewAdapter historyAdapter = (HistoryViewAdapter) getAdapter();
		
		String bgType;
		String textSize;
		try {
			bgType = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));
			textSize = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_text_size));
		} catch (NullPointerException npe) {	
			bgType = null;
			textSize = null;
			ServiceUtils.log(npe);
		} 
		if (bgType == null || bgType.equals("wallpaper")) {
			setBackgroundColor(0x60000000);
			historyAdapter.setBgColor(0x60000000);
			historyAdapter.setTextColor(0xffffffff);
		} else {
			try {
				int color = (int) Long.parseLong(bgType);
				setBackgroundColor(0);
				historyAdapter.setBgColor(0);
				historyAdapter.setTextColor((color - 0xff000000) > 0x777777 ? 0xff000000 : 0xffffffff);
			} catch (NumberFormatException e) {
				ServiceUtils.log(e);
			}
		}
		
		Display display = ((WindowManager) getEntryPoint().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		if (textSize == null || textSize.equals(getResources().getString(R.string.value_size_medium))) {
			historyAdapter.setTextSize(16 * metrics.density);
		} else if (textSize.equals(getResources().getString(R.string.value_size_big))) {
			historyAdapter.setTextSize(20 * metrics.density);
		} else if (textSize.equals(getResources().getString(R.string.value_size_small))) {
			historyAdapter.setTextSize(12 * metrics.density);
		} else {
			historyAdapter.setTextSize(8 * metrics.density);
		}				
	}
	
	public EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}

	@Override
	public void onStart() {}

	@Override
	public void configChanged() {}
}
