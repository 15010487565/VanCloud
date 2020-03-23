package com.vgtech.vancloud;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.LoadingActivity;
import com.vgtech.vancloud.ui.LoginActivity;
import com.vgtech.vancloud.ui.adapter.WelcomePageAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2015/11/6.
 */
public class WelcomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener
        , View.OnClickListener {

    private List<View> views;
    View view;
    private static final int[] pics = {R.layout.welcome_item_1,
            R.layout.welcome_item_2, R.layout.welcome_item_3, R.layout.welcome_item_3};


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected int getContentView() {
//       getWindow().getDecorView().setBackground(null);
        return R.layout.welcome_activity_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ApplicationTheme);
        if (!PrfUtils.getFirstLogin(this)) {
            Intent intent = getIntent();
            String scheme = intent.getScheme();
            Intent loadingIntent = new Intent(this, LoadingActivity.class);
            if (!TextUtils.isEmpty(scheme)) {
                Uri uri = intent.getData();
                loadingIntent.setData(uri);
            }
            startActivity(loadingIntent);
            overridePendingTransition(0, 0);

        } else {
            ViewPager viewPager = (ViewPager) findViewById(R.id.welcome_viewpage);
            LayoutInflater inflater = getLayoutInflater();
            views = new ArrayList<View>();

            for (int i = 0; i < pics.length; i++) {
                view = inflater.inflate(pics[i], null);
                ImageView img = (ImageView) view.findViewById(R.id.bg_welcome);
                int width = img.getWidth();
                int height = img.getHeight();
                Bitmap bitmap = getImageDrawable(this, "welcome_" + (i + 1) + ".png", width, height);
                img.setImageBitmap(bitmap);
                views.add(view);
            }

            WelcomePageAdapter welcomePageAdapter = new WelcomePageAdapter(this, views);
            viewPager.setAdapter(welcomePageAdapter);

            viewPager.addOnPageChangeListener(this);

            initDots();
        }
    }

    public Bitmap getImageDrawable(Context context, String image, int width, int height) {

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), getResouceId(context, image, "mipmap", context.getPackageName()), opt);
        int outWidth = opt.outWidth; //获得图片的实际高和宽
        int outHeight = opt.outHeight;
        opt.inDither = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        //设置加载图片的颜色数为16bit，默认是RGB_8888，表示24bit颜色和透明通道，但一般用不上
        opt.inSampleSize = 1;
        //设置缩放比,1表示原比例，2表示原来的四分之一....
        //计算缩放比
        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            opt.inSampleSize = sampleSize;
        }

        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(getResources(), getResouceId(context, image, "mipmap", context.getPackageName()), opt);

    }

    //得到资源Id
    public int getResouceId(Context context, String resouceName, String type,
                            String packageName) {
        int imageIndex = resouceName.indexOf(".");
        resouceName = resouceName.substring(0, imageIndex);
        return context.getResources().getIdentifier(resouceName, type,
                packageName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        welcomeHandler = new WelcomeHandler(this);
        welcomeHandler.sendEmptyMessageDelayed(1, 3000);
    }

    private WelcomeHandler welcomeHandler;

    public static class WelcomeHandler extends Handler {
        private WeakReference<WelcomeActivity> actRef;

        public WelcomeHandler(WelcomeActivity act) {
            actRef = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (actRef == null || actRef.get() == null || actRef.get().isFinishing()) {
                return;
            }
            actRef.get().finish();
        }
    }

//    private ImageView[] dots;
//    private int currentIndex;

    private void initDots() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.bottom_dot);

        ImageView[] dots = new ImageView[pics.length];

        for (int i = 0; i < pics.length; i++) {
            dots[i] = (ImageView) ll.getChildAt(i);
            dots[i].setEnabled(true);
            dots[i].setTag(i);

        }
//        int currentIndex = 0;
//        dots[currentIndex].setEnabled(false);
    }

//    private void setCurDot(int positon) {

//        dots[positon].setEnabled(false);
//        dots[currentIndex].setEnabled(true);
//        currentIndex = positon;
//    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
//        setCurDot(position);
        TextView viewById = (TextView) findViewById(R.id.enter_text);

        if (position == pics.length - 1) {
            viewById.setOnClickListener(this);
            viewById.setVisibility(View.VISIBLE);
        } else {
            viewById.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.enter_text:
            PrfUtils.setFirstLogin(this, false);
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
                break;
            default:
                super.onClick(v);
                break;
        }

    }

    public boolean swipeBackPriority() {
        return false;
    }
}

