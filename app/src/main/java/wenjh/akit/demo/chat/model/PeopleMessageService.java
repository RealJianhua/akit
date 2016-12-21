package wenjh.akit.demo.chat.model;

import java.util.Collections;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.util.ContextUtil;

public class PeopleMessageService  {
	private PeopleMessageDao msgDao = null;
	private ChatSessionDao sessionDao = null;
	private SQLiteDatabase db = null;
	
	public PeopleMessageService() {
		db = ContextUtil.getApp().getSqliteInstance();
		msgDao = new PeopleMessageDao(db);
		sessionDao = new ChatSessionDao(db);
	}

	public void saveMessage(Message msg) {
		if (StringUtil.isEmpty(msg.getMsgId())) {
			throw new NullPointerException("msg.msg.remoteId or msg.msgId is null");
		}
		msgDao.insert(msg);
		saveOrUpdateSession(msg);
	}

	public void saveMessages(List<Message> list, String remoteId) {
		db.beginTransaction();
		try {
			for (Message message : list) {
				if (existMessage(message.getMsgId())) {
					 msgDao.update(message);
				} else {
					msgDao.insert(message);
				}
			}

			ChatSession session = new ChatSession(remoteId);
			session.setSessionType(ChatSession.TYPE_PEOPLE);
			saveOrUpdateSession(getLastMessage(remoteId));
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private void saveOrUpdateSession(Message msg) {
		boolean upadte = true;
		ChatSession session = null;
		String sessionId = msg.getRemoteUserId();

		session = sessionDao.get(sessionId);
		if (session == null) {
			upadte = false;
			session = new ChatSession(sessionId);
		}

		session.setLastmsgId(msg.getMsgId());
		session.setSortIndex(ChatSessionOrderIdHandler.getInstance(db).nextId());
		session.setLastUpdateTime(msg.getTimestamp());
		session.setSessionType(ChatSession.TYPE_PEOPLE);

		if (upadte) {
			sessionDao.update(session);
		} else {
			sessionDao.insert(session);
		}
	}

	public boolean hasReply(String momoid) {
		return msgDao.count(new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_Received }, 
				new String[] { momoid, 0 + "" }) > 0;
	}

	public Message getLastMessage(String momoid) {
		return msgDao.max(IMessageTable.F_MessageTime, 
				new String[] { IMessageTable.F_RemoteUserId }, 
				new String[] { momoid });
	}

	public String getLastMessageId(String momoid) {
		return msgDao.maxField(IMessageTable.F_MessageID, IMessageTable.F_MessageTime, 
				new String[] { IMessageTable.F_RemoteUserId }, new String[] { momoid });
	}

	public void updateAllReceivedMessageReaded() {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { Message.STATUS_RECEIVE_READED },
				new String[] { IMessageTable.F_Received }, 
				new Object[] { 1 });
	}

