package com.vgtech.vancloud.ui.module.recruit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RecruitmentInfoBean;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.zxing.zxing.MipcaActivityCapture;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.ChannelStatus;
import com.vgtech.vancloud.api.ChannelValues;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.RecruitmentInfoAdapter;
import com.vgtech.vancloud.ui.adapter.ResumeChannelAdapter;
import com.vgtech.vancloud.ui.adapter.ResumeStatusAdapter;
import com.vgtech.vancloud.ui.register.ui.CompanyInfoEditActivity;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by code on 2015/12/21.
 * 职位信息
 */
public class RecruitmentInfoActivity extends BaseActivity implements View.OnClickListener, RecruitmentInfoAdapter.OnSelectListener, HttpListener<String>, AbsListView.OnScrollListener {

    @InjectView(R.id.listview)
    PullToRefreshListView listview;

    @InjectView(R.id.loading)
    VancloudLoadingLayout loading;

    @InjectView(R.id.del_layout)
    RelativeLayout delLayout;
    @InjectView(R.id.refresh_layout)
    RelativeLayout refreshLayout;
    @InjectView(R.id.publish_layout)
    RelativeLayout publishLayout;
    @InjectView(R.id.pause_layout)
    RelativeLayout pauseLayout;
    @InjectView(R.id.republish_layout)
    RelativeLayout republishLayout;
    @InjectView(R.id.restore_layout)
    RelativeLayout restoreLayout;
    @InjectView(R.id.view01)
    View view01;
    @InjectView(R.id.view02)
    View view02;
    @InjectView(R.id.view03)
    View view03;
    @InjectView(R.id.view04)
    View view04;
    @InjectView(R.id.view05)
    View view05;
//    @InjectView(R.id.bottom_layout)
//    LinearLayout bottomLayout;
    @InjectView(R.id.tv_scanning)
    TextView tvScanning;
    @InjectView(R.id.grid_view_channel)
    NoScrollGridview gridViewChannel;
    @InjectView(R.id.grid_view_status)
    NoScrollGridview gridViewStatus;

    private RecruitmentInfoAdapter recruitmentInfoAdapter;
    private ResumeChannelAdapter resumeChannelAdapter;
    private ResumeStatusAdapter resumeStatusAdapter;
    private HashMap<String, RecruitmentInfoBean> mIndexer = new HashMap<String, RecruitmentInfoBean>();
    private String channel = "vancloud"; //渠道：来源[vancloud、51job、zhaopin]
    private String status = ""; //职位状态:not_publish(未发布),publish(发布中),pause(已暂停),finish(发布结束)applying(申请中),pending(审核中),unpass(未通过),不填表示所有状态
    private boolean isListViewRefresh = false;
    private Boolean isloading = true;
    private int n = 12;
    private String nextId = "0";
    private String mLastId = "0";
    private boolean mSafe;
    private boolean mHasData;
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_LIST = 1;
    private static final int CALLBACK_REFRESH = 2;
    private static final int CALLBACK_DOS = 3;
    private static final int CALLBACK_CHANNEL = 4;
    private static final int CALLBACK_PHONE_LOGIN = 5;
    private static final int CALLBACK_TEMPLATES = 6;
    private static final int CALLBACK_PUBLISH = 10;
    private List<ChannelValues> mParentList = new ArrayList<>(); //渠道
    private List<ChannelValues> mVancloudList = new ArrayList<>(); //万客
    private List<ChannelValues> m51JobList = new ArrayList<>(); //51
    private List<ChannelValues> mZhilianList = new ArrayList<>(); //智联招聘

