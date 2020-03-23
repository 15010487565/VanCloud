package com.vgtech.vancloud.utils;

import android.content.Context;

import com.vgtech.vancloud.R;

/**
 * Created by zhangshaofang on 2015/9/7.
 */
public class ConfigUtils {
    public static String getTipValue(Context context, int typeId) {
        String array[] = context.getResources().getStringArray(R.array.tip_type);
        return array[typeId];
    }
}
