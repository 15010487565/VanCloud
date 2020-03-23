package com.vgtech.vancloud.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.igexin.sdk.PushManager;
import com.vgtech.common.BaseApp;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.LeaveInfo;
import com.vgtech.common.api.RecruitmentInfoBean;
import com.vgtech.common.api.ResumeBuyBean;
import com.vgtech.common.api.TemplateItem;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.utils.PublishConstants;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.service.SubmitService;
import com.vgtech.vancloud.ui.BaseActivity;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangshaofang on 2015/8/26.
 */
public class Utils {
    static Context context;
    private static volatile Utils instance;
    public static Utils getInstance(Context context) {
        Utils.context = context;
        if (instance == null) {
            synchronized (Utils.class) {
                if (instance == null) {
                    instance = new Utils();
                }
            }
        }
        return instance;
    }

    public static int getPublishTypeResId(int publishType) {
        int resId = 0;
        switch (publishType) {
            case PublishConstants.PUBLISH_APPLYBUYRESUME:
                resId = R.string.lable_resume_apply;
                break;
            case PublishConstants.PUBLISH_RECRUIT_FINISH:
                resId = R.string.recruit_ok;
                break;
            case PublishConstants.PUBLISH_TASK:
                resId = R.string.lable_task;
                break;
            case PublishConstants.PUBLISH_SCHEDULE:
                resId = R.string.lable_schedule;
                break;
            case PublishConstants.PUBLISH_HELP:
                resId = R.string.lable_helper;
                break;
            case PublishConstants.PUBLISH_SHARED:
                resId = R.string.lable_shared;
                break;
            case PublishConstants.PUBLISH_WORKREPORT:
                resId = R.string.lable_report;
                break;
            case PublishConstants.PUBLISH_FLOW:
                resId = R.string.title_flow_approve;
                break;
            case PublishConstants.PUBLISH_FLOW_LEAVE:
                resId = R.string.title_flow_leave;
                break;
            case PublishConstants.PUBLISH_ANNOUNCEMENT:
                resId = R.string.lable_send_announcement;
                break;
            case PublishConstants.PUBLISH_COMMENT:
                resId = R.string.comment;
                break;
            case PublishConstants.PUBLISH_WORK_REPORT:
                resId = R.string.lable_report_dianping;
                break;
            case PublishConstants.PUBLISH_TASK_CONDUCT:
                resId = R.string.task_conduct;
                break;
            case PublishConstants.PUBLISH_SCHEDULE_CONDUCT:
                resId = R.string.schedule_conduct;
                break;
            case PublishConstants.PUBLISH_RESUME_APPROVE:
            case PublishConstants.PUBLISH_RECRUIT_APPROVE:
            case PublishConstants.PUBLISH_FLOW_CONDUCT:
                resId = R.string.flow_conduct;
                break;
            case PublishConstants.PUBLISH_FORWARD:
                resId = R.string.forward;
                break;
        }
        return resId;
    }

    public static boolean isPersonalAppInstall(Context context, String packagename) {
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packagename))
                return true;
        }
        return false;
    }

    public static void startPersonalApp(Context context, String packagename) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packagename);
        context.startActivity(intent);
    }

    public static Context getContext() {
        return BaseApp.getAppContext();
    }

    public static Resources getResources() {
        return getContext().getResources();
    }