	public void updateReceiveUnreadedMessageIgnore(String remoteId) {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { Message.STATUS_IGNORE }, 
				new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_Received, IMessageTable.F_Status }, 
				new Object[] { remoteId, 1, Message.STATUS_RECEIVE_UNREADED });
	}

	public void updateAllRecevieUnreadMessageIgnoe() {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { Message.STATUS_IGNORE }, 
				new String[] { IMessageTable.F_Received, IMessageTable.F_Status }, 
				new Object[] { 1, Message.STATUS_RECEIVE_UNREADED });
	}

	public void updateMessagesReaded(String[] msgIds) {
		msgDao.updateIn(IMessageTable.F_Status, Message.STATUS_RECEIVE_READED, IMessageTable.F_MessageID, msgIds);
	}

	public void updateReceiveUnreadedMessageReaded(String remoteId) {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { Message.STATUS_RECEIVE_READED }, 
				new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_Received }, 
				new Object[] { remoteId, 1 });
	}

	public void updateSentMessageReaded(String remoteId) {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { Message.STATUS_SEND_READED }, 
				new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_Received, IMessageTable.F_Status }, 
				new Object[] { remoteId, 0, Message.STATUS_SEND_SUCCESSFUL });
	}

	public void updateSentMessageReaded(String[] msgIds) {
		msgDao.updateIn(IMessageTable.F_Status, Message.STATUS_SEND_READED, IMessageTable.F_MessageID, msgIds);
	}

	public void updateSentMessageSuccessful(String msgId) {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { Message.STATUS_SEND_SUCCESSFUL }, 
				new String[] { IMessageTable.F_MessageID },
				new Object[] { msgId });
	}

	public void updateAllSendingMessagefailed() {
		db.beginTransaction();
		try {
			msgDao.updateFiled(new String[] { IMessageTable.F_Status }, new Object[] { Message.STATUS_SEND_FAILED },
					new String[] { IMessageTable.F_Status }, new Object[] { Message.STATUS_SENDING });
			msgDao.updateFiled(new String[] { IMessageTable.F_Status }, new Object[] { Message.STATUS_SEND_FAILED },
					new String[] { IMessageTable.F_Status }, new Object[] { Message.STATUS_LOCATING });
			msgDao.updateFiled(new String[] { IMessageTable.F_Status }, new Object[] { Message.STATUS_SEND_FAILED },
					new String[] { IMessageTable.F_Status }, new Object[] { Message.STATUS_UPLOADING });
			db.setTransactionSuccessful();
		} catch (Exception e) {
		} finally {
			db.endTransaction();
		}
	}

	public void updateSendingMessageFailed(String msgId) {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { Message.STATUS_SEND_FAILED }, 
				new String[] { IMessageTable.F_MessageID },
				new Object[] { msgId });
	}

	public Message findMessageById(String msgId) {
		return msgDao.get(IMessageTable.F_MessageID, msgId);
	}

	public int getMessageStatus(String msgId) {
		String status = msgDao.getFiled(IMessageTable.F_Status, 
				new String[] { IMessageTable.F_MessageID }, 
				new String[] { msgId });
		
		if (!StringUtil.isEmpty(status)) {
			try {
				return Integer.parseInt(status);
			} catch (Exception e) {
			}
		}
		return -1;
	}

	public void updateMessage(Message m) {
		if (StringUtil.isEmpty(m.getRemoteUserId()) || StringUtil.isEmpty(m.getMsgId())) {
			throw new NullPointerException("msg.msg.remoteId or msg.msgId is null");
		}
		msgDao.update(m);
	}

	public void updateStatusByMessageId(String msgid, int status) {
		msgDao.updateFiled(
				new String[] { IMessageTable.F_Status }, 
				new Object[] { status }, 
				new String[] { IMessageTable.F_MessageID },
				new Object[] { msgid });
	}

	public boolean existMessage(String msgid) {
		return msgDao.count(new String[] { IMessageTable.F_MessageID }, new String[] { msgid }) > 0;
	}

	public List<Message> findAllMessage() {
		return msgDao.getAll(IMessageTable.F_MessageTime, true);
	}

	public List<Message> findMessageByRemoteId(String momoId) {
		List<Message> list = msgDao.list(
				new String[] { IMessageTable.F_RemoteUserId }, 
				new String[] { momoId }, IMessageTable.F_MessageTime, true);
		return list;
	}

	public List<Message> findMessageByRemoteId(String momoId, int startIndex, int offset) {
		List<Message> list = msgDao.list(
				new String[] { IMessageTable.F_RemoteUserId + "" }, 
				new String[] { momoId }, IMessageTable.F_MessageTime, false,
				startIndex, offset);
		Collections.reverse(list);
		return list;
	}

	public List<Message> findMessageWithRemoteIdAndContentType(String momoId, int contentType) {
		List<Message> list = msgDao.list(
				new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_ContentType }, 
				new String[] { momoId, contentType + "" }, IMessageTable.F_MessageTime, true);
		return list;
	}

	public List<Message> findMessageByRemoteIdAsc(String momoId, int startIndex, int offset) {
		List<Message> list = msgDao.list(
				new String[] { IMessageTable.F_RemoteUserId + "" }, 
				new String[] { momoId }, IMessageTable.F_MessageTime, true,
				startIndex, offset);
		return list;
	}

	public Message getMessage(String msgId) {
		return msgDao.get(IMessageTable.F_MessageID, msgId);
	}

	public void delete(Message msg) {
		msgDao.deleteInstence(msg);

		if (sessionDao.checkExist(msg.getRemoteUserId())) {
			String msgid = msgDao.maxField(
					IMessageTable.F_MessageID, IMessageTable.F_MessageTime, 
					new String[] { IMessageTable.F_RemoteUserId },
					new String[] { msg.getRemoteUserId() });
			msgid = StringUtil.isEmpty(msgid) ? "-1" : msgid;
			sessionDao.updateFiled(IChatSessionTable.F_LastMessage, msgid, msg.getRemoteUserId());
		}
	}

	public void delete(List<Message> messages) {
		db.beginTransaction();
		try {
			for (Message message : messages) {
				msgDao.deleteInstence(message);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void deleteByRemoteId(String momoId, boolean deleteSession) {
		msgDao.delete(IMessageTable.F_RemoteUserId, momoId);
		if (deleteSession) {
			sessionDao.delete(momoId);
		} else {
			sessionDao.updateFiled(IChatSessionTable.F_LastMessage, "", momoId);
		}
	}

	public void deleteByRemoteIds(String[] momoids, boolean deleteSessions) {
		deleteByRemoteIds(momoids);
		if (deleteSessions) {
			sessionDao.delelteIn(IChatSessionTable.F_SessionID, momoids);
		}
	}

	public void deleteByRemoteIds(String[] momoids) {
		msgDao.delelteIn(IMessageTable.F_MessageID, momoids);
	}

	public boolean hasUnreadedByUserId(String momoid) {
		return getUnreadedCountByRemoteid(momoid) > 0;
	}

	public int getUnreadedMessageCount() {
		int c = msgDao.count(new String[] { IMessageTable.F_Status }, new String[] { Message.STATUS_RECEIVE_UNREADED + "" });
		return c;
	}

	public int getUnreadedCountByRemoteids(String[] momoids) {
		if (momoids == null || momoids.length <= 0) {
			return 0;
		}
		return msgDao.countIn(IMessageTable.F_RemoteUserId, momoids, 
				new String[] { IMessageTable.F_Status }, 
				new String[] { Message.STATUS_RECEIVE_UNREADED + "" });
	}

	public void updateMessagesStatus(String[] msgid, int status) {
		if (msgid != null && msgid.length > 0) {
			msgDao.updateIn(IMessageTable.F_Status, status, IMessageTable.F_MessageID, msgid);
		}
	}

	public void updateMessagesStatusByUserIds(String[] momoids, int newStatus, int oldStatus) {
		if (momoids != null && momoids.length > 0) {
			msgDao.updateIn(IMessageTable.F_Status, newStatus, oldStatus, IMessageTable.F_RemoteUserId, momoids);
		}
	}

	public void updateMessagesStatusByUserId(String momoid, int newStatus, int oldStatus) {
		msgDao.updateFiled(new String[] { IMessageTable.F_Status }, new String[] { newStatus + "" }, 
				new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_Status }, 
				new String[] { momoid, oldStatus + "" });
	}

	public void updateMessagesStatusByUserId(String[] momoids, int status) {
		if (momoids != null && momoids.length > 0) {
			msgDao.updateIn(IMessageTable.F_Status, status, IMessageTable.F_RemoteUserId, momoids);
		}
	}

	public int getUnreadedCountByRemoteid(String momoid) {
		int c = msgDao.count(new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_Status }, 
				new String[] { momoid, Message.STATUS_RECEIVE_UNREADED + "" });
		return c;
	}

	public int getReciveMessageCountByRemoteid(String momoid) {
		return msgDao.count(new String[] { IMessageTable.F_RemoteUserId, IMessageTable.F_Received }, 
				new String[] { momoid, 1 + "" });
	}

	public int getMessageCountRemoteid(String momoid) {
		return msgDao.count(new String[] { IMessageTable.F_RemoteUserId }, new String[] { momoid });
	}
	
	public void clear() {
		msgDao.deleteAll();
	}
}
