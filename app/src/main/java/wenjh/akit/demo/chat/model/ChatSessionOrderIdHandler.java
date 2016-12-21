package wenjh.akit.demo.chat.model;

import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.common.util.StringUtil;

public class ChatSessionOrderIdHandler implements IChatSessionTable {
	private ChatSessionDao sessionDao = null;

	private ChatSessionOrderIdHandler(SQLiteDatabase db) {
		sessionDao = new ChatSessionDao(db);
	}

	public synchronized int nextId() {
		int id = 0;
		String result = sessionDao.maxField(F_SortIndex, F_SortIndex, new String[] {}, new String[] {});
		if (!StringUtil.isEmpty(result)) {
			try {
				id += Integer.parseInt(result);
			} catch (Exception e) {
			}
		}

		return id + 1;
	}

	private static ChatSessionOrderIdHandler instance = null;
	public synchronized static ChatSessionOrderIdHandler getInstance(SQLiteDatabase db) {
		if (instance == null) {
			instance = new ChatSessionOrderIdHandler(db);
		}
		return instance;
	}

	public synchronized static void release() {
		instance = null;
	}
}
