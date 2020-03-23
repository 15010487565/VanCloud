package com.vgtech.vancloud.ui.common.publish;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.common.publish.internal.Recorder;
import com.vgtech.vancloud.ui.common.record.DialogManager;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.vancloud.utils.Utils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import roboguice.util.Ln;

/**
 * Created by zhangshaofang on 2016/1/7.
 */
public class RecordTouchListener implements View.OnTouchListener {
    private float downY, curY;
    private Button recButton;
    private Context mContext;
    private int minute;
    private float second;
    private RecordFinishListener mRecorderFinishListener;

    public RecordTouchListener(Context context, Button recButton, RecordFinishListener listener) {
        mContext = context;
        this.recButton = recButton;
        mRecorderFinishListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                startRecord();
                recButton.setText(mContext.getString(R.string.loosen_end));
                recButton.setSelected(true);
                return true;
            case MotionEvent.ACTION_UP:
                boolean shortTime = false;
                if (minute == 0 && second < 2) {
                    shortTime = true;
                }
                stopRecord();
                recButton.setText(mContext.getString(R.string.hold_down_talk));
                recButton.setSelected(false);
                if (downY - curY > Utils.convertDipOrPx(mContext, 60)) {
                    return true;
                }
                if (shortTime) {
                    tipView(3);
                    return true;
                }
                sendRecord();
                return true;
            case MotionEvent.ACTION_CANCEL:
                stopRecord();
                recButton.setText(mContext.getString(R.string.hold_down_talk));
                recButton.setSelected(false);
                return true;
            case MotionEvent.ACTION_MOVE:
                curY = event.getY();
                if (downY - curY > Utils.convertDipOrPx(mContext, 60)) {
                    tipView(1);
                } else {
                    tipView(0);
                }
                return true;
        }
        return false;
    }

    MediaRecorder mediaRecorder;
    private boolean mSend;

    private void sendRecord() {
        if (mSend)
            return;
        mSend = true;
        Recorder recorder = new Recorder(minute * 60 + (int) second, audioFile.getAbsolutePath());
        mRecorderFinishListener.recorderFinished(recorder);
    }

    private void stopRecord() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                Ln.e(e);
            }
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mDialogManager != null) {
            mDialogManager.dimissDialog();
        }
    }

    @SuppressWarnings("deprecation")
    private void tipView(int type) {
        if (type == 3) {
            createRecordTipView();
        }
        if (mDialogManager == null) {
            return;
        }
        if (type == 0) {
            mDialogManager.recording();
        } else if (type == 1) {
            mDialogManager.wantToCancel();
        } else if (type == 2) {
            int second = (int) this.second;
            mDialogManager.setTime((minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second));
            mDialogManager.updateVoiceLevel(getVoiceLevel(7));
        } else if (type == 3) {
            mDialogManager.tooShort();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDialogManager.dimissDialog();
                }
            }, 1000);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 3200:
                    recButton.setText(mContext.getString(R.string.hold_down_talk));
                    recButton.setSelected(false);
                    sendRecord();
                    break;
                default:
                    tipView(2);
                    break;
            }


        }
    };

    // 获得声音的level
    public int getVoiceLevel(int maxLevel) {
        // mRecorder.getMaxAmplitude()这个是音频的振幅范围，值域是1-32767
        if (mediaRecorder != null) {
            try {
                // 取证+1，否则去不到7
                return maxLevel * mediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
                // TODO Auto-generated catch block

            }
        }

        return 1;
    }

    Timer timer;
    File audioFile;

    private boolean startRecord() {
        mSend = false;
        try {
            stopRecord();
            audioFile = new File(FileCacheUtils.getPublishAudioDir(mContext), String.valueOf(System.currentTimeMillis())
                    + ".amr");
            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.setAudioChannels(1);
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            timer = new Timer();
            minute = 0;
            second = 0f;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    second += 0.1f;
                    if (second >= 60) {
                        second = 0;
                        minute++;
                    }
                    if (minute >= 1) {
                        stopRecord();
                        handler.sendEmptyMessage(3200);
                    }
                    handler.sendEmptyMessage(1);
                }
            }, 100, 100);

            createRecordTipView();
        } catch (Exception e) {
            stopRecord();
            Ln.e(e);
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private DialogManager mDialogManager;

    private void createRecordTipView() {
        mDialogManager = new DialogManager(mContext);
        mDialogManager.showRecordingDialog();
    }
}
