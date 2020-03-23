package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.adapter.BaseSimpleAdapter;
import com.vgtech.common.api.InvestigationRecords;
import com.vgtech.vancloud.R;


/**
 * Created by code on 2016/10/14.
 */
public class InvestigationRecordsAdapter extends BaseSimpleAdapter<InvestigationRecords> {

    public InvestigationRecordsAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemResource(int viewType) {
        return R.layout.item_investigation_records;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        TextView records_num = holder.getView(R.id.records_num_tv);
        TextView type = holder.getView(R.id.type_tv);
        TextView name = holder.getView(R.id.name_tv);
        TextView phone = holder.getView(R.id.phone_tv);
        TextView price = holder.getView(R.id.price_tv);
        TextView data = holder.getView(R.id.data_tv);
        InvestigationRecords records = getItem(position);
        records_num.setText("提交编号：" + records.investigateReportId);
        if ("finished".equals(records.reportStatus)) {
            type.setText("调查成功");
        } else if ("progressing".equals(records.reportStatus)) {
            type.setText("调查中");
        } else {
            type.setText("调查失败");
        }
        name.setText(records.investigateName);
        phone.setText(records.investigatePhone);
        price.setText(records.price + "元");
        data.setText("调查时间：" + records.creatorTimeStr);
        return convertView;
    }
}
