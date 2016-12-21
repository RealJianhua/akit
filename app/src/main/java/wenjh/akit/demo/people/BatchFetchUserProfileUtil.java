package wenjh.akit.demo.people;

import wenjh.akit.common.asynctask.BatchDownloadProfileUtil;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.model.UserService;

public class BatchFetchUserProfileUtil extends BatchDownloadProfileUtil<User> {
	UserService userService = null;
	
	public BatchFetchUserProfileUtil() {
		userService = new UserService();
	}
	
	@Override
	protected User newObject(String key) {
		return new User(key);
	}

	@Override
	protected void download(User user) throws Exception {
		UserApi.getInstance().downloadUserProfile(user);
		userService.saveFullUser(user);
	}
	
	@Override
	public synchronized void remove(String userId) {
		super.remove(userId);
	}
	
	@Override
	public synchronized User addToBatchList(String userId) {
		return super.addToBatchList(userId);
	}
}
