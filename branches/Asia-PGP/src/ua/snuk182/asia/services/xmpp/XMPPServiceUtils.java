package ua.snuk182.asia.services.xmpp;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.api.AccountService;
import android.content.Context;
import android.content.res.TypedArray;

public final class XMPPServiceUtils {

	public static final byte[] statusValues = new byte[] { Buddy.ST_ONLINE, Buddy.ST_AWAY, Buddy.ST_NA, Buddy.ST_BUSY, Buddy.ST_FREE4CHAT, Buddy.ST_INVISIBLE };
	
	public static final boolean supportsPasswordedChats = true;
	public static final boolean supportsManuallyConnectedChats = true;
	public static final boolean supportsCustomChatNickname = true;	

	public static int getStatusResIdByStatusIdBigger(int statusId) {
		int statusResId;
	
		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_bigger;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_bigger;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_bigger;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_bigger;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_bigger;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_bigger;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_bigger;
			break;
		}
	
		return statusResId;
	}

	public static int getStatusResIdByStatusIdBig(int statusId) {
		int statusResId;
	
		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_big;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_big;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_big;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_big;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_big;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_big;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_big;
			break;
		}
	
		return statusResId;
	}

	public static int getStatusResIdByStatusIdSmall(int statusId) {
		int statusResId;
	
		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_small;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_small;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_small;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_small;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_small;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_small;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_small;
			break;
		}
	
		return statusResId;
	}

	public static int getStatusResIdByStatusIdMedium(int statusId) {
		int statusResId;
	
		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_medium;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_medium;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_medium;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_medium;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_medium;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_medium;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_medium;
			break;
		}
	
		return statusResId;
	}

	public static int getStatusResIdByStatusIdTiny(int statusId) {
		int statusResId;
	
		switch (statusId) {
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.xmpp_online_tiny;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.xmpp_free4chat_tiny;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.xmpp_away_tiny;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.xmpp_dnd_tiny;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.xmpp_invisible_tiny;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.xmpp_na_tiny;
			break;
		default:
			statusResId = R.drawable.xmpp_offline_tiny;
			break;
		}
	
		return statusResId;
	}

	/*
	 * public static int getStatusResIdByAccount(AccountView account, int size,
	 * boolean withoutConnectionState) {
	 * 
	 * if (size <= 16) { return getStatusResIdByAccountTiny(account,
	 * withoutConnectionState); } else { return
	 * getStatusResIdByAccountMedium(account, withoutConnectionState); } }
	 */
	public static int getStatusResIdByAccountTiny(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState) {
			switch (account.getConnectionState()) {
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.xmpp_offline_tiny;
			case AccountService.STATE_CONNECTING:
				return R.drawable.xmpp_connecting_tiny;
			}
		}
	
		return getStatusResIdByStatusIdTiny(account.status);
	}

	public static final int getStatusResIdByAccountSmall(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState) {
			switch (account.getConnectionState()) {
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.xmpp_offline_small;
			case AccountService.STATE_CONNECTING:
				return R.drawable.xmpp_connecting_small;
			}
		}
	
		return getStatusResIdByStatusIdSmall(account.status);
	}

	public static final int getStatusResIdByAccountMedium(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState) {
			switch (account.getConnectionState()) {
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.xmpp_offline_medium;
			case AccountService.STATE_CONNECTING:
				return R.drawable.xmpp_connecting_medium;
			}
		}
	
		return getStatusResIdByStatusIdMedium(account.status);
	}

	public static int getMenuResIdByAccount(AccountView account) {
		return R.menu.xmpp_cl_menu;
	}

	public static final String[] getStatusListNames(Context context) {
		return context.getResources().getStringArray(R.array.xmpp_status_strings);
	}

	public static final byte getStatusValueByCount(int count) {
		return statusValues[count];
	}

	public static TypedArray getStatusResIds(Context context) {
		return context.getResources().obtainTypedArray(R.array.xmpp_status_icons);
	}

}
