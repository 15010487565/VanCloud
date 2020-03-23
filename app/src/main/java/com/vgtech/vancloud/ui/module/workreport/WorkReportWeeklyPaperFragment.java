package com.vgtech.vancloud.ui.module.workreport;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.WorkReport;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.CalendarUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.WorkReportAdapter;
import com.vgtech.vancloud.ui.base.LazyLoadFragment;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2015/9/11.
 */
public class WorkReportWeeklyPaperFragment extends LazyLoadFragment implements HttpListener<String> {

    private PullToRefreshListView listView;
    //（1日报，2周报，3月报）
    private int datetype = 2;
    //分页的大小
    private int n = 10;
    private String nextId = "0";
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_LIST = 1;
    private String type = "1";
    private WorkReportAdapter adapter;
    private int listViewRefreshType = 0;
    private static final int CALLBACK_STATISTICS = 2;

    private String startdate;
    private String enddate;
    TextView thisTimeView;
    TextView commitsNumView;
    TextView uncommitsInfoView;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";

    WorkReportNewActivity workReportNewActivity;

    private VancloudLoadingLayout loadingLayout;
    private LinearLayout statisticsLayout;

    @Override
    protected int initLayoutId() {
        return R.layout.workreport_dailypaper_fragmentlayout;
    }

    @Override
    protected void initView(View view) {

        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);

        view.findViewById(R.id.viewpager).setVisibility(View.GONE);

        listView = (PullToRefreshListView) view.findViewById(R.id.listview);
//        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        adapter = new WorkReportAdapter(this, getActivity(), new ArrayList<WorkReport>());
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        View header = getActivity().getLayoutInflater().inflate(R.layout.work_report_statistics_layout, listView, false);
        header.setLayoutParams(layoutParams);
        statisticsLayout = (LinearLayout) header.findViewById(R.id.statistics_layout);
        ListView lv = listView.getRefreshableView();
        lv.addHeaderView(header, null, false);

        listView.setAdapter(adapter);

