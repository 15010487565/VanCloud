package com.vgtech.common.provider.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.provider.DBOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class WorkRelation extends AbsData {
    public static final String TABLE_NAME = "workRelation";
    private static final String USERID = "userId";
    private static final String WGROUPID = "workGroupId";
    public String userId;
    public String wgtoupId;

    public WorkRelation() {
    }


    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static String createTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(TABLE_NAME).append("(").append(_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(USERID).append(" STRING, ")
                .append(WGROUPID).append(" STRING, ")
                .append(TIMESTAMP).append(" LONG ").append(");");
        return sb.toString();
    }

    public static ArrayList<WorkRelation> queryWorkRelation(Context context) {
        ArrayList<WorkRelation> accounts = new ArrayList<WorkRelation>();
        Uri uri = getContentUri(WorkRelation.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            WorkRelation account = new WorkRelation();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    public static WorkRelation query(Context context, String wgtoupId) {
        Uri uri = getContentUri(WorkRelation.class, context);
        ContentResolver resolver = context.getContentResolver();
        String selection = WGROUPID + " = '" + wgtoupId + "'";
        Cursor cursor = resolver.query(uri, null, selection, null, null);
        WorkRelation user = null;
        if (cursor.moveToFirst()) {
            user = new WorkRelation();
            setValues(cursor, user);
            cursor.close();
        }
        return user;
    }
    public static void deleteFromWorkGroupId(Context context, String wgtoupId) {
        Uri uri = getContentUri(WorkRelation.class, context);
        ContentResolver resolver = context.getContentResolver();
        String selection = WGROUPID + " = '" + wgtoupId + "'";
         resolver.delete(uri,selection,null);

    }

    public ContentValues putValues() {
        ContentValues values = new ContentValues();
        values.put(WGROUPID, wgtoupId);
        values.put(USERID, userId);
        values.put(TIMESTAMP, System.currentTimeMillis());
        return values;
    }

    public static WorkRelation setValues(Cursor cursor, WorkRelation account) {
        account._id = cursor.getInt(cursor.getColumnIndex(_ID));
        account.userId = cursor.getString(cursor.getColumnIndex(USERID));
        account.wgtoupId = cursor.getString(cursor.getColumnIndex(WGROUPID));
        account.timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
        return account;
    }
    public static WorkRelation query(Context context, String wgtoupId,String userId) {
        Uri uri = getContentUri(WorkRelation.class, context);
        ContentResolver resolver = context.getContentResolver();
        String selection = WGROUPID + " = '" + wgtoupId + "'"+ " AND " + USERID + " = '" + userId +"'";
        Cursor cursor = resolver.query(uri, null, selection, null, null);
        WorkRelation user = null;
        if (cursor.moveToFirst()) {
            user = new WorkRelation();
            setValues(cursor, user);
            cursor.close();
        }
        return user;
    }
    public static void addWorkGroupRelationTable(List<WorkRelation> workGroups, Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = null;
        String[] args = null;
        try {
            db.beginTransaction();
            for (int i = 0; i < workGroups.size(); i++) {
                WorkRelation wg = workGroups.get(i);
                sql = "insert into " + TABLE_NAME + "(userId,workGroupId) values(?,?)";
                args = new String[2];
                args[0] = "" + wg.userId;
                args[1] = "" + wg.wgtoupId;
                db.execSQL(sql, args);
            }
            db.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交。在setTransactionSuccessful和endTransaction之间不进行任何数据库操作
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();   //事务完成。程序执行到endTransaction() 方法时会检查事务的标志是否为成功，如果程序执行到endTransaction()之前调用了setTransactionSuccessful() 方法设置事务的标志为成功，则所有从beginTransaction（）开始的操作都会被提交，如果没有调用setTransactionSuccessful() 方法则回滚事务。
            if (db != null) {
                db.close();
            }
        }
    }
    public static boolean updateWorkGroupRelationTable(List<WorkRelation> workGroups, Context context) {
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
                WorkRelation wg = workGroups.get(i);
                sql = "insert into " + TABLE_NAME + "(userId,workGroupId) values(?,?)";
                args = new String[2];
                args[0] = "" + wg.userId;
                args[1] = "" + wg.wgtoupId;
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
    public static void updateWorkGroupRelationTable(List<WorkRelation> workGroups, Context context,String wgtoupId) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = null;
        String[] args = null;
        try {
            sql = "delete from " + TABLE_NAME +" where workGroupId = " +wgtoupId;
            db.execSQL(sql);
            db.beginTransaction();
            for (int i = 0; i < workGroups.size(); i++) {
                WorkRelation wg = workGroups.get(i);
                sql = "insert into " + TABLE_NAME + "(userId,workGroupId) values(?,?)";
                args = new String[2];
                args[0] = "" + wg.userId;
                args[1] = "" + wg.wgtoupId;
                db.execSQL(sql, args);
            }
            db.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交。在setTransactionSuccessful和endTransaction之间不进行任何数据库操作
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();   //事务完成。程序执行到endTransaction() 方法时会检查事务的标志是否为成功，如果程序执行到endTransaction()之前调用了setTransactionSuccessful() 方法设置事务的标志为成功，则所有从beginTransaction（）开始的操作都会被提交，如果没有调用setTransactionSuccessful() 方法则回滚事务。
            if (db != null) {
                db.close();
            }
        }
    }

    public static List<User> queryWorkGroup(Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String where = "u.TAB_USERID='"+userId+"' and u.TAB_TENANTID='"+tenantId+"'";
//        String sql = "select u.userId,u.name,u.photo,u.job,u.departId,u.phone,wg.wgtoupId,wg.name  from user u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.groupid = wgl.groupid";
        String[] columns = new String[]{"u.userId,u.name,u.photo,u.job,u.departId,u.phone,wg.wgroupId,wg.name,de.name"};
        Cursor cursor = db.query("vguser u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.wgroupId = wgl.workGroupId inner join department de on de.did = u.departId", columns, where, null, null,
                null, null);
        List<User> nodeList = new ArrayList<User>();
        while (cursor.moveToNext()) {
            User user = new User();
            user.userId = cursor.getString(0);
            user.name = cursor.getString(1);
            user.photo = cursor.getString(2);
            user.job = cursor.getString(3);
            String phone = cursor.getString(5);
            user.departId = cursor.getString(6);
//            user.groupName = cursor.getString(7);
            user.department = cursor.getString(8);
            nodeList.add(user);
        }
        return nodeList;
    }
    public static List<User> queryVantopWorkGroup(Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String where = "u.TAB_USERID='"+userId+"' and u.TAB_TENANTID='"+tenantId+"'";
//        String sql = "select u.userId,u.name,u.photo,u.job,u.departId,u.phone,wg.wgtoupId,wg.name  from user u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.groupid = wgl.groupid";
        String[] columns = new String[]{"u.userId,u.name,u.photo,u.job,u.departId,u.phone,wg.wgroupId,wg.name,u.email"};
        Cursor cursor = db.query("vguser u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.wgroupId = wgl.workGroupId ", columns, where, null, null,
                null, null);
        List<User> nodeList = new ArrayList<User>();
        while (cursor.moveToNext()) {
            User user = new User();
            user.userId = cursor.getString(0);
            user.name = cursor.getString(1);
            user.photo = cursor.getString(2);
            user.job = cursor.getString(3);
//            String phone = cursor.getString(5);
            user.departId = cursor.getString(6);
            user.email = cursor.getString(8);
//            user.groupName = cursor.getString(7);
//            user.department = cursor.getString(8);
            nodeList.add(user);
        }
        return nodeList;
    }
    public static List<User> queryWorkGroupByWgId(Context context,String wgtoupId) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
//        String sql = "select u.userId,u.name,u.photo,u.job,u.departId,u.phone,wg.wgtoupId,wg.name  from user u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.groupid = wgl.groupid";
        String[] columns = new String[]{"u.userId,u.name,u.photo,u.job,u.departId,u.phone,wg.wgroupId,wg.name,de.name"};
        String where = "u.TAB_USERID='"+userId+"' and u.TAB_TENANTID='"+tenantId+"'";
        String selection = "wgl.workGroupId=="+wgtoupId+" and "+where;
        Cursor cursor = db.query("vguser u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.wgroupId = wgl.workGroupId inner join department de on de.did = u.departId", columns, selection, null, null,
                null, null);
        List<User> nodeList = new ArrayList<User>();
        while (cursor.moveToNext()) {
            User user = new User();
            user.userId = cursor.getString(0);
            user.name = cursor.getString(1);
            user.photo = cursor.getString(2);
            user.job = cursor.getString(3);
            String phone = cursor.getString(5);
            user.departId = cursor.getString(6);
//            user.groupName = cursor.getString(7);
            user.department = cursor.getString(8);
            nodeList.add(user);
        }
        return nodeList;
    }
    public static List<User> queryVantopWorkGroupByWgId(Context context,String wgtoupId) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
//        String sql = "select u.userId,u.name,u.photo,u.job,u.departId,u.phone,wg.wgtoupId,wg.name  from user u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.groupid = wgl.groupid";
        String[] columns = new String[]{"u.userId,u.name,u.photo,u.job"};
        String where = "u.TAB_USERID='"+userId+"' and u.TAB_TENANTID='"+tenantId+"'";
        String selection = "wgl.workGroupId=="+wgtoupId+" and "+where;
        Cursor cursor = db.query("vguser u inner join workRelation wgl on u.userid = wgl.userid inner join workGroup wg on wg.wgroupId = wgl.workGroupId", columns, selection, null, null,
                null, null);
        List<User> nodeList = new ArrayList<User>();
        while (cursor.moveToNext()) {
            User user = new User();
            user.userId = cursor.getString(0);
            user.name = cursor.getString(1);
            user.photo = cursor.getString(2);
            user.job = cursor.getString(3);
            nodeList.add(user);
        }
        return nodeList;
    }
}
