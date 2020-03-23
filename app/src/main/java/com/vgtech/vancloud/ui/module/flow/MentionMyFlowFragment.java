package com.vgtech.vancloud.ui.module.flow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Flow;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.FlowAdapter;
import com.vgtech.vancloud.ui.base.LazyLoadFragment;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2015/9/11.
 */
public class MentionMyFlowFragment extends LazyLoadFragment implements HttpListener<String> {

    private PullToRefreshListView listView;
    //1我发起的，2我审批的，3抄送我的
    private int type = 3;
    //1，待审批，2同意，3不同意，默认0全部
    private int state = 0;
    //分页的大小
    private int n = 10;
    //返回数据的起始index；默认为0，与n搭配使用
    private String nextId = "0";
    private static final int CALLBACK_FLOWLIST = 1;
    private int listViewRefreshType = 0;
    private NetworkManager mNetworkManager;
    private String permission = "1";
    private FlowAdapter adapter;

    FlowReportNewActivity flowReportNewActivity;

    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";

    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;

    @Override
    protected int initLayoutId() {
        return R.layout.i_send_out_fragment_layout;
    }

    @Override
    protected void initView(View view) {

        loadingLayout = (LinearLayout) view.findViewById(R.id.loading);
        loadingMagView = (TextView) view.findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) view.findViewById(R.id.progress_view);
        listView = (PullToRefreshListView) view.findViewById(R.id.listview);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        List<Flow> list = new ArrayList<>();
        adapter = new FlowAdapter(this, list, type);
        listView.setAdapter(adapter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_DRAFT);
        intentFilter.addAction(FlowReportNewActivity.FLOW_REFRESH);
        getActivity().registerReceiver(mReceiver, intentFilter);

