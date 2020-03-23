package com.vgtech.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 不能滚动的listview
 * Created by Duke on 2015/8/17.
 */
public class NoScrollListview extends ListView {


    public NoScrollListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(false);
        setPressed(false);
        setEnabled(false);
    }

    public void setItemClick(boolean flag) {
        setClickable(flag);
//        setPressed(flag);
        setEnabled(flag);
    }

    /**
     * 设置listview不能滚动
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

}