package com.vgtech.common.image;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.vgtech.common.R;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.view.IphoneDialog;

import java.util.List;

/**
 * Created by zhangshaofang on 2016/1/7.
 */
public class ImgGridAdapter extends BaseAdapter {
    private LayoutInflater inflater; // 视图容器
    private int selectedPosition = -1;// 选中的位置
    private boolean shape;
    public boolean isShape() {
        return shape;
    }
    public List<String> getImage() {
        return Bimp.drr;
    }
    public void setShape(boolean shape) {
        this.shape = shape;
    }

    private Context mContext;
    public View mRightTv;

    public ImgGridAdapter(Context context, View rightTv) {
        mContext = context;
        mRightTv = rightTv;
        inflater = LayoutInflater.from(context);
    }

    public void update() {
        loading();
    }

    public int getCount() {
        return (Bimp.drr.size() + 1);
    }

    public Object getItem(int arg0) {

        return null;
    }
    public long getItemId(int arg0) {

        return 0;
    }

    /**
     * ListView Item设置
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        final int coord = position;
        ViewHolder holder = null;
        if (convertView == null) {

            convertView = inflater.inflate(R.layout.item_published_grida,
                    parent, false);
            holder = new ViewHolder();
            holder.image = (SimpleDraweeView) convertView
                    .findViewById(R.id.item_grida_image);
            holder.deleteView = convertView
                    .findViewById(R.id.btn_delete);
            holder.deleteView.setOnClickListener(deleteListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == Bimp.drr.size()) {
            holder.image.setImageResource(R.drawable.icon_addpic_unfocused);
            holder.deleteView.setVisibility(View.GONE);
            if (position == 9) {
                {
                    holder.image.setVisibility(View.GONE);
                }
            }
        } else {
            holder.deleteView.setVisibility(View.VISIBLE);
            holder.deleteView.setTag(position);
            String path = Bimp.drr.get(position);
            if (ImageInfo.isLocal(path)) {
                if(!path.equals(holder.image.getTag()))
                {
                    holder.image.setTag(path);
//                    ImageOptions.setImage(holder.image,"file://" + path);
                    int width = 100, height = 100;
                    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://" + path))
                            .setResizeOptions(new ResizeOptions(width, height))
                            .setAutoRotateEnabled(true)
                            .build();
                    PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                            .setOldController(holder.image.getController())
                            .setImageRequest(request)
                            .build();
                    holder.image.setController(controller);


                }

            } else {
                ImageOptions.setImage(holder.image,path);
            }
        }

        return convertView;
    }

    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.btn_delete)
            {
                int position = (int) v.getTag();
                if ( position < getImage().size()) {
                    Bimp.drr.remove(position);
                    update();
                }
            }
        }
    };
    public class ViewHolder {
        public SimpleDraweeView image;
        public View deleteView;
    }
    private IphoneDialog iphoneDialog;
    /**
     * @param mContext
     * @param contentStr
     */
    public void showLoadingDialog(Context mContext, String contentStr) {
        if (iphoneDialog == null) {
            iphoneDialog = new IphoneDialog(mContext);
        }
        iphoneDialog.setMessage(contentStr);
        iphoneDialog.show(true);
    }

    public synchronized void loading() {
        notifyDataSetChanged();
    }
}