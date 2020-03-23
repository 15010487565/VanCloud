package com.vgtech.vancloud.ui.worklog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.CostCenter;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.ScheduleisExist;
import com.vgtech.common.api.WorkLogBean;
import com.vgtech.common.api.WorkLogSubBean;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.CalendarUtils;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.view.calendar.OnDateSelectListener;
import com.vgtech.common.view.calendar.WeekFragment;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.CalendarTitleGridAdapter;
import com.vgtech.vancloud.ui.adapter.WorkLogAdapter;
import com.vgtech.vancloud.ui.adapter.WorkLogSubAdapter;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorklogActivity extends SearchBaseActivity implements HttpListener<String>, OnDateSelectListener, PullToRefreshBase.OnRefreshListener2 {

    private static final int CALLBACK_MINE_WORK_LOG_LIST = 0;
    private static final int CALLBACK_WORK_LOG_COST_CENTER = 1;
    private static final int CALLBACK_SUB_SUM_WORK_LOG_LIST = 2;
    private static final int CALLBACK_MINE_SUBMIT_WORK_LOG_LIST = 3;
    private static final int CALLBACK_REVOKE_WORK_LOG = 4;
    private static final int CALLBACK_WORK_LOG_EXIST_DATAS = 5;
    private static final int CALLBACK_WORK_LOG_SUB_EXIST_DATAS = 6;
    private static final int CREATE_WORK_LOG = 10;
    private static final int UPDATE_WORK_LOG = 11;
    private static final int MINE_TYPE = 0x01;
    private static final int SUB_TYPE = 0x02;
    private static final int SHOW_ERROR = 21;
    private static final int SHOW_EMPTY = 22;
    private static final int SHOW_CONTENT = 23;
    private static final int SHOW_LOADING = 24;
    private static int TYPE_MASK = 0x00;
    private int mCurType = MINE_TYPE;
    private TextView mTitleDate;
    private LinearLayout mTypeGroup;
    private RelativeLayout mMineBtn;
    private RelativeLayout mSubordinateBtn;
    private VancloudLoadingLayout mLoadingView;
    private NetworkManager mNetworkManager;
    private PullToRefreshListView mListView;
    private WorkLogAdapter mMineAdapter;
    private WorkLogSubAdapter mSubAdapter;
    private boolean mIsInit;
    private boolean mIsPullRefresh;
    private GridView mWeekTitleGridView;
    private ViewPager mCalendarViewPager;
    private CalendarPagerAdapter mCalendarPagerAdapter;
    public static List<CostCenter> mCostCentersList;
    private int mPageNum = 1;
    private String mDate;
    private int mTotalPage;
    private LinearLayout mListViewParent;
    private Button mBtnSubmit;
    private boolean mIsSubmit;
    private int mShowState = SHOW_CONTENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worklog);

        try {
            String jsonTenant = PrfUtils.getPrfparams(this, "moudle_permissions");
            JSONArray jsonArray = new JSONArray(jsonTenant);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if ("gongzuorizhi".equals(jsonObject.getString("tag"))) {
                    JSONArray permissions = jsonObject.getJSONArray("permissions");
                    for (int j = 0; j < permissions.length(); j++) {
                        JSONObject jobj = permissions.getJSONObject(j);
                        if ("gongzuorizhi:wode".equals(jobj.getString("tag"))) {
                            TYPE_MASK |= MINE_TYPE;
                        }
                        if ("gongzuorizhi:xiashu".equals(jobj.getString("tag"))) {
                            TYPE_MASK |= SUB_TYPE;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initView();
        initData();
    }

    private void initData() {
        mNetworkManager = getAppliction().getNetworkManager();

        //获取成本中心列表
        getCostCenterList();

        Date date = new Date();
        Date weekFirstDate = CalendarUtils.getNowWeekMonday(date, Calendar.SUNDAY);
        Date weekLastDate = CalendarUtils.getNowWeekMonday(date, Calendar.SATURDAY);
        String startDate = DateTimeUtil.longToString_YMd(weekFirstDate.getTime());
        String endDate = DateTimeUtil.longToString_YMd(weekLastDate.getTime());
        getExistLogDatas(startDate, endDate);

        mDate = DateTimeUtil.getCurrentString_YMd();
        getList(1, true, false);
    }

    private void getExistLogDatas(String startDate, String endDate) {
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        if (mCurType == MINE_TYPE) {
            NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_WORK_LOG_EXIST_DATAS), params, this);
            mNetworkManager.load(CALLBACK_WORK_LOG_EXIST_DATAS, path, this);
        } else {
            NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_WORK_LOG_SUB_EXIST_DATAS), params, this);
            mNetworkManager.load(CALLBACK_WORK_LOG_SUB_EXIST_DATAS, path, this);
        }
    }

    private void getList(int pageNum, boolean isInit, boolean isPullRefresh) {
        //获取工作日志列表
        mIsInit = isInit;
        mIsPullRefresh = isPullRefresh;
        if (mCurType == MINE_TYPE) {
            getMineWorkLogList(pageNum);
            mListView.setAdapter(mMineAdapter);
        } else if (mCurType == SUB_TYPE) {
            getSubWorkLogList(pageNum);
            mListView.setAdapter(mSubAdapter);
        }
    }

    private void getCostCenterList() {
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_WORK_LOG_COST_CENTER), params, this);
        mNetworkManager.load(CALLBACK_WORK_LOG_COST_CENTER, path, this);
    }

    private void getSubWorkLogList(int pageNum) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("date", mDate);
        params.put("page_size", "10");
        params.put("page_now", pageNum + "");
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_SUB_SUM_WORK_LOG_LIST), params, this);
        mNetworkManager.load(CALLBACK_SUB_SUM_WORK_LOG_LIST, path, this);
        if (!mIsPullRefresh) {
            mLoadingView.showLoadingView(mListViewParent, "", false);
            mShowState = SHOW_LOADING;
        }
    }

    private void getMineWorkLogList(int pageNum) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("date", mDate);
        params.put("page_size", "10");
        params.put("page_now", pageNum + "");
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_MINE_WORK_LOG_LIST), params, this);
        mNetworkManager.load(CALLBACK_MINE_WORK_LOG_LIST, path, this);
        if (!mIsPullRefresh) {
            mLoadingView.showLoadingView(mListViewParent, "", false);
            mShowState = SHOW_LOADING;
        }
    }

    /**
     * 初始化标题
     */
    private void initView() {
        initTitleLayout();
        setTitleText(getString(R.string.lable_worklog));
        setTitleCenter();
        //标题日期
        mTitleDate = (TextView) findViewById(R.id.tv_title_date);
        String date = DateTimeUtil.getCurrentString("yyyy/MM");
        mTitleDate.setText(date);

        if ((TYPE_MASK & MINE_TYPE) == MINE_TYPE && (TYPE_MASK & SUB_TYPE) == SUB_TYPE) {
            arrowView.setVisibility(View.VISIBLE);
        } else if ((TYPE_MASK & MINE_TYPE) == MINE_TYPE && (TYPE_MASK & SUB_TYPE) == 0) {
            arrowView.setVisibility(View.GONE);
        }
        //我的、下属
        mTypeGroup = (LinearLayout) findViewById(R.id.type_group);
        mTypeGroup.setTag(true);

        mMineBtn = (RelativeLayout) findViewById(R.id.my_button);
        mSubordinateBtn = (RelativeLayout) findViewById(R.id.subordinate_button);
        mMineBtn.setOnClickListener(this);
        mSubordinateBtn.setOnClickListener(this);

        mWeekTitleGridView = (GridView) findViewById(R.id.week_grid_view);
        mWeekTitleGridView.setAdapter(new CalendarTitleGridAdapter(this));

        mCalendarViewPager = (ViewPager) findViewById(R.id.calendar_viewpager);
        mCalendarPagerAdapter = new CalendarPagerAdapter(getSupportFragmentManager(), this);
        mCalendarViewPager.setAdapter(mCalendarPagerAdapter);
        mCalendarViewPager.setCurrentItem(500);
        mCalendarViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                WeekFragment weekFragment = (WeekFragment) mCalendarPagerAdapter.instantiateItem(mCalendarViewPager, position);
                long d = DateTimeUtil.stringToLong_YMd(mDate);
                weekFragment.selectSpecifiedDate(d);

                Date date = CalendarUtils.getSelectWeek(position);
                Date weekFirstDate = CalendarUtils.getNowWeekMonday(date, Calendar.SUNDAY);
                Date weekLastDate = CalendarUtils.getNowWeekMonday(date, Calendar.SATURDAY);
                String startDate = DateTimeUtil.longToString_YMd(weekFirstDate.getTime());
                String endDate = DateTimeUtil.longToString_YMd(weekLastDate.getTime());
                getExistLogDatas(startDate, endDate);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.loading_layout);
        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                Date date = new Date(DateTimeUtil.stringToLong_YMd(mDate));
                Date weekFirstDate = CalendarUtils.getNowWeekMonday(date, Calendar.SUNDAY);
                Date weekLastDate = CalendarUtils.getNowWeekMonday(date, Calendar.SATURDAY);
                String startDate = DateTimeUtil.longToString_YMd(weekFirstDate.getTime());
                String endDate = DateTimeUtil.longToString_YMd(weekLastDate.getTime());
                getExistLogDatas(startDate, endDate);

                getList(1, true, false);
            }
        });

        mListViewParent = (LinearLayout) findViewById(R.id.work_log_listview_parent);
        mListView = (PullToRefreshListView) findViewById(R.id.work_log_listview);
        mBtnSubmit = (Button) findViewById(R.id.work_log_submit_all);
        mBtnSubmit.setVisibility(View.GONE);
        mMineAdapter = new WorkLogAdapter(new ArrayList<WorkLogBean>());
        mSubAdapter = new WorkLogSubAdapter(new ArrayList<WorkLogSubBean>());

        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        mBtnSubmit.setOnClickListener(this);
