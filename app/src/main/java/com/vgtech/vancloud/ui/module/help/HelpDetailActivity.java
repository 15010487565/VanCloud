package com.vgtech.vancloud.ui.module.help;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.HelpListItem;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.image.ImageLoadFresco;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.TabComPraiseIndicator;
import com.vgtech.common.view.TabInfo;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.weiboapi.Constants;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
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
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.VgTextUtils;
import com.vgtech.vancloud.wxapi.WXEntryActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nick on 2015/9/10.
 */
public class HelpDetailActivity extends WXEntryActivity implements HttpListener<String>, ViewListener,
        IWeiboHandler.Response, CountListener {


    private final int GET_HELP_DETAIL = 432;
    private final int COLLECTION_THIS_HELP = 1;
    private final int DELETE_THIS_HELP = 2;
    private MoreButtonPopupWindow menuWindow;
    private SimpleDraweeView chUserHead;
    private TextView tvUserName;
    private TextView tvCreateTime;
    private TextView tvHelpContent;

    private TextView tvReplyButton;
    private TextView tvPraiseButton;
    private TextView tvMoreButton;

    private RelativeLayout reply_button_onclick;
    private RelativeLayout praiseButtonClick;
    private RelativeLayout more_button_click;

    private NetworkManager mNetworkManager;

    private ImageView praiseIcon;

    private HelpListItem data;


    private NoScrollGridview imagegridview;
    private NoScrollListview voice_listview;

    private TextView reciverNamesView;
    private LinearLayout sharedContent;

    private TextView sharedContentText;
    private NoScrollGridview sharedImagegridview;
    private NoScrollListview sharedvoiceListview;

    private int position;
    private int commentCount;
    private String id;
    private boolean isRefresh = false;

    private Bitmap bitmap;
    private boolean isShowCommment;


    private VancloudLoadingLayout loadingLayout;

    @Override
    protected int getContentView() {
        return R.layout.app_detail;
    }


    private boolean fromeNotice;
    private ScrollableLayout mScrollLayout;
    private ViewPager mViewPager;
    private LinearLayout infoLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.help_title_lable));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        registerReceiver(mReceiver, intentFilter);
        position = getIntent().getExtras().getInt("position");
        String json = getIntent().getExtras().getString("json");
        id = getIntent().getExtras().getString("id");
        isShowCommment = getIntent().getBooleanExtra("showcomment", false);
        fromeNotice = getIntent().getBooleanExtra("fromeNotice", false);
        ViewStub viewStub = (ViewStub) findViewById(R.id.action_help);
        viewStub.inflate();
        ViewStub detail_help = (ViewStub) findViewById(R.id.detail_help);
        detail_help.inflate();
        initViews();
        boolean init = true;
        if (!TextUtil.isEmpty(json)) {
            try {
                data = JsonDataFactory.getData(HelpListItem.class, new JSONObject(json));
                addCommentFragment();
                initData();
                init = false;
            } catch (Exception e) {
            }
        }
        loadHelpInfo(init);
    }

    private TabComPraiseIndicator mTitleIndicator;

    private void initViews() {
        mTitleIndicator = (TabComPraiseIndicator) findViewById(R.id.title_indicator);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scrollableLayout);
        mScrollLayout.setHeaderIndex(1);
        mScrollLayout.scrollToBar(isShowCommment);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        chUserHead = (SimpleDraweeView) findViewById(R.id.user_photo);
        tvUserName = (TextView) findViewById(R.id.user_name);
        tvCreateTime = (TextView) findViewById(R.id.timestamp);
        reciverNamesView = (TextView) findViewById(R.id.reciver_names);
        tvHelpContent = (TextView) findViewById(R.id.help_content);
        tvHelpContent.setTextIsSelectable(true);
        imagegridview = (NoScrollGridview) findViewById(R.id.imagegridview);
        voice_listview = (NoScrollListview) findViewById(R.id.voice_listview);

        tvReplyButton = (TextView) findViewById(R.id.reply_button);
        tvPraiseButton = (TextView) findViewById(R.id.praise_button);
        tvMoreButton = (TextView) findViewById(R.id.more_button);

        reply_button_onclick = (RelativeLayout) findViewById(R.id.reply_button_onclick);
        praiseButtonClick = (RelativeLayout) findViewById(R.id.praise_button_click);
        more_button_click = (RelativeLayout) findViewById(R.id.more_button_click);

        praiseIcon = (ImageView) findViewById(R.id.img02);

        sharedContent = (LinearLayout) findViewById(R.id.shared_content_layout);
        sharedContentText = (TextView) findViewById(R.id.shared_content_text);
        sharedImagegridview = (NoScrollGridview) findViewById(R.id.shared_imagegridview);
        sharedvoiceListview = (NoScrollListview) findViewById(R.id.shared_voice_listview);

        tvReplyButton.setCompoundDrawablePadding(Utils.convertDipOrPx(this, 8));
        tvPraiseButton.setCompoundDrawablePadding(Utils.convertDipOrPx(this, 8));
        tvMoreButton.setCompoundDrawablePadding(Utils.convertDipOrPx(this, 8));
        infoLayout = (LinearLayout) findViewById(R.id.info);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.load_view);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadHelpInfo(true);
            }
        });

    }

    private void initData() {
        setListener();
        NewUser user = data.getData(NewUser.class);
        tvUserName.setText(user.name);
        long time = 0;
        try {
            time = Long.parseLong(data.timestamp);
        } catch (Exception e) {
            finish();
            return;
        }

        tvCreateTime.setText(Utils.getInstance(this).dateFormat(time));
        ImageOptions.setUserImage(chUserHead, user.photo);
        UserUtils.enterUserInfo(this, user.userid, user.name, user.photo, chUserHead);

        tvHelpContent.setText(EmojiFragment.getEmojiContentWithAt(this, tvHelpContent.getTextSize(),Html.fromHtml(data.content)));


        tvReplyButton.setText(R.string.detail_reply);
        tvPraiseButton.setText(R.string.help_list_item_praise_in_detail);

        commentCount = data.comments;
        if (data.ispraise) {
            praiseIcon.setImageResource(R.drawable.item_help_dig_orange);
            tvPraiseButton.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            praiseIcon.setImageResource(R.drawable.item_help_dig);
            tvPraiseButton.setTextColor(EditUtils.greyCreateColorStateList());
        }

        praiseButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublishUtils.toDig(HelpDetailActivity.this, data.helpId + "", PublishUtils.COMMENTTYPE_HELP, data.ispraise, new PublishUtils.DigCallBack() {

                    @Override
                    public void successful(boolean digType) {
                        if (digType)
                            data.praises -= data.praises > 0 ? 1 : 0;
                        else
                            data.praises += 1;
                        data.ispraise = !digType;
                        try {
                            data.getJson().put("praises", data.praises);
                        } catch (Exception e) {
                        }
                        try {
                            data.getJson().put("ispraise", data.ispraise);
                        } catch (Exception e) {
                        }
                        initData();
                        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + data.comments);
                        mTitleIndicator.updateTitle(1, getString(R.string.help_praise) + " " + data.praises);
                        mPriseFragment.refresh();
                        isRefresh = true;
                    }
                });
            }
        });

        List<NewUser> recivers = data.getArrayData(NewUser.class);
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
                    try {
                        String json = data.getJson().getJSONArray("receiver").toString();
                        Intent intent = new Intent(HelpDetailActivity.this, ReciverUserActivity.class);
                        intent.putExtra("json", json);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        List<ImageInfo> imags = data.getArrayData(ImageInfo.class);
        List<AudioInfo> audios = data.getArrayData(AudioInfo.class);
        if (imags != null && imags.size() > 0) {
            imagegridview.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imagegridview, this, imags);
            imagegridview.setAdapter(imageGridviewAdapter);
            new ImageLoadFresco.LoadImageFrescoBuilder(getApplicationContext(), new SimpleDraweeView(this), imags.get(0).thumb)
                    .setBitmapDataSubscriber(new BaseBitmapDataSubscriber() {
                        @Override
                        protected void onNewResultImpl(Bitmap bitmap) {
                            if (bitmap == null) {
                                HelpDetailActivity.this.bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                            } else {
                                HelpDetailActivity.this.bitmap = bitmap;
                            }
                        }

                        @Override
                        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                            HelpDetailActivity.this.bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                        }
                    })
                    .build();
        } else {
            imagegridview.setVisibility(View.GONE);
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }

        if (audios != null && !audios.isEmpty()) {
            voice_listview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            voice_listview.setAdapter(audioListAdapter);
        } else {
            voice_listview.setVisibility(View.GONE);

        }

        HelpListItem helpItem = data.getData(HelpListItem.class);
        if (helpItem != null) {

            sharedContent.setVisibility(View.VISIBLE);

            String userName = "@" + helpItem.getData(NewUser.class).name + ":";
            while (helpItem.getData(HelpListItem.class) != null) {
                helpItem = helpItem.getData(HelpListItem.class);
                NewUser sharedUser = helpItem.getData(NewUser.class);
                userName += "@" + sharedUser.name + ":";
            }
            if (!helpItem.state.equals("2")) {
                sharedContentText.setText(EmojiFragment.getEmojiContentWithAt(HelpDetailActivity.this, sharedContentText.getTextSize(),userName + helpItem.content));

                final List<ImageInfo> sharedImages = helpItem.getArrayData(ImageInfo.class);
                final List<AudioInfo> sharedAudios = helpItem.getArrayData(AudioInfo.class);

                if (sharedImages != null && sharedImages.size() > 0) {
                    sharedImagegridview.setVisibility(View.VISIBLE);
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(sharedImagegridview, HelpDetailActivity.this, sharedImages);
                    sharedImagegridview.setAdapter(imageGridviewAdapter);
                } else {
                    sharedImagegridview.setVisibility(View.GONE);
                }

                if (sharedAudios != null && !sharedAudios.isEmpty()) {
                    sharedvoiceListview.setVisibility(View.VISIBLE);
                    AudioListAdapter audioListAdapter = new AudioListAdapter(HelpDetailActivity.this, this);
                    audioListAdapter.dataSource.clear();
                    audioListAdapter.dataSource.addAll(sharedAudios);
                    audioListAdapter.notifyDataSetChanged();
                    sharedvoiceListview.setAdapter(audioListAdapter);
                } else {
                    sharedvoiceListview.setVisibility(View.GONE);

                }
                final String json = helpItem.getJson().toString();
                sharedContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HelpDetailActivity.this, HelpDetailActivity.class);
                        intent.putExtra("json", json);
                        startActivity(intent);
                    }
                });
            } else {
                sharedContentText.setText(getString(R.string.raw_deleted));

                sharedvoiceListview.setVisibility(View.GONE);
                sharedImagegridview.setVisibility(View.GONE);

                sharedContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }
        } else {
            sharedContent.setVisibility(View.GONE);
        }

    }

    private void setListener() {
        reply_button_onclick.setOnClickListener(this);
        more_button_click.setOnClickListener(this);
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
        mCommentFragment = CommentListFragment.create(PublishUtils.COMMENTTYPE_HELP, data.helpId);
        mPriseFragment = PraiseListFragment.create(PublishUtils.COMMENTTYPE_HELP, data.helpId);
        fragmentList.add(mCommentFragment);
        fragmentList.add(mPriseFragment);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mScrollLayout.getHelper().setCurrentScrollableContainer((ScrollableHelper.ScrollableContainer) fragmentList.get(0));
        viewPager.setCurrentItem(0);
        List<TabInfo> tabs = new ArrayList<>();
        tabs.add(new TabInfo(0, getString(R.string.comment) + " " + data.comments,
                CommentListFragment.class));
        tabs.add(new TabInfo(1, getString(R.string.help_praise) + " " + data.praises,
                PraiseListFragment.class));
        mTitleIndicator.init(0, tabs, mViewPager);
    }

    //网络请求
    private void loadHelpInfo(boolean init) {
        if (init) {
//            showLoadingDialog(this, "");
            loadingLayout.showLoadingView(infoLayout, "", true);
        }
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("helpid", data == null ? id : data.helpId + "");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_HELP_INFO), params, this);
        mNetworkManager.load(GET_HELP_DETAIL, path, this, true);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            if (callbackId == GET_HELP_DETAIL) {
                if (infoLayout.getVisibility() != View.VISIBLE && !checkDataIsNull()) {
                    loadingLayout.showErrorView(infoLayout);
                }
            }
            return;
        }
        switch (callbackId) {
            case GET_HELP_DETAIL:
                loadingLayout.dismiss(infoLayout);
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    data = JsonDataFactory.getData(HelpListItem.class, jsonObject);
                    if (data == null) {
                        loadingLayout.showErrorView(infoLayout);
                        return;
                    }
                    if ("2".equals(data.state)) {
                        loadingLayout.showEmptyView(infoLayout, getString(R.string.raw_deleted), true, true);
                    } else {
                        initData();
                        addCommentFragment();
                    }
                } catch (Exception e) {
                    loadingLayout.showErrorView(infoLayout);
                }
                break;
            case COLLECTION_THIS_HELP:
                isRefresh = true;
                try {
                    switch (data.type) {
                        case 0:
                        case 2:
                            data.type = 1;
                            Toast.makeText(this, this.getString(R.string.shared_collection_success), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            data.type = 2;
                            Toast.makeText(this, this.getString(R.string.shared_discollection_success), Toast.LENGTH_SHORT).show();
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DELETE_THIS_HELP:
                try {
                    menuWindow.dismiss();
                    mDelete = true;
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.reply_button_onclick:
                PublishUtils.addComment(this, PublishUtils.COMMENTTYPE_HELP, data.helpId + "");
                break;
//            case R.id.praise_button_click:
//                PublishUtils.toPraise(this, data.helpId + "", PublishUtils.COMMENTTYPE_HELP, new PublishUtils.successfulToDo() {
//                    @Override
//                    public void successful() {
//                        data.praises += 1;
//                        initData();
//                        isRefresh = true;
//                    }
//                });
//                break;

            case R.id.more_button_click:
                menuWindow = new MoreButtonPopupWindow(HelpDetailActivity.this, new MoreButtonPopupWindow.SharedBottomBar() {
                    @Override
                    public void collection(String id) {
                        collectionThisShared(id);
                    }

                    @Override
                    public void deleted(String id) {
                        deleteThisHelp(id);
                    }

                    @Override
                    public void sharedToWeiBo(String id) {
                        menuWindow.dismiss();
                        shareWeibo(data.content.length() > 20 ? data.content.substring(0, 17) + "…" : data.content);
                    }

                    @Override
                    public void sharedToWeiXinSession(String id) {
                        menuWindow.dismiss();

                        HashMap<String, String> obj = new HashMap<String, String>();
                        obj.put("resId", id);
                        obj.put("megTypeId", PublishUtils.COMMENTTYPE_HELP + "");
                        shareWebPage(SendMessageToWX.Req.WXSceneSession, bitmap, ApiUtils.generatorUrl(HelpDetailActivity.this, URLAddr.SHARE_URL + "1/" + data.helpId), getString(R.string.app_name), data.content);
                    }

                    @Override
                    public void sharedToWeiXin(String id) {
                        menuWindow.dismiss();
                        shareWebPage(SendMessageToWX.Req.WXSceneTimeline, bitmap, ApiUtils.generatorUrl(HelpDetailActivity.this, URLAddr.SHARE_URL + "1/" + data.helpId), getString(R.string.app_name), data.content);
                    }

                    @Override
                    public void forwardTo(String id, String json) {
                        menuWindow.dismiss();
                        Intent intent = new Intent(HelpDetailActivity.this, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FORWARD);
                        intent.putExtra("subtypeId", 1);
                        intent.putExtra("forwardId", id);
                        intent.putExtra("json", json);
                        startActivity(intent);
                    }
                }, data.helpId, data.getJson().toString());
                //显示窗口
                menuWindow.show();

                boolean isMyShared = false;
                try {
                    NewUser user = data.getData(NewUser.class);
                    if (user.userid.equals(PrfUtils.getUserId(HelpDetailActivity.this)))
                        isMyShared = true;
                } catch (Exception e) {
                }

                menuWindow.setIsMine(isMyShared);

                if (data.type == 1)
                    menuWindow.setIsCollection(this, true);
                else
                    menuWindow.setIsCollection(this, false);
                break;
        }
    }

    //网络请求
    private void collectionThisShared(String id) {
        showLoadingDialog(this, getString(R.string.prompt_info_01));
        menuWindow.dismiss();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("helpid", id);
        if (data.type != 1)
            params.put("type", "1");
        else
            params.put("type", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_HELP_COLLECTION), params, this);
        mNetworkManager.load(COLLECTION_THIS_HELP, path, this);
    }

    private void deleteThisHelp(String id) {
        menuWindow.dismiss();
        showLoadingDialog(this, "", false);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("helpid", id);
        params.put("state", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_HELP_DELETE), params, this);
        mNetworkManager.load(DELETE_THIS_HELP, path, this);
    }

    @Override
    public void finish() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (fromeNotice) {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            intent.putExtra("backRefresh", isRefresh);
            setResult(RESULT_OK, intent);
        } else {
            if (isRefresh)
                setResult();
        }
