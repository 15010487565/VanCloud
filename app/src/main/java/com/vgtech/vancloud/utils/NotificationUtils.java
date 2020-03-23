package com.vgtech.vancloud.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.NotificationExtension;
import com.vgtech.vancloud.api.TodoNotification;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.module.announcement.AnnouncementDetailActivity;
import com.vgtech.vancloud.ui.module.approval.ApprovalListActivity;
import com.vgtech.vancloud.ui.module.approval.MyApprovalActivity;
import com.vgtech.vancloud.ui.module.contact.StaffApplysActivity;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.ui.module.help.HelpDetailActivity;
import com.vgtech.vancloud.ui.module.schedule.ScheduleDetailActivity;
import com.vgtech.vancloud.ui.module.share.SharedInfoActivity;
import com.vgtech.vancloud.ui.module.task.TaskTransactActivity;
import com.vgtech.vancloud.ui.module.workreport.WorkReportTransactActivity;
import com.vgtech.vancloud.ui.web.BjdcWebActivity;
import com.vgtech.vancloud.ui.web.HttpWebActivity;
import com.vgtech.vancloud.ui.web.ProcessWebActivity;
import com.vgtech.vantop.ui.overtime.OverTimeDetailActivity;
import com.vgtech.vantop.ui.questionnaire.QuestionnaireActivity;
import com.vgtech.vantop.ui.signedcard.SignedCardApprovalDetailsActivity;
import com.vgtech.vantop.ui.vacation.VacationApplyDetailsActivity;

import java.util.HashMap;

/**
 * Created by Duke on 2016/9/20.
 */

public class NotificationUtils {

    private static String HASTEN = "hasten";
    private static String EXAM = "exam";

    /**
     * 设置图片LOGO
     *
     * @param context
     * @param imageView 左边LOGO
     * @param type      业务类型
     */
    public static void setImageView(Context context, ImageView imageView, String type) {
        GradientDrawable myGrad = (GradientDrawable) imageView.getBackground();
        Resources resources = context.getResources();
        int color = resources.getColor(R.color.blue);
        if (TypeUtils.APPROVAL.equals(type)) {
            color = resources.getColor(R.color.app_approval_shenpi);
            imageView.setImageResource(R.mipmap.ic_app_approve_shenpi);
        } else if (TypeUtils.SCHEDULE.equals(type)) {
            color = resources.getColor(R.color.app_schedule);
            imageView.setImageResource(R.mipmap.ic_app_schedule);
        } else if (TypeUtils.TSAK.equals(type)) {
            color = resources.getColor(R.color.app_task);
            imageView.setImageResource(R.mipmap.ic_app_task);
        } else if (TypeUtils.WORKREPORT.equals(type)) {
            color = resources.getColor(R.color.app_report);
            imageView.setImageResource(R.mipmap.ic_app_workreport);
        } else if (TypeUtils.NOTICE.equals(type)) {
            color = resources.getColor(R.color.app_notice);
            imageView.setImageResource(R.mipmap.ic_app_notice);
        } else if (TypeUtils.SHARE.equals(type)) {
            color = resources.getColor(R.color.app_share);
            imageView.setImageResource(R.mipmap.ic_app_share);
        } else if (TypeUtils.HELP.equals(type)) {
            color = resources.getColor(R.color.app_help);
            imageView.setImageResource(R.mipmap.ic_app_help);
        } else if (TypeUtils.BACKGROUNDINVESTIGATION.equals(type)) {
            color = resources.getColor(R.color.app_beidiao);
            imageView.setImageResource(R.mipmap.ic_app_beidiao);
        } else if (TypeUtils.MEETING.equals(type)) {
            color = resources.getColor(R.color.app_meeting);
            imageView.setImageResource(R.mipmap.ic_app_meeting);
        } else if (TypeUtils.VANTOPSIGNCARD.equals(type)) {
            //签卡
            color = resources.getColor(R.color.app_sign_card);
            imageView.setImageResource(R.mipmap.ic_app_signcard);
        } else if (TypeUtils.VANTOPLEAVE.equals(type)) {
            //vantop假期
            color = resources.getColor(R.color.app_leave);
            imageView.setImageResource(R.mipmap.ic_app_leave);
        } else if (TypeUtils.VANTOPOVERTIME.equals(type)) {
            //加班
            color = resources.getColor(R.color.app_overtime);
            imageView.setImageResource(R.mipmap.ic_app_over_time);
        } else if (!TypeUtils.JOINCOMPANY.equals(type) && !TypeUtils.NEWEMPLOYEE.equals(type)) {
            color = resources.getColor(R.color.app_approval_shenpi);
            imageView.setImageResource(R.mipmap.ic_app_approve_shenpi);
        }
        if (myGrad != null)
            myGrad.setColor(color);
    }

