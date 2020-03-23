package com.vgtech.common.api;

import android.content.Context;

import com.vgtech.common.R;

/**
 * Created by vic on 2016/9/13.
 */
public class SearchItem extends AbsApiData {
    public static enum Type {
        nnt,notice,todo,mynotice,user, chatgroup, chatmessage, pushmessage
    }

    public Type type;
    public String id;
    public String icon;
    public String title;
    public String subtitle;
    public String timestamp;
    public boolean isFirst;
    public boolean hasMore;
    public boolean isLast;
    public Object itemObj;

    public String getTypeTitle(Context context) {
        int resId = 0;
        switch (type) {
            case notice:
                resId = R.string.lable_notice;
                break;
            case mynotice:
                resId = R.string.lable_notification;
                break;
            case todo:
                resId = R.string.todo;
                break;
            case user:
                resId = R.string.search_user;
                break;
            case chatgroup:
                resId = R.string.search_chatgroup;
                break;
            case chatmessage:
                resId = R.string.search_chatmessage;
                break;
            case pushmessage:
                resId = R.string.search_pushmessage;
                break;
        }
        return context.getString(resId);
    }
    public static String getTypeTitle(Context context,Type type) {
        int resId = 0;
        switch (type) {
            case notice:
                resId = R.string.lable_notice;
                break;
            case mynotice:
                resId = R.string.lable_notification;
                break;
            case todo:
                resId = R.string.todo;
                break;
            case user:
                resId = R.string.search_user;
                break;
            case chatgroup:
                resId = R.string.search_chatgroup;
                break;
            case chatmessage:
                resId = R.string.search_chatmessage;
                break;
            case pushmessage:
                resId = R.string.search_pushmessage;
                break;
        }
        return  context.getString(R.string.search)  +context.getString(resId);
    }
}
