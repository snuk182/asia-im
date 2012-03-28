package ua.snuk182.asia.services.icq;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.api.AccountService;
import android.content.Context;
import android.content.res.TypedArray;

public final class ICQServiceUtils {

	public static final boolean supportsPasswordedChats = false;
	public static final boolean supportsManuallyConnectedChats = false;
	public static final boolean supportsCustomChatNickname = false;

	public static byte[] statusValues = new byte[]{ Buddy.ST_ONLINE, Buddy.ST_AWAY, Buddy.ST_NA, Buddy.ST_BUSY, Buddy.ST_DND, Buddy.ST_FREE4CHAT, Buddy.ST_INVISIBLE, Buddy.ST_ANGRY, Buddy.ST_DEPRESS, Buddy.ST_DINNER, Buddy.ST_HOME, Buddy.ST_WORK };

	public static final int getStatusResIdByAccountTiny(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.icq_offline_tiny;
			case AccountService.STATE_CONNECTING:
				return R.drawable.icq_connecting_tiny;
			}
		}
		
		return getStatusResIdByStatusIdTiny(account.status);
	}

	public static final int getStatusResIdByAccountMedium(AccountView account, boolean withoutConnectionState){
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.icq_offline_medium;
			case AccountService.STATE_CONNECTING:
				return R.drawable.icq_connecting_medium;
			}
		}
		
		return getStatusResIdByStatusIdMedium(account.status);
	}

	public static final int getStatusResIdByAccountSmall(AccountView account, boolean withoutConnectionState){
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.icq_offline_small;
			case AccountService.STATE_CONNECTING:
				return R.drawable.icq_connecting_small;
			}
		}
		
		return getStatusResIdByStatusIdSmall(account.status);
	}

	public static final int getStatusResIdByStatusIdBig(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.icq_online_big;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.icq_free4chat_big;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.icq_away_big;
			break;
		case Buddy.ST_BUSY:
			statusResId = R.drawable.icq_busy_big;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.icq_dnd_big;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.icq_invisible_big;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.icq_na_big;
			break;
		case Buddy.ST_ANGRY:
			statusResId = R.drawable.icq_angry_big;
			break;
		case Buddy.ST_DEPRESS:
			statusResId = R.drawable.icq_depress_big;
			break;
		case Buddy.ST_DINNER:
			statusResId = R.drawable.icq_dinner_big;
			break;
		case Buddy.ST_HOME:
			statusResId = R.drawable.icq_home_big;
			break;
		case Buddy.ST_WORK:
			statusResId = R.drawable.icq_work_big;
			break;
		default:
			statusResId = R.drawable.icq_offline_big;
			break;
		}
		return statusResId;
	}

	public static final int getStatusResIdByStatusIdBigger(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.icq_online_bigger;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.icq_free4chat_bigger;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.icq_away_bigger;
			break;
		case Buddy.ST_BUSY:
			statusResId = R.drawable.icq_busy_bigger;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.icq_dnd_bigger;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.icq_invisible_bigger;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.icq_na_bigger;
			break;
		case Buddy.ST_ANGRY:
			statusResId = R.drawable.icq_angry_bigger;
			break;
		case Buddy.ST_DEPRESS:
			statusResId = R.drawable.icq_depress_bigger;
			break;
		case Buddy.ST_DINNER:
			statusResId = R.drawable.icq_dinner_bigger;
			break;
		case Buddy.ST_HOME:
			statusResId = R.drawable.icq_home_bigger;
			break;
		case Buddy.ST_WORK:
			statusResId = R.drawable.icq_work_bigger;
			break;
		default:
			statusResId = R.drawable.icq_offline_bigger;
			break;
		}
		return statusResId;
	}

	public static final int getStatusResIdByStatusIdSmall(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.icq_online_small;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.icq_free4chat_small;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.icq_away_small;
			break;
		case Buddy.ST_BUSY:
			statusResId = R.drawable.icq_busy_small;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.icq_dnd_small;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.icq_invisible_small;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.icq_na_small;
			break;
		case Buddy.ST_ANGRY:
			statusResId = R.drawable.icq_angry_small;
			break;
		case Buddy.ST_DEPRESS:
			statusResId = R.drawable.icq_depress_small;
			break;
		case Buddy.ST_DINNER:
			statusResId = R.drawable.icq_dinner_small;
			break;
		case Buddy.ST_HOME:
			statusResId = R.drawable.icq_home_small;
			break;
		case Buddy.ST_WORK:
			statusResId = R.drawable.icq_work_small;
			break;
		default:
			statusResId = R.drawable.icq_offline_small;
			break;
		}
		
		return statusResId;
	}

	public static final int getStatusResIdByStatusIdMedium(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.icq_online_medium;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.icq_free4chat_medium;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.icq_away_medium;
			break;
		case Buddy.ST_BUSY:
			statusResId = R.drawable.icq_busy_medium;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.icq_dnd_medium;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.icq_invisible_medium;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.icq_na_medium;
			break;
		case Buddy.ST_ANGRY:
			statusResId = R.drawable.icq_angry_medium;
			break;
		case Buddy.ST_DEPRESS:
			statusResId = R.drawable.icq_depress_medium;
			break;
		case Buddy.ST_DINNER:
			statusResId = R.drawable.icq_dinner_medium;
			break;
		case Buddy.ST_HOME:
			statusResId = R.drawable.icq_home_medium;
			break;
		case Buddy.ST_WORK:
			statusResId = R.drawable.icq_work_medium;
			break;
		default:
			statusResId = R.drawable.icq_offline_medium;
			break;
		}
		
		return statusResId;
	}

	public static final int getStatusResIdByStatusIdTiny(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.icq_online_tiny;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.icq_free4chat_tiny;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.icq_away_tiny;
			break;
		case Buddy.ST_BUSY:
			statusResId = R.drawable.icq_busy_tiny;
			break;
		case Buddy.ST_DND:
			statusResId = R.drawable.icq_dnd_tiny;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.icq_invisible_tiny;
			break;
		case Buddy.ST_NA:
			statusResId = R.drawable.icq_na_tiny;
			break;
		case Buddy.ST_ANGRY:
			statusResId = R.drawable.icq_angry_tiny;
			break;
		case Buddy.ST_DEPRESS:
			statusResId = R.drawable.icq_depress_tiny;
			break;
		case Buddy.ST_DINNER:
			statusResId = R.drawable.icq_dinner_tiny;
			break;
		case Buddy.ST_HOME:
			statusResId = R.drawable.icq_home_tiny;
			break;
		case Buddy.ST_WORK:
			statusResId = R.drawable.icq_work_tiny;
			break;
		default:
			statusResId = R.drawable.icq_offline_tiny;
			break;
		}
		
		return statusResId;
	}

	public static int getMenuResIdByAccount(AccountView account) {
		return R.menu.icq_cl_menu;
	}

	public static final String[] getStatusListNames(Context context){
		return context.getResources().getStringArray(R.array.icq_status_strings);
	}

	public static final byte getStatusValueByCount(int count){
		return statusValues[count];
	}

	public static TypedArray getStatusResIds(Context context) {
		return context.getResources().obtainTypedArray(R.array.icq_status_icons);
	}

}
