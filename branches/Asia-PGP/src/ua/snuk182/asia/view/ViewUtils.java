package ua.snuk182.asia.view;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.MultiChatRoom;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.icq.ICQServiceUtils;
import ua.snuk182.asia.services.mrim.MrimServiceUtils;
import ua.snuk182.asia.services.plus.IconMenuAdapter;
import ua.snuk182.asia.services.xmpp.XMPPServiceUtils;
import ua.snuk182.asia.view.more.fileexplorer.FileExplorer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.text.ClipboardManager;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public final class ViewUtils {
	public static final VMRuntimeHack VMRUNTIME = new VMRuntimeHack();

	private static final Map<String, IconMenuAdapter> iconMenuAdapters = new HashMap<String, IconMenuAdapter>();

	private static TabsAdapter tabsAdapter = null;

	private static TabsAdapter getTabsAdapter(EntryPoint entryPoint) {
		if (tabsAdapter == null || tabsAdapter.getEntryPoint() != entryPoint) {
			tabsAdapter = new TabsAdapter(entryPoint);
		}
		return tabsAdapter;
	}

	protected static IconMenuAdapter getStatusIconMenuAdapterByTarget(String target, EntryPoint context) {
		IconMenuAdapter ica = iconMenuAdapters.get(target + "-status");

		if (ica == null) {
			if (target.equals(context.getResources().getString(R.string.icq_service_name))) {
				ica = new IconMenuAdapter(context, ICQServiceUtils.getStatusListNames(context), ICQServiceUtils.getStatusResIds(context));
			}
			if (target.equals(context.getResources().getString(R.string.xmpp_service_name))) {
				ica = new IconMenuAdapter(context, XMPPServiceUtils.getStatusListNames(context), XMPPServiceUtils.getStatusResIds(context));
			}
			if (target.equals(context.getResources().getString(R.string.mrim_service_name))) {
				ica = new IconMenuAdapter(context, MrimServiceUtils.getStatusListNames(context), MrimServiceUtils.getStatusResIds(context));
			}
			iconMenuAdapters.put(target, ica);
		}

		return ica;
	}

	protected static IconMenuAdapter getXStatusIconMenuAdapterByTarget(String target, EntryPoint context) {
		IconMenuAdapter ica = iconMenuAdapters.get(target + "-xst");

		if (ica == null) {
			if (target.equals(context.getResources().getString(R.string.icq_service_name))) {
				ica = new IconMenuAdapter(context, context.getResources().getStringArray(R.array.icq_xstatus_descr), context.getResources().obtainTypedArray(R.array.icq_xstatus_names_32));
			}
			if (target.equals(context.getResources().getString(R.string.mrim_service_name))) {
				ica = new IconMenuAdapter(context, context.getResources().getStringArray(R.array.mrim_xstatus_descr), context.getResources().obtainTypedArray(R.array.mrim_xstatus_names_32));
			}
			iconMenuAdapters.put(target, ica);
		}

		return ica;
	}

	public static void groupMenu(final EntryPoint entryPoint, final AccountView account, final BuddyGroup group) {
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(entryPoint.getResources().getString(R.string.label_group_menu) + " : " + group.name);
		final TypedArray values = entryPoint.getResources().obtainTypedArray(R.array.icq_group_menu_names);
		builder.setItems(R.array.icq_group_menu_names, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = values.getString(which);
				if (value.equals(entryPoint.getResources().getString(R.string.menu_value_rename))) {
					showGroupRenameDialog(group, entryPoint);
				}
				if (value.equals(entryPoint.getResources().getString(R.string.menu_value_add_group))) {
					showAddGroupDialog(account, entryPoint);
				}
				if (value.equals(entryPoint.getResources().getString(R.string.menu_value_delete_group))) {
					if (account.getBuddyGroupList().size() > 1) {
						showRemoveGroupDialog(account, group, entryPoint);
					}
				}
			}
		});
		builder.create().show();
	}
	
	private static final int getContactMenuResIdByAccount(Context context, AccountView account){
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))){
			return R.array.icq_contact_menu_names;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))){
			return R.array.xmpp_contact_menu_names;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))){
			return R.array.mrim_contact_menu_names;
		}
		return 0;
	}

	public static void contactMenu(final EntryPoint entryPoint, final AccountView account, final Buddy buddy) {
		int menuResId = getContactMenuResIdByAccount(entryPoint, account);
		
		if (menuResId == 0){
			return;
		}
		
		TypedArray items = entryPoint.getResources().obtainTypedArray(menuResId);

		if (items.length() < 1){
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(entryPoint.getResources().getString(R.string.label_contact_menu) + ": " + buddy.getName() + " (" + buddy.protocolUid + ")");
		final Resources res = entryPoint.getResources();
		final List<String> itemList = new ArrayList<String>(items.length());
		for (int i = 0; i < items.length(); i++) {
			String item = items.getString(i);
			/*
			 * if (value.equals(entryPoint.getResources().getString(R.string.
			 * menu_value_view_info))) { continue; }
			 */
			if (item.equals(res.getString(R.string.menu_value_add_to_list))) {
				if (buddy.groupId != AccountService.NOT_IN_LIST_GROUP_ID) {
					continue;
				}
			}
			if (item.equals(res.getString(R.string.menu_value_ask_auth))) {
				if (buddy.visibility != Buddy.VIS_NOT_AUTHORIZED) {
					continue;
				}
			}
			if (item.equals(res.getString(R.string.menu_value_to_permits))) {
				if (buddy.visibility == Buddy.VIS_NOT_AUTHORIZED || buddy.visibility == Buddy.VIS_PERMITTED) {
					continue;
				}
			}
			if (item.equals(res.getString(R.string.menu_value_to_denys))) {
				if (buddy.visibility == Buddy.VIS_NOT_AUTHORIZED || buddy.visibility == Buddy.VIS_DENIED) {
					continue;
				}
			}
			if (item.equals(res.getString(R.string.menu_value_from_permits))) {
				if (buddy.visibility != Buddy.VIS_PERMITTED) {
					continue;
				}
			}
			if (item.equals(res.getString(R.string.menu_value_from_denys))) {
				if (buddy.visibility != Buddy.VIS_DENIED) {
					continue;
				}
			}
			if (item.equals(res.getString(R.string.menu_value_join_chat))){
				if (buddy.visibility != Buddy.VIS_GROUPCHAT || buddy.status != Buddy.ST_OFFLINE){
					continue;
				}
			}
			if (item.equals(res.getString(R.string.menu_value_leave_chat))){
				if (buddy.visibility != Buddy.VIS_GROUPCHAT || buddy.status == Buddy.ST_OFFLINE){
					continue;
				}
			}
			if (item.equals(res.getString(R.string.label_edit))){
				
			}
			itemList.add(item);
		}

		items.recycle();
		
		String[] itemArray = itemList.toArray(new String[itemList.size()]);

		builder.setItems(itemArray, new OnClickListener() {

			@Override
			public void onClick(final DialogInterface bdialog, int which) {
				String command = itemList.get(which);
				if (command.equals(res.getString(R.string.menu_value_add_to_list))) {
					if (account.getConnectionState() == AccountService.STATE_CONNECTED) {
						showAddBuddyDialog(account, buddy, entryPoint);
					} else {
						Toast.makeText(entryPoint, entryPoint.getString(R.string.label_you_must_enter_uid), Toast.LENGTH_LONG).show();
					}
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_delete_contact)) || command.equals(res.getString(R.string.label_remove))) {
					showRemoveBuddyDialog(buddy, entryPoint);
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_view_info))) {
					try {
						entryPoint.runtimeService.requestBuddyFullInfo(buddy.serviceId, buddy.protocolUid);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_ask_auth))) {
					final Dialog dialog = new Dialog(entryPoint);

					dialog.setContentView(R.layout.ask_authorize);
					dialog.setTitle(R.string.label_auth_request);

					final Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
					final EditText reasonEdit = (EditText) dialog.findViewById(R.id.askAuthText);
					okBtn.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								entryPoint.runtimeService.requestAuthorization(buddy, reasonEdit.getText().toString());
								dialog.dismiss();
							} catch (NullPointerException npe) {
								ServiceUtils.log(npe);
							} catch (RemoteException e) {
								entryPoint.onRemoteCallFailed(e);
							}

						}

					});
					dialog.show();
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_rename))) {
					showBuddyRenameDialog(buddy, entryPoint);
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_move))) {
					showBuddyMoveDialog(account, buddy, entryPoint);
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_to_permits))) {
					buddy.visibility = Buddy.VIS_PERMITTED;
					try {
						entryPoint.runtimeService.editBuddyVisibility(buddy);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_to_denys))) {
					buddy.visibility = Buddy.VIS_DENIED;
					try {
						entryPoint.runtimeService.editBuddyVisibility(buddy);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_from_permits)) || command.equals(res.getString(R.string.menu_value_from_denys))) {
					buddy.visibility = Buddy.VIS_REGULAR;
					try {
						entryPoint.runtimeService.editBuddyVisibility(buddy);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
					return;
				}
				if (command.equals(res.getString(R.string.menu_value_join_chat))){
					try {
						entryPoint.runtimeService.joinExistingChat(buddy.serviceId, buddy.protocolUid);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
				}
				if (command.equals(res.getString(R.string.menu_value_leave_chat))){
					try {
						entryPoint.runtimeService.leaveChat(buddy.serviceId, buddy.protocolUid);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
				}
				if (command.equals(res.getString(R.string.label_edit))){
					showManualGroupChatOptions(entryPoint, account, buddy);
				}
				
			}
		});
		builder.create().show();
	}

	protected static void showRemoveBuddyDialog(final Buddy buddy, final EntryPoint entryPoint) {
		AlertDialog.Builder newBuilder = new AlertDialog.Builder(entryPoint);
		newBuilder.setMessage(entryPoint.getResources().getString(R.string.label_are_you_sure_you_want_to_remove) + buddy.protocolUid + "?").setCancelable(false).setPositiveButton(R.string.label_yes, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				entryPoint.removeBuddy(buddy);
			}

		}).setNegativeButton(R.string.label_no, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}

		});
		newBuilder.create().show();

	}

	private static void showRemoveGroupDialog(final AccountView account, final BuddyGroup group, final EntryPoint entryPoint) {
		AlertDialog.Builder newBuilder = new AlertDialog.Builder(entryPoint);
		newBuilder.setMessage(entryPoint.getResources().getString(R.string.label_are_you_sure_you_want_to_remove) + group.name + "?").setCancelable(false).setPositiveButton(R.string.label_yes, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (group.buddyList.size() > 0) {
					List<Buddy> buddies;
					try {
						buddies = entryPoint.runtimeService.getBuddiesFromGroup(group);
						showInemptyGroupDialog(account, group, buddies, entryPoint);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
				} else {
					try {
						entryPoint.runtimeService.removeGroup(group, null, null);
					} catch (NullPointerException npe) {
					} catch (RemoteException e) {
						ServiceUtils.log(e);
					}
				}
			}

		}).setNegativeButton(R.string.label_no, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}

		});
		newBuilder.create().show();

	}

	private static void showInemptyGroupDialog(final AccountView account, final BuddyGroup group, final List<Buddy> buddies, final EntryPoint entryPoint) {
		AlertDialog.Builder newBuilder = new AlertDialog.Builder(entryPoint);
		StringBuilder sb = new StringBuilder();
		for (Buddy buddy : buddies) {
			sb.append("\n");
			sb.append(buddy.name);
		}
		newBuilder.setMessage(entryPoint.getResources().getString(R.string.label_inempty_group) + "\n" + sb.toString()).setCancelable(false).setPositiveButton(R.string.label_move, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				showMoveBuddiesDialog(account, group, buddies, entryPoint);
				dialog.cancel();
			}

		}).setNegativeButton(R.string.label_remove, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					entryPoint.runtimeService.removeGroup(group, buddies, null);
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
				dialog.cancel();
			}

		}).setNeutralButton(R.string.label_cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

			}

		});
		newBuilder.create().show();

	}

	private static void showMoveBuddiesDialog(final AccountView account, final BuddyGroup group, final List<Buddy> buddies, final EntryPoint entryPoint) {
		final Dialog dialog = new Dialog(entryPoint);

		dialog.setContentView(R.layout.move_buddy);
		dialog.setTitle(R.string.label_move_buddy);

		final Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		final Spinner groupSpinner = (Spinner) dialog.findViewById(R.id.buddy_group);

		ArrayAdapter<BuddyGroup> adapter;
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>();
		groups.addAll(account.getBuddyGroupList());
		for (int i = groups.size() - 1; i >= 0; i--) {
			if (groups.get(i).id == group.id) {
				groups.remove(i);
				break;
			}
		}
		adapter = new ArrayAdapter<BuddyGroup>(entryPoint, android.R.layout.simple_spinner_dropdown_item, groups);
		groupSpinner.setAdapter(adapter);
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					BuddyGroup newGroup = (BuddyGroup) groupSpinner.getSelectedItem();
					entryPoint.runtimeService.removeGroup(group, buddies, newGroup);
					dialog.dismiss();
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}

			}
		});
		dialog.show();
	}

	private static final void showBuddyMoveDialog(final AccountView account, final Buddy buddy, final EntryPoint entryPoint) {
		final Dialog dialog = new Dialog(entryPoint);

		dialog.setContentView(R.layout.move_buddy);
		dialog.setTitle(R.string.label_move_buddy);

		final Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		final Spinner groupSpinner = (Spinner) dialog.findViewById(R.id.buddy_group);

		ArrayAdapter<BuddyGroup> adapter;
		BuddyGroup oldGroup = null;
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>();
		groups.addAll(account.getBuddyGroupList());
		for (int i = groups.size() - 1; i >= 0; i--) {
			if (groups.get(i).id == buddy.groupId) {
				oldGroup = groups.remove(i);
				break;
			}
		}
		adapter = new ArrayAdapter<BuddyGroup>(entryPoint, android.R.layout.simple_spinner_dropdown_item, groups);
		final BuddyGroup oldGroup2 = oldGroup;
		groupSpinner.setAdapter(adapter);
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					BuddyGroup newGroup = (BuddyGroup) groupSpinner.getSelectedItem();
					entryPoint.runtimeService.moveBuddy(buddy, oldGroup2, newGroup);
					dialog.dismiss();
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}

			}
		});
		dialog.show();
	}

	private static final void showBuddyRenameDialog(final Buddy buddy, final EntryPoint entryPoint) {
		final Dialog dialog = new Dialog(entryPoint);

		dialog.setContentView(R.layout.general_rename_item);
		dialog.setTitle(R.string.label_rename_buddy);

		final Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		final EditText nameEditor = (EditText) dialog.findViewById(R.id.name);

		nameEditor.setText(buddy.name);
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (nameEditor.getText().toString().equals(buddy.name)) {
					Toast.makeText(entryPoint, "Nothing to modify", Toast.LENGTH_SHORT).show();
					return;
				}
				buddy.name = nameEditor.getText().toString();
				try {
					entryPoint.runtimeService.renameBuddy(buddy);
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
				dialog.dismiss();
			}

		});
		dialog.show();
	}

	private static final void showAddGroupDialog(final AccountView account, final EntryPoint entryPoint) {
		final Dialog dialog = new Dialog(entryPoint);

		dialog.setContentView(R.layout.general_rename_item);
		dialog.setTitle(R.string.label_add_group);

		final Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		final EditText nameEditor = (EditText) dialog.findViewById(R.id.name);

		int maxLength = 40;
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(maxLength);
		nameEditor.setFilters(filterArray);

		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (nameEditor.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "Name too short", Toast.LENGTH_SHORT).show();
					return;
				}
				BuddyGroup group = new BuddyGroup(0, null, account.serviceId);
				group.name = nameEditor.getText().toString();
				try {
					entryPoint.runtimeService.addGroup(group);
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
				dialog.dismiss();
			}

		});
		dialog.show();
	}

	private static final void showGroupRenameDialog(final BuddyGroup group, final EntryPoint entryPoint) {
		final Dialog dialog = new Dialog(entryPoint);

		dialog.setContentView(R.layout.general_rename_item);
		dialog.setTitle(R.string.label_rename_group);

		final Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		final EditText nameEditor = (EditText) dialog.findViewById(R.id.name);

		nameEditor.setText(group.name);

		int maxLength = 40;
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(maxLength);
		nameEditor.setFilters(filterArray);

		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (nameEditor.getText().toString().equals(group.name)) {
					return;
				}
				group.name = nameEditor.getText().toString();
				try {
					entryPoint.runtimeService.renameGroup(group);
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
				dialog.dismiss();
			}

		});
		dialog.show();
	}

	public static void showAddBuddyDialog(final AccountView account, final Buddy buddy, final EntryPoint entryPoint) {
		final Dialog dialog = new Dialog(entryPoint);

		dialog.setContentView(R.layout.add_buddy);
		dialog.setTitle(R.string.label_add_buddy);

		final Spinner groupSpinner = (Spinner) dialog.findViewById(R.id.buddy_group);
		final EditText nameEditor = (EditText) dialog.findViewById(R.id.buddy_name);

		nameEditor.setText(buddy.getName());
		ArrayAdapter<BuddyGroup> adapter = new ArrayAdapter<BuddyGroup>(entryPoint, android.R.layout.simple_spinner_dropdown_item, account.getBuddyGroupList());
		groupSpinner.setAdapter(adapter);
		/*
		 * groupSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
		 * 
		 * @Override public void onItemSelected(AdapterView<?> parent, View
		 * view, int position, long id) {
		 * buddy.setGroupId(((BuddyGroup)groupSpinner
		 * .getSelectedItem()).getId()); }
		 * 
		 * @Override public void onNothingSelected(AdapterView<?> parent) { //
		 * TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * });
		 */
		Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (nameEditor.getText().toString().length() < 1) {
						Toast.makeText(entryPoint, "Name too short", Toast.LENGTH_SHORT).show();
						return;
					}
					buddy.name = nameEditor.getText().toString();
					buddy.groupId = ((BuddyGroup) groupSpinner.getSelectedItem()).id;
					entryPoint.runtimeService.addBuddy(buddy);
					dialog.dismiss();
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}

			}
		});
		dialog.show();
	}

	public static void menuEditStatus(final EntryPoint entryPoint, final AccountView account) {

		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(entryPoint.getResources().getString(R.string.label_pick_status));

		builder.setAdapter(getStatusIconMenuAdapterByTarget(account.protocolName, entryPoint), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					entryPoint.runtimeService.setStatus(account.serviceId, ServiceUtils.getStatusValueByCount(entryPoint, account, which));
					entryPoint.refreshMenu();
				} catch (NullPointerException npe) {
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
			}
		});
		builder.create().show();
	}

	public static void menuEditXStatus(final EntryPoint entryPoint, final AccountView account) {

		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(entryPoint.getResources().getString(R.string.label_pick_status));
		final TypedArray array = ServiceUtils.getXStatusArray32(entryPoint, account.protocolName);
		final IconMenuAdapter adapter = getXStatusIconMenuAdapterByTarget(account.protocolName, entryPoint);
		builder.setAdapter(adapter, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int position) {
				TypedValue v = new TypedValue();
				array.getValue(position, v);
				if (v.resourceId != R.drawable.xstatus_none) {
					menuEditXStatusText(entryPoint, account, position, (String) ((Object[]) adapter.getItem(position))[0]);
				} else {
					account.xStatus = -1;
					account.xStatusName = "";
					account.xStatusText = "";
					entryPoint.setXStatus(account);
				}
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	protected static void menuEditXStatusText(final EntryPoint entryPoint, final AccountView account, final int position, String xstatusName) {
		final Dialog dialog = new Dialog(entryPoint);
		dialog.setContentView(R.layout.xstatus_text_window_layout);

		dialog.setTitle(R.string.label_xstatus);
		Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		final EditText titleText = (EditText) dialog.findViewById(R.id.xstatus_title);
		final EditText valueText = (EditText) dialog.findViewById(R.id.xstatus_value);
		titleText.setText(xstatusName);
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				account.xStatus = (byte) position;
				account.xStatusName = titleText.getText().toString();
				account.xStatusText = valueText.getText().toString();
				entryPoint.setXStatus(account);
				dialog.dismiss();
			}

		});

		dialog.show();

	}

	public static void showTabChangeMenu(final EntryPoint entryPoint) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(R.string.label_tabs_list);
		final TabsAdapter adapter = getTabsAdapter(entryPoint);
		builder.setAdapter(adapter, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int position) {
				entryPoint.mainScreen.checkAndSetCurrentTabByTag(((TabInfo)adapter.getItem(position)).tag);
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	public static void showAuthRequestDialog(final ServiceMessage msg, final EntryPoint entryPoint) {
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		try {
			Buddy buddy = entryPoint.runtimeService.getBuddy(msg.serviceId, msg.from);
			builder.setTitle(entryPoint.getResources().getString(R.string.label_auth_request_from) + buddy.getName() + " (" + buddy.protocolUid + ")");
		} catch (Exception e) {
			ServiceUtils.log(e);
			builder.setTitle(entryPoint.getResources().getString(R.string.label_auth_request_from) + msg.from);
		}
		builder.setMessage(msg.text);
		builder.setNegativeButton(R.string.label_deny, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendAuthReply(entryPoint, msg, false);
				dialog.cancel();
			}

		});
		builder.setPositiveButton(R.string.label_allow, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendAuthReply(entryPoint, msg, true);
				dialog.cancel();
			}

		});
		builder.setNeutralButton(R.string.label_ignore, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}

		});
		builder.create().show();
	}

	private static void sendAuthReply(final EntryPoint entryPoint, final ServiceMessage msg, boolean b) {
		try {
			entryPoint.runtimeService.respondAuthorization(entryPoint.runtimeService.getBuddy(msg.serviceId, msg.from), b);
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			entryPoint.onRemoteCallFailed(e);
		}
	}

	public static void showFileRequestDialog(final FileMessage msg, final EntryPoint entryPoint) {
		if (msg == null || msg.files.size() < 1) {
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		try {
			Buddy buddy = entryPoint.runtimeService.getBuddy(msg.serviceId, msg.from);
			builder.setTitle(entryPoint.getResources().getString(R.string.label_file_from) + buddy.getName() + " (" + buddy.protocolUid + ")");
		} catch (Exception e) {
			ServiceUtils.log(e);
			builder.setTitle(entryPoint.getResources().getString(R.string.label_file_from) + msg.from);
		}
		builder.setMessage((msg.files.size() > 1 ? msg.files.size() + entryPoint.getResources().getString(R.string.label_files) : msg.files.get(0).filename) + ": " + msg.files.get(0).size + " bytes");
		builder.setNegativeButton(R.string.label_deny, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendFileReply(entryPoint, msg, false);
				dialog.cancel();
			}

		});
		builder.setPositiveButton(R.string.label_allow, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendFileReply(entryPoint, msg, true);
				dialog.cancel();
			}

		});
		builder.create().show();
	}

	private static void sendFileReply(EntryPoint entryPoint, FileMessage msg, boolean apply) {
		try {
			entryPoint.runtimeService.respondFileMessage(msg, apply);
		} catch (RemoteException e) {
			entryPoint.onRemoteCallFailed(e);
		}
	}

	public static void showSendFileDialog(EntryPoint entryPoint, Buddy buddy) {
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		FileExplorer explorer = new FileExplorer(entryPoint, buddy);
		try {
			builder.setTitle(explorer.currentDirectory.getCanonicalPath());
		} catch (IOException e) {
			ServiceUtils.log(e);
		}
		builder.setView(explorer);
		Dialog dialog = builder.create();
		explorer.setDialog(dialog);
		dialog.show();
	}

	public static void replaceClipboard(Context context, String msg, boolean add) {
		ClipboardManager clipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		clipMan.setText((add ? clipMan.getText() : "") + ">" + msg + "\n");
		Toast.makeText(context, R.string.label_copied_to_clipboard, Toast.LENGTH_SHORT).show();
	}

	public static void showVisibilityMenu(final EntryPoint entryPoint, final AccountView account) {
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(entryPoint.getResources().getString(R.string.label_manage_visibility));

		builder.setAdapter(new IconMenuAdapter(entryPoint, entryPoint.getResources().getStringArray(R.array.icq_visibility_descr), entryPoint.getResources().obtainTypedArray(R.array.icq_visibility_icons)), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					entryPoint.runtimeService.editMyVisibility(account.serviceId, ServiceUtils.getAccountVisibilityIdByVisibilityArrayId(which));
					entryPoint.refreshMenu();
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
			}
		});
		builder.create().show();
	}
	
	public static final Bitmap scaleBitmap(Bitmap bmp, int size, boolean fitHeightOnly){
		if (bmp == null){
			return null;
		}
		
		if (fitHeightOnly || bmp.getWidth() < bmp.getHeight()){
			int itemWidth = (size * bmp.getWidth()) / bmp.getHeight();
			return Bitmap.createScaledBitmap(bmp, itemWidth, size,  true);
		} else {
			int itemHeight = (size * bmp.getHeight()) / bmp.getWidth();
			return Bitmap.createScaledBitmap(bmp, size, itemHeight,  true);
		}
	}

	public static final void newGroupChatDialog(final EntryPoint entryPoint, final AccountView account) {
		final Dialog dialog = new Dialog(entryPoint);
		
		dialog.setContentView(R.layout.create_groupchat);
		dialog.setTitle(R.string.label_chat_enter_manually);

		Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		
		final View passwordProtectedLayout = dialog.findViewById(R.id.pwd_protected_layout);
		passwordProtectedLayout.setVisibility(accountSupportsPwdedChat(entryPoint, account) ? View.VISIBLE : View.GONE);
		final View nicknameLayout = dialog.findViewById(R.id.nickname_layout);
		nicknameLayout.setVisibility(accountSupportsCustomChatNickname(entryPoint, account) ? View.VISIBLE : View.GONE);
		
		final EditText chatNameText = (EditText) dialog.findViewById(R.id.chat_name);
		final EditText chatIdText = (EditText) dialog.findViewById(R.id.chat_id);
		final CheckBox protectedChatBox = (CheckBox) dialog.findViewById(R.id.pwd_protected_cb);
		final EditText passwordText = (EditText) dialog.findViewById(R.id.chat_password);
		final EditText nicknameText = (EditText) dialog.findViewById(R.id.nickname);
		
		final TextView passwordLbl = (TextView) dialog.findViewById(R.id.chat_password_lbl);
		
		nicknameText.setText(account.getSafeName());
		
		protectedChatBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					passwordLbl.setVisibility(View.VISIBLE);
					passwordText.setVisibility(View.VISIBLE);
				} else {
					passwordLbl.setVisibility(View.GONE);
					passwordText.setVisibility(View.GONE);
				}
			}
		});
		
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (chatNameText.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "Name cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (chatIdText.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "ID cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (protectedChatBox.isChecked() && passwordText.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "Password cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (nicknameText.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "Nickname cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//entryPoint.toggleWaitscreen(true);
				try {
					entryPoint.runtimeService.createChat(account.serviceId, chatIdText.getText().toString(), nicknameText.getText().toString(), chatNameText.getText().toString(), passwordText.getText().toString());
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
				
				dialog.dismiss();
			}

		});

		dialog.show();
	}

	public static final boolean accountSupportsPwdedChat(Context context, AccountView account) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))){
			return ICQServiceUtils.supportsPasswordedChats;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))){
			return XMPPServiceUtils.supportsPasswordedChats;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))){
			return MrimServiceUtils.supportsPasswordedChats;
		}
		return false;
	}
	
	public static final boolean accountSupportsCustomChatNickname(Context context, AccountView account) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))){
			return ICQServiceUtils.supportsCustomChatNickname;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))){
			return XMPPServiceUtils.supportsCustomChatNickname;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))){
			return MrimServiceUtils.supportsCustomChatNickname;
		}
		return false;
	}
	
	public static final boolean accountSupportsManualChatConnection(Context context, AccountView account) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))){
			return ICQServiceUtils.supportsManuallyConnectedChats;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))){
			return XMPPServiceUtils.supportsManuallyConnectedChats;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))){
			return MrimServiceUtils.supportsManuallyConnectedChats;
		}
		return false;
	}

	public static final void getGroupChatAction(EntryPoint entryPoint, AccountView account) {
		if (accountSupportsManualChatConnection(entryPoint, account)){
			showJoinGroupChatMenu(entryPoint, account);
		} else {
			entryPoint.addMyGroupChatsTab(account);
		}
	}

	private static final void showJoinGroupChatMenu(final EntryPoint entryPoint, final AccountView account) {
		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		
		builder.setTitle(R.string.label_connect_to_chat);
		builder.setPositiveButton(R.string.label_get_available_chats, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				entryPoint.addMyGroupChatsTab(account);
				//entryPoint.runtimeService.requestAvailableChatRooms(account.serviceId);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.label_chat_enter_manually, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showManualGroupChatOptions(entryPoint, account, null);
				dialog.dismiss();
			}
		});
		builder.setNeutralButton(R.string.label_create_groupchat, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				newGroupChatDialog(entryPoint, account);
			}
		});
		
		builder.create().show();
	}

	public final static void showManualGroupChatOptions(final EntryPoint entryPoint, final AccountView account, Buddy chat) {
		final Dialog dialog = new Dialog(entryPoint);
		
		dialog.setContentView(R.layout.create_groupchat);
		dialog.setTitle(R.string.label_chat_options);

		Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		
		final View passwordProtectedLayout = dialog.findViewById(R.id.pwd_protected_layout);
		passwordProtectedLayout.setVisibility(accountSupportsPwdedChat(entryPoint, account) ? View.VISIBLE : View.GONE);
		final View nicknameLayout = dialog.findViewById(R.id.nickname_layout);
		nicknameLayout.setVisibility(accountSupportsCustomChatNickname(entryPoint, account) ? View.VISIBLE : View.GONE);
		final View chatNameLayout = dialog.findViewById(R.id.chat_name_layout);
		chatNameLayout.setVisibility(View.GONE);
		
		final EditText chatIdText = (EditText) dialog.findViewById(R.id.chat_id);
		final CheckBox protectedChatBox = (CheckBox) dialog.findViewById(R.id.pwd_protected_cb);
		final EditText passwordText = (EditText) dialog.findViewById(R.id.chat_password);
		final EditText nicknameText = (EditText) dialog.findViewById(R.id.nickname);
		
		final TextView passwordLbl = (TextView) dialog.findViewById(R.id.chat_password_lbl);
		
		if (chat != null){
			chatIdText.setText(chat.protocolUid);
			chatIdText.setEnabled(false);
		}
		
		nicknameText.setText(account.getSafeName());
		
		protectedChatBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					passwordLbl.setVisibility(View.VISIBLE);
					passwordText.setVisibility(View.VISIBLE);
				} else {
					passwordLbl.setVisibility(View.GONE);
					passwordText.setVisibility(View.GONE);
				}
			}
		});
		
		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (chatIdText.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "ID cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (protectedChatBox.isChecked() && passwordText.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "Password cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (nicknameText.getText().toString().length() < 1) {
					Toast.makeText(entryPoint, "Nickname cannot be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//entryPoint.toggleWaitscreen(true);
				try {
					entryPoint.runtimeService.joinChat(account.serviceId, chatIdText.getText().toString(), nicknameText.getText().toString(), passwordText.getText().toString());
				} catch (RemoteException e) {
					entryPoint.onRemoteCallFailed(e);
				}
				
				dialog.dismiss();
			}

		});

		dialog.show();
	}
	
	public static final void styleTextView(TextView text){
		if (EntryPoint.bgColor < 0xff7f7f80){
			text.setShadowLayer(0, 0, 0, 0);
			text.setTextColor(0xffffffff);
			text.setBackgroundColor(0xe0000000);
		} else if (EntryPoint.bgColor == 0xff7f7f80){
			text.setShadowLayer(1.8f, 2, 2, 0xcd000000);
			text.setTextColor(0xffffffff);
			text.setBackgroundColor(0x00000000);
		} else {
			text.setShadowLayer(0, 0, 0, 0);
			text.setTextColor(0xff000000);
			text.setBackgroundColor(0xf0ffffff);
		}
	}

	public static final void showChatInfo(final AccountView account, final MultiChatRoom chat, PersonalInfo info, final EntryPoint entryPoint) {
		final Dialog dialog = new Dialog(entryPoint);
		
		dialog.setContentView(R.layout.chat_info_layout);
		dialog.setTitle(chat.getName());

		Button okBtn = (Button) dialog.findViewById(R.id.button_ok);
		LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.layout);
		
		okBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showManualGroupChatOptions(entryPoint, account, chat);
				dialog.dismiss();
			}
		});
		
		updatePersonalInfoLayout(info, layout, null);		
		
		dialog.show();
	}
	
	public static void updatePersonalInfoLayout(PersonalInfo info, LinearLayout layout, OnLongClickListener longClickListener){
		if (info == null || info.properties == null){
			return;
		}
		
		TextView uidView = new TextView(layout.getContext());
		uidView.setText("UID: "+info.protocolUid, TextView.BufferType.EDITABLE);
		ViewUtils.colorPersonalInfoView(uidView);
		if (longClickListener != null){
			uidView.setOnLongClickListener(longClickListener);
		}
		layout.addView(uidView);
		
		List<String> keys = new ArrayList<String>();
		keys.addAll(info.properties.keySet());
		
		Collections.sort(keys);
		
		for (String key: keys){
			Object value = info.properties.get(key);
			if (value == null || value.toString().equals("-1")){
				continue;
			}
			
			TextView iew = new TextView(layout.getContext());
			
			if (key.equals(PersonalInfo.INFO_GENDER)){
				info.properties.putString(key, ((Byte)info.properties.get(key)) == 1 ? layout.getResources().getString(R.string.label_male) : layout.getResources().getString(R.string.label_female));
			}
			
			iew.setText(key+": "+info.properties.get(key), TextView.BufferType.EDITABLE);
			colorPersonalInfoView(iew);
			if (longClickListener != null){
				iew.setOnLongClickListener(longClickListener);
			}
			layout.addView(iew);
		}
	}
	
	public static final void colorPersonalInfoView(TextView view){
		Spannable s = view.getEditableText();
		if (s == null){
			return;
		}
		
		if (view.getText().toString().indexOf("UID: ") > -1){
			s.setSpan(new ForegroundColorSpan(0xffff0000), 0, view.getText().toString().indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {
			s.setSpan(new ForegroundColorSpan(0xff00a5ff), 0, view.getText().toString().indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
	}
	
	public static class VMRuntimeHack {
        private Object runtime = null;
        private Method trackAllocation = null;
        private Method trackFree = null;
        
        public boolean trackAlloc(long size) {
            if (runtime == null)
                return false;
            try {
                Object res = trackAllocation.invoke(runtime, Long.valueOf(size));
                return (res instanceof Boolean) ? (Boolean)res : true;
            } catch (IllegalArgumentException e) {
                return false;
            } catch (IllegalAccessException e) {
                return false;
            } catch (InvocationTargetException e) {
                return false;
            }
        }

        public boolean trackFree(long size) {
            if (runtime == null)
                return false;
            try {
                Object res = trackFree.invoke(runtime, Long.valueOf(size));
                return (res instanceof Boolean) ? (Boolean)res : true;
            } catch (IllegalArgumentException e) {
                return false;
            } catch (IllegalAccessException e) {
                return false;
            } catch (InvocationTargetException e) {
                return false;
            }
        }
        
        public Bitmap allocBitmap(Bitmap bmp) {
            if (useHack) {
                trackFree(bmp.getRowBytes() * bmp.getHeight());
                hackedBitmaps.add(bmp);
            }
            allocatedBitmaps.add(bmp);
            return bmp;
    	}

    	public void freeBitmap(Bitmap bmp) {
   			bmp.recycle();
            if (hackedBitmaps.contains(bmp)) {
                trackAlloc(bmp.getRowBytes() * bmp.getHeight());
                hackedBitmaps.remove(bmp);
            }
            allocatedBitmaps.remove(bmp);
    	}

    	public void freeAll() {
    		for (Bitmap bmp : new LinkedList<Bitmap>(allocatedBitmaps))
    			freeBitmap(bmp);
    	}

    	//may be turned to changeable
    	private final boolean useHack = true;
    	
    	private Set<Bitmap> allocatedBitmaps = new HashSet<Bitmap>(); 
    	private Set<Bitmap> hackedBitmaps = new HashSet<Bitmap>(); 
        
        
        public VMRuntimeHack() {
            boolean success = false;
            try {
                Class<?> cl = Class.forName("dalvik.system.VMRuntime");
                Method getRt = cl.getMethod("getRuntime", new Class[0]);
                runtime = getRt.invoke(null, new Object[0]);
                trackAllocation = cl.getMethod("trackExternalAllocation", new Class[] {long.class});
                trackFree = cl.getMethod("trackExternalFree", new Class[] {long.class});
                success = true;
            } catch (ClassNotFoundException e) {
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            if (!success) {
                ServiceUtils.log("VMRuntime hack does not work!");
                runtime = null;
                trackAllocation = null;
                trackFree = null;
            }
        }
    }
}
