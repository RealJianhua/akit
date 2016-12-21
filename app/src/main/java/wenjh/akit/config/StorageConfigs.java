package wenjh.akit.config;

import java.io.File;

import android.net.Uri;
import android.os.Environment;

import wenjh.akit.common.util.Image;
import wenjh.akit.common.util.MD5Util;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.util.ContextUtil;

public abstract class StorageConfigs {

	public static File getImageCacheDir() {
		File dir = new File(ContextUtil.getContext().getExternalCacheDir(), "image");
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public static File getImageCacheFile(String filename) {
		File dir = new File(getImageCacheDir(), filename.substring(0, 1));
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir, filename);
	}
	
	public static File getImageCacheFileWithURL(String url) {
		String cacheUrlKey = Uri.parse(url).getPath();
		if(StringUtil.isEmpty(cacheUrlKey)) {
			cacheUrlKey = url;
		}
		String localCacheKey = MD5Util.getMd5HexString(cacheUrlKey);
		File cacheFile = getImageCacheFile(localCacheKey);
		return cacheFile;
	}
	
	/**
	 * 
	 * @param guid
	 * @param size size 1:small; 2:middle; 3:big
	 * @return
	 */
	public static File getImageFileWithGUID(String guid, int size) {
		return getImageCacheFileWithURL(HostConfigs.getImageUrlWithGUID(guid, size));
	}
	
	public static boolean isImageCachedWithURL(String url) {
		File cacheFile = getImageCacheFileWithURL(url);
		boolean cacheAvailable = cacheFile != null && cacheFile.exists() && cacheFile.length() > 0;
		return cacheAvailable;
	}
	
	public static boolean isImageCached(Image image) {
		Uri uri = image.getImageUri();
		if("file".equals(uri.getScheme())) {
			File cacheFile = new File(uri.getPath());
			boolean cacheAvailable = cacheFile != null && cacheFile.exists() && cacheFile.length() > 0;
			return cacheAvailable;
		} else {
			return isImageCachedWithURL(uri.toString());
		}
	}
	
	public final static File getInnerUserHome() {
		return getInnerUserHome(ContextUtil.getCurrentUser().getId());
	}

	public final static File getInnerUserHome(String uid) {
		File file = new File(ContextUtil.getContext().getFilesDir(), uid);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	public static File getUserImageSaveDir() {
		File dir = new File(Environment.getExternalStorageDirectory(), "DCIM/akit");
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}
