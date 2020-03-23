package com.vgtech.vantop.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.VerticalTimeLineMoudle;

import java.util.List;

/**
 * 垂直时间轴
 * Created by shilec on 2016/9/21.
 */
public class VerticalTimeLine extends LinearLayout{

    private List<VerticalTimeLineMoudle> mItems;
    public static final int COLOR_DESCR_BLUE = 0x001;
    public static final int COLOR_DESCR_YELLOW = 0X002;
    public static final int COLOR_DESCR_RED = 0X003;

    public VerticalTimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOrientation(VERTICAL);
    }

    public void addViews(List<VerticalTimeLineMoudle> items) {

        removeAllViews();
        mItems = items;
        if(items == null) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for(int i = 0; i < items.size(); i++) {
            View v = inflater.inflate(R.layout.clockin_timeline,this,false);
            addView(v);
            setData(v,items.get(i),i);
        }
    }

    private void setData(View itemView,VerticalTimeLineMoudle data,int index) {

        TextView tvMark = (TextView) itemView.findViewById(R.id.tv_mark);
        TextView tvLabel = (TextView) itemView.findViewById(R.id.tv_label);
        TextView tvValue = (TextView) itemView.findViewById(R.id.tv_value);
        View vLine = itemView.findViewById(R.id.v_line);
        TextView tvDescribe = (TextView) itemView.findViewById(R.id.tv_describe);

        tvMark.setText(data.markLabel);
        tvLabel.setText(data.label);
        tvValue.setText(data.value);

        //最后一个不显示垂直线
        if(index == mItems.size() - 1) {
            vLine.setVisibility(INVISIBLE);
        }
        if(data.isDecribeVisiable) {
            tvDescribe.setText(data.decribe);
            if(data.decribeColor == COLOR_DESCR_BLUE) {
                tvDescribe.setSelected(true);
                tvDescribe.setEnabled(false);
            }
            if(data.decribeColor == COLOR_DESCR_YELLOW) {
                tvDescribe.setSelected(false);
                tvDescribe.setEnabled(false);
            }
            if(data.decribeColor == COLOR_DESCR_RED) {
                tvDescribe.setEnabled(true);
                tvDescribe.setSelected(false);
            }
        } else {
            tvDescribe.setVisibility(INVISIBLE);
        }
    }
}
