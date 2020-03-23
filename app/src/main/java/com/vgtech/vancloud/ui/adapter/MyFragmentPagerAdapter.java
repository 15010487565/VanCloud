package com.vgtech.vancloud.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 *
 * Created by code on 15/12/9.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentsList;

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragmentsList = fragments;
    }
    public void update(List<Fragment> fragments)
    {
        this.fragmentsList = fragments;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return fragmentsList.size();
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragmentsList.get(arg0);
    }
}
