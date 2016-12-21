package wenjh.akit.common.http;

import java.net.HttpURLConnection;

public interface DownloadProgressCallback{
	public final static int STATUS_INIT = 1;
	public final static int STATUS_ERROR = 2;
	public final static int STATUS_RUNNING = 3;
	public final static int STATUS_STOP = 5;
	public final static int STATUS_FINISH = 4;
	
	public void callback(long total, long prgress, int status, HttpURLConnection connection);
	
	/**
	 * {@link #STATUS_STOP} stop download
	 * @return
	 */
	public int getControllerStatus();
}