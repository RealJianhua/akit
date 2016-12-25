package wenjh.akit.demo.chat.model;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.common.util.StringUtil;
import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.chat.NotificationUtil;
import wenjh.akit.demo.people.model.UserDao;
import wenjh.akit.common.util.LogUtil;

public class ChatSessionService {
	private PeopleMessageService peopleMessageService = null;
	private ChatSessionDao sessionDao = null;
	private SQLiteDatabase db = null;
	private UserDao userDao = null;
	private LogUtil log = new LogUtil("SessionService");
	
	public ChatSessionService() {
		db = ContextUtil.getApp().getSqliteInstance();
		peopleMessageService = new PeopleMessageService();
		sessionDao = new ChatSessionDao(db);
		userDao = new UserDao(db);
	}
	
	public List<ChatSession> findSessions(int startIndex, int offset) {
		List<ChatSession> list = sessionDao.list(new String[]{}, new String[]{}, IChatSessionTable.F_SortIndex, false, startIndex, offset);
		for (ChatSession session : list) {
			initSession(session);
		}
		return list;
	}
	
	public void saveOrUpdate(ChatSession session) {
		if(sessionDao.checkExist(session.getSessionId())) {
			sessionDao.update(session);
		} else {
			sessionDao.insert(session);
		}
	}
	
	private void initSession(ChatSession session) {
		if(session == null) return;
		switch (session.getSessionType()) {
		case ChatSession.TYPE_PEOPLE:
			session.setRemoteUser(userDao.get(session.getSessionId()));
			session.setUnreadMessageCount(peopleMessageService.getUnreadedCountByRemoteid(session.getSessionId()));
			if(!StringUtil.isEmpty(session.getLastmsgId())) {
				session.setLastMessage(peopleMessageService.getMessage(session.getLastmsgId()));
			}
			break;
		case ChatSession.TYPE_COMMUNITY:
			// TODO
//			session.setRemoteCommunity(communityDao.get(session.getSessionId()));
//			session.setUnreadMessageCount(communityMessageService.getUnreadedCountByCommunityId(session.getSessionId()));
//			if(!StringUtil.isEmpty(session.getLastmsgId())) {
//				session.setLastMessage(communityMessageService.findMessageById(session.getLastmsgId()));
//			}
			break;
		default:
			break;
		}
	}
	
	public ChatSession getSession(String sessionId) {
		ChatSession session = sessionDao.get(sessionId);
		initSession(session);
		return session;
	}
	
	public void delete(ChatSession session, boolean cleanMessages) {
		boolean hasSession = sessionDao.checkExist(session.getSessionId());
		if(hasSession) {
			sessionDao.delete(session.getSessionId());
		}
		
		if(session.getSessionType() == ChatSession.TYPE_PEOPLE) {
			if(session.getUnreadMessageCount() > 0) {
				NotificationUtil.removePeopleMessageNotify();
			}
			
			if(hasSession && cleanMessages) {
				peopleMessageService.deleteByRemoteId(session.getSessionId(), false);
			} else {
				peopleMessageService.updateReceiveUnreadedMessageIgnore(session.getSessionId());
			}
		} else if(session.getSessionType() == ChatSession.TYPE_COMMUNITY) {
			if(session.getUnreadMessageCount() > 0) {
				NotificationUtil.removeCommunityMessageNotify();
			}
			
//			if(cleanMessages) {
//				communityMessageService.deleteByCommunityId(session.getSessionId(), false);
//			} else {
//				communityMessageService.updateMessagsIgnore(session.getSessionId());
//			}
			
		}
	}
	
	public void delete(String sessionId, int sessionType, boolean cleanMessages) {
		ChatSession session = new ChatSession(sessionId);
		session.setSessionType(sessionType);
		delete(session, cleanMessages);
	}
	
	public void deleteSession(String sessionId) {
		sessionDao.delete(sessionId);
	}
	
	public void deleteSessions(List<ChatSession> sessions, boolean clearMessages) {
		db.beginTransaction();
		try {
			for (ChatSession session : sessions) {
				delete(session, clearMessages);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			log.e(e);
		} finally {
			db.endTransaction();
		}
	}
}


