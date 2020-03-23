package com.vgtech.vancloud.ui.module.task;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.CommentInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.Processer;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.Task;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.TabComPraiseIndicator;
import com.vgtech.common.view.TabInfo;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.AudioListAdapter;
import com.vgtech.vancloud.ui.adapter.MyFragmentPagerAdapter;
import com.vgtech.vancloud.ui.adapter.ViewListener;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.CountListener;
import com.vgtech.vancloud.ui.common.commentandpraise.CommentListFragment;
import com.vgtech.vancloud.ui.common.commentandpraise.PraiseListFragment;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.ReciverUserActivity;
import com.vgtech.vancloud.ui.module.recruit.RecruitmentDetailsActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.view.LinkTouchMovementMethod;
import com.vgtech.vancloud.ui.view.scrollablelayoutlib.ScrollableHelper;
import com.vgtech.vancloud.ui.view.scrollablelayoutlib.ScrollableLayout;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.VgTextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务办理
 * Created by Duke on 2015/8/21.
 */
public class TaskTransactActivity extends BaseActivity implements View.OnClickListener, HttpListener<String>, ViewListener, CountListener {


    RelativeLayout commentClickView;
    RelativeLayout praiseCLickView;

    SimpleDraweeView userPhotoView;
    TextView userNameView;
    TextView timestampView;


    TextView transactorNameView;
    TextView reciverNamesView;
//    TextView reciverNumView;

    LinearLayout processerLayout;
    SimpleDraweeView processerPhotoView;
    TextView processerNameView;
    TextView processerMestampView;
    TextView processerContentTextView;
    NoScrollGridview processerImageGridView;
    NoScrollListview processerVoiceListView;

    TextView topRightView;


    TextView contentTextView;
    NoScrollGridview imageGridView;
    TextView leftTimeView;
    TextView rightTimeView;
    ImageView finishLogoView;
    NoScrollListview voiceListview;

    private ImageView praiseImageView;
    private TextView praiseTextView;


    private String taskID;
    private Task task;

    private static final int CALLBACK_TASKINFO = 1;
    private static final int CALLBACK_TASKCANCLE = 2;


    private NetworkManager mNetworkManager;
    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";
    public static final String RECEIVER_PUSH = "RECEIVER_PUSH";
    int position;
    boolean backRefresh = false;
    private RelativeLayout cancelClickView;

    private ImageView commentImageView;
    private TextView commentTextView;

    private boolean isShowCommment;


    //    private LinearLayout LoadingView;
    private LinearLayout dataInfoLayout;
//    private ProgressWheel loadingBar;
//    private TextView loadingMagView;

