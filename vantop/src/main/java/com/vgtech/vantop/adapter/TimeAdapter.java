package com.vgtech.vantop.adapter;

import android.content.Context;
import android.media.Image;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.moudle.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * 打卡记录适配器
 * Created by vic on 2017/02/27.
 */
public class TimeAdapter extends AbsViewAdapter<PunchCardListData> {
    public TimeAdapter(Context context, List<PunchCardListData> datas) {
        super(context, datas);
    }


    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder holder = new Holder(itemView);
        holder.tvTime = (TextView) holder.itemView.findViewById(R.id.tv_time);
        holder.ic_time = (ImageView) holder.itemView.findViewById(R.id.ic_time);
        return holder;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {

        Holder h = (Holder) holder;
        PunchCardListData data = mDatas.get(posistion);
        h.tvTime.setText(data.getTime());
//        h.ic_time.setImageResource(DateTimeUtil.isAm(data.getTime()) ? R.mipmap.ic_time_am : R.mipmap.ic_time_pm);
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.card_time_item;
    }

    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        TextView tvTime;
        ImageView ic_time;
    }
}
