package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.services.ServiceStoredPreferences;
import ua.snuk182.asia.view.ITabContent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MasterPasswordView extends RelativeLayout implements ITabContent {
	
	final EditText pwText;

	public MasterPasswordView(final EntryPoint context) {
		super(context);
		
		LayoutInflater inf = LayoutInflater.from(context);
		inf.inflate(R.layout.master_pw_layout, this);
		
		pwText = (EditText) findViewById(R.id.master_pw_text);
		
		Button okBtn = (Button) findViewById(R.id.button_ok);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String pw = pwText.getText().toString();
				
				if (pw == null || pw.isEmpty()){
					Toast.makeText(context, R.string.label_pw_cannot_be_empty, Toast.LENGTH_LONG).show();
					return;
				}
				
				if (!checkPw(pw)){
					Toast.makeText(context, R.string.label_wrong_pw, Toast.LENGTH_LONG).show();
					return;
				}
				
				getEntryPoint().proceedLoading();
			}
		});
	}

	private boolean checkPw(String pw) {
		String pww = ServiceStoredPreferences.getOption(getContext(), getResources().getString(R.string.key_master_password));
		return pw.equals(pww);
	}

	@Override
	public int getMainMenuId() {
		return 0;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public boolean onKeyDown(int i, KeyEvent event) {
		return false;
	}

	@Override
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());
		
		tabWidgetLayout.setText(R.string.label_wait_starting);
		return tabWidgetLayout;
	}

	@Override
	public void visualStyleUpdated() {
	
	}

	@Override
	public void onStart() {}

	@Override
	public void configChanged() {}

}
