package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.utils.NotificationUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息提醒适配器
 * Created by Duke on 2016/3/10.
 */
public class NoticeCenterAdapter extends BaseAdapter {

    private List<MessageDB> dataList = new ArrayList<>();
    private List<MessageDB> selectedData = new ArrayList<>();
    private Context context;
    private boolean ifShowLogo;

    private boolean mSelected;

    public void setSelected(boolean selected) {
        mSelected = selected;
        notifyDataSetChanged();
    }

    public boolean isSelected() {
        return mSelected;
    }

    public List<MessageDB> getSelectedData() {
        return selectedData;
    }

    public NoticeCenterAdapter(Context context, List<MessageDB> messageDBList) {

        ifShowLogo = true;
        dataList = messageDBList;
        this.context = context;
    }


    public NoticeCenterAdapter(Context context, List<MessageDB> messageDBList, Boolean showLogo) {

        ifShowLogo = showLogo;
        dataList = messageDBList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.todo_notification_item, null);
            mViewHolder.imgLogoView = (ImageView) convertView.findViewById(R.id.img_logo);
            mViewHolder.titleView = (TextView) convertView.findViewById(R.id.title_tv);
            mViewHolder.stateView = (TextView) convertView.findViewById(R.id.state_tv);
            mViewHolder.timeView = (TextView) convertView.findViewById(R.id.time_tv);
            mViewHolder.isReadView = (TextView) convertView.findViewById(R.id.is_read);
            mViewHolder.icon_check = (ImageView) convertView.findViewById(R.id.icon_check);
            mViewHolder.companyLogoView = (SimpleDraweeView) convertView.findViewById(R.id.company_logo);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        MessageDB messageDB = dataList.get(position);

        mViewHolder.icon_check.setVisibility(mSelected ? View.VISIBLE : View.GONE);
        if (mSelected) {
            mViewHolder.icon_check.setImageResource(selectedData.contains(messageDB) ? R.mipmap.chk_on_normal : R.mipmap.chk_off_normal);
        }
        try {
            PushMessage pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
            if (0 == messageDB.messageState)
                mViewHolder.isReadView.setVisibility(View.VISIBLE);
            else
                mViewHolder.isReadView.setVisibility(View.GONE);
            if (ifShowLogo) {
                mViewHolder.imgLogoView.setVisibility(View.VISIBLE);
                if (TypeUtils.JOINCOMPANY.equals(pushMessage.msgTypeId)) {
                    mViewHolder.companyLogoView.setVisibility(View.VISIBLE);
                    ImageOptions.setUserImage(mViewHolder.companyLogoView, pushMessage.logo);
                } else if (TypeUtils.NEWEMPLOYEE.equals(pushMessage.msgTypeId)) {
                    mViewHolder.companyLogoView.setVisibility(View.VISIBLE);
                    mViewHolder.companyLogoView.setImageResource(R.mipmap.user_photo_default_small);
                } else {
                    mViewHolder.companyLogoView.setVisibility(View.GONE);
                    if (context.getResources().getString(R.string.message_comment).equals(messageDB.title)) {
                        GradientDrawable myGrad = (GradientDrawable) mViewHolder.imgLogoView.getBackground();
                        mViewHolder.imgLogoView.setImageResource(R.mipmap.ic_app_comment);
                        myGrad.setColor(context.getResources().getColor(R.color.bg_title));
                    } else
                        NotificationUtils.setImageView(context, mViewHolder.imgLogoView, pushMessage.msgTypeId);
                }

            } else
                mViewHolder.imgLogoView.setVisibility(View.GONE);
            mViewHolder.timeView.setText(Utils.getInstance(context).dateFormat(messageDB.timestamp));
            mViewHolder.stateView.setVisibility(View.GONE);
            if (context.getResources().getString(R.string.message_comment).equals(messageDB.title))
                mViewHolder.titleView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.titleView.getTextSize(),Html.fromHtml(messageDB.title)));
            else
                mViewHolder.titleView.setText(Html.fromHtml(pushMessage.content));
        } catch (Exception e) {
            e.printStackTrace();
        }


        return convertView;
    }


    private class ViewHolder {


        ImageView imgLogoView;
        TextView titleView;
        TextView stateView;
        TextView timeView;
        TextView isReadView;
        ImageView icon_check;
        SimpleDraweeView companyLogoView;
    }

    public void myNotifyDataSetChanged(List<MessageDB> list) {
        dataList = list;
        notifyDataSetChanged();
    }

    public List<MessageDB> getList() {
        return dataList;
    }

    public void chaneIsRead(int position) {
        MessageDB messageDB = dataList.get(position);
        messageDB.messageState = 1;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        MessageDB messageDB = dataList.get(position);
        dataList.remove(messageDB);
        notifyDataSetChanged();
    }
}
