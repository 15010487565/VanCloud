package com.vgtech.vancloud.reciver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.BindAliasCmdMessage;
import com.igexin.sdk.message.FeedbackCmdMessage;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.igexin.sdk.message.SetTagCmdMessage;
import com.igexin.sdk.message.UnBindAliasCmdMessage;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.utils.AppShortCutUtil;
import com.vgtech.common.utils.LogUtils;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.api.NotificationExtension;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.module.announcement.AnnouncementDetailActivity;
import com.vgtech.vancloud.ui.module.announcement.NoticeInfoActivity;
import com.vgtech.vancloud.ui.module.approval.ApprovalListActivity;
import com.vgtech.vancloud.ui.module.approval.MyApprovalActivity;
import com.vgtech.vancloud.ui.module.contact.StaffApplysActivity;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.ui.module.help.HelpDetailActivity;
import com.vgtech.vancloud.ui.module.me.SelfInfoActivity;
import com.vgtech.vancloud.ui.module.schedule.ScheduleDetailActivity;
import com.vgtech.vancloud.ui.module.share.SharedInfoActivity;
import com.vgtech.vancloud.ui.module.task.TaskTransactActivity;
import com.vgtech.vancloud.ui.module.todo.MessageListNewActivity;
import com.vgtech.vancloud.ui.module.todo.ToDoNotificationNewListFragment;
import com.vgtech.vancloud.ui.module.workreport.WorkReportTransactActivity;
import com.vgtech.vancloud.ui.web.BjdcWebActivity;
import com.vgtech.vancloud.ui.web.HttpWebActivity;
import com.vgtech.vancloud.ui.web.ProcessWebActivity;
import com.vgtech.vancloud.utils.NoticeUtils;
import com.vgtech.vantop.ui.overtime.OverTimeDetailActivity;
import com.vgtech.vantop.ui.questionnaire.QuestionnaireActivity;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
import com.vgtech.vantop.ui.vacation.VacationApplyDetailsActivity;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class GetuiGTIntentService extends GTIntentService implements HttpListener<String> {

    private Context mContext;
    private static int i = 1;
    //    public static final String RECEIVER_REFRESH_DATA = "RECEIVER_REFRESH_DATA";
    public static final String NOTIFICATION_CHANNEL = "com.vgtech.vancloud";
    private static final String NOTIFICATION_CHANNEL_NAME = "App_Channel";
    public static final String PUSHRECEIVER = "com.igexin.sdk.action.cuIrqZ8qF3AhK6U1gUltM6";
    public static final String RECEIVER_PUSH = "RECEIVER_PUSH";
    /**
     * 任务
     */
    public static final String TASK = "11";
    /**
     * 日程
     */
    public static final String SCHEDULE = "9";
    /**
     * 万客流程
     */
    public static final String FLOW = "2";
    /**
     * 帮帮
     */
    public static final String HELP = "5";
    /**
     * 分享
     */
    public static final String SHARE = "3";
    /**
     * 公告
     */
    public static final String ANNOUNCEMENT = "1";
    public static final String MYNOTICE = "105";
    /**
     * 工作汇报
     */
    public static final String WORKREPORT = "7";

//    /**
//     * 单聊
//     */
//    public static final String CHAT = "7";
//
//    /**
//     * 群聊
//     */
//    public static final String GROUP = "21";

    /**
     * 离职
     */
    public static final String LEAVEOFFICE = "22";
    /**
     * 组织结构变化
     */
    public static final String ORGANIZATIONSCHANGE = "24";
    /**
     * 角色变化
     */
    public static final String ROLECHANGE = "25";
    public static final String RESUME = "27";//简历

    /**
     * 会议邀请推送
     */
    public static final String MEETING_PUSH = "29";
    /**
     * 会议预约创建 修改 取消
     */
    public static final String MEETING_APPOINTMENTMETTINGPUSH = "28";
    /**
     * 职位订阅
     */
//    public static final String POSITIONSUBSCRIPTION = "16";

    /**
     * 背景调查
     */
    public static final String BACKGROUNDINVESTIGATION = "31";
    /**
     * vantop-签卡
     */
    public static final String VANTOPSIGNCARD = "34";
    /**
     * vantop-休假
     */
    public static final String VANTOPLEAVE = "35";
    /**
     * vantop-审批流程
     */
    public static final String APPROVAL_FLOW = "37";
    /**
     * vantop-加班
     */
    public static final String VANTOPOVERTIME = "36";
    /**
     * 申请加入公司
     */
    public static final String JOINCOMPANY = "102";
    /**
     * 新员工
     */
    public static final String NEWEMPLOYEE = "103";
    private String digType = "dig";

    private static String NOTICEID = "noticeid";

//    private static final String TAG = "TAG_GetuiBroadcast";

    private static final String TAG = "TAG_GetuiService";
    private String group = "group";
    private String chat = "chat";

    public GetuiGTIntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        // 透传消息的处理
        String appid = msg.getAppid();
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
        String pkg = msg.getPkgName();
        String cid = msg.getClientId();

        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
//        Log.e(TAG, "call sendFeedbackMessage = " + (result ? "success" : "failed"));

        Log.e(TAG, "onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nmessageid = " + messageid + "\npkg = " + pkg
                + "\ncid = " + cid);
        mContext = context;
        if (payload == null) {
            Log.e(TAG, "receiver payload = null");
        } else {
            String data = new String(payload);
            Log.e(TAG, "receiver payload = " + data);

                // TODO:接收处理透传（payload）数据
                try {
//                    boolean needNofity = true;
                    long noticeID = -1;
                    final PushMessage pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(data));
                    Log.e(TAG, "pushMessage=" + pushMessage.toString());
                    LogUtils.createLogFile(context,"push.log", "pushMessage》》》》》" + pushMessage);
                    NotificationExtension notificationExtension = null;
                    String extension = null;
                    if (pushMessage.getJson().has("extension")) {
                        extension = pushMessage.getJson().getString("extension");
                    }
                    if (!TextUtils.isEmpty(extension)) {
                        notificationExtension = JsonDataFactory.getData(NotificationExtension.class, new JSONObject(extension));
                    }
                    if (MYNOTICE.equals(pushMessage.msgTypeId)) {
//                            MessageDB messageDB = new MessageDB(pushMessage.msgTypeId, pushMessage.content, pushMessage.operationType, data);
//                            noticeID = messageDB.insert(mContext);
                        int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_NOTICE);
                        PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_NOTICE, ++num);
                    } else if (ANNOUNCEMENT.equals(pushMessage.msgTypeId) && TypeUtils.OPERATION_CREATE.equals(pushMessage.operationType)) {
//                            MessageDB messageDB = new MessageDB(pushMessage.msgTypeId, pushMessage.content, pushMessage.operationType, data);
//                            noticeID = messageDB.insert(mContext);
                        int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_GONGGAO);
                        PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_GONGGAO, ++num);
                    } else if (needSaveList().contains(pushMessage.msgTypeId)) {
                        MessageDB messageDB = new MessageDB(pushMessage.msgTypeId, pushMessage.content, pushMessage.operationType, data);
                        noticeID = messageDB.insert(mContext);
                        //去除日程、任务、工作汇报新建、催办、评论
                        if (!(TypeUtils.SCHEDULE.equals(pushMessage.msgTypeId)
                                && TypeUtils.OPERATION_CREATE.equals(pushMessage.operationType)
                                || TypeUtils.TSAK.equals(pushMessage.msgTypeId)
                                && TypeUtils.OPERATION_CREATE.equals(pushMessage.operationType)
                                || TypeUtils.WORKREPORT.equals(pushMessage.msgTypeId)
                                && TypeUtils.OPERATION_CREATE.equals(pushMessage.operationType)
                        ) && !"hasten".equals(pushMessage.operationType)//待办催办
                                && !"create".equals(pushMessage.operationType)//待办新建
                                && !TypeUtils.OPERATION_COMMENT.equals(pushMessage.operationType)//评论
                                && !VANTOPLEAVE.equals(pushMessage.msgTypeId)//休假
                                && !VANTOPOVERTIME.equals(pushMessage.msgTypeId)//加班
                                && !VANTOPSIGNCARD.equals(pushMessage.msgTypeId)//签卡
                        ) {
                            int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_MSG);
                            PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_MSG, ++num);
                            //保存最新推送数量
                        } else if (TypeUtils.OPERATION_COMMENT.equals(pushMessage.operationType)) {
//                                int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_COMMENT);
//                                PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_COMMENT, ++num);
                            int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_MSG);
                            PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_MSG, ++num);
                        }else if (TypeUtils.HELP.equals(pushMessage.msgTypeId)//帮帮
                                ||TypeUtils.SHARE.equals(pushMessage.msgTypeId)
                                ||TypeUtils.VOTE.equals(pushMessage.msgTypeId)//问卷
                                ||TypeUtils.ENTRYAPPROVE.equals(pushMessage.msgTypeId)//入职审批
                                ||TypeUtils.SCHEDULE.equals(pushMessage.msgTypeId)&&!"create".equals(pushMessage.operationType)
                                ||TypeUtils.VANTOPLEAVE.equals(pushMessage.msgTypeId)&&!"create".equals(pushMessage.operationType)
                                ||TypeUtils.VANTOPOVERTIME.equals(pushMessage.msgTypeId)&&!"create".equals(pushMessage.operationType)
                                ||TypeUtils.VANTOPSIGNCARD.equals(pushMessage.msgTypeId)&&!"create".equals(pushMessage.operationType)
                        ) {
                            int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_MSG);
                            PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_MSG, ++num);
                        }
                    }
                    Intent appIntent = null;
                    if (!TextUtils.isEmpty(pushMessage.msgTypeId)) {
                        if (MYNOTICE.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, NoticeInfoActivity.class);
                            String resId = pushMessage.resId;
//                                mPlanSeq = intent.getStringExtra("Seq");
//                                mStaffNo = intent.getStringExtra("StaffNo");
//                                mCode = intent.getStringExtra("Code");
                            if (!TextUtils.isEmpty(resId)) {
                                String[] ids = resId.split(",");
                                if (ids.length > 3) {
                                    String mynotice_id = ids[0];
                                    String StaffNo = ids[1];
                                    String Seq = ids[2];
                                    String Code = ids[3];
                                    appIntent.putExtra("Seq", Seq);
                                    appIntent.putExtra("StaffNo", StaffNo);
                                    appIntent.putExtra("Code", Code);
                                    appIntent.putExtra("mynotice_id", mynotice_id);
                                    appIntent.putExtra(NOTICEID, noticeID);
                                }
                            }
//                            needNofity = false;
                        } else if (TASK.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, TaskTransactActivity.class);
                            appIntent.putExtra("TaskID", "" + pushMessage.resId);
                            appIntent.putExtra(NOTICEID, noticeID);
