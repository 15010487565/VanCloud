package com.vgtech.vantop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.Node;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vantop.R;

import java.util.ArrayList;


/**
 * Data:  2019/6/27
 * Auther: xcd
 * Description:
 */
public class CcUserRecyAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Node> list;
    private OnDeleteClickListener onItemClickListener;

    public CcUserRecyAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setDeteleItemClickListener(OnDeleteClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(ArrayList<Node> list) {
        Log.e("TAG_RC","setData="+list.size());
        if (this.list == null){
            this.list = list;
        }else {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }
    public ArrayList<Node> getData() {
        return list;
    }
    public void setDetele(ArrayList<Node> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.rc_grid_user_item, parent, false);
        RecyclerView.ViewHolder holder = new CcUserViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Log.e("TAG_RC","onBindViewHolder="+list.size());
        Node node = list.get(position);

        CcUserViewHolder viewHolder = (CcUserViewHolder) holder;
        ImageOptions.setUserImage(viewHolder.photoView,node.getPhoto());
//        mViewHolder.photoView.setImageResource(R.mipmap.user_photo_default_small);
        viewHolder.tvName.setText(node.getName());

        viewHolder.ivDeleteUser.setTag(node);
//        onItemEventClick(viewHolder);
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }


    class CcUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvName;
        SimpleDraweeView photoView;
        ImageView ivDeleteUser;
        public CcUserViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            photoView = (SimpleDraweeView) itemView.findViewById(R.id.ItemImage);
            ivDeleteUser = (ImageView) itemView.findViewById(R.id.btn_delete_user);
            ivDeleteUser.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int i = view.getId();//删除
            if (i == R.id.btn_delete_user) {
                onItemClickListener.OnDeleteClick(view, getLayoutPosition());

            }
        }
    }

    public interface OnDeleteClickListener {
        //删除
        void OnDeleteClick(View view, int position);
    }
//    private void onItemEventClick(RecyclerView.ViewHolder holder) {
//        final int position = holder.getLayoutPosition();
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onItemClickListener.OnItemClick(v, position);
//            }
//        });
//
//    }
}
