package com.vgtech.vancloud.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.igexin.sdk.PushManager;
import com.vgtech.vancloud.R;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by zhangshaofang on 2015/10/26.
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_loading_layout);

        Drawable drawable = getImageDrawable(this, R.mipmap.loading_img);
        ImageView imageView = (ImageView) findViewById(R.id.bg_welcome);
        imageView.setImageDrawable(drawable);
        handler = new MyHandler(this);
        handler.sendEmptyMessageDelayed(1, 0);
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
        private WeakReference<SplashActivity> atyInstance;

        public MyHandler(SplashActivity aty) {
            this.atyInstance = new WeakReference<SplashActivity>(aty);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashActivity aty = atyInstance == null ? null : atyInstance.get();            //如果Activity被释放回收了，则不处理这些消息
            if (aty == null || aty.isFinishing()) {
                return;
            }
            Intent intent = new Intent(aty, MainActivity.class);
            intent.putExtra("switchTenant",true);
            aty.startActivity(intent);
            aty.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            aty.finish();
        }
    }

}
