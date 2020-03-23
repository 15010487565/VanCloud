package com.vgtech.common.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.R;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.WeakDataHolder;

import java.util.List;


public class ImageGridActivity extends BaseActivity {
    public static final String EXTRA_IMAGE_LIST = "imagelist";

    // ArrayList<Entity> dataList;//鐢ㄦ潵瑁呰浇鏁版嵁婧愮殑鍒楄〃
    List<ImageItem> dataList;
    GridView gridView;
    ImageGridAdapter adapter;// 鑷畾涔夌殑閫傞厤鍣�
    AlbumHelper helper;
    Button bt;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(ImageGridActivity.this, getString(R.string.vancloud_photo_album_prompt), Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    if (single) {
                        List<String> list = adapter.map;
                        String path = list.get(0);
                        Intent intent = new Intent();
                        intent.putExtra("path", path);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }

                    break;
                default:
                    break;
            }
        }
    };
    private boolean single;

    @Override
    protected int getContentView() {
        return R.layout.activity_image_grid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        single = getIntent().getBooleanExtra("single", false);
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

//        dataList = (List<ImageItem>) getIntent().getSerializableExtra(
//                EXTRA_IMAGE_LIST);
        dataList = (List<ImageItem>) WeakDataHolder.getInstance().getData(WeakDataHolder.IMAGEID);
        Log.e("TAG_临时取值","dataList="+dataList.size());
        findViewById(R.id.btn_back).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        finish();

                    }
                });
        TextView tvTitle = (TextView) findViewById(android.R.id.title);
        tvTitle.setText(getString(R.string.vancloud_photo_album));
        initView();

        bt = (Button) findViewById(R.id.bt);
        bt.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                List<String> list = adapter.map;
                Bimp.drr = list;
                setResult(Activity.RESULT_OK);
                finish();
            }

        });
        bt.setText(getString(R.string.btn_finish) + "(" + adapter.map.size() + ")");
        if (single) {
            bt.setVisibility(View.GONE);
        }
    }

    /**
     * 鍒濆鍖杤iew瑙嗗浘
     */
    private void initView() {
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new ImageGridAdapter(ImageGridActivity.this, dataList,
                mHandler);
        gridView.setAdapter(adapter);
        adapter.setTextCallback(new ImageGridAdapter.TextCallback() {
            public void onListen(int count) {
                bt.setText(getString(R.string.btn_finish) + "(" + count + ")");
            }
        });

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                adapter.notifyDataSetChanged();
            }

        });

    }
}
