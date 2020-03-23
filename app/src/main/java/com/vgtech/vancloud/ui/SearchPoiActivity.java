package com.vgtech.vancloud.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.vgtech.common.utils.DeviceUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.PoiItem;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2017/4/20.
 */
public class SearchPoiActivity extends BaseActivity implements OnItemClickListener {
    private TextView mSearchTv;
    private PoiSearch mPoiSearch;
    private View mWaitView;
    private LocationClient locationClient;
    private ApiDataAdapter<PoiItem> mPoiAdapter;
    private SuggestionSearch mSuggestionSearch;
    private OnGetSuggestionResultListener mSuggestionListener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            mWaitView.setVisibility(View.GONE);
            List<PoiItem> poiItems = new ArrayList<>();
            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                mPoiAdapter.clear();
                mPoiAdapter.notifyDataSetChanged();
                return;
            }
            //获取在线建议结果。
            List<SuggestionResult.SuggestionInfo> poiInfos = suggestionResult.getAllSuggestions();
            for (SuggestionResult.SuggestionInfo suggestionInfo : poiInfos) {
                PoiItem poiItem = new PoiItem();
                poiItem.name = suggestionInfo.key;
                poiItem.address = suggestionInfo.district;
                poiItem.latlng = suggestionInfo.pt;
                poiItems.add(poiItem);
            }
            mPoiAdapter.clear();
            mPoiAdapter.add(poiItems);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.location));
        mWaitView = findViewById(R.id.progressBar);
        mWaitView.setVisibility(View.GONE);
        ListView poiList = (ListView) findViewById(R.id.poilist);
        mPoiAdapter = new ApiDataAdapter(this);
        poiList.setAdapter(mPoiAdapter);
        poiList.setOnItemClickListener(this);
        final SearchView searchView = (SearchView) findViewById(R.id.searchview);
        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        int closeId = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        final ImageView closeView = (ImageView) searchView.findViewById(closeId);
        closeView.setBackgroundResource(R.drawable.btn_actionbar);
        final TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(Color.parseColor("#CCFFFFFF"));
        searchView.setIconified(false);
        mSearchTv = textView;
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(
                        mSearchTv.getWindowToken(), 0);
                onBackPressed();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return false;
            }
        });
        //百度在线建议
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(mSuggestionListener);

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

    private void search(String s) {
        mPoiAdapter.clear();
        if (!TextUtils.isEmpty(s)) {
            mWaitView.setVisibility(View.VISIBLE);
            locationClient.requestLocation();
        }

    }

    boolean isFirst = false;
    BDLocationListener bdLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }
            int type = DeviceUtils.checkOp(SearchPoiActivity.this, 1);
            if (TextUtils.isEmpty(bdLocation.getAddrStr()) && type == 1) {
                Toast.makeText(SearchPoiActivity.this, com.vgtech.vantop.R.string.vantop_location_refused, Toast.LENGTH_SHORT).show();
            }

            if (isFirst) {
                TextView tvAddressView = (TextView) findViewById(R.id.tv_address);
                tvAddressView.setText(bdLocation.getAddrStr());
                tvAddressView.setTag(R.id.latitude, bdLocation.getLatitude() + "");
                tvAddressView.setTag(R.id.longitude, bdLocation.getLongitude() + "");
                mWaitView.setVisibility(View.GONE);
                isFirst = false;
            }
//            PoiNearbySearchOption poiNearbySearchOption = new PoiNearbySearchOption();
//            poiNearbySearchOption.radius(2000);
//            poiNearbySearchOption.pageCapacity(50);
//            poiNearbySearchOption.keyword(mSearchTv.getText().toString());
//            poiNearbySearchOption.location(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude()));
//            mPoiSearch.searchNearby(poiNearbySearchOption);

            SuggestionSearchOption suggestionSearchOption = new SuggestionSearchOption();
            suggestionSearchOption.keyword(mSearchTv.getText().toString())
                    .city(bdLocation.getCity());
            mSuggestionSearch.requestSuggestion(suggestionSearchOption);
        }
    };
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        public void onGetPoiResult(PoiResult result) {
            mWaitView.setVisibility(View.GONE);
            List<PoiItem> poiItems = new ArrayList<>();
            if (result != null) {
                List<PoiInfo> poiInfos = result.getAllPoi();
                if (poiInfos != null) {
                    for (PoiInfo poiInfo : poiInfos) {
                        PoiItem poiItem = new PoiItem();
                        poiItem.name = poiInfo.name;
                        poiItem.address = poiInfo.address;
                        poiItem.latlng = poiInfo.location;
                        poiItems.add(poiItem);
                    }
                }
            }
            mPoiAdapter.clear();
            mPoiAdapter.add(poiItems);
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
    protected int getContentView() {
        return R.layout.activity_poisearch;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof PoiItem) {
            PoiItem poiItem = (PoiItem) obj;
            Intent intent = new Intent();
            intent.putExtra("name", poiItem.name);
            intent.putExtra("address", poiItem.address);
            intent.putExtra("latlng", poiItem.latlng);
            setResult(RESULT_OK, intent);
            finish();
        }
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

}
