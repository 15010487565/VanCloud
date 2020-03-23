package com.vgtech.vancloud.ui.module.workreport;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.WorkReport;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.WorkReportAdapter;
import com.vgtech.vancloud.ui.base.LazyLoadFragment;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2015/9/10.
 */
public class WorkReportISendOutFragment extends LazyLoadFragment implements HttpListener<String> {

    private PullToRefreshListView listView;
    //（0我点评的，1我发出的，2抄送给我的）
    private int subtype = 1;
    //分页的大小
    private int n = 10;
    private String nextId = "0";
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_LIST = 1;
    private String type = "0";
    private WorkReportAdapter adapter;
    private int listViewRefreshType = 0;

    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";
    public static final String RECEIVER_PUSH = "RECEIVER_PUSH";
    boolean showType = false;

    WorkReportNewActivity workReportActivity;

    private VancloudLoadingLayout loadingLayout;

    @Override
    protected int initLayoutId() {
        return R.layout.i_send_out_fragment_layout;
    }

    @Override
    protected void initView(View view) {

        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);
        listView = (PullToRefreshListView) view.findViewById(R.id.listview);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        adapter = new WorkReportAdapter(this, getActivity(), new ArrayList<WorkReport>());
        listView.setAdapter(adapter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_DRAFT);
        intentFilter.addAction(RECEIVER_PUSH);
        intentFilter.addAction(WorkReportNewActivity.WORKREPORT_REFRESH);
        getActivity().registerReceiver(mReceiver, intentFilter);

        workReportActivity = (WorkReportNewActivity) getActivity();
        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
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
    protected void initData() {
        initDate(subtype + "", type, nextId);
    }

    @Override
    protected void initEvent() {

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                listViewRefreshType = 2;
                nextId = "0";
                initDate(subtype + "", type, nextId);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                listViewRefreshType = 1;
                initDate(subtype + "", type, nextId);
            }
        });

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
            if (adapter.getlist().size() <= 0) {
                loadingLayout.showErrorView(listView);
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
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }


    private void initDate(String subtype, String type, String nextId) {

        if (listView.getMode() == PullToRefreshBase.Mode.PULL_FROM_START)
            listView.setMode(PullToRefreshBase.Mode.BOTH);

        if (listViewRefreshType == 0) {
            if (!showType) {
                showType = false;
                showLoadingView();
            }
        }
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("type", type);
        params.put("subtype", subtype);
        params.put("n", n + "");
        params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_WORKREPORT_LIST), params, getActivity());
        mNetworkManager.load(CALLBACK_LIST, path, this, listViewRefreshType == 0);
    }

    private void serchDate(String subtype, String type, String nextId, String keyword, String startTime, String endTime) {

        showLoadingView();
        nextId = "0";
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("type", type);
        params.put("subtype", subtype);
        params.put("n", "50");
        params.put("s", nextId);
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
        Log.e("ceshi", "WorkReportISendOutFragment---我发出的---keyword------" + keyword
                + "------startTime------" + sTime + "/" + startTime
                + "------endTime------" + eTime + "/" + endTime);
        serchDate(subtype + "", type, nextId, keyword, sTime + "", eTime + "");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_WORKREPORT:
                        listViewRefreshType = 3;
                        showType = true;
                        nextId = "0";
                        initDate(subtype + "", type, nextId);
                        break;
                    case PublishTask.PUBLISH_COMMENT:


                        if (workReportActivity.permission.equals("1") && workReportActivity.myTypeCurrentTab == 0) {
                            int position = intent.getIntExtra("position", -1);
                            int commentType = intent.getIntExtra("commentType", -1);
                            if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_WORKREPORT) {
                                adapter.chaneCommentNum(position);
                            }
                        }
                        break;
                }
            } else if (RECEIVER_PUSH.equals(action)) {
                String infoType = intent.getStringExtra("infotype");
//                if ("7".equals(infoType)) {
//                    backRefresh = true;
//                    requestTaskInfo(taskID);
//                }
            } else if (WorkReportNewActivity.WORKREPORT_REFRESH.equals(action)) {

                if (workReportActivity.permission.equals("1") && workReportActivity.myTypeCurrentTab == 0) {
                    listViewRefreshType = 3;
                    nextId = "0";
                    initDate(subtype + "", type, nextId);
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", -1);
                    if (position >= 0 &&
                            workReportActivity.permission.equals("1") && workReportActivity.myTypeCurrentTab == 0) {
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
