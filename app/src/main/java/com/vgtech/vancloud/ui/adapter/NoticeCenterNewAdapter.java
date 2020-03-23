package com.vgtech.vancloud.ui.adapter;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 消息提醒适配器
 * Created by Duke on 2016/3/10.
 */
public class NoticeCenterNewAdapter extends BaseQuickAdapter<MessageDB, BaseViewHolder> {

    public NoticeCenterNewAdapter(List<MessageDB> data) {
        super(data);

        setMultiTypeDelegate(new MultiTypeDelegate<MessageDB>() {
            @Override
            protected int getItemType(MessageDB notification) {

                return TextUtils.isEmpty(notification.type) ? 0 : Integer.valueOf(notification.type);
            }
        });
        getMultiTypeDelegate()
                .registerItemType(2, R.layout.todo_notification_new_item)//流程
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
                .registerItemType(0, R.layout.todo_notification_new_item)//加班
        ;
    }

    @Override
    protected void convert(BaseViewHolder helper, MessageDB messageDB) {


        helper.setText(R.id.tv_info_time,Utils.getInstance(mContext).dateFormat(messageDB.timestamp));

        try {
            TextView tvInfoTitle = helper.getView(R.id.tv_info_title);
            PushMessage pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
            if (mContext.getResources().getString(R.string.message_comment).equals(messageDB.title))
                tvInfoTitle.setText(EmojiFragment.getEmojiContent(mContext, tvInfoTitle.getTextSize(),Html.fromHtml(messageDB.title)));
            else
                tvInfoTitle.setText(Html.fromHtml(pushMessage.content));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LinearLayout llCheck = helper.getView(R.id.ll_check);
        if (!"chexiao".equals(messageDB.operationType)) {
            llCheck.setVisibility(View.VISIBLE);
        }else {
            llCheck.setVisibility(View.GONE);
        }
        TextView isRead = helper.getView(R.id.is_read);

        if (0 == messageDB.messageState) {
            isRead.setVisibility(View.VISIBLE);
        } else {
            isRead.setVisibility(View.GONE);
        }

        ImageView infoPhoto = helper.getView(R.id.info_photo);

        TextView tvInfnType = helper.getView(R.id.tv_info_type);

        helper.addOnClickListener(R.id.ll_check);
        helper.addOnClickListener(R.id.ll_Del);

        int viewType = helper.getItemViewType();
        switch (viewType) {
//            case 1://公告
//
//                break;
//            case 2://流程
//                infoPhoto.setImageResource(R.mipmap.flow);
//                infoPhoto.setBackgroundResource(R.drawable.shape_flow);
//                tvInfnType.setText(R.string.flow);
//
//                break;
            case 3://分享
                infoPhoto.setImageResource(R.mipmap.share);
                infoPhoto.setBackgroundResource(R.drawable.shape_share);
                tvInfnType.setText(R.string.lable_shared);

                break;
            case 5://帮帮
                infoPhoto.setImageResource((R.mipmap.help));
                infoPhoto.setBackgroundResource(R.drawable.shape_help);
                tvInfnType.setText(R.string.lable_helper);

                break;
            case 7://工作汇报
                infoPhoto.setImageResource((R.mipmap.report));
                infoPhoto.setBackgroundResource(R.drawable.shape_report);
                tvInfnType.setText(R.string.lable_report);

                break;
            case 9://日程
                infoPhoto.setImageResource((R.mipmap.schedule_new));
                infoPhoto.setBackgroundResource(R.drawable.shape_schedule);
                tvInfnType.setText(R.string.lable_schedule);

                break;
            case 11://任务
                infoPhoto.setImageResource((R.mipmap.ic_app_task));
                infoPhoto.setBackgroundResource(R.drawable.shape_task);
                tvInfnType.setText(R.string.lable_task);

                break;
            case 34: //签卡
                infoPhoto.setImageResource((R.mipmap.ic_app_signcard));
                infoPhoto.setBackgroundResource(R.drawable.shape_change_sign);
                tvInfnType.setText(R.string.change_sign);

                break;
            case 35://休假
                infoPhoto.setImageResource(R.mipmap.approval_vacation_logo);
                infoPhoto.setBackgroundResource(R.drawable.shape_leave);
                tvInfnType.setText(R.string.todo_menu_leave);

                break;
            case 36://加班
                infoPhoto.setImageResource((R.mipmap.approval_overtime_logo));
                infoPhoto.setBackgroundResource(R.drawable.shape_overtime);
                tvInfnType.setText(R.string.todo_menu_overtime);

                break;
            case 37://流程
                infoPhoto.setImageResource(R.mipmap.ic_app_flow);
                infoPhoto.setBackgroundResource(R.drawable.shape_flow);
                tvInfnType.setText(R.string.lable_flow);
                break;
            case 38://入职审批
                infoPhoto.setImageResource(R.mipmap.ic_app_entryapprove);
                infoPhoto.setBackgroundResource(R.drawable.shape_entryapprove);
                tvInfnType.setText(R.string.lable_entryapprove);
                break;
            case 39://调查问卷
                infoPhoto.setImageResource(R.mipmap.ic_app_vote);
                infoPhoto.setBackgroundResource(R.drawable.shape_vote);
                tvInfnType.setText(R.string.lable_vote);
                break;
            default:
                infoPhoto.setImageResource((R.mipmap.message_logo));
                infoPhoto.setBackgroundResource(R.drawable.shape_message);
                tvInfnType.setText(R.string.vantop_other);
                break;
        }
    }
}
