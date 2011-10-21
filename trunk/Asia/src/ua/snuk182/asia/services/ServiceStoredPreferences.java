package ua.snuk182.asia.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.snuk182.asia.core.dataentity.Account;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public final class ServiceStoredPreferences {
	
	private static final String CONTACT_OPTION_DIVIDER = " !div! ";
	private static final String CONTACT_ITEM_DIVIDER = " asiaacc ";
	private static final String SAVEDPARAM_ACCOUNTS = "AsiaSavedAccounts";
	private static final String SAVEDPARAM_ACCOUNT_PREFERENCES = "AsiaAccountPreferences";
	private static final String SAVEDPARAM_ACCOUNT_XSTATUSNAME = "AsiaAccountXStatusName";
	private static final String SAVEDPARAM_ACCOUNT_XSTATUSVALUE = "AsiaAccountXStatusValue";
	private static final String SAVEDPARAM_ACCOUNT_CONTACTS = "AsiaAccountContacts";
	private static final String SAVEDPARAM_ACCOUNT_GROUPS = "AsiaAccountGroups";
	//private static final String SAVEDPARAM_ACCOUNT_GROUP = "AccountGroup";
	private static final String SAVEDPARAMS_TOTAL = "AsiaTotalParams";
	private static final String SAVEDPARAM_ACCOUNT_PW = "AsiaAccountPw";
	
	private Context context;
	
	public ServiceStoredPreferences(Context context){
		this.context = context;
	}
	
	public synchronized void saveMap(final Map<String, String> map, final String storageName){
		
		new Thread("Preference saver "+storageName){
			@Override
			public void run(){
				SharedPreferences.Editor preferences = context.getSharedPreferences(storageName, 0).edit();
				
				for (String key:map.keySet()){
					preferences.putString(key, map.get(key));
				}
				
				preferences.commit();
			}
		}.start();
	}
	
	public static String getOption(Context context, String key){
		SharedPreferences soptions = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
		return soptions.getString(key, "false");
	}
	
	public Bundle getApplicationOptions(){
		return getOptions(null, context);
	}
		
	public synchronized Map<String,String> getMap(Set<String> keys, String storageName){
		if (keys==null){
			return null;
		}
		
		if (storageName==null){
			storageName = SAVEDPARAMS_TOTAL;
		}
		Map<String,String> map = new HashMap<String, String>();
		SharedPreferences preferences = context.getSharedPreferences(storageName, 0);
		
		for (String key:keys){
			map.put(key, preferences.getString(key, null));			
		}
		
		return map;
	}
	
	public List<AccountView> getAccounts() {
		SharedPreferences preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
		List<AccountView> protocols = new ArrayList<AccountView>();
		String[] accounts = preferences.getString(SAVEDPARAM_ACCOUNTS, "").split(CONTACT_ITEM_DIVIDER);
		if (accounts == null){
			accounts = new String[]{};
		}
		for (String account:accounts){
			if (account.indexOf(" ")<0){
				continue;
			}		
			AccountView paccount = getAccount(account, (byte) protocols.size());
			protocols.add(paccount);
		}
		
		return protocols;
	}

	public void saveServiceState(List<Account> accounts){
		SharedPreferences preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
		SharedPreferences.Editor editor = preferences.edit();
		StringBuilder sb = new StringBuilder();
		for (int i=0; i< accounts.size(); i++){
			Account a = accounts.get(i);
			sb.append(a.accountView.getAccountId());
			sb.append(" ");
			sb.append(a.accountView.getConnectionState());
			if (i < accounts.size()-1){
				sb.append(CONTACT_ITEM_DIVIDER);
			}
		}
		
		editor.putString(SAVEDPARAM_ACCOUNTS, sb.toString());
		editor.commit();
	}
	
	public void saveAccount(final AccountView origin){
		if (origin == null){
			return;
		}
		
		final AccountView account = new AccountView();
		account.merge(origin);
		
		new Thread("Account saver "+account.getAccountId()){
			
			@Override
			public void run(){
				SharedPreferences preferences = context.getSharedPreferences(account.getAccountId(), 0);
				SharedPreferences.Editor editor = preferences.edit();		
				
				//editor.putString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_PW, account.getPassword());
				String prefsString = (account.ownName!=null?account.ownName:"")+CONTACT_ITEM_DIVIDER+account.status+CONTACT_ITEM_DIVIDER+account.xStatus;
				editor.putString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_PREFERENCES, prefsString);
				editor.putString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_XSTATUSNAME, account.xStatusName);
				editor.putString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_XSTATUSVALUE, account.xStatusText);
				
				StringBuilder accountGroups = new StringBuilder();
				for (int i=0; i<account.getBuddyGroupList().size(); i++){
					accountGroups.append(account.getBuddyGroupList().get(i).id);
					accountGroups.append(CONTACT_OPTION_DIVIDER+account.getBuddyGroupList().get(i).name);
					accountGroups.append(CONTACT_OPTION_DIVIDER+account.getBuddyGroupList().get(i).isCollapsed);
					if (i < account.getBuddyGroupList().size()-1) {
						accountGroups.append(CONTACT_ITEM_DIVIDER);
					}
				}
				editor.putString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_GROUPS, accountGroups.toString());
				
				StringBuilder accountContacts = new StringBuilder();
				for (int i=0; i<account.getBuddyList().size(); i++){
					accountContacts.append(account.getBuddyList().get(i).protocolUid);
					accountContacts.append(CONTACT_OPTION_DIVIDER+((account.getBuddyList().get(i).getName()!=null)?account.getBuddyList().get(i).getName():""));
					accountContacts.append(CONTACT_OPTION_DIVIDER+account.getBuddyList().get(i).unread);
					accountContacts.append(CONTACT_OPTION_DIVIDER+account.getBuddyList().get(i).groupId);
					accountContacts.append(CONTACT_OPTION_DIVIDER+account.getBuddyList().get(i).iconHash);
					accountContacts.append(CONTACT_OPTION_DIVIDER+account.getBuddyList().get(i).visibility);
					if (i < account.getBuddyList().size()-1) {
						accountContacts.append(CONTACT_ITEM_DIVIDER);
					}	
				}
				editor.putString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_CONTACTS, accountContacts.toString());
				/*Set<String> keys = account.options.keySet();
				StringBuilder optionsKeys = new StringBuilder();
				for (String key:keys){
					editor.putString(SAVEDPARAM_ACCOUNT_OPTIONS+" "+key, account.options.getString(key));
					optionsKeys.append(key+" ");
				}
				editor.putString(SAVEDPARAM_ACCOUNT_OPTIONS, optionsKeys.toString());*/
				editor.commit();
				
				preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
				editor = preferences.edit();
				String accounts = preferences.getString(SAVEDPARAM_ACCOUNTS, null);
				String accountUid = account.getAccountId();
				
				if (accounts==null || accounts.indexOf(accountUid)<0){
					if (accounts !=null && accounts.length()>3){
						accounts += CONTACT_ITEM_DIVIDER;
					} else {
						accounts = "";
					}	
					
					accounts+=accountUid;
					
					editor.putString(SAVEDPARAM_ACCOUNTS, accounts);
					editor.commit();
				}				
			}
		}.start();
	}
	
	public AccountView getAccount(String accountId, byte serviceId){
		if (accountId == null) return null;
		
		String[] accountAttrs = accountId.split(" ");
		String accountUid = accountAttrs[0];
		String accountProtocol = accountAttrs[1];		
		
		AccountView account = new AccountView(accountUid, accountProtocol);
		account.serviceId = serviceId;
		if (accountAttrs.length > 2){
			account.setConnectionState(Short.parseShort(accountAttrs[2]));
		}
		
		SharedPreferences preferences = context.getSharedPreferences(account.getAccountId(), 0);	
		
		/*String optionNames = preferences.getString(SAVEDPARAM_ACCOUNT_OPTIONS, "");
		String[] options = optionNames.split(" ");
		Bundle bu = new Bundle();
		if (options!=null && options.length>0){
			for (String option:options){
				String optionValue = preferences.getString(SAVEDPARAM_ACCOUNT_OPTIONS+" "+option, null);
				if (optionValue != null){
					bu.putString(option, optionValue);
				}
			}
		}
		account.options = bu;*/
		
		String accountPreferences = preferences.getString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_PREFERENCES, "");
		
		if (accountPreferences.indexOf(CONTACT_ITEM_DIVIDER)>-1){
			String[] accountPreferencesArray = accountPreferences.split(CONTACT_ITEM_DIVIDER);
			account.ownName = (accountPreferencesArray[0].length()>0?accountPreferencesArray[0]:accountUid);
			account.status = Byte.parseByte(accountPreferencesArray[1]);
			account.xStatus = Byte.parseByte(accountPreferencesArray[2]);
			account.xStatusName = preferences.getString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_XSTATUSNAME, "");
			account.xStatusText = preferences.getString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_XSTATUSVALUE, "");
		}
		String accountContacts = preferences.getString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_CONTACTS, "");
		if (accountContacts.indexOf(CONTACT_ITEM_DIVIDER)>-1){
			String[] contacts = accountContacts.split(CONTACT_ITEM_DIVIDER);
			for (String contact:contacts){
				if (contact.length()<1){
					continue;
				}
				String[] contactAttrs = contact.split(CONTACT_OPTION_DIVIDER);
				Buddy buddy = new Buddy(contactAttrs[0], account);
				buddy.name = contactAttrs[1];
				buddy.ownerUid = account.protocolUid;
				
				if (contactAttrs.length>2){
					buddy.unread = Byte.parseByte(contactAttrs[2]);
				}
				
				if (contactAttrs.length>3){
					buddy.groupId = Integer.parseInt(contactAttrs[3]);
				}
				
				if (contactAttrs.length>4){
					buddy.iconHash = contactAttrs[4];
				}
				
				if (contactAttrs.length>5){
					buddy.visibility = Byte.parseByte(contactAttrs[5]);
				}
				
				buddy.serviceId = account.serviceId;
				
				account.getBuddyList().add(buddy);
			}
		} else if (accountContacts.indexOf(CONTACT_OPTION_DIVIDER)>-1){
			String[] contactAttrs = accountContacts.split(CONTACT_OPTION_DIVIDER);
			Buddy buddy = new Buddy(contactAttrs[0], account);
			buddy.name = contactAttrs[1];
			buddy.ownerUid = account.protocolUid;
			
			if (contactAttrs.length>2){
				buddy.unread = Byte.parseByte(contactAttrs[2]);
			}
			
			if (contactAttrs.length>3){
				buddy.groupId = Integer.parseInt(contactAttrs[3]);
			}
			
			if (contactAttrs.length>4){
				buddy.iconHash = contactAttrs[4];
			}
			
			if (contactAttrs.length>5){
				buddy.visibility = Byte.parseByte(contactAttrs[5]);
			}
			
			account.getBuddyList().add(buddy);
		}
		
		String accountContactGroups = preferences.getString(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_GROUPS, "");
		if (accountContactGroups.indexOf(CONTACT_ITEM_DIVIDER)>-1){
			String[] groups = accountContactGroups.split(CONTACT_ITEM_DIVIDER);
			for (String group:groups){
				if (group.length()<1){
					continue;
				}
				
				String[] groupItems = group.split(CONTACT_OPTION_DIVIDER);
				
				BuddyGroup buddyGroup = new BuddyGroup(Integer.parseInt(groupItems[0]), account.getAccountId(), account.serviceId);
				buddyGroup.name = groupItems[1];
				if (groupItems.length > 2){
					buddyGroup.isCollapsed = Boolean.parseBoolean(groupItems[2]);
				}
				buddyGroup.ownerUid = account.getAccountId();				
				account.getBuddyGroupList().add(buddyGroup);
			}
		} else if(accountContactGroups.indexOf(CONTACT_OPTION_DIVIDER)>-1){
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
		if (account != null){
			file = account.getAccountId();
		} else {
			file = SAVEDPARAMS_TOTAL;
		}
		SharedPreferences soptions = context.getSharedPreferences(file, 0);
		Bundle bbu = new Bundle();
		Map<String, ?> map = soptions.getAll();
		for (String key : map.keySet()) {
			bbu.putString(key, (String) map.get(key));
		}
		return bbu;
	}

	public Bitmap getBitmapFromLocalFile(String filename){
		filename = filename+Buddy.BUDDYICON_FILEEXT;
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(filename);
		} catch (FileNotFoundException e) {
		}
		
		if (fis == null) return null;
		
		return BitmapFactory.decodeStream(fis);
	}
	
	public void saveBinaryFile(String filename, byte[] contents, Runnable runOnFinish){
		new FileAsyncSaver(filename, contents, runOnFinish).start();		
	}
	
	public void saveIcon(String filename, byte[] contents, Runnable runOnFinish){
		saveBinaryFile(filename+Buddy.BUDDYICON_FILEEXT, contents, runOnFinish);
	}
	
	class FileAsyncSaver extends Thread {
		
		String fileName;
		byte[] contents;
		Runnable runOnFinish;
		
		public FileAsyncSaver(String fileName, byte[] contents, Runnable runOnFinish){
			this.fileName = fileName;
			this.contents = contents;
			this.runOnFinish = runOnFinish;
			setName("Preferences saver "+fileName);
		}
		
		@Override
		public void run(){
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
	
	public void savePreference(final String key, final String value, final AccountView account){
		new Thread("Single preference saver"){
			
			@Override
			public void run(){
				SharedPreferences preferences;
				if (account!=null){
					preferences = context.getSharedPreferences(account.getAccountId(), 0);
				}else{
					preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
				}
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(key, value);
				editor.commit();
			}
		}.start();
	}

	public void removeAccount(AccountView account) {
		SharedPreferences preferences = context.getSharedPreferences(SAVEDPARAMS_TOTAL, 0);
		SharedPreferences.Editor editor = preferences.edit();
		String[] accounts = preferences.getString(SAVEDPARAM_ACCOUNTS, "").split(CONTACT_ITEM_DIVIDER);
		
		StringBuilder newAccounts = new StringBuilder("");
		for (int i=0; i<accounts.length; i++){
			if (!accounts[i].equals(account.getAccountId())){
				newAccounts.append(accounts[i]);
				if (i<accounts.length-1){
					newAccounts.append(CONTACT_ITEM_DIVIDER);
				}
			}
		}
		editor.putString(SAVEDPARAM_ACCOUNTS, newAccounts.toString());
		editor.commit();
		
		//=========================================================================================DELETE
		editor.remove(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_PW);
		editor.remove(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_PREFERENCES);
		editor.remove(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_XSTATUSNAME);
		editor.remove(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_XSTATUSVALUE);
		
		/*String optionNames = preferences.getString(SAVEDPARAM_ACCOUNT_OPTIONS, "");
		String[] options = optionNames.split(" ");
		if (options!=null && options.length>0){
			for (String option:options){
				editor.remove(SAVEDPARAM_ACCOUNT_OPTIONS+" "+option);
			}
		}
		
		editor.remove(SAVEDPARAM_ACCOUNT_OPTIONS);*/
		
		context.deleteFile(account.getAccountId());
		
		editor.remove(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_GROUPS);
		
		editor.remove(account.getAccountId()+" "+SAVEDPARAM_ACCOUNT_CONTACTS);
		editor.commit();
		//============================================================================================
		context.deleteFile(account.getAccountId());
		
		context.deleteFile(account.getAccountId()+" "+IAccountServiceResponse.SHARED_PREFERENCES);
	}

	public void delete(String groupchatStorageName) {
		context.deleteFile(groupchatStorageName);
	}
}
