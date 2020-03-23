package com.vgtech.vancloud.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by zhangshaofang on 2015/10/26.
 */
public class LoadingActivity extends Activity {
    private static Drawable sBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("start-up", "LoadingActivity");
        setContentView(R.layout.first_loading_layout);
        if (sBackground == null)
            sBackground = getImageDrawable(this, R.mipmap.loading_img);
        ImageView imageView = (ImageView) findViewById(R.id.bg_welcome);
        imageView.setImageDrawable(sBackground);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LOADING_FINISH);
        registerReceiver(mReceiver, intentFilter);
        handler = new MyHandler(this);
        handler.sendEmptyMessageDelayed(1, 500);

        try {
            SharedPreferences sp = PrfUtils.getSharePreferences(this);
            int pre_version_code = sp.getInt("pre_version_code", 0);
            int versionCode = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
            if (pre_version_code < versionCode) {
                PrfUtils.setUpdateTipFlag(this, false);
                SharedPreferences.Editor edit = PrfUtils.getSharePreferences(this).edit();
                edit.putInt("pre_version_code", versionCode);
                edit.commit();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.finish();
    }

    public static String LOADING_FINISH = "LOADING_FINISH";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LOADING_FINISH.equals(action)) {
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        handler = null;
    }

    //缩放图片，优化
    public Drawable getImageDrawable(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(
                resId);
        return new BitmapDrawable(BitmapFactory.decodeStream(is, null, opt));
    }

    private MyHandler handler;

    private static class MyHandler extends Handler {        //第二步，将需要引用Activity的地方，改成弱引用。
        private WeakReference<LoadingActivity> atyInstance;

        public MyHandler(LoadingActivity aty) {
            this.atyInstance = new WeakReference<LoadingActivity>(aty);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoadingActivity aty = atyInstance == null ? null : atyInstance.get();            //如果Activity被释放回收了，则不处理这些消息
            if (aty == null || aty.isFinishing()) {
                return;
            }
            aty.init();
            aty.finish();
        }
    }

    private void init() {
        ActivityUtils.openAdActivity(this);
    }
}
