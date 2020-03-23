package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 回复列表适配器
 * Created by Duke on 2015/8/18.
 */
public class CommentAdapter extends BaseAdapter implements ViewListener {


    Context context;
    List<Comment> list = new ArrayList<Comment>();

    public CommentAdapter(Context context, List<Comment> list) {

        this.context = context;
        this.list = list;


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
            convertView = LayoutInflater.from(context).inflate(R.layout.commentlist_item_layout, null);
            mViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_text);
            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.userNameView.setTextColor(context.getResources().getColor(R.color.comment_name));
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.timeLayout = convertView.findViewById(R.id.time_layout);
            mViewHolder.textPaddingView = convertView.findViewById(R.id.text_padding);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            AudioListAdapter audioListAdapter = new AudioListAdapter(context, this);
            mViewHolder.voiceListview.setAdapter(audioListAdapter);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.textPaddingView.setVisibility(View.GONE);
        mViewHolder.timeLayout.setVisibility(View.GONE);

        Comment comment = list.get(position);
        NewUser user = comment.getData(NewUser.class);
        mViewHolder.userNameView.setText(Html.fromHtml(user.name));
        mViewHolder.timestampView.setText(Utils.getInstance(context).dateFormat(comment.timestamp));
//        mViewHolder.timestampView.setDettailText(context.getResources().getString(R.string.create) + "：" + Utils.dateFormat(comment.timestamp));
        if (TextUtils.isEmpty(comment.content)) {
            mViewHolder.contentTextView.setVisibility(View.GONE);
        } else {
            mViewHolder.contentTextView.setVisibility(View.VISIBLE);
            mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.contentTextView.getTextSize(),Html.fromHtml(comment.content)));
        }

        ImageOptions.setUserImage(mViewHolder.userPhotoView, user.photo);
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
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imageGridView, context, images);
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


    private class ViewHolder {

        NoScrollGridview imageGridView;
        TextView contentTextView;
        TextView timestampView;
        TextView userNameView;
        SimpleDraweeView userPhotoView;
        View timeLayout;
        NoScrollListview voiceListview;
        View textPaddingView;

    }

    public void myNotifyDataSetChanged(List<Comment> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
