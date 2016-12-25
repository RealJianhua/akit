package wenjh.akit.common.asynctask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import wenjh.akit.common.util.LogUtil;

public abstract class BatchDownloadProfileUtil<T> {
	private Map<String, T> readyBatchMap = null;
	private Map<String, T> allDownloadingMap = null;
	private AsyncCallback<T> callback = null;
	private LogUtil log = new LogUtil(this);
	
	public BatchDownloadProfileUtil() {
		readyBatchMap = new LinkedHashMap<String, T>();
		allDownloadingMap = new HashMap<String, T>();
	}
	
	public synchronized T addToBatchList(String key) {
		T object = allDownloadingMap.get(key);
		
		if(object == null) {
			object= readyBatchMap.get(key);
		}
		
		if(object == null) {
			object = newObject(key);
			readyBatchMap.put(key, object);
		}
		
		return object;
	}
	
	public synchronized void remove(String key) {
		readyBatchMap.remove(key);
	}
	
	public void setCallback(AsyncCallback<T> callback) {
		this.callback = callback;
	}
	
	private synchronized void removeFromDownloading(String key) {
		allDownloadingMap.remove(key);
	}
	
	public synchronized void requestAsync() {
		final Map<String, T> thisDownloadingMap = new LinkedHashMap<String, T>();
		thisDownloadingMap.putAll(readyBatchMap);
		readyBatchMap.clear();
		
		allDownloadingMap.putAll(thisDownloadingMap);
		
		HandyThreadPool.getGlobalThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<String, T> download : thisDownloadingMap.entrySet()) {
					try {
						T obj = download.getValue();
						download(obj);
						if(callback != null) {
							callback.callback(obj);
						}
					} catch (Exception e) {
						log.e(e);
					} finally {
						removeFromDownloading(download.getKey());
					}
				}
				
				thisDownloadingMap.clear();
			}
		});
	}
	
	protected abstract T newObject(String key);
	
	protected abstract void download(T obj) throws Exception;
	
}
