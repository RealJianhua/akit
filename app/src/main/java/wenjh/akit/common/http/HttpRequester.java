package wenjh.akit.common.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import org.apache.http.conn.ssl.AbstractVerifier;

import wenjh.akit.common.util.IOUtils;
import wenjh.akit.config.HostConfigs;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;

public class HttpRequester {
	public static final String HEADEP_CHECK_HISTORY = "checkhistory";
	private static LogUtil log = new LogUtil("HttpRequester");
	private static final int TIMEOUT_CONNECTION = 5 * 1000; // 连接超时时间
	private static final int TIMEOUT_READ = 15 * 1000; // 读取超时时间
	private static final int REPEAT_TIME = 20 * 1000; // 失敗自動充實時間
	private static final int REPEAT_COUNT = 4; // 失敗自動重试次数

	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new HttpsVerifer());
	}

	public static byte[] post(String url, Map<String, String> params, HttpFormFile[] files, Map<String, String> headers) throws Exception {
		int repeatCount = 0;
		long startTime = System.currentTimeMillis();
		while (repeatCount++ <= REPEAT_COUNT) {
			HttpURLConnection connection = null;
			try {
				connection = postConnection(url, params, files, headers);
				int responseCode = connection.getResponseCode();
				if (responseCode >= 200 && responseCode < 300) {
					return readStreamAndClose(connection.getInputStream());
				} else {
					throw new HttpResponseStatusErrorException(responseCode);
				}
			} catch (NetworkUnavailableException e) {
				throw e;
			} catch (Exception e) {
				boolean needRepeat = repeatCount < REPEAT_COUNT && System.currentTimeMillis() - startTime < REPEAT_TIME;
				if (!needRepeat) {
					throw e;
				}

				if (e instanceof HttpResponseStatusErrorException) {
//					HttpResponseStatusErrorException statusErrorException = (HttpResponseStatusErrorException) e;
//					if (statusErrorException.statusCode > 0 && statusErrorException.statusCode <= 500) {
//						throw e;
//					} else {
//						// 1-499 need repeat
//					}
					throw e;
				}
				log.e(e);

				log.w("retry----" + repeatCount + " -> " + url);
				Thread.sleep(1000);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
				HttpConnectionManager.getInstance().removeCurrentThreadActiveConnection();
			}
		}

		throw new NetworkBaseException();
	}

	public static byte[] get(String url, Map<String, String> params, Map<String, String> header) throws Exception {
		int repeatCount = 0;
		long startTime = System.currentTimeMillis();
		while (repeatCount++ <= REPEAT_COUNT) {
			HttpURLConnection connection = null;
			try {
				connection = requestGet(url, params, header);

				int statusCode = connection.getResponseCode();
				if (statusCode >= 200 && statusCode < 300) {
					InputStream inStream = new BufferedInputStream(connection.getInputStream());
					byte[] result = readStreamAndClose(inStream);
					return result;
				} else {
					throw new HttpResponseStatusErrorException(statusCode);
				}
			} catch (NetworkUnavailableException e) {
				throw e;
			} catch (Exception e) {
				boolean needRepeat = repeatCount < REPEAT_COUNT && System.currentTimeMillis() - startTime < REPEAT_TIME;
				if (!needRepeat) {
					throw e;
				}

				if (e instanceof HttpResponseStatusErrorException) {
					HttpResponseStatusErrorException statusErrorException = (HttpResponseStatusErrorException) e;
					if (statusErrorException.statusCode > 0 && statusErrorException.statusCode < 500) {
						throw e;
					} else {
						// 1-499 need repeat
					}
				}
				log.e(e);

				log.w("retry----" + repeatCount + " -> " + url);
				Thread.sleep(1000);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
				HttpConnectionManager.getInstance().removeCurrentThreadActiveConnection();
			}
		}

		throw new NetworkBaseException();
	}

	/**
	 * 获得Get请求的连接对象
	 * 
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpURLConnection requestGet(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
		// append parameter
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				url = URLProcessUtil.appendParameter(url, entry.getKey(), entry.getValue());
			}
		}

		HttpURLConnection conn = buildURLConnetion(url, headers);
		conn.setConnectTimeout(TIMEOUT_CONNECTION);
		conn.setReadTimeout(TIMEOUT_READ);
		conn.setRequestMethod("GET");

		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}

		return conn;
	}

	/**
	 * 获得http请求对象，可能走加密通道
	 * 
	 * @param urlAddress
	 * @return
	 * @throws IOException
	 */
	public static HttpURLConnection buildURLConnetion(String urlAddress, Map<String, String> header) throws IOException {
		log.i("--> " + urlAddress);
		URL url = new URL(urlAddress);
		HttpURLConnection connection = null;
		if (url.getProtocol().equals("https")) {
			HttpsURLConnection httpcon = (HttpsURLConnection) url.openConnection();
			connection = httpcon;
		} else {
			connection = (HttpURLConnection) url.openConnection();
		}
		
		HttpConnectionManager.getInstance().putCurrentThreadActiveConnection(connection);
		return connection;
	}

	private static class HttpsVerifer extends AbstractVerifier {
		@Override
		public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			try {
				verify(host, cns, subjectAlts, true);
			} catch (SSLException e) {
				log.i("host=" + host + ", cns=" + Arrays.toString(cns) + ", subjectAlts=" + Arrays.toString(subjectAlts));
				throw e;
			}
		}
	}

	private static byte[] readStreamAndClose(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream(4096);
		try {
			byte[] buffer = new byte[4096];
			int len = -1;
			while ((len = inStream.read(buffer)) != -1) {
				outSteam.write(buffer, 0, len);
			}
		} finally {
			IOUtils.closeQuietly(outSteam);
			IOUtils.closeQuietly(inStream);
		}
		return outSteam.toByteArray();
	}

	public static HttpURLConnection postConnection(String urlString, Map<String, String> params, HttpFormFile[] files, Map<String, String> headers)
			throws Exception {
		if (!ContextUtil.isNetworkAvailable()) {
			throw new NetworkUnavailableException();
		}
		
		if(HostConfigs.needSessionIdURL(urlString)) {
			if(params == null) {
				params = new HashMap<String, String>();
			}
			if(!params.containsKey("session")) {
				params.put("session", ContextUtil.getCurrentAccountSession());
			}
		}

		log.i("dopost, "+urlString+", formData="+params);
		
		final String BOUNDARY = "---------------------------7da2137580612"; // 数据分隔线
		final String ENDLINE = "--" + BOUNDARY + "--\r\n";// 数据结束标志
		final String CHARSET = "UTF-8";

		// 计算文件类型数据的总长度
		long fileDataLength = 0;
		if (files != null) {
			for (HttpFormFile uploadFile : files) {
				StringBuilder fileExplain = new StringBuilder();
				fileExplain.append("--");
				fileExplain.append(BOUNDARY);
				fileExplain.append("\r\n");
				fileExplain.append("Content-Disposition: form-data;name=\"" + uploadFile.getParameterName() + "\";filename=\""
						+ uploadFile.getFilname() + "\"\r\n");
				fileExplain.append("Content-Type: " + uploadFile.getContentType() + "\r\n\r\n");
				fileExplain.append("\r\n");
				fileDataLength += fileExplain.length();
				File file = uploadFile.getFile();
				if (file != null) {
					fileDataLength += file.length();
				} else if(uploadFile.getInputData() != null) {
					fileDataLength += uploadFile.getInputData().length;
				} else {
					throw new NetworkBaseException("upload data error.");
				}
			}
		}

		// 构造文本类型参数的实体数据
		StringBuilder textEntity = new StringBuilder();
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				textEntity.append("--");
				textEntity.append(BOUNDARY);
				textEntity.append("\r\n");
				textEntity.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
				textEntity.append(entry.getValue());
				textEntity.append("\r\n");
			}
		}

		// 计算传输给服务器的实体数据总长度
		long dataLength = textEntity.toString().getBytes().length + fileDataLength;
		if (dataLength > 0) {
			dataLength += ENDLINE.getBytes().length;
		}

		// 设置请求头信息
		HttpURLConnection conn = buildURLConnetion(urlString, headers);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		if (files != null && files.length > 0) {
			// 附带文件上传，超时时间加倍
			conn.setConnectTimeout(TIMEOUT_CONNECTION * 2);
			conn.setReadTimeout(TIMEOUT_READ * 2);
		} else {
			conn.setConnectTimeout(TIMEOUT_CONNECTION);
			conn.setReadTimeout(TIMEOUT_READ);
		}
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Charset", CHARSET);
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		conn.setRequestProperty("Content-Length", dataLength + "");
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Expect", "100-continue");
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}

		boolean hasParams = (params != null && !params.isEmpty()) || (files != null && files.length > 0);
		if (hasParams) {
			OutputStream outStream = null;
			try {
				outStream = new BufferedOutputStream(conn.getOutputStream());
				// 把所有文本类型的实体数据发送出来
				if (params != null && !params.isEmpty()) {
					byte[] bytes = textEntity.toString().getBytes();
					outStream.write(bytes);
					outStream.flush();
				}

				// 上传文件
				if (files != null && files.length > 0) {
					for (HttpFormFile uploadFile : files) {
						StringBuilder fileEntity = new StringBuilder();
						fileEntity.append("--");
						fileEntity.append(BOUNDARY);
						fileEntity.append("\r\n");
						fileEntity.append("Content-Disposition: form-data;name=\"" + uploadFile.getParameterName() + "\";filename=\""
								+ uploadFile.getFilname() + "\"\r\n");
						fileEntity.append("Content-Type: " + uploadFile.getContentType() + "\r\n\r\n");
						byte[] bytes = fileEntity.toString().getBytes();
						outStream.write(bytes);

						writeFile(uploadFile, outStream);
					}
					outStream.flush();
				}

				// 下面发送数据结束标志，表示数据已经结束
				byte[] bytes = ENDLINE.getBytes();
				outStream.write(bytes);
				outStream.flush();
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains("was not verified")) {
					throw new SSLException(e);
				}
				throw e;
			} finally {
				IOUtils.closeQuietly(outStream);
			}
		}

		return conn;
	}

	private static void writeFile(HttpFormFile uploadFile, OutputStream outStream) throws NetworkBaseException {
		// upload fille
		InputStream is = null;
		try {
			is = uploadFile.getInputStream();
			if (is != null) {
				byte[] buffer = new byte[2048];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
				}
			} else if (uploadFile.getInputData() == null) {
				throw new IllegalArgumentException("upload file is null");
			} else {
				outStream.write(uploadFile.getInputData(), 0, uploadFile.getInputData().length);
			}
			outStream.write("\r\n".getBytes());
		} catch (IOException e) {
			throw new NetworkBaseException("upload file is null", e);
		} finally {
			if (is != null) {
				IOUtils.closeQuietly(is);
			}
		}
	}

	public static byte[] postBaseBytes(String url, byte[] bytes, Map<String, String> headers) throws Exception {
		if (!ContextUtil.isNetworkAvailable()) {
			throw new NetworkUnavailableException();
		}
		HttpURLConnection conn = buildURLConnetion(url, headers);
		try {
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setConnectTimeout(TIMEOUT_CONNECTION);
			conn.setReadTimeout(TIMEOUT_READ);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset", "UTF-8");
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", bytes.length + "");

			OutputStream outStream = conn.getOutputStream();
			outStream.write(bytes);
			outStream.flush();
			outStream.close();
			return readStreamAndClose(conn.getInputStream());
		} catch (Exception e) {
			if (e.getMessage().contains("was not verified")) {
				throw new SSLException(e);
			}
			throw e;
		} finally {
			conn.disconnect();
			HttpConnectionManager.getInstance().removeCurrentThreadActiveConnection();
		}
	}
}