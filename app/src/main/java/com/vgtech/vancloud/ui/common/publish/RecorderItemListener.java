package com.vgtech.vancloud.ui.common.publish;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.net.NetSilentAsyncTask;
import com.vgtech.vancloud.ui.common.publish.internal.Recorder;
import com.vgtech.vancloud.ui.common.record.MediaManager;
import com.vgtech.vancloud.ui.common.record.RecorderAdapter;
import com.vgtech.common.FileCacheUtils;

import java.io.File;

import roboguice.util.Strings;

/**
 * Created by zhangshaofang on 2016/1/7.
 */
public class RecorderItemListener implements AdapterView.OnItemClickListener {
    private Context mContext;
    private View viewanim;
    public RecorderItemListener(Context context) {
        mContext = context;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view,
                            int position, long id) {
        RecorderAdapter mAdapter = (RecorderAdapter) parent.getAdapter();
        final Recorder recorder = mAdapter.getData().get(position);
        if (recorder.isLocal()) {
            play(view, recorder.filePathString);
        } else {
            final Uri uri = Uri.parse(recorder.filePathString);
            String namePath = uri.getLastPathSegment();
            int pindex = namePath.indexOf(".");
            String name = namePath.substring(0, pindex);
            String cachePath = FileCacheUtils.getAudioDir(mContext).getPath() + "/" + name + "." + "amr";
            File file = new File(cachePath);
            final View fail = view.findViewById(R.id.messages_item_fail);
            if (file.exists()) {
                play(view, cachePath);
            } else {
                new NetSilentAsyncTask<String>(mContext) {
                    @Override
                    protected void onSuccess(String filePath) throws Exception {

                        Log.e("ceshi", filePath);

                        if (Strings.isEmpty(filePath)) return;

                        play(view, filePath);
                    }

                    @Override
                    protected String doInBackground() throws Exception {
                        return net().download(uri.toString(), "amr", (Activity) context);
                    }

                    @Override
                    protected void onThrowable(Throwable t) throws RuntimeException {
                        Log.e("ceshi", "--------------------------------------");
                        fail.setVisibility(View.VISIBLE);
                        viewanim.setBackgroundResource(R.drawable.icon_voice_right3);
                        super.onThrowable(t);
                    }
                }.execute();
            }
        }
    }
    public void play(View view, String path) {
        if (viewanim != null) {//让第二个播放的时候第一个停止播放
            viewanim.setBackgroundResource(R.drawable.icon_voice_right3);
            viewanim = null;
        }
        viewanim = view.findViewById(R.id.id_recorder_anim);
        viewanim.setBackgroundResource(R.drawable.play);
        AnimationDrawable drawable = (AnimationDrawable) viewanim
                .getBackground();
        drawable.start();

        // 播放音频
        MediaManager.playSound(path,
                new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        viewanim.setBackgroundResource(R.drawable.icon_voice_right3);

                    }
                },viewanim);
    }
}
