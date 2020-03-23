package com.vgtech.vantop.ui.punchcard;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.CardArea;
import com.vgtech.vantop.moudle.Coord;
import com.vgtech.vantop.moudle.LangMoudle;
import com.vgtech.vantop.moudle.PunchCardInitData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2016/12/8.
 */
public class PunchMapActivity extends BaseActivity implements BDLocationListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("打卡范围");
        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.setVisibility(View.VISIBLE);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.NORMAL, true, null));
        initLocation();
        String json = getIntent().getStringExtra("json");
        try {
            JSONObject jsonObject = new JSONObject(json);
            PunchCardInitData mDatas = PunchCardInitData.fromJson(json.toString());

            if (mDatas == null)
                return;
            JSONObject clockAreaJsonObject = jsonObject.getJSONObject("clockArea");
            if (clockAreaJsonObject.has("coord")) {
                try {
                    List<Coord> coords = JsonDataFactory.getDataArray(Coord.class, clockAreaJsonObject.getJSONArray("coord"));
                    for (Coord coord : coords) {
                        if (coord.circle == 0) {
                            LatLng pCenter = new LatLng(coord.ltlatitude, coord.ltlongitufe);
                            CircleOptions circleOptions = new CircleOptions();
                            circleOptions.center(pCenter) //圆心
                                    .radius(coord.getRadius())//半径 单位米
                                    .fillColor(0xAAFFFF00)//填充色
                                    .stroke(new Stroke(5, 0xAA00FF00));//边框宽度和颜色
                            mBaiduMap.addOverlay(circleOptions);
                        } else {
                            ArrayList<LatLng> latLngs = new ArrayList<>();
                            latLngs.add(new LatLng(coord.ltlatitude, coord.ltlongitufe));
                            latLngs.add(new LatLng(coord.rtlatitude, coord.rtlongitude));
                            latLngs.add(new LatLng(coord.rblatitude, coord.rblongitude));
                            latLngs.add(new LatLng(coord.lblatitude, coord.lblongitude));
                            //构建用户绘制多边形的Option对象
                            OverlayOptions polygonOption = new PolygonOptions()
                                    .points(latLngs)
                                    .stroke(new Stroke(5, 0xAA00FF00))
                                    .fillColor(0xAAFFFF00);
//在地图上添加多边形Option，用于显示
                            mBaiduMap.addOverlay(polygonOption);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                addToMap(mDatas.getLonglat());
            }
            long currentTimeMillis = System.currentTimeMillis();
            List<CardArea> scheduleArea = JsonDataFactory.getDataArray(CardArea.class, jsonObject.getJSONArray("scheduleArea"));
            List<CardArea> leaArea = JsonDataFactory.getDataArray(CardArea.class, jsonObject.getJSONArray("leaArea"));
            long preClockintime = Long.parseLong(jsonObject.getString("preClockintime"));
            long sufclockinTime = Long.parseLong(jsonObject.getString("sufclockinTime"));
            int clockidRadius = Integer.parseInt(jsonObject.getString("clockidRadius"));
            for (CardArea cardArea : scheduleArea) {//日程
                LatLng pCenter = new LatLng(Double.parseDouble(cardArea.latitude), Double.parseDouble(cardArea.longitude));
                    if (currentTimeMillis >= cardArea.getStartTime() - preClockintime && currentTimeMillis <= cardArea.getStartTime() + sufclockinTime
                            || currentTimeMillis >= cardArea.getEndTime() - preClockintime && currentTimeMillis <= cardArea.getEndTime() + sufclockinTime
                            ) {
                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.center(pCenter) //圆心
                                .radius(clockidRadius)//半径 单位米
                                .fillColor(0xAAFFFF00)//填充色
                                .stroke(new Stroke(5, 0xAA00FF00));//边框宽度和颜色
                        mBaiduMap.addOverlay(circleOptions);
                    }
            }
            for (CardArea cardArea : leaArea) {//休假
                LatLng pCenter = new LatLng(Double.parseDouble(cardArea.latitude), Double.parseDouble(cardArea.longitude));
                if (currentTimeMillis >= cardArea.getStartTime() - preClockintime && currentTimeMillis <= cardArea.getStartTime() + sufclockinTime
                        || currentTimeMillis >= cardArea.getEndTime() - preClockintime && currentTimeMillis <= cardArea.getEndTime() + sufclockinTime
                        ) {
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(pCenter) //圆心
                            .radius(clockidRadius)//半径 单位米
                            .fillColor(0xAAFFFF00)//填充色
                            .stroke(new Stroke(5, 0xAA00FF00));//边框宽度和颜色
                    mBaiduMap.addOverlay(circleOptions);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLClient.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLClient.stop();
    }

    /***
     * 初始化定位服务
     */
    private void initLocation() {

        mLClient = new LocationClient(getApplicationContext());
        mLClient.registerLocationListener(this);

        LocationClientOption option = new LocationClientOption();
        option.disableCache(true);
        option.setPriority(LocationClientOption.GpsFirst);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 10 * 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        mLClient.setLocOption(option);
    }

    boolean isFirstLoc = true;

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        if (bdLocation != null) {
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    public void addToMap(String longlat) {

        if (TextUtils.isEmpty(longlat) || !longlat.contains(",")) {
            return;
        }
        if (longlat.contains("#")) {
            String[] companies = longlat.split("#");
            for (String company : companies) {
                //每个公司是一个用经纬度列表标识的区域
                String[] tudes = company.split(",,");
                List<LatLng> pts = new ArrayList<LatLng>();
                for (String tude : tudes) {
                    String lgtude = tude.split(",")[0];
                    String ltude = tude.split(",")[1];
                    LatLng pt1 = new LatLng(Double.parseDouble(ltude), Double.parseDouble(lgtude));
                    pts.add(pt1);
                }
                //构建用户绘制多边形的Option对象
                OverlayOptions polygonOption = new PolygonOptions()
                        .points(pts)
                        .stroke(new Stroke(5, 0xAA00FF00))
                        .fillColor(0xAAFFFF00);
//在地图上添加多边形Option，用于显示
                mBaiduMap.addOverlay(polygonOption);
            }
        } else {

            ArrayList<LangMoudle> latLngs = new ArrayList<>();
            //每个公司是一个用经纬度列表标识的区域
            String[] tudes = longlat.split(",,");
            List<LatLng> pts = new ArrayList<LatLng>();
            for (String tude : tudes) {
                String lgtude = tude.split(",")[0];
                String ltude = tude.split(",")[1];
                LatLng pt1 = new LatLng(Double.parseDouble(ltude), Double.parseDouble(lgtude));
                pts.add(pt1);
            }
            //构建用户绘制多边形的Option对象
            OverlayOptions polygonOption = new PolygonOptions()
                    .points(pts)
                    .stroke(new Stroke(5, 0xAA00FF00))
                    .fillColor(0xAAFFFF00);
//在地图上添加多边形Option，用于显示
            mBaiduMap.addOverlay(polygonOption);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_punchmap;
    }
}
