package com.vgtech.vancloud.ui.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.vgtech.common.utils.DeviceUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.PoiAdapter;

import java.util.List;

/**
 * Created by zhangshaofang on 2015/11/5.
 */
public class LocationActivity extends BaseActivity implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    @Override
    protected int getContentView() {
        return R.layout.location;
    }

    private PoiSearch mPoiSearch;
    private LocationClient locationClient;
    private PoiAdapter mPoiAdapter;

    private View mWaitView;

    boolean isFirst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.nearby));
        mPoiAdapter = new PoiAdapter(this);
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mPoiAdapter);
        listView.setOnItemClickListener(this);
        mWaitView = getLayoutInflater().inflate(R.layout.progress, null);
        TextView proTv = (TextView) mWaitView.findViewById(R.id.progress_tv);
        proTv.setTextColor(Color.parseColor("#929292"));
        listView.addFooterView(mWaitView);
//        mWaitView.setVisibility(View.GONE);

        initView();

        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        locationClient = new LocationClient(this);
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setOpenGps(true);
        locationClientOption.disableCache(true);
        locationClientOption.setCoorType("bd09ll");
        locationClientOption.setAddrType("all");
        locationClientOption.setPriority(LocationClientOption.NetWorkFirst);
        locationClient.setLocOption(locationClientOption);
        locationClient.registerLocationListener(bdLocationListener);
        locationClient.start();
        locationClient.requestLocation();

    }


    private EditText keywordEditText;
    private ImageView searchCancelView;
    private TextView cancleView;
    private TextView tvAddressView;

    public void initView() {

        isFirst = true;
        keywordEditText = (EditText) findViewById(R.id.keyword);
        searchCancelView = (ImageView) findViewById(R.id.search_cancel);
        cancleView = (TextView) findViewById(R.id.cancle_view);
        tvAddressView = (TextView) findViewById(R.id.tv_address);
        cancleView.setSelected(true);
        cancleView.setOnClickListener(this);
        searchCancelView.setOnClickListener(this);
        findViewById(R.id.now_address).setOnClickListener(this);

        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!keywordEditText.getText().toString().equals("")) {
                    searchCancelView.setVisibility(View.VISIBLE);
                    cancleView.setSelected(false);

                } else {
                    searchCancelView.setVisibility(View.INVISIBLE);
                    cancleView.setSelected(true);
                }
            }
        });

    }


    @Override
    public void finish() {
        if (locationClient != null && locationClient.isStarted()) {
            if (bdLocationListener != null) {
                locationClient.unRegisterLocationListener(bdLocationListener);
            }
            locationClient.stop();
            locationClient = null;
        }
        super.finish();
    }


    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        public void onGetPoiResult(PoiResult result) {
            mWaitView.setVisibility(View.GONE);
            if (result != null) {
                List<PoiInfo> poiInfos = result.getAllPoi();
                if (poiInfos != null)
                    mPoiAdapter.add(poiInfos);
            }
        }

        public void onGetPoiDetailResult(PoiDetailResult result) {
            //获取Place详情页检索结果
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof PoiInfo) {
            PoiInfo poiInfo = (PoiInfo) obj;
            Intent intent = new Intent();
            String latlng = "";
            if(poiInfo.location!=null)
                latlng = poiInfo.location.latitude + "," + poiInfo.location.longitude;
            intent.putExtra("latlng", latlng);
            intent.putExtra("address", poiInfo.name);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cancle_view:
                if (!TextUtils.isEmpty(keywordEditText.getText().toString())) {

                    hideKeyboard();
                    mPoiAdapter.clearList();
                    mWaitView.setVisibility(View.VISIBLE);
                    locationClient.requestLocation();
                }
                break;

            case R.id.search_cancel:
                keywordEditText.setText("");
                break;

            case R.id.now_address:

                if (!TextUtils.isEmpty(tvAddressView.getText())) {
                    String address = tvAddressView.getText().toString();
                    String latitude = tvAddressView.getTag(R.id.latitude).toString();
                    String longitude = tvAddressView.getTag(R.id.longitude).toString();
                    Intent intent = new Intent();
                    intent.putExtra("latlng", latitude + "," + longitude);
                    intent.putExtra("address", address);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            default:
                super.onClick(v);
                break;

        }
    }


    BDLocationListener bdLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }
            int type = DeviceUtils.checkOp(LocationActivity.this, 1);
            if (TextUtils.isEmpty(bdLocation.getAddrStr())&&type == 1) {
                Toast.makeText(LocationActivity.this, com.vgtech.vantop.R.string.vantop_location_refused, Toast.LENGTH_SHORT).show();
            }

            if (isFirst) {
                TextView tvAddressView = (TextView) findViewById(R.id.tv_address);
                tvAddressView.setText(bdLocation.getAddrStr());
                tvAddressView.setTag(R.id.latitude, bdLocation.getLatitude() + "");
                tvAddressView.setTag(R.id.longitude, bdLocation.getLongitude() + "");
                mWaitView.setVisibility(View.GONE);
                isFirst = false;
            }
            PoiNearbySearchOption poiNearbySearchOption = new PoiNearbySearchOption();
            poiNearbySearchOption.radius(2000);
            poiNearbySearchOption.pageCapacity(50);
            poiNearbySearchOption.keyword(keywordEditText.getText().toString());
            poiNearbySearchOption.location(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude()));
            mPoiSearch.searchNearby(poiNearbySearchOption);
        }
    };

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(keywordEditText.getWindowToken(), 0);
        }
    }
}
