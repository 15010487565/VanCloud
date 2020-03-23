package com.vgtech.vancloud.ui.module.workreport;

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
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.CommentInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.Processer;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.WorkReport;
import com.vgtech.common.config.ImageOptions;
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
 * Created by Duke on 2015/9/11.
 */
public class WorkReportTransactActivity extends BaseActivity implements HttpListener<String>, ViewListener, CountListener {

    private TextView mRightTv;
    public static String JSON = "json";
    public static String WORKREPORTID = "workreportid";
    private String workReportID;
    private String workReportJson;
    private NetworkManager mNetworkManager;
    public static final int CALLBACK_WORKREPORTINFO = 1;
    private WorkReport workReport;

    private SimpleDraweeView userPhotoView;
    private TextView userNameView;
    private TextView timestampView;
    private TextView contentTextView;
    private TextView workReportTitleView;
    private TextView transactorNameView;
    private TextView reciverNamesView;
    private TextView workReportTypeView;
    //    private TextView reciverNumView;
    private TextView workreportTypeTopView;
    private LinearLayout processerLayout;
    private SimpleDraweeView processerPhotoView;
    private TextView processerNameView;
    private TextView processerMestampView;
    private TextView processerContentTextView;
    NoScrollGridview processerImageGridView;
    NoScrollListview processerVoiceListView;

    private NoScrollGridview imageGridView;
    private NoScrollListview voiceListview;

    private RelativeLayout commentClickView;
    private RelativeLayout praiseCLickView;
    private RelativeLayout cancelClickView;


    private ImageView praiseImageView;
    private TextView praiseTextView;
    ImageView finishLogoView;
    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";
    public static final String RECEIVER_PUSH = "RECEIVER_PUSH";

    private int position;
    boolean backRefresh = false;

    private ImageView commentImageView;
    private TextView commentTextView;

    private boolean isShowCommment;

    private VancloudLoadingLayout loadingView;
    private LinearLayout dataInfoLayout;

