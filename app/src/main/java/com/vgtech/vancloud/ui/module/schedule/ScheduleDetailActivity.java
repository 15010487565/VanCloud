package com.vgtech.vancloud.ui.module.schedule;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.ScheduleItem;
import com.vgtech.common.api.ScheduleReciver;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.TabComPraiseIndicator;
import com.vgtech.common.view.TabInfo;
import com.vgtech.common.view.TitleIndicator;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
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
import com.vgtech.vancloud.ui.view.MoreButtonPopupWindow;
import com.vgtech.vancloud.ui.view.scrollablelayoutlib.ScrollableHelper;
import com.vgtech.vancloud.ui.view.scrollablelayoutlib.ScrollableLayout;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.VgTextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日程详情
 * Created by Nick on 2015/9/6.
 */
public class ScheduleDetailActivity extends BaseActivity implements View.OnClickListener, HttpListener<String>, ViewListener,
        MoreButtonPopupWindow.CancelSchedule, MoreButtonPopupWindow.EditSchedule, CountListener {

    private final int GET_SCHEDULE_DETAIL = 1;
    private final int GET_SCHEDULE_CANCEL = 2;
    private SimpleDraweeView userPhotoView;
    private ImageView cancelView;
    private TextView userNameView;
    private TextView timestampView;
    private TextView reciverCountView;
    private TextView contentText;
    private TextView leftTimeText;
    private TextView tv_schedule_adrname;
    private TextView rightTimeText;
    private TextView tv_schedule_duration;
    private NoScrollGridview processerImageGridView;
    private NoScrollListview voiceListview;
    private LinearLayout everyReplys;

    private NetworkManager mNetworkManager;
    private ScheduleItem scheduleItem;
    private String scheduleId;

    private List<ScheduleReciver> recivers;
    private List<NewUser> reciverCCusers;
    private MoreButtonPopupWindow menuWindow;
    private int position;

    private boolean isIncludeMe;
    private ImageView praiseIcon;
    private TextView praiseText;
    private boolean isShowCommment;
    boolean backRefresh = false;

    private VancloudLoadingLayout loadingView;
    private LinearLayout dataInfoLayout;
    private boolean fromeNotice;
    private ScrollableLayout mScrollLayout;
    private ViewPager mViewPager;
    private TabComPraiseIndicator mTitleIndicator;
    private boolean init;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_schedule_detail));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        intentFilter.addAction(RECEIVER_ERROR);
        registerReceiver(mReceiver, intentFilter);

        Intent intent = getIntent();
        position = getIntent().getExtras().getInt("position");
        String json = getIntent().getExtras().getString("data");
        isShowCommment = intent.getBooleanExtra("showcomment", false);
        fromeNotice = intent.getBooleanExtra("fromeNotice", false);
        scheduleId = getIntent().getExtras().getString("scheduleId");
        ViewStub viewStub = (ViewStub) findViewById(R.id.action_schedule);
        viewStub.inflate();
        ViewStub detail_schedule = (ViewStub) findViewById(R.id.detail_schedule);
        detail_schedule.inflate();
        initView();
        init = true;
        if (!TextUtils.isEmpty(json)) {
            try {
                scheduleItem = JsonDataFactory.getData(ScheduleItem.class, new JSONObject(json));
                initData();
                addCommentFragment();
                init = false;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
        }
        loadScheduleInfo(init);
    }

    private void addCommentFragment() {
        if (!mInit) {
            initFragmentPager(mViewPager, mScrollLayout);
        }
    }

    private CommentListFragment mCommentFragment;
    private PraiseListFragment mPriseFragment;
    private boolean mInit;

    public void initFragmentPager(final ViewPager viewPager, final ScrollableLayout mScrollLayout) {
        mInit = true;
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == 0) {
                            if (mPriseFragment.getScrollableView().getFirstVisiblePosition() < 2) ;
                            mCommentFragment.getScrollableView().setSelection(0);
                            mScrollLayout.getHelper().setCurrentScrollableContainer(mCommentFragment);
                        } else if (position == 1) {
                            if (mCommentFragment.getScrollableView().getFirstVisiblePosition() < 2)
                                ;
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
            }
        }, 1000);
        final ArrayList<Fragment> fragmentList = new ArrayList<>();
        mCommentFragment = CommentListFragment.create(PublishUtils.COMMENTTYPE_SCHEDULE, scheduleItem.scheduleid);
        mPriseFragment = PraiseListFragment.create(PublishUtils.COMMENTTYPE_SCHEDULE, scheduleItem.scheduleid);
        fragmentList.add(mCommentFragment);
        fragmentList.add(mPriseFragment);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mScrollLayout.getHelper().setCurrentScrollableContainer((ScrollableHelper.ScrollableContainer) fragmentList.get(0));
        viewPager.setCurrentItem(0);
        List<TabInfo> tabs = new ArrayList<>();
        tabs.add(new TabInfo(0, getString(R.string.comment) + " " + scheduleItem.comments,
                CommentListFragment.class));
        tabs.add(new TabInfo(1, getString(R.string.praise) + " " + scheduleItem.praises,
                PraiseListFragment.class));
        mTitleIndicator.init(0, tabs, mViewPager);
    }

    TextView reciverNamesView;

    private void initView() {
        mTitleIndicator = (TabComPraiseIndicator) findViewById(R.id.title_indicator);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scrollableLayout);
        mScrollLayout.setHeaderIndex(0);
        mScrollLayout.scrollToBar(isShowCommment);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        userPhotoView = (SimpleDraweeView) findViewById(R.id.user_photo);
        cancelView = (ImageView) findViewById(R.id.cancel_view);
        if (PrfUtils.isChineseForAppLanguage(this))
            cancelView.setImageResource(R.mipmap.cancel_logo_ch);
        else
            cancelView.setImageResource(R.mipmap.cancel_logo_en);

        userNameView = (TextView) findViewById(R.id.user_name);
        timestampView = (TextView) findViewById(R.id.timestamp);

        reciverCountView = (TextView) findViewById(R.id.reciver_count);
        processerImageGridView = (NoScrollGridview) findViewById(R.id.imagegridview);
        voiceListview = (NoScrollListview) findViewById(R.id.voice_listview);
        reciverNamesView = (TextView) findViewById(R.id.reciver_names);
        contentText = (TextView) findViewById(R.id.content_text);
        tv_schedule_adrname = (TextView) findViewById(R.id.tv_schedule_adrname);
        contentText.setTextIsSelectable(true);
        leftTimeText = (TextView) findViewById(R.id.left_time_text);
        rightTimeText = (TextView) findViewById(R.id.right_time_text);
        tv_schedule_duration = (TextView) findViewById(R.id.tv_schedule_duration);
        everyReplys = (LinearLayout) findViewById(R.id.every_replys);

        loadingView = (VancloudLoadingLayout) findViewById(R.id.load_view);
        dataInfoLayout = (LinearLayout) findViewById(R.id.info);

        loadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadScheduleInfo(true);
            }
        });
    }

    private void initData() {
        NewUser user = null;
        if (scheduleItem != null) {

            user = scheduleItem.getData(NewUser.class);
            List<ImageInfo> imags = scheduleItem.getArrayData(ImageInfo.class);
            List<AudioInfo> audios = scheduleItem.getArrayData(AudioInfo.class);

            ImageOptions.setUserImage(userPhotoView, user.photo);
            timestampView.setText(Utils.dateFormatStr(scheduleItem.starttime) + " -- " + Utils.dateFormatStr(scheduleItem.endtime));
            contentText.setText(EmojiFragment.getEmojiContent(this, contentText.getTextSize(),Html.fromHtml(scheduleItem.getContent())));
            leftTimeText.setText(getString(R.string.schedule_create_time, Utils.getInstance(this).dateFormat(scheduleItem.timestamp)));
            rightTimeText.setText(getString(R.string.schedule_update_time, Utils.getInstance(this).dateFormat(scheduleItem.getUpdateTime())));
            rightTimeText.setVisibility(scheduleItem.getUpdateTime() == 0 ? View.GONE : View.VISIBLE);
            tv_schedule_duration.setText(Utils.getDuration(this, scheduleItem.starttime, scheduleItem.endtime));
            userNameView.setText(user.name);
            if (imags != null && imags.size() > 0) {
                processerImageGridView.setVisibility(View.VISIBLE);
                ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(processerImageGridView, this, imags);
                processerImageGridView.setAdapter(imageGridviewAdapter);
            } else {
                processerImageGridView.setVisibility(View.GONE);
            }

            if (audios != null && !audios.isEmpty()) {
                voiceListview.setVisibility(View.VISIBLE);
                AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
                audioListAdapter.dataSource.addAll(audios);
                voiceListview.setAdapter(audioListAdapter);
            } else {
                voiceListview.setVisibility(View.GONE);
            }
            String adrName = scheduleItem.getAddress();
            tv_schedule_adrname.setText(adrName);
            tv_schedule_adrname.setVisibility(TextUtils.isEmpty(adrName) ? View.GONE : View.VISIBLE);
            try {
                JSONObject detailJson = new JSONObject(scheduleItem.getJson().toString());
                recivers = JsonDataFactory.getDataArray(ScheduleReciver.class, detailJson.getJSONArray("receiver"));
                reciverCCusers = new ArrayList<>();
                if (detailJson.has("ccUser")) {
                    reciverCCusers = JsonDataFactory.getDataArray(NewUser.class, detailJson.getJSONArray("ccUser"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

//            String reciversName = "";
//            for (ScheduleReciver p : recivers)
//                reciversName += EmojiFragment.getEmojiContent(this, Html.fromHtml(p.name)) + "，";
//            reciversName = reciversName.length() > 0 ? reciversName.substring(0, reciversName.length() - 1) : "";
//            reciversName += "(" + recivers.size() + getString(R.string.schedule_reciver_peple_unit) + ")";
            if (recivers.size() == 0) {
                reciverCountView.setText(R.string.no_time);
            } else {
                reciverCountView.setText(recivers.size() + getString(R.string.schedule_reciver_peple_unit));
            }
            UserUtils.enterUserInfo(this, user.userid + "", user.name, user.photo, userPhotoView);
        } else {
            Toast.makeText(this, getString(R.string.no_schedule_detail), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final NewUser finalUser = user;
        LayoutInflater inflater = LayoutInflater.from(this);
        if (finalUser.userid.equals(PrfUtils.getUserId(this)) && "1".equals(scheduleItem.getRepealstate())) {
            initRightTv(getString(R.string.revise));
        } else
            initRightTv(getString(R.string.revise)).setVisibility(View.GONE);

        View commentButton = findViewById(R.id.comment_click);
        praiseText = (TextView) findViewById(R.id.praise_num);
        final View pariseButton = findViewById(R.id.praise_click);
        final View moreButton = findViewById(R.id.more_click);

        commentButton.getLayoutParams().width = Utils.getWindowWidth(this, 3);
        pariseButton.getLayoutParams().width = Utils.getWindowWidth(this, 3);
        moreButton.getLayoutParams().width = Utils.getWindowWidth(this, 3);

        praiseIcon = (ImageView) findViewById(R.id.schedule_list_item_praise_icon);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //回复
                if (!"2".equals(scheduleItem.getRepealstate())) {
                    PublishUtils.addComment(ScheduleDetailActivity.this, PublishUtils.COMMENTTYPE_SCHEDULE, scheduleItem.scheduleid + "");
                }
            }
        });

        if (scheduleItem.ispraise) {
            praiseIcon.setImageResource(R.drawable.item_praise_click_red);
            praiseText.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            praiseIcon.setImageResource(R.drawable.item_praise_click);
            praiseText.setTextColor(EditUtils.greyCreateColorStateList());
        }

        pariseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"2".equals(scheduleItem.getRepealstate())) {
                    PublishUtils.toDig(ScheduleDetailActivity.this, scheduleItem.scheduleid + "", PublishUtils.COMMENTTYPE_SCHEDULE, scheduleItem.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(digType);
                        }
                    });
                }
            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("2".equals(scheduleItem.replyflag) || "3".equals(scheduleItem.replyflag)) {
                    showToast(getResources().getString(R.string.tip_schedule_sure));
                    return;
                }
                if (!finalUser.userid.equals(PrfUtils.getUserId(getApplicationContext())))
                    menuWindow = new MoreButtonPopupWindow(ScheduleDetailActivity.this, ScheduleDetailActivity.this, ScheduleDetailActivity.this, scheduleItem, MoreButtonPopupWindow.OTHERS_SCHEDULE_DEEP);
                else
                    menuWindow = new MoreButtonPopupWindow(ScheduleDetailActivity.this, ScheduleDetailActivity.this, ScheduleDetailActivity.this, scheduleItem, MoreButtonPopupWindow.MY_SCHEDULE_DEEP);
                //显示窗口
                menuWindow.show();
            }
        });
