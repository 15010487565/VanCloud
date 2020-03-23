package com.vgtech.common.api;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Duke on 2015/9/14.
 */
public class AudioInfo extends AbsApiData {

    public String fid;
    public static final int STATE_FAIL = 1;
    public static final int STATE_PLAY = 2;
    public String url;
    public int time;
    public int state;

    public AudioInfo() {
    }

    public AudioInfo(String url) {
        this.url = url;
    }

    public boolean isLocal() {
        return !TextUtils.isEmpty(url) && !url.startsWith("http://");
    }

    public static boolean isLocal(String url) {
        return !TextUtils.isEmpty(url) && !url.startsWith("http://");
    }

    public String getUrl() {
        String lurl = url;
        if (!isLocal()) {
            Uri uri = Uri.parse(lurl).buildUpon().appendQueryParameter("audioId", String.valueOf(fid)).build();
            lurl = uri.toString();
        }
        return lurl;
    }
    public static long getAudioId(String url) {
        Uri uri = Uri.parse(url);
        String imgId = uri.getQueryParameter("audioId");
        return Long.parseLong(imgId);
    }
    public static String getFid(String url) {
        Uri uri = Uri.parse(url);
        String imgId = uri.getQueryParameter("fid");
        return imgId;
    }
    public float getTime() {
        return time;
    }


}
