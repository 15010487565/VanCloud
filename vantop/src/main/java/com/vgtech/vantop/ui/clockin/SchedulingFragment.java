package com.vgtech.vantop.ui.clockin;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ScheduleAdapter;
import com.vgtech.vantop.moudle.Schedule;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.SearchActivity;
import com.vgtech.vantop.ui.punchcard.OperationType;
import com.vgtech.vantop.ui.punchcard.PunchCardActivity;
import com.vgtech.vantop.ui.punchcard.ReLoadFragment;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打卡记录页面
 * Created by shilec on 2016/7/15.
 */
public class SchedulingFragment extends BaseFragment
        implements View.OnClickListener, HttpListener, PullToRefreshBase.OnRefreshListener2,
        PunchCardActivity.OnDoSeachActionListenner, ReLoadFragment, AdapterView.OnItemClickListener {


    private final int CALLBACK_LOADDATA = 0X001;
    private final int FLAG_RELOAD = 0X002;
    private final int FLAG_LOAD_ADD = 0X003;

    private OperationType mType = OperationType.INIT;
    private PullToRefreshListView mListView;
    private ScheduleAdapter mAdapter;
    private final String TAG = "PunchCardHistory";

    private String mStartDate;
    private String mEndDate;
    private int mNextId = 1;
    private int mFlag = FLAG_RELOAD;
    private VancloudLoadingLayout mLoadingView;
    private boolean isViewCreated;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && isViewCreated){
            EventBus.getDefault().post(SearchActivity.RESTOR_SEARCH);
            mStartDate = "";
            mEndDate = "";
            initData();
        }
    }

    @Override
    protected int initLayoutId() {
        return R.layout.schedule_fragment;
    }

    @Override
    protected void initView(View view) {

        recordList = new ArrayList<>();
        mAdapter = new ScheduleAdapter(getActivity(), recordList);

        mListView = (PullToRefreshListView) view.findViewById(R.id.list_punchcard_record);
        mListView.setAdapter(mAdapter);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        mListView.setClickable(false);
        mListView.setPressed(false);
        mListView.setOnItemClickListener(this);
        mLoadingView = (VancloudLoadingLayout) view.findViewById(R.id.ll_loadingview);

        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    protected void initData() {
        mNextId = 1;
        mLoadingView.showLoadingView(mListView, "", true);
        initShowData();
    }

    private void initShowData() {
//        mStartDate = DataUtils.getFirstDayOfMonth();
//        mEndDate = DataUtils.getLastDayOfMonth();
        mNextId = 1;
        mType = OperationType.INIT;
        loadData(mStartDate, mEndDate, mNextId + "");
    }

    @Override
    protected void initEvent() {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    /***
     * 加载数据
     */
    private void loadData(String startDate, String endDate, String nextId) {

        String path = VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_ATTDETAIL_SEARCH);
        Uri uri = Uri.parse(path);/*.buildUpon().appendQueryParameter("startDate", startDate).
                appendQueryParameter("endDate", endDate)
                .appendQueryParameter("nextId", nextId).build();*/
        Map<String, String> params = new HashMap<>();
        //POST请求params不必须不能为空
        String staffNo = PrfUtils.getStaff_no(getActivity());
        if(!TextUtils.isEmpty(startDate))
        params.put("startDate", startDate);
        if(!TextUtils.isEmpty(endDate))
        params.put("endDate", endDate);
        params.put("nextId", nextId);
        params.put("staffNo", staffNo);
        params.put("loginUserCode", staffNo);
        NetworkPath np = new NetworkPath(uri.toString(), params, getActivity(), true);

        getApplication().getNetworkManager().load(CALLBACK_LOADDATA, np, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getApplication().getNetworkManager().cancle(this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListView);
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            mLoadingView.showErrorView(mListView);
            return;
        }
        switch (callbackId) {
            case CALLBACK_LOADDATA: {
                showData(rootData.getJson());
            }
            break;
        }
    }

    private List<Schedule> recordList;

    private void showData(JSONObject jsonData) {
        if (mType == OperationType.INIT || mType == OperationType.SEARCH || mType == OperationType.PULLTOREFRESHH) {
            recordList.clear();
        }
        try {
            List<Schedule> list = JsonDataFactory.getDataArray(Schedule.class, jsonData.getJSONArray("data"));
            recordList.addAll(list);
            if (mType == OperationType.PULLTOREFRESHH || mType == OperationType.PULLDOWNLOAD) {
                mListView.onRefreshComplete();
            }
            boolean down = mType == OperationType.PULLDOWNLOAD;
            if (down && list.isEmpty()) {
                //   Toast.makeText(getActivity(), getString(R.string.vantop_lastpage), Toast.LENGTH_SHORT).show();
            }
            if (recordList.isEmpty()) {
                mAdapter.notifyDataSetChanged();
                mLoadingView.showEmptyView(mListView, getString(R.string.vantop_no_list_data), true, true);
                return;
            } else {
                mAdapter.notifyDataSetChanged();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }

    @Override
    public void onSearch(String startTime, String endTime, String option1, String option2) {
        if (TextUtils.equals(getString(R.string.vantop_nothing), startTime)) {
            startTime = "";
        }
        if (TextUtils.equals(getString(R.string.vantop_nothing), endTime)) {
            endTime = "";
        }
        if (TextUtils.isEmpty(startTime) && TextUtils.isEmpty(startTime)) {
//            startTime = DataUtils.getFirstDayOfMonth();
//            endTime = DataUtils.getLastDayOfMonth();
        }
        mLoadingView.showLoadingView(mListView, "", true);
        mStartDate = startTime;
        mEndDate = endTime;
        mNextId = 1;
        mFlag = FLAG_RELOAD;
        mType = OperationType.SEARCH;
        loadData(startTime, endTime, mNextId + "");
    }

    @Override
    public void reLoad() {
        recordList.clear();
        mNextId = 1;
        //mFlag = FLAG_RELOAD;
//        showLoadingDialog(getActivity(), "");
        mLoadingView.showLoadingView(mListView, "", true);
        //loadData(mStartDate,mEndDate,mNextId+"");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mStartDate = sdf.format(date);
        mEndDate = sdf.format(date);
        loadData(mStartDate, mEndDate, mNextId + "");
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        mFlag = FLAG_RELOAD;
        mNextId = 1;
        mType = OperationType.PULLTOREFRESHH;
        loadData(mStartDate, mEndDate, mNextId + "");
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        mFlag = FLAG_LOAD_ADD;
        mNextId += 1;
        mType = OperationType.PULLDOWNLOAD;
        loadData(mStartDate, mEndDate, mNextId + "");
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }
}
