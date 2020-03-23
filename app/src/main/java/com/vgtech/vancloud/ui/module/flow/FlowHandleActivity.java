package com.vgtech.vancloud.ui.module.flow;

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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.CommentInfo;
import com.vgtech.common.api.Flow;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.Processer;
import com.vgtech.common.api.ResourceInfo;
import com.vgtech.common.api.RootData;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by frances on 2015/9/11.
 */
public class FlowHandleActivity extends BaseActivity implements HttpListener<String>, ViewListener, CountListener {
    private Flow flow;
    public static String JSON = "json";
    public static String FLOWID = "processid";
    public static String TYPER = "typer";
    public static String POSITION = "position";
    private String flowID;
    private String flowJson;
    private int type;
    private NetworkManager mNetworkManager;
    public static final int CALLBACK_FLOWINFO = 1;
    public static final int CALLBACK_FLOWCANCLE = 2;
    private SimpleDraweeView userPhotoView;
    private TextView userNameView;
    private TextView timestampView;
    private TextView contentTextView;
    private TextView transactorNameView;
    private TextView reciverNamesView;

    private View processerLayout;
    private SimpleDraweeView processerPhotoView;
    private TextView processerNameView;
    private TextView processerMestampView;
    private TextView processerContentTextView;
    private NoScrollGridview processerImageGridView;
    private NoScrollListview processerVoiceListView;

    NoScrollGridview imageGridView;
    NoScrollListview voiceListview;

    private View agreeclick;
    private View disagreeclick;
    private View revocationclick;

    private View commentClickView;
    private View praiseCLickView;

    private TextView operationTv;
    private ImageView operationTag;
    private ImageView praiseImageView;
    private TextView praiseTextView;
    private int position;

    boolean backRefresh = false;

    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";
    public static final String RECEIVER_PUSH = "RECEIVER_PUSH";

    ImageView finishLogoView;

    ImageView commentImageView;
    TextView commentTextView;
    private boolean isShowCommment;

    private VancloudLoadingLayout loadingView;
    private LinearLayout dataInfoLayout;


    private ImageView arrowView;
    private View resumeInfoLayout;
    private TextView resumeCountView;
    private TextView amountView;
    private NewUser user;

    private TextView clickToDetailsView;

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

