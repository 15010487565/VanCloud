package com.vgtech.vancloud.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vgtech.common.utils.AlarmUtils;
import com.vgtech.vancloud.ui.common.AlarmAlertActivity;
import com.vgtech.vancloud.ui.common.AlertActivity;

/**
 * Created by Administrator on 2015/10/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static String SCHEDULEACTION = "com.vgtech.vancloud.schedule";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AlarmUtils.ALARMALERT.equals(intent.getAction())) {
            //提醒时间到，执行相应操作,可以继续设置下一次提醒时间; TODO
            Intent i = new Intent(context, AlarmAlertActivity.class);
            i.putExtra("content", intent.getStringExtra("content"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return;
        } else if (SCHEDULEACTION.equals(intent.getAction())) {
            Intent i = new Intent(context, AlertActivity.class);
            i.putExtra("publishId", intent.getStringExtra("publishId"));
            i.putExtra("msg", intent.getStringExtra("msg"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
