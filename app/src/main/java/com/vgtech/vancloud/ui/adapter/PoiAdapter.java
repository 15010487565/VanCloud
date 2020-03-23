package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.vgtech.vancloud.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/11/5.
 */
public class PoiAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<PoiInfo> mPoiList;

    public PoiAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mPoiList = new ArrayList<>();
    }

    public void add(List<PoiInfo> poiInfos) {
        mPoiList.addAll(poiInfos);
        notifyDataSetChanged();
    }

    public void setmPoiList(List<PoiInfo> poiInfos) {
        this.mPoiList = poiInfos;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPoiList.size();
    }

    @Override
    public PoiInfo getItem(int position) {
        return mPoiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SparseArray<View> viewMap = null;
        if (convertView == null) {
            viewMap = new SparseArray<>();
            convertView = mInflater.inflate(R.layout.poi_item, null);
            viewMap.put(R.id.tv_name, convertView.findViewById(R.id.tv_name));
            viewMap.put(R.id.tv_address, convertView.findViewById(R.id.tv_address));
            convertView.setTag(viewMap);
        } else {
            viewMap = (SparseArray<View>) convertView.getTag();
        }
        TextView nameTv = (TextView) viewMap.get(R.id.tv_name);
        TextView addressTv = (TextView) viewMap.get(R.id.tv_address);
        PoiInfo poiInfo = getItem(position);
        nameTv.setText(poiInfo.name);
        addressTv.setText(poiInfo.address);
        return convertView;
    }

    public void clearList() {
        this.mPoiList.clear();
        notifyDataSetChanged();
    }

}
