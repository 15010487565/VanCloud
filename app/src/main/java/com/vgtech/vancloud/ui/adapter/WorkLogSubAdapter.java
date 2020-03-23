package com.vgtech.vancloud.ui.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.WorkLogSubBean;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;

import java.util.List;

/**
 * Data:  2018/7/27
 * Auther: 陈占洋
 * Description: 下属工作日志listview适配器
 */

public class WorkLogSubAdapter extends BaseAdapter {

    private List<WorkLogSubBean> mData;
    private OnItemClickListener mOnItemClickListener;

    public WorkLogSubAdapter(@NonNull List<WorkLogSubBean> data) {
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_log_sub_adater_item, null);
            viewHolder.root = (LinearLayout) convertView.findViewById(R.id.work_log_sub_adapter_root);
            viewHolder.portrait = (SimpleDraweeView) convertView.findViewById(R.id.work_log_sub_adapter_sdv_portrait);
            viewHolder.name = (TextView) convertView.findViewById(R.id.work_log_sub_adapter_tv_name);
            viewHolder.numDuration = (TextView) convertView.findViewById(R.id.work_log_sub_adapter_tv_num_duration);
            viewHolder.stateTime = (TextView) convertView.findViewById(R.id.work_log_sub_tv_adapter_state_time);
            viewHolder.revoke = (Button) convertView.findViewById(R.id.work_log_sub_adapter_btn_revoke);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        WorkLogSubBean workLogSubBean = mData.get(position);

        viewHolder.root.setTag(position);
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onRootClick(v, (Integer) v.getTag());
                }
            }
        });
        ImageOptions.setUserImage(viewHolder.portrait, "");
        viewHolder.name.setText(workLogSubBean.getStaffName() + "  (" + workLogSubBean.getStaffNo() + ")");
        //TODO 国际化
        viewHolder.numDuration.setText(workLogSubBean.getNum() + parent.getContext().getString(R.string.pieces)+"  " + workLogSubBean.getDuration() + parent.getContext().getString(R.string.working_hours));
        if (workLogSubBean.getNum() > 0) {
            if (workLogSubBean.getIsDone()) {
                viewHolder.stateTime.setText(parent.getContext().getString(R.string.vantop_havecommited)
                        + "  (" + workLogSubBean.getDoneDate() + " " + workLogSubBean.getDoneTime()+")");
            } else {
                viewHolder.stateTime.setText(parent.getContext().getString(R.string.vantop_nocommited));
            }
        } else {
            viewHolder.stateTime.setText(R.string.text_no_log_record);
        }
        if (!workLogSubBean.getIsDone()) {
            if (viewHolder.revoke.getVisibility() != View.GONE)
                viewHolder.revoke.setVisibility(View.GONE);
        } else {
            if (viewHolder.revoke.getVisibility() != View.VISIBLE)
                viewHolder.revoke.setVisibility(View.VISIBLE);
        }
        viewHolder.revoke.setTag(position);
        viewHolder.revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onRevokeClick(v, (Integer) v.getTag());
                }
            }
        });

        return convertView;
    }

    public void setData(List<WorkLogSubBean> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    public void addData(List<WorkLogSubBean> data) {
        if (mData != null) {
            mData.addAll(data);
            this.notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public List<WorkLogSubBean> getData() {
        return mData;
    }

    class ViewHolder {
        LinearLayout root;
        SimpleDraweeView portrait;
        TextView name;
        TextView numDuration;
        TextView stateTime;
        Button revoke;
    }

    public interface OnItemClickListener {
        /**
         * 条目被点击
         *
         * @param view
         */
        void onRootClick(View view, int position);

        /**
         * 撤销被点击
         *
         * @param view
         */
        void onRevokeClick(View view, int position);
    }
}
