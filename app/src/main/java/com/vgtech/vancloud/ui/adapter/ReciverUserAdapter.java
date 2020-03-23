package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.NewUser;
import com.vgtech.vancloud.R;

import java.util.List;

/**
 * Created by Duke on 2016/10/20.
 */

public class ReciverUserAdapter extends BaseAdapter {

    private Context context;
    private List<NewUser> list;


    public ReciverUserAdapter(List<NewUser> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.reciver_user_item_layout, null);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        NewUser user = list.get(position);
        mViewHolder.userNameView.setText(user.name);
        GenericDraweeHierarchy hierarchy = mViewHolder.userPhotoView.getHierarchy();
        hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
        hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
        mViewHolder.userPhotoView.setImageURI(user.photo);
        return convertView;
    }

    class ViewHolder {

        TextView userNameView;
        SimpleDraweeView userPhotoView;

    }

    public void add(List<NewUser> list) {
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<NewUser> list) {

        this.list = list;
        notifyDataSetChanged();
    }

    public List<NewUser> getList() {
        return list;
    }

}
