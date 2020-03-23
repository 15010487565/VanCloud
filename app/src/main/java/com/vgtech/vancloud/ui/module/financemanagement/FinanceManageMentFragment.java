package com.vgtech.vancloud.ui.module.financemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
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
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Duke on 2016/1/7.
 */
public class FinanceManageMentFragment extends BaseFragment implements View.OnClickListener, HttpListener<String> {

    private TextView balanceTextView;
    private static String INFO = "info";
    private String jsonText;
    private BalanceInfo balanceInfo;
    private final int CALLBACK_BALANCEINFO = 1;
    private NetworkManager mNetworkManager;

    private boolean isFirst = true;

    public static FinanceManageMentFragment create(String balanceJson) {
        FinanceManageMentFragment fragment = new FinanceManageMentFragment();
        Bundle args = new Bundle();
        args.putString(INFO, balanceJson);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jsonText = getArguments().getString(INFO);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.finance_fragment_layout;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.order_pay).setOnClickListener(this);
        view.findViewById(R.id.order_management).setOnClickListener(this);
        balanceTextView = (TextView) view.findViewById(R.id.balance);

    }

    @Override
    protected void initData() {

//        try {
//            if (!TextUtil.isEmpty(jsonText)) {
//                balanceInfo = JsonDataFactory.getData(BalanceInfo.class, new JSONObject(jsonText));
//                balanceTextView.setDettailText(Utils.priceFormat(balanceInfo.balance));
//            } else {
//                getBalanceInfo();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.order_pay:
                //TODO 订单支付
            {
                Intent intent = new Intent(getActivity(), ToPayListActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.order_management:
                Intent intent = new Intent(getActivity(), OrderListActivity.class);
                startActivity(intent);
                //TODO 订单管理
                break;
            default:
                break;

        }

    }

    public void getBalanceInfo() {
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_ACCOUNTS_BALANCE), params, getActivity());
        mNetworkManager.load(CALLBACK_BALANCEINFO, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {

            case CALLBACK_BALANCEINFO:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    balanceInfo = JsonDataFactory.getData(BalanceInfo.class, resutObject);
                    balanceTextView.setText(Utils.priceFormat01(balanceInfo.balance));
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

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (isFirst && !TextUtil.isEmpty(jsonText)) {
                balanceInfo = JsonDataFactory.getData(BalanceInfo.class, new JSONObject(jsonText));
                balanceTextView.setText(Utils.priceFormat01(balanceInfo.balance));
                isFirst = false;
            } else
                getBalanceInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
