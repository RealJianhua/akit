package wenjh.akit.common.receiver;

import wenjh.akit.common.util.ContextUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhonePowerOnReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(ContextUtil.getApp().isOnline()) {
			ContextUtil.getApp().launchIMService();
		}
	}

}
