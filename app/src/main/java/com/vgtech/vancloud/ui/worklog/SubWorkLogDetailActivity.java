package com.vgtech.vancloud.ui.worklog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.WorkLogBean;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.WorkLogAdapter;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data:  2018/7/30
 * Auther: 陈占洋
 * Description: 下属工作日志界面
 */

public class SubWorkLogDetailActivity extends SearchBaseActivity implements PullToRefreshBase.OnRefreshListener2, HttpListener<String> {

    private static final int CALLBACK_SUB_WORK_LOG_LIST = 0;
    private static final int CALLBACK_REVOKE_WORK_LOG = 1;
    private TextView mTvTitleDate;
    private String mSubName;
    private String mSubStaffNo;
    private String mDate;
    private PullToRefreshListView mListView;
    private WorkLogAdapter mAdapter;
    private NetworkManager mNetworkManager;
    private boolean mIsPullRefresh;
    private VancloudLoadingLayout mLoadingView;
    private boolean mIsInit;
    private int mPageNum = 1;
    private LinearLayout mListViewParent;
    private Button mBtnRevoke;
    private int mTotalPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_worklog);

        mSubName = getIntent().getStringExtra("sub_name");
        mSubStaffNo = getIntent().getStringExtra("sub_staff_no");
        mDate = getIntent().getStringExtra("sub_work_log_date");
        initView();
        initData();
    }

    private void initView() {
        initTitleLayout();
        setTitleText(mSubName);
        setTitleCenter();
        arrowView.setVisibility(View.GONE);
        //标题日期
        mTvTitleDate = (TextView) findViewById(R.id.tv_title_date);
//        String date = DateTimeUtil.getCurrentString("yyyy/MM/dd");
        mTvTitleDate.setText(mDate.replace("-", "/"));

        findViewById(R.id.right_views).setVisibility(View.GONE);

        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.loading_layout);

        mListViewParent = (LinearLayout) findViewById(R.id.sub_work_log_listview_parent);
        mBtnRevoke = (Button) findViewById(R.id.sub_work_log_revoke_all);
        mBtnRevoke.setOnClickListener(this);
        mListView = (PullToRefreshListView) findViewById(R.id.sub_work_log_listview);
        mAdapter = new WorkLogAdapter(new ArrayList<WorkLogBean>());
        mListView.setAdapter(mAdapter);

        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
    }

    private void initData() {

        mNetworkManager = getAppliction().getNetworkManager();
        getSubWorkLogList(1, true, false);
    }

    private void getSubWorkLogList(int pageNum, boolean isInit, boolean isPullRefresh) {
        mIsInit = isInit;
        mIsPullRefresh = isPullRefresh;

        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("date", mDate);
        params.put("staff_no", mSubStaffNo);
        params.put("page_size", "10");
        params.put("page_now", pageNum + "");
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_SUB_WORK_LOG_LIST), params, this);
        mNetworkManager.load(CALLBACK_SUB_WORK_LOG_LIST, path, this);
        if (!mIsPullRefresh)
            mLoadingView.showLoadingView(mListViewParent, "", false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.sub_work_log_revoke_all:
                Map<String, String> params = new HashMap<String, String>();
                params.put("tenant_id", PrfUtils.getTenantId(this));
                params.put("loginUserCode", PrfUtils.getStaff_no(this));
                params.put("date", mDate);
                params.put("staff_no", mSubStaffNo);
                NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_REVOKE_SUB_WORK_LOG), params, this);
                mNetworkManager.load(CALLBACK_REVOKE_WORK_LOG, path, this);
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        getSubWorkLogList(1, true, true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        if (mPageNum + 1 > mTotalPage) {
            //TODO 国际化
            Toast.makeText(this, "最后一页，没有数据了", Toast.LENGTH_SHORT).show();
            mListView.onRefreshComplete();
            return;
        }
        getSubWorkLogList(mPageNum, false, true);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListViewParent);
        mListView.onRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == CALLBACK_SUB_WORK_LOG_LIST) {
                if (mAdapter.getDataSize() <= 0) {
                    mLoadingView.showErrorView(mListViewParent, "", true, true);
                }
            }
            if (callbackId == CALLBACK_REVOKE_WORK_LOG) {
                String msg = rootData.getMsg();
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_SUB_WORK_LOG_LIST:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    mPageNum = resutObject.getInt("pageNo");
                    mTotalPage = resutObject.getInt("pageCount");
                    JSONArray rows = resutObject.getJSONArray("rows");
                    List<WorkLogBean> subWorkLogData = JsonDataFactory.getDataArray(WorkLogBean.class, rows);
                    if (subWorkLogData.size() > 0) {
                        if (mIsInit) {
                            mAdapter.setData(subWorkLogData);
                        } else {
                            mAdapter.addData(subWorkLogData);
                        }
                    } else {
                        if (mIsInit) {
                            //清空数据
                            mLoadingView.showEmptyView(mListViewParent, getString(R.string.no_list_data), true, true);
                            mAdapter.setData(null);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_REVOKE_WORK_LOG:
                String msg = rootData.getMsg();
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
