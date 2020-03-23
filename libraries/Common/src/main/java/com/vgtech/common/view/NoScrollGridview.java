package com.vgtech.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 不能滚动的Gridview
 * Created by Duke on 2015/8/20.
 */
public class NoScrollGridview extends GridView {


    public NoScrollGridview(Context context, AttributeSet attrs) {
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
     * 设置Gridview不能滚动
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

}
