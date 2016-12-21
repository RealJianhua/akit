package wenjh.akit.demo.account.model;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences.Editor;

import wenjh.akit.common.util.SharedPreferencesUtil;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.common.util.ContextUtil;

public class Preference {
	SharedPreferencesUtil p = null;
	Context context = null;
	String name = null;

	public Preference(Context context, String name) {
		this.context = context;
		this.name = name;
		p = SharedPreferencesUtil.getInstance(context, name);
	}

	public void saveField(String key, Object value) {
		p.put(key, value);
	}

	public void remove(String key) {
		p.remove(key);
	}

	public <T> T getValue(String key, T defaultValue) {
		try {
			return (T) p.get(key, defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	public Editor getEditor() {
		return p.getPreferences().edit();
	}

	public Date getTime(String key, Date defaultValue) {
		try {
			long value = p.get(key, 0L);
			if (value > 0) {
				return new Date(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public void saveTime(String key, Date value) {
		try {
			p.put(key, value == null ? 0L : value.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveValue(String key, Object value) {
		saveField(key, value);
	}

	private static Preference timePreference = null;

	public static Preference getTimesPreference(Context context) {
		User user = ContextUtil.getCurrentUser();
		String name = user != null ? user.getId() : "public";
		name += "times";

		if (timePreference == null || !name.equals(timePreference.name)) {
			timePreference = new Preference(context, name);
		}

		return timePreference;
	}

	private static Preference securityPreference = null;

	public static Preference getSecurityPreference(Context context) {
		User user = ContextUtil.getCurrentUser();
		String name = user != null ? user.getId() : "public";
		name += "securityinfo";

		if (securityPreference == null || !name.equals(securityPreference.name)) {
			securityPreference = new Preference(context, name);
		}

		return securityPreference;
	}

	static Preference flagPreference = null;

	public static Preference getFlagsPreference(Context context) {
		User user = ContextUtil.getCurrentUser();
		String name = user != null ? user.getId() : "public";
		name += "flags";
		if (flagPreference == null || !name.equals(flagPreference.name)) {
			flagPreference = new Preference(context, name);
		}
		return flagPreference;
	}

	static Preference imjPublicConfigsPreference = null;

	public static Preference getIMJPublichConfigsPreference(Context context) {
		String name = "imjpublicconfigs";
		if (imjPublicConfigsPreference == null) {
			imjPublicConfigsPreference = new Preference(context, name);
		}
		return imjPublicConfigsPreference;
	}

	public void clear() {
		p.clear();
	}

	public static Preference getLoginAccountInfoPreference(Context context) {
		return new Preference(context, "currentaccount");
	}
}