//                            needNofity = false;
                        } else if (FLOW.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, FlowHandleActivity.class);
                            appIntent.putExtra("processid", pushMessage.resId);
                            int type = -1;
                            if ("cc".equals(pushMessage.role)) {
                                type = 3;
                            } else if ("process".equals(pushMessage.role)) {
                                type = 2;
                            } else {
                                type = 1;
                            }
                            appIntent.putExtra("typer", type);
                            appIntent.putExtra(NOTICEID, noticeID);
//                            needNofity = false;
                        } else if (APPROVAL_FLOW.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, ProcessWebActivity.class);
                            appIntent.putExtra("flow_id", pushMessage.resId);
                            appIntent.putExtra("operation_type", pushMessage.operationType);
                            appIntent.putExtra("type", "push");
//                            needNofity = false;
                        } else if (WORKREPORT.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, WorkReportTransactActivity.class);
                            appIntent.putExtra("workreportid", pushMessage.resId);
                            appIntent.putExtra(NOTICEID, noticeID);
//                            needNofity = false;
                        } else if (SCHEDULE.equals(pushMessage.msgTypeId)) {
//                            needNofity = false;
                            appIntent = new Intent(context, ScheduleDetailActivity.class);
                            appIntent.putExtra("scheduleId", pushMessage.resId);
                            appIntent.putExtra(NOTICEID, noticeID);
                        } else if (HELP.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, HelpDetailActivity.class);
                            appIntent.putExtra("id", pushMessage.resId);
                            appIntent.putExtra(NOTICEID, noticeID);
