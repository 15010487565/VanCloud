package com.vgtech.vancloud.ui.module.announcement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AnnounceNotify;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.AnnouncementListAdapter;
import com.vgtech.vancloud.utils.NoticeUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vantop.ui.punchcard.OperationType;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementListActivity extends BaseActivity implements HttpListener<String>, PullToRefreshBase.OnRefreshListener2 {

    private final int GET_ANNOUNCEMENT_LIST = 323;
    private NetworkManager mNetworkManager;
    private AnnouncementListAdapter mAdapter;
    private PullToRefreshListView mListView;
    private VancloudLoadingLayout mLoadingView;

    @Override
    protected int getContentView() {
        return R.layout.announcement_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrfUtils.setMessageCountCount(this, PrfUtils.MESSAGE_GONGGAO, 0);
        NoticeUtils.updateAppNum(this);
        setTitle(getString(R.string.announcement_list_title));
        initView();
        initData();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        registerReceiver(mReceiver, intentFilter);
    }

    private void initView() {
        /**
         *  是否显示公告评论和点赞
         *  true 显示
         */
        Intent intent = getIntent();
        boolean isShowNotice = intent.getBooleanExtra("isShowNotice", false);

        mAdapter = new AnnouncementListAdapter(this,isShowNotice);
        mListView = (PullToRefreshListView) findViewById(com.vgtech.vantop.R.id.list_punchcard_record);
        mListView.setAdapter(mAdapter);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        mListView.setClickable(false);
        mListView.setPressed(false);
        mLoadingView = (VancloudLoadingLayout) findViewById(com.vgtech.vantop.R.id.ll_loadingview);
        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
    }

    private void initData() {
        mNextId = "0";
        mLoadingView.showLoadingView(mListView, "", true);
        load(mNextId, true);
    }

    //网络请求
    private void load(String nextId, boolean first) {
        mNetworkManager = this.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("uid",PrfUtils.getUserId(this));
        params.put("n", "12");
        if (!TextUtils.isEmpty(nextId))
            params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ANNOUNCEMENT_LIST), params, this);
        mNetworkManager.load(GET_ANNOUNCEMENT_LIST, path, this, first);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListView);
        boolean mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            mLoadingView.showErrorView(mListView);
            return;
        }
        switch (callbackId) {
            case GET_ANNOUNCEMENT_LIST:
                String s = path.getPostValues().get("s");
                if ("0".equals(s) || TextUtils.isEmpty(s)) {
                    mAdapter.clear();
                }
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mNextId = jsonObject.getString("nextid");
                    final boolean mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    List<AnnounceNotify> announceNotifies = JsonDataFactory.getDataArray(AnnounceNotify.class, jsonObject.getJSONArray("notice"));
                    mAdapter.add(announceNotifies);
                    mAdapter.notifyDataSetChanged();
                    mListView.onRefreshComplete();
                    mListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!mHasData) {
                                mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            } else {
                                mListView.setMode(PullToRefreshBase.Mode.BOTH);
                            }
                        }
                    }, 1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mAdapter.isEmpty())
                    mLoadingView.showEmptyView(mListView, getString(com.vgtech.vantop.R.string.vantop_no_list_data), true, true);
                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private String mNextId = "0";

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
                        if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_ANNOUNCEMENT) {
                            mAdapter.chaneCommentNum(position);
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                int position = data.getIntExtra("position", -1);
                boolean ispraise = data.getBooleanExtra("ispraise", false);
                int paraiseCount = data.getIntExtra("paraiseCount", 0);
                int commentCount = data.getIntExtra("commentCount", 0);
                if (position == -1)
                    return;
                mAdapter.chaneScheduleState(position, ispraise, paraiseCount);
                mAdapter.chaneCommentNum(position, commentCount);
            } else if (requestCode == 2) {
                boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                if (isRefresh) {
                    mAdapter.clear();
                    load(mNextId = null, true);
                }

            } else if (requestCode == 2000) {
                boolean refresh = data.getBooleanExtra("backRefresh", false);
                if (refresh) {
                    mAdapter.clear();
                    load(mNextId = null, true);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        mNextId = "0";
        load(mNextId + "", false);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        load(mNextId + "", false);
    }
}
