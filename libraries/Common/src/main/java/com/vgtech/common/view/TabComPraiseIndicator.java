package com.vgtech.common.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.common.R;

import java.util.List;

/**
 * Created by vic on 2017/4/27.
 */
public class TabComPraiseIndicator extends LinearLayout implements View.OnClickListener {
    public TabComPraiseIndicator(Context context) {
        super(context);
        initView(context);
    }

    public TabComPraiseIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TabComPraiseIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private View btnComment;
    private TextView tvComment;
    private View lineComment;
    private View btnPraise;
    private TextView tvPraise;
    private View linePraise;

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.tab_compraise, null);
        btnComment = view.findViewById(R.id.btn_tab_comment);
        tvComment = (TextView) view.findViewById(R.id.tv_tab_comment);
        lineComment = view.findViewById(R.id.line_tab_comment);
        btnPraise = view.findViewById(R.id.btn_tab_praise);
        tvPraise = (TextView) view.findViewById(R.id.tv_tab_praise);
        linePraise = view.findViewById(R.id.line_tab_praise);
        btnComment.setOnClickListener(this);
        btnPraise.setOnClickListener(this);
        addView(view);
    }

    private int mSelectedTab;

    public synchronized void onSwitched(int position) {
        if (mSelectedTab == position) {
            return;
        }
        setCurrentTab(position);
    }

    public void updateTitle(int postion, String title) {
        if (postion == 0) {
            tvComment.setText(title);
        } else {
            tvPraise.setText(title);
        }
    }

    public synchronized void setCurrentTab(int index) {
        mSelectedTab = index;
        if (index == 0) {
            mViewPager.setCurrentItem(0);
            tvPraise.setSelected(false);
            linePraise.setVisibility(View.GONE);
            tvComment.setSelected(true);
            lineComment.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            mViewPager.setCurrentItem(1);
            tvComment.setSelected(false);
            lineComment.setVisibility(View.GONE);
            tvPraise.setSelected(true);
            linePraise.setVisibility(View.VISIBLE);
        }
    }

    public void onScrolled(int h) {
    }

    private ViewPager mViewPager;

    //初始化选项卡
    public void init(int startPos, List<TabInfo> tabs, ViewPager viewPager) {
        this.mViewPager = viewPager;
        if (tabs != null) {
            tvComment.setText(tabs.get(0).getName());
            tvPraise.setText(tabs.get(1).getName());
        }
        setCurrentTab(startPos);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_tab_comment) {
            setCurrentTab(0);
        } else if (id == R.id.btn_tab_praise) {
            setCurrentTab(1);
        }
    }
}