//                            needNofity = false;
                        } else if (SHARE.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, SharedInfoActivity.class);
                            appIntent.putExtra("id", pushMessage.resId);
                            appIntent.putExtra(NOTICEID, noticeID);
//                            needNofity = false;
                        } else if (ANNOUNCEMENT.equals(pushMessage.msgTypeId)) {//公告
                            appIntent = new Intent(context, AnnouncementDetailActivity.class);
                            appIntent.putExtra("id", pushMessage.resId);
                            appIntent.putExtra(NOTICEID, noticeID);
//                            needNofity = false;
                        } else if (RESUME.equals(pushMessage.msgTypeId)) {
//                                String recruitId = pushMessage.recruitInfoId;
//                                String resume_id = pushMessage.resId;
//                                PrfUtils.countRecruit(context, recruitId, false);
//                                appIntent = new Intent(context, ResumePreviewActivity.class);
//                                appIntent.putExtra("type", 3);
//                                appIntent.putExtra("storeid", resume_id);
                        } else if (LEAVEOFFICE.equals(pushMessage.msgTypeId)) {
//                            needNofity = false;
                            appIntent = new Intent();
                        } else if (ORGANIZATIONSCHANGE.equals(pushMessage.msgTypeId)) {
                            // 组织机构发生变化
//                            needNofity = false;
                            Intent broadcIntent = new Intent(GroupReceiver.REFRESH);
                            mContext.sendBroadcast(broadcIntent);

                        } else if (ROLECHANGE.equals(pushMessage.msgTypeId)) {
//                            needNofity = false;
                            Intent broadcIntent = new Intent(GroupReceiver.PERMISSIONS_REFRESH);
                            mContext.sendBroadcast(broadcIntent);
                        } else if (MEETING_APPOINTMENTMETTINGPUSH.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent();
                            appIntent.setAction("com.vgtech.meeting.detail");
                            appIntent.putExtra("id", pushMessage.resId);
                            appIntent.putExtra(NOTICEID, noticeID);
//                            needNofity = false;
                        } else if (BACKGROUNDINVESTIGATION.equals(pushMessage.msgTypeId)) {
                            PrfUtils.saveInvestigationCount(context, 1);
                            String url = ApiUtils.generatorUrl(context, URLAddr.URL_INVESTIGATES
                                    + "?tenantId=" + PrfUtils.getTenantId(context)
                                    + "&userId=" + PrfUtils.getUserId(context)
                                    + "&orderInfoId=" + pushMessage.resId);
                            appIntent = new Intent(context, BjdcWebActivity.class);
                            appIntent.putExtra("title", context.getString(R.string.lable_jcxq));
                            appIntent.putExtra("url", url);
//                            needNofity = false;
                        } else if (JOINCOMPANY.equals(pushMessage.msgTypeId)) {
                            appIntent = new Intent(context, StaffApplysActivity.class);
                        } else if (VANTOPLEAVE.equals(pushMessage.msgTypeId)) {
                            //Vantop请假
                            if ("hasten".equals(pushMessage.operationType) || "create".equals(pushMessage.operationType)) {
//                                needNofity = false;
                                if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
                                    appIntent = new Intent(context, VacationApplyDetailsActivity.class);
                                    appIntent.putExtra("id", pushMessage.resId);
                                    appIntent.putExtra("staffno", notificationExtension.content);
                                    appIntent.putExtra("type", false);
                                }
                                Intent todoIntent = new Intent(ToDoNotificationNewListFragment.PUSH_TODO_MESSAGE);
                                todoIntent.putExtra("id", pushMessage.resId);
                                mContext.sendBroadcast(todoIntent);
                            } else if ("chexiao".equals(pushMessage.operationType)) {
//                                needNofity = false;
                                appIntent = null;
                            } else {
//                                needNofity = false;
                                appIntent = new Intent(context, ApprovalListActivity.class);
                                appIntent.putExtra("tag", "shenqing_vantop_holiday");

                            }

                        } else if (VANTOPOVERTIME.equals(pushMessage.msgTypeId)) {

                            if ("hasten".equals(pushMessage.operationType) || "create".equals(pushMessage.operationType)) {
                                //Vantop加班
                                if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
//                                    needNofity = false;
                                    appIntent = new Intent(context, OverTimeDetailActivity.class);
                                    appIntent.putExtra("taskId", pushMessage.resId);
                                    appIntent.putExtra("staffno", notificationExtension.content);
                                    appIntent.putExtra("type", false);
                                }
                                Intent todoIntent = new Intent(ToDoNotificationNewListFragment.PUSH_TODO_MESSAGE);
                                todoIntent.putExtra("id", pushMessage.resId);
                                mContext.sendBroadcast(todoIntent);
                            } else if ("chexiao".equals(pushMessage.operationType)) {
//                                needNofity = false;
                                appIntent = null;

                            } else {
//                                    appIntent = new Intent(context, OverTimeDetailActivity.class);
//                                    appIntent.putExtra("taskId", pushMessage.resId);
//                                needNofity = false;
                                appIntent = new Intent(context, ApprovalListActivity.class);
                                appIntent.putExtra("tag", "shenqing_extra_work");
                            }

                        } else if (VANTOPSIGNCARD.equals(pushMessage.msgTypeId)) {
                            //Vantop签卡
                            if ("hasten".equals(pushMessage.operationType) || "create".equals(pushMessage.operationType)) {
//                                needNofity = false;
                                if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
//                                        appIntent = new Intent(context, SignedCardApprovalDetailsActivity.class);
//                                        appIntent.putExtra("taskId", pushMessage.resId);
//                                        appIntent.putExtra("staffNo", notificationExtension.content);
                                    appIntent = new Intent(context, MyApprovalActivity.class);
                                    appIntent.putExtra("tag", AppPermission.Shenqing.shenqing_sign_card.toString());
                                }
                                Intent todoIntent = new Intent(ToDoNotificationNewListFragment.PUSH_TODO_MESSAGE);
                                todoIntent.putExtra("id", pushMessage.resId);
                                mContext.sendBroadcast(todoIntent);
                            } else if ("chexiao".equals(pushMessage.operationType)) {
//                                needNofity = false;
                                appIntent = null;
                            } else {
//                                needNofity = false;
                                appIntent = new Intent(context, ApprovalListActivity.class);
                                appIntent.putExtra("tag", "shenqing_sign_card");

                            }

                        } else if (NEWEMPLOYEE.equals(pushMessage.msgTypeId)) {

                            if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
//                                needNofity = false;
                                if (TenantPresenter.isVanTop(context)) {
                                    appIntent = new Intent(context, VantopUserInfoActivity.class);

                                    appIntent.putExtra(VantopUserInfoActivity.BUNDLE_USERID, notificationExtension.content);
                                } else {
                                    appIntent = new Intent(context, SelfInfoActivity.class);
                                    appIntent.putExtra("userId", notificationExtension.content);
                                    appIntent.putExtra("type", "0");
                                }
                            }
                        }else if ("38".equals(pushMessage.msgTypeId)) {//入职审批
                            appIntent = new Intent(context, HttpWebActivity.class);
                            appIntent.putExtra("code", Constants.ENTRYAPPROVE_CODE);
                        }else if ("39".equals(pushMessage.msgTypeId)) {//调查问卷
                            appIntent = new Intent(context, QuestionnaireActivity.class);
                        }
                        NoticeUtils.updateAppNum(context);
                        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
                        int bell = preferences.getInt("PREF_TIP_MSG", 1);

                        if (bell != 0) {
                            if (!digType.equals(pushMessage.operationType)) {
                                String url = "";
                                if (chat.equals(pushMessage.type) || group.equals(pushMessage.type)) {
                                    if (NoticeUtils.isBackground(context)) {
                                        String id = "";
                                        String name = "";
                                        NewUser user = JsonDataFactory.getData(NewUser.class, pushMessage.getJson().getJSONObject("user"));
                                        if (chat.equals(pushMessage.type)) {
                                            id = user.userid;
                                            name = user.name;
                                        } else {
                                            id = pushMessage.groupId;
                                            name = pushMessage.groupName;
                                        }
                                        CharSequence content = EmojiFragment.getEmojiContent(context, 0, pushMessage.content);
                                        url = user.photo;
                                        NoticeUtils.showChatNotify(context, id, name, url, content, pushMessage.type);
                                        Log.e("TAG_GetuiService","聊天="+pushMessage.type);
                                    }

                                }
                            }
                        }
                        Log.e("TAG_GetuiService","msgTypeId非空="+appIntent);

                        if (LEAVEOFFICE.equals(pushMessage.msgTypeId)) {

                            Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_LEAVEOFFICE);
                            context.sendBroadcast(reveiverIntent);
                        }
                        notifycationAndroid26(context, pushMessage, appIntent, "");

                        Intent broadcIntent = new Intent(RECEIVER_PUSH);
                        broadcIntent.putExtra("infotype", pushMessage.msgTypeId);
                        broadcIntent.putExtra("infoid", pushMessage.resId);
                        mContext.sendBroadcast(broadcIntent);
                    } else {

                        if ("chexiao".equals(pushMessage.operationType)) {
                            MessageDB messageDB = new MessageDB(pushMessage.msgTypeId, pushMessage.content, pushMessage.operationType, data);
                            noticeID = messageDB.insert(mContext);

                            //去除日程、任务、工作汇报新建、催办、评论
                            if (!(TypeUtils.SCHEDULE.equals(pushMessage.msgTypeId) && TypeUtils.OPERATION_CREATE.equals(pushMessage.operationType)
                                    || TypeUtils.TSAK.equals(pushMessage.msgTypeId) && TypeUtils.OPERATION_CREATE.equals(pushMessage.operationType)
                                    || TypeUtils.WORKREPORT.equals(pushMessage.msgTypeId) && TypeUtils.OPERATION_CREATE.equals(pushMessage.operationType)
                            ) && !"hasten".equals(pushMessage.operationType)//待办催办
                                    && !"create".equals(pushMessage.operationType)//待办新建
                                    && !TypeUtils.OPERATION_COMMENT.equals(pushMessage.operationType)//评论
                                    && !VANTOPLEAVE.equals(pushMessage.msgTypeId)//休假
                                    && !VANTOPOVERTIME.equals(pushMessage.msgTypeId)//加班
                                    && !VANTOPSIGNCARD.equals(pushMessage.msgTypeId)//签卡
                            ) {
                                int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_MSG);
                                PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_MSG, ++num);
                                //保存最新推送数量
                            } else if (TypeUtils.OPERATION_COMMENT.equals(pushMessage.operationType)) {
//                                int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_COMMENT);
//                                PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_COMMENT, ++num);
                                int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_MSG);
                                PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_MSG, ++num);
                            }else if (TypeUtils.HELP.equals(pushMessage.msgTypeId)//帮帮
                                    ||TypeUtils.SHARE.equals(pushMessage.msgTypeId)
                                    ||TypeUtils.SCHEDULE.equals(pushMessage.msgTypeId)) {
                                int num = PrfUtils.getMessageCount(mContext, PrfUtils.MESSAGE_MSG);
                                PrfUtils.setMessageCountCount(mContext, PrfUtils.MESSAGE_MSG, ++num);
                            }
                            Log.e("TAG_GetuiService","chexiao="+pushMessage.operationType);
                            appIntent = new Intent(context, MessageListNewActivity.class);
                            notifycationAndroid26(context, pushMessage, appIntent, "");

                            Intent broadcIntent = new Intent(RECEIVER_PUSH);
                            broadcIntent.putExtra("infotype", pushMessage.msgTypeId);
                            broadcIntent.putExtra("infoid", pushMessage.resId);
                            mContext.sendBroadcast(broadcIntent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }


    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        sendMessage(context, clientid, 1);
        LogUtils.createLogFile(context,"CID.log","clientid》》》》》"+clientid);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        Log.e(TAG, "个推是否在线online -> " + online);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
        Log.e(TAG, "onReceiveCommandResult -> " + cmdMessage.getClientId());
        Log.e(TAG, "action -> " + cmdMessage.getAction());
        int action = cmdMessage.getAction();
        if (action == PushConsts.SET_TAG_RESULT) {
            setTagResult((SetTagCmdMessage) cmdMessage);
        } else if (action == PushConsts.BIND_ALIAS_RESULT) {
//            updateClientId(context, cmdMessage.getClientId());
            bindAliasResult((BindAliasCmdMessage) cmdMessage);
        } else if (action == PushConsts.UNBIND_ALIAS_RESULT) {
            unbindAliasResult((UnBindAliasCmdMessage) cmdMessage);
        } else if ((action == PushConsts.THIRDPART_FEEDBACK)) {
            feedbackResult((FeedbackCmdMessage) cmdMessage);
        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage msg) {
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage msg) {
    }

    private void bindAliasResult(BindAliasCmdMessage bindAliasCmdMessage) {
        String sn = bindAliasCmdMessage.getSn();
        String code = bindAliasCmdMessage.getCode();
        Log.e(TAG, "code -> " + bindAliasCmdMessage.getCode());
        int text = R.string.bind_alias_unknown_exception;
        switch (Integer.valueOf(code)) {
            case PushConsts.BIND_ALIAS_SUCCESS:
                text = R.string.bind_alias_success;

                break;
            case PushConsts.ALIAS_ERROR_FREQUENCY:
                text = R.string.bind_alias_error_frequency;
                break;
            case PushConsts.ALIAS_OPERATE_PARAM_ERROR:
                text = R.string.bind_alias_error_param_error;
                break;
            case PushConsts.ALIAS_REQUEST_FILTER:
                text = R.string.bind_alias_error_request_filter;
                break;
            case PushConsts.ALIAS_OPERATE_ALIAS_FAILED:
                text = R.string.bind_alias_unknown_exception;
                break;
            case PushConsts.ALIAS_CID_LOST:
                text = R.string.bind_alias_error_cid_lost;
                break;
            case PushConsts.ALIAS_CONNECT_LOST:
                text = R.string.bind_alias_error_connect_lost;
                break;
            case PushConsts.ALIAS_INVALID:
                text = R.string.bind_alias_error_alias_invalid;
                break;
            case PushConsts.ALIAS_SN_INVALID:
                text = R.string.bind_alias_error_sn_invalid;
                break;
            default:
                break;

        }

        Log.e(TAG, "bindAlias result sn = " + sn + ", code = " + code + ", text = " + getResources().getString(text));

    }

    private void unbindAliasResult(UnBindAliasCmdMessage unBindAliasCmdMessage) {
        String sn = unBindAliasCmdMessage.getSn();
        String code = unBindAliasCmdMessage.getCode();
        Log.e(TAG, "code -> " + unBindAliasCmdMessage.getCode());
        int text = R.string.unbind_alias_unknown_exception;
        switch (Integer.valueOf(code)) {
            case PushConsts.UNBIND_ALIAS_SUCCESS:
                text = R.string.unbind_alias_success;
                break;
            case PushConsts.ALIAS_ERROR_FREQUENCY:
                text = R.string.unbind_alias_error_frequency;
                break;
            case PushConsts.ALIAS_OPERATE_PARAM_ERROR:
                text = R.string.unbind_alias_error_param_error;
                break;
            case PushConsts.ALIAS_REQUEST_FILTER:
                text = R.string.unbind_alias_error_request_filter;
                break;
            case PushConsts.ALIAS_OPERATE_ALIAS_FAILED:
                text = R.string.unbind_alias_unknown_exception;
                break;
            case PushConsts.ALIAS_CID_LOST:
                text = R.string.unbind_alias_error_cid_lost;
                break;
            case PushConsts.ALIAS_CONNECT_LOST:
                text = R.string.unbind_alias_error_connect_lost;
                break;
            case PushConsts.ALIAS_INVALID:
                text = R.string.unbind_alias_error_alias_invalid;
                break;
            case PushConsts.ALIAS_SN_INVALID:
                text = R.string.unbind_alias_error_sn_invalid;
                break;
            default:
                break;

        }

        Log.e(TAG, "unbindAlias result sn = " + sn + ", code = " + code + ", text = " + getResources().getString(text));

    }

    private void setTagResult(SetTagCmdMessage setTagCmdMsg) {
        String sn = setTagCmdMsg.getSn();
        String code = setTagCmdMsg.getCode();
        Log.e(TAG, "code -> " + setTagCmdMsg.getCode());
        int text = R.string.add_tag_unknown_exception;
        switch (Integer.valueOf(code)) {
            case PushConsts.SETTAG_SUCCESS:
                text = R.string.add_tag_success;
                break;

            case PushConsts.SETTAG_ERROR_COUNT:
                text = R.string.add_tag_error_count;
                break;

            case PushConsts.SETTAG_ERROR_FREQUENCY:
                text = R.string.add_tag_error_frequency;
                break;

            case PushConsts.SETTAG_ERROR_REPEAT:
                text = R.string.add_tag_error_repeat;
                break;

            case PushConsts.SETTAG_ERROR_UNBIND:
                text = R.string.add_tag_error_unbind;
                break;

            case PushConsts.SETTAG_ERROR_EXCEPTION:
                text = R.string.add_tag_unknown_exception;
                break;

            case PushConsts.SETTAG_ERROR_NULL:
                text = R.string.add_tag_error_null;
                break;

            case PushConsts.SETTAG_NOTONLINE:
                text = R.string.add_tag_error_not_online;
                break;

            case PushConsts.SETTAG_IN_BLACKLIST:
                text = R.string.add_tag_error_black_list;
                break;

            case PushConsts.SETTAG_NUM_EXCEED:
                text = R.string.add_tag_error_exceed;
                break;

            default:
                break;
        }

        Log.e(TAG, "settag result sn = " + sn + ", code = " + code + ", text = " + getResources().getString(text));
    }


    private void feedbackResult(FeedbackCmdMessage feedbackCmdMsg) {
        String appid = feedbackCmdMsg.getAppid();
        String taskid = feedbackCmdMsg.getTaskId();
        String actionid = feedbackCmdMsg.getActionId();
        String result = feedbackCmdMsg.getResult();
        long timestamp = feedbackCmdMsg.getTimeStamp();
        String cid = feedbackCmdMsg.getClientId();

        Log.e(TAG, "onReceiveCommandResult -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nactionid = " + actionid + "\nresult = " + result
                + "\ncid = " + cid + "\ntimestamp = " + timestamp);
    }

    private void notifycationAndroid26(Context context, PushMessage pushMessage, Intent appIntent, String s) {
        if (appIntent !=null)
            Log.e("TAG_GetuiService","operationType="+pushMessage.msgTypeId+"；appIntent="+appIntent.getClass().getSimpleName());
        int id = i++;
        PendingIntent contentIntent = PendingIntent.getActivity(context, id, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(mChannel);
            notification = new NotificationCompat
                    .Builder(context, NOTIFICATION_CHANNEL)
                    .setChannelId(NOTIFICATION_CHANNEL)
                    .setContentTitle(pushMessage.title)
                    .setContentText(pushMessage.content)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH).build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(pushMessage.title)
                    .setContentText(pushMessage.content)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setOngoing(true);
            notification = notificationBuilder.build();
        }
        notificationManager.notify(id, notification);
        AppShortCutUtil.setCount(id, context);
    }

    private void updateClientId(Context context, String clientid) {
        VanCloudApplication application = (VanCloudApplication) context
                .getApplicationContext();
        NetworkManager networkManager = application.getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(context));
        params.put("tenantid", PrfUtils.getTenantId(context));
        params.put("devicetype", "android");
        params.put("clientid", clientid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_VCHAT_PNS), params, context);
        networkManager.load(1, path, this);
    }

    /**
     * 需要保存到数据库的通知类型
     *
     * @return
     */
    public static List<String> needSaveList() {

        List<String> list = new ArrayList<>();
        list.add(TASK);
        list.add(SCHEDULE);
        list.add(SHARE);
        list.add(ANNOUNCEMENT);
        list.add(HELP);
        list.add(FLOW);//流程,已弃用
        list.add(WORKREPORT);
        list.add(MEETING_APPOINTMENTMETTINGPUSH);
        list.add(BACKGROUNDINVESTIGATION);
        list.add(VANTOPSIGNCARD);//签卡
        list.add(VANTOPLEAVE);//请假
        list.add(VANTOPOVERTIME);//加班
        list.add(JOINCOMPANY);
        list.add(NEWEMPLOYEE);
        list.add(APPROVAL_FLOW);//流程
        return list;
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            handler.postDelayed(this, 2000);
        }
    };

    private void sendMessage(Context context, String clientId, int what) {
        Log.e(TAG, "CID=" + clientId );
        VanCloudApplication vanCloudApplication = (VanCloudApplication) getApplication();
        vanCloudApplication.sendMessage(clientId);

    }
    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

//        boolean safe = ActivityUtils.prehandleNetworkData(mContext, this, callbackId, path, rootData, true);
//        Log.e("push", "upload_clientid" + safe);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}