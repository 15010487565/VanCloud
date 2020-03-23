package com.vgtech.common.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.adapter.PasswordKeyBoradAdapter;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.PasswordTextfiled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * 输入密码
 * Created by Nick on 2015/8/17.
 */
public class PasswordFragment extends BaseFragment implements HttpListener<String> {


    public static final String USERTYPE = "usertype";
    public static final String TYPE = "type";
    public static final String POSITION = "position";
    public static final String FROM = "from";

    public final static int CREATE_PASSWORD_OF_TYPE = 243;
    public final static int MODIFY_PASSWORD_OF_TYPE = CREATE_PASSWORD_OF_TYPE << 2;
    public final static int CHECK_PASSWORD_OF_TYPE = CREATE_PASSWORD_OF_TYPE << 3;
    public final static int FORGET_PASSWORD = 76;

    private final int COMMIT_PASSWORD = 431;
    private final int CHECK_PASSWORD = 1;
    private final int TO_PAY = 2;

    private int type;

    private GridView gridView;
    private PasswordTextfiled passwordTextFiled;
    private TextView passwordChkTitle;

    private final int INSERT_OLD_PASSWORD = 22;
    private final int INSERT_NEW_PASSWORD = 543;
    private final int INSERT_NEW_PASSWORD_AGAIN = 32;

    private int step;
    private boolean mSafe;

    private List<Character> oldPassword;
    private List<Character> firstPassword;

    private NetworkManager mNetworkManager;

    public String orderId;

    public static final String RECEIVER_PAY_SUCCESS = "RECEIVER_PAY_SUCCESS";
    private int position;

    private boolean fromFinanceManageMent = true;

    private static PasswordCallBackToDo myCallBack;
    /**
     * 公司用户类型
     */
    public static final int COMPANYUSER = 1;
    /**
     * 个人用户类型
     */
    public static final int INDIVIDUALUSER = 2;

    private int userType;//1公司用户，2个人用户

