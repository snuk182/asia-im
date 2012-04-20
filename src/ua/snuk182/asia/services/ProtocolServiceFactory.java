package ua.snuk182.asia.services;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import ua.snuk182.asia.services.icq.ICQService;
import ua.snuk182.asia.services.mrim.MrimService;
import ua.snuk182.asia.services.xmpp.XMPPService;
import android.content.Context;

public final class ProtocolServiceFactory {
	
	public static final AccountService createProtocolService(Context context, AccountView account, IAccountServiceResponse response){
		if (account == null) return null;
		if (account.protocolName.equals(context.getResources().getString(R.string.icq_service_name))){
			return new ICQService(context, response, account.serviceId);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.xmpp_service_name))){
			return new XMPPService(context, response, account.serviceId);
		}
		if (account.protocolName.equals(context.getResources().getString(R.string.mrim_service_name))){
			return new MrimService(context, response, account.serviceId);
		}
		return null;
	}

}
