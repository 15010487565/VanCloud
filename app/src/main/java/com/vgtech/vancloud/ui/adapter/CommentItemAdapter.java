package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.common.utils.PublishConstants;
import com.vgtech.vancloud.ui.common.publish.module.Pcomment;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 回复列表适配器
 * Created by Duke on 2015/8/18.
 */
public class CommentItemAdapter extends BaseAdapter implements ViewListener, View.OnClickListener {


    Context context;
    List<Comment> list = new ArrayList<Comment>();

    public CommentItemAdapter(Context context, List<Comment> list) {

        this.context = context;
        this.list = list;
    }
    private int position;

    public void setPosition(int position) {
        this.position = position;
    }

    private String typeId;

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public List<Comment> getList() {
        return list;
    }

    @Override
    public int getCount() {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.common_item, null);
            convertView.setOnClickListener(this);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_text);
            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            AudioListAdapter audioListAdapter = new AudioListAdapter(context, this);
            mViewHolder.voiceListview.setAdapter(audioListAdapter);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.setTag(R.string.app_name,position);
        Comment comment = list.get(position);
        convertView.setTag(R.mipmap.ic_launcher,comment);
        NewUser user = comment.getData(NewUser.class);

        mViewHolder.userNameView.setText(Html.fromHtml(user.name));
        mViewHolder.timestampView.setText(Utils.getInstance(context).dateFormat(comment.timestamp));
//        mViewHolder.timestampView.setDettailText(context.getResources().getString(R.string.create) + "：" + Utils.dateFormat(comment.timestamp));
        JSONObject jsonObject = comment.getJson();
        String replyuser = "";
        if (jsonObject.has("replyuser")) {
            Object obj = null;
            try {
                obj = jsonObject.get("replyuser");
                if (obj instanceof JSONObject) {
                    JSONObject replayObject = (JSONObject) obj;
                    replyuser = replayObject.getString("name");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if (TextUtils.isEmpty(comment.content)) {
            if (!TextUtils.isEmpty(replyuser)) {
                mViewHolder.contentTextView.setVisibility(View.VISIBLE);
                StringBuffer namebuf = new StringBuffer();
                namebuf
                        .append(context.getString(R.string.comment))
                        .append("<font color=\"#5577a7\">").append(replyuser).append("</font>")
                        .append("<font color=\"#5577a7\">").append(": ").append("</font>");
                mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.contentTextView.getTextSize(),Html.fromHtml(namebuf.toString())));
            } else {
                mViewHolder.contentTextView.setVisibility(View.GONE);
            }
        } else {
            mViewHolder.contentTextView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(replyuser)) {
                StringBuffer namebuf = new StringBuffer();
                namebuf
                        .append(context.getString(R.string.comment))
                        .append("<font color=\"#5577a7\">").append(replyuser).append("</font>")
                        .append("<font color=\"#5577a7\">").append(": ").append("</font>");
                namebuf.append(comment.content);
                mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context,mViewHolder.contentTextView.getTextSize(), Html.fromHtml(namebuf.toString())));
            } else {
                mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.contentTextView.getTextSize(),Html.fromHtml(comment.content)));
            }
        }
        ImageOptions.setUserImage(mViewHolder.userPhotoView,user.photo);
        UserUtils.enterUserInfo(context, user.userid + "", user.name, user.photo, mViewHolder.userPhotoView);

        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(comment.getJson().getString("audio"))) {
                audios = JsonDataFactory.getDataArray(AudioInfo.class, comment.getJson().getJSONArray("audio"));
            }
            if (!TextUtils.isEmpty(comment.getJson().getString("image"))) {
                images = JsonDataFactory.getDataArray(ImageInfo.class, comment.getJson().getJSONArray("image"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (images.size() > 0) {
            mViewHolder.imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imageGridView, context, images, Utils.convertDipOrPx(context, 40), 0);
            mViewHolder.imageGridView.setAdapter(imageGridviewAdapter);
        } else {
            mViewHolder.imageGridView.setVisibility(View.GONE);
        }

        AudioListAdapter audioListAdapter = (AudioListAdapter) mViewHolder.voiceListview.getAdapter();
        if (audios.size() > 0) {
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            mViewHolder.voiceListview.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.voiceListview.setVisibility(View.GONE);
        }

        return convertView;
    }

    private View lastView;

    @Override
    public View getLastView() {
        return lastView;
    }

    @Override
    public void setLastView(View view) {
        lastView = view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.comment_item:
                Comment comment = (Comment) v.getTag(R.mipmap.ic_launcher);
              //  int position = (int) v.getTag(R.string.app_name);
                NewUser user = comment.getData(NewUser.class);
                PublishTask publishTask = new PublishTask();
                publishTask.setPosition(position);
                publishTask.type = PublishConstants.PUBLISH_COMMENT;
                Pcomment pcomment = new Pcomment();
                pcomment.commentId = typeId;
                pcomment.commentType = PublishUtils.COMMENTTYPE_SHARE;
                pcomment.replyuserid = user.userid;
                pcomment.replayUser = user.name;
                pcomment.content = "";
                Gson gson = new Gson();
                publishTask.content = gson.toJson(pcomment);
                Intent intent = new Intent(context, NewPublishedActivity.class);
                intent.putExtra("publishTask", publishTask);
                context.startActivity(intent);
                break;
        }
    }


    private class ViewHolder {

        NoScrollGridview imageGridView;
        TextView contentTextView;
        TextView timestampView;
        TextView userNameView;
        SimpleDraweeView userPhotoView;
        NoScrollListview voiceListview;

    }

    public void myNotifyDataSetChanged(List<Comment> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
