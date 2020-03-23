package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.vgtech.common.utils.DataUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Notice;
import com.vgtech.vantop.adapter.AbsViewAdapter;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.ui.punchcard.CardInfoActivity;

import org.json.JSONObject;

import java.util.List;

/**
 * 打卡记录适配器
 * Created by shilec on 2016/9/6.
 */
public class NoticeAdapter extends AbsViewAdapter<Notice> {
    public NoticeAdapter(Context context, List<Notice> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder holder = new Holder(itemView);
        holder.notification_title = (TextView) holder.itemView.findViewById(R.id.notification_title);
        holder.tvDate = (TextView) holder.itemView.findViewById(R.id.notification_time);
        return holder;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {
        Holder h = (Holder) holder;
        Notice data = mDatas.get(posistion);
        h.tvDate.setText(DataUtils.dateFormat(data.create_time));
        h.notification_title.setText(data.subject);
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.item_notice;
    }


    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        TextView tvDate;
        TextView notification_title;
    }
}
