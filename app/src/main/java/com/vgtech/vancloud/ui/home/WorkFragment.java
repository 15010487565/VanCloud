package com.vgtech.vancloud.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.vgtech.common.BaseApp;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.NoScrollGridviewSpilview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.api.ADInfo;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.AppModuleAdapter;
import com.vgtech.vancloud.ui.beidiao.BeidiaoActivity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.module.AppListFragment;
import com.vgtech.vancloud.ui.module.ad.HorizontalAD;
import com.vgtech.vancloud.ui.module.announcement.AnnouncementListActivity;
import com.vgtech.vancloud.ui.module.approval.ApplyMainActivity;
import com.vgtech.vancloud.ui.module.approval.ApprovalMainActivity;
import com.vgtech.vancloud.ui.module.financemanagement.OrderListActivity;
import com.vgtech.vancloud.ui.module.help.HelpListActivity;
import com.vgtech.vancloud.ui.module.recruit.ResumeHomeActivity;
import com.vgtech.vancloud.ui.module.schedule.ScheduleHomeActivity;
import com.vgtech.vancloud.ui.module.share.ShareActivity;
import com.vgtech.vancloud.ui.module.task.TaskActivity;
import com.vgtech.vancloud.ui.module.workreport.WorkReportNewActivity;
import com.vgtech.vancloud.ui.view.cycle.CycleViewPager;
import com.vgtech.vancloud.ui.view.cycle.ViewFactory;
import com.vgtech.vancloud.ui.web.HttpWebActivity;
import com.vgtech.vancloud.ui.web.JixiaoWebActivity;
import com.vgtech.vancloud.ui.web.NeigouWebActivity;
import com.vgtech.vancloud.ui.web.ProcessWebActivity;
import com.vgtech.vancloud.ui.web.XinlitijianWebActivity;
import com.vgtech.vancloud.ui.worklog.WorklogActivity;
import com.vgtech.vancloud.utils.IpUtil;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.punchcard.AttendenceActivity;
import com.vgtech.vantop.ui.punchcard.PunchCardActivity;
import com.vgtech.vantop.ui.questionnaire.QuestionnaireActivity;
import com.vgtech.vantop.ui.salary.SalaryMainActivity;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.RoboGuice;

/**
 * Created by John on 2015/8/7.
 */
