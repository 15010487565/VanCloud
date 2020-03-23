package com.vgtech.vancloud.ui.module.schedule.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.ScheduleItem;
import com.vgtech.common.api.ScheduleisExist;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.CalendarUtils;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.view.calendar.OnDateSelectListener;
import com.vgtech.common.view.calendar.WeekFragment;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.ScheduleAdapter;
import com.vgtech.vancloud.ui.module.schedule.ScheduleFragmentActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by app02 on 2015/9/16.
 */
public class MyselfScheduleFragment extends BaseFragment implements HttpListener<String>,
        AbsListView.OnScrollListener, ScheduleFragmentActivity.ScheduleListLoadFragment, OnDateSelectListener {

    private final int GET_SCHEDULE_LIST = 1;
    private static final int CALLBACK_ISEXIST = 2;

    private NetworkManager mNetworkManager;
    private ScheduleFragmentActivity activity;
    private String mLastId;

    private ListView listView;
    private List<ScheduleItem> userList;
    private ScheduleAdapter adapter;

    private String currentTime;
    private String begintime;
    private String endtime;
    private String keyword;
    private ScheduleFragmentActivity.OnTimeSearchListener timeListener;
    private List<ScheduleisExist> scheduleisExists;
    ViewPager viewPager;
    ScreenSlidePagerAdapter screenSlidePagerAdapter;

    private VancloudLoadingLayout loadingLayout;

    @Override
    protected int initLayoutId() {
        return R.layout.list_view_only;
    }
    private int permission;
    @Override
    protected void initView(View view) {
        permission = getArguments().getInt("type");
        activity = (ScheduleFragmentActivity) getActivity();
        listView = (ListView) view.findViewById(R.id.listview);
        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                //TODO
                type = NOMAL_SEARCH;
                loadData(mNextId = null, null);
            }
        });

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        screenSlidePagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), this);
        viewPager.setAdapter(screenSlidePagerAdapter);
        viewPager.setCurrentItem(500);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Date myDate = CalendarUtils.getSelectWeek(position);
                Date sunday = CalendarUtils.getNowWeekMonday(myDate, Calendar.SUNDAY);
                Date saturday = CalendarUtils.getNowWeekMonday(myDate, Calendar.SATURDAY);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String str1 = format.format(sunday);
                String str2 = format.format(saturday);
                long startdate = DateTimeUtil.stringToLong_YMd(str1);
                long enddate = DateTimeUtil.stringToLong_YMd(str2);
                isExistData(String.valueOf(startdate), String.valueOf(enddate));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (receiver != null) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver);
            receiver = null;
        }
        receiver = new Receiver();
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, new IntentFilter(Actions.ACTION_CANREFRESH));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        intentFilter.addAction(BaseActivity.RECEIVER_ERROR);
        activity.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void initData() {
        userList = new ArrayList<ScheduleItem>();
        adapter = new ScheduleAdapter(this, userList);
        listView.setAdapter(adapter);
        type = NOMAL_SEARCH;
        loadData(mNextId, null);
    }

    @Override
    protected void initEvent() {
        listView.setOnScrollListener(this);
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }


    private String mNextId;
    private boolean mHasData;
    private boolean mSafe;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        if (view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            boolean flag = false;
            if (!TextUtils.isEmpty(mLastId)) {
                if (!mLastId.equals(mNextId))
                    flag = true;
            }
            if (flag && mSafe && mHasData) {
                type = NOMAL_SEARCH;
                loadData(mNextId, keyword);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }

    private static final String ADVANCE_SEARCH = "1";
    private static final String NOMAL_SEARCH = "0";
    private String type;

    //网络请求
    private void loadData(String nextId, String keywork) {
        if (TextUtil.isEmpty(keywork)) {
            this.keyword = keywork;
        }
        loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = activity.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(activity));
        params.put("tenantid", PrfUtils.getTenantId(activity));
        params.put("permission", ""+permission);

        String startDate = "";

        if (TextUtils.isEmpty(begintime)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            startDate = String.valueOf(DateTimeUtil.stringToLong_YMd(str));
        } else {
            startDate = begintime;
        }

        String endDate = "";
        if (TextUtils.isEmpty(endtime)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            endDate = String.valueOf(DateTimeUtil.stringToLong_YMd(str));
        } else {
            endDate = endtime;
        }


        params.put("startdate", startDate);
        params.put("enddate", endDate);
        params.put("n", "10000");
        if (!TextUtils.isEmpty(nextId))
            params.put("s", nextId);
        else
            params.put("s", "0");
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(activity, URLAddr.URL_SCHEDULE_LIST), params, getActivity());
        mNetworkManager.load(GET_SCHEDULE_LIST, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
    }

    private void isExistData(String startdate, String enddate) {
        mNetworkManager = activity.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("startdate", startdate);
        params.put("enddate", enddate);
        params.put("permission", ""+permission);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SCHEDULE_ISEXIST), params, getActivity());
        mNetworkManager.load(CALLBACK_ISEXIST, path, this, true);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        mSafe = ActivityUtils.prehandleNetworkData(activity, this, callbackId, path, rootData, true);
        if (!mSafe) {
            if (callbackId == GET_SCHEDULE_LIST) {
                loadingLayout.dismiss(listView);
                if (adapter.getCount() == 0) {
                    loadingLayout.showErrorView(listView);
                }
            }
            return;
        }
        switch (callbackId) {
            case GET_SCHEDULE_LIST:
                loadingLayout.dismiss(listView);
                List<ScheduleItem> scheduleItems = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mNextId = jsonObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    scheduleItems = JsonDataFactory.getDataArray(ScheduleItem.class, jsonObject.getJSONArray("rows"));
                    adapter.clear();
                    userList.addAll(scheduleItems);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (scheduleItems != null && scheduleItems.size() > 0) {

                } else {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_schedule_info_list), true, true);
                }
                break;
            case CALLBACK_ISEXIST:
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    scheduleisExists = JsonDataFactory.getDataArray(ScheduleisExist.class, jsonObject.getJSONArray("rows"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int index = viewPager.getCurrentItem();
                WeekFragment weekFragment = (WeekFragment) screenSlidePagerAdapter.instantiateItem(viewPager, index);
                weekFragment.refresh(scheduleisExists);
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
    public void load(String startTime, String endTime) {

    }

    @Override
    public void onSelected(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = format.format(date);
        begintime = String.valueOf(DateTimeUtil.stringToLong_YMd(str));
        endtime = begintime;
        currentTime = endtime;
        if (timeListener != null)
            timeListener.timeJump(Utils.dateFormatToDate(date.getTime()));
        userList.clear();
        adapter.notifyDataSetChanged();
        type = NOMAL_SEARCH;
        loadData(mNextId = null, null);

        SimpleDateFormat sdf = new SimpleDateFormat(activity.getString(R.string.date_format_for_schedule_list_tittle_time));
        activity.setTime(sdf.format(date));
    }

    @Override
    public void onSelected(String date) {
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private OnDateSelectListener onDateSelectListener;

        public ScreenSlidePagerAdapter(FragmentManager fragmentManager, OnDateSelectListener listener) {
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

    public void setTimeSearchListener(ScheduleFragmentActivity.OnTimeSearchListener timeListener) {
        this.timeListener = timeListener;
    }

    @Override
    public void searchRequest(String keyword, String startTime, String endTime) {
        super.searchRequest(keyword, startTime, endTime);
        userList.clear();
//        this.keyword = keyword;
        adapter.notifyDataSetChanged();
        type = ADVANCE_SEARCH;
        searchData(mNextId = null, keyword, startTime, endTime);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);

                switch (receiverType) {

                    case PublishTask.PUBLISH_COMMENT:
                        int position = intent.getIntExtra("position", -1);
                        int commentType = intent.getIntExtra("commentType", -1);
                        if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_SCHEDULE) {
                            ScheduleFragmentActivity scheduleFragmentActivity = (ScheduleFragmentActivity) getActivity();
                            if (scheduleFragmentActivity.subordinateTypeCurrentTab == 0) {
                                adapter.chaneCommentNum(position);
                            }
                        }
                        break;
                    case PublishTask.PUBLISH_SCHEDULE:
                        userList.clear();
                        type = NOMAL_SEARCH;
                        loadData(mNextId = null, null);
                        isRefreshRedDootAction();
                        break;
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT:
                        userList.clear();
                        type = NOMAL_SEARCH;
                        loadData(mNextId = null, null);
                        break;
                }
            } else if (BaseActivity.RECEIVER_ERROR.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_SCHEDULE:
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT:
                        String msg = intent.getStringExtra("rootDataMsg");
                        if (!TextUtil.isEmpty(msg)) {
                            ScheduleFragmentActivity scheduleFragmentActivity = (ScheduleFragmentActivity) getActivity();
                            if (scheduleFragmentActivity.subordinateTypeCurrentTab == 0) {
                                userList.clear();
                                type = NOMAL_SEARCH;
                                loadData(mNextId = null, null);
                                if (showToast)
                                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
            }
        }
    };

    private Receiver receiver;

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACTION_CANREFRESH.equals(intent.getAction())) {
                loadData(mNextId, null);
            }
        }
    }

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isRefreshRedDootAction();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = format.format(new Date());
        begintime = String.valueOf(DateTimeUtil.stringToLong_YMd(str));
        endtime = begintime;
    }

    public void isRefreshRedDootAction() {
        Date sunday = DataUtils.getFirstDayOfWeek(new Date());
        Date saturday = DataUtils.getLastDayOfWeek(new Date());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str1 = format.format(sunday);
        String str2 = format.format(saturday);
        long startdate = DateTimeUtil.stringToLong_YMd(str1);
        long enddate = DateTimeUtil.stringToLong_YMd(str2);
        isExistData(String.valueOf(startdate), String.valueOf(enddate));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                int position = data.getIntExtra("position", -1);
                int type = data.getIntExtra("type", -1);
                ScheduleFragmentActivity scheduleFragmentActivity = (ScheduleFragmentActivity) getActivity();
                if (scheduleFragmentActivity.subordinateTypeCurrentTab == 0) {
                    adapter.chaneScheduleState(position, type);
                }
                if (position >= 0) {
                    String json = data.getStringExtra("json");
                    ScheduleItem s = null;
                    try {
                        if (!TextUtil.isEmpty(json))
                            s = JsonDataFactory.getData(ScheduleItem.class, new JSONObject(json));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    adapter.chaneTask(position, s.getJson());
                }
                loadData(mNextId, null);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null)
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver);
        receiver = null;
        if (mReceiver != null)
            activity.unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private void searchData(String nextId, String keywork, String sTime, String eTime) {
        loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = activity.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(activity));
        params.put("tenantid", PrfUtils.getTenantId(activity));
        params.put("permission", ""+permission);

        if (!TextUtils.isEmpty(sTime) && getString(R.string.no_time).equals(sTime)) {
            sTime = begintime;
        } else if (!TextUtils.isEmpty(sTime) && !getString(R.string.no_time).equals(sTime)) {
            sTime = DateTimeUtil.stringToLong_YMd(sTime) + "";
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            sTime = String.valueOf(DateTimeUtil.stringToLong_YMd(str));
        }

        if (!TextUtils.isEmpty(eTime) && getString(R.string.no_time).equals(eTime)) {
            eTime = endtime;
        } else if (!TextUtils.isEmpty(eTime) && !getString(R.string.no_time).equals(eTime)) {
            eTime = DateTimeUtil.stringToLong_YMd(eTime) + "";
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            eTime = String.valueOf(DateTimeUtil.stringToLong_YMd(str));
        }
        params.put("startdate", sTime);
        params.put("enddate", eTime);
        if (!TextUtil.isEmpty(keywork)) {
            params.put("keyword", keywork);
        }
        params.put("n", "50");
        params.put("s", "0");
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(activity, URLAddr.URL_SEARCH_CALENDAR), params, getActivity());
        mNetworkManager.load(GET_SCHEDULE_LIST, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
    }

    @Override
    public void toRefresh() {

        userList.clear();
        type = NOMAL_SEARCH;
        loadData(mNextId = null, null);
        super.toRefresh();
    }
}
