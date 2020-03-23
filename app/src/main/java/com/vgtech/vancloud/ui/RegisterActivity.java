package com.vgtech.vancloud.ui;


import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.vgtech.common.Constants;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.DictSelectActivity;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.presenter.LoginPresenter;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.register.country.CountryActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.view.CountDownTextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangshaofang on 2016/5/24.
 */
public class RegisterActivity extends BaseActivity implements HttpListener<String> {
    private EditText mEtUsertel;
    private EditText mEtUserpwd;
    private TextView mAreaTv;
    private String areCode = "+86";
    private EditText mEdCode;
    private CountDownTextView getVerifyCodeCdtv;
    private String mHangyeId, mComTypeId, mGuimoId;
    private String mHangyeName, mComTypeName, mGuimoName;
    private boolean isUserExist = false;//用户是否存在
    public static final int HANGYE = 2;
    public static final int GONGSIXINGZHI = 4;
    public static final int GUIMO = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vancloud_company_registration));
        mEtUsertel = (EditText) findViewById(R.id.et_phone);
        mEtUserpwd = (EditText) findViewById(R.id.et_pwd);
        mEtUserpwd.setTypeface(Typeface.SANS_SERIF);
        mEdCode = (EditText) findViewById(R.id.et_code);
        getVerifyCodeCdtv = (CountDownTextView) findViewById(R.id.get_verify_code_cdtv);
        getVerifyCodeCdtv.setOnClickListener(this);
        mEtUsertel.addTextChangedListener(new VanTextWatcher(mEtUsertel, findViewById(R.id.del_phone)));
        mEtUserpwd.addTextChangedListener(new VanTextWatcher(mEtUserpwd, findViewById(R.id.del_pwd)));
        mEdCode.addTextChangedListener(new VanTextWatcher(mEdCode, findViewById(R.id.del_code)));
        mAreaTv = (TextView) findViewById(R.id.tv_chosed_country);
        findViewById(R.id.rala_chose_country).setOnClickListener(this);
        findViewById(R.id.btn_pe_company_type).setOnClickListener(this);
        findViewById(R.id.btn_pe_company_scale).setOnClickListener(this);
        findViewById(R.id.btn_pe_hangye).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
    }

    private static final int REQUEST_CHOOSE = 1001;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rala_chose_country: {
                Intent intent = new Intent(this, CountryActivity.class);
                intent.putExtra("style", "company");
                startActivityForResult(intent, REQUEST_CHOOSE);
            }
            break;
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
            case R.id.get_verify_code_cdtv:
                netGetCode();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private static final int CALL_BACK_GET_CODE = 1002;
    private NetworkManager mNetworkManager;

    private void netGetCode() {
        String phoneNum = mEtUsertel.getText().toString().trim();
        if (!TextUtil.isAvailablePhone(this, phoneNum, true)) {
            return;
        }
        String areaCode = TextUtil.formatAreaCode(areCode);
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("area_code", areaCode);
        params.put("username", phoneNum);
        params.put("type", "check");
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_CODE_GET_VALIDATECODE), params, this);
        mNetworkManager.load(CALL_BACK_GET_CODE, path, this);
        getVerifyCodeCdtv.setEnabled(false);
    }

    private void doRegisterCompany() {
        Map<String, String> params = new HashMap<>();
        String url = "";
        url = URLAddr.URL_REGISTER_TENANT;
        String phoneNum = mEtUsertel.getText().toString().trim();
        if (!TextUtil.isAvailablePhone(this, phoneNum, true)) {
            return;
        }
        String code = mEdCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            showToast(getString(R.string.vancloud_input_verification_code));
            return;
        }
        if (!isUserExist) {
            String pwd = mEtUserpwd.getText().toString().trim();
            if (TextUtils.isEmpty(pwd)) {
                showToast(getString(R.string.set_start_pwd));
                return;
            }
        }
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
        params.put("area_code", TextUtil.formatAreaCode(areCode));
        params.put("mobile", mEtUsertel.getText().toString());
        if (!isUserExist) {
            params.put("password", MD5.getMD5(mEtUserpwd.getText().toString()));
        }
        params.put("validate_code", mEdCode.getText().toString());
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
                case REQUEST_CHOOSE:
                    Bundle bundle = data.getExtras();
                    String countryName = bundle.getString("countryName");
                    String countryNum = bundle.getString("countryNumber");
                    areCode = countryNum;
                    mAreaTv.setText(countryNum + "  " + countryName);
                    break;
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_register;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        getVerifyCodeCdtv.setEnabled(true);
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_GET_CODE:
                if (Constants.DEBUG) {
                    String testYanzhengma = rootData.getJson().toString();
                    Toast.makeText(this, testYanzhengma, Toast.LENGTH_SHORT).show();
                }
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONObject resultObject = jsonObject.getJSONObject("data");
                    String tmpExist = resultObject.getString("user_exist");
                    if ("true".equals(tmpExist)) {
                        isUserExist = true;
                        findViewById(R.id.set_pwd_layout).setVisibility(View.GONE);
                    } else {
                        isUserExist = false;
                        findViewById(R.id.set_pwd_layout).setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getVerifyCodeCdtv.start();//开始计时
                break;
            case 1:
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
}
