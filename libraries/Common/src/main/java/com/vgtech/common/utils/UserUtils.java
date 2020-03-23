package com.vgtech.common.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;

/**
 * Created by zhangshaofang on 2015/9/29.
 */
public class UserUtils {

    public static final String BUNDLE_USERID = "user_id";
    public static final String GROUPCHAT_TYPE = "GROUPCHAT_TYPE";

    /**
     * 进入他人信息页面
     *
     * @param context
     * @param userId  用户id 必传
     * @param name    用户姓名or昵称//预加载使用
     * @param photo   用户头像//预加载使用
     */
    public static void enterUserInfo(Context context, String userId, String name, String photo) {
        String tenantId = PrfUtils.getTenantId(context);
        if (!TextUtils.isEmpty(tenantId)&&!TextUtils.isEmpty(userId)){
            userId = userId.replaceAll(tenantId, "");
        }
        if (TenantPresenter.isVanTop(context)) {
//            Intent intent = new Intent(context, VantopUserInfoActivity.class);
            Intent intent = new Intent("com.vgtech.vancloud.intent.action.VantopUserInfoActivity");
            intent.putExtra(BUNDLE_USERID, userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
//            Intent intent = new Intent(context, SelfInfoActivity.class);
            Intent intent = new Intent("com.vgtech.vancloud.intent.action.SelfInfoActivity");
            intent.putExtra("userId", userId);
            intent.putExtra("name", name);
            intent.putExtra("photo", photo);
            intent.putExtra("type", "0");
            context.startActivity(intent);
        }
    }

    public static void enterUserInfo(Context context, String userId, String name, String photo, boolean chat) {
//        if (!TextUtils.isEmpty(userId) && userId.length() > 18){
//            userId.replaceAll(".", "#");
//            userId = userId.substring(0, 18);
//        }
        String tenantId = PrfUtils.getTenantId(context);
        if (!TextUtils.isEmpty(tenantId)&&!TextUtils.isEmpty(userId)){
            userId = userId.replaceAll(tenantId, "");
        }

        if (TenantPresenter.isVanTop(context)) {
//            Intent intent = new Intent(context, VantopUserInfoActivity.class);
            Intent intent = new Intent("com.vgtech.vancloud.intent.action.VantopUserInfoActivity");
            intent.putExtra(BUNDLE_USERID, userId);
            intent.putExtra(GROUPCHAT_TYPE, chat);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
//            Intent intent = new Intent(context, SelfInfoActivity.class);
            Intent intent = new Intent("com.vgtech.vancloud.intent.action.SelfInfoActivity");
            intent.putExtra("userId", userId);
            intent.putExtra("name", name);
            intent.putExtra("photo", photo);
            intent.putExtra("type", "0");
            context.startActivity(intent);
        }
    }

    public static void enterUserInfo(final Context context, final String userId, final String name, final String photo, View userIv) {
        userIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = userId;
                if (TenantPresenter.isVanTop(context)) {
//                    Intent intent = new Intent(context, VantopUserInfoActivity.class);
                    Intent intent = new Intent("com.vgtech.vancloud.intent.action.VantopUserInfoActivity");
                    intent.putExtra(BUNDLE_USERID, userId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
//                    Intent intent = new Intent(context, SelfInfoActivity.class);
                    Intent intent = new Intent("com.vgtech.vancloud.intent.action.SelfInfoActivity");

                    String tenantId = PrfUtils.getTenantId(context);
                    if (!TextUtils.isEmpty(tenantId)&&!TextUtils.isEmpty(uid)){
                        uid = uid.replaceAll(tenantId, "");
                    }
                    intent.putExtra("userId", uid);
                    intent.putExtra("name", name);
                    intent.putExtra("photo", photo);
                    intent.putExtra("type", "0");
                    context.startActivity(intent);
                }
            }
        });

    }
}
