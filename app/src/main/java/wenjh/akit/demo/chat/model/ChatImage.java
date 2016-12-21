package wenjh.akit.demo.chat.model;

import java.io.File;

import android.net.Uri;

import wenjh.akit.common.util.StringUtil;
import wenjh.akit.config.HostConfigs;
import wenjh.akit.common.util.Image;

public class ChatImage extends Image {
	public long totalByteSize = 0;
	public long uplodedByteSize = 0;
	
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
		return !StringUtil.isEmpty(getImageGuid());
	}
	
	@Override
	public void setImageGuid(String imageGuid, int size) {
		super.setImageGuid(imageGuid, size);
		if(!StringUtil.isEmpty(imageGuid)) {
			String imageUrl = HostConfigs.getChatImageUrlWithGUID(imageGuid, size);
			setImageURL(imageUrl);
		}
	}
	
	@Override
	public Uri getImageUri() {
		return super.getImageUri();
	}
	
	public String getSmallImageUri() {
		if(!StringUtil.isEmpty(getImageGuid())) {
			return HostConfigs.getChatImageUrlWithGUID(getImageGuid(), 2);
		} else {
			Uri uri = getImageUri();
			return uri != null ? uri.toString() : null;
		}
	}
	
	public String getBigImageUri() {
		if(!StringUtil.isEmpty(getImageGuid())) {
			return HostConfigs.getChatImageUrlWithGUID(getImageGuid(), 3);
		} else {
			Uri uri = getImageUri();
			return uri != null ? uri.toString() : null;
		}
	}

}