        flowReportNewActivity = (FlowReportNewActivity) getActivity();

    }


    @Override
    protected void lazyLoad() {
        initDate(type, state + "", nextId);
    }

    @Override
    protected void initEvent() {

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
                                          @Override
                                          public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                                              listViewRefreshType = 2;
                                              nextId = "0";
                                              initDate(type, state + "", nextId);
                                          }

                                          @Override
                                          public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                                              listViewRefreshType = 1;
                                              initDate(type, state + "", nextId);
                                          }
                                      }
        );
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    private void initDate(int type, String state, String nextId) {

        if (listView.getMode() == PullToRefreshBase.Mode.PULL_FROM_START)
            listView.setMode(PullToRefreshBase.Mode.BOTH);

        if (listViewRefreshType == 0) {
            showLoadingView();
        }
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("type", type + "");
        params.put("state", state);
        params.put("n", n + "");
        params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_PROCESS_LIST), params, getActivity());
        mNetworkManager.load(CALLBACK_FLOWLIST, path, this, listViewRefreshType == 0);
    }


    private void searchDate(int type, String state, String nextId, String keyword, String startTime, String endTime) {

        showLoadingView();
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("type", type + "");
        params.put("state", state);
        params.put("n", "50");
        params.put("s", nextId);
        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);
        if (!TextUtils.isEmpty(startTime) && !"0".equals(startTime))
            params.put("startdate", startTime);
        if (!TextUtils.isEmpty(endTime) && !"0".equals(endTime))
            params.put("enddate", endTime);

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SEARCH_WORKFLOW), params, getActivity());
        mNetworkManager.load(CALLBACK_FLOWLIST, path, this, true);
    }

    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        hideLoadingView();

        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            if (listViewRefreshType != 0 && listViewRefreshType != 3) {
                listView.onRefreshComplete();
                listViewRefreshType = 0;
            } else {
                if (adapter.getList().size() <= 0) {
                    loadingLayout.setVisibility(View.VISIBLE);
                    loadingMagView.setText(getString(R.string.no_flow_detail));
                    loadingMagView.setVisibility(View.VISIBLE);
                }
            }
            return;
        }
        switch (callbackId) {

            case CALLBACK_FLOWLIST:
                List<Flow> flowkList = new ArrayList<Flow>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
//                    nextId = resutObject.getString("next_id");
                    String id = resutObject.getString("nextid");
                    if (!TextUtils.isEmpty("id") && !"0".equals(id)) {
                        nextId = id;
                    }
                    flowkList = JsonDataFactory.getDataArray(Flow.class, resutObject.getJSONArray("rows"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (adapter == null) {
                    adapter = new FlowAdapter(this, flowkList, type);
                    listView.setAdapter(adapter);
                } else {
                    String s = path.getPostValues().get("s");
                    if ("0".equals(s) || TextUtils.isEmpty(s)) {
                        adapter.getList().clear();
                    }
                    switch (listViewRefreshType) {
                        case 1:
                            List<Flow> list = adapter.getList();
                            list.addAll(flowkList);
                            adapter.myNotifyDataSetChanged(list);
                            listView.onRefreshComplete();
                            listViewRefreshType = 0;
                            break;
                        case 2:
                            adapter.myNotifyDataSetChanged(flowkList);
                            listView.onRefreshComplete();
                            listViewRefreshType = 0;
                            break;
                        default:
                            adapter.myNotifyDataSetChanged(flowkList);
                            break;
                    }
                    if (adapter.getList().size() <= 0) {
                        loadingLayout.setVisibility(View.VISIBLE);
                        loadingMagView.setText(getString(R.string.no_flow_detail));
                        loadingMagView.setVisibility(View.VISIBLE);
                    }
                }

                break;

            default:
                break;


        }

    }

    public void onErrorResponse(VolleyError error) {

    }

    public void onResponse(String response) {

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    //回复
                    case PublishTask.PUBLISH_COMMENT:
                        if (flowReportNewActivity.mCurrentTab == 2) {
                            int position = intent.getIntExtra("position", -1);
                            int commentType = intent.getIntExtra("commentType", -1);
                            if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_FLOW) {
                                adapter.chaneCommentNum(position);
                            }
                        }
                        break;
//
//                    case PublishTask.PUBLISH_FLOW_CONDUCT:
//                        if (flowReportNewActivity.mCurrentTab == 2) {
//                            listViewRefreshType = 0;
//                            nextId = "0";
//                            initDate(type, state + "", nextId);
//                        }
//                        break;
//
//                    case PublishTask.PUBLISH_FLOW:
//                        if (flowReportNewActivity.mCurrentTab == 2) {
//                            listViewRefreshType = 0;
//                            nextId = "0";
//                            initDate(type, state + "", nextId);
//                        }
//                        break;
//
//                    case PublishTask.PUBLISH_FLOW_LEAVE:
//                        if (flowReportNewActivity.mCurrentTab == 2) {
//                            listViewRefreshType = 0;
//                            nextId = "0";
//                            initDate(type, state + "", nextId);
//                        }
//                        break;
                }
            } else if (FlowReportNewActivity.FLOW_REFRESH.equals(action)) {

                if (flowReportNewActivity.mCurrentTab == 2) {
                    listViewRefreshType = 3;
                    nextId = "0";
                    initDate(type, state + "", nextId);
                }

            }
        }
    };


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
                    if (position >= 0 && flowReportNewActivity.mCurrentTab == 2) {
                        String json = data.getStringExtra("json");
                        Flow flow = null;
                        try {
                            if (!TextUtil.isEmpty(json))
                                flow = JsonDataFactory.getData(Flow.class, new JSONObject(json));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        adapter.chaneFlow(position, flow.getJson());
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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
        Log.e("ceshi", "MentionMyFlowFragment---抄送我的流程---keyword------" + keyword
                + "------startTime------" + sTime + "/" + startTime
                + "------endTime------" + eTime + "/" + endTime);
        searchDate(type, state + "", nextId, keyword, sTime + "", eTime + "");
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