package wenjh.akit.demo;

import wenjh.akit.demo.account.model.Account;
import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.demo.people.model.User;

public class ContextUtil extends wenjh.akit.common.util.ContextUtil {

	public static AKitApplicationDemo getApp() {
		return (AKitApplicationDemo) wenjh.akit.common.util.ContextUtil.getApp();
	}

	public static Account getCurrentAccount() {
		return getApp().getCurrentAccount();
	}
	
	public static String getCurrentAccountSession() {
		Account accountInfo = getApp().getCurrentAccount();
		if(accountInfo != null) {
			return accountInfo.getSessionId();
		}
		return null;
	}
	
	public static User getCurrentUser() {
		Account accountInfo = getApp().getCurrentAccount();
		if(accountInfo != null) {
			return accountInfo.getAccountUser();
		} else {
			return null;
		}
	}

	public static AccountSettingPreference getCurrentPreference() {
		Account accountInfo = getApp().getCurrentAccount();
		if(accountInfo != null) {
			return accountInfo.getAccountPreference();
		} else {
			return null;
		}
	}
}
