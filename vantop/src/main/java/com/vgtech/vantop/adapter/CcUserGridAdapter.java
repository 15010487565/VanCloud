package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.Node;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vantop.R;
import com.vgtech.vantop.ui.vacation.OnCcUserRemoveListener;

import java.util.ArrayList;

/**
 * Created by Duke on 2016/10/12.
 */

public class CcUserGridAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;
    private ArrayList<Node> list = new ArrayList<>();
    private OnCcUserRemoveListener listener;


    public CcUserGridAdapter(Context context, ArrayList<Node> list, OnCcUserRemoveListener listener) {

        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return list.size() > 10 ? 10 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_user_item, null);
            mViewHolder.nameView = (TextView) convertView.findViewById(R.id.tv_name);
            mViewHolder.photoView = (SimpleDraweeView) convertView.findViewById(R.id.ItemImage);
            mViewHolder.deleteView = (ImageView) convertView.findViewById(R.id.btn_delete_user);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Node node = list.get(position);
        ImageOptions.setUserImage(mViewHolder.photoView,node.getPhoto());
//        mViewHolder.photoView.setImageResource(R.mipmap.user_photo_default_small);
        mViewHolder.nameView.setText(node.getName());
        mViewHolder.deleteView.setOnClickListener(this);
        mViewHolder.deleteView.setTag(node);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        Node node = (Node) v.getTag();
        remove(node);
    }

    private class ViewHolder {
        TextView nameView;
        SimpleDraweeView photoView;
        ImageView deleteView;
    }

    public void remove(Node node) {
        list.remove(node);
        if (listener != null)
            listener.onRemove(node);
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(ArrayList<Node> lists) {
        list = lists;
        notifyDataSetChanged();
    }

    public ArrayList<Node> getLists() {
        return list;
    }
}
