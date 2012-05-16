package ua.snuk182.asia.services;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import ua.snuk182.asia.services.icq.ICQService;
import ua.snuk182.asia.services.mrim.MrimService;
import ua.snuk182.asia.services.xmpp.XMPPService;
import ua.snuk182.asia.view.more.AsiaCoreException;
import android.content.Context;

/**
 * Account service factory
 * 
 * @author SergiyP
 *
 */
public final class ProtocolServiceFactory {
	
	/**
	 * Create account service based on account view entity
	 * 
	 * @param context a context to operate in
	 * @param account input account view
	 * @param response response parameter for account service
	 * @return account service
	 * @throws AsiaCoreException if account has unknown service type
	 */
	public static final AccountService createProtocolService(Context context, AccountView account, IAccountServiceResponse response) throws AsiaCoreException{
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

		throw new AsiaCoreException("No suitable service found for "+account.protocolName);
	}

}
