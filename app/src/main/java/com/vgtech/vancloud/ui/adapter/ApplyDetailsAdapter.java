package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.ResumeBuyBean;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

/**
 * Created by code on 2015/12/29.
 */
public class ApplyDetailsAdapter extends BaseAdapter {

    Context context;
    List<ResumeBuyBean> mlist;
    int mPosition;

    public List<ResumeBuyBean> getMlist() {
        return mlist;
    }

    public ApplyDetailsAdapter(Context context, List<ResumeBuyBean> list) {
        this.context = context;
        this.mlist = list;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        this.mlist.clear();
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mPosition = position;
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_resume_buy, null);

            mViewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            mViewHolder.price_tv = (TextView) convertView.findViewById(R.id.price_tv);
            mViewHolder.user_photo = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.record_tv = (TextView) convertView.findViewById(R.id.record_tv);
            mViewHolder.job_type_tv = (TextView) convertView.findViewById(R.id.job_type_tv);
            mViewHolder.wish_job_tv = (TextView) convertView.findViewById(R.id.wish_job_tv);
            mViewHolder.paymoney_tv = (TextView) convertView.findViewById(R.id.paymoney_tv);
            mViewHolder.city_tv = (TextView) convertView.findViewById(R.id.city_tv);
            mViewHolder.praise_icon = (ImageView) convertView.findViewById(R.id.video_icon);
            mViewHolder.time_tv = (TextView) convertView.findViewById(R.id.time_tv);
            mViewHolder.select = (CheckBox) convertView.findViewById(R.id.checkbox_list_item);
            mViewHolder.select.setVisibility(View.GONE);
            mViewHolder.praise_icon.setVisibility(View.GONE);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final ResumeBuyBean resumeBuyBean = mlist.get(position);

        if (!TextUtils.isEmpty(resumeBuyBean.photo)) {
            ImageOptions.setUserImage(mViewHolder.user_photo,resumeBuyBean.photo);
        }

        mViewHolder.name_tv.setText(resumeBuyBean.name);
        mViewHolder.price_tv.setText(resumeBuyBean.price + context.getResources().getString(R.string.recruit_price));
        mViewHolder.record_tv.setText(resumeBuyBean.degree);
        mViewHolder.wish_job_tv.setText(context.getResources().getString(R.string.recruit_position) + resumeBuyBean.position);
        mViewHolder.paymoney_tv.setText(resumeBuyBean.salary_month);
        mViewHolder.city_tv.setText(resumeBuyBean.work_city);
        mViewHolder.time_tv.setText(context.getResources().getString(R.string.recruit_time) + Utils.getInstance(context).dateFormat(resumeBuyBean.creator_time));

        return convertView;
    }

    private class ViewHolder {
        SimpleDraweeView user_photo;
        ImageView praise_icon;
        TextView name_tv;
        TextView price_tv;
        TextView record_tv;
        TextView job_type_tv;
        TextView wish_job_tv;
        TextView paymoney_tv;
        TextView time_tv;
        TextView city_tv;
        CheckBox select;

    }

    public void myNotifyDataSetChanged(List<ResumeBuyBean> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }
}
