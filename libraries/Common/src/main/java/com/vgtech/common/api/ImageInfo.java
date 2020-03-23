package com.vgtech.common.api;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by John on 2015/9/11.
 */
public class ImageInfo extends AbsApiData {

    public String fid;
    public String url;
    public String thumb;

    public boolean take;

    public boolean isTake() {
        return take;
    }

    public void setTake(boolean take) {
        this.take = take;
    }

    public ImageInfo() {
    }

    public ImageInfo(String url) {
        this.url = url;
    }

    public ImageInfo(String url, String thumb) {
        this.url = url;
        this.thumb = thumb;
    }

    public boolean isLocal() {
        return !TextUtils.isEmpty(url) && !(url.startsWith("http://")||url.startsWith("https://"));
    }

    public static boolean isLocal(String url) {
        return !TextUtils.isEmpty(url) && !(url.startsWith("http://")||url.startsWith("https://"));
    }

    public String getUrl() {
        String lurl = url;
        if (!isLocal()) {
            Uri uri = Uri.parse(lurl).buildUpon().appendQueryParameter("imageId", String.valueOf(fid)).build();
            lurl = uri.toString();
        }
        return lurl;
    }

    public static String getImageId(String url) {
        Uri uri = Uri.parse(url);
        String imgId = uri.getQueryParameter("imageId");
        return imgId;
    }
}
