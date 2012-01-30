package ua.snuk182.asia.services;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.OnlineInfo;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.icq.ICQServiceUtils;
import ua.snuk182.asia.services.mrim.MrimServiceUtils;
import ua.snuk182.asia.services.xmpp.XMPPServiceUtils;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public final class ServiceUtils {

	final static DateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

	public static final Set<String> GROUPCHAT_PREFERENCE_MAP;
	public static boolean logToFile = false;

	static {
		Set<String> set = new HashSet<String>();
		set.add(ServiceConstants.GROUPCHAT_PREFERENCE_NICKNAME);
		set.add(ServiceConstants.GROUPCHAT_PREFERENCE_PASSWORD);
		GROUPCHAT_PREFERENCE_MAP = Collections.unmodifiableSet(set);
	}

	public static Buddy mergeBuddyWithOnlineInfo(Buddy buddy, OnlineInfo info) {
		if (buddy == null || info == null)
			return null;

		buddy.status = info.userStatus;
		buddy.externalIP = info.extIP;
		// buddy.capabilities = info.capabilities;
		buddy.canFileShare = info.canFileShare;
		if (buddy.visibility == Buddy.VIS_NOT_AUTHORIZED && info.visibility != Buddy.VIS_NOT_AUTHORIZED && info.userStatus != Buddy.ST_OFFLINE) {
			buddy.visibility = info.visibility;
		}
		buddy.signonTime = info.signonTime;
		buddy.onlineTime = info.onlineTime;
		buddy.xstatus = info.xstatus;
		if (info.xstatusName != null) {
			buddy.xstatusName = info.xstatusName;
			buddy.xstatusDescription = info.xstatusDescription;
		}
		return buddy;
	}

	public static void log(String string, AccountView account, boolean isError) {
		if (isError) {
			Log.w("Asia" + ((account != null) ? " " + account.getAccountId() : ""), string);
		}

		if (logToFile) {
			if (!isError) {
				Log.d("Asia" + ((account != null) ? " " + account.getAccountId() : ""), string);
			}

			String storageState = Environment.getExternalStorageState();
			if (storageState.equals(Environment.MEDIA_MOUNTED)) {
				try {
					File root = Environment.getExternalStorageDirectory();
					File downloads = new File(root, "Asia");
					downloads.mkdirs();
					File logFile;
					if (account != null) {
						logFile = new File(downloads, "Asia " + account.getAccountId() + ".log");
					} else {
						logFile = new File(downloads, "Asia.log");
					}

					if (!logFile.exists()) {
						logFile.createNewFile();
					}
					FileOutputStream fos = new FileOutputStream(logFile, true);
					fos.write(new String(new Date() + " --> " + string + "\n").getBytes());
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void log(String string) {
		log(string, null, false);
	}

	public static void log(Throwable e, AccountView account) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.toString());
		for (StackTraceElement el : e.getStackTrace()) {
			sb.append("\n" + el);
		}
		log(sb.toString(), account, true);
	}

	public static void log(Throwable e) {
		log(e, null);
	}

	public static Bitmap blur(Bitmap original) {
		Bitmap scaled = Bitmap.createScaledBitmap(original, original.getWidth() / 2, original.getHeight() / 2, true);
		Bitmap blurred = Bitmap.createScaledBitmap(scaled, original.getWidth(), original.getHeight(), true);
		return blurred;
	}

	public static TextMessage serviceMessage2TextMessage(ServiceMessage msg) {
		if (msg == null)
			return null;
		TextMessage message = new TextMessage(msg.from);
		message.from = msg.from;
		message.text = msg.text;
		message.time = msg.time;
		message.serviceId = msg.serviceId;
		message.to = msg.type;
		return message;
	}

	public static int getMenuResIdByAccount(Context context, AccountView account) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getMenuResIdByAccount(account);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getMenuResIdByAccount(account);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getMenuResIdByAccount(account);
		}
		return 0;
	}

	public static int getStatusResIdByAccountTiny(Context context, AccountView account, boolean withoutConnectionState) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByAccountTiny(account, withoutConnectionState);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByAccountTiny(account, withoutConnectionState);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByAccountTiny(account, withoutConnectionState);
		}
		return 0;
	}

	public static int getStatusResIdByBuddyTiny(Context context, Buddy buddy) {
		if (buddy.serviceName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByStatusIdTiny(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByStatusIdTiny(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByStatusIdTiny(buddy.status);
		}
		return 0;
	}

	public static int getStatusResIdByAccountSmall(Context context, AccountView account, boolean withoutConnectionState) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByAccountSmall(account, withoutConnectionState);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByAccountSmall(account, withoutConnectionState);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByAccountSmall(account, withoutConnectionState);
		}
		return 0;
	}

	public static int getStatusResIdByBuddyMedium(Context context, Buddy buddy) {
		if (buddy.serviceName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByStatusIdMedium(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByStatusIdMedium(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByStatusIdMedium(buddy.status);
		}
		return 0;
	}

	public static int getStatusResIdByAccountMedium(Context context, AccountView account, boolean withoutConnectionState) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByAccountMedium(account, withoutConnectionState);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByAccountMedium(account, withoutConnectionState);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByAccountMedium(account, withoutConnectionState);
		}
		return 0;
	}

	public static int getStatusResIdByBuddySmall(Context context, Buddy buddy) {
		if (buddy.serviceName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByStatusIdSmall(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByStatusIdSmall(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByStatusIdSmall(buddy.status);
		}
		return 0;
	}

	/*
	 * public static int getStatusResIdByAccountBig(Context context, AccountView
	 * account, int size, boolean withoutConnectionState) { if
	 * (account.protocolName
	 * .equals(context.getResources().getString(R.string.icq_service_name))){
	 * return ICQService.getStatusResIdByAccountBig(account,
	 * withoutConnectionState); } if
	 * (account.protocolName.equals(context.getResources
	 * ().getString(R.string.xmpp_service_name))){ return
	 * XMPPService.getStatusResIdByAccountBig(account, withoutConnectionState);
	 * } if
	 * (account.protocolName.equals(context.getResources().getString(R.string
	 * .mrim_service_name))){ return
	 * MrimService.getStatusResIdByAccountBig(account, withoutConnectionState);
	 * } return 0; }
	 */
	public static int getStatusResIdByBuddyBigger(Context context, Buddy buddy) {
		if (buddy.serviceName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByStatusIdBigger(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByStatusIdBigger(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByStatusIdBigger(buddy.status);
		}
		return 0;
	}

	public static int getStatusResIdByBuddyBig(Context context, Buddy buddy) {
		if (buddy.serviceName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIdByStatusIdBig(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIdByStatusIdBig(buddy.status);
		}
		if (buddy.serviceName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIdByStatusIdBig(buddy.status);
		}
		return 0;
	}

	public static String[] getStatusNamesByAccount(AccountView account, Context context) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusListNames(context);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusListNames(context);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusListNames(context);
		}
		return null;
	}

	public static byte getStatusValueByCount(Context context, AccountView account, int which) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusValueByCount(which);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusValueByCount(which);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusValueByCount(which);
		}
		return 0;
	}

	public static TypedArray getXStatusArray(Context context, String protocolName) {
		if (protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return context.getResources().obtainTypedArray(R.array.icq_xstatus_names);
		}
		if (protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return context.getResources().obtainTypedArray(R.array.mrim_xstatus_names);
		}
		return null;
	}

	public static TypedArray getXStatusArray32(Context context, String protocolName) {
		if (protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return context.getResources().obtainTypedArray(R.array.icq_xstatus_names_32);
		}
		if (protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return context.getResources().obtainTypedArray(R.array.mrim_xstatus_names_32);
		}
		return null;
	}

	public static TypedArray getStatusResIdsByAccount(AccountView account, Context context) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return ICQServiceUtils.getStatusResIds(context);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return XMPPServiceUtils.getStatusResIds(context);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return MrimServiceUtils.getStatusResIds(context);
		}
		return null;
	}

	public static String ipAddressToString(int addr) {
		StringBuffer buf = new StringBuffer();
		buf.append(addr & 0xff).append('.').append((addr >>>= 8) & 0xff).append('.').append((addr >>>= 8) & 0xff).append('.').append((addr >>>= 8) & 0xff);
		return buf.toString();
	}

	public static byte[] ipString2ByteBE(String rvIp) {
		if (rvIp == null) {
			return new byte[0];
		}
		String[] bytes = rvIp.split("\\.");
		byte[] ip = new byte[bytes.length];

		for (int i = 0; i < bytes.length; i++) {
			try {
				ip[i] = (byte) Integer.parseInt(bytes[i]);
			} catch (NumberFormatException e) {
				ip[i] = 0;
			}
		}

		return ip;
	}

	public static byte[] getIPBytesFromSystemIp(int systemIp) {
		String ipBinary = Integer.toBinaryString(systemIp);

		byte[] out;

		if (ipBinary.length() < 33) {
			// get the four different parts
			String a = ipBinary.substring(ipBinary.length() - 8, ipBinary.length() - 0);
			String b = ipBinary.substring(ipBinary.length() - 16, ipBinary.length() - 8);
			String c = ipBinary.substring(ipBinary.length() - 24, ipBinary.length() - 16);
			String d = ipBinary.substring(0, ipBinary.length() - 24);

			out = new byte[4];
			out[0] = (byte) Integer.parseInt(d);
			out[1] = (byte) Integer.parseInt(c);
			out[2] = (byte) Integer.parseInt(b);
			out[3] = (byte) Integer.parseInt(a);
		} else {
			// get the six different parts
			String a = ipBinary.substring(ipBinary.length() - 8, ipBinary.length() - 0);
			String b = ipBinary.substring(ipBinary.length() - 16, ipBinary.length() - 8);
			String c = ipBinary.substring(ipBinary.length() - 24, ipBinary.length() - 16);
			String d = ipBinary.substring(ipBinary.length() - 32, ipBinary.length() - 24);
			String e = ipBinary.substring(ipBinary.length() - 40, ipBinary.length() - 32);
			String f = ipBinary.substring(0, ipBinary.length() - 40);

			out = new byte[6];
			out[0] = (byte) Integer.parseInt(f);
			out[1] = (byte) Integer.parseInt(e);
			out[2] = (byte) Integer.parseInt(d);
			out[3] = (byte) Integer.parseInt(c);
			out[4] = (byte) Integer.parseInt(b);
			out[5] = (byte) Integer.parseInt(a);
		}

		return out;
	}

	public static int getPreferencesIdByAccount(Context context, AccountView account) {
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return R.xml.preferences_icq;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))) {
			return R.xml.preferences_xmpp;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			return R.xml.preferences_mrim;
		}
		return 0;
	}

	public static AccountView editXStatusWithPlayed(Context context, AccountView account, Object[] properties) {
		if (properties == null || properties.length < 1) {
			return account;
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			account.xStatus = 10;
			account.xStatusName = (String) properties[0];
			account.xStatusText = "";
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))) {
			account.xStatus = 24;
			account.xStatusName = (String) properties[0];
			account.xStatusText = "";
		}
		return account;
	}

	public static final TypedArray getSmileyResIdsByHeight(Context context, float textSize) {
		if (textSize < 12) {
			return context.getResources().obtainTypedArray(R.array.smiley_values_12);
		} else if (textSize < 18) {
			return context.getResources().obtainTypedArray(R.array.smiley_values_18);
		} else if (textSize < 24) {
			return context.getResources().obtainTypedArray(R.array.smiley_values_24);
		} else {
			return context.getResources().obtainTypedArray(R.array.smiley_values_30);
		}
	}

	public static final TypedArray getVisibilityNames(Context context, String protocolName) {
		if (protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return context.getResources().obtainTypedArray(R.array.icq_visibility_descr);
		}
		return null;
	}

	public static final TypedArray getVisibilityIcons(Context context, String protocolName) {
		if (protocolName.equals(context.getResources().getString(R.string.icq_service_name))) {
			return context.getResources().obtainTypedArray(R.array.icq_visibility_icons);
		}
		return null;
	}

	public static final byte getAccountVisibilityIdByVisibilityArrayId(int visibilityId) {
		switch (visibilityId) {
		default:
			return AccountView.VIS_TO_BUDDIES;
		case 0:
			return AccountView.VIS_TO_ALL;
		case 1:
			return AccountView.VIS_EXCEPT_DENIED;
		case 3:
			return AccountView.VIS_TO_PERMITTED;
		case 4:
			return AccountView.VIS_INVISIBLE;
		}
	}

	public static final byte getVisibilityArrayIdByAccountVisibilityId(int visibilityId) {
		switch (visibilityId) {
		default:
			return 2;
		case AccountView.VIS_TO_ALL:
			return 0;
		case AccountView.VIS_EXCEPT_DENIED:
			return 1;
		case AccountView.VIS_TO_PERMITTED:
			return 3;
		case AccountView.VIS_INVISIBLE:
			return 4;
		}
	}

	public static File createLocalFileForReceiving(String filename, long filesize, long modTime) {
		File root = Environment.getExternalStorageDirectory();
		File downloads = new File(root, "Asia");
		downloads.mkdirs();

		File file = new File(downloads, filename);

		if (file.exists()) {
			boolean deleted = file.delete();
			if (!deleted) {
				file = new File(downloads, new Random().nextInt() + "_" + filename);
			}
		}
		if (modTime > 0) {
			file.setLastModified(modTime);
		}

		return file;
	}

	public static synchronized void storeAccountActivity(AccountView account, String format, Object... object) {
		try {
			File root = Environment.getExternalStorageDirectory();
			File downloads = new File(root, "Asia");
			downloads.mkdirs();
			File logFile;
			if (account != null) {
				logFile = new File(downloads, "Activity " + account.getAccountId() + ".log");
			} else {
				logFile = new File(downloads, "Activity.log");
			}

			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(logFile, true);

			String string = String.format(format, object);

			fos.write(new String(DATE_FORMATTER.format(new Date()) + " --> " + string + "\n").getBytes());
			fos.close();
		} catch (IOException e) {
			log(e, account);
		}
	}

	public static synchronized List<ServiceMessage> getAccountActivity(AccountView account) {
		File root = Environment.getExternalStorageDirectory();
		File downloads = new File(root, "Asia");
		downloads.mkdirs();
		File file;
		if (account != null) {
			file = new File(downloads, "Activity " + account.getAccountId() + ".log");
		} else {
			file = new File(downloads, "Activity.log");
		}

		List<ServiceMessage> messages = new LinkedList<ServiceMessage>();

		if (!file.exists()) {
			return messages;
		}

		try {
			FileInputStream fis = new FileInputStream(file);

			String strLine = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fis)));

			while ((strLine = br.readLine()) != null) {
				ServiceMessage msg = new ServiceMessage(account.getAccountId());
				msg.serviceId = account.serviceId;
				msg.text = strLine;
				messages.add(msg);
			}

			fis.close();
		} catch (IOException e) {
			log(e, account);
		}

		return messages;
	}

	public static final String getBuddyStateName(byte status, Context ctx) {
		switch (status) {
		case Buddy.ST_ANGRY:
			return ctx.getString(R.string.label_st_angry);
		case Buddy.ST_AWAY:
			return ctx.getString(R.string.label_st_away);
		case Buddy.ST_BUSY:
			return ctx.getString(R.string.label_st_busy);
		case Buddy.ST_DEPRESS:
			return ctx.getString(R.string.label_st_depress);
		case Buddy.ST_DINNER:
			return ctx.getString(R.string.label_st_dinner);
		case Buddy.ST_DND:
			return ctx.getString(R.string.label_st_dnd);
		case Buddy.ST_FREE4CHAT:
			return ctx.getString(R.string.label_st_free4chat);
		case Buddy.ST_HOME:
			return ctx.getString(R.string.label_st_home);
		case Buddy.ST_INVISIBLE:
			return ctx.getString(R.string.label_st_invisible);
		case Buddy.ST_NA:
			return ctx.getString(R.string.label_st_na);
		case Buddy.ST_ONLINE:
			return ctx.getString(R.string.label_st_online);
		case Buddy.ST_OTHER:
			return ctx.getString(R.string.label_st_undetermined);
		default:
			return ctx.getString(R.string.label_st_offline);
		}
	}

	static final int DEFAULT_SKIP_AMOUNT = 1500;

}
