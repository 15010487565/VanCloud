package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.VacationApply;
import com.vgtech.vantop.moudle.Vacations;

import java.util.List;

import static com.vgtech.vantop.R.id.statusView;
import static com.vgtech.vantop.R.id.timeView;
import static com.vgtech.vantop.R.id.typeView;

/**
 * Created by Duke on 2016/9/28.
 */

public class VacationApplyListAdapter extends BaseAdapter {

    private List<VacationApply> mList;
    private Context mContext;
    private Vacations vacation;


    public VacationApplyListAdapter(Context context, List<VacationApply> list, Vacations vacations) {
        mList = list;
        mContext = context;
        vacation = vacations;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vancation_applies_item, null);
            mViewHolder.timeView = (TextView) convertView.findViewById(timeView);
            mViewHolder.typeView = (TextView) convertView.findViewById(typeView);
            mViewHolder.statusView = (TextView) convertView.findViewById(statusView);
            mViewHolder.statusView.setVisibility(View.GONE);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        VacationApply vacationApply = mList.get(position);

        String time = vacationApply.from + " " + mContext.getString(R.string.vantop_to) + " " + vacationApply.to;
        mViewHolder.timeView.setText(time);
        String desc = vacation.desc + " " + vacationApply.num + vacation.unit;
        mViewHolder.typeView.setText(desc);

        return convertView;
    }


    class ViewHolder {
        TextView timeView;
        TextView typeView;
        TextView statusView;
    }

    public void myNotifyDataSetChanged(List<VacationApply> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<VacationApply> getList() {
        return mList;
    }

    public void deleteItem(int position) {
        VacationApply vacationApply = mList.get(position);
        mList.remove(vacationApply);
        notifyDataSetChanged();
    }
}