//        if (mCurType == MINE_TYPE) {
//            if (mBtnSubmit.getVisibility() != View.VISIBLE)
//                mBtnSubmit.setVisibility(View.VISIBLE);
//        } else if (mCurType == SUB_TYPE) {
//            if (mBtnSubmit.getVisibility() != View.GONE)
//                mBtnSubmit.setVisibility(View.GONE);
//        }

        mMineAdapter.setOnRootClickListener(new WorkLogAdapter.OnRootClickListener() {
            @Override
            public void onRootClick(int position) {
                if (!mIsSubmit && mCurType == MINE_TYPE) {
                    WorkLogBean workLogBeen = mMineAdapter.getData().get(position);
                    Intent intent = new Intent(WorklogActivity.this, CreateWorkLogActivity.class);
                    intent.putExtra("date", workLogBeen.getDates());
                    intent.putExtra("work_log_bean", workLogBeen);
                    intent.putExtra("is_modify", true);
                    startActivityForResult(intent, UPDATE_WORK_LOG);
                }
            }
        });
        mSubAdapter.setOnItemClickListener(new WorkLogSubAdapter.OnItemClickListener() {
            @Override
            public void onRootClick(View view, int position) {
                Intent intent = new Intent(WorklogActivity.this, SubWorkLogDetailActivity.class);
                List<WorkLogSubBean> data = mSubAdapter.getData();
                WorkLogSubBean workLogSubBean = data.get(position);
                if (workLogSubBean.getNum() > 0) {
                    intent.putExtra("sub_name", workLogSubBean.getStaffName());
                    intent.putExtra("sub_staff_no", workLogSubBean.getStaffNo());
                    intent.putExtra("sub_work_log_date", mDate);
                    startActivity(intent);
                } else {
                    //TODO 国际化
                    Toast.makeText(WorklogActivity.this, R.string.toast_no_log, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRevokeClick(View view, int position) {
                WorkLogSubBean workLogSubBean = mSubAdapter.getData().get(position);
                Map<String, String> params = new HashMap<String, String>();
                params.put("tenant_id", PrfUtils.getTenantId(WorklogActivity.this));
                params.put("loginUserCode", PrfUtils.getStaff_no(WorklogActivity.this));
                params.put("date", workLogSubBean.getDoneDate());
                params.put("staff_no", workLogSubBean.getStaffNo());
                NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(WorklogActivity.this, UrlAddr.URL_REVOKE_SUB_WORK_LOG), params, WorklogActivity.this);
                path.setTag(position);
                mNetworkManager.load(CALLBACK_REVOKE_WORK_LOG, path, WorklogActivity.this);

                Date date = new Date(DateTimeUtil.stringToLong_YMd(mDate));
                Date weekFirstDate = CalendarUtils.getNowWeekMonday(date, Calendar.SUNDAY);
                Date weekLastDate = CalendarUtils.getNowWeekMonday(date, Calendar.SATURDAY);
                String startDate = DateTimeUtil.longToString_YMd(weekFirstDate.getTime());
                String endDate = DateTimeUtil.longToString_YMd(weekLastDate.getTime());
                getExistLogDatas(startDate, endDate);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.top_type_click:
                if ((TYPE_MASK & MINE_TYPE) == MINE_TYPE && (TYPE_MASK & SUB_TYPE) == SUB_TYPE) {
                    if (mTypeGroup.getVisibility() == View.VISIBLE) {
                        hideTopTypeselectView();
                    } else {
                        showTopTypeselectView();
                    }
                    if (mCurType == MINE_TYPE) {
                        //我的显示选中状态
                        mMineBtn.setSelected(true);
                        mSubordinateBtn.setSelected(false);
                    } else if (mCurType == SUB_TYPE) {
                        //下属显示选中状态
                        mSubordinateBtn.setSelected(true);
                        mMineBtn.setSelected(false);
                    }
                }
                break;
            case R.id.my_button:
                hideTopTypeselectView();
                if (mCurType != MINE_TYPE) {
                    mCurType = MINE_TYPE;

                    Date date = new Date(DateTimeUtil.stringToLong_YMd(mDate));
                    Date weekFirstDate = CalendarUtils.getNowWeekMonday(date, Calendar.SUNDAY);
                    Date weekLastDate = CalendarUtils.getNowWeekMonday(date, Calendar.SATURDAY);
                    String startDate = DateTimeUtil.longToString_YMd(weekFirstDate.getTime());
                    String endDate = DateTimeUtil.longToString_YMd(weekLastDate.getTime());
                    getExistLogDatas(startDate, endDate);

                    getList(1, true, false);

                    if (mBtnSubmit.getVisibility() != View.GONE)
                        mBtnSubmit.setVisibility(View.GONE);
                }
                return;
            case R.id.subordinate_button:
                hideTopTypeselectView();
                if (mCurType != SUB_TYPE) {
                    mCurType = SUB_TYPE;

                    Date date = new Date(DateTimeUtil.stringToLong_YMd(mDate));
                    Date weekFirstDate = CalendarUtils.getNowWeekMonday(date, Calendar.SUNDAY);
                    Date weekLastDate = CalendarUtils.getNowWeekMonday(date, Calendar.SATURDAY);
                    String startDate = DateTimeUtil.longToString_YMd(weekFirstDate.getTime());
                    String endDate = DateTimeUtil.longToString_YMd(weekLastDate.getTime());
                    getExistLogDatas(startDate, endDate);

                    getList(1, true, false);

                    if (mBtnSubmit.getVisibility() != View.GONE)
                        mBtnSubmit.setVisibility(View.GONE);
                }
                return;
            case R.id.add:
                Intent intent = new Intent(this, CreateWorkLogActivity.class);
                intent.putExtra("date", mDate);
                startActivityForResult(intent, CREATE_WORK_LOG);
                break;
            case R.id.search:
                Intent searchIntent = new Intent(this, WorkLogSearchActivity.class);
                searchIntent.putExtra("type", mCurType);
                startActivity(searchIntent);
                return;
            case R.id.work_log_submit_all:
                Map<String, String> params = new HashMap<String, String>();
                params.put("tenant_id", PrfUtils.getTenantId(this));
                params.put("loginUserCode", PrfUtils.getStaff_no(this));
                params.put("date", mDate);
                NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_SUBMIT_MINE_WORK_LOG), params, this);
                mNetworkManager.load(CALLBACK_MINE_SUBMIT_WORK_LOG_LIST, path, this);
                break;
        }
        super.onClick(v);
    }

    /**
     * 显示顶部分类选择布局
     */
    public void showTopTypeselectView() {

        openAnimation(mTypeGroup, arrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTypeGroup.setTag(false);
                mTypeGroup.setVisibility(View.VISIBLE);
                shadeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTypeGroup.setTag(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 隐藏顶部分类选择布局
     */
    public void hideTopTypeselectView() {


        closeAnimation(mTypeGroup, arrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTypeGroup.setTag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTypeGroup.setTag(true);
                mTypeGroup.setVisibility(View.INVISIBLE);
                shadeView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListViewParent);
        mListView.onRefreshComplete();
        boolean safe = false;
        if (callbackId == CALLBACK_MINE_WORK_LOG_LIST) {
            safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
            if (!safe) {
                if (mMineAdapter.getDataSize() <= 0) {
                    mLoadingView.showErrorView(mListViewParent, "", true, true);
                    mShowState = SHOW_ERROR;
                }
                return;
            }
        } else if (callbackId == CALLBACK_MINE_SUBMIT_WORK_LOG_LIST || callbackId == CALLBACK_REVOKE_WORK_LOG
                || callbackId == CALLBACK_WORK_LOG_EXIST_DATAS || callbackId == CALLBACK_WORK_LOG_SUB_EXIST_DATAS) {
            safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, false);
            if (!safe) {
                Toast.makeText(this, rootData.msg, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
            if (!safe) {
//                Toast.makeText(this, R.string.toast_cost_center_fialed, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        switch (callbackId) {
            case CALLBACK_MINE_WORK_LOG_LIST:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data").getJSONObject("list");
                    mIsSubmit = jsonObject.getJSONObject("data").getBoolean("isSubmit");
                    if (mIsSubmit) {
                        mBtnSubmit.setVisibility(View.GONE);
                        findViewById(R.id.add).setVisibility(View.GONE);
                    } else {
                        mBtnSubmit.setVisibility(View.VISIBLE);
                        findViewById(R.id.add).setVisibility(View.VISIBLE);
                    }
                    mTotalPage = resutObject.getInt("pageCount");
                    mPageNum = resutObject.getInt("pageNo");
                    JSONArray rows = resutObject.getJSONArray("rows");
                    List<WorkLogBean> mineWorkLogData = JsonDataFactory.getDataArray(WorkLogBean.class, rows);
                    if (mineWorkLogData.size() > 0) {
                        if (mIsInit) {
                            mMineAdapter.setData(mineWorkLogData);
                        } else {
                            mMineAdapter.addData(mineWorkLogData);
                        }
                        mShowState = SHOW_CONTENT;
                    } else {
                        if (mIsInit) {
                            mBtnSubmit.setVisibility(View.GONE);
                            //清空数据
                            mMineAdapter.setData(null);
                            mLoadingView.showEmptyView(mListViewParent, getString(R.string.no_list_data), true, true);
                            mShowState = SHOW_EMPTY;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case CALLBACK_MINE_SUBMIT_WORK_LOG_LIST:
                Toast.makeText(this, rootData.getMsg(), Toast.LENGTH_SHORT).show();
                findViewById(R.id.add).setVisibility(View.GONE);
                mBtnSubmit.setVisibility(View.GONE);
                break;

            case CALLBACK_SUB_SUM_WORK_LOG_LIST:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    mTotalPage = resutObject.getInt("pageCount");
                    mPageNum = resutObject.getInt("pageNo");
                    JSONArray rows = resutObject.getJSONArray("rows");
                    List<WorkLogSubBean> subWorkLogData = JsonDataFactory.getDataArray(WorkLogSubBean.class, rows);
                    if (subWorkLogData.size() > 0) {
                        if (mIsInit) {
                            mSubAdapter.setData(subWorkLogData);
                        } else {
                            mSubAdapter.addData(subWorkLogData);
                        }
                        mShowState = SHOW_CONTENT;
                    } else {
                        if (mIsInit) {
                            //清空数据
                            mLoadingView.showEmptyView(mListViewParent, getString(R.string.no_list_data), true, true);
                            mMineAdapter.setData(null);
                            mShowState = SHOW_EMPTY;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case CALLBACK_REVOKE_WORK_LOG:
                String msg = rootData.getMsg();
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                int position = (int) path.getTag();
                WorkLogSubBean workLogSubBean = mSubAdapter.getData().get(position);
                workLogSubBean.setIsDone("false");
                mSubAdapter.notifyDataSetChanged();
                break;

            case CALLBACK_WORK_LOG_COST_CENTER:
                try {
                    mCostCentersList = JsonDataFactory.getDataArray(CostCenter.class, rootData.getJson().getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_WORK_LOG_EXIST_DATAS:
            case CALLBACK_WORK_LOG_SUB_EXIST_DATAS:
                try {
                    JSONObject json = rootData.getJson();
                    JSONArray data = json.getJSONArray("data");
                    List<ScheduleisExist> existLogDates = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        String existDate = DateTimeUtil.stringToLong_YMd(data.getString(i)) + "";
                        ScheduleisExist existDateObj = new ScheduleisExist();
                        existDateObj.day = existDate;
                        existDateObj.num = "0";
                        existLogDates.add(existDateObj);
                    }

                    int idx = mCalendarViewPager.getCurrentItem();
                    WeekFragment weekFragment = (WeekFragment) mCalendarPagerAdapter.instantiateItem(mCalendarViewPager, idx);
                    weekFragment.refresh(existLogDates);
//                    mBtnSubmit.setVisibility(View.GONE);
                    if (mShowState == SHOW_EMPTY){
                        mLoadingView.showEmptyView(mListViewParent, getString(R.string.no_list_data), true, true);
                    }else if (mShowState == SHOW_ERROR){
                        mLoadingView.showErrorView(mListViewParent, "", true, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onSelected(Date date) {
        String d = DateTimeUtil.longToString_YMd(date.getTime());
        String replace = d.replace("-", "/");
        String title = replace.substring(0, replace.lastIndexOf("/"));
        mTitleDate.setText(title);
        if (mDate.equals(d)) {
            return;
        }
        mDate = d;
        getList(1, true, false);
    }

    @Override
    public void onSelected(String date) {

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        getList(1, true, true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        if (mPageNum + 1 > mTotalPage) {
            //TODO 国际化
            Toast.makeText(this, "最后一页，没有数据了", Toast.LENGTH_SHORT).show();
            mListView.onRefreshComplete();
            return;
        }
        getList(++mPageNum, false, true);
    }

    public class CalendarPagerAdapter extends FragmentStatePagerAdapter {
        private OnDateSelectListener onDateSelectListener;

        public CalendarPagerAdapter(FragmentManager fragmentManager, OnDateSelectListener listener) {
            super(fragmentManager);
            onDateSelectListener = listener;
        }

        @Override
        public Fragment getItem(int position) {
            WeekFragment weekFragment = WeekFragment.create(position);
            weekFragment.setDateSelectListener(onDateSelectListener);
            return weekFragment;
        }

        @Override
        public int getCount() {
            return 1000;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_WORK_LOG || requestCode == UPDATE_WORK_LOG && resultCode == RESULT_OK) {
            //刷新列表
            if (mCurType == MINE_TYPE) {
                getMineWorkLogList(mPageNum);

                Date date = new Date(DateTimeUtil.stringToLong_YMd(mDate));
                Date weekFirstDate = CalendarUtils.getNowWeekMonday(date, Calendar.SUNDAY);
                Date weekLastDate = CalendarUtils.getNowWeekMonday(date, Calendar.SATURDAY);
                String startDate = DateTimeUtil.longToString_YMd(weekFirstDate.getTime());
                String endDate = DateTimeUtil.longToString_YMd(weekLastDate.getTime());
                getExistLogDatas(startDate, endDate);
            }
        }
    }
}
