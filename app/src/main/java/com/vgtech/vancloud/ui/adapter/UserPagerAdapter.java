package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.Utils;

/**
 * Created by zhangshaofang on 2015/9/9.
 */
public class UserPagerAdapter<AbsApiData> extends ApiDataAdapter<AbsApiData> {

    public UserPagerAdapter(Context context) {
        super(context);
    }

    private int selectPosition;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SparseArray<View> viewMap = null;
        if (convertView == null) {
            viewMap = new SparseArray<View>();
            convertView = mInflater.inflate(R.layout.user_icon_item, parent, false);
            viewMap.put(R.id.iv_icon, convertView.findViewById(R.id.iv_icon));
            viewMap.put(R.id.tv_name, convertView.findViewById(R.id.tv_name));
            convertView.setTag(viewMap);
        }
        viewMap = (SparseArray<View>) convertView.getTag();
        NewUser newUser = (NewUser) getItem(position);
        SimpleDraweeView imageView = (SimpleDraweeView) viewMap.get(R.id.iv_icon);
        int wh = Utils.convertDipOrPx(mContext, 40);
        if (position == selectPosition) {
            wh = Utils.convertDipOrPx(mContext, 50);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(wh, wh);
        imageView.setLayoutParams(params);
        ImageOptions.setUserImage(imageView,newUser.photo);
        TextView nameTv = (TextView) viewMap.get(R.id.tv_name);
        nameTv.setText(newUser.name);
        return convertView;
    }


}
