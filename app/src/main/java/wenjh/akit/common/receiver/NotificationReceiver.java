package wenjh.akit.common.receiver;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wenjh.akit.R;

import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.demo.maintab.MainTabActivity;
import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.demo.maintab.Community;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.NotificationUtil;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.model.UserService;

public class NotificationReceiver extends AbsMessageReceiver {
	public final static int NOTIFYID_MESSAGE = 1950;
	public final static int NOTIFYID_COMMUNITY = 1952;
	public final static int NOTIFYID_COMMUNITY_NOTICE = 1953;

	User currentUser = null;
	AccountSettingPreference preference = null;
	Context context = null;
	LogUtil log = new LogUtil(this);

	public NotificationReceiver() {
		super(50, MessageKeys.Actions_AllNotificationMessage);
	}

	@Override
	public boolean onReceive(Bundle bundle, String action) {
		currentUser = ContextUtil.getCurrentUser();
		preference = ContextUtil.getCurrentPreference();
		context = ContextUtil.getContext();
		if (currentUser == null || preference == null || context == null) {
			return false;
		}

//		if (!preference.notifcationOpend) {
//			return false;
//		}
		
		if (preference.notifyOpenMuteTime) {
			int startHour = preference.startNotififyMuteHour;
			int endHour = preference.endNotifyMuteHour;

			Calendar c = Calendar.getInstance();
			int nowHour = c.get(Calendar.HOUR_OF_DAY);

			boolean mutetime = false;
			if (startHour < endHour) {
				mutetime = (nowHour >= startHour && nowHour < endHour);
			} else {
				mutetime = ((nowHour >= startHour && nowHour < 24) || (nowHour >= 0 && nowHour < endHour));
			}

			if (mutetime) {
				log.i("mutetime, action="+action);
				return true;
			}
		}

		/*if (MessageKeys.Action_CommunityMessge.equals(action)) {
			notifiyCommunityMessage(bundle);
		} else */
		if (MessageKeys.Action_UserMessge.equals(action)) {
			notifiyUserMessage(bundle);
		}
		return false;
	}

	private void notifiyUserMessage(Bundle bundle) {
		Message message = getMessage(bundle);
		if (message == null || !message.isReceived()) {
			return;
		}

		String title, content;
		int iconResId, notifyId, number;
		String largeId = null;
		content = message.getNotificationContent();

		User user = new UserService().getUser(message.getRemoteUserId());
		// default name TODO
		title = user == null ? "" : user.getDisplayName();
		content = user == null ? content : user.getDisplayName()+":"+content;
		largeId = user != null ? user.getAvatar() : null;

		Intent intent = new Intent(context, MainTabActivity.class);
		intent.putExtra(MainTabActivity.KEY_TABINDEX, 1);
		notifyId = NOTIFYID_MESSAGE;
		iconResId = R.drawable.ic_taskbar_icon;
		number = bundle.getInt(MessageKeys.Key_UnreadCount_User, 1);
		NotificationUtil.sendReceiveMessageNotify(largeId, iconResId, title, content, number, notifyId, false, intent);
	}

	private Message getMessage(Bundle bundle) {
		@SuppressWarnings("unchecked")
		List<Message> msgList = (List<Message>) bundle.getSerializable(MessageKeys.Key_MessageArray);
		if (msgList == null || msgList.isEmpty()) {
			return null;
		}

		// 从后向前找不是Notice的消息用来提示
		Message message = null;
		for (int i = msgList.size() - 1; i >= 0; i--) {
			Message m = msgList.get(i);
			// notice不提示
			if (m == null || Message.CONTENTTYPE_MESSAGE_NOTICE == m.getContentType()) {
				continue;
			} else {
				message = m;
				break;
			}
		}

		return message;
	}
}
