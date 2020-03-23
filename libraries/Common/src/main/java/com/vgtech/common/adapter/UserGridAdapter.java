package com.vgtech.common.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.R;
import com.vgtech.common.api.Node;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.listener.OnUserRemoveListener;
import com.vgtech.common.utils.UserUtils;

import java.util.ArrayList;

/**
 * Created by zhangshaofang on 2015/8/28.
 */
public class UserGridAdapter extends BaseAdapter implements View.OnClickListener {
    private Context context;
    private ArrayList<Node> list;
    private TextView mCountTv;
    private boolean isfromAppointmentDetail = false;
    private boolean isfromAppointmentUpdate = false;

    public void setIsfromAppointmentUpdate(boolean isfromAppointmentUpdate) {
        this.isfromAppointmentUpdate = isfromAppointmentUpdate;
    }

    public void setIsfromAppointmentDetail(boolean isfromAppointmentDetail) {
        this.isfromAppointmentDetail = isfromAppointmentDetail;
    }

    /**
     * 圆角配置
     */
//    public DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
//            .cacheInMemory(true)
//            .cacheOnDisk(true)
//            .showImageForEmptyUri(R.mipmap.user_photo_default_small)
//            .showImageOnLoading(R.mipmap.user_photo_default_small)
//            .showImageOnFail(R.mipmap.user_photo_default_small)
//            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
//            .displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE))
//            .build();
    public UserGridAdapter(Context _context, ArrayList<Node> _list, TextView countTv) {
        this.list = _list;
        mAllList = list;
        this.context = _context;
        mCountTv = countTv;
        if (mCountTv != null) {
            int count = 0;
            if (mAllList != null && !mAllList.isEmpty()) {
                int i = 0;
                for (Node n : mAllList) {
                    if (n.isUser())
                        i++;
                }
                count = i;
            } else {
                count = getCount();
            }
            mCountTv.setText(count == 0 ? "" : String.valueOf(count) + context.getResources().getString(R.string.man));
        }
    }

    private ArrayList<Node> mAllList;

    public UserGridAdapter(Context _context, ArrayList<Node> _list, ArrayList<Node> desplayNodes, TextView countTv) {
        mAllList = _list;
        this.list = desplayNodes;
        if (list == null || list.isEmpty()) {
            list = mAllList;
        }
        this.context = _context;
        mCountTv = countTv;
        if (mCountTv != null) {
            int count = 0;
            if (mAllList != null && !mAllList.isEmpty()) {
                int i = 0;
                for (Node n : mAllList) {
                    if (n.isUser())
                        i++;
                }
                count = i;
            } else {
                count = getCount();
            }
            mCountTv.setText(count == 0 ? "" : String.valueOf(count) + context.getResources().getString(R.string.man));
        }
    }

    private OnUserRemoveListener mRemoveListener;

    public void setOnRemoveListener(OnUserRemoveListener listener) {
        mRemoveListener = listener;
    }

    public ArrayList<Node> getList() {
        return mAllList;
    }

    public void setList(ArrayList<Node> list) {
        this.list = list;
        if (mAllList == null || mAllList.isEmpty()) {
            mAllList = list;
        } else if (list == null || list.isEmpty()) {
            this.list = mAllList;
        }
        notifyDataSetChanged();
        if (mCountTv != null) {
            int count = 0;
            if (mAllList != null && !mAllList.isEmpty()) {
                int i = 0;
                for (Node n : mAllList) {
                    if (n.isUser())
                        i++;
                }
                count = i;
            } else {
                count = getCount();
            }
            mCountTv.setText(count == 0 ? "" : String.valueOf(count) + context.getResources().getString(R.string.man));
        }
    }

    public void setList(ArrayList<Node> list, ArrayList<Node> desplayNodes) {
        mAllList = list;
        setList(desplayNodes);
    }

    public void remove(Node node) {
        mAllList.remove(node);
        list.remove(node);
        if (mRemoveListener != null)
            mRemoveListener.onRemove(node);
        notifyDataSetChanged();
        if (mCountTv != null) {
            int count = 0;
            if (mAllList != null && !mAllList.isEmpty()) {
                int i = 0;
                for (Node n : mAllList) {
                    if (n.isUser())
                        i++;
                }
                count = i;
            } else {
                count = getCount();
            }
            mCountTv.setText(count == 0 ? "" : String.valueOf(count) + context.getResources().getString(R.string.man));
        }
    }

    public View.OnClickListener mOnClickListener;

    @Override
    public int getCount() {
        if (list == null) return 0;
        return list.size() > 10 ? 10 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        convertView = layoutInflater.inflate(R.layout.grid_user_item, null);
        View deleteView = convertView.findViewById(R.id.btn_delete_user);
        if (mOnClickListener == null) {
            deleteView.setOnClickListener(this);
        } else {
            deleteView.setOnClickListener(mOnClickListener);
        }
        TextView nameTv = (TextView) convertView.findViewById(R.id.tv_name);
        SimpleDraweeView iconIv = (SimpleDraweeView) convertView.findViewById(R.id.ItemImage);
        Node node = list.get(position);
        if (!TextUtils.isEmpty(node.getId()) && node.getId().equals(-1)) {
            iconIv.setImageResource(R.drawable.wg_xx_middle_add_btn);
            nameTv.setText("");
            deleteView.setVisibility(View.GONE);
            deleteView.setTag(node);
        } else {
            deleteView.setVisibility(node.isUser() ? View.VISIBLE : View.GONE);
            if (node.type == 1) {
                iconIv.setImageResource(R.mipmap.icon_default_group);
            } else if (node.type == 2) {
                iconIv.setImageResource(R.mipmap.icon_depart);
            } else {
                ImageOptions.setUserImage(iconIv, node.getPhoto());
                UserUtils.enterUserInfo(context, node.getId(), node.getName(), node.getPhoto(), iconIv);
            }

            nameTv.setText(node.getName());
            deleteView.setTag(node);
        }
        if (isfromAppointmentDetail) {
            deleteView.setVisibility(View.GONE);
        }

        if (isfromAppointmentUpdate) {
            deleteView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {
        Node node = (Node) v.getTag();
        remove(node);
    }

    public void removeAllNode() {
        mAllList = new ArrayList<>();
        list = new ArrayList<>();
        notifyDataSetChanged();
        mCountTv.setText("");
    }
}

