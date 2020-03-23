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
import com.vgtech.common.api.DepartmentInfo;
import java.util.List;

/**
 * Created by code on 2015/10/19.
 */
public class DepartmentInfoDialogAdapter extends BaseAdapter implements ViewListener{

    Context context;
    public List<DepartmentInfo> getMlist() {
        return mlist;
    }
    List<DepartmentInfo> mlist;
    int mPosition;

    public DepartmentInfoDialogAdapter(Context context, List<DepartmentInfo> list) {
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
        final DepartmentInfo departmentInfo = mlist.get(position);
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_department_dialog, null);
            mViewHolder.title_name = (TextView) convertView.findViewById(R.id.tv_title_name);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.title_name.setText(departmentInfo.indus_name);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent action = new Intent();
                action.setAction(Actions.ACTION_DEPARTMENT);
                action.putExtra("indus_id", mlist.get(position).indus_id);
                action.putExtra("position",mPosition);
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

    public void myNotifyDataSetChanged(List<DepartmentInfo> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<DepartmentInfo> lists, boolean type) {
        this.mlist = lists;
        notifyDataSetChanged();
    }
}
