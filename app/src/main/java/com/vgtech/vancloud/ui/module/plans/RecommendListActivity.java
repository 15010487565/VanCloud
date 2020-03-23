package com.vgtech.vancloud.ui.module.plans;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RecommendListBean;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.RecommendListAdapter;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by code on 2015/11/5.
 * 推荐记录
 */
public class RecommendListActivity extends BaseActivity implements HttpListener<String>, AbsListView.OnScrollListener {

    @InjectView(R.id.recommend_tv)
    TextView recommendTv;
    @InjectView(R.id.using_tv)
    TextView usingTv;
    @InjectView(R.id.title_write_layout)
    RelativeLayout titleLayout;
    @InjectView(R.id.listview)
    PullToRefreshListView listview;

    private final int RECOMMENDEDRECORDLIST = 1;
    private NetworkManager mNetworkManager;
    private boolean isListViewRefresh = false;
    private Boolean isloading = true;
    private int n = 10;
    private String nextId = "0";
    private String mLastId = "0";
    private boolean mSafe;
    private boolean mHasData;
    private RecommendListAdapter adapter;

    private VancloudLoadingLayout loadingLayout;

    @Override
    protected int getContentView() {
        return R.layout.activity_recommendlist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.recommend_record));
        ButterKnife.inject(this);
        initData();
        initEvent();
    }

    private void initData() {
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        adapter = new RecommendListAdapter(this, new ArrayList<RecommendListBean>());
        listview.setAdapter(adapter);
        initDate();

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                isloading = true;
                isListViewRefresh = false;
                nextId = "0";
                mLastId = "0";
                initData();
            }
        });

    }

    private void initEvent() {
        listview.setOnScrollListener(this);
        listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isListViewRefresh = true;
                nextId = "0";
                initDate();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    private void initDate() {
        if (isloading) {
            loadingLayout.showLoadingView(listview, "", true);
        }
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("mobile", PrfUtils.getUserName(this));
        params.put("usertype", "tenant");
        params.put("endnumber", n + "");
        if (!TextUtils.isEmpty(nextId) && !isListViewRefresh) {
            params.put("startnumber", nextId);
        } else {
            params.put("startnumber", "0");
        }
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_REFEREE_RECORDS), params, this);
        mNetworkManager.load(RECOMMENDEDRECORDLIST, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listview);
        listview.onRefreshComplete();
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            if (adapter.getMlist().size() == 0) {
                loadingLayout.showErrorView(listview);
            }
            return;
        }
        switch (callbackId) {
            case RECOMMENDEDRECORDLIST:
                List<RecommendListBean> recommendListBeans = new ArrayList<RecommendListBean>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    nextId = resutObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(nextId) && !"0".equals(nextId);
                    String totle = resutObject.getString("totle");
                    String count = resutObject.getString("count");
                    if (!TextUtils.isEmpty(count)) {
                        recommendTv.setText(Utils.format(getString(R.string.vancloud_successful_recommendation), count));
                    }
                    if (!TextUtils.isEmpty(count)) {
                        usingTv.setText(Utils.format(getString(R.string.vancloud_amount_obtained), totle));
                    }
                    recommendListBeans = JsonDataFactory.getDataArray(RecommendListBean.class, resutObject.getJSONArray("records"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (recommendListBeans != null && recommendListBeans.size() > 0) {
                    titleLayout.setVisibility(View.VISIBLE);
                } else {
                    titleLayout.setVisibility(View.GONE);
                    loadingLayout.showEmptyView(listview, getString(R.string.no_recommend_detail), true, true);
                }
                if (adapter == null) {
                    adapter = new RecommendListAdapter(this, recommendListBeans);
                    listview.setAdapter(adapter);
                } else {
                    String page = path.getPostValues().get("startnumber");
                    if ("0".equals(page)) {
                        isListViewRefresh = true;
                    }
                    if (isListViewRefresh) {
                        adapter.clear();
                        listview.onRefreshComplete();
                        isListViewRefresh = false;
                    }
                    List<RecommendListBean> list = adapter.getMlist();
                    list.addAll(recommendListBeans);
                    adapter.myNotifyDataSetChanged(list);
                }
                isloading = false;
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
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean flag = false;
        if (!TextUtils.isEmpty(mLastId)) {
            if (mLastId.equals(nextId))
                flag = true;
        }
        if (!flag && mSafe && mHasData && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            initDate();
        }
    }
}
