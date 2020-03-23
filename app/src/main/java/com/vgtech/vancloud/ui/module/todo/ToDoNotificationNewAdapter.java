package com.vgtech.vancloud.ui.module.todo;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.d.lib.slidelayout.SlideLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.TodoNotification;
import com.vgtech.vancloud.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2016/9/20.
 */

public class ToDoNotificationNewAdapter extends BaseQuickAdapter<TodoNotification, BaseViewHolder> {

    //    private Context mContext;
//    private List<TodoNotification> todoNotificationList;
    private Map<String, Integer> mLocalTodoMessages;//待办催办次数
//    private LayoutInflater inflater;

    public ToDoNotificationNewAdapter(List<TodoNotification> data) {
        super(data);
        mLocalTodoMessages = new HashMap<>();

        setMultiTypeDelegate(new MultiTypeDelegate<TodoNotification>() {
            @Override
            protected int getItemType(TodoNotification notification) {
//                Log.e("TAG_待办set", "type=" + notification.type);
                if (TextUtils.isEmpty(notification.type)) {
                    return 0;
                } else {
                    Integer valueOf = Integer.valueOf(notification.type);
                    switch (valueOf) {
//                        case 2://流程
                        case 3://分享
                        case 5://帮帮
                        case 7://工作汇报
                        case 9://日程
                        case 11://任务
                        case 34://签卡
                        case 35://休假
                        case 36://加班
                        case 37://流程审批
                        case 38://入职审批
                        case 39://调查问卷
                            return valueOf;
                        default://其他
                            return 0;

                    }
                }

            }
        });
        getMultiTypeDelegate()
//                .registerItemType(2, R.layout.todo_notification_new_item)//流程办理
                .registerItemType(3, R.layout.todo_notification_new_item)//分享
                .registerItemType(5, R.layout.todo_notification_new_item)//帮帮
                .registerItemType(7, R.layout.todo_notification_new_item)//工作汇报
                .registerItemType(9, R.layout.todo_notification_new_item)//日程
                .registerItemType(11, R.layout.todo_notification_new_item)//任务
                .registerItemType(34, R.layout.todo_notification_new_item)//签卡
                .registerItemType(35, R.layout.todo_notification_new_item)//休假
                .registerItemType(36, R.layout.todo_notification_new_item)//任务
                .registerItemType(37, R.layout.todo_notification_new_item)//流程
                .registerItemType(38, R.layout.todo_notification_new_item)//入职审批
                .registerItemType(39, R.layout.todo_notification_new_item)//调查问卷
                .registerItemType(0, R.layout.todo_notification_new_item)//其他
        ;
    }

//    public void setData(List<TodoNotification> todoNotifications) {
//        todoNotificationList = todoNotifications;
//        notifyDataSetChanged();
//    }


