package com.vgtech.vancloud.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.provider.db.PublishTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * create by vic.zhang
 */
public class ExitReceiver extends BroadcastReceiver {

    public static String EXITACTION = "com.vgtech.vancloud.exit";
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (EXITACTION.equals(intent.getAction())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    clearCache(context);
                }
            }).start();

        }
    }
    public void clearCache(Context context)
    {
        File imgDir = FileCacheUtils.getPublishImageDir(context);
        File audioDir = FileCacheUtils.getPublishAudioDir(context);
        List<PublishTask> taskList = PublishTask.queryTask(context);
        List<String> imageList = new ArrayList<String>();
        List<String> audioList = new ArrayList<String>();
        for (PublishTask t : taskList) {
            String imagePath = t.image;
            if (!TextUtils.isEmpty(imagePath)) {
                String[] paths = imagePath.split(",");
                for(String path:paths)
                {
                    if(ImageInfo.isLocal(path))
                    {
                        imageList.add(path);
                    }
                }
            }
            String audioPath = t.audio;
            if (!TextUtils.isEmpty(audioPath)) {
                String[] paths = audioPath.split(",");
                for(String path:paths)
                {
                    if(AudioInfo.isLocal(path))
                    {
                        audioList.add(path);
                    }
                }
            }
        }
        try {
            FileCacheUtils.clearCache(imgDir,imageList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileCacheUtils.clearCache(audioDir,audioList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
