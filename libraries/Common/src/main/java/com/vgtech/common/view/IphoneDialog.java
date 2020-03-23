package com.vgtech.common.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.R;

/**
 * Created by Duke on 2015/10/23.
 */
public class IphoneDialog {
    private TextView tv;
    private View view;
    private Dialog dialog;
    private final Context context;

    public IphoneDialog(Context context) {
        this(context,true);
    }
    public IphoneDialog(Context context,boolean cancelable) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.iphone_dialog_layout, null);
        tv = (TextView) view.findViewById(R.id.textinfo);
        dialog = new Dialog(context, R.style.Activity_Pop);
        dialog.setCancelable(cancelable);
        dialog.setContentView(view);
    }

    /***
     * 关闭对话框
     */
    public void dismiss() {
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 显示对话框
     */
    public void show() {
        try {
            if (dialog != null && !dialog.isShowing()) {
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show(boolean ifCanTouchOutside) {
        try {
            if (dialog != null && !dialog.isShowing()) {
                dialog.setCanceledOnTouchOutside(ifCanTouchOutside);
                dialog.setCancelable(ifCanTouchOutside);
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * 设置提示信息
     *
     * @param message
     */
    public void setMessage(String message) {
        try {
            if (tv != null) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(message);
            }
            if (TextUtils.isEmpty(message)) {
                tv.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***
     * 判断对话框是否显示中
     *
     * @return
     */
    public boolean isShowing() {
        Boolean isFlag = false;
        try {
            isFlag = dialog.isShowing();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isFlag;
    }

    /***
     * 对话框关闭监听
     *
     * @param dismissListener
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener dismissListener) {
        try {
            dialog.setOnDismissListener(dismissListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 对话框按键监听
     *
     * @param
     */
    public void setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        try {
            dialog.setOnKeyListener(onKeyListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
