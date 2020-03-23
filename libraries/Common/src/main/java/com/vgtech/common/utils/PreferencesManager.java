package com.vgtech.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.network.ApiUtils;

/**
 * Created by code on 2016/3/7.
 */
public class PreferencesManager {
	private static SharedPreferences mSharePreferences;

	public static SharedPreferences getSharePreferences(Context context) {
		mSharePreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		return mSharePreferences;
	}

	/**
	 * 是否DEBUG模式
	 */
	public static void setDebug(Context context, boolean type) {
		final SharedPreferences preferences = getSharePreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("debug", type);
		editor.commit();
	}
	public static boolean getDebug(Context context) {
		SharedPreferences preferences = PreferencesManager.getSharePreferences(context);
		return preferences.getBoolean("debug", true);
	}

	/**
	 * 登录用户id
	 * @param context
	 * @param userId
	 */
	public static void setUserId(Context context, String userId) {
		final SharedPreferences preferences = getSharePreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("user_id", userId).commit();
	}
	public static String getUserId(Context context) {
		SharedPreferences preferences = PreferencesManager.getSharePreferences(context);
		return preferences.getString("user_id", "");
	}

	/**
	 * 租户id
	 * @param context
	 * @param tentendId
	 */
	public static void setTentendId(Context context, String tentendId) {
		final SharedPreferences preferences = getSharePreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("tentend_id", tentendId).commit();
	}

	public static String getTentendId(Context context) {
		SharedPreferences preferences = PreferencesManager.getSharePreferences(context);
		return preferences.getString("tentend_id", "");
	}

	/**
	 * 基地址
	 * @param context
	 * @param baseUrl
	 */
	public static void setBaseUrl(Context context, String baseUrl) {
		final SharedPreferences preferences = getSharePreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("base_url", baseUrl).commit();
	}

	public static String getBaseUrl(Context context) {
		SharedPreferences preferences = PreferencesManager.getSharePreferences(context);
		return preferences.getString("base_url", "");
	}


	public static void putString(Context context,String key,String value){
		if(mSharePreferences ==null){
			getSharePreferences(context);
		}
		mSharePreferences.edit().putString(key,value).commit();
	}

	public static String getString(Context context,String key,String defValue){
		if(mSharePreferences ==null){
			getSharePreferences(context);
		}
		return mSharePreferences.getString(key,defValue);
	}

	/**
	 * 登录用户名
	 * @param context
	 * @param userName
	 */
	public static void setUserName(Context context, String userName) {
		final SharedPreferences preferences = getSharePreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("user_name", userName).commit();
	}

	public static String getUserName(Context context) {
		SharedPreferences preferences = PreferencesManager.getSharePreferences(context);
		return preferences.getString("user_name", "_");
	}

	/**
	 * 是否有财务管理员权限
	 * @param context
	 * @param isPermissions
	 */
	public static void setPermissions(Context context, boolean isPermissions) {
		final SharedPreferences preferences = getSharePreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("is_permissions", isPermissions).commit();
	}

	public static boolean getPermissions(Context context) {
		SharedPreferences preferences = PreferencesManager.getSharePreferences(context);
		return preferences.getBoolean("is_permissions", true);
	}


	/**
	 * 用来初始化ZOOM所需要用到的参数
	 * @param context
	 */
	public  static void initUserMsg(final Context context){
				SharedPreferences preferences = getSharePreferences(context);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("base_url", ApiUtils.getHost(context)).
						putBoolean("debug", Constants.DEBUG).
						putString("tentend_id", PrfUtils.getTenantId(context)).
						putString("user_id",  PrfUtils.getUserId(context)).commit();
	}

}
