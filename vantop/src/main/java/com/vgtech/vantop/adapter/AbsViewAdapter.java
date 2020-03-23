package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 适配器基类
 * Created by shilec on 2016/9/5.
 */
public abstract class AbsViewAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;

    public AbsViewAdapter(Context context, List<T> datas) {

        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mDatas = datas;
    }

    public List<T> getData() {
        return mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(onInflateItemView(), viewGroup, false);
            holder = onCreateViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            //view = holder.itemView;
        }
        onBindData(holder, i);
        return view;
    }

    protected abstract ViewHolder onCreateViewHolder(View itemView);

    protected abstract void onBindData(ViewHolder holder, int posistion);

    protected abstract int onInflateItemView();

    protected abstract class ViewHolder {
        public View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
}
