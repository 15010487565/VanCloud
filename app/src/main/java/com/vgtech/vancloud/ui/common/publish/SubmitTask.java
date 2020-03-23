package com.vgtech.vancloud.ui.common.publish;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.vgtech.common.utils.EditionUtils;
import com.vgtech.common.utils.PublishConstants;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.FileInfo;
import com.vgtech.common.api.HelpListItem;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.MapItem;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.vancloud.ui.common.publish.json.JsonAudio;
import com.vgtech.vancloud.ui.common.publish.json.JsonComment;
import com.vgtech.vancloud.ui.common.publish.json.JsonImage;
import com.vgtech.vancloud.ui.common.publish.json.JsonUser;
import com.vgtech.vancloud.ui.common.publish.module.PapplyResume;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.common.publish.module.Pannouncement;
import com.vgtech.vancloud.ui.common.publish.module.Pcomment;
import com.vgtech.vancloud.ui.common.publish.module.Pflow;
import com.vgtech.vancloud.ui.common.publish.module.Phelp;
import com.vgtech.vancloud.ui.common.publish.module.Pschedule;
import com.vgtech.vancloud.ui.common.publish.module.Pshared;
import com.vgtech.vancloud.ui.common.publish.module.Ptask;
import com.vgtech.vancloud.ui.common.publish.module.PworkReport;
import com.vgtech.common.utils.AlarmUtils;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.image.ImageUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vgtech.vancloud.reciver.AlarmReceiver.SCHEDULEACTION;

/**
 * Created by zhangshaofang on 2015/9/17.
 */
public class SubmitTask implements Runnable, HttpListener<String> {
    public static final int CALLBACK_IMAGE = 1;
    public static final int CALLBACK_AUDIO = 2;
    public static final int CALLBACK_ATTACHMENT = 3;
    public static final int CALLBACK_CONTENT = 4;
    public static final int CALLBACK_COMMENT = 5;
    private NetworkManager mNetworkManager;
    private PublishTask mPublishTask;
    private Context mContext;
    private String RECEIVER_ERROR = "RECEIVER_ERROR";

    public SubmitTask(Context context, NetworkManager networkManager, PublishTask publishTask) {
        mContext = context;
        mNetworkManager = networkManager;
        mPublishTask = publishTask;
    }

    @Override
    public void run() {
        submitImageData();
    }

