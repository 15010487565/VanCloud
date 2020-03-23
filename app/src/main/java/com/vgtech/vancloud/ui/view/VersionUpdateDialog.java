package com.vgtech.vancloud.ui.view;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.vgtech.common.api.Update;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.UpdateManager;

/**
 * Created by vic on 2016/9/20.
 */
public class VersionUpdateDialog {
    private Activity mActivity;
    private Update mUpdate;
    public VersionUpdateDialog(Activity context,Update update)
    {
        mActivity = context;
        mUpdate = update;
    }
    public void showUpdateTip() {
        com.vgtech.common.view.AlertDialog alertDialog = new com.vgtech.common.view.AlertDialog(mActivity).builder().setTitle(mActivity.getString(R.string.checked_new_version))
                .setMsg(mUpdate.des);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setLeft();
        alertDialog.setPositiveButton(mActivity.getString(R.string.update_now), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateManager manager = new UpdateManager(
                        mActivity);
                // 检查软件更新
                manager.checkUpdate(mUpdate.downloadpath);
            }
        });
        if (TextUtils.isEmpty(mUpdate.isforceUpdate) || mUpdate.isforceUpdate.equals("N")) {
            alertDialog.setNegativeButton(mActivity.getString(R.string.update_later), new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        alertDialog.show();
    }
}