    @Override
    protected void convert(BaseViewHolder helper, TodoNotification notification) {


        helper.setText(R.id.tv_info_time, Utils.getInstance(mContext).dateFormat(notification.timestamp));
        helper.setText(R.id.tv_info_title, notification.title);
        SlideLayout slItem = helper.getView(R.id.sl_Item);
        if ("n".equals(notification.is_can_delete)) {//删除
            slItem.setEnable(false);
        } else {
            slItem.setEnable(true);
        }
        TextView isRead = helper.getView(R.id.is_read);
//        Log.e("TAG_红点", "is_read=" + notification.is_read);
//        if ("n".equals(notification.is_read)) {
//            isRead.setVisibility(View.VISIBLE);
//        } else {
            isRead.setVisibility(View.GONE);
//        }

        ImageView infoPhoto = helper.getView(R.id.info_photo);

        TextView tvInfnType = helper.getView(R.id.tv_info_type);

        helper.addOnClickListener(R.id.ll_check);
        helper.addOnClickListener(R.id.ll_Del);
        LinearLayout ll_check = helper.getView(R.id.ll_check);

        int viewType = helper.getItemViewType();
//        Log.e("TAG_待办convert", "type=" + notification.type);
        switch (viewType) {
//            case 1://公告
//
//                break;
//            case 2://流程
//                infoPhoto.setImageResource(R.mipmap.flow);
//                infoPhoto.setBackgroundResource(R.drawable.shape_flow);
//                tvInfnType.setText(R.string.flow);
//                ll_check.setVisibility(View.VISIBLE);
//                break;
            case 3://分享
                infoPhoto.setImageResource(R.mipmap.share);
                infoPhoto.setBackgroundResource(R.drawable.shape_share);
                tvInfnType.setText(R.string.lable_shared);
                ll_check.setVisibility(View.GONE);
                break;
            case 5://帮帮
                infoPhoto.setImageResource(R.mipmap.help);
                infoPhoto.setBackgroundResource(R.drawable.shape_help);
                tvInfnType.setText(R.string.lable_helper);
                ll_check.setVisibility(View.GONE);
                break;
            case 7://工作汇报
                infoPhoto.setImageResource(R.mipmap.report);
                infoPhoto.setBackgroundResource(R.drawable.shape_report);
                tvInfnType.setText(R.string.lable_report);
                ll_check.setVisibility(View.VISIBLE);
                break;
            case 9://日程
                infoPhoto.setImageResource(R.mipmap.schedule_new);
                infoPhoto.setBackgroundResource(R.drawable.shape_schedule);
                tvInfnType.setText(R.string.lable_schedule);
                ll_check.setVisibility(View.VISIBLE);
                break;
            case 11://任务
                infoPhoto.setImageResource(R.mipmap.ic_app_task);
                infoPhoto.setBackgroundResource(R.drawable.shape_task);
                tvInfnType.setText(R.string.lable_task);
                ll_check.setVisibility(View.VISIBLE);
                break;
            case 34: //签卡
                infoPhoto.setImageResource(R.mipmap.ic_app_signcard);
                infoPhoto.setBackgroundResource(R.drawable.shape_change_sign);
                tvInfnType.setText(R.string.change_sign);
                ll_check.setVisibility(View.VISIBLE);
                slItem.setEnable(false);
                break;
            case 35://休假
                infoPhoto.setImageResource(R.mipmap.approval_vacation_logo);
                infoPhoto.setBackgroundResource(R.drawable.shape_leave);
                tvInfnType.setText(R.string.todo_menu_leave);
                ll_check.setVisibility(View.VISIBLE);
                slItem.setEnable(false);
                break;
            case 36://加班
                infoPhoto.setImageResource(R.mipmap.approval_overtime_logo);
                infoPhoto.setBackgroundResource(R.drawable.shape_overtime);
                tvInfnType.setText(R.string.todo_menu_overtime);
                ll_check.setVisibility(View.VISIBLE);
                slItem.setEnable(false);
                break;
            case 37://流程
                infoPhoto.setImageResource(R.mipmap.ic_app_flow);
                infoPhoto.setBackgroundResource(R.drawable.shape_flow);
                tvInfnType.setText(R.string.lable_flow);
                ll_check.setVisibility(View.VISIBLE);
                slItem.setEnable(false);
                break;
            case 38://入职审批
                infoPhoto.setImageResource(R.mipmap.ic_app_entryapprove);
                infoPhoto.setBackgroundResource(R.drawable.shape_entryapprove);
                tvInfnType.setText(R.string.lable_entryapprove);
                ll_check.setVisibility(View.VISIBLE);
                slItem.setEnable(false);
                break;
            case 39://调查问卷
                infoPhoto.setImageResource(R.mipmap.ic_app_vote);
                infoPhoto.setBackgroundResource(R.drawable.shape_vote);
                tvInfnType.setText(R.string.lable_vote);
                ll_check.setVisibility(View.VISIBLE);
                slItem.setEnable(false);
                break;
//            case 102://申请加入企业推送
//                break;
//            case 103://同意加入企业推送
//                break;
            default:
                infoPhoto.setImageResource((R.mipmap.message_logo));
                infoPhoto.setBackgroundResource(R.drawable.shape_message);
                tvInfnType.setText(R.string.vantop_other);
                ll_check.setVisibility(View.GONE);
                break;
        }
    }