//        reciverCCusers = new ArrayList<>();//抄送人
        if (reciverCCusers.isEmpty()) {
            reciverNamesView.setText(getResources().getString(R.string.no_time));
        } else {
            reciverNamesView.setText(VgTextUtils.generaReceiver(this, reciverCCusers));
            reciverNamesView.setMovementMethod(new LinkTouchMovementMethod());
            reciverNamesView.setFocusable(false);
            reciverNamesView.setClickable(false);
            reciverNamesView.setLongClickable(false);
            findViewById(R.id.btn_reciver_user).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String json = new Gson().toJson(reciverCCusers);
                    Intent intent = new Intent(ScheduleDetailActivity.this, ReciverUserActivity.class);
                    intent.putExtra("json", json);
                    startActivity(intent);
                }
            });
        }

        if (recivers != null) {
            everyReplys.removeAllViews();
            for (int i = 0; i < recivers.size(); i++) {
                ScheduleReciver sr = recivers.get(i);
                if (!isIncludeMe) {
                    if (sr.userid.equals(PrfUtils.getUserId(this)))
                        isIncludeMe = true;
                }
                View contentItem = inflater.inflate(R.layout.schedule_detail_join_everyone_reply_item, null);
                if (i == recivers.size() - 1)
                    contentItem.findViewById(R.id.join_spit_line).setVisibility(View.GONE);
                SimpleDraweeView userHead = (SimpleDraweeView) contentItem.findViewById(R.id.user_photo);
                TextView userName = (TextView) contentItem.findViewById(R.id.user_name);
                ImageView operation = (ImageView) contentItem.findViewById(R.id.operation);
                TextView operation_tv = (TextView) contentItem.findViewById(R.id.operation_tv);
                TextView timestamp = (TextView) contentItem.findViewById(R.id.timestamp);
                TextView scheduleDeepContent = (TextView) contentItem.findViewById(R.id.schedule_deep_content);

                ImageOptions.setUserImage(userHead, sr.photo);
                UserUtils.enterUserInfo(this, sr.userid + "", sr.name, sr.photo, userHead);
                userName.setText(EmojiFragment.getEmojiContent(this, userName.getTextSize(),Html.fromHtml(sr.name)));
                timestamp.setText(Utils.getInstance(this).dateFormat(sr.timestamp));

                if (!TextUtils.isEmpty(sr.state)) {
                    if ("1".equals(sr.state)) {
                        //  待定
                        operation.setImageResource(R.mipmap.schedule_indeterminate_bg);
                        operation_tv.setText(getString(R.string.schedule_detail_watting) + "  ");
                        operation_tv.setTextColor(getResources().getColor(R.color.schedule_indeterminate_color));
                    } else if ("2".equals(sr.state)) {
                        //  谢绝
                        operation.setImageResource(R.mipmap.schedule_refuse_bg);
                        operation_tv.setText(getString(R.string.schedule_detail_refuse) + "  ");
                        operation_tv.setTextColor(getResources().getColor(R.color.schedule_refuse_color));
                    } else if ("3".equals(sr.state)) {
                        //  同意
                        operation.setImageResource(R.mipmap.schedule_agree_bg);
                        operation_tv.setText(getString(R.string.schedule_detail_agree) + "  ");
                        operation_tv.setTextColor(getResources().getColor(R.color.schedule_agree_color));
                    } else if ("4".equals(sr.state)) {
                        //  未处理（默认）
                        operation.setImageResource(R.mipmap.schedule_undispose_bg);
                        operation_tv.setText(getString(R.string.schedule_detail_not_deep));
                        operation_tv.setTextColor(getResources().getColor(R.color.schedule_undispose_color));
                    }
                }

                if (!TextUtils.isEmpty(sr.content)) {
                    scheduleDeepContent.setVisibility(View.VISIBLE);
                    scheduleDeepContent.setText(EmojiFragment.getEmojiContent(this, scheduleDeepContent.getTextSize(),Html.fromHtml(sr.content)));
                } else {
                    scheduleDeepContent.setVisibility(View.GONE);
                }
                List<ImageInfo> imagsReply = sr.getArrayData(ImageInfo.class);
                List<AudioInfo> audiosReply = sr.getArrayData(AudioInfo.class);

                NoScrollGridview processerImageGridViewReplys = (NoScrollGridview) contentItem.findViewById(R.id.imagegridview);
                NoScrollListview voiceListviewReplys = (NoScrollListview) contentItem.findViewById(R.id.voice_listview);

                if (imagsReply != null && imagsReply.size() > 0) {
                    processerImageGridViewReplys.setVisibility(View.VISIBLE);
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(processerImageGridViewReplys, this, imagsReply);
                    processerImageGridViewReplys.setAdapter(imageGridviewAdapter);
                } else {
                    processerImageGridViewReplys.setVisibility(View.GONE);
                }

                if (audiosReply != null && !audiosReply.isEmpty()) {
                    voiceListviewReplys.setVisibility(View.VISIBLE);
                    AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
                    audioListAdapter.dataSource.addAll(audiosReply);
                    voiceListviewReplys.setAdapter(audioListAdapter);
                } else {
                    voiceListviewReplys.setVisibility(View.GONE);
                }
                everyReplys.addView(contentItem);
            }
        }

        if ("2".equals(scheduleItem.getRepealstate())) {
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ("2".equals(scheduleItem.getRepealstate())) {
                        showToast(R.string.schedule_cancel_prompt);
                    }
                }
            });

            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ("2".equals(scheduleItem.getRepealstate())) {
                        showToast(R.string.schedule_cancel_prompt);
                    }
                }
            });

            pariseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ("2".equals(scheduleItem.getRepealstate())) {
                        showToast(R.string.schedule_cancel_prompt);
                    }
                }
            });
        } else {
            if ("3".equals(scheduleItem.type)) {
                moreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showToast(R.string.schedule_underling_prompt);
                    }
                });
            }
        }

        if ("2".equals(scheduleItem.getRepealstate())) {
            cancelView.setVisibility(View.VISIBLE);
        } else if ("2".equals(scheduleItem.signs)) {
            if (PrfUtils.isChineseForAppLanguage(this))
                cancelView.setImageResource(R.mipmap.expired_logo_ch);
            else
                cancelView.setImageResource(R.mipmap.expired_logo_en);
            cancelView.setVisibility(View.VISIBLE);
        } else
            cancelView.setVisibility(View.GONE);
    }

    @Override
    protected int getContentView() {
        return R.layout.app_detail;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE_UPDATE);
                intent.putExtra("scheduleInfo", scheduleItem.getJson().toString());
                startActivity(intent);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    //加载日程详情网络请求
    private void loadScheduleInfo(boolean init) {
        if (init) {
            loadingView.showLoadingView(dataInfoLayout, "", true);
        }
        mNetworkManager = this.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        if (scheduleItem != null)
            params.put("calendarid", scheduleItem.scheduleid + "");
        else
            params.put("calendarid", scheduleId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SCHEDULE_DETAIL), params, this);
        mNetworkManager.load(GET_SCHEDULE_DETAIL, path, this);
    }

    //取消日程网络请求
    private void cancelSchedule() {
        mNetworkManager = this.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        if (scheduleItem != null)
            params.put("calendarid", scheduleItem.scheduleid + "");
        else
            params.put("calendarid", scheduleId);

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SCHEDULE_CANCEL), params, this);
        mNetworkManager.load(GET_SCHEDULE_CANCEL, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
//        loadingView.dismiss(dataInfoLayout);
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            switch (callbackId) {
                case GET_SCHEDULE_DETAIL:
                    if (dataInfoLayout.getVisibility() != View.VISIBLE) {
                        loadingView.showErrorView(dataInfoLayout);
                    }
                    break;
            }
            return;
        }
        switch (callbackId) {
            case GET_SCHEDULE_DETAIL:
                loadingView.setVisibility(View.GONE);
                dataInfoLayout.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    scheduleItem = JsonDataFactory.getData(ScheduleItem.class, resutObject);
                    initData();
                    addCommentFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingView.showErrorView(dataInfoLayout);
                    return;
                }
                break;
            case GET_SCHEDULE_CANCEL:
                try {
                    if (rootData.result) {
                        Toast.makeText(this, this.getString(R.string.this_schedule_is_cancel), Toast.LENGTH_SHORT).show();
                        type = -2;
                        backRefresh = true;
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    @Override
    public View getLastView() {
        return mView;
    }

    @Override
    public void setLastView(View view) {
        mView = view;

    }

    public void chanePraiseNum(boolean digType) {
        int num = scheduleItem.praises;
        if (digType) {
            scheduleItem.praises = num - 1;
            praiseIcon.setImageResource(R.drawable.item_praise_click);
            praiseText.setTextColor(EditUtils.greyCreateColorStateList());
        } else {
            scheduleItem.praises = num + 1;
            praiseIcon.setImageResource(R.mipmap.item_is_praise);
            praiseText.setTextColor(EditUtils.redCreateColorStateList());
        }
        if (scheduleItem.praises < 0) {
            scheduleItem.praises = 0;
        }
        scheduleItem.ispraise = !digType;
        try {
            scheduleItem.getJson().put("praises", scheduleItem.praises);
            scheduleItem.getJson().put("ispraise", scheduleItem.ispraise);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        backRefresh = true;
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + scheduleItem.comments);
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + scheduleItem.praises);
        mPriseFragment.refresh();
    }

    private int clickType = -1;
    private int type = -1;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_SCHEDULE:
                        refreshContext();
                        backRefresh = true;
                        break;
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT:
                        type = clickType;
                        refreshContext();
                        backRefresh = true;
                        break;
                    case PublishTask.PUBLISH_COMMENT:
                        refreshContext();
                        backRefresh = true;
                        mCommentFragment.refresh();
                        break;
                }
            } else if (RECEIVER_ERROR.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_SCHEDULE:
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT:
                        String msg = intent.getStringExtra("rootDataMsg");
                        if (!TextUtil.isEmpty(msg)) {
                            refreshContext();
                            Toast.makeText(ScheduleDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void finish() {
        setResult();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.finish();
    }

    private void setResult() {

        if (fromeNotice) {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            intent.putExtra("backRefresh", backRefresh);
            setResult(RESULT_OK, intent);
        } else {
            if (backRefresh) {
                Intent data = new Intent();
                data.putExtra("position", position);
                data.putExtra("type", type);
                data.putExtra("json", scheduleItem.getJson().toString());
                setResult(Activity.RESULT_OK, data);
                backRefresh = false;
            }
        }
    }

    private void refreshContext() {
        loadScheduleInfo(false);
    }

    @Override
    public void cancelScheule(ScheduleItem schedule) {
        new AlertDialog(this).builder().setTitle(getString(R.string.frends_tip))
                .setMsg(getString(R.string.cancle_schedule))
                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelSchedule();
                    }
                }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    @Override
    public void editSchedule(ScheduleItem schedule) {
        Intent intent = new Intent(this, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE_UPDATE);
        intent.putExtra("scheduleInfo", scheduleItem.getJson().toString());
        startActivity(intent);
    }

    @Override
    public void commentsCount(int count) {
        scheduleItem.comments = count;
        try {
            scheduleItem.getJson().put("comments", scheduleItem.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + count);
    }

    @Override
    public void praiseCount(int count) {
        scheduleItem.praises = count;
        try {
            scheduleItem.getJson().put("praises", scheduleItem.praises);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + count);
    }
}
