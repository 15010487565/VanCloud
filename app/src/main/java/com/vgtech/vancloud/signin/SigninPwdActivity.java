package com.vgtech.vancloud.signin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
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
import com.vgtech.common.utils.MD5;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.register.country.CountryActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by code on 2016/9/12.
 */
public class SigninPwdActivity extends BaseActivity implements HttpListener<String> {
    public TextView tvSendCode;
    private Handler mHandler;
    private Button btnSubmit;
    public boolean mCanSendVercode;
    private EditText etCode,etPwd;
    private EditText etConfirmPwd;
    private static final int REQUEST_CHOOSE = 1001;
    String areaCode;
    String phone;

    private static final int CALL_BACK_PWD = 0x000001;
    private static final int CALL_BACK_VALIDATECODE = 0x000002;
    @Override
    protected int getContentView() {
        return R.layout.activity_signin_pwd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.vancloud_reset_password));
        Intent intent = getIntent();
        areaCode = intent.getStringExtra("area_code");
        phone = intent.getStringExtra("phone");
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < phone.length(); i++) {
            //先在余数后添加空格
            if (i == 2){
                newString.append(phone.charAt(i));
                newString.append("\t");
            }else if (i > 2 && (i-2) % 4 == 0 && i != phone.length()) {

                newString.append(phone.charAt(i));
                newString.append("\t");
            } else {
                newString.append(phone.charAt(i));
            }
        }
        TextView tvPhone = (TextView) findViewById(R.id.tv_SigniinTopHint_Phone);
        tvPhone.setText(areaCode+"\t"+newString);

        etCode = (EditText) findViewById(R.id.et_signin_phonecode);

        tvSendCode = (TextView) findViewById(R.id.send_ver_code);
        tvSendCode.setOnClickListener(this);

        etPwd = (EditText) findViewById(R.id.et_signin_pwd);
        etPwd.setTypeface(Typeface.SANS_SERIF);
        etPwd.addTextChangedListener(new VanTextWatcher(etPwd, findViewById(R.id.del_signin_pwd)));

        etConfirmPwd = (EditText) findViewById(R.id.et_sigin_pwd_confirm);
        etConfirmPwd.setTypeface(Typeface.SANS_SERIF);
        etConfirmPwd.addTextChangedListener(new VanTextWatcher(etConfirmPwd, findViewById(R.id.del_siginpwd_confirm)));

        //提交
        btnSubmit = (Button) findViewById(R.id.tv_signin_pwd_submit);
        btnSubmit.setOnClickListener(this);
        //获取验证码
        getValidatecode();

    }
    //获取验证码
    private void getValidatecode() {

        showLoadingDialog(this, "", false);
        Map<String, String> params = new HashMap<>();
        if (areaCode.indexOf("+") !=-1){
            areaCode = areaCode.replaceAll("\\+","");
        }
        params.put("area_code", areaCode);
        params.put("phone", phone);

        VanCloudApplication app = (VanCloudApplication) getApplication();
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.SIGNIN_GETVALIDATECODE), params, this);
        app.getNetworkManager().load(CALL_BACK_VALIDATECODE, path, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.re_signin_pwd_code: {//区号
                Intent intent = new Intent(this, CountryActivity.class);
                intent.putExtra("style", "company");
                startActivityForResult(intent, REQUEST_CHOOSE);
            }
            break;
            case R.id.send_ver_code://获取验证码
                getValidatecode();
                break;

            case R.id.tv_signin_pwd_submit:
                //验证码
                String phoneCode = etCode.getText().toString().trim();
                if (TextUtil.isEmpty(phoneCode)) {
                    ToastUtil.toast(this,"验证码不能为空！");
                    return;
                }
                //密码
                String pwd = etPwd.getText().toString().trim();
                if (TextUtil.isEmpty(pwd)) {
                    ToastUtil.toast(this,"密码不能为空！");
                    return;
                }
                //再次输入
                String pwdConfirm = etConfirmPwd.getText().toString().trim();
                if (TextUtil.isEmpty(pwdConfirm)) {
                    ToastUtil.toast(this,"确认密码不能为空！");
                    return;
                }
                if (!pwd.equals(pwdConfirm)) {
                    ToastUtil.toast(this,"两次输入的密码不相同！");
                    return;
                }
                showLoadingDialog(this, "", false);

                Map<String, String> params = new HashMap<>();
                if (areaCode.indexOf("+") !=-1){
                    areaCode = areaCode.replaceAll("\\+","");
                }
                params.put("area_code", areaCode);
                params.put("phone", phone);
                params.put("password", MD5.getMD5(pwd));
                params.put("validatecode", phoneCode);

                VanCloudApplication app = (VanCloudApplication) getApplication();
                NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.SIGNIN_RESETPWD), params, this);
                app.getNetworkManager().load(CALL_BACK_PWD, path, this);
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        try {
            JSONObject jsonObject = rootData.getJson();
            int code = jsonObject.optInt("code");
            String msg = jsonObject.optString("msg");
            switch (callbackId) {
                case CALL_BACK_PWD:
                    if (code == 200) {
    //                    String phone = TextUtil.getString(etPhone);
    //                    String codeArea = tvCode.getText().toString().trim();
    //                    Intent intent = new Intent(this, SigninPwdActivity.class);
    //                    intent.putExtra("area_code", codeArea);
    //                    intent.putExtra("phone", phone);
    //                    startActivity(intent);
                        EventBusMsg messageEvent = new EventBusMsg();
                        messageEvent.setaClassName(SigninPwdNextActivity.class);
                        EventBus.getDefault().post(messageEvent);
                        finish();
                    }
                    ToastUtil.toast(this, msg);
                    break;
                case CALL_BACK_VALIDATECODE:
                    if (code == 200) {
                        mHandler = new LoopHandler(this);
                        mHandler.sendEmptyMessage(60);
                    } else {
                        ToastUtil.toast(this, msg);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.toast(this,R.string.network_error);
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
        if (mHandler !=null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

    }

    private class LoopHandler extends Handler {

        private final WeakReference<SigninPwdActivity> mWeakRefContext;

        public LoopHandler(SigninPwdActivity context) {
            mWeakRefContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mWeakRefContext != null && mWeakRefContext.get() != null) {
                SigninPwdActivity ctx = mWeakRefContext.get();
                if (msg.what > 0) {
                    ctx.tvSendCode.setText(String.format(ctx.getString(R.string.x_sencend_resend), msg.what));
                    ctx.tvSendCode.setEnabled(false);
                    sendEmptyMessageDelayed(--msg.what,1000);
                    ctx.mCanSendVercode = false;
                } else {
                    ctx.tvSendCode.setTextColor(ContextCompat.getColor(SigninPwdActivity.this, com.vgtech.common.R.color.comment_blue));
                    ctx.tvSendCode.setText(ctx.getString(R.string.re_send));
                    ctx.tvSendCode.setEnabled(true);
                    ctx.mCanSendVercode = true;
                }
            }
        }
    }
}
