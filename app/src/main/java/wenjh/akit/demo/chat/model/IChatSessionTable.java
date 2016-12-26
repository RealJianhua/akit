package wenjh.akit.demo.chat.model;

import wenjh.akit.common.db.ITable;

public interface IChatSessionTable extends ITable {
	String TableName = "chatsessions";
	String F_SessionID = "sessionid";
	String F_UnreadedCount = DBFIELD_TMP + "1";
	String F_ChatType = DBFIELD_TMP + "2";
	String F_LastTime = DBFIELD_TMP + "3";
	String F_LastMessage = DBFIELD_TMP + "4";
	String F_SortIndex = DBFIELD_TMP + "5";
}
