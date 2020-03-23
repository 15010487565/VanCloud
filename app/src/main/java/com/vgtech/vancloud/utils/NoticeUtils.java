package com.vgtech.vancloud.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.image.ImageLoadFresco;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by zhangshaofang on 2015/12/8.
 */
public class NoticeUtils {

    public static boolean isBackground(Context context) {

        boolean background = false;
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningAppProcesses = am
                .getRunningAppProcesses();
        for (RunningAppProcessInfo info : runningAppProcesses) {
            if (info.processName.equals(context.getPackageName())) {
                if (info.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    background = true;
                }
                break;
            }
        }
        return background;
    }

    public static Map<String, ArrayList<CharSequence>> msgMap = new HashMap<String, ArrayList<CharSequence>>();

    public static void clearMessage(Context context) {
        msgMap.clear();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(R.string.app_name);

    }

    public static void showChatNotify(Context context, String userId, String name, String photo, CharSequence ticker, String type) {
        updateAppNum(context);
        String title = "";
        CharSequence content = "";
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.putExtra("chart", true);
        if (msgMap.isEmpty()) {
            appIntent.putExtra("chatType", type);
            appIntent.putExtra("userId", userId);
            appIntent.putExtra("name", name);
            appIntent.putExtra("photo", photo);
            title = name;
            content = ticker;
            ArrayList<CharSequence> conList = new ArrayList<>();
            conList.add(content);
            msgMap.put(userId, conList);
        } else {
            ArrayList<CharSequence> conList = msgMap.get(userId);
            if (conList == null) {
                conList = new ArrayList<>();
                msgMap.put(userId, conList);
            }
            conList.add(ticker);
            if (msgMap.size() == 1) {
                appIntent.putExtra("chatType", type);
                appIntent.putExtra("userId", userId);
                appIntent.putExtra("name", name);
                appIntent.putExtra("photo", photo);
                title = name + context.getString(R.string.new_message, conList.size());
                content = ticker;
            } else {
                title = context.getString(R.string.app_name);
                int count = 0;
                for (String key : msgMap.keySet()) {
                    ArrayList<CharSequence> tmpList = msgMap.get(userId);
                    count += tmpList.size();
                }
                content = context.getString(R.string.new_message_mul, msgMap.keySet().size(), count);
                photo = "";
                type = "";
            }
        }
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        int bell = preferences.getInt("PREF_TIP_MSG", 1);
        if (bell != 0) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = getNotificationBuilder(context, title, content, ticker, photo, type);
            PendingIntent contentIntent = PendingIntent.getActivity(context, R.string.app_name, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            manager.notify(R.string.app_name, notification);
        }
    }

    public static void updateAppNum(final Context context, int num) {
        if (num > 99) {
            num = 99;
        }
        ShortcutBadger.with(context).count(num);
    }

    public static void updateAppNum(final Context context) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                List<ChatGroup> groups = ChatGroup.findAllbyChat(PrfUtils.getUserId(context), PrfUtils.getTenantId(context));
                int num = 0;
                for (ChatGroup group : groups) {
                    num += group.unreadNum;
                }
                num += PrfUtils.getMessageCount(context);
                return num;
            }

            @Override
            protected void onPostExecute(Integer num) {
                if (num > 99) {
                    num = 99;
                }
                ShortcutBadger.with(context).count(num);
            }
        }.execute();
    }


    private static NotificationCompat.Builder getNotificationBuilder(final Context context, String contentTitle, CharSequence contentText, CharSequence ticker, String photo, String type) {

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.notice_ico)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(ticker)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLights(0xff00ff00, 300, 1000);
        if (TextUtils.isEmpty(photo)) {
            if ("group".equals(type))
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_default_group));
            else
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        } else {
            new ImageLoadFresco.LoadImageFrescoBuilder(context, new SimpleDraweeView(context), photo)
                    .setBitmapDataSubscriber(new BaseBitmapDataSubscriber() {
                        @Override
                        protected void onNewResultImpl(Bitmap bitmap) {
                            if (bitmap == null) {
                                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                            } else {
                                mBuilder.setLargeIcon(bitmap);
                            }
                        }

                        @Override
                        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                        }
                    })
                    .build();
        }

        return mBuilder;
    }
}
