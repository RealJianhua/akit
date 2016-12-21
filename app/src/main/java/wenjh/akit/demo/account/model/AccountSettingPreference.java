package wenjh.akit.demo.account.model;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class AccountSettingPreference extends Preference {
	private static final String NOTIFY_MUTETIME_END = "startmutetime";
	private static final String NOTIFY_MUTETIME_START = "endmutetime";
	private static final String NOTIFY_OPEN_MUTETIME = "openmutetime";
	private static final String NOTIFCATION_OPEND = "notifcationopend";
	private static final String NOTIFY_VIBRATE = "notifyvibrate";
	private static final String NOTIFY_SOUND = "notifysound";
	public boolean notificationSound = true;
	public boolean notificationVibrate = true;
	public boolean notifcationOpend = true;
	public boolean notifyOpenMuteTime;
	public int startNotififyMuteHour = 0;
	public int endNotifyMuteHour = 0;

	public AccountSettingPreference(Context context, String name) {
		super(context, name);
		init();
	}

	public void init() {
		notificationSound = getValue(NOTIFY_SOUND, false);
		notificationVibrate = getValue(NOTIFY_VIBRATE, false);
		notifcationOpend = getValue(NOTIFCATION_OPEND, true);
		notifyOpenMuteTime = getValue(NOTIFY_OPEN_MUTETIME, false);
		startNotififyMuteHour = getValue(NOTIFY_MUTETIME_START, 22);
		endNotifyMuteHour = getValue(NOTIFY_MUTETIME_END, 9);
	}
	
	public void saveAll() {
		Editor editor = getEditor();
		editor.putBoolean(NOTIFY_SOUND, notificationSound);
		editor.putBoolean(NOTIFY_VIBRATE, notificationVibrate);
		editor.putBoolean(NOTIFCATION_OPEND, notifcationOpend);
		editor.putBoolean(NOTIFY_OPEN_MUTETIME, notifyOpenMuteTime);
		editor.putInt(NOTIFY_MUTETIME_START, startNotififyMuteHour);
		editor.putInt(NOTIFY_MUTETIME_END, endNotifyMuteHour);
		editor.commit();
	}
}
