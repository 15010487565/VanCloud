package com.vgtech.vancloud.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Key;
import com.igexin.sdk.PushManager;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.Error;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.swipeback.SwipeBackActivity;
import com.vgtech.common.ui.permissions.PermissionsUtil;
import com.vgtech.common.utils.AlarmUtils;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.view.IphoneDialog;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.presenter.LoginPresenter;
import com.vgtech.vancloud.reciver.AlarmReceiver;
import com.vgtech.vancloud.reciver.ChatGroupReceiver;
import com.vgtech.vancloud.reciver.ExitReceiver;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.common.record.MediaManager;
import com.vgtech.vancloud.ui.web.AgreementDialogFragment;
import com.vgtech.vancloud.utils.NoticeUtils;
import com.vgtech.vancloud.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

import static com.vgtech.vancloud.reciver.AlarmReceiver.SCHEDULEACTION;

/**
 * Created by zhangshaofang on 2015/7/21.
 */
public class BaseActivity extends SwipeBackActivity implements IEncActivity,
        RoboContext
        , PermissionsUtil.IPermissionsCallback
        , AgreementDialogFragment.CloseDialogFragment {
    protected HashMap<Key<?>, Object> scopedObjects = new HashMap<Key<?>, Object>();

    protected IphoneDialog iphoneDialog;
    private Toast mToast;

    public static String RECEIVER_ERROR = "RECEIVER_ERROR";


    PermissionsUtil permissionsUtil;
    protected static final int PERMISSIONS_REQUESTCODE = 0x0000001;
    protected static final String[] permissions = {
            android.Manifest.permission.CAMERA,//相机

            android.Manifest.permission.RECORD_AUDIO,//麦克风

            android.Manifest.permission.READ_CONTACTS,//联系人

            android.Manifest.permission.CALL_PHONE,//相机

            android.Manifest.permission.READ_PHONE_STATE,

            PermissionsUtil.Permission.Storage.READ_EXTERNAL_STORAGE,

            PermissionsUtil.Permission.Location.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final RoboInjector injector = RoboGuice.getInjector(this);
        injector.injectMembersWithoutViews(this);
        super.onCreate(savedInstanceState);
        PushManager.getInstance().initialize(this.getApplicationContext(), com.vgtech.vancloud.reciver.GetuiPushReceiver.class);
        // MobclickAgent.onEvent(this, getClass().getName());
        setContentView(getContentView());
        init();
        registerExitReceiver();
        //推送广播
        registerPushReceiver();
        long noticeID = getIntent().getLongExtra("noticeid", -1);
        if (noticeID >= 0) {
            MessageDB.makeRead(this, noticeID);
        }
    }

    private void registerPushReceiver() {

        //兼容8.0广播注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GroupReceiver.REFRESH);
        receiver = new GroupReceiver();
        registerReceiver(receiver, intentFilter);

        IntentFilter intentFilterAlarm = new IntentFilter();
        intentFilterAlarm.addAction(AlarmUtils.ALARMALERT);
        intentFilterAlarm.addAction(SCHEDULEACTION);
        alarmReceiver = new AlarmReceiver();
        registerReceiver(alarmReceiver, intentFilterAlarm);

        IntentFilter intentFilterChatGroup = new IntentFilter();
        intentFilterChatGroup.addAction(Actions.ACTION_CHATGROUP_REFRESH);
        chatGroupReceiver = new ChatGroupReceiver();
        registerReceiver(chatGroupReceiver, intentFilterChatGroup);

        IntentFilter intentFilterExit = new IntentFilter();
        intentFilterExit.addAction(ExitReceiver.EXITACTION);
        exitReceiver = new ExitReceiver();
        registerReceiver(exitReceiver, intentFilterExit);
    }


    public TextView initRightTv(String lable) {
        TextView rightTv = (TextView) findViewById(R.id.tv_right);
        rightTv.setOnClickListener(this);
        rightTv.setText(lable);
        rightTv.setVisibility(View.VISIBLE);
        return rightTv;
    }

    public static final String RECEIVER_EXIT = "RECEIVER_EXIT";
    public static final String RECEIVER_CHAT = "RECEIVER_CHAT";
    public static final String RECEIVER_LEAVEOFFICE = "RECEIVER_LEAVEOFFICE";

    private void registerExitReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_EXIT);
        intentFilter.addAction(RECEIVER_CHAT);
        intentFilter.addAction(RECEIVER_LEAVEOFFICE);
        registerReceiver(mExitReceiver, intentFilter);
    }

    private HashMap<String, Object> mTempDataMap;

    public Object getTempData(String key, boolean remove) {
        if (mTempDataMap == null) {
            return null;
        }
        Object obj = remove ? mTempDataMap.remove(key) : mTempDataMap.get(key);
        try {
            return obj;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void addTempData(String key, Object data) {
        if (mTempDataMap == null) {
            mTempDataMap = new HashMap<String, Object>();
        }
        mTempDataMap.put(key, data);
    }

    private BroadcastReceiver mExitReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_EXIT.equals(action) && !(BaseActivity.this instanceof LoginActivity)) {
                FileUtils.writeString(context,"BaseActivity -> RECEIVER_EXIT 广播，关闭当前页面！\r\n");
                finish();
            } else if (RECEIVER_CHAT.equals(action)) {
                if (BaseActivity.this instanceof MainActivity) {
                    String userId = intent.getStringExtra("userId");
                    String name = intent.getStringExtra("name");
                    String photo = intent.getStringExtra("photo");
                    handlerChatMessage(userId, name, photo);
                } else {
                    finish();
                }
            } else if (RECEIVER_LEAVEOFFICE.equals(action)) {

                if (!NoticeUtils.isBackground(BaseActivity.this)) {
                    FileUtils.writeString(context,"BaseActivity -> LEAVEOFFICE(离职) 广播，退出到登录界面！\r\n");
                    Intent newintent = new Intent(BaseActivity.this, LoginActivity.class);
                    startActivity(newintent);
                }
                Utils.clearUserInfo(BaseActivity.this);
                FileUtils.writeString(context,"BaseActivity -> LEAVEOFFICE(离职) 广播，发送退出（RECEIVER_EXIT）广播！\r\n");
                Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                sendBroadcast(reveiverIntent);
            }
        }
    };

    protected void handlerChatMessage(String userId, String name, String photo) {

    }

    @Override
    public void finish() {
        try {
            if (mExitReceiver != null) {
                unregisterReceiver(mExitReceiver);
                mExitReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getAppliction().getNetworkManager().cancle(this);
        iphoneDialog = null;
        mErrorInfoView = null;
        mPath = null;
        mDialogListener = null;
        mExitReceiver = null;
        mHttpListener = null;
        scopedObjects = null;
        System.gc();
        super.finish();

    }

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }

    private void init() {
        String title = getTitle().toString();
        setTitle(title);
        View backView = findViewById(R.id.btn_back);
        if (backView != null)
            backView.setOnClickListener(this);
        View addView = findViewById(R.id.add);
        if (addView != null)
            addView.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    protected void setActionSearch() {
        View searchView = findViewById(R.id.btn_action_search);
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnClickListener(this);
    }

    protected void setNavigationGone() {
        findViewById(R.id.iv_back).setVisibility(View.GONE);
        findViewById(R.id.btn_back).setEnabled(false);
        findViewById(R.id.title_right).setVisibility(View.VISIBLE);
    }

    protected void setBackEnable(boolean enable) {
        findViewById(R.id.btn_back).setEnabled(enable);
    }

    protected void setActionMore() {
        View searchView = findViewById(R.id.btn_action_more);
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnClickListener(this);
    }

    protected TextView getTitleTv() {
        TextView titleTv = (TextView) findViewById(android.R.id.title);
        return titleTv;
    }

    @Override
    public void setTitle(CharSequence title) {

        TextView titleTv = (TextView) findViewById(android.R.id.title);
        if (titleTv != null)
            titleTv.setText(title);
    }

    protected int getContentView() {
        return -1;
    }


    @Override
    public VanCloudApplication getAppliction() {
        return (VanCloudApplication) getApplication();
    }

    private int mCallbackId;
    private NetworkPath mPath;
    private HttpListener<String> mHttpListener;

    @Override
    public void notifyErrDlg(int callbackId, NetworkPath path, HttpListener<String> listener) {
        mCallbackId = callbackId;
        mPath = path;
        mHttpListener = listener;
    }


    public void showError(com.vgtech.common.api.Error error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.err_msg_panel,
                null);
        setDialogError(view, error, mPath);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setTitle(R.string.title_error);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        AlertDialog dialog = builder.create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getText(R.string.btn_retry), mDialogListener);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getText(R.string.cancel), mDialogListener);
        if (!isFinishing())
            dialog.show();
    }

    protected void showProgress(View progressLayout, boolean show) {

        if (progressLayout == null) {
            return;
        }
        if (progressLayout.getVisibility() != View.VISIBLE) {
            progressLayout.setVisibility(View.VISIBLE);
        }
        if (show) {
            progressLayout.findViewById(R.id.error_footer).setVisibility(
                    View.GONE);
            progressLayout.findViewById(R.id.progressBar).setVisibility(
                    View.VISIBLE);
        } else {
            progressLayout.findViewById(R.id.error_footer).setVisibility(
                    View.VISIBLE);
            progressLayout.findViewById(R.id.progressBar).setVisibility(
                    View.GONE);
        }
    }

    private View mErrorInfoView;

    private void setDialogError(View view, Error error, NetworkPath path) {
        TextView errcodeView = (TextView) view.findViewById(R.id.errcode);
        TextView errmsgView = (TextView) view.findViewById(R.id.errmsg);
        errcodeView.setText(String.valueOf(error.code));
        errmsgView.setText(Html.fromHtml(error.msg));
        View btnErrorView = view.findViewById(R.id.btn_error_info);
        btnErrorView.setVisibility(View.GONE);
        if (Constants.DEBUG) {
//            if (mErrorInfoView != null) {
//                mErrorInfoView.setVisibility(View.GONE);
//            }
            btnErrorView.setVisibility(View.VISIBLE);
            btnErrorView.setOnClickListener(this);
            mErrorInfoView = view.findViewById(R.id.error_info);
            mErrorInfoView.setVisibility(View.VISIBLE);
            TextView url = (TextView) mErrorInfoView
                    .findViewById(R.id.error_url);
            TextView post = (TextView) mErrorInfoView
                    .findViewById(R.id.error_postvalue);
            TextView exception = (TextView) mErrorInfoView
                    .findViewById(R.id.error_exception);
            url.setText(path.getUrl());
            post.setText(String
                    .valueOf(path.getPostValues() == null ? ""
                            : path.getPostValues()));
            if (error != null && !TextUtils.isEmpty(error.desc))
                exception.setText(Html.fromHtml(error.desc));
        }
    }

    protected void retryLoading() {
    }

    protected void retry() {
        getAppliction().getNetworkManager().load(mCallbackId, mPath, mHttpListener);
    }

    private DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    retryLoading();
                    retry();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_error_info:
                if (mErrorInfoView != null) {
                    mErrorInfoView
                            .setVisibility(mErrorInfoView.getVisibility() == View.VISIBLE ? View.GONE
                                    : View.VISIBLE);
                }
                break;
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_action_more:
                // TODO search
                break;
            default:
                break;
        }
    }


    protected void activityForResult(int requestCode, int resultCode, Intent data) {

    }


    /**
     * @param mContext
     * @param contentStr
     */
    public void showLoadingDialog(Context mContext, String contentStr) {
        if (iphoneDialog == null) {
            iphoneDialog = new IphoneDialog(mContext);
        }
        iphoneDialog.setMessage(contentStr);
        iphoneDialog.show(true);
    }

    public void showLoadingDialog(Context mContext, String contentStr, boolean ifCandismiss) {
        if (iphoneDialog == null) {
            iphoneDialog = new IphoneDialog(mContext);
        }
        iphoneDialog.setMessage(contentStr);
        iphoneDialog.show(ifCandismiss);
    }


    /**
     *
     */
    public void dismisLoadingDialog() {
        if (iphoneDialog != null && iphoneDialog.isShowing()) {
            iphoneDialog.dismiss();
        }
    }

    /**
     * 显示toast
     */
    protected void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) return;
        if (null == mToast) mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mToast.setText(msg);
        mToast.show();
    }

    /**
     * 显示toast
     */
    protected void showToast(int msg) {
        showToast(getString(msg));
    }

    GroupReceiver receiver;
    AlarmReceiver alarmReceiver;
    ChatGroupReceiver chatGroupReceiver;
    ExitReceiver exitReceiver;

    @Override
    protected void onResume() {
//        Log.e("TAG_讨论组","onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.stop();
    }

    @Override
    protected void onDestroy() {
        setContentView(R.layout.activity_empty);
        super.onDestroy();
        //取消广播接收者的注册
//        unregisterReceiver(pushReceiver);

        unregisterReceiver(receiver);
        unregisterReceiver(alarmReceiver);
        unregisterReceiver(chatGroupReceiver);
        unregisterReceiver(exitReceiver);


    }

    public void initAgreementDialog() {
        AgreementDialogFragment agreement = new AgreementDialogFragment();
        agreement.setCloseDialogFragment(this);
//        Bundle bundle = new Bundle();
//        bundle.putString("url", downloadFile.getPath());
//        agreement.setArguments(bundle);
        agreement.show(getSupportFragmentManager(), "lose");
    }

    @Override
    public void close() {
        //通知权限
        isCheckNotifications();
        permissionsUtil = PermissionsUtil
                .with(this)
                .requestCode(PERMISSIONS_REQUESTCODE)
                .isDebug(true)//开启log
                .permissions(permissions)
                .request();
    }

    public void isCheckNotifications() {
        try {
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());//利用这个可以检测
            boolean isOpened = manager.areNotificationsEnabled();
            if (!isOpened) {
                new com.vgtech.common.view.AlertDialog(BaseActivity.this).builder().setTitle(getString(R.string.frends_tip))
                        .setMsg(getString(R.string.notification_permission))
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", BaseActivity.this.getApplication().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).setNegativeButton(getString(R.string.new_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUESTCODE:
                //监听跳转到权限设置界面后再回到应用
                if (permissionsUtil !=null){
                    permissionsUtil.onActivityResult(requestCode, resultCode, data);
                }

                break;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //需要调用onRequestPermissionsResult
        if (permissionsUtil !=null){
            permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onPermissionsGranted(int requestCode, String... permission) {

        for (String permiss: permission) {

            if (android.Manifest.permission.READ_PHONE_STATE.equals(permiss)) {
                String deviceId = ApiUtils.getDeviceId(this);
                Log.e("TAG_声明", "deviceId----" + deviceId);
                LoginPresenter loginPresenter = new LoginPresenter(this, null);
                loginPresenter.userauthConfirm(deviceId, Build.MODEL);
            }
        }
        //关闭协议弹窗
//        Log.e("TAG_权限",  "需要申请权限数量="+permissions.length+";已申请权限数量="+permission.length);
//        if (permissions.length==permission.length){
            PrfUtils.setAgreementFlag(true);

//        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, String... permission) {
        //权限被拒绝回调
        Toast.makeText(this, getString(R.string.device_permissions), Toast.LENGTH_SHORT).show();
        finish();
    }
    public RecyclerView.ItemDecoration getRecyclerViewDivider(@DrawableRes int drawableId) {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(drawableId));
        return itemDecoration;
    }

}
