package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.api.AppModule;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.vgtech.vancloud.utils.Utils.getResources;

/**
 * Created by app02 on 2015/8/28.
 */
public class AppModuleAdapter extends BaseAdapter {

    private Activity mContext;
    private LayoutInflater inflater;
    private List<AppModule> data;
    private int mWh;
    private int mBgRadius;
    private int mWorkFlowNum;
    private int mFlowNum;

    public AppModuleAdapter(Activity mContext, int numColumns) {
        data = new ArrayList<>();
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        mWh = width / numColumns;
        mBgRadius = Utils.convertDipOrPx(mContext, 3);
    }

    public void add(List<AppModule> appModules) {
        data.addAll(appModules);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public AppModule getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.work_main_fragment_gridview_item, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(mWh, mWh));
            mViewHolder.itemText = (TextView) convertView.findViewById(R.id.work_item_txt);
            mViewHolder.itemIcon = (ImageView) convertView.findViewById(R.id.work_item_icon);
            mViewHolder.itemNum = (TextView) convertView.findViewById(R.id.work_item_num);
            //  mViewHolder.itemImg = (RelativeLayout) convertView.findViewById(R.id.item_img);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        AppModule item = data.get(position);
        if (AppModulePresenter.Type.getType(item.tag) == AppModulePresenter.Type.work_flow
                ) {
//            Log.e("TAG_WorkFragment",";mWorkFlowNum="+mWorkFlowNum);
            //显示数字
            if (mWorkFlowNum > 99) {
                mViewHolder.itemNum.setText(99 + "+");
                mViewHolder.itemNum.setVisibility(View.VISIBLE);
            } else if (mWorkFlowNum > 0){
                mViewHolder.itemNum.setText(mWorkFlowNum + "");
                mViewHolder.itemNum.setVisibility(View.VISIBLE);
            }else {
                mViewHolder.itemNum.setText("");
                mViewHolder.itemNum.setVisibility(View.GONE);
            }
        }

        if (AppModulePresenter.Type.getType(item.tag) == AppModulePresenter.Type.flow
               ) {
            //显示数字
//            Log.e("TAG_WorkFragment",";mFlowNum="+mFlowNum);
            if (mFlowNum > 99) {
                mViewHolder.itemNum.setText(99 + "+");
                mViewHolder.itemNum.setVisibility(View.VISIBLE);
            } else if (mFlowNum > 0){
                mViewHolder.itemNum.setText(mFlowNum + "");
                mViewHolder.itemNum.setVisibility(View.VISIBLE);
            }else {
                mViewHolder.itemNum.setText("");
                mViewHolder.itemNum.setVisibility(View.GONE);
            }
        }

        //原代码
        mViewHolder.itemText.setText(item.resName == 0 ? item.name : mContext.getString(item.resName));
        mViewHolder.itemIcon.setImageResource(item.resIcon == 0 ? R.mipmap.ic_launcher : item.resIcon);
        int roundRadius = mBgRadius; // 8dp 圆角半径
        int fillColor = mContext.getResources().getColor(item.resColor == 0 ? R.color.bg_title : item.resColor);//内部填充颜色
        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(fillColor);
        gd.setCornerRadius(roundRadius);
        mViewHolder.itemIcon.setBackgroundDrawable(gd);

        return convertView;
    }

    public void updateNum(AppModulePresenter.Type type, int num) {
        if (type == AppModulePresenter.Type.work_flow) {
            this.mWorkFlowNum = num;
        } else if (type == AppModulePresenter.Type.flow) {
            this.mFlowNum = num;
        }
    }

    class ViewHolder {
        TextView itemText;
        ImageView itemIcon;
        TextView itemNum;
        //  RelativeLayout itemImg;
    }

//    public void flushWorkMain() {
//        data.clear();
//        init();
//        notifyDataSetChanged();
//    }
}
