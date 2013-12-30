package ua.snuk182.asia.services;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import ua.snuk182.asia.core.dataentity.Account;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.BuddyGroup;
import ua.snuk182.asia.core.dataentity.TextMessage;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Xml;

public final class Exporter {
	
	private static final Random rand = new Random();
	
	private static final String CHARSET = "UTF-16BE";
	private static final byte[] RECORD_DIVIDER_BYTES;
	private static final String RECORD_DIVIDER = "{}";
	private static final byte[] NEW_LINE_BYTES;
	
	private static final String XML_NAMESPACE = "aceim.app";
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
	//private static final String ATTR_VISIBILITY = "visibility";
	private static final String ATTR_XSTATUS = "xstatus";
	private static final String ATTR_STATUS = "status";
	private static final String ATTR_PROTOCOL_UID = "protocol_uid";
	private static final String ATTR_PROTOCOL_NAME = "protocol_name";
	//private static final String ATTR_PROTOCOL_SERVICE_CLASS_NAME = "protocol_service_class_name";
	private static final String TAG_ACCOUNT = "account";
	private static final String XML_ENCODING = "UTF-16LE";
	
	static {
		RECORD_DIVIDER_BYTES = getBytes(RECORD_DIVIDER);
		NEW_LINE_BYTES = getBytes("\n");
	}

	private final RuntimeService service;
	
	public Exporter(RuntimeService service) {
		this.service = service;
	}

	public void export(final String password){
		Executors.defaultThreadFactory().newThread(new Runnable() {

			@Override
			public void run() {
				zipAndEncode(password);
			}
		}).start();
	}
	
	private static Cipher generateKey(String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {
		byte[] keyStart = password.getBytes("UTF-8");

	    KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
	    sr.setSeed(keyStart);
	    kgen.init(128, sr);
	    SecretKey skey = kgen.generateKey();
	    
	    byte[] key = skey.getEncoded();

		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

		return cipher;
	}
	
	private void zipAndEncode(String password) {
		File target = ServiceUtils.createLocalFileForReceiving("Export " + DateFormat.getLongDateFormat(service.getBaseContext()).format(Calendar.getInstance().getTime()) + ".aceim", 0, 0);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(target);
			CipherOutputStream cos = new CipherOutputStream(new BufferedOutputStream(fos), generateKey(password));
			ZipOutputStream zos = new ZipOutputStream(cos);

			byte[] buffer = new byte[2048];
			
			int progress = 1;
			service.notificator.notifyFileProgress(0, target.getName(), 800, progress, true, null);
			
			List<File> files = new LinkedList<File>();
			
			File total = new File(service.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + ServiceStoredPreferences.XMLPARAMS_TOTAL);
			files.add(total);
			
			for (Account a : service.accounts) {
				File config = new File(service.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + a.accountView.getFilename() + ServiceStoredPreferences.PREFERENCES_FILEEXT);
				File icon = new File(service.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + a.accountView.getFilename() + Buddy.BUDDYICON_FILEEXT);
				
				files.add(icon);
				files.add(config);
				
				for (Buddy b : a.accountView.getBuddyList()) {
					File bicon = new File(service.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + b.getFilename() + Buddy.BUDDYICON_FILEEXT);
					File bhistory = new File(service.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + b.getOwnerAccountId()+" "+b.protocolUid + HistorySaver.SUFFIX);
					
					files.add(bicon);
					files.add(bhistory);
				}
			}

			ServiceUtils.log("Going to zip: " + files);
			
			for (File file : files) {
				if (!file.exists()) {
					continue;
				}
				
				File tmpFile = convertToAceImFormat(file);				
				
				FileInputStream in = new FileInputStream(tmpFile);
				zos.putNextEntry(new ZipEntry(tmpFile.getName()));

				ServiceUtils.log("Zipping: " + tmpFile);

				service.notificator.notifyFileProgress(0, target.getName(), 800, ++progress, true, null);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				zos.closeEntry();
				in.close();
				
				tmpFile.delete();
			}
			
			zos.close();
			
			service.notificator.notifyFileProgress(0, target.getName(), 800, 800, true, null);

			ServiceUtils.log("Export succeded to: " + target);
		} catch (Exception e) {
			ServiceUtils.log(e);
			
			service.notificator.notifyFileProgress(0, target.getName(), 0, 0, true, e.getMessage());

			try {
				if (fos != null) {
					fos.close();
				}
				
				target.delete();
			} catch (IOException e1) {
				ServiceUtils.log(e1);
			}
			
		}
	}

	private File convertToAceImFormat(File file) throws IllegalArgumentException, IllegalStateException, IOException {
		
		new File(Environment.getExternalStorageDirectory() + File.separator + "AceIM").mkdirs();
		
		File target;
		if (file.getName().endsWith(".history")) {
			target = new File(Environment.getExternalStorageDirectory() + File.separator + "AceIM" + File.separator + file.getName());
			convertHistory(file, target);
		} else if (file.getName().endsWith(".preferences")) {
			String[] pathParts = file.getName().split(" "); 
			target = new File(Environment.getExternalStorageDirectory() + File.separator + "AceIM" + File.separator + pathParts[0] + " " + pathParts[1] + ".preferences");
			convertPrefs(file, target);
		} else if (file.getName().equals(ServiceStoredPreferences.XMLPARAMS_TOTAL)) {
			target = new File(Environment.getExternalStorageDirectory() + File.separator + "AceIM" + File.separator + file.getName());
			saveAccountHeaders(target);
		} else {
			target = file;
		}
		
		return target;
	}