    /**
     * 设置图片LOGO和内容
     *
     * @param context
     * @param imageView 左边LOGO
     * @param textView  处理信息
     * @param type      业务类型
     * @param status    处理状态
     */
//    审批处理  1同意，2不同意
//    日程处理  1待定，2谢绝，3同意
//    任务处理  1完成
//    工作汇报  1点评

//    <!--同意-->
//    <color name="process_agree">#66c142</color>
//    <!--不同意，拒绝、谢绝-->
//    <color name="process_disagree">#ff6666</color>
//    <!--待定、待审批-->
//    <color name="process_undetermined">#ec8a43</color>
//    <!--完成、已点评-->
//    <color name="process_finish">#999999</color>
    public static void setItemView(Context context, ImageView imageView, TextView textView, String type, String status) {
        GradientDrawable myGrad = (GradientDrawable) imageView.getBackground();
        Resources resources = context.getResources();
        int color = resources.getColor(R.color.blue);
        int textcolor = resources.getColor(R.color.comment_grey);
        if (TypeUtils.APPROVAL.equals(type)) {
            color = resources.getColor(R.color.app_approval_shenpi);
            imageView.setImageResource(R.mipmap.ic_app_approve_shenpi);
            if ("1".equals(status))
                textcolor = resources.getColor(R.color.process_agree);
            else if ("2".equals(status))
                textcolor = resources.getColor(R.color.process_disagree);
        } else if (TypeUtils.SCHEDULE.equals(type)) {
            color = resources.getColor(R.color.app_schedule);
            imageView.setImageResource(R.mipmap.ic_app_schedule);
            if ("1".equals(status))
                textcolor = resources.getColor(R.color.process_undetermined);
            else if ("2".equals(status))
                textcolor = resources.getColor(R.color.process_disagree);
            else if ("3".equals(status))
                textcolor = resources.getColor(R.color.process_agree);
        } else if (TypeUtils.TSAK.equals(type)) {
            color = resources.getColor(R.color.app_task);
            imageView.setImageResource(R.mipmap.ic_app_task);
            if ("1".equals(status))
                textcolor = resources.getColor(R.color.process_finish);
        } else if (TypeUtils.WORKREPORT.equals(type)) {
            color = resources.getColor(R.color.app_report);
            imageView.setImageResource(R.mipmap.ic_app_workreport);
            if ("1".equals(status))
                textcolor = resources.getColor(R.color.process_finish);
        } else {

        }
        if (myGrad != null)
            myGrad.setColor(color);
        textView.setTextColor(textcolor);
    }

