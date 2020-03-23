package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.vgtech.common.api.RecruitmentInfoBean;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

/**
 * Created by code on 2015/12/23.
 */
public class RecruitmentInfoAdapter extends BaseAdapter {

    private OnSelectListener mListener;

    private boolean isCheckbox = false;

    public void setCheckbox(boolean checkbox) {
        isCheckbox = checkbox;
    }

    Context context;

    public List<RecruitmentInfoBean> getMlist() {
        return mlist;
    }

    public void setmListener(OnSelectListener mListener) {
        this.mListener = mListener;
    }

    List<RecruitmentInfoBean> mlist;
    int mPosition;

    public RecruitmentInfoAdapter(Context context, List<RecruitmentInfoBean> list) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        mPosition = position;
        final RecruitmentInfoBean recruitmentInfoBean = mlist.get(position);
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recruitment_info, null);
            mViewHolder.job = (TextView) convertView.findViewById(R.id.job_tv);
            mViewHolder.type = (TextView) convertView.findViewById(R.id.tv_type);
            mViewHolder.city = (TextView) convertView.findViewById(R.id.city_tv);
            mViewHolder.num = (TextView) convertView.findViewById(R.id.num_tv);
            mViewHolder.time = (TextView) convertView.findViewById(R.id.time_tv);
            mViewHolder.select = (CheckBox) convertView.findViewById(R.id.checkbox_list_item);
            mViewHolder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    RecruitmentInfoBean item = (RecruitmentInfoBean) buttonView.getTag();
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

        if (isCheckbox) {
            mViewHolder.select.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.select.setVisibility(View.GONE);
        }

        mViewHolder.job.setText(recruitmentInfoBean.job_name);
        if ("not_publish".equals(recruitmentInfoBean.status)) {
            mViewHolder.type.setVisibility(View.VISIBLE);
            mViewHolder.type.setText(R.string.tv_un_relase);
        } else if ("publish".equals(recruitmentInfoBean.status)) {
            mViewHolder.type.setVisibility(View.VISIBLE);
            mViewHolder.type.setText(R.string.tv_relaseing);
        } else if ("pause".equals(recruitmentInfoBean.status)) {
            mViewHolder.type.setVisibility(View.VISIBLE);
            mViewHolder.type.setText(R.string.tv_stop);
        } else if ("finish".equals(recruitmentInfoBean.status)) {
            mViewHolder.type.setVisibility(View.VISIBLE);
            mViewHolder.type.setText(R.string.tv_relase_finish);
        } else if ("applying".equals(recruitmentInfoBean.status)) {
            mViewHolder.type.setVisibility(View.VISIBLE);
            mViewHolder.type.setText(R.string.tv_applying);
        } else if ("pending".equals(recruitmentInfoBean.status)) {
            mViewHolder.type.setVisibility(View.VISIBLE);
            mViewHolder.type.setText(R.string.tv_pending);
        } else if ("unpass".equals(recruitmentInfoBean.status)) {
            mViewHolder.type.setVisibility(View.VISIBLE);
            mViewHolder.type.setText(R.string.tv_unpass);
        } else {
            mViewHolder.type.setVisibility(View.GONE);
        }
        mViewHolder.city.setText(recruitmentInfoBean.job_area);
        mViewHolder.num.setText(context.getResources().getString(R.string.recruit_ing) + recruitmentInfoBean.job_num + context.getResources().getString(R.string.recruit_persion));

        if ("not_publish".equals(recruitmentInfoBean.status)) {
            mViewHolder.time.setVisibility(View.GONE);
        } else {
            mViewHolder.time.setVisibility(View.VISIBLE);
            mViewHolder.time.setText(Utils.format(context.getString(R.string.vancloud_release), Utils.dateFormatDate(Long.valueOf(recruitmentInfoBean.job_create_date))));
        }

        mViewHolder.select.setTag(recruitmentInfoBean);
        boolean isSelect = (mListener != null && mListener.OnIsSelect(recruitmentInfoBean)) ? true : false;
        mViewHolder.select.setChecked(isSelect);
        return convertView;
    }

    private class ViewHolder {
        TextView job;
        TextView type;
        TextView city;
        TextView num;
        TextView time;
        TextView resumeNum;
        View relativeLayout;
        CheckBox select;
    }

    public void myNotifyDataSetChanged(List<RecruitmentInfoBean> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public interface OnSelectListener {
        /**
         * 选中
         */
        void OnSelected(RecruitmentInfoBean item);

        /**
         * 取消选中
         */
        void OnUnSelected(RecruitmentInfoBean item);

        /**
         * 判断是否选中
         */
        boolean OnIsSelect(RecruitmentInfoBean item);
    }
}
