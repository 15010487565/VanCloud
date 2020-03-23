package com.vgtech.vancloud.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.module.luntan.LuntanList;
import com.vgtech.vancloud.utils.TimeUtils;

import java.util.List;

/**
 * Data:  2017/8/4
 * Auther: 陈占洋
 * Description:
 */

public class LuntanListAdapter extends BaseAdapter {
    private List<LuntanList.DataBean.RowsBean> mData;

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LuntanListViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.luntan_list_item, parent, false);
            holder = new LuntanListViewHolder();
            holder.head_img = (SimpleDraweeView) convertView.findViewById(R.id.luntan_list_item_head_img);
            holder.name = (TextView) convertView.findViewById(R.id.luntan_list_item_name);
            holder.title = (TextView) convertView.findViewById(R.id.luntan_list_item_title);
            holder.look = (TextView) convertView.findViewById(R.id.luntan_list_item_look);
            holder.ping = (TextView) convertView.findViewById(R.id.luntan_list_item_ping);
            holder.time = (TextView) convertView.findViewById(R.id.luntan_list_item_time);
            convertView.setTag(holder);
        } else {
            holder = (LuntanListViewHolder) convertView.getTag();
        }
        LuntanList.DataBean.RowsBean rowsBean = mData.get(position);
        holder.name.setText(rowsBean.getUsername());
        holder.title.setText(rowsBean.getTitle());
        holder.look.setText(rowsBean.getVisitorCount() + "看");
        holder.ping.setText(rowsBean.getReplyCount() + "评");
        holder.time.setText(TimeUtils.newGetTimePassedDesc(Long.valueOf(rowsBean.getUpdateTime())));
        holder.head_img.setImageURI(rowsBean.getLogo());
        return convertView;
    }

    public void setData(List<LuntanList.DataBean.RowsBean> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    public void addData(List<LuntanList.DataBean.RowsBean> data) {
        this.mData.addAll(data);
        this.notifyDataSetChanged();
    }

    public List<LuntanList.DataBean.RowsBean> getData() {
        return mData;
    }

    class LuntanListViewHolder {
        SimpleDraweeView head_img;
        TextView name;
        TextView title;
        TextView look;
        TextView ping;
        TextView time;
    }
}
