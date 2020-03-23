package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.StaffInfo;

import java.util.List;

/**
 * Created by Duke on 2016/9/30.
 */

public class StaffAdapter extends BaseAdapter {
    private Context context;
    private List<StaffInfo> list;

    public StaffAdapter(Context context, List<StaffInfo> list) {
        this.context = context;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, null);
            mViewHolder.textView = (TextView) convertView.findViewById(R.id.text_view);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        StaffInfo staffInfo = list.get(position);
        mViewHolder.textView.setText(staffInfo.staff_name);

        return convertView;
    }

    private class ViewHolder {

        TextView textView;

    }

    public List<StaffInfo> getList() {
        return list;
    }
}
