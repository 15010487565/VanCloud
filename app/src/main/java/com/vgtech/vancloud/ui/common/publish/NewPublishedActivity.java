package com.vgtech.vancloud.ui.common.publish;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.adapter.UserGridAdapter;
import com.vgtech.common.api.AttachFile;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.HelpListItem;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.Processer;
import com.vgtech.common.api.ResumeBuyBean;
import com.vgtech.common.api.ScheduleItem;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.api.Task;
import com.vgtech.common.api.Template;
import com.vgtech.common.api.TemplateItem;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.image.ImgGridAdapter;
import com.vgtech.common.image.PhotoActivity;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.utils.CalendarUtils;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.common.utils.PublishConstants;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.service.SubmitService;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MapLocationActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.internal.Recorder;
import com.vgtech.vancloud.ui.common.publish.module.Pannouncement;
import com.vgtech.vancloud.ui.common.publish.module.PapplyResume;
import com.vgtech.vancloud.ui.common.publish.module.Pcomment;
import com.vgtech.vancloud.ui.common.publish.module.Pflow;
import com.vgtech.vancloud.ui.common.publish.module.Phelp;
import com.vgtech.vancloud.ui.common.publish.module.Pschedule;
import com.vgtech.vancloud.ui.common.publish.module.Pshared;
import com.vgtech.vancloud.ui.common.publish.module.Ptask;
import com.vgtech.vancloud.ui.common.publish.module.PworkReport;
import com.vgtech.vancloud.ui.common.record.MediaManager;
import com.vgtech.vancloud.ui.common.record.RecorderAdapter;
import com.vgtech.vancloud.ui.group.OrganizationSelectedActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedListener;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.ConfigUtils;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import roboguice.util.Strings;
import zhou.tools.fileselector.FileSelector;
import zhou.tools.fileselector.FileSelectorActivity;
import zhou.tools.fileselector.config.FileConfig;
import zhou.tools.fileselector.utils.FileFilter;

/**
 * 日程、任务、工作汇报、分享、帮帮、万客休假、职位申请、简历购买、分享回复等
 * 至后来者：
 * 需求变换莫测，慢慢的就写成这样了，时间不允许也懒得重构，凑合弄吧，别吐槽了
 */
public class NewPublishedActivity extends BaseActivity implements PublishConstants {
    public static final String PUBLISH_TYPE = "PUBLISH_TYPE";
    private View attachementView;
    private GridView noScrollgridview;
    private ImgGridAdapter adapter;
    private EditText mContentEt;
    private TextView mRightTv;
    private EmojiFragment emojiFragment;
    private View mEmojiView;
    private InputMethodManager mInputMethodManager;
    private ScrollView mScrollView;
    private int mPublishType;
    private PublishTask mPublishTask;
    private long mCurrent;

    @Override
    protected int getContentView() {
        return R.layout.activity_selectimg;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRightTv = initRightTv(getString(R.string.ok));
        mCurrent = System.currentTimeMillis();
        Intent intent = getIntent();
        mPublishType = intent.getIntExtra(PUBLISH_TYPE, 0);
        Init();
        initActionView();
        mPublishTask = intent.getParcelableExtra("publishTask");
        if (mPublishTask != null) {//从草稿箱进入
            mPublishType = mPublishTask.type;
        }
        if (mPublishType != PUBLISH_FORWARD && mPublishType != PUBLISH_FEEDBACK) {
            initInputView();
        } else {//转发/意见反馈 不需要图片，底部按钮（表情，拍照，录音）
            noScrollgridview.setVisibility(View.GONE);
            findViewById(R.id.bottom_input).setVisibility(View.GONE);
        }
        initViewData();
    }

