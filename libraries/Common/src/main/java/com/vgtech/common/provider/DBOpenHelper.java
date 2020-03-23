package com.vgtech.common.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;


/**
 * 数据库管理
 *
 * @author zhangshaofang
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    /**
     * 2  新增MessageDB
     * 5 user表增加tabuserid和tabtenantID
     *
     * @param context
     */
    public DBOpenHelper(Context context) {
        super(context, "saasprovider.db", null, 5);
    }

    /**
     * 数据库初始化，建表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(User.createTable());
        db.execSQL(Department.createTable());
        db.execSQL(PublishTask.createTable());
        db.execSQL(WorkGroup.createTable());
        db.execSQL(WorkRelation.createTable());
        db.execSQL(MessageDB.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 3) {//兼容2.1.0之前的版本
            db.execSQL(MessageDB.createTable());
        }
        if (newVersion > oldVersion) {
            db.execSQL(User.createTable());
        }
    }
}
