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
import ua.snuk182.asia.view.EventableScrollView;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.cl.IContactListDrawer;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class ContactListListDrawer extends EventableScrollView implements IContactListDrawer {

	private List<ContactListListGroupItem> groups = new ArrayList<ContactListListGroupItem>();
	private ContactListListGroupItem unreadGroup;
	private ContactListListGroupItem offlineGroup;
	private ContactListListGroupItem notInListGroup;
	private ContactListListGroupItem onlineGroup;
	private ContactListListGroupItem chatsGroup;
	private ContactListListGroupItem noGroup;

	private LinearLayout contactList;
	protected ContactList parent;

	boolean showGroups = false;
	boolean showOffline = false;
	private boolean showIcons = true;

	private boolean clInited = false;
	private int oldWidth = 0;
	
	private final List<ContactListListItem> tmpItems = new ArrayList<ContactListListItem>();
	
	private final Runnable updateViewRunnable = new Runnable() {
		
		@Override
		public void run() {
			String showGroupsStr = parent.getAccount().options.getString(getResources().getString(R.string.key_show_groups));
			showGroups = showGroupsStr != null ? Boolean.parseBoolean(showGroupsStr) : false;

			String showOfflineStr = parent.getAccount().options.getString(getResources().getString(R.string.key_show_offline));
			showOffline = showOfflineStr != null ? Boolean.parseBoolean(showOfflineStr) : true;

			String showIconsStr = parent.account.options.getString(getResources().getString(R.string.key_show_icons));
			showIcons = showIconsStr != null ? Boolean.parseBoolean(showIconsStr) : true;

			String itemSizeStr = getEntryPoint().getApplicationOptions().getString(getResources().getString(R.string.key_cl_item_size));
			int size;
			if (itemSizeStr == null || itemSizeStr.equals(getResources().getString(R.string.value_size_medium))) {
				size = 48;
			} else if (itemSizeStr.equals(getResources().getString(R.string.value_size_big))) {
				size = 64;
			} else if (itemSizeStr.equals(getResources().getString(R.string.value_size_small))) {
				size = 32;
			} else {
				size = 24;
			}
			ContactListListItem.resize(size);

			for (ContactListListGroupItem group: groups){
				tmpItems.addAll(group.getBuddyList());
			}
			
			for (ContactListListItem item: tmpItems) {
				item.removeFromParent();
			}
			
			groups.clear();

			unreadGroup.getBuddyList().clear();
			offlineGroup.getBuddyList().clear();
			notInListGroup.getBuddyList().clear();
			chatsGroup.getBuddyList().clear();
			noGroup.getBuddyList().clear();
			onlineGroup.getBuddyList().clear();

			groups.add(unreadGroup);

			if (showGroups) {
				Collections.sort(parent.account.getBuddyGroupList());
				for (final BuddyGroup group : parent.account.getBuddyGroupList()) {
					ContactListListGroupItem clgroup = new ContactListListGroupItem(getEntryPoint(), null, contactList, group.id + "", group.name);
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
				ContactListListItem item = getItem(buddy, showIcons);
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
							ContactListListGroupItem group = groups.get(i);
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

			for (ContactListListGroupItem group : groups) {
				group.resize(size);
				group.color();
				group.forceRefresh();
			}

			clInited = true;
			parent.setClReady(true);

			tmpItems.clear();
			
			/*for (ContactListListGroupItem group : groups){
				for (ContactListListItem item: group.getBuddyList()){
					item.requestIcon(parent.account.getBuddyByFullUid(item.getTag().toString()), getScrollY(), getScrollY()+(getBottom()-getTop()));
				}
			}*/
			
			onScrollStoppedListener.onScrollStopped(getScrollY(), getScrollY()+(getBottom()-getTop()));
		}
	};

	public ContactListListDrawer(EntryPoint entryPoint, AccountView account, final ContactList parent) {
		super(entryPoint);

		this.parent = parent;

		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.contact_list_grid_drawer, this);

		contactList = (LinearLayout) findViewById(R.id.contactgrouplist);

		unreadGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_UNREAD, getResources().getString(R.string.label_unread_group));
		unreadGroup.setVisibility(View.GONE);
		
		offlineGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_OFFLINE, getResources().getString(R.string.label_offline_group));

		notInListGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_NOT_IN_LIST, getResources().getString(R.string.label_not_in_list_group));

		onlineGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_ONLINE, getResources().getString(R.string.label_online_group));

		chatsGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_CHATS, getResources().getString(R.string.label_chats_group));

		noGroup = new ContactListListGroupItem(entryPoint, null, contactList, ServiceConstants.VIEWGROUP_NOGROUP, getResources().getString(R.string.label_no_group));
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
		
		setOnScrollStoppedListener(new OnScrollStoppedListener() {
			
			@Override
			public void onScrollStopped(int frameTop, int frameBottom) {
				for (ContactListListGroupItem group : groups){
					for (ContactListListItem item : group.getBuddyList()){
						item.onDrawerScrolled(frameTop, frameBottom);
					}
				}
			}
		});
		
		setFocusable(false);
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		layout.weight = 0.1f;
		setLayoutParams(layout);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int width = getWidth();
		
		super.onLayout(changed, left, top, right, bottom);
		if ((width-oldWidth) != 0) {
			getEntryPoint().threadMsgHandler.post(new Runnable() {

				@Override
				public void run() {
					oldWidth = width;
					if (clInited) {
						for (ContactListListGroupItem group : groups) {
							group.refresh();
						}
					} else {
						updateView();
					}
				}

			});
		}
	}

	@Override
	public synchronized void updateView() {
		if (this.getWidth() < 1) {
			return;
		}

		parent.setClReady(false);

		getEntryPoint().threadMsgHandler.post(updateViewRunnable );
	}

	@Override
	public synchronized void messageReceived(TextMessage message) {
		ContactListListItem buddy = null;
		for (int i = 0; i < groups.size(); i++) {
			ContactListListGroupItem groupItem = groups.get(i);
			ContactListListItem bu = groupItem.removeItem(parent.account.getAccountId()+" "+message.from);
			if (bu != null) {
				buddy = bu;

				groupItem.setRefreshContents(true);

				break;
			}

		}

		if (buddy == null) {
			return;
		}

		unreadGroup.setVisibility(VISIBLE);
		unreadGroup.getBuddyList().add(buddy);
		unreadGroup.setRefreshContents(true);

		contactList.removeAllViews();
		for (ContactListListGroupItem item : groups) {
			item.refresh();
		}
	}

	@Override
	public synchronized void updateBuddyState(Buddy buddy) {
		ContactListListItem item = null;
		for (int i = 0; i < groups.size(); i++) {
			ContactListListGroupItem groupItem = groups.get(i);

			item = groupItem.removeItem(buddy.getFullUid());
			if (item != null) {
				groupItem.setRefreshContents(true);
				break;
			}
		}

		if (item == null){			
			if (!showOffline && buddy.status != Buddy.ST_OFFLINE){
				item = getItem(buddy, showIcons);
			} else {
				ServiceUtils.log("no item found - "+buddy.getFullUid());
				return;
			}
		}

		ContactListListGroupItem groupItem = null;
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

		groupItem = (ContactListListGroupItem) findViewWithTag(tag);

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

		for (ContactListListGroupItem groItem : groups) {
			groItem.refresh();
		}

		item.populate(buddy, getScrollY(), getScrollY()+(getBottom()-getTop()));
	}

	@Override
	public String getType() {
		return getContext().getResources().getString(R.string.value_list_type_list);
	}

	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	private ContactListListItem findExistingItem(String buddyUid) {
		for (ContactListListItem item: tmpItems){
			if (item.getTag().equals(buddyUid)){
				return item;
			}
		}
		
		return (ContactListListItem) this.findViewWithTag(buddyUid);
	}

	private ContactListListItem getItem(final Buddy buddy, boolean showIcons) {
		ContactListListItem item;
		int height = (int) (ContactListListItem.itemHeight * getEntryPoint().metrics.density);
		if ((item = findExistingItem(buddy.getFullUid())) == null) {
			final ContactListListItem cli = new ContactListListItem(getEntryPoint(), buddy.getFullUid(), this);
			cli.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					cli.setBackgroundColor(0xddff7f00);
					getEntryPoint().threadMsgHandler.post(new Runnable() {

						@Override
						public void run() {
							try {
								getEntryPoint().getConversationTab(getEntryPoint().runtimeService.getBuddy(buddy.serviceId, buddy.protocolUid));
								cli.setBackgroundColor(0x00ffffff);
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
					if (parent.account.getConnectionState() == AccountService.STATE_CONNECTED) {
						try {
							ViewUtils.contactMenu(getEntryPoint(), parent.account, getEntryPoint().runtimeService.getBuddy(buddy.serviceId, buddy.protocolUid));
						} catch (NullPointerException npe) {
							ServiceUtils.log(npe);
						} catch (RemoteException e) {
							getEntryPoint().onRemoteCallFailed(e);
						}
					}
					return false;
				}

			});

			item = cli;
		}
		item.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height));

		item.removeFromParent();
		item.populate(buddy, showIcons, getScrollY(), getScrollY()+(getBottom()-getTop()));
		//item.requestIcon(buddy);

		return item;
	}

	@Override
	public void bitmap(String uid) {
		ContactListListItem item = findExistingItem(uid);
		if (item != null) {
			item.requestIcon(parent.account.getBuddyByProtocolUid(uid), getScrollY(), getScrollY()+(getBottom()-getTop()));
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
