package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.photodraweeview.Attacher;
import com.vgtech.common.view.photodraweeview.PhotoDraweeView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.net.NetSilentAsyncTask;

import java.io.File;
import java.util.List;

/**
 * Created by Duke on 2015/8/24.
 */
public class ImageCheckAdapter extends PagerAdapter implements View.OnClickListener {

    Context context;
    List<ImageInfo> list;
    private boolean isUser;

    public ImageCheckAdapter(Context context, List<ImageInfo> list, boolean isUser) {

        this.context = context;
        this.list = list;
        this.isUser = isUser;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.imagecheck_pager_layout, null);
//        final PhotoView photoView = new PhotoView(container.getContext());

        final PhotoDraweeView photoView = (PhotoDraweeView) view.findViewById(R.id.photo_view);
        final PhotoDraweeView userphoto_view = (PhotoDraweeView) view.findViewById(R.id.userphoto_view);
        LinearLayout delInfoLayout = (LinearLayout) view.findViewById(R.id.del_info);
        final ImageInfo imageInfo = list.get(position);
        boolean ifShowDelInfo = false;
        if (isUser) {
            delInfoLayout.setVisibility(View.GONE);
            if (TextUtils.isEmpty(imageInfo.url)) {
                userphoto_view.setImageResource(R.mipmap.default_user_photo_big);
            } else {
                userphoto_view.setPhotoUri(imageInfo.thumb, Uri.parse(imageInfo.url));
                userphoto_view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showDialog(imageInfo.url);
                        return false;
                    }
                });
            }

            userphoto_view.setOnViewTapListener(new Attacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v1) {
                    ((Activity) context).onBackPressed();
                }
            });
            userphoto_view.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
            container.addView(view);
        } else {
            if (imageInfo.isLocal() && !TextUtils.isEmpty(imageInfo.url)) {
                String path = imageInfo.url.substring(7, imageInfo.url.length());
                File file = new File(path);
                if (!file.exists())
                    ifShowDelInfo = true;
                if (ifShowDelInfo) {
                    photoView.setVisibility(View.GONE);
                    delInfoLayout.setVisibility(View.VISIBLE);
                } else {
                    photoView.setVisibility(View.VISIBLE);
                    delInfoLayout.setVisibility(View.GONE);
                    Uri uri = Uri.fromFile(file);
                    photoView.setPhotoUri(uri);
                }
                photoView.setOnViewTapListener(new Attacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float v, float v1) {
                        ((Activity) context).onBackPressed();
                    }
                });
            } else {
                photoView.setVisibility(View.VISIBLE);
                delInfoLayout.setVisibility(View.GONE);
                photoView.setPhotoUri(imageInfo.thumb, Uri.parse(imageInfo.url));
                photoView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showDialog(imageInfo.url);
                        return false;
                    }
                });
                photoView.setOnViewTapListener(new Attacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float v, float v1) {
                        ((Activity) context).onBackPressed();
                    }
                });
            }
            container.addView(view);
        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    /**
     * 下载图片到本地
     *
     * @param imgUrl 要保存的图片URL
     */

    public void downLoadImg(final String imgUrl) {

        new NetSilentAsyncTask<String>(context) {
            @Override
            protected void onSuccess(String filePath) throws Exception {
                Log.e("ceshi", filePath);
                //发送广播刷新相册。
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(new File(filePath));
                intent.setData(uri);
                context.sendBroadcast(intent);
                Toast.makeText(context, String.format(context.getString(R.string.image_check_toast), FileCacheUtils.getSaveImageDir(context) + ""), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground() throws Exception {
                return net().download(imgUrl, "png", (Activity) context);
            }

            @Override
            protected void onThrowable(Throwable t) throws RuntimeException {
                Toast.makeText(context, context.getString(R.string.image_save_fail), Toast.LENGTH_SHORT).show();

                super.onThrowable(t);
            }
        }.execute();

    }

    /**
     * 显示保存图片提示Dialog
     *
     * @param imgUrl 要保存的图片URL
     */

    View view;
    RelativeLayout saveImgView;

    public void showDialog(final String imgUrl) {

        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(context)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true);
        actionSheetDialog.setTitle(context.getString(R.string.prompt));
        actionSheetDialog.addSheetItem(context.getString(R.string.image_check_save), ActionSheetDialog.SheetItemColor.Blue,
                new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        downLoadImg(imgUrl);
                    }
                });
        actionSheetDialog.show();
    }

    @Override
    public void onClick(View v) {

//        switch (v.getId()) {
//            case R.id.save_img:
//                if (dialog != null && dialog.isShowing()) {
//                    dialog.dismiss();
//                }
//                String imgurl = v.getTag().toString();
//                downLoadImg(imgurl);
//                Log.e("ceshi", imgurl);
//                break;
//            default:
//                break;
//        }
//
    }
}
