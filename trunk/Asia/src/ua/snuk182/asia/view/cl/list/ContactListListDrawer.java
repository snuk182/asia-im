package ua.snuk182.asia.view.cl.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceConstants;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.cl.IContactListDrawer;
import android.content.Context;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ContactListListDrawer extends ScrollView implements IContactListDrawer{

	private List<ContactListListGroupItem> groups = new ArrayList<ContactListListGroupItem>();
	private ContactListListGroupItem unreadGroup;
	private ContactListListGroupItem offlineGroup;
	private ContactListListGroupItem notInListGroup;
	private ContactListListGroupItem onlineGroup;

	private String bgType;
	private LinearLayout contactList;
	
	private final AccountView account;

	boolean showGroups = false;
	boolean showOffline = false;
	private boolean showIcons = true;
	
	public ContactListListDrawer(EntryPoint entryPoint, AccountView account) {
		super(entryPoint);
		
		this.account = account;
		
		LayoutInflater inflate = (LayoutInflater)entryPoint.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.contact_list_grid_drawer, this);		
		
		contactList = (LinearLayout) findViewById(R.id.contactgrouplist);
		
		unreadGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_UNREAD, getResources().getString(R.string.label_unread_group));
		
		offlineGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_OFFLINE, getResources().getString(R.string.label_offline_group));
		
		notInListGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_NOT_IN_LIST, getResources().getString(R.string.label_not_in_list_group));
	
		onlineGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_ONLINE, getResources().getString(R.string.label_online_group));

		setFocusable(false);
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		layout.weight = 0.1f;
		setLayoutParams(layout);
	}

	@Override
	public synchronized void updateView(final ContactList parent) {
		String showGroupsStr = parent.getAccount().options.getString(getResources().getString(R.string.key_show_groups));
		showGroups = showGroupsStr!=null?Boolean.parseBoolean(showGroupsStr):false;
		
		String showOfflineStr = parent.getAccount().options.getString(getResources().getString(R.string.key_show_offline));
		showOffline = showOfflineStr!=null?Boolean.parseBoolean(showOfflineStr):true;
		
		String showIconsStr = account.options.getString(getResources().getString(R.string.key_show_icons));
		showIcons   = showIconsStr != null ? Boolean.parseBoolean(showIconsStr) : true;
		
		groups.clear();
		
		unreadGroup.getBuddyList().clear();
		offlineGroup.getBuddyList().clear();
		notInListGroup.getBuddyList().clear();
		onlineGroup.getBuddyList().clear();
		
		groups.add(unreadGroup);
		
		try {
			bgType = parent.getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_bg_type));
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} 
		
		if (showGroups && account.getBuddyGroupList().size() > 0){
			if (parent.sort){
				Collections.sort(account.getBuddyGroupList());
			}
			for (final BuddyGroup group: account.getBuddyGroupList()){
				ContactListListGroupItem clgroup = new ContactListListGroupItem(getEntryPoint(), null, contactList, group.id+"", group.name);
				clgroup.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View v) {
						if (parent.getAccount().getConnectionState() == AccountService.STATE_CONNECTED){
							ViewUtils.groupMenu(parent.getEntryPoint(), parent.getAccount(), group);
						}						
						return false;
					}}
				);
				clgroup.groupId = group.id;
				clgroup.serviceId = group.serviceId;
				clgroup.setCollapsed(group.isCollapsed);
				groups.add(clgroup);
			}
		} else {
			groups.add(onlineGroup);
		}
		
		for (Buddy buddy : account.getBuddyList()){
				ContactListListItem item = getItem(buddy, bgType, getEntryPoint(), showIcons);
				// items.add(item);

				if (showGroups && account.getBuddyGroupList().size() > 0) {
					if (buddy.unread > 0) {
						unreadGroup.getBuddyList().add(item);
					} else if (buddy.groupId == AccountService.NOT_IN_LIST_GROUP_ID) {
						notInListGroup.getBuddyList().add(item);
					} else if (buddy.status == Buddy.ST_OFFLINE) {
						if (showOffline) {
							offlineGroup.getBuddyList().add(item);
						}
					} else {
						for (int i = 1; i < groups.size(); i++) {
							ContactListListGroupItem group = groups.get(i);
							if (group.groupId == buddy.groupId) {
								group.getBuddyList().add(item);
							}
						}
					}

				} else {
					if (buddy.unread > 0) {
						unreadGroup.getBuddyList().add(item);
					} else if (buddy.groupId == AccountService.NOT_IN_LIST_GROUP_ID) {
						notInListGroup.getBuddyList().add(item);
					} else if (buddy.status == Buddy.ST_OFFLINE) {
						offlineGroup.getBuddyList().add(item);
					} else {
						onlineGroup.getBuddyList().add(item);
					}
				}
			}
		
		
		if (unreadGroup.getBuddyList().size()>0){
			unreadGroup.setVisibility(VISIBLE);
		}else {
			unreadGroup.setVisibility(GONE);
		}
		
		if (notInListGroup.getBuddyList().size()>0){
			groups.add(notInListGroup);
		}
		
		if (showOffline){
			groups.add(offlineGroup);
		}
		
		contactList.removeAllViews();
		
		for (ContactListListGroupItem group:groups){
			group.forceRefresh(bgType, parent.sort);
		}		
	}

	@Override
	public synchronized void messageReceived(TextMessage message) {
		ContactListListItem buddy = null;
		for (int i=0; i<groups.size(); i++){
			ContactListListGroupItem groupItem = groups.get(i);
			ContactListListItem bu = groupItem.removeItem(message.from);
			if (bu!=null){
				buddy = bu;
								
				groupItem.setRefreshContents(true);				
				
				break;
			}
			
		}
		
		if (buddy == null){
			return;
		}
		
		unreadGroup.setVisibility(VISIBLE);
		unreadGroup.getBuddyList().add(buddy);		
		unreadGroup.setRefreshContents(true);	
		
		contactList.removeAllViews();
		for (ContactListListGroupItem item:groups){
			item.refresh(bgType);
		}
		
		if (getEntryPoint().getTabHost().getCurrentTabTag().equals(ContactList.class.getSimpleName()+" "+account.serviceId)){
			try {
				Buddy budddy = getEntryPoint().runtimeService.getBuddy(account.serviceId, message.from);
				budddy.unread++;
				getEntryPoint().runtimeService.setUnread(budddy, message);
			} catch (NullPointerException npe) {	
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
		}
	}

	@Override
	public synchronized void updateBuddyState(Buddy buddy) {
		ContactListListItem item = null;
		for (int i = 0; i < groups.size(); i++) {
			ContactListListGroupItem groupItem = groups.get(i);

			item = groupItem.removeItem(buddy.protocolUid);
			if (item != null) {
				groupItem.setRefreshContents(true);
				break;
			}
		}
		
		if (item == null){
			ServiceUtils.log("no item found - "+buddy.protocolUid);
			return;
		}

		item.populate(buddy, bgType);
		
		ContactListListGroupItem groupItem = null;
		String tag = null;
		if (buddy.unread < 1){
			if (buddy.groupId == AccountService.NOT_IN_LIST_GROUP_ID){
				tag = ServiceConstants.VIEWGROUP_NOT_IN_LIST;
			} else {
				if (buddy.status == Buddy.ST_OFFLINE) {
					if (showOffline) {
						tag = ServiceConstants.VIEWGROUP_OFFLINE;
					}
				} else {
					if (showGroups && account.getBuddyGroupList().size() > 0) {
						tag = buddy.groupId + "";
					} else {
						tag = ServiceConstants.VIEWGROUP_ONLINE;
					}
				}
			}
		} else {
			tag = ServiceConstants.VIEWGROUP_UNREAD;
		}

		groupItem = (ContactListListGroupItem) findViewWithTag(tag);

		if (groupItem == null) {
			ServiceUtils.log("cannot find group in view " + buddy.groupId);
			return;
		}

		groupItem.getBuddyList().add(item);
		groupItem.setRefreshContents(true);

		if (unreadGroup.getBuddyList().size() < 1){
			unreadGroup.setVisibility(View.GONE);
		} else {
			unreadGroup.setVisibility(View.VISIBLE);
		}

		for (ContactListListGroupItem groItem:groups){
			groItem.refresh(bgType);
		}
		
	}

	@Override
	public String getType() {
		return getContext().getResources().getString(R.string.value_list_type_list);
	}
	
	public EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}
	
	private ContactListListItem findExistingItem(String buddyUid){
		return (ContactListListItem) this.findViewWithTag(buddyUid);
	}

	private ContactListListItem getItem(final Buddy buddy, String bgType, final EntryPoint entryPoint, boolean showIcons) {
		ContactListListItem item;
		if ((item = findExistingItem(buddy.protocolUid)) == null){			
			final ContactListListItem cli = new ContactListListItem(getEntryPoint(), buddy.protocolUid);
			cli.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, (int) (44 * getEntryPoint().metrics.density)));
			
			cli.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						entryPoint.getConversationTab(getEntryPoint().runtimeService.getBuddy(buddy.serviceId, buddy.protocolUid));
					} catch (NullPointerException npe) {	
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						getEntryPoint().onRemoteCallFailed(e);
					}
				}
			});
			cli.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (account.getConnectionState() == AccountService.STATE_CONNECTED) {
						try {
							ViewUtils.contactMenu(entryPoint, account, getEntryPoint().runtimeService.getBuddy(buddy.serviceId, buddy.protocolUid));
						} catch (NullPointerException npe) {	
							ServiceUtils.log(npe);
						} catch (RemoteException e) {
							getEntryPoint().onRemoteCallFailed(e);
						}
					}
					return false;
				}

			});
			
			cli.requestIcon(buddy);
			
			item = cli;
		}

		item.removeFromParent();
		item.populate(buddy, bgType, showIcons);
		return item;
	}

	@Override
	public void bitmap(String uid) {
		ContactListListItem item = findExistingItem(uid);
		try {
			if (item != null){
				item.requestIcon(getEntryPoint().runtimeService.getBuddy(account.serviceId, uid));
			}
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			getEntryPoint().onRemoteCallFailed(e);
		}
				
	}

	@Override
	public boolean hasUnreadMessages() {
		return unreadGroup.getVisibility() == View.VISIBLE;
	}
}
