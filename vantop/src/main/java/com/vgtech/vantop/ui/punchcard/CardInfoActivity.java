package com.vgtech.vantop.ui.punchcard;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.utils.AlignedTextUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2017/2/27.
 * 打卡 某个时间
 */
public class CardInfoActivity extends BaseActivity {

    @Override
    protected int getContentView() {
        return R.layout.activity_cardinfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vantop_punchcard_record_info));
        Intent intent = getIntent();
        String cardinfo = intent.getStringExtra("cardinfo");
        if (!TextUtils.isEmpty(cardinfo)) {
            PunchCardListData data = new Gson().fromJson(cardinfo
                    , PunchCardListData.class);
            if (data.getType() != -1) {
                setTitle(getString(data.getType()));
            }
            TextView tv_date = (TextView) findViewById(R.id.tv_date);
            TextView tv_time = (TextView) findViewById(R.id.tv_time);
            ImageView ic_time = (ImageView) findViewById(R.id.ic_time);

            ic_time.setImageResource(DateTimeUtil.isAm(data.getTime()) ? R.mipmap.ic_time_am : R.mipmap.ic_time_pm);
            tv_date.setText(data.getDate() + " " + DateTimeUtil.getWeekOfDate(this, data.getDate()));
            tv_time.setText(data.getTime());

            TextView tvLocation = (TextView) findViewById(R.id.tv_location);
            SpannableStringBuilder retailLocation = AlignedTextUtils.justifyString("经纬度", 4);
            retailLocation.append("：");
            tvLocation.setText(retailLocation);

            TextView tvLocationContent = (TextView) findViewById(R.id.tv_locationContent);
            String longitude = data.getLongitude();
            String latitude = data.getLatitude();
            LinearLayout llLocation = (LinearLayout) findViewById(R.id.ll_location);
            View line1 = findViewById(R.id.line1);
            if (TextUtils.isEmpty(longitude)||TextUtils.isEmpty(latitude)){
                llLocation.setVisibility(View.GONE);
                line1.setVisibility(View.GONE);
            }else {
                llLocation.setVisibility(View.VISIBLE);
                tvLocationContent.setText( (TextUtils.isEmpty(longitude) ? "0.00" : longitude)+ "," + (TextUtils.isEmpty(latitude) ? "0.00" : latitude));
                line1.setVisibility(View.VISIBLE);
            }

            TextView tvAddress = (TextView) findViewById(R.id.tv_address);
            String address = data.getAddress();

            LinearLayout llAddress = (LinearLayout) findViewById(R.id.ll_Address);
            View line2 = findViewById(R.id.line2);
            if (TextUtils.isEmpty(address)){
                llAddress.setVisibility(View.GONE);
                line2.setVisibility(View.GONE);
            }else {
                llAddress.setVisibility(View.VISIBLE);
                tvAddress.setText(address);
                line2.setVisibility(View.VISIBLE);
            }


            String remark = data.getRemark();
            LinearLayout ll_remark = (LinearLayout) findViewById(R.id.ll_remark);

            if (TextUtils.isEmpty(remark)){
                ll_remark.setVisibility(View.GONE);
                findViewById(R.id.line3).setVisibility(View.GONE);
            }else {
                ll_remark.setVisibility(View.VISIBLE);
                findViewById(R.id.line3).setVisibility(View.VISIBLE);

                TextView tv_remark = (TextView) findViewById(R.id.tv_remark);

                tv_remark.setText(remark);
                TextView tvRemarkLeft = (TextView) findViewById(R.id.tv_remarkLeft);
                SpannableStringBuilder retailRemarkLeft = AlignedTextUtils.justifyString("备注", 4);
                retailRemarkLeft.append("：");
                tvRemarkLeft.setText(retailRemarkLeft);

            }


            String imgs = data.getPictures();
            NoScrollGridview igView = (NoScrollGridview) findViewById(R.id.imagegridview);
            if (!TextUtils.isEmpty(imgs)) {
                final List<ImageInfo> imags = new ArrayList<>();
                if (imgs.contains(",")) {
                    String[] path = imgs.split(",");
                    for (String p : path) {
                        p = VanTopUtils.generatorImageUrl(this, p);
                        ImageInfo info = new ImageInfo(p);
                        info.thumb = p;
                        info.url = p;
                        imags.add(info);
                    }
                } else {
                    imgs = VanTopUtils.generatorImageUrl(this, imgs);
                    ImageInfo info = new ImageInfo(imgs);
                    info.thumb = imgs;
                    info.url = imgs;
                    imags.add(info);
                }
                ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(igView, this,3, imags);
                igView.setAdapter(imageGridviewAdapter);
                igView.setVisibility(View.VISIBLE);
            } else {
                igView.setVisibility(View.GONE);
                if (TextUtils.isEmpty(remark)){
                    findViewById(R.id.line2).setVisibility(View.GONE);
                }else {
                    findViewById(R.id.line3).setVisibility(View.GONE);
                }
            }
        }


    }
}
