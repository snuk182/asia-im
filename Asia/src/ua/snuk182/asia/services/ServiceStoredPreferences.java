package ua.snuk182.asia.services;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Account;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.services.api.AccountService;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Xml;

public final class ServiceStoredPreferences {

	private static final String ATTR_CONNECTION_STATE = "connection_state";
	private static final String ATTR_LAST_UPDATE = "last_update";
	private static final String TAG_ACCOUNTS = "accounts";
	private static final String ATTR_COLLAPSED = "is_collapsed";
	private static final String TAG_GROUP = "group";
	private static final String TAG_GROUPS = "groups";
	private static final String ATTR_GROUPS = TAG_GROUPS;
	private static final String ATTR_UNREAD = "unread";
	private static final String ATTR_ID = "id";
	private static final String ATTR_GROUP_ID = "group_id";
	private static final String TAG_NAME = "name";
	private static final String TAG_BUDDY_NAME = "buddy_name";
	private static final String TAG_GROUP_NAME = "group_name";
	private static final String TAG_BUDDY = "buddy";
	private static final String ATTR_BUDDIES = "buddies";
	private static final String TAG_BUDDIES = "buddies";
	private static final String TAG_XSTATUS_TEXT = "xstatus_text";
	private static final String TAG_XSTATUS_NAME = "xstatus_name";
	private static final String ATTR_VISIBILITY = "visibility";
	private static final String ATTR_XSTATUS = "xstatus";
	private static final String ATTR_STATUS = "status";
	private static final String ATTR_PROTOCOL_UID = "protocol_uid";
	private static final String ATTR_PROTOCOL_NAME = "protocol_name";
	private static final String TAG_ACCOUNT = "account";
	private static final String XML_ENCODING = "UTF-16LE";
	private static final String XMLPARAMS_TOTAL = "XmlTotalParams";
	private static final String PREFERENCES_FILEEXT = ".preferences";

	private static final String XML_NAMESPACE = "ua.snuk182.asia";

	private static final String CONTACT_OPTION_DIVIDER = " !div! ";
	private static final String CONTACT_ITEM_DIVIDER = " asiaacc ";
	private static final String SAVEDPARAM_ACCOUNTS = "AsiaSavedAccounts";
	private static final String SAVEDPARAM_ACCOUNT_PREFERENCES = "AsiaAccountPreferences";
	private static final String SAVEDPARAM_ACCOUNT_XSTATUSNAME = "AsiaAccountXStatusName";
	private static final String SAVEDPARAM_ACCOUNT_XSTATUSVALUE = "AsiaAccountXStatusValue";
	private static final String SAVEDPARAM_ACCOUNT_CONTACTS = "AsiaAccountContacts";
	private static final String SAVEDPARAM_ACCOUNT_GROUPS = "AsiaAccountGroups";
	// private static final String SAVEDPARAM_ACCOUNT_GROUP = "AccountGroup";
	private static final String SAVEDPARAMS_TOTAL = "AsiaTotalParams";

	private Context context;

	public ServiceStoredPreferences(Context context) {
		this.context = context;
	}

	public synchronized void saveMap(final Map<String, String> map, final String storageName) {

		new Thread("Preference saver " + storageName) {
			@Override
			public void run() {
				SharedPreferences.Editor preferences = context.getSharedPreferences(storageName, 0).edit();

				for (String key : map.keySet()) {
					preferences.putString(key, map.get(key));
				}

				preferences.commit();
			}
		}.start();
	}

	public static String getOption(Context context, String key) {
		SharedPreferences soptions = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
		return soptions.getString(key, "false");
	}

	public Bundle getApplicationOptions() {
		return getOptions(null, context);
	}

	public synchronized Map<String, String> getMap(Set<String> keys, String storageName) {
		if (keys == null) {
			return null;
		}

		if (storageName == null) {
			storageName = SAVEDPARAMS_TOTAL;
		}
		Map<String, String> map = new HashMap<String, String>();
		SharedPreferences preferences = context.getSharedPreferences(storageName, 0);

		for (String key : keys) {
			map.put(key, preferences.getString(key, null));
		}

		return map;
	}