    //    @Override
//    public int getItemCount() {
//        return todoNotificationList == null ? 0 : todoNotificationList.size();
//    }
//
    public void addPushMessage(Map<String, Integer> messageDBs) {
        mLocalTodoMessages.putAll(messageDBs);
//        Log.e("TAG_加急", "mLocalTodoMessages=" + mLocalTodoMessages.toString());
        notifyDataSetChanged();
    }

    public Map<String, Integer> getLocalPushMessage() {
        return mLocalTodoMessages;
    }
//
//
//    public List<TodoNotification> getList() {
//        return todoNotificationList;
//    }


//    public void deleteItem(int position) {
////        for (int i = todoNotificationList.size() - 1; i >= 0; i--){
////            if(i==position){
////                todoNotificationList.re
////            }
////
////        }
//        TodoNotification todoNotification = todoNotificationList.get(position);
//        todoNotificationList.remove(todoNotification);
//        notifyItemRemoved(position);
//        notifyItemRangeChanged(position,todoNotificationList.size()-position);
//    }

//    //签卡
//    public class helper extends RecyclerView.ViewHolder implements View.OnClickListener {
//
//        private SlideLayout slItem;
//        private ImageView infoPhoto;
//        private TextView tvInfnType;
//        private TextView tvInfoTitle;
//        private TextView tvInfoTime;
//        private TextView is_read;
//        private LinearLayout ll_check, llDel;
//
//        public helper(View itemView) {
//            super(itemView);
//            slItem = itemView.findViewById(R.id.sl_Item);
//            infoPhoto = itemView.findViewById(R.id.info_photo);
//            tvInfnType = itemView.findViewById(R.id.tv_info_type);
//            tvInfoTitle = itemView.findViewById(R.id.tv_info_title);
//            tvInfoTime = itemView.findViewById(R.id.tv_info_time);
//            ll_check = itemView.findViewById(R.id.ll_check);
//            ll_check.setOnClickListener(this);
//            is_read = itemView.findViewById(R.id.is_read);
//            llDel = itemView.findViewById(R.id.ll_Del);
//            llDel.setOnClickListener(this);
//
//        }
//
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.ll_check://查看
//                    if (slItem.isOpen()) {
//                        slItem.close();
//                    }
//                    mItemClickListener.onClick(getLayoutPosition());
//                    break;
//                case R.id.ll_Del://删除
//                    if (slItem.isOpen()) {
//                        slItem.close();
//                    }
//                    mItemClickListener.onDelClick(getLayoutPosition());
//                    break;
//            }
//        }
//    }

    //年假
//    public class AnnualLeaveHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//
//        private SlideLayout slItem;
//        private ImageView user_photo;
//        private TextView tvInfnType;
//        private TextView tvInfoTitle;
//        private TextView tvInfoTime;
//        private TextView is_read;
//        private LinearLayout llDel;
//
//        public AnnualLeaveHolder(View itemView) {
//            super(itemView);
//            slItem = itemView.findViewById(R.id.sl_Item);
//            user_photo = itemView.findViewById(R.id.user_photo);
//            tvInfnType = itemView.findViewById(R.id.tv_info_type);
//            tvInfoTitle = itemView.findViewById(R.id.tv_info_title);
//            tvInfoTime = itemView.findViewById(R.id.tv_info_time);
//            is_read = itemView.findViewById(R.id.is_read);
//            llDel = itemView.findViewById(R.id.ll_Del);
//            llDel.setOnClickListener(this);
//
//        }
//
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.tv_WorkGroupDel:
//                    if (slItem.isOpen()) {
//                        slItem.close();
//                    }
////                    mItemClickListener.onDelClick(getLayoutPosition());
//                    break;
//            }
//        }
//    }

//    public interface OnItemClickListener {
//        void onClick(int position);
//
//        void onDelClick(int position);
//    }
//    private OnItemClickListener mItemClickListener;
//    public void setItemClickListener(OnItemClickListener listener) {
//        this.mItemClickListener = listener;
//    }
}
