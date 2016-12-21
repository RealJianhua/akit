package wenjh.akit.common.util;

import java.io.File;
import java.util.Date;

public class Log2Sdcard extends LogUtil {
	File dirFile = null;
	
	public Log2Sdcard(String tag) {
		super(tag);
		setMsgPrefix("\n");
	}
	
	public Log2Sdcard(Object obj) {
		super(obj);
		setMsgPrefix("\n");
	}
	
	@Override
	public void printLog(String log, Throwable tr, LOG_LEVEL level) {
		super.printLog(log, tr, level);
		File saveFile = new File(dirFile, "log2sdcard_"+DateUtil.formateDate2(new Date())+".log");
		saveLog(log, getTag(), level, saveFile);
	}
}
