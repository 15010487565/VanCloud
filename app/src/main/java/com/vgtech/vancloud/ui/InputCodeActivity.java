package com.vgtech.vancloud.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;

/**
 * Created by code on 2016/9/12.
 * 注册和找回密码输入验证码
 */
public class InputCodeActivity extends BaseActivity {
    private TextView tvPhone;
    private InputCodeFragment fragment;
    private String from; //1、注册。2、找回密码,3更改登陆手机号
    private String areCode; //地区编码
    private String phoneNum;//手机号码
    private String userPwd;//注册公司密码

    @Override
    protected int getContentView() {
        return R.layout.activity_input_code;
    }

    private String mCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Actions.ACTION_UPDATE_PWD));
        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone_value");
        from = intent.getStringExtra("from");
        if ("3".equals(from))
            setTitle(getString(R.string.change_phone));
        else
            setTitle(getString(R.string.input_verification_code));

        areCode = intent.getStringExtra("areaCode");
        phoneNum = intent.getStringExtra("phoneNum");
        userPwd = intent.getStringExtra("userPwd");
        mCode =  intent.getStringExtra("code");
        tvPhone = (TextView) findViewById(R.id.phone_tv);
        tvPhone.setText(phone);
        initPasswordView();
    }

    public void initPasswordView() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if ("1".equals(from)) {
            fragment = InputCodeFragment.create(1, areCode, phoneNum, userPwd,mCode);
        } else if ("2".equals(from)) {
            fragment = InputCodeFragment.create(2, areCode, phoneNum, "",mCode);
        } else {
            fragment = InputCodeFragment.create(3, areCode, phoneNum, "",mCode);
        }
        findViewById(R.id.fragment_layout).setVisibility(View.VISIBLE);
        transaction.replace(R.id.fragment_layout, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private Receiver mReceiver;

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACTION_UPDATE_PWD.equals(intent.getAction())) {
                InputCodeActivity.this.setResult(-2);
                finish();
            }
        }
    }
}
