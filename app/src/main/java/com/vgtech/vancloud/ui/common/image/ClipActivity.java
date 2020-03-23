package com.vgtech.vancloud.ui.common.image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.vancloud.ui.view.clip.ClipImageLayout;
import com.vgtech.common.image.ImageUtility;

/**
 * Created by zhangshaofang on 2015/10/8.
 */
public class ClipActivity extends BaseActivity {
    private ClipImageLayout mClipImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.clip_photo));
        mClipImageLayout = (ClipImageLayout) findViewById(R.id.id_clipImageLayout);
        initRightTv(getString(R.string.ok));

        String style = getIntent().getStringExtra("style");
        if("personal".equals(style))
        {
            View bgTitleBar = findViewById(com.vgtech.common.R.id.bg_titlebar);
            bgTitleBar.setBackgroundColor(Color.parseColor("#faa41d"));
        }

        try {
            String path = getIntent().getStringExtra("path");
            Bitmap bm = Bimp.revitionImageSize(path);
            bm = ImageUtility.checkFileDegree(path, bm);
            mClipImageLayout.setImageBmp(bm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                Bitmap bitmap = mClipImageLayout.clip();
                String newStr = String.valueOf(System.currentTimeMillis());
                String path = FileUtils.saveBitmap(this,bitmap, "" + newStr);
                Intent intent = new Intent();
                intent.putExtra("path", path);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.clip_layout;
    }
}
