package wenjh.akit.demo.chat.model;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import wenjh.akit.common.util.LogUtil;
import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.MessageKeys;

public class MessageServiceWrapper {
	private LogUtil log = new LogUtil("MessageServiceHelper");
	private PeopleMessageService peopleMessageService = null;
//	private CommunityMessageService communityMessageService = null;
	private ChatSessionService sessionService = null;
	private SQLiteDatabase db = null;
	
	private static MessageServiceWrapper instance = null;
	
	private MessageServiceWrapper() {
		this.peopleMessageService = new PeopleMessageService();
//		this.communityMessageService = new CommunityMessageService();
		this.sessionService = new ChatSessionService();
		this.db = ContextUtil.getApp().getSqliteInstance();
	}
	
	public SQLiteDatabase getDb() {
		return db;
	}
	
	public PeopleMessageService getPeopleMessageService() {
		return peopleMessageService;
	}
	
	public ChatSessionService getSessionService() {
		return sessionService;
	}
	
	public synchronized static MessageServiceWrapper getInstance() {
		if(instance == null) {
			instance = new MessageServiceWrapper();
		}
		
		return instance;
	}
	
	public List<Message> findMessageByContentType(int chatSessionType, int contentType, String communityOrUserId) {
		if(chatSessionType == Message.CHATTYPE_PEOPLE) {
			return peopleMessageService.findMessageWithRemoteIdAndContentType(communityOrUserId, contentType);
		} else {
			throw new IllegalArgumentException("error message type");
		}
	}
	
	public void save(Message message) {
		if(message.getChatSessionType() == Message.CHATTYPE_PEOPLE) {
			peopleMessageService.saveMessage(message);
		} else {
			throw new IllegalArgumentException("error message type");
		}
	}
	
	public void updateStatus(String msgid, int status, int type) {
		if(type == Message.CHATTYPE_PEOPLE) {
			peopleMessageService.updateStatusByMessageId(msgid, status);
		}
	}
	
	public void updatePeopleMessageSentReaded(String remoteId) {
		peopleMessageService.updateSentMessageReaded(remoteId);
	}

	public boolean hasReply(String userid) {
		return peopleMessageService.hasReply(userid);
	}
	
	public boolean exist(String msgid, int type) {
		if(type == Message.CHATTYPE_PEOPLE) {
			return peopleMessageService.existMessage(msgid);
		}
		return false;
	}
	
	public Message getMessage(String msgid, int chatType) {
		if(chatType == Message.CHATTYPE_PEOPLE) {
			return peopleMessageService.findMessageById(msgid);
		}
		return null;
	}
	
	public int getPeopleMessageUnread() {
		return peopleMessageService.getUnreadedMessageCount();
	}
	
	public void cleanAllChatSessionsUnreadMessage() {
		MessageServiceWrapper.getInstance().getDb().beginTransaction();
		try {
			peopleMessageService.updateAllRecevieUnreadMessageIgnoe();
			MessageServiceWrapper.getInstance().getDb().setTransactionSuccessful();
		} finally {
			MessageServiceWrapper.getInstance().getDb().endTransaction();
		}
	}
	
	public int getSessionUnreadedCount() {
		if(db == null) {
			return 0;
		} else {
			return getPeopleMessageUnread()/* + getCommunityMessageUnread()*/;
		}
	}
	
	public synchronized static void release() {
		instance = null;
	}

	public void update(Message message) {
		if(message.getChatSessionType() == Message.CHATTYPE_COMMUNITY) {
//			communityMessageService.updateMessage(message);
		} else if(message.getChatSessionType() == Message.CHATTYPE_PEOPLE) {
			peopleMessageService.updateMessage(message);
		}
	}

	public Bundle putSessionUnreadExtra(Bundle bundle) {
		int chatMessageUnread = getPeopleMessageUnread();
		int groupMessageUnread = 0;//getCommunityMessageUnread(); //TODO
		int	allUnreaded = chatMessageUnread+groupMessageUnread;
//		bundle.putInt(MessageKeys.Key_UnreadCount_Community, groupMessageUnread);
		bundle.putInt(MessageKeys.Key_UnreadCount_User, chatMessageUnread);
		bundle.putInt(MessageKeys.Key_Uncount_Total_Session, allUnreaded);
		return bundle;
	}
}
