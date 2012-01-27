package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.icq.ICQService;
import ua.snuk182.asia.services.mrim.MrimService;
import ua.snuk182.asia.services.xmpp.XMPPService;
import ua.snuk182.asia.view.ITabContent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NewAccountView extends ScrollView implements ITabContent {
	
	private AccountView account;
	private Spinner protocolChooser;
	private LinearLayout customOptionsLayout;
	
	Bundle bu = null;
	public String tag;
	
	public NewAccountView(EntryPoint entryPoint, final AccountView account){
		super(entryPoint);
		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.new_account_view, this);
		
		this.account = account;
		
		if (account != null){
			tag = NewAccountView.class.getSimpleName()+" "+account.serviceId;
		} else {
			try {
				tag = NewAccountView.class.getSimpleName()+" "+entryPoint.runtimeService.getAccounts(true).size();
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				ServiceUtils.log(e);
				tag = NewAccountView.class.getSimpleName();
			}
		}
		
		protocolChooser = (Spinner) findViewById(R.id.newaccountprotocolchooser);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.protocolnames, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	    customOptionsLayout = (LinearLayout) findViewById(R.id.custom_options_layout);
	    
		protocolChooser.setAdapter(adapter);
		
		protocolChooser.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				customOptionsLayout.removeAllViews();
				
				String[] options = null;
				String[] optionNames = null;
				String[] optionDefaults = null;
				AccountService aps = null;
				if (((String)protocolChooser.getSelectedItem()).equals(getContext().getResources().getString(R.string.icq_service_name))){
					aps = new ICQService(getContext());
				}
				if (((String)protocolChooser.getSelectedItem()).equals(getContext().getResources().getString(R.string.xmpp_service_name))){
					aps = new XMPPService(getContext());
				}
				if (((String)protocolChooser.getSelectedItem()).equals(getContext().getString(R.string.mrim_service_name))){
					aps = new MrimService(getContext());
				}
				options = getResources().getStringArray(aps.getProtocolOptionNames());
				optionNames = getResources().getStringArray(aps.getProtocolOptionStrings());
				optionDefaults = getResources().getStringArray(aps.getProtocolOptionDefaults());
				
				if (options!=null){
					for (int i=0; i<options.length; i++){
						String option = options[i];
						
						TextView tv = new TextView(getContext());
						tv.setText(optionNames[i]);
						tv.setTag("");
						tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
						tv.setTextSize(16);
						
						EditText et = new EditText(getContext());
						et.setTag(option);
						//et.setTag(R.string.description, optionNames[i]);
						et.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
						
						et.setEnabled(account == null || !option.equals("uid"));
						if (option.equals("password")){
							et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
						}
						
						if (bu!=null){
							et.setText(bu.getString(option));
						}else {
							et.setText(optionDefaults[i]);
						}
						
						customOptionsLayout.addView(tv);
						customOptionsLayout.addView(et);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				customOptionsLayout.removeAllViews();
			}
		});
		
		if (account!=null){
			for (int i=0; i<adapter.getCount(); i++){
				if (adapter.getItem(i).equals(account.protocolName)){
					protocolChooser.setSelection(i);
				}
			}		
			
			try {
				bu = entryPoint.runtimeService.getProtocolServiceOptions(account.serviceId);
			} catch (RemoteException e) {
				ServiceUtils.log(e);
			}

			
		}
		
		Button createBtn = (Button) findViewById(R.id.newaccountokbutton);
		if (account != null){
			createBtn.setText(R.string.label_edit);
		} else {
			createBtn.setText(R.string.label_create);
		}
		createBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				createAccount();
			}
		});
		
		Button cancelBtn = (Button) findViewById(R.id.newaccountcancelbutton);
		cancelBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				removeMe();
			}});
		visualStyleUpdated();
	}
	
	private void removeMe(){
		getEntryPoint().mainScreen.removeTabByTag(tag);
	}
	
	@Override 
	public boolean onKeyDown(int i, KeyEvent event) {

		  if (i == KeyEvent.KEYCODE_BACK) {
		    Toast.makeText(getEntryPoint(), getResources().getString(R.string.label_sorry_back_button), Toast.LENGTH_LONG).show();
		    return true; 
		  }

		  return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.menuitem_create:
	    	createAccount();
	    	return true;
	    case R.id.menuitem_cancel:
	    	removeMe();
	    	return true;	    
	    }
	    return false;
	}
	
	private void createAccount(){
		bu = new Bundle();
		
		AccountView localAccount = account;
		
		for (int i=0; i<customOptionsLayout.getChildCount(); i++){
			if (!customOptionsLayout.getChildAt(i).getTag().equals("")){
				EditText et = (EditText) customOptionsLayout.getChildAt(i);
				bu.putString((String) et.getTag(), et.getText().toString());
				
				if (localAccount == null){
					if (et.getTag().equals("uid") || et.getTag().equals("jid") || et.getTag().equals("mrid")){
						if (et.getText().toString().length()<1){
							Toast.makeText(getEntryPoint(), R.string.label_you_must_enter_uid, Toast.LENGTH_SHORT).show();
							return;
						} else {
							localAccount = new AccountView(et.getText().toString(), (String)protocolChooser.getSelectedItem());
							try {
								localAccount.serviceId = getEntryPoint().runtimeService.createAccount(localAccount);
								account = localAccount;
							} catch (NullPointerException npe) {	
								ServiceUtils.log(npe);
							} catch (RemoteException e) {
								Toast.makeText(getEntryPoint(), "Error creating account", 1000).show();
							}
						}
					}
				} 
			}
		}
		
		try {
			getEntryPoint().runtimeService.saveProtocolServiceOptions(localAccount.serviceId, bu);
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			ServiceUtils.log(e);
		}
		if (account!=null){
			removeMe();
		}
	}

	@Override
	public int getMainMenuId() {
		return R.menu.new_account_view_menu;
	}

	@Override
	public TabWidgetLayout getTabWidgetLayout() {
		TabWidgetLayout tabWidgetLayout = new TabWidgetLayout(getEntryPoint());
		
		tabWidgetLayout.setText(R.string.label_new_account);
		tabWidgetLayout.setImageResource(R.drawable.dark_asia_32);
		//tabWidgetLayout.setScaleType(ScaleType.FIT_XY);
		
		return tabWidgetLayout;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem createItem = menu.findItem(R.id.menuitem_create);
		if (account != null){
			createItem.setTitle(R.string.label_edit);
		} else {
			createItem.setTitle(R.string.label_create);
		}
		return false;
	}
	
	@Override
	public void visualStyleUpdated() {
		if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER){
			setBackgroundColor(0x60000000);
			for (int i=0; i<customOptionsLayout.getChildCount(); i++){
				if (customOptionsLayout.getChildAt(i).getTag().equals("")){
					TextView tv = (TextView) customOptionsLayout.getChildAt(i);
					tv.setTextColor(ColorStateList.valueOf(0xff000000));
				}
			}
			
		}else {
			try {
				int color = EntryPoint.bgColor;
				setBackgroundColor(0);
				for (int i=0; i<customOptionsLayout.getChildCount(); i++){
					if (customOptionsLayout.getChildAt(i).getTag().equals("")){
						TextView tv = (TextView) customOptionsLayout.getChildAt(i);
						tv.setTextColor(ColorStateList.valueOf((color-0xff000000)>0x777777?0xff000000:0xffffffff));
					}
				}
				
			} catch (NumberFormatException e) {				
				ServiceUtils.log(e);
			}
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
