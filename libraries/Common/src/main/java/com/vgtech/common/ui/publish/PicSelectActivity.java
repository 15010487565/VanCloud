package com.vgtech.common.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.R;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.ui.permissions.PermissionsUtil;
import com.vgtech.common.utils.WeakDataHolder;

import java.io.Serializable;
import java.util.List;


public class PicSelectActivity extends BaseActivity implements  PermissionsUtil.IPermissionsCallback {
    // ArrayList<Entity> dataList;//用来装载数据源的列表
    List<ImageBucket> dataList;
    GridView gridView;
    ImageBucketAdapter adapter;// 自定义的适配器
    AlbumHelper helper;
    public static final String EXTRA_IMAGE_LIST = "imagelist";
    public static Bitmap bimap;
    /**
     * 打卡全选
     */
    PermissionsUtil permissionsUtil;

    @Override
    protected int getContentView() {
        return R.layout.activity_image_bucket;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tvTitle = (TextView) findViewById(android.R.id.title);
        tvTitle.setText(getString(R.string.vancloud_photo_album));
        findViewById(R.id.btn_back).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        finish();

                    }
                });
        //打卡权限申请
        permissionsUtil=  PermissionsUtil
                .with(this)
                .requestCode(CAMERA_REQUESTCODE)
                .isDebug(true)//开启log
                .permissions(PermissionsUtil.Permission.Storage.READ_EXTERNAL_STORAGE,
                        PermissionsUtil.Permission.Location.ACCESS_FINE_LOCATION)
                .request();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // /**
        // * 这里，我们假设已经从网络或者本地解析好了数据，所以直接在这里模拟了10个实体类，直接装进列表中
        // */
        // dataList = new ArrayList<Entity>();
        // for(int i=-0;i<10;i++){
        // Entity entity = new Entity(R.drawable.picture, false);
        // dataList.add(entity);
        // }
        dataList = helper.getImagesBucketList(false);
        bimap = BitmapFactory.decodeResource(getResources(),
                R.drawable.img_default);
    }

    private static final int REQUEST_CODE_PIC = 2001;
    private boolean single;

    @Override
    public void finish() {
        super.finish();
        AlbumHelper.hasBuildImagesBucketList = false;
        AlbumHelper.getHelper().bucketList.clear();
        AlbumHelper.getHelper().albumList.clear();
        AlbumHelper.getHelper().thumbnailList.clear();
    }

    /**
     * 初始化view视图
     */
    private void initView() {
        gridView = (GridView) findViewById(R.id.gridview);
        adapter = new ImageBucketAdapter(PicSelectActivity.this, dataList);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                /**
                 * 根据position参数，可以获得跟GridView的子View相绑定的实体类，然后根据它的isSelected状态，
                 * 来判断是否显示选中效果。 至于选中效果的规则，下面适配器的代码中会有说明
                 * 通知适配器，绑定的数据发生了改变，应当刷新视图
                 */
                // adapter.notifyDataSetChanged();
                Intent intent = new Intent(PicSelectActivity.this,
                        ImageGridActivity.class);
                single = getIntent().getBooleanExtra("single", false);
                intent.putExtra("single", single);
//                intent.putExtra(PicSelectActivity.EXTRA_IMAGE_LIST,
//                        (Serializable) dataList.get(position).imageList);
                WeakDataHolder.getInstance().saveData(WeakDataHolder.IMAGEID, dataList.get(position).imageList);
                // startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_PIC);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PIC:
                if (resultCode == Activity.RESULT_OK) {
                    if (single) {
                        String path = data.getStringExtra("path");
                        Intent intent = new Intent();
                        intent.putExtra("path", path);
                        setResult(Activity.RESULT_OK, intent);
                    } else {
                        setResult(Activity.RESULT_OK);
                    }
                    finish();
                }
                break;
            case CAMERA_REQUESTCODE:
                //监听跳转到权限设置界面后再回到应用
                permissionsUtil.onActivityResult(requestCode, resultCode, data);

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, String... permission) {
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        initData();
        initView();
    }

    @Override
    public void onPermissionsDenied(int requestCode, String... permission) {
        //权限被拒绝回调
      finish();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //需要调用onRequestPermissionsResult
        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
