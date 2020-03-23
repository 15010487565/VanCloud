package com.vgtech.vantop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.moudle.Record;
import com.vgtech.vantop.moudle.Schedule;
import com.vgtech.vantop.ui.punchcard.CardInfoActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 打卡记录适配器
 * Created by shilec on 2016/9/6.
 */
public class ScheduleAdapter extends AbsViewAdapter<Schedule> implements AdapterView.OnItemClickListener {
    public ScheduleAdapter(Context context, List<Schedule> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder holder = new Holder(itemView);
        holder.tvDate = (TextView) holder.itemView.findViewById(R.id.tv_date);
        holder.tvSchedule = (TextView) holder.itemView.findViewById(R.id.tv_schedule);
        holder.icIsAdd = holder.itemView.findViewById(R.id.ic_isadd);
        return holder;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {
        Holder h = (Holder) holder;
        Schedule data = mDatas.get(posistion);
        h.tvDate.setText(data.dates + " " + DateTimeUtil.getWeekOfDate(mContext, data.dates));
        h.tvSchedule.setText(data.shiftValue);
        h.icIsAdd.setVisibility(data.added==0?View.INVISIBLE:View.VISIBLE);
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.schedule_item;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PunchCardListData punch = (PunchCardListData) parent.getItemAtPosition(position);
        JSONObject jsonObject = punch.jsonObject;
        if (jsonObject != null) {
            Intent intent = new Intent(mContext, CardInfoActivity.class);
            intent.putExtra("cardinfo", jsonObject.toString());
            mContext.startActivity(intent);
        }
    }

    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        TextView tvDate;
        TextView tvSchedule;
        View icIsAdd;
    }
}
