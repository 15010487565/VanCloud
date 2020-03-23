package com.vgtech.vancloud.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.vgtech.vancloud.R;

/**
 * 更多弹出框流程
 */
public class MoreButtonPopupWindowFlow  {

    View mMenuView;
    RelativeLayout cancelClickLayout;//取消
    RelativeLayout agreeClickLayout;
    RelativeLayout disagreeClickLayout;

    private Dialog dialog;
    public MoreButtonPopupWindowFlow(Context context, View.OnClickListener itemsOnClick, int type) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.more_button_flow_layout, null);

        cancelClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.cancel_click);
        agreeClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.agree_click);
        disagreeClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.disagree_click);

        cancelClickLayout.setOnClickListener(itemsOnClick);
        agreeClickLayout.setOnClickListener(itemsOnClick);
        disagreeClickLayout.setOnClickListener(itemsOnClick);

        switch (type) {

            case 1:
                cancelClickLayout.setVisibility(View.VISIBLE);
                agreeClickLayout.setVisibility(View.GONE);
                disagreeClickLayout.setVisibility(View.GONE);
                break;
            case 2:
                cancelClickLayout.setVisibility(View.GONE);
                agreeClickLayout.setVisibility(View.VISIBLE);
                disagreeClickLayout.setVisibility(View.VISIBLE);
                break;
        }
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        dialog = new Dialog(context, com.vgtech.common.R.style.ActionSheetDialogStyle);
        mMenuView.setMinimumWidth(display.getWidth());
        dialog.setContentView(mMenuView);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
    }
    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

}
