package wenjh.akit.common.asynctask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HandyThreadPool extends ThreadPoolExecutor {
	private final static int SIZE_POOL_CORE = 2;
	private final static int SIZE_POLL_MAX = 10;
	private final static int TIME_KEEP_ALIVE = 2;
	private final static TimeUnit TIME_UNIT = TimeUnit.SECONDS;

	private HandyThreadPool() {
		super(SIZE_POOL_CORE, SIZE_POLL_MAX, TIME_KEEP_ALIVE, TIME_UNIT, new LinkedBlockingQueue<Runnable>(), new RejectedHandler());
	}


	public HandyThreadPool(int min, int max) {
		super(min, max, TIME_KEEP_ALIVE, TIME_UNIT, new LinkedBlockingQueue<Runnable>(), new RejectedHandler());
	}
	
	public HandyThreadPool(int min, int max, int keeptime) {
		super(min, max, keeptime, TIME_UNIT, new LinkedBlockingQueue<Runnable>(), new RejectedHandler());
		
	}
	
	public HandyThreadPool(int min, int max, RejectedExecutionHandler executionHandler) {
		super(min, max, TIME_KEEP_ALIVE, TIME_UNIT, new LinkedBlockingQueue<Runnable>(), executionHandler);
	}

	private static class RejectedHandler implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		}
	}
	
	private static ThreadPoolExecutor globalPool = null;

	/**
	 * 获取一个全新的线程池对象
	 * @return
	 */
	public static ThreadPoolExecutor getDefaultThreadPool() {
		return new HandyThreadPool();
	}
	
	@Override
	protected void finalize() {
		super.finalize();
	}

	/**
	 * 获取一个全局静态的线程池对象
	 *
	 * @return
	 */
	public static ThreadPoolExecutor getGlobalThreadPool() {
		if(globalPool == null) {
			globalPool = new HandyThreadPool(10, 10);
		}
		return globalPool;
	}
	
	private static ThreadPoolExecutor singleGlobalPool = null;
	public static ThreadPoolExecutor getGlobalSingleThreadPool() {
		if(singleGlobalPool == null) {
			singleGlobalPool = new HandyThreadPool(1, 1);
		}
		return singleGlobalPool;
	}
	
	public static ThreadPoolExecutor getSingleThreadPool() {
		return new HandyThreadPool(1, 1);
	}

	private static ThreadPoolExecutor httpPool = null;
	public static ThreadPoolExecutor getHttpImagePool() {
		if(httpPool == null) {
			httpPool = new HandyThreadPool(2, 2);
		}
		return httpPool;
	}

	private static ExecutorService localPool = null;
	public static ExecutorService getLocalImagePool() {
		if(localPool == null) {
			int c = Runtime.getRuntime().availableProcessors();
			localPool = new HandyThreadPool(c, c);
		}
		return localPool;
	}
	
	private static ThreadPoolExecutor profileLocalAvatarPool = null;
	public static ThreadPoolExecutor getProfileLocalAvatarPool() {
		if(profileLocalAvatarPool == null) {
			profileLocalAvatarPool = new HandyThreadPool(1, 1);
		}
		return profileLocalAvatarPool;
	}

	private static ThreadPoolExecutor profileHttpAvatarPool = null;
	public static ThreadPoolExecutor getProfileHttpAvatarPool() {
		if(profileHttpAvatarPool == null) {
			profileHttpAvatarPool = new HandyThreadPool(3, 3);
		}
		return profileHttpAvatarPool;
	}

	public static void reset() {
		if(profileLocalAvatarPool != null) {
			try {
				profileLocalAvatarPool.shutdownNow();
			} catch (Exception e) {
			}
			profileLocalAvatarPool = null;
		}

		if(profileHttpAvatarPool != null) {
			try {
				profileHttpAvatarPool.shutdownNow();
			} catch (Exception e) {
			}
			profileHttpAvatarPool = null;
		}
		
		if(globalPool != null) {
			try {
				globalPool.shutdownNow();
			} catch (Exception e) {
			}
			globalPool = null;
		}
	}

}
