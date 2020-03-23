package com.vgtech.vantop.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.ClockInListData;

import java.util.List;

/**
 * 考勤列表适配器
 * Created by shilec on 2016/9/7.
 */
public class ClockInListAdapter extends AbsViewAdapter<ClockInListData> {

    public ClockInListAdapter(Context context, List<ClockInListData> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder h = new Holder(itemView);
        h.tvDate = (TextView) itemView.findViewById(R.id.date_txt);
        h.tvException = (TextView) itemView.findViewById(R.id.exception_txt);
        h.tvInTime = (TextView) itemView.findViewById(R.id.inTime_txt);
        h.tvInTimeMid = (TextView) itemView.findViewById(R.id.inTimeMid_txt);
        h.tvOutTime = (TextView) itemView.findViewById(R.id.outTime_txt);
        h.tvOutTimeMid = (TextView) itemView.findViewById(R.id.outTimeMid_txt);
        h.tvType = (TextView) itemView.findViewById(R.id.type_txt);
        h.ivClockinDetail = (ImageView) itemView.findViewById(R.id.clock_img);
        h.lmidTime = (LinearLayout) itemView.findViewById(R.id.timeMid_rl);
        return h;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {
        Holder h = (Holder) holder;
        ClockInListData data = mDatas.get(posistion);
        h.tvDate.setText(data.getDate() + data.getWeek());
        String shiftName = data.getShiftName();
        if (!TextUtils.equals("null", shiftName)) {
            h.tvType.setText(shiftName);
        } else {
            h.tvType.setText("");
        }
        //没有内容则隐藏布局
        long mid_time = 0;
        String outTime = data.getOutTime();
        h.tvOutTime.setText(outTime);
        String inTime = data.getInTime();
        h.tvInTime.setText(inTime);
        String timeNum = data.getTimeNum();
        if (!TextUtils.equals("4",timeNum )) {
            h.lmidTime.setVisibility(View.GONE);
        } else {
            h.lmidTime.setVisibility(View.VISIBLE);
            String outTimeMid = data.getOutTimeMid();
            String inTimeMid = data.getInTimeMid();
            h.tvOutTimeMid.setText(mContext.getString(R.string.out_time_mid) + ": " + outTimeMid);
            h.tvInTimeMid.setText(mContext.getString(R.string.in_time_mid) + ": " + inTimeMid);
            if (!TextUtils.isEmpty(outTimeMid) && !TextUtils.isEmpty(inTimeMid)) {
                mid_time = DateTimeUtil.stringToLong_hm(inTimeMid) - DateTimeUtil.stringToLong_hm(outTimeMid);
            }
        }
        if (!TextUtils.isEmpty(outTime) && !TextUtils.isEmpty(inTime)) {
            long work_time = DateTimeUtil.stringToLong_hm(outTime) - DateTimeUtil.stringToLong_hm(inTime) - mid_time;
            double work_time_hours = DateTimeUtil.formatDuringH(work_time);
            String work_time_text = h.tvType.getText().toString() + "<font color='gray'>&nbsp;&nbsp;时长:" + work_time_hours + "</font>";
            h.tvType.setText(Html.fromHtml(work_time_text));
        } /*else {
            if (!TextUtils.isEmpty(h.tvType.getText())) {
                String work_time_text = h.tvType.getText().toString() + "(" + ")";
                h.tvType.setText(work_time_text);
            }
        }*/
        String isException = data.getIsException();
        h.tvException.setVisibility(Boolean.parseBoolean(isException) ? View.VISIBLE : View.GONE);
        h.ivClockinDetail.setVisibility(Boolean.parseBoolean(isException) ? View.VISIBLE : View.GONE);
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.clockin_list_item;
    }

    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        TextView tvDate;
        TextView tvException;
        TextView tvType;
        TextView tvInTime;
        TextView tvOutTime;
        TextView tvOutTimeMid;
        TextView tvInTimeMid;
        ImageView ivClockinDetail;
        LinearLayout lmidTime;
    }
}
