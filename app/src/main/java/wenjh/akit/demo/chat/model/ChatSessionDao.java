package wenjh.akit.demo.chat.model;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.common.db.BaseDao;

public class ChatSessionDao extends BaseDao<ChatSession, String> implements IChatSessionTable {

	public ChatSessionDao(SQLiteDatabase db) {
		super(db, TableName, F_SessionID);
	}

	@Override
	protected ChatSession assemble(Cursor cursor) {
		ChatSession chatSession = new ChatSession();
		assemble(chatSession, cursor);
		return chatSession;
	}

	@Override
	protected void assemble(ChatSession obj, Cursor cursor) {
		obj.setSessionType(getInt(cursor, F_ChatType));
		obj.setLastmsgId(getString(cursor, F_LastMessage));
		obj.setLastUpdateTime(getDate(cursor, F_LastTime));
		obj.setSessionId(getString(cursor, F_SessionID));
		obj.setSortIndex(getInt(cursor, F_SortIndex));
		obj.setUnreadMessageCount(getInt(cursor, F_UnreadedCount));
	}

	@Override
	public void insert(ChatSession t) {
		Map<String, Object> insertFiledsMap = new HashMap<String, Object>();
		insertFiledsMap.put(F_ChatType, t.getSessionType());
		insertFiledsMap.put(F_LastMessage, t.getLastmsgId());
		insertFiledsMap.put(F_LastTime, t.getLastUpdateTime());
		insertFiledsMap.put(F_SessionID, t.getSessionId());
		insertFiledsMap.put(F_SortIndex, t.getSortIndex());
		insertFiledsMap.put(F_UnreadedCount, t.getUnreadMessageCount());
		insertFileds(insertFiledsMap);
	}

	@Override
	public void update(ChatSession t) {
		Map<String, Object> insertFiledsMap = new HashMap<String, Object>();
		insertFiledsMap.put(F_ChatType, t.getSessionType());
		insertFiledsMap.put(F_LastMessage, t.getLastmsgId());
		insertFiledsMap.put(F_LastTime, t.getLastUpdateTime());
		insertFiledsMap.put(F_SortIndex, t.getSortIndex());
		insertFiledsMap.put(F_UnreadedCount, t.getUnreadMessageCount());
		updateFileds(insertFiledsMap, new String[]{F_SessionID}, new String[]{t.getSessionId()});
	}

	@Override
	public void deleteInstence(ChatSession obj) {
		delete(obj.getSessionId());
	}

}
