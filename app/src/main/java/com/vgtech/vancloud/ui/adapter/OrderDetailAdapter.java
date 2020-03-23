package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.common.api.OrderDetailInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单详情中商品信息适配器
 * Created by Duke on 2016/3/19.
 */
public class OrderDetailAdapter extends BaseAdapter {

    private Context context;
    private List<OrderDetailInfo> list = new ArrayList<>();
    private boolean option;
    private String orderType;

    public OrderDetailAdapter(Context context, List<OrderDetailInfo> list, boolean option, String orderType) {
        this.context = context;
        this.list = list;
        this.option = option;
        this.orderType = orderType;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.order_detail_item, null);
            mViewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
            mViewHolder.priceView = (TextView) convertView.findViewById(R.id.price);
            mViewHolder.icArrowView = (ImageView) convertView.findViewById(R.id.ic_arrow);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final OrderDetailInfo orderDetailInfo = list.get(position);
        mViewHolder.nameView.setText(orderDetailInfo.name);
        mViewHolder.priceView.setText(orderDetailInfo.price);
        if (option)
            mViewHolder.icArrowView.setVisibility(View.VISIBLE);
        else
            mViewHolder.icArrowView.setVisibility(View.GONE);

        convertView.setTag(R.id.position, orderDetailInfo.resourse_id);
        return convertView;
    }

    private class ViewHolder {

        TextView nameView;
        TextView priceView;
        ImageView icArrowView;

    }

    public void myNotifyDataSetChanged(List<OrderDetailInfo> list, boolean option, String orderType) {

        this.list = list;
        this.option = option;
        this.orderType = orderType;
        notifyDataSetChanged();
    }
}
