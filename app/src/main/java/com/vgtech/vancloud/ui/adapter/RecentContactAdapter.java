package com.vgtech.vancloud.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.provider.db.User;
import com.vgtech.vancloud.R;

/**
 * Created by brook on 16/9/26.
 */
public class RecentContactAdapter extends DataAdapter<User> {

    Context mContext;
    public RecentContactAdapter(Context context) {
        this.mContext = context;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recent_contact_item, null);
            assert convertView != null;
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        User user = dataSource.get(position);
        ImageOptions.setUserImage(viewHolder.photoIv,user.photo);
        viewHolder.userNameTxt.setText(user.getName());
        viewHolder.jobTxt.setText(user.job);
        return convertView;
    }

    public class ViewHolder {
        TextView userNameTxt;
        TextView jobTxt;
        SimpleDraweeView photoIv;

        public ViewHolder(final View view) {
            userNameTxt = (TextView) view.findViewById(R.id.userName_txt);
            jobTxt = (TextView) view.findViewById(R.id.job_txt);
            photoIv = (SimpleDraweeView) view.findViewById(R.id.photo_iv);

        }
    }
}
