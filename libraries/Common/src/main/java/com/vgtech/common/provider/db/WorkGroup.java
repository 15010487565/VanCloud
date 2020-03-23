package com.vgtech.common.provider.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vgtech.common.api.TreeNodeType;
import com.vgtech.common.provider.DBOpenHelper;
import com.vgtech.common.api.TreeNodeId;
import com.vgtech.common.api.TreeNodeLabel;

import java.util.ArrayList;
import java.util.List;

public class WorkGroup extends AbsData {
    public static final String TABLE_NAME = "workGroup";
    private static final String WGROUPID = "wgroupId";
    private static final String NAME = "name";
    @TreeNodeId
    public String wgtoupId;
    @TreeNodeType
    public int type = 1;
    @TreeNodeLabel
    public String name;

    public WorkGroup() {
    }


    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static String createTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(TABLE_NAME).append("(").append(_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(WGROUPID).append(" STRING, ")
                .append(NAME).append(" STRING, ")
                .append(TIMESTAMP).append(" LONG ").append(");");
        return sb.toString();
    }

    public static ArrayList<WorkGroup> queryWorkGroup(Context context) {
        ArrayList<WorkGroup> accounts = new ArrayList<WorkGroup>();
        Uri uri = getContentUri(WorkGroup.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            WorkGroup account = new WorkGroup();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }
    public void updateByWgId(Context context) {
        Uri uri = getContentUri(WorkGroup.class, context);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = putValues();
        String where = WGROUPID +" = "+wgtoupId;
        resolver.update(uri, values, where, null);
    }
    public void deleteByWgId(Context context,String wgtoupId) {
        Uri uri = getContentUri(WorkGroup.class, context);
        ContentResolver resolver = context.getContentResolver();
        String where = WGROUPID +" = "+wgtoupId;
        resolver.delete(uri,where, null);
    }

    public static WorkGroup query(Context context, String wgtoupId) {
        Uri uri = getContentUri(WorkGroup.class, context);
        ContentResolver resolver = context.getContentResolver();
        String selection = WGROUPID + " = '" + wgtoupId + "'";
        Cursor cursor = resolver.query(uri, null, selection, null, null);
        WorkGroup user = null;
        if (cursor.moveToFirst()) {
            user = new WorkGroup();
            setValues(cursor, user);
            cursor.close();
        }
        return user;
    }

    public ContentValues putValues() {
        ContentValues values = new ContentValues();
        values.put(WGROUPID, wgtoupId);
        values.put(NAME, name);
        values.put(TIMESTAMP, System.currentTimeMillis());
        return values;
    }

    public static WorkGroup setValues(Cursor cursor, WorkGroup account) {
        account._id = cursor.getInt(cursor.getColumnIndex(_ID));
        account.wgtoupId = cursor.getString(cursor.getColumnIndex(WGROUPID));
        account.name = cursor.getString(cursor.getColumnIndex(NAME));
        account.timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
        return account;
    }

    public static boolean updateWorkGroupTable(List<WorkGroup> workGroups, Context context) {
        boolean result = false;
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = null;
        String[] args = null;
        try {
            db.beginTransaction();
            sql = "delete from " + TABLE_NAME;
            db.execSQL(sql);
            for (int i = 0; i < workGroups.size(); i++) {
                WorkGroup wg = workGroups.get(i);
                sql = "insert into " + TABLE_NAME + "(wgroupId,name) values(?,?)";
                args = new String[2];
                args[0] = "" + wg.wgtoupId;
                args[1] = wg.name;
                db.execSQL(sql, args);
            }
            db.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交。在setTransactionSuccessful和endTransaction之间不进行任何数据库操作
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();   //事务完成。程序执行到endTransaction() 方法时会检查事务的标志是否为成功，如果程序执行到endTransaction()之前调用了setTransactionSuccessful() 方法设置事务的标志为成功，则所有从beginTransaction（）开始的操作都会被提交，如果没有调用setTransactionSuccessful() 方法则回滚事务。
            if (db != null) {
                db.close();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "WorkGroup{" +
                "wgtoupId='" + wgtoupId + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}
