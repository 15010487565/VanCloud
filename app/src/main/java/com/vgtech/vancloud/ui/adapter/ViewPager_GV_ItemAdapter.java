package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.XMLResParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by code on 2016/9/19.
 */
public class ViewPager_GV_ItemAdapter extends BaseAdapter {

    private List<XMLResParser.AppMenu> list_info;
    private Context context;
    private boolean isMove;
    /**
     * ViewPager页码
     */
    private int index;
    /**
     * 根据屏幕大小计算得到的每页item个数
     */
    private int pageItemCount;
    /**
     * 传进来的List的总长度
     */
    private int totalSize;
    private int mWh;
    /**
     * 当前页item的实际个数
     */
    // private int itemRealNum;
    @SuppressWarnings("unchecked")
    public ViewPager_GV_ItemAdapter(Context context, List<?> list) {
        this.context = context;
        this.list_info = (List<XMLResParser.AppMenu>) list;
    }

    public ViewPager_GV_ItemAdapter(Context context, List<?> list, int index, int pageItemCount) {
        this.context = context;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mWh = width/3;
        this.index = index;
        this.pageItemCount = pageItemCount;
        list_info = new ArrayList<XMLResParser.AppMenu>();
        totalSize = list.size();
        // itemRealNum=list.size()-index*pageItemCount;
        // 当前页的item对应的实体在List<?>中的其实下标
        int list_index = index * pageItemCount;
        for (int i = list_index; i < list.size(); i++) {
            list_info.add((XMLResParser.AppMenu) list.get(i));
        }

    }

    @Override
    public int getCount() {
        int size = totalSize / pageItemCount;
        if (index == size)
            return totalSize - pageItemCount * index;
        else
            return pageItemCount;
    }

    @Override
    public Object getItem(int position) {
        return list_info.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder iv;
        if (convertView == null) {
            iv = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.channel_gridview_item, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(mWh, mWh));
            iv.iv_icon = (ImageView) convertView.findViewById(R.id.iv_gv_item_icon);
            iv.tv_name = (TextView) convertView.findViewById(R.id.tv_gv_item_Name);
            convertView.setTag(iv);
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            scaleViewAnimation(v, 1.2f);
                            isMove = false;
                            new Handler().postDelayed(new Runnable(){
                                public void run() {
                                    if (!isMove) {
                                        scaleViewAnimation(v, 1);
                                    }
                                }
                            }, 300);
                            break;
                    }
                    return isMove;
                }
            });
        } else {
            iv = (ViewHolder) convertView.getTag();
        }
        iv.updateViews(position, null);
        return convertView;
    }

    private void scaleViewAnimation(View view, float value) {
        view.animate().scaleX(value).scaleY(value).setDuration(80).start();
    }

    class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;

        protected void updateViews(int position, Object inst) {
            iv_icon.setImageResource(list_info.get(position).getIcon());
            iv_icon.setBackgroundResource(R.drawable.round_todo);
            GradientDrawable myGrad = (GradientDrawable) iv_icon.getBackground();
            Resources resources = context.getResources();
            int color = resources.getColor(list_info.get(position).getColor());
            myGrad.setColor(color);
            tv_name.setText(list_info.get(position).getName()+"");
        }
    }


}
