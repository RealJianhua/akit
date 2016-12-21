package wenjh.akit.config;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class HostConfigs {
	private static final String APIHOST = "HostConfigs.java.APIHOST.test";
	private static final int APIPORT = 80;
	static final String CHATIMAGEHOST = "HostConfigs.java.IMAGEHOST.test";
	private static final String IMAGEHOST = "wenjh.com";

	private static String getApiUrl() {
		return "http://" + APIHOST+":"+APIPORT +"/";
	}

	private static String getImageUrl() {
		return "http://" + IMAGEHOST + "/img/";
	}

	private static String getChatImageUrl() {
		return "http://" + CHATIMAGEHOST + "/";
	}
	
	public static String getApiUrlWithSub(String subUrl) {
		return getApiUrl() + subUrl;
	}
	
	// 哪些URL需要传SESSION_ID，按照业务情况修改 TODO
	public static boolean needSessionIdURL(String urlString) {
		try {
			URL url = new URL(urlString);
			return url.getHost().endsWith(".wenjh.com") || url.getHost().equals(APIHOST);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 
	 * @param guid
	 * @param size 1:small; 2:middle; 3:big
	 * @return
	 */
	public static String getImageUrlWithGUID(String guid, int size) {
		// TODO 根据图片GUID，拼接图片下载地址，根据业务情况使用
		return getImageUrl() + guid + ".jpg";
	}

	/**
	 *
	 * @param guid
	 * @param size 1:small; 2:middle; 3:big
	 * @return
	 */
	public static String getChatImageUrlWithGUID(String guid, int size) {
		// TODO 根据图片GUID，拼接图片下载地址，根据业务情况使用
		return getImageUrlWithGUID(guid, size);
	}
	
	/**
	 * default size
	 * @param guid
	 * @return
	 */
	public static String getImageUrlWithGUID(String guid) {
		return getImageUrlWithGUID(guid, 2);
	}
	
	
}
