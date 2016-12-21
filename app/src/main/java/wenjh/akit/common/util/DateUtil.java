package wenjh.akit.common.util;

import android.annotation.SuppressLint;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author <a href="mailt:wenlin56@sina.com"> wjh </a>
 */
@SuppressLint("SimpleDateFormat")
public class DateUtil {
	public final static long DayMilliseconds = 86400000L;

	/**
	 * 获取时间对象的时间戳表示，即1970年以来的秒数
	 *
	 * @param date
	 * @return
	 */
	public static long formateTimestamp(Date date) {
		return date.getTime() / 1000;
	}

	/**
	 * 格式化日期对象为字符串，格式为：yyyyMMddHHmmss
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTime2(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(date);
	}

	/**
	 * 格式化日期对象为字符串，格式为：yyyyMMddHHmmssSSS
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTime5(Date date) {
		if(date == null) {
			return "UNKNOWN";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return dateFormat.format(date);
	}

	/**
	 * 格式化日期对象为字符串，格式为：yyyyMMdd
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTime11(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return dateFormat.format(date);
	}

	/**
	 * 格式化日期对象为字符串，格式为：HHmmss
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTime12(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
		return dateFormat.format(date);
	}

	/**
	 * 格式化日期对象为字符串，格式为：MM-dd HH:mm
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTime3(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");
		return dateFormat.format(date);
	}



	/**
	 * 格式化一个日期对象，格式为：yyyy-MM-dd
	 *
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date);
	}

	/**
	 * 格式化一个字符串为日期对象，字符串格式为：yyyy-MM-dd HH:mm:ss
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTime(Date date) {
		if(date == null || date.getTime() == 0){
			return "未知时间";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	/**
	 * 格式化一个字符串为日期对象，字符串格式为：MM-dd HH:mm
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTimeWithOutYear(Date date) {
		if(date == null || date.getTime() == 0){
			return "未知时间";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"MM-dd HH:mm");
		return dateFormat.format(date);
	}

	/**
	 * 格式化一个字符串为日期对象，字符串格式为：yyyy-MM-dd HH:mm
	 *
	 * @param date
	 * @return
	 */
	public static String formateDateTime4(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm");
		return dateFormat.format(date);
	}

	/**
	 * 格式日期，格式为：yyyyMMdd，不包含时间
	 *
	 * @param date
	 *            日期对象
	 * @return
	 */
	public static String formateDate2(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return dateFormat.format(date);
	}

	/**
	 * 格式日期，格式为：MM-dd，不包含时间
	 *
	 * @param date
	 *            日期对象
	 * @return
	 */
	public static String formateDate3(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
		return dateFormat.format(date);
	}

	/**
	 * 格式时间，格式为：HH:mm，不包含日期
	 *
	 * @param date
	 *            日期对象
	 * @return
	 */
	public static String formateTime(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		return dateFormat.format(date);
	}

	/**
	 * 将一个 yyyy-MM-dd 格式的字符串转换为 Date 对象，忽略时间
	 *
	 * @param dateStr
	 * @return
	 */
	public static Date parseStringToDate(String dateStr) {
		if(StringUtil.isEmpty(dateStr)) {
			return null;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return date;
	}

	/**
	 * 将时间戳(秒)字符串转换为 Date 对象
	 *
	 * @param timestamp
	 *            时间戳，单位是“秒”
	 * @return
	 */
	public static Date parseTimeStampToDate(long timestamp) {
		if(timestamp <= 0) {
			return null;
		}

		Date date = new Date();
		date.setTime(timestamp * 1000); // JAVA的Date对象以毫秒来表示时间戳
		return date;
	}


	/*public static Date parseTimeStampToDate2(long milSeconds){
		if(milSeconds <= 0) {
			return null;
		}

		Date date = new Date();
		date.setTime(milSeconds); // JAVA的Date对象以毫秒来表示时间戳
		return date;
	}*/

	/**
	 * 将一个 yyyyMMdd 格式的字符串转换为 Date 对象，忽略时间
	 *
	 * @param dateStr
	 * @return
	 */
	public static Date parseStringToDate2(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 将一个 yyyy-MM-dd HH:mm:ss 格式的字符串转换为 Date 对象，对象必须包含时间细节
	 *
	 * @param dateStr
	 * @return
	 */
	public static Date parseStringToDateTime(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 将一个 yyyyMMddHHmmss 格式的字符串转换为 Date 对象，对象必须包含时间细节
	 *
	 * @param dateStr
	 * @return
	 */
	public static Date parseStringToDateTime2(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = null;
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 将一个 yyyyMMddHHmmssSSS 格式的字符串转换为 Date 对象，对象必须包含时间细节
	 *
	 * @param dateStr
	 * @return
	 */
	public static Date parseStringToDateTime5(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date date = null;
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static int getAge(Date birthDay) {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            return -1;
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        } else {
        }

        return age;
    }


	public static Date getServerDate() {
		Calendar c = Calendar.getInstance();
		try {
			URL url = new URL("http://www.beijing-time.org/time.asp");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3000);
			conn.connect();
			InputStream is = conn.getInputStream();
			Properties properties = new Properties();
			properties.load(is);
			is.close();

			c.set(Calendar.YEAR, Integer.valueOf(((String)(properties.get("nyear"))).replace(";", "")));
			c.set(Calendar.MONTH, Integer.valueOf(((String)(properties.get("nmonth"))).replace(";", ""))-1);
			c.set(Calendar.DATE, Integer.valueOf(((String)(properties.get("nday"))).replace(";", ""))-1);
			c.set(Calendar.HOUR, Integer.valueOf(((String)(properties.get("nhrs"))).replace(";", "")));
			c.set(Calendar.MINUTE, Integer.valueOf(((String)(properties.get("nmin"))).replace(";", "")));
			c.set(Calendar.SECOND, Integer.valueOf(((String)(properties.get("nsec"))).replace(";", "")));

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return c.getTime();
	}

	public static String betweenWithCurrentTime(Date currentTime) {
		return between(currentTime, new Date());
	}
	
	public static long betweenTime(Date before, Date  after) {
		if(before == null || after == null) {
			return -1;
		}
		
		long afterL = after.getTime();
		long beforeL = before.getTime();
		return Math.abs(afterL-beforeL);
	}
	
	public static String between(Date before, Date after) {
		final int maxDay = 30;
		if(before==null || after ==null ) {
			return "Unknown";
		}

		if(after.before(before)) {
			return "1 mins";
		}

		long afterL = after.getTime();
		long beforeL = before.getTime();

		long n = Math.abs(afterL-beforeL);

		n /= 1000;
		long minute = n / 60;
		long hour = minute / 60;
		minute %= 60;


		if(hour >= maxDay*24) {
			return "30 days";
		}

		if(hour >= 24) {
			return  (int)hour/24 +" days";
		}


		if(hour > 0) {
			return hour +" hours";
		}


		if(minute < 1) {
			return "Just now";
		}

		return minute + " mins";

	}
}
