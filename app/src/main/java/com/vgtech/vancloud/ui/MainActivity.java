package com.vgtech.vancloud.ui;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.igexin.sdk.PushManager;
import com.umeng.analytics.MobclickAgent;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.adapter.TabViewPagerAdapter;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.Tenant;
import com.vgtech.common.api.Update;
import com.vgtech.common.api.User;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.utils.PreferencesManager;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.TabPageIndicator;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.presenter.LoginPresenter;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.service.SubmitService;
import com.vgtech.vancloud.ui.chat.MessagesFragment;
import com.vgtech.vancloud.ui.chat.OnEvent;
import com.vgtech.vancloud.ui.chat.UsersMessagesFragment;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.home.ContactsFragment;
import com.vgtech.vancloud.ui.home.FragmentLifecycle;
import com.vgtech.vancloud.ui.home.MeFragment;
import com.vgtech.vancloud.ui.home.WorkFragment;
import com.vgtech.vancloud.ui.search.SearchFragment;
import com.vgtech.vancloud.ui.view.QuickMenuActionDialog;
import com.vgtech.vancloud.ui.view.TenantSelectDialog;
import com.vgtech.vancloud.ui.view.TenantSelectListener;
import com.vgtech.vancloud.ui.view.VersionUpdateDialog;
import com.vgtech.vancloud.ui.web.HttpWebActivity;
import com.vgtech.vancloud.ui.web.UpdatDialogFragment;
import com.vgtech.vancloud.ui.web.WebX5Activity;
import com.vgtech.vancloud.utils.NoticeUtils;
import com.vgtech.vancloud.utils.UpdateManager;
import com.vgtech.vancloud.utils.Utils;

import org.jivesoftware.smack.SmackAndroid;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.event.Observes;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static android.provider.Settings.EXTRA_APP_PACKAGE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.vgtech.vancloud.reciver.ExitReceiver.EXITACTION;

;