    public static void itemClick(BaseFragment baseFragment, String type, String resId) {

        if (TypeUtils.APPROVAL.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), FlowHandleActivity.class);
            intent.putExtra(FlowHandleActivity.FLOWID, resId);
            baseFragment.startActivityForResult(intent, 1);
        } else if (TypeUtils.SCHEDULE.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), ScheduleDetailActivity.class);
            intent.putExtra("scheduleId", resId);
            baseFragment.startActivityForResult(intent, 1);
        } else if (TypeUtils.TSAK.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), TaskTransactActivity.class);
            intent.putExtra("TaskID", resId);
            baseFragment.startActivityForResult(intent, 1);
        } else if (TypeUtils.WORKREPORT.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), WorkReportTransactActivity.class);
            intent.putExtra(WorkReportTransactActivity.WORKREPORTID, resId);
            baseFragment.startActivityForResult(intent, 1);
        } else {

        }
    }

    public static void itemClick(BaseActivity baseActivity, String type, String resId) {
        itemClick(baseActivity,type,resId,null);
    }
    public static void itemClick(BaseActivity baseActivity, String type, String resId,String operationType) {

        if (TypeUtils.APPROVAL.equals(type)) {
            Intent intent = new Intent(baseActivity, FlowHandleActivity.class);
            intent.putExtra(FlowHandleActivity.FLOWID, resId);
            baseActivity.startActivityForResult(intent, 1);
        } else
        if (TypeUtils.SCHEDULE.equals(type)) {
            Intent intent = new Intent(baseActivity, ScheduleDetailActivity.class);
            intent.putExtra("scheduleId", resId);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.TSAK.equals(type)) {
            Intent intent = new Intent(baseActivity, TaskTransactActivity.class);
            intent.putExtra("TaskID", resId);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.WORKREPORT.equals(type)) {
            Intent intent = new Intent(baseActivity, WorkReportTransactActivity.class);
            intent.putExtra(WorkReportTransactActivity.WORKREPORTID, resId);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.NOTICE.equals(type)) {
            Intent intent = new Intent(baseActivity, AnnouncementDetailActivity.class);
            intent.putExtra("id", resId);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.SHARE.equals(type)) {
            Intent intent = new Intent(baseActivity, SharedInfoActivity.class);
            intent.putExtra("id", resId);
            baseActivity.startActivityForResult(intent, 1);

        } else if (TypeUtils.HELP.equals(type)) {
            Intent intent = new Intent(baseActivity, HelpDetailActivity.class);
            intent.putExtra("id", resId);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.JOINCOMPANY.equals(type)) {
            Intent intent = new Intent(baseActivity, StaffApplysActivity.class);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.BACKGROUNDINVESTIGATION.equals(type)) {
            String url = ApiUtils.generatorUrl(baseActivity, URLAddr.URL_INVESTIGATES
                    + "?tenantId=" + PrfUtils.getTenantId(baseActivity)
                    + "&userId=" + PrfUtils.getUserId(baseActivity)
                    + "&orderInfoId=" + resId);
            Intent appIntent = new Intent(baseActivity, BjdcWebActivity.class);
            appIntent.putExtra("title", baseActivity.getString(R.string.lable_jcxq));
            appIntent.putExtra("url", url);
            baseActivity.startActivity(appIntent);
        } else if (TypeUtils.APPROVAL_FLOW.equals(type)) {

            Intent intent = new Intent(baseActivity, ProcessWebActivity.class);
            intent.putExtra("type", "push");
            intent.putExtra("flow_id", resId);
            intent.putExtra("operation_type", operationType);
//            intent.putExtra("position", position);
            baseActivity.startActivity(intent);
        }
        else {

        }
    }


    public static void commentItemClick(BaseActivity baseActivity, String type, String resId) {

//        if (TypeUtils.APPROVAL.equals(type)) {
//            Intent intent = new Intent(baseActivity, FlowHandleActivity.class);
//            intent.putExtra(FlowHandleActivity.FLOWID, resId);
//            baseActivity.startActivityForResult(intent, 1);
//        } else

        if (TypeUtils.SCHEDULE.equals(type)) {
            Intent intent = new Intent(baseActivity, ScheduleDetailActivity.class);
            intent.putExtra("scheduleId", resId);
            intent.putExtra("showcomment", true);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.TSAK.equals(type)) {
            Intent intent = new Intent(baseActivity, TaskTransactActivity.class);
            intent.putExtra("TaskID", resId);
            intent.putExtra("showcomment", true);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.WORKREPORT.equals(type)) {
            Intent intent = new Intent(baseActivity, WorkReportTransactActivity.class);
            intent.putExtra(WorkReportTransactActivity.WORKREPORTID, resId);
            intent.putExtra("showcomment", true);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.NOTICE.equals(type)) {
            Intent intent = new Intent(baseActivity, AnnouncementDetailActivity.class);
            intent.putExtra("id", resId);
            intent.putExtra("showcomment", true);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.SHARE.equals(type)) {
            Intent intent = new Intent(baseActivity, SharedInfoActivity.class);
            intent.putExtra("id", resId);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.HELP.equals(type)) {
            Intent intent = new Intent(baseActivity, HelpDetailActivity.class);
            intent.putExtra("id", resId);
            intent.putExtra("showcomment", true);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.JOINCOMPANY.equals(type)) {
            Intent intent = new Intent(baseActivity, StaffApplysActivity.class);
            baseActivity.startActivityForResult(intent, 1);
        } else if (TypeUtils.BACKGROUNDINVESTIGATION.equals(type)) {
            String url = ApiUtils.generatorUrl(baseActivity, URLAddr.URL_INVESTIGATES
                    + "?tenantId=" + PrfUtils.getTenantId(baseActivity)
                    + "&userId=" + PrfUtils.getUserId(baseActivity)
                    + "&orderInfoId=" + resId);
            Intent appIntent = new Intent(baseActivity, BjdcWebActivity.class);
            appIntent.putExtra("title", baseActivity.getString(R.string.lable_jcxq));
            appIntent.putExtra("url", url);
            baseActivity.startActivity(appIntent);
        } else {

        }
    }

    public static void itemClick(Fragment baseFragment, String type, String resId, String staffNo, int position) {

        if (TypeUtils.APPROVAL.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), FlowHandleActivity.class);
            intent.putExtra(FlowHandleActivity.FLOWID, resId);
            baseFragment.startActivity(intent);
        } else if (TypeUtils.SCHEDULE.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), ScheduleDetailActivity.class);
            intent.putExtra("scheduleId", resId);
            baseFragment.startActivity(intent);
        } else if (TypeUtils.TSAK.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), TaskTransactActivity.class);
            intent.putExtra("TaskID", resId);
            baseFragment.startActivity(intent);
        } else if (TypeUtils.WORKREPORT.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), WorkReportTransactActivity.class);
            intent.putExtra(WorkReportTransactActivity.WORKREPORTID, resId);
            baseFragment.startActivity(intent);
        } else if (TypeUtils.VANTOPSIGNCARD.equals(type)) {
            //签卡
            Intent intent = new Intent(baseFragment.getActivity(), MyApprovalActivity.class);
            intent.putExtra("tag", AppPermission.Shenqing.shenqing_sign_card.toString());
            baseFragment.startActivity(intent);
        } else if (TypeUtils.VANTOPLEAVE.equals(type)) {
            //vantop假期
            Intent intent = new Intent(baseFragment.getActivity(), VacationApplyDetailsActivity.class);
            intent.putExtra("id", resId);
            intent.putExtra("staffno", staffNo);
            intent.putExtra("type", false);
            intent.putExtra("position", position);
            baseFragment.startActivity(intent);

        } else if (TypeUtils.VANTOPOVERTIME.equals(type)) {
            //加班
            Intent intent = new Intent(baseFragment.getActivity(), OverTimeDetailActivity.class);
            intent.putExtra("taskId", resId);
            intent.putExtra("staffno", staffNo);
            intent.putExtra("type", false);
            intent.putExtra("position", position);
            baseFragment.startActivity(intent);
        }else {
            SharedPreferences preferences = PrfUtils.getSharePreferences(baseFragment.getActivity());
            String userId = preferences.getString("uid", "");
            String employee_no = preferences.getString("user_no", "");
            String tenantId = preferences.getString("tenantId", "");

            HashMap<String, String> params = new HashMap<>();
            params.put("user_id", userId);
            params.put("employee_no", employee_no);
            params.put("tenant_id", tenantId);
            params.put("operation_ip", IpUtil.getIpAddressString());
            params.put("operation_url", "");
            if ("38".equals(type)) {
                //入职审批
                BehaviorStatistics.getInstance().startBehavior(params);
                Intent intentBG = new Intent(baseFragment.getActivity(), HttpWebActivity.class);
                intentBG.putExtra("code", Constants.ENTRYAPPROVE_CODE);
                baseFragment.startActivity(intentBG);
            }else if ("39".equals(type)) {
                //调查问卷
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent qustion = new Intent(baseFragment.getActivity(), QuestionnaireActivity.class);
                baseFragment.startActivity(qustion);
            }
        }
    }

    public static void itemClick(Fragment baseFragment, String type, TodoNotification todoNotification, int position) {
        if (TypeUtils.APPROVAL_FLOW.equals(type)) {
            Intent intent = new Intent(baseFragment.getActivity(), ProcessWebActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("flow_id", todoNotification.res_id);
            intent.putExtra("approval_type", todoNotification.state);
            intent.putExtra("position", position);
            baseFragment.startActivity(intent);
        }else {
            itemClick(baseFragment,type,todoNotification.res_id,todoNotification.create_user_no,position);
        }
    }

    public static void vantopItemClick(Context context, PushMessage pushMessage, NotificationExtension notificationExtension) {
        vantopItemClick(context, pushMessage, notificationExtension, 0);
    }

    public static void vantopItemClick(Context context, PushMessage pushMessage, NotificationExtension notificationExtension, int position) {

        if (TypeUtils.VANTOPLEAVE.equals(pushMessage.msgTypeId)) {

            if (HASTEN.equals(pushMessage.operationType)) {
                if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
                    Intent appIntent = new Intent(context, VacationApplyDetailsActivity.class);
                    appIntent.putExtra("id", pushMessage.resId);
                    appIntent.putExtra("staffno", notificationExtension.content);
                    appIntent.putExtra("type", false);
                    appIntent.putExtra("position", position);
                    context.startActivity(appIntent);
                }
            } else {
                if (!TextUtils.isEmpty(pushMessage.content) && (pushMessage.content.contains("撤消") || pushMessage.content.contains("Withdrawal"))) {//审批人
//                    Intent intent = new Intent(context, MyApprovalActivity.class);
//                    intent.putExtra("tag", AppPermission.Shenqing.shenqing_vantop_holiday.toString());
//                    context.startActivity(intent);
                } else {
                    //申请人
                    Intent intent = new Intent(context, ApprovalListActivity.class);
                    intent.putExtra("tag", AppPermission.Shenqing.shenqing_vantop_holiday.toString());
                    intent.putExtra("title", context.getString(R.string.leave));
                    context.startActivity(intent);
                }


//                Intent appIntent = new Intent(context, VacationApplyDetailsActivity.class);
//                appIntent.putExtra("id", pushMessage.resId);
//                context.startActivity(appIntent);
            }
        } else if (TypeUtils.VANTOPSIGNCARD.equals(pushMessage.msgTypeId)) {
            if (HASTEN.equals(pushMessage.operationType)) {
                if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
                    Intent appIntent = new Intent(context, SignedCardApprovalDetailsActivity.class);
                    appIntent.putExtra("taskId", pushMessage.resId);
                    appIntent.putExtra("position", position);
                    appIntent.putExtra("staffNo", notificationExtension.content);
                    context.startActivity(appIntent);
                }
            } else {
                if (!TextUtils.isEmpty(pushMessage.content) && (pushMessage.content.contains("撤消") || pushMessage.content.contains("Withdrawal"))) {//审批人
//                    Intent intent = new Intent(context, MyApprovalActivity.class);
//                    intent.putExtra("tag", AppPermission.Shenqing.shenqing_sign_card.toString());
//                    context.startActivity(intent);
                } else {
                    //申请人
                    Intent intent = new Intent(context, ApprovalListActivity.class);
                    intent.putExtra("tag", AppPermission.Shenqing.shenqing_sign_card.toString());
                    intent.putExtra("title", context.getString(R.string.change_sign));
                    context.startActivity(intent);
                }
            }

        } else if (TypeUtils.VANTOPOVERTIME.equals(pushMessage.msgTypeId)) {

            if (HASTEN.equals(pushMessage.operationType)) {
                if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
                    Intent appIntent = new Intent(context, OverTimeDetailActivity.class);
                    appIntent.putExtra("taskId", pushMessage.resId);
                    appIntent.putExtra("position", position);
                    appIntent.putExtra("staffno", notificationExtension.content);
                    appIntent.putExtra("type", false);
                    context.startActivity(appIntent);
                }
            } else {
//                Intent appIntent = new Intent(context, OverTimeDetailActivity.class);
//                appIntent.putExtra("taskId", pushMessage.resId);
//                context.startActivity(appIntent);

                Intent intent = new Intent(context, ApprovalListActivity.class);
                intent.putExtra("tag", AppPermission.Shenqing.shenqing_extra_work.toString());
                intent.putExtra("title", context.getString(R.string.overtime));
                context.startActivity(intent);
            }
        }
    }

}
