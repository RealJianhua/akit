package wenjh.akit.demo.chat.model;

import java.util.Date;

import android.os.Bundle;

import wenjh.akit.common.util.AvatarAndName;
import wenjh.akit.common.util.DateUtil;
import wenjh.akit.common.util.Image;
import wenjh.akit.demo.MessageKeys;
import wenjh.akit.demo.maintab.Community;
import wenjh.akit.demo.people.model.User;

public class ChatSession  implements AvatarAndName {
	public final static int TYPE_PEOPLE = 1;
	public final static int TYPE_COMMUNITY = 2;
	
	private int unreadMessageCount;
	private int sortIndex;
	private int sessionType = TYPE_PEOPLE;
	private Date lastUpdateTime;
	private String lastUpdateTimeString = "";
	private Message lastMessage;
	private String lastMessgId;
	private String sessionId = "";
	private User remoteUser;
	private Community remoteCommunity;

	public ChatSession() {
	}
	
	public ChatSession(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public int getUnreadMessageCount() {
		return unreadMessageCount;
	}

	public void setUnreadMessageCount(int unreadMessageCount) {
		this.unreadMessageCount = unreadMessageCount;
	}
	
	public User getRemoteUser() {
		return remoteUser;
	}
	
	public void setRemoteUser(User remoteUser) {
		this.remoteUser = remoteUser;
	}
	
	public Community getRemoteCommunity() {
		return remoteCommunity;
	}
	
	public void setRemoteCommunity(Community remoteCommunity) {
		this.remoteCommunity = remoteCommunity;
	}

	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int orderid) {
		this.sortIndex = orderid;
	}

	public int getSessionType() {
		return sessionType;
	}

	public void setSessionType(int type) {
		this.sessionType = type;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
		this.lastUpdateTimeString = DateUtil.betweenWithCurrentTime(lastUpdateTime);
	}
	
	public String getLastUpdateTimeString() {
		return lastUpdateTimeString;
	}

	public String getLastmsgId() {
		return lastMessgId;
	}

	public void setLastmsgId(String lastmsgId) {
		this.lastMessgId = lastmsgId;
	}

	public Message getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(Message lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	public String getDisplayName() {
		if(sessionType == TYPE_COMMUNITY) {
			return remoteCommunity == null ? "" : remoteCommunity.getDisplayName();
		} else if(sessionType == TYPE_PEOPLE) {
			return remoteUser == null ? "" : remoteUser.getDisplayName();
		}
		return "";
	}
	
	public String getAvatar() {
		if(sessionType == TYPE_COMMUNITY) {
			return remoteCommunity == null ? "" : remoteCommunity.getCover();
		} else if(sessionType == TYPE_PEOPLE) {
			return remoteUser == null ? "" : remoteUser.getAvatar();
		}
		return "";
	}

	public Image getAvatarImage() {
		if(sessionType == TYPE_COMMUNITY) {
			return remoteCommunity == null ? null : remoteCommunity.getCoverImage();
		} else if(sessionType == TYPE_PEOPLE) {
			return remoteUser == null ? null : remoteUser.getAvatarImage();
		}
		return null;
	}
	
	public String getTextContent() {
		if(lastMessage != null) {
			return lastMessage.getNotificationContent();
		} else {
			return "";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatSession other = (ChatSession) obj;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		return true;
	}
	
	public static String getSessionIdFromMessageBundle(Bundle bundle) {
		String sessionid = null;
		int chatType = bundle.getInt(MessageKeys.Key_ChatSessionType);
		if (chatType == TYPE_COMMUNITY) {
			sessionid = bundle.getString(MessageKeys.Key_GroupId);
		} else if (chatType == TYPE_PEOPLE) {
			sessionid = bundle.getString(MessageKeys.Key_RemoteId);
		}
		return sessionid;
	}
	
	public int getLastMessageStatus() {
		if(lastMessage != null) {
			return lastMessage.getStatus();
		} else {
			return -1;
		}
	}

	@Override
	public String getId() {
		return sessionId;
	}

	@Override
	public String toString() {
		return "ChatSession [lastMessage=" + lastMessage + ", lastMessgId=" + lastMessgId + ", sessionId=" + sessionId + "]";
	}
	
	
}
