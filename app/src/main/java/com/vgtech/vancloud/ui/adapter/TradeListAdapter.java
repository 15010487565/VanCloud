package com.vgtech.vancloud.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.common.api.TradeListItem;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Nick on 2015/12/29.
 */
public class TradeListAdapter extends BaseAdapter implements ViewListener {


    private BaseActivity mContext;
    private List<TradeListItem> data;

    public TradeListAdapter(BaseActivity mContext, List<TradeListItem> data) {
        this.mContext = mContext;
        this.data = data;
    }

    public List<TradeListItem> getData() {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.trade_list_item, null);

            mViewHolder.tradeMonth = (RelativeLayout) convertView.findViewById(R.id.trade_month);
            mViewHolder.month = (TextView) convertView.findViewById(R.id.month);
            mViewHolder.hadMoney = (TextView) convertView.findViewById(R.id.had_money);
//            mViewHolder.listview = (NoScrollListview) convertView.findViewById(R.id.listview);

            mViewHolder.tradeTitle = (TextView) convertView.findViewById(R.id.trade_title);
            mViewHolder.tradeTime = (TextView) convertView.findViewById(R.id.trade_time);
            mViewHolder.price = (TextView) convertView.findViewById(R.id.price);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final TradeListItem item = data.get(position);

//        if(!TextUtils.isEmpty(item.month) && !TextUtils.isEmpty(item.monthbalance)){
//            mViewHolder.tradeMonth.setVisibility(View.VISIBLE);
//            mViewHolder.month.setDettailText(item.month);
//            mViewHolder.hadMoney.setDettailText(String.format(mContext.getString(R.string.balance_money_), item.monthbalance));
//        }else{
            mViewHolder.tradeMonth.setVisibility(View.GONE);
//        }



        String time=mContext.getString(R.string.trade_detail_default_time_format);
        try {
            SimpleDateFormat sdf=new SimpleDateFormat(String.format(mContext.getString(R.string.trade_list_time_format)));
            time = sdf.format(Long.parseLong(item.time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mViewHolder.tradeTime.setText(time);
        mViewHolder.price.setText(item.amount);


        if(item.name.equals("recharge")){

            try {
                float result = Float.parseFloat(item.amount);
                final String lableAdd ="￥"+result;
                mViewHolder.price.setText(lableAdd);
            }catch (Exception e){
                mViewHolder.price.setText("￥0.00");
            }

            mViewHolder.price.setTextColor(Color.rgb(0xee, 0xa1, 0x27));
            mViewHolder.tradeTitle.setText(mContext.getString(R.string.recharge));
        }else if(item.name.equals("refound")){
            try {
                float result = Float.parseFloat(item.amount);
                final String lableAdd ="￥"+result;
                mViewHolder.price.setText(lableAdd);
            }catch (Exception e){
                mViewHolder.price.setText("￥0.00");
            }

            mViewHolder.price.setTextColor(Color.rgb(0xee, 0xa1, 0x27));
            mViewHolder.tradeTitle.setText(item.discription);
        }else{
            try {
                float result = Float.parseFloat(item.amount);
                final String lableCost ="￥"+result;
                mViewHolder.price.setText(lableCost);
            }catch (Exception e){
                mViewHolder.price.setText("￥0.00");
            }

            mViewHolder.price.setTextColor(Color.rgb(0x43, 0x44, 0x45));
            mViewHolder.tradeTitle.setText(item.discription);
        }

        return convertView;
    }

    class ViewHolder {

        RelativeLayout tradeMonth;
        TextView month;
        TextView hadMoney;
//        NoScrollListview listview;

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
