package com.vgtech.common.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class TabPageIndicator extends LinearLayout {

    private int i = 0;

    public TabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private View mTabView;
    private ViewPager mViewPager;

    public void setViewPager(ViewPager viewPager) {
        i = 0;
        mViewPager = viewPager;
    }


    public void addTab(View tabView) {
        tabView.setTag(i++);
        tabView.setOnClickListener(mClickListener);
        addView(tabView, new LayoutParams(0, MATCH_PARENT, 1));
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = (int) v.getTag();
            int current = mViewPager.getCurrentItem();
            mViewPager.setCurrentItem(index,false);
            if (mTabView != null) {
                if (mTabView.equals(v))
                    return;
                mTabView.setSelected(false);
            }
            v.setSelected(true);
            mTabView = v;
        }
    };

    public void setCurrentTab(int index) {
        mClickListener.onClick(getChildAt(index));
    }
}
