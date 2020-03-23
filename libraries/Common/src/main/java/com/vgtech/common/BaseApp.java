package com.vgtech.common;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.vgtech.common.network.ApiUtils;

/**
 * Data:  2019/7/19
 * Auther: xcd
 * Description:
 */
public class BaseApp extends MultiDexApplication {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        BaseApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return BaseApp.context;
    }

    private ApiUtils mApiUtils;
    public ApiUtils getApiUtils() {

        if (mApiUtils == null) {
            mApiUtils = new ApiUtils(this);
        } else {
            mApiUtils.ensureSignInfo();
        }
        return mApiUtils;
    }

}
