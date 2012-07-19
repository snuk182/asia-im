package ua.snuk182.asia.view.conversations;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants;
import ua.snuk182.asia.view.cl.ContactListItem;
import ua.snuk182.asia.view.cl.grid.ContactListGridItem;
import ua.snuk182.asia.view.cl.list.ContactListListItem;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationsViewParticipantsAdapter extends BaseExpandableListAdapter {
	
	private final EntryPoint entryPoint;
	private final MultiChatRoomOccupants occupants;
	
	boolean showIcons = true;
	
	public ConversationsViewParticipantsAdapter(EntryPoint entryPoint, MultiChatRoomOccupants occupants, boolean showIcons){
		super();
		this.entryPoint = entryPoint;
		this.occupants = occupants;
		
		this.showIcons = showIcons;
	}
	
	@Override
	public int getGroupCount() {
		return occupants.groups.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return occupants.groups.get(groupPosition).buddyList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return occupants.groups.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		int i = occupants.groups.get(groupPosition).buddyList.get(childPosition);
		for (Buddy bu: occupants.buddies){
			if (bu.id == i){
				return bu;
			}
		}
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return occupants.groups.get(groupPosition).id;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return occupants.groups.get(groupPosition).buddyList.get(childPosition).longValue();
	}

	@Override
	public boolean hasStableIds() {
		return false; //TODO check this
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		BuddyGroup group = (BuddyGroup) getGroup(groupPosition);
		
		TextView tv;
		
		if (convertView == null){
			tv = new TextView(entryPoint);
			tv.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));
			tv.setGravity(Gravity.RIGHT);
		} else {
			//parent.removeView(convertView);
			tv = (TextView) convertView;
		}
		
		tv.setTextSize(10);
		tv.setText(group.name+"\n"+group.buddyList.size());
		return tv;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Buddy buddy = (Buddy) getChild(groupPosition, childPosition);
		
		final ContactListItem cli;
		
		if (showIcons){
			if (convertView == null){
				convertView = new ContactListGridItem(entryPoint, null);
			} 
			((ContactListGridItem)convertView).setTag(buddy.protocolUid);
			((ContactListGridItem)convertView).populate(buddy, showIcons, new AbsListView.LayoutParams(ContactListGridItem.itemSize, ContactListGridItem.itemSize));
		} else {
			if (convertView == null){
				convertView = new ContactListListItem(entryPoint, null);
			} 
			((ContactListListItem)convertView).setTag(buddy.protocolUid);
			((ContactListListItem)convertView).populate(buddy, showIcons);
	        
		}	
		
		cli = (ContactListItem) convertView;
		
		cli.color();
 		cli.requestIcon(buddy);
 		
		return (View) cli;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	public void bitmap(String uid) {
		notifyDataSetInvalidated();
	}

	public void refreshOccupants(MultiChatRoomOccupants occupants) {
		this.occupants.buddies.clear();
		this.occupants.groups.clear();
		this.occupants.buddies.addAll(occupants.buddies);
		this.occupants.groups.addAll(occupants.groups);		
		notifyDataSetInvalidated();
	}
}