	private void convertPrefs(File source, File target) {
		for (Account account : service.accounts) {
			if (source.getName().equals(account.accountView.getFilename() + ServiceStoredPreferences.PREFERENCES_FILEEXT)) {
				saveAccountInternal(account, target);
				break;
			}
		}	
	}
	
	private synchronized void saveAccountHeaders(File target) throws IllegalArgumentException, IllegalStateException, FileNotFoundException, IOException {
		XmlSerializer serializer = Xml.newSerializer();
		serializer.setOutput(new BufferedOutputStream(new FileOutputStream(target)), XML_ENCODING);
		serializer.startDocument(XML_ENCODING, true);

		serializer.startTag(XML_NAMESPACE, TAG_ACCOUNTS);

		for (Account account : service.accounts) {
			serializer.startTag(XML_NAMESPACE, TAG_ACCOUNT);
			serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_UID, account.accountView.protocolUid);
			serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_NAME, account.accountView.protocolName);
			//serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_SERVICE_CLASS_NAME, account.getProtocolServicePackageName());
			//serializer.attribute(XML_NAMESPACE, ATTR_CONNECTION_STATE, Integer.toString(account.getConnectionState().ordinal()));
			serializer.endTag(XML_NAMESPACE, TAG_ACCOUNT);
		}
		serializer.endTag(XML_NAMESPACE, TAG_ACCOUNTS);
		serializer.endDocument();
	}
	
	private void saveAccountInternal(Account account, File target) {
		XmlSerializer serializer = Xml.newSerializer();
		try {
			serializer.setOutput(new BufferedOutputStream(new FileOutputStream(target), ServiceStoredPreferences.getAccessMode()), XML_ENCODING);
			serializer.startDocument(XML_ENCODING, true);
			serializer.startTag(XML_NAMESPACE, TAG_ACCOUNT);
			serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_NAME, account.accountView.protocolName.trim());
			//serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_SERVICE_CLASS_NAME, account.getProtocolServicePackageName().trim());
			serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_UID, account.accountView.protocolUid.trim());
			serializer.attribute(XML_NAMESPACE, ATTR_STATUS, Byte.toString(account.accountView.status));
			serializer.attribute(XML_NAMESPACE, ATTR_XSTATUS, Byte.toString(account.accountView.xStatus));
			/*if (account.getOnlineInfo().getAccountVisibility() != null) {
				serializer.attribute(XML_NAMESPACE, ATTR_VISIBILITY, Integer.toString(account.getOnlineInfo().getAccountVisibility().ordinal()));
			}*/
			
			if (account.accountView.ownName != null) {
				serializer.startTag(XML_NAMESPACE, TAG_NAME);
				serializer.text(account.accountView.ownName);
				serializer.endTag(XML_NAMESPACE, TAG_NAME);
			}

			if (account.accountView.xStatusName != null) {
				serializer.startTag(XML_NAMESPACE, TAG_XSTATUS_NAME);
				serializer.text(account.accountView.xStatusName);
				serializer.endTag(XML_NAMESPACE, TAG_XSTATUS_NAME);				
			}
			
			if (account.accountView.xStatusText != null) {
				serializer.startTag(XML_NAMESPACE, TAG_XSTATUS_TEXT);
				serializer.text(account.accountView.xStatusText);
				serializer.endTag(XML_NAMESPACE, TAG_XSTATUS_TEXT);
			}

			serializer.startTag(XML_NAMESPACE, TAG_GROUPS);
			serializer.attribute(XML_NAMESPACE, ATTR_GROUPS, Integer.toString(account.accountView.getBuddyGroupList().size()));
			for (BuddyGroup group : account.accountView.getBuddyGroupList()) {
				serializer.startTag(XML_NAMESPACE, TAG_GROUP);
				serializer.attribute(XML_NAMESPACE, ATTR_ID, Integer.toString(group.id));
				serializer.attribute(XML_NAMESPACE, ATTR_COLLAPSED, Boolean.toString(group.isCollapsed));

				serializer.startTag(XML_NAMESPACE, TAG_GROUP_NAME);
				serializer.text(group.name);
				serializer.endTag(XML_NAMESPACE, TAG_GROUP_NAME);
				
				serializer.startTag(XML_NAMESPACE, TAG_BUDDIES);
				
				List<Buddy> buddies = account.accountView.getBuddiesForGroup(group);
				
				serializer.attribute(XML_NAMESPACE, ATTR_BUDDIES, Integer.toString(buddies.size()));
				
				for (Buddy buddy : buddies) {
					serializer.startTag(XML_NAMESPACE, TAG_BUDDY);
					serializer.attribute(XML_NAMESPACE, ATTR_PROTOCOL_UID, buddy.protocolUid.trim());
					serializer.attribute(XML_NAMESPACE, ATTR_GROUP_ID, Integer.toString(buddy.groupId));
					serializer.attribute(XML_NAMESPACE, ATTR_ID, Integer.toString(buddy.id));
					serializer.attribute(XML_NAMESPACE, ATTR_UNREAD, Byte.toString(buddy.unread));
					/*if (buddy.getOnlineInfo().getBuddyVisibility() != null) {
						serializer.attribute(XML_NAMESPACE, ATTR_VISIBILITY, Integer.toString(buddy.getOnlineInfo().getBuddyVisibility().ordinal()));
					}
					*/
					if (buddy.getName() != null) {
						serializer.startTag(XML_NAMESPACE, TAG_BUDDY_NAME);
						serializer.text(buddy.getName());
						serializer.endTag(XML_NAMESPACE, TAG_BUDDY_NAME);
					}
					serializer.endTag(XML_NAMESPACE, TAG_BUDDY);
				}
				serializer.endTag(XML_NAMESPACE, TAG_BUDDIES);

				serializer.endTag(XML_NAMESPACE, TAG_GROUP);
			}
			serializer.endTag(XML_NAMESPACE, TAG_GROUPS);
			serializer.endTag(XML_NAMESPACE, TAG_ACCOUNT);
			serializer.endDocument();
		} catch (Exception e) {
			ServiceUtils.log(e);
		}
	}

	private void convertHistory(File source, File target) throws FileNotFoundException {
		BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(source)));
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(target));
		
		String sourceName = source.getName();
		convertMessages(is, os, sourceName.substring(0, sourceName.lastIndexOf(".history")).split(" "));
		
		try {
			os.close();
		} catch (IOException e) {
			ServiceUtils.log(e);
		}
		try {
			is.close();
		} catch (IOException e) {
			ServiceUtils.log(e);
		}
	}
	
	private void convertMessages(BufferedReader br, BufferedOutputStream os, String[] split) {
		String strLine = "";
		StringBuilder sb = new StringBuilder();
		TextMessage tm = null;
		try {
			 while ((strLine = br.readLine()) != null){
				 if (strLine.startsWith(HistorySaver.RECORD_DIVIDER)) {
					 
					 if (tm != null) {
						 tm.text = sb.toString();
						 sb = new StringBuilder();
						 saveMessage(split[2], tm, os);
					 }
					 
					 if (strLine.equals(HistorySaver.RECORD_DIVIDER + HistorySaver.MARK_IN)) {
						 tm = new TextMessage(split[2]);
					 } else if (strLine.equals(HistorySaver.RECORD_DIVIDER + HistorySaver.MARK_OUT)) {
						 tm = new TextMessage(split[0]);
					 }
				 } else {
					 if (tm != null) {
						 tm.messageId = rand.nextLong();
						 
						 int dateStart = strLine.indexOf(" (");
						 int dateEnd = strLine.indexOf("):");
						 
						 if (dateStart > -1 && dateEnd > -1 && dateEnd > dateStart) {
							 String date = strLine.substring(dateStart + 2, dateEnd);
							 try {
								tm.time = ServiceUtils.DATE_FORMATTER.parse(date);
							} catch (ParseException e) {
								tm.time = Calendar.getInstance().getTime();
							}
							 
							 sb.append(strLine.substring(dateEnd + 2));
							 tm.writerUid = strLine.substring(0, dateStart);
						 } else {
							 sb.append(strLine);
						 }
					 }
				 }
			 }
		} catch (IOException e) {	
			ServiceUtils.log(e);
		}
	}

	private void saveMessage(String from, TextMessage message, BufferedOutputStream stream) {
		JSONObject o;
		try {
			o = HistoryObject.fromMessage(message, from);
		} catch (JSONException e) {
			ServiceUtils.log(e);
			o = new JSONObject();
		}
		
		byte[] buffer = getBytes(o.toString());

		try {
			stream.write(RECORD_DIVIDER_BYTES);
			stream.write(NEW_LINE_BYTES);
			stream.write(buffer);
			stream.write(NEW_LINE_BYTES);					
		} catch (IOException e) {
			ServiceUtils.log(e);
		} 
	}

	private static final byte[] getBytes(String string) {
		try {
			return string.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			ServiceUtils.log(CHARSET + " is unsupported, using " + Charset.defaultCharset() + " instead");
			return string.getBytes();
		}
	}

	private static final class HistoryObject extends JSONObject {
		
		private HistoryObject(){
			super();
		}
		
		private HistoryObject(String str) throws JSONException {
			super(str);
		}
		
		//writerUid == from -> incoming
		static HistoryObject fromMessage(TextMessage message, String from) throws JSONException{
			HistoryObject o = new HistoryObject();
			
			o.put("type", "TEXT");
			o.put("contact-name", message.writerUid != null ? message.writerUid : message.from);
			o.put("incoming", from.equals(message.from));
			o.put("text", message.text);
			o.put("message-id", message.hashCode());
			o.put("time", message.time.getTime());
			
			return o;
		}
	}
}
