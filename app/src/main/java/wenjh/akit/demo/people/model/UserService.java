package wenjh.akit.demo.people.model;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.demo.account.db.AccountDBOpenHandler;
import wenjh.akit.demo.account.model.IUserTable;
import wenjh.akit.common.util.ContextUtil;

public class UserService {
	private UserDao userDao = null;
	private SQLiteDatabase db = null;
	
	public UserService() {
		db = ContextUtil.getApp().getSqliteInstance();
		userDao = new UserDao(db);
	}
	
	public UserService(String id) {
		AccountDBOpenHandler dbOpenHandler = new AccountDBOpenHandler(ContextUtil.getContext(), id);
		db = dbOpenHandler.getWritableDatabase();
		userDao = new UserDao(db);
	}
	
	public SQLiteDatabase getDb() {
		return db;
	}
	
	public void saveFullUser(User user) {
		if(userDao.checkExist(user.getId())) {
			userDao.update(user);
		} else {
			userDao.insert(user);
		}
	}
	
	public boolean isUserCacheExist(String userId) {
		return userDao.checkExist(userId);
	}
	
	public void saveFullUser(List<User> users) {
		db.beginTransaction();
		for (User user : users) {
			saveFullUser(user);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public void saveSimpleUser(User user) {
		// TODO
	}
	
	public void saveSimpleUser(List<User> users) {
		db.beginTransaction();
		for (User user : users) {
			saveSimpleUser(user);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public User getUser(String userId) {
		return userDao.get(userId);
	}
	
	/**
	 * 
	 * @param user
	 * @return false: find cache failed(not cache this user)
	 */
	public boolean getUser(User user) {
		return userDao.get(user, user.getId());
	}
	
	public void blockUser(String userId) {
		userDao.updateFiled(IUserTable.F_Blocked, true, userId);
	}
}
