package com.vgtech.vancloud.ui.web;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.zxing.zxing.ToastUtil;
import com.vgtech.common.utils.DataUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Data:  2019/6/18
 * Auther: xcd
 * Description:
 */
public class HelpOpenFileUtils {

    /**
     * 读写权限
     */
    protected static final int WRITE_PERMISSION = 20003;
    protected static final String[] WRITEPERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static String mDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "wanke";

    private static final String TAG = "HelpOpenFileUtils";

    private static void openFile(FragmentActivity context, File downloadFile) {
        Log.e("TAG_文件下载", "downloadFile=" + downloadFile.getPath());
        String name = downloadFile.getName().toLowerCase();
        Log.e("TAG_文件下载", "name=" + name);
        if (name.endsWith(".doc") || name.endsWith(".docx") || name.endsWith(".xls")
                || name.endsWith(".txt") || name.endsWith(".ppt") || name.endsWith(".pptx")
                || name.endsWith(".xlsx") || name.endsWith(".pdf")) {
            Intent piCoinInt = new Intent(context, WebX5FileActivity.class);
            piCoinInt.putExtra("url", downloadFile.getPath());
            piCoinInt.putExtra("name", downloadFile.getName());
            piCoinInt.putExtra("title", "详情");
            context.startActivity(piCoinInt);
        } else if (name.endsWith(".jpg") || name.endsWith(".png")
                || name.endsWith(".gif")){
            Intent piCoinInt = new Intent(context, WebX5Activity.class);
            piCoinInt.putExtra("url", "file://mnt/sdcard/Download/wanke/" + downloadFile.getName());
            piCoinInt.putExtra("title", "详情");
            context.startActivity(piCoinInt);
        }else if (name.endsWith(".bmp")){
            ImageDialogFragment imageDialog = new ImageDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url", downloadFile.getPath());
            imageDialog.setArguments(bundle);
            imageDialog.show(context.getSupportFragmentManager(),"lose");
        } else {
            Intent intent = OpenFileUtil.openFile( context, downloadFile.getPath());
            context.startActivity(intent);

        }
    }

    private static void nativeDownloadFile(final FragmentActivity context, String url, final File downloadFile) {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).get().build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtil.toast(context, "下载文件失败！！！");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                if (code >= 500) {
                    ToastUtil.toast(context, "下载文件失败！！！");
                } else if (code >= 200 && code < 300) {
                    InputStream is = response.body().byteStream();
                    byte[] buf = new byte[8 * 1024];
                    int len = 0;
                    long sum = 0;
                    OutputStream fos = null;
                    try {
                        fos = new FileOutputStream(downloadFile);

                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            sum += len;
                        }
                        fos.flush();
                        openFile(context, downloadFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtil.toast(context, "下载文件失败！！！");
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    }
                }

            }
        });
    }

    //文件下载
    public static void downloadFile(FragmentActivity context, String url) {

        String type=url.substring(url.lastIndexOf(".")+1);
        Log.e(TAG, "type=" + type);
        PermissionsChecker mChecker = new PermissionsChecker(context);
        if (mChecker.lacksPermissions(WRITEPERMISSION)) {
            // 请求权限
            PermissionsActivity.startActivityForResult((Activity) context, WRITE_PERMISSION, WRITEPERMISSION);
        } else {
            // 全部权限都已获取
            Log.e(TAG, "url=" + url);
            if (TextUtils.isEmpty(url)) {
                Log.e(TAG, "downloadFile: url下载地址为空！！！");
                return;
            }
            try {
                int indexOf = url.indexOf("file_name=");
                if (indexOf != -1) {
                    url = url.substring(0, indexOf + 10) + URLEncoder.encode(url.substring(indexOf + 10), "UTF-8");
                    Log.e(TAG, "url=" + url);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            File downloadFile = createDefaultTimeFile(type);
            if (null == downloadFile) {
                Log.e(TAG, "downloadFile: 文件创建失败！！！");
                return;
            }
            nativeDownloadFile(context,url, downloadFile);
        }
    }
    private static File createDefaultTimeFile(String type) {
        String fileName = DataUtils.dateFormat(System.currentTimeMillis(), "yyyyMMddHHmmss");
        File downloadFile = new File(mDownloadDir, fileName + "." + type);
        return createFile(downloadFile);
    }
    private static File createFile(File file) {
        Log.e(TAG, "file=" + file.toString());
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                Log.e(TAG, "createFile: 目录创建失败！！！");
                return null;
            }
        }
        if (file.exists()) {
            Log.e(TAG, "downloadFile: 文件已存在！！！");
            String filePath = file.toString();
            if (filePath.lastIndexOf("(") == -1) {
                filePath += "(1)";
                file = new File(filePath);
            } else {
                int sindex = filePath.lastIndexOf("(");
                int eindex = filePath.lastIndexOf(")");
                String numstr = filePath.substring(sindex + 1, eindex);
                int num = Integer.parseInt(numstr);
                String originalPath = filePath.substring(0, sindex);
                filePath = originalPath + "(" + (++num) + ")";
                file = new File(filePath);
            }
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;

    }
}