public class MainActivity extends BaseActivity implements BackHandledInterface
        , HttpListener<String>
        , ViewPager.OnPageChangeListener
        , View.OnClickListener
        , TenantSelectListener {
    private static final int CALLBACK_UPDATE = 10;
    private static final int CALLBACK_PHONE_LOGIN = 5;
    @Inject
    Controller controller;
    @Inject
    XmppController xmpp;
    @Inject
    NotificationManager notificationManager;
    //  NetAsyncTask task;
    private BaseFragment mSelectedFragment;

    private NetworkManager mNetworkManager;

    private ViewPager mViewPager;
    List<Fragment> fragmentList;
    private boolean mErrorExit;
    private android.app.AlertDialog mUpdateTipDialog;
    private android.app.AlertDialog.Builder mUpdateTipDialogBuilder;
    private View bgTitlebar;
    TextView titleTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushManager.getInstance().initialize(this.getApplicationContext(),com.vgtech.vancloud.reciver.GetuiPushReceiver.class);
        PushManager.getInstance().registerPushIntentService(getApplicationContext(),com.vgtech.vancloud.reciver.GetuiGTIntentService.class );
        VanCloudApplication vanCloudApplication = (VanCloudApplication) getApplication();
        vanCloudApplication.getApiUtils();

        String deviceId = vanCloudApplication.getApiUtils().getSignParams().get("device_id");
        SharedPreferences preferences = PrfUtils.getSharePreferences(MainActivity.this);
        String userId = preferences.getString("uid", "");
        String tenantId = preferences.getString("tenantId", "");
        Log.e("TAG_GetuiSdkService", "userId----" + userId);
        Log.e("TAG_GetuiSdkService", "tenantId----" + tenantId);
        Log.e("TAG_GetuiSdkService", "deviceId----" + deviceId);
        String alias = MD5.getMD5(userId + tenantId + deviceId);
        boolean result = PushManager.getInstance().bindAlias(MainActivity.this, alias,"android");
        Log.e("TAG_GetuiSdkService", "绑定别名----" + alias + "----结果----" + result);
        boolean pushTurnedOn = PushManager.getInstance().isPushTurnedOn(this);
//        Log.e("TAG_GetuiSdkService", "SDK服务状态----" + pushTurnedOn);

        //顶部标题
        bgTitlebar = findViewById(R.id.bg_titlebar);
        titleTv = getTitleTv();
        setActionSearch();
        setActionMore();
        setSwipeBackEnable(false);
        NoticeUtils.clearMessage(this);
        SmackAndroid smackAndroid = SmackAndroid.init(this);
        xmpp.init();
        mErrorExit = getIntent().getBooleanExtra("error_exit", false);
        mNetworkManager = getAppliction().getNetworkManager();
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        registerDraftReceiver();
        initTenantEdition();
        checkNotifySetting();

        if (DEBUG) {
            try {
                PackageManager manager = this.getPackageManager();
                PackageInfo info = manager.getPackageInfo(getPackageName(),
                        0);
                TextView versionTv = (TextView) findViewById(R.id.tv_version);
                versionTv.setText(info.versionName + "\n" + ApiUtils.getHost(this));
                versionTv.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        boolean switchTenant = getIntent().getBooleanExtra("switchTenant", false);
        if (switchTenant)
            mViewPager.setCurrentItem(1);
        if (!PrfUtils.getAgreementFlag()) {
            initAgreementDialog();
        }else {
            checkUpdate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String type = intent.getStringExtra("intent_type");
        if ("chat".equals(type)) {
            String userId = intent.getStringExtra("userId");
            String name = intent.getStringExtra("name");
            String photo = intent.getStringExtra("photo");
            handlerChatMessage(userId, name, photo);
        } else if ("switchTenant".equals(type)) {//TODO 切换租户

        } else
            initMessage(intent);
    }

    private void initTenantEdition() {
        setNavigationGone();

        setTitle(getString(R.string.below_button_msg));

        initTabBar();
        try {
            xmpp.mTenantId = PrfUtils.getTenantId(this);
            controller.updateMessagesBarNum(this);
        } catch (Exception e) {
        }
        Intent intent = new Intent(MainActivity.RECEIVER_DRAFT);
        sendBroadcast(intent);
        initMessage(getIntent());
        PreferencesController preferencesController = new PreferencesController();
        preferencesController.context = this;
        UserAccount userAccount = preferencesController.getAccount();
        if (userAccount == null) {
            FileUtils.writeString(this,"MainActivity -> 获取用户账号为空，退出到登录界面！\r\n");
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            SharedPreferences preferences = PrfUtils.getSharePreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_name", userAccount.nickname());
            editor.commit();
            PreferencesManager.initUserMsg(this);
        }
        sendBroadcast(new Intent(GroupReceiver.REFRESH));
    }

    private void initMessage(Intent intent) {

        NoticeUtils.clearMessage(this);
        boolean chart = intent.getBooleanExtra("chart", false);
        if (chart) {
            String chatType = intent.getStringExtra("chatType");
            String userId = intent.getStringExtra("userId");
            String name = intent.getStringExtra("name");
            String photo = intent.getStringExtra("photo");
            if (!TextUtils.isEmpty(chatType)) {
                if ("chat".equals(chatType)) {
                    chatUser = new User();
                    chatUser.userid = userId;
                    chatUser.name = name;
                    chatUser.photo = photo;
                } else if ("group".equals(chatType)) {
                    try {
                        chatGroup = ChatGroup.updateFromGroupType(userId, PrfUtils.getUserId(this), System.currentTimeMillis(), PrfUtils.getTenantId(this), name);
                    } catch (Exception e) {

                    }
                }
            }
            selectFragment(0);
        }
    }

    private void checkUpdate() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("devicetype", "android");
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(),
                    0);
            int versionCode = info.versionCode;
            params.put("version", String.valueOf(versionCode));
        } catch (Exception e) {

        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_COMMOM_VERSION), params, this);
        mNetworkManager.load(CALLBACK_UPDATE, path, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (!mErrorExit && xmpp != null && !xmpp.isConnected()) {
            xmpp.startXmpp();
        }
        // if (!DEBUG) {
        sendBroadcast(new Intent(RECEIVER_DRAFT));
        NoticeUtils.updateAppNum(this);
        //  }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @SuppressWarnings("UnusedDeclaration")
    void handleEvent(@Observes OnEvent event) {
        eventHandler.sendMessage(eventHandler.obtainMessage(0, event));
    }

    //region event handler
    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            OnEvent event = (OnEvent) msg.obj;
            assert event != null;
            if (event.type == OnEvent.EventType.NEW) {
                controller.updateMessagesBarNum(ChatGroup.findAll(PrfUtils.getUserId(MainActivity.this), PrfUtils.getTenantId(MainActivity.this)));
            }
        }
    };

    public TabPageIndicator getTabHost() {
        return tabPageIndicator;
    }

    @Override
    public void finish() {
        getAppliction().release();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (eventHandler != null)
            eventHandler.removeCallbacksAndMessages(null);
        eventHandler = null;
        if (xmpp != null)
            xmpp.stopXmpp();
        controller = null;
        xmpp = null;
        notificationManager = null;
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mSelectedFragment = null;
        mViewPager = null;
        fragmentList = null;
        Intent intent = new Intent(this, SubmitService.class);
        stopService(intent);
        super.finish();
    }

    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";
    public static final String RECEIVER_XMPP = "RECEIVER_XMPP";
    public static final String RECEIVER_UPDATE = "RECEIVER_UPDATE";
    public static final String RECEIVER_MAIN_FINISH = "RECEIVER_MAIN_FINISH";

    private void registerDraftReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_DRAFT);
        intentFilter.addAction(RECEIVER_XMPP);
        intentFilter.addAction(RECEIVER_MAIN_FINISH);
        intentFilter.addAction(RECEIVER_UPDATE);
        registerReceiver(mReceiver, intentFilter);

    }

    private static final int MSG_DRAFT = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DRAFT:
//                    updateTabNums(4, (Integer) msg.obj);
                    break;
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (RECEIVER_DRAFT.equals(action)) {
//                int taskCount = PublishTask.queryPublishCount(getApplicationContext());
//                Message msg = new Message();
//                msg.what = MSG_DRAFT;
//                msg.obj = taskCount;
//                mHandler.sendMessage(msg);
            } else if (RECEIVER_XMPP.equals(action)) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xmpp.reStartXmpp();
                    }
                }, 5000);
            } else if (RECEIVER_MAIN_FINISH.equals(action)) {
                finish();
            } else if (RECEIVER_UPDATE.equals(action)) {
                final String android_update_url = intent.getStringExtra("android_update_url");
                if (!TextUtils.isEmpty(android_update_url)) {//个人版已发版，本地未安装，弹出下载提示
                    String update_content = intent.getStringExtra("update_content");
                    AlertDialog alertDialog = new AlertDialog(MainActivity.this).builder().setTitle(getString(R.string.frends_tip))
                            .setMsg(update_content);
//                            alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setLeft();
                    alertDialog.setPositiveButton(getString(R.string.download_now), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UpdateManager manager = new UpdateManager(
                                    MainActivity.this);
                            manager.checkUpdate(android_update_url);
                        }
                    });
                    alertDialog.show();
                }
            }
        }
    };

    @Override
    protected int getContentView() {
        return R.layout.main_layout;
    }

    private TabPageIndicator tabPageIndicator;
    TabViewPagerAdapter fragmentViewPagerAdapter;
    private void initTabBar() {
        fragmentList = new ArrayList<>();
        fragmentList.add(new MessagesFragment());
        fragmentList.add(new WorkFragment());
        fragmentList.add(new ContactsFragment());
        fragmentList.add(new MeFragment());
        fragmentViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(fragmentViewPagerAdapter);
        tabPageIndicator = (TabPageIndicator) findViewById(R.id.tabindicator);
        tabPageIndicator.removeAllViews();
        tabPageIndicator.setViewPager(mViewPager);
        tabPageIndicator.addTab(getIndicator(R.drawable.icon_tab_msg, R.string.below_button_msg));
        tabPageIndicator.addTab(getIndicator(R.drawable.icon_tab_app, R.string.below_button_work));
//        if (AppModulePresenter.hasOpenedModule(this, "paicoin")) {//有派币功能
            //默认显示派币图标
            tabPageIndicator.addView(getIndicator(R.mipmap.ic_pi_coin), new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
//        } else {//无派币功能
//            tabPageIndicator.addView(getIndicator(R.mipmap.btn_add_normal), new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
//        }
        tabPageIndicator.addTab(getIndicator(R.drawable.icon_tab_contact, R.string.below_button_contacts));
        tabPageIndicator.addTab(getIndicator(R.drawable.icon_tab_my, R.string.below_button_me));
        tabPageIndicator.setCurrentTab(0);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(fragmentList.size());
    }

    public User chatUser;
    public ChatGroup chatGroup;

    protected void handlerChatMessage(String userId, String name, String photo) {
//        controller.clearBackStack(this);
        List<Staff> contactses = new ArrayList<Staff>();
        Staff staff = new Staff(userId, userId, name, photo, PrfUtils.getTenantId(this));
        contactses.add(staff);
        UsersMessagesFragment fragment = UsersMessagesFragment.newInstance(
                ChatGroup.fromStaff(contactses.get(0), PrfUtils.getUserId(this), PrfUtils.getTenantId(this)), null);
        controller.pushUserMessagesFragment(fragment);
    }

    public void selectFragment(int index) {
        tabPageIndicator.setCurrentTab(index);
    }

    private View getIndicator(int iconResId, int textResId) {
        View tabIndicator = getLayoutInflater().inflate(R.layout.below_button_layout, null);
        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.button_ico);
        icon.setImageResource(iconResId);
        TextView tv = (TextView) tabIndicator.findViewById(R.id.button_text);
        ColorStateList csl = (ColorStateList) getResources().getColorStateList(R.color.bottom_text_background);
        tv.setTextColor(csl);
        tv.setText(textResId);

        return tabIndicator;
    }

    private View getIndicator(int iconResId) {
        View tabIndicator = getLayoutInflater().inflate(R.layout.middle_tabbar_add, null);
        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.button_ico);
        FrameLayout frameLayout = (FrameLayout) tabIndicator.findViewById(R.id.button_ico_layout);
        icon.setImageResource(iconResId);
        frameLayout.setOnClickListener(this);
        return tabIndicator;
    }

    /**
     * 更新底部标签数字
     *
     * @param index 底部标签ID(0-首页，1-办公，2-通讯)
     * @param num   数字
     */
    public void updateTabNums(int index, int num) {
//        if (Constants.DEBUG){
//            Log.e("TAG_首页Main","num="+num);
//        }
        if (tabPageIndicator != null) {
            View view = tabPageIndicator.getChildAt(index);
            TextView numTextView = (TextView) view.findViewById(R.id.num);
            if (num == 0) {
                numTextView.setVisibility(View.GONE);
            } else {
                numTextView.setVisibility(View.VISIBLE);
                numTextView.setText("" + (num < 100 ? num : "N"));
            }
        }
    }

