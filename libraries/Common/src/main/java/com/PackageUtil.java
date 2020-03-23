package com;

import android.app.Application;
import android.content.Context;

/**
 * Data:  2018/10/25
 * Auther: 陈占洋
 * Description:
 */

public class PackageUtil {

    private static Application sApp;

    public static void init(Application app){
//        Log.e("TAG_初始化","app");
        sApp = app;
    }

    public static Context getAppCtx(){
//        Log.e("TAG_初始化","sApp="+(sApp == null));
        return sApp.getApplicationContext();
    }
}
