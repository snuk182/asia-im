package ua.snuk182.asia.view.more;

import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.services.ServiceUtils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class AccountAdapter extends ArrayAdapter<AccountView> {

	private int textColor = 0;
	private AccountManagerView amView;

	public AccountAdapter(AccountManagerView accountManagerView, List<AccountView> objects) {
		super(accountManagerView.getContext(), 0, 0, objects);		
		amView = accountManagerView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final AccountView account = getItem(position);
		final ProtocolAccountView view;
		if (convertView == null){
			 view = new ProtocolAccountView(getContext(), null);
		} else {
			view = (ProtocolAccountView) convertView;
		}
		view.accountName.setText(account.protocolUid);
		view.accountName.setTextColor(0xff000000+textColor);
		
		view.accountTypeImg.setImageResource(ServiceUtils.getStatusResIdByAccountMedium(getEntryPoint(), account, true));
		
		if (account.protocolName.equals(getContext().getResources().getString(R.string.icq_service_name))){
			view.accountTypeImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icq_online_medium));
		}
		if (account.protocolName.equals(getContext().getResources().getString(R.string.xmpp_service_name))){
			view.accountTypeImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.xmpp_online_medium));
		}
		if (account.protocolName.equals(getContext().getResources().getString(R.string.mrim_service_name))){
			view.accountTypeImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mrim_online_medium));
		}
		
		view.accEnabledCb.setChecked(!Boolean.parseBoolean(account.options.getString(getEntryPoint().getString(R.string.key_disabled))));
		
		view.accEnabledCb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				
				int askId = view.accEnabledCb.isChecked() ? R.string.label_this_account_to_be_enabled : R.string.label_this_account_to_be_disabled;
				builder.setMessage(getContext().getResources().getString(askId)+account.protocolUid+"?")
				       .setCancelable(false)
				       .setPositiveButton(getContext().getResources().getString(R.string.label_yes), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   String key = getContext().getString(R.string.key_disabled);
								String value = Boolean.toString(!view.accEnabledCb.isChecked());
								account.options.putString(key, value);
								try {
									getEntryPoint().runtimeService.savePreference(key, value, account!=null ? account.serviceId : -1);
									getEntryPoint().refreshAccounts();
									getEntryPoint().addAccountsManagerTab();
								} catch (NullPointerException npe) {	
									ServiceUtils.log(npe);
								} catch (RemoteException e) {
									getEntryPoint().onRemoteCallFailed(e);
								}
				           }
				       })
				       .setNegativeButton(getContext().getResources().getString(R.string.label_no), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				                view.accEnabledCb.setChecked(!view.accEnabledCb.isChecked());
				           }
				       });
				AlertDialog dialog = builder.create();
				dialog.show();				
			}
		});
		
		view.editBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				getEntryPoint().addAccountEditorTab(account);
			}}
		);
		
		view.removeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setMessage(getContext().getResources().getString(R.string.label_are_you_sure_you_want_to_remove)+account.protocolUid+"?")
				       .setCancelable(false)
				       .setPositiveButton(getContext().getResources().getString(R.string.label_yes), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   try {
									getEntryPoint().runtimeService.deleteAccount(account);
									remove(account);
								} catch (NullPointerException npe) {	
									ServiceUtils.log(npe);
								} catch (RemoteException e) {
									getEntryPoint().onRemoteCallFailed(e);
								}
				           }
				       })
				       .setNegativeButton(getContext().getResources().getString(R.string.label_no), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			
		});
		
		return view;
	}

	public int getTextColor() {
		return textColor  ;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}
	
	private EntryPoint getEntryPoint(){
		return (EntryPoint) amView.getContext();
	}
}
