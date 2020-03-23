package com.vgtech.vantop.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.vgtech.common.NetworkHelpers;
import com.vgtech.common.api.Error;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vantop.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhangshaofang on 2016/3/14.
 */
public class VanTopActivityUtils {
    public static boolean prehandleNetworkData(Context context, HttpListener<String> listener, int callbackId, NetworkPath path, RootData rootData, boolean needNotify) {
        JSONObject jsonObject = rootData.getJson();
        String code = "0";
        String _msg = "";
        if (jsonObject != null) {
            rootData.result = true;
            if (jsonObject.has("_code")) {
                try {
                    String c = jsonObject.getString("_code");
                    if (!code.equals(c)) {
                        rootData.result = false;
                        code = c;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (jsonObject.has("_msg")) {
                try {
                    _msg = jsonObject.getString("_msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        boolean result = rootData.isSuccess();
        if (!result) {
            com.vgtech.common.api.Error error = new Error();
            error.code = Integer.parseInt(code);
            error.msg = _msg;
            error.desc = _msg;
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity = getRootActivity(activity);
                if (activity != null) {
                    if (needNotify) {
                        if (!NetworkHelpers.isNetworkAvailable(context)) {
                            error.msg = context.getString(R.string.no_network);
                            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
                        } else {
                            if (!TextUtils.isEmpty(error.msg) && !code.equals("0")) {
                                Toast.makeText(context, error.msg, Toast.LENGTH_SHORT).show();
                            } else {
                                error.msg = context.getString(R.string.network_error);
                                Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean prehandleNetworkData1(Context context, HttpListener<String> listener, int callbackId, NetworkPath path, RootData rootData, boolean needNotify) {
        JSONObject jsonObject = rootData.getJson();
        String code = "200";
        String _msg = "";
        if (jsonObject != null) {
            rootData.result = true;
            if (jsonObject.has("code")) {
                try {
                    String c = jsonObject.getString("code");
                    rootData.code = Integer.parseInt(c);
                    if (!code.equals(c)) {
                        rootData.result = false;
                        code = c;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (jsonObject.has("msg")) {
                try {
                    _msg = jsonObject.getString("msg");
                    rootData.msg = _msg;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        boolean result = rootData.isSuccess();
        if (!result) {
            com.vgtech.common.api.Error error = new Error();
            error.code = Integer.parseInt(code);
            error.msg = _msg;
            error.desc = _msg;
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity = getRootActivity(activity);
                if (activity != null) {
                    if (needNotify) {
                        if (!NetworkHelpers.isNetworkAvailable(context)) {
                            error.msg = context.getString(R.string.no_network);
                            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
                        } else {
                            if (!TextUtils.isEmpty(error.msg) && !code.equals("0")) {
                                Toast.makeText(context, error.msg, Toast.LENGTH_SHORT).show();
                            } else {
                                error.msg = context.getString(R.string.network_error);
                                Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }
            }
        }
        return result;
    }

    public static Activity getRootActivity(Activity activity) {
        Activity parent = activity == null ? null : activity.getParent();
        if (parent != null) {
            return getRootActivity(parent);
        }
        return activity;
    }
}

