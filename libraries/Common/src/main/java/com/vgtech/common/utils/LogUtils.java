package com.vgtech.common.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Data:  2019/10/12
 * Auther: xcd
 * Description:
 */
public class LogUtils {
    /**
     * 读写权限
     */
    protected static final String[] WRITEPERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void createLogFile(Context context,String fileName, String txtContent){
        PermissionsChecker mChecker = new PermissionsChecker(context);
        if (!mChecker.lacksPermissions(WRITEPERMISSION)) {// 请求权限
            String logPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/com.vgtech.vancloud/"+fileName;
            File logFile = new File(logPath);
            if (!logFile.exists()) {
                if (!logFile.getParentFile().exists()) {
                    logFile.getParentFile().mkdirs();
                }
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileWriter fileWriter = null;
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
                //获取当前时间
                Date date = new Date(System.currentTimeMillis());

                fileWriter = new FileWriter(logFile, true);
                fileWriter.append("Date获取当前日期时间"+simpleDateFormat.format(date)+"============="+txtContent+"\n");
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileWriter != null) {
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
