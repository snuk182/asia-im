package ua.snuk182.asia.view.conversations;

import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.cl.grid.ContactListGridItem;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ConversationsViewParticipantsAdapter extends ArrayAdapter<Buddy> {
	
	public ConversationsViewParticipantsAdapter(EntryPoint context, List<Buddy> objects){
		this(context, 0, 0, objects);
	}

	public ConversationsViewParticipantsAdapter(Context context, int resource,
			int textViewResourceId, List<Buddy> objects) {
		super(context, resource, textViewResourceId, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final Buddy buddy = getItem(position);
		String showIconsStr = null;
		try {
			showIconsStr = getEntryPoint().runtimeService.getAccountView(buddy.serviceId).options.getString(getEntryPoint().getResources().getString(R.string.key_show_icons));
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			getEntryPoint().onRemoteCallFailed(e);
		} catch (NotFoundException e1) {
			ServiceUtils.log(e1);
		}
		boolean showIcons  = showIconsStr != null ? Boolean.parseBoolean(showIconsStr) : true;
		
		if (convertView == null){
			final ContactListGridItem cli = new ContactListGridItem((EntryPoint) getContext(), null);
			convertView = cli;
		}
		
		final ContactListGridItem cli = (ContactListGridItem) convertView;
		
		cli.populate(buddy, 50, showIcons);
        
        return cli;
	}
	
	private EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}
}
