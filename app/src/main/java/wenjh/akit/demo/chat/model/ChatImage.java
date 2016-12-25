package wenjh.akit.demo.chat.model;

import java.io.File;

import android.net.Uri;

import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.util.Image;
import wenjh.akit.demo.account.DemoImage;
import wenjh.akit.demo.config.HostConfigs;

public class ChatImage extends Image {
	public long totalByteSize = 0;
	public long uplodedByteSize = 0;
	private String imageGuid;
	
	public ChatImage() {
	}

	public ChatImage(File file) {
		super(file);
		this.totalByteSize = file.length();
	}
	
	public ChatImage(String uriString) {
		super(uriString);
	}
	
	public boolean isUploadSuccess() {
		return !StringUtil.isEmpty(imageGuid);
	}
	
	void setImageGuid(String imageGuid, int size) {
		this.imageGuid = imageGuid;
		if(!StringUtil.isEmpty(imageGuid)) {
			String imageUrl = HostConfigs.getChatImageUrlWithGUID(imageGuid, size);
			setImageURL(imageUrl);
		}
	}

	public void setImageGuid(String imageGuid) {
		setImageGuid(imageGuid, 2);
	}

	
	@Override
	public Uri getImageUri() {
		return super.getImageUri();
	}
	
	public String getSmallImageUri() {
		if(!StringUtil.isEmpty(imageGuid)) {
			return HostConfigs.getChatImageUrlWithGUID(imageGuid, 2);
		} else {
			Uri uri = getImageUri();
			return uri != null ? uri.toString() : null;
		}
	}
	
	public String getBigImageUri() {
		if(!StringUtil.isEmpty(imageGuid)) {
			return HostConfigs.getChatImageUrlWithGUID(imageGuid, 3);
		} else {
			Uri uri = getImageUri();
			return uri != null ? uri.toString() : null;
		}
	}

	public String getImageGuid() {
		return imageGuid;
	}
}
