package com.vgtech.vancloud.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.vgtech.common.utils.DeviceUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

/**
 * Created by zhangshaofang on 2015/11/14.
 */
public class MapLocationActivity extends BaseActivity {
    private String address = "";
    private double latitude = -1;
    private double longitude = -1;
    private LocationClient locationClient;
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.nearby));
        initView();
    }

    private boolean mEdit;
    private boolean mInit;

    private void initView() {
        mMapView = (MapView) findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        Intent intent = getIntent();
        String latlng = intent.getStringExtra("latlng");
        String address = intent.getStringExtra("address");
        mEdit = intent.getBooleanExtra("edit", false);
        TextView addressTv = (TextView) findViewById(R.id.et_address);
        if (!TextUtils.isEmpty(latlng)) {
            addressTv.setText(address);
            String[] gps = latlng.split(",");
            if (gps.length > 1) {
                latitude = Double.parseDouble(gps[0]);
                longitude = Double.parseDouble(gps[1]);
            }
        }
        if (!mEdit) {
            addressTv.setEnabled(false);
            findViewById(R.id.btn_location).setVisibility(View.GONE);
        }
        if (latitude > 0) {
            if (mEdit) {
                initRightTv(getString(R.string.ok));
            }
            mInit = true;
            LatLng ll = new LatLng(latitude,
                    longitude);
            BitmapDescriptor bd = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_gcoding);
            MarkerOptions ooA = new MarkerOptions().position(ll).icon(bd)
                    .zIndex(9).draggable(true);
            final MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.addOverlay(ooA);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    mBaiduMap.animateMapStatus(u);
                }
            }, 100);
        }
        if (mEdit) {
            if (latitude < 0) {
                Toast.makeText(this, R.string.toast_get_location, Toast.LENGTH_SHORT).show();
            }
            findViewById(R.id.btn_location).setOnClickListener(this);
            locationClient = new LocationClient(this);
            LocationClientOption locationClientOption = new LocationClientOption();
            locationClientOption.setOpenGps(true);
            locationClientOption.disableCache(true);
            locationClientOption.setCoorType("bd09ll");
            locationClientOption.setAddrType("all");
            locationClientOption.setPriority(LocationClientOption.NetWorkFirst);
            locationClient.setLocOption(locationClientOption);
            locationClient.registerLocationListener(myListener);
            locationClient.start();
            locationClient.requestLocation();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                if (latitude != -1) {
                    TextView addressTv = (TextView) findViewById(R.id.et_address);
                    String address = addressTv.getText().toString();
                    if (TextUtils.isEmpty(address)) {
                        Toast.makeText(this, R.string.get_address, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("latlng", latitude + "," + longitude);
                    intent.putExtra("address", address);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(this, R.string.get_address_failed, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btn_location:
                mInit = false;
                locationClient.requestLocation();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void finish() {
        if (locationClient != null && locationClient.isStarted()) {
            if (myListener != null) {
                locationClient.unRegisterLocationListener(myListener);
            }
            locationClient.stop();
            locationClient = null;
        }
        super.finish();
    }

    public MyLocationListenner myListener = new MyLocationListenner();

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }
            if (!mInit) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                initRightTv(getString(R.string.ok));
                address = location.getAddrStr();
                int type = DeviceUtils.checkOp(MapLocationActivity.this, 1);
                if (TextUtils.isEmpty(address) && type == 1) {
                    Toast.makeText(MapLocationActivity.this, com.vgtech.vantop.R.string.vantop_location_refused, Toast.LENGTH_SHORT).show();
                }
                TextView addressTv = (TextView) findViewById(R.id.et_address);
                addressTv.setText(address);
                LatLng ll = new LatLng(latitude,
                        longitude);
                BitmapDescriptor bd = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_gcoding);
                MarkerOptions ooA = new MarkerOptions().position(ll).icon(bd)
                        .zIndex(9).draggable(true);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.addOverlay(ooA);
                mBaiduMap.animateMapStatus(u);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    public boolean swipeBackPriority() {
        return false;
    }

    @Override
    protected int getContentView() {
        return R.layout.map_location;
    }
}
