package com.vgtech.common.image;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.vgtech.common.R;
import com.vgtech.common.api.ImageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片Gridview适配器
 * Created by Duke on 2015/8/20.
 */
public class ImageGridviewAdapter extends BaseAdapter {

    public static final String TYPE = "common";
    public static final String CARDINFO = "CardInfo";
    Context mContext;
    List<ImageInfo> mList = new ArrayList<>();


//    public ImageGridviewAdapter(Context context, List<ImageInfo> list) {
//
//        mContext = context;
//        mList = list;
//
//    }

    private int mWh;

    public ImageGridviewAdapter(GridView gridView, Context context,int column, List<ImageInfo> list) {
        mContext = context;
        mList = list;
//        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mWh = (width - convertDipOrPx(context, 70 + (column - 1) * 5)) / column;
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list) {
        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mWh = (width - convertDipOrPx(context, 70 + (column - 1) * 5)) / column;
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list, String type) {
        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        if (TYPE.equals(type)) {
            mWh = (width - convertDipOrPx(context, 70 + (column - 1) * 5)) / column;
        } else if (CARDINFO.equals(type)) {
            mWh = (width - convertDipOrPx(context, 40 + (column - 1) * 5)) / column;
        } else {
            mWh = (width - convertDipOrPx(context, 12 + 12 + (column - 1) * 5)) / column;
        }

    }


    // dip--px
    public static int convertDipOrPx(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list, int itemWh, int other) {
        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        mWh = itemWh;
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list, int spitWidth) {

        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels - spitWidth;
        mWh = (width - convertDipOrPx(context, 12 + 12 + (column - 1) * 5)) / column;
    }

    public ImageGridviewAdapter(GridView gridView, Context context, List<ImageInfo> list, boolean isSchedul) {

        mContext = context;
        mList = list;
        int column = list.size() > 1 ? 3 : 2;
        gridView.setNumColumns(column);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        width = width - convertDipOrPx(context, 80);
        mWh = (width - convertDipOrPx(context, 12 + 12 + (column - 1) * 5)) / column;
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
        GenericDraweeHierarchy hierarchy = mViewHolder.imageView.getHierarchy();
        hierarchy.setPlaceholderImage(R.drawable.img_default);
        hierarchy.setFailureImage(R.drawable.img_default);
//        int width = 100, height = 100;
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(mList.get(position).thumb))
                .setResizeOptions(new ResizeOptions(mWh, mWh))

                .setAutoRotateEnabled(true)
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(mViewHolder.imageView.getController())
                .setImageRequest(request)
                .build();
        mViewHolder.imageView.setController(controller);

//
//        mViewHolder.imageView.setImageURI(mList.get(position).thumb);


        mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.vgtech.imagecheck");
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
