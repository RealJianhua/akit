package wenjh.akit.common.http;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import wenjh.akit.common.asynctask.HandyThreadPool;
import wenjh.akit.common.util.LogUtil;

public class HttpConnectionManager {
	LogUtil log = new LogUtil(this);
	Map<Long, HttpURLConnection> activeMap = null;

	private HttpConnectionManager() {
		activeMap = new HashMap<Long, HttpURLConnection>();
	}

	private static HttpConnectionManager sConnectionManagerInstance = null;

	public static HttpConnectionManager getInstance() {
		if (sConnectionManagerInstance == null) {
			sConnectionManagerInstance = new HttpConnectionManager();
		}
		return sConnectionManagerInstance;
	}

	public HttpURLConnection getActiveConnectionByThreadId(long threadId) {
		return activeMap.get(threadId);
	}

	public HttpURLConnection getCurrentThreadActiveConnection(long threadId) {
		return getActiveConnectionByThreadId(Thread.currentThread().getId());
	}

	public HttpURLConnection removeCurrentThreadActiveConnection() {
		return removeThreadActiveConnection(Thread.currentThread().getId());
	}

	public HttpURLConnection removeThreadActiveConnection(long threadId) {
		return activeMap.remove(threadId);
	}

	public void putActiveConnection(long threadId, HttpURLConnection connection) {
		activeMap.put(threadId, connection);
	}
	
	public void putCurrentThreadActiveConnection(HttpURLConnection connection) {
		putActiveConnection(Thread.currentThread().getId(), connection);
	}

	public void killActiveConnectionByThreadId(long threadId) {
		final HttpURLConnection httpURLConnection = removeThreadActiveConnection(threadId);
		if (httpURLConnection != null) {
			HandyThreadPool.getGlobalThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						log.w(httpURLConnection.getURL().getPath() + "-> kill");
						httpURLConnection.disconnect();
					} catch (Exception e) {
						log.e(e);
					}
				}
			});
		}
	}
	
	public void killCurrentThreadActiveConnection() {
		killActiveConnectionByThreadId(Thread.currentThread().getId());
	}
}
