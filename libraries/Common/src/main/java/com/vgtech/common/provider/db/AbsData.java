package com.vgtech.common.provider.db;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

import com.vgtech.common.provider.VanCloudProvider;

public abstract class AbsData implements BaseColumns {

	protected static String TABLE_NAME = null;
	protected static final String TIMESTAMP = "timestamp";
	protected static final String STATE = "state";
	protected static final String USERID = "userId";
	protected static final String TENANTID = "tenantId";
	protected static Uri sContentUri = null;
	public int _id = -1;
	public long timestamp;
	public int state;
	public String userId;
	public String tenantId;
	public AbsData() {
	}

	public static Uri getContentUri(Class<? extends AbsData> c, Context context) {
		try {
			AbsData absData = c.newInstance();
			TABLE_NAME = absData.getTableName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Uri contentUri = VanCloudProvider.getContentUri(context);
		sContentUri = Uri.withAppendedPath(contentUri, TABLE_NAME);
		return sContentUri;
	}

	public long insert(Context context) {
		mContext = context;
		Uri uri = getContentUri(getClass(), context);
		ContentResolver resolver = context.getContentResolver();
		timestamp = System.currentTimeMillis();
		ContentValues values = putValues();
		long rowId = -1;
		try {
			Uri result = resolver.insert(uri, values);
			rowId = ContentUris.parseId(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rowId;
	}
	protected  Context mContext;
	public void update(Context context) {
		mContext = context;
		if (_id == -1) {
			return;
		}
		Uri uri = ContentUris.withAppendedId(
				getContentUri(getClass(), context), _id);
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = putValues();
		resolver.update(uri, values, null, null);
	}

	public void delete(Context context) {
		mContext = context;
		Uri uri = ContentUris.withAppendedId(
				getContentUri(getClass(), context), _id);
		ContentResolver resolver = context.getContentResolver();
		resolver.delete(uri, null, null);
	}
	public void deleteAll(Context context) {
		mContext = context;
		Uri uri = getContentUri(getClass(), context);
		ContentResolver resolver = context.getContentResolver();
		resolver.delete(uri, null, null);
	}

	protected abstract ContentValues putValues();

	public abstract String getTableName();
}
