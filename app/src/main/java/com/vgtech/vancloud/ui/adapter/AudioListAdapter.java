package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.api.ImageInfo;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.vancloud.ui.chat.net.NetSilentAsyncTask;
import com.vgtech.vancloud.ui.common.record.MediaManager;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.vancloud.utils.Utils;

import java.io.File;
import java.util.HashMap;

import roboguice.util.Strings;

/**
 * Created by John on 2015/9/14.
 */
public class AudioListAdapter extends DataAdapter<AudioInfo> implements View.OnClickListener {

    private Context context;
    private HashMap<String, Integer> map = new HashMap<>();
    private int mPosition;
    private ViewListener mViewListener;
    public boolean small;
    private int mMaxInner;
    private int mMaxOutter;
    private int mMinLength;

    public AudioListAdapter(Context context, ViewListener viewListener) {
        this.context = context;
        mViewListener = viewListener;
        WindowManager wManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wManager.getDefaultDisplay().getMetrics(outMetrics);
        int maxWidth = context.getResources().getDisplayMetrics().widthPixels - Utils.convertDipOrPx(context, 160);
        mMinLength = Utils.convertDipOrPx(context, 25);
        mMaxInner = (int) (maxWidth * 0.7f / 10);
        mMaxOutter = (int) (maxWidth * 0.3f / 50);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.seconds = (TextView) convertView.findViewById(R.id.recorder_time);
            viewHolder.length = convertView.findViewById(R.id.recorder_length);
            viewHolder.deleteView = convertView.findViewById(R.id.btn_voice_delete);
            viewHolder.deleteView.setVisibility(View.GONE);
            viewHolder.animView = convertView.findViewById(R.id.id_recorder_anim);
            viewHolder.messagesItemFailView = (ImageView) convertView.findViewById(R.id.messages_item_fail);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        mPosition = position;
        final AudioInfo audioInfo = dataSource.get(position);

        viewHolder.seconds.setText(audioInfo.time + "\"");
        ViewGroup.LayoutParams lParams = viewHolder.length.getLayoutParams();

        int duration = audioInfo.time;
        if (duration > 60)
            duration = 60;
        int inner = 0, outter = 0;
        if (duration <= 10) {
            inner = duration;
        } else {
            inner = 10;
            outter = duration - 10;
        }
        lParams.width = inner * mMaxInner + outter * mMaxOutter + mMinLength;
        viewHolder.length.setLayoutParams(lParams);
        convertView.setTag(R.string.app_name, audioInfo);
        viewHolder.messagesItemFailView.setVisibility(View.GONE);
        viewHolder.animView.setBackgroundResource(R.drawable.icon_voice_right3);
        if (audioInfo.state == AudioInfo.STATE_PLAY) {
            mViewListener.setLastView(convertView);
            final View viewanim = viewHolder.animView;
            viewanim.setBackgroundResource(R.drawable.play);
            viewanim.post(new Runnable() {
                @Override
                public void run() {
                    Drawable drawable = viewanim
                            .getBackground();
                    if (drawable instanceof AnimationDrawable) {
                        AnimationDrawable animdrawable = (AnimationDrawable) drawable;
                        animdrawable.start();
                    }

                }
            });
        } else if (audioInfo.state == AudioInfo.STATE_FAIL) {
            viewHolder.messagesItemFailView.setVisibility(View.VISIBLE);
        }


        return convertView;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.audio_view:
                final AudioInfo audioInfo = (AudioInfo) v.getTag(R.string.app_name);

                View mLastAudioInfo = mViewListener.getLastView();
                if (mLastAudioInfo != null) {//让第二个播放的时候第一个停止播放
                    AudioInfo lastInfo = (AudioInfo) mLastAudioInfo.getTag(R.string.app_name);
                    if (lastInfo.state == AudioInfo.STATE_PLAY) {
                        lastInfo.state = 0;
                    }
                    View viewanim = mLastAudioInfo.findViewById(R.id.id_recorder_anim);
                    ;
                    viewanim.setBackgroundResource(R.drawable.icon_voice_right3);
                }
                audioInfo.state = AudioInfo.STATE_PLAY;
                mViewListener.setLastView(v);
                final View viewanim = v.findViewById(R.id.id_recorder_anim);
                viewanim.setBackgroundResource(R.drawable.play);
                AnimationDrawable drawable = (AnimationDrawable) viewanim
                        .getBackground();
                drawable.start();
                if (ImageInfo.isLocal(audioInfo.url)) {
                    File file = new File(audioInfo.url);
                    if (file.exists()) {
                        MediaManager.playSound(audioInfo.url,
                                new MediaPlayer.OnCompletionListener() {

                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        audioInfo.state = 0;
                                        viewanim.setBackgroundResource(R.drawable.icon_voice_right3);

                                    }
                                }, viewanim);
                        return;
                    }
                }
                Uri uri = Uri.parse(audioInfo.url);
                String namePath = uri.getLastPathSegment();
                int pindex = namePath.indexOf(".");
                String name = namePath.substring(0, pindex);
                String cachePath = FileCacheUtils.getAudioDir(context) + "/" + name + "." + "amr";
                File file = new File(cachePath);
                final View fail = v.findViewById(R.id.messages_item_fail);
                if (file.exists()) {
                    MediaManager.playSound(cachePath,
                            new MediaPlayer.OnCompletionListener() {

                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    audioInfo.state = 0;
                                    viewanim.setBackgroundResource(R.drawable.icon_voice_right3);

                                }
                            }, viewanim);
                } else {
                    new NetSilentAsyncTask<String>(context) {
                        @Override
                        protected void onSuccess(String filePath) throws Exception {

                            Log.e("ceshi", filePath);

                            if (Strings.isEmpty(filePath)) return;

                            MediaManager.playSound(filePath,
                                    new MediaPlayer.OnCompletionListener() {

                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            audioInfo.state = 0;
                                            viewanim.setBackgroundResource(R.drawable.icon_voice_right3);
                                        }

                                    }, viewanim);
                        }

                        @Override
                        protected String doInBackground() throws Exception {
                            return net().download(audioInfo.url, "amr", (Activity) context);
                        }


                        @Override
                        protected void onThrowable(Throwable t) throws RuntimeException {
                            Log.e("ceshi", "--------------------------------------");
//                            viewanim.setBackgroundResource(R.drawable.icon_voice_right3);
//                            viewanim = null;
                            fail.setVisibility(View.VISIBLE);
                            viewanim.setBackgroundResource(R.drawable.icon_voice_right3);
                            audioInfo.state = AudioInfo.STATE_FAIL;
                            super.onThrowable(t);
                        }
                    }.execute();
                }
                break;
        }
    }

    class ViewHolder {
        TextView seconds;// 时间
        View length;// 对话框长度
        View deleteView;
        View animView;
        ImageView messagesItemFailView;
    }
}
