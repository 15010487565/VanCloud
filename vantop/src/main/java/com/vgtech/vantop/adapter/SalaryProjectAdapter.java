package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.SalaryItemChildData;

import java.util.List;

/**
 * 项目查询工资适配器
 * Created by shilec on 2016/9/13.
 */
public class SalaryProjectAdapter extends AbsViewAdapter<SalaryItemChildData> {
    public SalaryProjectAdapter(Context context, List<SalaryItemChildData> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder h = new Holder(itemView);
        h.tvLabel = (TextView) itemView.findViewById(R.id.tv_label);
        h.tvValue = (TextView) itemView.findViewById(R.id.tv_value);
        return h;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {

        Holder h = (Holder) holder;
        h.tvLabel.setText(mDatas.get(posistion).month);
        h.tvValue.setText(mDatas.get(posistion).value);
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.salary_date_items;
    }

    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
        TextView tvLabel;
        TextView tvValue;
    }
}
