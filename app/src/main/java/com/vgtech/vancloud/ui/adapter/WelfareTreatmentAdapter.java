package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.api.Dict;
import com.vgtech.vancloud.R;

import java.util.List;

/**
 * Created by code on 2016/5/30.
 */
public class WelfareTreatmentAdapter extends BaseAdapter {

    private OnSelectListener mListener;

    Context context;

    public List<Dict> getMlist() {
        return mlist;
    }

    public void setmListener(OnSelectListener mListener) {
        this.mListener = mListener;
    }

    List<Dict> mlist;
    int mPosition;


    public WelfareTreatmentAdapter(Context context, List<Dict> list) {
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
        Dict dict = mlist.get(position);
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_welfare_treatment, null);
            mViewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            mViewHolder.select = (CheckBox) convertView.findViewById(R.id.checkbox_list_item);
//            mViewHolder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    Dict item = (Dict) buttonView.getTag();
//                    if (isChecked) {
//                        if (getSelectCount() > 8) {
//                            buttonView.setChecked(false);
//                            Toast.makeText(context, context.getString(R.string.vancloud_welfare_prompt), Toast.LENGTH_SHORT).show();
//                        } else {
//                            if (mListener != null) {
//                                mListener.OnSelected(item);
//                            }
//                        }
//                    } else {
//                        if (mListener != null) {
//                            mListener.OnUnSelected(item);
//                        }
//                    }
//                }
//            });


            mViewHolder.select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    Dict item = (Dict) v.getTag();
                    if (getSelectCount() <= 7) {
                        boolean isSelect = (mListener != null && mListener.OnIsSelect(item)) ? true : false;
                        setItemChecked(!isSelect, item);
                        checkBox.setChecked(!isSelect);
                    } else {
                        checkBox.setChecked(false);
                        Toast.makeText(context, context.getString(R.string.vancloud_welfare_prompt), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.tv_name.setText(dict.name);
        mViewHolder.select.setTag(dict);

        boolean isSelect = (mListener != null && mListener.OnIsSelect(dict)) ? true : false;
        setItemChecked(isSelect, dict);
        mViewHolder.select.setChecked(isSelect);
        return convertView;
    }

    private class ViewHolder {
        TextView tv_name;
        CheckBox select;
    }

    public void myNotifyDataSetChanged(List<Dict> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public interface OnSelectListener {
        /**
         * 选中
         */
        void OnSelected(Dict item);

        /**
         * 取消选中
         */
        void OnUnSelected(Dict item);

        /**
         * 判断是否选中
         */
        boolean OnIsSelect(Dict item);
    }

    public int getSelectCount() {

        int count = 0;
        for (Dict dict : mlist) {
            boolean isSelect = (mListener != null && mListener.OnIsSelect(dict)) ? true : false;
            if (isSelect)
                count++;
        }
        return count;
    }

    public void setItemChecked(boolean isSelect, Dict dict) {
        if (isSelect) {
            if (mListener != null) {
                mListener.OnSelected(dict);
            }
        } else {
            if (mListener != null) {
                mListener.OnUnSelected(dict);
            }
        }
    }
}
