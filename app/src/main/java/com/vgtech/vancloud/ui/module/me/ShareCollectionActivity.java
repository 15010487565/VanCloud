package com.vgtech.vancloud.ui.module.me;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.ShareCollectionAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by code on 2015-10-15
 * 分享收藏界面
 */
public class ShareCollectionActivity extends BaseActivity implements HttpListener<String>, AbsListView.OnScrollListener, ShareCollectionAdapter.OnSelectListener {

    @InjectView(R.id.tv_right_edit)
    TextView tvRightEdit;
    @InjectView(R.id.listview)
    PullToRefreshListView listview;
    @InjectView(R.id.tv_right_uncollect)
    TextView tvRightUncollect;

    private final int GET_SHARECOLLECTION_LIST = 1;
    private final int GET_SHARECOLLECTION_UNCOLLECT = 2;
    private ShareCollectionAdapter adapter;
    private NetworkManager mNetworkManager;
    private boolean isListViewRefresh = false;
    private Boolean loading = true;
    private int n = 10;
    private String nextId = "0";
    private String mLastId = "0";
    private boolean mSafe;
    private boolean mHasData;
    private HashMap<String, SharedListItem> mIndexer;

    private VancloudLoadingLayout loadingLayout;

    @Override
    protected int getContentView() {
        return R.layout.share_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        initData();
        initEvent();
    }

    private void initData() {
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);

        adapter = new ShareCollectionAdapter(this, new ArrayList<SharedListItem>());
        mIndexer = new HashMap<String, SharedListItem>();
        adapter.setOnSelectListener(this);
        listview.setAdapter(adapter);
        initDate("1");
    }

    private void initEvent() {
        listview.setOnScrollListener(this);
        listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isListViewRefresh = true;
                nextId = "0";
                initDate("1");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                isListViewRefresh = false;
                nextId = "0";
                loading = true;
                mLastId = "0";
                initDate("1");
            }
        });
    }

    private void initDate(String type) {
        if (loading) {
            loadingLayout.showLoadingView(listview, "", true);
        }
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("type", type);
        params.put("n", n + "");
        if (!TextUtils.isEmpty(nextId) && !isListViewRefresh) {
            params.put("s", nextId);
        } else {
            params.put("s", "0");
        }
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SHARED_LIST), params, this);
        mNetworkManager.load(GET_SHARECOLLECTION_LIST, path, this);
    }

    /**
     * 取消收藏
     */
    private void UnCollectAction(String topicid) {
        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("topicid", topicid);
        params.put("type", "2");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SHARED_COLLECTION), params, this);
        mNetworkManager.load(GET_SHARECOLLECTION_UNCOLLECT, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listview);
        listview.onRefreshComplete();

        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            if (callbackId == GET_SHARECOLLECTION_LIST && adapter.getMlist().size() == 0) {
                loadingLayout.showErrorView(listview);
                tvRightEdit.setVisibility(View.GONE);
                tvRightUncollect.setVisibility(View.GONE);
            }
            return;
        }
        switch (callbackId) {

            case GET_SHARECOLLECTION_LIST:

                List<SharedListItem> shareCollectionBeans = new ArrayList<SharedListItem>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    nextId = resutObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(nextId) && !"0".equals(nextId);
                    shareCollectionBeans = JsonDataFactory.getDataArray(SharedListItem.class, resutObject.getJSONArray("rows"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (shareCollectionBeans != null && shareCollectionBeans.size() > 0) {
                    tvRightEdit.setVisibility(View.VISIBLE);
                    tvRightUncollect.setVisibility(View.GONE);
                    adapter.setIsSelect(false);
                    mIndexer.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    loadingLayout.showEmptyView(listview, getString(R.string.no_info_list), true, true);
                    tvRightEdit.setVisibility(View.GONE);
                    tvRightUncollect.setVisibility(View.GONE);
                }
                if (adapter == null) {
                    adapter = new ShareCollectionAdapter(this, shareCollectionBeans);
                    listview.setAdapter(adapter);
                } else {
                    String page = path.getPostValues().get("s");
                    if ("0".equals(page)) {
                        isListViewRefresh = true;
                    }
                    if (isListViewRefresh) {
                        adapter.clear();
                        isListViewRefresh = false;
                    }
                    List<SharedListItem> list = adapter.getMlist();
                    list.addAll(shareCollectionBeans);
                    adapter.myNotifyDataSetChanged(list);
                }
                loading = false;
                break;

            case GET_SHARECOLLECTION_UNCOLLECT:
                dismisLoadingDialog();
                isListViewRefresh = true;
                nextId = "0";
                initDate("1");
                showToast(getString(R.string.help_uncollect_success));
                adapter.setIsSelect(false);
                mIndexer.clear();
                tvRightEdit.setVisibility(View.VISIBLE);
                tvRightUncollect.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                break;

            default:
                break;
        }
    }

    /*获取选中的topicId*/
    private String getKeys(HashMap<String, SharedListItem> map) {
        StringBuffer buffer = new StringBuffer();

        Set<String> keySet = map.keySet();
        for (String keyName : keySet) {
            SharedListItem item = map.get(keyName);
            buffer.append(item.topicId).append(",");
        }
        if (buffer.toString().length() > 1) {
            return buffer.toString().substring(0, buffer.toString().length() - 1);
        } else {
            return buffer.toString().substring(0, buffer.toString().length());
        }
    }

    @OnClick(R.id.tv_right_edit)
    public void editAction() {
        tvRightEdit.setVisibility(View.GONE);
        tvRightUncollect.setVisibility(View.VISIBLE);
        adapter.setIsSelect(true);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.tv_right_uncollect)
    public void unCollectAction() {
        if (mIndexer.size() == 0) {
            showToast(getString(R.string.choose_help_uncollect));
        } else {
            AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.edit_department_title)).setMsg(getString(R.string.ischoose_help_uncollect));
            alertDialog.setPositiveButton(getString(R.string.yes), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UnCollectAction(getKeys(mIndexer));
                }
            });
            alertDialog.setNegativeButton(getString(R.string.no), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvRightEdit.setVisibility(View.VISIBLE);
                    tvRightUncollect.setVisibility(View.GONE);
                    adapter.setIsSelect(false);
                    mIndexer.clear();
                    adapter.notifyDataSetChanged();
                }
            });
            alertDialog.show();
        }
    }

    @OnClick(R.id.top_type_click)
    public void goBack() {
        finish();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void OnSelected(SharedListItem item) {
        mIndexer.put(item.topicId, item);
    }

    @Override
    public void OnUnSelected(SharedListItem item) {
        mIndexer.remove(item.topicId);
    }

    @Override
    public boolean OnIsSelect(SharedListItem item) {
        return mIndexer.containsKey(item.topicId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setIsSelect(false);
        mIndexer.clear();
        // tvRightEdit.setVisibility(View.VISIBLE);
        // tvRightUncollect.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        isListViewRefresh = true;
        nextId = "0";
        initDate("1");
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
            initDate("1");
        }
    }
}
