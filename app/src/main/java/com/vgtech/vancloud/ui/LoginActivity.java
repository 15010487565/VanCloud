package com.vgtech.vancloud.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.ui.permissions.PermissionsUtil;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.LoginPresenter;
import com.vgtech.vancloud.signin.SignInLoginActivity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.register.country.CountryActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.Utils;

/**
 * Created by zhangshaofang on 2016/5/24.
 */
public class LoginActivity extends BaseActivity {
    private EditText mEtUsertel;
    private EditText mEtPassword;
    private TextView mAreaTv;
    public static final String RECEIVER = "CHANE_RECEIVER";
    @Inject
    Controller controller;



    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver);
        mReceiver = null;
        mEtUsertel = null;
        mEtPassword = null;
        mAreaTv = null;
        controller = null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(false);

        if (!PrfUtils.getAgreementFlag()) {
            initAgreementDialog();
        }
        com.vgtech.common.utils.ActivityUtils.setAppLanguage(this);
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Actions.ACTION_SIGIN_END));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER);
        registerReceiver(mReceiver, intentFilter);

        Intent intent = getIntent();
        boolean logout = intent.getBooleanExtra("logout", false);
        if (logout) {
            String params = intent.getStringExtra("params");
            String result = intent.getStringExtra("result");
            if (Constants.DEBUG) {
                TextView error_log = (TextView) findViewById(R.id.error_log);
                error_log.setText("logoutFrom" + params + "\n" + result);
            }
            if (intent.getBooleanExtra("sendbroadcast", false)) {
                Intent reveiverIntent = new Intent("RECEIVER_EXIT");
                sendBroadcast(reveiverIntent);
            }
            Utils.clearUserInfo(this);
        }
        if (LoginPresenter.isLogin(this)) {
            LoginPresenter loginPresenter = new LoginPresenter(this, controller);
            loginPresenter.enterSystem();
            return;
        }
        if (Constants.DEBUG) {
            findViewById(R.id.btn_setting).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_setting).setOnClickListener(this);
        }
        mEtUsertel = (EditText) findViewById(R.id.et_phone);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mEtPassword.setTypeface(Typeface.SANS_SERIF);
        mEtUsertel.addTextChangedListener(new VanTextWatcher(mEtUsertel, findViewById(R.id.del_phone)));
        mEtPassword.addTextChangedListener(new VanTextWatcher(mEtPassword, findViewById(R.id.del_pwd)));
        mAreaTv = (TextView) findViewById(R.id.tv_area_code);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_forget_pwd).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);
        findViewById(R.id.tv_SignIn).setOnClickListener(this);
        findViewById(R.id.area_code_layout).setOnClickListener(this);
        CheckBox toggleButton = (CheckBox) findViewById(R.id.tb_pwd);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                    mEtPassword.setSelection(mEtPassword.getText().toString().length());
                } else {
                    mEtPassword.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                    mEtPassword.setSelection(mEtPassword.getText().toString().length());
                }
            }
        });
        initData();

    }

    private void initData() {
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        String username = preferences.getString("username", "");
        String areaCode = preferences.getString("areaCode", "");
        if (!TextUtil.isEmpty(username)) {
            mEtUsertel.setText(username);
            if (!areaCode.contains("+"))
                areaCode = "+" + areaCode;
            mAreaTv.setText(areaCode);
        }
    }

    private static final int REQUEST_CHOOSE = 1001;
    private static final int REQUEST_REGISTER = 1002;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting: {
                Intent intent = new Intent(this, HostSettingActivity.class);
                startActivity(intent);
//                PrfUtils.setAgreementFlag(false);
            }
            break;
            case R.id.area_code_layout: {
                Intent intent = new Intent(this, CountryActivity.class);
                intent.putExtra("style", "company");
                startActivityForResult(intent, REQUEST_CHOOSE);
            }
            break;
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_forget_pwd: {//忘记密码
                Intent intent = new Intent(this, FindPwdStartActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.tv_SignIn: {//个人登录
                Intent intent = new Intent(this, SignInLoginActivity.class);
                startActivity(intent);
                finish();
            }
            break;
            case R.id.btn_register: {
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_REGISTER);
            }
            break;
            default:
                super.onClick(v);
                break;
        }
    }

    public boolean isChina() {
        String areaCode = mAreaTv.getText().toString();
        areaCode = TextUtil.formatAreaCode(areaCode);
        return TextUtils.equals(areaCode, "86") || TextUtils.equals(areaCode, "+86");
    }

    /**
     * 登录方法
     */
    private void login() {
        //取出登陆数据
        String areaCode = mAreaTv.getText().toString();
        areaCode = TextUtil.formatAreaCode(areaCode);
        String phone = TextUtil.getString(mEtUsertel);
        String passWord = mEtPassword.getText().toString();
        //校验数据
        if (TextUtil.isEmpty(this, mEtUsertel, mEtPassword)) {
            return;
        }
        if (!TextUtil.isAvailablePhone(this, phone, isChina())) {
            return;
        }
        LoginPresenter loginPresenter = new LoginPresenter(this, controller);
        loginPresenter.login(areaCode, phone, passWord, null);
    }

    public String getAreaCode() {
        String areaCode = mAreaTv.getText().toString();
        return TextUtil.formatAreaCode(areaCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String countryName = bundle.getString("countryName");
                    String countryNum = bundle.getString("countryNumber");
                    mAreaTv.setText(countryNum);
                }
                break;
            case REQUEST_REGISTER:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Receiver mReceiver;

    @Override
    public void close() {
        Log.e("TAG_login","关闭保护协议");
        //通知权限
        isCheckNotifications();
        permissionsUtil = PermissionsUtil
                .with(this)
                .requestCode(PERMISSIONS_REQUESTCODE)
                .isDebug(true)//开启log
                .permissions(permissions)
                .request();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACTION_SIGIN_END.equals(intent.getAction())) {
//                FileUtils.writeString("LoginActivity -> ACTION_SIGIN_END 关闭登录界面！");
                LoginActivity.this.finish();
            } else if (RECEIVER.equals(intent.getAction())) {

                String countryName = intent.getStringExtra("countryName");
                String countryNum = intent.getStringExtra("countryNumber");
                mAreaTv.setText(countryNum);

            }
        }
    }

}
