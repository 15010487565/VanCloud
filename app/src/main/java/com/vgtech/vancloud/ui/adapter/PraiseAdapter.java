package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.NewUser;
import com.vgtech.vancloud.R;
import com.vgtech.common.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 赞列表适配器
 * Created by Duke on 2015/8/18.
 */
public class PraiseAdapter extends BaseAdapter {

    Context context;
    List<NewUser> userList = new ArrayList<NewUser>();


    public PraiseAdapter(Context context, List<NewUser> userList) {

        this.context = context;
        this.userList = userList;
    }


    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.praiselist_item_layout, null);
            mViewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
            mViewHolder.nameView.setTextColor(context.getResources().getColor(R.color.comment_name));
            mViewHolder.photoView = (SimpleDraweeView) convertView.findViewById(R.id.photo);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if (userList.size() > 0) {

            NewUser user = userList.get(position);
            mViewHolder.nameView.setText(Html.fromHtml(user.name));
            mViewHolder.photoView.setTag(user.userid);
            GenericDraweeHierarchy hierarchy = mViewHolder.photoView.getHierarchy();
            hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
            hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
            mViewHolder.photoView.setImageURI(user.photo);
            UserUtils.enterUserInfo(context, user.userid + "", user.name, user.photo, mViewHolder.photoView);
        }

        return convertView;
    }

    class ViewHolder {
        TextView nameView;
        SimpleDraweeView photoView;
    }

    public void myNotifyDataSetChanged(List<NewUser> list) {

        this.userList = list;
        notifyDataSetChanged();

    }


}
