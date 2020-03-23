package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Account;
import com.vgtech.vancloud.ui.module.accountmanagement.AccountBindingActivity;

import java.util.List;

/**
 * Created by Duke on 2016/8/2.
 */
public class AccountListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Account> mList;

    public AccountListAdapter(Context context, List<Account> list) {
        mContext = context;
        mList = list;

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.account_list_item, null);
            mViewHolder.accountNameView = (TextView) convertView.findViewById(R.id.account_name);
            mViewHolder.bindStateView = (TextView) convertView.findViewById(R.id.bind_state);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        final Account account = mList.get(position);
        if ("51job".equals(account.source)) {
            mViewHolder.accountNameView.setText(mContext.getString(R.string.vancloud_zhaopin));
        } else {
            mViewHolder.accountNameView.setText(mContext.getString(R.string.vancloud_zhilian));
        }
        if ("Y".equals(account.is_bind)) {
            mViewHolder.bindStateView.setText(mContext.getString(R.string.vancloud_bound));
        } else {
            mViewHolder.bindStateView.setText(mContext.getString(R.string.vancloud_not_bound));
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, AccountBindingActivity.class);
                intent.putExtra("source", account.getJson().toString());
                ((Activity) mContext).startActivityForResult(intent, 101);

            }
        });

        return convertView;
    }

    public void myNotifyDataSetChanged(List<Account> list) {

        mList = list;
        notifyDataSetChanged();
    }

    public List<Account> getList() {
        return mList;
    }

    class ViewHolder {

        TextView accountNameView;
        TextView bindStateView;

    }
}
