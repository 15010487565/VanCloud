package com.vgtech.vancloud.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vgtech.vancloud.R;

import java.util.List;

/**
 * Data:  2017/8/3
 * Auther: 陈占洋
 * Description:
 */

public class PopRvAdapter extends RecyclerView.Adapter<PopRvAdapter.PopViewHolder> {

    private List<String> mData;
    private View.OnClickListener mItemClickListener;

    @Override
    public PopViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fatie_pop_list_item, viewGroup, false);
        return new PopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PopViewHolder popViewHolder, int position) {
        String text = mData.get(position);
        popViewHolder.text.setText(text);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setData(List<String> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    public class PopViewHolder extends RecyclerView.ViewHolder {

        private TextView text;

        public PopViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.fatie_pop_rv_item_text);
            if (mItemClickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemClickListener.onClick(text);
                    }
                });
            }
        }
    }

    public void setItemClickListener(View.OnClickListener listener) {
        this.mItemClickListener = listener;
    }
}
