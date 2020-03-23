package com.vgtech.common.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.PackageUtil;
import com.google.gson.Gson;
import com.vgtech.common.BaseApp;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by gs on 2020-02-19.
 */
public class HttpUtils {

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(50000, TimeUnit.MILLISECONDS)
            .readTimeout(50000, TimeUnit.MILLISECONDS)
            .writeTimeout(50000, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true).build();

    public static void load(final Activity context, final int callbackId, final NetworkPath path, final HttpView httpListener) {


        MediaType mediaType = MediaType.parse("application/json");
        Map<String, String> postValues = path.getPostValues();
        BaseApp vanCloudApplication = (BaseApp) BaseApp.getAppContext();
        final String url = vanCloudApplication.getApiUtils().appendSignInfo(path.getUrl());
        StringBuilder encodedParams = new StringBuilder();
        if (postValues != null) {
            encodedParams.append(url);
            encodedParams.append("&");

        }
        for (Map.Entry<String, String> entry : postValues.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            encodedParams.append(key);
            encodedParams.append('=');
            encodedParams.append(TextUtils.isEmpty(value) ? "" : value);

            encodedParams.append('&');

        }
        Log.e("TAG_httpUtils", "encodedParams=" + encodedParams.toString());
        String toJson = new Gson().toJson(postValues);
        Log.e("TAG_httpUtils", "toJson=" + toJson);
        RequestBody body = RequestBody.create(mediaType, new byte[0]);
        String token = PrfUtils.getToken(BaseApp.getAppContext());
        Request.Builder request = new Request.Builder()
                .url(encodedParams.toString())
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", TextUtils.isEmpty(token)?"":token)
                .addHeader(URLAddr.URL_PARAM_OID,PrfUtils.getUserId(vanCloudApplication));

        String language = PrfUtils.getAppLanguage(BaseApp.getAppContext());
        DisplayMetrics d = BaseApp.getAppContext().getResources().getDisplayMetrics();
        int screenHeight = d.heightPixels;
        int screenWidth = d.widthPixels;
        int dpi = d.densityDpi;
        String sdk = Build.VERSION.SDK;//操作系统版本
        String model = Build.MODEL;//设备名称
//        Log.e("TAG_GetuiSdkDemo", "getDeviceId----");
        String deviceId = ApiUtils.getDeviceId(BaseApp.getAppContext());//设备标示
        request.addHeader("language", language)//语言
        .addHeader("Lang", TextUtils.isEmpty(language)?"":language)//语言
        .addHeader("device_id", deviceId)//设备唯一标示
        .addHeader(URLAddr.URL_PARAM_OID, PrfUtils.getUserId(BaseApp.getAppContext()))//login userid
        .addHeader("device_model", TextUtils.isEmpty(model)?"":model)//设备型号
        .addHeader("screen_width", String.valueOf(screenWidth))//屏幕宽度
                 .addHeader("screen_height", String.valueOf(screenHeight))//屏幕高度
                .addHeader("dpi", String.valueOf(dpi))//屏幕密度
                .addHeader("osver", sdk);//操作系统版本

        PackageManager packageManager = BaseApp.getAppContext().getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(BaseApp.getAppContext().getPackageName(), 0);
            request.addHeader("apiversion",info.versionName==null?"0.0.0":info.versionName);//api_version
            int versionCode = info.versionCode;
            request.addHeader("vercode", String.valueOf(versionCode));//应用版本号
        } catch (Exception e) {
        }
        ApplicationProxy vgCloudApplication = (ApplicationProxy) BaseApp.getAppContext().getApplicationContext();
        String channelId = vgCloudApplication.getChannelId();
        request.addHeader("channel", channelId);//应用渠道

        Request build = request.build();

        Call call = okHttpClient.newCall(build);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("TAG_httpUtils", "onFailure=" + e.toString());


                if (httpListener != null) {
                    httpListener.onFailure(callbackId, e.toString());
                }
            }

            @Override
            public void onResponse(Call call, final Response succeed) throws IOException {
                final String response = succeed.body().string();
                Log.e("TAG_httpUtils", "onResponse=" + response);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                if (Constants.DEBUG) {
//                    if (response.length() > logNum) {
//                        for (int i = 0; i < response.length(); i += logNum) {
//                            if (i + logNum < response.length())
//                                Log.e("TAG_成功", "第"+(i/logNum)+"段log===" + response.substring(i, i + logNum));
//                            else {
//                                Log.e("TAG_成功", "第"+(i/logNum)+"段log===" + response.substring(i, response.length()));
//                            }
//                        }
//                    } else {
//                        Log.e("TAG_成功", "result=" + response);
//                    }
//                    Log.e("TAG_成功", "path=" + mPath.getPath());
//                }
                            RootData rootData = null;
                            boolean success = false;
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (path.isVantop()) {
                                    rootData = new RootData();
                                } else {
                                    rootData = JsonDataFactory.getData(jsonObject);
                                }
                                rootData.setJson(jsonObject);
                            } catch (Exception e) {
                                rootData = new RootData();
                                if (path.getType() == NetworkPath.TYPE_JSONARRAY) {
                                    try {
                                        JSONArray jsonArray = new JSONArray(response);
                                        success = true;
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                rootData.msg = e.getMessage();
                                rootData.responce = response;
                            } finally {
    //                    if (mCache != null && (rootData.isSuccess() || success) && path.isCache()) {
    //                        if (mPath.getType() == NetworkPath.TYPE_JSONARRAY) {
    //                            mCache.put(mPath.getPath(), response);
    //                        } else {
    //                            mCache.put(mPath.getPath(), rootData.getJson());
    //                        }
    //                    }

                                if (rootData != null && rootData.getJson() != null) {
                                    try {
                                        boolean isencode = rootData.getJson().optBoolean("isencode");
                                        if (isencode) {
                                            String encrypStr = rootData.getJson().optString("data");
                                            String staff_no = PrfUtils.getStaff_no(PackageUtil.getAppCtx());
                                            Des3Util des3Util = new Des3Util();
                                            des3Util.setSecretKey(staff_no);
                                            String data = des3Util.decode(encrypStr);
                                            //TODO code msg 需要添加
                                            rootData.setJson(new JSONObject("{data:" + data + "}"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


                            }

                            if (httpListener != null) {
                                int code = succeed.code();
                                if (code == 200) {
                                    httpListener.dataLoaded(callbackId, path, rootData);

                                } else {
                                    httpListener.onFailure(callbackId, succeed.message());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            httpListener.onFailure(callbackId, "请求异常");
                        }
                    }
                });
            }
        });
    }
}
