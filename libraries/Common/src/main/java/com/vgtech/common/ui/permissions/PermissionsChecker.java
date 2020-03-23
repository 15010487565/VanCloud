package com.vgtech.common.ui.permissions;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * 6.0 危险权限检查
 * Data:  2019/3/6
 * Auther: xcd
 * Description:
 */

public class PermissionsChecker {
    private Context mContext;

    public PermissionsChecker(Context context) {

        try {
            mContext = context.getApplicationContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 判断权限集合
    public boolean lacksPermissions(String... permissions) {
        if (permissions!=null){
            for (String permission : permissions) {
                if (lacksPermission(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private boolean lacksPermission(String permission) {

        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_DENIED;
//        return false;
    }
}
