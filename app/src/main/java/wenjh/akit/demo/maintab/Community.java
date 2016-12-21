package wenjh.akit.demo.maintab;

import java.io.Serializable;

import wenjh.akit.common.util.AvatarAndName;

public class Community implements AvatarAndName, Serializable {
	public static final int RELATION_NONE = 0;
	public static final int RELATION_CREATOR = 1;
	public static final int RELATION_PENDING = 2;
	public static final int RELATION_JOINED = 3;
	public static final int RELATION_BLOCK = 4;

	private String name = "";
	private String cover = "";
	private String cid = "";
	private String createtime = "";
	private float distance;
	private String distanceString;
	private int communityRelation;
	private int member_count;
	private int max_member;

	public Community() {
	}

	public Community(String id) {
		this.cid = id;
	}

	public int getCommunityRelation() {
		return communityRelation;
	}

	public void setCommunityRelation(int communityRelation) {
		this.communityRelation = communityRelation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getId() {
		return cid;
	}

	public void setId(String cid) {
		this.cid = cid;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public float getDistance() {
		return distance;
	}

	public String getDistanceString() {
		return distanceString;
	}

	public void setDistance(float distance) {
		this.distance = distance;
		if (distance >= 0) {
			distanceString = ((int) distance) + "m";
		} else {
			distanceString = "";
		}
	}

	public int getMember_count() {
		return member_count;
	}

	public void setMember_count(int member_count) {
		this.member_count = member_count;
	}

	public int getMax_member() {
		return max_member;
	}

	public void setMax_member(int max_member) {
		this.max_member = max_member;
	}

	public String getDisplayName() {
		return name == null ? "" : name;
	}

	@Override
	public String getAvatar() {
		return getCover();
	}

	@Override
	public String toString() {
		return "Community [name=" + name + ", cid=" + cid + "]";
	}

	public boolean canChat() {
		return isJoined();
	}

	public boolean isPending() {
		return communityRelation == RELATION_PENDING;
	}

	public boolean canJoined() {
		return !isJoined() && communityRelation != RELATION_BLOCK;
	}

	public boolean isJoined() {
		return communityRelation == RELATION_JOINED || communityRelation == RELATION_CREATOR;
	}

	public boolean isCreator() {
		return communityRelation == RELATION_CREATOR;
	}
}
