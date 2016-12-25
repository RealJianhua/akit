package wenjh.akit.demo.account;

import android.net.Uri;

import java.io.File;

import wenjh.akit.common.util.Image;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.demo.config.HostConfigs;

public class DemoImage extends Image {

    public DemoImage(File file) {
        super(file);
    }

    public DemoImage(String uriString) {
        super(uriString);
    }

    public DemoImage() {
        super();
    }

    private String imageGuid = null;

    public void setImageGuid(String imageGuid) {
        setImageGuid(imageGuid, 2);
    }

    public void setImageGuid(String imageGuid, int size) {
        this.imageGuid = imageGuid;
        if (!StringUtil.isEmpty(imageGuid)) {
            String imageUrl = HostConfigs.getImageUrlWithGUID(imageGuid, size);
            setImageURL(imageUrl);
        } else {
            setImageUri(null);
        }
    }

    public String getImageGuid() {
        return imageGuid;
    }
}
