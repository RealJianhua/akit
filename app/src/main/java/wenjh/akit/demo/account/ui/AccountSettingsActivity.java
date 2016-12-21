package wenjh.akit.demo.account.ui;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.demo.maintab.SplashActivity;
import wenjh.akit.activity.base.BaseActivity;
import wenjh.akit.common.receiver.MessageKeys;
import wenjh.akit.common.view.HoursPickerDialog;

public class AccountSettingsActivity extends BaseActivity {
	private Switch mNotificationSwitch;
	private TextView  mLogoutView;
	
	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		setContentView(R.layout.activity_account_settings);
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected void initViews() {
		mNotificationSwitch = (Switch) findViewById(R.id.setting_notification_switch);
		mLogoutView = (TextView) findViewById(R.id.logout_textview);
		mNotificationSwitch.setChecked(userPreference.notifyOpenMuteTime);
	}

	@Override
	protected void initDatas() {

	}

	/**
	 * add all the listener in here
	 */
	@Override
	protected void initEvents() {
		// notification switch listener
		mNotificationSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					HoursPickerDialog dialog = new HoursPickerDialog(thisActivity());
					dialog.setTitle("Chose Mute Time");
					dialog.setInitValue(userPreference.startNotififyMuteHour, userPreference.endNotifyMuteHour);
					dialog.setConfimlListener(new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							userPreference.notifyOpenMuteTime = true;
							userPreference.startNotififyMuteHour = ((HoursPickerDialog)dialog).getStartHour();
							userPreference.endNotifyMuteHour =((HoursPickerDialog)dialog).getEndHour();
							userPreference.saveAll();
							dialog.dismiss();
						}
					});
					dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							mNotificationSwitch.setChecked(false);
						}
					});
					showDialog(dialog);
				} else {
					userPreference.notifyOpenMuteTime = false;
				}
			}
		});

		// log out listener
		mLogoutView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getApp().logout();
				Intent i = new Intent(getApplicationContext(), SplashActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
				getApp().dispatchMessage(new Bundle(), MessageKeys.Action_Account_Logout);
			}
		});
	}
	
}
