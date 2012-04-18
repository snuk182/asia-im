package ua.snuk182.asia.services.mrim;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.api.AccountService;
import android.content.Context;
import android.content.res.TypedArray;

public final class MrimServiceUtils {

	public static final boolean supportsPasswordedChats = false;
	public static final boolean supportsManuallyConnectedChats = false;
	public static final boolean supportsCustomChatNickname = false;

	public static byte[] statusValues = new byte[]{ Buddy.ST_ONLINE, Buddy.ST_AWAY, Buddy.ST_OTHER, Buddy.ST_FREE4CHAT, Buddy.ST_INVISIBLE};

	public static int getStatusResIdByAccountTiny(AccountView account, boolean withoutConnectionState) {
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.mrim_offline_tiny;
			case AccountService.STATE_CONNECTING:
				return R.drawable.mrim_connecting_tiny;
			}
		}
		
		return getStatusResIdByStatusIdTiny(account.status);
	}

	public static final int getStatusResIdByAccountMedium(AccountView account, boolean withoutConnectionState){
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.mrim_offline_medium;
			case AccountService.STATE_CONNECTING:
				return R.drawable.mrim_connecting_medium;
			}
		}
		
		return getStatusResIdByStatusIdMedium(account.status);
	}

	public static final int getStatusResIdByAccountSmall(AccountView account, boolean withoutConnectionState){
		if (!withoutConnectionState){
			switch(account.getConnectionState()){
			case AccountService.STATE_DISCONNECTED:
				return R.drawable.mrim_offline_small;
			case AccountService.STATE_CONNECTING:
				return R.drawable.mrim_connecting_small;
			}
		}
		
		return getStatusResIdByStatusIdSmall(account.status);
	}

	public static int getStatusResIdByStatusIdBigger(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_bigger;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_bigger;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_bigger;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_bigger;
			break;
		default:
			statusResId = R.drawable.mrim_offline_bigger;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_bigger;
			break;
		}
		return statusResId;
	}

	public static int getStatusResIdByStatusIdBig(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_big;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_big;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_big;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_big;
			break;
		default:
			statusResId = R.drawable.mrim_offline_big;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_big;
			break;
		}
		return statusResId;
	}

	public static int getStatusResIdByStatusIdSmall(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_small;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_small;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_small;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_small;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_small;
			break;
		default:
			statusResId = R.drawable.mrim_offline_small;
			break;
		}
		
		return statusResId;
	}

	public static int getStatusResIdByStatusIdMedium(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_medium;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_medium;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_medium;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_medium;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_medium;
			break;
		default:
			statusResId = R.drawable.mrim_offline_medium;
			break;
		}
		
		return statusResId;
	}

	public static int getStatusResIdByStatusIdTiny(int statusId) {
		int statusResId;
		
		switch(statusId){
		case Buddy.ST_ONLINE:
			statusResId = R.drawable.mrim_online_tiny;
			break;
		case Buddy.ST_AWAY:
			statusResId = R.drawable.mrim_away_tiny;
			break;
		case Buddy.ST_OTHER:
			statusResId = R.drawable.mrim_undetermined_tiny;
			break;
		case Buddy.ST_INVISIBLE:
			statusResId = R.drawable.mrim_invisible_tiny;
			break;
		case Buddy.ST_FREE4CHAT:
			statusResId = R.drawable.mrim_free4chat_tiny;
			break;
		default:
			statusResId = R.drawable.mrim_offline_tiny;
			break;
		}
		
		return statusResId;
	}

	public static int getMenuResIdByAccount(AccountView account) {
		return R.menu.mrim_cl_menu;
	}

	public static final String[] getStatusListNames(Context context){
		return context.getResources().getStringArray(R.array.mrim_status_strings);
	}

	public static final byte getStatusValueByCount(int count){
		return statusValues[count];
	}

	public static TypedArray getStatusResIds(Context context) {
		return context.getResources().obtainTypedArray(R.array.mrim_status_icons);
	}

}
