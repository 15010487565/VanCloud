package com.vgtech.vancloud.ui.chat.controllers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.register.ui.CompanyInfoEditActivity;
import com.vgtech.vancloud.ui.view.groupimageview.NineGridImageView;
import com.vgtech.vancloud.ui.view.groupimageview.NineGridImageViewAdapter;

import java.io.File;
import java.util.List;

import roboguice.inject.ContextSingleton;

/**
 * @author xuanqiang
 */
@ContextSingleton
public class AvatarController {
    public static final int IMAGE_REQUEST_CODE = 0;
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int RESULT_REQUEST_CODE = 2;

//    public static Uri imageCaptureUri;
    /**
     * 相机权限
     */
    protected static final int CAMERA_REQUESTCODE = 20001;
    protected static final String[] CAMERAPERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.READ_EXTERNAL_STORAGE
            ,Manifest.permission.CAMERA
    };
    public static void choose(final Fragment fragment) {
        new AlertDialog.Builder(fragment.getActivity()).setTitle(fragment.getString(R.string.select_avatar)).setItems(new String[]{fragment.getString(R.string.select_photo),
                fragment.getString(R.string.select_camera)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                        } else {
                            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        }
                        fragment.startActivityForResult(intent, IMAGE_REQUEST_CODE);
                        break;
                    case 1:
//                        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        imageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), String.valueOf(System.currentTimeMillis()) + ".jpg"));
//                        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
//                        fragment.startActivityForResult(intentCamera, CAMERA_REQUEST_CODE);
                        PermissionsChecker mChecker = new PermissionsChecker(fragment.getActivity());
                        if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                            // 请求权限
                            PermissionsActivity.startActivityForResult(fragment.getActivity(), CAMERA_REQUESTCODE, CAMERAPERMISSION);
                        } else {
                            // 全部权限都已获取
                            EasyPhotos.createCamera(fragment)
                                    .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                    .start(CAMERA_REQUEST_CODE);
                        }
                        break;
                }
            }
        }).setNegativeButton(fragment.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public static void crop(final Fragment fragment, final Uri uri, final Bitmap data) {
        if (data == null && uri == null)
            return;
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (uri == null) {
            intent.setType("image/*");
            intent.putExtra("data", data);
            intent.putExtra("crop", "true");
        } else {
            String imagePath = MediaController.queryImagePath(fragment.getActivity(), uri);
            Uri newUri = Uri.fromFile(new File(imagePath));
            intent.setDataAndType(newUri, "image/*");
        }
        intent.putExtra("aspectX", 85);
        intent.putExtra("aspectY", 100);
        intent.putExtra("outputX", (int) (320 * 0.85));
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("noFaceDetection", true);
        fragment.startActivityForResult(intent, RESULT_REQUEST_CODE);
    }
    public static void setAvatarView(final String avatar, final SimpleDraweeView avatarView) {
        RoundingParams roundingParams = new RoundingParams();
        roundingParams.setRoundAsCircle(true);
        GenericDraweeHierarchy hierarchy = avatarView.getHierarchy();
        hierarchy.setRoundingParams(roundingParams);
        hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
        hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
        avatarView.setImageURI(avatar);
    }
    public void setAvatarContainer(final SimpleDraweeView avatarView, final NineGridImageView avatarContainer,
                                   final List<String> avatars) {
        int count = avatars.size();
        if (count == 1) {
            avatarContainer.setVisibility(View.GONE);
            avatarView.setVisibility(View.VISIBLE);
            setAvatarView(avatars.get(0), avatarView);
        } else {
            avatarView.setVisibility(View.GONE);
            avatarContainer.setVisibility(View.VISIBLE);
            avatarContainer.setAdapter(mAdapter);
            avatarContainer.setImagesData(avatars);
        }
    }
    NineGridImageViewAdapter<String> mAdapter = new NineGridImageViewAdapter<String>() {
        @Override
        protected void onDisplayImage(Context context, SimpleDraweeView avatarView, String s) {
            GenericDraweeHierarchy hierarchy = avatarView.getHierarchy();
            hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
            hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
            avatarView.setImageURI(s);
        }
    };
    public int convertDipOrPx(int dip) {
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    @Inject
    Context context;
    @Inject
    PreferencesController prefController;
    @Inject
    @Named("density")
    float density;

}
