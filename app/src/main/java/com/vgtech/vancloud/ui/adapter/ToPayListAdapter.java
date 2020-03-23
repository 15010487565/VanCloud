package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.OrderDetail;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.module.financemanagement.OrderDetailActivity;
import com.vgtech.vancloud.ui.module.financemanagement.PayActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2015/12/29.
 */
public class ToPayListAdapter extends BaseAdapter implements View.OnClickListener {
    private Context context;
    private NetworkManager mNetworkManager;
    private final int CALLBACK_ORDERS_CANCEL = 1;
    private BaseActivity baseActivity;

    private List<OrderDetail> list;
    private Fragment fragment;

    public ToPayListAdapter(List<OrderDetail> list, Context context) {
        this.context = context;
        this.list = list;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    public ToPayListAdapter(List<OrderDetail> list, Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.getActivity();
        this.list = list;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.topay_list_item, null);
            mViewHolder.orderidView = (TextView) convertView.findViewById(R.id.orderid);
            mViewHolder.orderunmView = (TextView) convertView.findViewById(R.id.orderunm);
            mViewHolder.ordermoneyView = (TextView) convertView.findViewById(R.id.ordermoney);
            mViewHolder.payButton = (Button) convertView.findViewById(R.id.pay_btn);
            mViewHolder.payButton.setOnClickListener(this);
            mViewHolder.cancelButton = (Button) convertView.findViewById(R.id.cancel_btn);
            mViewHolder.cancelButton.setOnClickListener(this);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            convertView.findViewById(R.id.top_view).setVisibility(View.GONE);
        } else {
            convertView.findViewById(R.id.top_view).setVisibility(View.VISIBLE);
        }
        OrderDetail orderDetail = list.get(position);
        mViewHolder.payButton.setTag(R.id.order_info_id, orderDetail.getJson().toString());
        mViewHolder.payButton.setTag(R.id.position, position);
        mViewHolder.cancelButton.setTag(R.id.order_info_id, orderDetail.order_info_id);
        mViewHolder.cancelButton.setTag(R.id.position, position);
        mViewHolder.orderidView.setText(String.format(context.getString(R.string.order_id), orderDetail.order_info_id));
        mViewHolder.ordermoneyView.setText(String.format(context.getString(R.string.order_total_01), orderDetail.amount));
        mViewHolder.orderunmView.setText(orderDetail.order_description);
        convertView.setOnClickListener(this);
        convertView.setTag(R.id.order_info_id, orderDetail.getJson().toString());
        convertView.setTag(R.id.position, position);
        return convertView;
    }

    public void cancelOrder(String orderInfoId, final int position) {

        baseActivity.showLoadingDialog(context, context.getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(context));
        params.put("tenant_id", PrfUtils.getTenantId(context));
        params.put("order_info_id", orderInfoId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_ORDERS_CANCEL), params, context);
        mNetworkManager.load(CALLBACK_ORDERS_CANCEL, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                baseActivity.dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                if (!safe) {
                    return;
                }
                switch (callbackId) {
                    case CALLBACK_ORDERS_CANCEL:
                        Toast.makeText(context, context.getString(R.string.cancel_success), Toast.LENGTH_SHORT).show();
                        removeOrder(position);
                        break;
                }

            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {

            }
        }, false);
    }

    class ViewHolder {
        TextView orderidView;
        TextView orderunmView;
        TextView ordermoneyView;
        Button cancelButton;
        Button payButton;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cancel_btn:
                //TODO 取消订单
                final String orderid = (String) v.getTag(R.id.order_info_id);
                final int position = (int) v.getTag(R.id.position);
                new AlertDialog(context).builder() .setTitle(context.getString(R.string.frends_tip))
                        .setMsg(context.getString(R.string.cancel_order))
                        .setPositiveButton(context.getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelOrder(orderid, position);
                            }
                        }).setNegativeButton(context.getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();

                break;

            case R.id.pay_btn:
                //TODO 付款
            {
                int positionid = (int) v.getTag(R.id.position);
                String json = (String) v.getTag(R.id.order_info_id);

                try {
                    OrderDetail data = JsonDataFactory.getData(OrderDetail.class, new JSONObject(json));

                    Intent intent = new Intent(context, PayActivity.class);
                    intent.putExtra("position", positionid);
//                intent.putExtra("order_data", json);
                    intent.putExtra("order_describe", data.resume_count);
                    intent.putExtra("order_id", data.order_info_id);
                    intent.putExtra("order_total", data.amount);
                    intent.putExtra("order_number", data.order_info_id);
                    fragment.startActivityForResult(intent, 1);

                } catch (Exception e) {

                }
            }
            break;
            case R.id.order_layout: {
                String json = (String) v.getTag(R.id.order_info_id);
                int positionid = (int) v.getTag(R.id.position);
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("json", json);
                intent.putExtra("position", positionid);
                fragment.startActivityForResult(intent, 1);
            }
            break;
        }
    }

    public void removeOrder(int position) {
        OrderDetail orderDetail = list.get(position);
        list.remove(orderDetail);
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<OrderDetail> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void destroy() {
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    public List<OrderDetail> getList() {
        return list;
    }

}
