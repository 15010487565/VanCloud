package com.vgtech.vancloud.ui.module.schedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.ScheduleItem;
import com.vgtech.common.api.ScheduleMap;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.utils.KeyboardUtil;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.common.record.MediaManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/9/9.
 */
public class ScheduleSearchActivity extends SearchBaseActivity implements HttpListener<String>, AbsListView.OnScrollListener {
    private View mWaitView;
    private NetworkManager mNetworkManager;
    private ApiDataAdapter<ScheduleMap> mScheduleAdapter;
    private ApiDataAdapter<ScheduleMap> mSchedulesubAdapter;
    private RelativeLayout openlayout;
    private NoScrollListview footviewlist;
    private ListView listView;
    private boolean isLoadSubData = false;
    private VancloudLoadingLayout mLoadingLayout;
    private View mBgSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkManager = getAppliction().getNetworkManager();
        mBgSearch = findViewById(R.id.bg_schdule_home);
        mBgSearch.setVisibility(View.GONE);
        listView = (ListView) findViewById(android.R.id.list);
        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mLoadingLayout.showEmptyView(listView, getString(R.string.schedule_search_tip), true, true);
        listView.setVisibility(View.GONE);
        View headerView = getHeadLayout();
        View footerView = getFootViewLayout();
        listView.addFooterView(footerView);
        listView.addHeaderView(headerView);
        listView.setOnScrollListener(this);
        mScheduleAdapter = new ApiDataAdapter<ScheduleMap>(this);
        mScheduleAdapter.setIsMySchedule(true);
        mScheduleAdapter.setDateVisible(true);
        mSchedulesubAdapter = new ApiDataAdapter<ScheduleMap>(this);
        mSchedulesubAdapter.setDateVisible(true);
        listView.setAdapter(mScheduleAdapter);
        footviewlist.setAdapter(mSchedulesubAdapter);
        initTitleLayout();
        onClick(findViewById(R.id.search));

    }

    private static final int CALLBACK_INIT = 1;
    private static final int CALLBACKSUB_INIT = 2;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_schedule_more: {
                Intent intent = new Intent(this, ScheduleFragmentActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_retry:
                retryLoading();
                retry();
                break;
            case R.id.add: {
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE);
                startActivity(intent);
            }
            break;
            case R.id.cancel:
                if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
                    hideAdvancedSearchLayout();
                }
                hideKeyboard();
                KeyboardUtil.hideSoftInput(ScheduleSearchActivity.this);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (cancelView.getText().equals(getString(R.string.search))) {
                            searchRequest();
                        } else {
                            finish();
                        }
                        sendBroadcast(new Intent(Actions.ACTION_SHOW_MORE_VIEW));
                    }
                }, 500);

                break;
            default:
                super.onClick(v);
                break;
        }
    }

    protected void doSearch() {
        hideAdvancedSearchLayout();
        hideKeyboard();
        searchRequest();
    }

    private static final int CALLBACK_NEWS_INIT = 1;
    private static final int CALLBACK_NEWS = 2;
    private int mPageN;
    private String mLastId;
    private String mSubLastId;

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        MediaManager.pause();
    }

    private String mStartTime = "";
    private String mEndTime = "";

//    private void loadData(String nextId) {
//        showProgress(mWaitView, true);
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("ownid", PrfUtils.getUserId(this));
//        params.put("tenantid", PrfUtils.getTenantId(this));
//        params.put("startdate", mStartTime);
//        params.put("enddate", mEndTime);
//        params.put("permission", "1");
//        params.put("n", "10000");
//        if (!TextUtils.isEmpty(nextId))
//            params.put("s", nextId);
//
//        params.put("query", "0");
//        mLastId = nextId;
//        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SCHEDULE_LIST), params, this);
//        mNetworkManager.load(CALLBACK_INIT, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
//    }


    private String mNextId;
    private String mSubNextId;
    private boolean mHasData;
    private boolean mSubHasData;
    private boolean mSafe;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
//        if (!isLoadSubData) {
//            boolean flag = true;
//            if (!TextUtils.isEmpty(mLastId)) {
//                if (!mLastId.equals(mNextId))
//                    flag = false;
//            }
//            if (!flag && mSafe && mHasData
//                    && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
//                loadData(mNextId);
//            }
//        } else {
//            boolean flag = true;
//            if (!TextUtils.isEmpty(mSubLastId)) {
//                if (!mSubLastId.equals(mSubNextId))
//                    flag = false;
//            }
//            if (!flag && mSafe && mSubHasData
//                    && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
//                loadSubData(mNextId);
//            }
//        }


    }

    /**
     * @param arg0
     * @param arg1
     * @Description:
     * @Created:shaofang 2014年6月25日下午3:38:11
     * @Modified:
     */
    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }

    @Override
    protected void retryLoading() {
        showProgress(mWaitView, true);
    }

    @Override
    protected int getContentView() {
        return R.layout.schedule_home;
    }
