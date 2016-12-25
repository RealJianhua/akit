package wenjh.akit.demo.chat.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.wenjh.akit.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import wenjh.akit.activity.base.MainScreenFragment;
import wenjh.akit.demo.chat.NotificationUtil;
import wenjh.akit.demo.chat.model.ChatImage;
import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.demo.chat.model.MessageServiceWrapper;
import wenjh.akit.common.asynctask.AsyncCallback;
import wenjh.akit.common.asynctask.HandyThreadPool;
import wenjh.akit.common.receiver.MessageKeys;
import wenjh.akit.common.util.DateUtil;
import wenjh.akit.common.util.TakePictureHelper;
import wenjh.akit.common.view.PullToRefreshListView;
import wenjh.akit.demo.config.ImageConfigs;
import wenjh.akit.demo.location.model.LatLng;
import wenjh.akit.demo.location.model.LocationHelper;

public abstract class BaseChatFragment extends MainScreenFragment {
	protected static final int PAGECOUNT = 20;
	private static final int TIMESTAMP_INTERVAL = 5 * 60 * 1000; // 5 mins
	protected BaseChatMessageAdapter mAdapter = null;
	protected PullToRefreshListView mListview = null;
	private EditText inputTextEditeText = null;
	private int mSessionType;
	private String mRemoteUserId;
	private String mRemoteCommunityId;
	private TakePictureHelper takePictureHelper = null;
	private int mShownMessageCount = 0;
	
	@Override
	protected void onCreated(Bundle savedInstanceState) {
		initViews();
		initEvents();
		readChatInfoFromIntent();
		initDatas();
	}

	void setSessionChatType(int chatType) {
		mSessionType = chatType;
	}
	
	@Override
	protected int getContentViewResourceIdAndCreateView() {
		return R.layout.fragment_basechat;
	}
	
	@Override
	protected void onBeforeCreated(Bundle savedInstanceState) {
		super.onBeforeCreated(savedInstanceState);
		// before #onrestoreinstance called, init pictureHelper
		takePictureHelper = new TakePictureHelper(this);
		takePictureHelper.setCompressSize(ImageConfigs.CHATIMAGE_MAX_WIDTH, ImageConfigs.CHATIMAGE_MAX_HEIGHT);
		takePictureHelper.setCallback(new AsyncCallback<File>() {
			@Override
			public void callback(File result) {
				if(result != null) {
					ChatImage image = new ChatImage(result);
					sendImageMessage(image);
				}
			}
		});
	}

	private void readChatInfoFromIntent() {
//		new IMJConnectionMonitor(this, getBaseActivity());
		/*if (mSessionType == Message.CHATTYPE_COMMUNITY) {
			mRemoteCommunityId = getIntent().getStringExtra(CommunityChatActivity.KEY_REMOTECOMMUNITYID);
		} else */
		if (mSessionType == Message.CHATTYPE_PEOPLE) {
			mRemoteUserId = getIntent().getStringExtra(PeopleChatActivity.KEY_REMOTEUSERID);
		} else {
			finish();
		}
	}

	@Override
	protected void initViews() {
		this.mListview = (PullToRefreshListView) findViewById(R.id.listview);
		this.inputTextEditeText = (EditText) findViewById(R.id.chat_et_inputmessage);
		this.mListview.setTimeViewVisibility(false);
		this.mListview.setCompleteScrollTop(false);
	}

