package com.vgtech.vancloud.ui;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangshaofang on 2016/5/24.
 */
public class FindPwdActivity extends BaseActivity implements HttpListener<String> {
    private EditText mEtPassword;
    private EditText mEdConfirmPassword;
    private String code;//验证码
    private String phoneNum;//手机号
    private String areaCode;//区号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.find_pwd));
        Intent intent =  getIntent();
        code = intent.getStringExtra("code_value");
        phoneNum = intent.getStringExtra("phoneNum");
        areaCode = intent.getStringExtra("areaCode");
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mEtPassword.setTypeface(Typeface.SANS_SERIF);
        mEtPassword.addTextChangedListener(new VanTextWatcher(mEtPassword, findViewById(R.id.del_pwd)));
        mEdConfirmPassword = (EditText) findViewById(R.id.et_password_confirm);
        mEdConfirmPassword.setTypeface(Typeface.SANS_SERIF);
        mEdConfirmPassword.addTextChangedListener(new VanTextWatcher(mEdConfirmPassword, findViewById(R.id.del_pwd_confirm)));
        findViewById(R.id.btn_reset_pwd).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_pwd:
                netResetPwd();
                break;
            default:
                super.onClick(v);
                break;
        }
    }
    private static final int CALL_BACK_RESET_PWD = 1001;

    private void netResetPwd() {
        String password = mEtPassword.getText().toString().trim();
        String confirmpassword = mEdConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            showToast(getString(R.string.please_input_new_pwd));
            return;
        }
        if (TextUtils.isEmpty(confirmpassword)) {
            showToast(getString(R.string.please_input_confirm_pwd));
            return;
        }
        if (!TextUtil.isAvailablePwd(this, password)) {
            return;
        }
        if (!password.equals(confirmpassword)) {
            showToast(getString(R.string.confirm_passwrod));
            return;
        }
        showLoadingDialog(this, getString(R.string.dataloading));
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("area_code", TextUtil.formatAreaCode(areaCode));
        params.put("validatecode", code);
        params.put("username", phoneNum);
        params.put("password", password);
        params.put("flag", "1");
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_REGISTER_UPDATEPWD), params, this);
        mNetworkManager.load(CALL_BACK_RESET_PWD, path, this);
    }

    private NetworkManager mNetworkManager;

    @Override
    protected int getContentView() {
        return R.layout.activity_find_pwd;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_RESET_PWD:
                Toast.makeText(this, getString(R.string.vancloud_reset_password_prompt), Toast.LENGTH_SHORT).show();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Actions.ACTION_UPDATE_PWD));
                finish();
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
