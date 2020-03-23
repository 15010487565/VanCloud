package com.vgtech.vancloud.signin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.EventBusMsg;
import com.android.volley.VolleyError;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.zxing.zxing.ToastUtil;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.register.country.CountryActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by code on 2016/9/12.
 */
public class SigninPwdNextActivity extends BaseActivity implements HttpListener<String> {
    private EditText etPhone;
    private TextView tvCode;
    private Button btnNext;
    private static final int REQUEST_CHOOSE = 1001;
    //忘记密码
    private static final int CALL_BACK_REGISTER_PWD = 0x000001;
    //注册
    private static final int CALL_BACK_REGISTER_REGISTER = 0x000002;
    @Override
    protected int getContentView() {
        return R.layout.activity_signin_pwd_next;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        int status = getIntent().getIntExtra("status", 0);
        if (status == 1) {//忘记密码
            setTitle(getString(R.string.vancloud_reset_password));
        } else {//注册
            setTitle(getString(R.string.register));
        }


        findViewById(R.id.re_signin_pwd_code).setOnClickListener(this);
        tvCode = (TextView) findViewById(R.id.tv_signin_pwd_code);

        etPhone = (EditText) findViewById(R.id.et_signin_phone);

        etPhone.addTextChangedListener(new VanTextWatcher(etPhone, findViewById(R.id.del_phone)) {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                btnNext.setEnabled(TextUtils.isEmpty(s) ? false : true);
            }
        });
        //下一步
        btnNext = (Button) findViewById(R.id.tv_signin_pwd_next);
        btnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.re_signin_pwd_code: {
                Intent intent = new Intent(this, CountryActivity.class);
                intent.putExtra("style", "company");
                startActivityForResult(intent, REQUEST_CHOOSE);
            }
            break;
            case R.id.tv_signin_pwd_next:

                //取出登陆数据
                String areaCode = tvCode.getText().toString();
                areaCode = TextUtil.formatAreaCode(areaCode);
                String phone = TextUtil.getString(etPhone);
                //校验数据
                if (TextUtil.isEmpty(this, etPhone)) {
                    return;
                }
                if (!TextUtil.isAvailablePhone(this, phone, isChina())) {
                    return;
                }
                showLoadingDialog(this, "", false);
                Map<String, String> params = new HashMap<>();
                if (areaCode.indexOf("+") != -1) {
                    areaCode = areaCode.replaceAll("\\+", "");
                }
                params.put("area_code", areaCode);
                params.put("phone", phone);

                VanCloudApplication app = (VanCloudApplication) getApplication();
                int status = getIntent().getIntExtra("status", 0);
                if (status == 1) {//忘记密码
                    NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.SIGNIN_REGISTER_PWD), params, this);
                    app.getNetworkManager().load(CALL_BACK_REGISTER_PWD, path, this);
                } else {//注册
                    NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.SIGNIN_REGISTER_REGISTER), params, this);
                    app.getNetworkManager().load(CALL_BACK_REGISTER_REGISTER, path, this);
                }
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    public boolean isChina() {
        String areaCode = tvCode.getText().toString();
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
                    tvCode.setText(countryNum);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        switch (callbackId) {
            case CALL_BACK_REGISTER_PWD:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    if (jsonObject != null) {
                        int code = jsonObject.optInt("code");
                        String msg = jsonObject.optString("msg");
                        if (code == 200) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            boolean isExist = data.optBoolean("isExist", false);
                            if (isExist){
                                String phone = TextUtil.getString(etPhone);
                                String codeArea = tvCode.getText().toString().trim();
                                Intent intent = new Intent(this, SigninPwdActivity.class);
                                intent.putExtra("area_code", codeArea);
                                intent.putExtra("phone", phone);
                                startActivity(intent);
                            }else {
                                ToastUtil.toast(this, "用户不存在，请先注册!");
                            }

                        }else {
                            ToastUtil.toast(this, msg);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toast(this, R.string.network_error);
                }
                break;
            case CALL_BACK_REGISTER_REGISTER:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    if (jsonObject != null) {
                        int code = jsonObject.optInt("code");
                        String msg = jsonObject.optString("msg");
                        if (code == 200) {
                            String phone = TextUtil.getString(etPhone);
                            String codeArea = tvCode.getText().toString().trim();
                            Intent intent = new Intent(this, SigninPwdActivity.class);
                            intent.putExtra("area_code", codeArea);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                        } else {
                            ToastUtil.toast(this, msg);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toast(this, R.string.network_error);
                }
                break;
        }
    }

    @Override
    public void onResponse(String response) {
        dismisLoadingDialog();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        dismisLoadingDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMsg event) {
        Class<?> aClass = event.getaClassName();
        Log.e("TAG_关闭", aClass + "=-====" + SigninPwdNextActivity.class);
        Log.e("TAG_关闭", aClass.equals(SigninPwdNextActivity.class)+"");
        if (aClass.equals(SigninPwdNextActivity.class)) {
            finish();
        }

    }
}
