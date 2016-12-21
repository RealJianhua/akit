package wenjh.akit.common.receiver;

public interface MessageKeys {
	String Action_Account_Logout = "actions.logout";
	String Action_CommunityNotification = "actions.groupaction";
	String Action_UserMessge = "actions.usermessage";
	String Action_CommunityMessge = "actions.communitymessage";
	String Action_MessgeStatus = "actions.message.status";
	String Action_ChatSessionChanged = "actions.chatsession.update";
	String Action_MyProfileUpdate = "actions.myprofile.update";

	String Key_Latitude = "lat";
	String Key_Longitude = "lng";
	String Key_Accuracy = "acc";
	String Key_MessageArray = "messagearray";
	String Key_MessageObject = "messageobj";
	String Key_MessageId = "msgid";
	String Key_RemoteId = "remoteuserid";
	String Key_GroupId = "groupid";
	String Key_Type = "stype";
	String Key_ChatSessionType = "chattype";
	String Key_SessionId = "sessionid";
	String Key_RemoteType = "remotetype";
	
	String Key_UnreadCount_User = "userunreaded";
	String Key_Uncount_Total_Session = "totalunreaded";
	

	String MsgStatus_Success = "msgsuccess";
	String MsgStatus_Failed = "msgfailed";
	String MsgStatus_Sending = "msgsending";
	String MsgStatus_Readed = "msgreaded";

	String[] Actions_AllNotificationMessage = new String[]{
			Action_CommunityMessge, 
			Action_UserMessge,
			Action_CommunityNotification
	};
	
	String[] Actions_AllSessions = new String[]{
			Action_CommunityMessge, 
			Action_UserMessge,
			Action_MessgeStatus
	};
}
