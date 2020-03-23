package com.vgtech.common.provider.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.TreeNodeDepartment;
import com.vgtech.common.api.TreeNodeEmail;
import com.vgtech.common.api.TreeNodeId;
import com.vgtech.common.api.TreeNodeJob;
import com.vgtech.common.api.TreeNodeLabel;
import com.vgtech.common.api.TreeNodePhone;
import com.vgtech.common.api.TreeNodePhoto;
import com.vgtech.common.api.TreeNodePid;
import com.vgtech.common.api.TreeNodeType;
import com.vgtech.common.api.TreeNodeUser;
import com.vgtech.common.provider.DBOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User extends AbsData {
    public static final String TABLE_NAME = "vguser";
    private static final String USERID = "userid";
    private static final String NAME = "name";
    private static final String PHOTO = "photo";
    private static final String JOB = "job";
    private static final String DEPARTID = "departId";
    private static final String PHONE = "phone";
    private static final String ACCESSTIME = "accessTime";
    private static final String EMAIL = "email";

    private static final String TAB_USERID = "TAB_USERID";
    private static final String TAB_TENANTID = "TAB_TENANTID";
    @TreeNodeId
    public String userId;
    @TreeNodeType
    public int type = 0;
    @TreeNodeLabel
    public String name;
    @TreeNodePhoto
    public String photo;
    @TreeNodeJob
    public String job;
    @TreeNodePid
    public String departId;
    @TreeNodeDepartment
    public String department;
    @TreeNodePhone
    public String phone;
    @TreeNodeUser
    public boolean isUser = true;
    @TreeNodeEmail
    public String email;
    public long accessTime;
    public char firstSpell = '#';
    public boolean isShowHead;
    public String tabUserId;
    public String tabTenantId;

    public User() {
        isUser = true;
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
                .append(NAME).append(" STRING, ")
                .append(PHOTO).append(" STRING, ")
                .append(JOB).append(" STRING, ")
                .append(DEPARTID).append(" STRING, ")
                .append(PHONE).append(" STRING, ")
                .append(EMAIL).append(" STRING, ")
                .append(TAB_USERID).append(" STRING, ")
                .append(TAB_TENANTID).append(" STRING, ")
                .append(ACCESSTIME).append(" LONG, ")
                .append(TIMESTAMP).append(" LONG ")
                .append(");");
        return sb.toString();
    }


    public static ArrayList<User> queryUser(Context context) {
        ArrayList<User> accounts = new ArrayList<User>();
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            User account = new User();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    public static ArrayList<Node> queryVantopUser(Context context) {
        ArrayList<Node> accounts = new ArrayList<Node>();
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "'";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        while (cursor.moveToNext()) {
            User account = new User();
            setValues(cursor, account);
            Node node = new Node(account.userId, account.name, true, account.photo);
            accounts.add(node);
        }
        cursor.close();
        return accounts;
    }

    public static Map<String, Long> queryUserId(Context context) {
        Map<String, Long> accounts = new HashMap<>();
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "'";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        while (cursor.moveToNext()) {
            User account = new User();
            setValues(cursor, account);
            accounts.put(account.userId, account.accessTime);
        }
        cursor.close();
        return accounts;
    }

    public static ArrayList<User> queryUserWithKeyWord(Context context, String keyWord) {
        ArrayList<User> accounts = new ArrayList<User>();
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, "name like ? or job like ?", new String[]{"%" + keyWord + "%", "%" + keyWord + "%"}, sortOrder);
        while (cursor.moveToNext()) {
            User account = new User();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }
    //去除自己
    public static ArrayList<User> queryAccessUserExceptLoginer(Context context) {
        ArrayList<User> accounts = new ArrayList<User>();
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = ACCESSTIME + " desc";
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = USERID +" <> '" +userId+"' AND "  +TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "' AND " + ACCESSTIME + " > " + 0;
//        Log.e("TAG_query","uri="+uri);
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        while (cursor.moveToNext()) {
            User account = new User();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    public static ArrayList<User> queryUserByDepart(Context context, String departId) {
        ArrayList<User> accounts = new ArrayList<User>();
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = ACCESSTIME + " desc";
        String where = DEPARTID + " = '" + departId + "'";
        Cursor cursor = resolver.query(uri, null, where, null, sortOrder);
        while (cursor.moveToNext()) {
            User account = new User();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    public static Map<String, User> queryAccessUserForMap(Context context) {
        Map<String, User> userMap = new HashMap<>();
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = ACCESSTIME + " desc";
        String selection = ACCESSTIME + " > " + 0;

        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        while (cursor.moveToNext()) {
            User account = new User();
            setValues(cursor, account);
            userMap.put(account.userId, account);
        }
        cursor.close();
        return userMap;
    }

    public String getName() {
        if (name == null)
            name = "";
        return name;
    }

    public synchronized static User queryUser(Context context, String uid) {
        Uri uri = getContentUri(User.class, context);
        ContentResolver resolver = context.getContentResolver();
        String userId = PrfUtils.getUserId(context);
        String tenantId = PrfUtils.getTenantId(context);
        String selection = TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "' AND " + USERID + " = '" + uid + "'";
        Cursor cursor = resolver.query(uri, null, selection, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            setValues(cursor, user);
            cursor.close();
        }
        return user;
    }

    public ContentValues putValues() {
        ContentValues values = new ContentValues();
        values.put(USERID, userId);
        values.put(NAME, name);
        values.put(PHOTO, photo);
        values.put(JOB, job);
        values.put(DEPARTID, departId);
        values.put(PHONE, phone);
        values.put(EMAIL, email);
        values.put(TAB_USERID, PrfUtils.getUserId(mContext));
        values.put(TAB_TENANTID, PrfUtils.getTenantId(mContext));
        values.put(ACCESSTIME, accessTime);
        values.put(TIMESTAMP, System.currentTimeMillis());
        return values;
    }

    public static User setValues(Cursor cursor, User account) {
        account._id = cursor.getInt(cursor.getColumnIndex(_ID));
        account.userId = cursor.getString(cursor.getColumnIndex(USERID));
        account.name = cursor.getString(cursor.getColumnIndex(NAME));
        account.photo = cursor.getString(cursor.getColumnIndex(PHOTO));
        account.job = cursor.getString(cursor.getColumnIndex(JOB));
        account.departId = cursor.getString(cursor.getColumnIndex(DEPARTID));
        account.phone = cursor.getString(cursor.getColumnIndex(PHONE));
        account.email = cursor.getString(cursor.getColumnIndex(EMAIL));
        account.tabUserId = cursor.getString(cursor.getColumnIndex(TAB_USERID));
        account.tabTenantId = cursor.getString(cursor.getColumnIndex(TAB_TENANTID));
        account.accessTime = cursor.getLong(cursor.getColumnIndex(ACCESSTIME));
        account.timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
        return account;
    }

    public synchronized static void updateUserAccessTimeAndJob(Context context, List<String> ids, List<Long> createTimes, List<String> jobs) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = null;
        try {
            db.beginTransaction();
            String userId = PrfUtils.getUserId(context);
            String tenantId = PrfUtils.getTenantId(context);
            for (int i = 0; i < ids.size(); i++) {
//                update user set accessTime = 1234567890 , job ='asdfghjk' where userid=728384980434030592


                String job = jobs.get(i);
                if (TextUtils.isEmpty(job)) {
                    sql = "UPDATE " + TABLE_NAME + " SET " + ACCESSTIME + " = " + createTimes.get(i) + " WHERE " + USERID + " = " + ids.get(i) + " AND " + TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "'";
                } else {
                    sql = "UPDATE " + TABLE_NAME + " SET " + ACCESSTIME + " = " + createTimes.get(i) + " ,"
                            + JOB + "='" + job + "' WHERE " + USERID + " = " + ids.get(i) + " AND " + TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "'";
//                    update user
//                    set
//                            accessTime = 123456789,
//                    job=case when job is null then '测试aaa' else job end
//                        where userid=728384900796780544
                }
                db.execSQL(sql);
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
        context.sendBroadcast(new Intent("com.vgtech.vancloud.ACTION_RECENTCONTACTS_REFRESH"));
    }

    public static void updateUserAccessTime(Context context, List<String> ids, List<Long> createTimes) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = null;
        try {
            String userId = PrfUtils.getUserId(context);
            String tenantId = PrfUtils.getTenantId(context);
            db.beginTransaction();
            for (int i = 0; i < ids.size(); i++) {
                sql = "UPDATE " + TABLE_NAME + " SET " + ACCESSTIME + " = " + createTimes.get(i) + " WHERE " + USERID + " = " + ids.get(i) + " AND " + TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "'";
                ;
                db.execSQL(sql);
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
        context.sendBroadcast(new Intent("com.vgtech.vancloud.ACTION_RECENTCONTACTS_REFRESH"));
    }

    public static boolean updateUserTable(List<com.vgtech.common.api.User> users, Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        boolean result = false;
        Map<String, User> accessUsers = queryAccessUserForMap(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = null;
        String[] args = null;
        try {
            String userId = PrfUtils.getUserId(context);
            String tenantId = PrfUtils.getTenantId(context);
            db.beginTransaction();
            sql = "delete from " + TABLE_NAME;
            db.execSQL(sql);
            for (int i = 0; i < users.size(); i++) {
                com.vgtech.common.api.User user = users.get(i);
                sql = "insert into " + TABLE_NAME + "(userid,name,photo,job,departId,phone,email,accessTime,TAB_USERID,TAB_TENANTID) values(?,?,?,?,?,?,?,?,?,?)";
                args = new String[8];
                args[0] = user.userid;
                args[1] = user.name;
                args[2] = user.photo;
                args[3] = user.job;
                args[4] = user.departid;
                args[5] = user.phone;
                args[6] = user.getEmail();
                User lastUser = accessUsers.get(user.userid);
                args[7] = lastUser == null ? String.valueOf(0) : String.valueOf(lastUser.accessTime);
                args[8] = userId;
                args[9] = tenantId;
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

    public static boolean updateVantopUserTable(List<com.vgtech.common.api.User> users, Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        boolean result = false;
        Map<String, Long> accessUsers = queryUserId(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] args = null;
        String sql;
        try {
            String userId = PrfUtils.getUserId(context);
            String tenantId = PrfUtils.getTenantId(context);
            db.beginTransaction();
            Set<String> userIds = accessUsers.keySet();
            for (int i = 0; i < users.size(); i++) {
                com.vgtech.common.api.User user = users.get(i);
                if (!userIds.contains(user.userid)) {
                    sql = "insert into " + TABLE_NAME + "(userid,name,photo,job,departId,phone,email,accessTime,TAB_USERID,TAB_TENANTID) values(?,?,?,?,?,?,?,?,?,?)";
                    args = new String[10];
                    args[0] = user.userid;
                    args[1] = user.name;
                    args[2] = user.photo;
                    args[3] = user.job;
                    args[4] = user.departid;
                    args[5] = user.phone;
                    args[6] = user.getEmail();
                    args[7] = String.valueOf(0);
                    args[8] = userId;
                    args[9] = tenantId;
                    db.execSQL(sql, args);
                } else {
                    long accessTime = accessUsers.get(user.userid);
                    sql = "update " + TABLE_NAME + " set " + ACCESSTIME + " = '" + accessTime + "' where " + USERID + " = '" + user.userid + "' AND " + TAB_USERID + " = '" + userId + "'" + " AND " + TAB_TENANTID + " = '" + tenantId + "'";
                    db.execSQL(sql);
                }
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
}
