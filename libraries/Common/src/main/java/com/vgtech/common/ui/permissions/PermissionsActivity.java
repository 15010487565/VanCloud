package com.vgtech.common.ui.permissions;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.vgtech.common.R;
import com.vgtech.common.ui.zxing.zxing.ToastUtil;


/**
 * Data:  2019/3/6
 * Auther:  xcd
 * Description:
 */

public class PermissionsActivity extends FragmentActivity {
    /**
     * 权限授权
     */
    public static final int PERMISSIONS_GRANTED = 0;
    /**
     * 权限拒绝
     */
    public static final int PERMISSIONS_DENIED = 1;
    /**
     * 系统权限管理页面的参数
     */
    private static final int PERMISSION_REQUEST_CODE = 1;
    /**
     * 权限参数
     */
    private static final String EXTRA_PERMISSIONS = "com.vgtech.vantop.permissions.extra_permission";
    /**
     * 权限参数
     */
    private static final String EXTRA_REQUESTCODE = "requestCode";
    /**
     * 方案
     */
    private static final String PACKAGE_URL_SCHEME = "package:";
    /**
     * 权限检测器
     */
    private PermissionsChecker mChecker;
    /**
     * 是否需要系统权限检测
     */
    private boolean isRequireCheck;

    /**
     * 启动当前权限页面的公开接口
     *
     * @param activity
     * @param requestCode
     * @param permissions
     */
    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        intent.putExtra(EXTRA_REQUESTCODE, requestCode);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity需要使用静态startActivityForResult方法启动!");
        }
        setContentView(R.layout.activity_permissions);

        mChecker = new PermissionsChecker(this);
        isRequireCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            String[] permissions = getPermissions();
            if (mChecker.lacksPermissions(permissions)) {
                // 请求权限
                requestPermissions(permissions);
            } else {
                // 全部权限都已获取
                allPermissionsGranted();
            }
        } else {
            isRequireCheck = true;
        }
    }

    /**
     * 返回传递的权限参数
     *
     * @return
     */
    private String[] getPermissions() {
        Intent intent = getIntent();
        return intent.getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    /**
     * 请求权限兼容低版本
     *
     * @param permissions
     */
    private void requestPermissions(String... permissions) {
        Intent intent = getIntent();
        int extra_requestcodes = intent.getIntExtra(EXTRA_REQUESTCODE, PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, permissions, extra_requestcodes);
    }

    /**
     * 全部权限均已获取
     */
    private void allPermissionsGranted() {
        setResult(PERMISSIONS_GRANTED);
        finish();
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 20001://相机
                if (hasAllPermissionsGranted(permissions, grantResults, PERMISSION_REQUEST_CODE)) {
                    allPermissionsGranted();
                    isRequireCheck = true;

                } else {
                    finish();
                    isRequireCheck = false;
                    //showMissingPermissionDialog();
                    //跳转设置
                    // startAppSettings();
                }
                break;
            case 20002://手机唯一标识
                if (hasAllPermissionsGranted(permissions, grantResults, 20002)) {
                    isRequireCheck = true;
//                    allPermissionsGranted();
                } else {
                    finish();
                    isRequireCheck = false;
//                    Toast.makeText(this, getString(R.string.vantop_isemulator), Toast.LENGTH_SHORT).show();
                }
                break;
            case 20003:
                if (hasAllPermissionsGranted(permissions, grantResults, 20002)) {
                    isRequireCheck = true;
                } else {
                    finish();
                    isRequireCheck = false;
                    Toast.makeText(this, getString(R.string.permissions_equipment), Toast.LENGTH_SHORT).show();

                }
                break;

        }

    }

    // 含有全部的权限
    protected boolean hasAllPermissionsGranted(String[] permissions, @NonNull int[] grantResults, int requestCode) {
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int grantResult = grantResults[i];
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                try {
                    boolean isShow = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                    if (!isShow) {//勾选拒绝再次提示
                        switch (requestCode) {
                            case PERMISSION_REQUEST_CODE:
                                ToastUtil.toast(this, getString(R.string.camera_permissions));
                                break;
                            case 20002:
                                ToastUtil.toast(this, getString(R.string.vantop_isemulator));
                                break;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // 显示缺失权限提示
//    private void showMissingPermissionDialog() {
//        CustomDialog.Builder builder = new CustomDialog.Builder(this);
//        builder.setTitle(R.string.notifyTitle);
//        builder.setMessage(R.string.notifyMsg);
//        builder.setPositiveButton(R.string.errcode_cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                setResult(PERMISSIONS_DENIED);
//                finish();
//            }
//        });
//        builder.setNegativeButton(R.string.settings, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startAppSettings();
//            }
//        });
//        builder.create().show();
//    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }
}
