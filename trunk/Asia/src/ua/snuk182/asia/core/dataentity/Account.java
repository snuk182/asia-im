package ua.snuk182.asia.core.dataentity;

import java.io.Serializable;

import ua.snuk182.asia.services.ProtocolServiceFactory;
import ua.snuk182.asia.services.api.AccountService;
import ua.snuk182.asia.services.api.IAccountServiceResponse;
import android.content.Context;

/**
 * Account storage entity. Ties the view part and service part of account.
 * 
 * @author SergiyP
 *
 */
public class Account implements Serializable {

	private static final long serialVersionUID = 1380460197305553929L;
	
	public AccountView accountView;
	public AccountService accountService; 
	
	public Account(Context context, AccountView accountView, IAccountServiceResponse serviceResponse){
		this.accountView = accountView;
		accountService = ProtocolServiceFactory.createProtocolService(context, accountView, serviceResponse);
	}
}
