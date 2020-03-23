package com.vgtech.vancloud.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.module.luntan.LuntanList;
import com.vgtech.vancloud.ui.module.luntan.LuntanReplyList;
import com.vgtech.vancloud.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Data:  2017/8/9
 * Auther: 陈占洋
 * Description:
 */

public class LuntanReplyListAdapter extends BaseAdapter {

    public static final int HEAD = 0;
    public static final int ITEM = 1;

    private LuntanList.DataBean.RowsBean mPost;
    private List<LuntanReplyList.DataBean.RowsBean> mData;

    public LuntanReplyListAdapter(LuntanList.DataBean.RowsBean post) {
        mPost = post;
    }

    @Override
    public int getCount() {
        return mData == null ? 1 : mData.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD;
        } else {
            return ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        HeadViewHolder headViewHolder = null;
        ItemViewHolder itemViewHolder = null;
        if (convertView == null) {
            if (type == HEAD) {
                headViewHolder = new HeadViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_list_head, null);
                headViewHolder.head_img = (SimpleDraweeView) convertView.findViewById(R.id.reply_list_head_img);
                headViewHolder.name = (TextView) convertView.findViewById(R.id.reply_list_head_name);
                headViewHolder.time = (TextView) convertView.findViewById(R.id.reply_list_head_time);
                headViewHolder.title = (TextView) convertView.findViewById(R.id.reply_list_head_title);
                headViewHolder.content = (TextView) convertView.findViewById(R.id.reply_list_head_content);
                convertView.setTag(headViewHolder);
            }
            if (type == ITEM) {
                itemViewHolder = new ItemViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_list_item, null);
                itemViewHolder.head_img = (SimpleDraweeView) convertView.findViewById(R.id.reply_list_item_img);
                itemViewHolder.name = (TextView) convertView.findViewById(R.id.reply_list_item_name);
                itemViewHolder.num = (TextView) convertView.findViewById(R.id.reply_list_item_num);
                itemViewHolder.content = (TextView) convertView.findViewById(R.id.reply_list_item_content);
                itemViewHolder.time = (TextView) convertView.findViewById(R.id.reply_list_item_time);
                convertView.setTag(itemViewHolder);
            }
        } else {
            if (type == HEAD) {
                headViewHolder = (HeadViewHolder) convertView.getTag();
            }
            if (type == ITEM) {
                itemViewHolder = (ItemViewHolder) convertView.getTag();
            }
        }

        if (type == HEAD) {
            headViewHolder.head_img.setImageURI(mPost.getLogo());
            headViewHolder.name.setText(mPost.getUsername());
            headViewHolder.time.setText(TimeUtils.newGetTimePassedDesc(Long.valueOf(mPost.getUpdateTime())));
            headViewHolder.title.setText(mPost.getTitle());
            headViewHolder.content.setText(mPost.getContent());
        }
        if (type == ITEM) {
            LuntanReplyList.DataBean.RowsBean rowsBean = mData.get(position - 1);
            itemViewHolder.head_img.setImageURI(rowsBean.getLogo());
            itemViewHolder.name.setText(rowsBean.getUsername());
            itemViewHolder.num.setText(rowsBean.getFloorNum() + "#");
            itemViewHolder.content.setText(rowsBean.getReplyContent());
            itemViewHolder.time.setText(TimeUtils.newGetTimePassedDesc(Long.valueOf(rowsBean.getCreateTime())));
        }

        return convertView;
    }

    public void setData(List<LuntanReplyList.DataBean.RowsBean> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    public void addData(List<LuntanReplyList.DataBean.RowsBean> data) {
        this.mData.addAll(data);
        this.notifyDataSetChanged();
    }

    public class HeadViewHolder {
        SimpleDraweeView head_img;
        TextView name;
        TextView time;
        TextView title;
        TextView content;
    }

    public class ItemViewHolder {
        SimpleDraweeView head_img;
        TextView name;
        TextView num;
        TextView content;
        TextView time;
    }
}
