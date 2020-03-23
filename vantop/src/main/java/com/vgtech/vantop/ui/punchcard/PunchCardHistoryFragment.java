package com.vgtech.vantop.ui.punchcard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.PunchcardRecordListAdapter;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.moudle.Record;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.SearchActivity;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.greenrobot.eventbus.EventBus;

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
public class PunchCardHistoryFragment extends BaseFragment
        implements View.OnClickListener, HttpListener, PullToRefreshBase.OnRefreshListener2,
        PunchCardActivity.OnDoSeachActionListenner, ReLoadFragment {


    private final int CALLBACK_LOADDATA = 0X001;
    private final int FLAG_RELOAD = 0X002;
    private final int FLAG_LOAD_ADD = 0X003;

    private OperationType mType = OperationType.INIT;
    private PullToRefreshListView mListView;
    private PunchcardRecordListAdapter mAdapter;
    private final String TAG = "PunchCardHistory";

    private String mStartDate;
    private String mEndDate;
    private int mNextId = 1;
    private int mFlag = FLAG_RELOAD;
    private VancloudLoadingLayout mLoadingView;
    private boolean mIsResume = false;


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            EventBus.getDefault().post(SearchActivity.RESTOR_SEARCH);
            initData();
        }
    }

    @Override
    protected int initLayoutId() {
        return R.layout.punchcard_record_fragment;
    }

    @Override
    protected void initView(View view) {

        recordList = new ArrayList<>();
        mAdapter = new PunchcardRecordListAdapter(getActivity(), recordList);

        mListView = (PullToRefreshListView) view.findViewById(R.id.list_punchcard_record);
        mListView.setAdapter(mAdapter);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        mLoadingView = (VancloudLoadingLayout) view.findViewById(R.id.ll_loadingview);

        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("sign_card");
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("sign_card".equals(intent.getAction())) {
                initData();
            }
        }
    };

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

        //long ld = System.currentTimeMillis();
        //Date date = new Date(ld);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //String sDate = sdf.format(date);
        //Log.i(TAG, "startDate===" + sDate);

        mStartDate = "";
        mEndDate = "";
        //分页 下页编号
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
        mIsResume = true;
        PunchCardActivity punchCardActivity = (PunchCardActivity) getActivity();
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    /***
     * 加载数据
     */
    private void loadData(String startDate, String endDate, String nextId) {

        //默认分页50条
        String path = VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_PUNCHCARD_LOADHISTORY);
        Uri uri = Uri.parse(path);/*.buildUpon().appendQueryParameter("startDate", startDate).
                appendQueryParameter("endDate", endDate)
                .appendQueryParameter("nextId", nextId).build();*/
        Map<String, String> params = new HashMap<>();
        //POST请求params不必须不能为空
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("nextId", nextId);
        params.put("loginUserCode", PrfUtils.getStaff_no(getActivity()));
        NetworkPath np = new NetworkPath(uri.toString(), params, getActivity(), true);

        getApplication().getNetworkManager().load(CALLBACK_LOADDATA, np, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getApplication().getNetworkManager().cancle(this);
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListView);
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, false);
        if (!safe) {
            mLoadingView.showErrorView(mListView);
            return;
        }
        switch (callbackId) {
            case CALLBACK_LOADDATA: {
                showData(rootData.getJson().toString());
            }
            break;
        }
    }

    private List<Record> recordList;

    private void showData(String jsonData) {
        Log.e("TAG_打卡记录","jsonData="+jsonData);
        //未登录或当天无打卡记录
        if (TextUtils.isEmpty(jsonData)) {
            if (recordList != null)
                recordList.clear();
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
            return;
        }
        if (mType == OperationType.INIT || mType == OperationType.SEARCH || mType == OperationType.PULLTOREFRESHH) {
            recordList.clear();
        }
        List<PunchCardListData> list = PunchCardListData.fromJson(jsonData);
        for (PunchCardListData punch : list) {//将打卡记录  按是日期分组
            String date = punch.getDate();
            Record curRecord = null;
            for (Record record : recordList) {
                if (record.date.equals(date)) {
                    curRecord = record;
                    break;
                }
            }
            if (curRecord == null) {
                curRecord = new Record();
                curRecord.date = date;
                curRecord.cards = new ArrayList<>();
                recordList.add(curRecord);
            }
            curRecord.cards.add(punch);
        }
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
            Log.e("TAG_打卡记录","recordList="+recordList.toString());
            mAdapter.notifyDataSetChanged();
            return;
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
        if (recordList != null)
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
}
