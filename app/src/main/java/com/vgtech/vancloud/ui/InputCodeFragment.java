package com.vgtech.vancloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.adapter.PasswordKeyBoradAdapter;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.PasswordTextfiled;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.register.RegisterCompanyActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by code on 2016/9/12.
 */
public class InputCodeFragment extends com.vgtech.common.ui.BaseFragment implements HttpListener<String> {

    private GridView gridView;
    private PasswordTextfiled passwordTextFiled;
    public static final String TYPE = "type";
    public static final String ARECODE = "areaCode";
    public static final String PHONENUM = "phoneNum";
    public static final String USERPWD = "userPwd";
    public static final String CODE = "code";
    private int type; //1、注册，2、找回密码，3更换登陆手机号
    private String areCode;//区号
    private String phoneNum;//手机号
    private String userPwd;//注册公司密码
    private String mCode;
    private NetworkManager mNetworkManager;
    private final int CALLBACK_CHANGE_PHONENO = 1;

    public static InputCodeFragment create(int type, String arecode, String phonenum, String userPwd, String code) {
        InputCodeFragment fragment = new InputCodeFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(ARECODE, arecode);
        args.putString(PHONENUM, phonenum);
        args.putString(USERPWD, userPwd);
        args.putString(CODE, code);
        fragment.setArguments(args);
        return fragment;
    }

    public InputCodeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(TYPE);
            areCode = getArguments().getString("areaCode");
            phoneNum = getArguments().getString("phoneNum");
            userPwd = getArguments().getString("userPwd");
            mCode = getArguments().getString(CODE);
        }
    }

    @Override
    protected int initLayoutId() {
        return com.vgtech.common.R.layout.password_and_keyboard;
    }


    @Override
    protected void initView(View view) {
        gridView = (GridView) view.findViewById(com.vgtech.common.R.id.grid_view);
        passwordTextFiled = (PasswordTextfiled) view.findViewById(com.vgtech.common.R.id.password_text_filed);
        passwordTextFiled.setShow(true);
    }

    @Override
    protected void initData() {
        gridView.setAdapter(new PasswordKeyBoradAdapter(getActivity(), new PasswordKeyBoradAdapter.PressListener() {
            @Override
            public void pressKey(Character character) {

                int size;
                if (character != '-')
                    size = passwordTextFiled.pushChar(character);
                else
                    size = passwordTextFiled.pop();

                if (size == 6) {
                    if (!TextUtils.isEmpty(mCode)) {
                        List<Character> firstPassword = new Stack<Character>();
                        firstPassword.addAll(passwordTextFiled.getData());
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < firstPassword.size(); i++) {
                            sb.append(firstPassword.get(i));
                        }
                        if (mCode.contains(sb.toString())) {
                            commit();
                        } else {
                            passwordTextFiled.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    passwordTextFiled.clearAll();
                                }
                            }, 100);
                            Toast.makeText(getActivity(), R.string.check_code_error, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        commit();
                    }
                }
            }
        }));
    }

    @Override
    protected void initEvent() {
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    private void commit() {
        insertNewPassword();
    }

    private void insertNewPassword() {
        List<Character> firstPassword = new Stack<Character>();
        firstPassword.addAll(passwordTextFiled.getData());
        passwordTextFiled.postDelayed(new Runnable() {
            @Override
            public void run() {
                passwordTextFiled.clearAll();
            }
        }, 100);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < firstPassword.size(); i++) {
            sb.append(firstPassword.get(i));
        }
        switch (type) {
            case 1:
                Intent intent = new Intent(getActivity(), RegisterCompanyActivity.class);
                intent.putExtra("code_value", sb.toString());
                intent.putExtra("phoneNum", phoneNum);
                intent.putExtra("userPwd", userPwd);
                intent.putExtra("areaCode", areCode);
                startActivityForResult(intent, 100);
                break;
            case 2:
                Intent intent1 = new Intent(getActivity(), FindPwdActivity.class);
                intent1.putExtra("code_value", sb.toString());
                intent1.putExtra("phoneNum", phoneNum);
                intent1.putExtra("areaCode", areCode);
                startActivityForResult(intent1, 100);
                break;

            case 3:
                changePhoneno(sb.toString());
                break;
        }
    }

    private void changePhoneno(String code) {
        showLoadingDialog(getActivity(), getString(R.string.dataloading));
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("area_code", TextUtil.formatAreaCode(areCode));
        params.put("validate_code", code);
        params.put("mobile", phoneNum);
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(getActivity(), URLAddr.URL_USER_CHANGE_PHONENO), params, getActivity());
        mNetworkManager.load(CALLBACK_CHANGE_PHONENO, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_CHANGE_PHONENO:
                Toast.makeText(getActivity(), getString(R.string.chane_number_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("phone", phoneNum);
                getActivity().setResult(-2, intent);
                getActivity().finish();
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
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }
}
