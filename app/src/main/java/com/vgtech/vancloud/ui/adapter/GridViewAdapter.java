package com.vgtech.vancloud.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by app02 on 2015/8/26.
 */
public class GridViewAdapter extends BaseAdapter{

    private List<View> data;
    public GridViewAdapter(List<View> data){
        this.data=data;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        return data.get(position);
    }
}
