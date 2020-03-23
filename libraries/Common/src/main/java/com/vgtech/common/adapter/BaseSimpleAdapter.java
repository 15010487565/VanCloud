package com.vgtech.common.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * Created by code on 2016/3/7.
 */
public abstract class BaseSimpleAdapter<T> extends BaseAdapter<T> {

    public BaseSimpleAdapter(Context context) {
        super(context);
    }

    /**
     * 该方法需要子类实现，需要返回item布局的resource id
     *
     * @param viewType view类型
     * @return
     */
    public abstract int getItemResource(int viewType);

    /**
     * 使用该getItemView方法替换原来的getView方法，需要子类实现
     *
     * @param position
     * @param convertView
     * @param holder
     * @return
     */
    public abstract View getItemView(int position, View convertView, ViewHolder holder);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            int viewType = getItemViewType(position);
            convertView = View.inflate(mContext, getItemResource(viewType), null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        return getItemView(position, convertView, holder);
    }

    public class ViewHolder {
        private SparseArray<View> views = new SparseArray<View>();
        private View convertView;

        public ViewHolder(View convertView) {
            this.convertView = convertView;
        }

        public <T extends View> T getView(int resId) {
            View v = views.get(resId);
            if (null == v) {
                v = convertView.findViewById(resId);
                views.put(resId, v);
            }
            return (T) v;
        }
    }
}
