package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

/**
 * Created by brook on 2015/8/18.
 */
public class QrCodeActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String qrCodeUri = intent.getStringExtra("qrCodeUri");
        SimpleDraweeView qrImage = (SimpleDraweeView)findViewById(R.id.iv_qrCode);
        ImageOptions.setImage(qrImage,qrCodeUri);
        qrImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_qr_code;
    }
}
