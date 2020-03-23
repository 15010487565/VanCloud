package com.vgtech.common.provider.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.provider.DBOpenHelper;
import com.vgtech.common.utils.PublishConstants;

import java.util.ArrayList;
import java.util.List;

public class PublishTask extends AbsData implements PublishConstants, Parcelable {
    public boolean sending;
    public static PublishTask query(Context context, String id) {
        Uri uri = getContentUri(PublishTask.class, context);
        ContentResolver resolver = context.getContentResolver();
        String selection = _ID + " = '" + id + "'";
        Cursor cursor = resolver.query(uri, null, selection, null, null);
        PublishTask user = null;
        if (cursor.moveToFirst()) {
            user = new PublishTask();
            setValues(cursor, user);
            cursor.close();
        }
        return user;
    }

    public static final String TABLE_NAME = "publishTask";
    private static final String PUBLISHID = "publishId";
    private static final String PUBLISHTYPE = "PUBLISHTYPE";
    private static final String CONTENT = "content";
    private static final String IMAGE = "image";
    private static final String AUDIO = "audio";
    private static final String AUDIOTIME = "audiotime";
    private static final String ATTACHMENT = "ATTACHMENT";

    public String publishId;
    public int type;
    public String content;
    public String image;
    public String audio;
    public String attachment;
    public String time;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int position=-1;

    public PublishTask() {
    }


    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static String createTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(TABLE_NAME).append("(").append(_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(PUBLISHID).append(" STRING, ")
                .append(PUBLISHTYPE).append(" INTEGER, ")
                .append(CONTENT).append(" STRING, ")
                .append(IMAGE).append(" STRING, ")
                .append(AUDIO).append(" STRING, ")
                .append(AUDIOTIME).append(" STRING, ")
                .append(ATTACHMENT).append(" STRING, ")
                .append(USERID).append(" STRING, ")
                .append(TENANTID).append(" STRING, ")
                .append(TIMESTAMP).append(" LONG ").append(");");
        return sb.toString();
    }

    public static int queryPublishCount(Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = new String[]{"count(*)"};
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId + "'";
//        if (Constants.DEBUG){
//            Log.e("TAG_首页Task","USERID="+USERID);
//        }
        Cursor cursor = db.query(TABLE_NAME, columns, selection, null, null,
                null, null);
        List<User> nodeList = new ArrayList<User>();
        int count = 0;
        if (cursor.moveToFirst()) {
            count = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        return count;
    }

    public static ArrayList<PublishTask> queryTask(Context context) {
        ArrayList<PublishTask> accounts = new ArrayList<PublishTask>();
        Uri uri = getContentUri(PublishTask.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID + " = '" + userId + "'" + " AND " + TENANTID + " = '" + tenantId + "'";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        while (cursor.moveToNext()) {
            PublishTask account = new PublishTask();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    public ContentValues putValues() {
        ContentValues values = new ContentValues();
        values.put(PUBLISHID, publishId);
        values.put(PUBLISHTYPE, type);
        values.put(CONTENT, content);
        values.put(IMAGE, image);
        values.put(AUDIO, audio);
        values.put(AUDIOTIME, time);
        values.put(ATTACHMENT, attachment);
        values.put(USERID, PrfUtils.getUserId(mContext));
        values.put(TENANTID, PrfUtils.getTenantId(mContext));
        values.put(TIMESTAMP, System.currentTimeMillis());
        return values;
    }

    public static PublishTask setValues(Cursor cursor, PublishTask account) {
        account._id = cursor.getInt(cursor.getColumnIndex(_ID));
        account.publishId = cursor.getString(cursor.getColumnIndex(PUBLISHID));
        account.type = cursor.getInt(cursor.getColumnIndex(PUBLISHTYPE));
        account.content = cursor.getString(cursor.getColumnIndex(CONTENT));
        account.image = cursor.getString(cursor.getColumnIndex(IMAGE));
        account.audio = cursor.getString(cursor.getColumnIndex(AUDIO));
        account.time = cursor.getString(cursor.getColumnIndex(AUDIOTIME));
        account.attachment = cursor.getString(cursor.getColumnIndex(ATTACHMENT));
        account.timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
        return account;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<PublishTask> CREATOR = new Creator<PublishTask>() {
        public PublishTask createFromParcel(Parcel in) {
            return new PublishTask(in);
        }

        public PublishTask[] newArray(int size) {
            return new PublishTask[size];
        }
    };

    private PublishTask(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(publishId);
        dest.writeInt(type);
        dest.writeString(content);
        dest.writeString(image);
        dest.writeString(audio);
        dest.writeString(time);
        dest.writeString(attachment);
        dest.writeInt(position);
    }

    public void readFromParcel(Parcel in) {
        _id = in.readInt();
        publishId = in.readString();
        type = in.readInt();
        content = in.readString();
        image = in.readString();
        audio = in.readString();
        time = in.readString();
        attachment = in.readString();
        position = in.readInt();
    }
}
