package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.Indus;

import java.util.List;

/**
 * Created by code on 2015/10/19.
 */
public class PositionInfoDialogAdapter extends BaseAdapter implements ViewListener{
    Context context;
    public List<Indus> getMlist() {
        return mlist;
    }
    List<Indus> mlist;
    int mPosition;

    public PositionInfoDialogAdapter(Context context, List<Indus> list) {
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mPosition = position;
        final Indus indus = mlist.get(position);
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_department_dialog, null);
            mViewHolder.title_name = (TextView) convertView.findViewById(R.id.tv_title_name);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.title_name.setText(indus.indus_name);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent action = new Intent();
                action.setAction(Actions.ACTION_DEFAULT);
                action.putExtra("indus_id",mlist.get(position).indus_id);
                LocalBroadcastManager.getInstance(context).sendBroadcast(action);
            }
        });
        return convertView;
    }

    private View lastView;

    @Override
    public View getLastView() {
        return lastView;
    }

    @Override
    public void setLastView(View view) {
        lastView = view;
    }

    private class ViewHolder {
        TextView title_name;
    }

    public void myNotifyDataSetChanged(List<Indus> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<Indus> lists, boolean type) {
        this.mlist = lists;
        notifyDataSetChanged();
    }
}