//    //从string.xml中获取字符串
//    public static String getString(int stringId) {
//        //上下文环境获取资源文件夹
//        return getResources().getString(stringId);
//    }
    //从string.xml中获取字符串
    public static String getString(Context context,int stringId) {
        //上下文环境获取资源文件夹
        return context.getResources().getString(stringId);
    }

    //通过资源文件id获取图片对象
    public static Drawable getDrawable(int drawableID) {
        return getResources().getDrawable(drawableID);
    }

    //添加string类型数组的方法
    public static String[] getStringArray(int stringArrayId) {
        return getResources().getStringArray(stringArrayId);
    }

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT = "yyyy/MM/dd";
    public static final String DATE_TIME_FORMAT_HOUR = "HH:mm";
    private final static String EMAIL_PATTERN = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$";
    private final static String CARD_PATTERN = "^([0-9]{17}[0-9X]{1})|([0-9]{15})$";


    public static boolean isEmail(String mail) {
        return Pattern.matches(EMAIL_PATTERN, mail);
    }

    public static boolean isCard(String mail) {
        return Pattern.matches(CARD_PATTERN, mail);
    }

    public static String priceFormat(String price) {
        DecimalFormat nf = new DecimalFormat("0.00");
        String s = nf.format(Double.parseDouble(price));
        return s;
    }

    public static String priceFormat01(String price) {
        DecimalFormat nf = new DecimalFormat("#,##0.00");
        String s = nf.format(Double.parseDouble(price));
        return s;
    }

    // dip--px
    public static int convertDipOrPx(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static String dateFormat(long times) {
        if (times <= 0)
            return "";
        long current = System.currentTimeMillis();
        Date date = new Date(times);
        Date curr = new Date(current);
        String dateformat = DATE_TIME_FORMAT;
        int y = curr.getYear() - date.getYear();
        int m = curr.getMonth() - date.getMonth();
        int t = curr.getDate() - date.getDate();
        if (y == 0 && m == 0) {
            dateformat = "HH:mm";
        }
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);

        String Str = "";
        if (y == 0) {
            if (m == 0) {
                if (t == 0) {
                    Str = dateStr;
                } else if (t == 1) {
                    Str = getString(context,R.string.yesterday) + " " + dateStr;
                } else {
                    SimpleDateFormat dateformat2 = new SimpleDateFormat("MM-dd HH:mm");
                    Str = dateformat2.format(date);
                }
            } else {
                SimpleDateFormat dateformat2 = new SimpleDateFormat("MM-dd HH:mm");
                Str = dateformat2.format(date);
            }
        } else {
            Str = dateStr;
        }
        return Str;
    }

    /**
     * 计算 时长  xx天xx小时，精确小数点后一位
     *
     * @param context
     * @param start   开始时间
     * @param end     结束时间
     * @return
     */
    public static String getDuration(Context context, long start, long end) {
        long day = 24 * 60 * 60 * 1000;
        double hour = 60 * 60 * 1000;
        long duration = end - start;
        int d = (int) (duration / day);
        double h = (duration % day) / hour;
        DecimalFormat df = new DecimalFormat("#0.0");
        String hs = df.format(h);
        if (d > 0) {
            return context.getString(R.string.lable_duration_dayhour, d, hs);
        } else {
            return context.getString(R.string.lable_duration_hour, hs);
        }
    }

    public static String dateFormatNoYesterday(long times) {
        long current = System.currentTimeMillis();
        Date date = new Date(times);
        Date curr = new Date(current);
        String dateformat = DATE_TIME_FORMAT;
        int y = curr.getYear() - date.getYear();
        int m = curr.getMonth() - date.getMonth();
        int t = curr.getDate() - date.getDate();
        if (y == 0 && m == 0) {
            dateformat = "HH:mm";
        }
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);

        String Str = "";
        if (y == 0) {
            if (m == 0) {
                if (t == 0) {
                    Str = dateStr;
                } else {
                    SimpleDateFormat dateformat2 = new SimpleDateFormat("MM-dd HH:mm");
                    Str = dateformat2.format(date);
                }
            } else {
                SimpleDateFormat dateformat2 = new SimpleDateFormat("MM-dd HH:mm");
                Str = dateformat2.format(date);
            }
        } else {
            Str = dateStr;
        }
        return Str;
    }


    public static final String VANTOP_DATE_TIME_FORMAT = "yyyy-MM-dd";
    public static final String VANTOP_MONTH_DAY_FORMAT = "MM-dd";

    public static String vantopDateFormat(long times) {
        long current = System.currentTimeMillis();
        Date date = new Date(times);
        Date curr = new Date(current);
        String dateformat = VANTOP_DATE_TIME_FORMAT;
        int y = curr.getYear() - date.getYear();
        int m = curr.getMonth() - date.getMonth();
        int t = curr.getDate() - date.getDate();

        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);
        String Str = "";

        if (y == 0 && m == 0) {
            if (t == 0) {
                Str = getString(context,R.string.today);
            } else if (t == 1) {
                Str = getString(context,R.string.yesterday);
            } else if (t == 2) {
                Str = getString(context,R.string.anteayer);
            } else {
                SimpleDateFormat dateformat2 = new SimpleDateFormat(VANTOP_DATE_TIME_FORMAT);
                Str = dateformat2.format(date);
            }
        } else {
            Str = dateStr;
        }

        return Str;
    }

    /**
     * 用在申请、审批时间上
     *
     * @param times
     * @return
     */
    public static String vantopDateFormat1(Context context,long times) {
        long current = System.currentTimeMillis();
        Date date = new Date(times);
        Date curr = new Date(current);
        String dateformat = VANTOP_DATE_TIME_FORMAT;
        int y = curr.getYear() - date.getYear();
        int m = curr.getMonth() - date.getMonth();
        int t = curr.getDate() - date.getDate();

//        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
//        String dateStr = dateformat1.format(date);
        String Str = "";

        if (y == 0) {
            if (m == 0) {
                if (t == 0) {
                    Str = getString(context,R.string.today);
                } else if (t == 1) {
                    Str = getString(context,R.string.yesterday);
                }else {
                    SimpleDateFormat dateformat2 = new SimpleDateFormat(VANTOP_MONTH_DAY_FORMAT);
                    Str = dateformat2.format(date);
                }
            } else {
                SimpleDateFormat dateformat2 = new SimpleDateFormat(VANTOP_MONTH_DAY_FORMAT);
                Str = dateformat2.format(date);
            }
        } else {
            SimpleDateFormat dateformat2 = new SimpleDateFormat(VANTOP_DATE_TIME_FORMAT);
            Str = dateformat2.format(date);
        }

        return Str;
    }

    /**
     * @param lo 毫秒数
     * @return String yyyy-MM-dd HH:mm:ss
     * @Description: long类型转换成日期
     */
    public static String longToDate(long lo) {
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sd.format(date);
    }
