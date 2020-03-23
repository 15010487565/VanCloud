package com.vgtech.vancloud.ui.module.financemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.TradeTypeListItem;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.TradeTypeListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nick on 2016/1/4.
 */
public class TradeTypeListActivity extends BaseActivity implements HttpListener<String>, View.OnClickListener {

    private final int GET_TRADE_LIST = 431;
    private PullToRefreshListView listView;

    private TradeTypeListAdapter adapter;
    private List<TradeTypeListItem> listData;

    private NetworkManager mNetworkManager;

    private View mWaitView;
    private TextView tipTv;
    private RelativeLayout mDefaultlayout;
    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;

    private boolean mSafe;

    @Override
    protected int getContentView() {
        return R.layout.trade_type_list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.vancloud_trade_type));
        initView();
        initData();
        setListener();
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.add:
                break;
            default:
                super.onClick(v);
                break;

        }
    }

    public void initView() {

        listView = (PullToRefreshListView) findViewById(R.id.listview);
        mWaitView = getLayoutInflater().inflate(R.layout.progress, null);
        tipTv = (TextView) findViewById(R.id.nodetailview);
        mDefaultlayout = (RelativeLayout) findViewById(R.id.default_layout);
        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);

//        findViewById(R.id.add).setVisibility(View.GONE);
//        findViewById(R.id.search).setVisibility(View.GONE);
    }

    private void initData() {
        listData = new ArrayList<TradeTypeListItem>();
        adapter = new TradeTypeListAdapter(this, listData);
        listView.setAdapter(adapter);
        loadTradeTypeInfo();
    }

    private void setListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("data", listData.get(position - 1));
                setResult(TradeListActivity.CALLBACKID, intent);
                finish();
            }
        });
    }


    //网络请求
    private void loadTradeTypeInfo() {
        showLoadingView();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_TYPE), params, this);
        mNetworkManager.load(GET_TRADE_LIST, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        hideLoadingView();
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);


        if (!mSafe) {
            hideLoadingView();
            listView.onRefreshComplete();
            return;
        }
        switch (callbackId) {
            case GET_TRADE_LIST:
                listView.onRefreshComplete();
                mWaitView.setVisibility(View.GONE);
                List<TradeTypeListItem> tradeItems;
                try {
                    TradeTypeListItem itmeAll = new TradeTypeListItem();
                    itmeAll.value = getString(R.string.all);
                    itmeAll.name = "";
                    listData.add(itmeAll);
                    tradeItems = JsonDataFactory.getDataArray(TradeTypeListItem.class, rootData.getJson().getJSONArray("data"));
                    listData.addAll(tradeItems);
                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

        }
    }

    private HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    @Override
    public void onResponse(String response) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mNetworkManager != null) {
            mNetworkManager.cancle(this);
            mNetworkManager = null;
        }


    }

    private void showLoadingView() {

        listView.setVisibility(View.INVISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMagView.setVisibility(View.VISIBLE);
        loadingMagView.setText(getString(R.string.dataloading));

    }

    private void hideLoadingView() {

        loadingLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingMagView.setVisibility(View.GONE);
        loadingMagView.setText(null);
        listView.setVisibility(View.VISIBLE);

    }
}
