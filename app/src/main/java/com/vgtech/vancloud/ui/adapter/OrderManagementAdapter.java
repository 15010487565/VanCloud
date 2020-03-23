package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.OrderDetail;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.module.financemanagement.NewOrderDetailActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Duke on 2015/12/31.
 */
public class OrderManagementAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;
    private List<OrderDetail> list;
    private Fragment fragment;


    public OrderManagementAdapter(List<OrderDetail> list, Context context) {
        this.list = list;
        this.context = context;
    }


    public OrderManagementAdapter(List<OrderDetail> list, Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.getActivity();
        this.list = list;

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

            convertView = LayoutInflater.from(context).inflate(R.layout.order_management_item, null);
            mViewHolder.orderTypeView = (TextView) convertView.findViewById(R.id.order_type);
            mViewHolder.orderDescriptionView = (TextView) convertView.findViewById(R.id.order_description);
            mViewHolder.orderNumView = (TextView) convertView.findViewById(R.id.order_num);
            mViewHolder.priceView = (TextView) convertView.findViewById(R.id.price);
            mViewHolder.amountView = (TextView) convertView.findViewById(R.id.amount);
            mViewHolder.payButton = (Button) convertView.findViewById(R.id.pay_btn);
            mViewHolder.payButton.setOnClickListener(this);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

//        if (position == 0) {
//            convertView.findViewById(R.id.top_view).setVisibility(View.GONE);
//        } else {
//            convertView.findViewById(R.id.top_view).setVisibility(View.VISIBLE);
//        }
        OrderDetail orderDetail = list.get(position);


        mViewHolder.orderTypeView.setText(String.format(context.getString(R.string.order_type), orderDetail.order_type_name));
        mViewHolder.orderDescriptionView.setText(String.format(context.getString(R.string.order_description), orderDetail.order_description));
        mViewHolder.orderNumView.setText(String.format(context.getString(R.string.order_info_num), orderDetail.order_info_id));
        mViewHolder.priceView.setText(String.format(context.getString(R.string.amount_payable01), orderDetail.price));
        mViewHolder.amountView.setText(String.format(context.getString(R.string.pay_amount01), orderDetail.amount));

        if ("pending".equals(orderDetail.payment_status))
            mViewHolder.payButton.setVisibility(View.VISIBLE);
        else
            mViewHolder.payButton.setVisibility(View.GONE);

        mViewHolder.payButton.setTag(R.id.order_info_id, orderDetail.getJson().toString());
        mViewHolder.payButton.setTag(R.id.position, position);
        convertView.setTag(R.id.order_info_id, orderDetail.order_info_id);
        convertView.setTag(R.id.position, position);
        convertView.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.pay_btn:
                //TODO 付款
            {
                int positionid = (int) v.getTag(R.id.position);
                String json = (String) v.getTag(R.id.order_info_id);
                try {
                    if (!TextUtil.isEmpty(json)) {
                        OrderDetail orderDetail = JsonDataFactory.getData(OrderDetail.class, new JSONObject(json));
                        if (fragment == null)
                            com.vgtech.common.utils.ActivityUtils.toPay((Activity) context, orderDetail.order_info_id, orderDetail.order_description, orderDetail.price, orderDetail.order_type_name, positionid);
                        else
                            com.vgtech.common.utils.ActivityUtils.toPay(fragment, orderDetail.order_info_id, orderDetail.order_description, orderDetail.price, orderDetail.order_type_name, positionid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case R.id.order_layout: {
                String id = (String) v.getTag(R.id.order_info_id);
                int positionid = (int) v.getTag(R.id.position);
                Intent intent = new Intent(context, NewOrderDetailActivity.class);
                intent.putExtra("infoid", id);
                intent.putExtra("position", positionid);
                if (fragment == null) {
                    ((Activity) context).startActivityForResult(intent, 200);
                } else {
                    fragment.startActivityForResult(intent, 200);
                }
            }
            break;
        }


    }

    class ViewHolder {

        TextView orderTypeView;
        TextView orderDescriptionView;
        TextView orderNumView;
        TextView priceView;
        TextView amountView;
        Button payButton;

    }

    public void myNotifyDataSetChanged(List<OrderDetail> list) {

        this.list = list;
        notifyDataSetChanged();
    }

    public List<OrderDetail> getList() {
        return list;
    }


//    public String getOrderTypeName(String orderType) {
//
//        String type = "";
//        String json = PrfUtils.getAllOrderTypes(context);
//        if (!TextUtils.isEmpty(json)) {
//            try {
//                List<OrderType> orderTypeList = JsonDataFactory.getDataArray(OrderType.class, new JSONArray(json));
//                for (OrderType morderType : orderTypeList) {
//
//                    if (orderType.equals(morderType.order_type))
//                        type = morderType.order_type_name;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return type;
//
//    }

    public void removeOrder(int position) {
        OrderDetail orderDetail = list.get(position);
        list.remove(orderDetail);
        notifyDataSetChanged();
    }
}
