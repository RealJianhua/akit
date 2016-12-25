package wenjh.akit.demo.chat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import com.wenjh.akit.R;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.PicassoUtil;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.NotificationReceiver;
import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.demo.config.HostConfigs;

public class NotificationUtil {
	private static LogUtil log = new LogUtil("NotificationUtil");
	private static long preNotifyTime = 0;
	private static int requestCode = 1;
	
	@SuppressLint("NewApi")
	public static void sendReceiveMessageNotify(String largeIconGUID, int iconResId, String title, String content, int number,
			int notifyID, boolean mute, Intent intent) {
		AccountSettingPreference preference = ContextUtil.getCurrentPreference();
		NotificationManager nm = (NotificationManager) ContextUtil.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(ContextUtil.getContext());
		
		while (preference.notificationSound || preference.notificationVibrate) {
			if (preNotifyTime != 0 && Math.abs(System.currentTimeMillis() - preNotifyTime) < 2000) {
				break;
			}

			nm.cancel(notifyID);
			
			if(mute) {
				break;
			}
			
			preNotifyTime = System.currentTimeMillis();
			if(preference.notificationSound) {
				builder.setSound(Uri.parse("android.resource://" + ContextUtil.getPackageName() + "/" + R.raw.ms));
			}

			if(preference.notificationVibrate) {
				builder.setVibrate(new long[] { 50, 100});
			}

			builder.setLights(Color.BLUE, 500, 1500);
			
			break;
		}
		
		if(number > 0) {
			builder.setNumber(number);
		}

		if (content.length() > 20) {
			String ticker = content.substring(0, 20) + "...";
			builder.setTicker(ticker);
		} else {
			builder.setTicker(content);
		}
		
		builder.setAutoCancel(true);
		builder.setSmallIcon(iconResId);
		builder.setContentTitle(title);
		builder.setContentText(content);
		
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendIntent = PendingIntent.getActivity(ContextUtil.getContext(), requestCode ++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendIntent);

		if(ContextUtil.isJBVsersion()) {
			Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
			bigTextStyle.bigText(content);
			builder.setStyle(bigTextStyle);
		}
		
		Notification notification = null;
		if(Build.VERSION.SDK_INT >= 16) {
			notification = builder.build();
		} else {
			notification = builder.getNotification();
		}
		
		nm.notify(notifyID, notification);
		if(!StringUtil.isEmpty(largeIconGUID)) {
			PicassoUtil.picasso().load(HostConfigs.getImageUrlWithGUID(largeIconGUID))
					.into(notification.contentView, android.R.id.icon, notifyID, notification);
		}
	}
	
	
	public static void removePeopleMessageNotify() {
		NotificationManager nm = (NotificationManager) ContextUtil.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NotificationReceiver.NOTIFYID_MESSAGE);
	}

	public static void removeCommunityMessageNotify() {
		NotificationManager nm = (NotificationManager) ContextUtil.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NotificationReceiver.NOTIFYID_COMMUNITY);
	}
	
	public static void removeCommunityNoticeNotify() {
		NotificationManager nm = (NotificationManager) ContextUtil.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NotificationReceiver.NOTIFYID_COMMUNITY_NOTICE);
	}
	
	public static void removeAllMessageNotify() {
		NotificationManager nm = (NotificationManager) ContextUtil.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
	}
}
