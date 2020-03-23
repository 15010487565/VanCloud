package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.ScheduleReciver;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.module.share.SharedInfoActivity;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by code on 2015-10-15
 */
public class ShareCollectionAdapter extends BaseAdapter implements ViewListener {

    Context context;

    public List<SharedListItem> getMlist() {
        return mlist;
    }

    List<SharedListItem> mlist;
    int mPosition;
    private OnSelectListener mListener;
    private boolean isSelect = false;

    public ShareCollectionAdapter(Context context, List<SharedListItem> list) {
        this.context = context;
        this.mlist = list;
    }

    public void setOnSelectListener(OnSelectListener listener) {
        mListener = listener;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        mPosition = position;
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.share_collection_list_item, null);

            mViewHolder.imagegridview = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.user_photo = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.user_name = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.content_text = (TextView) convertView.findViewById(R.id.content_text);
            EditUtils.SetTextViewMaxLines(mViewHolder.content_text,5);
            mViewHolder.voice_listview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            mViewHolder.select = (CheckBox) convertView.findViewById(R.id.checkbox_list_item);
            mViewHolder.timeLayout = convertView.findViewById(R.id.time_layout);
            mViewHolder.reciverTextView = (TextView) convertView.findViewById(R.id.reciver_text);
            AudioListAdapter audioListAdapter = new AudioListAdapter(context, this);
            mViewHolder.voice_listview.setAdapter(audioListAdapter);
            mViewHolder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedListItem item = (SharedListItem) buttonView.getTag();
                    if (isChecked) {
                        if (mListener != null) {
                            mListener.OnSelected(item);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.OnUnSelected(item);
                        }
                    }
                }
            });

            mViewHolder.sharedContent = (LinearLayout) convertView.findViewById(R.id.shared_content_layout);
            mViewHolder.sharedContentText = (TextView) convertView.findViewById(R.id.shared_content_text);
            EditUtils.SetTextViewMaxLines(mViewHolder.sharedContentText,5);
            mViewHolder.sharedImagegridview = (NoScrollGridview) convertView.findViewById(R.id.shared_imagegridview);

            mViewHolder.sharedvoiceListview = (NoScrollListview) convertView.findViewById(R.id.shared_voice_listview);
            mViewHolder.sharedvoiceListview.setAdapter(audioListAdapter);

            mViewHolder.locate_space = (LinearLayout) convertView.findViewById(R.id.locate_space);
            mViewHolder.locate_text = (TextView) convertView.findViewById(R.id.locate_text);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final SharedListItem shareCollectionBean = mlist.get(position);

        NewUser user = shareCollectionBean.getData(NewUser.class);
        SharedListItem sharedItem = shareCollectionBean.getData(SharedListItem.class);//转发
        mViewHolder.user_name.setText(user.name);
        mViewHolder.timestamp.setText(Utils.getInstance(context).dateFormat(Long.parseLong(shareCollectionBean.timestamp)));

        mViewHolder.content_text.setText(EmojiFragment.getEmojiContentWithAt(context, mViewHolder.content_text.getTextSize(),shareCollectionBean.content));
        mViewHolder.timeLayout.setVisibility(View.GONE);
        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        List<ScheduleReciver> recivers = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(shareCollectionBean.getJson().getString("audio"))) {
                audios = JsonDataFactory.getDataArray(AudioInfo.class, shareCollectionBean.getJson().getJSONArray("audio"));
            }
            if (!TextUtils.isEmpty(shareCollectionBean.getJson().getString("image"))) {
                images = JsonDataFactory.getDataArray(ImageInfo.class, shareCollectionBean.getJson().getJSONArray("image"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (!TextUtils.isEmpty(shareCollectionBean.getJson().getString("receiver"))) {
                recivers = JsonDataFactory.getDataArray(ScheduleReciver.class, new JSONObject(shareCollectionBean.getJson().toString()).getJSONArray("receiver"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (shareCollectionBean!=null){
            if (!TextUtils.isEmpty(shareCollectionBean.address)) {
                mViewHolder.locate_space.setVisibility(View.VISIBLE);
                mViewHolder.locate_text.setText(shareCollectionBean.address);
            } else {
                mViewHolder.locate_space.setVisibility(View.GONE);
            }
        }

        String reciversName = "";
        for (ScheduleReciver p : recivers)
            reciversName += p.name + ",";
        reciversName = reciversName.length() > 0 ? reciversName.substring(0, reciversName.length() - 1) : "";
        reciversName += "(" + recivers.size() + context.getString(R.string.schedule_reciver_peple_unit) + ")";
        mViewHolder.reciverTextView.setText(context.getString(R.string.share_collection_info) + reciversName);

        if (!TextUtils.isEmpty(user.photo)) {
            ImageOptions.setUserImage(mViewHolder.user_photo,user.photo);
        }

        if (images.size() > 0) {
            mViewHolder.imagegridview.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imagegridview, context, images);
            mViewHolder.imagegridview.setAdapter(imageGridviewAdapter);
        } else {
            mViewHolder.imagegridview.setVisibility(View.GONE);
        }

        AudioListAdapter audioListAdapter = (AudioListAdapter) mViewHolder.voice_listview.getAdapter();
        if (audios.size() > 0) {
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            mViewHolder.voice_listview.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.voice_listview.setVisibility(View.GONE);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SharedInfoActivity.class);
                intent.putExtra("json", mlist.get(position).getJson().toString());
                context.startActivity(intent);
            }
        });

        if (sharedItem != null) {

            mViewHolder.sharedContent.setVisibility(View.VISIBLE);
            NewUser shareUser = sharedItem.getData(NewUser.class);
            String userName = null;
            if(shareUser!=null)
                userName = "@" + (TextUtils.isEmpty(shareUser.name) ? "" : shareUser.name) + ":";
            while(sharedItem.getData(SharedListItem.class) != null) {
                sharedItem = sharedItem.getData(SharedListItem.class);
                if(sharedItem!=null)
                userName += "@" + (TextUtils.isEmpty(shareUser.name) ? "" : shareUser.name) + ":";
            }
            mViewHolder.sharedContentText.setText(EmojiFragment.getEmojiContentWithAt(context, mViewHolder.sharedContentText.getTextSize(),Html.fromHtml(userName + sharedItem.content)));

            final List<ImageInfo> sharedImages = sharedItem.getArrayData(ImageInfo.class);
            final List<AudioInfo> sharedAudios = sharedItem.getArrayData(AudioInfo.class);

            if (sharedImages != null && sharedImages.size() > 0) {
                mViewHolder.sharedImagegridview.setVisibility(View.VISIBLE);
                ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.sharedImagegridview, context, sharedImages);
                mViewHolder.sharedImagegridview.setAdapter(imageGridviewAdapter);
            } else {
                mViewHolder.sharedImagegridview.setVisibility(View.GONE);
            }

            AudioListAdapter audioListAdapters = (AudioListAdapter) mViewHolder.sharedvoiceListview.getAdapter();
            if (sharedAudios != null && !sharedAudios.isEmpty()) {
                mViewHolder.sharedvoiceListview.setVisibility(View.VISIBLE);

                audioListAdapters.dataSource.clear();
                audioListAdapters.dataSource.addAll(sharedAudios);
                audioListAdapters.notifyDataSetChanged();
                mViewHolder.sharedvoiceListview.setAdapter(audioListAdapters);
            } else {
                mViewHolder.sharedvoiceListview.setVisibility(View.GONE);
            }

            final String jsons=sharedItem.getJson().toString();
            mViewHolder.sharedContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SharedInfoActivity.class);
                    intent.putExtra("json", jsons);
                    context.startActivity(intent);
                }
            });
        } else {
            mViewHolder.sharedContent.setVisibility(View.GONE);
        }

        if (isSelect) {
            mViewHolder.select.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.select.setVisibility(View.GONE);
        }
        mViewHolder.select.setTag(shareCollectionBean);
        boolean isSelect = (mListener != null && mListener.OnIsSelect(shareCollectionBean)) ? true : false;
        mViewHolder.select.setChecked(isSelect);

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
        SimpleDraweeView user_photo;
        TextView user_name;
        TextView timestamp;
        TextView content_text;
        NoScrollGridview imagegridview;
        NoScrollListview voice_listview;
        TextView reciverTextView;
        View timeLayout;
        CheckBox select;
        LinearLayout sharedContent;
        TextView sharedContentText;
        NoScrollGridview sharedImagegridview;
        NoScrollListview sharedvoiceListview;

        LinearLayout locate_space;
        TextView locate_text;
    }

    public void myNotifyDataSetChanged(List<SharedListItem> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<SharedListItem> lists, boolean type) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public interface OnSelectListener {
        /**
         * 选中
         */
        void OnSelected(SharedListItem item);

        /**
         * 取消选中
         */
        void OnUnSelected(SharedListItem item);

        /**
         * 判断是否选中
         */
        boolean OnIsSelect(SharedListItem item);
    }
}