        Intent intent = getIntent();
        flowID = intent.getStringExtra(FLOWID);
        flowJson = intent.getStringExtra(JSON);
        type = intent.getIntExtra(TYPER, 2);
        position = intent.getIntExtra(POSITION, -1);
        isShowCommment = intent.getBooleanExtra("showcomment", false);
        fromeNotice = intent.getBooleanExtra("fromeNotice", false);
        initViews();
        if (!TextUtils.isEmpty(flowJson)) {
            try {
                flow = JsonDataFactory.getData(Flow.class, new JSONObject(flowJson));
                setViewData(flow);
                addCommentFragment();
                initFlow(flowID, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            initFlow(flowID, true);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.app_detail;
    }

    private void initViews() {

        setTitle(getString(R.string.flow_handle));
        ViewStub viewStub = (ViewStub) findViewById(R.id.action_flow);
        viewStub.inflate();
        ViewStub detail_flow = (ViewStub) findViewById(R.id.detail_flow);
        detail_flow.inflate();
        findViewById(R.id.time_layout).setVisibility(View.GONE);
        mTitleIndicator = (TabComPraiseIndicator) findViewById(R.id.title_indicator);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scrollableLayout);
        mScrollLayout.setHeaderIndex(5);
        mScrollLayout.scrollToBar(isShowCommment);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        imageGridView = (NoScrollGridview) findViewById(R.id.imagegridview);
        voiceListview = (NoScrollListview) findViewById(R.id.voice_listview);
        userPhotoView = (SimpleDraweeView) findViewById(R.id.user_photo);
        userNameView = (TextView) findViewById(R.id.user_name);
        timestampView = (TextView) findViewById(R.id.timestamp);
        contentTextView = (TextView) findViewById(R.id.content_text);
        contentTextView.setTextIsSelectable(true);
        transactorNameView = (TextView) findViewById(R.id.transactor_name);
        reciverNamesView = (TextView) findViewById(R.id.reciver_names);
        processerLayout =findViewById(R.id.processer_layout);
        processerPhotoView = (SimpleDraweeView) findViewById(R.id.processer_photo);
        processerNameView = (TextView) findViewById(R.id.processer_name);
        processerMestampView = (TextView) findViewById(R.id.processer_mestamp);
        processerContentTextView = (TextView) findViewById(R.id.processer_content_text);

        agreeclick =  findViewById(R.id.agree_click);
        disagreeclick =  findViewById(R.id.disagress_click);
        revocationclick =  findViewById(R.id.revocation_click);
        commentClickView =  findViewById(R.id.comment_click);
        praiseCLickView =  findViewById(R.id.praise_click);

        operationTv = (TextView) findViewById(R.id.operation_tv);
        operationTag = (ImageView) findViewById(R.id.operation);

        processerImageGridView = (NoScrollGridview) findViewById(R.id.processer_imagegridview);
        processerVoiceListView = (NoScrollListview) findViewById(R.id.processer_voice_listview);

        praiseImageView = (ImageView) findViewById(R.id.img02);
        praiseTextView = (TextView) findViewById(R.id.praisetext);

        commentImageView = (ImageView) findViewById(R.id.img01);
        commentTextView = (TextView) findViewById(R.id.commenttext);


        finishLogoView = (ImageView) findViewById(R.id.finish_logo);

        loadingView = (VancloudLoadingLayout) findViewById(R.id.load_view);
        dataInfoLayout = (LinearLayout) findViewById(R.id.info);

        resumeInfoLayout =  findViewById(R.id.resumeinfo);
        amountView = (TextView) findViewById(R.id.amount);
        resumeCountView = (TextView) findViewById(R.id.resume_count);

        arrowView = (ImageView) findViewById(R.id.arrow);
        findViewById(R.id.mid_click).setOnClickListener(this);
        clickToDetailsView = (TextView) findViewById(R.id.click_to_details);

        agreeclick.setOnClickListener(this);
        disagreeclick.setOnClickListener(this);
        revocationclick.setOnClickListener(this);
        commentClickView.setOnClickListener(this);
        praiseCLickView.setOnClickListener(this);


        loadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initFlow(flowID, true);
            }
        });
    }

    private void setViewData(Flow flow) {

        user = flow.getData(NewUser.class);
        Processer leader = flow.getData(Processer.class);
        final List<NewUser> receiver = flow.getArrayData(NewUser.class);
        CommentInfo commentInfo = leader.getData(CommentInfo.class);
        if (leader != null && !TextUtils.isEmpty(leader.name)) {
            transactorNameView.setTextColor(getResources().getColor(R.color.bg_title));
            transactorNameView.setText(Html.fromHtml(leader.name));
            UserUtils.enterUserInfo(this, leader.userid + "", leader.name, leader.photo, transactorNameView);
//            if (commentInfo != null) {
//                processerLayout.setVisibility(View.VISIBLE);
//                processerNameView.setDettailText(leader.name);
//                ImageLoader.getInstance().displayImage(leader.photo, processerPhotoView, ImageOptions.displayImageOptions);
//                processerContentTextView.setDettailText(EmojiFragment.getEmojiContent(this, commentInfo.content));
//                if (TextUtils.isEmpty(commentInfo.content))
//                    processerContentTextView.setVisibility(View.GONE);
//                else
//                    processerContentTextView.setVisibility(View.VISIBLE);
//            } else {
//                processerLayout.setVisibility(View.GONE);
//            }

        } else {
            transactorNameView.setTextColor(getResources().getColor(R.color.comment_grey));
            transactorNameView.setText(getResources().getString(R.string.no_time));
        }

        if (flow.ispraise) {
            praiseImageView.setImageResource(R.drawable.item_praise_click_red);
            praiseTextView.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            praiseImageView.setImageResource(R.drawable.item_praise_click);
            praiseTextView.setTextColor(EditUtils.greyCreateColorStateList());
        }

        if ("2".equals(flow.repealstate)) {
            operationTv.setVisibility(View.GONE);
            operationTag.setVisibility(View.GONE);
            if (PrfUtils.isChineseForAppLanguage(this))
                finishLogoView.setImageResource(R.mipmap.cancel_logo_ch);
            else
                finishLogoView.setImageResource(R.mipmap.cancel_logo_en);
            finishLogoView.setVisibility(View.VISIBLE);
            agreeclick.setVisibility(View.GONE);
            disagreeclick.setVisibility(View.GONE);
            revocationclick.setVisibility(View.GONE);
            commentClickView.setVisibility(View.VISIBLE);
            praiseCLickView.setVisibility(View.VISIBLE);

            praiseImageView.setSelected(true);
            praiseTextView.setSelected(true);
            commentImageView.setSelected(true);
            commentTextView.setSelected(true);
        } else {
            praiseImageView.setSelected(false);
            praiseTextView.setSelected(false);
            commentImageView.setSelected(false);
            commentTextView.setSelected(false);
            finishLogoView.setVisibility(View.GONE);
            //1同意，2不同意，3待审批，默认0全部
            operationTv.setVisibility(View.VISIBLE);
            operationTag.setVisibility(View.VISIBLE);
            if ("1".equals(flow.state)) {
                operationTag.setImageResource(R.mipmap.schedule_agree_bg);
                operationTv.setText(getResources().getString(R.string.agree) + "  ");
                operationTv.setTextColor(getResources().getColor(R.color.schedule_agree_color));

            } else if ("2".equals(flow.state)) {
                operationTag.setImageResource(R.mipmap.schedule_refuse_bg);
                operationTv.setText(getResources().getString(R.string.disagree));
                operationTv.setTextColor(getResources().getColor(R.color.schedule_refuse_color));

            } else if ("3".equals(flow.state)) {
                operationTag.setImageResource(R.mipmap.schedule_undispose_bg);
                operationTv.setText(getResources().getString(R.string.approvaling));
                operationTv.setTextColor(getResources().getColor(R.color.schedule_undispose_color));
            }

            if (type == 1 && "3".equals(flow.state)) {
                agreeclick.setVisibility(View.GONE);
                disagreeclick.setVisibility(View.GONE);
                revocationclick.setVisibility(View.VISIBLE);
                commentClickView.setVisibility(View.VISIBLE);
                praiseCLickView.setVisibility(View.VISIBLE);
            } else if (type == 2 && "3".equals(flow.state)) {
                agreeclick.setVisibility(View.VISIBLE);
                disagreeclick.setVisibility(View.VISIBLE);
                revocationclick.setVisibility(View.GONE);
                commentClickView.setVisibility(View.GONE);
                praiseCLickView.setVisibility(View.GONE);
            } else {
                agreeclick.setVisibility(View.GONE);
                disagreeclick.setVisibility(View.GONE);
                revocationclick.setVisibility(View.GONE);
                commentClickView.setVisibility(View.VISIBLE);
                praiseCLickView.setVisibility(View.VISIBLE);
            }

        }

        if (flow.resource == 1) {
            arrowView.setVisibility(View.INVISIBLE);
            clickToDetailsView.setVisibility(View.GONE);
        } else if (flow.resource == 3) {
            revocationclick.setVisibility(View.GONE);
            clickToDetailsView.setVisibility(View.VISIBLE);
            arrowView.setVisibility(View.INVISIBLE);
        } else if (flow.resource == 5) {
            revocationclick.setVisibility(View.GONE);
            clickToDetailsView.setVisibility(View.VISIBLE);
            arrowView.setVisibility(View.INVISIBLE);
        } else {
            revocationclick.setVisibility(View.GONE);
            clickToDetailsView.setVisibility(View.GONE);
            arrowView.setVisibility(View.VISIBLE);
        }

        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        try {
            audios = JsonDataFactory.getDataArray(AudioInfo.class, flow.getJson().getJSONArray("audio"));
            images = JsonDataFactory.getDataArray(ImageInfo.class, flow.getJson().getJSONArray("image"));
            if (!TextUtils.isEmpty(flow.resourceinfo))
                resourceInfos = JsonDataFactory.getDataArray(ResourceInfo.class, new JSONArray(flow.resourceinfo));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (flow.resource == 4 && resourceInfos.size() > 0) {
            resumeInfoLayout.setVisibility(View.VISIBLE);
            resumeCountView.setText(Utils.format(getString(R.string.resumes_total), resourceInfos.get(0).resume_count));
            amountView.setText(Utils.format(getString(R.string.order_total_01), resourceInfos.get(0).amount));

        } else
            resumeInfoLayout.setVisibility(View.GONE);

        if (images.size() > 0) {
            imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imageGridView, FlowHandleActivity.this, images);
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
                    Intent intent = new Intent(FlowHandleActivity.this, ReciverUserActivity.class);
                    intent.putExtra("json", json);
                    startActivity(intent);
                }
            });
        }

        userNameView.setText(Html.fromHtml(user.name));
        ImageOptions.setUserImage(userPhotoView, user.photo);
        UserUtils.enterUserInfo(this, user.userid + "", user.name, user.photo, userPhotoView);
