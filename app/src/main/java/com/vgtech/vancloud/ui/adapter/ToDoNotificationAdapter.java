package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.TodoNotification;
import com.vgtech.vancloud.utils.NotificationUtils;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.ui.overtime.OverTimeDetailActivity;
import com.vgtech.vantop.ui.signedcard.SignedCardApprovalDetailsActivity;
import com.vgtech.vantop.ui.vacation.VacationApplyDetailsActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2016/9/20.
 */

public class ToDoNotificationAdapter extends BaseAdapter {

    private Context mContext;
    private List<TodoNotification> todoNotificationList;
    private Map<String, Integer> mLocalTodoMessages;//待办催办次数

    public ToDoNotificationAdapter(Context context, List<TodoNotification> todoNotifications) {
        mContext = context;
        todoNotificationList = todoNotifications;
        mLocalTodoMessages = new HashMap<>();
    }

    public void addPushMessage(Map<String, Integer> messageDBs) {
        mLocalTodoMessages.putAll(messageDBs);
        notifyDataSetChanged();
    }

    public Map<String, Integer> getLocalPushMessage() {
        return mLocalTodoMessages;
    }

    @Override
    public int getCount() {
        return todoNotificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return todoNotificationList.get(position);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.todo_notification_item, null);
            mViewHolder.imgLogoView = (ImageView) convertView.findViewById(R.id.img_logo);
            mViewHolder.titleView = (TextView) convertView.findViewById(R.id.title_tv);
            mViewHolder.stateView = (TextView) convertView.findViewById(R.id.state_tv);
            mViewHolder.timeView = (TextView) convertView.findViewById(R.id.time_tv);
            mViewHolder.isReadView = (TextView) convertView.findViewById(R.id.is_read);
            mViewHolder.hasten_tv = (TextView) convertView.findViewById(R.id.hasten_tv);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        TodoNotification todoNotification = todoNotificationList.get(position);
//        NotificationUtils.setImageView(mContext, mViewHolder.imgLogoView, todoNotification.type);
        if ("pending".equals(todoNotification.state)) {
            NotificationUtils.setImageView(mContext, mViewHolder.imgLogoView, todoNotification.type);
            mViewHolder.stateView.setVisibility(View.GONE);
        } else {
            NotificationUtils.setItemView(mContext, mViewHolder.imgLogoView, mViewHolder.stateView, todoNotification.type, todoNotification.process_status);
            mViewHolder.stateView.setVisibility(View.VISIBLE);
            mViewHolder.stateView.setText(todoNotification.process_msg);
        }
        if ("n".equals(todoNotification.is_read)) {
            mViewHolder.isReadView.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.isReadView.setVisibility(View.GONE);
        }
        int count = 0;
        if (mLocalTodoMessages.containsKey(todoNotification.res_id)) {
            count = mLocalTodoMessages.get(todoNotification.res_id);
        }
        mViewHolder.hasten_tv.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        mViewHolder.hasten_tv.setText(mContext.getString(R.string.lable_hasten,count));
        mViewHolder.titleView.setText(Html.fromHtml(todoNotification.title));
        mViewHolder.timeView.setText(Utils.getInstance(mContext).dateFormat(todoNotification.timestamp));

        return convertView;
    }

    class ViewHolder {

        ImageView imgLogoView;
        TextView titleView;
        TextView stateView;
        TextView timeView;
        TextView isReadView;
        TextView hasten_tv;

    }

    public void myNotifyDataSetChanged(List<TodoNotification> list) {

        todoNotificationList = list;
        notifyDataSetChanged();
    }

    public List<TodoNotification> getList() {
        return todoNotificationList;
    }

    public void chaneIsRead(int position) {
        TodoNotification todoNotification = todoNotificationList.get(position);
        todoNotification.is_read = "y";
        try {
            todoNotification.getJson().put("is_read", "y");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
//        for (int i = todoNotificationList.size() - 1; i >= 0; i--){
//            if(i==position){
//                todoNotificationList.re
//            }
//
//        }
        TodoNotification todoNotification = todoNotificationList.get(position);
        todoNotificationList.remove(todoNotification);
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        TodoNotification todoNotification = todoNotificationList.get(position);
        if ("y".equals(todoNotification.is_can_delete)) {
            return 0;
        } else {
            return 1;
        }
    }
}
