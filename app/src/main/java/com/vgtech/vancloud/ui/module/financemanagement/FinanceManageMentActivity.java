package com.vgtech.vancloud.ui.module.financemanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.BalanceInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.common.PrfUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Duke on 2015/12/25.
 */
public class FinanceManageMentActivity extends BaseActivity implements HttpListener<String> {

    private ImageButton rightButton;

    private TextView loadingMessageView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;

    private RelativeLayout infoLayout;

    private static final int CALLBACK_BALANCEINFO = 1;
    private NetworkManager mNetworkManager;

    BalanceInfo balanceInfo;


    @Override
    protected int getContentView() {
        return R.layout.finance_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.lable_finance));
        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMessageView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
        infoLayout = (RelativeLayout) findViewById(R.id.fragment_layout);

        getBalanceInfo();


    }


    public void getBalanceInfo() {

        showLoadingView();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ACCOUNTS_BALANCE), params, this);
        mNetworkManager.load(CALLBACK_BALANCEINFO, path, this);
    }

    public void initInfoView(String json) {
        rightButton = (ImageButton) findViewById(R.id.btn_right);
        rightButton.setImageDrawable(getResources().getDrawable(R.drawable.order_topright_btn));
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setOnClickListener(this);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_layout, FinanceManageMentFragment.create(json));
        transaction.commit();
    }

    private void initPasswordView() {

        setTitle(getString(R.string.set_paypassword));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_layout, PasswordFragment.create(PasswordFragment.CREATE_PASSWORD_OF_TYPE,PasswordFragment.COMPANYUSER, new PasswordFragment.PasswordCallBackToDo() {
            @Override
            public void callBackToDo() {
                initInfoView("");
            }
        }));
        transaction.commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_right:
                showPopupWindow(v);
                break;
            case R.id.item_one:
                //TODO 交易记录
                popupWindow.dismiss();
                Intent toTradeList = new Intent(this, TradeListActivity.class);
                toTradeList.putExtra("balance", balanceInfo.balance);
                startActivity(toTradeList);
                break;
            case R.id.item_two:
                //TODO 支付密码
                popupWindow.dismiss();
                Intent toModify = new Intent(this, PasswordActivity.class);
                toModify.putExtra("type", PasswordFragment.MODIFY_PASSWORD_OF_TYPE);
                startActivity(toModify);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private PopupWindow popupWindow;
    private TextView itemOne;
    private TextView itemTwo;

    private void showPopupWindow(View v) {

        if (popupWindow == null) {
            View view = View.inflate(this, R.layout.popup_dialog, null);
            itemOne = (TextView) view.findViewById(R.id.item_one);
            itemOne.setText(getString(R.string.trade_detail));
            itemOne.setOnClickListener(this);
            itemTwo = (TextView) view.findViewById(R.id.item_two);
            itemTwo.setText(getString(R.string.password_change));
            itemTwo.setOnClickListener(this);
            popupWindow = new PopupWindow(view, convertDipOrPx(this, 160),
                    ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
            popupWindow.setFocusable(true);// 使其聚集
            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
            popupWindow.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.bg_pop_chat_select));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.update();
        }

        popupWindow.showAsDropDown(v, 0 - convertDipOrPx(this, 120), 0);

    }

    // dip--px
    public static int convertDipOrPx(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public void showLoadingView() {

        infoLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMessageView.setVisibility(View.VISIBLE);
        loadingMessageView.setText(getString(R.string.dataloading));

    }

    public void hideLoadingView() {
        loadingLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingMessageView.setVisibility(View.GONE);
        loadingMessageView.setText(null);
        infoLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        hideLoadingView();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            loadingLayout.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.GONE);
            loadingMessageView.setVisibility(View.VISIBLE);
            loadingMessageView.setText(getString(R.string.loading_error));
            infoLayout.setVisibility(View.GONE);
            return;
        }
        switch (callbackId) {

            case CALLBACK_BALANCEINFO:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    balanceInfo = JsonDataFactory.getData(BalanceInfo.class, resutObject);
                    if (balanceInfo.is_first_enter) {
                        initPasswordView();
                    } else {
                        initInfoView(resutObject.toString());
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
}
