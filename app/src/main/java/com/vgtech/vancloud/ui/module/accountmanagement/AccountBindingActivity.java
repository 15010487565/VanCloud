package com.vgtech.vancloud.ui.module.accountmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Account;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Duke on 2016/7/21.
 */
public class AccountBindingActivity extends BaseActivity implements HttpListener<String> {


    private static final int CALLBACK_BIND = 1;
    private static final int CALLBACK_UNBIND = 2;
    private Account account;
    ImageView typeView;
    private EditText etMemberName;
    private EditText etUserName;
    private EditText etPassword;
    private LinearLayout memberNameLayout;

    private Button button;

    private boolean backRefresh = false;


    @Override
    protected int getContentView() {
        return R.layout.account_binding_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String json = intent.getStringExtra("source");


        typeView = (ImageView) findViewById(R.id.iv_type);
        etMemberName = (EditText) findViewById(R.id.et_member_name);
        etUserName = (EditText) findViewById(R.id.et_user_name);
        etPassword = (EditText) findViewById(R.id.et_password);
        memberNameLayout = (LinearLayout) findViewById(R.id.member_name_layout);

        try {
            if (!TextUtil.isEmpty(json)) {
                account = JsonDataFactory.getData(Account.class, new JSONObject(json));
                button = (Button) findViewById(R.id.btn_binding);
                button.setOnClickListener(this);

                if ("51job".equals(account.source)) {
                    memberNameLayout.setVisibility(View.VISIBLE);
                    typeView.setImageResource(R.mipmap.icon_51);
                    setTitle(getString(R.string.vancloud_zhaopin));
                    if ("Y".equals(account.is_bind)) {
                        etUserName.setText(account.user_name);
                        etMemberName.setText(account.leaguer_name);
                        button.setText(getString(R.string.personal_btn_un_bangding));
                    } else {
                        button.setText(getString(R.string.personal_btn_bangding));
                    }

                } else {
                    memberNameLayout.setVisibility(View.GONE);
                    typeView.setImageResource(R.mipmap.icon_zhilian);
                    setTitle(getString(R.string.vancloud_zhilian));
                    if ("Y".equals(account.is_bind)) {
                        etUserName.setText(account.user_name);
                        button.setText(getString(R.string.personal_btn_un_bangding));
                    } else {
                        button.setText(getString(R.string.personal_btn_bangding));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_binding:
                if ("Y".equals(account.is_bind)) {
                    unBindAccount(account.job_tenant_account_id);
                } else {
                    verificationData();
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    public void verificationData() {
        String memberName = etMemberName.getText().toString();
        String userName = etUserName.getText().toString();
        String password = etPassword.getText().toString();
        if ("51job".equals(account.source)) {
            if (TextUtils.isEmpty(memberName)) {
                Toast.makeText(this, getString(R.string.vancloud_bind_membername_prompt), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(this, getString(R.string.vancloud_bind_username_prompt), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtil.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, getString(R.string.vancloud_bind_password_prompt), Toast.LENGTH_SHORT).show();
                return;
            }

        } else {
            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(this, getString(R.string.vancloud_bind_user_name), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtil.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, getString(R.string.vancloud_bind_password_prompt02), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        bindAccount(memberName, userName, password);
    }

    public void bindAccount(String membername, String username, String password) {
        showLoadingDialog(this, getString(R.string.prompt_info_02));
        NetworkManager mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        if (!TextUtil.isEmpty(membername))
            params.put("company_code", membername);
        params.put("user_name", username);
        params.put("password", password);
        params.put("source", account.source);
        params.put("bind_type", "vancloud");
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_BIND_ACCOUNT), params, this);
        mNetworkManager.load(CALLBACK_BIND, path, this);
    }

    public void unBindAccount(String account_id) {
        showLoadingDialog(this, getString(R.string.prompt_info_02));
        NetworkManager mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("account_id", account_id);
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_UNBIND_ACCOUNT), params, this);
        mNetworkManager.load(CALLBACK_UNBIND, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_BIND:
                setResult(RESULT_OK);
                finish();
                break;

            case CALLBACK_UNBIND:

                Toast.makeText(this, getString(R.string.unbundling_prompt), Toast.LENGTH_SHORT).show();
                backRefresh = true;
                button.setText(getString(R.string.personal_btn_bangding));
                account.is_bind = "N";
                etMemberName.setText("");
                etUserName.setText("");
                etPassword.setText("");
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
    public void finish() {
        if (backRefresh) {
            setResult(RESULT_OK);
            backRefresh = false;
        }
        super.finish();
    }
}
