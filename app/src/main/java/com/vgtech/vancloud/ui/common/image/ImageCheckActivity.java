package com.vgtech.vancloud.ui.common.image;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.ImageCheckAdapter;
import com.vgtech.vancloud.ui.view.ImageCheckViewPager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片查看
 * Created by Duke on 2015/8/18.
 */
public class ImageCheckActivity extends BaseActivity {

    ImageCheckViewPager viewPager;
    List<ImageInfo> imageInfoList = new ArrayList<>();
    TextView numView;
    String josnString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        josnString = intent.getStringExtra("listjson");
        boolean numVisible = intent.getBooleanExtra("numVisible",true);
        boolean userphoto = intent.getBooleanExtra("userphoto",false);
        try {
            imageInfoList = JsonDataFactory.getDataArray(ImageInfo.class, new JSONArray(josnString));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        numView = (TextView) findViewById(R.id.num);

        viewPager = (ImageCheckViewPager) findViewById(R.id.viewpager);
        ImageCheckAdapter imageCheckAdapter = new ImageCheckAdapter(this, imageInfoList,userphoto);
        viewPager.setAdapter(imageCheckAdapter);

        viewPager.setCurrentItem(position);
        numView.setText(position + 1 + "/" + imageInfoList.size());
        if(!numVisible)
        {
            numView.setVisibility(View.GONE);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                numView.setText(position + 1 + "/" + imageInfoList.size());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.imagecheck_layout;
    }
}
