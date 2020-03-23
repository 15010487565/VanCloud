package com.vgtech.vancloud.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.vgtech.common.utils.KeyboardUtil;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.LuntanReplyListAdapter;
import com.vgtech.vancloud.ui.module.luntan.EventMsg;
import com.vgtech.vancloud.ui.module.luntan.LuntanList;
import com.vgtech.vancloud.ui.module.luntan.LuntanReplyList;
import com.vgtech.vancloud.utils.SoftKeyboardStateHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

/**
 * Data:  2017/8/4
 * Auther: 陈占洋
 * Description:
 */

public class LuntanReplyListActivity extends BaseActivity implements HttpListener<String>, PullToRefreshBase.OnRefreshListener2 {

    private static final int REPLY_LIST_CALLBACK_ID = 10003;
    private static final int REPLY_LIST_CALLBACK_ID_INIT = 10004;
    private static final int REPLY_CREATE_CALLBACK_ID = 10005;
    private PullToRefreshListView mReplyList;
    private VancloudLoadingLayout mLoadingView;
    private LinearLayout mReplyParentScall;
    private TextView mReplyNowClick;
    private RelativeLayout mToTop;
    private TextView mNum;
    private LinearLayout mReplyParentExpand;
    private EditText mReplyContent;
    private Button mReplySend;
    private NetworkManager mNetworkManager;
    private int mPageSize = 20;
    private int mCurrentPage = 1;
    private LuntanList.DataBean.RowsBean post;
    private LuntanReplyListAdapter mLuntanReplyListAdapter;
    private View mRootView;
    private SoftKeyboardStateHelper mSoftKeyboardStateHelper;
    private boolean mSended = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.reply_list));
        post = (LuntanList.DataBean.RowsBean) getIntent().getSerializableExtra("post");
        mNetworkManager = getAppliction().getNetworkManager();

        initView();
        initListener();
        initData(true);
    }

    private void initView() {
        mRootView = findViewById(R.id.reply_list_root_view);
        mReplyList = (PullToRefreshListView) findViewById(R.id.reply_list_prlv);
        mReplyList.setMode(PullToRefreshBase.Mode.BOTH);
        mReplyList.setOnRefreshListener(this);
        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.reply_list_loadingview);

        mReplyParentScall = (LinearLayout) findViewById(R.id.reply_list_ll_scall);
        mReplyNowClick = (TextView) findViewById(R.id.reply_list_tv_reply_now);
        mToTop = (RelativeLayout) findViewById(R.id.reply_list_rl_to_top);
        mNum = (TextView) findViewById(R.id.reply_list_tv_to_top_num);
        mNum.setText(post.getReplyCount() + "");

        mReplyParentExpand = (LinearLayout) findViewById(R.id.reply_list_ll_reply_expand);
        mReplyContent = (EditText) findViewById(R.id.reply_list_et_reply_content);
        mReplySend = (Button) findViewById(R.id.reply_list_btn_reply_send);

        mLuntanReplyListAdapter = new LuntanReplyListAdapter(post);
        mReplyList.setAdapter(mLuntanReplyListAdapter);

    }

    private void initListener() {
        mReplyNowClick.setOnClickListener(this);
        mToTop.setOnClickListener(this);
        mReplySend.setOnClickListener(this);
        mSoftKeyboardStateHelper = new SoftKeyboardStateHelper(mRootView);
        mSoftKeyboardStateHelper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                Log.i("chen", "onSoftKeyboardOpened: ");
            }

            @Override
            public void onSoftKeyboardClosed() {
                if (mReplyParentExpand.getVisibility() != View.GONE) {
                    mReplyParentScall.setVisibility(View.VISIBLE);
                    mReplyParentExpand.setVisibility(View.GONE);

                    mReplyContent.setText("");
                }
            }
        });
    }

    private void initData(boolean showLoading) {
        HashMap<String, String> params = new HashMap<>();
        params.put("topic_id", post.getId());
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("page_size", mPageSize + "");
        params.put("page_now", mCurrentPage + "");

        String url = ApiUtils.generatorUrl(this, URLAddr.REPLY_LIST);
        NetworkPath path = new NetworkPath(url, params, this);
        if (showLoading) {
            mNetworkManager.load(REPLY_LIST_CALLBACK_ID_INIT, path, this);
            mLoadingView.showLoadingView(mReplyList, "", true);
        } else {
            mNetworkManager.load(REPLY_LIST_CALLBACK_ID, path, this);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_luntan_reply_list;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_close && mSended){
            EventBus.getDefault().post(new EventMsg(EventMsg.LUNTAN_REFRESH_LIST));
        }
        super.onClick(v);
        switch (v.getId()) {
            case R.id.reply_list_tv_reply_now:
                mReplyParentScall.setVisibility(View.GONE);
                mReplyParentExpand.setVisibility(View.VISIBLE);
                mReplyContent.setFocusable(true);
                mReplyContent.requestFocus();
                KeyboardUtil.showSoftInput(mReplyContent);
                break;

            case R.id.reply_list_rl_to_top:
                mReplyList.getRefreshableView().smoothScrollToPosition(0);
                break;

            case R.id.reply_list_btn_reply_send:
                mSended = true;
                String reply_content = mReplyContent.getText().toString().trim();
                if (TextUtils.isEmpty(reply_content)) {
                    Toast.makeText(this, getString(R.string.toast_content_not_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, String> params = new HashMap<>();
                params.put("tenant_id", PrfUtils.getTenantId(this));
                params.put("user_id", PrfUtils.getUserId(this));
                params.put("topic_id", post.getId());
                params.put("reply_content", reply_content);

                String url = ApiUtils.generatorUrl(this, URLAddr.REPLY_CREATE);
                NetworkPath path = new NetworkPath(url, params, this);
                mNetworkManager.load(REPLY_CREATE_CALLBACK_ID, path, this);

                mReplyContent.setText("");
                break;

        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        switch (callbackId) {
            case REPLY_LIST_CALLBACK_ID_INIT:
                mLoadingView.dismiss(mReplyList);
                boolean safe_init = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (safe_init) {
                    Gson gson = new Gson();
                    LuntanReplyList replyList = gson.fromJson(rootData.getJson().toString(), LuntanReplyList.class);
                    mLuntanReplyListAdapter.setData(replyList.getData().getRows());
                    mReplyList.setMode(PullToRefreshBase.Mode.BOTH);
                } else {
                    mLoadingView.showErrorView(mReplyList);
                }
                break;

            case REPLY_LIST_CALLBACK_ID:
                mReplyList.onRefreshComplete();
                boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (safe) {
                    Gson gson = new Gson();
                    LuntanReplyList replyList = gson.fromJson(rootData.getJson().toString(), LuntanReplyList.class);
                    if (replyList.getData().getRows().size() > 0) {
                        if (replyList.getData().getPageNo() <= 1) {
                            mCurrentPage = 1;
                            mLuntanReplyListAdapter.setData(replyList.getData().getRows());
                        } else {
                            mCurrentPage = replyList.getData().getPageNo();
                            mLuntanReplyListAdapter.addData(replyList.getData().getRows());
                        }
                    } else {
                        if (mCurrentPage <= 1) {
//                            Toast.makeText(this, getString(R.string.), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, getString(R.string.is_end), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;

            case REPLY_CREATE_CALLBACK_ID:
                boolean reply_safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (reply_safe) {
                    Toast.makeText(this, getString(R.string.reply_success), Toast.LENGTH_SHORT).show();
                    mReplyList.setRefreshing();
                    if (mReplyParentExpand.getVisibility() != View.GONE) {
                        KeyboardUtil.hideSoftInput(LuntanReplyListActivity.this);
                        mReplyParentScall.setVisibility(View.VISIBLE);
                        mReplyParentExpand.setVisibility(View.GONE);
                    }
                    Integer num = Integer.valueOf(mNum.getText().toString().trim());
                    mNum.setText(num + 1 + "");
                    EventBus.getDefault().post(new EventMsg(EventMsg.LUNTAN_REFRESH_LIST));
                    initData(false);
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
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        mCurrentPage = 1;
        mReplyList.setRefreshing();
        initData(false);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        mCurrentPage++;
        mReplyList.setRefreshing();
        initData(false);
    }

}
