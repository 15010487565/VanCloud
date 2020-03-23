package com.vgtech.vancloud.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.api.Notice;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.NoticeAdapter;
import com.vgtech.vancloud.ui.module.announcement.NoticeInfoActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.ui.punchcard.OperationType;
import com.vgtech.vantop.ui.punchcard.ReLoadFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeFragment extends BaseFragment
        implements View.OnClickListener, HttpListener, PullToRefreshBase.OnRefreshListener2,
        ReLoadFragment, AdapterView.OnItemClickListener {

    public static final String NOTICE_CONFIRM = "NOTICEFRAGMENT_NOTICE_CONFIRM";

    private final int CALLBACK_LOADDATA = 0X001;
    private final int FLAG_RELOAD = 0X002;
    private final int FLAG_LOAD_ADD = 0X003;

    private OperationType mType = OperationType.INIT;
    private PullToRefreshListView mListView;
    private NoticeAdapter mAdapter;
    private final String TAG = "PunchCardHistory";

    private String mNextId = "0";
    private int mFlag = FLAG_RELOAD;
    private VancloudLoadingLayout mLoadingView;

    public static NoticeFragment getFragment(String is_confirm) {
        NoticeFragment noticeFragment = new NoticeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("is_confirm", is_confirm);
        noticeFragment.setArguments(bundle);
        return noticeFragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.notice_fragment;
    }

    @Override
    protected void initView(View view) {

        recordList = new ArrayList<>();
        mAdapter = new NoticeAdapter(getActivity(), recordList);

        mListView = (PullToRefreshListView) view.findViewById(R.id.list_punchcard_record);
        mListView.setAdapter(mAdapter);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(this);
        mLoadingView = (VancloudLoadingLayout) view.findViewById(R.id.ll_loadingview);

        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTICE_CONFIRM);
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NOTICE_CONFIRM.equals(action)) {
                if ("0".equals(is_confirm)) {
                    int position = intent.getIntExtra("position", -1) - 1;
                    String mynotice_code = intent.getStringExtra("mynotice_code");
                    if (position >= 0 && mAdapter.getCount() > position) {
                        Notice notice = mAdapter.getItem(position);
                        if (notice.mynotice_code.equals(mynotice_code)) {
                            mAdapter.getData().remove(notice);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    initShowData();
                }

            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    protected void initData() {
        is_confirm = getArguments().getString("is_confirm");
        mLoadingView.showLoadingView(mListView, "", true);
        initShowData();
    }

    private void initShowData() {
        mNextId = "0";
        mType = OperationType.INIT;
        loadData(mNextId + "");
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

    public String is_confirm;

    /***
     * 加载数据
     */
    private void loadData(String nextId) {

        Map<String, String> params = new HashMap<>();
        //POST请求params不必须不能为空
//        String staffNo = PrfUtils.getStaff_no(getActivity());
//        params.put("user_id", "609094884778840064");
//        params.put("tenant_id", "608694301886517249");
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("is_confirm", is_confirm);
        params.put("n", "12");
        params.put("s", nextId);
        NetworkPath np = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_MYNOTICE_LIST), params, getActivity());

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
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
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

    private List<Notice> recordList;

    private void showData(JSONObject jsonData) {
        if (mType == OperationType.INIT || mType == OperationType.SEARCH || mType == OperationType.PULLTOREFRESHH) {
            recordList.clear();
        }
        try {
            JSONObject dataObject = jsonData.getJSONObject("data");
            List<Notice> list = JsonDataFactory.getDataArray(Notice.class, dataObject.getJSONArray("rows"));
            mNextId = dataObject.getString("nextid");

            recordList.addAll(list);
            if (mType == OperationType.PULLTOREFRESHH || mType == OperationType.PULLDOWNLOAD) {
                mListView.onRefreshComplete();
            }
            mListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ("0".equals(mNextId)) {
                        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    } else {
                        mListView.setMode(PullToRefreshBase.Mode.BOTH);
                    }
                }
            }, 1000);
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
    public void reLoad() {
        recordList.clear();
        mLoadingView.showLoadingView(mListView, "", true);
        loadData(mNextId);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        mFlag = FLAG_RELOAD;
        mNextId = "0";
        mType = OperationType.PULLTOREFRESHH;
        loadData(mNextId);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        mFlag = FLAG_LOAD_ADD;
        mType = OperationType.PULLDOWNLOAD;
        loadData(mNextId);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Notice notice = (Notice) adapterView.getItemAtPosition(i);
        Intent intent = new Intent(getActivity(), NoticeInfoActivity.class);
        intent.putExtra("Seq", notice.mynotice_seq);
        intent.putExtra("StaffNo", notice.staff_no);
        intent.putExtra("Code", notice.mynotice_code);
        intent.putExtra("mynotice_id", notice.mynotice_id);
        intent.putExtra("position", i);
        startActivity(intent);
    }
}
