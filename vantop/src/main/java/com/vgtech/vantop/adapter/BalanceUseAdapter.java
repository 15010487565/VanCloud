package com.vgtech.vantop.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.Vacations;
import com.vgtech.vantop.moudle.VacationsBalanceUses;

import java.util.List;

/**
 * Created by Duke on 2016/9/13.
 */
public class BalanceUseAdapter extends BaseAdapter {

    private Context mContext;
    private List<VacationsBalanceUses> mList;
    private LayoutInflater inflater;
    private Vacations vacations;

    public BalanceUseAdapter(Context context, List<VacationsBalanceUses> list, Vacations vacations) {

        mContext = context;
        mList = list;
        this.vacations = vacations;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.balance_use_item, null);
            mViewHolder.beginTimeView = (TextView) convertView.findViewById(R.id.beginTimeView);
            mViewHolder.endTimeView = (TextView) convertView.findViewById(R.id.endTimeView);
            mViewHolder.durationView = (TextView) convertView.findViewById(R.id.durationView);
            mViewHolder.remarkView = (TextView) convertView.findViewById(R.id.remarkView);
            mViewHolder.lineView = convertView.findViewById(R.id.lineView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        VacationsBalanceUses vacationsBalanceUses = mList.get(position);
        mViewHolder.beginTimeView.setText(vacationsBalanceUses.from);
        mViewHolder.endTimeView.setText(vacationsBalanceUses.to);
        mViewHolder.durationView.setText(vacationsBalanceUses.num + vacations.unit);
        mViewHolder.remarkView.setText(vacationsBalanceUses.remark);
        View pview = (View) mViewHolder.remarkView.getParent();
        pview.setVisibility(TextUtils.isEmpty(vacationsBalanceUses.remark) ? View.GONE : View.VISIBLE);
        mViewHolder.lineView.setVisibility(getCount() - 1 == position ? View.GONE : View.VISIBLE);

        return convertView;
    }


    private class ViewHolder {
        TextView beginTimeView;
        TextView endTimeView;
        TextView durationView;
        TextView remarkView;
        View lineView;
    }

    public void myNotifyDataSetChanged(List<VacationsBalanceUses> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<VacationsBalanceUses> getList() {
        return mList;
    }
}
