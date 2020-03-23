package com.vgtech.vancloud.ui.module.share;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.PublishConstants;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.SharedAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.common.publish.module.Pcomment;
import com.vgtech.vancloud.utils.PublishUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分享
 * Created by Duke on 2015/9/9.
 */
public class ShareActivity extends SearchBaseActivity implements HttpListener<String>, AbsListView.OnScrollListener, View.OnClickListener {

    private final int GET_HELP_LIST = 431;
    private PullToRefreshListView listView;

    private SharedAdapter adapter;
    private List<SharedListItem> data;

    private NetworkManager mNetworkManager;

    private boolean mSafe;
    private String mNextId;
    private boolean mHasData;
    private String mLastId;
    private View mWaitView;
    private EditText serchContext;

    private EditText serchContextView;
    private TextView startTimeView;
    private TextView endTimeView;

    private boolean isRefresh = false;
    private String firstPageEndId = null;

    private String keyword;
    private String startTime;
    private String endTime;
    private boolean enableScroll = true;

    private VancloudLoadingLayout loadingLayout;

    private boolean isShowLoad = true;

//    private NoticeLayout noticeLayout;

    @Override
    protected int getContentView() {
        return R.layout.help_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitleLayout();
        setTitleText(getString(R.string.lable_shared));

        initView();
        initData();
        setListener();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        registerReceiver(mReceiver, intentFilter);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add:
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SHARED);
                startActivity(intent);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private SharedListItem mSharedItem;
    private NewUser mNewUser;
    private int mPosition;

    public void commentUser(int position, SharedListItem sharedListItem, NewUser user) {
        mPosition = position;
        mSharedItem = sharedListItem;
        mNewUser = user;
        PublishTask publishTask = new PublishTask();
        publishTask.setPosition(mPosition);
        publishTask.type = PublishConstants.PUBLISH_COMMENT;
        Pcomment pcomment = new Pcomment();
        pcomment.commentId = mSharedItem.topicId;
        pcomment.commentType = PublishUtils.COMMENTTYPE_SHARE;
        pcomment.replyuserid = mNewUser.userid;
        pcomment.replayUser = mNewUser.name;
        pcomment.content = "";
        Gson gson = new Gson();
        publishTask.content = gson.toJson(pcomment);
        Intent intent = new Intent(this, NewPublishedActivity.class);
        intent.putExtra("publishTask", publishTask);
        startActivity(intent);
//        mInputView.setVisibility(View.VISIBLE);
//        messageInput.setCommentUser(sharedListItem, user);
    }

    public void initView() {

        arrowView.setVisibility(View.GONE);
        listView = (PullToRefreshListView) findViewById(R.id.listview);
        mWaitView = getLayoutInflater().inflate(R.layout.progress, null);
        serchContext = (EditText) findViewById(R.id.serch_context);
        serchContextView = (EditText) findViewById(R.id.serch_context);
        startTimeView = (TextView) findViewById(R.id.start_time);
        endTimeView = (TextView) findViewById(R.id.end_time);

        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                isShowLoad = true;
                mLastId = "0";
                mNextId = "0";
                loadHelpInfo(mNextId, false);
            }
        });
    }

    private void initData() {

        data = new ArrayList<SharedListItem>();
        adapter = new SharedAdapter(this, data, findViewById(R.id.add));
        listView.setAdapter(adapter);
        loadHelpInfo(mNextId, false);
    }

    private void setListener() {
        listView.setOnScrollListener(this);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isRefresh = true;
                mLastId = "0";
                mNextId = "0";
                keyword = null;
                serchContext.setText("");
                isShowLoad = false;
                loadHelpInfo(mNextId, true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        serchContext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {

//                data.clear();
                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
//                    keyword = serchContextView.getText().toString();
//                    loadAdvaceHelpInfo(mNextId = null);
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(serchContext.getWindowToken(), 0);
                return false;
            }

        });
        startTimeView.setOnClickListener(this);
        endTimeView.setOnClickListener(this);
    }

    @Override
    public void searchRequest() {
        data.clear();
        keyword = serchContextView.getText().toString();
        startTime = startTimeView.getText().toString();
        endTime = endTimeView.getText().toString();
        enableScroll = false;
        loadAdvaceHelpInfo(mNextId = null);
    }

    //高级搜索
    private void loadAdvaceHelpInfo(String nextId) {

        loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));


        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);

        try {
            if (!TextUtils.isEmpty(startTime) && !getString(R.string.no_time).equals(startTime))
                params.put("startdate", (new SimpleDateFormat("yyyy-MM-dd").parse(startTime).getTime()) + "");
        } catch (Exception e) {
        }

        try {
            if (!TextUtils.isEmpty(endTime) && !getString(R.string.no_time).equals(endTime))
                params.put("enddate", (new SimpleDateFormat("yyyy-MM-dd").parse(endTime).getTime()) + "");
        } catch (Exception e) {
        }


        params.put("type", "0");
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("n", "12");
        if (!TextUtils.isEmpty(nextId) && !isRefresh)
            params.put("s", nextId);
        else
            params.put("s", "0");


        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SEARCH_TOPIC), params, this);
        mNetworkManager.load(GET_HELP_LIST, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
    }

    //网络请求
    private void loadHelpInfo(String nextId, boolean refresh) {
        if (isShowLoad)
            loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));