	@Override
	protected void initEvents() {
		inputTextEditeText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				log.i("inputTextEditeText onEditorAction, actionid=" + actionId + ", event=" + event);
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					takeTextMessageAndSend();
					return true;
				} else {
					return false;
				}
			}
		});

		mListview.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction() && (event.getEventTime() - event.getDownTime()) < 100) {
					hideInputMethod();
				}
				return false;
			}
		});
		
		findViewById(R.id.chat_iv_plusbtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] items = new String[]{"Camera", "Photo Gallery", "Location"};
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							takeImageFromCameraAndSend();
							break;
						case 1:
							takeImageFromAlhumAndSend();
							break;
						case 2:
							sendLocationMessage();
							break;

						default:
							break;
						}
						
						dialog.dismiss();
					}
				});
				builder.setTitle(R.string.chat_plus_dialogtitle);
				builder.create().show();
			}
		});
		
		// pull to loadmore
		mListview.setOnPullToRefreshListener(new PullToRefreshListView.OnPullToRefreshListener() {
			@Override
			public void onPullToRefresh() {
				postAsyncRunnable(new Runnable() {
					@Override
					public void run() {
						loadMessagesFromDatabase(true);
					}
				});
			}
		});
	}

	@Override
	protected void initDatas() {
		mAdapter = new BaseChatMessageAdapter(getActivity(), new ArrayList<Message>());
		mListview.setAdapter(mAdapter);
		registerMessageReceiver(500, MessageKeys.Action_MessgeStatus);
		
		postAsyncRunnable(new Runnable() {
			@Override
			public void run() {
				removeNotificationIfHasUnreaded();
				initDatasSync();
			}
		});
	}
	
	protected void initDatasSync() {
		loadMessagesFromDatabase(false);
	}

	private Message getEarlyMessageOfListView() {
		int adapterCount = mAdapter.getCount();
		for (int i = 0; i < adapterCount; i++) {
			Message message = mAdapter.getItem(i);
			if(TimestampMessage.CONTENTTYPE_MESSAGETIME != message.getContentType()) {
				return message;
			}
		}
		return null;
	}
	
	private Message getLastMessageOfListView() {
		int adapterCount = mAdapter.getCount();
		for (int i = adapterCount-1; i >= 0; i--) {
			Message message = mAdapter.getItem(i);
			if(TimestampMessage.CONTENTTYPE_MESSAGETIME != message.getContentType()) {
				return message;
			}
		}
		return null;
	}
	
	private void loadMessagesFromDatabase(final boolean append) {
		final List<Message> loadedMessages = new ArrayList<Message>(PAGECOUNT);
		final boolean hashMoreMessage = loadHistoryMessagesToListView(loadedMessages, append ? mShownMessageCount : 0);
		mShownMessageCount += loadedMessages.size();
		insertTimestampeToList(loadedMessages, true);
		
		// reset 'top timstampe view (index 0)'
		int removeIndex = -1;
		if(append && loadedMessages.size() > 0 && mAdapter.getCount() > 1) {
			Message currentMessage = loadedMessages.get(loadedMessages.size()-1);
			Message previousMessage = mAdapter.getItem(1); // index 0, is timestamp view.
			long between = DateUtil.betweenTime(previousMessage.getTimestamp(), currentMessage.getTimestamp());
			if(between > 0 && between < TIMESTAMP_INTERVAL) {
				// remove 0, it mean is remove timestamp view (on index 0).
				removeIndex = 0;
			}
		}
		
		final int fRemoteIndex = removeIndex;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if(!hashMoreMessage) {
					mListview.setEnablePullToRefresh(false);
				}
				mAdapter.setNotifyOnChange(false);

				if(fRemoteIndex >= 0) {
					mAdapter.remove(0);
				}

				if(append) {
					mListview.refreshComplete();
					mAdapter.addAll(0, loadedMessages);
				} else {
					mAdapter.addAll(loadedMessages);
					mListview.setSelection(0);
				}
				
				mAdapter.setNotifyOnChange(true);
				mAdapter.notifyDataSetChanged();
				if(append) {
					mListview.setSelectionFromTop(loadedMessages.size()+2, mListview.getLoadingHeigth());
				}
			}
		});
	}
	
	protected abstract boolean loadHistoryMessagesToListView(List<Message> list, int startIndex);
	
	private void removeNotificationIfHasUnreaded() {
		/*if (mSessionType == Message.CHATTYPE_COMMUNITY) {
			boolean hasUnreadMessage = MessageServiceWrapper.getInstance().getCommunityMessageUnread() > 0;
			if(hasUnreadMessage) {
				NotificationUtil.removeCommunityMessageNotify();
			}
		} else */
		if (mSessionType == Message.CHATTYPE_PEOPLE) {
			boolean hasUnreadMessage = MessageServiceWrapper.getInstance().getPeopleMessageUnread() > 0;
			if(hasUnreadMessage) {
				NotificationUtil.removePeopleMessageNotify();
			}
		}
	}
	
	private void takeImageFromCameraAndSend() {
		takePictureHelper.takeNewPictureFromCamera();
		// on callback method send
	}
	
	private void takeImageFromAlhumAndSend() {
		takePictureHelper.takeNewPictureFromAlbum();
		// on callback method send
	}
	
	private void takeTextMessageAndSend() {
		Editable textContent = inputTextEditeText.getText();
		if (textContent.toString().trim().length() > 0) {
			sendTextMessage(textContent.toString().trim());
		}
		textContent.clear();
	}
	
	protected Message sendLocationMessage() {
		Message message = new Message();
		message.generateRandomMessageId();
		message.setContentType(Message.CONTENTTYPE_MESSAGE_MAP);
		message.setReceived(false);
		message.setStatus(Message.STATUS_LOCATING);
		realSend(message);
		return message;
	}
	
	protected Message sendImageMessage(ChatImage image) {
		Message message = new Message();
		message.generateRandomMessageId();
		message.setContentType(Message.CONTENTTYPE_MESSAGE_IMAGE);
		message.setReceived(false);
		message.setStatus(Message.STATUS_UPLOADING);
		message.setImageContent(image);
		realSend(message);
		return message;
	}
	
	protected Message sendTextMessage(String text) {
		Message message = new Message();
		message.generateRandomMessageId();
		message.setContentType(Message.CONTENTTYPE_MESSAGE_TEXT);
		message.setReceived(false);
		message.setStatus(Message.STATUS_SENDING);
		message.setTextContent(text);
		realSend(message);
		return message;
	}

	private void realSend(final Message message) {
		message.setTimestamp(new Date());
		onBeforeMessageSend(message);
		appendMessageToBottom(message);

		// real send, 进 dispather 队列
		HandyThreadPool.getGlobalThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				MessageServiceWrapper.getInstance().save(message);
//				if(message.getContentType() == Message.CONTENTTYPE_MESSAGE_TEXT) {
//					TextMessageTask task = new TextMessageTask(message);
//					SendTaskDispather.getInstance().put(task);
//				} else if(message.getContentType() == Message.CONTENTTYPE_MESSAGE_MAP) {
//					MapMessageTask mapMessageTask = new MapMessageTask(message);
//					SendTaskDispather.getInstance().put(mapMessageTask);
//				} else if(message.getContentType() == Message.CONTENTTYPE_MESSAGE_IMAGE) {
//					ImageMessageTask imageMessageTask = new ImageMessageTask(message);
//					SendTaskDispather.getInstance().put(imageMessageTask);
//				}
			}
		});

		onAfterMessageSent(message);
	}
	
	private void appendMessageToBottom(Message message) {
		List<Message> list = new ArrayList<Message>();
		list.add(message);
		appendReceiveMessageToBottom(list);
	}

	protected void appendReceiveMessageToBottom(List<Message> appendMessages) {
		mShownMessageCount += appendMessages.size();
		insertTimestampeToList(appendMessages, false);
		mAdapter.addAll(appendMessages);
	}
	
	/**
	 * 
	 * @param messages
	 * @param toTopMessages true is top, false is bottom
	 */
	private int insertTimestampeToList(List<Message> messages, boolean toTopMessages) {
		int addedTimeCount = 0;
		List<Message> tempMessageList = new ArrayList<Message>(messages);
		
		for (int i = 0; i < tempMessageList.size(); i++) {
			Message previousMessage = null;
			Message currentIndexMessage = tempMessageList.get(i);
			if(i == 0) {
				previousMessage = toTopMessages ? null : getLastMessageOfListView();
			} else {
				previousMessage = tempMessageList.get(i-1);
			}
			
			long between = DateUtil.betweenTime(
					previousMessage == null ? null : previousMessage.getTimestamp(), 
					currentIndexMessage.getTimestamp());
			
			if(between < 0 || between > TIMESTAMP_INTERVAL) {
				int targetIndex = i + addedTimeCount;
				messages.add(targetIndex, new TimestampMessage(currentIndexMessage.getTimestamp()));
				addedTimeCount++;
				log.i("insert time, position="+targetIndex);
			}
		}
		
		return addedTimeCount;
	}

	protected abstract boolean onBeforeMessageSend(Message message);

	protected void onAfterMessageSent(Message message) {
		mListview.scrollToBottom();
	}

	@Override
	protected boolean onMessageReceive(Bundle bundle, String action) {
		if (MessageKeys.Action_MessgeStatus.equals(action)) {
			if (!isReceivedCurrentChatMessage(bundle, action)) {
				return false;
			}

			String statusType = bundle.getString(MessageKeys.Key_Type);
			if (MessageKeys.MsgStatus_Success.equals(statusType)) {
				String messageId = bundle.getString(MessageKeys.Key_MessageId, "");
				onReceiveMessageSuccess(bundle, messageId);
			} else if (MessageKeys.MsgStatus_Readed.equals(statusType)) {
				String[] messageIds = bundle.getStringArray(MessageKeys.Key_MessageId);
				onReceiveMessageReaded(bundle, messageIds);
			} else if (MessageKeys.MsgStatus_Failed.equals(statusType)) {
				String messageId = bundle.getString(MessageKeys.Key_MessageId, "");
				onReceiveMessageFailed(bundle, messageId);
			} else if (MessageKeys.MsgStatus_Sending.equals(statusType)) {
				String messageId = bundle.getString(MessageKeys.Key_MessageId, "");
				onReceiveMessageSending(bundle, messageId);
			}
		}

		return super.onMessageReceive(bundle, action);
	}

	private boolean isReceivedCurrentChatMessage(Bundle bundle, String action) {
		int sessionType = bundle.getInt(MessageKeys.Key_ChatSessionType, -1);
		String remoteId = bundle.getString(MessageKeys.Key_RemoteId);
		String cummunityId = bundle.getString(MessageKeys.Key_GroupId);
		boolean isCurrentCumminityMessage = sessionType == Message.CHATTYPE_COMMUNITY  && mRemoteCommunityId != null && mRemoteCommunityId.equals(cummunityId);
		boolean isCurrentPeopleMessage = sessionType == Message.CHATTYPE_PEOPLE &&  mRemoteUserId != null &&  mRemoteUserId.equals(remoteId);
		return isCurrentCumminityMessage || isCurrentPeopleMessage;
	}

	protected boolean onReceiveMessageSuccess(Bundle bundle, String msgId) {
		for (int i = mAdapter.getCount() - 1; i >= 0; i--) {
			Message message = mAdapter.getItem(i);
			if (msgId.equals(message.getMsgId())) {
				message.setStatus(Message.STATUS_SEND_SUCCESSFUL);
				mAdapter.notifyDataSetChanged();
				break;
			}
		}
		return true;
	}

	protected boolean onReceiveMessageFailed(Bundle bundle, String msgId) {
		for (int i = mAdapter.getCount() - 1; i >= 0; i--) {
			Message message = mAdapter.getItem(i);
			if (msgId.equals(message.getMsgId())) {
				message.setStatus(Message.STATUS_SEND_FAILED);
				mAdapter.notifyDataSetChanged();
				break;
			}
		}
		return true;
	}
	
	protected boolean onReceiveMessageSending(Bundle bundle, String msgId) {
		for (int i = mAdapter.getCount() - 1; i >= 0; i--) {
			Message message = mAdapter.getItem(i);
			if (msgId.equals(message.getMsgId())) {
				message.setStatus(Message.STATUS_SENDING);

				// receive location
				if(message.getContentType() == Message.CONTENTTYPE_MESSAGE_MAP && message.getMapMessageLocation() == null) {
					double lat = bundle.getDouble(MessageKeys.Key_Latitude);
					double lng = bundle.getDouble(MessageKeys.Key_Longitude);
					float acc = bundle.getFloat(MessageKeys.Key_Accuracy);
					if(LocationHelper.isLocationAvailable(lat, lng)) {
						LatLng latLng = new LatLng(lat, lng, acc);
						message.setMapMessageLocation(latLng);
					}
				}
				
				mAdapter.notifyDataSetChanged();
				break;
			}
		}
		return true;
	}

	protected boolean onReceiveMessageReaded(Bundle bundle, String[] msgIds) {
		for (int i = 0; i < msgIds.length; i++) {
			for (int j = mAdapter.getCount() - 1; j >= 0; j--) {
				Message message = mAdapter.getItem(j);
				if (msgIds[i].equals(message.getMsgId())) {
					message.setStatus(Message.STATUS_SEND_READED);
					mAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		log.i("onResume");
		dispatchResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		log.i("onPause");
		dispatchPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		notifyChatSessionChanged();
	}
	
	private void notifyChatSessionChanged() {
		Bundle bundle = new Bundle();
		if (mSessionType == Message.CHATTYPE_COMMUNITY) {
			bundle.putString(MessageKeys.Key_SessionId, mRemoteCommunityId);
		} else if (mSessionType == Message.CHATTYPE_PEOPLE) {
			bundle.putString(MessageKeys.Key_SessionId, mRemoteUserId);
		}
		bundle.putInt(MessageKeys.Key_ChatSessionType, mSessionType);
		getApp().dispatchMessage(bundle, MessageKeys.Action_ChatSessionChanged);
	}
	
	@Override
	protected void onActivityResultReceived(int requestCode, int resultCode, Intent data) {
		takePictureHelper.onActivityResult(requestCode, resultCode, data);
		super.onActivityResultReceived(requestCode, resultCode, data);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		takePictureHelper.onSaveInstanceState(outState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		takePictureHelper.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		takePictureHelper.onDestory();
	}

	public static class TimestampMessage extends Message {
		// chatmessageadapter's timeview
		public static final int CONTENTTYPE_MESSAGETIME = -73;
		
		public TimestampMessage(Date date) {
			setTimestamp(date);
			setContentType(CONTENTTYPE_MESSAGETIME);
		}
	}
}

