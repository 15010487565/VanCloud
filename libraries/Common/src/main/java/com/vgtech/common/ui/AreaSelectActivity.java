package com.vgtech.common.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.vgtech.common.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.adapter.ComAdapter;
import com.vgtech.common.api.Dict;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by zhangshaofang on 2016/5/23.
 */
public class AreaSelectActivity extends BaseActivity implements HttpListener<String>, OnItemClickListener {
    private ComAdapter<Dict> mAreaAdapter;
    private LocationClient locationClient;
    private TextView mCurrentCityTv;
    private View mCurrentSelectedView;

    @Override
    protected int getContentView() {
        return R.layout.activity_area_select;
    }

    private int mLeave = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String style = getIntent().getStringExtra("style");
        if ("personal".equals(style)) {
            View bgTitleBar = findViewById(R.id.bg_titlebar);
            bgTitleBar.setBackgroundColor(Color.parseColor("#faa41d"));
        }
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView titleTv = (TextView) findViewById(android.R.id.title);
        String title = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(title))
            title = getString(R.string.personal_choose_place);
        titleTv.setText(title);
        Intent intent = getIntent();
        String json = intent.getStringExtra("json");
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        if (!TextUtils.isEmpty(json)) {
            mAreaAdapter = new ComAdapter<>(this);
            listView.setAdapter(mAreaAdapter);
            try {
                mHasParent = intent.getBooleanExtra("hasParent", false);
                mLeave = intent.getIntExtra("leave", 0) + 1;
                List<Dict> areas = JsonDataFactory.getDataArray(Dict.class, new JSONArray(json));
                if (areas.size() == 1) {
                    Dict dict = areas.get(0);
                    Dict d = new Dict();
                    d.id = dict.id;
                    d.name = dict.name;
                    mAreaAdapter.add(d);
                    areas = dict.getArrayData(Dict.class);
                } else if (mLeave == 2) {
                    Dict subDict = new Dict();
                    String parent_id = intent.getStringExtra("parent_id");
                    String parent_name = intent.getStringExtra("parent_name");
                    subDict.id = parent_id;
                    subDict.name = parent_name;
                    mAreaAdapter.add(subDict);
                }
                mAreaAdapter.add(areas);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            View headerView = getLayoutInflater().inflate(R.layout.area_header, null);
            mCurrentSelectedView = headerView.findViewById(R.id.iv_selected);
            mCurrentCityTv = (TextView) headerView.findViewById(R.id.tv_current_city);
            mCurrentCityTv.setEnabled(false);
            listView.addHeaderView(headerView);
            mAreaAdapter = new ComAdapter<>(this);
            listView.setAdapter(mAreaAdapter);
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
            loadAreaData();
        }
        String id = intent.getStringExtra("id");
        if (!TextUtils.isEmpty(id)) {
            Dict dict = new Dict();
            dict.id = id;
            mAreaAdapter.addSelected(dict);
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

    BDLocationListener bdLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }
            if (!TextUtils.isEmpty(bdLocation.getCity())) {
                String id = getIntent().getStringExtra("id");
                if (!TextUtils.isEmpty(id) && id.equals(bdLocation.getCityCode()))
                    mCurrentSelectedView.setVisibility(View.VISIBLE);
                mCurrentCityTv.setEnabled(true);
                mCurrentCityTv.setText(bdLocation.getCity());
                mCurrentCityTv.setTextColor(Color.parseColor("#444444"));
                mCurrentCityTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("id", bdLocation.getCityCode());
                        intent.putExtra("name", bdLocation.getCity());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
                if (locationClient != null && locationClient.isStarted()) {
                    if (bdLocationListener != null) {
                        locationClient.unRegisterLocationListener(bdLocationListener);
                    }
                    locationClient.stop();
                    locationClient = null;
                }
            }

        }
    };
    private boolean mHasParent = true;

    private void loadAreaData() {
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        String url = URLAddr.URL_DICT_VANCLOUD_CITYS;
        int type = getIntent().getIntExtra("type", 0);
        if (type == 1) {
            mHasParent = false;
            url = URLAddr.URL_AREA;
        }
        NetworkPath path = new NetworkPath(url);
        path.setType(NetworkPath.TYPE_JSONARRAY);
        networkManager.load(1, path, this, true);
    }

    private boolean mInit;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (!TextUtils.isEmpty(rootData.responce)) {
            try {
                if (mInit)
                    return;
                mInit = true;

                String id = getIntent().getStringExtra("id");
                String name = getIntent().getStringExtra("name");
                if (!TextUtils.isEmpty(id)) {
                    Dict dict = new Dict();
                    dict.id = id;
                    mAreaAdapter.addSelected(dict);
                }

                List<Dict> areaList = JsonDataFactory.getDataArray(Dict.class, new JSONArray(rootData.responce));
                Dict d = null;
                if (TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
                    for (Dict area : areaList) {
                        List<Dict> subList = area.getArrayData(Dict.class);
                        for (Dict sarea : subList) {
                            if (name.equals(sarea.name)) {
                                d = sarea;
                                break;
                            }
                        }
                    }
                }
                if (d != null) {
                    mAreaAdapter.addSelected(d);
                }
                mAreaAdapter.add(areaList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof Dict) {
            Dict area = (Dict) obj;
            if (area.getArrayData(Dict.class).isEmpty()) {
                Intent intent = new Intent();
                intent.putExtra("id", area.id);
                intent.putExtra("name", area.name);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Intent intent = new Intent(this, AreaSelectActivity.class);
                intent.putExtra("hasParent", mHasParent);
                intent.putExtra("leave", mLeave);
                intent.putExtra("parent_id", area.getId());
                intent.putExtra("parent_name", area.getName());
                intent.putExtra("style", getIntent().getStringExtra("style"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                intent.putExtra("name", getIntent().getStringExtra("name"));
                intent.putExtra("title", area.name);
                try {
                    intent.putExtra("json", area.getJson().getJSONArray("sub_data").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, 1001);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String id = data.getStringExtra("id");
            String name = data.getStringExtra("name");
            Intent intent = new Intent();
            intent.putExtra("id", id);
            intent.putExtra("name", name);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
