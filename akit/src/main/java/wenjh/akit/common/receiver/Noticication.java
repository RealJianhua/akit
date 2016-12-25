package wenjh.akit.common.receiver;

import android.os.Bundle;

import java.io.Serializable;

public class Noticication {
	private Bundle bundle = null;
	private String action = null;
	
	public Noticication(String action) {
		this.bundle = new Bundle();
		this.action = action;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public void putString(String key, String value) {
		bundle.putString(key, value);
	}
	
	public boolean containsKey(String key) {
		return bundle.containsKey(key);
	}
	
	public void putInt(String key, int value) {
		bundle.putInt(key, value);
	}
	
	public void putStringArray(String key, String[] array) {
		bundle.putStringArray(key, array);
	}

	public void putSerializable(String key, Serializable obj) {
		bundle.putSerializable(key, obj);
	}
	
	public Serializable getSerializable(String key) {
		return bundle.getSerializable(key);
	}
	
	public String getString(String key) {
		return bundle.getString(key);
	}
}
