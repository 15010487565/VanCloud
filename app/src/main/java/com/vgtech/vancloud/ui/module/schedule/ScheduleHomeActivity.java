package com.vgtech.vancloud.ui.module.schedule;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
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
import com.vgtech.common.api.ScheduleisExist;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.CalendarUtils;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.calendar.CalendarFragment;
import com.vgtech.common.view.calendar.OnDateSelectListener;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;
import com.vgtech.vancloud.ui.adapter.CalendarTitleGridAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.common.record.MediaManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/9/9.
 */
public class ScheduleHomeActivity extends SearchBaseActivity implements HttpListener<String>, AbsListView.OnScrollListener, OnDateSelectListener {
    private View mWaitView;
    private NetworkManager mNetworkManager;
    private ApiDataAdapter<ScheduleMap> mScheduleAdapter;
    private ApiDataAdapter<ScheduleMap> mSchedulesubAdapter;
    private RelativeLayout openlayout;
    private NoScrollListview footviewlist;
    private ListView listView;
    private List<ScheduleisExist> scheduleisExists;
    private boolean isLoadSubData = false;
    private boolean isClickSearch = false;
    ScreenSlidePagerAdapter screenSlidePagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendarCenter = (TextView) findViewById(R.id.tv_title_date);
        mNetworkManager = getAppliction().getNetworkManager();
        findViewById(R.id.btn_schedule_more).setOnClickListener(this);
        findViewById(R.id.btn_schedule_more).setEnabled(false);
        findViewById(R.id.btn_schedule_more).setVisibility(View.VISIBLE);
        listView = (ListView) findViewById(android.R.id.list);
        View headerView = getHeadLayout();
        View footerView = getFootViewLayout();

