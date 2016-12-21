package wenjh.akit.common.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapCache extends LruCache<String, Bitmap> {
	private static final int MaxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	private static final int sCacheSize = MaxMemory / 8;
	private Map<String, SoftReference<Bitmap>> imagePool = new HashMap<String, SoftReference<Bitmap>>();
	
	private BitmapCache(int size) {
		super(size);
	}
	
	private static BitmapCache instance = null;
	
	public static BitmapCache getInstance() {
		if(instance == null) {
			int size;
			if(sCacheSize > 10240) {
				size = 10240;
			} else if(sCacheSize < 10240) {
				size = 4096;
			} else {
				size = sCacheSize;
			}
			instance = new BitmapCache(size);
		}
		return instance;
	}
	
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
		if(oldValue != null) {
			imagePool.put(key, new SoftReference<Bitmap>(oldValue));
		}
	}
	
	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes()*value.getHeight()/1024;
	}
	
	public Bitmap getBitmap(String key) {
		Bitmap result = get(key);
		if(result == null) {
			result = imagePool.get(key) != null ?  imagePool.get(key).get() : null;
		}
		return result;
	}
	
	public void putBitmap(String key, Bitmap bitmap) {
		if(bitmap != null) {
			put(key, bitmap);
		} else {
			remove(key);
		}
	}
}
