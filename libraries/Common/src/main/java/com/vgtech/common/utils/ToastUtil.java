package com.vgtech.common.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.vgtech.common.BaseApp;

/**
 * Created by gs on 2019-12-22.
 */
public class ToastUtil {
    private static Toast toast;

    public static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void showToast(String text) {
        showToast(text, Toast.LENGTH_SHORT);
    }

    public static void showToast(final String text, final int duration) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            show(text, duration);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    show(text, duration);
                }
            });
        }
    }

    private static void show(String text, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        Log.e("TAG_Toast","text="+text);
        toast = Toast.makeText(BaseApp.getAppContext(), TextUtils.isEmpty(text)?"":text, duration);
        toast.show();
    }
}
