package com.vgtech.vancloud.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.adapter.BaseSimpleAdapter;
import com.vgtech.common.api.AppModule;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.Utils;

/**
 * Created by code on 2016/9/5.
 */
public class AppListAdapter extends BaseSimpleAdapter<AppModule> {
    private int mBgRadius;

    public AppListAdapter(Context context) {
        super(context);
        mBgRadius = Utils.convertDipOrPx(mContext, 10);
    }

    @Override
    public int getItemResource(int viewType) {
        return R.layout.item_see_more;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        ImageView icon = holder.getView(R.id.module_icon);
        TextView title = holder.getView(R.id.module_tv);
        TextView type = holder.getView(R.id.module_type);
        AppModule item = getItem(position);
        title.setText(item.resName == 0 ? item.name : mContext.getString(item.resName));
        icon.setImageResource(item.resIcon == 0 ? R.mipmap.ic_launcher : item.resIcon);
        int roundRadius = mBgRadius; // 8dp 圆角半径
        int fillColor = mContext.getResources().getColor(item.resColor == 0 ? R.color.bg_title : item.resColor);//内部填充颜色
        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(fillColor);
        gd.setCornerRadius(roundRadius);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            icon.setBackgroundDrawable(gd);
        } else {
            icon.setBackground(gd);
        }
        if (!item.isOpen()) {
            type.setTextColor(mContext.getResources().getColor(R.color.bg_title));
            type.setText(mContext.getString(R.string.app_see_detail));//查看详情
            type.setBackgroundResource(R.drawable.bg_blue_white);
        } else {
            type.setText(mContext.getString(R.string.app_has_open));//已开通
            type.setTextColor(mContext.getResources().getColor(R.color.out_line));
            type.setBackgroundResource(R.drawable.bg_gray_white);
        }
        return convertView;
    }
}
