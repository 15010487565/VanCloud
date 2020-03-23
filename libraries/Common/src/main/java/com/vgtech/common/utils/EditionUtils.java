package com.vgtech.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.vgtech.common.PrfUtils;

/**
 * Created by zhangshaofang on 2016/5/24.
 */
public class EditionUtils {
    public static final int REQUEST_EDITION = 1111;
    public static final String EDITION_PERSONAL = "personal";
    public static final String EDITION_TENANT = "tenant";
    public static final String SWITCH_EDITION = "com.vgtech.vancloud.switch";

    public static void switchEdition(Activity activity) {
        activity.startActivityForResult(new Intent("com.vgtech.vancloud.excess"), REQUEST_EDITION);
    }


    public static String getCurrentEdition(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        String username = preferences.getString("username","");
        return preferences.getString(username + "edition", EDITION_TENANT);
    }
    public static String getCurrentEdition(String username,Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        return preferences.getString(username + "edition", EDITION_PERSONAL);
    }

    public static boolean isTenantUser(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        String userType = preferences.getString("user_type", EDITION_PERSONAL);
        return userType.equals(EDITION_TENANT);
    }

    public static void setCurrentEdition(Context context, String edition) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        String username = preferences.getString("username","");
        editor.putString(username + "edition", edition);
        editor.commit();
    }
    public static void setCurrentEdition(Context context, String username,String edition) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(username + "edition", edition);
        editor.commit();
    }

    public static String fromString(String s) {
        if (TextUtils.isEmpty(s) || ("null".equals(s)))
            return "";
        else
            return s;
    }
}
