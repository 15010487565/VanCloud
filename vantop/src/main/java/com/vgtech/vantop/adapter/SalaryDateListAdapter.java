package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.SalaryDateData;

import java.util.List;

/**
 * 日期查询工资适配器
 * Created by shilec on 2016/9/12.
 */
public class SalaryDateListAdapter extends BaseAdapter {

    private Context mContext;
    private List<SalaryDateData> mDatas;
    private LayoutInflater mInflater;

    public SalaryDateListAdapter(Context context,List<SalaryDateData> datas) {
        mDatas = datas;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }
  /*  @Override
    public int getGroupCount() {
        return mDatas.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mDatas.get(i).items.size();
    }*/

    /*@Override
    public Object getGroup(int i) {
        return mDatas.get(i).month;
    }

    @Override
    public Object getChild(int i, int i1) {
        return mDatas.get(i).items.get(i1);
    }*/

  /*  @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }*/

    @Override
    public int getCount() {
        if(mDatas.isEmpty())
            return 0;
        return mDatas.get(0).items.size();
    }

    @Override
    public Object getItem(int i) {
        if(mDatas.isEmpty())
            return null;
        return mDatas.get(0).items.get(i);
    }

    @Override
    public long getItemId(int i) {
        if(mDatas.isEmpty())
            return 0;
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*@Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        TextView tvTitle;
        if(view == null) {
            view = mInflater.inflate(R.layout.salary_date_list_group_item,viewGroup,false);
            tvTitle = (TextView) view.findViewById(R.id.tv_groupname);
            view.setTag(tvTitle);
        } else {
            tvTitle = (TextView) view.getTag();
        }
        tvTitle.setText(mDatas.get(i).month);
        return view;
    }*/

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder h;
        if(view == null) {
            view = mInflater.inflate(R.layout.salary_date_child_list_item,viewGroup,false);
            h = new ViewHolder();
            h.tvLabel = (TextView) view.findViewById(R.id.tv_label);
            h.tvValue = (TextView) view.findViewById(R.id.tv_value);
            view.setTag(h);
        } else {
            h = (ViewHolder) view.getTag();
        }

        h.tvLabel.setText(mDatas.get(0).items.get(i).label);
        h.tvValue.setText(mDatas.get(0).items.get(i).value);
        return view;
    }

   /* @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }*/

    private class ViewHolder {
        TextView tvLabel;
        TextView tvValue;
    }
}