    private boolean fromeNotice;
    private ScrollableLayout mScrollLayout;
    private ViewPager mViewPager;
    private TabComPraiseIndicator mTitleIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkManager = getAppliction().getNetworkManager();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_DRAFT);
        intentFilter.addAction(RECEIVER_PUSH);
        intentFilter.addAction(RECEIVER_ERROR);
        registerReceiver(mReceiver, intentFilter);

        setTitle(getString(R.string.work_report_transact));

        Intent intent = getIntent();
        workReportID = intent.getStringExtra(WORKREPORTID);
        workReportJson = intent.getStringExtra(JSON);
        position = intent.getIntExtra("position", -1);
        isShowCommment = intent.getBooleanExtra("showcomment", false);
        fromeNotice = intent.getBooleanExtra("fromeNotice", false);
        initViews();
        if (!TextUtils.isEmpty(workReportJson)) {
            try {
                workReport = JsonDataFactory.getData(WorkReport.class, new JSONObject(workReportJson));
                setViewData(workReport);
                addCommentFragment();
                initWorkReport(workReportID, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            initWorkReport(workReportID, true);
        }
    }


    @Override
    protected int getContentView() {
        return R.layout.app_detail;
    }

    private void initViews() {
        mTitleIndicator = (TabComPraiseIndicator) findViewById(R.id.title_indicator);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scrollableLayout);
        mScrollLayout.setHeaderIndex(3);
        mScrollLayout.scrollToBar(isShowCommment);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mRightTv = initRightTv(getString(R.string.right_comment));
        ViewStub viewStub = (ViewStub) findViewById(R.id.action_workreport);
        viewStub.inflate();
        ViewStub detail_workreport = (ViewStub) findViewById(R.id.detail_workreport);
        detail_workreport.inflate();
        findViewById(R.id.time_layout).setVisibility(View.GONE);
        workreportTypeTopView = (TextView) findViewById(R.id.workreport_type_top);
        workReportTitleView = (TextView) findViewById(R.id.work_report_title);
        workreportTypeTopView.setVisibility(View.VISIBLE);

        userPhotoView = (SimpleDraweeView) findViewById(R.id.user_photo);
        userNameView = (TextView) findViewById(R.id.user_name);
        timestampView = (TextView) findViewById(R.id.timestamp);
        workReportTitleView = (TextView) findViewById(R.id.work_report_title);
        contentTextView = (TextView) findViewById(R.id.content_text);
        contentTextView.setTextIsSelectable(true);
        workReportTypeView = (TextView) findViewById(R.id.work_report_type);
        transactorNameView = (TextView) findViewById(R.id.transactor_name);
        reciverNamesView = (TextView) findViewById(R.id.reciver_names);
//        reciverNumView = (TextView) findViewById(R.id.reciver_num);
        processerLayout = (LinearLayout) findViewById(R.id.processer_layout);
        processerPhotoView = (SimpleDraweeView) findViewById(R.id.processer_photo);
        processerNameView = (TextView) findViewById(R.id.processer_name);
        processerMestampView = (TextView) findViewById(R.id.processer_mestamp);
        processerContentTextView = (TextView) findViewById(R.id.processer_content_text);

        imageGridView = (NoScrollGridview) findViewById(R.id.imagegridview);
        voiceListview = (NoScrollListview) findViewById(R.id.voice_listview);
        commentClickView = (RelativeLayout) findViewById(R.id.comment_click);
        praiseCLickView = (RelativeLayout) findViewById(R.id.praise_click);
        cancelClickView = (RelativeLayout) findViewById(R.id.cancel_click);


        finishLogoView = (ImageView) findViewById(R.id.finish_logo);
        processerImageGridView = (NoScrollGridview) findViewById(R.id.processer_imagegridview);
        processerVoiceListView = (NoScrollListview) findViewById(R.id.processer_voice_listview);
        praiseImageView = (ImageView) findViewById(R.id.img02);
        praiseTextView = (TextView) findViewById(R.id.praisetext);

        loadingView = (VancloudLoadingLayout) findViewById(R.id.load_view);
        dataInfoLayout = (LinearLayout) findViewById(R.id.info);

        commentImageView = (ImageView) findViewById(R.id.img01);
        commentTextView = (TextView) findViewById(R.id.commenttext);


        loadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initWorkReport(workReportID, true);
            }
        });
    }

    private void setViewData(WorkReport workReport) {
        commentClickView.setOnClickListener(this);
        praiseCLickView.setOnClickListener(this);
        cancelClickView.setOnClickListener(this);
        NewUser user = workReport.getData(NewUser.class);
        Processer leader = workReport.getData(Processer.class);
        final List<NewUser> receiver = workReport.getArrayData(NewUser.class);
        CommentInfo commentInfo = leader.getData(CommentInfo.class);

        userNameView.setText(Html.fromHtml(user.name));
        ImageOptions.setUserImage(userPhotoView, user.photo);
        UserUtils.enterUserInfo(this, user.userid + "", user.name, user.photo, userPhotoView);
//        timestampView.setDettailText(getResources().getString(R.string.create) + "：" + Utils.dateFormat(workReport.timestamp));
        timestampView.setText(Utils.getInstance(this).dateFormat(workReport.timestamp));

//        contentTextView.setDettailText(EmojiFragment.getEmojiContent(this, workReport.content));

        String content = Utils.formatHtmlWorkReportContent(workReport.content);

        contentTextView.setText(EmojiFragment.getEmojiContent(this, contentTextView.getTextSize(), Html.fromHtml(content)));
        workReportTitleView.setText(workReport.title);

        workreportTypeTopView.setText(getString(R.string.work_report_type));

        if (workReport.ispraise) {
            praiseImageView.setImageResource(R.drawable.item_praise_click_red);
            praiseTextView.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            praiseImageView.setImageResource(R.drawable.item_praise_click);
            praiseTextView.setTextColor(EditUtils.greyCreateColorStateList());
        }

        if ("2".equals(workReport.repealstate)) {
            if (PrfUtils.isChineseForAppLanguage(this))
                finishLogoView.setImageResource(R.mipmap.cancel_logo_ch);
            else
                finishLogoView.setImageResource(R.mipmap.cancel_logo_en);
            finishLogoView.setVisibility(View.VISIBLE);
            cancelClickView.setVisibility(View.GONE);

            praiseImageView.setSelected(true);
            praiseTextView.setSelected(true);
            commentTextView.setSelected(true);
            commentImageView.setSelected(true);
            mRightTv.setEnabled(false);
            mRightTv.setTag(false);

        } else {
            finishLogoView.setVisibility(View.GONE);
            praiseImageView.setSelected(false);
            praiseTextView.setSelected(false);
            commentTextView.setSelected(false);
            commentImageView.setSelected(false);
            mRightTv.setEnabled(true);

            if (workReport.subtype == 0) {
                mRightTv.setVisibility(View.VISIBLE);
                if ("1".equals(commentInfo.state)) {
                    mRightTv.setEnabled(true);
                    mRightTv.setTag(true);
                } else {
                    mRightTv.setEnabled(false);
                    mRightTv.setTag(false);
                    mRightTv.setVisibility(View.GONE);
                }
            } else {
                mRightTv.setVisibility(View.GONE);
            }
        }

        if ("1".equals(workReport.type)) {
            workReportTypeView.setText(getString(R.string.daily_paper));
            workreportTypeTopView.setText(workreportTypeTopView.getText().toString() + getString(R.string.daily_paper));
        } else if ("2".equals(workReport.type)) {
            workReportTypeView.setText(getString(R.string.weekly_paper));
            workreportTypeTopView.setText(workreportTypeTopView.getText().toString() + getString(R.string.weekly_paper));

        } else if ("3".equals(workReport.type)) {
            workReportTypeView.setText(getString(R.string.monthly_paper));
            workreportTypeTopView.setText(workreportTypeTopView.getText().toString() + getString(R.string.monthly_paper));
        }


        if (workReport.subtype == 1) {
            if ("1".equals(commentInfo.state) && "1".equals(workReport.repealstate)) {
                cancelClickView.setVisibility(View.VISIBLE);
            } else {
                cancelClickView.setVisibility(View.GONE);
            }
        } else {
            cancelClickView.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(leader.name)) {
            transactorNameView.setText(Html.fromHtml(leader.name));
            UserUtils.enterUserInfo(this, leader.userid + "", leader.name, leader.photo, transactorNameView);
        } else {
            transactorNameView.setText(getResources().getString(R.string.no_time));
        }
        if (receiver.isEmpty()) {
            reciverNamesView.setText(getResources().getString(R.string.no_time));
        } else {
            reciverNamesView.setText(VgTextUtils.generaReceiver(this, receiver));
            reciverNamesView.setMovementMethod(new LinkTouchMovementMethod());
            reciverNamesView.setFocusable(false);
            reciverNamesView.setClickable(false);
            reciverNamesView.setLongClickable(false);
            findViewById(R.id.btn_reciver_user).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String json = new Gson().toJson(receiver);
                    Intent intent = new Intent(WorkReportTransactActivity.this, ReciverUserActivity.class);
                    intent.putExtra("json", json);
                    startActivity(intent);
                }
            });
        }

        if (commentInfo != null) {
            if ("2".equals(commentInfo.state) && commentInfo.timestamp != 0) {
                if (PrfUtils.isChineseForAppLanguage(this))
                    finishLogoView.setImageResource(R.mipmap.comment_logo_ch);
                else
                    finishLogoView.setImageResource(R.mipmap.comment_logo_en);
                finishLogoView.setVisibility(View.VISIBLE);
                processerLayout.setVisibility(View.VISIBLE);

                ImageOptions.setUserImage(processerPhotoView, leader.photo);
                UserUtils.enterUserInfo(this, leader.userid + "", leader.name, leader.photo, processerPhotoView);
                processerNameView.setText(Html.fromHtml(leader.name));
                processerContentTextView.setText(EmojiFragment.getEmojiContent(this, processerContentTextView.getTextSize(), commentInfo.content));
                if (TextUtils.isEmpty(commentInfo.content))
                    processerContentTextView.setVisibility(View.GONE);
                else
                    processerContentTextView.setVisibility(View.VISIBLE);
//                processerMestampView.setDettailText(getString(R.string.create) + "：" + Utils.dateFormat(commentInfo.timestamp));
                processerMestampView.setText(Utils.getInstance(this).dateFormat(commentInfo.timestamp));

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
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(processerImageGridView, WorkReportTransactActivity.this, images);
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
            } else {
                processerLayout.setVisibility(View.GONE);
            }
        } else
            processerLayout.setVisibility(View.GONE);

        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        try

        {
            audios = JsonDataFactory.getDataArray(AudioInfo.class, workReport.getJson().getJSONArray("audio"));
            images = JsonDataFactory.getDataArray(ImageInfo.class, workReport.getJson().getJSONArray("image"));
        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }

        if (images.size() > 0)

        {
            imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imageGridView, WorkReportTransactActivity.this, images);
            imageGridView.setAdapter(imageGridviewAdapter);
        } else

        {
            imageGridView.setVisibility(View.GONE);
        }

        if (audios.size() > 0)

        {

            voiceListview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            voiceListview.setAdapter(audioListAdapter);

        } else

        {
            voiceListview.setVisibility(View.GONE);
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
        mCommentFragment = CommentListFragment.create(PublishUtils.COMMENTTYPE_WORKREPORT, workReport.workreportid);
        mPriseFragment = PraiseListFragment.create(PublishUtils.COMMENTTYPE_WORKREPORT, workReport.workreportid);
        fragmentList.add(mCommentFragment);
        fragmentList.add(mPriseFragment);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mScrollLayout.getHelper().setCurrentScrollableContainer((ScrollableHelper.ScrollableContainer) fragmentList.get(0));
        viewPager.setCurrentItem(0);
        List<TabInfo> tabs = new ArrayList<>();
        tabs.add(new TabInfo(0, getString(R.string.comment) + " " + workReport.comments,
                CommentListFragment.class));
        tabs.add(new TabInfo(1, getString(R.string.praise) + " " + workReport.praises,
                PraiseListFragment.class));
        mTitleIndicator.init(0, tabs, mViewPager);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.comment_click: {
                if (!"2".equals(workReport.repealstate))
                    PublishUtils.addComment(this, PublishUtils.COMMENTTYPE_WORKREPORT, workReport.workreportid);
                else
                    showToast(R.string.workreport_cancel_prompt);

            }
            break;
            case R.id.praise_click:
                if (!"2".equals(workReport.repealstate)) {
                    PublishUtils.toDig(this, workReport.workreportid, PublishUtils.COMMENTTYPE_WORKREPORT, workReport.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(digType);
                        }
                    });
                } else
                    showToast(R.string.workreport_cancel_prompt);
                break;

            case R.id.cancel_click:
                new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                        .setMsg(getString(R.string.cancel_workreport))
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancleWorkReport(workReport.workreportid);
                            }
                        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();

                break;
            case R.id.tv_right: {

                boolean canClick = (boolean) v.getTag();
                if (canClick) {
                    Intent intent = new Intent(this, NewPublishedActivity.class);
                    intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_WORK_REPORT);
                    intent.putExtra("workReportId", workReport.workreportid);
                    startActivity(intent);
                }
            }
            break;
            case R.id.btn_back:
                onBackPressed();
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    private void initWorkReport(String workReportid, boolean ifShowDialog) {
        if (ifShowDialog) {
            loadingView.showLoadingView(dataInfoLayout, "", true);
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("workreportid", workReportid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WORKREPORT_DETAIL), params, this);
        mNetworkManager.load(CALLBACK_WORKREPORTINFO, path, this, true);

    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(WorkReportTransactActivity.this, this, callbackId, path, rootData, true);
        if (!safe) {

            switch (callbackId) {
                case CALLBACK_WORKREPORTINFO:
                    if (dataInfoLayout.getVisibility() != View.VISIBLE) {
                        loadingView.showErrorView(dataInfoLayout);
                        mRightTv.setVisibility(View.GONE);
                    }
                    break;
            }

            return;
        }
        switch (callbackId) {
            case CALLBACK_WORKREPORTINFO:
                loadingView.dismiss(dataInfoLayout);
                try {
                    JSONObject jsonObject = rootData.getJson();
                    workReport = JsonDataFactory.getData(WorkReport.class, jsonObject.getJSONObject("data"));
                    setViewData(workReport);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addCommentFragment();
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
        int num = workReport.praises;
        if (digType) {
            workReport.praises = num - 1;
            praiseImageView.setImageResource(R.drawable.item_praise_click);
            praiseTextView.setTextColor(EditUtils.greyCreateColorStateList());
        } else {
            workReport.praises = num + 1;
            praiseImageView.setImageResource(R.drawable.item_praise_click_red);
            praiseTextView.setTextColor(EditUtils.redCreateColorStateList());
        }
        if (workReport.praises < 0)
            workReport.praises = 0;
        workReport.ispraise = !digType;
        try {
            workReport.getJson().put("praises", workReport.praises);
            workReport.getJson().put("ispraise", workReport.ispraise);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + workReport.praises);
        mPriseFragment.refresh();
    }


    public void cancleWorkReport(String workreportid) {
        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("workreportid", workreportid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WORKREPORT_BACKOUT), params, this);
        mNetworkManager.load(200, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(WorkReportTransactActivity.this, this, callbackId, path, rootData, true);
                if (!safe) {
                    Toast.makeText(WorkReportTransactActivity.this, getString(R.string.cancel_fail_info), Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (callbackId) {
                    case 200:
                        Toast.makeText(WorkReportTransactActivity.this, getString(R.string.cancel_success), Toast.LENGTH_SHORT).show();
                        chaneWorkReportState();
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

    public void chaneWorkReportState() {
        backRefresh = true;
        workReport.repealstate = "2";
        try {
            workReport.getJson().put("repealstate", "2");
            setViewData(workReport);
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
                    case PublishTask.PUBLISH_WORK_REPORT:
                        initWorkReport(workReportID + "", false);
                        break;

                    case PublishTask.PUBLISH_COMMENT:
                        int commentType = intent.getIntExtra("commentType", -1);
                        if (commentType == PublishUtils.COMMENTTYPE_WORKREPORT) {
                            chaneCommentNum();
                        }
                        break;
                }
            } else if (RECEIVER_PUSH.equals(action)) {

                String infoType = intent.getStringExtra("infotype");
                String infoid = intent.getStringExtra("infoid");
                if ("7".equals(infoType) && workReportID.equals(infoid)) {
                    backRefresh = true;
                    initWorkReport(workReportID + "", false);
                }
            } else if (RECEIVER_ERROR.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_WORK_REPORT:
                        String msg = intent.getStringExtra("rootDataMsg");
                        if (!TextUtil.isEmpty(msg)) {
                            initWorkReport(workReportID + "", false);
                            Toast.makeText(WorkReportTransactActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    public void chaneCommentNum() {

        backRefresh = true;
        int num = workReport.comments;
        workReport.comments = num + 1;
        try {
            workReport.getJson().put("comments", workReport.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + workReport.comments);
        mCommentFragment.refresh();
    }

    @Override
    public void commentsCount(int count) {
        workReport.comments = count;
        try {
            workReport.getJson().put("comments", workReport.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + count);
    }

    @Override
    public void praiseCount(int count) {
        workReport.praises = count;
        try {
            workReport.getJson().put("praises", workReport.praises);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + count);
    }

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
                intent.putExtra("json", workReport.getJson().toString());
                setResult(RESULT_OK, intent);
                backRefresh = false;
            }
        }
        finish();
    }
}
