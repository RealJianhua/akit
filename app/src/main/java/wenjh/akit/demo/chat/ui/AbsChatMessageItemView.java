package wenjh.akit.demo.chat.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.common.view.SmartImageView;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.ui.UserProfileActivity;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;

public class AbsChatMessageItemView extends FrameLayout {
	private boolean mReceived = false;
	private Message mMessage = null;
	private User mOwnerUser = null;
	private SmartImageView mAavatarImageView = null;
	private TextView mUserNameView = null;
	private TextView mStatusView = null;
	private LogUtil log = new LogUtil(this);
	private List<Message> mAllMessages = null;

	public AbsChatMessageItemView(Context context, boolean reveive) {
		super(context);
		this.mReceived = reveive;
	}

	protected void setContentView(int layoutView) {
		if (getChildCount() > 0) {
			removeAllViews();
		}
		inflate(getContext(), layoutView, this);
		initViews();
	}

	protected void setContentView(View view) {
		if (getChildCount() > 0) {
			removeAllViews();
		}
		addView(view);
		initViews();
	}

	private void initViews() {
		mAavatarImageView = (SmartImageView) findViewById(R.id.chatmessageitem_iv_photo);
		mUserNameView = (TextView) findViewById(R.id.chatmessageitem_tv_ownername);
		mStatusView = (TextView) findViewById(R.id.chatmessageitem_tv_status);
		if (mAavatarImageView != null) {
			mAavatarImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getContext(), UserProfileActivity.class);
					intent.putExtra(UserProfileActivity.KEY_USERID, mOwnerUser.getId());
					getContext().startActivity(intent);
				}
			});
		}
	}

	public void setMessage(Message message) {
		this.mMessage = message;
		if (mReceived) {
			this.mOwnerUser = message.getRemoteUser();
		} else {
			this.mOwnerUser = ContextUtil.getCurrentUser();
		}
		refreshViews();
	}

	public void setAllAdapterMessages(List<Message> allMessages) {
		this.mAllMessages = allMessages;
	}

	public List<Message> getAllAdapterMessages() {
		return mAllMessages;
	}

	public void refreshViews() {
		if (mAavatarImageView != null) {
			mAavatarImageView.loadImageGuid(mOwnerUser.getAvatar());
		}
		if (mUserNameView != null) {
			mUserNameView.setText(mOwnerUser.getDisplayName());
		}

		if (mStatusView != null && showStatusView()) {
			mStatusView.setVisibility(View.VISIBLE);
			switch (mMessage.getStatus()) {
			case Message.STATUS_LOCATING:
				mStatusView.setText("locating");
				mStatusView.setBackgroundResource(R.drawable.bg_message_status_sending);
				break;
			case Message.STATUS_UPLOADING:
				mStatusView.setText("uploading");
				mStatusView.setBackgroundResource(R.drawable.bg_message_status_sending);
				break;
			case Message.STATUS_SENDING:
				mStatusView.setText("sending");
				mStatusView.setBackgroundResource(R.drawable.bg_message_status_sending);
				break;
			case Message.STATUS_SEND_FAILED:
				mStatusView.setText("failed");
				mStatusView.setBackgroundResource(R.drawable.bg_message_status_failed);
				break;
			case Message.STATUS_SEND_READED:
				mStatusView.setText("readed");
				mStatusView.setBackgroundResource(R.drawable.bg_message_status_readed);
				break;
			case Message.STATUS_SEND_SUCCESSFUL:
				mStatusView.setText("success");
				mStatusView.setBackgroundResource(R.drawable.bg_message_status_sended);
				break;
			default:
				break;
			}
		} else if(mStatusView != null) {
			mStatusView.setVisibility(View.GONE);
		}
	}
	
	private boolean showStatusView() {
		return !(mMessage.getStatus() == Message.STATUS_SEND_SUCCESSFUL && mMessage.getChatSessionType() == Message.CHATTYPE_COMMUNITY)
				&& !(mMessage.getStatus() == Message.STATUS_SEND_READED && mMessage.getChatSessionType() == Message.CHATTYPE_COMMUNITY);
	}

	protected User getOwnerUser() {
		return mOwnerUser;
	}

	protected Message getMessage() {
		return mMessage;
	}

	protected boolean isReceived() {
		return mReceived;
	}
}
