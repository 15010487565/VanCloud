package com.vgtech.vancloud.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.VolleyError;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Notice;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.NoticeNewAdapter;
import com.vgtech.vancloud.ui.module.announcement.NoticeInfoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeNewFragment extends BaseFragment
        implements HttpListener<String>,
        BaseQuickAdapter.OnItemClickListener,
        BaseQuickAdapter.RequestLoadMoreListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String NOTICE_CONFIRM = "NOTICEFRAGMENT_NOTICE_CONFIRM";

    private final int CALLBACK_LOADDATA = 0X001;
    private final int FLAG_RELOAD = 0X002;
    private final int FLAG_LOAD_ADD = 0X003;

//    private OperationType mType = OperationType.INIT;
    int page = 0;
    private RecyclerView recyclerView;
    NoticeNewAdapter mAdapter;

//    private final String TAG = "PunchCardHistory";

    private String mNextId = "0";
//    private int mFlag = FLAG_RELOAD;
    private VancloudLoadingLayout mLoadingView;

    public static NoticeNewFragment getFragment(String is_confirm) {
        NoticeNewFragment noticeFragment = new NoticeNewFragment();
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


        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recordList = new ArrayList<>();
        mAdapter = new NoticeNewAdapter(R.layout.item_notice_new,recordList);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(getRecyclerViewDivider(R.drawable.inset_recyclerview_divider));

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
            Log.e("TAG_通知","action="+action);
            if (NOTICE_CONFIRM.equals(action)) {
//                if ("0".equals(is_confirm)) {
//                    int position = intent.getIntExtra("position", -1) - 1;
//                    String mynotice_code = intent.getStringExtra("mynotice_code");
//                    if (position >= 0 && mAdapter.getData().size() > position) {
//                        Notice notice = mAdapter.getItem(position);
//                        if (notice.mynotice_code.equals(mynotice_code)) {
//                            mAdapter.getData().remove(notice);
//                            mAdapter.notifyDataSetChanged();
//                        }
//                    }
//                } else {
                    initShowData();
//                }

            }
        }
    };

    @Override
    protected void initData() {
        is_confirm = getArguments().getString("is_confirm");
        mLoadingView.showLoadingView(recyclerView, "", true);
        initShowData();
    }

    private void initShowData() {
        mNextId = "0";
        page = 0;
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
        mLoadingView.dismiss(recyclerView);
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            mLoadingView.showErrorView(recyclerView);
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

        try {
            JSONObject dataObject = jsonData.getJSONObject("data");
            List<Notice> list = JsonDataFactory.getDataArray(Notice.class, dataObject.getJSONArray("rows"));
            mNextId = dataObject.getString("nextid");
            if (page == 0) {
                if ("0".equals(mNextId)) {
                    mAdapter.setNewData(list);
                    mAdapter.loadMoreEnd();
                } else {
                    mAdapter.setNewData(list);
                    mAdapter.loadMoreComplete();
                }
            } else {
                if ("0".equals(mNextId)) {
                    mAdapter.addData(list);
                    mAdapter.loadMoreEnd();
                } else {
                    mAdapter.addData(list);
                    mAdapter.loadMoreComplete();
                }
            }

            if (mAdapter.getData().size() <= 0) {
                mLoadingView.showEmptyView(recyclerView, getString(com.vgtech.vancloud.R.string.no_information_todo), true, true);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public void reLoad() {
//        recordList.clear();
//        mLoadingView.showLoadingView(recyclerView, "", true);
//        loadData(mNextId);
//    }


    @Override
    public void onRefresh() {
//        mFlag = FLAG_RELOAD;
        mNextId = "0";
        page = 0;
        loadData(mNextId);
    }

    @Override
    public void onResponse(String response) {

    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Notice notice = (Notice)adapter.getData().get(position);
        Intent intent = new Intent(getActivity(), NoticeInfoActivity.class);
        intent.putExtra("Seq", notice.mynotice_seq);
        intent.putExtra("StaffNo", notice.staff_no);
        intent.putExtra("Code", notice.mynotice_code);
        intent.putExtra("mynotice_id", notice.mynotice_id);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public void onLoadMoreRequested() {
//        mFlag = FLAG_LOAD_ADD;
        page++;
        loadData(mNextId);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        dismisLoadingDialog();
    }
}
