package com.vgtech.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vgtech.common.utils.MD5;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by zhangshaofang on 2015/8/6.
 */
public class PrfUtils {
    private static SharedPreferences mSharePreferences;

    public static SharedPreferences getSharePreferences(Context context) {
//        if (mSharePreferences == null) {
//        Context.MODE_WORLD_READABLE替换成Context.MODE_PRIVATE
        mSharePreferences = BaseApp.getAppContext().getSharedPreferences(BaseApp.getAppContext().getPackageName() + "_preferences", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
//            mSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        }
        return mSharePreferences;
    }

    public static void savePrfparams(Context context, String key, String value) {
        SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getStaff_no(Context context) {
//        return "D0001";
        return PrfUtils.getPrfparams(BaseApp.getAppContext(), "user_no");
    }

    public static String getVantopServiceHost(Context context) {
        return PrfUtils.getPrfparams(BaseApp.getAppContext(), "vantop_service_host");
    }

    public static String getPrfparams(Context context, String key) {
        SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        return preferences.getString(key, null);
    }

    public static boolean isChineseForAppLanguage(Context context) {
        return "zh".equals(getAppLanguage(BaseApp.getAppContext()));
    }

    public static String getAppLanguage(Context context) {
        String language = PrfUtils.getPrfparams(BaseApp.getAppContext(), "is_language");
        if (TextUtils.isEmpty(language)) {
            if (BaseApp.getAppContext().getResources().getConfiguration().locale.getLanguage().equals("zh")) {
                language = "zh";
            } else {
                language = "en";
            }
        }
        return language;
    }

    public static String getPrfparams(Context context, String key, String defValue) {
        SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        return preferences.getString(key, defValue);
    }

    public static void setWorkReportTempleate(Context context, String templeate) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("templeate", templeate);
        editor.commit();
    }

    public static String getWorkReportTempleate(Context context) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("templeate", null);
    }

