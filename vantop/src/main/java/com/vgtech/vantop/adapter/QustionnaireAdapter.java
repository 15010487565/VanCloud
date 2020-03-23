package com.vgtech.vantop.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.QuestionnaireListData;

import java.util.List;

/**
 * 调查问卷适配器
 * Created by shilec on 2016/9/12.
 */
public class QustionnaireAdapter extends AbsViewAdapter<QuestionnaireListData> {

    public QustionnaireAdapter(Context context, List<QuestionnaireListData> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {

        Holder h = new Holder(itemView);
        h.tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
        h.tvStartTime = (TextView) itemView.findViewById(R.id.tv_starttime);
        h.tvEndtime = (TextView) itemView.findViewById(R.id.tv_endtime);
        h.tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
        return h;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {

        QuestionnaireListData data = mDatas.get(posistion);
        Holder h = (Holder) holder;
        h.tvTitle.setText(data.title);
        h.tvStartTime.setText(data.startTime);
        h.tvEndtime.setText(data.endTime);

        Resources res = mContext.getResources();
        int nStatus = Integer.parseInt(data.status);
        //三种状态颜色不同
        String status = nStatus == 1 ? mContext.getString(R.string.vantop_havecommited) : (nStatus == 2 ? mContext.getString(R.string.vantop_hasfailed) : mContext.getString(R.string.vantop_nocommited));
        int color = nStatus == 1 ? res.getColor(R.color.green) : (nStatus == 2 ? res.getColor(R.color.font) : res.getColor(R.color.red));
        h.tvStatus.setText(status);
        h.tvStatus.setTextColor(color);
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.list_questionnaire_item;
    }

    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
        TextView tvTitle;
        TextView tvStartTime;
        TextView tvEndtime;
        TextView tvStatus;
    }
}
