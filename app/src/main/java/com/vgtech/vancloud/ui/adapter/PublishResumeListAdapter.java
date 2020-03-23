package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.common.api.PublishResumeList;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by code on 2016/6/1.
 * 新版简历列表，搜索列表，删除列表共用适配器
 */
public class PublishResumeListAdapter extends BaseAdapter {
    Context context;
    List<PublishResumeList> mlist = new ArrayList<>();
    Activity activity;
    private BaseActivity baseActivity;
    int mPosition;
    private OnSelectListener mListener;
    private boolean isCheckbox = false;

    private int type;

    public void setType(int type) {
        this.type = type;
    }

    public void setIsCheckbox(boolean isCheckbox) {
        this.isCheckbox = isCheckbox;
    }

    public List<PublishResumeList> getMlist() {
        return mlist;
    }

    public PublishResumeListAdapter(Context context, List<PublishResumeList> list) {
        this.context = context;
        this.mlist = list;
        baseActivity = (BaseActivity) context;
    }

//    public PublishResumeListAdapter(Context context, List<PublishResumeList> list, int type) {
//        this.context = context;
//        this.mlist = list;
//        baseActivity = (BaseActivity) context;
//    }

    public void setOnSelectListener(OnSelectListener listener) {
        mListener = listener;
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
        final PublishResumeList publishResumeList = mlist.get(position);
        mPosition = position;
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_resume_buy, null);

            mViewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            mViewHolder.price_layout = (LinearLayout) convertView.findViewById(R.id.price_layout);
            mViewHolder.price_tv = (TextView) convertView.findViewById(R.id.price_tv);
            mViewHolder.user_photo = (ImageView) convertView.findViewById(R.id.user_photo);
            mViewHolder.sex_iv = (ImageView) convertView.findViewById(R.id.sex_iv);
            mViewHolder.record_tv = (TextView) convertView.findViewById(R.id.record_tv);
            mViewHolder.job_type_tv = (TextView) convertView.findViewById(R.id.job_type_tv);
            mViewHolder.wish_job_tv = (TextView) convertView.findViewById(R.id.wish_job_tv);
            mViewHolder.paymoney_tv = (TextView) convertView.findViewById(R.id.paymoney_tv);
            mViewHolder.city_tv = (TextView) convertView.findViewById(R.id.city_tv);
            mViewHolder.video_icon = (ImageView) convertView.findViewById(R.id.video_icon);
            mViewHolder.time_tv = (TextView) convertView.findViewById(R.id.time_tv);

            mViewHolder.select = (CheckBox) convertView.findViewById(R.id.checkbox_list_item);
            mViewHolder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    PublishResumeList item = (PublishResumeList) buttonView.getTag();
                    if (isChecked) {
                        if (mListener != null) {
                            mListener.OnSelected(item);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.OnUnSelected(item);
                        }
                    }
                }
            });
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.name_tv.setText(publishResumeList.fullname_base);
        if (!TextUtils.isEmpty(publishResumeList.price) && "N".equals(publishResumeList.is_free)) {
            mViewHolder.price_tv.setText(publishResumeList.price + context.getResources().getString(R.string.recruit_price));
            mViewHolder.price_layout.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.price_layout.setVisibility(View.GONE);
        }
        mViewHolder.record_tv.setText(publishResumeList.degree_base);
        mViewHolder.wish_job_tv.setText(context.getResources().getString(R.string.recruit_position) + publishResumeList.jobtitle_other);
        mViewHolder.paymoney_tv.setText(publishResumeList.salaryrange_other);
        mViewHolder.city_tv.setText(publishResumeList.city_base);
        if (type == 2) {
//            mViewHolder.paymoney_tv.setVisibility(View.GONE);
            mViewHolder.time_tv.setText(baseActivity.getResources().getString(R.string.recruit_search_time) + Utils.dateFormatDate(Long.valueOf(publishResumeList.send_date)));
        } else {
//            mViewHolder.paymoney_tv.setVisibility(View.VISIBLE);
            mViewHolder.time_tv.setText(baseActivity.getResources().getString(R.string.recruit_time) + Utils.dateFormatDate(Long.valueOf(publishResumeList.send_date)));
        }

        if (context.getString(R.string.vancloud_male).equals(publishResumeList.gender_base)) {
            mViewHolder.sex_iv.setImageResource(R.mipmap.icon_sex_man);
        } else {
            mViewHolder.sex_iv.setImageResource(R.mipmap.icon_sex_women);
        }

        if ("text".equals(publishResumeList.resume_type)) {
            mViewHolder.video_icon.setVisibility(View.GONE);
        } else {
            mViewHolder.video_icon.setVisibility(View.VISIBLE);
        }

        if (isCheckbox) {
            if ("Y".equals(publishResumeList.is_free)) {
                mViewHolder.select.setVisibility(View.GONE);
            } else {
                mViewHolder.select.setVisibility(View.VISIBLE);
            }
        } else {
            mViewHolder.select.setVisibility(View.GONE);
        }
        mViewHolder.select.setTag(publishResumeList);
        boolean isSelect = (mListener != null && mListener.OnIsSelect(publishResumeList)) ? true : false;
        mViewHolder.select.setChecked(isSelect);

        return convertView;
    }

    private class ViewHolder {
        ImageView user_photo;
        ImageView sex_iv;
        ImageView video_icon;
        TextView name_tv;
        TextView price_tv;
        LinearLayout price_layout;
        TextView record_tv;
        TextView job_type_tv;
        TextView wish_job_tv;
        TextView paymoney_tv;
        TextView time_tv;
        TextView city_tv;
        CheckBox select;

    }

    public void removeItemAction(int position) {
        PublishResumeList publishResumeList = mlist.get(position);
        mlist.remove(publishResumeList);
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<PublishResumeList> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public interface OnSelectListener {
        /**
         * 选中
         */
        void OnSelected(PublishResumeList item);

        /**
         * 取消选中
         */
        void OnUnSelected(PublishResumeList item);

        /**
         * 判断是否选中
         */
        boolean OnIsSelect(PublishResumeList item);
    }
}