    public static String getUserId(Context context) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("uid", null);
    }

   /* public static String getServiceHost(Context context) {
        return ApiUtils.getHost(context);
    }*/

    public static String getToken(Context context) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("token", null);
    }

    public static String getTenantId(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("tenantId", null);
    }

    /**
     * 获取首页快捷方式类型
     *
     * @param context
     * @return
     */
    public static String gethomeShortcutType(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("homeShortcutType", null);
    }

    /**
     * 设置首页快捷方式类型
     *
     * @param context
     * @param homeShortcutType
     */
    public static void sethomeShortcutType(Context context, String homeShortcutType) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("homeShortcutType", homeShortcutType);
        editor.commit();
    }


    public static final String RECRUIT_COUNT = "RECRUIT_COUNT";

    /**
     * 统计推送招聘职位推送简历数量
     *
     * @param context
     * @param recruitId 招聘职位or招聘计划id
     * @param revert    是否重置
     */
    public static void countRecruit(Context context, String recruitId, boolean revert) {
        String recruitCount = PrfUtils.getPrfparams(BaseApp.getAppContext(), RECRUIT_COUNT);
        JSONObject jsonObject = null;
        try {
            if (!TextUtils.isEmpty(recruitCount)) {
                jsonObject = new JSONObject(recruitCount);
            } else {
                jsonObject = new JSONObject();
            }
            int count = 1;
            if (jsonObject.has(recruitId)) {
                count = jsonObject.getInt(recruitId);
                count += 1;
                jsonObject.put(recruitId, count);
            }
            jsonObject.put(recruitId, revert ? 0 : count);
            PrfUtils.savePrfparams(BaseApp.getAppContext(), RECRUIT_COUNT, jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取推送简历总数
     *
     * @param context
     * @return
     */
    public static int getRecruitCount(Context context) {
        String recruitCount = PrfUtils.getPrfparams(BaseApp.getAppContext(), RECRUIT_COUNT);
        int count = 0;
        if (!TextUtils.isEmpty(recruitCount)) {
            try {
                JSONObject jsonObject = new JSONObject(recruitCount);
                Iterator<?> keyIterator = jsonObject.keys();
                while (keyIterator.hasNext()) {
                    String key = (String) keyIterator.next();
                    int num = jsonObject.getInt(key);
                    count += num;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    /**
     * 获取  推送统计招聘职位推送简历数量
     *
     * @param context
     * @param recruitId 招聘职位or招聘计划id
     */
    public static int getRecruitCount(Context context, String recruitId) {
        int count = 0;
        String recruitCount = PrfUtils.getPrfparams(BaseApp.getAppContext(), RECRUIT_COUNT);
        try {
            if (!TextUtils.isEmpty(recruitCount)) {
                JSONObject jsonObject = new JSONObject(recruitCount);
                if (jsonObject.has(recruitId)) {
                    count = jsonObject.getInt(recruitId);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 设置首次登录
     *
     * @param context
     */
    public static void setFirstLogin(Context context, boolean type) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("FirstLogin", type);
        editor.commit();
    }
    /**
     * 获取是否首次登录
     *
     * @param context
     * @return
     */
    public static boolean getFirstLogin(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getBoolean("FirstLogin", true);
    }
    public static void setUpdateTipFlag(Context context, boolean type) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("UpdateTipFlag", type);
        editor.commit();
    }

    public static boolean getUpdateTipFlag(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getBoolean("UpdateTipFlag", false);
    }
    public static void setAgreementFlag(boolean type) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("AgreementFlag", type);
        editor.commit();
    }

    public static boolean getAgreementFlag() {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getBoolean("AgreementFlag", false);
    }
    /**
     * 是否为执行人
     *
     * @param context
     * @param type
     */
    public static void setExecutor(Context context, boolean type) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isExecutor", type);
        editor.commit();
    }

    public static boolean getExecutor(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getBoolean("isExecutor", true);
    }

    public static void setResumeId(Context context, String resumeid) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("resume_id", resumeid).commit();
    }

    public static String getResumeId(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("resume_id", "");
    }

    public static void setUserPhone(Context context, String mobile) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("login_user_mobile", mobile).commit();
    }

    public static String getUserPhone(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("login_user_mobile", "");
    }


    public static String getFirstNotifyId(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("FirstNotifyId", null);
    }


    public static void setFirstNotifyId(Context context, String notifyid) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("FirstNotifyId", notifyid);
        editor.commit();
    }


    public static void saveAllOrderTypes(Context context, String orderTypeJson) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("OrderTypeJson", orderTypeJson);
        editor.commit();
    }


    public static String getAllOrderTypes(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("OrderTypeJson", null);
    }

    /**
     * 保存背景调查推送数字
     *
     * @param context
     * @param count   （增加的数量，传0时把数量清0）
     */
    public static void saveInvestigationCount(Context context, int count) {

        int investigationCount = getInvestigationCount(BaseApp.getAppContext());
        investigationCount = investigationCount + count;
        if (investigationCount < 0) {
            investigationCount = 0;
        }
        if (count == 0) {
            investigationCount = 0;
        }
        SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Investigation", investigationCount + "");
        editor.commit();
    }

    /**
     * 获取背景调查推送数量。
     *
     * @param context
     * @return
     */
    public static int getInvestigationCount(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        String s = preferences.getString("Investigation", null);
        if (TextUtils.isEmpty(s)) {
            return 0;
        } else {
            return Integer.valueOf(s);
        }
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        return preferences.getString("username", "");
    }

    /**
     * 设置审批搜索记录
     *
     * @param context
     * @param type    1：我审批的待审批。2:我审批的已审批。3：我发起的。4：抄送我的
     * @param list
     */
    public static void setApprovalSearchLog(Context context, String type, List<String> list) {
        String key = getTenantId(BaseApp.getAppContext()) + getUserId(BaseApp.getAppContext()) + "approval_search_log" + type;
        PrfUtils.savePrfparams(BaseApp.getAppContext(), MD5.getMD5(key), new Gson().toJson(list));
    }


    /**
     * 设置审批搜索记录
     *
     * @param context
     * @param type    1：我审批的待审批。2:我审批的已审批。3：我发起的。4：抄送我的
     * @param keyword
     */
    public static void setApprovalSearchLog(Context context, String type, String keyword) {
        List<String> list = getApprovalSearchLog(BaseApp.getAppContext(), type);
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s.equals(keyword)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            list.remove(index);
        }
        list.add(0, keyword);
        String key = getTenantId(BaseApp.getAppContext()) + getUserId(BaseApp.getAppContext()) + "approval_search_log" + type;
        PrfUtils.savePrfparams(BaseApp.getAppContext(), MD5.getMD5(key), new Gson().toJson(list));
    }

    /**
     * 清除审批搜索记录
     *
     * @param context
     * @param type    1：我审批的待审批。2:我审批的已审批。3：我发起的。4：抄送我的
     */
    public static void clearApprovalSearchLog(Context context, String type) {
        String key = getTenantId(BaseApp.getAppContext()) + getUserId(BaseApp.getAppContext()) + "approval_search_log" + type;
        PrfUtils.savePrfparams(BaseApp.getAppContext(), MD5.getMD5(key), "");
    }

    /**
     * 获取审批搜索记录
     *
     * @param context
     * @param type    1：我审批的待审批。2:我审批的已审批。3：我发起的。4：抄送我的
     * @return
     */
    public static List<String> getApprovalSearchLog(Context context, String type) {
        String key = getTenantId(BaseApp.getAppContext()) + getUserId(BaseApp.getAppContext()) + "approval_search_log" + type;
        String keywords = PrfUtils.getPrfparams(BaseApp.getAppContext(), MD5.getMD5(key));
        List<String> lists = new ArrayList<>();
        if (!TextUtils.isEmpty(keywords)) {
            lists = new Gson().fromJson(keywords, new TypeToken<List<String>>() {
            }.getType());
        }
        return lists;
    }

    public static final String MESSAGE_TODO = "MESSAGE_TODO";
    public static final String MESSAGE_NOTICE = "MESSAGE_NOTICE";
    public static final String MESSAGE_GONGGAO = "MESSAGE_GONGGAO";
    public static final String MESSAGE_MSG = "MESSAGE_MSG";
    public static final String MESSAGE_COMMENT = "MESSAGE_COMMENT";

    public static int getMessageCount(Context context, String msgType) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        String tenantId = preferences.getString("tenantId", null);
        String uid = preferences.getString("uid", null);
        String key = msgType + tenantId + "_" + uid;
        return preferences.getInt(key, 0);
    }

    public static int getMessageCount(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(BaseApp.getAppContext());
        String tenantId = preferences.getString("tenantId", null);
        String uid = preferences.getString("uid", null);
        int todoCount = preferences.getInt(MESSAGE_TODO + tenantId + "_" + uid, 0);
        int noticeCount = preferences.getInt(MESSAGE_NOTICE + tenantId + "_" + uid, 0);
        int gonggaoCount = preferences.getInt(MESSAGE_GONGGAO + tenantId + "_" + uid, 0);
        int msgCount = preferences.getInt(MESSAGE_MSG + tenantId + "_" + uid, 0);
        return todoCount + noticeCount + gonggaoCount + msgCount;
    }

    public static void setMessageCountCount(Context context, String msgType, int unm) {
        final SharedPreferences preferences = getSharePreferences(BaseApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        String tenantId = preferences.getString("tenantId", null);
        String uid = preferences.getString("uid", null);
        String key = msgType + tenantId + "_" + uid;
        editor.putInt(key, unm).apply();
        editor.commit();
    }


    public static Context setLocal(Context context) {
        return updateResources(context, getSetLanguageLocale(context));
    }

    private static Context updateResources(Context context, Locale locale) {
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }


    /**
     * 获取选择的语言设置 * * @param context * @return
     */
    public static Locale getSetLanguageLocale(Context context) {
        int selectLanguage = PrfUtils.getInstance(context).getSelectLanguage();
//        Log.e("TAG_切换语言","selectLanguage="+selectLanguage);
        switch (selectLanguage) {
            case 0:
//                return getSystemLocale(context);
            case 1:
                return Locale.CHINA;
            case 2:
                return Locale.TAIWAN;
            case 3:
            default:
                return Locale.ENGLISH;
        }
    }

    /**
     * 获取系统的locale * * @return Locale对象
     */
    public static Locale getSystemLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale;
    }


    private final String SP_NAME = "language_setting";
    private final String TAG_LANGUAGE = "language_select";
    private static volatile PrfUtils instance;
    private final SharedPreferences mSharedPreferences;

    public PrfUtils(Context context) {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void saveLanguage(int select) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(TAG_LANGUAGE, select);
        edit.commit();
    }

    public int getSelectLanguage() {
        return mSharedPreferences.getInt(TAG_LANGUAGE, 0);
    }

    public static PrfUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (PrfUtils.class) {
                if (instance == null) {
                    instance = new PrfUtils(context);
                }
            }
        }
        return instance;
    }

}
