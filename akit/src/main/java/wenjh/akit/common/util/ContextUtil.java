package wenjh.akit.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;

import java.io.File;
import java.util.Locale;

import wenjh.akit.AKitApplication;

public class ContextUtil {
	private static Context sApplicationContext = null;
	private static ContentResolver sContentResolver = null;
	private static String sPackageName = null;
	private static Float sScreenDensity = null;
	private static Float sTextScale = null;
	private static File sExternalStorageDirectory = null;
	private static LayoutInflater sLayoutInflater = null;
	
	public static void initApplicationContext(Context context) {
		ContextUtil.sApplicationContext = context;
	}
	
	public static String getPackageName() {
		if (sPackageName == null) {
			sPackageName = sApplicationContext.getPackageName();
			if (sPackageName.indexOf(":") >= 0) {
				sPackageName = sPackageName.substring(0, sPackageName.lastIndexOf(":"));
			}
		}

		return sPackageName;
	}
	
	public static String getStringFromResource(int resId) {
		return sApplicationContext.getString(resId);
	}

	public static Resources getResources() {
		return sApplicationContext.getResources();
	}

	public static Context getContext() {
		return sApplicationContext;
	}
	
	public static boolean isIcsVsersion() {
		return Build.VERSION.SDK_INT >= 14;
	}
	
	public static DisplayMetrics getDisplayMetrics() {
		return getResources().getDisplayMetrics();
	}
	
	public static int sp2pix(float sp) {
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getDisplayMetrics()));
	}
	
	public static int dip2Pixels(float dip) {
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getDisplayMetrics()));
	}

	public static boolean isNetworkAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) sApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static String getNetWorkType() {
		ConnectivityManager connManager = (ConnectivityManager) sApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();// 获取网络的连接情况
		if (activeNetInfo != null) {
			if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				return "wifi";
			} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				return "mobile";
			}
		}
		return null;
	}

	public static boolean isWifi() {
		return "wifi".equals(getNetWorkType());
	}

	public static int getMobileNetType() {
		try {
			TelephonyManager manager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
			return manager.getNetworkType();
		} catch (Exception e) {
		}
		return -1;
	}

	public static int getVersionCode() {
		int versionCode = 0;
		try {
			PackageInfo pinfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_CONFIGURATIONS);
			versionCode = pinfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	public static String getVersionName() {
		try {
			PackageInfo pinfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_CONFIGURATIONS);
			return pinfo.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}

	public static AKitApplication getApp() {
		return (AKitApplication) sApplicationContext;
	}

	public static boolean isJBVsersion() {
		return Build.VERSION.SDK_INT >= 16;
	}

	public static LayoutInflater getLayoutInflater() {
		return LayoutInflater.from(sApplicationContext);
	}
	
	public static int getScreenWidth() {
		return getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight() {
		return getDisplayMetrics().heightPixels;
	}
	

	public static String getString(int resId) {
		return getResources().getString(resId);
	}

	public static int getColor(int resId) {
		return getResources().getColor(resId);
	}

	private static final String SDCARD_PATH_FORMAT = "Android/data/%s";
	public static File getExternalStorageDirectory() {
		return new File(Environment.getExternalStorageDirectory(), String.format(Locale.US, SDCARD_PATH_FORMAT, getPackageName()));
	}

	public static Bitmap getBitmap(int resourceId) {
		return BitmapFactory.decodeResource(getResources(), resourceId);
	}

	public static float getScreenDensity() {
		return getDisplayMetrics().density;
	}

	public static ContentResolver getContentResolver() {
		return sApplicationContext.getContentResolver();
	}

	public static File getCacheDir() {
		return ContextUtil.getContext().getExternalCacheDir();
	}

}