    @Override
    protected int getContentView() {
        return R.layout.activity_recruit_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vocation_admin));
        ButterKnife.inject(this);
        initEvent();
        initData();
        getChannelStatus();
    }

    private void initEvent() {
        tvScanning.setOnClickListener(this);
        delLayout.setOnClickListener(this);
        refreshLayout.setOnClickListener(this);
        publishLayout.setOnClickListener(this);
        pauseLayout.setOnClickListener(this);
        republishLayout.setOnClickListener(this);
        restoreLayout.setOnClickListener(this);

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
                RecruitmentInfoBean recruitmentInfoBean = (RecruitmentInfoBean) recruitmentInfoAdapter.getItem(position - 1);
                Intent intent = new Intent(RecruitmentInfoActivity.this, RecruitmentDetailsActivity.class);
                intent.putExtra("recruitInfo", recruitmentInfoBean);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        recruitmentInfoAdapter = new RecruitmentInfoAdapter(this, new ArrayList<RecruitmentInfoBean>());
        recruitmentInfoAdapter.setmListener(this);
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
                if ("vancloud".equals(channelValues.value)) {
                    setGridViewStatus(mVancloudList);
                } else if ("51job".equals(channelValues.value)) {
                    setGridViewStatus(m51JobList);
                } else if ("zhaopin".equals(channelValues.value)) {
                    setGridViewStatus(mZhilianList);
                }
                isloading = true;
                isListViewRefresh = true;
                nextId = "0";
                mIndexer.clear();
                if (mNetworkManager != null) {
                    mNetworkManager.cancle(this);
                }
//                bottomLayout.setVisibility(View.GONE);
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
                mIndexer.clear();
                if (mNetworkManager != null) {
                    mNetworkManager.cancle(this);
                }
//                bottomLayout.setVisibility(View.GONE);
                initDate(channel, status);
            }
        });
