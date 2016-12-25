package wenjh.akit.activity.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityHandler {
	private final static int TIME_APPEXIT = 10000; // 退出前台后，超过10秒，发出应用退出的信号
	private static Map<String, ApplicationEventListener> listeners = new HashMap<String, ApplicationEventListener>();
	private static AppPausedTask appPausedTask = null;
	private static Timer timer = new Timer();
	private static boolean isForeground = true;

	private ActivityHandler() {
	}

	static void onActivityResume() {
		if (!isForeground) {
			isForeground = true;
			// 回到前台
			for (ApplicationEventListener listener : listeners.values()) {
				listener.onAppEnter();
			}
		}

		removeTask();
	}


	public static boolean isAppForeground() {
		return isForeground;
	}

	static void onActivityPause() {
		postDelayedTask(new AppPausedTask());
	}

	public static void addEventListner(String tag, ApplicationEventListener listener) {
		listeners.put(tag, listener);
	}

	private static void postDelayedTask(AppPausedTask task) {
		removeTask();

		appPausedTask = task;
		timer.schedule(appPausedTask, TIME_APPEXIT);
	}

	private static void removeTask() {
		if (appPausedTask != null) {
			appPausedTask.cancel();
			appPausedTask = null;

			timer.purge();
		}
	}

	public static void reset() {
		listeners.clear();
		removeTask();
	}

	private static class AppPausedTask extends TimerTask {
		@Override
		public void run() {
			appPausedTask = null;
			isForeground = false;

			for (ApplicationEventListener listener : listeners.values()) {
				listener.onAppExit();
			}
		}
	}

	public interface ApplicationEventListener {
		/**
		 * 所有Activity均退出前台
		 */
		void onAppExit();

		/**
		 * 程序回到前台
		 */
		void onAppEnter();
	}
}
