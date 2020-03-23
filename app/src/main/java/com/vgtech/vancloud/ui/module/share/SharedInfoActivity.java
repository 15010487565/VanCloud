package com.vgtech.vancloud.ui.module.share;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.google.gson.Gson;
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
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.api.UserAccount;
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
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.weiboapi.Constants;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.AudioListAdapter;
import com.vgtech.vancloud.ui.adapter.ViewListener;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.common.CountListener;
import com.vgtech.vancloud.ui.common.commentandpraise.ComPraiseFragment;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.ReciverUserActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.view.LinkTouchMovementMethod;
import com.vgtech.vancloud.ui.view.MoreButtonPopupWindow;
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
 * Created by zhangshaofang on 2016/01/18.
 */
public class SharedInfoActivity extends WXEntryActivity implements HttpListener<String>, ViewListener,
        IWeiboHandler.Response, CountListener {


    private final int GET_HELP_DETAIL = 432;
    private final int COLLECTION_THIS_SHARED = 1;

    private final int DELETE_THIS_SHARED = 3;
    private SimpleDraweeView chUserHead;
    private TextView tvUserName;
    private TextView tvCreateTime;
    private TextView tvHelpContent;
    private NetworkManager mNetworkManager;
    private SharedListItem data;
    private String id;

    private MoreButtonPopupWindow menuWindow;

    private NoScrollGridview imagegridview;
    private NoScrollListview voiceListview;

    private LinearLayout sharedContent;
    private TextView reciverNamesView;

    private TextView sharedContentText;
    private NoScrollGridview sharedImagegridview;
    private NoScrollListview sharedvoiceListview;

    private TextView locatetext;

    private int position;
    private int commentCount;

    private boolean isAddCommentFragment = false;

    private boolean isShowCommment;

    private Bitmap bitmap;

    private boolean fromeNotice;

    private boolean backRefresh = false;

    private VancloudLoadingLayout loadingLayout;
    private ListView listView;


    @Override
    protected int getContentView() {
        return R.layout.white_list;
    }

    private View mHeaderView;
    private int showType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.shared_detail_title));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        registerReceiver(mReceiver, intentFilter);
        position = getIntent().getExtras().getInt("position");
        String json = getIntent().getExtras().getString("json");
        id = getIntent().getExtras().getString("id");
        isShowCommment = getIntent().getBooleanExtra("showcomment", false);
        fromeNotice = getIntent().getBooleanExtra("fromeNotice", false);
        listView = (ListView) findViewById(android.R.id.list);
        mHeaderView = getLayoutInflater().inflate(R.layout.share_header, null);
        listView.addHeaderView(mHeaderView);
        View footerView = getLayoutInflater().inflate(R.layout.fragment_layout, null);
        listView.addFooterView(footerView);
        String[] strs = new String[]{};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, strs);
        listView.setAdapter(adapter);
        initViews();
        if (!TextUtil.isEmpty(json)) {
            try {
                data = JsonDataFactory.getData(SharedListItem.class, new JSONObject(json));
                addCommentFragment(data.topicId);
//                addCommentFragment();
                if (isShowCommment) {
                    listView.setSelection(1);
                }
                initData();
            } catch (Exception e) {
            }
        }
        loadHelpInfo();
    }


    private void initViews() {
        chUserHead = (SimpleDraweeView) mHeaderView.findViewById(R.id.user_photo);
        tvUserName = (TextView) mHeaderView.findViewById(R.id.user_name);
        tvCreateTime = (TextView) mHeaderView.findViewById(R.id.timestamp);
        reciverNamesView = (TextView) mHeaderView.findViewById(R.id.reciver_names);
        reciverNamesView.setVisibility(View.VISIBLE);
        tvHelpContent = (TextView) mHeaderView.findViewById(R.id.content_text);
        mHeaderView.findViewById(R.id.btn_share_action).setOnClickListener(this);
        imagegridview = (NoScrollGridview) mHeaderView.findViewById(R.id.imagegridview);
        voiceListview = (NoScrollListview) mHeaderView.findViewById(R.id.voice_listview);
        locatetext = (TextView) mHeaderView.findViewById(R.id.locate_text);
        sharedContent = (LinearLayout) mHeaderView.findViewById(R.id.forward_view);
        sharedContentText = (TextView) mHeaderView.findViewById(R.id.forward_text);
        sharedImagegridview = (NoScrollGridview) mHeaderView.findViewById(R.id.forward_image);
        sharedvoiceListview = (NoScrollListview) mHeaderView.findViewById(R.id.forward_audio);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.load_view);
        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadingLayout.showLoadingView(listView, "", true);
                loadHelpInfo();
            }
        });
    }

    private void initData() {
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

        if (!TextUtil.isEmpty(data.address)) {
            locatetext.setText(data.address);
            locatetext.setVisibility(View.VISIBLE);
        } else {
            locatetext.setVisibility(View.GONE);
        }

        commentCount = data.comments;

        List<NewUser> recivers = data.getArrayData(NewUser.class);
        if (recivers.isEmpty()) {
            reciverNamesView.setText(getResources().getString(R.string.no_time));
        } else {
            reciverNamesView.setText(VgTextUtils.generaReceiver(this, recivers));
            reciverNamesView.setMovementMethod(new LinkTouchMovementMethod());
            reciverNamesView.setFocusable(false);
            reciverNamesView.setClickable(false);
            reciverNamesView.setLongClickable(false);
            mHeaderView.findViewById(R.id.btn_reciver_user).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String json = data.getJson().getJSONArray("receiver").toString();
                        Intent intent = new Intent(SharedInfoActivity.this, ReciverUserActivity.class);
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
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imagegridview, this, imags, Utils.dp2px(this, 60));
            imagegridview.setAdapter(imageGridviewAdapter);
            new ImageLoadFresco.LoadImageFrescoBuilder(getApplicationContext(), new SimpleDraweeView(this), imags.get(0).thumb)
                    .setBitmapDataSubscriber(new BaseBitmapDataSubscriber() {
                        @Override
                        protected void onNewResultImpl(Bitmap bitmap) {
                            if (bitmap == null) {
                                SharedInfoActivity.this.bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                            } else {
                                SharedInfoActivity.this.bitmap = bitmap;
                            }
                        }

                        @Override
                        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                            SharedInfoActivity.this.bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                        }
                    })
                    .build();
        } else {
            imagegridview.setVisibility(View.GONE);
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }


        if (audios != null && !audios.isEmpty()) {
            voiceListview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            voiceListview.setAdapter(audioListAdapter);
        } else {
            voiceListview.setVisibility(View.GONE);

        }
        SharedListItem sharedItem = data.getData(SharedListItem.class);
        if (sharedItem != null) {

            sharedContent.setVisibility(View.VISIBLE);
            String userName = "";
            NewUser u = sharedItem.getData(NewUser.class);
            if (u != null)
                userName = "@" + u.name + ":";
            while (sharedItem.getData(SharedListItem.class) != null) {
                sharedItem = sharedItem.getData(SharedListItem.class);
                NewUser sharedUser = sharedItem.getData(NewUser.class);
                if (sharedUser != null)
                    userName += "@" + sharedUser.name + ":";
            }
            if (!sharedItem.state.equals("2")) {
                sharedContentText.setText(EmojiFragment.getEmojiContentWithAt(SharedInfoActivity.this, sharedContentText.getTextSize(),userName + sharedItem.content));

                final List<ImageInfo> sharedImages = sharedItem.getArrayData(ImageInfo.class);
                final List<AudioInfo> sharedAudios = sharedItem.getArrayData(AudioInfo.class);

                if (sharedImages != null && sharedImages.size() > 0) {
                    sharedImagegridview.setVisibility(View.VISIBLE);
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(sharedImagegridview, SharedInfoActivity.this, sharedImages, Utils.dp2px(this, 70));
                    sharedImagegridview.setAdapter(imageGridviewAdapter);
                } else {
                    sharedImagegridview.setVisibility(View.GONE);
                }

                if (sharedAudios != null && !sharedAudios.isEmpty()) {
                    sharedvoiceListview.setVisibility(View.VISIBLE);
                    AudioListAdapter audioListAdapter = new AudioListAdapter(SharedInfoActivity.this, this);
                    audioListAdapter.dataSource.clear();
                    audioListAdapter.dataSource.addAll(sharedAudios);
                    audioListAdapter.notifyDataSetChanged();
                    sharedvoiceListview.setAdapter(audioListAdapter);
                } else {
                    sharedvoiceListview.setVisibility(View.GONE);

                }

                final String json = sharedItem.getJson().toString();
                sharedContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SharedInfoActivity.this, SharedInfoActivity.class);
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

    private ComPraiseFragment mCommentPraiseFragment;

    private void addCommentFragment(String topicId) {
        if (mCommentPraiseFragment == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            mCommentPraiseFragment = ComPraiseFragment.create(PublishUtils.COMMENTTYPE_SHARE, topicId, position);
            transaction.replace(R.id.fragment_layout, mCommentPraiseFragment);
            transaction.commitAllowingStateLoss();
        }
    }

    //网络请求
    private void loadHelpInfo() {
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("ownid", PrfUtils.getUserId(this));
        String topicId = data == null ? id : data.topicId + "";
        params.put("topicid", topicId);
        addCommentFragment(topicId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SHARED_DETAIL), params, this);
        mNetworkManager.load(GET_HELP_DETAIL, path, this, true);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();

        boolean mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {

            if (callbackId == GET_HELP_DETAIL && !checkDataIsNull()) {
                loadingLayout.showErrorView(listView);
            }
            return;
        }
        switch (callbackId) {
            case GET_HELP_DETAIL:
                loadingLayout.dismiss(listView);
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    data = JsonDataFactory.getData(SharedListItem.class, jsonObject);
                    if ("2".equals(data.state)) {
                        loadingLayout.showEmptyView(listView, getString(R.string.raw_deleted), true, true);
                    } else {
                        initData();
                    }
                } catch (Exception e) {
                    loadingLayout.showErrorView(listView);
                }
                break;
            case COLLECTION_THIS_SHARED:
                try {
                    switch (data.type) {
                        case 0:
                        case 2:
                            backRefresh = true;
                            data.type = 1;
                            Toast.makeText(this, this.getString(R.string.shared_collection_success), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            backRefresh = true;
                            data.type = 2;
                            Toast.makeText(this, this.getString(R.string.shared_discollection_success), Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DELETE_THIS_SHARED:
                try {
                    menuWindow.dismiss();
                    mDelete = true;
                    finish();
                    return;
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
        switch (v.getId()) {
            case R.id.btn_action_comment:
                if (mActionPopView != null && mActionPopView.isShowing())
                    mActionPopView.dismiss();
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_COMMENT);
                intent.putExtra("comment_type", PublishUtils.COMMENTTYPE_SHARE);
                intent.putExtra("publishId", data.topicId);
                intent.putExtra("position", position);
                startActivity(intent);
                break;
            case R.id.btn_share_action:
                showActionMenu(v);
                break;
            case R.id.btn_action_praise: {
                if (mActionPopView != null && mActionPopView.isShowing())
                    mActionPopView.dismiss();
                PublishUtils.toDig(SharedInfoActivity.this, data.topicId + "", PublishUtils.COMMENTTYPE_SHARE, data.ispraise, new PublishUtils.DigCallBack() {

                    @Override
                    public void successful(boolean digType) {
                        if (digType) {
                            data.praises -= data.praises > 0 ? 1 : 0;
                            if (mCommentPraiseFragment != null) {
                                PreferencesController preferencesController = new PreferencesController();
                                preferencesController.context = SharedInfoActivity.this;
                                UserAccount userAccount = preferencesController.getAccount();
                                NewUser user = new NewUser(userAccount.user_id, userAccount.nickname(), userAccount.photo);
                                if (mCommentPraiseFragment.contains(user)) {
                                    mCommentPraiseFragment.remove(user);
                                }
                            }
                        } else {
                            data.praises += 1;
                            if (mCommentPraiseFragment != null) {
                                PreferencesController preferencesController = new PreferencesController();
                                preferencesController.context = SharedInfoActivity.this;
                                UserAccount userAccount = preferencesController.getAccount();
                                NewUser user = new NewUser(userAccount.user_id, userAccount.nickname(), userAccount.photo);
                                if (!mCommentPraiseFragment.contains(user)) {
                                    mCommentPraiseFragment.add(user);
                                }
                            }
                        }
                        data.ispraise = !digType;
                        try {
                            data.getJson().put("praises", data.praises);
                        } catch (Exception e) {
                        }
                        try {
                            data.getJson().put("ispraise", data.ispraise);
                        } catch (Exception e) {
                        }
                        isAddCommentFragment = false;
                        //  initData();

                        backRefresh = true;
                    }
                });
            }
            break;
            case R.id.btn_action_more:
                if (mActionPopView != null && mActionPopView.isShowing())
                    mActionPopView.dismiss();
                menuWindow = new MoreButtonPopupWindow(SharedInfoActivity.this, new MoreButtonPopupWindow.SharedBottomBar() {
                    @Override
                    public void collection(String id) {
                        collectionThisShared(id);
                    }

                    @Override
                    public void deleted(String id) {
                        deleteThisShared(id);
                    }

                    @Override
                    public void sharedToWeiBo(String id) {
                        shareWeibo(data.content.length() > 20 ? data.content.substring(0, 17) + "…" : data.content);
                    }

                    @Override
                    public void sharedToWeiXinSession(String id) {
                        menuWindow.dismiss();

                        HashMap<String, String> obj = new HashMap<String, String>();
                        obj.put("resId", id);
                        obj.put("megTypeId", PublishUtils.COMMENTTYPE_SHARE + "2/" + data.topicId);
                        shareWebPage(SendMessageToWX.Req.WXSceneSession, bitmap, ApiUtils.generatorUrl(SharedInfoActivity.this, URLAddr.SHARE_URL + "2/" + data.topicId), getString(R.string.app_name), data.content);
                    }

                    @Override
                    public void sharedToWeiXin(String id) {
                        menuWindow.dismiss();
                        shareWebPage(SendMessageToWX.Req.WXSceneTimeline, bitmap, ApiUtils.generatorUrl(SharedInfoActivity.this, URLAddr.SHARE_URL + "2/" + data.topicId), getString(R.string.app_name), data.content);
                    }

                    @Override
                    public void forwardTo(String id, String json) {
                        menuWindow.dismiss();
                        Intent intent = new Intent(SharedInfoActivity.this, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FORWARD);
                        intent.putExtra("subtypeId", 2);
                        intent.putExtra("forwardId", id);
                        intent.putExtra("json", json);
                        startActivity(intent);
                    }
                }, data.topicId, data.getJson().toString());
                //显示窗口
                menuWindow.show();
                boolean isMyShared = false;
                try {
                    NewUser user = data.getData(NewUser.class);
                    if (user.userid.equals(PrfUtils.getUserId(SharedInfoActivity.this)))
                        isMyShared = true;
                } catch (Exception e) {
                }

                menuWindow.setIsMine(isMyShared);

                if (data.type == 1)
                    menuWindow.setIsCollection(this, true);
                else
                    menuWindow.setIsCollection(this, false);


                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private PopupWindow mActionPopView;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showActionMenu(View v) {
        View view = LayoutInflater.from(this).inflate(R.layout.share_action, null);
        TextView ivPraise = (TextView) view.findViewById(R.id.iv_praise);
        if (data == null)
            return;
        if (data.ispraise) {
            ivPraise.setText(getString(R.string.cancel));
        } else {
            ivPraise.setText(getString(R.string.praise));
        }
        mActionPopView = new PopupWindow(view, Utils.convertDipOrPx(this, 187),
                ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
        mActionPopView.setFocusable(true);// 使其聚集
        mActionPopView.setOutsideTouchable(true);// 设置允许在外点击消失
 /*       mActionPopView.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.bg_share_action));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景*/
        mActionPopView.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));

        mActionPopView.setAnimationStyle(R.style.Animation_PopupWindow);
        view.findViewById(R.id.btn_action_comment).setOnClickListener(this);
        view.findViewById(R.id.btn_action_praise).setOnClickListener(this);
        view.findViewById(R.id.btn_action_more).setOnClickListener(this);
        mActionPopView.update();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 19) {
            mActionPopView.showAsDropDown(v, 0 - Utils.convertDipOrPx(this, 187), 0 - Utils.convertDipOrPx(this, 25), Gravity.LEFT);
        } else {
            mActionPopView.showAsDropDown(v, 0 - Utils.convertDipOrPx(this, 187), 0 - Utils.convertDipOrPx(this, 25));
        }
    }

    //网络请求
    private void collectionThisShared(String id) {
        menuWindow.dismiss();
        showLoadingDialog(this, getString(R.string.prompt_info_01));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("topicid", id);
        if (data.type != 1)
            params.put("type", "1");
        else
            params.put("type", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SHARED_COLLECTION), params, this);
        mNetworkManager.load(COLLECTION_THIS_SHARED, path, this);
    }

    private boolean mDelete;

    private void deleteThisShared(String id) {

        menuWindow.dismiss();
        showLoadingDialog(this, "", false);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("topicid", id);
        params.put("state", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SHARED_REMOVE), params, this);
        mNetworkManager.load(DELETE_THIS_SHARED, path, this);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_COMMENT:
                        mCommentPraiseFragment.refreshComment();
                        backRefresh = true;
                        break;
                }
            }
        }
    };

    @Override
    public void finish() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (fromeNotice) {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            intent.putExtra("backRefresh", backRefresh);
            setResult(RESULT_OK, intent);
        } else {
            setResult();
        }
        super.finish();
    }

    private void setResult() {
        Intent response = new Intent();
        response.putExtra("position", position);
        response.putExtra("commentCount", commentCount);
        response.putExtra("delete", mDelete);
        if (data != null) {
            response.putExtra("ispraise", data.ispraise);
            response.putExtra("paraiseCount", data.praises);
            response.putExtra("type", data.type);
        }
        if (mCommentPraiseFragment != null) {
            List<NewUser> users = mCommentPraiseFragment.getPraiseList();
            Gson gson = new Gson();
            if (users != null) {
                List<String> userNames = new ArrayList<>(users.size());
                for (NewUser user : users)
                    userNames.add(user.name);
                String praiseUser = gson.toJson(userNames);
                response.putExtra("praiseUser", praiseUser);
            }
            List<Comment> comments = mCommentPraiseFragment.getCommentList();
            if (comments != null && !comments.isEmpty()) {
                int min = Math.min(5, comments.size());
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("[");
                for (int i = 0; i < min; i++) {
                    stringBuffer.append(comments.get(i).getJson().toString());
                    if (i != (min - 1)) {
                        stringBuffer.append(",");
                    }
                }
                stringBuffer.append("]");
                response.putExtra("topCom", stringBuffer.toString());
            }
        }
        setResult(Activity.RESULT_OK, response);
    }

    private IWeiboShareAPI mWeiboShareAPI = null;

    public void shareWeibo(String content) {
        menuWindow.dismiss();
        AuthInfo mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        SsoHandler mSsoHandler = new SsoHandler(this, mAuthInfo);
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        if (!mWeiboShareAPI.isWeiboAppInstalled()) {
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

        int bitmapLength = bitmap.getByteCount();
        mediaObject.setThumbImage(bitmap);

        mediaObject.actionUrl = ApiUtils.generatorUrl(SharedInfoActivity.this, URLAddr.SHARE_URL + "2/" + data.topicId);

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
    }

    @Override
    public void praiseCount(int count) {
        this.data.praises = count;
    }

    private boolean checkDataIsNull() {
        boolean hasValues = false;
        Field fields[] = SharedListItem.class.getDeclaredFields();
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
