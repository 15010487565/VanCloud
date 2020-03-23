package com.vgtech.vancloud.ui.view.cycle;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.ADInfo;

public class ViewFactory {

    public static View generaItemView(final Context context, final ADInfo adInfo, final CycleViewPager cycleViewPager) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_banner, null);
        SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.background);
        imageView.setImageURI(adInfo.picture_address);
        imageView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
        int width = context.getResources().getDisplayMetrics().widthPixels;
//        int bmWidth = 750;
//        float be = width / bmWidth;
        int height = (int) (width / 3f);
        imageView.setAspectRatio(3f);
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(adInfo.picture_address))
//                .setResizeOptions(new ResizeOptions(width, height))
//                .setAutoRotateEnabled(true)
//                .build();
//        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
//                .setOldController(imageView.getController())
//                .setImageRequest(request)
//                .build();
//        imageView.setController(controller);
        TextView titleTv = (TextView) view.findViewById(R.id.tv_title);
        titleTv.setText(adInfo.title);
        cycleViewPager.getView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return view;
    }

    public static View generaItemView(final Context context, final ADInfo adInfo, int i, final CycleViewPager cycleViewPager) {
        int[] adNormalIds = {R.mipmap.ad_normal_1, R.mipmap.ad_normal_2, R.mipmap.ad_normal_3, R.mipmap.ad_normal_4, R.mipmap.ad_normal_5};

        View view = LayoutInflater.from(context).inflate(
                R.layout.view_banner, null);
        SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.background);
        imageView.setImageURI(adInfo.picture_address);
        imageView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
        imageView.getHierarchy().setFailureImage(adNormalIds[i]);
        int width = context.getResources().getDisplayMetrics().widthPixels;
//        int bmWidth = 750;
//        float be = width / bmWidth;
        int height = (int) (width / 3f);
        imageView.setAspectRatio(3f);
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(adInfo.picture_address))
//                .setResizeOptions(new ResizeOptions(width, height))
//                .setAutoRotateEnabled(true)
//                .build();
//        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
//                .setOldController(imageView.getController())
//                .setImageRequest(request)
//                .build();
//        imageView.setController(controller);
        TextView titleTv = (TextView) view.findViewById(R.id.tv_title);
        titleTv.setText(adInfo.title);
        cycleViewPager.getView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return view;
    }
}
