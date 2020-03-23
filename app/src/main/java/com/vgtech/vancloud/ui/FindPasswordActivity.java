package com.vgtech.vancloud.ui;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangshaofang on 2016/5/24.
 */
public class FindPasswordActivity extends BaseActivity implements HttpListener<String> {
    private EditText mEtPassword;
    private EditText mEdConfirmPassword;
    private String phoneNum;//手机号
    private String areaCode;//区号
    private EditText mEtVercode;
    public TextView mTvSendVercode;
    private Handler mHandler;
    public boolean mCanSendVercode;
    private int fromType;

    public class LoopHandler extends Handler {

        private final WeakReference<FindPasswordActivity> mWeakRefContext;

        public LoopHandler(FindPasswordActivity context) {
            mWeakRefContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mWeakRefContext != null && mWeakRefContext.get() != null) {
                FindPasswordActivity ctx = mWeakRefContext.get();
                if (msg.what > 0) {
                    ctx.mTvSendVercode.setText(String.format(ctx.getString(R.string.x_sencend_resend), msg.what));
                    sendEmptyMessageDelayed(--msg.what,1000);
                    ctx.mCanSendVercode = false;
                } else {
                    ctx.mTvSendVercode.setTextColor(ContextCompat.getColor(FindPasswordActivity.this,R.color.comment_blue));
                    ctx.mTvSendVercode.setText(ctx.getString(R.string.re_send));
                    ctx.mCanSendVercode = true;
                }
            }
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_find_password;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.find_pwd));
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("phoneNum");
        areaCode = intent.getStringExtra("areaCode");
        fromType = intent.getIntExtra("from", -1);

        mHandler = new LoopHandler(this);
        mEtVercode = (EditText) findViewById(R.id.et_ver_code);
        mTvSendVercode = (TextView) findViewById(R.id.send_ver_code);
        mTvSendVercode.setOnClickListener(this);
        mHandler.sendEmptyMessage(60);

        mEtPassword = (EditText) findViewById(R.id.et_password);
        mEtPassword.setTypeface(Typeface.SANS_SERIF);
        mEtPassword.addTextChangedListener(new VanTextWatcher(mEtPassword, findViewById(R.id.del_pwd)));
        mEdConfirmPassword = (EditText) findViewById(R.id.et_password_confirm);
        mEdConfirmPassword.setTypeface(Typeface.SANS_SERIF);
        mEdConfirmPassword.addTextChangedListener(new VanTextWatcher(mEdConfirmPassword, findViewById(R.id.del_pwd_confirm)));
        findViewById(R.id.btn_reset_pwd).setOnClickListener(this);

        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_pwd:
                netResetPwd();
                break;
            case R.id.send_ver_code:
                if (mCanSendVercode)
                    sendVerCode();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private static final int CALL_BACK_RESET_PWD = 1001;
    private static final int CALL_BACK_SEND_VERCODE = 1002;

    /**
     * 发送验证码
     */
    private void sendVerCode() {
        Map<String, String> params = new HashMap<>();
        areaCode = TextUtil.formatAreaCode(areaCode);
        params.put("area_code", areaCode);
        params.put("username", phoneNum);
        if (fromType == 1)
            params.put("type", "uncheck");
        else
            params.put("type", "check");
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_CODE_GET_ACCESS_VALIDATECODE), params, this);
        mNetworkManager.load(CALL_BACK_SEND_VERCODE, path, this);
    }

    private void netResetPwd() {
        String verCode = mEtVercode.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();
        String confirmpassword = mEdConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(verCode)){
            showToast(getString(R.string.please_input_verify_code));
            return;
        }
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
        Map<String, String> params = new HashMap<>();
        params.put("area_code", TextUtil.formatAreaCode(areaCode));
        params.put("validatecode", verCode);
        params.put("username", phoneNum);
        params.put("password", password);
        params.put("flag", "1");
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_REGISTER_UPDATEPWD), params, this);
        mNetworkManager.load(CALL_BACK_RESET_PWD, path, this);
    }

    private NetworkManager mNetworkManager;
    public static final String CLEARN_TOP_ACT = "clearn_top_act";


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
//                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Actions.ACTION_UPDATE_PWD));
//                finish();
//                FileUtils.writeString("FinsPasswordActivity -> 重置密码成功，退出到登录界面！\r\n");
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("clearn_top_act",true);
                startActivity(intent);
                break;
            case CALL_BACK_SEND_VERCODE:
                Toast.makeText(this, getString(R.string.get_access_validatecode_prompt1), Toast.LENGTH_SHORT).show();
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
