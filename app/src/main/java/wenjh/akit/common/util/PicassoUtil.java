package wenjh.akit.common.util;

import wenjh.akit.config.DebugConfigs;

import com.squareup.picasso.Picasso;

/**
 * Created by wjh on 2016/11/30.
 */

public class PicassoUtil {
    private static Picasso picassoSingleton;

    public static Picasso picasso() {
        if (picassoSingleton == null) {
            synchronized (Picasso.class) {
                if (picassoSingleton == null) {
                    //可以重写OkHttp3Downloader的构造方法, 实现不同的cache类，
                    // 重写cache类中的urlToKey方法，来自定义图片缓存文件的名字
                    picassoSingleton = new Picasso.Builder(ContextUtil.getContext())/*.downloader(momookhttpdownloader)*/.build();
                    picassoSingleton.setIndicatorsEnabled(DebugConfigs.DEBUGGABLE);
                    picassoSingleton.setLoggingEnabled(DebugConfigs.DEBUGGABLE);
                }
            }
        }
        return picassoSingleton;
    }
}