	public List<AccountView> getAccounts() throws Exception {
		List<AccountView> accounts;

		SharedPreferences preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
		String accs = preferences.getString(SAVEDPARAM_ACCOUNTS, null);

		if (accs != null) {
			accounts = getAccountsOld();
			try {
				saveAccountHeaders(accounts);
				for (AccountView account : accounts) {
					saveAccount(account);
					SharedPreferences ipreferences = context.getSharedPreferences(account.getAccountId(), 0);
					SharedPreferences.Editor ieditor = ipreferences.edit();
					ieditor.clear();
					ieditor.commit();
				}

				SharedPreferences.Editor editor = preferences.edit();
				editor.remove(SAVEDPARAM_ACCOUNTS);
				editor.commit();
			} catch (Exception e) {
				ServiceUtils.log(e);
			}
		} else {
			accounts = getAccountHeaders();
			for (AccountView account : accounts) {
				try {				
					getAccount(account, true);					
				} catch (Exception e) {
					ServiceUtils.log(e, account);
				}
			}
		}

		return accounts;
	}

	public List<AccountView> getAccountsOld() {
		SharedPreferences preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
		List<AccountView> protocols = new ArrayList<AccountView>();
		String[] accounts = preferences.getString(SAVEDPARAM_ACCOUNTS, "").split(CONTACT_ITEM_DIVIDER);
		if (accounts == null) {
			accounts = new String[] {};
		}
		for (String account : accounts) {
			if (account.indexOf(" ") < 0) {
				continue;
			}
			AccountView paccount = getAccountOld(account, (byte) protocols.size());
			protocols.add(paccount);
		}

		return protocols;
	}
	
	public void saveAccounts(List<Account> accounts) {
		for (Account account : accounts) {
			saveAccount(account.accountView, false);
		}		
	}

	public void saveServiceState(List<Account> accounts) {
		List<AccountView> accountViews = new LinkedList<AccountView>();
		for (Account account : accounts) {
			accountViews.add(account.accountView);
		}
		try {
			saveAccountHeaders(accountViews);
		} catch (Exception e) {
			ServiceUtils.log(e);
		}
	}

	public void saveAccountOld(final AccountView account) {
		if (account == null) {
			return;
		}

		new Thread("Account saver " + account.getAccountId()) {

			@Override
			public void run() {
				SharedPreferences preferences = context.getSharedPreferences(account.getAccountId(), 0);
				SharedPreferences.Editor editor = preferences.edit();

				// editor.putString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_PW,
				// account.getPassword());
				String prefsString = (account.ownName != null ? account.ownName : "") + CONTACT_ITEM_DIVIDER + account.status + CONTACT_ITEM_DIVIDER + account.xStatus;
				editor.putString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_PREFERENCES, prefsString);
				editor.putString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_XSTATUSNAME, account.xStatusName);
				editor.putString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_XSTATUSVALUE, account.xStatusText);

