package ua.snuk182.asia.view.cl.grid;

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
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ContactListGridDrawer extends ScrollView implements IContactListDrawer {

	private static int COLUMN_COUNT = 0;

	private LinearLayout contactList;

	private List<ContactListGridGroupItem> groups = new ArrayList<ContactListGridGroupItem>();
	private ContactListGridGroupItem unreadGroup;
	private ContactListGridGroupItem offlineGroup;
	private ContactListGridGroupItem notInListGroup;
	private ContactListGridGroupItem onlineGroup;
	private ContactListGridGroupItem chatsGroup;
	private ContactListGridGroupItem noGroup;

	boolean showGroups = false;
	boolean showOffline = false;
	private boolean showIcons = true;

	protected ContactList parent;
	
	private boolean clInited = false;
	private int oldWidth = 0;
	
	private final List<ContactListGridItem> tmpItems = new ArrayList<ContactListGridItem>();
	
	private final Runnable updateViewRunnable = new Runnable() {
		
		@Override
		public void run() {
			String showGroupsStr = parent.account.options.getString(getResources().getString(R.string.key_show_groups));
			showGroups = showGroupsStr != null ? Boolean.parseBoolean(showGroupsStr) : false;

			String showOfflineStr = parent.account.options.getString(getResources().getString(R.string.key_show_offline));
			showOffline = showOfflineStr != null ? Boolean.parseBoolean(showOfflineStr) : true;

			String showIconsStr = parent.account.options.getString(getResources().getString(R.string.key_show_icons));
			showIcons = showIconsStr != null ? Boolean.parseBoolean(showIconsStr) : true;
			
			String itemSizeStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_cl_item_size));
			final int size;
			if (itemSizeStr == null || itemSizeStr.equals(getResources().getString(R.string.value_size_medium))){
				size = 75;
			} else if (itemSizeStr.equals(getResources().getString(R.string.value_size_big))){
				size = 96;
			} else if (itemSizeStr.equals(getResources().getString(R.string.value_size_small))){
				size = 62;
			} else {
				size = 48;
			}
			ContactListGridItem.resize(size, getEntryPoint());
			
			COLUMN_COUNT = ContactListGridDrawer.this.getWidth() / ContactListGridItem.itemSize;
			
			for (ContactListGridGroupItem group: groups){
				tmpItems.addAll(group.getBuddyList());
			}
			
			for (ContactListGridItem item: tmpItems) {
				item.removeFromParent();
			}
			
			groups.clear();			

			unreadGroup.getBuddyList().clear();
			offlineGroup.getBuddyList().clear();
			onlineGroup.getBuddyList().clear();
			notInListGroup.getBuddyList().clear();
			chatsGroup.getBuddyList().clear();
			noGroup.getBuddyList().clear();
			groups.add(unreadGroup);

			// Display display = ((WindowManager)
			// getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

			/*
			 * int itemSize = (int) (75 * getEntryPoint().metrics.density);
			 * COLUMN_COUNT = this.getWidth() / itemSize;
			 */

			if (showGroups) {
				Collections.sort(parent.account.getBuddyGroupList());
				for (final BuddyGroup group : parent.account.getBuddyGroupList()) {
					ContactListGridGroupItem clgroup = new ContactListGridGroupItem(getEntryPoint(), null, contactList, group.id + "", group.name);
					clgroup.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View v) {
							try {
								AccountView myacc = getEntryPoint().runtimeService.getAccountView(parent.account.serviceId);
								if (myacc.getConnectionState() == AccountService.STATE_CONNECTED) {
									ViewUtils.groupMenu(getEntryPoint(), parent.account, group);
								}
							} catch (NullPointerException npe) {
								ServiceUtils.log(npe);
							} catch (RemoteException e) {
								getEntryPoint().onRemoteCallFailed(e);
							}
							return false;
						}
					});

					clgroup.groupId = group.id;
					clgroup.serviceId = group.serviceId;
					clgroup.setCollapsed(group.isCollapsed);
					groups.add(clgroup);
				}
			} else {
				groups.add(onlineGroup);
			}

			for (Buddy buddy : parent.account.getBuddyList()) {
				ContactListGridItem item = getItem(buddy, showIcons);
				// items.add(item);
				item.color();
				
				if (showGroups) {
					if (buddy.unread > 0) {
						unreadGroup.getBuddyList().add(item);
					} else if (buddy.groupId == AccountService.NOT_IN_LIST_GROUP_ID) {
						notInListGroup.getBuddyList().add(item);
					} else if (buddy.visibility == Buddy.VIS_GROUPCHAT) {
						chatsGroup.getBuddyList().add(item);
					} else if (buddy.status == Buddy.ST_OFFLINE && !showOffline) {
						continue;
					} else if (buddy.groupId == AccountService.NO_GROUP_ID) {
						noGroup.getBuddyList().add(item);
					} else {
						for (int i = 1; i < groups.size(); i++) {
							ContactListGridGroupItem group = groups.get(i);
							if (group.groupId == buddy.groupId) {
								group.getBuddyList().add(item);
								break;
							}
						}
					}					

				} else {
					if (buddy.unread > 0) {
						unreadGroup.getBuddyList().add(item);
					} else if (buddy.groupId == AccountService.NOT_IN_LIST_GROUP_ID) {
						notInListGroup.getBuddyList().add(item);
					} else if (buddy.visibility == Buddy.VIS_GROUPCHAT) {
						chatsGroup.getBuddyList().add(item);
					} else if (buddy.status == Buddy.ST_OFFLINE) {
						offlineGroup.getBuddyList().add(item);
					} else {
						onlineGroup.getBuddyList().add(item);
					}
				}
			}

			if (unreadGroup.getBuddyList().size() > 0) {
				unreadGroup.setVisibility(VISIBLE);
			} else {
				unreadGroup.setVisibility(GONE);
			}

			if (notInListGroup.getBuddyList().size() > 0) {
				groups.add(notInListGroup);
			}
			
			if (chatsGroup.getBuddyList().size() > 0) {
				groups.add(chatsGroup);
			}
			
			if (noGroup.getBuddyList().size() > 0) {
				groups.add(noGroup);
			}

			if (!showGroups && showOffline) {
				groups.add(offlineGroup);
			}

			contactList.removeAllViews();

			for (ContactListGridGroupItem group : groups) {
				group.resize(size);
				group.color();
				group.refresh(COLUMN_COUNT);
			}
			
			clInited = true;
			parent.setClReady(true);
			
			tmpItems.clear();
			
			for (ContactListGridGroupItem group : groups){
				for (ContactListGridItem item: group.getBuddyList()){
					item.requestIcon(parent.account.getBuddyByProtocolUid(item.getTag().toString()));
				}
			}
		}
	};
	
	public ContactListGridDrawer(EntryPoint entryPoint, AccountView account, final ContactList parent) {
		super(entryPoint);

		this.parent = parent;

		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.contact_list_grid_drawer, this);

		contactList = (LinearLayout) findViewById(R.id.contactgrouplist);

		unreadGroup = new ContactListGridGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_UNREAD, getResources().getString(R.string.label_unread_group));
		unreadGroup.setVisibility(View.GONE);
		
		offlineGroup = new ContactListGridGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_OFFLINE, getResources().getString(R.string.label_offline_group));

		notInListGroup = new ContactListGridGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_NOT_IN_LIST, getResources().getString(R.string.label_not_in_list_group));

		onlineGroup = new ContactListGridGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_ONLINE, getResources().getString(R.string.label_online_group));
		
		chatsGroup = new ContactListGridGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_CHATS, getResources().getString(R.string.label_chats_group));

		noGroup = new ContactListGridGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_NOGROUP, getResources().getString(R.string.label_no_group));
		noGroup.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				try {
					AccountView myacc = getEntryPoint().runtimeService.getAccountView(parent.account.serviceId);
					if (myacc.getConnectionState() == AccountService.STATE_CONNECTED) {
						ViewUtils.groupMenu(getEntryPoint(), parent.account, null);
					}
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					getEntryPoint().onRemoteCallFailed(e);
				}
				return false;
			}
		});
		
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		layout.weight = 0.1f;
		setLayoutParams(layout);
		setFocusable(false);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int width = getWidth();
		//System.out.println("exist "+width);
		//boolean diff = getWidth() != width; 
		super.onLayout(changed, left, top, right, bottom);
		if ((width-oldWidth) != 0) {
			getEntryPoint().threadMsgHandler.post(new Runnable() {
				
				@Override
				public void run() {
					oldWidth = width;
					if (clInited && COLUMN_COUNT > 0){
						int newColumnCount = width / ContactListGridItem.itemSize;
						//if (COLUMN_COUNT != newColumnCount){
							COLUMN_COUNT = newColumnCount;
							for (ContactListGridGroupItem group : groups) {
								group.refresh(COLUMN_COUNT);
							}
						//} 
					} else {
						updateView();
					}
				}
			});
		}		
	}

	@Override
	public synchronized void updateView() {
		if (this.getWidth() < 1){
			return;
		}
		
		parent.setClReady(false);
		getEntryPoint().threadMsgHandler.post(updateViewRunnable);
	}

	@Override
	public void messageReceived(TextMessage message) {
		ContactListGridItem buddy = null;
		for (int i = 0; i < groups.size(); i++) {
			ContactListGridGroupItem groupItem = groups.get(i);
			ContactListGridItem bu = groupItem.removeItem(message.from);
			if (bu != null) {
				// if (buddy == null) {
				buddy = bu;
				// }

				groupItem.setRefreshContents(true);

				break;
			}

		}

		if (buddy == null) {
			return;
		}

		unreadGroup.setVisibility(View.VISIBLE);
		unreadGroup.getBuddyList().add(buddy);
		unreadGroup.setRefreshContents(true);

		contactList.removeAllViews();
		for (ContactListGridGroupItem item : groups) {
			item.refresh();
		}

		/*String tag = ConversationsView.class.getSimpleName()+" "+message.serviceId+" "+message.from;
		if (getEntryPoint().mainScreen.getCurrentChatsTabTag().equals(tag)) {
			try {
				Buddy budddy = getEntryPoint().runtimeService.getBuddy(parent.account.serviceId, message.from);
				budddy.unread++;
				getEntryPoint().runtimeService.setUnread(budddy, message);
			} catch (NullPointerException npe) {
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
		}*/
	}

	@Override
	public synchronized void updateBuddyState(Buddy buddy) {
		ContactListGridItem item = null;
		for (int i = 0; i < groups.size(); i++) {
			ContactListGridGroupItem groupItem = groups.get(i);

			item = groupItem.removeItem(buddy.protocolUid);
			if (item != null) {
				groupItem.setRefreshContents(true);
				break;
			}
		}

		if (item == null){			
			if (!showOffline && buddy.status != Buddy.ST_OFFLINE){
				item = getItem(buddy, showIcons);
			} else {
				ServiceUtils.log("no item found - "+buddy.protocolUid);
				return;
			}
		}

		item.populate(buddy);

		ContactListGridGroupItem groupItem = null;
		String tag = null;
		if (buddy.unread < 1) {
			if (buddy.groupId == AccountService.NOT_IN_LIST_GROUP_ID) {
				tag = ServiceConstants.VIEWGROUP_NOT_IN_LIST;
			} else if (buddy.visibility == Buddy.VIS_GROUPCHAT) {
				tag = ServiceConstants.VIEWGROUP_CHATS;
			} else if (showGroups && buddy.groupId == AccountService.NO_GROUP_ID) {
				tag = ServiceConstants.VIEWGROUP_NOGROUP;
			} else {
				if (buddy.status == Buddy.ST_OFFLINE && !showGroups && showOffline) {
					tag = ServiceConstants.VIEWGROUP_OFFLINE;
				} else {
					if (showGroups) {
						tag = buddy.groupId + "";
					} else {
						tag = ServiceConstants.VIEWGROUP_ONLINE;
					}
				}
			}
		} else {
			tag = ServiceConstants.VIEWGROUP_UNREAD;
		}

		groupItem = (ContactListGridGroupItem) findViewWithTag(tag);

		if (groupItem == null) {
			ServiceUtils.log("cannot find group in view " + buddy.groupId);
			return;
		}

		groupItem.getBuddyList().add(item);
		groupItem.setRefreshContents(true);

		if (unreadGroup.getBuddyList().size() < 1) {
			unreadGroup.setVisibility(View.GONE);
		} else {
			unreadGroup.setVisibility(View.VISIBLE);
		}

		for (ContactListGridGroupItem groItem : groups) {
			groItem.refresh();
		}
	}

	@Override
	public String getType() {
		return getContext().getResources().getString(R.string.value_list_type_grid);
	}

	private ContactListGridItem findExistingItem(String buddyUid) {
		if (buddyUid == null || buddyUid.length() < 1) {
			return null;
		}
		for (ContactListGridItem item: tmpItems){
			if (item.getTag().equals(buddyUid)){
				return item;
			}
		}
		
		View view = this.findViewWithTag(buddyUid);
		return (ContactListGridItem) view;
	}

	private ContactListGridItem getItem(final Buddy buddy, boolean showIcons) {
		ContactListGridItem item;
		if ((item = findExistingItem(buddy.protocolUid)) == null) {
			final ContactListGridItem cli = new ContactListGridItem(getEntryPoint(), buddy.protocolUid);
			cli.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					cli.buddyImage.onFocusChange(null, true);
					getEntryPoint().threadMsgHandler.post(new Runnable() {
						
						@Override
						public void run() {
							try {
								getEntryPoint().getConversationTab(getEntryPoint().runtimeService.getBuddy(buddy.serviceId, buddy.protocolUid));
								cli.buddyImage.onFocusChange(null, false);
							} catch (NullPointerException npe) {
								ServiceUtils.log(npe);
							} catch (RemoteException e) {
								getEntryPoint().onRemoteCallFailed(e);
							}
						}
					});
				}
			});
			cli.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					getEntryPoint().threadMsgHandler.post(new Runnable() {
						
						@Override
						public void run() {
							if (parent.account.getConnectionState() == AccountService.STATE_CONNECTED) {
								try {
									ViewUtils.contactMenu(getEntryPoint(), parent.account, getEntryPoint().runtimeService.getBuddy(buddy.serviceId, buddy.protocolUid));
								} catch (NullPointerException npe) {
									ServiceUtils.log(npe);
								} catch (RemoteException e) {
									getEntryPoint().onRemoteCallFailed(e);
								}
							}
						}
					});
					return true;
				}

			});

			item = cli;
		}

		item.removeFromParent();
		item.populate(buddy, showIcons);
		//item.requestIcon(buddy);
		
		return item;
	}

	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public void bitmap(String uid) {
		ContactListGridItem item = findExistingItem(uid);
		if (item != null) {
			try {
				item.requestIcon(getEntryPoint().runtimeService.getBuddy(parent.account.serviceId, uid));
			} catch (NullPointerException npe) {
				ServiceUtils.log(npe);
			} catch (RemoteException e) {
				getEntryPoint().onRemoteCallFailed(e);
			}
		}
	}

	@Override
	public boolean hasUnreadMessages() {
		return unreadGroup.getVisibility() == View.VISIBLE;
	}

	@Override
	public void configChanged() {
		
	}
}
