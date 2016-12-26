package wenjh.akit.demo.account.model;

import wenjh.akit.common.db.ITable;

public interface IUserTable extends ITable {
	String TableName = "users";
	String F_UserId = "u_id";
	String F_Name = DBFIELD_TMP+"1";
	String F_Avatar = DBFIELD_TMP+"2";
	String F_Cover = DBFIELD_TMP+"3";
	String F_About = DBFIELD_TMP+"4";
	String F_Friends = DBFIELD_TMP+"5";
	String F_Age = DBFIELD_TMP+"6";
	String F_Interests = DBFIELD_TMP+"7";
	String F_Gender = DBFIELD_TMP+"8";
	String F_Blocked = DBFIELD_TMP+"9";
}
