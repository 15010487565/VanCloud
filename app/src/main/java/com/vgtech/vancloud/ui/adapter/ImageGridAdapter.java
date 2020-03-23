package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.common.image.ImageCheckActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片Gridview适配器
 * Created by Duke on 2015/8/20.
 */
public class ImageGridAdapter extends BaseAdapter {

    Context mContext;
    List<String> mList = new ArrayList<>();


    public ImageGridAdapter(Context context, List<String> list) {

        mContext = context;
        mList = list;

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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, null);

            mViewHolder.imageView = (SimpleDraweeView) convertView.findViewById(R.id.image);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        ImageOptions.setImage(mViewHolder.imageView,mList.get(position));

        mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, ImageCheckActivity.class);
                intent.putExtra("position", position);

                intent.putExtra("list", (Serializable)mList);

                mContext.startActivity(intent);

            }
        });
        return convertView;
    }


    private class ViewHolder {

        SimpleDraweeView imageView;
    }
}
