package com.vgtech.vancloud.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.vgtech.common.Constants;
import com.vgtech.common.NetworkHelpers;
import com.vgtech.common.api.Error;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.vancloud.R;

/**
 * Created by zhangshaofang on 2015/8/3.
 */
public class ActivityUtils {
    public static boolean prehandleNetworkData(final Context context, HttpListener<String> listener, int callbackId, NetworkPath path, RootData rootData, boolean needNotify) {
//        if (rootData != null && rootData.getJson() != null) {
//            FileUtils.writeString("\r\nActivityUtils -> prehandleNetworkData url = " + path.getUrl() + "\r\n");
//            FileUtils.writeString("\r\nActivityUtils -> prehandleNetworkData response json = " + rootData.getJson());
//        }
        boolean result = rootData.isSuccess();
        if (!result) {
            Error error = new Error();
            int code = rootData.getCode();
            String msg = rootData.getMsg();
            error.code = code;
            error.msg = msg;
            error.desc = msg;
            if (Constants.DEBUG) {
                String time = DateTimeUtil.getCurrentString("yyyy-MM-dd HH:mm:ss");
                FileUtils.writeString(context,"\r\n" + time + "    ActivityUtils -> prehandleNetworkData url = " + path.getUrl() + "\r\n");
            }
            if (context != null) {
                if (rootData.code == Constants.RESPONCE_CODE_UNLOGIN) {
                    Intent intent = new Intent();
                    intent.setAction("com.vgtech.vancloud.login");
                    intent.putExtra("logout", true);
                    intent.putExtra("logoutFrom", rootData.code);
                    intent.putExtra("params", path.getPath());
                    intent.putExtra("result", rootData.getJson().toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                    context.sendBroadcast(reveiverIntent);
                    return false;
                } else if (rootData.code == Constants.RESPONCE_CODE_MULLOGIN) {
                    Intent intent = new Intent();
                    intent.setAction("com.vgtech.vancloud.logout");
                    intent.putExtra("logoutFrom", rootData.code);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else if (rootData.code == Constants.RESPONCE_CODE_TOKEN_EXPIRED) {
                    Toast.makeText(context, R.string.toast_login_expired, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction("com.vgtech.vancloud.login");
                    intent.putExtra("logoutFrom", rootData.code);
                    intent.putExtra("logout", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
//                    FileUtils.writeString("ActivityUtils -> prehandleNetworkData response code(RESPONCE_CODE_TOKEN_EXPIRED) = " + Constants.RESPONCE_CODE_TOKEN_EXPIRED);
                    Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                    context.sendBroadcast(reveiverIntent);
                }
            }
            IEncActivity enActivity = null;
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity = getRootActivity(activity);
                if (activity instanceof IEncActivity) {
                    enActivity = (IEncActivity) activity;
                }
                if (enActivity != null) {
                    if (needNotify) {
                        enActivity.notifyErrDlg(callbackId, path, listener);
                        if (!NetworkHelpers.isNetworkAvailable(context)) {
                            error.msg = context.getString(R.string.no_network);
                            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
                        } else {
                            if (!TextUtils.isEmpty(error.msg) && code != 0) {
                                Toast.makeText(context, error.msg, Toast.LENGTH_SHORT).show();
                            } else {
                                error.msg = context.getString(R.string.network_error);
                                Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                            }

                        }
                        if (Constants.DEBUG && needNotify){
                            enActivity.showError(error);
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

    public static void openAdActivity(Context context) {

        context.startActivity(new Intent(context, RSplashActivity.class));
    }

}
