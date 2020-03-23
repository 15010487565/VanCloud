package com.vgtech.vancloud.ui.beidiao;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.adapter.BaseSimpleAdapter;
import com.vgtech.vancloud.R;


/**
 * Created by code on 2016/9/5.
 */
public class CheckItemAdapter extends BaseSimpleAdapter<CheckItem> {

    private CheckItemSelectedListener selectedListener;
    public CheckItemAdapter(Context context, CheckItemSelectedListener listener) {
        super(context);
        selectedListener = listener;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemResource(int viewType) {
        return R.layout.bd_check_item;
    }


    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        convertView.setBackgroundColor(Color.parseColor(position % 2 == 0 ? "#ffffffff" : "#fff3f7ff"));
        CheckItem checkItem = getItem(position);
        ImageView checkBox = holder.getView(R.id.cb_item);
        checkBox.setVisibility(selectedListener.getUnSeleced().contains(checkItem)?View.INVISIBLE:View.VISIBLE);
        checkBox.setImageResource(selectedListener.contains(checkItem) ? R.mipmap.chk_on_normal : R.mipmap.chk_off_normal);
        TextView nameTv = holder.getView(R.id.tv_name);
        nameTv.setText(checkItem.name);
        TextView tv_price = holder.getView(R.id.tv_price);
        tv_price.setText(String.valueOf(checkItem.price));
        return convertView;
    }
}
