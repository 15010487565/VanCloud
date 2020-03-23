package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.common.api.OrderType;
import com.vgtech.vancloud.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2016/3/17.
 */
public class OrderTypeAdapter extends BaseAdapter {

    private List<OrderType> list = new ArrayList<>();
    private Context context;

    public List<OrderType> getList() {
        return list;
    }

    public OrderTypeAdapter(List<OrderType> list, Context context) {

        this.list = list;
        this.context = context;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.order_type_item, null);
            mViewHolder.nameView = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        OrderType orderType = list.get(position);
        mViewHolder.nameView.setText(orderType.order_type_name);
        convertView.setTag(R.id.order_info_id, orderType.getJson().toString());
        return convertView;
    }


    private class ViewHolder {

        TextView nameView;
    }

    public void myNotifyDataSetChanged(List<OrderType> orderTypes) {

        this.list = orderTypes;
        notifyDataSetChanged();
    }


}
