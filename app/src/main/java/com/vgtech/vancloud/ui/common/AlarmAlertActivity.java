package com.vgtech.vancloud.ui.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.PublishConstants;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.service.AlarmKlaxonService;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.module.Pschedule;
import com.vgtech.vancloud.ui.common.publish.module.Ptask;
import com.vgtech.vancloud.ui.module.schedule.ScheduleDetailActivity;
import com.vgtech.vancloud.ui.module.task.TaskTransactActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhangshaofang on 2015/10/19.
 */
public class AlarmAlertActivity extends BaseActivity {
    @Override
    protected int getContentView() {
        return R.layout.tip_alertdialog;
    }

    public PublishTask publishTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFinishOnTouchOutside(false);
        super.onCreate(savedInstanceState);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        String content = getIntent().getStringExtra("content");
        publishTask = new Gson().fromJson(content, PublishTask.class);
        String tipTip = "";
        String ok = "";
        String tipContent = "";
        if (publishTask.type == PublishConstants.PUBLISH_TASK) {
            tipTip = getString(R.string.lable_task_tip);
            ok = getString(R.string.look_task);
            try {
                Ptask mTask = JsonDataFactory.getData(Ptask.class, new JSONObject(publishTask.content));
                tipContent = mTask.content;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (publishTask.type == PublishConstants.PUBLISH_SCHEDULE) {
            tipTip = getString(R.string.lable_schedule_tip);
            ok = getString(R.string.look_schedule);
            try {
                Pschedule mTask = JsonDataFactory.getData(Pschedule.class, new JSONObject(publishTask.content));
                tipContent = mTask.content;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }  else {
            finish();
        }
        StringBuffer msg = new StringBuffer();
        msg.append(EmojiFragment.getEmojiContent(this, 0,tipContent));
        findViewById(R.id.et_msg).setVisibility(View.GONE);
        TextView titleTv = (TextView) findViewById(R.id.txt_title);
        titleTv.setText(tipTip);

        TextView msgTv = (TextView) findViewById(R.id.txt_msg);
        msgTv.setText(msg);

        TextView btn_neg = (TextView) findViewById(R.id.btn_neg);
        btn_neg.setText(R.string.cancel);
        TextView btn_pos = (TextView) findViewById(R.id.btn_pos);
        btn_pos.setText(ok);
        btn_neg.setOnClickListener(this);
        btn_pos.setOnClickListener(this);
        Intent intent = new Intent(this, AlarmKlaxonService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        timer.schedule(task, 1000 * 60);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_neg:
                finish();
                break;
            case R.id.btn_pos:
                if (publishTask.type == PublishConstants.PUBLISH_TASK) {
                    String taskId = publishTask.publishId;
                    Intent intent = new Intent(AlarmAlertActivity.this, TaskTransactActivity.class);
                    intent.putExtra("TaskID", taskId);
                    startActivity(intent);
                } else if (publishTask.type == PublishConstants.PUBLISH_SCHEDULE) {
                    String scheduleId = publishTask.publishId;
                    Intent intent = new Intent(AlarmAlertActivity.this, ScheduleDetailActivity.class);
                    intent.putExtra("scheduleId", scheduleId);
                    startActivity(intent);
                } else if (publishTask.type == PublishConstants.PUBLISH_WORKREPORT) {

                }
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        closeTimer();
        unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final Timer timer = new Timer();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            finish();
            super.handleMessage(msg);
        }
    };

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private void closeTimer() {
        try {
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
