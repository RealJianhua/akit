package wenjh.akit;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import wenjh.akit.demo.account.model.Account;
import wenjh.akit.demo.account.model.Preference;
import wenjh.akit.demo.chat.model.ChatSessionOrderIdHandler;
import wenjh.akit.demo.account.db.AccountDBOpenHandler;
import wenjh.akit.common.receiver.NotificationReceiver;
import wenjh.akit.common.receiver.ReceiverDispatcher;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.view.Toaster;
import wenjh.akit.demo.chat.model.MessageServiceWrapper;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;

public class AKitApplication extends Application {
	private final static int Msg_DispatchMessage = 954;
	private Account currentAccount = null;
	private SQLiteDatabase accountSqliteInstance = null;
	private LogUtil log = new LogUtil(this);
	private boolean online = false;
	private ReceiverDispatcher receiverDispatcher = null;

	@Override
	public void onCreate() {
		super.onCreate();
		ContextUtil.initApplicationContext(this);
		Toaster.initApplicationContext(this);

		// restore account class
		Preference preference = Preference.getLoginAccountInfoPreference(getApplicationContext());
		String currentLoginedUserId = preference.getValue("userid", null);
		String currentLoginedSession = preference.getValue("session", null);
		log.i("BlupeApplication onCreated, currentLoginedUserId="+currentLoginedUserId+", currentLoginedSession="+currentLoginedSession);
		if(!StringUtil.isEmpty(currentLoginedUserId) && !StringUtil.isEmpty(currentLoginedSession)) {
			Account account = new Account(currentLoginedUserId, currentLoginedSession);
			this.currentAccount = account;
			online = true;
		}
		
		receiverDispatcher = new ReceiverDispatcher();
		receiverDispatcher.registerReceiver(new NotificationReceiver());
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
		closeIMService();
	}

	
	public void closeIMService() {
		// TODO
//		stopService(new Intent(getApplicationContext(), IMBackgroundService.class));
	}
	
	public void launchIMService() {
		// TODO
//		startService(new Intent(getApplicationContext(), IMBackgroundService.class));
	}
	
	public void dispatchMessage(Bundle bundle, String action) {
		android.os.Message message = new android.os.Message();
		message.what = Msg_DispatchMessage;
		message.obj = action;
		message.setData(bundle);
		handler.sendMessage(message);
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

	public ReceiverDispatcher getReceiverDispatcher() {
		return receiverDispatcher;
	}

	public Account getCurrentAccount() {
		return currentAccount;
	}

	public void onApplicationOpened() {
		launchIMService();
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(msg.what == Msg_DispatchMessage) {
				Bundle bundle = msg.getData();
				if(bundle == null) {
					bundle = new Bundle();
				}
				if(msg.obj == null){
					log.w("dispatcher receive， action is null");
					return;
				}
				receiverDispatcher.dispatch(bundle, msg.obj+"");
			}
		};
	};
}
