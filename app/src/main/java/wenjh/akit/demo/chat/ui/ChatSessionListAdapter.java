package wenjh.akit.demo.chat.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.common.util.BaseListAdapter;
import wenjh.akit.demo.chat.model.ChatSession;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.view.SmartImageView;

public class ChatSessionListAdapter extends BaseListAdapter<ChatSession> {
	OnItemClickListener itemClickListener = null;
	LogUtil log = new LogUtil(this);
	
	public ChatSessionListAdapter(Context context, List<ChatSession> objects, OnItemClickListener itemClickListener) {
		super(context, objects);
		this.itemClickListener = itemClickListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			convertView = inflate(R.layout.listitem_session);
			holder = new ViewHolder();
			holder.avatarImageView = (SmartImageView) convertView.findViewById(R.id.chatsessionitem_iv_avatar);
			holder.contentView = (TextView) convertView.findViewById(R.id.chatsessionitem_tv_content);
			holder.newBubbleView = (TextView) convertView.findViewById(R.id.chatsessionitem_tv_newbubble);
			holder.timeView = (TextView) convertView.findViewById(R.id.chatsessionitem_tv_time);
			holder.titleView = (TextView) convertView.findViewById(R.id.chatsessionitem_tv_title);
			holder.statusView = (TextView) convertView.findViewById(R.id.chatsessionitem_tv_status);
			convertView.setTag(holder);
			holder.avatarImageView.setPlaceholder(R.drawable.ic_common_def_header); //default avatar
			holder.avatarImageView.setOnClickListener(mAvatarOnClickListener);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ChatSession chatSession = getItem(position);
		
		holder.avatarImageView.setTag(position);
		holder.avatarImageView.loadImageGuid(chatSession.getAvatar());
		holder.contentView.setText(chatSession.getTextContent());
		holder.titleView.setText(chatSession.getDisplayName());
		
		int unreadCount = chatSession.getUnreadMessageCount();
		if(unreadCount > 0) {
			holder.timeView.setVisibility(View.GONE);
			holder.newBubbleView.setVisibility(View.VISIBLE);
			holder.newBubbleView.setText(unreadCount+"");
		} else {
			holder.newBubbleView.setVisibility(View.GONE);
			holder.timeView.setVisibility(View.VISIBLE);
			holder.timeView.setText(chatSession.getLastUpdateTimeString());
		}
		
		if(chatSession.getSessionType() == ChatSession.TYPE_PEOPLE) {
			refreshStatusView(chatSession.getLastMessageStatus(), holder.statusView);
		} else {
			holder.statusView.setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		log.w(new Exception());;
	}
	
	private void refreshStatusView(int status, TextView mStatusView) {
		mStatusView.setVisibility(View.VISIBLE);
		switch (status) {
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
			mStatusView.setVisibility(View.GONE);
			break;
		}
	}
	
	private View.OnClickListener mAvatarOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			if(itemClickListener != null) {
				itemClickListener.onItemClick(null, v, position, v.getId());
			}
		}
	};
	
	private static class ViewHolder {
		TextView contentView = null;
		TextView titleView = null;
		SmartImageView avatarImageView = null;
		TextView timeView = null;
		TextView newBubbleView = null;
		TextView statusView = null;
	}

}
