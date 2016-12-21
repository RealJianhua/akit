package wenjh.akit.demo.chat.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.common.receiver.MessageKeys;
import wenjh.akit.demo.chat.model.ChatSession;
import wenjh.akit.demo.chat.model.ChatSessionService;
import wenjh.akit.demo.maintab.Community;
import wenjh.akit.activity.base.BaseListFragment;
import wenjh.akit.demo.chat.model.MessageServiceWrapper;
import wenjh.akit.common.asynctask.AsyncCallback;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.view.MAlertDialog;
import wenjh.akit.demo.people.BatchFetchUserProfileUtil;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.ui.UserProfileActivity;

public class ChatSessionListFragment extends BaseListFragment<ChatSession> {
	private static final int PAGECOUNT = 50;
	private ChatSessionService mSessionService;
	private TextView totalUnreadBubbleView;
	private boolean mHasMoreSessions = false;
	private BatchFetchUserProfileUtil fetchUsersProrileUtil = null;
	
	@Override
	protected void onCreated(Bundle savedInstanceState) {
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected int getContentViewResourceIdAndCreateView() {
		return R.layout.fragment_sessionlist;
	}
	
	@Override
	protected void initViews() {
		setCanPullToRefresh(false);
//		new IMJConnectionMonitor(this, getBaseActivity());
	}
	
	@Override
	public void setIndicatorView(View indicatorView) {
		super.setIndicatorView(indicatorView);
		if(indicatorView != null) {
			totalUnreadBubbleView = (TextView) indicatorView.findViewById(R.id.tab_item_tv_badge);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ChatSession session = mAdapter.getItem(position);
		if(id == R.id.chatsessionitem_iv_avatar) {
			// avatar clicked, goto profile.
			Intent intent = null;
			if(session.getSessionType() == ChatSession.TYPE_COMMUNITY) {
			} else {
				intent = new Intent(getContext(), UserProfileActivity.class);
				intent.putExtra(UserProfileActivity.KEY_USERID, session.getSessionId());
			}
			startActivity(intent);
		} else {
			// item clicked, goto chat.
			Intent intent = null;
//			if(session.getSessionType() == ChatSession.TYPE_COMMUNITY) {
//				intent = new Intent(getContext(), CommunityChatActivity.class);
//				intent.putExtra(CommunityChatActivity.KEY_REMOTECOMMUNITYID, session.getSessionId());
//			} else {
			intent = new Intent(getContext(), PeopleChatActivity.class);
			intent.putExtra(PeopleChatActivity.KEY_REMOTEUSERID, session.getSessionId());
//			}
			startActivity(intent);
		}
	}
	
	@Override
	protected void onListItemLongClick(ListView l, View v, int position, long id) {
		final ChatSession chatSession = mAdapter.getItem(position);
		MAlertDialog.makeConfirm(getActivity(), "Delete this chat?", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSessionService.delete(chatSession, true);
				mAdapter.remove(chatSession);
			}
		}).show();;
	}

	@Override
	protected void onLoadMore() {
		super.onLoadMore();
		loadSessionList();
	}
	
	@Override
	protected void initDatas() {
		ChatSessionListAdapter adapter = new ChatSessionListAdapter(getContext(), new ArrayList<ChatSession>(), getOnItemClickListener());
		mSessionService = new ChatSessionService();
		setListAdapter(adapter);
		
		// download profile helper
		fetchUsersProrileUtil = new BatchFetchUserProfileUtil();
		fetchUsersProrileUtil.setCallback(new BatchDownloadUserProfileCallback());
		
		postAsyncRunnable(new Runnable() {
			@Override
			public void run() {
				loadSessionList();
				refreshUnreadMessageCount(-1);
				registerMessageReceiver(400, MessageKeys.Actions_AllSessions);
				registerMessageReceiver(400, MessageKeys.Action_ChatSessionChanged);
			}
		});
	}
	