    private TextView clickToDetailsView;
    private boolean fromeNotice;
    private ScrollableLayout mScrollLayout;
    private ViewPager mViewPager;
    private TabComPraiseIndicator mTitleIndicator;
    private VancloudLoadingLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.task_transact_title));

        mNetworkManager = getAppliction().getNetworkManager();

        Intent intent = getIntent();
        taskID = intent.getStringExtra("TaskID");
        String taskJson = intent.getStringExtra("Task");
        position = intent.getIntExtra("position", -1);
        isShowCommment = intent.getBooleanExtra("showcomment", false);
        fromeNotice = intent.getBooleanExtra("fromeNotice", false);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_DRAFT);
        intentFilter.addAction(RECEIVER_PUSH);
        intentFilter.addAction(RECEIVER_ERROR);
        registerReceiver(mReceiver, intentFilter);
        initLayout();
        initView();
        try {
            if (!TextUtil.isEmpty(taskJson)) {
                task = JsonDataFactory.getData(Task.class, new JSONObject(taskJson));
                setTaskInfoToView(task);
                addCommentFragment();
                requestTaskInfo(taskID, false);
            } else {
                requestTaskInfo(taskID, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initLayout() {
        ViewStub viewStub = (ViewStub) findViewById(R.id.action_task);
        viewStub.inflate();
        ViewStub detail_task = (ViewStub) findViewById(R.id.detail_task);
        detail_task.inflate();
    }

    @Override
    protected int getContentView() {
        return R.layout.app_detail;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.comment_click: {
                if (!"2".equals(task.repealstate) && "1".equals(task.iscanviewdetail))
                    PublishUtils.addComment(this, PublishUtils.COMMENTTYPE_TASK, task.taskid);
                else
                    showToast(R.string.task_cancel_prompt);

            }
            break;
            case R.id.praise_click:

                if (!"2".equals(task.repealstate) && "1".equals(task.iscanviewdetail)) {
                    PublishUtils.toDig(this, task.taskid, PublishUtils.COMMENTTYPE_TASK, task.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(digType);
                        }
                    });
                } else
                    showToast(R.string.task_cancel_prompt);
                break;

            case R.id.tv_right:

                if (topRightView.getText().equals(getResources().getString(R.string.revise))) {
                    //TODO
                    //修改
                    if (task.resource == 1) {
                        Intent intent = new Intent(this, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_TASK_UPDATE);
                        intent.putExtra("taskInfo", task.getJson().toString());
                        startActivity(intent);
                    }

                } else {
                    //TODO
                    //完成
//                    if (task.resource == 2) {
//                        PublishUtils.recruitFinish(this, task.resourceid);
//                    } else {
                    Intent intent = new Intent(this, NewPublishedActivity.class);
                    intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_TASK_CONDUCT);
                    intent.putExtra(Constants.TYPE, Constants.FINISH);
                    intent.putExtra("taskid", task.taskid);
                    startActivity(intent);
//                    }
                }

                break;

            case R.id.cancel_click:
                if (task.resource == 1) {
                    new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                            .setMsg(getString(R.string.cancel_task))
                            .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancleTask(task.taskid);
                                }
                            }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
                break;
            case R.id.mid_click:
                if (task.resource == 2) {
                    Intent intent = new Intent(this, RecruitmentDetailsActivity.class);
                    intent.putExtra("id", task.resourceid);
                    intent.putExtra("type", "1");
                    startActivity(intent);
                }
                break;
            default:
                super.onClick(v);
                break;
        }

    }


    private void initView() {

        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.load_view);
        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                requestTaskInfo(taskID, true);
            }
        });
        mTitleIndicator = (TabComPraiseIndicator) findViewById(R.id.title_indicator);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scrollableLayout);
        mScrollLayout.setHeaderIndex(2);
        mScrollLayout.scrollToBar(isShowCommment);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        topRightView = initRightTv(getString(R.string.finish));
        commentClickView = (RelativeLayout) findViewById(R.id.comment_click);
        praiseCLickView = (RelativeLayout) findViewById(R.id.praise_click);
        praiseImageView = (ImageView) findViewById(R.id.img02);
        praiseTextView = (TextView) findViewById(R.id.praisetext);
        commentImageView = (ImageView) findViewById(R.id.img01);
        commentTextView = (TextView) findViewById(R.id.commenttext);
        cancelClickView = (RelativeLayout) findViewById(R.id.cancel_click);
        topRightView.setOnClickListener(this);

        userPhotoView = (SimpleDraweeView) findViewById(R.id.user_photo);
        userNameView = (TextView) findViewById(R.id.user_name);
        timestampView = (TextView) findViewById(R.id.timestamp);
        contentTextView = (TextView) findViewById(R.id.content_text);
        contentTextView.setTextIsSelectable(true);
        leftTimeView = (TextView) findViewById(R.id.left_time_text);
        rightTimeView = (TextView) findViewById(R.id.right_time_text);

        finishLogoView = (ImageView) findViewById(R.id.finish_logo);

        imageGridView = (NoScrollGridview) findViewById(R.id.imagegridview);

        transactorNameView = (TextView) findViewById(R.id.transactor_name);
        reciverNamesView = (TextView) findViewById(R.id.reciver_names);
        processerLayout = (LinearLayout) findViewById(R.id.processer_layout);
        processerPhotoView = (SimpleDraweeView) findViewById(R.id.processer_photo);
        processerNameView = (TextView) findViewById(R.id.processer_name);
        processerMestampView = (TextView) findViewById(R.id.processer_mestamp);
        processerContentTextView = (TextView) findViewById(R.id.processer_content_text);
        processerImageGridView = (NoScrollGridview) findViewById(R.id.processer_imagegridview);
        processerVoiceListView = (NoScrollListview) findViewById(R.id.processer_voice_listview);
        voiceListview = (NoScrollListview) findViewById(R.id.voice_listview);

