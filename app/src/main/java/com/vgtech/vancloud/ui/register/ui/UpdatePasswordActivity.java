package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.ui.register.RegisterConstants;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.utils.MD5;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jackson on 2015/11/30.
 * Version : 1
 * Details :
 */
public class UpdatePasswordActivity extends BaseActivity implements HttpListener<String> {

    private EditText et_pwd1;
    private EditText et_pwd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
        setTitle(getString(R.string.set_login_pwd));
    }

    private void initListener() {
        btn_set_pwd.setOnClickListener(this);
        et_pwd1.addTextChangedListener(new VanTextWatcher(et_pwd1, findViewById(R.id.del_pwd1)));
        et_pwd2.addTextChangedListener(new VanTextWatcher(et_pwd2, findViewById(R.id.del_pwd2)));
    }

    private String area_code_data;
    private String username_data;

    private void initData() {
        Intent data = getIntent();
        area_code_data = data.getStringExtra("area_code");
        username_data = data.getStringExtra("username");
        TextView tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_phone.setText(area_code_data + " " + username_data);
    }

    Button btn_set_pwd;
    String pwd2;

    private void initView() {
        btn_set_pwd = (Button) findViewById(R.id.btn_set_pwd);
        et_pwd1 = (EditText) findViewById(R.id.et_pwd1);
        et_pwd2 = (EditText) findViewById(R.id.et_pwd2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_pwd:
                updatePwd();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void updatePwd() {
        String pwd1 = et_pwd1.getText().toString();
        pwd2 = et_pwd2.getText().toString();
        if (!TextUtil.isAvailablePwd(UpdatePasswordActivity.this, pwd1, pwd2)) return;
        netUpdatePwd(MD5.getMD5(pwd2));
    }

    private NetworkManager mNetworkManager;

    private void netUpdatePwd(String pwdNew) {
        showLoadingDialog(this, "");
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("area_code", area_code_data);
        params.put("username", username_data);
        params.put("password", pwdNew);
        params.put("flag", "1");
        params.put("originalpwd", "");//后台要求这么传，不传就报错
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_REGISTER_UPDATEPWD), params, this);
        mNetworkManager.load(RegisterConstants.INTENT_TYPE_FIND_PASSWORD, path, this);
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
        switch (callbackId) {
            case RegisterConstants.INTENT_TYPE_FIND_PASSWORD:
                Toast.makeText(this, R.string.toast_set_pwd_ok, Toast.LENGTH_SHORT).show();
               /* Intent intent = new Intent(RegisteredLoginActivity.RECEIVER_SET_USER_NAME);
                intent.putExtra("username",username_data);
                sendBroadcast(intent);*/
                Intent intent = new Intent();
                intent.putExtra("username", username_data);
                intent.putExtra("area_code", area_code_data);
                setResult(RESULT_OK, intent);
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

    @Override
    protected int getContentView() {
        return R.layout.activity_update_password;
    }


}
