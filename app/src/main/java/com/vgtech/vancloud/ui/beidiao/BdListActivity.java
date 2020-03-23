package com.vgtech.vancloud.ui.beidiao;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.InvestigationRecords;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.InvestigationRecordsAdapter;
import com.vgtech.vancloud.ui.web.BjdcWebActivity;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by vic on 2016/10/17.
 */
public class BdListActivity extends BaseActivity implements HttpListener<String>, AdapterView.OnItemClickListener {

    private static final int CALLBACK_LIST = 1;
    private PullToRefreshListView listView;


    private VancloudLoadingLayout loadingLayout;

    private InvestigationRecordsAdapter mRecordAdapter;
    private View searchBtn;
    private TextView mSearchTv;

    private String mOriUrl;

    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.beidiao, AppPermission.Beidiao.my.toString())) {
            setTitle("我的调查记录");
            mOriUrl = URLAddr.URL_PERSONAL_LIST;
        } else if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.beidiao, AppPermission.Beidiao.all.toString())) {
            setTitle("调查记录");
            mOriUrl = URLAddr.URL_INVESTIGATES_RECORDS;
        }
        listView = (PullToRefreshListView) findViewById(R.id.listview);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                load(1, false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (mHasData) {
                    load(mPageNo + 1, false);
                } else {
                    loadingLayout.dismiss(listView);
                }
            }
        });
        listView.setOnItemClickListener(this);
        mRecordAdapter = new InvestigationRecordsAdapter(this);
        listView.setAdapter(mRecordAdapter);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        searchBtn = findViewById(R.id.btn_action_search);
        searchBtn.setOnClickListener(this);
        load(1, true);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                load(1, true, key);
            }
        });
    }

    @Override
    public void finish() {
        closeSearch();
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_action_search: {
                LinearLayout btn_back = (LinearLayout) findViewById(R.id.btn_back);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(0, 0, 0, 0);
                btn_back.setLayoutParams(lp);
                getTitleTv().setVisibility(View.GONE);
                searchBtn.setVisibility(View.GONE);
                final SearchView searchView = (SearchView) findViewById(R.id.searchview);
                searchView.setVisibility(View.VISIBLE);
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
                        closeSearch();
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

                        if (!TextUtils.isEmpty(s)) {
                            load(1, true, s);
                        }
                        return false;
                    }
                });
            }
            break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void closeSearch() {
        if (mSearchTv != null) {
            mRecordAdapter.clear();
            load(1, true);
            final SearchView searchView = (SearchView) findViewById(R.id.searchview);
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(
                    mSearchTv.getWindowToken(), 0);
            searchView.setVisibility(View.GONE);
            searchBtn.setVisibility(View.VISIBLE);
            getTitleTv().setVisibility(View.VISIBLE);
            LinearLayout btn_back = (LinearLayout) findViewById(R.id.btn_back);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, Utils.convertDipOrPx(this, 50), 0);
            btn_back.setLayoutParams(lp);
            mSearchTv = null;
        }

    }

    private void load(int pageNo, boolean show) {
        load(pageNo, show, null);
    }

    private void load(int pageNo, boolean show, String keyword) {
        key = keyword;
        if (show)
            loadingLayout.showLoadingView(listView, "", true);
        getAppliction().getNetworkManager().cancle(this);
        String url = ApiUtils.generatorUrl(this, mOriUrl);
        Uri uri = Uri.parse(url).buildUpon().appendQueryParameter("tenantId", PrfUtils.getTenantId(this))
                .appendQueryParameter("userId", PrfUtils.getUserId(this))
                .appendQueryParameter("pageSize", "20")
                .appendQueryParameter("pageNo", String.valueOf(pageNo)).build();
        if (!TextUtils.isEmpty(keyword))
            uri = uri.buildUpon().appendQueryParameter("search", keyword).build();
        NetworkPath path = new NetworkPath(uri.toString());
        getAppliction().getNetworkManager().load(CALLBACK_LIST, path, this);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_bdlist;
    }

    private boolean mHasData;
    private int mPageNo;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listView);
        listView.onNewRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (mRecordAdapter.getData().size() == 0) {
                loadingLayout.showErrorView(listView);
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_LIST:
                try {
                    mPageNo = Integer.parseInt(Uri.parse(path.getUrl()).getQueryParameter("pageNo"));
                    if (mPageNo == 1)
                        mRecordAdapter.clear();
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    JSONArray jsonArray = jsonObject.getJSONArray("rows");
                    int total = jsonObject.getInt("total");
                    int pageSize = jsonObject.getInt("pageSize");
                    if (mPageNo * pageSize <= total) {
                        mHasData = true;
                        listView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else {
                        mHasData = false;
                        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    List<InvestigationRecords> recordses = JsonDataFactory.getDataArray(InvestigationRecords.class, jsonArray);
                    mRecordAdapter.addAllDataAndNorify(recordses);
                    if (mRecordAdapter.getCount() == 0) {
                        loadingLayout.showEmptyView(listView, getString(R.string.no_list_data), true, true);
                        listView.setVisibility(View.VISIBLE);
                    } else {
                    }
                } catch (JSONException e) {
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof InvestigationRecords) {
            InvestigationRecords records = (InvestigationRecords) obj;
            Uri uri = Uri.parse(ApiUtils.generatorUrl(this, URLAddr.URL_INVESTIGATES)).buildUpon()
                    .appendQueryParameter("userId", PrfUtils.getUserId(this))
                    .appendQueryParameter("tenantId", PrfUtils.getTenantId(this))
                    .appendQueryParameter("orderInfoId", records.orderInfoId).build();
            Intent intent = new Intent(this, BjdcWebActivity.class);
            intent.putExtra("title", "调查详情");
            intent.putExtra("info", true);
            intent.putExtra("url", uri.toString());
            startActivity(intent);
        }
    }
}
