package com.vgtech.vancloud.ui.module.recruit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RecruitmentInfoBean;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.ChannelStatus;
import com.vgtech.vancloud.api.ChannelValues;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;
import com.vgtech.vancloud.ui.adapter.ResumeChannelAdapter;
import com.vgtech.vancloud.ui.adapter.ResumeStatusAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adm01 on 2016/6/7.
 */
public class ResumeManageActivity extends BaseActivity implements AbsListView.OnScrollListener, HttpListener<String> {
    private boolean isListViewRefresh = false;
    private Boolean isloading = true;
    private int n = 12;
    private String nextId = "0";
    private String mLastId = "0";
    private boolean mSafe;
    private boolean mHasData;
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_LIST = 1;
    private static final int CALLBACK_CHANNEL = 2;
    private ResumeChannelAdapter resumeChannelAdapter;
    private ResumeStatusAdapter resumeStatusAdapter;
    private String channel = ""; //来源[vancloud、51job、zhaopin]
    private String status = ""; //职位状态:publish(已发布),pause(已暂停),finish(发布结束),remove(已删除),不填表示上述所有状态
    private List<ChannelValues> mParentList = new ArrayList<>(); //渠道
    private List<ChannelValues> mTotleList = new ArrayList<>(); //全部
    private List<ChannelValues> mVancloudList = new ArrayList<>(); //万客
    private List<ChannelValues> m51JobList = new ArrayList<>(); //51
    private List<ChannelValues> mZhilianList = new ArrayList<>(); //智联招聘
    private NoScrollGridview gridViewChannel;
    private NoScrollGridview gridViewStatus;

    @Override
    protected int getContentView() {
        return R.layout.fragment_resume;
    }

