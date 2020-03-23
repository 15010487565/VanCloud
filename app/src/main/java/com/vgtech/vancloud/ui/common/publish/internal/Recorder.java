package com.vgtech.vancloud.ui.common.publish.internal;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by zhangshaofang on 2015/8/27.
 */
public class Recorder {
    private int time;
    public String filePathString;

    public Recorder(float time, String filePathString) {
        super();
        this.time = (int) time;
        this.filePathString = filePathString;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getFilePathString() {
        return filePathString;
    }

    public void setFilePathString(String filePathString) {
        this.filePathString = filePathString;
    }


    public boolean isLocal() {
        return !TextUtils.isEmpty(filePathString) && !filePathString.startsWith("http://");
    }

    public static boolean isLocal(String url) {
        return !TextUtils.isEmpty(url) && !url.startsWith("http://");
    }

    public long getAudioId(String url) {
        Uri uri = Uri.parse(url);
        String imgId = uri.getQueryParameter("audioId");
        return Long.parseLong(imgId);
    }
}