//        bottomLayout.setVisibility(View.GONE);
    }

    //获取渠道及状态
    private void getChannelStatus() {
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_CHANNEL_STATUS), params, this);
        mNetworkManager.load(CALLBACK_CHANNEL, path, this);
    }

    //bottomlayout changes
    private void setChangeBottomLayout() {
        if ("vancloud".equals(channel) && TextUtils.isEmpty(status)
                || "51job".equals(channel) && TextUtils.isEmpty(status)
                || "zhaopin".equals(channel) && TextUtils.isEmpty(status)
                || "zhaopin".equals(channel) && "pending".equals(status)
                || "zhaopin".equals(channel) && "unpass".equals(status)) {
//            bottomLayout.setVisibility(View.GONE);
            delLayout.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.GONE);
            publishLayout.setVisibility(View.GONE);
            pauseLayout.setVisibility(View.GONE);
            republishLayout.setVisibility(View.GONE);
            restoreLayout.setVisibility(View.GONE);
            recruitmentInfoAdapter.setCheckbox(false);
            view01.setVisibility(View.GONE);
            view02.setVisibility(View.GONE);
            view03.setVisibility(View.GONE);
            view04.setVisibility(View.GONE);
            view05.setVisibility(View.GONE);
        } else {
//            bottomLayout.setVisibility(View.VISIBLE);
            delLayout.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.VISIBLE);
            publishLayout.setVisibility(View.VISIBLE);
            pauseLayout.setVisibility(View.VISIBLE);
            republishLayout.setVisibility(View.VISIBLE);
            restoreLayout.setVisibility(View.VISIBLE);
            recruitmentInfoAdapter.setCheckbox(true);
            view01.setVisibility(View.GONE);
            view02.setVisibility(View.GONE);
            view03.setVisibility(View.GONE);
            view04.setVisibility(View.GONE);
            view05.setVisibility(View.GONE);
        }

        if ("vancloud".equals(channel)) {
            if ("not_publish".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.VISIBLE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.GONE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
            if ("publish".equals(status)) {
                delLayout.setVisibility(View.GONE);
                refreshLayout.setVisibility(View.VISIBLE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.VISIBLE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.GONE);
                view02.setVisibility(View.VISIBLE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.VISIBLE);
                view05.setVisibility(View.GONE);
            }
            if ("pause".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.VISIBLE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.VISIBLE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
            if ("finish".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
        } else if ("51job".equals(channel)) {
            if ("not_publish".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.VISIBLE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.GONE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
            if ("publish".equals(status)) {
                delLayout.setVisibility(View.GONE);
                refreshLayout.setVisibility(View.VISIBLE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.VISIBLE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.GONE);
                view02.setVisibility(View.VISIBLE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.VISIBLE);
                view05.setVisibility(View.GONE);
            }
            if ("pause".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.VISIBLE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.VISIBLE);
                view05.setVisibility(View.GONE);
            }
            if ("finish".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
            if ("applying".equals(status)) {
                delLayout.setVisibility(View.GONE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.VISIBLE);
                republishLayout.setVisibility(View.GONE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.GONE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
        } else if ("zhaopin".equals(channel)) {
            if ("not_publish".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.VISIBLE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.GONE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
            if ("publish".equals(status)) {
                delLayout.setVisibility(View.GONE);
                refreshLayout.setVisibility(View.VISIBLE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.VISIBLE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.GONE);
                view02.setVisibility(View.VISIBLE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.VISIBLE);
                view05.setVisibility(View.GONE);
            }
            if ("pause".equals(status)) {
                delLayout.setVisibility(View.GONE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.GONE);
                restoreLayout.setVisibility(View.VISIBLE);
                view01.setVisibility(View.GONE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
            if ("finish".equals(status)) {
                delLayout.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.GONE);
                publishLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.GONE);
                republishLayout.setVisibility(View.VISIBLE);
                restoreLayout.setVisibility(View.GONE);
                view01.setVisibility(View.VISIBLE);
                view02.setVisibility(View.GONE);
                view03.setVisibility(View.GONE);
                view04.setVisibility(View.GONE);
                view05.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btn_more:
//                Intent intent = new Intent(this, JobCreateActivity.class);
//                intent.putExtra("type", "1");
//                startActivityForResult(intent, 120);
//                break;
            case R.id.tv_scanning:
                templatesAction();
                break;
            case R.id.refresh_layout: //刷新
                if (mIndexer.size() == 0) {
                    showToast(getResources().getString(R.string.no_choose_msg));
                } else {
                    refreshAction(Utils.getIds(mIndexer));
                }
                break;
            case R.id.del_layout://删除
                if (mIndexer.size() == 0) {
                    showToast(getResources().getString(R.string.no_choose_msg));
                } else {
                    AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_del));
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setLeft();
                    alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            delAction(Utils.getIds(mIndexer));
                        }
                    });
                    alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    alertDialog.show();
                }
                break;
            case R.id.publish_layout://发布
                if (mIndexer.size() == 0) {
                    showToast(getResources().getString(R.string.no_choose_msg));
                } else {
                    if ("vancloud".equals(channel)) {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_publish));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doAction(Utils.getIds(mIndexer), "publish");
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    } else if ("51job".equals(channel)) {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_publish)).setMsg(getString(R.string.tips_publish_hint));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                publishAction(Utils.getIds(mIndexer), "51job");
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_publish)).setMsg(getString(R.string.tips_publish_hint));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                publishAction(Utils.getIds(mIndexer), "zhaopin");
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    }
                }
                break;
            case R.id.pause_layout://暂停
                if (mIndexer.size() == 0) {
                    showToast(getResources().getString(R.string.no_choose_msg));
                } else {
                    if ("vancloud".equals(channel)) {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_pause));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doAction(Utils.getIds(mIndexer), "pause");
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    } else if ("51job".equals(channel)) {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_pause)).setMsg(getString(R.string.tips_pause_hint));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pauseAction(Utils.getIds(mIndexer));
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_pause_hint_sips));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doAction(Utils.getIds(mIndexer), "pause");
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    }
                }
                break;
            case R.id.republish_layout://再发布
                if (mIndexer.size() == 0) {
                    showToast(getResources().getString(R.string.no_choose_msg));
                } else {
                    if ("vancloud".equals(channel)) {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_republish));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doAction(Utils.getIds(mIndexer), "republish");
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.tips_republish)).setMsg(getString(R.string.tips_republish_hint));
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setLeft();
                        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doAction(Utils.getIds(mIndexer), "republish");
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        alertDialog.show();
                    }
                }
                break;
            case R.id.restore_layout://恢复
                if (mIndexer.size() == 0) {
                    showToast(getResources().getString(R.string.no_choose_msg));
                } else {
                    doAction(Utils.getIds(mIndexer), "restore");
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void templatesAction() {
        showLoadingDialog(this, "");
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("template_flag", "tenant_phone_login");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_TENANT_PHONE_LOGIN), params, this);
        mNetworkManager.load(CALLBACK_TEMPLATES, path, this);
    }

    private void refreshAction(String ids) {
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("job_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_FRESH_JOBS), params, this);
        mNetworkManager.load(CALLBACK_REFRESH, path, this);
    }

    private void delAction(String ids) {
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("job_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_DELETE_JOBS), params, this);
        mNetworkManager.load(CALLBACK_REFRESH, path, this);
    }

    //51申请中暂停职位
    private void pauseAction(String ids) {
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("job_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_APPLYING_PAUSE_JOBS), params, this);
        mNetworkManager.load(CALLBACK_DOS, path, this);
    }

    //51、智联未发布-发布职位
    private void publishAction(String ids, String type) {
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("source", type);
        params.put("job_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_NO_PUBLISH_JOBS), params, this);
        mNetworkManager.load(CALLBACK_DOS, path, this);
    }

    private void doAction(String ids, String status) {
        String url = "";
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("job_ids", ids);

        int callbackId = CALLBACK_DOS;
        if ("publish".equals(status)) { //发布
            callbackId = CALLBACK_PUBLISH;
            url = URLAddr.URL_VANCLOUD_JOB_PUBLISH_JOBS;
        } else if ("republish".equals(status)) { //再发布
            url = URLAddr.URL_VANCLOUD_JOB_REPUBLISH_JOBS;
        } else if ("restore".equals(status)) { //恢复
            url = URLAddr.URL_VANCLOUD_JOB_RENEW_JOBS;
        } else if ("pause".equals(status)) { //暂停
            url = URLAddr.URL_VANCLOUD_JOB_PAUSE_JOBS;
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, url), params, this);
        mNetworkManager.load(callbackId, path, this);
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
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_JOBS), params, this);
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

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (callbackId == CALLBACK_LIST)
            loading.dismiss(listview);
        listview.onRefreshComplete();
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, callbackId != CALLBACK_PUBLISH);
        if (!mSafe) {
            String msg = rootData.getMsg();
            if (!TextUtils.isEmpty(msg) && msg.startsWith("请完善公司基本信息")) {
                AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.prompt)).setMsg(msg);
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setLeft();
                alertDialog.setPositiveButton(getString(R.string.tip_go_edit), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RecruitmentInfoActivity.this, CompanyInfoEditActivity.class);
                        startActivity(intent);
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                alertDialog.show();
            } else {
                if (TextUtils.isEmpty(msg))
                    msg = getString(R.string.network_error);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
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
                        mVancloudList = JsonDataFactory.getDataArray(ChannelValues.class,
                                channelStatuses.get(0).getJson().getJSONArray("status"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        m51JobList = JsonDataFactory.getDataArray(ChannelValues.class,
                                channelStatuses.get(1).getJson().getJSONArray("status"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        mZhilianList = JsonDataFactory.getDataArray(ChannelValues.class,
                                channelStatuses.get(2).getJson().getJSONArray("status"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mParentList != null && mParentList.size() > 0) {
                        setGridViewChannel(mParentList);
                    }
                    if (mVancloudList != null && mVancloudList.size() > 0) {
                        setGridViewStatus(mVancloudList);
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

                if (recruitmentInfoAdapter == null) {
                    recruitmentInfoAdapter = new RecruitmentInfoAdapter(this, recruitmentInfoBeans);
                    listview.setAdapter(recruitmentInfoAdapter);
                } else {
                    String page = path.getPostValues().get("start_index");
                    if ("0".equals(page)) {
                        isListViewRefresh = true;
                    }
                    if (isListViewRefresh) {
                        recruitmentInfoAdapter.getMlist().clear();
                        listview.onRefreshComplete();
                        isListViewRefresh = false;
                    }
                    List<RecruitmentInfoBean> list = recruitmentInfoAdapter.getMlist();
                    list.addAll(recruitmentInfoBeans);
                    recruitmentInfoAdapter.myNotifyDataSetChanged(list);

                    if (list != null && list.size() > 0) {

//                        bottomLayout.setVisibility(View.VISIBLE);
                        setChangeBottomLayout();
                    } else {
                        loading.showEmptyView(listview, getString(R.string.no_recruit_detail), true, true);
//                        bottomLayout.setVisibility(View.GONE);
                    }
                }
                isloading = false;
                break;
            case CALLBACK_REFRESH:
                mIndexer.clear();
                isListViewRefresh = true;
                nextId = "0";
                initDate(channel, status);
                break;
            case CALLBACK_PUBLISH:
            case CALLBACK_DOS:
                if ("not_publish".equals(status)) {
                    try {
                        String data = rootData.getJson().getString("data");
                        if ("uncomplete_tenant_info".equals(data)) {
                            showCompanyinfoTip();
                        } else {
                            mIndexer.clear();
                            isListViewRefresh = true;
                            nextId = "0";
                            initDate(channel, status);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mIndexer.clear();
                    isListViewRefresh = true;
                    nextId = "0";
                    initDate(channel, status);
                }
                break;
            case CALLBACK_PHONE_LOGIN:
                dismisLoadingDialog();
                Toast.makeText(this, getString(R.string.personal_login_success_msg), Toast.LENGTH_SHORT).show();
                break;
            case CALLBACK_TEMPLATES:
                dismisLoadingDialog();
                try {
                    String data = rootData.getJson().getString("data");
                    Intent intent_scanning = new Intent(this, MipcaActivityCapture.class);
                    intent_scanning.putExtra("templates_url", data);
                    intent_scanning.putExtra("style", "company");
                    startActivityForResult(intent_scanning, 200);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    private void loginPc(String barcode) {
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("qr_id", barcode);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_PHONE_LOGIN), params, this);
        showLoadingDialog(this, "");
        networkManager.load(CALLBACK_PHONE_LOGIN, path, this);
    }

    //租户信息不全提示
    private void showCompanyinfoTip() {
        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.updata_company_info));
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setLeft();
        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecruitmentInfoActivity.this, CompanyInfoEditActivity.class));
            }
        });
        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100:
                    mIndexer.clear();
                    isListViewRefresh = true;
                    nextId = "0";
                    initDate(channel, status);
                    break;
                case 120:
                    mIndexer.clear();
                    isListViewRefresh = true;
                    nextId = "0";
                    initDate(channel, status);
                    break;
                case 200://扫码
                    String barcode = data.getStringExtra("barcode");
                    loginPc(barcode);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null) {
            mNetworkManager.cancle(this);
        }
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
            initDate(channel, status);
        }
    }

    @Override
    public void OnSelected(RecruitmentInfoBean item) {
        mIndexer.put(item.job_id, item);
    }

    @Override
    public void OnUnSelected(RecruitmentInfoBean item) {
        mIndexer.remove(item.job_id);
    }

    @Override
    public boolean OnIsSelect(RecruitmentInfoBean item) {
        return mIndexer.containsKey(item.job_id);
    }
}
