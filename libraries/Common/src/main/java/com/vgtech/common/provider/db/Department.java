package com.vgtech.common.provider.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vgtech.common.api.Group;
import com.vgtech.common.api.TreeNodeType;
import com.vgtech.common.provider.DBOpenHelper;
import com.vgtech.common.api.TreeNodeBranch;
import com.vgtech.common.api.TreeNodeId;
import com.vgtech.common.api.TreeNodeLabel;
import com.vgtech.common.api.TreeNodePid;

import java.util.ArrayList;
import java.util.List;

public class Department extends AbsData {
    public static final String TABLE_NAME = "department";
    private static final String DEPARTID = "did";
    private static final String NAME = "name";
    private static final String PID = "pid";
    private static final String VERSION = "version";
    private static final String LAST_VERSION = "last_version";




    @TreeNodeId
    public String did;
    @TreeNodeType
    public int type = 2;
    @TreeNodeLabel
    public String name;
    @TreeNodePid
    public String pid;
    @TreeNodeBranch
    public String isbranch;
    public String lastVersion;

    public Department() {
    }

    public Department(String did, String name, String pid) {
        this.did = did;
        this.name = name;
        this.pid = pid;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static String createTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(TABLE_NAME).append("(").append(_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(DEPARTID).append(" STRING, ")
                .append(NAME).append(" STRING, ")
                .append(PID).append(" STRING, ")
                .append(VERSION).append(" STRING, ")
                .append(LAST_VERSION).append(" STRING, ")
                .append(TIMESTAMP).append(" LONG ").append(");");
        return sb.toString();
    }

    public static ArrayList<Department> queryDepartment(Context context) {
        ArrayList<Department> accounts = new ArrayList<Department>();
        Uri uri = getContentUri(Department.class, context);
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            Department account = new Department();
            setValues(cursor, account);
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    public static ArrayList<Department> queryChildDepartment(Context context, String departId) {
        Uri uri = getContentUri(Department.class, context);
        ContentResolver resolver = context.getContentResolver();
        String selection = PID + " = '" + departId + "'";
        String sortOrder = TIMESTAMP + " desc";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        ArrayList<Department> accounts = new ArrayList<Department>();
        while (cursor.moveToNext()) {
            Department account = new Department();
            setValues(cursor, account);
            accounts.add(account);
        }
        return accounts;
    }

    public ContentValues putValues() {
        ContentValues values = new ContentValues();
        values.put(DEPARTID, did);
        values.put(NAME, name);
        values.put(PID, pid);
        values.put(VERSION, isbranch);
        values.put(LAST_VERSION, lastVersion);
        values.put(TIMESTAMP, System.currentTimeMillis());
        return values;
    }

    public static Department setValues(Cursor cursor, Department account) {
        account._id = cursor.getInt(cursor.getColumnIndex(_ID));
        account.did = cursor.getString(cursor.getColumnIndex(DEPARTID));
        account.name = cursor.getString(cursor.getColumnIndex(NAME));
        account.pid = cursor.getString(cursor.getColumnIndex(PID));
        account.isbranch = cursor.getString(cursor.getColumnIndex(VERSION));
        account.lastVersion = cursor.getString(cursor.getColumnIndex(LAST_VERSION));
        account.timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
        return account;
    }

    public static boolean updateGroupTable(List<Group> groups, Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = null;
        String[] args = null;
        boolean result = false;
        try {
            db.beginTransaction();
            sql = "delete from " + TABLE_NAME;
            db.execSQL(sql);
            for (int i = 0; i < groups.size(); i++) {
                Group group = groups.get(i);
                sql = "insert into " + TABLE_NAME + "(did,name,pid,version) values(?,?,?,?)";
                args = new String[4];
                args[0] = "" + group.getGid();
                args[1] = group.getName();
                args[2] = "" + group.getPid();
                args[3] = group.getVersion();
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
}
