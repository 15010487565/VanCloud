package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
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
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.NotificationExtension;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.utils.NotificationUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2016/9/23.
 */

public class CommentMessageAdapter extends BaseAdapter {


    private List<MessageDB> dataList = new ArrayList<>();
    private Context context;

    public CommentMessageAdapter(Context context, List<MessageDB> messageDBList) {

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
            convertView = LayoutInflater.from(context).inflate(R.layout.comment_message_item, null);
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.imgLogoView = (ImageView) convertView.findViewById(R.id.img_logo);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.commentView = (TextView) convertView.findViewById(R.id.content_text);
            mViewHolder.timeView = (TextView) convertView.findViewById(R.id.time_text);
            mViewHolder.itemTextView = (TextView) convertView.findViewById(R.id.item_text);
            mViewHolder.isReadView = (TextView) convertView.findViewById(R.id.is_read);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        MessageDB messageDB = dataList.get(position);
        try {
            PushMessage pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
            NotificationExtension notificationExtension = JsonDataFactory.getData(NotificationExtension.class, new JSONObject(pushMessage.getJson().getString("extension")));
            if (0 == messageDB.messageState)
                mViewHolder.isReadView.setVisibility(View.VISIBLE);
            else
                mViewHolder.isReadView.setVisibility(View.GONE);
            NotificationUtils.setImageView(context, mViewHolder.imgLogoView, pushMessage.msgTypeId);
            mViewHolder.timeView.setText(Utils.getInstance(context).dateFormat(messageDB.timestamp));
            mViewHolder.itemTextView.setText(notificationExtension.bizTitle);
            String contentText = "";
            if (!TextUtils.isEmpty(notificationExtension.content))
                contentText = contentText + notificationExtension.content;
            if (!TextUtils.isEmpty(notificationExtension.audio))
                contentText = contentText + context.getResources().getString(R.string.comment_audio);
            if (!TextUtils.isEmpty(notificationExtension.image))
                contentText = contentText + context.getResources().getString(R.string.comment_img);
            mViewHolder.commentView.setText(EmojiFragment.getEmojiContent(context,mViewHolder.commentView.getTextSize(), contentText));
            mViewHolder.userNameView.setText(notificationExtension.name);
            ImageOptions.setUserImage(mViewHolder.userPhotoView,notificationExtension.logo);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
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

    private class ViewHolder {

        SimpleDraweeView userPhotoView;
        ImageView imgLogoView;
        TextView userNameView;
        TextView commentView;
        TextView timeView;
        TextView itemTextView;
        TextView isReadView;
    }
}