        thisTimeView = (TextView) header.findViewById(R.id.this_time);
        thisTimeView.setText(format.format(new Date()));
        commitsNumView = (TextView) header.findViewById(R.id.commits_num);
        commitsNumView.setVisibility(View.GONE);
        uncommitsInfoView = (TextView) header.findViewById(R.id.uncommittedss_info);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_DRAFT);
        intentFilter.addAction(BaseActivity.RECEIVER_ERROR);
        intentFilter.addAction(WorkReportNewActivity.WORKREPORT_REFRESH);
        getActivity().registerReceiver(mReceiver, intentFilter);

        workReportNewActivity = (WorkReportNewActivity) getActivity();

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
    }

    @Override
    protected void lazyLoad() {
        CalendarUtils.getWeekMonday(new Date());
        CalendarUtils.getWeekSunday(new Date());
        startdate = CalendarUtils.getWeekMonday(new Date(), "yyyy/MM/dd") + "";
        enddate = CalendarUtils.getWeekSunday(new Date(), "yyyy/MM/dd") + "";
        initDate(datetype + "", type, nextId, startdate, enddate);
        getWorkReportStatisticsInfo(startdate, enddate);
    }

    @Override
    protected void initEvent() {

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
                                          @Override
                                          public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                                              listViewRefreshType = 2;
                                              nextId = "0";
                                              initDate(datetype + "", type, nextId, startdate, enddate);
                                              getWorkReportStatisticsInfo(startdate, enddate);
                                          }

                                          @Override
                                          public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                                              listViewRefreshType = 1;
                                              initDate(datetype + "", type, nextId, startdate, enddate);
                                          }
                                      }
        );
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        switch (callbackId) {
            case CALLBACK_LIST:
                hideLoadingView();
                break;
        }

        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        listView.onRefreshComplete();
        if (!safe) {
            switch (callbackId) {
                case CALLBACK_LIST:
                    if (adapter.getlist().size() <= 0) {
                        loadingLayout.showErrorView(listView);
                    }
                    break;
            }
            return;
        }
        switch (callbackId) {

            case CALLBACK_LIST:

                List<WorkReport> workReports = new ArrayList<WorkReport>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    String id = resutObject.getString("nextid");
                    if (!TextUtils.isEmpty("id") && !"0".equals(id)) {
                        nextId = id;
                    }
                    workReports = JsonDataFactory.getDataArray(WorkReport.class, resutObject.getJSONArray("rows"));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter == null) {
                    adapter = new WorkReportAdapter(this, getActivity(), new ArrayList<WorkReport>());
                    listView.setAdapter(adapter);
                } else {
                    String s = path.getPostValues().get("s");
                    if ("0".equals(s) || TextUtils.isEmpty(s)) {
                        adapter.getlist().clear();
                    }
                    switch (listViewRefreshType) {
                        case 1:
                            List<WorkReport> list = adapter.getlist();
                            list.addAll(workReports);
                            adapter.myNotifyDataSetChanged(list);
                            listView.onRefreshComplete();
                            listViewRefreshType = 0;
                            break;
                        case 2:
                            adapter.myNotifyDataSetChanged(workReports);
                            listView.onRefreshComplete();
                            listViewRefreshType = 0;
                            break;
                        default:
                            adapter.myNotifyDataSetChanged(workReports);
                            break;
                    }
                    if (adapter.getlist().size() <= 0) {
                        loadingLayout.showEmptyView(listView, getString(R.string.no_workreport_detail), true, true);
                        listView.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case CALLBACK_STATISTICS:

                List<NewUser> commits = new ArrayList<NewUser>();
                List<NewUser> uncommitteds = new ArrayList<NewUser>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    commits = JsonDataFactory.getDataArray(NewUser.class, resutObject.getJSONArray("commits"));
                    uncommitteds = JsonDataFactory.getDataArray(NewUser.class, resutObject.getJSONArray("uncommitteds"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (statisticsLayout.getVisibility() == View.GONE)
                    statisticsLayout.setVisibility(View.VISIBLE);
                commitsNumView.setVisibility(View.VISIBLE);
                if (commits.size() > 0 || uncommitteds.size() > 0) {
                    commitsNumView.setText(commits.size() + uncommitteds.size() + getResources().getString(R.string.fen));
                } else {
                    commitsNumView.setText("0" + getResources().getString(R.string.fen));
                }
                if (uncommitteds.size() > 0) {
                    StringBuffer info = new StringBuffer();
                    String names = "";
                    for (int i = 0; i < uncommitteds.size(); i++) {
                        NewUser user = uncommitteds.get(i);
                        if (i == 0) {
                            names = user.name;
                        } else {
                            names = names + "，" + user.name;
                        }
                    }
                    info.append(uncommitteds.size() + getResources().getString(R.string.fen))
                            .append("（")
                            .append(names)
                            .append("）");
                    uncommitsInfoView.setText(info.toString());
                } else {
                    uncommitsInfoView.setText("0" + getResources().getString(R.string.fen));
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

    private void initDate(String datetype, String type, String nextId, String startdate, String enddate) {

        if (listView.getMode() == PullToRefreshBase.Mode.PULL_FROM_START)
            listView.setMode(PullToRefreshBase.Mode.BOTH);

        if (listViewRefreshType == 0) {
            showLoadingView();
        }
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("type", type);
        params.put("datetype", datetype);
        params.put("startdate", startdate);
        params.put("enddate", enddate);
        params.put("n", n + "");
        params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_WORKREPORT_LIST), params, getActivity());
        mNetworkManager.load(CALLBACK_LIST, path, this, listViewRefreshType == 0);
    }


    private void initDate(String datetype, String type, String nextId) {

//        if (!isListViewRefresh) {
//            showLoadingDialog(getActivity(), "", "正在加载数据。。。");
//        }
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("type", type);
        params.put("datetype", datetype);
        params.put("n", n + "");
        params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_WORKREPORT_LIST), params, getActivity());
        mNetworkManager.load(CALLBACK_LIST, path, this, true);
    }

    public void getWorkReportStatisticsInfo(String startdate, String enddate) {

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("startdate", startdate);
        params.put("enddate", enddate);
        params.put("datetype", datetype + "");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_WORKREPORT_STATISTICS), params, getActivity());
        mNetworkManager.load(CALLBACK_STATISTICS, path, this, true);

    }

    private void searchDate(String datetype, String type, String nextId, String keyword, String startTime, String endTime, boolean isSerch) {

        showLoadingView();
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("type", type);
        params.put("datetype", datetype);
        params.put("n", "50");
        params.put("s", nextId);
        if (isSerch)
            params.put("querytype", "2");
        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);
        if (!TextUtils.isEmpty(startTime) && !"0".equals(startTime))
            params.put("startdate", startTime);
        if (!TextUtils.isEmpty(endTime) && !"0".equals(endTime))
            params.put("enddate", endTime);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SEARCH_WORKREPORT), params, getActivity());
        mNetworkManager.load(CALLBACK_LIST, path, this, true);
    }

    @Override
    public void searchRequest(String keyword, String startTime, String endTime) {

        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        long sTime = 0;
        long eTime = 0;
        if (!TextUtils.isEmpty(startTime) && !getString(R.string.no_time).equals(startTime)) {
            sTime = Utils.dateFormat(startTime, "yyyy-MM-dd");
        }
        if (!TextUtils.isEmpty(endTime) && !getString(R.string.no_time).equals(endTime)) {
            eTime = Utils.dateFormat(endTime, "yyyy-MM-dd");
        }
        nextId = "0";
        Log.e("ceshi", "WorkReportWeeklyPaperFragment---周报---keyword------" + keyword
                + "------startTime------" + sTime + "/" + startTime
                + "------endTime------" + eTime + "/" + endTime);
        searchDate(datetype + "", type, nextId, keyword, sTime + "", eTime + "", true);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_COMMENT:


                        if (workReportNewActivity.permission.equals("2") && workReportNewActivity.subordinateTypeCurrentTab == 1) {
                            int position = intent.getIntExtra("position", -1);
                            int commentType = intent.getIntExtra("commentType", -1);
                            if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_WORKREPORT) {
                                adapter.chaneCommentNum(position);
                            }
                        }
                        break;

                    case PublishTask.PUBLISH_WORK_REPORT:
                        if (workReportNewActivity.permission.equals("2") && workReportNewActivity.subordinateTypeCurrentTab == 1) {
                            listViewRefreshType = 3;
                            nextId = "0";
                            initDate(datetype + "", type, nextId, startdate, enddate);
                            getWorkReportStatisticsInfo(startdate, enddate);
                        }
                        break;
                }
            } else if (BaseActivity.RECEIVER_ERROR.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_WORK_REPORT:
                        String msg = intent.getStringExtra("rootDataMsg");
                        if (!TextUtil.isEmpty(msg)) {
                            if (workReportNewActivity.permission.equals("2") && workReportNewActivity.subordinateTypeCurrentTab == 1) {
                                listViewRefreshType = 3;
                                nextId = "0";
                                initDate(datetype + "", type, nextId, startdate, enddate);
                                getWorkReportStatisticsInfo(startdate, enddate);
                                if (showToast)
                                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
            } else if (WorkReportNewActivity.WORKREPORT_REFRESH.equals(action)) {

                if (workReportNewActivity.permission.equals("2") && workReportNewActivity.subordinateTypeCurrentTab == 1) {
                    listViewRefreshType = 3;
                    nextId = "0";
                    initDate(datetype + "", type, nextId, startdate, enddate);
                    getWorkReportStatisticsInfo(startdate, enddate);
                }
            }
        }
    };
    private boolean showToast = true;

    @Override
    public void onPause() {
        super.onPause();
        showToast = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        showToast = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (adapter != null)
            adapter.destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", -1);
                    if (position >= 0 &&
                            workReportNewActivity.permission.equals("2") && workReportNewActivity.subordinateTypeCurrentTab == 1) {
                        String json = data.getStringExtra("json");
                        WorkReport workReport = null;
                        try {
                            if (!TextUtil.isEmpty(json))
                                workReport = JsonDataFactory.getData(WorkReport.class, new JSONObject(json));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        adapter.chaneWorkReport(position, workReport.getJson());
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    private void showLoadingView() {
        loadingLayout.showLoadingView(listView, "", true);
    }

    private void hideLoadingView() {
        loadingLayout.dismiss(listView);
    }
}