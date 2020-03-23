package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.adapter.BaseSimpleAdapter;
import com.vgtech.common.api.UserProperty;
import com.vgtech.vancloud.R;

/**
 * Created by code on 2016/9/5.
 */
public class UserPropertyAdapter extends BaseSimpleAdapter<UserProperty> {

    private View.OnClickListener onClickListener;

    public UserPropertyAdapter(Context context, View.OnClickListener onClickListener) {
        super(context);
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemResource(int viewType) {
        return R.layout.userproperty_item;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        UserProperty userProperty = getItem(position);
        View btn_userproperty = holder.getView(R.id.btn_userproperty);
        View spitItem = holder.getView(R.id.spit_item);
        View spitLine = holder.getView(R.id.spit_line);
        View ic_arrow = holder.getView(R.id.ic_arrow);
        ic_arrow.setVisibility(userProperty.edit ? View.VISIBLE : View.GONE);
        spitItem.setVisibility(userProperty.spit ? View.VISIBLE : View.GONE);
        spitLine.setVisibility(userProperty.spit ? View.GONE : View.VISIBLE);
        TextView tv_name = (TextView) holder.getView(R.id.tv_name);
        TextView tv_value = (TextView) holder.getView(R.id.tv_value);
        tv_name.setText(userProperty.lable);
        tv_value.setText(userProperty.name);
        if (userProperty.edit) {
            btn_userproperty.setOnClickListener(onClickListener);
            btn_userproperty.setTag(userProperty);
        }
        return convertView;
    }
}
