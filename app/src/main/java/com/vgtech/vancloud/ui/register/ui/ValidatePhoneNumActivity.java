package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.module.financemanagement.PasswordActivity;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.vancloud.ui.register.RegisterConstants;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.ui.view.CountDownTextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jackson on 2015/11/30.
 * Version : 1
 * Details :
 */
public class ValidatePhoneNumActivity extends ChooseCountryActivity implements HttpListener<String>, View.OnClickListener {

    public static final int FORGET_NOMAL_PASSWORD = 11;
    public static final int FORGET_PAY_PASSWORD = 22;
    private int type;

    private static final int CALLBACK_GET_AREA_CODE = 0;
    private static final int CALLBACK_SUBMIT_AREA_CODE = 1;
    private static final int CALLBACK_GET_CHECK_CODE = 3;
    private CountDownTextView getVerifyCodeCdtv;
    private EditText et_usertel;
    private Button btn_login;
    private EditText et_code;

    private int userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", FORGET_NOMAL_PASSWORD);
        userType = getIntent().getIntExtra("userType", 1);
        initView();
        setListener();
        setTitle(getString(R.string.validate_vercode2));
    }

    private String phone;
    private String area_code_data;

    private void setListener() {
        getVerifyCodeCdtv.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        et_usertel.addTextChangedListener(new VanTextWatcher(et_usertel, findViewById(R.id.del_phone)));
        et_code.addTextChangedListener(new VanTextWatcher(et_code, findViewById(R.id.del_code)));
    }

    private void initView() {
        et_usertel = (EditText) findViewById(R.id.et_usertel);
        btn_login = (Button) findViewById(R.id.btn_login);
        et_code = (EditText) findViewById(R.id.et_code);
        getVerifyCodeCdtv = (CountDownTextView) findViewById(R.id.get_verify_code_cdtv);

        if (userType == PasswordFragment.INDIVIDUALUSER) {
            findViewById(R.id.title_bar).setBackgroundColor(Color.parseColor("#faa41d"));
            getVerifyCodeCdtv.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_rectangle));
            btn_login.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_rectangle));
        }
    }

    private void getCode() {
        area_code_data = getAreaCode();
        phone = et_usertel.getText().toString();
        if (TextUtil.isEmpty(ValidatePhoneNumActivity.this, et_usertel)) return;
        if (!TextUtil.isAvailablePhone(this, phone, isChina())) return;
        getVerifyCodeCdtv.setEnabled(false);
        netGetCode(area_code_data, phone);
    }

    private void submitCode() {
        area_code_data = getAreaCode();
        phone = et_usertel.getText().toString();
        String code = et_code.getText().toString().trim();
        if (TextUtil.isEmpty(ValidatePhoneNumActivity.this, et_usertel, et_code)) return;
        if (!TextUtil.isAvailablePhone(this, phone, isChina())) return;
        netSubmitCode(area_code_data, phone, code);
    }

    private void netSubmitCode(String area_code_data, String username_data, String code) {
        showLoadingDialog(this, "");
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("area_code", area_code_data);
        params.put("username", username_data);
        params.put("validatecode", code);
        params.put("type", "0");
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_CODE_CHECK_VALIDATECODE), params, this);
        mNetworkManager.load(CALLBACK_SUBMIT_AREA_CODE, path, this);
    }


    private NetworkManager mNetworkManager;

    private void netGetCode(String area_code, String username) {
        showLoadingDialog(this, "");
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();

        if (type == FORGET_NOMAL_PASSWORD) {
            params.put("area_code", area_code);
            params.put("username", username);
            NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_CODE_GET_VALIDATECODE), params, this);
            mNetworkManager.load(CALLBACK_GET_AREA_CODE, path, this);
        } else if (type == FORGET_PAY_PASSWORD) {
            params.put("area_code", area_code);
            params.put("mobile", username);
            params.put("user_id", PrfUtils.getUserId(this));
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ACCOUNTS_GET_VALIDATECODE), params, this);
            mNetworkManager.load(CALLBACK_GET_AREA_CODE, path, this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RegisterConstants.REQUEST_UPDATE_PWD:
                if (resultCode == RESULT_OK) {
                    data.putExtra("countryName", getCountryName());
                    setResult(RESULT_OK, data);
                    finish();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        getVerifyCodeCdtv.setEnabled(true);
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            Toast.makeText(ValidatePhoneNumActivity.this, rootData.getMsg(), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (callbackId) {
            case CALLBACK_GET_AREA_CODE:
                if (Constants.DEBUG) {
                    String testYanzhengma = rootData.getJson().toString();
                    Toast.makeText(this, testYanzhengma, Toast.LENGTH_SHORT).show();
                }
                getVerifyCodeCdtv.start();//开始计时
                break;
            case CALLBACK_SUBMIT_AREA_CODE:
                if (type == FORGET_NOMAL_PASSWORD) {
                    Intent intent = new Intent(this, UpdatePasswordActivity.class);
                    intent.putExtra("area_code", area_code_data);
                    intent.putExtra("username", phone);
                    startActivityForResult(intent, RegisterConstants.REQUEST_UPDATE_PWD);
                } else if (type == FORGET_PAY_PASSWORD) {
                    Intent intent = new Intent(this, PasswordActivity.class);
                    intent.putExtra("type", PasswordFragment.FORGET_PASSWORD);
                    intent.putExtra("userType", userType);
                    startActivity(intent);
                    finish();
                }

                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_verify_code_cdtv:
                getCode();
                break;
            case R.id.btn_login:
                submitCode();
                break;
        }
        super.onClick(v);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_validate_phone_num;
    }
}
