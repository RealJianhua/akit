package wenjh.akit.common.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.BitmapFactory;

import wenjh.akit.common.util.IOUtils;
import wenjh.akit.config.HostConfigs;
import wenjh.akit.common.util.LogUtil;

public class HttpClient {

	protected LogUtil log = new LogUtil(this);
	private static LogUtil LOG = new LogUtil("HttpClient");
	
	public static String getStaticMapUrl(String lat, String lng, int width, int height) {
		if(width <= 0 || height <= 0) {
			width = 300;
			height = 150;
		}
		StringBuffer urlBuilder = new StringBuffer();
		urlBuilder.append("http://maps.googleapis.com/maps/api/staticmap")
		.append("?").append("size=" + width + "x" + height)
		.append("&zoom=17").append("&scale=2")
		.append("&format=JPEG").append("&markers=color:red|size:|label:A|")
		.append(lat).append(",").append(lng);
		return urlBuilder.toString();
		
	}
	
	public String uploadRangeBytesData(RangeUploadHandler.ResumableUploadData uploadData, int source) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uploadData.uuid + "");
		params.put("offset", uploadData.offset + "" );
		params.put("length", uploadData.totalLength + "");
		params.put("index", uploadData.index + "");
		params.put("type", 0 + "");
		params.put("source", source + "");

		HttpFormFile uploadDataFormFile = new HttpFormFile("upload.jpg", uploadData.buffer, "fileblock", "application/octet-stream");

		String result = doPost(HostConfigs.getApiUrlWithSub("upload"), params, new HttpFormFile[]{uploadDataFormFile} );

		String serverGUIDName = null;
		if (uploadData.offset + uploadData.buffer.length >= uploadData.totalLength) {
			serverGUIDName = new JSONObject(result).getJSONObject("data").optString("file_id");
		}
		return serverGUIDName;
	}

	/**
	 * 发出Post请求。
	 * 
	 * @param urlString
	 *            请求地址
	 * @param formData
	 *            文本参数
	 * @return
	 * @throws Exception
	 */
	public String doPost(String urlString, Map<String, String> formData) throws Exception {
		return doPost(urlString, formData, null, null);
	}

	public String doPost(String urlString, Map<String, String> formData, HttpFormFile[] files, Map<String, String> headers) throws Exception {
		byte[] resultBytes = null;
		try {
			resultBytes = HttpRequester.post(urlString, formData, files, headers);
		} catch (InterruptedIOException e) {
			throw new HttpTimeoutException();
		} catch (NetworkBaseException e) {
			throw e;
		} catch (Exception e) {
			throw new NetworkBaseException(e);
		}

		String result = new String(resultBytes);
		log.i("[urlString] Result : " + result);

		// 解析有无异常
		parseError(result);

		return result;
	}

	/**
	 * 发出Post请求，允许同时上传文件
	 * 
	 * @param urlString
	 * @param formData
	 * @param files
	 * @return
	 * @throws Exception
	 */
	protected String doPost(String urlString, Map<String, String> formData, HttpFormFile[] files) throws Exception {
		return doPost(urlString, formData, files, null);
	}

	/**
	 * 发起GET请求
	 * 
	 * @param urlString
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public String doGet(String urlString, Map<String, String> params) throws Exception {
		return doGet(urlString, params, null);
	}

	/**
	 * 发起GET请求，并可以定义HTTP的Header信息
	 * 
	 * @param urlString
	 * @param params
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	protected String doGet(String urlString, Map<String, String> params, Map<String, String> headers) throws Exception {
		byte[] resultBytes = null;
		try {
			resultBytes = HttpRequester.get(urlString, params, headers);
		} catch (InterruptedIOException e) {
			throw new HttpTimeoutException();
		} catch (Exception e) {
			throw new NetworkBaseException(e);
		}

		String result = new String(resultBytes);
		log.i("[urlString] Result : " + result);
		// 解析有无异常
		parseError(result);

		return result;
	}

	protected void parseError(String result) throws HttpServerReturnedException, JSONException {
		JSONObject json = new JSONObject(result);
		int ec = json.optInt("ec");
		if (ec > 0) {
			String errmsg = json.optString("em");
			throw new HttpServerReturnedException(errmsg, ec, result);
		}
	}

	public static NetBitmap downloadBitmapWithGuidAndType(String guid, int type, DownloadProgressCallback callback) throws Exception {
		// return downloadBitmap(HostImage + append, callback);
		return null;
	}

	public static NetBitmap downloadBitmapWithUrl(String url, DownloadProgressCallback callback) throws Exception {
		return downloadBitmapWithUrl(url, callback, null);
	}

	public static NetBitmap downloadBitmapWithUrl(String url, DownloadProgressCallback callback, Map<String, String> params) throws Exception {
		HttpURLConnection httpConnection = null;
		long downloadSize = -1;
		
		try {
			if (callback != null)
				callback.callback(-1, -1, DownloadProgressCallback.STATUS_INIT, null);

			httpConnection = HttpRequester.requestGet(url, params, null);
			httpConnection.setReadTimeout(60 * 1000);
			int statusCode = httpConnection.getResponseCode();
			if (statusCode < 200 || statusCode >= 300) {
				throw new HttpResponseStatusErrorException(statusCode);
			}

			long totalSize = httpConnection.getContentLength();
			LOG.i("content-length=" + totalSize);
			
			if(callback != null) {
				callback.callback(totalSize, downloadSize, DownloadProgressCallback.STATUS_RUNNING, httpConnection);
			}

			InputStream is = new BufferedInputStream(httpConnection.getInputStream(), 2048);
			try {
				// read imagestream
				ByteArrayOutputStream outStream = new ByteArrayOutputStream(8192);
				byte[] buffer = new byte[4096];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
					downloadSize += len;
					
					if (callback != null)
						callback.callback(totalSize, downloadSize, DownloadProgressCallback.STATUS_RUNNING, httpConnection);
				}
				
				// decode bitmap
				byte[] arr = outStream.toByteArray();
				if (arr != null && arr.length > 0) {
					NetBitmap netBitmap = new NetBitmap();

					netBitmap.contentType = httpConnection.getContentType();
					netBitmap.bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);

					if (callback != null)
						callback.callback(totalSize, downloadSize, DownloadProgressCallback.STATUS_FINISH, httpConnection);
					return netBitmap;
				}
			} catch (Exception e) {
				if (callback != null)
					callback.callback(totalSize, downloadSize, DownloadProgressCallback.STATUS_ERROR, httpConnection);
				throw e;
			} finally {
				IOUtils.closeQuietly(is);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			HttpConnectionManager.getInstance().removeCurrentThreadActiveConnection();
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}

		return new NetBitmap();
	}

	public static void saveFile(String url, File file, DownloadProgressCallback callback) throws Exception {
		saveFile(url, file, null, callback);
	}

	public static void saveFile(String url, File file, Map<String, String> map, DownloadProgressCallback callback) throws Exception {
		LOG.i("save->" + url);
		HttpURLConnection postConnection = null;
		long downloadSize = 0;
		long contentLength = 0;
		try {
			if (callback != null) {
				callback.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_INIT, postConnection);
			}

			postConnection = HttpRequester.buildURLConnetion(url, map);
			postConnection.setReadTimeout(60 * 1000);
			int statusCode = postConnection.getResponseCode();
			if (statusCode < 200 || statusCode >= 300) {
				throw new HttpResponseStatusErrorException(statusCode);
			}

			InputStream is = new BufferedInputStream(postConnection.getInputStream(), 2048);
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			contentLength = postConnection.getContentLength();
			
			// callback.getControllerStatus() == DownloadProgressCallback.STATUS_STOP 外部停止, 下同
			if (callback != null && callback.getControllerStatus() == DownloadProgressCallback.STATUS_STOP) {
				// 中途被停止，停止
				callback.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_STOP, postConnection);
				return;
			} else if (callback != null) {
				callback.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_RUNNING, postConnection);
			}
			
			try {
				byte[] buffer = new byte[2048];
				int len = -1;
				while ((len = is.read(buffer)) != -1) {
					os.write(buffer, 0, len);
					downloadSize += len;
					if (callback != null) {
						callback.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_RUNNING, postConnection);
						// 中途被停止，停止读取
						if (callback.getControllerStatus() == DownloadProgressCallback.STATUS_STOP) {
							callback.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_STOP, postConnection);
							break;
						}
					}
				}
				os.flush();

				if (callback != null && callback.getControllerStatus() == DownloadProgressCallback.STATUS_RUNNING) {
					callback.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_FINISH, null);
				}

			} finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}
		} catch (Exception t) {
			if (file.exists()) {
				file.delete();
			}
			if (callback != null)
				callback.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_ERROR, null);
			throw t;
		} finally {
			HttpConnectionManager.getInstance().removeCurrentThreadActiveConnection();
			if (postConnection != null) {
				postConnection.disconnect();
			}
		}
	}

	/**
	 * 将php接口返回的时间戳转换为Java的时间对象
	 * 
	 * @param seconds
	 * @return
	 */
	public static Date toJavaDate(long seconds) {
		if (seconds > 0) {
			return new Date(seconds * 1000L);
		} else {
			return null;
		}
	}

	/**
	 * 将Java的时间对象转换为接口时间戳
	 * 
	 * @param date
	 * @return
	 */
	public static long toApiDate(Date date) {
		if (date != null) {
			try {
				return date.getTime() / 1000;
			} catch (Exception e) {
			}
		}
		return 0;
	}

	/**
	 * 将Json数组对象转换为字符串数组对象
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static String[] toJavaArray(JSONArray jsonArray) {
		if (jsonArray != null) {
			String[] array = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				array[i] = jsonArray.optString(i);
			}
			return array;
		}
		return null;
	}
	
	public static List<String> toJavaStringList(JSONArray jsonArray) {
		if (jsonArray != null) {
			List<String> list = new java.util.ArrayList<String>(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				list.add(jsonArray.optString(i));
			}
			return list;
		}
		return null;
	}

	public static Date parseDateFromId(String id) {
		if(id == null || id.length() < 8) {
			return null;
		}
		
		try {
			long timestamp = Long.parseLong(id.substring(0, 8), 16) * 1000;
			return new Date(timestamp);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

}