//        setResult();
        super.finish();
    }

    private boolean mDelete;

    private void setResult() {
        Intent response = new Intent();
        response.putExtra("position", position);

        response.putExtra("commentCount", commentCount);
        response.putExtra("isRefresh", isRefresh);
        response.putExtra("delete", mDelete);

        if (data != null) {
            response.putExtra("ispraise", data.ispraise);
            response.putExtra("paraiseCount", data.praises);
            response.putExtra("type", data.type);
        }
        setResult(Activity.RESULT_OK, response);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_COMMENT:
                        mCommentFragment.refresh();
                        isRefresh = true;
                        break;
                }
            }
        }
    };

    private IWeiboShareAPI mWeiboShareAPI = null;

    public void shareWeibo(String content) {
        AuthInfo mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        SsoHandler mSsoHandler = new SsoHandler(this, mAuthInfo);
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        if (!mWeiboShareAPI.isWeiboAppInstalled() || !mWeiboShareAPI.isWeiboAppSupportAPI()) {
            Toast.makeText(this, getString(R.string.please_install_weibo), Toast.LENGTH_SHORT).show();
            return;
        }

        mWeiboShareAPI.registerApp();
        mWeiboShareAPI.handleWeiboResponse(this.getIntent(), this);

        WeiboMessage weiboMessage = new WeiboMessage();

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = getString(R.string.app_name);
        mediaObject.description = content;

        mediaObject.setThumbImage(/*Util.compressBitmapToWeibo(*/bitmap/*)*/);
        mediaObject.actionUrl = ApiUtils.generatorUrl(HelpDetailActivity.this, URLAddr.SHARE_URL + "1/" + data.helpId);

        weiboMessage.mediaObject = mediaObject;

        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        mWeiboShareAPI.sendRequest(this, request);
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                break;
        }
    }

    @Override
    public void commentsCount(int count) {
        commentCount = count;
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + count);
    }

    @Override
    public void praiseCount(int count) {
        this.data.praises = count;
        mTitleIndicator.updateTitle(1, getString(R.string.help_praise) + " " + count);
    }


    private boolean checkDataIsNull() {
        boolean hasValues = false;
        Field fields[] = HelpListItem.class.getDeclaredFields();
        for (Field f : fields) {

            try {
                Object obj = f.get(data);
                if (obj == null)
                    continue;
                if (obj instanceof String) {
                    if (!obj.equals("")) {
                        hasValues = true;
                        break;
                    }
                } else if (obj instanceof Integer) {
                    if (((Integer) obj != 0)) {
                        hasValues = true;
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
        return hasValues;
    }
}
