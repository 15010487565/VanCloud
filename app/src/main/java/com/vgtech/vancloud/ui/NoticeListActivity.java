package com.vgtech.vancloud.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vancloud.ui.fragment.NoticeNewFragment;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ViewPagerAdapter;
import com.vgtech.vantop.ui.view.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2017/2/24.
 */
public class NoticeListActivity extends BaseActivity implements
        ViewPager.OnPageChangeListener {
    private ViewPager mVpger;
    private ViewPagerIndicator mIndicator;
    private List<String> mTabs;
    private ViewPagerAdapter mPagerAdapter;
    private List<Fragment> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.notice_title));
        PrfUtils.setMessageCountCount(this, PrfUtils.MESSAGE_NOTICE, 0);
        initData();
        initViews();
    }

    private void initViews() {

        mIndicator = (ViewPagerIndicator) findViewById(R.id.indicator_vp);
        mVpger = (ViewPager) findViewById(R.id.vp_menu);
        mVpger.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mVpger, 0);
        mIndicator.setTabItemTitles(mTabs);
        mIndicator.setOnPageChangeListenner(this);
    }


    private void initData() {
        mTabs = new ArrayList<>();
        mDatas = new ArrayList<>();
        mTabs.add(getString(R.string.notice_tobedone));
        mDatas.add(NoticeNewFragment.getFragment("0"));
        mTabs.add(getString(R.string.notice_readydone));
        mDatas.add(NoticeNewFragment.getFragment("1"));
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mDatas);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_noticelist;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
