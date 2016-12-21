package wenjh.akit.demo.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wenjh.akit.R;

import java.util.ArrayList;
import java.util.List;

import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.demo.chat.model.PeopleMessageService;
import wenjh.akit.common.asynctask.AsyncCallback;
import wenjh.akit.common.receiver.MessageKeys;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.demo.people.BatchFetchUserProfileUtil;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.model.UserService;
import wenjh.akit.demo.people.ui.UserProfileActivity;

public class PeopleChatFragment extends BaseChatFragment {
	private String mRemoteUserId;
	private User mRemoteUser = null;
	private List<String> mUnreadMessageIdList = new ArrayList<String>();
	private PeopleMessageService messageService = null;
	private UserService userService = null;
	
	@Override
	protected void onCreated(Bundle savedInstanceState) {
		mRemoteUserId = getIntent().getStringExtra(PeopleChatActivity.KEY_REMOTEUSERID);
		super.onCreated(savedInstanceState);
	}

	@Override
	protected void initDatasSync() {
		messageService = new PeopleMessageService();
		userService = new UserService();
		initRemoteUser();
		
		super.initDatasSync();
		
		registerMessageReceiver(500, MessageKeys.Action_UserMessge);
		messageService.updateReceiveUnreadedMessageIgnore(mRemoteUserId);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.peoplechat, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.peoplechat_menu_profile) {
			Intent intent = new Intent(getContext(), UserProfileActivity.class);
			intent.putExtra(UserProfileActivity.KEY_USERID, mRemoteUserId);
			startActivity(intent);
		} else if(item.getItemId() == R.id.peoplechat_menu_block) {
		}
		return true;
	}
	
	private void initRemoteUser() {
		mRemoteUser = userService.getUser(mRemoteUserId);
		if(mRemoteUser == null) {
			// download user profile
			BatchFetchUserProfileUtil fetchUserUtil = new BatchFetchUserProfileUtil();
			mRemoteUser = fetchUserUtil.addToBatchList(mRemoteUserId);
			fetchUserUtil.setCallback(new AsyncCallback<User>() {
				@Override
				public void callback(User result) {
					mAdapter.notifyDataSetChanged();
					refrehTitle();
				}
			});
			fetchUserUtil.requestAsync();
		} else {
			refrehTitle();
		}
	}
	
	private void refrehTitle() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(!StringUtil.isEmpty(mRemoteUser.getDisplayName())) {
					getActionBar().setTitle(mRemoteUser.getDisplayName());
				}
			}
		});
	}
	
	@Override
	protected boolean loadHistoryMessagesToListView(List<Message> list, int startIndex) {
		boolean hashMoreMessage = false;
		
		final List<Message> messages = messageService.findMessageByRemoteId(mRemoteUserId, startIndex, PAGECOUNT+1);
		if(messages.size() > PAGECOUNT) {
			messages.remove(0);
			hashMoreMessage = true;
		}
		
		for (Message message : messages) {
			message.setRemoteUser(mRemoteUser);
			if (message.getStatus() != Message.STATUS_RECEIVE_READED && message.isReceived() ) {
				message.setStatus(Message.STATUS_RECEIVE_READED);
				mUnreadMessageIdList.add(message.getMsgId());
			}
			list.add(message);
		}
		
		return hashMoreMessage;
	}
	
	@Override
	protected boolean onBeforeMessageSend(Message message) {
		message.setChatSessionType(Message.CHATTYPE_PEOPLE);
		message.setRemoteUserId(mRemoteUserId);
		message.setRemoteUser(mRemoteUser);
		return true;
	}
	
	@Override
	protected boolean onMessageReceive(Bundle bundle, String action) {
		if(MessageKeys.Action_UserMessge.equals(action)) {
			String remoteId = bundle.getString(MessageKeys.Key_RemoteId);
			// not current remote user
			if (!mRemoteUserId.equals(remoteId)) {
				return false;
			}
			List<Message> list = (List<Message>) bundle.getSerializable(MessageKeys.Key_MessageArray);
			return receiveMessage(list);
		}
		
		return super.onMessageReceive(bundle, action);
	}
	
	private boolean receiveMessage(List<Message> list) {
		for (Message message : list) {
			String msgId = message.getMsgId();
			
			// send readedmsg && chang message status
			if (message.getStatus() != Message.STATUS_RECEIVE_READED && message.isReceived()) {
				message.setStatus(Message.STATUS_RECEIVE_READED);
				mUnreadMessageIdList.add(msgId);
			}
			
			message.setRemoteUser(mRemoteUser);
		}
		
		appendReceiveMessageToBottom(list);
		mListview.scrollToBottom();
		
		if (isForeground()) {
			return true; // intercept this messages
		} else {
			return false;
		}
	}
	
	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();
	}
}
