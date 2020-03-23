package com.vgtech.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by code on 2016/3/7.
 */
public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {

    protected ArrayList<T> mData;
    protected LayoutInflater mInflater;
    protected Context mContext;


    public BaseAdapter(Context context) {
        this(context, null);
    }

    public BaseAdapter(Context context, ArrayList<T> data) {
        mData = data == null ? new ArrayList<T>() : new ArrayList<T>(data);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    public void clearData() {
        mData.clear();
    }
    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public void addAllData(List<T> list) {
        if (list!=null) {
            mData.addAll(list);
        }
    }

    public void addAllDataAndNorify(List<T> list) {
        this.addAllData(list);
        this.notifyDataSetChanged();
    }
    public void remove(T dada)
    {
        mData.remove(dada);
        notifyDataSetChanged();
    }
    public void addDataAndNorify(T data) {
        mData.add(data);
        this.notifyDataSetChanged();
    }

    public ArrayList<T> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
