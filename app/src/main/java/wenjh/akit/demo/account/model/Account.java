package wenjh.akit.demo.account.model;

import java.util.Date;

import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.location.model.LatLng;
import wenjh.akit.demo.location.model.LocationHelper;
import wenjh.akit.demo.people.model.User;

public class Account {
	private User userInfo;
	private String sessionId;
	private String deviceId;
	private AccountSettingPreference preference = null;
	private LatLng location;
	private Preference locationPreference = null;

	public Account() {
		userInfo = new User();
	}

	public Account(String userId) {
		User user = new User(userId);
		setUserInfo(user);
	}

	public Account(String userId, String sessionId) {
		this(userId);
		this.sessionId = sessionId;
	}

	public Account(User currentUser) {
		if (currentUser == null) {
			throw new NullPointerException("AccountInfo init failed. currentUser != null.");
		}
		setUserInfo(currentUser);
	}

	public User getAccountUser() {
		return userInfo;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public AccountSettingPreference getAccountPreference() {
		return preference;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setUserInfo(User userInfo) {
		this.userInfo = userInfo;
		preference = new AccountSettingPreference(ContextUtil.getContext(), userInfo.getId());
		locationPreference = new Preference(ContextUtil.getContext(), "myloc_"+userInfo.getId());
		double lat = locationPreference.getValue("lat", 0f);
		double lng = locationPreference.getValue("lng", 0f);
		float acc = locationPreference.getValue("acc", 0f);
		Date time = locationPreference.getTime("time", null);
		if(LocationHelper.isLocationAvailable(lat, lng)) {
			location = new LatLng(lat, lng, acc);
			location.setTime(time);
		}
	}

	public String getUserId() {
		return this.userInfo != null ? this.userInfo.getId() : null;
	}
	
	public void updateCurrentLocation(LatLng location) {
		this.location = location;
		if(locationPreference != null && location != null) {
			locationPreference.saveField("lat", location.getLatitude());
			locationPreference.saveField("lng", location.getLongitude());
			locationPreference.saveField("acc", location.getAccuracy());
			locationPreference.saveTime("time", location.getTime());
		}
	}
	
	public LatLng getLocation() {
		return location;
	}
	
	public Date getLocationTime() {
		return location != null ? location.getTime() : null;
	}

	@Override
	public String toString() {
		return "Account [userInfo=" + userInfo + ", sessionId=" + sessionId + ", deviceId=" + deviceId
				+ ", userId=" + getUserId() + ", preference=" + preference + "]";
	}

}
