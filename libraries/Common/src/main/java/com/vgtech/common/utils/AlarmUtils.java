package com.vgtech.common.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2015/10/15.
 * 事件提醒
 */
public class AlarmUtils {
    public static String ALARMALERT = "com.vgtech.vancloud.alarm.action";

    public static void setAlarmTime(Context context,long timeInMillis,String content){
//        timeInMillis=System.currentTimeMillis()+10*1000;
        if(timeInMillis<System.currentTimeMillis())
            return;
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARMALERT);
        intent.putExtra("content",content);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        int interval = 60 * 1000;//提醒间隔，这里设为1分钟提醒一次，在第2步我们将每隔1分钟收到一次广播
//        am.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, interval, sender);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, sender);
    }
}
