package wenjh.akit.demo.people;

import java.util.ArrayList;

import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.location.model.LatLng;
import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.common.http.HttpClient;

public class UserApi extends HttpClient {
	private static UserApi sUserApiInstance = null;
	
	public static UserApi getInstance() {
		if(sUserApiInstance == null) {
			sUserApiInstance = new UserApi();
		}
		return sUserApiInstance;
	}
	
	/**
	 * download other people's profile you can not use this to get your own
	 * profile, you must use downloadMyProfile()
	 * 
	 * @param user
	 * @throws Exception
	 */
	public void downloadUserProfile(User user) throws Exception {
		if("akit_tmploginuserid_001".equals(user.getId())) {
			user.setAbout("这是一段介绍");
			user.setName("akit");
			user.setCover("post-bg-js-module");
			user.setAvatar("digwyfvrfoefowbf");
			user.setAge(18);
		} else if("test_id_2".equals(user.getId())) {
			user.setCover("tag-bg");
			user.setAvatar("kywhwkkd82");
			user.setName("akit46");
		} else if("test_id_3".equals(user.getId())) {
			user.setCover("post-bg-2015");
			user.setAvatar("kw-wdwe-kjw2");
			user.setName("akit48");
		} else if("test_id_4".equals(user.getId())) {
			user.setCover("home-bg-o");
			user.setAvatar("egergergreegqq22");
			user.setName("akit48");
		} else {
			throw new IllegalAccessException("not implement");
		}
	}


	/**
	 * use this method to set your profile
	 * 
	 * @param user
	 * @throws Exception
	 */
	public void updateUserProfile(User user, AccountSettingPreference preference) throws Exception {
		throw new IllegalAccessException("not implement");
	}

	public void blockUser(User user, boolean report, int reportReasons) throws Exception {
		throw new IllegalAccessException("not implement");
	}

	public void unblockUser(User user) throws Exception {
		throw new IllegalAccessException("not implement");
	}

	/**
	 * this will give you a list of blocked user id, you can use this ID later
	 * to query their profile.
	 * 
	 * @return
	 * @return
	 * @throws Exception
	 */
	public ArrayList<User> getBlockUserList() throws Exception {
		throw new IllegalAccessException("not implement");
	}

	
	public void updateLocation(LatLng latLng) throws Exception{
		throw new IllegalAccessException("not implement");
	}
	

}
