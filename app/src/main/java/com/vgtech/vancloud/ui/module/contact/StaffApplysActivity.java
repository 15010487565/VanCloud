package com.vgtech.vancloud.ui.module.contact;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.StaffApplyItem;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuCreator;
import com.vgtech.common.view.swipemenu.SwipeMenuItem;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.StaffApplyAdapter;
import com.vgtech.vancloud.ui.module.me.SelfInfoActivity;
import com.vgtech.vancloud.ui.view.pullswipemenulistview.MySwipeMenuListView;
import com.vgtech.vancloud.ui.view.pullswipemenulistview.PullToRefreshSwipeMenuListView;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brook on 2016/9/06.
 * Version : 1
 * Details :
 */
public class StaffApplysActivity extends BaseActivity implements View.OnClickListener, HttpListener<String>, AbsListView.OnScrollListener {

    private static final int CALL_BACK_STAFFAPPLYS = 1;
    private final int DELETE_APPLY = 2;
    private final String DELETE = "delete";
    private NetworkManager mNetworkManager;
    private boolean isShowLoad = true;
    private boolean isRefresh = false;
    private VancloudLoadingLayout loadingLayout;
    private PullToRefreshSwipeMenuListView listView;
    private String mNextId;
    private StaffApplyAdapter adapter;
    private boolean mSafe;
    private boolean mHasData;
    private String mLastId;
    private int mPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRefresh = true;
        mLastId = "0";
        mNextId = "0";
        isShowLoad = true;
        loadApplyInfo(mNextId, true);
    }

    private void initView() {
        setTitle(R.string.staff_apply);
        listView = (PullToRefreshSwipeMenuListView) findViewById(R.id.listView);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                isRefresh = true;
                mLastId = "0";
                mNextId = "0";
                isShowLoad = true;
                loadApplyInfo(mNextId, true);
            }
        });
    }

    private void initData() {
        mNetworkManager = getAppliction().getNetworkManager();
        adapter = new StaffApplyAdapter(this);
        listView.setAdapter(adapter);
        listView.getRefreshableView().setMenuCreator(creator);
        listView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    StaffApplyItem item = adapter.dataSource.get(position-1);
                    if ("refused".equals(item.state) || "pending".equals(item.state)) {
                        Intent intent = new Intent(StaffApplysActivity.this, StaffApprovalActivity.class);
                        intent.putExtra("topicId", item.invite_id);
                        intent.putExtra("number", item.mobile);
                        intent.putExtra("user_name", item.name);
                        intent.putExtra("state", item.state);
                        startActivity(intent);
                    } else if ("agreed".equals(item.state)) {
                        Intent intent = new Intent(StaffApplysActivity.this, SelfInfoActivity.class);
                        intent.putExtra("userId", item.user_id);
                        intent.putExtra("type", "0");
                        startActivityForResult(intent, 1002);
                    }
                }
            }
        });

        listView.getRefreshableView().setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu,
                                           int index) {
                switch (index) {
                    case 0: {
                        mPosition = position;
                        delItem(adapter.dataSource.get(position));
                    }
                    break;
                }
                return false;
            }
        });
        setListener();
    }

    private void delItem(StaffApplyItem staffApplyItem) {
        showLoadingDialog(this, "", false);
        Map<String, String> params = new HashMap<String, String>();
        params.put("invite_id", staffApplyItem.invite_id);
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("option", DELETE);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_INVITE_OPTION), params, this);
        mNetworkManager.load(DELETE_APPLY, path, this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_staff_apply;
    }


    //网络请求
    private void loadApplyInfo(String nextId, boolean refresh) {
        if (isShowLoad)
            loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("n", "12");
        if (!TextUtils.isEmpty(nextId) && !isRefresh)
            params.put("s", nextId);
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_STAFFAPPLY_LIST), params, this);
        mNetworkManager.load(CALL_BACK_STAFFAPPLYS, path, this, (TextUtils.isEmpty(nextId) || "0".equals(nextId)) && !refresh);
    }


    private void setListener() {
        listView.setOnScrollListener(this);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<MySwipeMenuListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<MySwipeMenuListView> refreshView) {
                isRefresh = true;
                mLastId = "0";
                mNextId = "0";
                isShowLoad = false;
                loadApplyInfo(mNextId, true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<MySwipeMenuListView> refreshView) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
        super.onClick(v);
    }

    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu, int position) {
            SwipeMenuItem openItem = new SwipeMenuItem(StaffApplysActivity.this);
            openItem.setBackground(new ColorDrawable(Color.rgb(0xff, 0x00,
                    0x00)));
            openItem.setWidth(Utils.dp2px(StaffApplysActivity.this, 90));
            openItem.setTitle(getString(R.string.delete));
            openItem.setTitleSize(18);
            openItem.setTitleColor(Color.WHITE);
            menu.addMenuItem(openItem);
        }
    };

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (isShowLoad)
            loadingLayout.dismiss(listView);
        isShowLoad = true;
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            loadingLayout.showErrorView(listView, "", true, true);
            listView.onRefreshComplete();
            return;
        }
        switch (callbackId) {
            case CALL_BACK_STAFFAPPLYS:
                List<StaffApplyItem> items = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mNextId = jsonObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    items = JsonDataFactory.getDataArray(StaffApplyItem.class, jsonObject.getJSONArray("rows"));
                    if (items.size() < 12) {
                        mHasData = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter != null) {
                    String s = path.getPostValues().get("s");
                    if (isRefresh || "0".equals(s) || TextUtils.isEmpty(s)) {
                        adapter.dataSource.clear();
                        listView.onRefreshComplete();
                        isRefresh = false;
                    }
                    adapter.dataSource.addAll(items);
                    if (adapter.dataSource != null && adapter.dataSource.size() == 0) {
                        loadingLayout.showEmptyView(listView, getString(R.string.no_list_data), true, true);
                    } else {
                        loadingLayout.dismiss(listView);
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
            case DELETE_APPLY:
                try {
                    dismisLoadingDialog();
                    if (rootData.result) {
                        Toast.makeText(this, this.getString(R.string.shared_delete_success), Toast.LENGTH_SHORT).show();
                        if (mPosition != -1) {
                            adapter.dataSource.remove(mPosition);
                            adapter.notifyDataSetChanged();
                        }
                    } else
                        Toast.makeText(this, this.getString(R.string.shared_delete_fail), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            if (mLastId.equals(mNextId))
                flag = true;
        }
        if (!flag && mSafe && mHasData
                && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            isShowLoad = false;
            loadApplyInfo(mNextId, true);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null) {
            mNetworkManager.cancle(this);
            mNetworkManager = null;
        }
    }
}