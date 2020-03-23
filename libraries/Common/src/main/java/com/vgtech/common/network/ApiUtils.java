package com.vgtech.common.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.utils.MD5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by zhangshaofang on 2015/8/5.
 */
public class ApiUtils {

    private static List<String> ignoreVersionUrl = null;

    /**
     * 以下url与host之间不需要拼接version
     */
    static {
        ignoreVersionUrl = new ArrayList<>();
        ignoreVersionUrl.add(URLAddr.URL_IMAGE);
        ignoreVersionUrl.add(URLAddr.URL_AUDIO);
        ignoreVersionUrl.add(URLAddr.URL_ATTACHMENT);
        ignoreVersionUrl.add(URLAddr.URL_EWM);
        ignoreVersionUrl.add(URLAddr.URL_BG_INVEST_INDEX);
        ignoreVersionUrl.add(URLAddr.URL_BG_INVEST_RESULT);
        ignoreVersionUrl.add(URLAddr.URL_BG_INVEST_RECORD_LIST);
        ignoreVersionUrl.add(URLAddr.URL_BG_INVEST_MYRECORD_LIST);
        ignoreVersionUrl.add(URLAddr.URL_BG_SEARCH);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_CALENDAR);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_CHAT);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_NOTICE);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_TASK);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_TOPIC);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_WORKFLOW);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_WORKREPORT);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_RECRUIT);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_FINANCE);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_BEIDIAO);
        ignoreVersionUrl.add(URLAddr.URL_ASSIST_MEETING);
    }

    private Context mContext;
    private final Random mRandom;

    private Map<String, String> mSignParams;

    public static String serviceHost = null;

    /**
     * 获取登录返回host
     *
     * @param context
     * @return
     */
    public static String getHost(Context context) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        return preferences.getString("service_host", getLocalHost(context));
    }

    /**
     * 获取本地路径
     *
     * @param context
     * @return
     */
    private static String getLocalHost(Context context) {
        String host = PrfUtils.getPrfparams(context, "host");
        String port = PrfUtils.getPrfparams(context, "port");
        if (!Constants.DEBUG || TextUtils.isEmpty(host) || TextUtils.isEmpty(port)) {
            host = URLAddr.IP;
            port = URLAddr.PORT;
        }
        String scheme = PrfUtils.getPrfparams(context, "scheme", URLAddr.SCHEME);
        String serviceHost = scheme + "://" + host + "/";
        if (!"80".equals(port)) {
            serviceHost = scheme + "://" + host + ":" + port + "/";
        }
        return serviceHost;
//        return "http://192.168.2.77:8085";
    }

    public static String generatorUrl(Context context, String url) {
        serviceHost = getHost(context);
        return appendUrl(context, serviceHost, url, true);
    }

    public static String generatorUrl(Context context, String url, boolean addVersion) {
        serviceHost = getHost(context);
        return appendUrl(context, serviceHost, url, addVersion);
    }

    /**
     * 拼接本地url  无需登录验证
     *
     * @param context
     * @param url
     * @return
     */
    public static String generatorLocalUrl(Context context, String url) {
        return appendUrl(context, getLocalHost(context), url, true);
    }

    private static int mAppVersion = 0;

    public static int getAppVersion(Context context) {
        if (mAppVersion == 0) {
            PackageManager packageManager = context.getPackageManager();
            try {
                PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
                mAppVersion = info.versionCode;
            } catch (Exception e) {
            }
        }
        return mAppVersion;
    }

    public static String addAppVersion(Context context, String path) {
        int version = getAppVersion(context);
        return getHost(context) + "v" + version + "/" + path;
    }

    public static String appendUrl(Context context, String host, String path, boolean addVersion) {
        String url = "";
        if (!addVersion || ignoreVersionUrl.contains(path)) {
            url = host + path;
        } else {
            int version = getAppVersion(context);
            if (path.contains("%")) {

                path = String.format(path, version);

                url = host + path;
            } else {
                url = host + path;
            }

        }
        return url;
    }

    public ApiUtils(Context context) {
        mContext = context;
        mRandom = new Random();
        mSignParams = new HashMap<String, String>();
        ensureSignInfo();
    }

    public Map<String, String> getSignParams() {
        return mSignParams;
    }

    public String getTenantId() {
        return PrfUtils.getTenantId(mContext);
    }

    public void setLanguage(String language) {
        mSignParams.put("language", language);//语言
        mSignParams.put("Lang", language);//语言
    }

    public void ensureSignInfo() {

        String language = PrfUtils.getAppLanguage(mContext);
        DisplayMetrics d = mContext.getResources().getDisplayMetrics();
        int screenHeight = d.heightPixels;
        int screenWidth = d.widthPixels;
        int dpi = d.densityDpi;
        String sdk = Build.VERSION.SDK;//操作系统版本
        String model = Build.MODEL;//设备名称
//        Log.e("TAG_GetuiSdkDemo", "getDeviceId----");
        String deviceId = getDeviceId(mContext);//设备标示
        mSignParams.put("language", language);//语言
        mSignParams.put("Lang", language);//语言
//        mSignParams.put("local", local);//时区
        mSignParams.put("device_id", deviceId);//设备唯一标示
        mSignParams.put(URLAddr.URL_PARAM_OID, PrfUtils.getUserId(mContext));//login userid
        mSignParams.put("device_model", model);//设备型号
        mSignParams.put("screen_width", String.valueOf(screenWidth));//屏幕宽度
        mSignParams.put("screen_height", String.valueOf(screenHeight));//屏幕高度
        mSignParams.put("dpi", String.valueOf(dpi));//屏幕密度
        mSignParams.put("osver", sdk);//操作系统版本

        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            mSignParams.put("apiversion",info.versionName==null?"0.0.0":info.versionName);//api_version
//            mSignParams.put("apiversion", Constants.API_VERSION);//api_version
            int versionCode = info.versionCode;
            mSignParams.put("vercode", String.valueOf(versionCode));//应用版本号
        } catch (Exception e) {
        }
        ApplicationProxy vgCloudApplication = (ApplicationProxy) mContext.getApplicationContext();
        String channelId = vgCloudApplication.getChannelId();
        mSignParams.put("channel", channelId);//应用渠道

    }


    public String appendSignInfo(String url) {
//        final String uid = PrfUtils.getUserId(mContext);
//        final String token = PrfUtils.getToken(mContext);
        Uri.Builder builder = Uri.parse(url).buildUpon();
//        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(token)) {
//            builder.appendQueryParameter("ownId", String.valueOf(uid)).appendQueryParameter("token", token);
//        }
//        if (TextUtils.isEmpty(Uri.parse(url).getQueryParameter("tenantId"))) {
//            builder.appendQueryParameter("tenantId", getTenantId());
//        }
        //  builder.appendQueryParameter("nonce", String.valueOf(mRandom.nextInt())).appendQueryParameter("timestamp", String.valueOf(System.currentTimeMillis()));
        Iterator<String> keyIterator = mSignParams.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = (String) keyIterator.next();
            if (TextUtils.isEmpty(Uri.parse(url).getQueryParameter(key))) {
                builder.appendQueryParameter(key, mSignParams.get(key));
            }
        }

        return builder.build().toString();
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        //android.telephony.TelephonyManager
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (mTelephony.getDeviceId() != null) {
                tmDevice = mTelephony.getDeviceId();
            } else {
                //android.provider.Settings;
                tmDevice = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            String deviceId = deviceUuid.toString();
//            Log.e("TAG_GetuiSdkDemo","DeviceId="+deviceId);
            String enDeviceId = MD5.getMD5(deviceId);
//            Log.e("TAG_GetuiSdkDemo","enDeviceId="+enDeviceId);
            return enDeviceId;
        } catch (Exception e) {
            e.printStackTrace();
//            Log.e("TAG_GetuiSdkDemo","DeviceId=Exception");
        }
      return "android";
    }

    public static String getSign(Map<String, String> paramMap, String pwd) {
        String[] keyArray = paramMap.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keyArray) {
            stringBuilder.append(key.toLowerCase()).append("=").append(paramMap.get(key)).append("&");
        }
        if (stringBuilder.length() > 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        if (!TextUtils.isEmpty(pwd))
            stringBuilder.append(pwd);
        String codes = stringBuilder.toString();
        Log.e("TAG_工资","codes="+codes);
        return MD5.getMD5(codes);
    }
}
