package com.vgtech.vancloud.signin;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.zxing.zxing.ToastUtil;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.LoginActivity;
import com.vgtech.vancloud.ui.register.country.CountryActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignInLoginActivity extends BaseActivity implements HttpListener<String> {

    private static final int REQUEST_CHOOSE = 1001;
    private TextView mAreaTv;
    private EditText mEtUsertel;
    private EditText mEtPassword;
    private static final int CALL_BACK_LOGIN = 0x000001;
    @Override
    protected int getContentView() {
        return R.layout.activity_sign_in_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //清空万客登录
        Utils.clearUserInfo(this);
        ApplicationProxy applicationProxy = (ApplicationProxy) this.getApplication();
        applicationProxy.clear();
        String token = PrfUtils.getToken(this);
        Log.e("TAG_个人派币","token="+token);
        String moudle_permissions = PrfUtils.getPrfparams(this, "moudle_permissions");
        Log.e("TAG_个人派币","moudle_permissions="+moudle_permissions);
        String sigin_areaCode = PrfUtils.getPrfparams(this, "sigin_areaCode", "");

        String sigin_mobile = PrfUtils.getPrfparams(this, "sigin_mobile", "");

        //切换区号
        findViewById(R.id.area_code_layout).setOnClickListener(this);
        mAreaTv = (TextView) findViewById(R.id.tv_area_code);
        if (!TextUtils.isEmpty(sigin_areaCode)){
            mAreaTv.setText(sigin_areaCode);
        }
        mEtUsertel = (EditText) findViewById(R.id.et_phone);
        if (!TextUtils.isEmpty(sigin_mobile)){
            mEtUsertel.setText(sigin_mobile);
        }
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mEtPassword.setTypeface(Typeface.SANS_SERIF);
        mEtUsertel.addTextChangedListener(new VanTextWatcher(mEtUsertel, findViewById(R.id.del_phone)));
        mEtPassword.addTextChangedListener(new VanTextWatcher(mEtPassword, findViewById(R.id.del_pwd)));
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
        //注册
        findViewById(R.id.tv_register).setOnClickListener(this);
        //忘记密码
        findViewById(R.id.tv_forget_pwd).setOnClickListener(this);
       //切换到公司登录
        findViewById(R.id.tv_CompanyIn).setOnClickListener(this);
        //登录按钮
        findViewById(R.id.btn_login).setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.area_code_layout: {
                Intent intent = new Intent(this, CountryActivity.class);
                intent.putExtra("style", "company");
                startActivityForResult(intent, REQUEST_CHOOSE);
            }
            break;
            case R.id.btn_login:
                login();
                break;
            case R.id.tv_register:
                startSigninPwdNext(0);
                break;
            case R.id.tv_forget_pwd: {//忘记密码
                startSigninPwdNext(1);
            }
            break;
            case R.id.tv_CompanyIn: {//公司登录
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            break;

            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * 0 注册
     * 1 忘记密码
     * @param status
     */
    private void startSigninPwdNext(int status) {
        Intent intent = new Intent(this, SigninPwdNextActivity.class);
        intent.putExtra("status",status);
        startActivity(intent);
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
        showLoadingDialog(this, this.getString(R.string.loading_login));

        Map<String, String> params = new HashMap<>();
        if (areaCode.indexOf("+") !=-1){
            areaCode = areaCode.replaceAll("\\+","");
        }
        params.put("areaCode", areaCode);
        PrfUtils.savePrfparams(this, "sigin_areaCode",areaCode);
        params.put("mobile", phone);
        PrfUtils.savePrfparams(this, "sigin_mobile",phone);
        params.put("password", MD5.getMD5(passWord));

        VanCloudApplication app = (VanCloudApplication)getApplication();
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.SIGNIN_LOGIN), params, this);
        app.getNetworkManager().load(CALL_BACK_LOGIN, path, this);
    }

    public boolean isChina() {
        String areaCode = mAreaTv.getText().toString();
        areaCode = TextUtil.formatAreaCode(areaCode);
        return TextUtils.equals(areaCode, "86") || TextUtils.equals(areaCode, "+86");
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        switch (callbackId) {
            case CALL_BACK_LOGIN:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    if (jsonObject != null){
                        int code = jsonObject.optInt("code");
                        String msg = jsonObject.optString("msg");
                        if (code == 200){
                            //个人派币登录成功

                            Intent intent = new Intent(this, SigninMainX5Activity.class);
                            JSONObject dataObject = jsonObject.getJSONObject("data");
                            String userId = dataObject.optString("userId");
                            PrfUtils.savePrfparams(this, "sigin_userId",userId);
                            String token = dataObject.optString("token");
                            PrfUtils.savePrfparams(this, "sigin_token",token);

                            startActivity(intent);
                            finish();
                        }else if (code == 1001){//账户不存在
                            new AlertDialog(this).builder().setTitle(getString(R.string.frends_tip))
                                    .setMsg(getString(R.string.signin_register_hint_toast))
                                    .setPositiveButton(getString(R.string.register), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startSigninPwdNext(0);
                                        }
                                    }).setNegativeButton(getString(R.string.new_cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }else {
                            ToastUtil.toast(this,msg);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toast(this,R.string.network_error);
                }
                break;
        }
    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