				StringBuilder accountGroups = new StringBuilder();
				for (int i = 0; i < account.getBuddyGroupList().size(); i++) {
					accountGroups.append(account.getBuddyGroupList().get(i).id);
					accountGroups.append(CONTACT_OPTION_DIVIDER + account.getBuddyGroupList().get(i).name);
					accountGroups.append(CONTACT_OPTION_DIVIDER + account.getBuddyGroupList().get(i).isCollapsed);
					if (i < account.getBuddyGroupList().size() - 1) {
						accountGroups.append(CONTACT_ITEM_DIVIDER);
					}
				}
				editor.putString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_GROUPS, accountGroups.toString());

				StringBuilder accountContacts = new StringBuilder();
				for (int i = 0; i < account.getBuddyList().size(); i++) {
					accountContacts.append(account.getBuddyList().get(i).protocolUid);
					accountContacts.append(CONTACT_OPTION_DIVIDER + ((account.getBuddyList().get(i).getName() != null) ? account.getBuddyList().get(i).getName() : ""));
					accountContacts.append(CONTACT_OPTION_DIVIDER + account.getBuddyList().get(i).unread);
					accountContacts.append(CONTACT_OPTION_DIVIDER + account.getBuddyList().get(i).groupId);
					accountContacts.append(CONTACT_OPTION_DIVIDER + account.getBuddyList().get(i).iconHash);
					accountContacts.append(CONTACT_OPTION_DIVIDER + account.getBuddyList().get(i).visibility);
					if (i < account.getBuddyList().size() - 1) {
						accountContacts.append(CONTACT_ITEM_DIVIDER);
					}
				}
				editor.putString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_CONTACTS, accountContacts.toString());
				/*
				 * Set<String> keys = account.options.keySet(); StringBuilder
				 * optionsKeys = new StringBuilder(); for (String key:keys){
				 * editor.putString(SAVEDPARAM_ACCOUNT_OPTIONS+" "+key,
				 * account.options.getString(key)); optionsKeys.append(key+" ");
				 * } editor.putString(SAVEDPARAM_ACCOUNT_OPTIONS,
				 * optionsKeys.toString());
				 */
				editor.commit();

				preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
				editor = preferences.edit();
				String accounts = preferences.getString(SAVEDPARAM_ACCOUNTS, null);
				String accountUid = account.getAccountId();

				if (accounts == null || accounts.indexOf(accountUid) < 0) {
					if (accounts != null && accounts.length() > 3) {
						accounts += CONTACT_ITEM_DIVIDER;
					} else {
						accounts = "";
					}

					accounts += accountUid;

					editor.putString(SAVEDPARAM_ACCOUNTS, accounts);
					editor.commit();
				}
			}
		}.start();
	}

	private AccountView getAccountOld(String accountId, byte serviceId) {
		if (accountId == null)
			return null;

		String[] accountAttrs = accountId.split("  *");
		String accountUid = accountAttrs[0];
		String accountProtocol = accountAttrs[1];

		AccountView account = new AccountView(accountUid, accountProtocol);
		account.serviceId = serviceId;
		if (accountAttrs.length > 2) {
			short connState;
			try {
				connState = Short.parseShort(accountAttrs[2]);
			} catch (NumberFormatException e) {
				connState = AccountService.STATE_DISCONNECTED;
			}
			account.setConnectionState(connState);
		}

		SharedPreferences preferences = context.getSharedPreferences(account.getAccountId(), 0);

		/*
		 * String optionNames =
		 * preferences.getString(SAVEDPARAM_ACCOUNT_OPTIONS, ""); String[]
		 * options = optionNames.split(" "); Bundle bu = new Bundle(); if
		 * (options!=null && options.length>0){ for (String option:options){
		 * String optionValue =
		 * preferences.getString(SAVEDPARAM_ACCOUNT_OPTIONS+" "+option, null);
		 * if (optionValue != null){ bu.putString(option, optionValue); } } }
		 * account.options = bu;
		 */

		String accountPreferences = preferences.getString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_PREFERENCES, "");

		if (accountPreferences.indexOf(CONTACT_ITEM_DIVIDER) > -1) {
			String[] accountPreferencesArray = accountPreferences.split(CONTACT_ITEM_DIVIDER);
			account.ownName = (accountPreferencesArray[0].length() > 0 ? accountPreferencesArray[0] : accountUid);
			account.status = Byte.parseByte(accountPreferencesArray[1]);
			account.xStatus = Byte.parseByte(accountPreferencesArray[2]);
			account.xStatusName = preferences.getString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_XSTATUSNAME, "");
			account.xStatusText = preferences.getString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_XSTATUSVALUE, "");
		}
		String accountContacts = preferences.getString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_CONTACTS, "");
		if (accountContacts.indexOf(CONTACT_ITEM_DIVIDER) > -1) {
			String[] contacts = accountContacts.split(CONTACT_ITEM_DIVIDER);
			for (String contact : contacts) {
				if (contact.length() < 1) {
					continue;
				}
				String[] contactAttrs = contact.split(CONTACT_OPTION_DIVIDER);
				Buddy buddy = new Buddy(contactAttrs[0], account);
				buddy.name = contactAttrs[1];
				buddy.ownerUid = account.protocolUid;

				if (contactAttrs.length > 2) {
					buddy.unread = Byte.parseByte(contactAttrs[2]);
				}

				if (contactAttrs.length > 3) {
					buddy.groupId = Integer.parseInt(contactAttrs[3]);
				}

				if (contactAttrs.length > 4) {
					buddy.iconHash = contactAttrs[4];
				}

				if (contactAttrs.length > 5) {
					buddy.visibility = Byte.parseByte(contactAttrs[5]);
				}

				buddy.serviceId = account.serviceId;

				account.getBuddyList().add(buddy);
			}
		} else if (accountContacts.indexOf(CONTACT_OPTION_DIVIDER) > -1) {
			String[] contactAttrs = accountContacts.split(CONTACT_OPTION_DIVIDER);
			Buddy buddy = new Buddy(contactAttrs[0], account);
			buddy.name = contactAttrs[1];
			buddy.ownerUid = account.protocolUid;

			if (contactAttrs.length > 2) {
				buddy.unread = Byte.parseByte(contactAttrs[2]);
			}

			if (contactAttrs.length > 3) {
				buddy.groupId = Integer.parseInt(contactAttrs[3]);
			}

			if (contactAttrs.length > 4) {
				buddy.iconHash = contactAttrs[4];
			}

			if (contactAttrs.length > 5) {
				buddy.visibility = Byte.parseByte(contactAttrs[5]);
			}

			account.getBuddyList().add(buddy);
		}

		String accountContactGroups = preferences.getString(account.getAccountId() + " " + SAVEDPARAM_ACCOUNT_GROUPS, "");
		if (accountContactGroups.indexOf(CONTACT_ITEM_DIVIDER) > -1) {
			String[] groups = accountContactGroups.split(CONTACT_ITEM_DIVIDER);
			for (String group : groups) {
				if (group.length() < 1) {
					continue;
				}

				String[] groupItems = group.split(CONTACT_OPTION_DIVIDER);

				BuddyGroup buddyGroup = new BuddyGroup(Integer.parseInt(groupItems[0]), account.getAccountId(), account.serviceId);
				buddyGroup.name = groupItems[1];
				if (groupItems.length > 2) {
					buddyGroup.isCollapsed = Boolean.parseBoolean(groupItems[2]);
				}
				buddyGroup.ownerUid = account.getAccountId();
				account.getBuddyGroupList().add(buddyGroup);
			}
		} else if (accountContactGroups.indexOf(CONTACT_OPTION_DIVIDER) > -1) {
			String[] groupItems = accountContactGroups.split(CONTACT_OPTION_DIVIDER);

			BuddyGroup buddyGroup = new BuddyGroup(Integer.parseInt(groupItems[0]), account.getAccountId(), account.serviceId);
			buddyGroup.name = groupItems[1];
			buddyGroup.ownerUid = account.getAccountId();
			account.getBuddyGroupList().add(buddyGroup);
		}

		account.options = getOptions(account, context);

		return account;
	}

	private Bundle getOptions(AccountView account, Context context) {
		String file;
		if (account != null) {
			file = account.getAccountId();
		} else {
			file = SAVEDPARAMS_TOTAL;
		}
		SharedPreferences soptions = context.getSharedPreferences(file, 0);
		Bundle bbu = new Bundle();
		Map<String, ?> map = soptions.getAll();
		for (String key : map.keySet()) {
			bbu.putString(key, (String) map.get(key));
			if (key.endsWith(context.getResources().getString(R.string.key_disabled))) {
				account.isEnabled = !Boolean.parseBoolean((String) map.get(key));
			}
		}
		return bbu;
	}

	public Bitmap getBitmapFromLocalFile(String filename) {
		filename = filename + Buddy.BUDDYICON_FILEEXT;
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(filename);
		} catch (FileNotFoundException e) {
		}

		if (fis == null)
			return null;

		return BitmapFactory.decodeStream(fis);
	}

	public void saveBinaryFile(String filename, byte[] contents, Runnable runOnFinish) {
		new FileAsyncSaver(filename, contents, runOnFinish).start();
	}

	public void saveIcon(String filename, byte[] contents, Runnable runOnFinish) {
		saveBinaryFile(filename + Buddy.BUDDYICON_FILEEXT, contents, runOnFinish);
	}

	class FileAsyncSaver extends Thread {

		String fileName;
		byte[] contents;
		Runnable runOnFinish;

		public FileAsyncSaver(String fileName, byte[] contents, Runnable runOnFinish) {
			this.fileName = fileName;
			this.contents = contents;
			this.runOnFinish = runOnFinish;
			setName("Preferences saver " + fileName);
		}

		@Override
		public void run() {
			FileOutputStream fos = null;
			try {
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				fos.write(contents);
				fos.close();
				runOnFinish.run();
			} catch (FileNotFoundException e) {
				ServiceUtils.log(e);
			} catch (IOException e) {
				ServiceUtils.log(e);
			}
		}
	}

	public void savePreference(final String key, final String value, final AccountView account) {
		new Thread("Single preference saver") {

			@Override
			public void run() {
				SharedPreferences preferences;
				if (account != null) {
					preferences = context.getSharedPreferences(account.getAccountId(), 0);
				} else {
					preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
				}
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(key, value);
				editor.commit();
			}
		}.start();
	}

	public void removeAccount(AccountView account) throws XmlPullParserException, IOException {
		context.deleteFile(account.getAccountId());
		context.deleteFile(account.getFilename() + PREFERENCES_FILEEXT);

		List<AccountView> acco = getAccountHeaders();
		for (int i = acco.size() - 1; i >= 0; i--) {
			if (acco.get(i).protocolUid.equalsIgnoreCase(account.protocolUid)) {
				acco.remove(i);
				break;
			}
		}
		saveAccountHeaders(acco);
	}
	
	public void saveAccount(final AccountView account) {
		saveAccount(account, false);
	}

	public void saveAccount(final AccountView account, final boolean saveHeaders) {
		if (account == null) {
			return;
		}

		new Thread("Xml Account saver " + account.getAccountId()) {

			@Override
			public void run() {

				XmlSerializer serializer = Xml.newSerializer();
				try {
					serializer.setOutput(new BufferedOutputStream(context.openFileOutput(account.getFilename() + PREFERENCES_FILEEXT, 0)), XML_ENCODING);
					serializer.startDocument(XML_ENCODING, true);
					serializer.startTag(XML_NAMESPACE, TAG_ACCOUNT);
					serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_NAME, account.protocolName.trim());
					serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_UID, account.protocolUid.trim());
					serializer.attribute(XML_NAMESPACE, ATTR_STATUS, Byte.toString(account.status));
					serializer.attribute(XML_NAMESPACE, ATTR_XSTATUS, Byte.toString(account.xStatus));
					serializer.attribute(XML_NAMESPACE, ATTR_VISIBILITY, Byte.toString(account.visibility));
					serializer.attribute(XML_NAMESPACE, ATTR_LAST_UPDATE, Long.toString(account.lastUpdateTime));

					serializer.startTag(XML_NAMESPACE, TAG_NAME);
					serializer.text(account.getSafeName().trim());
					serializer.endTag(XML_NAMESPACE, TAG_NAME);

					serializer.startTag(XML_NAMESPACE, TAG_XSTATUS_NAME);
					serializer.text(account.xStatusName.trim());
					serializer.endTag(XML_NAMESPACE, TAG_XSTATUS_NAME);

					serializer.startTag(XML_NAMESPACE, TAG_XSTATUS_TEXT);
					serializer.text(account.xStatusText.trim());
					serializer.endTag(XML_NAMESPACE, TAG_XSTATUS_TEXT);

					serializer.startTag(XML_NAMESPACE, TAG_BUDDIES);
					serializer.attribute(XML_NAMESPACE, ATTR_BUDDIES, Integer.toString(account.getBuddyList().size()));
					for (Buddy buddy : account.getBuddyList()) {
						serializer.startTag(XML_NAMESPACE, TAG_BUDDY);
						serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_UID, buddy.protocolUid.trim());
						serializer.attribute(XML_NAMESPACE, ATTR_GROUP_ID, Integer.toString(buddy.groupId));
						serializer.attribute(XML_NAMESPACE, ATTR_ID, Integer.toString(buddy.id));
						serializer.attribute(XML_NAMESPACE, ATTR_UNREAD, Byte.toString(buddy.unread));
						serializer.attribute(XML_NAMESPACE, ATTR_VISIBILITY, Integer.toString(buddy.visibility));
						
						serializer.startTag(XML_NAMESPACE, TAG_BUDDY_NAME);
						serializer.text(buddy.getName().trim());
						serializer.endTag(XML_NAMESPACE, TAG_BUDDY_NAME);

						serializer.endTag(XML_NAMESPACE, TAG_BUDDY);
					}
					serializer.endTag(XML_NAMESPACE, TAG_BUDDIES);

					serializer.startTag(XML_NAMESPACE, TAG_GROUPS);
					serializer.attribute(XML_NAMESPACE, ATTR_GROUPS, Integer.toString(account.getBuddyGroupList().size()));
					for (BuddyGroup group : account.getBuddyGroupList()) {
						serializer.startTag(XML_NAMESPACE, TAG_GROUP);
						serializer.attribute(XML_NAMESPACE, ATTR_ID, Integer.toString(group.id));
						serializer.attribute(XML_NAMESPACE, ATTR_COLLAPSED, Boolean.toString(group.isCollapsed));
						
						serializer.startTag(XML_NAMESPACE, TAG_GROUP_NAME);
						serializer.text(group.name.trim());
						serializer.endTag(XML_NAMESPACE, TAG_GROUP_NAME);

						serializer.endTag(XML_NAMESPACE, TAG_GROUP);
					}
					serializer.endTag(XML_NAMESPACE, TAG_GROUPS);
					serializer.endTag(XML_NAMESPACE, TAG_ACCOUNT);
					serializer.endDocument();

					if (saveHeaders){
						List<AccountView> accounts = getAccountHeaders();

						boolean found = false;
						for (AccountView acco : accounts) {
							if (acco.protocolUid.equalsIgnoreCase(account.protocolUid)) {
								//acco.merge(account);
								found = true;
							}
						}

						if (!found) {
							accounts.add(account);
						}

						saveAccountHeaders(accounts);
					}
				} catch (Exception e) {
					ServiceUtils.log(e);
				}
			}
		}.start();
	}

	private void saveAccountHeaders(List<AccountView> accounts) throws IllegalArgumentException, IllegalStateException, FileNotFoundException, IOException {
		XmlSerializer serializer = Xml.newSerializer();
		serializer.setOutput(new BufferedOutputStream(context.openFileOutput(XMLPARAMS_TOTAL, 0)), XML_ENCODING);
		serializer.startDocument(XML_ENCODING, true);

		serializer.startTag(XML_NAMESPACE, TAG_ACCOUNTS);

		for (AccountView account : accounts) {
			serializer.startTag(XML_NAMESPACE, TAG_ACCOUNT);
			serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_UID, account.protocolUid);
			serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_NAME, account.protocolName);
			serializer.attribute(XML_NAMESPACE, ATTR_CONNECTION_STATE, Short.toString(account.getConnectionState()));
			serializer.endTag(XML_NAMESPACE, TAG_ACCOUNT);
		}
		serializer.endTag(XML_NAMESPACE, TAG_ACCOUNTS);
		serializer.endDocument();
	}

	private List<AccountView> getAccountHeaders() throws XmlPullParserException, IOException {
		List<AccountView> accounts = new ArrayList<AccountView>();

		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(context.openFileInput(XMLPARAMS_TOTAL), XML_ENCODING);

		int eventType = parser.getEventType();
		AccountView account = null;
		boolean done = false;
		while (eventType != XmlPullParser.END_DOCUMENT && !done) {
			String name = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if (name.equalsIgnoreCase(TAG_ACCOUNT)) {
					account = new AccountView(parser.getAttributeValue(XML_NAMESPACE, ATTR_PROTOCOL_UID), parser.getAttributeValue(XML_NAMESPACE, ATTR_PROTOCOL_NAME));
					account.setConnectionState(Short.parseShort(parser.getAttributeValue(XML_NAMESPACE, ATTR_CONNECTION_STATE)));
					account.serviceId = (byte) accounts.size();
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				if (name.equalsIgnoreCase(TAG_ACCOUNT) && account != null) {
					accounts.add(account);
				} else if (name.equalsIgnoreCase(TAG_ACCOUNTS)) {
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}

		return accounts;
	}

	public AccountView getAccount(AccountView account, boolean getBuddies) throws XmlPullParserException, IOException {
		if (account == null) {
			return null;
		}

		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(context.openFileInput(account.getFilename() + PREFERENCES_FILEEXT), XML_ENCODING);

		int eventType = parser.getEventType();
		Buddy buddy = null;
		BuddyGroup group = null;
		boolean done = false;
		while (eventType != XmlPullParser.END_DOCUMENT && !done) {
			String name = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if (name.equalsIgnoreCase(TAG_ACCOUNT)) {
					account.lastUpdateTime = Long.parseLong(parser.getAttributeValue(XML_NAMESPACE, ATTR_LAST_UPDATE));
					account.status = Byte.parseByte(parser.getAttributeValue(XML_NAMESPACE, ATTR_STATUS));
					account.xStatus = Byte.parseByte(parser.getAttributeValue(XML_NAMESPACE, ATTR_XSTATUS));
					account.visibility = Byte.parseByte(parser.getAttributeValue(XML_NAMESPACE, ATTR_VISIBILITY));
				} else if (account != null) {
					if (name.equalsIgnoreCase(TAG_NAME)) {
						if (account != null) {
							account.ownName = parser.nextText();
						}
					} else if (name.equalsIgnoreCase(TAG_XSTATUS_NAME)) {
						if (account != null) {
							account.xStatusName = parser.nextText();
						}
					} else if (name.equalsIgnoreCase(TAG_XSTATUS_TEXT)) {
						if (account != null) {
							account.xStatusText = parser.nextText();
						}
					} else if (name.equalsIgnoreCase(TAG_BUDDY_NAME)) {
						if (buddy != null) {
							buddy.name = parser.nextText();
						}
					} else if (name.equalsIgnoreCase(TAG_GROUP_NAME)) {
						if (group != null) {
							group.name = parser.nextText();
						}
					} else if (name.equalsIgnoreCase(TAG_BUDDY) && getBuddies) {
						buddy = new Buddy(parser.getAttributeValue(XML_NAMESPACE, ATTR_PROTOCOL_UID), account);

						buddy.groupId = Integer.parseInt(parser.getAttributeValue(XML_NAMESPACE, ATTR_GROUP_ID));
						buddy.id = Integer.parseInt(parser.getAttributeValue(XML_NAMESPACE, ATTR_ID));
						buddy.unread = Byte.parseByte(parser.getAttributeValue(XML_NAMESPACE, ATTR_UNREAD));
						buddy.visibility = Byte.parseByte(parser.getAttributeValue(XML_NAMESPACE, ATTR_VISIBILITY));

					} else if (name.equalsIgnoreCase(TAG_GROUP) && getBuddies) {
						group = new BuddyGroup(Integer.parseInt(parser.getAttributeValue(XML_NAMESPACE, ATTR_ID)), account.protocolUid, account.serviceId);
						group.isCollapsed = Boolean.parseBoolean(parser.getAttributeValue(XML_NAMESPACE, ATTR_COLLAPSED));
					}
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				if (name.equalsIgnoreCase(TAG_BUDDY)) {
					if (buddy != null){
						account.getBuddyList().add(buddy);
						buddy = null;
					}
				} else if (name.equalsIgnoreCase(TAG_GROUP)) {
					if (group != null){
						account.getBuddyGroupList().add(group);
						group = null;
					}
				} else if (name.equalsIgnoreCase(TAG_ACCOUNT)) {
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}

		account.options = getOptions(account, context);
		
		return account;
	}

	public void delete(String storageName) {
		context.deleteFile(storageName);
	}
}
