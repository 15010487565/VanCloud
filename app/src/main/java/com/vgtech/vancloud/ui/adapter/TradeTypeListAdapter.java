package com.vgtech.vancloud.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.common.api.TradeTypeListItem;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import java.util.List;

/**
 * Created by Nick on 2015/12/29.
 */
public class TradeTypeListAdapter extends BaseAdapter implements ViewListener {


    private BaseActivity mContext;
    private List<TradeTypeListItem> data;

    public TradeTypeListAdapter(BaseActivity mContext, List<TradeTypeListItem> data) {
        this.mContext = mContext;
        this.data = data;
    }

    public List<TradeTypeListItem> getData() {
        return data;
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

    public void clear() {
        this.data.clear();
        try {
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.trade_type_list_item, null);

            mViewHolder.trade_type_title = (TextView) convertView.findViewById(R.id.trade_type_title);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final TradeTypeListItem item = data.get(position);
        mViewHolder.trade_type_title.setText(item.value);

        return convertView;
    }

    class ViewHolder {

        TextView trade_type_title;
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
