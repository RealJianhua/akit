package wenjh.akit.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtil {
	private SharedPreferences preferences = null;
	private String name;
	private final static String FILE_NAME = "preference";
	
	private SharedPreferencesUtil(Context context, String name) {
		preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		this.name = name;
	}
	
	public static SharedPreferencesUtil getInstance(Context context, String name) {
		SharedPreferencesUtil instance = new SharedPreferencesUtil(context, name);
		return instance;
	}
	
	public boolean contains(String key) {
		return preferences.contains(key);
	}
	
	public void putValue(String key, int value) {
		Editor editor = preferences.edit();  
        editor.putInt(key, value);  
        editor.commit();  
	}
	
	public void putValue(String key, String value) {
		Editor editor = preferences.edit();  
        editor.putString(key, value);
        editor.commit();  
	}
	
	public void remove(String key) {
		Editor editor = preferences.edit();  
        editor.remove(key);
        editor.commit();
	}
	
	public void putValue(String key, double value) {
		Editor editor = preferences.edit();  
        editor.putFloat(key, (float)value);
        editor.commit();  
	}
	
	public void putValue(String key, boolean value) {
		Editor editor = preferences.edit();  
        editor.putBoolean(key, value);
        editor.commit();  
	}
	
	public void putValue(String key, float value) {
		Editor editor = preferences.edit();  
        editor.putFloat(key, value);
        editor.commit();  
	}
	
	public void putValue(String key, long value) {
		Editor editor = preferences.edit();  
        editor.putLong(key, value);
        editor.commit();  
	}
	
	public void put(String key, Object value) {
		if(value instanceof Long) {
			putValue(key, (Long)value);
		} else if(value instanceof Float) {
			putValue(key, (Float)value);
		} else if(value instanceof String) {
			putValue(key, (String)value);
		} else if(value instanceof Integer) {
			putValue(key, (Integer)value);
		} else if(value instanceof Boolean) {
			putValue(key, (Boolean)value);
		} else if(value instanceof Double) {
			putValue(key, (Double)value);
		} else {
			putValue(key, value.toString());
		}
	}
	
	public Object get(String key, Object defaultValue) {
		Object result = null;
		try {
			if(defaultValue instanceof Long) {
				result = get(key, (Long)defaultValue);
			} else if(defaultValue instanceof Float) {
				result = get(key, (Float)defaultValue);
			} else if(defaultValue instanceof String) {
				result = get(key, (String)defaultValue);
			} else if(defaultValue instanceof Integer) {
				result = get(key, (Integer)defaultValue);
			} else if(defaultValue instanceof Boolean) {
				result = get(key, (Boolean)defaultValue);
			} else {
				return preferences.getString(key, null);
			}
			return result;
		} catch (Exception e) {
		}
		return defaultValue;
	}
	
	public String get(String key, String defValue) {
		return preferences.getString(key, defValue);
	}	
	
	public long get(String key, Long defValue) {
		try {
			return preferences.getLong(key, defValue);
		} catch (Exception e) {
		}
		return defValue;
	}	
	
	public float get(String key, Float defValue) {
		return preferences.getFloat(key, defValue);
	}	
	
	public int get(String key, Integer defValue) {
		return preferences.getInt(key, defValue);
	}
	
	public boolean get(String key, Boolean defValue) {
		return preferences.getBoolean(key, defValue);
	}
	
	public SharedPreferences getPreferences() {
		return preferences;
	}
	
	public void clear() {
		Editor edit = preferences.edit();
		edit.clear();
		edit.commit();
	}
}
