package com.vgtech.vancloud.ui.web;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.InvestigationRecords;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.InvestigationRecordsAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by code on 2016/10/14.
 * 调查记录
 */
public class InvestigationRecordsActivity extends BaseActivity implements HttpListener<String>, AbsListView.OnScrollListener, View.OnClickListener {

    private final int GET_RECORDS_LIST = 1;
    private PullToRefreshListView listView;
    private NetworkManager mNetworkManager;
    private InvestigationRecordsAdapter recordsAdapter;

    private boolean mSafe;
    private String mNextId;
    private boolean mHasData;
    private String mLastId;
    private View mWaitView;
    private EditText keywordEd;
    private ImageButton btn_back;
    private ImageButton btn_right;
    private TextView tv_search;
    private RelativeLayout search_layout;

    private boolean isRefresh = false;
    private String firstPageEndId = null;

    private String keyword;
    private RelativeLayout mDefaultlayout;
    private boolean enableScroll = true;

    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;

    private boolean isShowLoad = true;

    @Override
    protected int getContentView() {
        return R.layout.activity_investigation_records;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        loadInfo(mNextId, "", false);
        setListener();
    }

    public void initView() {
        listView = (PullToRefreshListView) findViewById(R.id.listview);
        recordsAdapter = new InvestigationRecordsAdapter(this);
        listView.setAdapter(recordsAdapter);
        mWaitView = getLayoutInflater().inflate(R.layout.progress, null);
        keywordEd = (EditText) findViewById(R.id.serch_context);
        keywordEd.addTextChangedListener(textWatcher);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        btn_right = (ImageButton) findViewById(R.id.btn_right);
        btn_right.setOnClickListener(this);
        tv_search = (TextView) findViewById(R.id.tv_search);
        tv_search.setOnClickListener(this);
        search_layout = (RelativeLayout) findViewById(R.id.search_layout);
        mDefaultlayout = (RelativeLayout) findViewById(R.id.default_layout);
        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_right:
                btn_right.setVisibility(View.GONE);
                search_layout.setVisibility(View.VISIBLE);
                tv_search.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_search:
                btn_right.setVisibility(View.VISIBLE);
                search_layout.setVisibility(View.GONE);
                tv_search.setVisibility(View.GONE);
                break;
            default:
                super.onClick(v);
                break;

        }
    }

    private void setListener() {
        listView.setOnScrollListener(this);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isRefresh = true;
                mLastId = "0";
                mNextId = "0";
                keyword = null;
                isShowLoad = false;
                loadInfo(mNextId, "", true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(keywordEd.getWindowToken(), 0);
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            keyword = keywordEd.getText().toString();
            if (!TextUtils.isEmpty(keyword)) {
                searchRequest();
            }
        }
    };

    public void searchRequest() {
        enableScroll = false;
        loadInfo(mNextId, keyword, false);
    }

    //网络请求
    private void loadInfo(String nextId, String keyword, boolean refresh) {
        if (isShowLoad)
            showLoadingView();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantId", PrfUtils.getTenantId(this));
        params.put("userId", PrfUtils.getUserId(this));
        if (!TextUtils.isEmpty(keyword)) {
            params.put("search", keyword);
        }
        params.put("pageSize", "12");
        if (!TextUtils.isEmpty(nextId) && !isRefresh)
            params.put("pageNo", nextId);
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_INVESTIGATES_RECORDS), params, this);
        mNetworkManager.load(GET_RECORDS_LIST, path, this, (TextUtils.isEmpty(nextId) || "0".equals(nextId)) && !refresh);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (isShowLoad)
            hideLoadingView();
        isShowLoad = true;
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            hideLoadingView();
            listView.onRefreshComplete();
            return;
        }
        switch (callbackId) {
            case GET_RECORDS_LIST:
                mWaitView.setVisibility(View.GONE);
                List<InvestigationRecords> recordses = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mNextId = jsonObject.getString("pageNo");
                    if (firstPageEndId == null)
                        firstPageEndId = mNextId;
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    recordses = JsonDataFactory.getDataArray(InvestigationRecords.class, jsonObject.getJSONArray("rows"));
                    if(recordses.size()<12)
                    {
                        mHasData = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (recordsAdapter != null) {
                    String s = path.getPostValues().get("pageNo");
                    if (isRefresh || "0".equals(s) || TextUtils.isEmpty(s)) {
                        recordsAdapter.clear();
                        listView.onRefreshComplete();
                        isRefresh = false;
                    }
                    recordsAdapter.addAllData(recordses);
                    recordsAdapter.notifyDataSetChanged();
                    enableScroll = true;
                }
                if (recordses != null && recordses.size() > 0) {
                    mDefaultlayout.setVisibility(View.GONE);
                } else {
                    mDefaultlayout.setVisibility(View.VISIBLE);
                    return;
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
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        boolean flag = false;
        if (!TextUtils.isEmpty(mLastId)) {
            if (mLastId.equals(mNextId))
                flag = true;
        }
        if (enableScroll && !flag && mSafe && mHasData
                && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            isShowLoad = false;
            if (!TextUtils.isEmpty(keyword)) {
                loadInfo(mNextId, keyword, false);
            } else {
                loadInfo(mNextId, "", true);
            }

        }

    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

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
