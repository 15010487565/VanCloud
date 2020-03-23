package com.vgtech.vancloud.ui.register;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.DictSelectActivity;
import com.vgtech.common.utils.MD5;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.presenter.LoginPresenter;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adm01 on 2016/5/31.
 */
public class RegisterCompanyActivity extends BaseActivity implements HttpListener<String> {
    private String code;//验证码
    private String areaCode;//区号
    private String phoneNum;//手机号
    private String userPwd;//注册公司密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_company));
        Intent intent = getIntent();
        code = intent.getStringExtra("code_value");
        phoneNum = intent.getStringExtra("phoneNum");
        userPwd = intent.getStringExtra("userPwd");
        areaCode = intent.getStringExtra("areaCode");
        findViewById(R.id.btn_pe_company_type).setOnClickListener(this);
        findViewById(R.id.btn_pe_company_scale).setOnClickListener(this);
        findViewById(R.id.btn_pe_hangye).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
    }

    private String mHangyeId, mComTypeId, mGuimoId;
    private String mHangyeName, mComTypeName, mGuimoName;
    public static final int HANGYE = 2;
    public static final int GONGSIXINGZHI = 4;
    public static final int GUIMO = 5;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pe_company_type: {
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", getString(R.string.vancloud_select_company_nature));
                intent.putExtra("style", "company");
                intent.putExtra("id", mComTypeId);
                intent.setData(Uri.parse(URLAddr.URL_RESUME_COMPANYTYPE));
                startActivityForResult(intent, GONGSIXINGZHI);
            }
            break;
            case R.id.btn_pe_company_scale: {
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", getString(R.string.vancloud_select_company_size));
                intent.putExtra("style", "company");
                intent.putExtra("id", mGuimoId);
                intent.setData(Uri.parse(URLAddr.URL_RESUME_COSIZE));
                startActivityForResult(intent, GUIMO);
            }
            break;
            case R.id.btn_pe_hangye: {
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", getString(R.string.vancloud_select_industry));
                intent.putExtra("id", mHangyeId);
                intent.putExtra("style", "company");
                intent.putExtra("type", DictSelectActivity.DICT_TITLE);
                intent.setData(Uri.parse(URLAddr.URL_INDUSTRY));
                startActivityForResult(intent, HANGYE);
            }
            break;
            case R.id.btn_next:
                doRegisterCompany();
                break;
            default:
                super.onClick(v);
                break;
        }
    }
    private void doRegisterCompany() {
        Map<String, String> params = new HashMap<>();
        String url = "";
        url = URLAddr.URL_REGISTER_TENANT;
        params.put("area_code", TextUtil.formatAreaCode(areaCode));
        params.put("mobile", phoneNum);
        TextView companyNameTv = (TextView) findViewById(R.id.tv_company_name);
        String companyName = companyNameTv.getText().toString();
        if (TextUtils.isEmpty(companyName)) {
            Toast.makeText(this, companyNameTv.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mComTypeName)) {
            TextView nameTv = (TextView) findViewById(R.id.tv_company_type);
            Toast.makeText(this, nameTv.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mGuimoName)) {
            TextView nameTv = (TextView) findViewById(R.id.tv_company_scale);
            Toast.makeText(this, nameTv.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mHangyeName)) {
            TextView nameTv = (TextView) findViewById(R.id.tv_hangye);
            Toast.makeText(this, nameTv.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        TextView contNameTv = (TextView) findViewById(R.id.tv_cont_name);
        String conName = contNameTv.getText().toString();
        if (TextUtils.isEmpty(conName)) {
            Toast.makeText(this, contNameTv.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        TextView pushMobileTv = (TextView) findViewById(R.id.tv_push_mobile);
        String pushMobile = pushMobileTv.getText().toString();
        showLoadingDialog(this, getString(R.string.loading_login));
        VanCloudApplication app = (VanCloudApplication) getApplication();
        NetworkManager mNetworkManager = app.getNetworkManager();
        params.put("password", MD5.getMD5(userPwd));
        params.put("validate_code", code);
        params.put("tenant_name", companyName);
        params.put("tenant_nature", mComTypeName);
        params.put("tenant_scale", mGuimoName);
        params.put("tenant_industry", mHangyeName);
        params.put("contact_name", conName);
        params.put("referee_code", pushMobile);
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, url), params, this);
        mNetworkManager.load(1, path, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String id = data.getStringExtra("id");
            String name = data.getStringExtra("name");
            String referCode = data.getStringExtra("referCode");
            switch (requestCode) {
                case HANGYE: {
                    mHangyeId = id;
                    mHangyeName = name;
                    TextView nameTv = (TextView) findViewById(R.id.tv_hangye);
                    nameTv.setText(name);
                    nameTv.setTag(referCode);
                }
                break;
                case GONGSIXINGZHI: {
                    mComTypeId = id;
                    mComTypeName = name;
                    TextView nameTv = (TextView) findViewById(R.id.tv_company_type);
                    nameTv.setText(name);
                    nameTv.setTag(referCode);
                }
                break;
                case GUIMO: {
                    mGuimoId = id;
                    mGuimoName = name;
                    TextView nameTv = (TextView) findViewById(R.id.tv_company_scale);
                    nameTv.setText(name);
                    nameTv.setTag(referCode);
                }
                break;
            }
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case 1:
                PrfUtils.savePrfparams(this,"password",userPwd);
                LoginPresenter loginPresenter = new LoginPresenter(this, controller);
                loginPresenter.processLoginResult(path, rootData);
                sendBroadcast(new Intent(MainActivity.RECEIVER_MAIN_FINISH));
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Actions.ACTION_SIGIN_END));//关闭注册打开的页面。
                finish();
                break;
        }
    }

    @Inject
    Controller controller;

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_register_company;
    }
}
