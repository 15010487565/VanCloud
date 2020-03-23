package com.vgtech.vancloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.LuntanListAdapter;
import com.vgtech.vancloud.ui.adapter.PopRvAdapter;
import com.vgtech.vancloud.ui.module.luntan.EventMsg;
import com.vgtech.vancloud.ui.module.luntan.LuntanList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data:  2017/8/2
 * Auther: 陈占洋
 * Description: 论坛界面
 */

public class LuntanListActivity extends BaseActivity implements PullToRefreshBase.OnRefreshListener2, AdapterView.OnItemClickListener, HttpListener<String> {

    private static final int TOPIC_LIST_CALLBACK_ID = 10000;
    private static final int TOPIC_LIST_CALLBACK_ID_INIT = 10001;
    private static final int POST_ADD_VISITOR_COUNT_CALLBACK_ID = 10002;
    private RelativeLayout mRlAll;
    private RelativeLayout mRlNormalSort;
    private RelativeLayout mRlFatie;
    private PopupWindow mPopWindow;
    private RecyclerView mPopRv;
    private PopRvAdapter mPopRvAdapter;
    private LinearLayout mPop;
    private boolean mAllPopIsShow;
    private boolean mSortPopIsShow;
    private PullToRefreshListView mLuntanList;
    private LuntanListAdapter mLuntanListAdapter;
    private NetworkManager mNetworkManager;
    private Sort mSort = Sort.NEW_REPLY;
    private Category mCategory = Category.ALL;
    private int mPageSize = 20;
    private int mCurrentPage = 1;
    private TextView mTvAll;
    private TextView mTvNormalSort;
    private ImageView mIvAll;
    private ImageView mIvNormalSort;
    private VancloudLoadingLayout mLoadingView;
    private int mPostAddPosition;

    public enum Sort {
        NEW_REPLY(0), MOST_REMARKS(1), POST_TIME(2);
        private int value;

