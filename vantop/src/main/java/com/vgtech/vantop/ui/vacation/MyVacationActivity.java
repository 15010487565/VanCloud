package com.vgtech.vantop.ui.vacation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.MyVacationsAdapter;
import com.vgtech.vantop.moudle.Vacations;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的假期
 * Created by Duke on 2016/9/12.
 */
public class MyVacationActivity extends BaseActivity implements HttpListener<String>, PullToRefreshBase.OnRefreshListener2 {

    PullToRefreshListView listView;
    public VancloudLoadingLayout loadingLayout;
    private NetworkManager mNetworkManager;

    private final int CALLBACK_LIST = 1;

    private MyVacationsAdapter adapter;

    private boolean showError = true;

    @Override
    protected int getContentView() {
        return R.layout.my_vacation_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.my_vacation));

        mNetworkManager = getApplicationProxy().getNetworkManager();

        initView();
        initData();
    }

    public void initView() {
        listView = (PullToRefreshListView) findViewById(R.id.list);
        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        adapter = new MyVacationsAdapter(this, new ArrayList<Vacations>());
        listView.setAdapter(adapter);

        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Vacations vacations = adapter.getList().get(position - listView.getRefreshableView().getHeaderViewsCount());
                Intent intent = new Intent(MyVacationActivity.this, VacationBalanceActivity.class);
                intent.putExtra("data", vacations.getJson().toString());
                startActivity(intent);

            }
        });

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
    }

    public void initData() {
        loadingLayout.showLoadingView(listView, "", true);
        getData();
    }

    public void getData() {
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(MyVacationActivity.this, UrlAddr.URL_VACATIONS), null, this, true);
        mNetworkManager.load(CALLBACK_LIST, path, MyVacationActivity.this, false);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        loadingLayout.dismiss(listView);
        listView.onRefreshComplete();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (showError)
                loadingLayout.showErrorView(listView);
            else {
                showError = true;
                if (adapter.getList().size() <= 0) {
                    loadingLayout.showErrorView(listView);
                }
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_LIST:

                if (!showError)
                    showError = true;
                List<Vacations> vacationses = new ArrayList<Vacations>();
                try {
                    vacationses = JsonDataFactory.getDataArray(Vacations.class, rootData.getJson().getJSONArray("data"));
                    Log.e("TAG_加班签卡","vacationses="+vacationses);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter == null) {
                    adapter = new MyVacationsAdapter(this, vacationses);
                    listView.setAdapter(adapter);
                } else {
                    adapter.myNotifyDataSetChanged(vacationses);
                }
                if (adapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
                    listView.setVisibility(View.VISIBLE);
                }
                break;

            default:

                break;

        }


    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        showError = false;
        getData();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        showError = false;
        getData();
    }
}
