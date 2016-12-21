package wenjh.akit.demo.chat.greendaomodel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

import wenjh.akit.common.util.DateUtil;
import wenjh.akit.common.util.UniqueIDentity;
import wenjh.akit.demo.chat.model.ChatImage;
import wenjh.akit.demo.chat.model.ChatSession;
import wenjh.akit.demo.maintab.Community;
import wenjh.akit.demo.location.model.LatLng;
import wenjh.akit.demo.people.model.User;

@Entity
public class Message {
	public static final int STATUS_SENDING = 1;
	public static final int STATUS_SEND_SUCCESSFUL = 2;
	public static final int STATUS_SEND_FAILED = 3;
	public static final int STATUS_LOCATING = 4;
	public static final int STATUS_RECEIVE_READED = 5;
	public static final int STATUS_SEND_READED = 6;
	public static final int STATUS_RECEIVE_UNREADED = 7;
	public static final int STATUS_UPLOADING = 8;
	public static final int STATUS_IGNORE = 9;
	
	public static final int CONTENTTYPE_MESSAGE_TEXT = 1;
	public static final int CONTENTTYPE_MESSAGE_IMAGE = 2;
	public static final int CONTENTTYPE_MESSAGE_MAP = 3;
	public static final int CONTENTTYPE_MESSAGE_NOTICE = 4;
	
	public static final int CHATTYPE_PEOPLE = ChatSession.TYPE_PEOPLE;
	public static final int CHATTYPE_COMMUNITY = ChatSession.TYPE_COMMUNITY;

	private int chatType = CHATTYPE_PEOPLE;
	private int contentType = CONTENTTYPE_MESSAGE_TEXT;
	private int status = STATUS_SENDING;
	@Id
	private long id;
	@Index(unique = true)
	private String msgId;
	private String communityId = null;
	private String remoteUserId;
	private String textContent;
	private Date timestamp = null;
	private boolean received = false;
	private float distance = -1;
	private Date distanceTime = null;
	private LatLng mapMessageLocation = null;
	private ChatImage imageMessage = null;
	private String messageTimestampString = "";
	private User remoteUser = null;
	private Community ownerCommunity = null;

	/**
	 * community or people
	 * @return
	 */
	public int getChatSessionType() {
		return chatType;
	}

	/**
	 * community or people
	 * @param chatType
	 */
	public void setChatSessionType(int chatType) {
		this.chatType = chatType;
	}

	public int getContentType() {
		return contentType;
	}

	public void setContentType(int contentType) {
		this.contentType = contentType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getCommunityId() {
		return communityId;
	}

	public void setCommunityId(String communityId) {
		this.communityId = communityId;
	}

	public String getRemoteUserId() {
		return remoteUserId;
	}

	public void setRemoteUserId(String remoteUserId) {
		this.remoteUserId = remoteUserId;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		if(timestamp != null) {
			this.messageTimestampString = DateUtil.formateDateTime(timestamp);
		}
	}
	
	public Community getOwnerCommunity() {
		return ownerCommunity;
	}
	
	public void setRemoteCommunity(Community ownerCommunity) {
		this.ownerCommunity = ownerCommunity;
	}
	
	public User getRemoteUser() {
		return remoteUser;
	}
	
	public void setRemoteUser(User remoteUser) {
		this.remoteUser = remoteUser;
	}

	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public Date getDistanceTime() {
		return distanceTime;
	}

	public void setDistanceTime(Date distanceTime) {
		this.distanceTime = distanceTime;
	}

	public LatLng getMapMessageLocation() {
		return mapMessageLocation;
	}

	public void setMapMessageLocation(LatLng mapMessageLocation) {
		this.mapMessageLocation = mapMessageLocation;
	}
	
	public String getMessageTimestampString() {
		return messageTimestampString;
	}

	public void setImageContent(ChatImage imageMessage) {
		this.imageMessage = imageMessage;
	}
	
	public ChatImage getImageContent() {
		return imageMessage;
	}
	
	public void generateRandomMessageId() {
		this.msgId = UniqueIDentity.nextId();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msgId == null) ? 0 : msgId.hashCode());
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
		Message other = (Message) obj;
		if (msgId == null) {
			if (other.msgId != null)
				return false;
		} else if (!msgId.equals(other.msgId))
			return false;
		return true;
	}

	public void setDistanceTime(long optLong) {
		if(optLong > 0) {
			this.distanceTime = new Date(optLong);
		}
	}
	
	public String getNotificationContent() {
		String content = "";
		if (contentType == CONTENTTYPE_MESSAGE_TEXT || contentType == CONTENTTYPE_MESSAGE_NOTICE) {
			content = textContent;
		} else if (contentType == CONTENTTYPE_MESSAGE_IMAGE) {
			content = "[image]";
		} else if (contentType == Message.CONTENTTYPE_MESSAGE_MAP) {
			content = "[location]";
		}
		return content;
	}

	@Override
	public String toString() {
		return "Message [status=" + status + ", msgId=" + msgId + ", textContent=" + textContent + "]";
	}
	
	
}