    PullToRefreshListView listview;
    VancloudLoadingLayout loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.resume_manager));
        initView();
        getChannelStatus();

        recruitmentInfoAdapter = new ApiDataAdapter(this);
        listview.setAdapter(recruitmentInfoAdapter);

        resumeChannelAdapter = new ResumeChannelAdapter(this);
        resumeChannelAdapter.setSeclection(0);
        gridViewChannel.setAdapter(resumeChannelAdapter);
        gridViewChannel.setItemClick(true);
        gridViewChannel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChannelValues channelValues = resumeChannelAdapter.getItem(position);
                channel = channelValues.value;
                status = ""; //全部
                resumeChannelAdapter.setSeclection(position);
                resumeStatusAdapter.setSeclection(0);
                resumeChannelAdapter.notifyDataSetChanged();
                if ("".equals(channelValues.value)) {
                    setGridViewStatus(mTotleList);
                } else if ("vancloud".equals(channelValues.value)) {
                    setGridViewStatus(mVancloudList);
                } else if ("51job".equals(channelValues.value)) {
                    setGridViewStatus(m51JobList);
                } else if ("zhaopin".equals(channelValues.value)) {
                    setGridViewStatus(mZhilianList);
                }
                isloading = true;
                isListViewRefresh = true;
                nextId = "0";
                if (mNetworkManager != null) {
                    mNetworkManager.cancle(this);
                }
                initDate(channel, status);
            }
        });

        resumeStatusAdapter = new ResumeStatusAdapter(this);
        resumeStatusAdapter.setSeclection(0);
        gridViewStatus.setAdapter(resumeStatusAdapter);
        gridViewStatus.setItemClick(true);
        gridViewStatus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChannelValues channelValues = resumeStatusAdapter.getItem(position);
                status = channelValues.value;
                resumeStatusAdapter.setSeclection(position);
                resumeStatusAdapter.notifyDataSetChanged();
                isloading = true;
                isListViewRefresh = true;
                nextId = "0";
                if (mNetworkManager != null) {
                    mNetworkManager.cancle(this);
                }
                initDate(channel, status);
            }
        });
    }

    private void initView() {
        loading = (VancloudLoadingLayout) findViewById(R.id.loading);

        gridViewChannel = (NoScrollGridview) findViewById(R.id.grid_view_channel);
        gridViewStatus = (NoScrollGridview) findViewById(R.id.grid_view_status);
        listview = (PullToRefreshListView) findViewById(R.id.listview);
        listview.setOnScrollListener(this);
        listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isListViewRefresh = true;
                nextId = "0";
                initDate(channel, status);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = parent.getItemAtPosition(position);
                if (obj instanceof RecruitmentInfoBean) {
                    RecruitmentInfoBean recruit = (RecruitmentInfoBean) obj;
                    Intent intent = new Intent(ResumeManageActivity.this, ResumeListActivity.class);
                    intent.putExtra("type", "1");
                    intent.putExtra("position_id", recruit.job_id);
                    startActivity(intent);
                }
            }
        });
    }

    //获取渠道及状态
    private void getChannelStatus() {
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_RESUME_CHANNEL_STATUS), params, this);
        mNetworkManager.load(CALLBACK_CHANNEL, path, this);
    }

    private void initDate(String source, String status) {
        if (isloading) {
            loading.showLoadingView(listview, "", true);
        }
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("source", source);
        params.put("status", status);
        params.put("num", n + "");
        if (!TextUtils.isEmpty(nextId) && !isListViewRefresh) {
            params.put("start_index", nextId);
        } else {
            params.put("start_index", "0");
        }
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_RESUMES), params, this);
        mNetworkManager.load(CALLBACK_LIST, path, this);
    }

    private void setGridViewChannel(List<ChannelValues> channel) {
        resumeChannelAdapter.clearData();
        resumeChannelAdapter.addAllData(channel);
        resumeChannelAdapter.notifyDataSetChanged();
    }

    private void setGridViewStatus(List<ChannelValues> status) {
        resumeStatusAdapter.clearData();
        resumeStatusAdapter.addAllData(status);
        resumeStatusAdapter.notifyDataSetChanged();
    }

    private ApiDataAdapter<RecruitmentInfoBean> recruitmentInfoAdapter;

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
            initDate(channel, status);
        }
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        loading.dismiss(listview);
        listview.onRefreshComplete();
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {

            return;
        }
        switch (callbackId) {
            case CALLBACK_CHANNEL:
                List<ChannelStatus> channelStatuses = new ArrayList<>();
                try {
                    String data = rootData.getJson().getString("data");
                    JSONArray jsonArray = new JSONArray(data);
                    channelStatuses = JsonDataFactory.getDataArray(ChannelStatus.class, jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (channelStatuses != null && channelStatuses.size() > 0) {
                    for (int i = 0; i < channelStatuses.size(); i++) {
                        ChannelValues channelValues = new ChannelValues();
                        channelValues.key = channelStatuses.get(i).channel_name;
                        channelValues.value = channelStatuses.get(i).channel_value;
                        mParentList.add(channelValues);
                    }

                    try {
                        mTotleList = JsonDataFactory.getDataArray(ChannelValues.class,
                                channelStatuses.get(0).getJson().getJSONArray("status"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        mVancloudList = JsonDataFactory.getDataArray(ChannelValues.class,
                                channelStatuses.get(1).getJson().getJSONArray("status"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        m51JobList = JsonDataFactory.getDataArray(ChannelValues.class,
                                channelStatuses.get(2).getJson().getJSONArray("status"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        mZhilianList = JsonDataFactory.getDataArray(ChannelValues.class,
                                channelStatuses.get(3).getJson().getJSONArray("status"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mParentList != null && mParentList.size() > 0) {
                        setGridViewChannel(mParentList);
                    }
                    if (mTotleList != null && mTotleList.size() > 0) {
                        setGridViewStatus(mTotleList);
                    }
                }
                initDate(channel, status);
                break;
            case CALLBACK_LIST:
                List<RecruitmentInfoBean> recruitmentInfoBeans = new ArrayList<RecruitmentInfoBean>();
                try {
                    String data = rootData.getJson().getString("data");
                    JSONObject resutObject = new JSONObject(data);
                    nextId = resutObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(nextId) && !"0".equals(nextId);
                    recruitmentInfoBeans = JsonDataFactory.getDataArray(RecruitmentInfoBean.class, resutObject.getJSONArray("records"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                String page = path.getPostValues().get("start_index");
                if ("0".equals(page)) {
                    isListViewRefresh = true;
                }
                if (isListViewRefresh) {
                    recruitmentInfoAdapter.clear();
                    listview.onRefreshComplete();
                    isListViewRefresh = false;
                }
                recruitmentInfoAdapter.add(recruitmentInfoBeans);

                if (recruitmentInfoAdapter.getCount() > 0) {
                } else {
                    loading.showEmptyView(listview, getString(R.string.no_recruit_detail), true, true);
                }
                isloading = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
