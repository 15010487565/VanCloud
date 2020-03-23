package com.vgtech.vancloud.ui.workgroup;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.d.lib.slidelayout.SlideLayout;
import com.vgtech.common.api.Organization;
import com.vgtech.vancloud.R;

import java.util.List;

/**
 * Created by code on 2016/9/5.
 */
public class WorkGroupRcAdapter extends RecyclerView.Adapter<WorkGroupRcAdapter.WorkGroupViewHolder> {

    private List<Organization> mData;
    private OnItemClickListener mItemClickListener;

    @Override
    public WorkGroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.workgroup_item, viewGroup, false);
        return new WorkGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WorkGroupViewHolder viewHolder, final int position) {

        Organization organization = mData.get(position);
        viewHolder.depart_name.setText(organization.label);
        viewHolder.depart_count.setText(organization.num);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setData(List<Organization> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }
    public List<Organization> getData() {
        return mData;
    }

    public class WorkGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SlideLayout slItem;
        private TextView depart_name;
        private TextView depart_count;

        private TextView tvWorkGroupDel;

        public WorkGroupViewHolder(View itemView) {
            super(itemView);
            slItem = itemView.findViewById(R.id.sl_Item);
            depart_name = itemView.findViewById(R.id.depart_name);
            depart_count = itemView.findViewById(R.id.depart_count);

            tvWorkGroupDel = itemView.findViewById(R.id.tv_WorkGroupDel);
            tvWorkGroupDel.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_WorkGroupDel:
                    if (slItem.isOpen()) {
                        slItem.close();
                    }
                    mItemClickListener.onDelClick(getLayoutPosition());
                    break;
            }
        }
    }
    //第一步 定义接口
    public interface OnItemClickListener {
        void onClick(int position);
        void onDelClick(int position);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }
}


