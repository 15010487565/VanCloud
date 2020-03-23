package com.vgtech.vancloud.ui.worklog;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.WorkLogBean;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.WorkLogAdapter;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data:  2018/8/2
 * Auther: 陈占洋
 * Description:
 */

public class WorkLogSearchActivity extends SearchBaseActivity implements HttpListener<String>, PullToRefreshBase.OnRefreshListener2 {
    private static final int CALLBACK_MINE_SEARCH_WORK_LOG_LIST = 0;
    private static final int CALLBACK_SUB_SEARCH_WORK_LOG_LIST = 1;
    private static final int MINE_TYPE = 0x01;
    private static final int SUB_TYPE = 0x02;
    private static final int SUB_TYPE_SORT_STAFF = 0x03;
    private int mTotalPage = 1;
    private int mPageNum = 1;
    private boolean mIsInit = true;
    private boolean mIsPullRefresh = false;
    private WorkLogAdapter mMineSearchAdapter;
    private String mSearchKeyWord;
    private String mSearchStartDate;
    private String mSearchEndDate;
    private NetworkManager mNetworkManager;
    private int mType;
    private LinearLayout mLLSort;
    private RadioGroup mSortRg;
    private PullToRefreshListView mListView;
    private VancloudLoadingLayout mLoadingView;
    private String sortFiled = "dates";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_log_search);

        mType = getIntent().getIntExtra("type", 0x1);
        mNetworkManager = getAppliction().getNetworkManager();

        initView();
        initData();
    }


    private void initView() {
        initTitleLayout();
        findViewById(R.id.search).setVisibility(View.VISIBLE);
        onClick(findViewById(R.id.search));

        mLLSort = (LinearLayout) findViewById(R.id.search_sort);

        mSortRg = (RadioGroup) findViewById(R.id.search_sort_rg);

        mListView = (PullToRefreshListView) findViewById(R.id.work_log_search_listview);
        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.work_log_search_loading_layout);
        mMineSearchAdapter = new WorkLogAdapter(new ArrayList<WorkLogBean>());
        if (mType == SUB_TYPE) {
            mLLSort.setVisibility(View.VISIBLE);
            mMineSearchAdapter.setType(SUB_TYPE);
            mListView.setAdapter(mMineSearchAdapter);
        } else {
            mMineSearchAdapter.setType(MINE_TYPE);
            mListView.setAdapter(mMineSearchAdapter);
        }
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);

        mSortRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.search_sort_time:
                        sortFiled = "dates";
                        break;

                    case R.id.search_sort_staff:
                        sortFiled = "staffNo";
                        break;
                }
            }
        });
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                if (((TextView) v).getText().equals(getString(R.string.search))) {
                    searchRequest();
                } else {
                    finish();
                }
                return;
        }
        super.onClick(v);
    }

    @Override
    protected void doSearch() {
        hideAdvancedSearchLayout();
        hideKeyboard();
        searchRequest();
    }

    @Override
    public void searchRequest() {
        super.searchRequest();
        //关键字
        mSearchKeyWord = serchContextView.getText().toString().trim();
        if (TextUtils.isEmpty(mSearchKeyWord)) {
            return;
        }
        //开始、结束时间
        mSearchStartDate = startTimeView.getText().toString().trim();
        mSearchEndDate = endTimeView.getText().toString().trim();
        if (getString(R.string.no_time).equals(mSearchStartDate)) {
            mSearchStartDate = "";
        }
        if (getString(R.string.no_time).equals(mSearchEndDate)) {
            mSearchEndDate = "";
        }
        getSearchList(mSearchKeyWord, mSearchStartDate, mSearchEndDate, 1, true, false);
    }

    private void getSearchList(String keyWord, String startDate, String endDate, int pageNum, boolean isInit, boolean isPullRefresh) {
        mIsInit = isInit;
        mIsPullRefresh = isPullRefresh;
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("keyWord", keyWord);
        params.put("page_size", "10");
        params.put("page_now", pageNum + "");
        if (mType == SUB_TYPE) {
            params.put("sortField", sortFiled);
            NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_SUB_SEARCH_WORK_LOG_LIST), params, this);
            path.setTag(sortFiled);
            mNetworkManager.load(CALLBACK_SUB_SEARCH_WORK_LOG_LIST, path, this);
        } else {
            NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_MINE_SEARCH_WORK_LOG_LIST), params, this);
            mNetworkManager.load(CALLBACK_MINE_SEARCH_WORK_LOG_LIST, path, this);
        }
        if (!mIsPullRefresh)
            mLoadingView.showLoadingView(mListView, "", true);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListView);
        mListView.onRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, false);
        if (!safe) {
            Toast.makeText(this, rootData.msg, Toast.LENGTH_SHORT).show();
            return;
        }
        switch (callbackId) {
            case CALLBACK_MINE_SEARCH_WORK_LOG_LIST:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    mTotalPage = resutObject.getInt("pageCount");
                    mPageNum = resutObject.getInt("pageNo");
                    JSONArray rows = resutObject.getJSONArray("rows");
                    mMineSearchAdapter.setType(MINE_TYPE);
                    List<WorkLogBean> mineWorkLogData = JsonDataFactory.getDataArray(WorkLogBean.class, rows);
                    if (mineWorkLogData.size() > 0) {
                        if (mIsInit) {
                            mMineSearchAdapter.setData(mineWorkLogData);
                        } else {
                            mMineSearchAdapter.addData(mineWorkLogData);
                        }
                    } else {
                        if (mIsInit) {
                            //清空数据
                            mLoadingView.showEmptyView(mListView, getString(R.string.no_list_data), true, true);
                            mMineSearchAdapter.setData(null);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case CALLBACK_SUB_SEARCH_WORK_LOG_LIST:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    mTotalPage = resutObject.getInt("pageCount");
                    mPageNum = resutObject.getInt("pageNo");
                    JSONArray rows = resutObject.getJSONArray("rows");
                    List<WorkLogBean> mineWorkLogData = JsonDataFactory.getDataArray(WorkLogBean.class, rows);
                    mMineSearchAdapter.setType(SUB_TYPE);
                    if (mineWorkLogData.size() > 0) {
                        if (mIsInit) {
                            String sf = (String) path.getTag();
                            if ("staffNo".equals(sf)) {
                                //按员工排序
                                mMineSearchAdapter.setType(SUB_TYPE_SORT_STAFF);
                                List<WorkLogBean> sortWorkLogData = sortWorkLog(mineWorkLogData);
                                mMineSearchAdapter.setData(sortWorkLogData);
                            } else {
                                //按时间排序
                                mMineSearchAdapter.setData(mineWorkLogData);
                            }
                        } else {
                            mMineSearchAdapter.addData(mineWorkLogData);
                        }
                    } else {
                        if (mIsInit) {
                            //清空数据
                            mLoadingView.showEmptyView(mListView, getString(R.string.no_list_data), true, true);
                            mMineSearchAdapter.setData(null);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private List<WorkLogBean> sortWorkLog(List<WorkLogBean> mineWorkLogData) {
        String tempStaffNo = "";
        for (int i = 0; i < mineWorkLogData.size(); i++) {
            WorkLogBean workLogBean = mineWorkLogData.get(i);
            if (!tempStaffNo.equals(workLogBean.getStaffNo())) {
                workLogBean.setOnlyStaff(true);
                tempStaffNo=workLogBean.getStaffNo();
            } else {
                workLogBean.setOnlyStaff(false);
            }
        }
        return mineWorkLogData;
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        getSearchList(mSearchKeyWord, mSearchStartDate, mSearchEndDate, 1, true, true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        if (mPageNum + 1 > mTotalPage) {
            //TODO 国际化
            Toast.makeText(this, "最后一页，没有数据了", Toast.LENGTH_SHORT).show();
            mListView.onRefreshComplete();
            return;
        }
        getSearchList(mSearchKeyWord, mSearchStartDate, mSearchEndDate, ++mPageNum, false, true);
    }
}
