package wenjh.akit.demo;

import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.AKitApplication;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.demo.account.db.AccountDBOpenHandler;
import wenjh.akit.demo.account.model.Account;
import wenjh.akit.demo.account.model.Preference;
import wenjh.akit.demo.chat.model.ChatSessionOrderIdHandler;
import wenjh.akit.demo.chat.model.MessageServiceWrapper;

public class AKitApplicationDemo extends AKitApplication {
	private Account currentAccount = null;
	private SQLiteDatabase accountSqliteInstance = null;
	private LogUtil log = new LogUtil(this);
	private boolean online = false;

	@Override
	public void onCreate() {
		super.onCreate();
		// restore account class
		Preference preference = Preference.getLoginAccountInfoPreference(getApplicationContext());
		String currentLoginedUserId = preference.getValue("userid", null);
		String currentLoginedSession = preference.getValue("session", null);
		log.i("AKitApplication onCreated, currentLoginedUserId="+currentLoginedUserId+", currentLoginedSession="+currentLoginedSession);
		if(!StringUtil.isEmpty(currentLoginedUserId) && !StringUtil.isEmpty(currentLoginedSession)) {
			Account account = new Account(currentLoginedUserId, currentLoginedSession);
			this.currentAccount = account;
			online = true;
		}
	}

	public boolean isOnline() {
		return online && currentAccount != null && !StringUtil.isEmpty(currentAccount.getSessionId());
	}
	
	public void login(Account accountInfo) {
		if(accountInfo.getAccountUser() == null || accountInfo.getAccountPreference() == null
				|| StringUtil.isEmpty(accountInfo.getSessionId()) || StringUtil.isEmpty(accountInfo.getUserId())) {
			throw new IllegalArgumentException("account information is null, account="+accountInfo);
		}
		
		currentAccount = accountInfo;
		// save userid & sessionid
		Preference preference = Preference.getLoginAccountInfoPreference(getApplicationContext());
		preference.saveField("userid", accountInfo.getUserId());
		preference.saveField("session", accountInfo.getSessionId());
		online = true;
		log.d("in login method, currentAccount="+preference.getValue("userid", null)+", session="+preference.getValue("session", null));
	}

	public void logout() {
		Preference preference = Preference.getLoginAccountInfoPreference(getApplicationContext());
		preference.remove("userid");
		preference.remove("session");
		ChatSessionOrderIdHandler.release();
		MessageServiceWrapper.release();
		online = false;
		accountSqliteInstance = null;
		currentAccount.setSessionId("");
	}

	public SQLiteDatabase getSqliteInstance() {
		if (!isOnline()) {
			return null;
		}
		if (accountSqliteInstance == null || !accountSqliteInstance.isOpen()) {
			AccountDBOpenHandler dbOpenHandler = new AccountDBOpenHandler(this, currentAccount.getUserId());
			accountSqliteInstance = dbOpenHandler.getWritableDatabase();
		}
		return accountSqliteInstance;
	}


	public Account getCurrentAccount() {
		return currentAccount;
	}
}
