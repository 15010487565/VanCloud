package com.vgtech.vancloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.PoiItem;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/11/14.
 */
public class MapLocationActivity extends BaseActivity implements OnGetGeoCoderResultListener, AdapterView.OnItemClickListener {
    private double latitude = -1;
    private double longitude = -1;
    private LocationClient locationClient;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private String mCityCode;
    @Override
    protected int getContentView() {
        return R.layout.mappoi_location;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
        initView();
        setTitle(getString(R.string.location));

    }

    private boolean mEdit;
    private boolean mInit;
    private ApiDataAdapter<PoiItem> mPoiAdapter;
    private CircleOptions mCircleOptions;

    private void initView() {
        Intent intent = getIntent();
        String latlng = intent.getStringExtra("latlng");
        String address = intent.getStringExtra("address");
        String name = intent.getStringExtra("name");
        mEdit = intent.getBooleanExtra("edit", false);
        if (mEdit) {
            ListView poiList = (ListView) findViewById(R.id.poilist);
            mPoiAdapter = new ApiDataAdapter(this);
            poiList.setAdapter(mPoiAdapter);
            poiList.setOnItemClickListener(this);
        } else {
            findViewById(R.id.footer_view).setVisibility(View.GONE);
            findViewById(R.id.icon_gcoding).setVisibility(View.GONE);
        }
        mMapView = (MapView) findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        if (!TextUtils.isEmpty(latlng)) {
            String[] gps = latlng.split(",");
            if (gps.length > 1) {
                latitude = Double.parseDouble(gps[0]);
                longitude = Double.parseDouble(gps[1]);
            }
        }
        if (!mEdit) {
            findViewById(R.id.btn_location).setVisibility(View.GONE);
            findViewById(R.id.btn_action_search).setVisibility(View.GONE);
        }
        if (mEdit)
            mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
                /**
                 * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
                 * @param status 地图状态改变开始时的地图状态
                 */
                public void onMapStatusChangeStart(MapStatus status) {
                    mBaiduMap.clear();
                }

                @Override
                public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

                }

                /**
                 * 地图状态变化中
                 * @param status 当前地图状态
                 */
                public void onMapStatusChange(MapStatus status) {
                }

                /**
                 * 地图状态改变结束
                 * @param status 地图状态改变结束后的地图状态
                 */
                public void onMapStatusChangeFinish(MapStatus status) {
                    LatLng ll = status.target;
                    mCircleOptions = new CircleOptions();
                    mCircleOptions.center(ll) //圆心
                            .radius(200)//半径 单位米
                            .fillColor(0xAA3ab5ff);//填充色
//                        .stroke(new Stroke(5, 0xAA00FF00));//边框宽度和颜色
                    mBaiduMap.addOverlay(mCircleOptions);
                    if (mUserClick) {
                        mUserClick = false;
                        return;
                    }
                    latitude = ll.latitude;
                    longitude = ll.longitude;
                    mPoiAdapter.clear();
                    findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(ll));
                }
            });
        if (latitude > 0) {
            LatLng ll = new LatLng(latitude,
                    longitude);
            if (mEdit) {
                initRightTv(getString(R.string.ok));

                mInit = true;
                PoiItem currentPoiItem = new PoiItem();
                currentPoiItem.name = name;
                currentPoiItem.address = address;
                currentPoiItem.latlng = ll;
                currentPoiItem.init = true;
                mPoiAdapter.clear();
                mPoiAdapter.getSelectedData().clear();
                mPoiAdapter.addSelected(currentPoiItem);
                mPoiAdapter.add(currentPoiItem);
                findViewById(R.id.icon_gcoding).setVisibility(View.VISIBLE);
                mUserClick = true;
                findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(ll));
            }
            final MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    mBaiduMap.animateMapStatus(u);
                }
            }, 100);
        }
        if (mEdit) {
            setActionSearch();
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
        } else {
            int type = intent.getIntExtra("type", 0);
            if (type == 1) {
                LatLng ln = new LatLng(latitude, longitude);
                BitmapDescriptor bd = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_gcoding);
                MarkerOptions ooA = new MarkerOptions().position(ln).icon(bd)
                        .zIndex(9).draggable(true);
                final MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ln);
                mBaiduMap.addOverlay(ooA);
                mCircleOptions = new CircleOptions();
                mCircleOptions.center(ln) //圆心
                        .radius(200)//半径 单位米
                        .fillColor(0xAA3ab5ff);//填充色
