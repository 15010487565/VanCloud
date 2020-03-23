package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.ShiftSelect;

import java.util.List;

/**
 * Created by Duke on 2016/7/20.
 */
public class ShiftSelectAdapter extends BaseAdapter {
    Context context;

    public List<ShiftSelect> getMlist() {
        return mlist;
    }

    List<ShiftSelect> mlist;
    int mPosition;

    public ShiftSelectAdapter(Context context, List<ShiftSelect> list) {
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
        final ShiftSelect shiftSelect = mlist.get(position);
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.shift_item, null);
            mViewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.check);
            mViewHolder.title = (TextView) convertView.findViewById(R.id.title_tv);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.checkbox.setChecked(shiftSelect.checked);
        mViewHolder.title.setText(shiftSelect.shiftValue);

        return convertView;
    }

    private class ViewHolder {
        CheckBox checkbox;
        TextView title;
    }

    public void myNotifyDataSetChanged(List<ShiftSelect> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public List<ShiftSelect> getLists() {
        return mlist;
    }
}
