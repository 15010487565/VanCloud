package com.vgtech.vancloud.ui.view.groupimageview;

import android.content.Context;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

public abstract class NineGridImageViewAdapter<T> {
    protected abstract void onDisplayImage(Context context, SimpleDraweeView imageView, T t);

    protected SimpleDraweeView generateImageView(Context context){
        SimpleDraweeView imageView = new SimpleDraweeView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }
}
