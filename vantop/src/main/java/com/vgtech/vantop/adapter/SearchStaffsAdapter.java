package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.StaffInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2016/10/8.
 */

public class SearchStaffsAdapter extends BaseAdapter {

    private Context context;
    private List<StaffInfo> list = new ArrayList<>();


    public SearchStaffsAdapter(Context context, List<StaffInfo> list) {

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
            convertView = LayoutInflater.from(context).inflate(R.layout.search_staffs_item, null);
            mViewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check);
            mViewHolder.textView = (TextView) convertView.findViewById(R.id.textView);
            mViewHolder.emailView = (TextView) convertView.findViewById(R.id.emailView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        StaffInfo staffInfo = list.get(position);
        mViewHolder.checkBox.setChecked(staffInfo.checked);
        mViewHolder.textView.setText(staffInfo.staff_name);
        mViewHolder.emailView.setText(staffInfo.e_mail);
        return convertView;
    }


    public class ViewHolder {
        public  CheckBox checkBox;
        public  TextView textView;
        public TextView emailView;
    }

    public void myNotifyDataSetChanged(List<StaffInfo> lists) {
        list = lists;
        notifyDataSetChanged();
    }

    public List<StaffInfo> getLists() {
        return list;
    }
}
