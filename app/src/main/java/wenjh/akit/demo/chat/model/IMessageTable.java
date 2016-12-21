package wenjh.akit.demo.chat.model;

import wenjh.akit.demo.account.db.ITable;

public interface IMessageTable extends ITable {
	String PeopleTableName = "pep_messages";
	String F_MessageID = "msgid";
	String F_RemoteUserId = DBFIELD_TMP + "1";
	String F_CommunityID = DBFIELD_TMP + "2";
	String F_MessageTime = DBFIELD_TMP + "3";
	String F_TextContent = DBFIELD_TMP + "4";
	String F_Received = DBFIELD_TMP + "5";
	String F_Distance = DBFIELD_TMP + "6";
	String F_DistanceTime = DBFIELD_TMP + "7";
	String F_MapMessageLocation = DBFIELD_TMP + "8";
	String F_Status = DBFIELD_TMP + "9";
	String F_ContentType = DBFIELD_TMP + "10";
	String F_Image = DBFIELD_TMP + "11";
}
