package com.vgtech.vancloud.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.common.api.CostCenter;
import com.vgtech.vancloud.R;

import java.util.List;

/**
 * Data:  2018/7/6
 * Auther: 陈占洋
 * Description:
 */

public class CostCenterAdapter extends BaseAdapter {

    public List<CostCenter> mData;

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cost_center, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.item_cost_center_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(mData.get(position).getDictionary_name_chn());
        return convertView;
    }

    public List<CostCenter> getData() {
        return mData;
    }

    public class ViewHolder {
        TextView name;
    }

    public void setData(List<CostCenter> data) {
        mData = data;
        notifyDataSetChanged();
    }
}
