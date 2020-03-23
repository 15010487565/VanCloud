package com.vgtech.vantop.utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ScaleXSpan;

/**
 * Created by gs on 2020-03-02.
 * xcd
 */
public class AlignedTextUtils {
    private static int n = 0;// 原Str拥有的字符个数
    private static SpannableString spannableString;
    private static double multiple = 0;// 放大倍数

    /**
     * 对显示的字符串进行格式化 比如输入：出生年月 输出结果：出正生正年正月
     */
    public static String formatStr(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        str = ToSBC(str);
        n = str.length()-1;
        if (n >= 4) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        for (int i = n - 1; i > 0; i--) {
            sb.insert(i, "正");
        }
        return sb.toString();
    }

    /**
     * 对显示字符串进行格式化 比如输入：安正卓正机正器正人 输出结果：安 卓 机 器 人
     *
     * @param str
     * @return     */
    public static SpannableString formatText(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        str = formatStr(str);
        if (str.length() <= 4) {
            return null;
        }
        spannableString = new SpannableString(str);
        switch (n) {

            case 3:
                multiple = 0.3333333333333333333333333333333333;
                break;

            default:
                break;
        }
        for (int i = 1; i < str.length()-1; i = i + 2)
        {
            spannableString.setSpan(new RelativeSizeSpan((float) multiple), i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(Color.TRANSPARENT), i, i + 1,  Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }
    /**
     转全角的方法(SBC case)<br/><br/>
     全角空格为12288，半角空格为32
     其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * @param input 任意字符串
     * @return 半角字符串
     *
     */
    public static String ToSBC(String input)
    {
        //半角转全角：
        char[] c=input.toCharArray();
        for (int i = 0; i < c.length; i++)
        {
            if (c[i]==32)
            {
                c[i]=(char)12288;
                continue;
            }
            if (c[i]<127)
                c[i]=(char)(c[i]+65248);
        }
        return new String(c);
    }
    public static SpannableStringBuilder justifyString(String str, int num) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (TextUtils.isEmpty(str)) {
            return spannableStringBuilder;
        }
        char[] chars = str.toCharArray();
        if (chars.length >= num || chars.length == 1) {
            return spannableStringBuilder.append(str);
        }
        int l = chars.length;
        float scale = (float) (num - l) / (l - 1);
        for (int i = 0; i < l; i++) {
            spannableStringBuilder.append(chars[i]);
            if (i != l - 1) {
                SpannableString s = new SpannableString("　");//全角空格
                s.setSpan(new ScaleXSpan(scale), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.append(s);
            }
        }
        return spannableStringBuilder;
    }

}
