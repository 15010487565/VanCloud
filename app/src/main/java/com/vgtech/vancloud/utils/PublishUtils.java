package com.vgtech.vancloud.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/9/21.
 */
public class PublishUtils {
    /**
     * 任务
     */
    public static final int COMMENTTYPE_TASK = 11;
    /**
     * 日程
     */
    public static final int COMMENTTYPE_SCHEDULE = 9;
    /**
     * 流程
     */
    public static final int COMMENTTYPE_FLOW = 2;
    /**
     * 帮帮
     */
    public static final int COMMENTTYPE_HELP = 5;
    /**
     * 分享
     */
    public static final int COMMENTTYPE_SHARE = 3;
    /**
     * 公告
     */
    public static final int COMMENTTYPE_ANNOUNCEMENT = 1;
    /**
     * 工作汇报
     */
    public static final int COMMENTTYPE_WORKREPORT = 7;

    private static final int CALLBACK_PRAISE = 1;

    /**
     * 招聘职位审批
     * @param context
     * @param actionType 1,同意，2不同意
     * @param resource_id  招聘id
     */
    public static void recruitApproveAction(Context context, int actionType, String resource_id) {
        Intent intent = new Intent(context, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_RECRUIT_APPROVE);
        intent.putExtra(Constants.TYPE, actionType);
        intent.putExtra("resource_id", resource_id);
        context.startActivity(intent);
    }
    /**
     * 招聘完成
     * @param context
     * @param resource_id  招聘id
     */
    public static void recruitFinish(Context context,String resource_id) {
        Intent intent = new Intent(context, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_RECRUIT_FINISH);
        intent.putExtra("resource_id", resource_id);
        context.startActivity(intent);
    }

    /**
     * ，简历购买审批
     * @param context
     * @param actionType 1,同意，2不同意
     * @param resource_id 简历购买审批id
     */
    public static void resumeApproveAction(Context context, int actionType, String resource_id) {
        Intent intent = new Intent(context, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_RESUME_APPROVE);
        intent.putExtra(Constants.TYPE, actionType);
        intent.putExtra("resource_id", resource_id);
        context.startActivity(intent);
    }

    /**
     * @param context
     * @param commentType 评论类型
     * @param id          评论对象id
     */
    public static void addComment(Context context, int commentType, String id) {
        Intent intent = new Intent(context, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_COMMENT);
        intent.putExtra("comment_type", commentType);
        intent.putExtra("publishId", id);
        context.startActivity(intent);
    }

    /**
     * @param context
     * @param commentType 评论类型
     * @param id          评论对象id
     */
    public static void addComment(Context context, int commentType, String id, int position) {
        Intent intent = new Intent(context, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_COMMENT);
        intent.putExtra("comment_type", commentType);
        intent.putExtra("publishId", id);
        intent.putExtra("position", position);
        context.startActivity(intent);
    }

    public static String getLeaveType(Context context, int type) {
        String[] leaveTypes = context.getResources().getStringArray(R.array.leave_type);
        if (type < 0 || type > (leaveTypes.length - 1)) {
            type = 0;
        }
        return leaveTypes[type];
    }

    /**
     * 日程处理
     *
     * @param context
     * @param type       1待定，2谢绝，3同意
     * @param scheduleId
     */
    public static void conductSchedule(Context context, int type, String scheduleId) {
        Intent intent = new Intent(context, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE_CONDUCT);
        intent.putExtra(Constants.TYPE, type);
        intent.putExtra("scheduleId", scheduleId);
        context.startActivity(intent);
    }

    /**
     * 流程处理
     *
     * @param context
     * @param type       1同意，2不同意,3撤销
     * @param scheduleId
     */
    public static void conductProcess(Context context, int type, String scheduleId) {
        Intent intent = new Intent(context, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE_CONDUCT);
        intent.putExtra("type", type);
        intent.putExtra("scheduleId", scheduleId);
        context.startActivity(intent);
    }

    private static boolean isDoDig = true;

    /**
     * 点赞
     *
     * @param typeid
     * @param type(1-公告，3-分享，5-帮帮，7-工作汇报，9-日程，11-任务，12-聊天)
     */
    public static void toPraise(final Context context, String typeid, int type, final successfulToDo successfultodo) {

        if (isDoDig) {
            isDoDig = false;
            final BaseActivity baseActivity = (BaseActivity) context;
            NetworkManager mNetworkManager = baseActivity.getAppliction().getNetworkManager();
            baseActivity.showLoadingDialog(context, context.getResources().getString(R.string.prompt_info_02));
            Map<String, String> params = new HashMap<String, String>();
            params.put("ownid", PrfUtils.getUserId(context));
            params.put("tenantid", PrfUtils.getTenantId(context));
            params.put("opeationtype", "1");
            params.put("type", type + "");
            params.put("typeid", typeid);
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_SUPPORT_DOPRAISE), params, context);
            mNetworkManager.load(CALLBACK_PRAISE, path, new HttpListener<String>() {
                @Override
                public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                    isDoDig = true;
                    baseActivity.dismisLoadingDialog();
                    boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                    if (!safe) {
                        Toast.makeText(context, context.getResources().getString(R.string.dig_fail_info), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    switch (callbackId) {

                        case CALLBACK_PRAISE:
                            Toast.makeText(context, context.getResources().getString(R.string.dig_success_info), Toast.LENGTH_SHORT).show();
                            successfultodo.successful();
                            break;
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }

                @Override
                public void onResponse(String response) {

                }
            });

        }

    }


    private static boolean isToDoDig = true;

    /**
     * 点赞
     *
     * @param typeid
     * @param type(1-公告，3-分享，5-帮帮，7-工作汇报，9-日程，11-任务，12-聊天)
     * @param digType                                      ture 取消点赞,false 点赞
     */
    public static void toDig(final Context context, String typeid, int type, final boolean digType, final DigCallBack digCallBack) {

        if (isToDoDig) {
            isToDoDig = false;
            final BaseActivity baseActivity = (BaseActivity) context;
            NetworkManager mNetworkManager = baseActivity.getAppliction().getNetworkManager();
            baseActivity.showLoadingDialog(context, context.getResources().getString(R.string.prompt_info_02));
            Map<String, String> params = new HashMap<String, String>();
            params.put("ownid", PrfUtils.getUserId(context));
            params.put("tenantid", PrfUtils.getTenantId(context));
            if (digType) {
                params.put("opeationtype", "2");
            } else {
                params.put("opeationtype", "1");
            }
            params.put("type", type + "");
            params.put("typeid", typeid);
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_SUPPORT_DOPRAISE), params, context);
            mNetworkManager.load(CALLBACK_PRAISE, path, new HttpListener<String>() {
                @Override
                public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                    isToDoDig = true;
                    baseActivity.dismisLoadingDialog();
                    boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                    if (!safe) {
//                        Toast.makeText(context, context.getResources().getString(R.string.dig_fail_info), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    switch (callbackId) {

                        case CALLBACK_PRAISE:
//                            Toast.makeText(context, context.getResources().getString(R.string.dig_success_info), Toast.LENGTH_SHORT).show();
                            digCallBack.successful(digType);
                            break;
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }

                @Override
                public void onResponse(String response) {

                }
            });

        }

    }


    public interface successfulToDo {

        void successful();
    }


    public interface DigCallBack {

        /**
         * 点赞和取消点赞成功后的回调
         *
         * @param digType ture取消点赞，false点赞
         */
        void successful(boolean digType);
    }
}