//
//    /**
//     * cache dir
//     * @param context
//     * @return
//     */
//    public static String getTempDirectoryPath(Context context) {
//        return context.getExternalFilesDir(null).getAbsolutePath();
//    }

    public static String dateFormatStr(long times) {
        Date date = new Date(times);
        String dateformat = DATE_TIME_FORMAT;
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);
        return dateStr;
    }

    public static String dateFormatDate(long times) {
        Date date = new Date(times);
        String dateformat = "yyyy-MM-dd";
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);
        return dateStr;
    }

    public static String dateFormatToDate(long times) {
        Date date = new Date(times);
        String dateformat = DATE_FORMAT;
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);
        return dateStr;
    }

    public static String dateFormatHour(long times) {
        Date date = new Date(times);
        String dateformat = DATE_TIME_FORMAT_HOUR;
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);
        return dateStr;
    }

    /**
     * 时间格式化
     *
     * @param times
     * @return
     */
    public static long dateFormat(String times) {
        DateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date date = null;
        try {
            date = sdf.parse(times);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static long dateFormat(String times, String format) {
        DateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(times);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static String dateFormat(long times, String format) {
        Date date = new Date(times);
        SimpleDateFormat dateformat1 = new SimpleDateFormat(format);
        String dateStr = dateformat1.format(date);
        return dateStr;
    }

    public static int convertDipOrPx(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    public static String[] getIds(String idstr) {
        String[] ids = null;
        if (!TextUtils.isEmpty(idstr)) {
            if (idstr.contains(",")) {
                ids = idstr.split("[,]");
            } else {
                ids = new String[]{idstr};
            }
        }
        return ids;
    }

    public static String dateFormatByString(String times) {

        String newTime = "";
        long time = 0;
        if (!TextUtils.isEmpty(times)) {
            time = Long.valueOf(times);
        }
        if (time > 0) {
            newTime = dateFormat(time);
        }
        return newTime;
    }

    public static String getDate(String times, SimpleDateFormat format) {
        Date date = null;
        try {
            date = format.parse(times);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime() + "";
    }


    /**
     * 汉字转换位汉语拼音首字母，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpell(String chines) {
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }

    public static void clearUserInfo(final Context context) {
        //个推解绑
//        unBindAlias(context);

        final SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("token");
        editor.remove("password");
        editor.remove("tenantId");
        editor.remove("homeShortcutType");
        editor.remove("message_alert_num");
        editor.commit();

        PushManager.getInstance().stopService(context);
        Intent intent = new Intent(context, SubmitService.class);
        context.stopService(intent);
    }

    public static void clearUserInfoBySwitchTenant(final Context context) {
        //个推解绑,切换分公司
        unBindAlias(context);

        final SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("token");
        editor.remove("tenantId");
        editor.remove("homeShortcutType");
        editor.remove("message_alert_num");
        editor.commit();

//        PushManager.getInstance().stopService(context);
        Intent intent = new Intent(context, SubmitService.class);
        context.stopService(intent);
    }

    /**
     * 个推解绑
     * @param context
     */
    public static void unBindAlias(final Context context) {
        Log.e("TAG_GetuiSdkService", "解绑别名=");
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            String localClassName = activity.getLocalClassName();
            Log.e("TAG_别名切换", "localClassName=" + localClassName);
        }
        VanCloudApplication vanCloudApplication = (VanCloudApplication) context.getApplicationContext();
        String deviceId = vanCloudApplication.getApiUtils().getSignParams().get("device_id");
        String alias = MD5.getMD5(PrfUtils.getUserId(context) + PrfUtils.getTenantId(context) + deviceId);
        PushManager.getInstance().unBindAlias(context, alias, true,"android");

    }
    /**
     * 汉字转换位汉语拼音，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToSpell(String chines) {
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }
//    public static String formatWorkReportContent(String content) {
//
//        String newContent = "";
//        if (!TextUtils.isEmpty(content)) {
//            if (content.contains("[")) {
//                try {
//                    List<TemplateItem> tList = JsonDataFactory.getDataArray(TemplateItem.class, new JSONArray(content));
//                    for (int i = 0; i < tList.size(); i++) {
//                        TemplateItem item = tList.get(i);
//                        if (i == 0) {
//                            newContent = newContent + item.title + "：" + "\n" + item.content;
//                        } else {
//                            newContent = newContent + "\n\n" + item.title + "：" + "\n" + item.content;
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                newContent = content;
//            }
//        }
//        return newContent;
//    }

    public static String formatHtmlWorkReportContent(String content) {

        String newContent = "";
        if (!TextUtils.isEmpty(content)) {
            if (content.contains("[")) {
                try {
                    List<TemplateItem> tList = JsonDataFactory.getDataArray(TemplateItem.class, new JSONArray(content));
                    for (int i = 0; i < tList.size(); i++) {
                        TemplateItem item = tList.get(i);
                        if (i == 0) {
                            newContent = newContent + item.title + "：<br>" + item.content;
                        } else {
                            newContent = newContent + "<br><br>" + item.title + "：<br>" + item.content;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                newContent = content;
            }
        }
        return newContent;
    }


    public static String formatHtmlLeaveInfoContent(Context context, String leaveinfo) {

        String newContent = "";
        if (!TextUtils.isEmpty(leaveinfo)) {
            if (leaveinfo.contains("[")) {
                try {
                    List<LeaveInfo> tList = JsonDataFactory.getDataArray(LeaveInfo.class, new JSONArray(leaveinfo));
                    for (int i = 0; i < tList.size(); i++) {
                        LeaveInfo leaveInfo = tList.get(i);
                        String value = "";
                        if (context.getResources().getString(R.string.start_time).equals(leaveInfo.key) || context.getResources().getString(R.string.end_time).equals(leaveInfo.key)) {
                            value = dateFormatByString(leaveInfo.value);
                        } else if (leaveInfo.key.contains(context.getResources().getString(R.string.lable_type))) {
                            value = PublishUtils.getLeaveType(context, Integer.valueOf(leaveInfo.value));
                        } else if (context.getResources().getString(R.string.leave_time_length).equals(leaveInfo.key)) {
                            value = leaveInfo.value + context.getResources().getString(R.string.hour);
                        } else {
                            value = leaveInfo.value;
                        }
                        if (i == 0) {
                            newContent = leaveInfo.key + "：" + value;
                        } else {
                            newContent = newContent + "<br>" + leaveInfo.key + "：" + value;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                newContent = "";
            }
        }
        return newContent;
    }

    public static boolean isPhoneNum(String value) {
        String regex = "^((13[0-9])|(15[0-9])|(17[0-9])|(14[0-9])|(18[0-9]))\\d{8}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(value);
        return m.find();
    }

//    public static String formatLeaveInfoContent(Context context, String leaveinfo) {
//
//        String newContent = "";
//        if (!TextUtils.isEmpty(leaveinfo)) {
//            if (leaveinfo.contains("[")) {
//                try {
//                    List<LeaveInfo> tList = JsonDataFactory.getDataArray(LeaveInfo.class, new JSONArray(leaveinfo));
//                    for (int i = 0; i < tList.size(); i++) {
//                        LeaveInfo leaveInfo = tList.get(i);
//                        String value = "";
//                        if (context.getResources().getString(R.string.start_time).equals(leaveInfo.key) || context.getResources().getString(R.string.end_time).equals(leaveInfo.key)) {
//                            value = dateFormatByString(leaveInfo.value);
//                        } else if (leaveInfo.key.contains(context.getResources().getString(R.string.lable_type))) {
//                            value = PublishUtils.getLeaveType(context, Integer.valueOf(leaveInfo.value));
//                        } else if (context.getResources().getString(R.string.leave_time_length).equals(leaveInfo.key)) {
//                            value = leaveInfo.value + context.getResources().getString(R.string.day);
//                        } else {
//                            value = leaveInfo.value;
//                        }
//                        if (i == 0) {
//                            newContent = leaveInfo.key + "：" + value;
//                        } else {
//                            newContent = newContent + "\n" + leaveInfo.key + "：" + value;
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                newContent = "";
//            }
//        }
//        return newContent;
//    }


    public static int getWindowWidth(Context context, int number) {
        DisplayMetrics dm = new DisplayMetrics();
        // 取得窗口属性
        ((BaseActivity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        // 窗口的宽度
        return dm.widthPixels / number;
    }

    public static int getWindowheight(Context context, int number) {
        DisplayMetrics dm = new DisplayMetrics();
        // 取得窗口属性
        ((BaseActivity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        // 窗口的宽度
        return dm.heightPixels / number;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i(context.getPackageName(), "此appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }


    public static Bitmap imageZoom(Bitmap bitMap) {
        if (bitMap == null)
            return null;
        //图片允许最大空间   单位：KB
        double maxSize = 32.00;
        //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        //将字节换成KB
        double mid = b.length / 1024;
        //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
        if (mid > maxSize) {
            //获取bitmap大小 是允许最大大小的多少倍
            double i = mid / maxSize;
            //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
            double lengthSide = bitMap.getWidth() > bitMap.getHeight() ? bitMap.getHeight() : bitMap.getWidth();
            bitMap = zoomImage(bitMap, lengthSide / Math.sqrt(i),
                    lengthSide / Math.sqrt(i));
        }
        return bitMap;
    }


    /**
     * 图片的缩放方法
     *
     * @param bgimage   ：源图片资源
     * @param newWidth  ：缩放后宽度
     * @param newHeight ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) newWidth,
                (int) newHeight, matrix, true);
        return bitmap;
    }

    /*获取选中的id*/
    public static String getKeys(HashMap<String, ResumeBuyBean> map) {
        StringBuffer buffer = new StringBuffer();

        Set<String> keySet = map.keySet();
        for (String keyName : keySet) {
            ResumeBuyBean item = map.get(keyName);
            buffer.append(item.resume_id).append(",");
        }
        if (buffer.toString().length() > 1) {
            return buffer.toString().substring(0, buffer.toString().length() - 1);
        } else {
            return buffer.toString().substring(0, buffer.toString().length());
        }
    }

    public static String getIds(HashMap<String, RecruitmentInfoBean> map) {
        StringBuffer buffer = new StringBuffer();

        Set<String> keySet = map.keySet();
        for (String keyName : keySet) {
            RecruitmentInfoBean item = map.get(keyName);
            buffer.append(item.job_id).append(",");
        }
        if (buffer.toString().length() > 1) {
            return buffer.toString().substring(0, buffer.toString().length() - 1);
        } else {
            return buffer.toString().substring(0, buffer.toString().length());
        }
    }

    public static String format(String format, String str) {
        if (TextUtils.isEmpty(str))
            str = "";
        return String.format(format, str);
    }

    /**
     * 官方dp转px
     *
     * @param resources
     * @param dps
     * @return
     */
    public static int sysDpToPx(Resources resources, int dps) {
        return Math.round(resources.getDisplayMetrics().density * (float) dps);
    }

}