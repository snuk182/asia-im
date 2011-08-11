package ua.snuk182.asia.services.plus;

/**
 * 
 * Native LED controller is created by apangin ( http://apangin.habrahabr.ru/ )
 */
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.conversations.ConversationsView;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

public class Notificator {

	private Context context;
	private NotificationManager notificator;
	private Vibrator vibrator;
	private AudioManager audioManager;

	private static final int appIconId = -100500;

	private LedBlinker ledBlinker;

	private Map<Byte, Notification> accountNotifications = new HashMap<Byte, Notification>();
	private Map<Long, Notification> fileTransferViews = new HashMap<Long, Notification>();

	private Notification getAccountNotification(byte serviceId) {
		return accountNotifications.get(serviceId);
	}

	private Notification getFileNotifications(long messageId) {
		return fileTransferViews.get(messageId);
	}

	public Notificator(Context context) {
		this.context = context;
		notificator = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void notifyMessageReceived(TextMessage message, Buddy buddy, boolean updateAppIcon, boolean blinkLed) {
		int icon = R.drawable.message_tiny;
		CharSequence tickerText = buddy.getName() + ": " + message.text.substring(message.text.indexOf("):") + 2);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if (blinkLed){
			if (!checkAndRunNativeLed()){
				notification.ledARGB = 0xff00ff00;
				notification.ledOnMS = 400;
				notification.ledOffMS = 400;

				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
				//notification.defaults |= Notification.DEFAULT_LIGHTS;
			}
		}
		CharSequence contentTitle = buddy.getName();
		CharSequence contentText = message.text.substring(message.text.indexOf("):") + 2);
		Intent notificationIntent = new Intent(context, EntryPoint.class);
		String notificatorId = ConversationsView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid;
		notificationIntent.setData((Uri.parse("asia://" + notificatorId)));
		PendingIntent contentIntent = PendingIntent.getActivity(context, buddy.id, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		if (updateAppIcon) {
			notificator.notify(appIconId, notification);
		} else {
			notificator.notify(buddy.getFilename().hashCode(), notification);
		}
	}

	public void cancel(AccountView account) {
		cancel(account.getAccountId().hashCode());
	}

	public void cancel(Buddy buddy) {
		cancel(buddy.getFilename().hashCode());
	}

	private void cancel(int id) {
		notificator.cancel(id);
		if (ledBlinker != null){
			ledBlinker.stopped = true;
		}
	}

	public void notifyServiceMessageReceived(ServiceMessage message, Buddy buddy) {
		int icon = R.drawable.message_tiny;
		CharSequence tickerText = buddy.getName() + ": " + message.text.substring(message.text.indexOf("):") + 2);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		CharSequence contentTitle = buddy.getName();
		CharSequence contentText = message.text;
		Intent notificationIntent = new Intent(context, EntryPoint.class);
		String notificatorId = ConversationsView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid;
		notificationIntent.setData((Uri.parse("asia://" + notificatorId)));
		PendingIntent contentIntent = PendingIntent.getActivity(context, buddy.id, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificator.notify(buddy.getFilename().hashCode(), notification);
	}

	public void notifyAccountChanged(AccountView account, String text) {
		int icon = account.hasUnreadMessages() ? R.drawable.message_tiny : ServiceUtils.getStatusResIdByAccountMedium(context, account, false);
		long when = System.currentTimeMillis();
		Notification notification = getAccountNotification(account.serviceId);
		if (notification == null) {
			notification = new Notification(icon, null, when);
			if (text != null) {
				notification.tickerText = text;
			}
			notification.flags = Notification.FLAG_NO_CLEAR;
			accountNotifications.put(account.serviceId, notification);
		} else {
			if (text != null) {
				notification.tickerText = text;
			}
			notification.icon = icon;
			notification.when = when;
		}
		CharSequence contentTitle = account.getSafeName();
		CharSequence contentText = account.getSafeName() + " (" + account.protocolUid + ") " + ((icon == R.drawable.message_tiny) ? context.getResources().getString(R.string.label_unread_messages) : "");
		Intent notificationIntent = new Intent(context, EntryPoint.class);
		String notificatorId = ContactList.class.getSimpleName() + " " + account.serviceId;
		notificationIntent.setData((Uri.parse("asia://" + notificatorId)));
		PendingIntent contentIntent = PendingIntent.getActivity(context, account.serviceId, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		notificator.notify(account.getAccountId().hashCode(), notification);
	}

	public void notifyFileProgress(long messageId, String filename, long totalSize, long sizeTransferred, Boolean isReceive, String error) {

		String file = filename.substring(filename.lastIndexOf(File.separator) + File.separator.length(), filename.length());

		Notification n = getFileNotifications(messageId);
		long when = System.currentTimeMillis();
		if (n == null) {
			n = new Notification(android.R.drawable.ic_menu_save, file, when);
			fileTransferViews.put(messageId, n);
		} else {
			n.when = when;
		}

		Intent intent;
		if (error == null) {
			if (sizeTransferred >= totalSize) {
				n.flags = Notification.FLAG_AUTO_CANCEL;
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + filename));
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
				n.setLatestEventInfo(context, file, context.getResources().getString(R.string.label_completed), contentIntent);
			} else {
				if (totalSize == 0) {
					totalSize = 0xffffffff;
				}
				n.flags = Notification.FLAG_AUTO_CANCEL;
				intent = new Intent(context, EntryPoint.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
				n.setLatestEventInfo(context, file, ((100 * sizeTransferred) / totalSize) + "%", contentIntent);
			}
		} else {
			n.flags = Notification.FLAG_AUTO_CANCEL;
			intent = new Intent(context, EntryPoint.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

			n.setLatestEventInfo(context, file, error, contentIntent);
		}

		notificator.notify(n.hashCode(), n);
	}

	public void cancelFileNotification(long messageId) {
		Notification n = getFileNotifications(messageId);

		if (n != null) {
			notificator.cancel(n.hashCode());
		}
	}

	public void showAppIcon() {
		int icon = R.drawable.asia_tray;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, null, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		CharSequence contentTitle = context.getResources().getString(R.string.app_name);
		Intent notificationIntent = new Intent(context, EntryPoint.class);
		String notificatorId = ContactList.class.getSimpleName();
		notificationIntent.setData((Uri.parse("asia://" + notificatorId)));
		PendingIntent contentIntent = PendingIntent.getActivity(context, appIconId, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, "", contentIntent);
		notificator.notify(appIconId, notification);
	}

	public void removeAppIcon() {
		cancel(appIconId);
	}

	class LedBlinker extends Thread {
		private final String red;
		private final String green;
		private final String blue;

		volatile boolean stopped = false;

		protected LedBlinker(boolean isSE) {
			if (isSE) {
				red = "ledc:rgb1:red";
				green = "ledc:rgb1:green";
				blue = "ledc:rgb1:blue";
			} else {
				red = "amber";
				green = "green";
				blue = "blue";
			}
		}

		public void run() {
			for (int ledState = 0; !stopped; ledState = (ledState + 1) % 6) {
				switch (ledState) {
				case 0:
					try {
						nativeLedControl(red, 255);
					} catch (Exception e1) {}
					break;
				case 1:
					try {
						nativeLedControl(blue, 255);
					} catch (Exception e1) {}
					break;
				case 2:
					try {
						nativeLedControl(red, 0);
					} catch (Exception e1) {}
					break;
				case 3:
					try {
						nativeLedControl(green, 255);
					} catch (Exception e1) {}
					break;
				case 4:
					try {
						nativeLedControl(blue, 0);
					} catch (Exception e1) {}
					break;
				case 5:
					try {
						nativeLedControl(green, 0);
					} catch (Exception e1) {}
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			try {
				nativeLedControl(red, 0);
			} catch (Exception e) {}
			try {
				nativeLedControl(green, 0);
			} catch (Exception e) {}
			try {
				nativeLedControl(blue, 0);
			} catch (Exception e) {}
		}
	}

	private boolean checkAndRunNativeLed() {
		if (ledBlinker == null) {
			boolean isSE = true;

			// sony ericsson case
			try {
				nativeLedControl("ledc:rgb1:green", 0);
			} catch (Exception e) {
				isSE = false;
				try {
					nativeLedControl("green", 0);
				} catch (Exception e1) {

					// no native led api available
					return false;
				}
			}

			ledBlinker = new LedBlinker(isSE);
		}

		ledBlinker.start();

		return true;
	}

	/**
	 * /sys/class/leds/ledc:rgb1:red/brightness - red
	 * /sys/class/leds/ledc:rgb1:green/brightness - green
	 * /sys/class/leds/ledc:rgb1:blue/brightness - blue
	 */
	private void nativeLedControl(String name, int brightness) throws Exception {
		FileWriter fw = new FileWriter("/sys/class/leds/" + name + "/brightness");
		fw.write(Integer.toString(brightness));
		fw.close();
	}
	
	
	private void play(final int res) {
		new Thread("Sound play") {

			@Override
			public void run() {
				MediaPlayer mp = MediaPlayer.create(context, res);
				mp.start();
			}
		}.start();
	}

	public void playMessage(boolean playSound, boolean vibrate) {
		if (audioManager != null && playSound) {
			play(R.raw.message);
		}
		if (vibrator != null && vibrate) {
			vibrator.vibrate(600);
		}
	}

	public void playMessageBasedOnProfile() {
		int ringerMode = audioManager.getRingerMode();
		switch (ringerMode) {
		case AudioManager.RINGER_MODE_NORMAL:
			playMessage(true, true);
			break;
		case AudioManager.RINGER_MODE_VIBRATE:
			playMessage(false, true);
			break;
		case AudioManager.RINGER_MODE_SILENT:
			playMessage(false, false);
			break;
		}
	}

	public void playOnline() {
		if (audioManager != null) {
			play(R.raw.online);
		}
	}

	public void playOnlineBasedOnProfile() {
		int ringerMode = audioManager.getRingerMode();
		switch (ringerMode) {
		case AudioManager.RINGER_MODE_NORMAL:
			playOnline();
			break;
		}
	}
}