//        timestampView.setDettailText(getResources().getString(R.string.create) + "：" + Utils.dateFormat(flow.timestamp));
        timestampView.setText(Utils.getInstance(this).dateFormat(flow.timestamp));

        String leaveinfo = Utils.formatHtmlLeaveInfoContent(this, flow.leaveinfo);
        if (!TextUtils.isEmpty(leaveinfo)) {
            contentTextView.setText(EmojiFragment.getEmojiContent(this, contentTextView.getTextSize(),Html.fromHtml(flow.content + "<br>" + leaveinfo)));
        } else {
            contentTextView.setText(EmojiFragment.getEmojiContent(this, contentTextView.getTextSize(),Html.fromHtml(flow.content)));
        }

        //点评信息
        if (commentInfo != null) {
            if (commentInfo.timestamp != 0) {
                processerLayout.setVisibility(View.VISIBLE);
                ImageOptions.setUserImage(processerPhotoView, leader.photo);
                UserUtils.enterUserInfo(this, leader.userid + "", leader.name, leader.photo, processerPhotoView);
                processerNameView.setText(Html.fromHtml(leader.name));
                processerContentTextView.setText(EmojiFragment.getEmojiContent(this, processerContentTextView.getTextSize(),commentInfo.content));
//                processerMestampView.setDettailText(getString(R.string.create) + "：" + Utils.dateFormat(commentInfo.timestamp));
                if (TextUtils.isEmpty(commentInfo.content))
                    processerContentTextView.setVisibility(View.GONE);
                else
                    processerContentTextView.setVisibility(View.VISIBLE);
                processerMestampView.setText(Utils.getInstance(this).dateFormat(commentInfo.timestamp));

                List<ImageInfo> processerimages = new ArrayList<>();
                List<AudioInfo> processeraudios = new ArrayList<>();
                try {
                    if (!TextUtils.isEmpty(commentInfo.getJson().getString("audio"))) {
                        processeraudios = JsonDataFactory.getDataArray(AudioInfo.class, commentInfo.getJson().getJSONArray("audio"));
                    }
                    if (!TextUtils.isEmpty(commentInfo.getJson().getString("image"))) {
                        processerimages = JsonDataFactory.getDataArray(ImageInfo.class, commentInfo.getJson().getJSONArray("image"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (processerimages.size() > 0) {
                    processerImageGridView.setVisibility(View.VISIBLE);
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(processerImageGridView, FlowHandleActivity.this, processerimages);
                    processerImageGridView.setAdapter(imageGridviewAdapter);
                } else {
                    processerImageGridView.setVisibility(View.GONE);
                }

                if (processeraudios.size() > 0) {

                    processerVoiceListView.setVisibility(View.VISIBLE);
                    AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
                    audioListAdapter.dataSource.clear();
                    audioListAdapter.dataSource.addAll(processeraudios);
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
    }

    private void initFlow(String processid, boolean ifshowDialog) {
        if (ifshowDialog) {
            loadingView.showLoadingView(dataInfoLayout, "", true);
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("processid", processid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_PROCESS_INFO), params, this);
        mNetworkManager.load(CALLBACK_FLOWINFO, path, this, true);

    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(FlowHandleActivity.this, this, callbackId, path, rootData, true);
        if (!safe) {

            switch (callbackId) {
                case CALLBACK_FLOWINFO:

                    if (dataInfoLayout.getVisibility() != View.VISIBLE) {
                        loadingView.showErrorView(dataInfoLayout);
                    }

                    break;
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_FLOWINFO:
                loadingView.dismiss(dataInfoLayout);
                try {
                    JSONObject jsonObject = rootData.getJson();
                    flow = JsonDataFactory.getData(Flow.class, jsonObject.getJSONObject("data"));
                    setViewData(flow);
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingView.showErrorView(dataInfoLayout);
                    return;
                }
                if (TextUtils.isEmpty(flow.processid) || "0".equals(flow.processid)) {
                    loadingView.showErrorView(dataInfoLayout);
                    return;
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

    private View mView;

    public View getLastView() {
        return mView;
    }


    public void setLastView(View view) {
        mView = view;

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
        mCommentFragment = CommentListFragment.create(PublishUtils.COMMENTTYPE_FLOW, flow.processid);
        mPriseFragment = PraiseListFragment.create(PublishUtils.COMMENTTYPE_FLOW, flow.processid);
        fragmentList.add(mCommentFragment);
        fragmentList.add(mPriseFragment);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mScrollLayout.getHelper().setCurrentScrollableContainer((ScrollableHelper.ScrollableContainer) fragmentList.get(0));
        viewPager.setCurrentItem(0);
        List<TabInfo> tabs = new ArrayList<>();
        tabs.add(new TabInfo(0, getString(R.string.comment) + " " + flow.comments,
                CommentListFragment.class));
        tabs.add(new TabInfo(1, getString(R.string.praise) + " " + flow.praises,
                PraiseListFragment.class));
        mTitleIndicator.init(0, tabs, mViewPager);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.revocation_click:
                if (flow.resource == 1) {
                    new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                            .setMsg(getString(R.string.cancel_flow))
                            .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancleFlow(flow.processid + "");
                                }
                            }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
                break;
            case R.id.agree_click: {
//                switch (flow.resource) {
//                    case 3:
//                        PublishUtils.recruitApproveAction(this, 1, flow.resourceid);
//                        break;
//                    case 4:
//                        PublishUtils.resumeApproveAction(this, 1, flow.resourceid);
//                        break;
//                    default:
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW_CONDUCT);
                intent.putExtra(Constants.TYPE, Constants.AGREE);
                intent.putExtra("flowId", "" + flow.processid);
                startActivity(intent);
//                        break;
//                }
            }
            break;
            case R.id.disagress_click: {

//                switch (flow.resource) {
//                    case 3:
//                        PublishUtils.recruitApproveAction(this, 2, flow.resourceid);
//                        break;
//                    case 4:
//                        PublishUtils.resumeApproveAction(this, 2, flow.resourceid);
//                        break;
//                    default:
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW_CONDUCT);
                intent.putExtra(Constants.TYPE, Constants.UNAGREE);
                intent.putExtra("flowId", "" + flow.processid);
                startActivity(intent);
//                        break;
//                }
            }
            break;
            case R.id.comment_click: {
                if (!"2".equals(flow.repealstate)) {
                    PublishUtils.addComment(this, PublishUtils.COMMENTTYPE_FLOW, flow.processid + "");
                }
            }
            break;
            case R.id.praise_click:
                if (!"2".equals(flow.repealstate)) {
                    PublishUtils.toDig(this, flow.processid + "", PublishUtils.COMMENTTYPE_FLOW, flow.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(digType);
                        }
                    });
                }
                break;
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.mid_click:
                break;

            default:
                super.onClick(v);
                break;
        }
    }


    public void cancleFlow(String processid) {
        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("processid", processid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_PROCESS_CANCEL), params, this);
        mNetworkManager.load(CALLBACK_FLOWCANCLE, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(FlowHandleActivity.this, this, callbackId, path, rootData, true);
                if (!safe) {
//                    Toast.makeText(FlowHandleActivity.this, "取消失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (callbackId) {
                    case CALLBACK_FLOWCANCLE:
                        Toast.makeText(FlowHandleActivity.this, getString(R.string.cancel_success), Toast.LENGTH_SHORT).show();
                        chaneFlowState();
                        //TODO
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

    public void chaneCommentNum() {

        backRefresh = true;
        int num = flow.comments;
        flow.comments = num + 1;
        try {
            flow.getJson().put("comments", flow.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + flow.comments);
        mCommentFragment.refresh();
    }

    public void chaneFlowState() {
        backRefresh = true;
        flow.repealstate = "2";
        try {
            flow.getJson().put("repealstate", "2");
            setViewData(flow);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commentsCount(int count) {
        flow.comments = count;
        try {
            flow.getJson().put("comments", flow.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + count);
    }

    @Override
    public void praiseCount(int count) {
        flow.praises = count;
        try {
            flow.getJson().put("praises", flow.praises);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + count);
    }

    public void chanePraiseNum(boolean digType) {

        backRefresh = true;
        int num = flow.praises;
        if (digType) {
            flow.praises = num - 1;
            praiseImageView.setImageResource(R.drawable.item_praise_click);
            praiseTextView.setTextColor(EditUtils.greyCreateColorStateList());
        } else {
            flow.praises = num + 1;
            praiseImageView.setImageResource(R.drawable.item_praise_click_red);
            praiseTextView.setTextColor(EditUtils.redCreateColorStateList());
        }
        if (flow.praises < 0)
            flow.praises = 0;
        flow.ispraise = !digType;
        try {
            flow.getJson().put("praises", flow.praises);
            flow.getJson().put("ispraise", !digType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + flow.praises);
        mPriseFragment.refresh();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_FLOW_CONDUCT:
                    case PublishTask.PUBLISH_RESUME_APPROVE:
                    case PublishTask.PUBLISH_RECRUIT_APPROVE:
                        initFlow(flowID, false);
                        break;
                    case PublishTask.PUBLISH_COMMENT:
                        int commentType = intent.getIntExtra("commentType", -1);
                        if (commentType == PublishUtils.COMMENTTYPE_FLOW) {
                            chaneCommentNum();
                        }
                        break;
                }
            } else if (RECEIVER_PUSH.equals(action)) {

                String infoType = intent.getStringExtra("infotype");
                String infoid = intent.getStringExtra("infoid");
                if ("2".equals(infoType) && flowID.equals(infoid)) {
                    backRefresh = true;
                    initFlow(flowID, false);
                }
            } else if (RECEIVER_ERROR.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_FLOW_CONDUCT:
                        String msg = intent.getStringExtra("rootDataMsg");
                        if (!TextUtil.isEmpty(msg)) {
                            initFlow(flowID, false);
                            Toast.makeText(FlowHandleActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                intent.putExtra("json", flow.getJson().toString());
                setResult(RESULT_OK, intent);
                backRefresh = false;
            }
        }
        super.onBackPressed();
    }

}