//
//    private void loadSubData(String nextId) {
//        showProgress(mWaitView, true);
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("ownid", PrfUtils.getUserId(this));
//        params.put("tenantid", PrfUtils.getTenantId(this));
//        params.put("startdate", String.valueOf(mStartTime));
//        params.put("enddate", String.valueOf(mEndTime));
//        params.put("permission", "2");
//        params.put("n", "10000");
//        if (!TextUtils.isEmpty(nextId))
//            params.put("s", nextId);
//        mSubLastId = nextId;
//        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SCHEDULE_LIST), params, this);
//        mNetworkManager.load(CALLBACKSUB_INIT, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
//    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            showProgress(mWaitView, false);
            return;
        }
        switch (callbackId) {
            case CALLBACK_INIT:
                mWaitView.setVisibility(View.GONE);
                try {
                    String lnextId = path.getPostValues().get("s");
                    if (TextUtils.isEmpty(lnextId) || "0".equals(lnextId))
                        mScheduleAdapter.clear();
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    if (isSearch) {
                        mNextId = "0";
                        isSearch = false;
                    } else {
                        mNextId = jsonObject.getString("nextid");
                    }
                    String count = jsonObject.getString("count");
//                    mScheduleStartView.setVisibility("0".equals(count) ? View.GONE : View.VISIBLE);
                    mCountTv.setText(getString(R.string.lable_schedule_count, count));
                    mCountView.setVisibility(View.VISIBLE);
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    Object object = jsonObject.get("rows");
                    List<ScheduleItem> calendarItems = new ArrayList<>();
                    if (object instanceof JSONArray) {
                        calendarItems = JsonDataFactory.getDataArray(ScheduleItem.class, jsonObject.getJSONArray("rows"));
                    }
                    Map<Long, List<ScheduleItem>> map = new LinkedHashMap<Long, List<ScheduleItem>>();
                    for (ScheduleItem scheduleItem : calendarItems) {
                        List<ScheduleItem> scheduleItems = map.get(scheduleItem.starttime);
                        if (scheduleItems == null) {
                            scheduleItems = new ArrayList<>();
                            map.put(scheduleItem.starttime, scheduleItems);
                        }
                        scheduleItems.add(scheduleItem);
                    }
                    List<ScheduleMap> scheduleMaps = new ArrayList<ScheduleMap>();
                    for (Long key : map.keySet()) {
                        scheduleMaps.add(new ScheduleMap(key, map.get(key)));
                    }
                    mScheduleAdapter.add(scheduleMaps);
                    boolean hasPermission = false;
                    if (jsonObject.has("hasPermission")) {
                        hasPermission = jsonObject.getBoolean("hasPermission");
                    }
                    if (mScheduleAdapter.isEmpty() && !hasPermission) {
                        listView.setVisibility(View.GONE);
                        mBgSearch.setVisibility(View.GONE);
                        String s = path.getPostValues().get("keyword");
                        String tip = getString(R.string.tip_search_empty, s);
                        tip = tip.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
                        mLoadingLayout.showHtmlMessage(tip);
                        mLoadingLayout.setVisibility(View.VISIBLE);
                    } else {
                        mLoadingLayout.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        mBgSearch.setVisibility(View.VISIBLE);
                    }
                    openlayout.setVisibility(hasPermission ? View.VISIBLE : View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACKSUB_INIT:
                try {
                    String lnextId = path.getPostValues().get("s");
                    if (TextUtils.isEmpty(lnextId))
                        mSchedulesubAdapter.clear();
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");

                    if (isSearchSubData) {
                        mNextId = "0";
                        isSearchSubData = false;
                    } else {
                        mNextId = jsonObject.getString("nextid");
                    }
                    mSubNextId = jsonObject.getString("nextid");
                    mSubHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    List<ScheduleItem> calendarItems = JsonDataFactory.getDataArray(ScheduleItem.class, jsonObject.getJSONArray("rows"));
                    Map<Long, List<ScheduleItem>> map = new LinkedHashMap<Long, List<ScheduleItem>>();
                    for (ScheduleItem scheduleItem : calendarItems) {
                        List<ScheduleItem> scheduleItems = map.get(scheduleItem.starttime);
                        if (scheduleItems == null) {
                            scheduleItems = new ArrayList<>();
                            map.put(scheduleItem.starttime, scheduleItems);
                        }
                        scheduleItems.add(scheduleItem);
                    }
                    List<ScheduleMap> scheduleMaps = new ArrayList<ScheduleMap>();
                    for (Long key : map.keySet()) {
                        scheduleMaps.add(new ScheduleMap(key, map.get(key)));
                    }
                    mSchedulesubAdapter.clear();
                    mSchedulesubAdapter.add(scheduleMaps);
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

    private boolean mOpenSub;
    private ImageView ic_expend_state;

    public View getFootViewLayout() {
        View footView = getLayoutInflater().inflate(R.layout.schedule_footview_layout, null);
        openlayout = (RelativeLayout) footView.findViewById(R.id.open_layout);
        openlayout.setVisibility(View.GONE);
        footviewlist = (NoScrollListview) footView.findViewById(R.id.sublist);
        ic_expend_state = (ImageView) footView.findViewById(R.id.ic_expend_state);
        openlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenSub = !mOpenSub;
                if (mOpenSub) {
                    ic_expend_state.setImageResource(R.mipmap.ic_expend_open);
                    isLoadSubData = true;
                    mSubNextId = "";
                    searchSubData("0", serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
                    footviewlist.setVisibility(View.VISIBLE);
                    listView.setSelection(listView.getBottom());
                } else {
                    footviewlist.setVisibility(View.GONE);
                    ic_expend_state.setImageResource(R.mipmap.ic_expend_close);
                }

            }
        });
        return footView;
    }

    private TextView mCountTv;
    private View mCountView;

    public View getHeadLayout() {
        View headerView = getLayoutInflater().inflate(R.layout.schedule_header_search, null);
        mWaitView = headerView.findViewById(R.id.progress_view_schedule);
        mWaitView.findViewById(R.id.btn_retry).setOnClickListener(this);
//        mWaitView.setVisibility(View.GONE);
        mCountView = headerView.findViewById(R.id.schedule_count_view);
        mCountTv = (TextView) headerView.findViewById(R.id.tv_schedule_count);
        return headerView;

    }

    boolean isSearch = false;
    boolean isSearchSubData = false;

    @Override
    public void searchRequest() {
        isSearch = true;
        mScheduleAdapter.clear();
        mSchedulesubAdapter.clear();
        mCountView.setVisibility(View.GONE);
        openlayout.setVisibility(View.GONE);
        searchData("0", serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
        if (isLoadSubData) {
            isSearchSubData = true;
            searchSubData("0", serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
        }
    }

    private void searchData(String nextId, String keyword, String sTime, String eTime) {
        mLoadingLayout.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        showProgress(mWaitView, true);
        if (!TextUtils.isEmpty(sTime) && getString(R.string.no_time).equals(sTime))
            sTime = String.valueOf(mStartTime);
        else
            sTime = DateTimeUtil.stringToLong_YMd(sTime) + "";
        if (!TextUtils.isEmpty(eTime) && getString(R.string.no_time).equals(eTime))
            eTime = String.valueOf(mStartTime);
        else
            eTime = DateTimeUtil.stringToLong_YMd(eTime) + "";
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("startdate", sTime);
        params.put("enddate", eTime);
        params.put("permission", "9");
        params.put("n", "1000");
        params.put("s", nextId);
        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SEARCH_CALENDAR), params, this);
        mNetworkManager.load(CALLBACK_INIT, path, this);
    }


    private void searchSubData(String nextId, String keyword, String sTime, String eTime) {
        nextId = "";
        showProgress(mWaitView, true);
        if (!TextUtils.isEmpty(sTime) && getString(R.string.no_time).equals(sTime)) {
            sTime = String.valueOf(mStartTime);
        } else {
            sTime = DateTimeUtil.stringToLong_YMd(sTime) + "";
        }
        if (!TextUtils.isEmpty(eTime) && getString(R.string.no_time).equals(eTime)) {
            eTime = String.valueOf(mStartTime);
        } else {
            eTime = DateTimeUtil.stringToLong_YMd(eTime) + "";
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("startdate", sTime);
        params.put("enddate", eTime);
        params.put("permission", "7");
        params.put("n", "1000");
        if (!TextUtils.isEmpty(nextId))
            params.put("s", nextId);
        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);
        mSubLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SEARCH_CALENDAR), params, this);
        mNetworkManager.load(CALLBACKSUB_INIT, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                int position = data.getIntExtra("position", -1);
                int type = data.getIntExtra("type", -1);
                mScheduleAdapter.chaneScheduleState(position, type);
            }
        }
    }
}
