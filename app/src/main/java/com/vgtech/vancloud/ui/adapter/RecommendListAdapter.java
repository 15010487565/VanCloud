package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.common.api.RecommendListBean;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

/**
 * Created by code on 2015/11/5.
 */
public class RecommendListAdapter extends BaseAdapter {
    Context context;

    public List<RecommendListBean> getMlist() {
        return mlist;
    }

    List<RecommendListBean> mlist;
    int mPosition;

    public RecommendListAdapter(Context context, List<RecommendListBean> list) {
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
        final RecommendListBean recommendListBean = mlist.get(position);
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recommend_list, null);
            mViewHolder.company_name = (TextView) convertView.findViewById(R.id.tv_company_name);
            mViewHolder.company_phone = (TextView) convertView.findViewById(R.id.tv_company_phone);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.company_name.setText(Utils.format(context.getString(R.string.vancloud_company_name), recommendListBean.account_name));
        mViewHolder.company_phone.setText(Utils.format(context.getString(R.string.vancloud_telephone_number), recommendListBean.invite_mobile));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }

    private class ViewHolder {
        TextView company_name;
        TextView company_phone;
    }

    public void myNotifyDataSetChanged(List<RecommendListBean> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }
}
