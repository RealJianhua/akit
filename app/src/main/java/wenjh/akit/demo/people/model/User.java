package wenjh.akit.demo.people.model;

import android.net.Uri;

import java.util.List;

import wenjh.akit.common.util.AvatarAndName;
import wenjh.akit.common.util.Image;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.demo.account.DemoImage;

public class User implements AvatarAndName {
	private String id;
	private String name;
	private String avatar;
	private String cover = "";
	private String about = "";
	private DemoImage coverImage;
	private DemoImage avatarImage;
	private List<String> interests;
	private int age;
	private UserGender gender;
	private List<User> friends;

	public User() {
	}
	
	public User(String userId) {
		this.id = userId;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatar() {
		return avatar;
	}

	public Image getAvatarImage() {
		return avatarImage;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
		if(StringUtil.isEmpty(avatar)) {
			if(avatarImage != null) {
				avatarImage.setImageGuid(null);
			}
			avatarImage = null;
		} else {
			if(avatarImage == null) {
				avatarImage = new DemoImage();
			}
			avatarImage.setImageGuid(avatar);
		}
	}

	public String getCover() {
		return cover;
	}

	public Image getCoverImage() {
		return coverImage;
	}

	public void setCover(String cover) {
		this.cover = cover;
		if(StringUtil.isEmpty(cover)) {
			if(coverImage != null) {
				coverImage.setImageGuid(null);
			}
			coverImage = null;
		} else {
			if(coverImage == null) {
				coverImage = new DemoImage();
			}
			coverImage.setImageGuid(cover);
		}
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public List<String> getInterests() {
		return interests;
	}

	public void setInterests(List<String> interests) {
		this.interests = interests;
	}
	

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public UserGender getGender() {
		return gender;
	}

	public void setGender(UserGender gender) {
		this.gender = gender;
	}

	public List<User> getFriends() {
		return friends;
	}

	public void setFriends(List<User> friends) {
		this.friends = friends;
	}

	public String getDisplayName() {
		return name == null ? "" : name;
	}


	@Override
	public String toString() {
		return "User{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", avatar='" + avatar + '\'' +
				", cover='" + cover + '\'' +
				", about='" + about + '\'' +
				", age=" + age +
				", gender=" + gender +
				'}';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public boolean canChat() {
		return true;
	}
}
