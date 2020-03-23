package com.vgtech.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

/**
 * Created by Duke on 2016/8/23.
 */
public class CommonUtils {

    public static final String ACTION_APPROVAL_PROCESS = "com.vgtech.vancloud.APPROVAL_PROCESS";


    // dip--px
    public static int convertDipOrPx(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为1,英文字符长度为0.5
     *
     * @return int 得到的字符串长度
     */
    public static int getTextLength(String s) {
        if (TextUtils.isEmpty(s))
            return 0;
        double valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (int i = 0; i < s.length(); i++) {
            // 获取一个字符
            String temp = s.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为1
                valueLength += 1;
            } else {
                // 其他字符长度为0.5
                valueLength += 0.5;
            }
        }
        //进位取整
        return (int) (Math.ceil(valueLength) + 0.5);
    }

    /**
     * dp转px
     *
     * @param resources
     * @param dps
     * @return
     */
    public static int dpToPx(Resources resources, int dps) {
        return Math.round(resources.getDisplayMetrics().density * (float) dps);
    }
}
