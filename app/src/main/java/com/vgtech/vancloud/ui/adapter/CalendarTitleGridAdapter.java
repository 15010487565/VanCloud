package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.vancloud.R;


/**
 * Created by Duke on 2015/9/16.
 */
public class CalendarTitleGridAdapter extends BaseAdapter {

    int[] titles = new int[]{R.string.sun, R.string.mon, R.string.tue,
            R.string.wed, R.string.thu, R.string.fri, R.string.sat};

    private Activity activity;

    public CalendarTitleGridAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return titles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout iv = new LinearLayout(activity);
        TextView txtDay = new TextView(activity);
        txtDay.setFocusable(false);
        txtDay.setBackgroundColor(Color.TRANSPARENT);
        iv.setOrientation(LinearLayout.VERTICAL);
        txtDay.setGravity(Gravity.CENTER);
        txtDay.setTextSize(12);
        txtDay.setPadding(0,30,0,30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        txtDay.setTextColor(activity.getResources().getColor(R.color.white));
        txtDay.setText((Integer) getItem(position));
        iv.addView(txtDay, lp);
        return iv;
    }
}

