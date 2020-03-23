package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.AppPermission;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.module.approval.MyApprovalActivity;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by app02 on 2015/8/28.
 */
public class ApproveModuleAdapter extends BaseAdapter {

    private Activity mContext;
    private LayoutInflater inflater;
    private List<AppModule> data;
    private int mWh;
    private int mBgRadius;

    public ApproveModuleAdapter(Activity mContext, int numColumns) {
        data = new ArrayList<>();
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        mWh = width / numColumns;
        mBgRadius = Utils.convertDipOrPx(mContext, 10);
    }

    public void updateModuleCount(int un_approve_lea, int un_approve_car, int un_approve_ot) {
        for (AppModule item : data) {
            switch (AppPermission.WorkFlow.getType(item.tag)) {
                case extra_work: {
                    item.count = un_approve_ot;
                }
                break;
                case sign_card: {
                    item.count = un_approve_car;
                }
                break;
                case vantop_holiday: {
                    item.count = un_approve_lea;
                }
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void add(List<AppModule> appModules) {
        data.addAll(appModules);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public AppModule getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.approve_gridview_item, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(mWh, mWh));
            mViewHolder.itemText = (TextView) convertView.findViewById(R.id.work_item_txt);
            mViewHolder.itemIcon = (ImageView) convertView.findViewById(R.id.work_item_icon);
            mViewHolder.itemNum = (TextView) convertView.findViewById(R.id.work_item_num);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        AppModule item = data.get(position);
        String name = item.resName == 0 ? item.name : mContext.getString(item.resName);
//        if (item.count > 0) {
//            name = name + "(" + item.count + ")";
//        }
        mViewHolder.itemText.setText(name);
        mViewHolder.itemIcon.setBackgroundResource(item.resIcon == 0 ? R.mipmap.ic_launcher : item.resIcon);
        if (item.count < 1) {
            if (mViewHolder.itemNum.getVisibility() != View.GONE) {
                mViewHolder.itemNum.setVisibility(View.GONE);
            }
        } else {
            if (mViewHolder.itemNum.getVisibility() != View.VISIBLE) {
                mViewHolder.itemNum.setVisibility(View.VISIBLE);
            }
            mViewHolder.itemNum.setText(item.count < 100 ? String.valueOf(item.count) : "N");
        }
        return convertView;
    }

    class ViewHolder {
        TextView itemText;
        ImageView itemIcon;
        TextView itemNum;
    }

//    public void flushWorkMain() {
//        data.clear();
//        init();
//        notifyDataSetChanged();
//    }
}
