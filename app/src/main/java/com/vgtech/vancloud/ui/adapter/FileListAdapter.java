package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.utils.MD5;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.AttachFile;
import com.vgtech.vancloud.ui.chat.net.NetSilentAsyncTask;
import com.vgtech.vancloud.ui.web.HelpOpenFileUtils;

import java.io.File;
import java.util.HashMap;

import roboguice.util.Strings;

/**
 * Created by John on 2015/9/14.
 */
public class FileListAdapter extends DataAdapter<AttachFile> {

    private Context mContext;
    private int mMinItemWith;
    private int mMaxItemWith;
    private HashMap<String, Integer> map = new HashMap<>();
    private int mPosition;
    private ViewListener mViewListener;

    public FileListAdapter(Context context, ViewListener viewListener) {
        this.mContext = context;
        mViewListener = viewListener;
        WindowManager wManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wManager.getDefaultDisplay().getMetrics(outMetrics);
        mMaxItemWith = (int) (outMetrics.widthPixels * 0.7f);
        mMinItemWith = (int) (outMetrics.widthPixels * 0.15f);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        FileListAdapter.ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.attach_file_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.attachment_file_name = (TextView) convertView.findViewById(R.id.attachment_file_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        mPosition = position;
        final AttachFile audioInfo = dataSource.get(position);

        viewHolder.attachment_file_name.setText(audioInfo.name);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HelpOpenFileUtils.downloadFile((FragmentActivity) mContext,dataSource.get(position).url);

            }
        });

        return convertView;
    }


    class ViewHolder {
        TextView attachment_file_name;
    }
}