//                        .stroke(new Stroke(5, 0xAA00FF00));//边框宽度和颜色
                mBaiduMap.addOverlay(mCircleOptions);
                findViewById(R.id.single_address).setVisibility(View.VISIBLE);
                TextView tv_name = (TextView) findViewById(R.id.tv_name);
                tv_name.setText("[" + getString(R.string.location) + "]");
                getAddressByLatLng(ln);
            }
        }
    }

    private void getAddressByLatLng(LatLng latlnt) {
        // 创建地理编码检索实例
        GeoCoder geoCoder = GeoCoder.newInstance();
        //
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            // 反地理编码查询结果回调函数
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                }
                String address = result.getAddress();
                TextView tv_address = (TextView) findViewById(R.id.tv_address);
                tv_address.setText(address);
            }

            // 地理编码查询结果回调函数
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                }
            }
        };
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);
        //
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latlnt));
        // 释放地理编码检索实例
        // geoCoder.destroy();
    }

    private GeoCoder mSearch = null;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                if (latitude != -1) {
                    List<PoiItem> poiItems = mPoiAdapter.getSelectedData();
                    if (poiItems == null || poiItems.isEmpty()) {
                        Toast.makeText(this, R.string.get_address, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PoiItem poiItem = mPoiAdapter.getSelectedData().get(0);
                    Intent intent = new Intent();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#.000000");
                    intent.putExtra("latlng", df.format(poiItem.latlng.latitude) + "," + df.format(poiItem.latlng.longitude));
                    intent.putExtra("address", poiItem.address);
                    intent.putExtra("name", poiItem.name);
                    intent.putExtra("citycode", mCityCode);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(this, R.string.get_address_failed, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btn_action_search: {
                Intent intent = new Intent(this, SearchPoiActivity.class);
                startActivityForResult(intent, 100);
            }
            break;
            case R.id.btn_location:
                mUserClick = false;
                mInit = false;
                locationClient.requestLocation();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            LatLng ll = intent.getParcelableExtra("latlng");
            String address = intent.getStringExtra("address");
            String name = intent.getStringExtra("name");
            PoiItem poiItem = new PoiItem();
            poiItem.name = name;
            poiItem.address = address;
            poiItem.latlng = ll;
            mPoiAdapter.getSelectedData().clear();
            mPoiAdapter.addSelected(poiItem);
            mPoiAdapter.notifyDataSetChanged();
            mUserClick = false;
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(poiItem.latlng);
            mBaiduMap.animateMapStatus(u);

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

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        findViewById(R.id.progressbar).setVisibility(View.GONE);
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MapLocationActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        List<PoiInfo> poiInfoList = result.getPoiList();
        List<PoiItem> poiItems = new ArrayList<>();
        PoiItem currentPoiItem = null;
        if (!mPoiAdapter.getSelectedData().isEmpty()) {
            PoiItem poiItem = mPoiAdapter.getSelectedData().get(0);
            if (poiItem.init)
                currentPoiItem = poiItem;
        }
        if (currentPoiItem == null) {
            currentPoiItem = new PoiItem();
            currentPoiItem.name = "";
            currentPoiItem.address = result.getAddress();
            currentPoiItem.latlng = result.getLocation();
        }
        poiItems.add(currentPoiItem);
        if (poiInfoList != null) {
            for (PoiInfo poiInfo : poiInfoList) {
                if (currentPoiItem.name.equals(poiInfo.name)) {
                    continue;
                }
                PoiItem poiItem = new PoiItem();
                poiItem.name = poiInfo.name;
                poiItem.address = poiInfo.address;
                poiItem.latlng = poiInfo.location;
                poiItems.add(poiItem);
            }
        }
        mPoiAdapter.clear();
        mPoiAdapter.getSelectedData().clear();
        mPoiAdapter.addSelected(currentPoiItem);
        mPoiAdapter.add(poiItems);
    }

    private boolean mUserClick;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Object obj = adapterView.getItemAtPosition(i);
        if (obj instanceof PoiItem) {
            PoiItem poiItem = (PoiItem) obj;
            mPoiAdapter.getSelectedData().clear();
            mPoiAdapter.addSelected(poiItem);
            mPoiAdapter.notifyDataSetChanged();
            mUserClick = true;
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(poiItem.latlng);
            mBaiduMap.animateMapStatus(u);
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }
            if (!mInit && mEdit) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                initRightTv(getString(R.string.ok));
                mCityCode = location.getCityCode();
                LatLng ll = new LatLng(latitude,
                        longitude);
                PoiItem currentPoiItem = new PoiItem();
                currentPoiItem.name = "";
                currentPoiItem.address = location.getAddrStr();
                currentPoiItem.latlng = ll;
                mPoiAdapter.clear();
                mPoiAdapter.getSelectedData().clear();
                mPoiAdapter.addSelected(currentPoiItem);
                mPoiAdapter.add(currentPoiItem);
                findViewById(R.id.icon_gcoding).setVisibility(View.VISIBLE);
                mUserClick = true;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
                findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(ll));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }


}
