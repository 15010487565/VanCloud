package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.FindPwdStartActivity;
import com.vgtech.vancloud.ui.LoginActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class UpdatePwdActivity extends BaseActivity implements HttpListener<String> {
    private EditText et_password;
    private EditText et_new_password;
    private EditText et_confirm_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.me_setting));
        String style = getIntent().getStringExtra("style");
        if ("personal".equals(style)) {
            View bgTitleBar = findViewById(com.vgtech.common.R.id.bg_titlebar);
            bgTitleBar.setBackgroundColor(Color.parseColor("#faa41d"));
        }
        initView();
    }

    @Override
    protected int getContentView() {
        return R.layout.setting_layout;
    }

    private void initView() {
        initRightTv(getString(R.string.ok));
        findViewById(R.id.btn_forget_pwd).setOnClickListener(this);

        et_password = (EditText) findViewById(R.id.old_password);
        et_password.setTypeface(Typeface.SANS_SERIF);
        et_new_password = (EditText) findViewById(R.id.new_password);
        et_new_password.setTypeface(Typeface.SANS_SERIF);
        et_confirm_password = (EditText) findViewById(R.id.input_new_password1);
        et_confirm_password.setTypeface(Typeface.SANS_SERIF);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                doSubmit();
                break;
            case R.id.btn_forget_pwd:
                Intent intent = new Intent(this, FindPwdStartActivity.class);
                startActivity(intent);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private NetworkManager mNetworkManager;

    private void doSubmit() {
        String pwd1 = et_password.getText().toString();
        String pwd2 = et_new_password.getText().toString();
        String pwd3 = et_confirm_password.getText().toString();
        if (TextUtil.isEmpty(this, et_password, et_new_password, et_confirm_password)) return;
        if (!TextUtil.isAvailablePwd(this, pwd2, pwd3)) return;
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        String username = preferences.getString("username", "");
        String areaCode = preferences.getString("areaCode", "");

        showLoadingDialog(this, "");
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<>();

        params.put("area_code", areaCode);
        params.put("username", username);
        params.put("originalpwd", pwd1);
        params.put("password", pwd2);
        params.put("flag", "0");
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_USER_UPDATEPASSWORD), params, this);
        mNetworkManager.load(1, path, this);
    }

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
//        FileUtils.writeString("UpdatePwdActivity -> 更新密码成功，退出到登录界面！\r\n");
        Intent intent = new Intent(this, LoginActivity.class);
        Utils.clearUserInfo(this);
        startActivity(intent);
        Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
        sendBroadcast(reveiverIntent);
        Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}