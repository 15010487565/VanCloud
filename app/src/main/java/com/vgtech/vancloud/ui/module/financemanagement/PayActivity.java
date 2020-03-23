package com.vgtech.vancloud.ui.module.financemanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.BalanceInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 输入密码页面
 * Created by Nick on 2015/12/25.
 */
public class PayActivity extends BaseActivity implements HttpListener<String> {

    private static final int CALLBACK_BALANCEINFO = 1;
    private NetworkManager mNetworkManager;

    private TextView balance;
    private TextView needMoney;

    private LinearLayout loading;

    //    private OrderDetail data;
    private double orderCost;

    private PasswordFragment fragment;
    /**
     * 订单
     */
    private String theOrderId;
    /**
     * 订单总价
     */
    private String ordreTotal;

    private int userType;

    @Override
    protected int getContentView() {
        return R.layout.edit_pay_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.pay_the_orders_title));
        theOrderId = getIntent().getStringExtra("order_id");
        ordreTotal = getIntent().getStringExtra("order_total");
        userType = getIntent().getIntExtra("usertype", 1);
        if (userType == PasswordFragment.INDIVIDUALUSER)
            findViewById(R.id.title).setBackgroundColor(Color.parseColor("#faa41d"));
        if (TextUtils.isEmpty(theOrderId)) {
            dialog(getString(R.string.not_using_order), null);
            finish();
        }
        getBalanceInfo();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add:
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    public void initView() {
//        orderInfoHeader = (LinearLayout) findViewById(R.id.order_info_header);
//        orderInfoHeader.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //点击查看详情暂时禁用
//
//                if (fromType == 1) {
//                    finish();
//                } else {
//                    Intent intent = new Intent(PayActivity.this, OrderDetailActivity.class);
////                    intent.putExtra("json", data.getJson().toString());
//                    intent.putExtra("infoid", theOrderId);
//                    startActivity(intent);
//                }
//            }
//        });
        needMoney = (TextView) findViewById(R.id.amount);
        balance = (TextView) findViewById(R.id.balance);
        balance.setVisibility(View.GONE);
        initData();
//        initPasswordView();
    }

    private void initData() {
        needMoney.setText(String.format(getString(R.string.payment_amount), ordreTotal));
    }

    public void initPasswordView() {

        balance.setVisibility(View.VISIBLE);
        needMoney.setVisibility(View.VISIBLE);
        setTitle(getString(R.string.pay_for_order_title));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        fragment = PasswordFragment.create(PasswordFragment.CHECK_PASSWORD_OF_TYPE, userType, new PasswordFragment.PasswordCallBackToDo() {
            @Override
            public void callBackToDo() {
                initPasswordView();
            }
        });
        fragment.orderId = theOrderId;
        findViewById(R.id.fragment_layout).setVisibility(View.VISIBLE);
        transaction.replace(R.id.fragment_layout, fragment);
        transaction.commitAllowingStateLoss();
    }

    private void initFirstPasswordView() {

        setTitle(getString(R.string.set_paypassword));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        findViewById(R.id.fragment_layout).setVisibility(View.VISIBLE);
        transaction.replace(R.id.fragment_layout, PasswordFragment.create(PasswordFragment.CREATE_PASSWORD_OF_TYPE, userType, false, new PasswordFragment.PasswordCallBackToDo() {
            @Override
            public void callBackToDo() {
                initPasswordView();
            }
        }));
        transaction.commitAllowingStateLoss();
    }


    public void getBalanceInfo() {

        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ACCOUNTS_BALANCE), params, this);
        mNetworkManager.load(CALLBACK_BALANCEINFO, path, this);
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        findViewById(R.id.top_view).setVisibility(View.VISIBLE);
        findViewById(R.id.progress_view).setVisibility(View.VISIBLE);
        findViewById(R.id.loadding_msg).setVisibility(View.VISIBLE);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        findViewById(R.id.loading).setVisibility(View.GONE);
        findViewById(R.id.top_view).setVisibility(View.GONE);
        findViewById(R.id.progress_view).setVisibility(View.GONE);
        findViewById(R.id.loadding_msg).setVisibility(View.GONE);

        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            findViewById(R.id.load_err).setVisibility(View.VISIBLE);
            return;
        }

        initView();
        switch (callbackId) {

            case CALLBACK_BALANCEINFO:
                BalanceInfo balanceInfo;
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    balanceInfo = JsonDataFactory.getData(BalanceInfo.class, resutObject);
                    if (userType == PasswordFragment.COMPANYUSER)
                        balance.setText(String.format(getString(R.string.company_balance), balanceInfo.balance));
                    else
                        balance.setText(String.format(getString(R.string.uese_balance), balanceInfo.balance));
//                   double balancePrice = Double.parseDouble(balanceInfo.balance);
//                    if (orderCost > balancePrice) {
//                        balance.setTextColor(Color.RED);
//                        fragment.setPasswordEnable(false);
//                        dialog(getString(R.string.balance_not_enough), null);
//                    } else
//                        fragment.setPasswordEnable(true);
                    if (balanceInfo.is_first_enter) {
                        initFirstPasswordView();
                        balance.setVisibility(View.GONE);
                        needMoney.setVisibility(View.GONE);
                    } else {
                        initPasswordView();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private void dialog(String str, View.OnClickListener listener) {
        if (listener != null)
            new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                    .setMsg(str)
                    .setPositiveButton(getString(R.string.ok), listener).show();
        else
            new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                    .setMsg(str)
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
    }

}
