/*
 * Copyright (C) 2010-2011 Android Cache Library Project,
 * All rights reserved by PCR(Wanzheng Ma)
 * 
 * Version 0.9
 * Date 2011-May-16
 * Support: pcrxjxj@gmail.com
 */
package com.vgtech.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkHelpers {
	private final static String LOG_TAG = "Cache.NetworkHelpers";

	private NetworkHelpers() {
	}

	/**
	 * Returns whether the network is available
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity == null) {
			Log.w(LOG_TAG, "Couldn't get connectivity manager");
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