    public void submitImageData() {
//        Toast.makeText(mContext, R.string.toast_submit_service, Toast.LENGTH_SHORT).show();
        showNotify(R.mipmap.sending, mContext.getString(R.string.toast_sending));
//        try {
//            Thread.sleep(5 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String imagePath = mPublishTask.image;
        int type = 0;
        switch (mPublishTask.type) {
            case PublishConstants.PUBLISH_TASK_CONDUCT:
            case PublishConstants.PUBLISH_TASK:
                type = 11;
                break;
            case PublishConstants.PUBLISH_RECRUIT_FINISH:
            case PublishConstants.PUBLISH_RESUME_APPROVE:
            case PublishConstants.PUBLISH_RECRUIT_APPROVE:
            case PublishConstants.PUBLISH_APPLYBUYRESUME:
                type = 13;
                break;
            case PublishConstants.PUBLISH_SCHEDULE_CONDUCT:
            case PublishConstants.PUBLISH_SCHEDULE:
                type = 9;
                break;
            case PublishConstants.PUBLISH_WORK_REPORT:
            case PublishConstants.PUBLISH_WORKREPORT:
                type = 7;
                break;
            case PublishConstants.PUBLISH_HELP:
                type = 5;
                break;
            case PublishConstants.PUBLISH_FLOW_CONDUCT:
            case PublishConstants.PUBLISH_FLOW:
            case PublishConstants.PUBLISH_FLOW_LEAVE:
                type = 2;
                break;
            case PublishConstants.PUBLISH_ANNOUNCEMENT:
                type = 1;
                break;
            case PublishConstants.PUBLISH_FEEDBACK:
                type = 15;
                break;
            case PublishConstants.PUBLISH_SHARED:
                type = 3;
                break;
            case PublishConstants.PUBLISH_COMMENT:
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    type = pcomment.commentType;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if (!TextUtils.isEmpty(imagePath)) {
            String[] paths = imagePath.split(",");
            List<FilePair> imgPairList = new ArrayList<>();
            for (String path : paths) {
                if (!ImageInfo.isLocal(path)) {
                    String imageId = ImageInfo.getImageId(path);
                    if (TextUtils.isEmpty(mImageIds)) {
                        mImageIds = "";
                    }
                    StringBuilder stringBuilder = new StringBuilder(mImageIds);
                    if (!TextUtils.isEmpty(stringBuilder.toString())) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(imageId);
                    mImageIds = stringBuilder.toString();
                } else {
                    Bitmap bm = Bimp.getimage(path);
                    bm = ImageUtility.checkFileDegree(path, bm);
                    String newStr = "";
                    try {
                        newStr = path.substring(
                                path.lastIndexOf("/") + 1,
                                path.lastIndexOf("."));
                    } catch (Exception e) {
                        newStr = String.valueOf(System.currentTimeMillis());
                    }
                    path = FileUtils.saveBitmap(mContext, bm, "" + newStr);
                    File file = new File(path);
                    if (file.exists()) {
                        FilePair filePair = new FilePair("pic", file);
                        imgPairList.add(filePair);
                    }
                }

            }
            if (!imgPairList.isEmpty()) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ownid", PrfUtils.getUserId(mContext));
                params.put("tenantid", PrfUtils.getTenantId(mContext));
                params.put("type", String.valueOf(type));
                NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_IMAGE), params, imgPairList);
                mNetworkManager.load(CALLBACK_IMAGE, path, this);
            } else {
                if (mImageIds.endsWith(",")) {
                    mImageIds = new StringBuffer(mImageIds).deleteCharAt(mImageIds.length() - 1).toString();
                }
                submitAudioData();
            }

        } else {
            submitAudioData();
        }
    }

    public void submitAudioData() {
        String audioPath = mPublishTask.audio;
        String audioTime = mPublishTask.time;
        int type = 0;
        switch (mPublishTask.type) {
            case PublishConstants.PUBLISH_TASK_CONDUCT:
            case PublishConstants.PUBLISH_TASK:
                type = 11;
                break;
            case PublishConstants.PUBLISH_RECRUIT_FINISH:
            case PublishConstants.PUBLISH_RESUME_APPROVE:
            case PublishConstants.PUBLISH_RECRUIT_APPROVE:
            case PublishConstants.PUBLISH_APPLYBUYRESUME:
                type = 13;
                break;
            case PublishConstants.PUBLISH_SCHEDULE_CONDUCT:
            case PublishConstants.PUBLISH_SCHEDULE:
                type = 9;
                break;
            case PublishConstants.PUBLISH_WORK_REPORT:
            case PublishConstants.PUBLISH_WORKREPORT:
                type = 7;
                break;
            case PublishConstants.PUBLISH_HELP:
                type = 5;
                break;
            case PublishConstants.PUBLISH_FLOW_CONDUCT:
            case PublishConstants.PUBLISH_FLOW:
            case PublishConstants.PUBLISH_FLOW_LEAVE:
                type = 2;
                break;
            case PublishConstants.PUBLISH_ANNOUNCEMENT:
                type = 1;
                break;
            case PublishConstants.PUBLISH_FEEDBACK:
                type = 15;
                break;
            case PublishConstants.PUBLISH_SHARED:
                type = 3;
                break;
            case PublishConstants.PUBLISH_COMMENT:
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    type = pcomment.commentType;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if (!TextUtils.isEmpty(audioPath)) {
            String[] paths = audioPath.split(",");
            String[] times = audioTime.split(",");
            List<FilePair> imgPairList = new ArrayList<>();
            StringBuilder stime = new StringBuilder();
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                if (!AudioInfo.isLocal(path)) {
                    long imageId = AudioInfo.getAudioId(path);
                    if (TextUtils.isEmpty(mAudioIds)) {
                        mAudioIds = "";
                    }
                    StringBuilder stringBuilder = new StringBuilder(mAudioIds);
                    if (!TextUtils.isEmpty(stringBuilder.toString())) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(imageId);
                    mAudioIds = stringBuilder.toString();
                } else {
                    stime.append(times[i]).append(",");
                    FilePair filePair = new FilePair("audio", new File(path));
                    imgPairList.add(filePair);
                }
            }
            if (!TextUtils.isEmpty(stime)) {
                audioTime = stime.deleteCharAt(stime.length() - 1).toString();
            }
            if (!imgPairList.isEmpty()) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ownid", PrfUtils.getUserId(mContext));
                params.put("tenantid", PrfUtils.getTenantId(mContext));
                params.put("type", String.valueOf(type));
                params.put("time", audioTime);
                NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_AUDIO), params, imgPairList);
                mNetworkManager.load(CALLBACK_AUDIO, path, this);
            } else {
                if (mAudioIds.endsWith(",")) {
                    mAudioIds = new StringBuffer(mAudioIds).deleteCharAt(mAudioIds.length() - 1).toString();
                }
                submitAttachmentData();
            }


        } else {
            submitAttachmentData();
        }
    }


    public void submitAttachmentData() {
        String attachmentPath = mPublishTask.attachment;
//        String audioTime = mPublishTask.time;
        int type = 0;
        switch (mPublishTask.type) {
            case PublishConstants.PUBLISH_TASK_CONDUCT:
            case PublishConstants.PUBLISH_TASK:
                type = 11;
                break;
            case PublishConstants.PUBLISH_SCHEDULE_CONDUCT:
            case PublishConstants.PUBLISH_SCHEDULE:
                type = 9;
                break;
            case PublishConstants.PUBLISH_WORK_REPORT:
            case PublishConstants.PUBLISH_WORKREPORT:
                type = 7;
                break;
            case PublishConstants.PUBLISH_HELP:
                type = 5;
                break;
            case PublishConstants.PUBLISH_FLOW_CONDUCT:
            case PublishConstants.PUBLISH_FLOW:
            case PublishConstants.PUBLISH_FLOW_LEAVE:
                type = 2;
                break;
            case PublishConstants.PUBLISH_ANNOUNCEMENT:
                type = 1;
                break;
            case PublishConstants.PUBLISH_FEEDBACK:
                type = 15;
                break;
            case PublishConstants.PUBLISH_SHARED:
                type = 3;
                break;
            case PublishConstants.PUBLISH_COMMENT:
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    type = pcomment.commentType;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if (!TextUtils.isEmpty(attachmentPath)) {
            String[] paths = attachmentPath.split(",");
//            String[] times = audioTime.split(",");
            List<FilePair> attachmentPairList = new ArrayList<>();
            StringBuilder stime = new StringBuilder();
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                if (!AudioInfo.isLocal(path)) {
                    String imageId = AudioInfo.getFid(path);
                    if (TextUtils.isEmpty(mAttachmetIds)) {
                        mAttachmetIds = "";
                    }
                    StringBuilder stringBuilder = new StringBuilder(mAttachmetIds);
                    if (!TextUtils.isEmpty(stringBuilder.toString())) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(imageId);
                    mAttachmetIds = stringBuilder.toString();
                } else {
//                    stime.append(times[i]).append(",");
                    FilePair filePair = new FilePair("attach", new File(path));
                    attachmentPairList.add(filePair);
                }
            }
//            if (!TextUtils.isEmpty(stime)) {
//                audioTime = stime.deleteCharAt(stime.length() - 1).toString();
//            }
            if (!attachmentPairList.isEmpty()) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ownid", PrfUtils.getUserId(mContext));
                params.put("tenantid", PrfUtils.getTenantId(mContext));
                params.put("type", String.valueOf(type));
                NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_ATTACHMENT), params, attachmentPairList);
                mNetworkManager.load(CALLBACK_ATTACHMENT, path, this);
            } else {
                submitContent();
            }
        } else {
            submitContent();
        }
    }

    private NotificationManager notificationManager;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showNotify(int resIcon, String msg) {
        if (notificationManager == null)
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder1 = new Notification.Builder(mContext);
        builder1.setSmallIcon(resIcon); //设置图标
        builder1.setTicker(mContext.getString(R.string.app_name));
        builder1.setContentTitle( mContext.getString(R.string.app_name)); //设置标题
        builder1.setContentText(msg); //消息内容
        builder1.setWhen(System.currentTimeMillis()); //发送时间
        builder1.setDefaults(Notification.DEFAULT_ALL); //设置默认的提示音，振动方式，灯光
        builder1.setAutoCancel(true);//打开程序后图标消失
//        PendingIntent pendingIntent =PendingIntent.getActivity(context, 0, intent, 0);
        // 问题就在这里的id了
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent  = PendingIntent.getActivity(mContext, 0, intent,0);
        builder1.setContentIntent(pendingIntent);
        Notification notification = builder1.build();
        notificationManager.notify(resIcon, notification);
        notificationManager.cancel(resIcon);

//        Notification notification = new Notification(resIcon,
//                mContext.getString(R.string.app_name), System.currentTimeMillis());
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        intent.setClass(mContext, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//        notification.tickerText = msg;
//        PendingIntent contextIntent = PendingIntent.getActivity(mContext, 0,
//                intent, 0);
//        notification.setLatestEventInfo(mContext,
//                mContext.getString(R.string.app_name), msg, contextIntent);
//        notificationManager.notify(resIcon, notification);
//        notificationManager.cancel(resIcon);
    }

    public void submitContent() {
        Map<String, String> params = new HashMap<String, String>();
        String userId = PrfUtils.getUserId(mContext);
        String tenantId = PrfUtils.getTenantId(mContext);
        params.put("ownid", userId);
        params.put("user_id", userId);
        if (mPublishTask.type == PublishConstants.PUBLISH_FEEDBACK) {
            tenantId = "0";
            String edition = EditionUtils.getCurrentEdition(mContext);
            if (EditionUtils.EDITION_PERSONAL.equals(edition)) {
                params.put("source", "personal_app");
            } else {
                params.put("source", "tenant_app");
            }
        }

        params.put("tenantid", tenantId);
        params.put("tenant_id", tenantId);
        String url = "";
        switch (mPublishTask.type) {

            case PublishConstants.PUBLISH_FLOW: {
                url = URLAddr.URL_PROCESS_CREATE;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pflow mTask = JsonDataFactory.getData(Pflow.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId))
                        params.put("processId", mPublishTask.publishId);
                    params.put("processtype", "1");
                    params.put("content", mTask.content);
                    params.put("processerid", mTask.processerid);
                    params.put("receiverids", mTask.receiverids);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_APPLYBUYRESUME: {
                url = URLAddr.URL_RECRUIT_PURCHASE_APPLICATION_CREATE;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    PapplyResume pcomment = JsonDataFactory.getData(PapplyResume.class, new JSONObject(mPublishTask.content));
                    params.put("content", pcomment.content);
                    params.put("resume_ids", pcomment.resume_ids);
                    params.put("receiverids", pcomment.receiverids);
                    params.put("recruit_id", pcomment.recruit_id);
                    params.put("approval_user_id", pcomment.approval_user_id);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_SCHEDULE_CONDUCT: {
                url = URLAddr.URL_SCHEDULE_CONDUCT;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    params.put("content", pcomment.content);
                    params.put("calendarid", pcomment.commentId);
                    params.put("state", "" + pcomment.commentType);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_FLOW_CONDUCT: {
                url = URLAddr.URL_PROCESS_PROCESSING;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    params.put("content", pcomment.content);
                    params.put("processid", pcomment.commentId);
                    params.put("process", "" + pcomment.commentType);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_RECRUIT_FINISH: {
                url = URLAddr.URL_RECRUIT_TASK_PROCESSING;
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    url = ApiUtils.generatorUrl(mContext, url);
                    params.put("content", pcomment.content);
                    params.put("resource_id", pcomment.commentId);
                    params.put("status", "finished");
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_RESUME_APPROVE:
            case PublishConstants.PUBLISH_RECRUIT_APPROVE: {
                url = URLAddr.URL_RECRUIT_WORKFLOW_PROCESSING;
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    String type = "";
                    if (mPublishTask.type == PublishConstants.PUBLISH_RECRUIT_APPROVE) {
                        type = "recruit";
                    } else {
                        type = "resume";
                    }
                    String status = "";
                    if (pcomment.commentType == 1) {
                        status = "approved";
                    } else if (pcomment.commentType == 2) {
                        status = "rebutted";
                    }
                    url = ApiUtils.generatorUrl(mContext, url);
                    params.put("content", pcomment.content);
                    params.put("resource_id", pcomment.commentId);
                    params.put("type", type);
                    params.put("status", status);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_TASK_CONDUCT: {
                url = URLAddr.URL_TASK_CONDUCT;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    params.put("content", pcomment.content);
                    params.put("taskid", pcomment.commentId);
                    params.put("state", "" + pcomment.commentType);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_WORK_REPORT: {
                url = URLAddr.URL_WORKREPORT_CONDUCT;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    params.put("content", pcomment.content);
                    params.put("workreportid", pcomment.commentId);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_FEEDBACK: {
                url = ApiUtils.generatorUrl(mContext, URLAddr.URL_SUPPORT_FEEDBACK);
                params.put("tenantid", PrfUtils.getTenantId(mContext));
                params.put("content", mPublishTask.content);
            }
            break;
            case PublishConstants.PUBLISH_COMMENT: {
                url = ApiUtils.generatorUrl(mContext, URLAddr.URL_SUPPORT_ADDCOMMENT);
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(pcomment.replyuserid))
                        params.put("replyuserid", pcomment.replyuserid);
                    params.put("content", pcomment.content);
                    params.put("type", "" + pcomment.commentType);
                    params.put("typeid", pcomment.commentId);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_FLOW_LEAVE: {
                url = URLAddr.URL_PROCESS_CREATE;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pflow mTask = JsonDataFactory.getData(Pflow.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId))
                        params.put("processId", mPublishTask.publishId);

                    params.put("processtype", "2");
                    Gson gson = new Gson();
                    List<MapItem> items = new ArrayList<MapItem>();
                    items.add(new MapItem(mContext.getString(R.string.start_time), String.valueOf(mTask.startTime)));
                    items.add(new MapItem(mContext.getString(R.string.end_time), String.valueOf(mTask.endTime)));
                    items.add(new MapItem(mContext.getString(R.string.leave_type), String.valueOf(mTask.leaveType)));
                    items.add(new MapItem(mContext.getString(R.string.leave_time_length), String.valueOf(mTask.leaveTime)));
                    params.put("leaveinfo", gson.toJson(items));
                    params.put("content", mTask.content);
                    params.put("processerid", mTask.processerid);
                    params.put("receiverids", mTask.receiverids);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_FORWARD: {
                try {
                    JSONObject jsonObject = new JSONObject(mPublishTask.content);
                    int subType = jsonObject.getInt("subType");
                    switch (subType) {
                        case 1: {
                            url = ApiUtils.generatorUrl(mContext, URLAddr.URL_HELP_CREATE);
                            try {
                                Phelp mTask = JsonDataFactory.getData(Phelp.class, new JSONObject(mPublishTask.content));
                                if (!TextUtils.isEmpty(mPublishTask.publishId)) {
                                    url = ApiUtils.generatorUrl(mContext, URLAddr.URL_HELP_FORWARD);
                                    HelpListItem item = JsonDataFactory.getData(HelpListItem.class, new JSONObject(mPublishTask.publishId));
                                    HelpListItem sharedItem = item.getData(HelpListItem.class);
                                    if (sharedItem != null)
                                        params.put("helpid", sharedItem.helpId);
                                    else
                                        params.put("helpid", item.helpId);
                                }
                                params.put("content", mTask.content);
                                params.put("receiverids", mTask.receiverids);
                                params.put("userid", PrfUtils.getUserId(mContext));
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                        case 2: {
                            url = ApiUtils.generatorUrl(mContext, URLAddr.URL_SHARED_CREATE);
                            try {
                                Pshared mTask = JsonDataFactory.getData(Pshared.class, new JSONObject(mPublishTask.content));
                                if (!TextUtils.isEmpty(mPublishTask.publishId)) {
                                    url = ApiUtils.generatorUrl(mContext, URLAddr.URL_SHARED_FORWARD);
                                    SharedListItem item = JsonDataFactory.getData(SharedListItem.class, new JSONObject(mPublishTask.publishId));
                                    SharedListItem sharedItem = item.getData(SharedListItem.class);
                                    if (sharedItem != null)
                                        params.put("topicid", sharedItem.topicId);
                                    else
                                        params.put("topicid", item.topicId);
                                }
                                params.put("tenantid", PrfUtils.getTenantId(mContext));
                                params.put("content", mTask.content);
                                if (!TextUtil.isEmpty(mTask.address))
                                    params.put("address", mTask.address);
                                if (!TextUtil.isEmpty(mTask.latlng))
                                    params.put("latlng", mTask.latlng);
                                params.put("receiverids", mTask.receiverids);
                                params.put("ownid", PrfUtils.getUserId(mContext));
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            break;
            case PublishConstants.PUBLISH_HELP: {
                url = ApiUtils.generatorUrl(mContext, URLAddr.URL_HELP_CREATE);
                try {
                    Phelp mTask = JsonDataFactory.getData(Phelp.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId)) {
                        url = ApiUtils.generatorUrl(mContext, URLAddr.URL_HELP_FORWARD);
                        params.put("helpid", mPublishTask.publishId);
                    }
                    params.put("content", mTask.content);
                    params.put("receiverids", mTask.receiverids);
                    params.put("userid", PrfUtils.getUserId(mContext));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_SHARED: {
                url = ApiUtils.generatorUrl(mContext, URLAddr.URL_SHARED_CREATE);
                try {
                    Pshared mTask = JsonDataFactory.getData(Pshared.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId)) {
                        url = ApiUtils.generatorUrl(mContext, URLAddr.URL_SHARED_FORWARD);
                        params.put("topicid", mPublishTask.publishId);
                    }
                    params.put("tenantid", PrfUtils.getTenantId(mContext));
                    params.put("content", mTask.content);
                    if (!TextUtil.isEmpty(mTask.address))
                        params.put("address", mTask.address);
                    if (!TextUtil.isEmpty(mTask.latlng))
                        params.put("latlng", mTask.latlng);
                    params.put("receiverids", mTask.receiverids);
                    params.put("ownid", PrfUtils.getUserId(mContext));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_ANNOUNCEMENT: {
                url = URLAddr.URL_ANNOUNCEMENT_CREATE;
//                if (!TextUtils.isEmpty(mPublishTask.publishId)) {
//                    url = URLAddr.URL_ANNOUNCEMENT_UPDATE;
//                }
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pannouncement mTask = JsonDataFactory.getData(Pannouncement.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId))
                        params.put("notifyid", mPublishTask.publishId);
                    params.put("title", mTask.title);
                    params.put("content", mTask.content);
                    params.put("ishigh", mTask.isTop ? "2" : "1");
                    params.put("type", mTask.isSend ? "2" : "1");
                    params.put("receiverids", mTask.receiverids);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PublishConstants.PUBLISH_WORKREPORT: {
                url = URLAddr.URL_WORKREPORT_CREATE;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    PworkReport mTask = JsonDataFactory.getData(PworkReport.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId))
                        params.put("workreportid", mPublishTask.publishId);
                    params.put("content", mTask.content);
                    params.put("type", "" + mTask.type);
                    params.put("startdate", "" + mTask.startdate);
                    params.put("enddate", "" + mTask.enddate);
                    params.put("title", mTask.title);
                    params.put("receiverids", mTask.receiverids);
                    params.put("leader", mTask.leader);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            break;
            case PublishConstants.PUBLISH_TASK:
                url = URLAddr.URL_TASK_CREATE;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Ptask mTask = JsonDataFactory.getData(Ptask.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId))
                        params.put("taskid", mPublishTask.publishId);
                    params.put("content", mTask.content);
                    if (mTask.notifyType != 0 && mTask.notifyTime != 0)
                        AlarmUtils.setAlarmTime(mContext, mTask.notifyTime, new Gson().toJson(mPublishTask));
                    params.put("notifytype", "" + mTask.notifyType);
                    params.put("finishtime", "" + mTask.planFinishTime);
                    params.put("receiverids", mTask.receiverIds);
                    params.put("processerid", mTask.processerId);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case PublishConstants.PUBLISH_SCHEDULE:
                url = URLAddr.URL_SCHEDULE_CREATE;
                url = ApiUtils.generatorUrl(mContext, url);
                try {
                    Pschedule mSchedule = JsonDataFactory.getData(Pschedule.class, new JSONObject(mPublishTask.content));
                    if (!TextUtils.isEmpty(mPublishTask.publishId))
                        params.put("calendarid", mPublishTask.publishId);
                    params.put("starttime", "" + mSchedule.startTime);
                    params.put("endtime", "" + mSchedule.endTime);

                    if (mSchedule.notifyType != 0 && mSchedule.notifyTime != 0)
                        AlarmUtils.setAlarmTime(mContext, mSchedule.notifyTime, new Gson().toJson(mPublishTask));
                    params.put("notifytype", "" + mSchedule.notifyType);
                    params.put("notifytime", "" + mSchedule.notifyTime);
                    params.put("content", mSchedule.content);
                    if (mSchedule.isrepeat != 0)
                        params.put("isrepeat", "" + mSchedule.isrepeat);
                    params.put("permission", "" + mSchedule.permission);

                    if (!TextUtils.isEmpty(mSchedule.ccUserIds))//抄送人
                    {
                        params.put("ccids", mSchedule.ccUserIds);
                    }
                    params.put("isclock", String.valueOf(mSchedule.isclock));//外勤打卡
                    if (mSchedule.isclock == 1) {
                        params.put("longitude", mSchedule.longitude);
                        params.put("latitude", mSchedule.latitude);
                        params.put("clockaddress", mSchedule.address);
                        params.put("poiaddress", mSchedule.poiname);
                    }
                    params.put("receiverids", mSchedule.receiverIds);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if (!TextUtils.isEmpty(mImageIds)) {
            params.put("pid", mImageIds);
//            params.put("image_list_id", mImageIds);
        }
        if (!TextUtils.isEmpty(mAudioIds)) {
            params.put("audioid", mAudioIds);
//            params.put("audio_list_id", mAudioIds);
        }
        if (!TextUtils.isEmpty(mAttachmetIds)) {
            params.put("fileid", mAttachmetIds);
        }
        NetworkPath path = new NetworkPath(url, params, mContext);
        mNetworkManager.load(mPublishTask.type == PublishConstants.PUBLISH_COMMENT ? CALLBACK_COMMENT : CALLBACK_CONTENT, path, this);
    }

    private String mImageIds;
    private String mAudioIds;
    private String mAttachmetIds;
    private List<FilePair> mAudioFilePair;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        Log.e("TAG_swj",rootData.getJson().toString());
        boolean safe = rootData.isSuccess();
        if (!safe) {
//            Toast.makeText(mContext, R.string.toast_failed, Toast.LENGTH_SHORT).show();
            showNotify(R.mipmap.send_failure, mContext.getString(R.string.toast_failed));
            if (mPublishTask != null && rootData.code != 200) {
//                showNotify(R.mipmap.send_failure, mContext.getString(R.string.toast_failed));
                if (mPublishTask._id != -1) {
                    mPublishTask.update(mContext);
                } else {
                    mPublishTask.insert(mContext);
                }
            }
//            else
//            {
//                showNotify(R.mipmap.send_failure,rootData.getMsg());
//            }
            Intent intent = new Intent(MainActivity.RECEIVER_DRAFT);
            mContext.sendBroadcast(intent);
            if (rootData.getCode() == 200) {
                Intent errorIntent = new Intent(RECEIVER_ERROR);
                errorIntent.putExtra("rootDataMsg", rootData.getMsg());
                errorIntent.putExtra("rootDataJson", rootData.getJson().toString());
                errorIntent.putExtra("type", mPublishTask.type);
                mContext.sendBroadcast(errorIntent);
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_IMAGE: {
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    List<FileInfo> fileInfos = JsonDataFactory.getDataArray(FileInfo.class, dataArray);
                    if (TextUtils.isEmpty(mImageIds)) {
                        mImageIds = "";
                    }
                    StringBuilder stringBuilder = new StringBuilder(mImageIds);
                    for (FileInfo fileInfo : fileInfos) {
                        if (!TextUtils.isEmpty(stringBuilder.toString())) {
                            stringBuilder.append(",");
                        }
                        stringBuilder.append(fileInfo.fid);
                    }
                    mImageIds = stringBuilder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                submitAudioData();
            }
            break;
            case CALLBACK_AUDIO: {
                mAudioFilePair = (List<FilePair>) path.getExtraData();
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    List<FileInfo> fileInfos = JsonDataFactory.getDataArray(FileInfo.class, dataArray);
                    if (TextUtils.isEmpty(mAudioIds)) {
                        mAudioIds = "";
                    }
                    StringBuilder stringBuilder = new StringBuilder(mAudioIds);
                    for (FileInfo fileInfo : fileInfos) {
                        if (!TextUtils.isEmpty(stringBuilder.toString())) {
                            stringBuilder.append(",");
                        }
                        stringBuilder.append(fileInfo.fid);
                    }
                    mAudioIds = stringBuilder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                submitAttachmentData();
            }
            break;
            case CALLBACK_ATTACHMENT: {
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    List<FileInfo> fileInfos = JsonDataFactory.getDataArray(FileInfo.class, dataArray);
                    if (TextUtils.isEmpty(mAttachmetIds)) {
                        mAttachmetIds = "";
                    }
                    StringBuilder stringBuilder = new StringBuilder(mAttachmetIds);
                    for (FileInfo fileInfo : fileInfos) {
                        if (!TextUtils.isEmpty(stringBuilder.toString())) {
                            stringBuilder.append(",");
                        }
                        stringBuilder.append(fileInfo.fid);
                    }
                    mAttachmetIds = stringBuilder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                submitContent();
            }
            break;
            case CALLBACK_COMMENT:
            case CALLBACK_CONTENT:
                if (mPublishTask.type == PublishConstants.PUBLISH_TASK) {
//                    PushManager.getInstance().sendMessage()
                    try {
                        if (TextUtil.isEmpty(mPublishTask.publishId)) {
                            JSONObject resultData = rootData.getJson().getJSONObject("data");
                            String taskId = resultData.getString("taskid");
                            mPublishTask.publishId = taskId;
                        }

                        Ptask mTask = JsonDataFactory.getData(Ptask.class, new JSONObject(mPublishTask.content));
                        if (mTask.notifyType != 0 && mTask.notifyTime != 0)
                            AlarmUtils.setAlarmTime(mContext, mTask.notifyTime, new Gson().toJson(mPublishTask));
                    } catch (Exception e) {

                    }
                } else if (mPublishTask.type == PublishConstants.PUBLISH_SCHEDULE) {
                    //ssss
                    try {
//                        String exit = "1";
                        String exit = rootData.getJson().getJSONObject("data").getString("exit");
                        if ("1".equals(exit)) {
                            if (mPublishTask._id != -1) {
                                mPublishTask.update(mContext);
                            } else {
                                long rowId = mPublishTask.insert(mContext);
                                mPublishTask._id = (int) rowId;
                            }
                            Intent intent = new Intent(SCHEDULEACTION);
//                            intent.putExtra("content", new Gson().toJson(mPublishTask));
                            intent.putExtra("publishId", "" + mPublishTask._id);
                            intent.putExtra("msg", rootData.getMsg());
                            mContext.sendBroadcast(intent);
                            return;
                        }
                        if (TextUtil.isEmpty(mPublishTask.publishId)) {
                            JSONObject resultData = rootData.getJson().getJSONObject("data");//.getJSONObject("schedule");
                            String scheduleId = resultData.getString("scheduleid");
                            mPublishTask.publishId = scheduleId;
                        }
                        Pschedule mSchedule = JsonDataFactory.getData(Pschedule.class, new JSONObject(mPublishTask.content));
                        if (mSchedule.notifyType != 0 && mSchedule.notifyTime != 0)
                            AlarmUtils.setAlarmTime(mContext, mSchedule.notifyTime, new Gson().toJson(mPublishTask));
                    } catch (Exception e) {

                    }
                }
                if (mPublishTask != null && mPublishTask._id != -1) {
                    mPublishTask.delete(mContext);
                }
//                if (mAudioFilePair != null)
//                    for (FilePair filePair : mAudioFilePair) {
//                        filePair.getFile().delete();
//                    }
                int commentType = 0;
                Intent intent = new Intent(MainActivity.RECEIVER_DRAFT);
                try {
                    Pcomment pcomment = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                    commentType = pcomment.commentType;
                    if (callbackId == CALLBACK_COMMENT) {
                        JsonComment jsonComment = new JsonComment();
                        jsonComment.content = pcomment.content;
                        jsonComment.timestamp = String.valueOf(System.currentTimeMillis());
                        PreferencesController preferencesController = new PreferencesController();
                        preferencesController.context = mContext;
                        UserAccount userAccount = preferencesController.getAccount();
                        JsonUser jsonUser = new JsonUser();
                        jsonUser.name = userAccount.nickname();
                        jsonUser.userid = userAccount.user_id;
                        jsonUser.photo = userAccount.photo;
                        jsonComment.user = jsonUser;
                        if (!TextUtils.isEmpty(pcomment.replyuserid)) {
                            JsonUser replyUser = new JsonUser();
                            replyUser.name = pcomment.replayUser;
                            jsonComment.replyuser = replyUser;
                        }
                        String imagePath = mPublishTask.image;
                        if (!TextUtils.isEmpty(imagePath)) {
                            String[] paths = imagePath.split(",");
                            List<JsonImage> jsonImages = new ArrayList<>();
                            for (String imgPath : paths) {
                                JsonImage jsonImage = new JsonImage();
                                jsonImage.url = "file://" + imgPath;
                                jsonImage.thumb = "file://" + imgPath;
                                jsonImages.add(jsonImage);
                            }
                            jsonComment.image = jsonImages;
                        }
                        String audioPath = mPublishTask.audio;
                        String audioTime = mPublishTask.time;
                        if (!TextUtils.isEmpty(audioPath)) {
                            List<JsonAudio> jsonAudios = new ArrayList<>();
                            String[] paths = audioPath.split(",");
                            String[] times = audioTime.split(",");
                            for (int i = 0; i < paths.length; i++) {
                                JsonAudio jsonAudio = new JsonAudio();
                                jsonAudio.url = paths[i];
                                jsonAudio.time = times[i];
                                jsonAudios.add(jsonAudio);
                            }
                            jsonComment.audio = jsonAudios;
                        }
                        String comment = new Gson().toJson(jsonComment);
                        intent.putExtra("comment", comment);
                        intent.putExtra("commentId", pcomment.commentId);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showNotify(R.mipmap.send_success, mContext.getString(R.string.toast_success));

                intent.putExtra("type", mPublishTask.type);
                intent.putExtra("position", mPublishTask.getPosition());
                intent.putExtra("commentType", commentType);
                mContext.sendBroadcast(intent);
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("onResponse", "" + error);
    }

    @Override
    public void onResponse(String response) {

        Log.e("onResponse", response);
    }
}
