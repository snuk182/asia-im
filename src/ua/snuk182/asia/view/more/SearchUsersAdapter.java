package ua.snuk182.asia.view.more;

import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.view.ViewUtils;
import android.content.Context;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class SearchUsersAdapter extends ArrayAdapter<PersonalInfo> {
	
	private byte serviceId = -1;
	private int textColor = 0;
	
	private SearchUsersAdapter(Context context, List<PersonalInfo> objects){
		super(context, 0, 0, objects);
	}

	public SearchUsersAdapter(EntryPoint entryPoint, byte serviceId, List<PersonalInfo> objects) {
		super(entryPoint, 0, 0, objects);
		this.serviceId = serviceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final PersonalInfo buddy = getItem(position);
		final SearchUsersResultItem cli;
		
		if (convertView == null){
			cli = new SearchUsersResultItem(getContext(), null);
		} else {
			cli = (SearchUsersResultItem) convertView;
		}
		
		cli.populate(buddy);
		cli.infoLabel.setTextColor(0xff000000+textColor);
		cli.userNameLabel.setTextColor(0xff000000+textColor);
		cli.addButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final EntryPoint entryPoint = (EntryPoint)getContext();
				Buddy newBuddy;
				try {
					AccountView account = entryPoint.runtimeService.getAccountView(serviceId);
					newBuddy = new Buddy(buddy.protocolUid, account);
					newBuddy.name = buddy.properties.getString(PersonalInfo.INFO_NICK);
					newBuddy.groupId = AccountService.NOT_IN_LIST_GROUP_ID;
					
					ViewUtils.showAddBuddyDialog(account, newBuddy, entryPoint);
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
			}
			
		});
        
        return cli;
	}
	
	public int getTextColor() {
		return textColor ;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}
}

