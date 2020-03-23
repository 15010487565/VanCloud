package com.vgtech.vancloud.ui.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.AnnounceNotify;
import com.vgtech.common.api.AttachFile;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.NewUser;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.module.announcement.AnnouncementDetailActivity;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by app02 on 2015/9/9.
 */
public class AnnouncementListAdapter extends BaseAdapter implements ViewListener {

    private int mUnreadColor;
    private int mReadedColor;
    private List<AnnounceNotify> data;
    private BaseActivity mContext;
    /**
     *  是否显示公告评论和点赞
     *  true 显示
     */
    private boolean isShowNotice;
    public AnnouncementListAdapter(BaseActivity mContext,boolean isShowNotice) {
        this.mContext = mContext;
        this.isShowNotice = isShowNotice;
        data = new ArrayList<>();
        mReadedColor = mContext.getResources().getColor(R.color.notice_readed);
        mUnreadColor = mContext.getResources().getColor(R.color.notice_unread);
    }

    public void clear() {
        data.clear();
    }

    public void add(List<AnnounceNotify> announceNotifies) {
        data.addAll(announceNotifies);
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.announcement_list_item, null);
            mViewHolder.replyButton = (TextView) convertView.findViewById(R.id.reply_button);
            mViewHolder.praiseButton = (TextView) convertView.findViewById(R.id.praise_button);


            mViewHolder.attachementList = (NoScrollListview) convertView.findViewById(R.id.attachement_list);
            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);

            mViewHolder.notifyTitle = (TextView) convertView.findViewById(R.id.notify_title);
            mViewHolder.notifyTime = (TextView) convertView.findViewById(R.id.notify_time);
            mViewHolder.notifyContent = (TextView) convertView.findViewById(R.id.notify_content);
            //评论
            mViewHolder.replyButtonOnclick = (RelativeLayout) convertView.findViewById(R.id.reply_button_onclick);
            //点赞
            mViewHolder.praiseButtonClick = (RelativeLayout) convertView.findViewById(R.id.praise_button_click);

            mViewHolder.llreply = convertView.findViewById(R.id.ll_reply);
            /**
             *  是否显示公告评论和点赞
             *  true 显示
             */
            if (isShowNotice){
                mViewHolder.llreply.setVisibility(View.VISIBLE);
            }else {
                mViewHolder.llreply.setVisibility(View.GONE);
            }

            mViewHolder.scheduleListItemPraiseIcon = (ImageView) convertView.findViewById(R.id.schedule_list_item_praise_icon);

            mViewHolder.attachmentFileName = (TextView) convertView.findViewById(R.id.attachment_file_name);

            mViewHolder.status = (TextView) convertView.findViewById(R.id.status);
            mViewHolder.mIvReadTag = (ImageView) convertView.findViewById(R.id.iv_read_tag);
//            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);

//            convertView.findViewById(R.id.left_time_layout).setVisibility(View.GONE);
//            convertView.findViewById(R.id.right_time_layout).setVisibility(View.GONE);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.replyButton.setCompoundDrawablePadding(Utils.convertDipOrPx(mContext, 10));
        mViewHolder.praiseButton.setCompoundDrawablePadding(Utils.convertDipOrPx(mContext, 10));

        final AnnounceNotify item = data.get(position);
        final NewUser user = item.getData(NewUser.class);
        final List<ImageInfo> imags = item.getArrayData(ImageInfo.class);
        final List<AudioInfo> audios = item.getArrayData(AudioInfo.class);

        List<AttachFile> files = item.getArrayData(AttachFile.class);

        //1未读，2已读
        int isread = 1;
        try {
            isread = Integer.parseInt(item.isread);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mViewHolder.notifyTitle.setText(item.title);
        mViewHolder.notifyTime.setText(Utils.getInstance(mContext).dateFormat(item.timestamp));
        mViewHolder.notifyContent.setText(EmojiFragment.getEmojiContent(mContext, mViewHolder.notifyContent.getTextSize(),Html.fromHtml(item.content)));

        if (item != null && item.comments > 0) {
            mViewHolder.replyButton.setText(item.comments + "");
        } else {
            mViewHolder.replyButton.setText(mContext.getResources().getString(R.string.comment));
        }
        if (item != null && item.praises > 0) {
            mViewHolder.praiseButton.setText(item.praises + "");
        } else {
            mViewHolder.praiseButton.setText(mContext.getResources().getString(R.string.praise));
        }

        if (imags != null && imags.size() > 0) {
            mViewHolder.imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imageGridView, mContext, imags,null);
            mViewHolder.imageGridView.setAdapter(imageGridviewAdapter);
        } else {
            mViewHolder.imageGridView.setVisibility(View.GONE);
        }


        if (audios != null && !audios.isEmpty()) {
            mViewHolder.voiceListview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(mContext, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            mViewHolder.voiceListview.setAdapter(audioListAdapter);
        } else {
            mViewHolder.voiceListview.setVisibility(View.GONE);

        }

        if (files != null && !files.isEmpty()) {
            mViewHolder.attachementList.setVisibility(View.VISIBLE);
            FileListAdapter fileListAdapter = new FileListAdapter(mContext, this);
            fileListAdapter.dataSource.clear();
            fileListAdapter.dataSource.addAll(files);
            mViewHolder.attachementList.setAdapter(fileListAdapter);
            fileListAdapter.notifyDataSetChanged();

        } else
            mViewHolder.attachementList.setVisibility(View.GONE);

        final View finalConvertView = convertView;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    PrfUtils.setFirstNotifyId(mContext, item.notifyid);
                }
