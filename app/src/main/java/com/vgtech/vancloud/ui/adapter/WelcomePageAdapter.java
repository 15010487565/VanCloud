package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Duke on 2015/11/6.
 */
public class WelcomePageAdapter extends PagerAdapter {

    List<View> lists;
    Context context;

    public WelcomePageAdapter(Context context, List<View> lists) {

        this.context = context;
        this.lists = lists;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(lists.get(position));
    }

    @Override
    public int getCount() {
        if (lists != null) {
            return lists.size();
        }
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(lists.get(position), 0);
        return lists.get(position);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

}
