package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片Gridview适配器
 * Created by Duke on 2015/8/20.
 */
public class UserImageGridAdapter extends BaseAdapter {

    Context mContext;
    List<NewUser> mList = new ArrayList<>();

    public UserImageGridAdapter(GridView gridView, Context context, List<NewUser> list) {
        mContext = context;
        mList = list;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        width = width - Utils.convertDipOrPx(context, 50);
        int columns = width / Utils.convertDipOrPx(context, 40);
        gridView.setNumColumns(columns);
    }

    public boolean contains(NewUser user) {
        return mList.contains(user);
    }

    public void remove(NewUser user) {
        mList.remove(user);
        notifyDataSetChanged();
    }

    public List<NewUser> getList() {
        return mList;
    }

    public void add(NewUser user) {
        mList.add(0, user);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_icon_small, null);

            mViewHolder.imageView = (SimpleDraweeView) convertView.findViewById(R.id.image);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        ImageOptions.setUserImage(mViewHolder.imageView,mList.get(position).photo);
        mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewUser user = mList.get(position);
                UserUtils.enterUserInfo(mContext, user.userid, user.name, user.photo);
            }
        });
        return convertView;
    }


    private class ViewHolder {

        SimpleDraweeView imageView;
    }
}
