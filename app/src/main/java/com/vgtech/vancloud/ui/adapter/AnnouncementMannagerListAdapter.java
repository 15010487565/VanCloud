package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.AnnounceNotify;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.common.utils.PublishConstants;
import com.vgtech.vancloud.ui.common.publish.module.Pannouncement;
import com.vgtech.vancloud.ui.module.announcement.AnnouncementDetailActivity;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

/**
 * Created by app02 on 2015/9/9.
 */
public class AnnouncementMannagerListAdapter extends BaseAdapter{

    private List<AnnounceNotify> data;
    private Context mContext;

    public AnnouncementMannagerListAdapter(Context mContext, List<AnnounceNotify> data){
        this.mContext=mContext;
        this.data=data;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.announcement_mannager_list_item, null);
            mViewHolder.status_icon=(ImageView)convertView.findViewById(R.id.status_icon);
            mViewHolder.announcement_mannager_title=(TextView)convertView.findViewById(R.id.announcement_mannager_title);
            mViewHolder.announcement_mannager_time=(TextView)convertView.findViewById(R.id.announcement_mannager_time);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        AnnounceNotify an= data.get(position);

        mViewHolder.announcement_mannager_title.setText(an.title);
        mViewHolder.announcement_mannager_time.setText(String.format(mContext.getString(R.string.announcement_create_time), Utils.getInstance(mContext).dateFormat(an.timestamp)));

        if("1".equals(an.type)){
            mViewHolder.status_icon.setImageResource(R.mipmap.manuscript);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnnounceNotify an = data.get(position);
                    PublishTask  mPublishTask = new PublishTask();
                    mPublishTask.publishId = an.notifyid;
                    Pannouncement mPannouncement = new Pannouncement();
                    mPannouncement.title = an.title;
                    mPannouncement.content = an.content;
                    mPannouncement.isTop =an.ishigh;
                    mPannouncement.isSend = false;
                    mPublishTask.type = PublishConstants.PUBLISH_ANNOUNCEMENT;
                    Gson gson = new Gson();
                    mPublishTask.content = gson.toJson(mPannouncement);
                    Intent intent = new Intent(mContext, NewPublishedActivity.class);
                    intent.putExtra("json", an.getJson().toString());
                    intent.putExtra("publishTask",mPublishTask);
                    mContext.startActivity(intent);
                }
            });
        }else if("2".equals(an.type)){
            mViewHolder.status_icon.setImageResource(R.mipmap.is_sent);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnnounceNotify an=data.get(position);
                    Intent intent = new Intent(mContext, AnnouncementDetailActivity.class);
                    intent.putExtra("json",an.getJson().toString());
                    mContext.startActivity(intent);
                }
            });
        }

        return convertView;
    }

    class ViewHolder{

        ImageView status_icon;
        TextView announcement_mannager_title;
        TextView announcement_mannager_time;
    }
}
