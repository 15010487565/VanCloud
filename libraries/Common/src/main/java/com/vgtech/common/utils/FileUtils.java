package com.vgtech.common.utils;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.ui.permissions.PermissionsChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {


    public static String saveBitmap(Context context, Bitmap bm, String picName) {
        Log.e("", "保存图片");
        try {
            File f = new File(FileCacheUtils.getPublishImageDir(context), picName + ".ing");
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.e("", "已经保存");
            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String saveBitmap(Context context, Bitmap bm, String picName, String type) {
        Log.e("", "保存图片");
        try {
            File f = new File(FileCacheUtils.getPublishImageDir(context), picName + "." + type);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.e("", "已经保存");
            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String saveXmppBitmap(Context context, Bitmap bm, String picName) {
        Log.e("", "保存图片");
        try {
            File f = new File(FileCacheUtils.getXmppImageDir(context), picName + ".ing");
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.e("", "已经保存");
            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 读写权限
     */
    protected static final String[] WRITEPERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void writeString(Context context,String s) {
        PermissionsChecker mChecker = new PermissionsChecker(context);
        Log.e("TAG_FileUtils","writeString="+(mChecker.lacksPermissions(WRITEPERMISSION)));
        if (!mChecker.lacksPermissions(WRITEPERMISSION)) {// 请求权限
            String logPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + File.separator + "vgtech" + File.separator + "log.txt";
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
                fileWriter = new FileWriter(logFile, true);
                fileWriter.append(s);
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
