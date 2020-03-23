package com.vgtech.vancloud.ui.web;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.smtt.sdk.TbsReaderView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import java.io.File;


public class WebX5FileActivity extends BaseActivity implements TbsReaderView.ReaderCallback{

    private RelativeLayout mRelativeLayout;
    private TbsReaderView mTbsReaderView;
    private TextView tvTitle;
    @Override
    protected int getContentView() {
        return R.layout.activity_web_x5_file;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }


    public void init() {
        findViewById(R.id.iv_back).setOnClickListener(this);

        tvTitle = (TextView) findViewById(R.id.tv_title);

        mTbsReaderView = new TbsReaderView(this, this);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.tbsView);
        mRelativeLayout.addView(mTbsReaderView,new RelativeLayout.LayoutParams(-1,-1));
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        tvTitle.setText(title);


        String url = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        displayFile(url,name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
    }
    private String tbsReaderTemp =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "wanke";;
    private void displayFile(String filePath, String fileName) {
//    Log.e("TAG_查看文件","filePath="+filePath);
//        Log.e("TAG_查看文件","fileName="+fileName);
//        Log.e("TAG_查看文件","tbsReaderTemp="+tbsReaderTemp);
    //        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
//        String bsReaderTemp = tbsReaderTemp;
//        File bsReaderTempFile =new File(bsReaderTemp);
//        if (!bsReaderTempFile.exists()) {
//            Log.d("print","准备创建/TbsReaderTemp！！");
//            boolean mkdir = bsReaderTempFile.mkdir();
//            if(!mkdir){
//                Log.d("print","创建/TbsReaderTemp失败！！！！！");
//            }
//        }
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("tempPath", tbsReaderTemp);
        boolean result = mTbsReaderView.preOpen(getFileType(fileName), false);
//        Log.d("print","查看文档---"+result);
        if (result) {
            mTbsReaderView.openFile(bundle);
        }else{

        }
    }

    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
//            Log.d("print", "paramString---->null");
            return str;
        }
//        Log.d("print", "paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
//            Log.d("print", "i <= -1");
            return str;
        }

        str = paramString.substring(i + 1);
//        Log.d("print", "paramString.substring(i + 1)------>" + str);
        return str;
    }

    public static void show(Context context, String url) {
//        Log.e("TAG_X5文件","url="+url);
        Intent intent = new Intent(context, WebX5FileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("path", url);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }
}
