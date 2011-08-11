package ua.snuk182.asia.services;

import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.Splashscreen;
import ua.snuk182.asia.core.dataentity.AccountView;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import ua.snuk182.asia.core.dataentity.TabInfo;
import ua.snuk182.asia.view.cl.ContactList;
import ua.snuk182.asia.view.conversations.ConversationsView;
import ua.snuk182.asia.view.more.AccountManagerView;
import ua.snuk182.asia.view.more.AsiaCoreException;
import ua.snuk182.asia.view.more.FileTransferView;
import ua.snuk182.asia.view.more.HistoryView;
import ua.snuk182.asia.view.more.NewAccountView;
import ua.snuk182.asia.view.more.PersonalInfoView;
import ua.snuk182.asia.view.more.PreferencesView;
import ua.snuk182.asia.view.more.SearchUsersView;
import android.content.Intent;
import android.os.RemoteException;
import android.view.View;

public final class TabInfoFactory {

	public static final TabInfo createContactList(EntryPoint entryPoint, AccountView account) throws AsiaCoreException {
		if (account == null) {
			throw new AsiaCoreException("Account is null");
		}
		ContactList cl = new ContactList(entryPoint, account);
		String tag = ContactList.class.getSimpleName() + " " + account.serviceId;
		TabInfo tab = new TabInfo(tag, cl, entryPoint);

		return tab;
	}

	public static final TabInfo createConversation(EntryPoint entryPoint, AccountView account, List<Buddy> buddies) throws AsiaCoreException {
		if (buddies == null || buddies.size() < 1) {
			throw new AsiaCoreException("Buddies array is invalid");
		}
		ConversationsView view = new ConversationsView(entryPoint, buddies, account);
		TabInfo tab = new TabInfo(view.chatId, view, entryPoint);
		return tab;
	}

	public static final TabInfo createSplashscreenTab(EntryPoint entryPoint) {
		Splashscreen splash = new Splashscreen(entryPoint);
		String tag = Splashscreen.class.getSimpleName();
		TabInfo tab = new TabInfo(tag, splash, entryPoint);
		entryPoint.getTabWidget().setVisibility(View.GONE);

		return tab;
	}

	public static final TabInfo createAccountEditTab(EntryPoint entryPoint, AccountView account) {
		NewAccountView view = new NewAccountView(entryPoint, account);
		TabInfo tab = new TabInfo(view.tag, view, entryPoint);

		return tab;
	}

	public static final TabInfo createAccountManager(EntryPoint entryPoint) {
		AccountManagerView view = new AccountManagerView(entryPoint);
		String tag = AccountManagerView.class.getSimpleName();
		TabInfo tab = new TabInfo(tag, view, entryPoint);

		return tab;
	}

	public static final TabInfo createPreferencesTab(EntryPoint entryPoint, AccountView account) {
		Intent intent = new Intent(entryPoint, PreferencesView.class);
		intent.putExtra(ServiceConstants.INTENTEXTRA_SERVICEID, account);

		String tag;
		String title;
		int iconId;

		if (account != null) {
			tag = PreferencesView.class.getSimpleName() + " " + account.serviceId;
			title = account.getAccountId();
			iconId = R.drawable.account_settings;
		} else {
			tag = PreferencesView.class.getSimpleName();
			title = entryPoint.getResources().getString(R.string.label_preferences);
			iconId = R.drawable.preferences;
		}

		TabInfo info = new TabInfo(tag, title, intent, entryPoint, iconId);

		return info;
	}

	public static final TabInfo createHistoryTab(EntryPoint entryPoint, Buddy buddy) {
		String tag = HistoryView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid;
		HistoryView view = new HistoryView(entryPoint, buddy, tag);
		TabInfo tab = new TabInfo(tag, view, entryPoint);

		return tab;
	}

	public static final TabInfo recreateTabContent(EntryPoint entryPoint, TabInfo tab) throws AsiaCoreException {
		if (tab == null) {
			throw new AsiaCoreException("Tab is null");
		}

		if (tab.tag.indexOf(ContactList.class.getSimpleName()) > -1) {
			try {
				tab.content = new ContactList(entryPoint, entryPoint.runtimeService.getAccountView(Byte.parseByte(tab.tag.split(" ")[1])));
			} catch (NullPointerException npe) {
				throw new AsiaCoreException(npe.getMessage());
			} catch (NumberFormatException e) {
				throw new AsiaCoreException(e.getMessage());
			} catch (RemoteException e) {
				throw new AsiaCoreException(e.getMessage());
			}
		}

		if (tab.tag.indexOf(ConversationsView.class.getSimpleName()) > -1) {
			String[] params = tab.tag.split(" ");
			try {
				List<Buddy> buddies = new ArrayList<Buddy>();
				buddies.add(entryPoint.runtimeService.getBuddy(Byte.parseByte(params[1]), params[2]));
				tab.content = new ConversationsView(entryPoint, buddies, entryPoint.runtimeService.getAccountView(Byte.parseByte(params[1])));
			} catch (NullPointerException npe) {
				throw new AsiaCoreException(npe.getMessage());
			} catch (NumberFormatException e) {
				throw new AsiaCoreException(e.getMessage());
			} catch (RemoteException e) {
				throw new AsiaCoreException(e.getMessage());
			}
		}

		if (tab.tag.indexOf(PreferencesView.class.getSimpleName()) > -1) {
			String[] tagOpts = tab.tag.split(" ");
			byte serviceId = -1;
			if (tagOpts.length == 2) {
				serviceId = Byte.parseByte(tagOpts[1]);
			}
			try {
				tab = createPreferencesTab(entryPoint, entryPoint.runtimeService.getAccountView(serviceId));
			} catch (NullPointerException npe) {
				throw new AsiaCoreException(npe.getMessage());
			} catch (NumberFormatException e) {
				throw new AsiaCoreException(e.getMessage());
			} catch (RemoteException e) {
				throw new AsiaCoreException(e.getMessage());
			}
		}
		tab.construct(entryPoint.getTabHost());
		return tab;
	}

	public static final TabInfo createSearchTab(EntryPoint entryPoint, AccountView account) {
		SearchUsersView view = new SearchUsersView(entryPoint, account);
		String tag = SearchUsersView.class.getSimpleName() + " " + account.serviceId;
		TabInfo tab = new TabInfo(tag, view, entryPoint);

		return tab;
	}

	public static final TabInfo createPersonalInfoTab(EntryPoint entryPoint, Buddy buddy, PersonalInfo info) {
		PersonalInfoView infoView = new PersonalInfoView(entryPoint, buddy, info);
		String tag = PersonalInfoView.class.getSimpleName() + " " + buddy.serviceId + " " + buddy.protocolUid;
		TabInfo tab = new TabInfo(tag, infoView, entryPoint);

		return tab;
	}

	public static final TabInfo createFileTransferTab(EntryPoint entryPoint, byte serviceId) {
		AccountView account = null;

		try {
			account = entryPoint.runtimeService.getAccountView(serviceId);
		} catch (NullPointerException npe) {
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			entryPoint.onRemoteCallFailed(e);
		}

		FileTransferView ftView = new FileTransferView(entryPoint, account);
		String tag = FileTransferView.class.getSimpleName() + " " + serviceId;
		TabInfo tab = new TabInfo(tag, ftView, entryPoint);

		return tab;
	}
}
