package com.vgtech.vancloud.ui.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.inject.Inject;
import com.igexin.sdk.PushManager;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.LoginPresenter;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.LoginActivity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.utils.Utils;

/**
 * Created by zhangshaofang on 2015/11/12.
 */
public class LogoutActivity extends BaseActivity implements View.OnClickListener {
    @Inject
    Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFinishOnTouchOutside(false);
        super.onCreate(savedInstanceState);
        TextView titleTv = (TextView) findViewById(R.id.txt_title);
        titleTv.setText(R.string.offline_title);
        TextView msgTv = (TextView) findViewById(R.id.txt_msg);
        int logoutFrom = getIntent().getIntExtra("logoutFrom", 0);
        findViewById(R.id.et_msg).setVisibility(View.GONE);
        TextView btn_neg = (TextView) findViewById(R.id.btn_neg);
        btn_neg.setText(R.string.log_out);
        TextView btn_pos = (TextView) findViewById(R.id.btn_pos);
        btn_pos.setText(R.string.login_again);
        btn_neg.setOnClickListener(this);
        btn_pos.setOnClickListener(this);
        String date = Utils.getInstance(this).dateFormat(System.currentTimeMillis());
        msgTv.setText(getString(R.string.offline_reasion));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected int getContentView() {
        return R.layout.view_alertdialog;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_neg: {
                getAppliction().clear();
//                FileUtils.writeString("LogoutActivity -> 退出对话框,取消按钮，退出到登录界面！\r\n");
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 11);
                Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                sendBroadcast(reveiverIntent);
                finish();
            }
            break;
            case R.id.btn_pos:
                final SharedPreferences preferences = PrfUtils.getSharePreferences(this);
                if (!TextUtils.isEmpty(preferences.getString("password", null))) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("token");
                    editor.commit();
                    LoginPresenter loginPresenter = new LoginPresenter(this, controller, LoginPresenter.LoginType.logout);
                    loginPresenter.login();
                    PushManager.getInstance().turnOnPush(this);
                } else {
                    getAppliction().clear();
//                    FileUtils.writeString("LogoutActivity -> 退出对话框，确认按钮，密码为空，退出到登录界面！\r\n");
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, 11);
                    Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                    sendBroadcast(reveiverIntent);
                    finish();
                }
                break;
        }
    }
}
