package wenjh.akit.common.cropimg;

import android.graphics.Bitmap;

import java.io.File;

import wenjh.akit.common.util.ContextUtil;

public class ImageResourceOperation extends ImageRequest.Operation {
    protected static final String TAG = "DroidKit";

    protected static final int BUFFER_SIZE = 64 * 1024; // 64KB

    int resourceId = 0;
    String cacheName = null;
    File cacheFile = null;
    
    public ImageResourceOperation(ImageRequest request, int resourceId) {
        super(request);
        this.resourceId = resourceId;
        cacheName = request.cacheNameFor("R.drawable." + Integer.toString(resourceId));
        cacheFile = request.cacheFileFor(cacheName);
    }

    @Override
    public Bitmap perform(Bitmap previousBitmap) {
        if (resourceId == 0)
            return null;
        else
            return ContextUtil.getBitmap(resourceId);
    }

    @Override
    public String name(String previousName) {
        return cacheName;
    }
    
    @Override
    public boolean isCached() {
        return true;
    }


}