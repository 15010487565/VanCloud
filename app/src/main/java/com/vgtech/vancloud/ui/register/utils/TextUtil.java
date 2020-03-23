package com.vgtech.vancloud.ui.register.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.vancloud.R;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Jackson on 2015/10/15.
 * Version : 1
 * Details :
 */
public class TextUtil {

    public static String getString(EditText et) {
        return et.getText().toString().trim();
    }

    public static boolean isEmpty(String... arr) {
        for (String st : arr) {
            if (TextUtils.isEmpty(st)) return true;
        }
        return false;
    }

    public static <T extends TextView> T isEmpty(T... arrView) {
        for (T view : arrView) {
            if (TextUtils.isEmpty(view.getText().toString())) {
                if (TextUtils.isEmpty(view.getHint()))
                    view.setHint(R.string.toast_data_cant_empty);
                return view;
            }
        }
        return null;
    }

    public static <T extends TextView> boolean isEmpty(Context ctx, T... arrView) {
        TextView emptyView = TextUtil.isEmpty(arrView);
        if (emptyView != null) {
            Toast.makeText(ctx, emptyView.getHint(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    /**
     * 将+86的加号去掉
     *
     * @param areaCode
     * @return
     */
    public static String formatAreaCode(String areaCode) {
        if (TextUtils.isEmpty(areaCode)) return "";
        if (areaCode.charAt(0) == '+') {
            return areaCode.substring(1);
        }
        return areaCode;
    }

    public static boolean isSame(String pwdNew, String pwdSecond) {
        return pwdNew.equals(pwdSecond);
    }

    /**
     * @param context
     * @param phone   手机号
     * @param isChina 是中国区号
     * @return true代表手机号可用，false代表不可用
     * 中国区号时：判断第一位是1，总位数为11位
     * 其他区号时：判断不为空
     */
    public static boolean isAvailablePhone(Context context, String phone, boolean isChina) {
        boolean isAvailablePhone = false;
        if (isChina) {
            String regexPhone = "^1\\d{10}$";
//            String regexPassword = "^((13[0-9])|(15[0-9])|(17[0-9])|(14[0-9])|(18[0-9]))\\d{8}$";
//              String regexPassword = "(13[0-9]|14[57]|15[012356789]|17[05678]|18[012356789])\\d{8}";
//            String regexPassword = "1\\d{10}";
            isAvailablePhone = phone.matches(regexPhone);
            if (!isAvailablePhone) {
                Toast.makeText(context, R.string.toast_please_submit_right_number, Toast.LENGTH_SHORT).show();
            }
        } else {
            isAvailablePhone = !TextUtils.isEmpty(phone);
            if (!isAvailablePhone) {
                Toast.makeText(context, R.string.toast_please_submit_right_number, Toast.LENGTH_SHORT).show();
            }
        }
        return isAvailablePhone;
    }

    /**
     * 判断 包含字母和数字大于6位
     *
     * @param str
     * @return
     */
    public static boolean isRulePass(String str) {
        /*String regexMath = "[A-Za-z]{6,}";
        String regexWord = "[0-9]{6,}";
        String regexMix = "[A-Za-z0-9]{6,}";
        boolean b = str.matches(regexMath);
        boolean c = str.matches(regexWord);
        boolean d = str.matches(regexMix);
        return !b && !c && d;*/
        int length = str.length();
        return length >= 6 && length <= 16;
    }

    public static boolean isChina(String areaCode) {
        return TextUtils.equals(areaCode, "86") || TextUtils.equals(areaCode, "+86");
    }

    /**
     * 判断密码是否符合要求
     *
     * @param pwdNew
     * @param pwdSecond
     * @return
     */
    public static boolean isAvailablePwd(Context context, String pwdNew, String pwdSecond) {
        if (TextUtils.isEmpty(pwdNew) || TextUtils.isEmpty(pwdSecond)) {
            Toast.makeText(context, R.string.toast_pwd_must_6_size, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isRulePass(pwdNew)) {
            Toast.makeText(context, R.string.toast_pwd_must_6_size, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isSame(pwdNew, pwdSecond)) {
            Toast.makeText(context, R.string.toast_pwd_is_same, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 判断密码是否符合要求
     *
     * @return
     */
    public static boolean isAvailablePwd(Context context, String password) {
        boolean isAvailablePwd = true;
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, R.string.toast_pwd_must_6_size, Toast.LENGTH_SHORT).show();
            isAvailablePwd = false;
        }
        if (!isRulePass(password)) {
            Toast.makeText(context, R.string.toast_pwd_must_6_size, Toast.LENGTH_SHORT).show();
            isAvailablePwd = false;
        }
        return isAvailablePwd;
    }

    /**
     * 用来拼接集合对象中的id字段 输出结果类似于： "123,321,321"
     *
     * @param list     集合
     * @param fileName Bean中要拼接的字段名 如 bean.name   这是可以传入 "name"
     * @return
     */
    public static <T> String splitJoint(List<T> list, String fileName) {
        if (list == null || list.size() == 0) return "";
        Field filed = null;
        StringBuilder sb = new StringBuilder();
        try {
            filed = list.get(0).getClass().getDeclaredField(fileName);
            for (T bean : list) {
                filed.setAccessible(true);
                String id = String.valueOf(filed.get(bean));
                filed.setAccessible(false);
                sb.append(id);
                sb.append(",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


    public static String deleSpace(String str) {
        return str.replaceAll(" ", "");
    }
}
