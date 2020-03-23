package com.vgtech.common.ui.zxing.zxing;

import android.content.Context;
import android.widget.Toast;


/**
 * @ClassName: ToastUtil
 * @Description: Toast工具类
 * @author zhifei.tang tangzhifei@126.com
 * @date 2013-11-22 下午10:22:42
 */
public class ToastUtil {
	public static void toast(Context context, String text) {
		if (context != null)
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void toast(Context context, int text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

}
