package wenjh.akit.demo.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wenjh.akit.demo.account.model.Account;
import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.common.http.HttpClient;
import wenjh.akit.demo.people.model.User;

public class AccountApi extends HttpClient {

	private static AccountApi accountApiInstance = null;

	private AccountApi() {
	}

	public static AccountApi getInstance() {
		if (accountApiInstance == null) {
			accountApiInstance = new AccountApi();
		}
		return accountApiInstance;
	}

	public void loginWithFacebookAccount(Account accountInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("device_id", accountInfo.getDeviceId());

		// login

		// parse response
		accountInfo.setSessionId("AKIT_TEST_SESSIONID");

		// parse my profile
		User user = new User("akit_tmploginuserid_001");
		accountInfo.setUserInfo(user);
		downloadMyProfile(user, accountInfo.getAccountPreference());
	}

	public void downloadMyProfile(User user, AccountSettingPreference preference) throws Exception {
		user.setAbout("这是一段介绍");
		user.setName("akit");
		user.setCover("post-bg-js-module");
		user.setAvatar("digwyfvrfoefowbf");
		user.setAge(18);

		List<String> list = new ArrayList<String>();
		list.add("Android");
		list.add("Developer");
		list.add("Kit");
		user.setInterests(list);

		List<User> friends = new ArrayList<>();
		User tmpUser = new User("test_id_2");
		tmpUser.setAvatar("kywhwkkd82");
		friends.add(tmpUser);
		tmpUser = new User("test_id_3");
		tmpUser.setAvatar("kw-wdwe-kjw2");
		friends.add(tmpUser);
		tmpUser = new User("test_id_4");
		tmpUser.setAvatar("egergergreegqq22");
		friends.add(tmpUser);
		user.setFriends(friends);
	}
}
