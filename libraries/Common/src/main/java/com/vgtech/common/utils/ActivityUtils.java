package com.vgtech.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.vgtech.common.Constants;
import com.vgtech.common.NetworkHelpers;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.R;
import com.vgtech.common.api.Error;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;

import java.util.Locale;

/**
 * Created by zhangshaofang on 2016/3/14.
 */
public class ActivityUtils {
    public static boolean prehandleNetworkData(Context context, HttpListener<String> listener, int callbackId, NetworkPath path, RootData rootData, boolean needNotify) {
        boolean result = rootData.isSuccess();
        if (!result) {
            com.vgtech.common.api.Error error = new Error();
            int code = rootData.getCode();
            String msg = rootData.getMsg();
            error.code = code;
            error.msg = msg;
            error.desc = msg;
            if (context != null) {
                if (rootData.code == Constants.RESPONCE_CODE_UNLOGIN) {
                    Intent intent = new Intent();
                    intent.setAction("com.vgtech.vancloud.login");
                    intent.putExtra("logout", true);
                    intent.putExtra("logoutFrom", rootData.code);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    Intent reveiverIntent = new Intent("RECEIVER_EXIT");
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
                    Intent reveiverIntent = new Intent("RECEIVER_EXIT");
                    context.sendBroadcast(reveiverIntent);
                }
            }
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity = getRootActivity(activity);
                if (activity != null) {
                    if (needNotify) {
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

    /**
     * 跳转支付页面
     *
     * @param activity
     * @param orderID          订单ID
     * @param orderDescription 订单描述
     * @param amount           订单金额
     * @param orderType        订单类型 investigate背景调查,recruit简历,meeting视频会议
     * @param position         列表标记（不需要时传-1）
     */
    public static void toPay(Activity activity, String orderID, String orderDescription, String amount, String orderType, int position) {
        Intent intent = new Intent();
        intent.setAction("com.vgtech.vancloud.intent.action.OrderPayActivity");
        intent.putExtra("orderid", orderID);
        intent.putExtra("orderdescription", orderDescription);
        intent.putExtra("amount", amount);
        intent.putExtra("ordertype", orderType);
        intent.putExtra("position", position);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, 200);
    }

    /**
     * 跳转支付页面
     *
     * @param fragment
     * @param orderID          订单ID
     * @param orderDescription 订单描述
     * @param amount           订单金额
     * @param orderType        订单类型 investigate背景调查,recruit简历,meeting视频会议
     * @param position         列表标记（不需要时传-1）
     */
    public static void toPay(Fragment fragment, String orderID, String orderDescription, String amount, String orderType, int position) {
        Intent intent = new Intent();
        intent.setAction("com.vgtech.vancloud.intent.action.OrderPayActivity");
        intent.putExtra("orderid", orderID);
        intent.putExtra("orderdescription", orderDescription);
        intent.putExtra("amount", amount);
        intent.putExtra("ordertype", orderType);
        intent.putExtra("position", position);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fragment.startActivityForResult(intent, 200);
    }

    /**
     * 跳转支付页面
     *
     * @param activity
     * @param orderID          订单ID
     * @param orderDescription 订单描述
     * @param amount           订单金额
     * @param orderType        订单类型 investigate背景调查,recruit简历,meeting视频会议
     * @param position         列表标记（不需要时传-1）
     */
    public static void toPay(Activity activity, String orderID, String orderDescription, String amount, String orderType, int position, boolean isfromDetails) {
        Intent intent = new Intent();
        intent.setAction("com.vgtech.vancloud.intent.action.OrderPayActivity");
        intent.putExtra("orderid", orderID);
        intent.putExtra("orderdescription", orderDescription);
        intent.putExtra("amount", amount);
        intent.putExtra("ordertype", orderType);
        intent.putExtra("position", position);
        intent.putExtra("isfromdetails", isfromDetails);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, 200);
    }

    /**
     * 跳转支付页面
     *
     * @param activity
     * @param orderID          订单ID
     * @param orderDescription 订单描述
     * @param amount           订单金额
     * @param orderType        订单类型 investigate背景调查,recruit简历,meeting视频会议
     * @param position         列表标记（不需要时传-1）
     * @param userType         用户类型 1.公司，2.个人
     */
    public static void toPay(Activity activity, String orderID, String orderDescription, String amount, String orderType, int position, int userType) {
        Intent intent = new Intent();
        intent.setAction("com.vgtech.vancloud.intent.action.OrderPayActivity");
        intent.putExtra("orderid", orderID);
        intent.putExtra("orderdescription", orderDescription);
        intent.putExtra("amount", amount);
        intent.putExtra("ordertype", orderType);
        intent.putExtra("position", position);
        intent.putExtra("userType", userType);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, 200);
    }

    public static void setAppLanguage(Context context) {
        String language = PrfUtils.getPrfparams(context, "is_language");
        if ("en".equals(language)) {
            PrfUtils.getInstance(context).saveLanguage( 3);
        } else if ("zh".equals(language)) {
            PrfUtils.getInstance(context).saveLanguage( 1);
        }
    }

    private static int MAX_MEM = 30 * ByteConstants.MB;

    public static void initFresco(Context context) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                MAX_MEM,// 内存缓存中总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中图片的最大数量。
                MAX_MEM,// 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中准备清除的总图片的最大数量。
                Integer.MAX_VALUE);// 内存缓存中单个图片的最大大小。
        ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context);
        configBuilder.setBitmapsConfig(Bitmap.Config.RGB_565);
        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };
        configBuilder.setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams);
        Fresco.initialize(context, configBuilder.build());
    }
}
