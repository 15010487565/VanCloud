package com.vgtech.vancloud.ui.module.financemanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.OrderDetail;
import com.vgtech.common.api.OrderResume;
import com.vgtech.common.api.ResumeBuyBean;
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
import com.vgtech.vancloud.ui.adapter.ResumeBuyAdapter;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nick on 2015/12/29.
 */
public class OrderDetailActivity extends BaseActivity implements HttpListener<String> {

    private TextView orderIdView;
    private TextView orderCreateTimeView;
    private TextView orderPayTimeView;
    private TextView moneyView;
    private TextView stateTypeView;
    private LinearLayout buttonLayout;
    private TextView orderDescriptionView;
    private TextView orderTotalView;

    private NetworkManager mNetworkManager;
    private static final int CALLBACK_ORDERS_DETAIL = 1;
    private static final int CALLBACK_ORDERS_CANCEL = 2;

    private ResumeBuyAdapter resumeBuyAdapter;
    private PullToRefreshListView listView;

    String json;
    private OrderDetail orderDetail;
    private boolean backRefresh = false;
    private int position;


    @Override
    protected int getContentView() {
        return R.layout.order_detail_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.order_detail_title));
        Intent intent = getIntent();
        json = intent.getStringExtra("json");
        String infoID = intent.getStringExtra("infoid");
        position = intent.getIntExtra("position", -1);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PasswordFragment.RECEIVER_PAY_SUCCESS);
        registerReceiver(mReceiver, intentFilter);

        initView();
        try {
            if (TextUtils.isEmpty(json) && !TextUtils.isEmpty(infoID)) {
                getOrderDetail(infoID);
            } else {
                orderDetail = JsonDataFactory.getData(OrderDetail.class, new JSONObject(json));
                setViewData(orderDetail);
                getOrderDetail(orderDetail.order_info_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancel_click:
                //TODO 取消订单
                new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                        .setMsg(getString(R.string.cancel_order))
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelOrder(orderDetail.order_info_id);
                            }
                        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.pay_click:
                //TODO 付款
                Intent intent = new Intent(this, PayActivity.class);
//                intent.putExtra("order_data", orderDetail.getJson().toString());

                intent.putExtra("order_describe", orderDetail.order_description);
                intent.putExtra("order_id", orderDetail.order_info_id);
                intent.putExtra("order_total", orderDetail.amount);
                intent.putExtra("order_number", orderDetail.order_info_id);

                intent.putExtra("from", 1);
                startActivity(intent);
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    public void initView() {

        orderIdView = (TextView) findViewById(R.id.order_id);
        orderCreateTimeView = (TextView) findViewById(R.id.order_create_time);
        orderPayTimeView = (TextView) findViewById(R.id.order_pay_time);
        moneyView = (TextView) findViewById(R.id.money);
        stateTypeView = (TextView) findViewById(R.id.state_type);
        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        orderDescriptionView = (TextView) findViewById(R.id.order_description);
        orderTotalView = (TextView) findViewById(R.id.order_total);

        listView = (PullToRefreshListView) findViewById(R.id.listview);
        resumeBuyAdapter = new ResumeBuyAdapter(this, new ArrayList<ResumeBuyBean>());
        listView.setAdapter(resumeBuyAdapter);
        listView.setMode(PullToRefreshBase.Mode.DISABLED);

        findViewById(R.id.cancel_click).setOnClickListener(this);
        findViewById(R.id.pay_click).setOnClickListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                ResumeBuyBean resumeBuyBean = (ResumeBuyBean) resumeBuyAdapter.getItem(position - 1);
//                Intent intent = new Intent(OrderDetailActivity.this, ResumePreviewActivity.class);
//                intent.putExtra("type", 3);
//                intent.putExtra("storeid", resumeBuyBean.resume_id);
//                startActivity(intent);

            }
        });

    }


    private void getOrderDetail(String orderid) {

        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("order_info_id", orderid);
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("n", "100");
        params.put("s", "0");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_DETAIL), params, this);
        mNetworkManager.load(CALLBACK_ORDERS_DETAIL, path, this, false);

    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_ORDERS_DETAIL:
                try {
                    orderDetail = JsonDataFactory.getData(OrderDetail.class, rootData.getJson().getJSONObject("data"));
                    setViewData(orderDetail);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_ORDERS_CANCEL:
                Toast.makeText(this, getString(R.string.cancel_success), Toast.LENGTH_SHORT).show();
                chaneOrderStatus("canceled");
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private void setViewData(OrderDetail orderDetail) {

        orderIdView.setText(Utils.format(getString(R.string.order_info_id), orderDetail.order_info_id));
        orderCreateTimeView.setText(Utils.format(getString(R.string.order_create_time), orderDetail.creator_time));
//        orderPayTimeView.setDettailText(Utils.format(getString(R.string.order_pay_time), orderDetail.payment_time));
        moneyView.setText(Utils.format(getString(R.string.order_amount_01), orderDetail.amount));
        if ("canceled".equals(orderDetail.payment_status)) {
            buttonLayout.setVisibility(View.GONE);
            stateTypeView.setText(getString(R.string.order_canceled));
            stateTypeView.setTextColor(getResources().getColor(R.color.order_grey));
            orderPayTimeView.setText(Utils.format(getString(R.string.order_cancele_time), orderDetail.cancel_time));
        } else if ("paid".equals(orderDetail.payment_status)) {
            buttonLayout.setVisibility(View.GONE);
            stateTypeView.setText(getString(R.string.order_paid));
            stateTypeView.setTextColor(getResources().getColor(R.color.bg_title));
            orderPayTimeView.setText(Utils.format(getString(R.string.order_pay_time), orderDetail.payment_time));
        } else {
            buttonLayout.setVisibility(View.VISIBLE);
            stateTypeView.setText(getString(R.string.order_pending));
            stateTypeView.setTextColor(getResources().getColor(R.color.bg_title));
            orderPayTimeView.setText(Utils.format(getString(R.string.order_pay_time), ""));
        }
        orderDescriptionView.setText(orderDetail.order_description);
        orderTotalView.setText(Utils.format(getString(R.string.order_total_01), orderDetail.amount));


        List<ResumeBuyBean> list = new ArrayList<>();
        List<OrderResume> orderResumes = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(orderDetail.getJson().getString("resume_list")))
                list = JsonDataFactory.getDataArray(ResumeBuyBean.class, orderDetail.getJson().getJSONArray("resume_list"));
//                orderResumes = JsonDataFactory.getDataArray(OrderResume.class, orderDetail.getJson().getJSONArray("resume_list"));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for (OrderResume orderResume : orderResumes) {
//            list.add(new ResumeBuyBean(orderResume));
//        }

        if (resumeBuyAdapter == null) {
            resumeBuyAdapter = new ResumeBuyAdapter(this, list);
            listView.setAdapter(resumeBuyAdapter);
        } else {
            resumeBuyAdapter.myNotifyDataSetChanged(list);
        }

    }


    public void cancelOrder(String orderInfoId) {

        showLoadingDialog(this, getString(R.string.prompt_info_02));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("order_info_id", orderInfoId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_CANCEL), params, this);
        mNetworkManager.load(CALLBACK_ORDERS_CANCEL, path, this, false);
    }

    public void chaneOrderStatus(String orderStatus) {
        buttonLayout.setVisibility(View.GONE);
        backRefresh = true;
        if ("canceled".equals(orderStatus)) {
            stateTypeView.setText(getString(R.string.order_canceled));
            stateTypeView.setTextColor(getResources().getColor(R.color.order_grey));
        } else {
            stateTypeView.setText(getString(R.string.order_paid));
            stateTypeView.setTextColor(getResources().getColor(R.color.bg_title));
        }
        orderDetail.payment_status = orderStatus;
        try {
            orderDetail.getJson().put("payment_status", orderStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getOrderDetail(orderDetail.order_info_id);
    }

    @Override
    public void finish() {

        if (backRefresh) {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            setResult(RESULT_OK, intent);
            backRefresh = false;
        }
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        super.finish();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PasswordFragment.RECEIVER_PAY_SUCCESS.equals(action)) {
                chaneOrderStatus("paid");
            }
        }
    };



}
