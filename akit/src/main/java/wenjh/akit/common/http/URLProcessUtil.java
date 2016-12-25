package wenjh.akit.common.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLProcessUtil {

	public static String url(String baseUrl, String relativePath) {
		if (relativePath == null || relativePath.length() == 0) {
			return relativePath;
		}

		if (relativePath.contains("://") || relativePath.matches("(?s)^[a-zA-Z][a-zA-Z0-9+-.]*:.*$")) {
			return relativePath;
		}

		if (relativePath.charAt(0) == '/') {
			int index = baseUrl.indexOf("://");
			index = baseUrl.indexOf("/", index + 3);
			if (index == -1) {
				return baseUrl + relativePath;
			} else {
				return baseUrl.substring(0, index) + relativePath;
			}
		} else {
			int index = baseUrl.lastIndexOf('/');
			while (index > 0 && relativePath.startsWith("../")) {
				index = baseUrl.lastIndexOf('/', index - 1);
				relativePath = relativePath.substring(3);
			}
			return baseUrl.substring(0, index + 1) + relativePath;
		}
	}

	public static boolean hasParameter(String url, String name) {
		int index = url.lastIndexOf('/') + 1;
		if (index == -1 || index >= url.length()) {
			return false;
		}
		index = url.indexOf('?', index);
		while (index != -1) {
			int start = index + 1;
			if (start >= url.length()) {
				return false;
			}
			int eqIndex = url.indexOf('=', start);
			if (eqIndex == -1) {
				return false;
			}
			if (url.substring(start, eqIndex).equals(name)) {
				return true;
			}
			index = url.indexOf('&', start);
		}
		return false;
	}

	public static String appendParameter(String url, String name, String value) {
		if (name == null || value == null) {
			return url;
		}
		value = value.trim();
		if (value.length() == 0) {
			return url;
		}
		try {
			value = URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		int index = url.indexOf('?', url.lastIndexOf('/') + 1);
		char delimiter = (index == -1) ? '?' : '&';
		while (index != -1) {
			final int start = index + 1;
			final int eqIndex = url.indexOf('=', start);
			index = url.indexOf('&', start);
			if (eqIndex != -1 && url.substring(start, eqIndex).equals(name)) {
				final int end = (index != -1 ? index : url.length());
				if (url.substring(eqIndex + 1, end).equals(value)) {
					return url;
				} else {
					return new StringBuilder(url).replace(eqIndex + 1, end, value).toString();
				}
			}
		}
		return new StringBuilder(url).append(delimiter).append(name).append('=').append(value).toString();
	}

	public static String hostFromUrl(String url) {
		String host = url;
		int index = host.indexOf("://");
		if (index != -1) {
			host = host.substring(index + 3);
		}
		index = host.indexOf("/");
		if (index != -1) {
			host = host.substring(0, index);
		}
		return host;
	}

}