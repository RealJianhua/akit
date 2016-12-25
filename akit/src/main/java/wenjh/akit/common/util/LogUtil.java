package wenjh.akit.common.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import wenjh.akit.config.DebugConfigs;

/**
 * 日志工具类，可以集中管理日志输出
 * 
 * wenjh
 */
@SuppressLint("SimpleDateFormat")
public class LogUtil {
	private final static String TAG = "Log4Android";
	private String mTag = TAG;
	/* 日志前缀 */
	private String mMsgPrefix = "akit==**  ";
	/* 是否为debug模式, debug模式才打印日志，否则关闭所有日志输出 */
	private boolean mDebuggable = DebugConfigs.DEBUGGABLE;

	public LogUtil(String tag) {
		setTag(tag);
	}

	/**
	 * 取class名字为tag
	 * 
	 * @param object
	 */
	public LogUtil(Object object) {
		this(object.getClass().getSimpleName());
	}

	public LogUtil closeDebug() {
		this.mDebuggable = false;
		return this;
	}

	public LogUtil openDebug() {
		this.mDebuggable = DebugConfigs.DEBUGGABLE;
		return this;
	}

	public boolean isDebuggable() {
		return mDebuggable;
	}

	/**
	 * 设置日志标签
	 * 
	 * @param tag
	 */
	public void setTag(String tag) {
		this.mTag = tag;
	}

	public String getTag() {
		return mTag;
	}

	/**
	 * 设置日志消息前缀
	 * 
	 * @param msgPrefix
	 */
	public void setMsgPrefix(String msgPrefix) {
		this.mMsgPrefix = msgPrefix;
	}

	public String getMsgPrefix() {
		return mMsgPrefix;
	}

	public void i(Object info) {
		printLog(mMsgPrefix + info, null, LOG_LEVEL.LOG_INFO);
	}

	public void e(Throwable error) {
		e(mMsgPrefix + (error != null ? error.getMessage() : "null") +"", error);
	}

	public void e(String errorInfo, Throwable error) {
		printLog(errorInfo, error, LOG_LEVEL.LOG_ERROR);
	}

	public void d(Object debugInfo) {
		printLog(mMsgPrefix + debugInfo, null, LOG_LEVEL.LOG_DEBUG);
	}

	public void w(Object warnInfo) {
		printLog(mMsgPrefix + warnInfo, null, LOG_LEVEL.LOG_WARNING);
	}
	
	public void w(Throwable e) {
		printLog(mMsgPrefix + (e != null ? e.getMessage() : "null") +"", null, LOG_LEVEL.LOG_WARNING);
	}

	/**
	 * 打印日志
	 * 
	 * @param log
	 *            日志内容
	 * @param tr
	 *            异常,只对 LOG_ERROR 级别的消息起作用，其它类型的消息，传入 null 即可
	 * @param level
	 *            日志级别
	 */
	public void printLog(String log, Throwable tr, LOG_LEVEL level) {
		switch (level) {
		case LOG_DEBUG:
			if (mDebuggable)
				Log.d(mTag, log);
			break;
		case LOG_ERROR:
			if (tr == null) {
				Log.e(mTag, log);
			} else {
				if (mDebuggable)
					Log.e(mTag, log, tr);
			}
			break;
		case LOG_INFO:
			if (mDebuggable)
				Log.i(mTag, log);
			break;
		case LOG_VERBOSE:
			if (mDebuggable)
				Log.v(mTag, log);
			break;
		case LOG_WARNING:
			if (tr == null) {
				Log.w(mTag, log);
			} else {
				if (mDebuggable)
					Log.w(mTag, log, tr);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 保存日志内容
	 * 
	 * @param log
	 *            日志内容
	 * @param tag
	 *            日志
	 * @param level
	 * @param saveFile
	 */
	public static void saveLog(String log, String tag, LOG_LEVEL level, File saveFile) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM.dd HH:mm:ss.SSS");
		StringBuilder stringBuilder = new StringBuilder("=====================").append('\n').append(dateFormat.format(new Date())).append('\n')
				.append(level.name() + "/" + tag + "\t").append(log).append('\n');

		Writer writer = null;
		try {
			writer = new FileWriter(saveFile, true); // 不是覆盖
			writer.append(stringBuilder);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	/**
	 * 传入一个异常对象，保存异常内容到文件.自动输出附加信息，例如日志级别、标签、时间等等。
	 * 
	 * @param tr
	 *            错误对象
	 * @param tag
	 *            日志
	 * @param saveFile
	 *            文件对象
	 */
	public static void saveLog(Throwable tr, String tag, File saveFile) {
		StringBuilder stringBuilder = new StringBuilder();
		formatErrorStack(stringBuilder, tr);
		saveLog(stringBuilder.toString(), tag, LOG_LEVEL.LOG_ERROR, saveFile);
	}

	/**
	 * 保存异常内容到文件。自动输出附加信息，例如日志级别、标签、时间等等。
	 * 
	 * @param sb
	 *            附带的日志内容
	 * @param tr
	 *            异常对象，将读取详细的异常内容，保存到文件
	 * @param tag
	 *            日志
	 * @param saveFile
	 *            文件对象
	 */
	public static void saveLog(StringBuilder sb, Throwable tr, String tag, File saveFile) {
		formatErrorStack(sb, tr);
		saveLog(sb.toString(), tag, LOG_LEVEL.LOG_ERROR, saveFile);
	}

	public static void formatErrorStack(Appendable err, Throwable tr) {
		formatErrorStack(err, "", tr);
	}

	public static void formatErrorStack(Appendable err, String indent, Throwable tr) {
		try {
			final Writer result = new StringWriter(512);
			final PrintWriter printWriter = new PrintWriter(result);
			Throwable cause = tr;
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			final String stacktraceAsString = result.toString();
			printWriter.close();
			err.append(stacktraceAsString);
		} catch (Exception e) {
			throw new AssertionError();
		}
	}

	/**
	 * 打印日志，使用 INFO 日志级别，默认日志标签为 “Log4AndroidUtils”
	 * 
	 * @param log
	 *            日志内容
	 */
	public static void printLog(String log) {
		if (DebugConfigs.DEBUGGABLE)
			Log.i(TAG, "akit==** " + log);
	}

	/**
	 * 打印日志，使用指定日志级别
	 * 
	 * @param tag
	 *            日志标签
	 * @param log
	 *            日志内容
	 */
	public static void printLog(String tag, String log) {
		if (DebugConfigs.DEBUGGABLE)
			Log.i(tag, log);
	}

	public enum LOG_LEVEL {
		LOG_INFO, LOG_DEBUG, LOG_ERROR, LOG_WARNING, LOG_VERBOSE
	}
}
