package com.vgtech.vancloud.ui.module.financemanagement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.OrderDetail;
import com.vgtech.common.api.OrderDetailInfo;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Refund;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.OrderDetailAdapter;
import com.vgtech.vancloud.ui.module.resume.ResumeDetail;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新版订单详情
 * Created by Duke on 2016/3/18.
 */
public class NewOrderDetailActivity extends BaseActivity implements HttpListener<String>, AdapterView.OnItemClickListener {

    private TextView orderNumberView;
    private TextView orderDescriptionView;
    private TextView creatorTimeView;
    private TextView paymentTimeView;
    private TextView paymentStatusNameView;
    private TextView payerView;
    private TextView amountView;
    private NoScrollListview listview;

    private NetworkManager mNetworkManager;
    private static final int CALLBACK_ORDERS_DETAIL = 1;
    private static final int CALLBACK_RESUME_DETAIL = 2;

    private OrderDetail orderDetail;

    private OrderDetailAdapter orderDetailAdapter;
    private ScrollView showInfoView;
    private VancloudLoadingLayout loadingView;

    private boolean backRefresh = false;
    private int position;

    private boolean isFromPay = false;

    private RelativeLayout payLayout;
    private TextView orderTypeView;
    private TextView priceView;

    private TextView refundInfoView;
    private RelativeLayout refundLayout;
    private TextView refundAmountView;
    private TextView refundTimeView;
    private TextView refundNoteView;

    private String infoID;
    private String resourseID;

    private boolean ifShowPayButton;

    private TextView timeInfoView;

    @Override
    protected int getContentView() {
        return R.layout.new_order_detail_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.order_detail_title));

        Intent intent = getIntent();