//        LoadingView = (LinearLayout) findViewById(R.id.loading_view);
//        loadingBar = (ProgressWheel) findViewById(R.id.progress_view);
        dataInfoLayout = (LinearLayout) findViewById(R.id.info);
//        loadingMagView = (TextView) findViewById(R.id.loadding_msg);

        clickToDetailsView = (TextView) findViewById(R.id.click_to_details);
        findViewById(R.id.mid_click).setOnClickListener(this);
    }

    private void setTaskInfoToView(Task task) {
        NewUser user = task.getData(NewUser.class);
        userNameView.setText(Html.fromHtml(user.name));
        GenericDraweeHierarchy hierarchy = userPhotoView.getHierarchy();
        hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
        hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
        userPhotoView.setImageURI(user.photo);
        UserUtils.enterUserInfo(this, user.userid + "", user.name, user.photo, userPhotoView);

        timestampView.setText(Utils.getInstance(this).dateFormat(task.timestamp));
        contentTextView.setText(EmojiFragment.getEmojiContent(this, contentTextView.getTextSize(),Html.fromHtml(task.content)));

        if (task.resource == 1)
            clickToDetailsView.setVisibility(View.GONE);
        else
            clickToDetailsView.setVisibility(View.VISIBLE);

        leftTimeView.setText(getResources().getString(R.string.plant_) + "：" + Utils.getInstance(this).dateFormat(task.plantime));
        commentClickView.setOnClickListener(this);
        praiseCLickView.setOnClickListener(this);
        cancelClickView.setOnClickListener(this);
        if (task.ispraise) {
            praiseImageView.setImageResource(R.drawable.item_praise_click_red);
            praiseTextView.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            praiseImageView.setImageResource(R.drawable.item_praise_click);
            praiseTextView.setTextColor(EditUtils.greyCreateColorStateList());
        }

        //"2", 代表撤销  1 代表正常使用
        if ("2".equals(task.repealstate) || "2".equals(task.iscanviewdetail)) {
            if (PrfUtils.isChineseForAppLanguage(this))
                finishLogoView.setImageResource(R.mipmap.cancel_logo_ch);
            else
                finishLogoView.setImageResource(R.mipmap.cancel_logo_en);
            finishLogoView.setVisibility(View.VISIBLE);
            rightTimeView.setVisibility(View.GONE);
            praiseImageView.setSelected(true);
            praiseTextView.setSelected(true);
            commentTextView.setSelected(true);
            commentImageView.setSelected(true);
            topRightView.setVisibility(View.GONE);
            cancelClickView.setVisibility(View.GONE);
        } else {
            //2代表 未完成  1 代表 已完成
            if ("1".equals(task.state)) {
                if (PrfUtils.isChineseForAppLanguage(this))
                    finishLogoView.setImageResource(R.mipmap.finish_logo_ch);
                else
                    finishLogoView.setImageResource(R.mipmap.finish_logo_en);
                finishLogoView.setVisibility(View.VISIBLE);
                rightTimeView.setVisibility(View.VISIBLE);
                rightTimeView.setText(getResources().getString(R.string.finish_time) + "：" + Utils.getInstance(this).dateFormat(task.finishtime));
                rightTimeView.setVisibility(View.VISIBLE);
                praiseImageView.setSelected(false);
                praiseTextView.setSelected(false);
                commentTextView.setSelected(false);
                commentImageView.setSelected(false);
                topRightView.setVisibility(View.GONE);
                cancelClickView.setVisibility(View.GONE);
            } else {
                finishLogoView.setVisibility(View.GONE);
                rightTimeView.setVisibility(View.GONE);
                //type = 3代表抄送给我的
                switch (task.type) {
                    case 1:
                        topRightView.setVisibility(View.VISIBLE);
                        topRightView.setText(getResources().getString(R.string.btn_finish));
                        cancelClickView.setVisibility(View.GONE);
                        break;
                    case 2:
                        if (task.resource == 1) {
                            topRightView.setVisibility(View.VISIBLE);
                            topRightView.setText(getResources().getString(R.string.revise));
                            cancelClickView.setVisibility(View.VISIBLE);
                        } else {
                            topRightView.setVisibility(View.GONE);
                            cancelClickView.setVisibility(View.GONE);
                        }
                        break;
                    case 3:
                    case 4:
                        topRightView.setVisibility(View.GONE);
                        cancelClickView.setVisibility(View.GONE);
                        break;
                }
                praiseImageView.setSelected(false);
                praiseTextView.setSelected(false);
            }
        }

        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(task.getJson().getString("audio"))) {
                audios = JsonDataFactory.getDataArray(AudioInfo.class, task.getJson().getJSONArray("audio"));
            }
            if (!TextUtils.isEmpty(task.getJson().getString("image"))) {
                images = JsonDataFactory.getDataArray(ImageInfo.class, task.getJson().getJSONArray("image"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (images.size() > 0) {
            imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imageGridView, TaskTransactActivity.this, images);
            imageGridView.setAdapter(imageGridviewAdapter);
        } else {
            imageGridView.setVisibility(View.GONE);
        }

        if (audios.size() > 0) {

            voiceListview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            voiceListview.setAdapter(audioListAdapter);

        } else {
            voiceListview.setVisibility(View.GONE);
        }
    }

    private void setProcesserAndReceiverToView(Task task) {

        final List<NewUser> recivers = task.getArrayData(NewUser.class);
        Processer processer = task.getData(Processer.class);
        if (processer != null && !TextUtils.isEmpty(processer.name)) {
            CommentInfo commentInfo = processer.getData(CommentInfo.class);
            transactorNameView.setText(Html.fromHtml(processer.name));
            UserUtils.enterUserInfo(this, processer.userid + "", processer.name, processer.photo, transactorNameView);
            if (commentInfo != null) {
                processerLayout.setVisibility(View.VISIBLE);
                if (commentInfo.timestamp == 0) {
                    processerLayout.setVisibility(View.GONE);
                } else {
                    processerLayout.setVisibility(View.VISIBLE);
                    processerNameView.setText(Html.fromHtml(processer.name));
                    processerMestampView.setText(getResources().getString(R.string.task_finish_time) + "：" + Utils.getInstance(this).dateFormat(commentInfo.timestamp));

                    GenericDraweeHierarchy hierarchy = processerPhotoView.getHierarchy();
                    hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
                    hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
                    processerPhotoView.setImageURI(processer.photo);
                    UserUtils.enterUserInfo(this, processer.userid + "", processer.name, processer.photo, processerPhotoView);
                    processerContentTextView.setText(EmojiFragment.getEmojiContent(this,processerContentTextView.getTextSize(), commentInfo.content));
                    if (!TextUtils.isEmpty(commentInfo.content))
                        processerContentTextView.setVisibility(View.VISIBLE);
                    else
                        processerContentTextView.setVisibility(View.GONE);

                    List<ImageInfo> images = new ArrayList<>();
                    List<AudioInfo> audios = new ArrayList<>();
                    try {
                        if (!TextUtils.isEmpty(commentInfo.getJson().getString("audio"))) {
                            audios = JsonDataFactory.getDataArray(AudioInfo.class, commentInfo.getJson().getJSONArray("audio"));
                        }
                        if (!TextUtils.isEmpty(commentInfo.getJson().getString("image"))) {
                            images = JsonDataFactory.getDataArray(ImageInfo.class, commentInfo.getJson().getJSONArray("image"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (images.size() > 0) {
                        processerImageGridView.setVisibility(View.VISIBLE);
                        ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(processerImageGridView, TaskTransactActivity.this, images);
                        processerImageGridView.setAdapter(imageGridviewAdapter);
                    } else {
                        processerImageGridView.setVisibility(View.GONE);
                    }

                    if (audios.size() > 0) {

                        processerVoiceListView.setVisibility(View.VISIBLE);
                        AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
                        audioListAdapter.dataSource.clear();
                        audioListAdapter.dataSource.addAll(audios);
                        audioListAdapter.notifyDataSetChanged();
                        processerVoiceListView.setAdapter(audioListAdapter);

                    } else {
                        processerVoiceListView.setVisibility(View.GONE);
                    }
                }
            } else {
                processerLayout.setVisibility(View.GONE);
            }

        } else {
            transactorNameView.setTextColor(getResources().getColor(R.color.comment_grey));
            processerLayout.setVisibility(View.GONE);
        }
        if (recivers.isEmpty()) {
            reciverNamesView.setText(getResources().getString(R.string.no_time));
        } else {
            reciverNamesView.setText(VgTextUtils.generaReceiver(this, recivers));
            reciverNamesView.setMovementMethod(new LinkTouchMovementMethod());
            reciverNamesView.setFocusable(false);
            reciverNamesView.setClickable(false);
            reciverNamesView.setLongClickable(false);
            findViewById(R.id.btn_reciver_user).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String json = new Gson().toJson(recivers);
                    Intent intent = new Intent(TaskTransactActivity.this, ReciverUserActivity.class);
                    intent.putExtra("json", json);
                    startActivity(intent);
                }
            });
        }
    }

    private void addCommentFragment() {
        if (!mInit) {
            initFragmentPager(mViewPager, mScrollLayout);
        }
    }

    private CommentListFragment mCommentFragment;
    private PraiseListFragment mPriseFragment;
    private boolean mInit;

    public void initFragmentPager(ViewPager viewPager, final ScrollableLayout mScrollLayout) {
        mInit = true;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    if (mPriseFragment.getScrollableView().getFirstVisiblePosition() < 2) ;
                    mCommentFragment.getScrollableView().setSelection(0);
                    mScrollLayout.getHelper().setCurrentScrollableContainer(mCommentFragment);
                } else if (position == 1) {
                    if (mCommentFragment.getScrollableView().getFirstVisiblePosition() < 2) ;
                    mPriseFragment.getScrollableView().setSelection(0);
                    mScrollLayout.getHelper().setCurrentScrollableContainer(mPriseFragment);
                }
                mTitleIndicator.onScrolled((mViewPager.getWidth() + mViewPager.getPageMargin()) * position + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                mTitleIndicator.onSwitched(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        final ArrayList<Fragment> fragmentList = new ArrayList<>();
        mCommentFragment = CommentListFragment.create(PublishUtils.COMMENTTYPE_TASK, task.taskid);
        mPriseFragment = PraiseListFragment.create(PublishUtils.COMMENTTYPE_TASK, task.taskid);
        fragmentList.add(mCommentFragment);
        fragmentList.add(mPriseFragment);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mScrollLayout.getHelper().setCurrentScrollableContainer((ScrollableHelper.ScrollableContainer) fragmentList.get(0));
        viewPager.setCurrentItem(0);
        List<TabInfo> tabs = new ArrayList<>();
        tabs.add(new TabInfo(0, getString(R.string.comment) + " " + task.comments,
                CommentListFragment.class));
        tabs.add(new TabInfo(1, getString(R.string.praise) + " " + task.praises,
                PraiseListFragment.class));
        mTitleIndicator.init(0, tabs, mViewPager);
    }

    private void requestTaskInfo(String taskid, boolean ifShowLoading) {
        if (ifShowLoading) {
            loadingLayout.showLoadingView(dataInfoLayout, "", true);
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("taskid", taskid);

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_TASK_INFO), params, this);
        mNetworkManager.load(CALLBACK_TASKINFO, path, this, true);
    }

    public void cancleTask(String taskid) {
        showLoadingDialog(this, getString(R.string.prompt_info_01));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("taskid", taskid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_TASK_BACKOUT), params, this);
        mNetworkManager.load(CALLBACK_TASKCANCLE, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        dismisLoadingDialog();

        boolean safe = ActivityUtils.prehandleNetworkData(TaskTransactActivity.this, this, callbackId, path, rootData, true);
        if (!safe) {
            switch (callbackId) {
                case CALLBACK_TASKINFO:
                    if (dataInfoLayout.getVisibility() != View.VISIBLE) {
                        loadingLayout.showErrorView(dataInfoLayout, "", true, true);
                        topRightView.setVisibility(View.GONE);
                    }
                    break;
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_TASKINFO:
                loadingLayout.dismiss(dataInfoLayout);

                try {
                    JSONObject jsonObject = rootData.getJson();
                    task = JsonDataFactory.getData(Task.class, jsonObject.getJSONObject("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingLayout.showErrorView(dataInfoLayout, "", true, true);
                    topRightView.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(task.taskid) || "0".equals(task.taskid)) {
                    loadingLayout.showErrorView(dataInfoLayout, "", true, true);
                    topRightView.setVisibility(View.GONE);
                    return;
                }

                setTaskInfoToView(task);
                setProcesserAndReceiverToView(task);
                addCommentFragment();
                break;

            case CALLBACK_TASKCANCLE:
                Toast.makeText(this, getString(R.string.cancel_success_info), Toast.LENGTH_SHORT).show();
                chaneTaskReportState();
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private View lastView;

    @Override
    public View getLastView() {
        return lastView;
    }

    @Override
    public void setLastView(View view) {
        lastView = view;
    }

    public void chanePraiseNum(boolean digType) {

        backRefresh = true;
        int num = task.praises;
        if (digType) {
            task.praises = num - 1;
            praiseImageView.setImageResource(R.drawable.item_praise_click);
            praiseTextView.setTextColor(EditUtils.greyCreateColorStateList());
        } else {
            task.praises = num + 1;
            praiseImageView.setImageResource(R.drawable.item_praise_click_red);
            praiseTextView.setTextColor(EditUtils.redCreateColorStateList());
        }
        if (task.praises < 0)
            task.praises = 0;
        task.ispraise = !digType;
        try {
            task.getJson().put("praises", task.praises);
            task.getJson().put("ispraise", task.ispraise);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + task.comments);
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + task.praises);
        mPriseFragment.refresh();
    }

    public void chaneCommentNum() {
        backRefresh = true;
        int num = task.comments;
        task.comments = num + 1;
        try {
            task.getJson().put("comments", task.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + task.comments);
        mCommentFragment.refresh();
    }

    public void chaneTaskReportState() {
        backRefresh = true;
        task.repealstate = "2";
        try {
            task.getJson().put("repealstate", "2");
            setTaskInfoToView(task);
            setProcesserAndReceiverToView(task);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_TASK:
                    case PublishTask.PUBLISH_TASK_CONDUCT:
                    case PublishTask.PUBLISH_RECRUIT_FINISH:
                        backRefresh = true;
                        requestTaskInfo(taskID, false);
                        break;

                    case PublishTask.PUBLISH_COMMENT:
                        int commentType = intent.getIntExtra("commentType", -1);
                        if (commentType == PublishUtils.COMMENTTYPE_TASK) {
                            chaneCommentNum();
                        }
                        break;
                }
            } else if (RECEIVER_PUSH.equals(action)) {

                String infoType = intent.getStringExtra("infotype");
                String infoid = intent.getStringExtra("infoid");
                if ("11".equals(infoType) && taskID.equals(infoid)) {
                    backRefresh = true;
                    requestTaskInfo(taskID, false);
                }
            } else if (RECEIVER_ERROR.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_TASK:
                    case PublishTask.PUBLISH_TASK_CONDUCT:
                        String msg = intent.getStringExtra("rootDataMsg");
                        if (!TextUtil.isEmpty(msg)) {
                            backRefresh = true;
                            requestTaskInfo(taskID, false);
                            Toast.makeText(TaskTransactActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onBackPressed() {

        if (fromeNotice) {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            intent.putExtra("backRefresh", backRefresh);
            setResult(RESULT_OK, intent);
        } else {
            if (backRefresh) {
                Intent intent = new Intent();
                intent.putExtra("position", position);
                intent.putExtra("json", task.getJson().toString());
                setResult(RESULT_OK, intent);
                backRefresh = false;
            }
        }
        finish();
    }

    @Override
    public void finish() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        super.finish();
    }

    @Override
    public void commentsCount(int count) {
        task.comments = count;
        try {
            task.getJson().put("comments", task.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + count);
    }

    @Override
    public void praiseCount(int count) {
        task.praises = count;
        try {
            task.getJson().put("praises", task.praises);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + count);
    }
}
