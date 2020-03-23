package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.OverTime;

import java.util.List;

/**
 * Created by code on 2016/7/20.
 */
public class OverTimeListAdapter extends BaseAdapter {
    Context context;
    public List<OverTime> getMlist() {
        return mlist;
    }

    List<OverTime> mlist;
    int mPosition;

    public OverTimeListAdapter(Context context, List<OverTime> list) {
        this.context = context;
        this.mlist = list;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        this.mlist.clear();
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mPosition = position;
        final OverTime overTime = mlist.get(position);
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.overtime_item, null);
            mViewHolder.data = (TextView) convertView.findViewById(R.id.date_txt);
            mViewHolder.type = (TextView) convertView.findViewById(R.id.type_txt);
            mViewHolder.time = (TextView) convertView.findViewById(R.id.time_txt);
            mViewHolder.timeNum = (TextView) convertView.findViewById(R.id.time_num_txt);
            mViewHolder.status = (TextView) convertView.findViewById(R.id.status_txt);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.data.setText(overTime.data);
        mViewHolder.type.setText(overTime.type);
        mViewHolder.time.setText(context.getString(R.string.vantop_time) + overTime.time);
        mViewHolder.timeNum.setText(context.getString(R.string.vantop_time_length) + overTime.time_long + context.getString(R.string.vantop_hour));
        if ("0".equals(overTime.status)) {
            mViewHolder.status.setText(context.getString(R.string.vantop_approving));
            mViewHolder.status.setTextColor(context.getResources().getColor(R.color.approvaling_txt));
        } else if ("1".equals(overTime.status)) {
            mViewHolder.status.setText(context.getString(R.string.vantop_adopt));
            mViewHolder.status.setTextColor(context.getResources().getColor(R.color.adopted_txt));
        } else if ("2".equals(overTime.status)) {
            mViewHolder.status.setText(context.getString(R.string.vantop_refuse));
            mViewHolder.status.setTextColor(context.getResources().getColor(R.color.refused_txt));
        }
        return convertView;
    }

    private class ViewHolder {
        TextView data;
        TextView type;
        TextView time;
        TextView timeNum;
        TextView status;
    }

    public void removeItemAction(int position) {
        OverTime overTime = mlist.get(position);
        mlist.remove(overTime);
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<OverTime> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }
}
