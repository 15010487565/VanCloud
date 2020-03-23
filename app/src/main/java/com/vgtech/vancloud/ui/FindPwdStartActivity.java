package com.vgtech.vancloud.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.register.country.CountryActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by code on 2016/9/12.
 */
public class FindPwdStartActivity extends BaseActivity implements HttpListener<String> {
    private EditText mEtUsertel;
    private TextView mAreaTv;
    private String areCode = "+86";
    private int fromType;//1.来自更改手机号
    private TextView phoneNnmberView;
    private static final int REQUEST_CHOOSE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fromType = getIntent().getIntExtra("from", -1);

        phoneNnmberView = (TextView) findViewById(R.id.now_phone_unmber);
        if (fromType == 1) {
            setTitle(getString(R.string.change_phone));
            phoneNnmberView.setText(getString(R.string.now_phone_number, PrfUtils.getUserPhone(this)));
            phoneNnmberView.setVisibility(View.VISIBLE);
        } else {
            setTitle(getString(R.string.find_pwd));
            phoneNnmberView.setVisibility(View.INVISIBLE);
        }
        mEtUsertel = (EditText) findViewById(R.id.et_phone);
        mEtUsertel.addTextChangedListener(new VanTextWatcher(mEtUsertel, findViewById(R.id.del_phone)));
        mAreaTv = (TextView) findViewById(R.id.tv_chosed_country);
        findViewById(R.id.set_pwd_line).setVisibility(View.GONE);
        findViewById(R.id.set_pwd_layout).setVisibility(View.GONE);
        findViewById(R.id.rala_chose_country).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);

        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        String username = preferences.getString("username", "");
        if (!TextUtil.isEmpty(username)) {
            mEtUsertel.setText(username);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rala_chose_country: {
                Intent intent = new Intent(this, CountryActivity.class);
                intent.putExtra("style", "company");
                startActivityForResult(intent, REQUEST_CHOOSE);
            }
            break;
            case R.id.btn_next:
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
        showLoadingDialog(this, getString(R.string.prompt_info_02));
        String phoneNum = mEtUsertel.getText().toString().trim();
        String areaCode = TextUtil.formatAreaCode(areCode);
        if (!TextUtil.isAvailablePhone(this, phoneNum, TextUtil.isChina(areaCode))) {
            return;
        }
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("area_code", areaCode);
        params.put("username", phoneNum);
        if (fromType == 1)
            params.put("type", "uncheck");
        else
            params.put("type", "check");
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_CODE_GET_ACCESS_VALIDATECODE), params, this);
        mNetworkManager.load(CALL_BACK_GET_CODE, path, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String countryName = bundle.getString("countryName");
                    String countryNum = bundle.getString("countryNumber");
                    areCode = countryNum;
                    mAreaTv.setText(countryNum + "  " + countryName);
                }
                break;
            case 100:
                if (resultCode == -2) {

                    if (fromType == 1) {
                        String newPhone = data.getStringExtra("phone");
                        if (!TextUtils.isEmpty(newPhone)) {
                            Intent intent = new Intent();
                            intent.putExtra("newphone", mEtUsertel.getText().toString());
                            setResult(RESULT_OK, intent);
                        }
                    } else {
                        setResult(RESULT_OK);
                    }
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_findpwd;
    }

    private String mCode;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            int code = rootData.getCode();
            String msg = rootData.getMsg();
            if (1000 == code) { //验证码已发送，请耐心等候
                if (getString(R.string.get_access_validatecode_prompt1).equals(msg) || getString(R.string.get_access_validatecode_prompt2).equals(msg))
                    nextInputCodeAction();
            }
            return;
        }
        switch (callbackId) {
            case CALL_BACK_GET_CODE:
//                mCode = rootData.getJson().toString();
                if (Constants.DEBUG) {
//                    Toast.makeText(this, mCode, Toast.LENGTH_SHORT).show();
                }
                nextInputCodeAction();
                break;

        }
    }

    private void nextInputCodeAction() {
//        Intent intent = new Intent(FindPwdStartActivity.this, InputCodeActivity.class);
        Intent intent = new Intent(FindPwdStartActivity.this, FindPasswordActivity.class);
        intent.putExtra("phone_value", areCode + " " + mEtUsertel.getText().toString());
        intent.putExtra("fromType", fromType);
        if (fromType == 1) {
            intent.putExtra("from", "3");
        } else {
            intent.putExtra("from", "2");
        }

        intent.putExtra("areaCode", areCode);
//        intent.putExtra("code", mCode);
        intent.putExtra("phoneNum", mEtUsertel.getText().toString());
        startActivityForResult(intent, 100);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }


}
