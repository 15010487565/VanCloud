package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.module.approval.SearchLogInterface;

import java.util.List;

/**
 * Created by Duke on 2016/10/9.
 */

public class SearchLogAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;
    private List<String> list;
    private String type;
    private SearchLogInterface searchLogInterface;

    public SearchLogAdapter(Context context, List<String> list, SearchLogInterface searchLogInterface) {

        this.context = context;
        this.list = list;
        this.searchLogInterface = searchLogInterface;

    }

    @Override
    public int getCount() {
        if (list.size() >= 8)
            return 8;
        else
            return list.size();
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
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.search_log_list_item, null);
            mViewHolder.logTextView = (TextView) convertView.findViewById(R.id.log_text);
            mViewHolder.removeBtn = (ImageView) convertView.findViewById(R.id.remove_btn);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        String log = list.get(position);
        mViewHolder.logTextView.setText(log);

        mViewHolder.removeBtn.setTag(log);
        mViewHolder.removeBtn.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.remove_btn:
                String log = (String) v.getTag();
                list.remove(log);
                notifyDataSetChanged();
                if (list.size() == 0) {
                    searchLogInterface.listNoData();
                }
                PrfUtils.setApprovalSearchLog(context, type, list);
                break;
        }
    }

    class ViewHolder {

        TextView logTextView;
        ImageView removeBtn;
    }

    public void myNotifyDataSetChanged(List<String> list) {

        this.list = list;
        notifyDataSetChanged();
    }

    public List<String> getList() {
        return list;
    }

}