public class WorkFragment extends BaseFragment implements
        AdapterView.OnItemClickListener
        , View.OnClickListener
        , HttpListener<String>
        , FragmentLifecycle {

    private static final int APPROVAL_COUNT = 4;
    private NoScrollGridviewSpilview gvGridView;
    public MainActivity mainActivity;

    private FrameLayout bannerView;
    private ImageView closeBtn;
    private RelativeLayout moreLayout;
    private static final int CALLBACK_AD = 1;
    private static final int CALLBACK_PC_VER_NUM = 2;
    private static final int CALLBACK_IS_SHOW_PWD = 3;
    private NetworkManager mNetworkManager;
    private boolean mIsShowSalaryPwdDialog = true;
//    private static Handler sHandler;

    @Override
    protected int initLayoutId() {
        return R.layout.work_fragment_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectViewMembers(this);
    }

    private View mWorkView;

    @Override
    protected void initView(View view) {
        mWorkView = view.findViewById(R.id.work_view);
        bannerView = (FrameLayout) view.findViewById(R.id.banner_view);
        closeBtn = (ImageView) view.findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(this);
        gvGridView = (NoScrollGridviewSpilview) view.findViewById(R.id.grid_view);
        gvGridView.setOnItemClickListener(this);

        gvGridView.setFocusable(false);

        moreLayout = (RelativeLayout) view.findViewById(R.id.more_layout);
        moreLayout.setOnClickListener(this);
        initCycleView();
    }

    private List<ADInfo> mAdInfos;
    private List<View> views = new ArrayList<View>();
    private CycleViewPager cycleViewPager;

    private void initCycleView() {
        cycleViewPager = new CycleViewPager(bannerView, getActivity());
    }


    private void
    initCycleView(List<ADInfo> adInfos, boolean canClose) {
        if (canClose)
            closeBtn.setVisibility(View.VISIBLE);
        if (!adInfos.isEmpty()) {
            bannerView.setVisibility(View.VISIBLE);
        }
        mAdInfos = adInfos;
        // 将最后一个ImageView添加进来
        views.clear();
        if (mAdInfos.size() > 1)
            views.add(ViewFactory.generaItemView(getActivity(), mAdInfos.get(mAdInfos.size() - 1), cycleViewPager));
        for (int i = 0; i < mAdInfos.size(); i++) {
            views.add(ViewFactory.generaItemView(getActivity(), mAdInfos.get(i), i, cycleViewPager));
        }
        // 将第一个ImageView添加进来
        if (mAdInfos.size() > 1)
            views.add(ViewFactory.generaItemView(getActivity(), mAdInfos.get(0), cycleViewPager));
        // 设置循环，在调用setData方法前调�?
        if (adInfos.size() > 1)
            cycleViewPager.setCycle(true);
        // 在加载数据前设置是否循环
        cycleViewPager.setData(views, mAdInfos, mAdCycleViewListener);
        //设置轮播
        if (adInfos.size() > 1)
            cycleViewPager.setWheel(true);
        // 设置轮播时间，默�?000ms
        if (adInfos.size() > 1)
            cycleViewPager.setTime(3000);
        //设置圆点指示图标组居中显示，默认靠右
        cycleViewPager.setIndicatorCenter();
    }

    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(ADInfo info, int position, View imageView) {
//            if (cycleViewPager.isCycle()) {
            position = position - 1;
            ADInfo adInfo = mAdInfos.get(position);
            if (TextUtils.isEmpty(adInfo.url)) {
                adInfo.url = "https://www.vgsaas.com/";
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(adInfo.url);
            intent.setData(uri);
            startActivity(intent);

//            Intent intent = new Intent(getActivity(), WebActivity.class);
//            intent.putExtra("title", adInfo.title);
//            intent.setData(Uri.parse(adInfo.url));
//            startActivity(intent);
//            }

        }

    };

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
//        Log.e("TAG_WorkFragment", "onResumeFragment()");
        if (AppModulePresenter.hasOpenedModule(WorkFragment.this.getContext(), AppModulePresenter.Type.work_flow.toString())
                || AppModulePresenter.hasOpenedModule(WorkFragment.this.getContext(), AppModulePresenter.Type.flow.toString())) {
            getUnApprovalCount();
        }
    }

    private AppModuleAdapter mAdapter;

    @Override
    public void onResume() {
        super.onResume();
//        Log.e("TAG_WorkFragment", "onResume");
        if (AppModulePresenter.hasOpenedModule(WorkFragment.this.getContext(), AppModulePresenter.Type.work_flow.toString())
                || AppModulePresenter.hasOpenedModule(WorkFragment.this.getContext(), AppModulePresenter.Type.flow.toString())) {
            getUnApprovalCount();
        }
    }

    @Override
    protected void initData() {
        getPcVerNum();
        initAd();
//        if (AppModulePresenter.hasOpenedModule(this.getContext(), AppModulePresenter.Type.work_flow.toString())) {
//            getUnApprovalCount();
//        }
        mainActivity = (MainActivity) getActivity();
        mAdapter = new AppModuleAdapter(getActivity(), 4);
        gvGridView.setAdapter(mAdapter);
        mWorkView.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, List<AppModule>>() {
            @Override
            protected List<AppModule> doInBackground(Void... params) {
                List<AppModule> appModules = AppModulePresenter.getAppOpenModules(getActivity());
                return appModules;
            }

            @Override
            protected void onPostExecute(List<AppModule> appModules) {
                Log.e("TAG_应用","appModules="+appModules.toString());
                mAdapter.add(appModules);
            }
        }.execute();
    }

    public void getUnApprovalCount() {
        if (mNetworkManager == null)
            mNetworkManager =  ((VanCloudApplication) BaseApp.getAppContext()).getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        String url = VanTopUtils.generatorUrl(this.getContext(), UrlAddr.URL_APPROVECOMMONNUM);
        params.put("loginUserCode", PrfUtils.getStaff_no(this.getContext()));
        NetworkPath path = new NetworkPath(url, params, this.getContext(), true);
        mNetworkManager.load(APPROVAL_COUNT, path, this, true);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        AppModule item = mAdapter.getItem(position);

        SharedPreferences preferences = PrfUtils.getSharePreferences(this.getActivity());
        String userId = preferences.getString("uid", "");
        String employee_no = preferences.getString("user_no", "");
        String tenantId = preferences.getString("tenantId", "");

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("employee_no", employee_no);
        params.put("tenant_id", tenantId);
        params.put("permission_id", item.id);
        params.put("operation_ip", IpUtil.getIpAddressString());
        params.put("operation_url", "");
        switch (AppModulePresenter.Type.getType(item.tag)) {
            case neigou: {
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent punch = new Intent(getActivity(), NeigouWebActivity.class);
                startActivity(punch);
            }
            break;
            //积分
            case integral:
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent integralIntent = new Intent(getActivity(), HttpWebActivity.class);
                integralIntent.putExtra("code", Constants.INTEGRAL_CODE);
                startActivity(integralIntent);
                break;
            //个税申报
            case tax:
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent taxIntent = new Intent(getActivity(), HttpWebActivity.class);
                taxIntent.putExtra("code", Constants.TAX_CODE);
                startActivity(taxIntent);
                break;
            case leadersearch: {//领导查询
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent intent = new Intent(getActivity(), HttpWebActivity.class);
                intent.putExtra("code", Constants.LEADER_SEARCH_CODE);
                startActivity(intent);
            }
            break;
            //心理体检
            case xinlitijian: {
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent intent = new Intent(getActivity(), XinlitijianWebActivity.class);
                startActivity(intent);
            }
            break;
            case clock_out: //打卡
            {
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent punch = new Intent(getActivity(), PunchCardActivity.class);
                startActivity(punch);
            }
            break;
            case kaoqin: //考勤
            {
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent punch = new Intent(getActivity(), AttendenceActivity.class);
                punch.putExtra("title", getString(item.resName));
                startActivity(punch);
            }
            break;
            case salary: //工资
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent salary = new Intent(getActivity(), SalaryMainActivity.class);
                salary.putExtra("is_show_pwd_dialog", mIsShowSalaryPwdDialog);
                startActivity(salary);
                break;
            case vote: //问卷调查
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent qustion = new Intent(getActivity(), QuestionnaireActivity.class);
                startActivity(qustion);
                break;
            case order: //订单管理
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent order = new Intent(getActivity(), OrderListActivity.class);
                startActivity(order);
                break;

            case task: //任务
                BehaviorStatistics.getInstance().startBehavior(params);

//                Intent task = new Intent(getActivity(), SimpleActivity.class);
//                startActivity(task);
                Intent task = new Intent(getActivity(), TaskActivity.class);
                startActivity(task);
                break;
            case calendar: //日程
                BehaviorStatistics.getInstance().startBehavior(params);

//                Intent schedule = new Intent(getActivity(), RdOssMainActivity.class);
//                startActivity(schedule);
                Intent schedule = new Intent(getActivity(), ScheduleHomeActivity.class);
                startActivity(schedule);
                break;
            case notice: //公告
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent announcement = new Intent(getActivity(), AnnouncementListActivity.class);
                startActivity(announcement);
                break;
            case work_reportting: //工作汇报
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent wrkreport = new Intent(getActivity(), WorkReportNewActivity.class);
                startActivity(wrkreport);
                break;
            case help: //帮帮
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent help = new Intent(getActivity(), HelpListActivity.class);
                startActivity(help);
                break;
            case work_flow: //审批

                Intent intent_work = new Intent(getActivity(), ApprovalMainActivity.class);
                startActivity(intent_work);
                break;
            case shenqing: //申请

                Intent applyIntent = new Intent(getActivity(), ApplyMainActivity.class);
                startActivity(applyIntent);
                break;
            case topic: //分享
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent share = new Intent(getActivity(), ShareActivity.class);
                startActivity(share);
                break;
            case zhaopin: //招聘
                BehaviorStatistics.getInstance().startBehavior(params);

                startActivity(new Intent(getActivity(), ResumeHomeActivity.class));
                break;
//            case AppModuleAdapter.WORK_TYPE_FINANCE: //财务管理
//                Intent finance = new Intent(getActivity(), FinanceManageMentActivity.class);
//                startActivity(finance);
//                break;
            case beidiao: //vantop背景调查
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent intent = new Intent(getActivity(), BeidiaoActivity.class);
                intent.putExtra("title", getString(R.string.lable_investigate));
                startActivity(intent);
                break;
            case bgdiaocha: //万客极速背调
                BehaviorStatistics.getInstance().startBehavior(params);
                Intent intentEntryapprove = new Intent(getActivity(), HttpWebActivity.class);
                intentEntryapprove.putExtra("code", Constants.BGDIAOCHA_CODE);
                startActivity(intentEntryapprove);
                break;
            case entryapprove: //入职审批

                BehaviorStatistics.getInstance().startBehavior(params);
                Intent intentBG = new Intent(getActivity(), HttpWebActivity.class);
                intentBG.putExtra("code", Constants.ENTRYAPPROVE_CODE);
                startActivity(intentBG);

                break;

            case flow: //流程
                BehaviorStatistics.getInstance().startBehavior(params);

                startActivity(new Intent(getActivity(), ProcessWebActivity.class));
                break;

            case jixiao: //绩效
                BehaviorStatistics.getInstance().startBehavior(params);

                startActivity(new Intent(getActivity(), JixiaoWebActivity.class));
                break;

            case gongzuorizhi: //工作日志
                BehaviorStatistics.getInstance().startBehavior(params);

                startActivity(new Intent(getActivity(), WorklogActivity.class));
                break;
            case secdefreport: //报表
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent itn = new Intent(getActivity(), HttpWebActivity.class);
                itn.putExtra("code", Constants.REPORT_FORM_CODE);
                startActivity(itn);
                break;

            case see_more:
                AppListFragment fragment = new AppListFragment();
                controller.pushFragment(fragment);
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    @Inject
    Controller controller;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                bannerView.setVisibility(View.GONE);
                break;
            case R.id.more_layout:
                AppListFragment fragment = new AppListFragment();
                controller.pushFragment(fragment);
                break;
            default:
                break;
        }
    }

    private void getPcVerNum() {
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this.getActivity(), UrlAddr.URL_PC_VER_NUM), null, this.getActivity(), true);
        getApplication().getNetworkManager().load(CALLBACK_PC_VER_NUM, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                boolean safe = VanTopActivityUtils.prehandleNetworkData(WorkFragment.this.getActivity(), null, callbackId, path, rootData, false);
                if (safe) {
                    try {
                        String verName = rootData.getJson().getJSONObject("data").getString("verName");
                        String ver_name = verName.replace(".", "");
                        if (ver_name.length() > 3) {
                            ver_name = ver_name.substring(0, 3);
                        }
                        try {
                            int tar_ver_name = Integer.parseInt(ver_name);
                            if (tar_ver_name >= 321) {
                                getIsShowPwd();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {

            }
        }, true);
    }

    private void getIsShowPwd() {
        if (getActivity() != null && getActivity().getApplicationContext()!= null){
            SharedPreferences preferences = PrfUtils.getSharePreferences(this.getActivity().getApplicationContext());
            String employee_no = preferences.getString("user_no", "");

            HashMap<String, String> params = new HashMap<>();
            params.put("staff_no", employee_no);

            NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this.getActivity(), UrlAddr.URL_IS_SHOW_PWD), params, this.getActivity(), true);
            getApplication().getNetworkManager().load(CALLBACK_IS_SHOW_PWD, path, new HttpListener<String>() {

                @Override
                public void onErrorResponse(VolleyError error) {

                }

                @Override
                public void onResponse(String response) {

                }

                @Override
                public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                    boolean safe = VanTopActivityUtils.prehandleNetworkData1(WorkFragment.this.getActivity(), null, callbackId, path, rootData, false);
                    if (safe) {
                        try {
                            mIsShowSalaryPwdDialog = rootData.getJson().getJSONObject("data").getBoolean("is_show");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, true);
        }
    }

    private void initAd() {
        String size_flag = "";
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;
        if (height <= 1280) {
            size_flag = "small";
        } else {
            size_flag = "big";
        }

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<>();
//        params.put("u_id", PrfUtils.getUserId(getActivity()));
//        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("user_id", "");
        params.put("tenant_id", "");
        params.put("size_flag", size_flag);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_HORIZONTAL_AD), params, getActivity());
//        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_AD), params, getActivity());
        mNetworkManager.load(CALLBACK_AD, path, this, true);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe;
        if (callbackId == APPROVAL_COUNT) {
            safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, false);
        } else {
            safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        }
//        Log.e("WorkFragment", "safe=" + safe + ";callbackId=" + callbackId);
        if (callbackId == APPROVAL_COUNT) {
            try {
                if (rootData.getJson() == null) {
                    Log.e("WorkFragment", "dataLoaded: 获取未审批条目个数失败！！！");
                    return;
                }
                JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                int un_approve_lea = Integer.parseInt(jsonObject.getString("un_approve_lea"));
                int un_approve_car = Integer.parseInt(jsonObject.getString("un_approve_car"));
                int un_approve_ot = Integer.parseInt(jsonObject.getString("un_approve_ot"));
                int un_approve_flow = Integer.parseInt(jsonObject.getString("un_approve_flow"));
                if (mAdapter != null) {
                    mAdapter.updateNum(AppModulePresenter.Type.work_flow, un_approve_lea + un_approve_car + un_approve_ot);
                    mAdapter.updateNum(AppModulePresenter.Type.flow, un_approve_flow);
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }
        if (!safe) {
            switch (callbackId) {
                case CALLBACK_AD:
                    int[] adNormalIds = {R.mipmap.ad_normal_1, R.mipmap.ad_normal_2, R.mipmap.ad_normal_3, R.mipmap.ad_normal_4, R.mipmap.ad_normal_5};
                    ArrayList<ADInfo> adInfos = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        ADInfo adInfo = new ADInfo();
                        adInfo.picture_address = "res://" + getActivity().getPackageName() + "/" + adNormalIds[i];
                        adInfos.add(adInfo);
                    }
                    initCycleView(adInfos, false);
                    break;
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_AD:
                /*try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    boolean visiable = jsonObject.getBoolean("visible");
                    List<ADInfo> adInfos = JsonDataFactory.getDataArray(ADInfo.class, jsonObject.getJSONArray("pictures"));
                    if (mAdInfos != null && mAdInfos.size() == adInfos.size())
                        return;
                    initCycleView(adInfos, visiable);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

                ArrayList<ADInfo> adInfos = new ArrayList<>();
                Gson gson = new Gson();
                HorizontalAD horizontalAD = gson.fromJson(rootData.getJson().toString(), HorizontalAD.class);

                if (horizontalAD.getData().isIs_show()) {
                    List<HorizontalAD.DataBeanX.DataBean> data = horizontalAD.getData().getData();
                    for (int i = 0; i < data.size(); i++) {
                        HorizontalAD.DataBeanX.DataBean dataBean = data.get(i);
                        if (dataBean.isShow()) {
                            ADInfo adInfo = new ADInfo();
                            adInfo.id = dataBean.getId();
                            adInfo.picture_address = dataBean.getImg_url();
                            adInfo.url = dataBean.getImg_href();
                            adInfos.add(adInfo);
                        }
                    }
                    if (adInfos.size() > 0) {
                        bannerView.setVisibility(View.VISIBLE);
                        initCycleView(adInfos, false);
                    } else {
                        bannerView.setVisibility(View.GONE);
                    }
                } else {
                    bannerView.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
//        bannerView.setVisibility(View.GONE);
        int[] adNormalIds = {R.mipmap.ad_normal_1, R.mipmap.ad_normal_2, R.mipmap.ad_normal_3, R.mipmap.ad_normal_4, R.mipmap.ad_normal_5};
        ArrayList<ADInfo> adInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ADInfo adInfo = new ADInfo();
            adInfo.picture_address = "res://" + getActivity().getPackageName() + "/" + adNormalIds[i];
            adInfos.add(adInfo);
        }
        initCycleView(adInfos, false);
    }

    @Override
    public void onResponse(String response) {

    }
}