/*    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 && mSelectedFragment.onBackPressed()) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }*/

    @Override
    public void setSelectedFragment(BaseFragment selectedFragment) {
        this.mSelectedFragment = selectedFragment;
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_UPDATE: {
                JSONObject jsonObject = rootData.getJson();
                try {
                    Update update = JsonDataFactory.getData(Update.class, jsonObject.getJSONObject("data").getJSONObject("version"));
                    PackageManager manager = this.getPackageManager();
                    PackageInfo info = manager.getPackageInfo(getPackageName(),
                            0);
                    int versionCode = info.versionCode;
                    if (versionCode < Integer.parseInt(update.vercode)) {
                        VersionUpdateDialog versionUpdateDialog = new VersionUpdateDialog(this, update);
                        versionUpdateDialog.showUpdateTip();
                    } else {
                        if (!PrfUtils.getUpdateTipFlag(this)) {
//                            startActivity(new Intent(this, UpdateTipActivity.class));
                            updataDialog();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!PrfUtils.getUpdateTipFlag(this)) {
//                        startActivity(new Intent(this, UpdateTipActivity.class));
                        updataDialog();
                    }
                }
            }
            break;
            case CALLBACK_PHONE_LOGIN:
                Toast.makeText(this, getString(R.string.personal_login_success_msg), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("万客","keyCode="+keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (controller != null && controller.fm().getBackStackEntryCount() == 0) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, getString(R.string.toast_exit), Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    sendBroadcast(new Intent(EXITACTION));
                    finish();
                }
                return true;
            } else {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0 && mSelectedFragment.onBackPressed()) {
                    getSupportFragmentManager().popBackStack();
                    return true;
                } else {
                    List<Fragment> fragments = getSupportFragmentManager().getFragments();
                    if (fragments != null && fragments.size() > 0) {
                        Fragment fragment = fragments.get(fragments.size() - 1);
                        if (fragment instanceof UsersMessagesFragment) {
                            UsersMessagesFragment usersMessagesFragment = (UsersMessagesFragment) fragment;
                            usersMessagesFragment.onBackPressed();
                            return true;
                        }
                    }
                }
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            //跳转职位
            case 100:
//                if (!mTenant) {
//                    if (resultCode == RESULT_OK)
//                        tabPageIndicator.setCurrentTab(1);
//                }
                break;
            case QuickMenuActionDialog.REQUEST_SCANBAR:
                String barcode = data.getStringExtra("barcode");
                loginPc(barcode);
                break;
            case QuickMenuActionDialog.REQUEST_USERSELECT:
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                    if (userSelectList != null && !userSelectList.isEmpty()) {
                        List<Staff> contactses = new ArrayList<Staff>();
                        for (Node node : userSelectList) {
                            if (node.isUser()) {
                                Staff staff = new Staff(String.valueOf(node.getId()), String.valueOf(node.getId()), node.getName(), node.getPhoto(), PrfUtils.getTenantId(this));
                                contactses.add(staff);
                            }
                        }
                        if (contactses.isEmpty()) {
                            Toast.makeText(this, R.string.toast_chatgroup_empty, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        xmpp.chat(contactses, null);
                    }
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void loginPc(String barcode) {
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("qr_id", barcode);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_PHONE_LOGIN), params, this);
        showLoadingDialog(this, "");
        networkManager.load(CALLBACK_PHONE_LOGIN, path, this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                setTitle(getString(R.string.below_button_msg));
                titleTv.setTextColor(ContextCompat.getColor(this,R.color.white));
                titleTv.setTextSize(17);
                bgTitlebar.setBackgroundColor(ContextCompat.getColor(this,R.color.bg_title));
                setActionSearch();
                setActionMore();
                break;
            case 1:
                FragmentLifecycle fragmentToShow = (FragmentLifecycle)fragmentList.get(position);
                fragmentToShow.onResumeFragment();

                titleTv.setOnClickListener(this);
                titleTv.setTextColor(ContextCompat.getColor(this,R.color.black_22));
                titleTv.setTextSize(17);
                bgTitlebar.setBackgroundColor(ContextCompat.getColor(this,R.color.below_button));
                setTitle(TenantPresenter.getCurrentTenant(this).tenant_name);
                findViewById(R.id.btn_action_search).setVisibility(View.GONE);
                findViewById(R.id.btn_action_more).setVisibility(View.GONE);

                break;
            case 2:
                titleTv.setTextColor(ContextCompat.getColor(this,R.color.white));
                titleTv.setTextSize(17);
                setTitle(getString(R.string.below_button_contacts));
                bgTitlebar.setBackgroundColor(ContextCompat.getColor(this,R.color.bg_title));
                findViewById(R.id.btn_action_search).setVisibility(View.GONE);
                findViewById(R.id.btn_action_more).setVisibility(View.GONE);
                break;
            case 3:
                titleTv.setTextColor(ContextCompat.getColor(this,R.color.white));
                titleTv.setTextSize(17);
                setTitle(getString(R.string.below_button_me));
                bgTitlebar.setBackgroundColor(ContextCompat.getColor(this,R.color.bg_title));
                findViewById(R.id.btn_action_search).setVisibility(View.GONE);
                findViewById(R.id.btn_action_more).setVisibility(View.GONE);
                break;
        }

        TextView titleTv = getTitleTv();
        if (position == 1 && TenantPresenter.getTenant(this).size() > 1) {
            setBackEnable(true);
            Drawable icon = this.getResources().getDrawable(R.drawable.ic_title_arrow);
            titleTv.setCompoundDrawablesWithIntrinsicBounds(null, null,icon , null);
            titleTv.setCompoundDrawablePadding(15);
        } else {
            setBackEnable(false);
            titleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (2 == position) {
            tabPageIndicator.setCurrentTab(position + 1);
        } else if (3 == position) {
            tabPageIndicator.setCurrentTab(position + 1);
        } else {
            tabPageIndicator.setCurrentTab(position);
        }

    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_ico_layout://π币

                if (AppModulePresenter.hasOpenedModule(this, "paicoin")) {
                    Intent piCoinInt = new Intent(this, HttpWebActivity.class);
                    piCoinInt.putExtra("code",43);
                    piCoinInt.putExtra("isSratrPi",true);
                    startActivity(piCoinInt);
                } else { //无π币功能禁止点击
//                    QuickOptionDialog quickOptionDialog = new QuickOptionDialog(this);
//                    quickOptionDialog.initQuickDialog();
                    String hostUrl = ApiUtils.getHost(this);
                    Intent piCoinInt = new Intent(this, WebX5Activity.class);
                    piCoinInt.putExtra("url",hostUrl+URLAddr.POST_PAI_UNPERMISSION);
                    piCoinInt.putExtra("title","派商城");
                    startActivity(piCoinInt);
                }
                break;
            case R.id.btn_action_more:
                QuickMenuActionDialog quickOptionActionDialog = new QuickMenuActionDialog(this);
                quickOptionActionDialog.showPop(v);
                break;
            case R.id.btn_action_search:
                SearchFragment fragment = new SearchFragment();
                controller.pushFragment(fragment);
                break;
            case R.id.btn_back:
                break;
            case android.R.id.title:
                TenantSelectDialog tenantSelectDialog = new TenantSelectDialog(this, this);
                tenantSelectDialog.showPop(v);
                break;
            default:
                super.onClick(v);
                break;
        }
    }


    @Override
    public void onTenantSelected(Tenant tenant) {
        if (!tenant.tenant_id.equals(TenantPresenter.getCurrentTenant(this).tenant_id)) {
//            Toast.makeText(this, tenant.tenant_name, Toast.LENGTH_SHORT).show();
            Utils.clearUserInfoBySwitchTenant(this);
            if (!TextUtils.isEmpty(PrfUtils.getPrfparams(this, "password"))) {
                LoginPresenter loginPresenter = new LoginPresenter(this, controller);
                loginPresenter.login(tenant.tenant_id);
            } else {
                Intent intent = new Intent();
                intent.setAction("com.vgtech.vancloud.login");
                intent.putExtra("logout", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
//                FileUtils.writeString("MainActivity -> onTenantSelected 发送广播RECEIVER_EXIT");
                Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                sendBroadcast(reveiverIntent);
            }
        }
    }

    /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * @return true: Activity可以滑动退出, 并且总是优先;  false: Activity不允许滑动退出
     */
    @Override
    public boolean swipeBackPriority() {
        int c = getSupportFragmentManager().getBackStackEntryCount();
        return c < 0;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * 作者：CnPeng
     * 时间：2018/7/12 上午9:02
     * 功用：检查是否已经开启了通知权限
     * 说明：
     */
    private void checkNotifySetting() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        boolean isOpened = manager.areNotificationsEnabled();

        if (!isOpened) {
            new AlertDialog(MainActivity.this).builder().setTitle(getString(R.string.frends_tip))
                    .setMsg(getString(R.string.notification_permission))
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                                intent.putExtra(EXTRA_APP_PACKAGE, getPackageName());
                                intent.putExtra(EXTRA_CHANNEL_ID, getApplicationInfo().uid);

                                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                                intent.putExtra("app_package", getPackageName());
                                intent.putExtra("app_uid", getApplicationInfo().uid);

                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                                Intent intent = new Intent();

                                //下面这种方案是直接跳转到当前应用的设置界面。
                                //https://blog.csdn.net/ysy950803/article/details/71910806
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    }).setNegativeButton(getString(R.string.new_cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();

        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, String... permission) {
        super.onPermissionsGranted(requestCode, permission);
        Log.e("TAG_同意权限","调用更新日志");
        checkUpdate();
    }
    private void updataDialog(){
        UpdatDialogFragment updataDialog = new UpdatDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("url", downloadFile.getPath());
//        updataDialog.setArguments(bundle);
        updataDialog.show(getSupportFragmentManager(),"updata");
    }
}