        listView.addFooterView(footerView);
        listView.addHeaderView(headerView);
        listView.setOnScrollListener(this);
        mScheduleAdapter = new ApiDataAdapter<ScheduleMap>(this);
        mScheduleAdapter.setIsMySchedule(true);
        mSchedulesubAdapter = new ApiDataAdapter<ScheduleMap>(this);
        listView.setAdapter(mScheduleAdapter);
        footviewlist.setAdapter(mSchedulesubAdapter);
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        intentFilter.addAction(Actions.ACTION_CANREFRESH);
        intentFilter.addAction(Actions.ACTION_HIDE_MORE_VIEW);
        intentFilter.addAction(Actions.ACTION_SHOW_MORE_VIEW);
        registerReceiver(receiver, intentFilter);
        initTitleLayout();
        loadData(mNextId);
        isRefreshRedDootAction();
    }

    private static final int CALLBACK_INIT = 1;
    private static final int CALLBACKSUB_INIT = 2;
    private static final int CALLBACK_ISEXIST = 3;

    private TextView calendarCenter;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_schedule_more: {
                Intent intent = new Intent(this, ScheduleFragmentActivity.class);
                intent.putExtra("hasEmployee", mHasEmployee);
                startActivity(intent);
            }
            break;
            case R.id.btn_retry:
                retry();
                break;
            case R.id.add: {
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE);
                startActivity(intent);
            }
            break;
            case R.id.search: {
                Intent intent = new Intent(this, ScheduleSearchActivity.class);
                startActivity(intent);
            }
            break;
            default:
                super.onClick(v);
                break;
        }
    }


    private static final int CALLBACK_NEWS_INIT = 1;
    private static final int CALLBACK_NEWS = 2;
    private int mPageN;
    private String mLastId;
    private String mSubLastId;

    @Override
    protected int getContentView() {
        return R.layout.schedule_home;
    }

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        MediaManager.pause();
    }

    private long mStartTime;
    private long mEndTime;

    private void loadData(String nextId) {
        showProgress(mWaitView, true);
        if (mStartTime == 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            mStartTime = DateTimeUtil.stringToLong_YMd(str);
        }
        if (mEndTime == 0) {
            mEndTime = mStartTime;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("startdate", String.valueOf(mStartTime));
        params.put("enddate", String.valueOf(mEndTime));
        params.put("permission", "9");
        params.put("n", "10000");
        if (!TextUtils.isEmpty(nextId))
            params.put("s", nextId);

        params.put("query", "0");
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SCHEDULE_LIST), params, this);
        mNetworkManager.load(CALLBACK_INIT, path, this);
    }

    public void isRefreshRedDootAction() {
        Date firstDayOfMonth = CalendarUtils.getMonthFirstDay(new Date());
        Date lastDayOfMonth = CalendarUtils.getMonthLastDay(new Date());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str1 = format.format(firstDayOfMonth);
        String str2 = format.format(lastDayOfMonth);
        long startdate = DateTimeUtil.stringToLong_YMd(str1);
        long enddate = DateTimeUtil.stringToLong_YMd(str2);
        isExistData(String.valueOf(startdate), String.valueOf(enddate));
    }

    private Receiver receiver;

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_SCHEDULE:
                        mNextId = null;
                        loadData(mNextId);
                        isRefreshRedDootAction();
                        break;
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT:
                        mNextId = null;
                        loadData(mNextId);
                        break;
                }
            } else if (Actions.ACTION_CANREFRESH.equals(intent.getAction())) {
                loadData(mNextId);
            }
        }
    }

    @Override
    public void onSelected(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = format.format(date);
        mStartTime = DateTimeUtil.stringToLong_YMd(str);
        mEndTime = DateTimeUtil.stringToLong_YMd(str);
        mNextId = "";
        mScheduleAdapter.clear();
        mSchedulesubAdapter.clear();
        mCountView.setVisibility(View.GONE);
        openlayout.setVisibility(View.GONE);
        loadData(mNextId);
    }

    @Override
    public void onSelected(String date) {

    }

    private String mNextId;
    private String mSubNextId;
    private boolean mHasData;
    private boolean mSafe;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (!isLoadSubData) {
            boolean flag = true;
            if (!TextUtils.isEmpty(mLastId)) {
                if (!mLastId.equals(mNextId))
                    flag = false;
            }
            if (!flag && mSafe && mHasData
                    && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
                loadData(mNextId);
            }
        } else {
            boolean flag = true;
            if (!TextUtils.isEmpty(mLastId)) {
                if (!mLastId.equals(mNextId))
                    flag = false;
            }
            if (!flag && mSafe && mHasData
                    && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
                loadData(mNextId);
            }
        }


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



    private void loadSubData(String nextId) {
//        showProgress(mWaitView, true);
        if (mStartTime == 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            mStartTime = DateTimeUtil.stringToLong_YMd(str);
        }
        if (mEndTime == 0) {
            mEndTime = mStartTime;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("startdate", String.valueOf(mStartTime));
        params.put("enddate", String.valueOf(mEndTime));
        params.put("permission", "7");
        params.put("n", "10000");
        if (!TextUtils.isEmpty(nextId))
            params.put("s", nextId);
        mSubLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SCHEDULE_LIST), params, this);
        mNetworkManager.load(CALLBACKSUB_INIT, path, this);
    }

    private boolean mHasEmployee;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, false);
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
                    mCountView.setVisibility(View.VISIBLE);
                    mCountTv.setText(getString(R.string.lable_schedule_count, count));
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
//                    jsonObject.getJSONArray("rows")
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
                    mHasEmployee = false;
                    if (jsonObject.has("hasPermission")) {
                        mHasEmployee = jsonObject.getBoolean("hasPermission");
                    }
                    findViewById(R.id.btn_schedule_more).setEnabled(true);
                    openlayout.setVisibility(mHasEmployee ? View.VISIBLE : View.GONE);
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
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    List<ScheduleItem> calendarItems = new ArrayList<>();
                    Object object = jsonObject.get("rows");
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
                    mSchedulesubAdapter.clear();
                    mSchedulesubAdapter.add(scheduleMaps);
                } catch (Exception e) {
                    e.printStackTrace();
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
                CalendarFragment calendarFragment = (CalendarFragment) screenSlidePagerAdapter.instantiateItem(viewPager, index);
                calendarFragment.refresh(scheduleisExists);

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
                    loadSubData(mSubNextId);
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

    String month;
    private TextView mCountTv;
    private View mScheduleStartView;
    private View mCountView;

    public View getHeadLayout() {

        View headerView = getLayoutInflater().inflate(R.layout.schedule_header_layout, null);
        mWaitView = headerView.findViewById(R.id.progress_view_schedule);
        mWaitView.findViewById(R.id.btn_retry).setOnClickListener(this);
        mCountView = headerView.findViewById(R.id.schedule_count_view);
        mScheduleStartView = headerView.findViewById(R.id.schedule_start_line);
        mCountTv = (TextView) headerView.findViewById(R.id.tv_schedule_count);
        GridView titleGridView = (GridView) headerView.findViewById(R.id.title_gridview);
        titleGridView.setAdapter(new CalendarTitleGridAdapter(ScheduleHomeActivity.this));
        viewPager = (ViewPager) headerView.findViewById(R.id.viewpager);
        screenSlidePagerAdapter = new ScreenSlidePagerAdapter(
                getSupportFragmentManager(), this);
        viewPager.setAdapter(screenSlidePagerAdapter);
        viewPager.setCurrentItem(500);
        month = Calendar.getInstance().get(Calendar.YEAR)
                + "/"
                + CalendarUtils.LeftPad_Tow_Zero(Calendar.getInstance().get(
                Calendar.MONTH) + 1);
        calendarCenter.setText(month);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                final Calendar calendar = CalendarUtils.getSelectCalendar(position);
                month = calendar.get(Calendar.YEAR)
                        + "/"
                        + CalendarUtils.LeftPad_Tow_Zero(calendar.get(Calendar.MONTH) + 1);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Integer temyear = calendar.get(Calendar.YEAR);
                        String temmonth = CalendarUtils.LeftPad_Tow_Zero(calendar.get(Calendar.MONTH) + 1);
                        /**获取当前日期所在月的第一天和最后一天*/
                        Date firstDayOfMonth = DataUtils.getFirstDayOfMonth(temyear, Integer.valueOf(temmonth));
                        Date lastDayOfMonth = DataUtils.getLastDayOfMonth(temyear, Integer.valueOf(temmonth));
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        String str1 = format.format(firstDayOfMonth);
                        String str2 = format.format(lastDayOfMonth);
                        long startdate = DateTimeUtil.stringToLong_YMd(str1);
                        long enddate = DateTimeUtil.stringToLong_YMd(str2);

                        isExistData(String.valueOf(startdate), String.valueOf(enddate));
                    }
                }, 300);

                calendarCenter.setText(month);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        return headerView;

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private OnDateSelectListener dateSelectListener;

        public ScreenSlidePagerAdapter(FragmentManager fragmentManager, OnDateSelectListener listener) {
            super(fragmentManager);
            dateSelectListener = listener;
        }

        @Override
        public Fragment getItem(int position) {
            CalendarFragment calendarFragment = CalendarFragment.create(position);
            calendarFragment.setDateSelectListener(dateSelectListener);
            return calendarFragment;
        }

        @Override
        public int getCount() {
            return 1000;
        }
    }

    boolean isSearch = false;
    boolean isSearchSubData = false;

    @Override
    public void searchRequest() {
        isSearch = true;
        searchData("0", serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
        if (isLoadSubData) {
            isSearchSubData = true;
            searchSubData("0", serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
        }
    }

    /**
     * 日程是否存在
     */
    private void isExistData(String startdate, String enddate) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("startdate", startdate);
        params.put("enddate", enddate);
        params.put("permission", "9");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SCHEDULE_ISEXIST), params, this);
        mNetworkManager.load(CALLBACK_ISEXIST, path, this);
    }


    private void searchData(String nextId, String keyword, String sTime, String eTime) {
        showProgress(mWaitView, true);
        if (mStartTime == 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            mStartTime = DateTimeUtil.stringToLong_YMd(str);
        }
        if (mEndTime == 0) {
            mEndTime = mStartTime;
        }
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
        if (mStartTime == 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(new Date());
            mStartTime = DateTimeUtil.stringToLong_YMd(str);
        }
        if (mEndTime == 0) {
            mEndTime = mStartTime;
        }
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
        params.put("permission", "2");
        params.put("n", "1000");
        if (!TextUtils.isEmpty(nextId))
            params.put("s", nextId);
        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);
        mSubLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SEARCH_CALENDAR), params, this);
        mNetworkManager.load(CALLBACKSUB_INIT, path, this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        receiver = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isClickSearch) {
            findViewById(R.id.btn_schedule_more).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_schedule_more).setVisibility(View.VISIBLE);
        }
    }
}