//                item.isread = "2";
//                getView(position, finalConvertView,parent);
                Intent intent = new Intent(mContext, AnnouncementDetailActivity.class);
                intent.putExtra("json", item.getJson().toString());
                intent.putExtra("position", position);
                intent.putExtra("fromeNotice", true);
                /**
                 *  是否显示公告评论和点赞
                 *  true 显示
                 */
                intent.putExtra("isShowNotice",isShowNotice);
                mContext.startActivityForResult(intent, 1);
            }
        });

        mViewHolder.replyButtonOnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.comments > 0) {
                    if (position == 0) {
                        PrfUtils.setFirstNotifyId(mContext, item.notifyid);
                    }
                    Intent intent = new Intent(mContext, AnnouncementDetailActivity.class);
                    intent.putExtra("json", item.getJson().toString());
                    intent.putExtra("position", position);
                    intent.putExtra("showcomment", true);
                    mContext.startActivityForResult(intent, 1);

                } else {
                    PublishUtils.addComment(mContext, PublishUtils.COMMENTTYPE_ANNOUNCEMENT, item.notifyid + "", position);
                }
            }
        });

        if (item.ispraise) {
            mViewHolder.scheduleListItemPraiseIcon.setImageResource(R.drawable.item_praise_click_red);
            mViewHolder.praiseButton.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            mViewHolder.scheduleListItemPraiseIcon.setImageResource(R.drawable.item_praise_click);
            mViewHolder.praiseButton.setTextColor(EditUtils.greyCreateColorStateList());
        }

        mViewHolder.praiseButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                item.ispraise=!item.ispraise;
                PublishUtils.toDig(mContext, item.notifyid + "", PublishUtils.COMMENTTYPE_ANNOUNCEMENT, data.get(position).ispraise, new PublishUtils.DigCallBack() {

                    @Override
                    public void successful(boolean digType) {
                        if (digType)
                            data.get(position).praises -= data.get(position).praises > 0 ? 1 : 0;
                        else
                            data.get(position).praises += 1;
                        data.get(position).ispraise = !digType;
                        try {
                            data.get(position).getJson().put("praises", data.get(position).praises);
                        } catch (Exception e) {
                        }
                        try {
                            data.get(position).getJson().put("ispraise", !digType);
                        } catch (Exception e) {
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });

        boolean isMine = false;
        try {
            isMine = user.userid.equals(PrfUtils.getUserId(mContext));
        } catch (Exception e) {
        }

        if (!isMine && item.isread != null && "0".equals(item.isread)) {
            mViewHolder.status.setVisibility(View.VISIBLE);
            mViewHolder.status.setBackgroundResource(R.mipmap.operation_red);
            mViewHolder.status.setText(mContext.getString(R.string.is_not_read));
        } else// if(item.isread!=null && item.isread.equals("1"))
            mViewHolder.status.setVisibility(View.GONE);

        if (isread == 2){
            mViewHolder.notifyTitle.setTextColor(mReadedColor);
            mViewHolder.notifyContent.setTextColor(mReadedColor);
            mViewHolder.replyButton.setTextColor(mReadedColor);
            mViewHolder.praiseButton.setTextColor(mReadedColor);
//            mViewHolder.attachmentFileName.setTextColor(mReadedColor);
            mViewHolder.status.setTextColor(mReadedColor);
            mViewHolder.mIvReadTag.setImageResource(R.mipmap.icon_readed);
        }else {
            mViewHolder.notifyTitle.setTextColor(mUnreadColor);
            mViewHolder.notifyContent.setTextColor(mUnreadColor);
            mViewHolder.replyButton.setTextColor(mUnreadColor);
            mViewHolder.praiseButton.setTextColor(mUnreadColor);
//            mViewHolder.attachmentFileName.setTextColor(mReadedColor);
            mViewHolder.status.setTextColor(mUnreadColor);
            mViewHolder.mIvReadTag.setImageResource(R.mipmap.icon_unread);
        }

        return convertView;
    }

    class ViewHolder {

        TextView replyButton;
        TextView praiseButton;

        NoScrollGridview imageGridView;
        NoScrollListview voiceListview;

        NoScrollListview attachementList;

        TextView notifyTitle;
        TextView notifyTime;
        TextView notifyContent;

        LinearLayout llreply;
        RelativeLayout replyButtonOnclick;
        RelativeLayout praiseButtonClick;
        ImageView scheduleListItemPraiseIcon;

        TextView attachmentFileName;

        TextView status;
        ImageView mIvReadTag;
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

    private String getDateFormatString(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    public void chaneCommentNum(int position) {
        AnnounceNotify item = data.get(position);
        int num = item.comments;
        item.comments = num + 1;
        try {
            item.getJson().put("comments", item.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneCommentNum(int position, int commentCount) {
        AnnounceNotify item = data.get(position);
        item.comments = commentCount;
        try {
            item.getJson().put("comments", item.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneScheduleState(int position, boolean isPraise, int paraiseCount) {
        if (position == -1)
            return;
        AnnounceNotify item = data.get(position);
        item.ispraise = isPraise;
        item.isread = "2";
        item.praises = paraiseCount;
        try {
            item.getJson().put("ispraise", item.ispraise);
            item.getJson().put("isread", item.isread);
            item.getJson().put("praises", item.praises);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }
}