        private Sort(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Category {
        ALL(0), MY_POST(1), MY_REPLY(2);
        private int value;

        private Category(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_luntan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.message_luntan));

        mNetworkManager = getAppliction().getNetworkManager();
        EventBus.getDefault().register(this);
        initView();
        initListener();
        initData(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefresh(EventMsg msg) {
        if (msg.code == EventMsg.LUNTAN_REFRESH_LIST) {
            mCurrentPage = 1;
//            mCategory = Category.ALL;
//            mTvAll.setDettailText(getString(R.string.all));
//            mSort = Sort.NEW_REPLY;
//            mTvNormalSort.setDettailText(getString(R.string.normal_sort));
            mLuntanList.setRefreshing();
            initData(false);
        }
    }

    private void initView() {
        mRlAll = (RelativeLayout) findViewById(R.id.rl_message_luntan_all);
        mTvAll = (TextView) findViewById(R.id.tv_message_luntan_all);
        mIvAll = (ImageView) findViewById(R.id.iv_message_luntan_all);
        mRlNormalSort = (RelativeLayout) findViewById(R.id.rl_message_luntan_normal_sort);
        mTvNormalSort = (TextView) findViewById(R.id.tv_message_luntan_normal_sort);
        mIvNormalSort = (ImageView) findViewById(R.id.iv_message_luntan_normal_sort);
        mRlFatie = (RelativeLayout) findViewById(R.id.rl_message_luntan_fatie);

        mPop = (LinearLayout) findViewById(R.id.message_luntan_pop_ll);
        mPopRv = (RecyclerView) findViewById(R.id.message_luntan_pop_rv);
        mPopRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mPopRvAdapter = new PopRvAdapter();
        mPopRv.setAdapter(mPopRvAdapter);
        mPopRvAdapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof TextView) {
                    String text = ((TextView) view).getText().toString();
                    if (getString(R.string.all).equals(text)) {
                        mCategory = Category.ALL;
                        mTvAll.setText(getString(R.string.all));
                    } else if (getString(R.string.my_fatie).equals(text)) {
                        mCategory = Category.MY_POST;
                        mTvAll.setText(getString(R.string.my_fatie));
                    } else if (getString(R.string.my_huitie).equals(text)) {
                        mCategory = Category.MY_REPLY;
                        mTvAll.setText(getString(R.string.my_huitie));
                    } else if (getString(R.string.normal_sort).equals(text)) {
                        mSort = Sort.NEW_REPLY;
                        mTvNormalSort.setText(getString(R.string.normal_sort));
                    } else if (getString(R.string.most_pinglun).equals(text)) {
                        mSort = Sort.MOST_REMARKS;
                        mTvNormalSort.setText(getString(R.string.most_pinglun));
                    } else if (getString(R.string.time_fatie).equals(text)) {
                        mSort = Sort.POST_TIME;
                        mTvNormalSort.setText(getString(R.string.time_fatie));
                    }
                    if (mPop.getVisibility() != View.GONE) {
                        mAllPopIsShow = false;
                        mSortPopIsShow = false;
                        mPop.setVisibility(View.GONE);
                        mIvAll.setImageResource(R.mipmap.icon_down_arrow_new);
                        mIvNormalSort.setImageResource(R.mipmap.icon_down_arrow_new);
                    }
                    mCurrentPage = 1;
                    initData(true);
                }
            }
        });

        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.prlv_messaage_luntan_loadingview);
        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData(true);
            }
        });
        mLuntanList = (PullToRefreshListView) findViewById(R.id.prlv_messaage_luntan_list);
        mLuntanList.setMode(PullToRefreshBase.Mode.BOTH);
        mLuntanListAdapter = new LuntanListAdapter();

        mLuntanList.setAdapter(mLuntanListAdapter);
        mLuntanList.setOnRefreshListener(this);
        mLuntanList.setOnItemClickListener(this);

    }

    private void initListener() {
        mRlAll.setOnClickListener(this);
        mRlNormalSort.setOnClickListener(this);
        mRlFatie.setOnClickListener(this);
    }

    private void initData(boolean showLoading) {
        //获取列表
        HashMap<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("sort", mSort.getValue() + "");
        params.put("category", mCategory.getValue() + "");
        params.put("page_size", mPageSize + "");
        params.put("page_now", mCurrentPage + "");

        String url = ApiUtils.generatorUrl(this, URLAddr.TOPIC_LIST);
        NetworkPath path = new NetworkPath(url, params, this);

        if (showLoading) {
            mNetworkManager.load(TOPIC_LIST_CALLBACK_ID_INIT, path, this);
            mLoadingView.showLoadingView(mLuntanList, "", true);
        } else {
            mNetworkManager.load(TOPIC_LIST_CALLBACK_ID, path, this);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_message_luntan_all:
                mAllPopIsShow = !mAllPopIsShow;
                if (mAllPopIsShow) {
                    showPop(v);
                    mIvAll.setImageResource(R.mipmap.icon_up_arrow_new);
                    mIvNormalSort.setImageResource(R.mipmap.icon_down_arrow_new);
                } else {
                    if (mPop.getVisibility() != View.GONE) {
                        mPop.setVisibility(View.GONE);
                        mIvAll.setImageResource(R.mipmap.icon_down_arrow_new);
                        mIvNormalSort.setImageResource(R.mipmap.icon_down_arrow_new);
                    }
                }
                mSortPopIsShow = false;
                break;
            case R.id.rl_message_luntan_normal_sort:
                mSortPopIsShow = !mSortPopIsShow;
                if (mSortPopIsShow) {
                    showPop(v);
                    mIvAll.setImageResource(R.mipmap.icon_down_arrow_new);
                    mIvNormalSort.setImageResource(R.mipmap.icon_up_arrow_new);
                } else {
                    if (mPop.getVisibility() != View.GONE) {
                        mPop.setVisibility(View.GONE);
                        mIvAll.setImageResource(R.mipmap.icon_down_arrow_new);
                        mIvNormalSort.setImageResource(R.mipmap.icon_down_arrow_new);
                    }
                }
                mAllPopIsShow = false;
                break;
            case R.id.rl_message_luntan_fatie:
                Intent fatie = new Intent(this, LuntanFatieActivity.class);
                startActivity(fatie);
                break;
        }
    }

    private void showPop(View view) {

        ArrayList<String> data = new ArrayList<>();
        if (view.getId() == R.id.rl_message_luntan_all) {
            data.add(getString(R.string.all));
            data.add(getString(R.string.my_fatie));
            data.add(getString(R.string.my_huitie));
        } else if (view.getId() == R.id.rl_message_luntan_normal_sort) {
            data.add(getString(R.string.normal_sort));
            data.add(getString(R.string.most_pinglun));
            data.add(getString(R.string.time_fatie));
        }
        mPopRvAdapter.setData(data);
        if (mPop.getVisibility() != View.VISIBLE) {
            mPop.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase refreshView) {
        mCurrentPage = 1;
        initData(false);
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase refreshView) {
        mCurrentPage++;
        initData(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LuntanList.DataBean.RowsBean rowsBean = mLuntanListAdapter.getData().get(position - 1);
        mPostAddPosition = position - 1;

        HashMap<String, String> params = new HashMap<>();
        params.put("tenant_id",PrfUtils.getTenantId(this));
        params.put("topic_id",rowsBean.getId());
        String url = ApiUtils.generatorUrl(this, URLAddr.POST_ADD_VISITOR_COUNT);
        NetworkPath path = new NetworkPath(url, params, this);
        mNetworkManager.load(POST_ADD_VISITOR_COUNT_CALLBACK_ID,path,this);

        Intent intent = new Intent(this, LuntanReplyListActivity.class);
        intent.putExtra("post", rowsBean);
        startActivity(intent);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        switch (callbackId) {
            case TOPIC_LIST_CALLBACK_ID_INIT:
                mLoadingView.dismiss(mLuntanList);
                boolean safe_init = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (safe_init) {
                    Gson gson = new Gson();
                    LuntanList luntanList = gson.fromJson(rootData.getJson().toString(), LuntanList.class);
                    if (luntanList.getData().getRows().size() <= 0) {
                        mLoadingView.showEmptyView(mLuntanList, getString(R.string.no_notice_info), true, true);
                    } else {
                        mLuntanListAdapter.setData(luntanList.getData().getRows());
                    }
                } else {
                    mLoadingView.showErrorView(mLuntanList, "", true, true);
                }
                break;

            case TOPIC_LIST_CALLBACK_ID:
                boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (safe) {
                    Gson gson = new Gson();
                    LuntanList luntanList = gson.fromJson(rootData.getJson().toString(), LuntanList.class);
                    if (luntanList.getData().getRows().size() > 0) {
                        if (luntanList.getData().getPageNo() <= 1) {
                            mCurrentPage = 1;
                            mLuntanListAdapter.setData(luntanList.getData().getRows());
                        } else {
                            mCurrentPage = luntanList.getData().getPageNo();
                            mLuntanListAdapter.addData(luntanList.getData().getRows());
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.is_end), Toast.LENGTH_SHORT).show();
                    }
                }
                mLuntanList.onRefreshComplete();
                break;
            case POST_ADD_VISITOR_COUNT_CALLBACK_ID:
                boolean post_add_safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (post_add_safe){
                    int visitorCount = mLuntanListAdapter.getData().get(mPostAddPosition).getVisitorCount();
                    mLuntanListAdapter.getData().get(mPostAddPosition).setVisitorCount(++visitorCount);
                    mLuntanListAdapter.notifyDataSetChanged();
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
