package ua.snuk182.asia.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.FileMessage;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.icq.ICQService;
import ua.snuk182.asia.services.mrim.MrimService;
import ua.snuk182.asia.services.plus.IconMenuAdapter;
import ua.snuk182.asia.services.xmpp.XMPPService;
import ua.snuk182.asia.view.more.fileexplorer.FileExplorer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.text.ClipboardManager;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public final class ViewUtils {

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
				ica = new IconMenuAdapter(context, ICQService.getStatusListNames(context), ICQService.getStatusResIds(context));
			}
			if (target.equals(context.getResources().getString(R.string.xmpp_service_name))) {
				ica = new IconMenuAdapter(context, XMPPService.getStatusListNames(context), XMPPService.getStatusResIds(context));
			}
			if (target.equals(context.getResources().getString(R.string.mrim_service_name))) {
				ica = new IconMenuAdapter(context, MrimService.getStatusListNames(context), MrimService.getStatusResIds(context));
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

	public static void contactMenu(final EntryPoint entryPoint, final AccountView account, final Buddy buddy) {
		// stub
		if (!account.protocolName.equals(entryPoint.getResources().getString(R.string.icq_service_name))) {
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(entryPoint.getResources().getString(R.string.label_contact_menu) + ": " + buddy.getName() + " (" + buddy.protocolUid + ")");
		TypedArray items = entryPoint.getResources().obtainTypedArray(R.array.icq_contact_menu_names);
		
		final List<String> itemList = new ArrayList<String>(items.length());
		for (int i = 0; i < items.length(); i++) {
			String item = items.getString(i);
			/*
			 * if (value.equals(entryPoint.getResources().getString(R.string.
			 * menu_value_view_info))) { continue; }
			 */
			if (item.equals(entryPoint.getResources().getString(R.string.menu_value_add_to_list))) {
				if (buddy.groupId != AccountService.NOT_IN_LIST_GROUP_ID) {
					continue;
				}
			}
			if (item.equals(entryPoint.getResources().getString(R.string.menu_value_ask_auth))) {
				if (buddy.visibility != Buddy.VIS_NOT_AUTHORIZED) {
					continue;
				}
			}
			if (item.equals(entryPoint.getResources().getString(R.string.menu_value_to_permits))) {
				if (buddy.visibility == Buddy.VIS_NOT_AUTHORIZED || buddy.visibility == Buddy.VIS_PERMITTED) {
					continue;
				}
			}
			if (item.equals(entryPoint.getResources().getString(R.string.menu_value_to_denys))) {
				if (buddy.visibility == Buddy.VIS_NOT_AUTHORIZED || buddy.visibility == Buddy.VIS_DENIED) {
					continue;
				}
			}
			if (item.equals(entryPoint.getResources().getString(R.string.menu_value_from_permits))) {
				if (buddy.visibility != Buddy.VIS_PERMITTED) {
					continue;
				}
			}
			if (item.equals(entryPoint.getResources().getString(R.string.menu_value_from_denys))) {
				if (buddy.visibility != Buddy.VIS_DENIED) {
					continue;
				}
			}
			itemList.add(item);
		}

		items.recycle();
		
		String[] itemArray = itemList.toArray(new String[itemList.size()]);

		builder.setItems(itemArray, new OnClickListener() {

			@Override
			public void onClick(final DialogInterface bdialog, int which) {
				String command = itemList.get(which);
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_add_to_list))) {
					if (account.getConnectionState() == AccountService.STATE_CONNECTED) {
						showAddBuddyDialog(account, buddy, entryPoint);
					} else {
						Toast.makeText(entryPoint, entryPoint.getString(R.string.label_you_must_enter_uid), Toast.LENGTH_LONG).show();
					}
					return;
				}
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_delete_contact))) {
					showRemoveBuddyDialog(buddy, entryPoint);
					return;
				}
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_view_info))) {
					try {
						entryPoint.runtimeService.requestBuddyFullInfo(buddy.serviceId, buddy.protocolUid);
					} catch (NullPointerException npe) {
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						entryPoint.onRemoteCallFailed(e);
					}
					return;
				}
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_ask_auth))) {
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
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_rename))) {
					showBuddyRenameDialog(buddy, entryPoint);
					return;
				}
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_move))) {
					showBuddyMoveDialog(account, buddy, entryPoint);
					return;
				}
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_to_permits))) {
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
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_to_denys))) {
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
				if (command.equals(entryPoint.getResources().getString(R.string.menu_value_from_permits)) || command.equals(entryPoint.getResources().getString(R.string.menu_value_from_denys))) {
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

		WindowManager.LayoutParams layout = entryPoint.getWindow().getAttributes();
		layout.width = android.view.WindowManager.LayoutParams.FILL_PARENT;
		layout.height = android.view.WindowManager.LayoutParams.FILL_PARENT;
		entryPoint.getWindow().setAttributes(layout);

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
		/*
		 * List<TabInfo> tabs = entryPoint.getTabs(); final Dialog dialog = new
		 * Dialog(entryPoint); dialog.setTitle(R.string.label_tabs_list);
		 * ListView list = new ListView(entryPoint); list.setLayoutParams(new
		 * LayoutParams(AbsListView.LayoutParams.FILL_PARENT,
		 * AbsListView.LayoutParams.WRAP_CONTENT)); list.setAdapter(new
		 * TabsAdapter(entryPoint, tabs)); list.setOnItemClickListener(new
		 * OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) {
		 * entryPoint.getTabHost().setCurrentTab(position); dialog.dismiss(); }
		 * 
		 * }); dialog.setContentView(list); dialog.show();
		 */

		AlertDialog.Builder builder = new AlertDialog.Builder(entryPoint);
		builder.setTitle(R.string.label_tabs_list);
		builder.setAdapter(getTabsAdapter(entryPoint), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int position) {
				entryPoint.getTabHost().setCurrentTab(position);
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
}
