package com.vgtech.vancloud.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.common.api.TradeInfoItem;
import com.vgtech.vancloud.ui.BaseActivity;

import java.util.List;

/**
 * Created by Nick on 2016/1/11.
 */
public class TradeInfoAdapter extends BaseAdapter implements ViewListener {

    private List<TradeInfoItem> data;
    private BaseActivity mContext;

    public TradeInfoAdapter(BaseActivity mContext, List<TradeInfoItem> data) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.trade_info_item, null);
            mViewHolder.tradeTitle = (TextView) convertView.findViewById(R.id.trade_title);
            mViewHolder.tradeTime = (TextView) convertView.findViewById(R.id.trade_time);
            mViewHolder.price = (TextView) convertView.findViewById(R.id.price);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final TradeInfoItem item = data.get(position);


        mViewHolder.tradeTime.setText(item.time);
        mViewHolder.price.setText(item.amount);
        if(item.name.equals("buy_resume")){

            try {
                float result = Float.parseFloat(item.amount);
                final String lableCost ="￥"+result;
                mViewHolder.price.setText(lableCost);
            }catch (Exception e){
                mViewHolder.price.setText("￥0.00");
            }

            mViewHolder.price.setTextColor(Color.rgb(0x43, 0x44, 0x45));
            mViewHolder.tradeTitle.setText(mContext.getString(R.string.buy_resume));
        }else if(item.name.equals("recharge")){

            try {
                float result = Float.parseFloat(item.amount);
                final String lableAdd ="￥"+result;
                mViewHolder.price.setText(lableAdd);
            }catch (Exception e){
                mViewHolder.price.setText("￥0.00");
            }

            mViewHolder.price.setTextColor(Color.rgb(0xee, 0xa1, 0x27));
            mViewHolder.tradeTitle.setText(mContext.getString(R.string.recharge));
        }
        return convertView;
    }

    class ViewHolder {

        TextView tradeTitle;
        TextView tradeTime;
        TextView price;
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