    private void initViewData() {
        if (mPublishTask != null) {
            initImageAndroidAudioData();
        }
        switch (mPublishType) {
            case PUBLISH_APPLYBUYRESUME: {
                try {
                    initResumeView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case PUBLISH_SCHEDULE:
                setTitle(getString(R.string.app_quickoption_schedule));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                initScheduleView();
                try {
                    //新建日程默认开始时间为当前时间五分钟
                    final Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(mCurrent);
                    long mHour = mCalendar.get(Calendar.HOUR);// 取得小时：
                    long mMinuts = mCalendar.get(Calendar.MINUTE);
                    if (mMinuts % 5 != 0) {
                        mMinuts = (mMinuts / 5 + 1) * 5;
                        if (mMinuts == 60) {
                            mCurrent = mCurrent + 60 * 60 * 1000 - mMinuts;
                            mMinuts = 0;
                        }
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
                    Date curDate = new Date(mCurrent);
                    String str = dateFormat.format(curDate);
                    mStartTimeTv.setText(str + ":" + (mMinuts < 10 ? ("0" + mMinuts) : mMinuts));

                    initScheduleData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case PUBLISH_WORKREPORT:
                setTitle(getString(R.string.title_create_workreport));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                initExecuterView(getString(R.string.lable_dianpingren));
                initWorkReportView();
                break;
            case PUBLISH_TASK:
                setTitle(getString(R.string.title_create_task));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                initTaskView();
                initExecuterView(null);
                try {
                    initTaskData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PUBLISH_TASK_UPDATE:
                setTitle(getString(R.string.task_update));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                initTaskView();
                initExecuterView(null);
                String taskInfo = getIntent().getStringExtra("taskInfo");
                try {
                    Task task = JsonDataFactory.getData(Task.class, new JSONObject(taskInfo));
                    initTaskData(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PUBLISH_SCHEDULE_UPDATE:
                setTitle(getString(R.string.title_schedule_update));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                initScheduleView();
                String scheduleInfo = getIntent().getStringExtra("scheduleInfo");
                try {
                    ScheduleItem scheduleItem = JsonDataFactory.getData(ScheduleItem.class, new JSONObject(scheduleInfo));
                    initScheduleInfo(scheduleItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PUBLISH_FLOW: {
                setTitle(getString(R.string.title_flow_approve));
                initExecuterView(getString(R.string.lable_approver));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                TextView receiverTypeTv = (TextView) findViewById(R.id.tv_receiver_type);
                receiverTypeTv.setText(R.string.lable_chaosong);
                try {
                    if (mPublishTask != null) {
                        Pflow ptask = JsonDataFactory.getData(Pflow.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        initExecuterAndReceiver(ptask.processerid, ptask.receiverids);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case PUBLISH_FLOW_LEAVE: {
                setTitle(getString(R.string.title_flow_leave));
                initLeaveInfoView();
                initExecuterView(getString(R.string.lable_approver));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                TextView receiverTypeTv = (TextView) findViewById(R.id.tv_receiver_type);
                receiverTypeTv.setText(R.string.lable_chaosong);
                try {
                    initFlowLeaveData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            break;
            case PUBLISH_HELP: {
                setTitle(getString(R.string.title_send_help));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                TextView receiverTypeTv = (TextView) findViewById(R.id.tv_receiver_type);
                receiverTypeTv.setText(R.string.lable_send_circle);
                try {
                    if (mPublishTask != null) {
                        Phelp ptask = JsonDataFactory.getData(Phelp.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        initExecuterAndReceiver(ptask.receiverids);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case PUBLISH_SHARED: {
                setTitle(getString(R.string.title_create_shared));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                TextView receiverTypeTv = (TextView) findViewById(R.id.tv_receiver_type);
                receiverTypeTv.setText(R.string.lable_send_circle);
                try {
                    initShareData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case PUBLISH_FORWARD: {
                ViewStub vsforward = (ViewStub) findViewById(R.id.vs_forward);
                vsforward.inflate();
                setTitle(getString(R.string.forward));
                findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                TextView forwardTv = (TextView) findViewById(R.id.tv_receiver_type);
                forwardTv.setText(R.string.lable_chaosong);
                boolean mInit = false;
                if (mPublishTask != null) {
                    mInit = true;
                    try {
                        JSONObject jsonObject = new JSONObject(mPublishTask.content);
                        int subType = jsonObject.getInt("subType");
                        if (subType == 1) {
                            Phelp ptask = JsonDataFactory.getData(Phelp.class, new JSONObject(mPublishTask.content));
                            mContentEt.setText(EmojiFragment.getEmojiContent(getApplicationContext(), 0, ptask.content));
                            initExecuterAndReceiver(ptask.receiverids);
                        } else if (subType == 2) {
                            Pshared ptask = JsonDataFactory.getData(Pshared.class, new JSONObject(mPublishTask.content));
                            mContentEt.setText(EmojiFragment.getEmojiContent(getApplicationContext(), 0, ptask.content));
                            initExecuterAndReceiver(ptask.receiverids);
                        }
                        Intent params = new Intent();
                        params.putExtra("json", mPublishTask.publishId);
                        params.putExtra("subtypeId", subType);
                        setIntent(params);
                    } catch (Exception e) {

                    }
                }

                int stype = getIntent().getIntExtra("subtypeId", 0);
                String json = getIntent().getStringExtra("json");
                if (!TextUtils.isEmpty(json)) {
                    SimpleDraweeView forwarIv = (SimpleDraweeView) findViewById(R.id.forward_icon);
                    TextView forwarName = (TextView) findViewById(R.id.forward_name);
                    TextView forwarText = (TextView) findViewById(R.id.forward_text);
                    switch (stype) {
                        case 1:
                            try {
                                HelpListItem item = JsonDataFactory.getData(HelpListItem.class, new JSONObject(json));
                                HelpListItem sharedItem = item.getData(HelpListItem.class);
                                final NewUser user = item.getData(NewUser.class);
                                if (sharedItem != null) {
                                    if ("2".equals(sharedItem.state)) {
                                        forwarIv.setImageResource(R.mipmap.ic_launcher);
                                        forwarName.setVisibility(View.GONE);
                                        forwarText.setText(R.string.raw_deleted);
                                    } else {
                                        final NewUser forwarduser = sharedItem.getData(NewUser.class);
                                        final List<ImageInfo> imags = sharedItem.getArrayData(ImageInfo.class);
                                        String url = forwarduser.photo;
                                        if (imags != null && !imags.isEmpty()) {
                                            url = imags.get(0).thumb;
                                            ImageOptions.setImage(forwarIv, url);
                                        } else {
                                            ImageOptions.setUserImage(forwarIv, url);
                                        }
                                        forwarName.setText("@" + forwarduser.name);
                                        forwarText.setText(sharedItem.content);
                                        //forward
                                        if (!mInit) {
                                            mContentEt.setText(EmojiFragment.getEmojiContent(getApplicationContext(), 0, "//@" + user.name + ":" + item.content));
                                            mContentEt.setSelection(0);
                                        }
                                    }

                                } else {
                                    final List<ImageInfo> imags = item.getArrayData(ImageInfo.class);
                                    String url = user.photo;
                                    if (imags != null && !imags.isEmpty()) {
                                        url = imags.get(0).thumb;
                                        ImageOptions.setImage(forwarIv, url);
                                    } else {
                                        ImageOptions.setUserImage(forwarIv, url);
                                    }
                                    forwarName.setText("@" + user.name);
                                    forwarText.setText(item.content);
                                }

                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 2:
                            try {
                                SharedListItem item = JsonDataFactory.getData(SharedListItem.class, new JSONObject(json));
                                SharedListItem sharedItem = item.getData(SharedListItem.class);
                                final NewUser user = item.getData(NewUser.class);
                                if (sharedItem != null) {
                                    if ("2".equals(sharedItem.state)) {
                                        forwarIv.setImageResource(R.mipmap.ic_launcher);
                                        forwarName.setVisibility(View.GONE);
                                        forwarText.setText(R.string.raw_deleted);
                                    } else {
                                        final NewUser forwarduser = sharedItem.getData(NewUser.class);
                                        final List<ImageInfo> imags = sharedItem.getArrayData(ImageInfo.class);
                                        String url = forwarduser.photo;
                                        if (imags != null && !imags.isEmpty()) {
                                            url = imags.get(0).thumb;
                                            ImageOptions.setImage(forwarIv, url);
                                        } else {
                                            ImageOptions.setUserImage(forwarIv, url);
                                        }
                                        forwarName.setText("@" + forwarduser.name);
                                        forwarText.setText(sharedItem.content);
                                        //forward
                                        if (!mInit) {
                                            mContentEt.setText(EmojiFragment.getEmojiContent(getApplicationContext(), 0, "//@" + user.name + ":" + item.content));

                                            mContentEt.setSelection(0);
                                        }
                                    }
                                } else {
                                    final List<ImageInfo> imags = item.getArrayData(ImageInfo.class);
                                    String url = user.photo;
                                    if (imags != null && !imags.isEmpty()) {
                                        url = imags.get(0).thumb;
                                        ImageOptions.setImage(forwarIv, url);
                                    } else {
                                        ImageOptions.setUserImage(forwarIv, url);
                                    }
                                    forwarName.setText("@" + user.name);
                                    forwarText.setText(item.content);
                                }

                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }


            }
            break;
            case PUBLISH_WORK_REPORT:
                setTitle(getString(R.string.right_comment));
                if (mPublishTask != null) {
                    try {
                        Pcomment ptask = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        Intent params = new Intent();
                        params.putExtra("workReportId", ptask.commentId);
                        setIntent(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PUBLISH_FEEDBACK:
                setTitle(getString(R.string.feedback));
                if (mPublishTask != null) {
                    try {
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, mPublishTask.content));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PUBLISH_COMMENT:
                setTitle(getString(R.string.comment));
                if (mPublishTask != null) {
                    try {
                        Pcomment ptask = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                        if (!TextUtils.isEmpty(ptask.replayUser)) {
                            setTitle(getString(R.string.comment) + ptask.replayUser);
                        }
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        Intent params = new Intent();
                        params.putExtra("comment_type", ptask.commentType);
                        params.putExtra("replyuserid", ptask.replyuserid);
                        params.putExtra("replayUser", ptask.replayUser);
                        params.putExtra("publishId", ptask.commentId);
                        params.putExtra("position", mPublishTask.position);
                        setIntent(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PUBLISH_TASK_CONDUCT:
                setTitle(getString(R.string.task_conduct));
                if (mPublishTask != null) {
                    try {
                        Pcomment ptask = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        Intent params = new Intent();
                        params.putExtra(Constants.TYPE, ptask.commentType);
                        params.putExtra("taskid", ptask.commentId);
                        setIntent(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PUBLISH_FLOW_CONDUCT:
                setTitle(getString(R.string.flow_conduct));
                if (mPublishTask != null) {
                    try {
                        Pcomment ptask = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        Intent params = new Intent();
                        params.putExtra(Constants.TYPE, ptask.commentType);
                        params.putExtra("flowId", ptask.commentId);
                        setIntent(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PUBLISH_RECRUIT_FINISH: {
                if (mPublishTask != null) {
                    try {
                        Pcomment ptask = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        Intent params = new Intent();
                        params.putExtra("resource_id", ptask.commentId);
                        setIntent(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                setTitle(getString(R.string.finish));
            }
            break;
            case PUBLISH_RESUME_APPROVE:
            case PUBLISH_RECRUIT_APPROVE: {
                if (mPublishTask != null) {
                    try {
                        Pcomment ptask = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        Intent params = new Intent();
                        params.putExtra(Constants.TYPE, ptask.commentType);
                        params.putExtra("resource_id", ptask.commentId);
                        setIntent(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int type = getIntent().getIntExtra(Constants.TYPE, 0);
                int strId = R.string.recruit_conduct;
                if (type == 1) {
                    strId = R.string.schedule_detail_agree;
                } else if (type == 2) {
                    strId = R.string.disagree;
                }
                setTitle(getString(strId));
            }
            break;
            case PUBLISH_SCHEDULE_CONDUCT: {
                if (mPublishTask != null) {
                    try {
                        Pcomment ptask = JsonDataFactory.getData(Pcomment.class, new JSONObject(mPublishTask.content));
                        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
                        Intent params = new Intent();
                        params.putExtra(Constants.TYPE, ptask.commentType);
                        params.putExtra("scheduleId", ptask.commentId);
                        setIntent(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int type = getIntent().getIntExtra(Constants.TYPE, 0);
                int strId = R.string.task_conduct;
                if (type == 1) {
                    strId = R.string.schedule_detail_watting;
                } else if (type == 2) {
                    strId = R.string.schedule_detail_refuse;
                } else if (type == 3) {
                    strId = R.string.schedule_detail_agree;
                }
                setTitle(getString(strId));
                mContentEt.setText(getString(strId));
            }
            break;
        }
    }

    private void initResumeView() throws Exception {
        setTitle(getString(R.string.recruit_tobuy));
        ViewStub viewStub = (ViewStub) findViewById(R.id.vs_resume);
        viewStub.inflate();
        initExecuterView(getString(R.string.lable_approver));
        findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);

        TextView receiverTypeTv = (TextView) findViewById(R.id.tv_receiver_type);
        receiverTypeTv.setText(R.string.lable_chaosong);

        if (mPublishTask != null) {
            PapplyResume pcomment = JsonDataFactory.getData(PapplyResume.class, new JSONObject(mPublishTask.content));
            getIntent().putExtra("resumes", pcomment.resumes);
            getIntent().putExtra("recruit_id", pcomment.recruit_id);
            mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, pcomment.content));
            initExecuterAndReceiver(pcomment.approval_user_id, pcomment.receiverids);
        }
        String resumes = getIntent().getStringExtra("resumes");
        initResumeData(resumes);
    }

    private void initResumeData(String resumes) {
        try {
            List<ResumeBuyBean> resumeBuyBeans = JsonDataFactory.getDataArray(ResumeBuyBean.class, new JSONArray(resumes));
            if (resumeBuyBeans.isEmpty()) {
                finish();
                return;
            }
            View btnResume = findViewById(R.id.btn_resume);
            btnResume.setOnClickListener(this);
            btnResume.setTag(resumeBuyBeans);
            String name = getString(R.string.resumes_total, String.valueOf(resumeBuyBeans.size()));
            float amount = 0;
            for (ResumeBuyBean resume : resumeBuyBeans) {
                String price = resume.price;
                if (!TextUtils.isEmpty(price)) {
                    float fprice = Float.parseFloat(price);
                    amount += fprice;
                }
            }
            String price = Utils.priceFormat(String.valueOf(amount));
            String value = getString(R.string.order_total, price) + getString(R.string.recruit_price);
            TextView nameTv = (TextView) findViewById(R.id.tv_resume_name);
            TextView valueTv = (TextView) findViewById(R.id.tv_resume_value);
            nameTv.setText(name);
            valueTv.setText(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initImageAndroidAudioData() {
        if (!TextUtils.isEmpty(mPublishTask.image)) {
            String[] paths = mPublishTask.image.split(",");
            for (String path : paths) {
                Bimp.drr.add(path);
            }
            adapter.update();
        }
        if (!TextUtils.isEmpty(mPublishTask.audio)) {
            String[] paths = mPublishTask.audio.split(",");
            String[] times = mPublishTask.time.split(",");
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                Recorder recorder = new Recorder(Float.parseFloat(times[i]), path);
                mAdapter.getData().add(recorder);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private TextView mPlantTimeTv;
    private TextView mTaskTipTv;

    private void initTaskView() {
        ViewStub vsTask = (ViewStub) findViewById(R.id.vs_task);
        vsTask.inflate();
        mPlantTimeTv = (TextView) findViewById(R.id.plant_time);
        mTaskTipTv = (TextView) findViewById(R.id.btn_task_type);
        findViewById(R.id.plant_time_click).setOnClickListener(this);
        findViewById(R.id.btn_task_type_click).setOnClickListener(this);
        TextView receiverTypeTv = (TextView) findViewById(R.id.tv_receiver_type);
        receiverTypeTv.setText(R.string.lable_chaosong);
    }

    private void initTaskData() throws Exception {
        if (mPublishTask != null) {
            Ptask ptask = JsonDataFactory.getData(Ptask.class, new JSONObject(mPublishTask.content));
            mPlantTimeTv.setText(Utils.dateFormatStr(ptask.planFinishTime));
            mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
            String tip = ConfigUtils.getTipValue(this, ptask.notifyType);
            mTaskTipTv.setText(tip);
            initExecuterAndReceiver(ptask.processerId, ptask.receiverIds);
        }
    }

    private RecordFinishListener mRecorderFinishListener = new RecordFinishListener() {

        @Override
        public void recorderFinished(Recorder recorder) {
            mAdapter.getData().add(recorder);
            mAdapter.notifyDataSetChanged();
            mScrollView.fullScroll(View.FOCUS_DOWN);
        }
    };

    private void initShareData() throws Exception {
        View addressView = findViewById(R.id.address_view);
        addressView.setVisibility(View.VISIBLE);
        TextView nameTv = (TextView) findViewById(R.id.address_name);
        nameTv.setText(R.string.lable_location);
        findViewById(R.id.btn_location).setOnClickListener(this);
        nameTv.setTextColor(Color.GRAY);
        findViewById(R.id.btn_delete_address).setOnClickListener(this);
        if (mPublishTask != null) {
            if (!TextUtils.isEmpty(mPublishTask.publishId)) {
                addressView.setVisibility(View.GONE);
                noScrollgridview.setVisibility(View.GONE);
                findViewById(R.id.bottom_input).setVisibility(View.GONE);
            }
            Pshared ptask = JsonDataFactory.getData(Pshared.class, new JSONObject(mPublishTask.content));
            mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
            if (!TextUtils.isEmpty(ptask.address)) {
                nameTv.setText(ptask.address);
                nameTv.setTag(ptask.latlng);
                nameTv.setTextColor(Color.parseColor("#4ab9fc"));
                findViewById(R.id.btn_delete_address).setVisibility(View.VISIBLE);
            }
            initExecuterAndReceiver(ptask.receiverids);
        }
    }

    private Task mTask;

    private void initTaskData(Task taskInfo) throws Exception {
        mTask = taskInfo;
        mPlantTimeTv.setText(Utils.dateFormatStr(taskInfo.plantime));
        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, taskInfo.content));
        String tip = ConfigUtils.getTipValue(this, taskInfo.notifytype);
        mTaskTipTv.setText(tip);
        List<NewUser> receiver = taskInfo.getArrayData(NewUser.class);
        Processer processer = taskInfo.getData(Processer.class);
        initExecuterAndReceiver(new NewUser(processer.userid, processer.name, processer.photo), receiver);
        List<ImageInfo> images = taskInfo.getArrayData(ImageInfo.class);
        if (images != null && !images.isEmpty()) {
            for (ImageInfo img : images) {
                Bimp.drr.add(img.getUrl());
            }
            adapter.update();
        }
        List<AudioInfo> audios = JsonDataFactory.getDataArray(AudioInfo.class, taskInfo.getJson().getJSONArray("audio"));
        if (audios != null && !audios.isEmpty()) {
            for (AudioInfo audioInfo : audios) {
                Recorder recorder = new Recorder(audioInfo.getTime(), audioInfo.getUrl());
                mAdapter.getData().add(recorder);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private ScheduleItem mScheduleItem;

    private void initScheduleInfo(ScheduleItem scheduleItem) throws Exception {
        mScheduleItem = scheduleItem;

        mStartTimeTv.setText(Utils.dateFormatStr(mScheduleItem.starttime));
        mEndTimeTv.setText(Utils.dateFormatStr(mScheduleItem.endtime));
        int type = 0;
        if (!TextUtil.isEmpty(mScheduleItem.notifytype)) {
            type = Integer.parseInt(mScheduleItem.notifytype);
        }
        String tip = ConfigUtils.getTipValue(this, type);
        mTipTv.setText(tip);
        if (mScheduleItem.permission == 2) {
            RadioButton selfBtn = (RadioButton) findViewById(R.id.rb_self);
            selfBtn.setChecked(true);
        }
        mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, mScheduleItem.content));
        List<NewUser> receiver = scheduleItem.getArrayData(NewUser.class);
        initExecuterAndReceiver(receiver);
        if (scheduleItem.getJson().has("ccUser")) {
            List<NewUser> reciverCCusers = JsonDataFactory.getDataArray(NewUser.class, scheduleItem.getJson().getJSONArray("ccUser"));
            ArrayList<Node> desplayNodes = new ArrayList<>();
            ArrayList<Node> receiverNodes = new ArrayList<>();
            for (NewUser user : reciverCCusers) {
                Node node = new Node();
                node.setIsUser(true);
                node.setId(user.userid);
                node.setPhoto(user.photo);
                node.setName(user.name);
                receiverNodes.add(node);
            }
            desplayNodes.addAll(receiverNodes);
            setCcuserGridView(receiverNodes, desplayNodes);
        }
        mScheduleOwCb.setChecked(mScheduleItem.isclock.equals("1"));
        if (mScheduleItem.isclock.equals("1")) {
            mAddress = mScheduleItem.clockaddress;
            mAddName = mScheduleItem.poiaddress;
            if (TextUtils.isEmpty(mAddName)) {
                mScheduleOwAddTv.setText(mAddress);
            } else {
                mScheduleOwAddTv.setText(mAddName);
            }
            mLatlng = mScheduleItem.latitude + "," + mScheduleItem.longitude;
        }
        List<ImageInfo> images = scheduleItem.getArrayData(ImageInfo.class);
        if (images != null && !images.isEmpty()) {
            for (ImageInfo img : images) {
                Bimp.drr.add(img.getUrl());
            }
            adapter.update();
        }

        List<AudioInfo> audios = JsonDataFactory.getDataArray(AudioInfo.class, scheduleItem.getJson().getJSONArray("audio"));
        if (audios != null && !audios.isEmpty()) {
            for (AudioInfo audioInfo : audios) {
                Recorder recorder = new Recorder(audioInfo.getTime(), audioInfo.getUrl());
                mAdapter.getData().add(recorder);
            }
            mAdapter.notifyDataSetChanged();
        }
        if ("2".equals(scheduleItem.signs))//已过期
        {
            vsScheudle.setVisibility(View.GONE);
            vs_ccuser.setVisibility(View.GONE);
            findViewById(R.id.select_user_view).setVisibility(View.GONE);
            ViewStub vs_schedule_start_end_date = (ViewStub) findViewById(R.id.vs_schedule_start_end_date);
            vs_schedule_start_end_date.inflate();
            TextView schedule_start_date = (TextView) findViewById(R.id.schedule_start_date);
            TextView schedule_end_date = (TextView) findViewById(R.id.schedule_end_date);
            schedule_start_date.setText(getString(R.string.schedule_begin_time, Utils.dateFormatStr(mScheduleItem.starttime)));
            schedule_end_date.setText(getString(R.string.schedule_end_time, Utils.dateFormatStr(mScheduleItem.endtime)));
        }

    }

    private GridView mExecuterGrid;
    private GridView mCcGrid;

    private void initExecuterView(String lable) {
        ViewStub vs_executer = (ViewStub) findViewById(R.id.vs_executer);
        vs_executer.inflate();
        mExecuterCountTv = (TextView) findViewById(R.id.tv_executer_count);
        TextView lableTv = (TextView) findViewById(R.id.tv_lable_single_user);
        if (!TextUtils.isEmpty(lable))
            lableTv.setText(lable);
        findViewById(R.id.btn_executer_user).setOnClickListener(this);
        mExecuterGrid = (GridView) findViewById(R.id.executer_gridview);
    }

    private void initInputView() {

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        emojiFragment = new EmojiFragment();
        emojiFragment.setListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Map<String, Object> map = emojiFragment.emojis.get(emojiFragment.page).get(pos);
                int start = Math.max(mContentEt.getSelectionStart(), 0);
                int end = Math.max(mContentEt.getSelectionEnd(), 0);
                String str = (String) map.get("text");
                if (str.contains("删除")) {

                    //动作按下
                    int action = KeyEvent.ACTION_DOWN;
                    //code:删除，其他code也可以，例如 code = 0
                    int code = KeyEvent.KEYCODE_DEL;
                    KeyEvent event = new KeyEvent(action, code);
                    mContentEt.onKeyDown(KeyEvent.KEYCODE_DEL, event);
                    return;
                }
                //noinspection ConstantConditions
                mContentEt.getText().replace(Math.min(start, end), Math.max(start, end), str, 0, str.length());
            }
        });
        mEmojiView = findViewById(R.id.more_input_container);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mEmojiView != null && ev.getAction() == MotionEvent.ACTION_UP) {
            View v = mContentEt;
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                if (mEmojiView.getVisibility() == View.VISIBLE && isShouldHideInput(mEmojiView, ev)) {
                    hideEmojiFragment();
                }
            } else {
                hideEmojiFragment();
            }
//            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
//        if (getWindow().superDispatchTouchEvent(ev)) {
//            return super.dispatchTouchEvent(ev);
//        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private TextView mStartTimeTv;
    private TextView mEndTimeTv;
    private TextView mTipTv;
    private TextView mCcUserCountTv;
    private TextView mScheduleOwAddTv;
    private View mScheduleOwAddBtn;
    private CheckBox mScheduleOwCb;
    ViewStub vsScheudle;
    ViewStub vs_ccuser;

    private void initScheduleView() {
        vsScheudle = (ViewStub) findViewById(R.id.vs_schedule);
        vsScheudle.inflate();
        vs_ccuser = (ViewStub) findViewById(R.id.vs_ccuser);
        vs_ccuser.inflate();
        mCcUserCountTv = (TextView) findViewById(R.id.tv_cc_count);
        findViewById(R.id.btn_cc_user).setOnClickListener(this);
        mCcGrid = (GridView) findViewById(R.id.cc_gridview);
        mScheduleOwAddTv = (TextView) findViewById(R.id.tv_schedule_outwook_address);
        mScheduleOwAddBtn = findViewById(R.id.btn_schedule_outwook_address);
        mScheduleOwAddBtn.setOnClickListener(this);
        mScheduleOwCb = (CheckBox) findViewById(R.id.rb_outwook);
        findViewById(R.id.schedule_outwook_address_divider).setVisibility(View.GONE);
        mScheduleOwCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mScheduleOwAddBtn.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                findViewById(R.id.schedule_outwook_address_divider).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        mStartTimeTv = (TextView) findViewById(R.id.start_time);
        findViewById(R.id.start_time_click).setOnClickListener(this);
        mEndTimeTv = (TextView) findViewById(R.id.end_time);
        findViewById(R.id.end_time_click).setOnClickListener(this);
//        mEndTimeTv.addTextChangedListener(this);
//        mStartTimeTv.addTextChangedListener(this);
        mTipTv = (TextView) findViewById(R.id.btn_schedule_type);
        findViewById(R.id.btn_schedule_type_click).setOnClickListener(this);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rb_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_open:
                        findViewById(R.id.select_user_view).setVisibility(View.VISIBLE);
                        vs_ccuser.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_self:
                        vs_ccuser.setVisibility(View.GONE);
                        findViewById(R.id.select_user_view).setVisibility(View.GONE);
                        if (mReceiverAdapter != null) {
                            mReceiverAdapter.getList().clear();
                            mUserCountTv.setText("");
                            mReceiverAdapter.notifyDataSetChanged();
                        }
                        if (mCcUserAdapter != null) {
                            mCcUserAdapter.getList().clear();
                            mCcUserCountTv.setText("");
                            mCcUserAdapter.notifyDataSetChanged();
                        }

                        break;
                }
            }
        });
    }

    private void initLeaveInfoView() {
        ViewStub vsScheudle = (ViewStub) findViewById(R.id.vs_leave_info);
        vsScheudle.inflate();

        mStartTimeTv = (TextView) findViewById(R.id.start_time);
        findViewById(R.id.start_time_click).setOnClickListener(this);
        mEndTimeTv = (TextView) findViewById(R.id.end_time);
        findViewById(R.id.end_time_click).setOnClickListener(this);
        mTipTv = (TextView) findViewById(R.id.btn_leave_type);
        findViewById(R.id.btn_leave_type_click).setOnClickListener(this);
    }

    private void initFlowLeaveData() throws Exception {
        if (mPublishTask != null) {
            Pflow ptask = JsonDataFactory.getData(Pflow.class, new JSONObject(mPublishTask.content));
            mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, ptask.content));
            String tip = PublishUtils.getLeaveType(this, ptask.leaveType);
            mTipTv.setText(tip);
            mStartTimeTv.setText(Utils.dateFormatStr(ptask.startTime));
            mEndTimeTv.setText(Utils.dateFormatStr(ptask.endTime));
            TextView leaveTimeTv = (TextView) findViewById(R.id.et_leave_time);
            leaveTimeTv.setText(String.valueOf(ptask.leaveTime));
            initExecuterAndReceiver(ptask.processerid, ptask.receiverids);
        }
    }

    private TextView mWorkReportTypeTv;
    private TextView mWorkReportDateTv;
    private RadioGroup mWorkReportTitleGroup;
    private LinearLayout mTemplateLayout;
    private LayoutInflater mInflater;

    private String[] mDayArray;
    private String[] mWeekArray;
    private String[] mMonthArray;
    private TextView mWorkReportTitleTv;
    private RadioButton mRadioButton;

    private void initWorkReportView() {
        int length = 7;
        mDayArray = new String[length];
        long dayMillis = 24 * 60 * 60 * 1000l;
        for (int i = 0; i < length; i++) {
            long times = mCurrent - i * dayMillis;
            String date = Utils.dateFormatToDate(times);
            mDayArray[i] = date;
        }
        mWeekArray = CalendarUtils.getWeek(new Date(), 3);
        mMonthArray = CalendarUtils.getMonths(new Date(), 3);
        mInflater = getLayoutInflater();
        mContentEt.setVisibility(View.GONE);
        ViewStub vsScheudle = (ViewStub) findViewById(R.id.vs_workreport);
        vsScheudle.inflate();
        mWorkReportTitleTv = (TextView) findViewById(R.id.tv_report_title);
        mWorkReportTypeTv = (TextView) findViewById(R.id.btn_work_report_type);
        mWorkReportDateTv = (TextView) findViewById(R.id.tv_work_report_date);
        findViewById(R.id.btn_work_report_type_click).setOnClickListener(this);
        findViewById(R.id.tv_work_report_date_click).setOnClickListener(this);
        TextView receiverTypeTv = (TextView) findViewById(R.id.tv_receiver_type);
        receiverTypeTv.setText(R.string.lable_chaosong);
        mWorkReportTitleGroup = (RadioGroup) findViewById(R.id.rb_group);
        mTemplateLayout = (LinearLayout) findViewById(R.id.template_layout);
        mWorkReportTitleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
//                radioButton.setTag(R.string.app_name,items);
                saveWorkReport();
                Template template = (Template) radioButton.getTag();
                List<TemplateItem> templateItems = (List<TemplateItem>) radioButton.getTag(R.string.app_name);
                if (templateItems == null) {
                    templateItems = template.getArrayData(TemplateItem.class);
                }
                initTemplateLayout(template, templateItems);
            }
        });
        String json = PrfUtils.getWorkReportTempleate(this);
        try {
            mWorkReportTypeTv.setText(getResources().getStringArray(R.array.report_type)[0]);
            mWorkReportDateTv.setText(Utils.dateFormatToDate(mCurrent));
            mWorkReportTitleTv.setText(mWorkReportTypeTv.getText() + "(" + mWorkReportDateTv.getText() + ")");
            JSONObject jsonObject = new JSONObject(json);
            mDayTemplates = JsonDataFactory.getDataArray(Template.class, jsonObject.getJSONArray("day"));
            mWeekTemplates = JsonDataFactory.getDataArray(Template.class, jsonObject.getJSONArray("week"));
            mMonthTemplates = JsonDataFactory.getDataArray(Template.class, jsonObject.getJSONArray("month"));
            if (mPublishTask == null)
                initTemplateView(mDayTemplates, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mPublishTask != null) {
            try {
                PworkReport pworkReport = JsonDataFactory.getData(PworkReport.class, new JSONObject(mPublishTask.content));
                initExecuterAndReceiver(pworkReport.leader, pworkReport.receiverids);
                int tempateId = pworkReport.templateId;
                List<TemplateItem> tList = JsonDataFactory.getDataArray(TemplateItem.class, new JSONArray(pworkReport.content));
                mReportType = pworkReport.type;
                String[] typeArray = getResources().getStringArray(R.array.report_type);
                if (mReportType == 1) {
                    initTemplateView(mDayTemplates, tempateId, tList);
                    mWorkReportTypeTv.setText(typeArray[0]);
                    String date = Utils.dateFormat(pworkReport.startdate, Utils.DATE_FORMAT);
                    mWorkReportDateTv.setText(date);
                } else if (mReportType == 2) {
                    initTemplateView(mWeekTemplates, tempateId, tList);
                    mWorkReportTypeTv.setText(typeArray[1]);
                    String start = Utils.dateFormat(pworkReport.startdate, Utils.DATE_FORMAT);
                    String end = Utils.dateFormat(pworkReport.enddate, Utils.DATE_FORMAT);
                    mWorkReportDateTv.setText(start + "--" + end);
                } else if (mReportType == 3) {
                    initTemplateView(mMonthTemplates, tempateId, tList);
                    mWorkReportTypeTv.setText(typeArray[2]);
                    String start = Utils.dateFormat(pworkReport.startdate, "yyyy/MM");
                    mWorkReportDateTv.setText(start);
                }
                mWorkReportTitleTv.setText(mWorkReportTypeTv.getText() + "(" + mWorkReportDateTv.getText() + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<Template> mDayTemplates;
    private List<Template> mWeekTemplates;
    private List<Template> mMonthTemplates;

    private void initTemplateView(List<Template> templates, int templeId, List<TemplateItem> items) {
        mWorkReportTitleGroup.removeAllViews();
        RadioButton radioButton = null;
        for (Template template : templates) {
            RadioButton itemView = (RadioButton) mInflater.inflate(R.layout.rb_item, null);
            if (templeId == template.id) {
                radioButton = itemView;
                radioButton.setTag(R.string.app_name, items);
            }
            itemView.setId(template.id);
            itemView.setTag(template);
            itemView.setText(template.name);
            mWorkReportTitleGroup.addView(itemView);
        }
        if (mWorkReportTitleGroup.getChildCount() > 0 && templeId == -1) {
            RadioButton itemView = (RadioButton) mWorkReportTitleGroup.getChildAt(0);
            itemView.setChecked(true);
        }
        if (radioButton != null) {
            radioButton.setChecked(true);
        }
    }

    private void initTemplateView(List<Template> templates, int templeId) {
        initTemplateView(templates, templeId, null);
    }

    private void initTemplateLayout(Template template, List<TemplateItem> templateItems) {
        mTemplateLayout.removeAllViews();
        mTemplateLayout.setTag(template);
        for (TemplateItem item : templateItems) {
            View itemView = mInflater.inflate(R.layout.template_item, null);
            EditText editText = (EditText) itemView.findViewById(R.id.et_content);
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mContentEt = (EditText) v;
                    return false;
                }
            });
//            editText.addTextChangedListener(new TextWatcher() {
            EditUtils.limitEditTextLength(editText, Constants.TEXT_MAX_VALUE, new TextWatcher() {
                private boolean hasChanged = true;

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if (mEmojiView == null || mEmojiView.getVisibility() == View.GONE) {
                        return;
                    }

                    if (!hasChanged) {
                        return;
                    }
                    String str = editable.toString();
                    if (Strings.isEmpty(str)) {
                        mRightTv.setEnabled(false);
                    } else {
                        mRightTv.setEnabled(true);
                        hasChanged = false;
                        int start = mContentEt.getSelectionStart();
                        int end = mContentEt.getSelectionEnd();
                        mContentEt.setText(EmojiFragment.getEmojiContent(getApplicationContext(), 0, str));
                        mContentEt.setSelection(start, end);
                        hasChanged = true;
                    }
                }
            });
            if (!TextUtil.isEmpty(item.content)) {
                editText.setText(EmojiFragment.getEmojiContent(getApplicationContext(), 0, item.content));
            }
            itemView.setTag(item);
            TextView titleTv = (TextView) itemView.findViewById(R.id.tv_title);
            titleTv.setText(item.title);
            mTemplateLayout.addView(itemView);
        }
    }

    private void initScheduleData() throws Exception {
        if (mPublishTask != null) {
            Pschedule pschedule = JsonDataFactory.getData(Pschedule.class, new JSONObject(mPublishTask.content));
            mStartTimeTv.setText(Utils.dateFormatStr(pschedule.startTime));
            mEndTimeTv.setText(Utils.dateFormatStr(pschedule.endTime));
            String tip = ConfigUtils.getTipValue(this, pschedule.notifyType);
            mTipTv.setText(tip);
            if (pschedule.permission == 2) {
                RadioButton selfBtn = (RadioButton) findViewById(R.id.rb_self);
                selfBtn.setChecked(true);
            }
            mScheduleOwCb.setChecked(pschedule.isclock == 1);
            if (pschedule.isclock == 1) {
                mAddress = pschedule.address;
                mAddName = pschedule.poiname;
                if (TextUtils.isEmpty(mAddName)) {
                    mScheduleOwAddTv.setText(mAddress);
                } else {
                    mScheduleOwAddTv.setText(mAddName);
                }
                mLatlng = pschedule.latitude + "," + pschedule.longitude;
            }
            mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, pschedule.content));
            initExecuterAndReceiver(pschedule.receiverIds);


            String[] receiverIds = null;
            if (!TextUtils.isEmpty(pschedule.ccUserIds)) {
                receiverIds = pschedule.ccUserIds.split(",");
            }
            List<Node> nodes = TenantPresenter.isVanTop(this) ? getAppliction().getVanTopCacheUserNode() : getAppliction().getCacheTreeNode();
            ArrayList<Node> receiverNodes = new ArrayList<Node>();
            for (Node n : nodes) {
                if (receiverIds != null) {
                    for (String uid : receiverIds) {
                        if (!TextUtils.isEmpty(uid))
                            if (n.getId().equals(uid)) {
                                receiverNodes.add(n);
                            }
                    }
                }
            }
            if (!receiverNodes.isEmpty()) {
                ArrayList<Node> desplayNodes = new ArrayList<>();
                desplayNodes.addAll(receiverNodes);
                setCcuserGridView(receiverNodes, desplayNodes);
            }
        } else//日程提醒:默认不提醒
        {
            String type = getResources().getStringArray(R.array.tip_type)[0];
            mTipTv.setText(type);
            mTipTv.setTag(0);
        }
    }

    private void initExecuterAndReceiver(String receiverIdsStr) {
        initExecuterAndReceiver(null, receiverIdsStr);
    }

    private void initExecuterAndReceiver(List<NewUser> receiver) {
        initExecuterAndReceiver(null, receiver);
    }

    /**
     * 初始化抄送人
     *
     * @param executerStr    执行人或审批人id，逗号分隔
     * @param receiverIdsStr 抄送人id，逗号分隔
     */
    private void initExecuterAndReceiver(String executerStr, String receiverIdsStr) {
// //TODO 此处需优化，异步获取组织架构
        String[] uids = null;
        if (!TextUtils.isEmpty(executerStr)) {
            uids = executerStr.split(",");
        }
        String[] receiverIds = null;
        if (!TextUtils.isEmpty(receiverIdsStr)) {
            receiverIds = receiverIdsStr.split(",");
        }
        if (uids != null || receiverIds != null) {
            List<Node> nodes = TenantPresenter.isVanTop(this) ? getAppliction().getVanTopCacheUserNode() : getAppliction().getCacheTreeNode();
            ArrayList<Node> processNodes = new ArrayList<Node>();
            ArrayList<Node> receiverNodes = new ArrayList<Node>();
            for (Node n : nodes) {
                if (uids != null) {
                    for (String uid : uids) {
                        if (!TextUtils.isEmpty(uid))
                            if (n.getId().equals(uid)) {
                                processNodes.add(n);
                            }
                    }
                }
                if (receiverIds != null) {
                    for (String uid : receiverIds) {
                        if (!TextUtils.isEmpty(uid))
                            if (n.getId().equals(uid)) {
                                receiverNodes.add(n);
                            }
                    }
                }
            }
            if (!processNodes.isEmpty())
                setmExecuterGridView(processNodes);
            if (!receiverNodes.isEmpty())
                setGridView(receiverNodes);
        }
    }

    private void initExecuterAndReceiver(NewUser executerUser, List<NewUser> receiver) {
        if (executerUser != null || receiver != null) {

            ArrayList<Node> processNodes = new ArrayList<Node>();
            ArrayList<Node> receiverNodes = new ArrayList<Node>();
            if (TenantPresenter.isVanTop(this)) {
                if (executerUser != null) {
                    Node node = new Node();
                    node.setIsUser(true);
                    node.setId(executerUser.userid);
                    node.setPhoto(executerUser.photo);
                    node.setName(executerUser.name);
                    processNodes.add(node);
                }
                if (receiver != null) {
                    for (NewUser user : receiver) {
                        Node node = new Node();
                        node.setIsUser(true);
                        node.setId(user.userid);
                        node.setPhoto(user.photo);
                        node.setName(user.name);
                        receiverNodes.add(node);
                    }
                }
            } else {
                List<Node> nodes = getAppliction().getCacheTreeNode();
                for (Node n : nodes) {
                    if (executerUser != null) {
                        if (n.getId().equals(executerUser.userid)) {
                            processNodes.add(n);
                        }
                    }
                    if (receiver != null) {
                        for (NewUser user : receiver) {
                            if (n.getId().equals(user.userid)) {
                                receiverNodes.add(n);
                            }
                        }
                    }
                }
            }

            if (!processNodes.isEmpty())
                setmExecuterGridView(processNodes);
            if (!receiverNodes.isEmpty())
                setGridView(receiverNodes);
        }

    }

    private void initAnnouncementData() throws Exception {
        if (mPublishTask != null) {
            Pannouncement pannouncement = JsonDataFactory.getData(Pannouncement.class, new JSONObject(mPublishTask.content));
            TextView titleTv = (TextView) findViewById(R.id.et_announcement_title);
            titleTv.setText(pannouncement.title);
            CheckBox checkBox = (CheckBox) findViewById(R.id.rb_top);
            checkBox.setChecked(pannouncement.isTop);
            mPannouncement = new Pannouncement();
            mContentEt.setText(EmojiFragment.getEmojiContent(this, 0, pannouncement.content));
            initExecuterAndReceiver(pannouncement.receiverids);
            String filePath = mPublishTask.attachment;
            if (!TextUtil.isEmpty(filePath)) {
                File file = new File(filePath);
                if (file.exists()) {
                    attachementView = findViewById(R.id.attachment_view);
                    attachementView.setVisibility(View.VISIBLE);
                    TextView nameTv = (TextView) findViewById(R.id.attachment_name);
                    findViewById(R.id.btn_attachment_delete).setOnClickListener(this);
                    nameTv.setText(file.getName());
                    attachementView.setTag(file);

                } else {
                    mPublishTask.attachment = "";
                }

            }
        }
    }

    private void showEmojiFragment() {

        mInputMethodManager.hideSoftInputFromWindow(
                mContentEt.getWindowToken(), 0);
        // 隐藏表情选择框
        if (mEmojiView.getVisibility() == View.VISIBLE) {
            mEmojiView.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().remove(emojiFragment).commit();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSupportFragmentManager().beginTransaction().replace(R.id.more_input_container, emojiFragment).commit();
                    mEmojiView.setVisibility(View.VISIBLE);
                }
            }, 200);
        }


    }

    private void hideEmojiFragment() {
        mEmojiView.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().remove(emojiFragment).commit();
    }

    private void initActionView() {
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_emoji).setOnClickListener(this);
        findViewById(R.id.btn_arrow_down).setOnClickListener(this);
        Button recButton = (Button) findViewById(R.id.recordButton);
        mAdapter = new RecorderAdapter(this, new ArrayList<Recorder>(), this);
        recButton.setOnTouchListener(new RecordTouchListener(this, recButton, mRecorderFinishListener));
        mlistview.setAdapter(mAdapter);
        mlistview.setItemClick(true);
        mlistview.setOnItemClickListener(new RecorderItemListener(this));
    }


    private NoScrollListview mlistview;
    private RecorderAdapter mAdapter;
    private GridView mUserGridView;
    private TextView mUserCountTv;
    private TextView mExecuterCountTv;

    public void Init() {
        attachementView = findViewById(R.id.attachment_view);
        mUserGridView = (GridView) findViewById(R.id.user_gridview);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mContentEt = (EditText) findViewById(R.id.et_sendmessage);
        mContentEt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && mInputMethodManager != null) {
                    boolean bool = mInputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                    if (bool) {
                        hideEmojiFragment();
                    }
                }
                return false;
            }
        });
//        mContentEt.addTextChangedListener(new TextWatcher() {
        EditUtils.limitEditTextLength(mContentEt, Constants.TEXT_MAX_VALUE, new TextWatcher() {
            private boolean hasChanged = true;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (mEmojiView == null || mEmojiView.getVisibility() == View.GONE) {
                    return;
                }

                if (!hasChanged) {
                    return;
                }
                String str = editable.toString();
                if (!Strings.isEmpty(str)) {
                    hasChanged = false;
                    int start = mContentEt.getSelectionStart();
                    int end = mContentEt.getSelectionEnd();
                    mContentEt.setText(EmojiFragment.getEmojiContent(getApplicationContext(), 0, str));
                    mContentEt.setSelection(start, end);
                    hasChanged = true;
                }
            }
        });
        mUserCountTv = (TextView) findViewById(R.id.tv_user_count);
        findViewById(R.id.btn_select_user).setOnClickListener(this);
        mlistview = (NoScrollListview) findViewById(R.id.listview);
        noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
        adapter = new ImgGridAdapter(this, mRightTv);
        adapter.update();
        noScrollgridview.setAdapter(adapter);
        noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int index,
                                    long arg3) {
                if (index == Bimp.drr.size()) {
                    if (view.findViewById(R.id.item_grida_image).getVisibility() == View.VISIBLE) {
                        Intent intent = new Intent(NewPublishedActivity.this,
                                PicSelectActivity.class);
                        startActivityForResult(intent, FROM_PHOTO);
                    }
                } else {
                    Intent intent = new Intent(NewPublishedActivity.this,
                            PhotoActivity.class);
                    intent.putExtra("ID", index);
                    startActivityForResult(intent, DELETE_PICTURE);
                }
            }
        });
    }

    private Pannouncement mPannouncement;

    private boolean isEmpty() {
        String content = mContentEt.getText().toString();
        StringBuilder imagePath = new StringBuilder();
        if (!adapter.getImage().isEmpty()) {
            for (int i = 0; i < adapter.getImage().size(); i++) {
                imagePath.append(adapter.getImage().get(i)).append(",");
            }
            imagePath.deleteCharAt(imagePath.length() - 1);
        }
        StringBuilder audioPaths = new StringBuilder();
        StringBuilder audioTimes = new StringBuilder();
        if (!mAdapter.getData().isEmpty()) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                Recorder recorder = mAdapter.getData().get(i);

                audioPaths.append(recorder.getFilePathString()).append(",");
                audioTimes.append(Math.round(recorder.getTime())).append(",");
            }
            audioPaths.deleteCharAt(audioPaths.length() - 1);
            audioTimes.deleteCharAt(audioTimes.length() - 1);
        }
        StringBuilder receiverIds = new StringBuilder();
        if (mReceiverAdapter != null && mReceiverAdapter.getList() != null && !mReceiverAdapter.getList().isEmpty()) {
            List<Node> node = mReceiverAdapter.getList();
            for (Node n : node) {
                if (n.isUser())
                    receiverIds.append(n.getId()).append(",");
            }
            if (!TextUtil.isEmpty(receiverIds.toString()))
                receiverIds.deleteCharAt(receiverIds.length() - 1);
        }
        StringBuilder executerUserIds = new StringBuilder();
        if (mExecuterAdapter != null && mExecuterAdapter.getList() != null && !mExecuterAdapter.getList().isEmpty()) {
            List<Node> node = mExecuterAdapter.getList();
            for (Node n : node) {
                executerUserIds.append(n.getId()).append(",");
            }
            executerUserIds.deleteCharAt(executerUserIds.length() - 1);
        }
        StringBuilder workReport = new StringBuilder();
        if (mTemplateLayout != null) {
            int itemCount = mTemplateLayout.getChildCount();
            List<TemplateItem> templateItems = new ArrayList<TemplateItem>();
            Template templateObj = (Template) mTemplateLayout.getTag();
            for (int i = 0; i < itemCount; i++) {
                View itemView = mTemplateLayout.getChildAt(i);
                TemplateItem item = (TemplateItem) itemView.getTag();
                TextView lableTv = (TextView) itemView.findViewById(R.id.tv_title);
                EditText contentTv = (EditText) itemView.findViewById(R.id.et_content);
                String subContent = contentTv.getText().toString();
                String subTitle = lableTv.getText().toString();
                workReport.append(subContent);
            }
        }
        if (mPublishType == PUBLISH_SCHEDULE_UPDATE || mPublishType == PUBLISH_SCHEDULE) {
            String startTime = mStartTimeTv.getText().toString();
            if (!TextUtils.isEmpty(startTime)) {
                return false;
            }
            String endTime = mEndTimeTv.getText().toString();
            if (!TextUtils.isEmpty(endTime)) {
                return false;
            }
            if (mScheduleOwCb.isChecked()) {
                if (TextUtils.isEmpty(mLatlng)) {
                    return false;
                }
            }
        }
        return TextUtils.isEmpty(content) && TextUtils.isEmpty(audioPaths) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(receiverIds) && TextUtils.isEmpty(executerUserIds) && TextUtils.isEmpty(workReport);
    }

    private void doSubmit() {
        String content = mContentEt.getText().toString();
        StringBuilder imagePath = new StringBuilder();
        if (!adapter.getImage().isEmpty()) {
            for (int i = 0; i < adapter.getImage().size(); i++) {
                imagePath.append(adapter.getImage().get(i)).append(",");
            }
            imagePath.deleteCharAt(imagePath.length() - 1);
        }
        StringBuilder audioPaths = new StringBuilder();
        StringBuilder audioTimes = new StringBuilder();
        if (!mAdapter.getData().isEmpty()) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                Recorder recorder = mAdapter.getData().get(i);

                audioPaths.append(recorder.getFilePathString()).append(",");
                audioTimes.append(Math.round(recorder.getTime())).append(",");
            }
            audioPaths.deleteCharAt(audioPaths.length() - 1);
            audioTimes.deleteCharAt(audioTimes.length() - 1);
        }
        StringBuilder receiverIds = new StringBuilder();
        if (mReceiverAdapter != null && mReceiverAdapter.getList() != null && !mReceiverAdapter.getList().isEmpty()) {
            List<Node> node = mReceiverAdapter.getList();
            for (Node n : node) {
                if (n.isUser())
                    receiverIds.append(n.getId()).append(",");
            }
            if (!TextUtil.isEmpty(receiverIds.toString()))
                receiverIds.deleteCharAt(receiverIds.length() - 1);
        }
        switch (mPublishType) {
            case PUBLISH_ANNOUNCEMENT: {
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();

                TextView titleTv = (TextView) findViewById(R.id.et_announcement_title);
                String title = titleTv.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(this, R.string.toast_title, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(receiverIds)) {
                    Toast.makeText(this, R.string.toast_send_receiver_announcement, Toast.LENGTH_SHORT).show();
                    return;
                }
                CheckBox checkBox = (CheckBox) findViewById(R.id.rb_top);
                mPannouncement = new Pannouncement();
                mPannouncement.content = content;
                mPannouncement.title = title;
                mPannouncement.isTop = checkBox.isChecked();
                mPannouncement.receiverids = receiverIds.toString();
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                Object obj = attachementView.getTag();
                if (obj instanceof AttachFile) {
                    AttachFile attachFile = (AttachFile) obj;
                    Uri uri = Uri.parse(attachFile.url).buildUpon().appendQueryParameter("fid", String.valueOf(attachFile.fid)).build();
                    mPublishTask.attachment = uri.toString();
                    //TODO
                } else {
                    File f = (File) obj;
                    if (f != null)
                        mPublishTask.attachment = f.getAbsolutePath();
                }
                mPublishTask.type = mPublishType;
                showSubmit();
            }
            break;
            case PUBLISH_FLOW: {
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                Pflow phelp = new Pflow();
                Gson gson = new Gson();
                phelp.content = content;
                StringBuilder executerUserIds = new StringBuilder();
                if (mExecuterAdapter != null && mExecuterAdapter.getList() != null && !mExecuterAdapter.getList().isEmpty()) {
                    List<Node> node = mExecuterAdapter.getList();
                    for (Node n : node) {
                        executerUserIds.append(n.getId()).append(",");
                    }
                    executerUserIds.deleteCharAt(executerUserIds.length() - 1);
                }
                if (TextUtils.isEmpty(executerUserIds.toString())) {
                    Toast.makeText(this, R.string.sp_person_is_not_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                phelp.processerid = executerUserIds.toString();
                phelp.receiverids = receiverIds.toString();
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.type = mPublishType;
                mPublishTask.time = audioTimes.toString();
                mPublishTask.content = gson.toJson(phelp);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_FLOW_LEAVE: {
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mTipTv.getTag() == null) {
                    Toast.makeText(this, R.string.toast_leave_type, Toast.LENGTH_SHORT).show();
                    return;
                }
                String startTime = mStartTimeTv.getText().toString();
                if (TextUtils.isEmpty(startTime)) {
                    Toast.makeText(this, R.string.toast_starttime, Toast.LENGTH_SHORT).show();
                    return;
                }
                String endTime = mEndTimeTv.getText().toString();
                if (TextUtils.isEmpty(startTime)) {
                    Toast.makeText(this, R.string.toast_endtime, Toast.LENGTH_SHORT).show();
                    return;
                }
                long s = Utils.dateFormat(startTime);
                long e = Utils.dateFormat(endTime);
                if (e <= s) {
                    Toast.makeText(this, R.string.toast_endtime_starttime, Toast.LENGTH_SHORT).show();
                    return;
                }

                TextView leaveTimeTv = (TextView) findViewById(R.id.et_leave_time);
                String leaveTime = leaveTimeTv.getText().toString();
                if (TextUtils.isEmpty(leaveTime)) {
                    Toast.makeText(this, R.string.toast_leave_time, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                StringBuilder executerUserIds = new StringBuilder();
                if (mExecuterAdapter != null && mExecuterAdapter.getList() != null && !mExecuterAdapter.getList().isEmpty()) {
                    List<Node> node = mExecuterAdapter.getList();
                    for (Node n : node) {
                        executerUserIds.append(n.getId()).append(",");
                    }
                    executerUserIds.deleteCharAt(executerUserIds.length() - 1);
                }
                if (TextUtils.isEmpty(executerUserIds.toString())) {
                    Toast.makeText(this, R.string.sp_person_is_not_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                Pflow phelp = new Pflow();
                Gson gson = new Gson();
                phelp.startTime = Utils.dateFormat(startTime);
                phelp.endTime = Utils.dateFormat(endTime);
                phelp.leaveType = (int) mTipTv.getTag();
                phelp.leaveTime = Integer.parseInt(leaveTime.toString());
                phelp.processerid = executerUserIds.toString();
                phelp.content = content;
                phelp.receiverids = receiverIds.toString();
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.type = mPublishType;
                mPublishTask.time = audioTimes.toString();
                mPublishTask.content = gson.toJson(phelp);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_FORWARD: {
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                Intent lastIntent = getIntent();
                mPublishTask.publishId = lastIntent.getStringExtra("json");
                int subType = lastIntent.getIntExtra("subtypeId", 0);

                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();

                mPublishTask.time = audioTimes.toString();
                if (TextUtils.isEmpty(receiverIds)) {
                    Toast.makeText(this, R.string.toast_send_receiver_announcement, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (subType == 1)//转发帮帮
                {
                    mPublishTask.type = mPublishType;
                    Phelp phelp = new Phelp();
                    Gson gson = new Gson();
                    phelp.content = content;
                    phelp.subType = subType;
                    phelp.receiverids = receiverIds.toString();
                    mPublishTask.content = gson.toJson(phelp);
                } else if (subType == 2)//转发分享
                {
                    mPublishTask.type = mPublishType;
                    Pshared pshared = new Pshared();
                    Gson gson = new Gson();
                    TextView nameTv = (TextView) findViewById(R.id.address_name);
                    if (nameTv.getTag() != null) {
                        pshared.address = nameTv.getText().toString();
                        pshared.latlng = nameTv.getTag().toString();
                    }
                    pshared.content = content;
                    pshared.subType = subType;
                    pshared.receiverids = receiverIds.toString();
                    mPublishTask.content = gson.toJson(pshared);
                }
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_HELP: {
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();

                Phelp phelp = new Phelp();
                Gson gson = new Gson();
                phelp.content = content;
                if (TextUtils.isEmpty(receiverIds)) {
                    Toast.makeText(this, R.string.toast_send_receiver_announcement, Toast.LENGTH_SHORT).show();
                    return;
                }
                phelp.receiverids = receiverIds.toString();
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.type = mPublishType;
                mPublishTask.time = audioTimes.toString();
                mPublishTask.content = gson.toJson(phelp);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_SHARED: {
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();

                Pshared pshared = new Pshared();
                Gson gson = new Gson();
                pshared.content = content;
                if (TextUtils.isEmpty(receiverIds)) {
                    Toast.makeText(this, R.string.toast_send_receiver_announcement, Toast.LENGTH_SHORT).show();
                    return;
                }
                TextView nameTv = (TextView) findViewById(R.id.address_name);
                if (nameTv.getTag() != null) {
                    pshared.address = nameTv.getText().toString();
                    pshared.latlng = nameTv.getTag().toString();
                }

                pshared.receiverids = receiverIds.toString();
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.type = mPublishType;
                mPublishTask.time = audioTimes.toString();
                mPublishTask.content = gson.toJson(pshared);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_WORKREPORT: {
                int itemCount = mTemplateLayout.getChildCount();
                List<TemplateItem> templateItems = new ArrayList<TemplateItem>();
                Template templateObj = (Template) mTemplateLayout.getTag();
                for (int i = 0; i < itemCount; i++) {
                    View itemView = mTemplateLayout.getChildAt(i);
                    TemplateItem item = (TemplateItem) itemView.getTag();
                    TextView lableTv = (TextView) itemView.findViewById(R.id.tv_title);
                    EditText contentTv = (EditText) itemView.findViewById(R.id.et_content);
                    String subContent = contentTv.getText().toString();
                    String subTitle = lableTv.getText().toString();
                    if (TextUtil.isEmpty(subContent)) {
                        Toast.makeText(this, getString(R.string.toast_input) + subTitle, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TemplateItem template = new TemplateItem(subTitle, subContent);
                    template.id = item.id;
                    templateItems.add(template);
                }

                if (mPublishTask == null)
                    mPublishTask = new PublishTask();

                StringBuilder executerUserIds = new StringBuilder();
                if (mExecuterAdapter != null && mExecuterAdapter.getList() != null && !mExecuterAdapter.getList().isEmpty()) {
                    List<Node> node = mExecuterAdapter.getList();
                    for (Node n : node) {
                        executerUserIds.append(n.getId()).append(",");
                    }
                    executerUserIds.deleteCharAt(executerUserIds.length() - 1);
                }
                if (TextUtils.isEmpty(executerUserIds)) {
                    Toast.makeText(this, R.string.toast_dianping, Toast.LENGTH_SHORT).show();
                    return;
                }
                PworkReport pworkReport = new PworkReport();
                Gson gson = new Gson();
                pworkReport.templateId = templateObj.id;
                pworkReport.type = mReportType;
                if (mReportType == 1) {
                    String date = mWorkReportDateTv.getText().toString();
                    pworkReport.startdate = Utils.dateFormat(date, Utils.DATE_FORMAT);
                    pworkReport.enddate = Utils.dateFormat(date, Utils.DATE_FORMAT);
                } else if (mReportType == 2) {
                    String date = mWorkReportDateTv.getText().toString();
                    String[] dates = date.split("--");
                    pworkReport.startdate = Utils.dateFormat(dates[0], Utils.DATE_FORMAT);
                    pworkReport.enddate = Utils.dateFormat(dates[1], Utils.DATE_FORMAT);
                } else if (mReportType == 3) {
                    String date = mWorkReportDateTv.getText().toString();
                    pworkReport.startdate = CalendarUtils.getMonthFirstDay(date, "yyyy/MM");
//                    pworkReport.enddate = Utils.dateFormat(date, "yyyy/MM");
                    pworkReport.enddate = CalendarUtils.getMonthLastDay(date, "yyyy/MM");
                }
                pworkReport.title = mWorkReportTitleTv.getText().toString();
                pworkReport.content = gson.toJson(templateItems);
                pworkReport.leader = executerUserIds.toString();
                pworkReport.receiverids = receiverIds.toString();

                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.type = mPublishType;
                mPublishTask.time = audioTimes.toString();
                mPublishTask.content = gson.toJson(pworkReport);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();

            }
            break;
            case PUBLISH_SCHEDULE_UPDATE:
            case PUBLISH_SCHEDULE: {
                String startTime = mStartTimeTv.getText().toString();
                if (TextUtils.isEmpty(startTime)) {
                    Toast.makeText(this, R.string.toast_starttime, Toast.LENGTH_SHORT).show();
                    return;
                }
                String endTime = mEndTimeTv.getText().toString();
                if (TextUtils.isEmpty(endTime)) {
                    Toast.makeText(this, R.string.toast_endtime, Toast.LENGTH_SHORT).show();
                    return;
                }

                Pschedule pschedule = new Pschedule();
                pschedule.startTime = Utils.dateFormat(startTime);
                pschedule.endTime = Utils.dateFormat(endTime);
                if (pschedule.endTime <= pschedule.startTime) {
                    Toast.makeText(this, R.string.toast_endtime_starttime, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mScheduleOwCb.isChecked()) {
                    if (TextUtils.isEmpty(mLatlng)) {
                        Toast.makeText(this, R.string.toast_clock_in_location, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rb_group);
                int checkId = radioGroup.getCheckedRadioButtonId();
                int persmission = 1;
                if (checkId == R.id.rb_self) {
                    persmission = 2;
                }

                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                if (mScheduleItem != null) {
                    mPublishTask.publishId = "" + mScheduleItem.scheduleid;
                }

//                if (persmission == 1 && TextUtil.isEmpty(receiverIds.toString())) {
//                    Toast.makeText(this, R.string.toast_receiver, Toast.LENGTH_SHORT).show();
//                    return;
//                }
                StringBuilder ccUserIds = new StringBuilder();
                if (mCcUserAdapter != null && mCcUserAdapter.getList() != null && !mCcUserAdapter.getList().isEmpty()) {
                    List<Node> node = mCcUserAdapter.getList();
                    for (Node n : node) {
                        if (n.isUser())
                            ccUserIds.append(n.getId()).append(",");
                    }
                    if (!TextUtil.isEmpty(ccUserIds.toString()))
                        ccUserIds.deleteCharAt(ccUserIds.length() - 1);
                }

                int tipType = 0;
                Object obj = mTipTv.getTag();
                if (obj != null)
                    tipType = (int) obj;
                pschedule.notifyType = tipType;
                long time = getAlarmTime(tipType);
                if (time != 0)
                    pschedule.notifyTime = pschedule.startTime - time;
                pschedule.content = content;
                pschedule.permission = persmission;
                pschedule.receiverIds = receiverIds.toString();
                pschedule.ccUserIds = ccUserIds.toString();
                if (mScheduleOwCb.isChecked()) {
                    pschedule.isclock = 1;
                    if (!TextUtils.isEmpty(mLatlng)) {
                        String[] gps = mLatlng.split(",");
                        if (gps.length > 1) {
                            pschedule.latitude = gps[0];
                            pschedule.longitude = gps[1];
                        }
                    }
                    pschedule.address = mAddress;
                    pschedule.poiname = mAddName;
                }
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.type = PUBLISH_SCHEDULE;
                mPublishTask.time = audioTimes.toString();
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(pschedule);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_APPLYBUYRESUME: {
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                PapplyResume papplyResume = new PapplyResume();
                papplyResume.content = content;
                StringBuilder executerUserIds = new StringBuilder();
                if (mExecuterAdapter != null && mExecuterAdapter.getList() != null && !mExecuterAdapter.getList().isEmpty()) {
                    List<Node> node = mExecuterAdapter.getList();
                    for (Node n : node) {
                        executerUserIds.append(n.getId()).append(",");
                    }
                    executerUserIds.deleteCharAt(executerUserIds.length() - 1);
                }
                if (TextUtils.isEmpty(executerUserIds.toString())) {
                    Toast.makeText(this, R.string.sp_person_is_not_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                papplyResume.approval_user_id = executerUserIds.toString();
                papplyResume.receiverids = receiverIds.toString();
                String recruit_id = getIntent().getStringExtra("recruit_id");
                papplyResume.recruit_id = recruit_id;
                List<ResumeBuyBean> resumeBuyBeans = (List<ResumeBuyBean>) findViewById(R.id.btn_resume).getTag();
                StringBuilder resumeIds = new StringBuilder();
                for (ResumeBuyBean resume : resumeBuyBeans) {
                    resumeIds.append(resume.resume_id).append(",");
                }
                if (TextUtils.isEmpty(resumeIds.toString())) {
                    Toast.makeText(this, R.string.toast_resume_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                resumeIds.deleteCharAt(resumeIds.length() - 1);
                papplyResume.resume_ids = resumeIds.toString();
                Gson gson = new Gson();
                papplyResume.resumes = gson.toJson(resumeBuyBeans);

                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.content = gson.toJson(papplyResume);
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                mPublishTask.image = imagePath.toString();
                mPublishTask.type = mPublishType;
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_TASK_UPDATE:
            case PUBLISH_TASK: {
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                String finishTime = mPlantTimeTv.getText().toString();
                if (TextUtils.isEmpty(finishTime)) {
                    Toast.makeText(this, R.string.toast_planttime, Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder executerUserIds = new StringBuilder();
                if (mExecuterAdapter != null && mExecuterAdapter.getList() != null && !mExecuterAdapter.getList().isEmpty()) {
                    List<Node> node = mExecuterAdapter.getList();
                    for (Node n : node) {
                        executerUserIds.append(n.getId()).append(",");
                    }
                    executerUserIds.deleteCharAt(executerUserIds.length() - 1);
                }
                if (TextUtils.isEmpty(executerUserIds)) {
                    Toast.makeText(this, R.string.toast_executer, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                if (mTask != null) {
                    mPublishTask.publishId = mTask.taskid;
                }
                mPublishTask.type = PUBLISH_TASK;
                Ptask ptask = new Ptask();
                mPublishTask.image = imagePath.toString();
                ptask.content = content;
                ptask.planFinishTime = Utils.dateFormat(finishTime);
                int tipType = 0;
                Object obj = mTaskTipTv.getTag();
                if (obj != null)
                    tipType = (int) obj;
                ptask.notifyType = tipType;
                long time = getAlarmTime(tipType);
                if (time != 0)
                    ptask.notifyTime = ptask.planFinishTime - time;
                ptask.processerId = executerUserIds.toString();
                ptask.receiverIds = receiverIds.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(ptask);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_WORK_REPORT: {
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.type = mPublishType;
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(audioPaths)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intentParams = getIntent();
                String commentId = intentParams.getStringExtra("workReportId");
                Pcomment pcomment = new Pcomment();
                pcomment.commentId = commentId;
                pcomment.content = content;
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(pcomment);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_TASK_CONDUCT: {
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(audioPaths)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                int type = getIntent().getIntExtra(Constants.TYPE, 0);
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.type = mPublishType;
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                Intent intentParams = getIntent();
                String commentId = intentParams.getStringExtra("taskid");
                Pcomment pcomment = new Pcomment();
                pcomment.commentId = commentId;
                pcomment.content = content;
                pcomment.commentType = type;
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(pcomment);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_FLOW_CONDUCT: {
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(audioPaths)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                int type = getIntent().getIntExtra(Constants.TYPE, 0);
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.type = mPublishType;
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                Intent intentParams = getIntent();
                String commentId = intentParams.getStringExtra("flowId");
                Pcomment pcomment = new Pcomment();
                pcomment.commentId = commentId;
                pcomment.content = content;
                pcomment.commentType = type;
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(pcomment);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_RECRUIT_FINISH:
            case PUBLISH_RESUME_APPROVE:
            case PUBLISH_RECRUIT_APPROVE: {
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(audioPaths)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                int type = getIntent().getIntExtra(Constants.TYPE, 0);
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.type = mPublishType;
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                Intent intentParams = getIntent();
                String commentId = intentParams.getStringExtra("resource_id");
                Pcomment pcomment = new Pcomment();
                pcomment.commentId = commentId;
                pcomment.content = content;
                pcomment.commentType = type;
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(pcomment);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_SCHEDULE_CONDUCT: {
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(audioPaths)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                int type = getIntent().getIntExtra(Constants.TYPE, 0);
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.type = mPublishType;
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                Intent intentParams = getIntent();
                String commentId = intentParams.getStringExtra("scheduleId");
                Pcomment pcomment = new Pcomment();
                pcomment.commentId = commentId;
                pcomment.content = content;
                pcomment.commentType = type;
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(pcomment);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_FEEDBACK: {
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(audioPaths)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.type = mPublishType;
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();

                Gson gson = new Gson();
                mPublishTask.content = content;
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
            break;
            case PUBLISH_COMMENT:
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(imagePath) && TextUtils.isEmpty(audioPaths)) {
                    Toast.makeText(this, R.string.toast_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mPublishTask == null)
                    mPublishTask = new PublishTask();
                mPublishTask.type = mPublishType;
                mPublishTask.image = imagePath.toString();
                mPublishTask.audio = audioPaths.toString();
                mPublishTask.time = audioTimes.toString();
                if (TextUtils.isEmpty(content) && TextUtils.isEmpty(mPublishTask.image) && TextUtils.isEmpty(mPublishTask.audio)) {
                    Toast.makeText(this, R.string.toast_comment, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intentParams = getIntent();
                int commentType = intentParams.getIntExtra("comment_type", 0);
                String commentId = intentParams.getStringExtra("publishId");
                int position = intentParams.getIntExtra("position", -1);
                mPublishTask.setPosition(position);
                String replyuserid = getIntent().getStringExtra("replyuserid");
                String replayUser = getIntent().getStringExtra("replayUser");
                Pcomment pcomment = new Pcomment();
                pcomment.commentId = commentId;
                pcomment.commentType = commentType;
                pcomment.content = content;
                pcomment.replayUser = replayUser;
                pcomment.replyuserid = replyuserid;
                Gson gson = new Gson();
                mPublishTask.content = gson.toJson(pcomment);
                Intent intent = new Intent(this, SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
                break;
        }

//        mNetworkManager.load(CALLBACK_TASK_CREATE, path, this);
    }

    public long getAlarmTime(int type) {
        long time = 0;
        switch (type) {
            case 1:
                time = 10 * 60 * 1000;
                break;
            case 2:
                time = 30 * 60 * 1000;
                break;
            case 3:
                time = 60 * 60 * 1000;
                break;

        }
        return time;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitEdit();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void showExitEdit() {
        if (isEmpty()) {
            finish();
            return;
        }
        new AlertDialog(this).builder().setTitle(getString(R.string.exit_edit))
                .setMsg(getString(R.string.logout_current_account_confirm))
                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        }
                ).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    private void showSubmit() {
        new AlertDialog(this).builder().setTitle(getString(R.string.lable_send_announcement))
                .setPositiveButton(getString(R.string.btn_send), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Gson gson = new Gson();
                        mPannouncement.isSend = true;
                        mPublishTask.content = gson.toJson(mPannouncement);
                        Intent intent = new Intent(getApplicationContext(), SubmitService.class);
                        intent.putExtra("publishTask", mPublishTask);
                        startService(intent);
                        finish();
                    }
                }).setNegativeButton(getString(R.string.lable_send_caogao), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                mPannouncement.isSend = false;
                mPublishTask.content = gson.toJson(mPannouncement);
                Intent intent = new Intent(getApplicationContext(), SubmitService.class);
                intent.putExtra("publishTask", mPublishTask);
                startService(intent);
                finish();
            }
        }).show();
    }

    private int mReportType = 1;
    private static final int REQUEST_CODE_USERSELECT = 1001;
    private static final int REQUEST_CODE_CCUSERSELECT = 1008;
    private static final int REQUEST_CODE_EXECUTER = 1002;
    private static final int REQUEST_CODE_FILE_CONFIG = 5001;
    private static final int REQUEST_CODE_LOCATION = 5002;
    private static final int REQUEST_CODE_RESUME = 5003;
    private static final int REQUEST_CODE_MAP_POI = 5004;
    private FileConfig fileConfig;
    private String[] dateTypes = null;

    @Override
    protected void onPause() {
        super.onPause();
        if (mContentEt != null && mInputMethodManager != null)
            mInputMethodManager.hideSoftInputFromWindow(
                    mContentEt.getWindowToken(), 0);
    }

    private void saveWorkReport() {
        if (mTemplateLayout != null) {
            int itemCount = mTemplateLayout.getChildCount();
            for (int i = 0; i < itemCount; i++) {
                View itemView = mTemplateLayout.getChildAt(i);
                TemplateItem item = (TemplateItem) itemView.getTag();
                TextView lableTv = (TextView) itemView.findViewById(R.id.tv_title);
                EditText contentTv = (EditText) itemView.findViewById(R.id.et_content);
                String subContent = contentTv.getText().toString();
                item.content = subContent;
            }
        }
    }

    private String mLatlng;
    private String mAddress;
    private String mAddName;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_schedule_outwook_address: {
                Intent intent = new Intent(this, MapLocationActivity.class);
                intent.putExtra("latlng", mLatlng);
                intent.putExtra("address", mAddress);
                intent.putExtra("name", mAddName);
                intent.putExtra("edit", true);
                startActivityForResult(intent, REQUEST_CODE_MAP_POI);
            }
            break;
            case R.id.btn_back:
                showExitEdit();
                break;
            case R.id.btn_camera: {
                photo();
            }
            break;
            case R.id.btn_resume: {
//                Intent intent = new Intent(this, ResumeListActivity.class);
//                List<ResumeBuyBean> resumeBuyBeans = (List<ResumeBuyBean>) v.getTag();
//                intent.putExtra("resumes", new Gson().toJson(resumeBuyBeans));
//                startActivityForResult(intent, REQUEST_CODE_RESUME);
            }
            break;
            case R.id.btn_location: {
                Intent intent = new Intent(this, MapLocationActivity.class);
                intent.putExtra("latlng", mLatlng);
                intent.putExtra("address", mAddress);
                intent.putExtra("name", mAddName);
                intent.putExtra("edit", true);
                startActivityForResult(intent, REQUEST_CODE_LOCATION);
//                Intent intent = new Intent(this, LocationActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_LOCATION);
            }
            break;
            case R.id.btn_attachment_delete: {
                TextView nameTv = (TextView) findViewById(R.id.attachment_name);
                nameTv.setText("");
                attachementView.setTag(null);
                attachementView.setVisibility(View.GONE);
            }
            break;
            case R.id.btn_delete_address:
                TextView nameTv = (TextView) findViewById(R.id.address_name);
                nameTv.setText(R.string.lable_location);
                nameTv.setTextColor(Color.GRAY);
                findViewById(R.id.btn_delete_address).setVisibility(View.GONE);
                nameTv.setTag(null);
                break;
            case R.id.btn_attachment: {
                if (fileConfig == null) {
                    fileConfig = new FileConfig();
                    fileConfig.filterModel = FileFilter.FILTER_NOTICE;
                }
                Intent intent = new Intent(getApplicationContext(), FileSelectorActivity.class);
                fileConfig.startPath = Environment.getExternalStorageDirectory().getPath();
                fileConfig.rootPath = "/";
                //传递配置文件
                intent.putExtra(FileConfig.FILE_CONFIG, fileConfig);
                //启动
                startActivityForResult(intent, REQUEST_CODE_FILE_CONFIG);

            }
            break;
            case R.id.btn_cc_user: {//日程抄送人
                Intent intent = new Intent(this, OrganizationSelectedActivity.class);
                if (mCcUserAdapter != null) {
                    intent.putParcelableArrayListExtra("select", mCcUserAdapter.getList());
                }
                if (mReceiverAdapter != null) {
                    intent.putParcelableArrayListExtra("unselect", mReceiverAdapter.getList());
                }
                //intent.putExtra("selectSelf",true);
                intent.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_MULTI);
                startActivityForResult(intent, REQUEST_CODE_CCUSERSELECT);
            }
            break;
            case R.id.btn_select_user: {
                Intent intent = new Intent(this, OrganizationSelectedActivity.class);
                if (mReceiverAdapter != null) {
                    intent.putParcelableArrayListExtra("select", mReceiverAdapter.getList());
                }
                if (mExecuterAdapter != null) {
                    intent.putParcelableArrayListExtra("unselect", mExecuterAdapter.getList());
                }
                if (mCcUserAdapter != null) {
                    intent.putParcelableArrayListExtra("unselect", mCcUserAdapter.getList());
                }
                //intent.putExtra("selectSelf",true);
                intent.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_MULTI);
                startActivityForResult(intent, REQUEST_CODE_USERSELECT);
            }
            break;
            case R.id.btn_executer_user: {
                Intent intent = new Intent(this, OrganizationSelectedActivity.class);
                if (mExecuterAdapter != null) {
                    intent.putParcelableArrayListExtra("select", mExecuterAdapter.getList());
                }
                if (mReceiverAdapter != null) {
                    intent.putParcelableArrayListExtra("unselect", mReceiverAdapter.getList());
                }
                intent.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_SINGLE);
                startActivityForResult(intent, REQUEST_CODE_EXECUTER);
            }

            break;
            case R.id.start_time_click: {
                DataUtils.showDateSelect(this, mStartTimeTv, DateFullDialogView.DATE_TYPE_MINUTE_SPIT_FIVE, null);
            }
            break;
            case R.id.end_time_click: {
                DataUtils.showDateSelect(this, mEndTimeTv, DateFullDialogView.DATE_TYPE_MINUTE_SPIT_FIVE, null);
            }
            break;
            case R.id.plant_time_click: {
                DataUtils.showDateSelect(this, mPlantTimeTv, DataUtils.DATE_TYPE_AFTER_TODAY, null);
            }
            break;
            case R.id.btn_leave_type_click: {
                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);

                for (String option : getResources().getStringArray(R.array.leave_type)) {
                    actionSheetDialog.addSheetItem(option, ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    String type = getResources().getStringArray(R.array.leave_type)[which];
                                    mTipTv.setText(type);
                                    mTipTv.setTag(which);
                                }
                            });
                }
                actionSheetDialog.show();
            }
            break;
            case R.id.btn_task_type_click: {
                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);

                for (String option : getResources().getStringArray(R.array.tip_type)) {
                    actionSheetDialog.addSheetItem(option, ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    String type = getResources().getStringArray(R.array.tip_type)[which];
                                    mTaskTipTv.setText(type);
                                    mTaskTipTv.setTag(which);
                                }
                            });
                }
                actionSheetDialog.show();

//                DialogSelectView dialogSelectView = new DialogSelectView(this, mTaskTipTv, getResources().getStringArray(R.array.tip_type));
//                dialogSelectView.show(mTaskTipTv);
            }
            break;
            case R.id.btn_schedule_type_click: {

                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);

                for (String option : getResources().getStringArray(R.array.tip_type)) {
                    actionSheetDialog.addSheetItem(option, ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    String type = getResources().getStringArray(R.array.tip_type)[which];
                                    mTipTv.setText(type);
                                    mTipTv.setTag(which);
                                }
                            });
                }
                actionSheetDialog.show();

//                DialogSelectView dialogSelectView = new DialogSelectView(this, mTipTv, getResources().getStringArray(R.array.tip_type));
//                dialogSelectView.show(mTipTv);
            }
            break;
            case R.id.btn_work_report_type_click: {


                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);

                for (String option : getResources().getStringArray(R.array.report_type)) {
                    actionSheetDialog.addSheetItem(option, ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    String type = getResources().getStringArray(R.array.report_type)[which];
                                    String[] array = getResources().getStringArray(R.array.report_type);
                                    if (array[0].equals(type)) {
                                        if (mReportType == 1) {
                                            return;
                                        }/* else if (!isWorkReportEmpty()) {
                                            showTipEdit(1,type);
                                            return;
                                        }*/
                                        mWorkReportTypeTv.setText(type);
                                        mReportType = 1;
                                        mWorkReportDateTv.setText(mDayArray[0]);
                                        initTemplateView(mDayTemplates, -1);
                                    } else if (array[1].equals(type)) {
                                        if (mReportType == 2) {
                                            return;
                                        } /*else if (!isWorkReportEmpty()) {
                                            showTipEdit(2,type);
                                            return;
                                        }*/
                                        mWorkReportTypeTv.setText(type);
                                        mReportType = 2;
                                        mWorkReportDateTv.setText(mWeekArray[0]);
                                        initTemplateView(mWeekTemplates, -1);
                                    } else if (array[2].equals(type)) {
                                        if (mReportType == 3) {
                                            return;
                                        }/* else if (!isWorkReportEmpty()) {
                                            showTipEdit(3,type);
                                            return;
                                        }*/
                                        mWorkReportTypeTv.setText(type);
                                        mReportType = 3;
                                        mWorkReportDateTv.setText(mMonthArray[0]);
                                        initTemplateView(mMonthTemplates, -1);
                                    }
                                    mWorkReportTitleTv.setText(mWorkReportTypeTv.getText() + "(" + mWorkReportDateTv.getText() + ")");

                                }
                            });
                }
                actionSheetDialog.show();
            }
            break;
            case R.id.tv_work_report_date_click: {

                if (mReportType == 1) {
                    dateTypes = mDayArray;
                } else if (mReportType == 2) {
                    dateTypes = mWeekArray;
                } else if (mReportType == 3) {
                    dateTypes = mMonthArray;
                }


                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);

                for (String option : dateTypes) {
                    actionSheetDialog.addSheetItem(option, ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    String type = dateTypes[which];
                                    mWorkReportDateTv.setText(type);

                                    mWorkReportTitleTv.setText(mWorkReportTypeTv.getText() + "(" + mWorkReportDateTv.getText() + ")");
                                }
                            });
                }
                actionSheetDialog.show();
            }
            break;

            case R.id.btn_emoji:
                showEmojiFragment();
                break;
            case R.id.btn_arrow_down:
                hideEmojiFragment();
                if (mContentEt != null)
                    mInputMethodManager.hideSoftInputFromWindow(
                            mContentEt.getWindowToken(), 0);
                break;
            case R.id.tv_right:
                doSubmit();
                break;
            case R.id.btn_voice_delete: {
                Recorder recorder = (Recorder) v.getTag();
                if (recorder.isLocal()) {
                    File file = new File(recorder.getFilePathString());
                    file.deleteOnExit();
                }
                mAdapter.getData().remove(recorder);
                mAdapter.notifyDataSetChanged();
            }
            break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * @Description:
     * @Created:shaofang 2014年6月26日下午9:52:18
     * @Modified:
     */
    @Override
    public void finish() {
        Bimp.drr.clear();
        MediaManager.stop();
        super.finish();
    }

    public String getString(String s) {
        String path = null;
        if (s == null)
            return "";
        for (int i = s.length() - 1; i > 0; i++) {
            s.charAt(i);
        }
        return path;
    }

    private static final int TAKE_PICTURE = 0x000000;
    private static final int FROM_PHOTO = 0x000001;
    private static final int DELETE_PICTURE = 0x000002;
    private String path = "";

    public void photo() {
        if (Bimp.drr.size() < 9) {
//            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            File file = new File(FileCacheUtils.getPublishImageDir(this), String.valueOf(System.currentTimeMillis())
//                    + ".ing");
//            path = file.getPath();
//            Uri imageUri = Uri.fromFile(file);
//            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//            startActivityForResult(openCameraIntent, TAKE_PICTURE);
            PermissionsChecker mChecker = new PermissionsChecker(NewPublishedActivity.this);
            if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                // 请求权限
                PermissionsActivity.startActivityForResult(NewPublishedActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
            } else {
                // 全部权限都已获取
                EasyPhotos.createCamera(NewPublishedActivity.this)
                        .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                        .start(TAKE_PICTURE);
            }
        } else {
            Toast.makeText(this, R.string.tip_more_nine, Toast.LENGTH_SHORT).show();
        }

    }

    private void setGridView(ArrayList<Node> nodes, ArrayList<Node> desplayNodes) {
        if (mReceiverAdapter == null) {
            mReceiverAdapter = new UserGridAdapter(this,
                    nodes, desplayNodes, mUserCountTv);
            mUserGridView.setAdapter(mReceiverAdapter);
        } else {
            mReceiverAdapter.setList(nodes, desplayNodes);
        }
    }

    private void setCcuserGridView(ArrayList<Node> nodes, ArrayList<Node> desplayNodes) {
        if (mCcUserAdapter == null) {
            mCcUserAdapter = new UserGridAdapter(this,
                    nodes, desplayNodes, mCcUserCountTv);
            mCcGrid.setAdapter(mCcUserAdapter);
        } else {
            mCcUserAdapter.setList(nodes, desplayNodes);
        }
    }

    private void setGridView(ArrayList<Node> nodes) {
        ArrayList<Node> desplayNodes = new ArrayList<>();
        desplayNodes.addAll(nodes);
        setGridView(nodes, desplayNodes);
    }

    private UserGridAdapter mExecuterAdapter;
    private UserGridAdapter mReceiverAdapter;
    private UserGridAdapter mCcUserAdapter;

    private void setmExecuterGridView(ArrayList<Node> nodes) {
        if (mExecuterAdapter == null) {
            mExecuterAdapter = new UserGridAdapter(this,
                    nodes, mExecuterCountTv);
            mExecuterGrid.setAdapter(mExecuterAdapter);
        } else {
            mExecuterAdapter.setList(nodes, nodes);
        }

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {

            case REQUEST_CODE_RESUME:
                String resumes = data.getStringExtra("resumes");
                getIntent().putExtra("resumes", resumes);
                initResumeData(resumes);
                break;
            case REQUEST_CODE_FILE_CONFIG:
                ArrayList<String> list = data.getStringArrayListExtra(FileSelector.RESULT);
                if (list != null && !list.isEmpty()) {
                    attachementView = findViewById(R.id.attachment_view);
                    findViewById(R.id.btn_attachment_delete).setOnClickListener(this);
                    TextView nameTv = (TextView) findViewById(R.id.attachment_name);
                    File file = new File(list.get(0));
                    nameTv.setText(file.getName());
                    attachementView.setTag(file);
                    attachementView.setVisibility(View.VISIBLE);
                }
                break;
            case REQUEST_CODE_MAP_POI:
                mLatlng = data.getStringExtra("latlng");
                mAddress = data.getStringExtra("address");
                mAddName = data.getStringExtra("name");
                if (TextUtils.isEmpty(mAddName)) {
                    mScheduleOwAddTv.setText(mAddress);
                } else {
                    mScheduleOwAddTv.setText(mAddName);
                }

                break;
            case REQUEST_CODE_LOCATION:
                mLatlng = data.getStringExtra("latlng");
                mAddress = data.getStringExtra("address");
                mAddName = data.getStringExtra("name");
                if (!TextUtils.isEmpty(mAddName))
                    mAddress = mAddName;
                View addressView = findViewById(R.id.address_view);
                addressView.setVisibility(View.VISIBLE);
                TextView nameTv = (TextView) findViewById(R.id.address_name);
                nameTv.setText(mAddress);
                nameTv.setTag(mLatlng);
                nameTv.setTextColor(Color.parseColor("#4ab9fc"));
                findViewById(R.id.btn_delete_address).setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_CCUSERSELECT: {
                ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                ArrayList<Node> desplayNodes = data.getParcelableArrayListExtra("desplayNodes");
                setCcuserGridView(userSelectList, desplayNodes);
            }
            break;
            case REQUEST_CODE_USERSELECT: {
                ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                ArrayList<Node> desplayNodes = data.getParcelableArrayListExtra("desplayNodes");
                setGridView(userSelectList, desplayNodes);
            }
            break;
            case REQUEST_CODE_EXECUTER:
                ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                setmExecuterGridView(userSelectList);
                break;
            case TAKE_PICTURE:
                if (Bimp.drr.size() < 9 && resultCode == -1) {
                    //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                    ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                    //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                    boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                    if (resultPaths != null && resultPaths.size() > 0) {
                        path = resultPaths.get(0);
                        Bimp.drr.add(path);
                    }
                }
                adapter.update();
                break;
            case DELETE_PICTURE:
                adapter.notifyDataSetChanged();
            case FROM_PHOTO:
                adapter.update();
                break;
        }
    }

    public boolean swipeBackPriority() {
        return false;
    }
}
