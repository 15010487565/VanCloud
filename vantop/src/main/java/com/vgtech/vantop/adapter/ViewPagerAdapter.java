package com.vgtech.vantop.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by shilec on 2016/9/5.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mDatas;

    private ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public ViewPagerAdapter(FragmentManager fm,List<Fragment> datas) {
        this(fm);
        mDatas = datas;
    }

    @Override
    public Fragment getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }
}
