package wenjh.akit.common.util;

import android.net.Uri;

import java.io.File;

public class Image {
	private Uri imageUri = null;

	public Image(File file) {
		imageUri = Uri.fromFile(file);
	}
	
	public Image(String uriString) {
		imageUri = Uri.parse(uriString);
	}
	
	public Image() {
	}

	public void setImageUri(Uri imageUri) {
		this.imageUri = imageUri;
	}
	
	public void setImageFile(File file) {
		imageUri = Uri.fromFile(file);
	}
	
	public void setImageURL(String url) {
		imageUri = Uri.parse(url);
	}
	
	public void setImageFile(String filePath) {
		setImageFile(filePath);
	}
	
	public Uri getImageUri() {
		return imageUri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((imageUri == null) ? 0 : imageUri.hashCode());
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
		Image other = (Image) obj;
		if (imageUri == null) {
			if (other.imageUri != null)
				return false;
		} else if (!imageUri.equals(other.imageUri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Image [imageUri=" + imageUri + "]";
	}
}