//        String keyword = serchContext.getText().toString();


        params.put("type", "0");
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("n", "12");
        if (!TextUtils.isEmpty(nextId) && !isRefresh)
            params.put("s", nextId);
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SHARED_LIST), params, this);
        mNetworkManager.load(GET_HELP_LIST, path, this, (TextUtils.isEmpty(nextId) || "0".equals(nextId)) && !refresh);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        listView.onRefreshComplete();
        loadingLayout.dismiss(listView);
        isShowLoad = true;
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            if (adapter.getData().size() == 0) {
                loadingLayout.showErrorView(listView);
            }
            return;
        }
        switch (callbackId) {
            case GET_HELP_LIST:
                mWaitView.setVisibility(View.GONE);
                List<SharedListItem> shareItems = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mNextId = jsonObject.getString("nextid");
                    if (firstPageEndId == null)
                        firstPageEndId = mNextId;
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    shareItems = JsonDataFactory.getDataArray(SharedListItem.class, jsonObject.getJSONArray("rows"));
                    if (shareItems.size() < 12) {
                        mHasData = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter != null) {
                    String s = path.getPostValues().get("s");
                    if (isRefresh || "0".equals(s) || TextUtils.isEmpty(s)) {
                        adapter.clear();
                        listView.onRefreshComplete();
                        isRefresh = false;
                    }
                    data.addAll(shareItems);
                    adapter.notifyDataSetChanged();
                    enableScroll = true;
                }
                if (data != null && data.size() > 0) {
                } else {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_share_detail), true, true);
                    listView.setVisibility(View.VISIBLE);
                    return;
                }
                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    @Override
    public void onResponse(String response) {

    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        boolean flag = false;
        if (!TextUtils.isEmpty(mLastId)) {
            if (mLastId.equals(mNextId))
                flag = true;
        }
        if (enableScroll && !flag && mSafe && mHasData
                && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            isShowLoad = false;
            if (!TextUtils.isEmpty(keyword)) {
                loadAdvaceHelpInfo(mNextId);
            } else {
                loadHelpInfo(mNextId, true);
            }

        }

    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_COMMENT:
                        int position = intent.getIntExtra("position", -1);
                        if (position < 0)
                            return;
                        int commentType = intent.getIntExtra("commentType", -1);
                        String sharedId = intent.getStringExtra("commentId");
                        String commentStr = intent.getStringExtra("comment");
                        if (!TextUtils.isEmpty(sharedId) && !TextUtils.isEmpty(commentStr)) {
                            SharedListItem sharedListItem = adapter.getItem(position);
                            if (sharedId.equals(sharedListItem.topicId)) {
                                List<Comment> comments = sharedListItem.getArrayData(Comment.class);
                                if (sharedListItem.comments <= comments.size()) {
                                    try {
                                        Comment comment = JsonDataFactory.getData(Comment.class, new JSONObject(commentStr));
                                        comments.add(comment);
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }


                            }
                        }
                        if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_SHARE) {
                            adapter.chaneCommentNum(position);
                        }
                        break;
                    case PublishTask.PUBLISH_SHARED:
                    case PublishTask.PUBLISH_FORWARD:
                        data.clear();
                        isShowLoad = false;
                        loadHelpInfo(mNextId = null, true);
                        break;
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 400) {
                int position = data.getIntExtra("position", -1);

                boolean delete = data.getBooleanExtra("delete", false);
                if (position == -1)
                    return;
                if (delete) {
                    adapter.getData().remove(position);
                    adapter.notifyDataSetChanged();
                    return;
                }

                boolean ispraise = data.getBooleanExtra("ispraise", false);
                int paraiseCount = data.getIntExtra("paraiseCount", 0);
                int commentCount = data.getIntExtra("commentCount", 0);
                int type = data.getIntExtra("type", 0);
                if (position == -1)
                    return;
                String praiseUser = data.getStringExtra("praiseUser");
                String topCom = data.getStringExtra("topCom");
                SharedListItem shared = adapter.getSharedItem(position);
                if (shared == null)
                    return;
                if (!TextUtils.isEmpty(praiseUser)) {
                    try {
                        shared.getJson().put("prise_list", new JSONArray(praiseUser));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!TextUtils.isEmpty(topCom)) {
                    try {
                        shared.getJson().put("comment_list", new JSONArray(topCom));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    shared = JsonDataFactory.getData(SharedListItem.class, shared.getJson());
                    adapter.getData().remove(position);
                    adapter.getData().add(position, shared);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapter.changeCollection(position, type);
                adapter.chaneScheduleState(position, ispraise, paraiseCount);
                adapter.chaneCommentNum(position, commentCount);
                boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                if (isRefresh) {
                    this.data.clear();
                    isShowLoad = false;
                    loadHelpInfo(mNextId = null, false);
                }
            } else if (requestCode == 2000) {

                boolean refresh = data.getBooleanExtra("backRefresh", false);

                if (refresh) {
                    this.data.clear();
                    isShowLoad = false;
                    loadHelpInfo(mNextId = null, false);
                }

            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
        adapter.cancleNetWork();

        if (mNetworkManager != null) {
            mNetworkManager.cancle(this);
            mNetworkManager = null;
        }
    }
}
