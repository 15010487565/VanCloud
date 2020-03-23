package com.vgtech.vancloud.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vgtech.common.api.StaffApplyItem;
import com.vgtech.vancloud.R;

/**
 * Created by brook on 16/9/7.
 */
public class StaffApplyAdapter extends DataAdapter<StaffApplyItem> {

    Context mContext;

    public StaffApplyAdapter(Context context) {
        this.mContext = context;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.staff_apply_item, null);
            assert convertView != null;
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        StaffApplyItem item = dataSource.get(position);

        viewHolder.userNameTxt.setText(item.name);
        viewHolder.numberTxt.setText(item.mobile);
        viewHolder.companyNameTxt.setText(mContext.getResources().getString(R.string.apply_add)+item.tenant);
        viewHolder.statusTxt.setText(item.remark);

        String status = item.state;
        if ("pending".equals(item.state)) {
            //viewHolder.statusTxt.setDettailText(mContext.getResources().getString(R.string.need_handle));
            viewHolder.statusTxt.setTextColor(mContext.getResources().getColor(R.color.approvaling_txt));
        } else if ("agreed".equals(status)) {
            //viewHolder.statusTxt.setDettailText(mContext.getResources().getString(R.string.agreed));
            viewHolder.statusTxt.setTextColor(mContext.getResources().getColor(R.color.adopted_txt));
        } else if ("refused".equals(status)) {
            //viewHolder.statusTxt.setDettailText(mContext.getResources().getString(R.string.refused));
            viewHolder.statusTxt.setTextColor(mContext.getResources().getColor(R.color.refused_txt));
        }
        viewHolder.statusTxt.setText(item.remark);
        return convertView;
    }

    public class ViewHolder {
        TextView userNameTxt;
        TextView numberTxt;
        TextView companyNameTxt;
        TextView statusTxt;

        public ViewHolder(final View view) {
            userNameTxt = (TextView) view.findViewById(R.id.userName_txt);
            numberTxt = (TextView) view.findViewById(R.id.number_txt);
            companyNameTxt = (TextView) view.findViewById(R.id.companyName_txt);
            statusTxt = (TextView) view.findViewById(R.id.status_txt);
        }
    }
}
