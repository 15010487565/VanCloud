package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.adapter.BaseSimpleAdapter;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.ChannelValues;


/**
 * Created by code on 2016/7/27.
 */
public class ResumeStatusAdapter extends BaseSimpleAdapter<ChannelValues> {

    private int clickItem = -1;

    public ResumeStatusAdapter(Context context) {
        super(context);
    }

    public void setSeclection(int position) {
        clickItem = position;
    }

    @Override
    public int getItemResource(int viewType) {
        return R.layout.resume_channel_item;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        TextView name = holder.getView(R.id.tv_name);
        final ChannelValues channelValues = getItem(position);
        if (clickItem == position) {
            name.setTextColor(mContext.getResources().getColor(R.color.order_money));
        } else {
            name.setTextColor(mContext.getResources().getColor(R.color.grey_text_color));
        }
        name.setText(channelValues.key);
        return convertView;
    }
}
