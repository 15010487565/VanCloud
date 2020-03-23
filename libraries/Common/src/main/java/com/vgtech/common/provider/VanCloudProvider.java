package com.vgtech.common.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.content.ContentProvider;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;

/**
 * Created by zhangshaofang on 2015/7/21.
 */
public class VanCloudProvider extends ContentProvider {
    private static final String[] TABLENAMES = new String[]{User.TABLE_NAME, Department.TABLE_NAME, PublishTask.TABLE_NAME, WorkGroup.TABLE_NAME, WorkRelation.TABLE_NAME, MessageDB.TABLE_NAME};
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//    private static String sAuthority = "null";
    private static String sAuthority = "com.vgtech.vancloud";
    private static Uri sContentUri = null;
    private static final String LOG_TAG = "TAG_VanCloudProvider";
    private static final int PROVIDER = 1;
    private static final int PROVIDER_ID = 2;
    private DBOpenHelper dbOpenHelper;

    @Override
    public boolean onCreate() {
        super.onCreate();
        dbOpenHelper = new DBOpenHelper(this.getContext());
//        init(getContext());
        initNew(getContext());

        return true;
    }

    synchronized private static void initNew(Context c) {
//        sContentUri = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + sAuthority);
        sContentUri = new Uri.Builder().authority(sAuthority).scheme("content").build();
        UriMatcher matcher = sURIMatcher;
        for (String TABLENAME : TABLENAMES) {
            matcher.addURI(sAuthority, TABLENAME, PROVIDER);
            matcher.addURI(sAuthority, TABLENAME + "/#", PROVIDER_ID);
        }
    }
    synchronized private static void init(Context c) {
        if (!TextUtils.isEmpty(sAuthority)) {
            return;
        }
        ProviderInfo info = null;
        ProviderInfo[] providerInfos = null;
        try {
            providerInfos = c.getPackageManager().getPackageInfo(c.getPackageName(), PackageManager.GET_PROVIDERS).providers;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Failed get Package info.", e);
        }

        if (providerInfos != null) {
            String className = VanCloudProvider.class.getName();
            for (ProviderInfo providerInfo : providerInfos) {
                if (className.equals(providerInfo.name)) {
                    info = providerInfo;
                    break;
                }
            }
        }
        if (info == null) {
            throw new IllegalArgumentException("Not found the definition for this Provider in AndroidManifest.xml.");
        }

        sAuthority = info.authority;
        if (TextUtils.isEmpty(sAuthority)) {
            return;
        }
        sContentUri = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + sAuthority);
//        sContentUri = new Uri.Builder().authority(sAuthority).scheme("content").build();

        UriMatcher matcher = sURIMatcher;
        for (String TABLENAME : TABLENAMES) {
            matcher.addURI(sAuthority, TABLENAME, PROVIDER);
            matcher.addURI(sAuthority, TABLENAME + "/#", PROVIDER_ID);
        }
    }
    @Override
    public String getType(Uri uri) {
//        super.getType(uri);
        final int match = sURIMatcher.match(uri);
        String ret;
        switch (match) {
            case PROVIDER:
                ret = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + sAuthority;
                break;
            case PROVIDER_ID:
                ret = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + sAuthority;
                break;
            default:
                throw new IllegalArgumentException("this is an unknown Uri:" + uri);
        }
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
//        super.insert(uri,values);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Uri insertUri;
        String tableName = uri.getLastPathSegment();
        switch (sURIMatcher.match(uri)) {
            case PROVIDER:
                long rowid = db.insert(tableName, null, values);
                insertUri = ContentUris.withAppendedId(uri, rowid);
                getContext().getContentResolver().notifyChange(uri, new MycontentObserver(new Handler()));
                break;
            case PROVIDER_ID:
                throw new IllegalArgumentException("Can't support this Uri:" + uri);
            default:
                throw new IllegalArgumentException("this is an unknown Uri:" + uri);
        }
        return insertUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        super.query(uri,projection,selection,selectionArgs,sortOrder);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor result;
        String tableName;
        tableName = uri.getLastPathSegment();
        switch (sURIMatcher.match(uri)) {
            case PROVIDER:
                result = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROVIDER_ID:
                tableName = subTableName(uri);
                long rowid = ContentUris.parseId(uri);
                String where = BaseColumns._ID + "=" + rowid;
                if (TextUtils.isEmpty(selection)) {
                    selection = where;
                    // where += " and " + selection;
                }

                result = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("this is an unknown Uri:" + uri);
        }
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        super.update(uri, values, selection, selectionArgs);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int num;
        String tableName = subTableName(uri);
        switch (sURIMatcher.match(uri)) {
            case PROVIDER:
                num = db.update(tableName, values, selection, selectionArgs);
                break;
            case PROVIDER_ID:
                long rowid = ContentUris.parseId(uri);
                String where = BaseColumns._ID + "=" + rowid;
                if (!TextUtils.isEmpty(selection)) {
                    where += " and " + selection;
                }
                num = db.update(tableName, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("this is an unknown Uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, new MycontentObserver(new Handler()));
        return num;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        super.delete(uri, selection, selectionArgs);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int num;
        String tableName = subTableName(uri);
        switch (sURIMatcher.match(uri)) {
            case PROVIDER:
                num = db.delete(tableName, selection, selectionArgs);
                break;
            case PROVIDER_ID:
                long rowid = ContentUris.parseId(uri);
                String where = BaseColumns._ID + "=" + rowid;
                if (!TextUtils.isEmpty(selection)) {
                    where += " and " + selection;
                }
                num = db.delete(tableName, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("This is an unknown Uri:" + uri);
        }
        // getContext().getContentResolver().notifyChange(uri, null);
        return num;
    }

    public static Uri getContentUri(Context c) {
//        init(c);
        initNew(c);
        return sContentUri;

    }

    public static String subTableName(Uri uri) {
        String path = uri.getPath();
        int first = path.indexOf("/");
        int end = path.lastIndexOf("/");
        String tableName = path.substring(first + 1, end == first ? path.length() : end);
        return tableName;
    }

    public static String[] getTables() {
        return TABLENAMES;
    }

    private  class  MycontentObserver extends ContentObserver{
        public MycontentObserver(Handler handler){

            super(handler);
        }

        //当内容改变的时候调用

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);


        }
    }
}
