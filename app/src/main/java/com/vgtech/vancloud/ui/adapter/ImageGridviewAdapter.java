package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.common.image.ImageCheckActivity;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片Gridview适配器
 * Created by Duke on 2015/8/20.
 */
public class ImageGridviewAdapter extends BaseAdapter {

    Context mContext;
    List<ImageInfo> mList = new ArrayList<>();


//    public ImageGridviewAdapter(Context context, List<ImageInfo> list) {
//
//        mContext = context;
//        mList = list;
//
//    }

    private int mWh;

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list) {

        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mWh = (width - Utils.convertDipOrPx(context, 12 + 12 + (column - 1) * 5)) / column;
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list, int itemWh, int other) {
        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        mWh =itemWh;
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list, int spitWidth) {

        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels - spitWidth;
        mWh = (width - Utils.convertDipOrPx(context, 12 + 12 + (column - 1) * 5)) / column;
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list, boolean isSchedul) {

        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        width = width - Utils.convertDipOrPx(context, 80);
        mWh = (width - Utils.convertDipOrPx(context, 12 + 12 + (column - 1) * 5)) / column;
    }

    private void addImage(ImageInfo imageInfo) {
        mList.add(imageInfo);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.image_gridview_item_layout, null);

            mViewHolder.imageView = (SimpleDraweeView) convertView.findViewById(R.id.image);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.imageView.setLayoutParams(new FrameLayout.LayoutParams(mWh, mWh));
        ImageOptions.setImage(mViewHolder.imageView,mList.get(position).thumb);
        mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ImageCheckActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("listjson", new Gson().toJson(mList));
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }


    private class ViewHolder {

        SimpleDraweeView imageView;
    }
}