//        json = intent.getStringExtra("json");
        infoID = intent.getStringExtra("infoid");
        position = intent.getIntExtra("position", -1);
        isFromPay = intent.getBooleanExtra("isfrompay", false);
        ifShowPayButton = intent.getBooleanExtra("ifshowpaybutton", true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PasswordFragment.RECEIVER_PAY_SUCCESS);
        registerReceiver(mReceiver, intentFilter);

        initViews();
        getOrderDetail(infoID);
    }

    public void initViews() {
        orderNumberView = (TextView) findViewById(R.id.order_number_tv);
        orderDescriptionView = (TextView) findViewById(R.id.order_description_tv);
        creatorTimeView = (TextView) findViewById(R.id.creator_time_tv);
        paymentTimeView = (TextView) findViewById(R.id.payment_time_tv);
        paymentStatusNameView = (TextView) findViewById(R.id.payment_status_name_tv);
        payerView = (TextView) findViewById(R.id.payer_tv);
        amountView = (TextView) findViewById(R.id.amount_tv);
        listview = (NoScrollListview) findViewById(R.id.listview);
        payLayout = (RelativeLayout) findViewById(R.id.pay_layout);
        orderTypeView = (TextView) findViewById(R.id.order_type_tv);
        priceView = (TextView) findViewById(R.id.price_tv);

        refundInfoView = (TextView) findViewById(R.id.refund_info);
        refundLayout = (RelativeLayout) findViewById(R.id.refund_layout);
        refundAmountView = (TextView) findViewById(R.id.refund_amount_tv);
        refundTimeView = (TextView) findViewById(R.id.refund_time_tv);
        refundNoteView = (TextView) findViewById(R.id.refund_note_tv);

        timeInfoView = (TextView) findViewById(R.id.tv_05);

        orderDetailAdapter = new OrderDetailAdapter(this, new ArrayList<OrderDetailInfo>(), false, "");
        listview.setFocusable(false);
        listview.setAdapter(orderDetailAdapter);
        listview.setItemClick(true);
        listview.setOnItemClickListener(this);
        showInfoView = (ScrollView) findViewById(R.id.showinfo);
        loadingView = (VancloudLoadingLayout) findViewById(R.id.loading);

        payLayout.setOnClickListener(this);

    }

    private void getOrderDetail(String orderid) {

        loadingView.showLoadingView(showInfoView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("order_info_id", orderid);
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_DETAIL), params, this);
        mNetworkManager.load(CALLBACK_ORDERS_DETAIL, path, this, false);

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PasswordFragment.RECEIVER_PAY_SUCCESS.equals(action)) {
            }
        }
    };

    @Override
    public void finish() {
        if (isFromPay) {
            setResult(11);
        } else {
            if (backRefresh) {
                Intent intent = new Intent();
                intent.putExtra("position", position);
                setResult(RESULT_OK, intent);
                backRefresh = false;
            }
        }
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        super.finish();
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        loadingView.dismiss(showInfoView);
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == CALLBACK_ORDERS_DETAIL)
                loadingView.showErrorView(showInfoView, "", true, true);
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

            case CALLBACK_RESUME_DETAIL:

                try {
                    Intent intent = new Intent(this, ResumeDetail.class);
                    intent.putExtra("order_info_id", infoID);
                    intent.putExtra("resource_id", resourseID);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
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


    private void setViewData(OrderDetail orderDetail) {

        orderNumberView.setText(orderDetail.order_info_id);
        orderDescriptionView.setText(orderDetail.order_description);
        creatorTimeView.setText(orderDetail.creator_time);
        paymentStatusNameView.setText(orderDetail.payment_status_name);
        payerView.setText(orderDetail.payer);
        amountView.setText(Utils.format(getString(R.string.help_pay_money), orderDetail.amount));
        priceView.setText(Utils.format(getString(R.string.help_pay_money), orderDetail.price));

        if ("canceled".equals(orderDetail.payment_status)) {
            timeInfoView.setText(getString(R.string.order_cancele_time_tv));
            payLayout.setVisibility(View.GONE);
            paymentTimeView.setText(orderDetail.cancel_time);
        } else if ("pending".equals(orderDetail.payment_status)) {
            timeInfoView.setText(getString(R.string.order_pay_time_tv));
            payLayout.setVisibility(View.VISIBLE);
            paymentTimeView.setText("");
        } else {
            timeInfoView.setText(getString(R.string.order_pay_time_tv));
            payLayout.setVisibility(View.GONE);
            paymentTimeView.setText(orderDetail.payment_time);
        }

//        if (OrderPayActivity.INVESTIGATE.equals(orderDetail.order_type))
//            orderTypeView.setDettailText(getString(R.string.lable_investigate));
//        else if (OrderPayActivity.MEETING.equals(orderDetail.order_type))
//            orderTypeView.setDettailText(getString(R.string.lable_vidio_metting));
//        else if (OrderPayActivity.RECRUIT.equals(orderDetail.order_type))
//            orderTypeView.setDettailText(getString(R.string.buy_resume));

        orderTypeView.setText(orderDetail.order_type_name);

        List<Refund> refunds = new ArrayList<>();
        List<OrderDetailInfo> orderDetailInfos = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(orderDetail.getJson().getString("detail")))
                orderDetailInfos = JsonDataFactory.getDataArray(OrderDetailInfo.class, orderDetail.getJson().getJSONArray("detail"));
            if (!TextUtils.isEmpty(orderDetail.getJson().getString("refunds")))
                refunds = JsonDataFactory.getDataArray(Refund.class, orderDetail.getJson().getJSONArray("refunds"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (refunds.size() > 0) {
            refundInfoView.setVisibility(View.VISIBLE);
            refundLayout.setVisibility(View.VISIBLE);
            Refund refund = refunds.get(0);
            refundAmountView.setText(Utils.format(getString(R.string.help_pay_money), refund.refund_amount));
            refundNoteView.setText(refund.refund_remark);
            refundTimeView.setText(refund.refund_time);
        } else {
            refundInfoView.setVisibility(View.GONE);
            refundLayout.setVisibility(View.GONE);
        }

        if (orderDetailAdapter == null) {
            orderDetailAdapter = new OrderDetailAdapter(this, orderDetailInfos, orderDetail.option, orderDetail.url);
            listview.setAdapter(orderDetailAdapter);
        } else {
            orderDetailAdapter.myNotifyDataSetChanged(orderDetailInfos, orderDetail.option, orderDetail.url);
        }

        if (!ifShowPayButton) {
            payLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.pay_layout:
                com.vgtech.common.utils.ActivityUtils.toPay(NewOrderDetailActivity.this, orderDetail.order_info_id, orderDetail.order_description, orderDetail.price, orderDetail.order_type_name, -1, true);
                break;

            default:
                super.onClick(v);
                break;

        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (orderDetail.option && "recruit".equals(orderDetail.order_type)) {
            OrderDetailInfo orderDetailInfo = (OrderDetailInfo) orderDetailAdapter.getItem(position);
//            Intent intent = new Intent(this, ResumeDetail.class);
//            intent.putExtra("type", ResumeDetail.ENTERPRISE);
//            intent.putExtra("resume_id", orderDetailInfo.resourse_id);
//            startActivity(intent);
            resourseID = orderDetailInfo.resourse_id;
            getPaidResumeDetail(orderDetailInfo.resourse_id);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 200:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getOrderDetail(infoID);
                        backRefresh = true;
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void getPaidResumeDetail(String resourceId) {
        showLoadingDialog(this, getString(R.string.prompt_info_common));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("order_info_id", infoID);
        params.put("resource_id", resourceId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_PAID_RESUME_DETAIL), params, this);
        mNetworkManager.load(CALLBACK_RESUME_DETAIL, path, this, false);
    }
}
