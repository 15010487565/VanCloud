package com.vgtech.common.config;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.R;

/**
 * Created by Duke on 2015/9/7.
 */
public class ImageOptions {


    /**
     * 设置用户头像
     *
     * @param simpleDraweeView
     * @param photoUrl
     */
    public static void setUserImage(SimpleDraweeView simpleDraweeView, String photoUrl) {

//        GenericDraweeHierarchy hierarchy = simpleDraweeView.getHierarchy();
//        hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
//        hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
//        simpleDraweeView.setImageURI(photoUrl);
        RoundingParams roundingParams = new RoundingParams();
        roundingParams.setRoundAsCircle(true);
        GenericDraweeHierarchy hierarchy = simpleDraweeView.getHierarchy();
        hierarchy.setRoundingParams(roundingParams);
        hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
        hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
        simpleDraweeView.setImageURI(photoUrl);
    }


    /**
     * 设置图片
     *
     * @param simpleDraweeView
     * @param photoUrl
     */
    public static void setImage(SimpleDraweeView simpleDraweeView, String photoUrl) {
        GenericDraweeHierarchy hierarchy = simpleDraweeView.getHierarchy();
        hierarchy.setPlaceholderImage(R.drawable.img_default);
        hierarchy.setFailureImage(R.drawable.img_default);
        simpleDraweeView.setImageURI(photoUrl);
    }
}
