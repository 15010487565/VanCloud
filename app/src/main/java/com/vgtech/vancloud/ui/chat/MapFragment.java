package com.vgtech.vancloud.ui.chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.DecimalFormat;

import roboguice.util.Strings;

/**
 * @author xuanqiang
 */
public class MapFragment extends ActionBarFragment {
    View doneButton;

    public static MapFragment newInstance(final String address, final double latitude, final double longitude) {
        return newInstance(address, latitude, longitude, null);
    }

    public static MapFragment newInstance(final String title) {
        return newInstance(null, -1, -1, title);
    }

    public static MapFragment newInstance(final String address, final double latitude, final double longitude, final String title) {
        Bundle bundle = new Bundle();
        bundle.putString("address", address);
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude", longitude);
        bundle.putString("title", title);
        MapFragment fragment = new MapFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            address = bundle.getString("address");
            latitude = bundle.getDouble("latitude", -1);
            longitude = bundle.getDouble("longitude", -1);
            title = bundle.getString("title");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = createContentView(R.layout.map);
        mMapView = (MapView) view.findViewById(R.id.map_view);
        doneButton = view.findViewById(R.id.tv_right);
        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(this);
        setSwipeBackEnable(false);
        return attachToSwipeBack(view);
    }

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;

    @SuppressLint("InflateParams")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Strings.isEmpty(title)) {
            title = getString(R.string.location);
        }
        titleView.setText(title);
        if (listener != null) {
            doneButton.setVisibility(View.VISIBLE);
            doneButton.setOnClickListener(this);
        }
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        if (latitude < 0) {
            doneButton.setVisibility(View.VISIBLE);
            locationClient = new LocationClient(getActivity());
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
            doneButton.setVisibility(View.GONE);
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

    }

    public MyLocationListenner myListener = new MyLocationListenner();
    boolean isFirstLoc = true; // 是否首次定位
    public static final DecimalFormat df = new DecimalFormat("0.000000");

    public static double doubleFormat(double d) {
        return Double.parseDouble(df.format(d));
    }

    private boolean mFinish;

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null || mFinish) {
                return;
            }
            latitude = doubleFormat(location.getLatitude());
            longitude = doubleFormat(location.getLongitude());
            address = location.getAddrStr();
            int type = DeviceUtils.checkOp(getActivity(), 1);
            if (TextUtils.isEmpty(address)&&type == 1) {
                Toast.makeText(getActivity(), com.vgtech.vantop.R.string.vantop_location_refused, Toast.LENGTH_SHORT).show();
            }
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

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    public void onClick(View view) {
        if (view == doneButton) {
            if (listener != null && latitude > 0) {
                mFinish = true;
                listener.onLocationSelected(address, latitude, longitude);
            }
        } else {
            super.onClick(view);
        }
    }

    @Override
    public void onDestroy() {
        if (locationClient != null && locationClient.isStarted()) {
            if (myListener != null) {
                locationClient.unRegisterLocationListener(myListener);
            }
            locationClient.stop();
            locationClient = null;
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
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


    public void setListener(final MapListener listener) {
        this.listener = listener;
    }

    public interface MapListener {
        void onLocationSelected(final String address, final double latitude, final double longitude);
    }

    private LocationClient locationClient;
    private View popupView;

    private Message message = new Message();

    private String address = "";
    private double latitude = -1;
    private double longitude = -1;

    private MapListener listener;
    private String title;

}
