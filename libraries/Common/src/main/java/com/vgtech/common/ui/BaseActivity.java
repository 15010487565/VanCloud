package com.vgtech.common.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.R;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.swipeback.SwipeBackActivity;
import com.vgtech.common.ui.permissions.PermissionsUtil;
import com.vgtech.common.view.IphoneDialog;

/**
 * Created by zhangshaofang on 2016/5/17.
 */
public class BaseActivity extends SwipeBackActivity implements View.OnClickListener {
    private Toast mToast;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        init();
        registerExitReceiver();
    }

    public static final String RECEIVER_EXIT = "RECEIVER_EXIT";
    public static final String RECEIVER_CHAT = "RECEIVER_CHAT";

    private void registerExitReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_EXIT);
        intentFilter.addAction(RECEIVER_CHAT);
        registerReceiver(mExitReceiver, intentFilter);

    }

    private BroadcastReceiver mExitReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_EXIT.equals(action)) {
                finish();
            } else if (RECEIVER_CHAT.equals(action)) {
                finish();
            }
        }
    };

    private void init() {
        String title = getTitle().toString();
        setTitle(title);
        View backView = findViewById(R.id.btn_back);
        if (backView != null)
            backView.setOnClickListener(this);
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView titleTv = (TextView) findViewById(android.R.id.title);
        if (titleTv != null)
            titleTv.setText(title);
    }

    public TextView initRightTv(String lable) {
        TextView rightTv = (TextView) findViewById(R.id.tv_right);
        rightTv.setOnClickListener(this);
        rightTv.setText(lable);
        rightTv.setVisibility(View.VISIBLE);
        return rightTv;
    }

    protected int getContentView() {
        return -1;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            finish();
        }
    }

    protected IphoneDialog iphoneDialog = null;

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
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        networkManager.cancle(this);
        iphoneDialog = null;
        mExitReceiver = null;
        mToast = null;
        System.gc();
        super.finish();
    }

    @Override
    protected void onDestroy() {
//        setContentView(R.layout.activity_empty);
        super.onDestroy();
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

    public ApplicationProxy getApplicationProxy() {
        return (ApplicationProxy) getApplication();
    }

}
