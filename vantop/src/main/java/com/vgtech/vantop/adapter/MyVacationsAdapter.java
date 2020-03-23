package com.vgtech.vantop.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.Vacations;
import com.vgtech.vantop.ui.vacation.ApplyVacationActivity;

import java.util.List;

/**
 * Created by Duke on 2016/7/22.
 */
public class MyVacationsAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;
    private List<Vacations> mList;
    private LayoutInflater inflater;
    private String username;
    public MyVacationsAdapter(Context context, List<Vacations> list) {
        mContext = context;
        mList = list;
        inflater = LayoutInflater.from(context);
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        username = preferences.getString("username", "");
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
            convertView = inflater.inflate(R.layout.my_vacations_item, null);
            mViewHolder.typeView = (TextView) convertView.findViewById(R.id.typeView);
            mViewHolder.timeView = (TextView) convertView.findViewById(R.id.timeView);
            mViewHolder.durationView = (TextView) convertView.findViewById(R.id.durationView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Vacations vacations = mList.get(position);
        String desc = vacations.desc;
        switch (username){
            case "15010487565":
            case "18395614432":
                if (desc.equals("Antenatal Care")){
                    mViewHolder.typeView.setText("产前保健");
                }else if (desc.equals("Annual Leave")){
                    mViewHolder.typeView.setText("年假");
                }else if (desc.equals("Business Travel")){
                    mViewHolder.typeView.setText("商务旅行");
                }else if (desc.equals("Maternity Leave")){
                    mViewHolder.typeView.setText("产假");
                }else if (desc.equals("Compensation Leave")){
                    mViewHolder.typeView.setText("薪酬休假");
                }else if (desc.equals("Funeral  Leave")){
                    mViewHolder.typeView.setText("丧葬假");
                }else if (desc.equals("Half Pay Sick Leave")){
                    mViewHolder.typeView.setText("半薪病假");
                }else if (desc.equals("Married Leave")){
                    mViewHolder.typeView.setText("婚假");
                }else if (desc.equals("No pay leave")){
                    mViewHolder.typeView.setText("无薪假");
                }else if (desc.equals("Paternity Leave")){
                    mViewHolder.typeView.setText("陪产假");
                }else if (desc.equals("Sick Leave（paid）")){
                    mViewHolder.typeView.setText("病假（有薪）");
                }else {
                    mViewHolder.typeView.setText(desc);
                }
                break;
            default:
                mViewHolder.typeView.setText(desc);
                break;
        }
        String time = mContext.getString(R.string.vantop_end_to) + " ";
        time += vacations.date;
        time += " " + mContext.getString(R.string.vantop_balance) + " ";
        String unit = vacations.unit;
        time += "<font color='#929292'>";
        time += vacations.balance + unit;
        time += "</font>";
        mViewHolder.timeView.setText(Html.fromHtml(time));
        mViewHolder.durationView.setTag(vacations);
        mViewHolder.durationView.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        Vacations vacations = (Vacations) v.getTag();
        Intent intent = new Intent(mContext, ApplyVacationActivity.class);
        intent.putExtra("json", vacations.getJson().toString());
        mContext.startActivity(intent);
    }

    private class ViewHolder {

        TextView typeView;
        TextView timeView;
        TextView durationView;

    }

    public void myNotifyDataSetChanged(List<Vacations> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<Vacations> getList() {
        return mList;
    }
}
