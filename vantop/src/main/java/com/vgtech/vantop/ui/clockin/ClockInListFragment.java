package com.vgtech.vantop.ui.clockin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.vgtech.vantop.adapter.ClockInListAdapter;
import com.vgtech.vantop.moudle.ClockInListData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.SearchActivity;
import com.vgtech.vantop.ui.punchcard.OperationType;
import com.vgtech.vantop.ui.punchcard.PunchCardActivity;
import com.vgtech.vantop.ui.punchcard.ReLoadFragment;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 考勤详情列表
 * Created by shilec on 2016/9/7.
 */
public class ClockInListFragment extends BaseFragment implements HttpListener<String>,
        PunchCardActivity.OnDoSeachActionListenner, ReLoadFragment, PullToRefreshBase.OnRefreshListener2, AdapterView.OnItemClickListener {

    private final int CALLBACK_INIT_LIST = 0X001;
    private final String TAG = "ClockInListFragment";
    private String mDate;
    private int mNextId = 1;

    private PullToRefreshListView mListView;
    private List<ClockInListData> mDatas;
    private ClockInListAdapter mAdapter;
    private VancloudLoadingLayout mLoadingView;
    private OperationType mType = OperationType.INIT;

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_attendance_list;
    }

    @Override
    protected void initView(View view) {

        mListView = (PullToRefreshListView) view.findViewById(R.id.list_clock);

        mDatas = new ArrayList<>();
        mAdapter = new ClockInListAdapter(getActivity(), mDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mDate = new SimpleDateFormat("yyyy-MM").format(new Date(System.currentTimeMillis()));
    }

    @Override
    protected void initData() {
        mNextId = 1;
        mLoadingView.showLoadingView(mListView, "", true);
        loadData(mDate, mNextId);
    }

    private void loadData(String date, int nextId) {
        Log.e(TAG, "loadData="+date);
        String url = VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_CLOCKIN_LIST);
        //Log.i(TAG, "URL:" + url);
        Map<String, String> p = new HashMap<>();
        p.put("date", TextUtils.isEmpty(date)?"":date);
        p.put("nextId", nextId + "");
        p.put("pageSize","31");//一个月最多31天
        p.put("loginUserCode", PrfUtils.getStaff_no(getActivity()));
        NetworkPath np = new NetworkPath(url, p, getActivity(), true);
        getApplication().getNetworkManager().load(CALLBACK_INIT_LIST, np, this);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListView);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            mLoadingView.showErrorView(mListView);
            return;
        }
        switch (callbackId) {
            case CALLBACK_INIT_LIST: {
                JSONArray jArr = rootData.getJson().optJSONArray("data");
                List<ClockInListData> list = JsonDataFactory.getDataArray(ClockInListData.class, jArr);
                Log.e("TAG_考勤","list="+list.size());
                if (list == null) {
                    return;
                }
                CopyOnWriteArrayList<ClockInListData> listDatas = new CopyOnWriteArrayList<ClockInListData>();
                listDatas.addAll(list);
                String searchOption = getResources().getStringArray(R.array.sign_status)[1];
                Log.e("TAG_考勤","searchOption="+searchOption);
                if (searchOption.equals(mOption)) {
                    for (ClockInListData listData : listDatas) {
                        if (!Boolean.parseBoolean(listData.getIsException())) {
                            listDatas.remove(listData);
                        }
                    }
                }
                showData(listDatas);
            }
            break;
        }
    }

    private void showData(List<ClockInListData> datas) {
        Log.e("TAG_考勤","mType="+mType+"；datas="+datas.size());
        ((SearchActivity) getActivity()).bsShowData(mType, mDatas, datas, mListView, mLoadingView, mAdapter);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private String mOption;

    @Override
    public void onSearch(String startTime, String endTime, String option1, String option2) {
        Log.e(TAG, startTime);
        Log.e(TAG, ""+getString(R.string.vantop_nothing));
        Log.e(TAG, ""+(TextUtils.equals(getString(R.string.vantop_nothing), startTime)));
        if (TextUtils.equals(getString(R.string.vantop_nothing), startTime)) {
//            startTime = new SimpleDateFormat("yyyy-MM").format(new Date(System.currentTimeMillis()));
            mDate = "";
        }else {
            mDate = startTime;
        }
        mNextId = 1;
        mOption = option1;
        mType = OperationType.SEARCH;
        mLoadingView.showLoadingView(mListView, "", true);
        loadData(mDate, mNextId);
    }

    @Override
    public void reLoad() {
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        mNextId = 1;
        mType = OperationType.PULLTOREFRESHH;
        loadData(mDate, mNextId);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {

        mNextId += 1;
        mType = OperationType.PULLDOWNLOAD;
        loadData(mDate, mNextId);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        ClockInListData data = mDatas.get(i - 1);
        Log.e("TAG_考勤","data="+(data==null));
//        if (!Boolean.parseBoolean(data.isException)) {
//            return;
//        }
        Intent intent = new Intent(getActivity(), ClockInDetailActivity.class);
        intent.putExtra(ClockInDetailActivity.EXTRAS_REQUEST_INFO, data);
        startActivity(intent);
    }
}
