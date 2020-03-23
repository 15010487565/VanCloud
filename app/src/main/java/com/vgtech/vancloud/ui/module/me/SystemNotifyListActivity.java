package com.vgtech.vancloud.ui.module.me;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.SystemNotifyItem;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.SystemNotifyAdapter;
import com.vgtech.vancloud.ui.web.WebActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Nick on 2016/1/4.
 */
public class SystemNotifyListActivity extends BaseActivity implements HttpListener<String>, AbsListView.OnScrollListener, View.OnClickListener {

    private final int GET_SYSTEM_NOTIFY_LIST = 431;
    private ListView listView;

    private SystemNotifyAdapter adapter;

    private NetworkManager mNetworkManager;

    private boolean mSafe;
    private View mWaitView;
    private TextView tipTv;
    private RelativeLayout mDefaultlayout;

    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;

    private boolean isShowLoad=true;

    private List<SystemNotifyItem> data;

    //personal  company
    private String style;

    @Override
    protected int getContentView() {
        return R.layout.system_notify_list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.feature_introduce));

        style = getIntent().getStringExtra("style");

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
    private boolean mPersonal;

    public void initView() {

        listView = (ListView) findViewById(R.id.listview);
        mWaitView = getLayoutInflater().inflate(R.layout.progress, null);
        tipTv = (TextView) findViewById(R.id.nodetailview);
        mDefaultlayout = (RelativeLayout) findViewById(R.id.default_layout);
        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
    }

    private void initData() {
        data = new ArrayList<SystemNotifyItem>();
        adapter = new SystemNotifyAdapter(this, data);
        listView.setAdapter(adapter);
        loadTradeInfo();
    }

    private void setListener() {
        listView.setOnScrollListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SystemNotifyItem item = data.get(position);
                Intent intent = new Intent(SystemNotifyListActivity.this,WebActivity.class);
                intent.putExtra("title",item.title);
                intent.putExtra("style",style);
                String url = ApiUtils.generatorUrl(SystemNotifyListActivity.this, String.format(URLAddr.URL_NOTIFICATIONS,item.buildno));
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    //网络请求
    private void loadTradeInfo() {
        if (isShowLoad)
            showLoadingView();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid","0");
        params.put("clienttype","personal".equals(style)?"personal":"enterprise");
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("devicetype", "android");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SYSTEM_NOTIFY), params, this);
        mNetworkManager.load(GET_SYSTEM_NOTIFY_LIST, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if(isShowLoad)
            hideLoadingView();
        isShowLoad=true;
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            hideLoadingView();
            return;
        }
        switch (callbackId) {
            case GET_SYSTEM_NOTIFY_LIST:
                mWaitView.setVisibility(View.GONE);

                List<SystemNotifyItem> items = null;
                try {
                    items = JsonDataFactory.getDataArray(SystemNotifyItem.class, rootData.getJson().getJSONArray("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (items != null && items.size() > 0) {
                    mDefaultlayout.setVisibility(View.GONE);
                } else {
                    tipTv.setText(R.string.no_system_notify);
                    mDefaultlayout.setVisibility(View.VISIBLE);
                    return;
                }

                data.addAll(items);
                adapter.notifyDataSetChanged();
                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    @Override
    public void onResponse(String response) {

    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mNetworkManager!=null){
            mNetworkManager.cancle(this);
            mNetworkManager=null;
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
