package wenjh.akit.activity.base;

public class TipsMessage {
	int priority;
	int id;
	String message;
	boolean clickable;
	public final static int MAX_PRIORITY = Integer.MAX_VALUE;

	public final static int ID_IMJSON_AUTH = 1007;
	public final static int ID_IMJSON_OTHER = 1008;
	public final static int ID_GROUP = 1001;
	public final static int ID_USERMSG = 1002;
	public final static int ID_HIMSG = 1003;
	public final static int ID_FEED = 1004;
	public final static int ID_NIGHT = 1005;
	public final static int ID_NEARBYPEOPLE = 1006;
	public final static int ID_NEWAREGROUP = 1009;
	public final static int ID_SHOW_FEED_GRADE_DETAIL = 1010;
	public final static int ID_MYINFO_UNBINDPHONE = 1011;
	public final static int ID_MYINFO_PROFILE_INCOMPLETE = 1012;
	public final static int ID_COMMERCE_COMMPLETE = 1013; // 商家资料不完整
	public final static int ID_MYINFO_WEAKPWD = 1014; // 弱密码

	public TipsMessage(int id, String message, int priority) {
		this.message = message;
		this.priority = priority;
		this.id = id;
	}

	public TipsMessage(int id, String message, int priority, boolean clickable) {
		this.message = message;
		this.priority = priority;
		this.id = id;
		this.clickable = clickable;
	}

	@Override
	public String toString() {
		return "TipsMessage [id=" + id + ", message=" + message + "]";
	}

	public TipsMessage(int id, String message) {
		this.message = message;
		this.id = id;
	}

	public TipsMessage(int id) {
		this.id = id;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		TipsMessage other = (TipsMessage) obj;
		if (id != other.id)
			return false;
		return true;
	}
}