    /**
     * 初始化PasswordFragment
     *
     * @param type
     * @param userType 用户类型（1公司用户，2个人用户）
     * @param back     回调函数
     * @return
     */
    public static PasswordFragment create(int type, int userType, PasswordCallBackToDo back) {
        myCallBack = back;
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putInt(USERTYPE, userType);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * @param type
     * @param fromFinanceManageMent 是否从订单管理界面跳入
     * @return
     */
    public static PasswordFragment create(int type, int userType, boolean fromFinanceManageMent, PasswordCallBackToDo back) {
        myCallBack = back;
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putInt(USERTYPE, userType);
        args.putBoolean(FROM, fromFinanceManageMent);
        fragment.setArguments(args);
        return fragment;
    }

//    public static PasswordFragment create(int type, int pposition) {
//        PasswordFragment fragment = new PasswordFragment();
//        Bundle args = new Bundle();
//        args.putInt(TYPE, type);
//        args.putInt(POSITION, pposition);
//        fragment.setArguments(args);
//        return fragment;
//    }

    public PasswordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userType = getArguments().getInt(USERTYPE, 1);
            type = getArguments().getInt(TYPE);
            position = getArguments().getInt(POSITION, -1);
            fromFinanceManageMent = getArguments().getBoolean(FROM, true);
        }
    }

    @Override
    protected int initLayoutId() {
        return R.layout.password_and_keyboard;
    }


    @Override
    protected void initView(View view) {

        gridView = (GridView) view.findViewById(R.id.grid_view);
        passwordTextFiled = (PasswordTextfiled) view.findViewById(R.id.password_text_filed);
        passwordChkTitle = (TextView) view.findViewById(R.id.password_chk_title);
        passwordTextFiled.setShow(false);
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
                    commit();
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

    @Override
    public void onResume() {
        super.onResume();

        if (type == CREATE_PASSWORD_OF_TYPE) {
            step = INSERT_NEW_PASSWORD;
            passwordChkTitle.setTextColor(Color.rgb(0xf9, 0x0e, 0x1c));
            passwordChkTitle.setText(getString(R.string.first_password));

        } else if (type == MODIFY_PASSWORD_OF_TYPE) {
            step = INSERT_OLD_PASSWORD;
            passwordChkTitle.setTextColor(Color.rgb(0x43, 0x44, 0x45));
            passwordChkTitle.setText(getString(R.string.insert_this_password));
        } else if (type == CHECK_PASSWORD_OF_TYPE) {
            step = INSERT_OLD_PASSWORD;
            passwordChkTitle.setTextColor(Color.rgb(0x43, 0x44, 0x45));
            passwordChkTitle.setText(getString(R.string.insert_pay_password_please));
        } else if (type == FORGET_PASSWORD) {
            type = MODIFY_PASSWORD_OF_TYPE;
            step = INSERT_NEW_PASSWORD;
            passwordChkTitle.setTextColor(Color.rgb(0x43, 0x44, 0x45));
            passwordChkTitle.setText(getString(R.string.insert_new_password));
        }
    }

    private void commit() {
        if (type == CREATE_PASSWORD_OF_TYPE) {
            if (step == INSERT_NEW_PASSWORD) {
                insertNewPassword();
            } else if (step == INSERT_NEW_PASSWORD_AGAIN) {
                //TODO submit
                insertPasswordAgain();
            }
        } else if (type == MODIFY_PASSWORD_OF_TYPE) {

            if (step == INSERT_OLD_PASSWORD) {
                oldPassword = passwordTextFiled.getData();

                StringBuilder sb = new StringBuilder();
                for (Character c : oldPassword)
                    sb.append(c);
                checkPassword(sb.toString());
                step = INSERT_NEW_PASSWORD;
//                passwordTextFiled.clearAll();
            } else if (step == INSERT_NEW_PASSWORD) {
                insertNewPassword();
            } else if (step == INSERT_NEW_PASSWORD_AGAIN) {
                //TODO submit
                insertPasswordAgain();
            }
        } else if (type == CHECK_PASSWORD_OF_TYPE) {
            oldPassword = passwordTextFiled.getData();

            StringBuilder sb = new StringBuilder();
            for (Character c : oldPassword)
                sb.append(c);
            pay(sb.toString());
        }
    }

    private void insertNewPassword() {
        passwordChkTitle.setText(getString(R.string.insert_new_password_again));
        firstPassword = new Stack<Character>();
        firstPassword.addAll(passwordTextFiled.getData());
        passwordTextFiled.postDelayed(new Runnable() {
            @Override
            public void run() {
                passwordTextFiled.clearAll();
            }
        }, 100);
//        passwordTextFiled.clearAll();
        step = INSERT_NEW_PASSWORD_AGAIN;
    }

    private void insertPasswordAgain() {
        passwordChkTitle.setText(getString(R.string.insert_new_password_again));
        List<Character> againPassword = passwordTextFiled.getData();

        StringBuilder sb = new StringBuilder();
        boolean isSame = true;
        for (int i = 0; i < againPassword.size(); i++) {

            if (againPassword.get(i) == firstPassword.get(i)) {
                isSame = true;
                sb.append(againPassword.get(i));
            } else {
                isSame = false;
                break;
            }
        }
//        passwordTextFiled.clearAll();

        if (isSame) {
            if (type == CREATE_PASSWORD_OF_TYPE) {
                if (!fromFinanceManageMent) {
                    showLoadingDialog(getActivity(), "");
                }
                commitPassword(sb.toString());
                if (fromFinanceManageMent && userType == COMPANYUSER) {
                    Intent intent = new Intent();
                    intent.setAction("com.vgtech.vancloud.intent.action.FinanceManageMentActivity");
                    startActivity(intent);
                    getActivity().finish();
                }
            } else if (type == MODIFY_PASSWORD_OF_TYPE) {
                commitPassword(sb.toString());
            }
        } else {

            passwordTextFiled.clearAll();
            if (oldPassword != null)
                oldPassword.clear();
            if (firstPassword != null)
                firstPassword.clear();
            step = INSERT_NEW_PASSWORD;
            passwordChkTitle.setText(getString(R.string.insert_new_password_please));

            dialog(getString(R.string.twice_password_is_not_same), null);
        }
    }

    //提交密码
    private void commitPassword(String password) {
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        String url;
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("payment_password", MD5.getMD5(password));
        if (userType == INDIVIDUALUSER) {
            url = URLAddr.URL_ACCOUNTS_SETTINGS_PERSONAL_PASSWORD;
        } else {
            params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
            url = URLAddr.URL_ACCOUNTS_SETTINGS_PASSWORD;
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), url), params, getActivity());
        mNetworkManager.load(COMMIT_PASSWORD, path, this);
    }

    //验证密码
    private void checkPassword(String password) {
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        String url;
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("payment_password", MD5.getMD5(password));
        if (userType == INDIVIDUALUSER) {
            url = URLAddr.URL_ACCOUNTS_VALIDATING_PERSONAL_PASSWORD;
        } else {
            params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
            url = URLAddr.URL_ACCOUNTS_VALIDATING_PASSWORD;
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), url), params, getActivity());
        mNetworkManager.load(CHECK_PASSWORD, path, this);
    }

    //支付订单
    private void pay(String password) {

        if (orderId == null) {
            dialog(getString(R.string.can_pay_for_order), null);
            return;
        }

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        if (userType == COMPANYUSER)
            params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        else{
            params.put("tenant_id", "0");
            params.put("pay_channel", "APP");
            params.put("pay_method", "vancloudpay");
            params.put("user_type", "personal");
            params.put("third_party", "account");
        }
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("order_info_id", orderId);
        params.put("payment_password", MD5.getMD5(password));


        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_ORDERS_PAYMENT), params, getActivity());
        mNetworkManager.load(TO_PAY, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        mSafe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!mSafe) {
            step = INSERT_OLD_PASSWORD;
            if (type == MODIFY_PASSWORD_OF_TYPE)
                passwordChkTitle.setText(getActivity().getString(R.string.insert_this_password));
            else if (type == CHECK_PASSWORD_OF_TYPE)
                passwordChkTitle.setText(getActivity().getString(R.string.insert_pay_password_please));
//            dialog(, null);
            if (rootData.code == 1000)
                dialog(rootData.msg, null);
            else
                showTowButtonDialog(rootData.msg);
            passwordTextFiled.clearAll();
            return;
        }
        switch (callbackId) {
            case COMMIT_PASSWORD:
                if (type == FORGET_PASSWORD) {
                    getActivity().finish();
                    return;
                }
//                if (fromFinanceManageMent) {
//                    Intent intent = new Intent();
//                    if (userType == INDIVIDUALUSER) {
//                        intent.setAction("com.vgtech.vancloud.intent.action.MyWalletActivity");
//                    } else {
//                        intent.setAction("com.vgtech.vancloud.intent.action.FinanceManageMentActivity");
//                    }
//                    startActivity(intent);
//                    getActivity().finish();
//                }
                try {
                    Activity activity = getActivity();
                    if (myCallBack != null) {
                        myCallBack.callBackToDo();
                    } else {
                        activity.finish();
                    }
//                    if (activity instanceof FinanceManageMentActivity)
//                        ((FinanceManageMentActivity) getActivity()).initInfoView("");
//                    else if (activity instanceof PayActivity) {
//                        ((PayActivity) getActivity()).initPasswordView();
//                    } else
//                        activity.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CHECK_PASSWORD:

                if (type == MODIFY_PASSWORD_OF_TYPE) {
                    try {
                        if (rootData.result) {
                            step = INSERT_NEW_PASSWORD;
                            passwordChkTitle.setText(getString(R.string.insert_new_password));
                        }
                        passwordTextFiled.clearAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TO_PAY:
                //TODO 支付成功发送广播
//                Intent broadcIntent = new Intent(RECEIVER_PAY_SUCCESS);
//                broadcIntent.putExtra(POSITION, position);
//                getActivity().sendBroadcast(broadcIntent);

                passwordTextFiled.setEnable(false);
                passwordTextFiled.clearAll();

                Intent intent = new Intent();
                intent.putExtra("paymentmethod", 1);//支付类型：余额支付。
                intent.putExtra("result", rootData.result);//支付状态：true成功。
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    @Override
    public void onResponse(String response) {

    }

    public void setPasswordEnable(boolean enable) {
        passwordTextFiled.setEnable(enable);
    }

    private void dialog(String str, View.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog(getActivity()).builder();
        dialog.setCanceledOnTouchOutside(false);
        if (listener != null)
            dialog
                    .setMsg(str)
                    .setPositiveButton(getString(R.string.ok), listener).show();
        else
            dialog
                    .setMsg(str)
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
    }

    private void showTowButtonDialog(String str) {
        AlertDialog dialog = new AlertDialog(getActivity()).builder()
                .setMsg(str)
                .setPositiveButton(getString(R.string.forget_password), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent toForgetPassword = new Intent();
                        toForgetPassword.setAction("com.vgtech.vancloud.intent.action.ValidatePhoneNumActivity");
                        toForgetPassword.putExtra("type", 22);
                        toForgetPassword.putExtra("userType", userType);
                        startActivity(toForgetPassword);
                        type = CHECK_PASSWORD_OF_TYPE;
                    }
                }).setNegativeButton(getString(R.string.cancel_dialog), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    public interface PasswordCallBackToDo {
        void callBackToDo();
    }
}
