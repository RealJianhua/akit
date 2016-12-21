package wenjh.akit.demo.people.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.demo.account.model.IUserTable;
import wenjh.akit.demo.account.db.BaseDao;
import wenjh.akit.common.util.StringUtil;

public class UserDao extends BaseDao<User, String> implements IUserTable {

	public UserDao(SQLiteDatabase db) {
		super(db, TableName, F_UserId);
	}

	@Override
	protected User assemble(Cursor cursor) {
		User user = new User();
		assemble(user, cursor);
		return user;
	}

	@Override
	protected void assemble(User user, Cursor cursor) {
		user.setAbout(getString(cursor, F_About));
		user.setAvatar(getString(cursor, F_Avatar));
		user.setCover(getString(cursor, F_Cover));
		user.setId(getString(cursor, F_UserId));
		user.setName(getString(cursor, F_Name));
		user.setInterests(getStringList(cursor, F_Interests));
		user.setAge(getInt(cursor, F_Age));
		try {
			String jsonString = getString(cursor, F_Gender);
			if(!StringUtil.isEmpty(jsonString)) {
				JSONObject jsonObject = new JSONObject(jsonString);
				UserGender gender = new UserGender();
				gender.setGender_disp(jsonObject.optString("disp"));
				gender.setGender_orig(jsonObject.optInt("gender"));
				user.setGender(gender);
			}
		} catch (JSONException e) {
			log.e(e);
		}

		try {
			String jsonString = getString(cursor, F_Friends);
			if(!StringUtil.isEmpty(jsonString)) {
				JSONArray jsonArray = new JSONArray(jsonString);
				List<User> userList = new ArrayList<User>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					User u = new User();
					u.setId(jsonObject.getString("id"));
					u.setName(jsonObject.optString("name"));
					u.setCover(jsonObject.optString("cover"));
					u.setAvatar(jsonObject.optString("avatar"));
					userList.add(u);
				}
				user.setFriends(userList);
			}
		} catch (JSONException e) {
			log.e(e);
		}
	}

	@Override
	public void insert(User t) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(F_UserId, t.getId());
		map.put(F_Name, t.getName());
		map.put(F_Avatar, t.getAvatar());
		map.put(F_Cover, t.getCover());
		map.put(F_About, t.getAbout());
		map.put(F_Interests, t.getInterests());
		map.put(F_Age, t.getAge());
		if(t.getGender()!= null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("gender", t.getGender().getGender_orig());
				jsonObject.put("disp", t.getGender().getGender_disp());
				map.put(F_Gender, jsonObject.toString());
			} catch (JSONException e) {
				log.e(e);
			}
		}

		if(t.getFriends() != null) {
			try {
				JSONArray jsonArray = new JSONArray();
				for (User u : t.getFriends()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", u.getId());
					jsonObject.put("name", u.getName());
					jsonObject.put("cover", u.getCover());
					jsonObject.put("avatar", u.getAvatar());
					jsonArray.put(jsonObject);
				}
				map.put(F_Friends, jsonArray.toString());
			} catch (JSONException e) {
				log.e(e);
			}
		}

		insertFileds(map);
	}

	@Override
	public void update(User t) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(F_Name, t.getName());
		map.put(F_Avatar, t.getAvatar());
		map.put(F_Cover, t.getCover());
		map.put(F_About, t.getAbout());
		map.put(F_Interests, t.getInterests());
		map.put(F_Age, t.getAge());
		if(t.getGender()!= null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("gender", t.getGender().getGender_orig());
				jsonObject.put("disp", t.getGender().getGender_disp());
				map.put(F_Gender, jsonObject.toString());
			} catch (JSONException e) {
				log.e(e);
			}
		}
		updateFileds(map, new String[]{F_UserId}, new String[]{t.getId()});;
	}

	@Override
	public void deleteInstence(User obj) {
		delete(obj.getId());
	}

}
