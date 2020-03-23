package com.vgtech.vancloud.ui.web;

import android.app.Activity;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vgtech.common.wxapi.Constants;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Data:  2017/11/29
 * Auther: 陈占洋
 * Description:
 */

class DownloadJSInterface {

//    /**
//     * 读写权限
//     */
//    protected static final int WRITE_PERMISSION = 20003;
//    protected static final String[] WRITEPERMISSION = {
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };

    private WeakReference<Activity> mActWeakRef;
//    private static final String TAG = "DownloadJSInterface";
//    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.obj instanceof HttpException) {
//                HttpException ex = (HttpException) msg.obj;
//                if (mActWeakRef != null && mActWeakRef.get() != null) {
//                    Toast.makeText(mActWeakRef.get(), ex.mExmsg, Toast.LENGTH_SHORT).show();
//                }
//                if (ex.mCode == 200) {
//
//                    if (!TextUtils.isEmpty(ex.msg)) {
//
//                        Intent intent = OpenFileUtil.openFile(mActWeakRef.get(), ex.msg);
//
//                        if (mActWeakRef != null && mActWeakRef.get() != null) {
//                            mActWeakRef.get().startActivity(intent);
//                        }
//                    }
//                }
//            }
//        }
//    };
    private String mDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "wanke";


    public DownloadJSInterface(Activity act) {
        mActWeakRef = new WeakReference<>(act);
    }

    //文件下载
    @JavascriptInterface
    public void downloadFile(String url, String type) {
        Activity activity = mActWeakRef.get();
        Log.e("TAG_文件下载", url);
        HelpOpenFileUtils.downloadFile((FragmentActivity) activity,url);

    }

    public void clearDownloadDir() {
        File downloadDir = new File(mDownloadDir);
        deleteDir(downloadDir);
    }

    private void deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            String[] childFiles = dir.list();
            for (int i = 0; i < childFiles.length; i++) {
                File file = new File(dir, childFiles[i]);
                if (!file.isDirectory()) {
                    file.delete();
                } else {
//                    deleteDir(dir);
                }
                dir.delete();
            }
        }
    }

    /**
     * 微信登录相关
     */
    private IWXAPI api;

    @JavascriptInterface
    public void weixinLogin() {
        Log.e("TAG_微信", "登录");

        //通过WXAPIFactory工厂获取IWXApI的示例
        api = WXAPIFactory.createWXAPI(mActWeakRef.get(), Constants.APP_ID, true);
        //将应用的appid注册到微信
        api.registerApp(Constants.APP_ID);

        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";//
//                req.scope = "snsapi_login";//提示 scope参数错误，或者没有scope权限
        req.state = "wechat_sdk_微信登录";
        api.sendReq(req);
    }
}
