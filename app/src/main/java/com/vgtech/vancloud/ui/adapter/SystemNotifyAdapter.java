package com.vgtech.vancloud.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.common.api.SystemNotifyItem;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

/**
 * Created by Nick on 2016/1/11.
 */
public class SystemNotifyAdapter extends BaseAdapter implements ViewListener {

    private List<SystemNotifyItem> data;
    private BaseActivity mContext;

    public SystemNotifyAdapter(BaseActivity mContext, List<SystemNotifyItem> data) {
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.system_notify_item, null);
            mViewHolder.title = (TextView) convertView.findViewById(R.id.title);
            mViewHolder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final SystemNotifyItem item = data.get(position);


        mViewHolder.time.setText(Utils.getInstance(mContext).dateFormat(item.date));
        mViewHolder.title.setText(item.title);

        return convertView;
    }

    class ViewHolder {

        TextView title;
        TextView time;
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

}