	private void loadSessionList() {
		mAdapter.setNotifyOnChange(false);
		
		List<ChatSession> chatSessionList = mSessionService.findSessions(mAdapter.getCount(), PAGECOUNT+1);
		final boolean hasMoreSession = chatSessionList.size() > PAGECOUNT;
		if(hasMoreSession) {
			chatSessionList.remove(PAGECOUNT);
		}
		this.mHasMoreSessions = hasMoreSession;
		
		for (ChatSession chatSession : chatSessionList) {
			mAdapter.add(chatSession);
			initNewSession(chatSession);
		}
		
		mAdapter.setNotifyOnChange(true);
		mAdapter.notifyDataSetChanged();
		
		fetchUsersProrileUtil.requestAsync();
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setLoadMoreComplete();
				setLoadMoreButtonVisibility(hasMoreSession);
			}
		});
	}
	
	@Override
	protected boolean onMessageReceive(Bundle bundle, String action) {
		int totalUnreadedCount = bundle.getInt(MessageKeys.Key_Uncount_Total_Session, -1);
		String changedSessionId = null;
		if (MessageKeys.Action_UserMessge.equals(action)) {
			changedSessionId = bundle.getString(MessageKeys.Key_RemoteId);
		} else if (MessageKeys.Action_CommunityMessge.equals(action) ) {
			changedSessionId = bundle.getString(MessageKeys.Key_GroupId);
		} else if (MessageKeys.Action_ChatSessionChanged.equals(action)) {
			changedSessionId = bundle.getString(MessageKeys.Key_SessionId);
		} else if (MessageKeys.Action_MessgeStatus.equals(action)) {
			changedSessionId = ChatSession.getSessionIdFromMessageBundle(bundle);
			String statusType = bundle.getString(MessageKeys.Key_Type);
			changeMessageStauts(changedSessionId, statusType, bundle);
		} else {
			return super.onMessageReceive(bundle, action);
		}
		
		updateSession(changedSessionId);
		refreshUnreadMessageCount(totalUnreadedCount);
		
		return super.onMessageReceive(bundle, action);
	}
	
	private void updateSession(String sessionId) {
		if (StringUtil.isEmpty(sessionId)) {
			debugToast("sessionlistfragment, sessionid is empty");
			return;
		}
		
		int position = mAdapter.getPosition(new ChatSession(sessionId));
		
		// session has been deleted in database. remove if from ListView.
		ChatSession newSession = mSessionService.getSession(sessionId);
		if (newSession == null) { 
			if(position >= 0) {
				mAdapter.remove(position);
			}
			return;
		}

		ChatSession oldSession = null;
		if (position >= 0) {
			// this session exist in ListView, remove old session, insert new session.
			oldSession = mAdapter.delete(position);
		} else {
			boolean thisSessionInMore = mHasMoreSessions && mAdapter.getCount() > 0 
					&& mAdapter.getItem(mAdapter.getCount()-1).getSortIndex() > newSession.getSortIndex();
			if(thisSessionInMore) {
				// this session not exist in ListView. It exists in the additional list.
				return;
			}
			// this session not exist, default position at frist.
			position = 0;
		}

		initNewSession(newSession);
		
		// find real index of newsession
		boolean sortIndexChanged = oldSession == null || oldSession.getSortIndex() != newSession.getSortIndex();
		if (mAdapter.getCount() > 0  && sortIndexChanged) {
			int index = -1;
			while (++index < position) {
				if (mAdapter.getItem(index).getSortIndex() < newSession.getSortIndex()) {
					position = index;
					break;
				}
			}
		}

		mAdapter.insert(newSession, position);
		
		fetchUsersProrileUtil.requestAsync();
	}

	private void changeMessageStauts(String sessionid, String status, Bundle bundle) {
		if (MessageKeys.MsgStatus_Readed.equals(status)) {
			changeMessageReaded(sessionid, bundle.getStringArray(MessageKeys.Key_MessageId));
		} else if (MessageKeys.MsgStatus_Sending.equals(status)) {
			changeMessageStatus(sessionid, bundle.getString(MessageKeys.Key_MessageId), Message.STATUS_SENDING);
		} else if (MessageKeys.MsgStatus_Success.equals(status)) {
			changeMessageStatus(sessionid, bundle.getString(MessageKeys.Key_MessageId), Message.STATUS_SEND_SUCCESSFUL);
		} else if (MessageKeys.MsgStatus_Failed.equals(status)) {
			changeMessageStatus(sessionid, bundle.getString(MessageKeys.Key_MessageId), Message.STATUS_SEND_FAILED);
		}
	}
	
	private void changeMessageStatus(String sessionId, String msgId, int status) {
		int position = mAdapter.getPosition(new ChatSession(sessionId));
		if(position < 0) {
			return; // session not exist
		}
		
		ChatSession session = mAdapter.getItem(position);
		Message lastMessage = session.getLastMessage();
		if(lastMessage == null) {
			return;
		}
		
		if(lastMessage.getMsgId().equals(msgId)) {
			lastMessage.setStatus(status);
			mAdapter.notifyDataSetChanged();
			// this change is matching
		}
	}
	
	private void changeMessageReaded(String sessionId, String[] msgIds) {
		int position = mAdapter.getPosition(new ChatSession(sessionId));
		if(position < 0) {
			return; // session not exist
		}
		
		ChatSession session = mAdapter.getItem(position);
		Message lastMessage = session.getLastMessage();
		if(lastMessage == null) {
			return;
		}
		
		for (int i = 0; i < msgIds.length; i++) {
			String msgId = msgIds[i];
			if(lastMessage.getMsgId().equals(msgId)) {
				lastMessage.setStatus(Message.STATUS_SEND_READED);
				mAdapter.notifyDataSetChanged();
				break; // this change is matching
			}
		}
	}
	
	private void initNewSession(ChatSession session) {
		int sessionType = session.getSessionType();
		if((sessionType == ChatSession.TYPE_COMMUNITY)) {
			if(session.getRemoteCommunity() == null) {
				// TODO add to batch download
			}
		} else if((sessionType == ChatSession.TYPE_PEOPLE)) {
			if(session.getRemoteUser() == null) {
				User user = fetchUsersProrileUtil.addToBatchList(session.getSessionId());
				session.setRemoteUser(user);
			}
		}
	}

	/**
	 * 
	 * @param unreadCount If less than 0, count unread from database.
	 */
	private void refreshUnreadMessageCount(int unreadCount) {
		if(totalUnreadBubbleView == null) {
			log.w("totalUnreadBubbleView==null");
			return;
		}
		
		if (unreadCount < 0) {
			refreshUnreadMessageCountAsync();
			return;
		}
		
		if(unreadCount > 0) {
			totalUnreadBubbleView.setText(unreadCount+"");
			totalUnreadBubbleView.setVisibility(View.VISIBLE);
		} else {
			totalUnreadBubbleView.setVisibility(View.GONE);
		}
	}
	
	private void refreshUnreadMessageCountAsync() {
		postAsyncRunnable(new Runnable() {
			@Override
			public void run() {
				int count = MessageServiceWrapper.getInstance().getSessionUnreadedCount();
				if(count < 0) {
					count = 0;
				}
				
				final int unreadCount = count;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						refreshUnreadMessageCount(unreadCount);
					}
				});
			}
		});
	}
	
	private class BatchDownloadUserProfileCallback implements AsyncCallback<User> {
		@Override
		public void callback(User result) {
			if(isCreated()) {
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private class BatchDownloadCommunityProfileCallback implements AsyncCallback<Community> {
		@Override
		public void callback(Community result) {
			if(isCreated()) {
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	protected void initEvents() {
		// TODO Auto-generated method stub
		
	}
}