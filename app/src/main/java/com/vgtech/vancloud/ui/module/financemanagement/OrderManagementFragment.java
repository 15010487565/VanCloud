package com.vgtech.vancloud.ui.module.financemanagement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.OrderDetail;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.OrderManagementAdapter;
import com.vgtech.vancloud.ui.base.LazyLoadFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2015/12/29.
 */
public class OrderManagementFragment extends LazyLoadFragment implements HttpListener<String>, PullToRefreshListView.OnRefreshListener2 {

    private static String STATUS = "status";
    private static String TYPE = "type";
    private static String NUM = "num";
    private String n = "12";
    private String nextId = "0";

    PullToRefreshListView pullToRefreshListView;
    public VancloudLoadingLayout loadingLayout;

    private NetworkManager mNetworkManager;
    private final int CALLBACK_ORDERS_MANAGEMENTS = 1;

    OrderManagementAdapter orderManagementAdapter;

    private String payment_status;
    private String order_type;
//    private int tab_num;

    public static OrderManagementFragment create(String paymentStatus) {
        OrderManagementFragment fragment = new OrderManagementFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, paymentStatus);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * @param paymentStatus 订单支付状态
     * @param orderType     订单类型
     * @param tabNum        页码
     * @return
     */
    public static OrderManagementFragment create(String paymentStatus, String orderType, int tabNum) {
        OrderManagementFragment fragment = new OrderManagementFragment();
        Bundle args = new Bundle();
        args.putString(STATUS, paymentStatus);
        args.putString(TYPE, orderType);
        args.putInt(NUM, tabNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        payment_status = getArguments().getString(STATUS);
        order_type = getArguments().getString(TYPE);
//        tab_num = getArguments().getInt(NUM, -1);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.list_layout;
    }

    @Override
    protected void initView(View view) {

        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_list);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);
        orderManagementAdapter = new OrderManagementAdapter(new ArrayList<OrderDetail>(), this);
        pullToRefreshListView.setAdapter(orderManagementAdapter);
        pullToRefreshListView.setOnRefreshListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PasswordFragment.RECEIVER_PAY_SUCCESS);
        getActivity().registerReceiver(mReceiver, intentFilter);

    }

    @Override
    protected void lazyLoad() {
//        if (tab_num > 0) {
        loadingLayout.showLoadingView(pullToRefreshListView, "", true);
        getOrders("0");
//        }
    }

    @Override
    protected void initData() {

//        if (tab_num == 0) {
        loadingLayout.showLoadingView(pullToRefreshListView, "", true);
        getOrders("0");
//        }

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    private void getOrders(String nextid) {

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("status", payment_status);
        params.put("order_type", order_type);
        params.put("n", n);
        params.put("s", nextid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_ORDERS_MANAGEMENTS), params, getActivity());
        mNetworkManager.load(CALLBACK_ORDERS_MANAGEMENTS, path, this, false);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        loadingLayout.dismiss(pullToRefreshListView);
        pullToRefreshListView.onRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            loadingLayout.showErrorView(pullToRefreshListView);
            return;
        }
        switch (callbackId) {
            case CALLBACK_ORDERS_MANAGEMENTS:

                List<OrderDetail> orderDetails = new ArrayList<OrderDetail>();
                String oldeNextId = path.getPostValues().get("s");
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    String id = jsonObject.getString("nextid");
                    if (!TextUtils.isEmpty(id) && !"0".equals(id)) {
                        nextId = id;
                    }
                    orderDetails = JsonDataFactory.getDataArray(OrderDetail.class, jsonObject.getJSONArray("manager_list"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (orderManagementAdapter == null) {
                    pullToRefreshListView.setAdapter(orderManagementAdapter);
                } else {
                    if ("0".equals(oldeNextId)) {
                        orderManagementAdapter.myNotifyDataSetChanged(orderDetails);
                    } else {
                        List<OrderDetail> list = orderManagementAdapter.getList();
                        list.addAll(orderDetails);
                        orderManagementAdapter.myNotifyDataSetChanged(list);
                    }
                }
                if (orderManagementAdapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(pullToRefreshListView, getString(R.string.no_order), true, true);
                    pullToRefreshListView.setVisibility(View.VISIBLE);
                }
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
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        getOrders("0");
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        getOrders(nextId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 200:
                if (resultCode == Activity.RESULT_OK && "pending".equals(payment_status)) {
                    int position = data.getIntExtra("position", -1);
                    if (position >= 0) {
                        orderManagementAdapter.removeOrder(position);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PasswordFragment.RECEIVER_PAY_SUCCESS.equals(action) && "pending".equals(payment_status)) {
                int position = intent.getIntExtra("position", -1);
                if (position >= 0) {
                    orderManagementAdapter.removeOrder(position);
                }
            }
        }
    };
}

