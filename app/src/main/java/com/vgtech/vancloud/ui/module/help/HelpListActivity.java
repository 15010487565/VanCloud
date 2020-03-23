package com.vgtech.vancloud.ui.module.help;

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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.HelpListItem;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.HelpListAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.utils.PublishUtils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帮帮
 * Created by Duke on 2015/9/9.
 */
public class HelpListActivity extends SearchBaseActivity implements HttpListener<String>, AbsListView.OnScrollListener, View.OnClickListener {

    private final int GET_HELP_LIST = 431;
    private PullToRefreshListView listView;

    private HelpListAdapter adapter;
    private List<HelpListItem> data;

    private NetworkManager mNetworkManager;

    private boolean mSafe;
    private String mNextId = "0";
    private boolean mHasData;
    private String mLastId;
    private View mWaitView;
    private EditText serchContext;

    private EditText serchContextView;
    private TextView startTimeView;
    private TextView endTimeView;

    private String keyword;
    private String startTime;
    private String endTime;
    private boolean isRefresh = false;

    private boolean scrollEable = true;

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
        setTitleText(getString(R.string.lable_helper));

//        hidAdvancedSearch();


        initView();
        initData();
        setListener();
//        installShortCut();
//        delShortcut();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
//            case R.id.top_type_click:
//
//                break;
            case R.id.add:
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_HELP);
                startActivity(intent);
                break;
//            case R.id.search_cancel:
//                serchContextView.setDettailText("");
//                break;
//            case R.id.start_time:
//                showDateDialogview(startTimeView);
//                break;
//            case R.id.end_time:
//                showDateDialogview(endTimeView);
//                break;
//            case R.id.confirm_button:
//                hideAdvancedSearchLayout();
//                startTime = startTimeView.getText().toString();
//                endTime = endTimeView.getText().toString();
////                ((ScheduleListLoadFragment)subordinateTypeTabs.get(subordinateTypeCurrentTab).fragment).load(startTimeView.getText().toString(),endTimeView.getText().toString());
//                break;
            default:
                super.onClick(v);
                break;

        }
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

//        mWaitView.findViewById(R.id.btn_retry).setOnClickListener(this);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                //TODO
                isShowLoad = true;
                mNextId = "0";
                mLastId = "0";
                loadHelpInfo(mNextId);
            }
        });

    }

    private void initData() {

        data = new ArrayList<HelpListItem>();
        adapter = new HelpListAdapter(this, data);
        listView.setAdapter(adapter);
        loadHelpInfo(mNextId);
    }

    private void setListener() {
        listView.setOnScrollListener(this);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isRefresh = true;
                mLastId = "0";
                mNextId = "0";
                isShowLoad = false;
                loadHelpInfo(mNextId);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        serchContext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {

                data.clear();
                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                    loadHelpInfo(mNextId = "0");
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
        scrollEable = false;
        loadAdvaceHelpInfo(mNextId = null);
    }

    //高级搜索
    private void loadAdvaceHelpInfo(String nextId) {
//        showProgress(mWaitView, true);
//        showLoadingDialog(this, getString(R.string.prompt_info_01));
        loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));

        String keyword = serchContext.getText().toString();

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
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SEARCH_HELP), params, this);
        mNetworkManager.load(GET_HELP_LIST, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
    }

    //网络请求
    private void loadHelpInfo(String nextId) {
//        showProgress(mWaitView, true);
//        showLoadingDialog(this, getString(R.string.prompt_info_01));
        if (isShowLoad)
            loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("ownid", PrfUtils.getUserId(this));
        String keyword = serchContext.getText().toString();

        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);
        if (!TextUtils.isEmpty(startTime) && !getString(R.string.no_time).equals(startTime))
            params.put("startdate", startTime);
        if (!TextUtils.isEmpty(endTime) && !getString(R.string.no_time).equals(endTime))
            params.put("enddate", endTime);


        params.put("type", "0");


        params.put("n", "12");
        if (!TextUtils.isEmpty(nextId))
            params.put("s", nextId);
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_HELP_LIST), params, this);
        mNetworkManager.load(GET_HELP_LIST, path, this, TextUtils.isEmpty(nextId) || "0".equals(nextId));
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        listView.onRefreshComplete();
        loadingLayout.dismiss(listView);
        dismisLoadingDialog();
        isShowLoad = true;
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
//            showProgress(mWaitView, false);
            if (adapter.getData().size() == 0) {
                loadingLayout.showErrorView(listView);
            }
            return;
        }
        switch (callbackId) {
            case GET_HELP_LIST:
                mWaitView.setVisibility(View.GONE);
                List<HelpListItem> calendarItems = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mNextId = jsonObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    calendarItems = JsonDataFactory.getDataArray(HelpListItem.class, jsonObject.getJSONArray("rows"));
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
                    data.addAll(calendarItems);
                    adapter.notifyDataSetChanged();
                    scrollEable = true;
                }
                if (data != null && data.size() > 0) {

                } else {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_help_detail), true, true);
                    listView.setVisibility(View.VISIBLE);
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
        if (scrollEable && !flag && mSafe && mHasData
                && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            isShowLoad = false;
            loadHelpInfo(mNextId);
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }


    public void showDateDialogview(TextView textView) {
        String dateS = textView.getText().toString();
        Calendar calendar = null;
        if (!TextUtils.isEmpty(dateS)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date parse = dateFormat.parse(dateS);
                calendar = Calendar.getInstance();
                calendar.setTime(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        DateFullDialogView dateDialogview = new DateFullDialogView(this,
                textView, "full", "ymdhm", calendar, getResources().getColor(R.color.text_black));//年月日时分秒 当前日期之后选择
        dateDialogview.show(textView);
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
                        int commentType = intent.getIntExtra("commentType", -1);
                        if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_HELP) {
                            adapter.chaneCommentNum(position);
                        }
                        break;
                    case PublishTask.PUBLISH_HELP:
                    case PublishTask.PUBLISH_FORWARD:
                        data.clear();
                        isShowLoad = false;
                        loadHelpInfo(mNextId = null);
                        break;
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                boolean delete = data.getBooleanExtra("delete", false);
                int position = data.getIntExtra("position", -1);
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
                int type = data.getIntExtra("type", -1);

                adapter.chaneScheduleState(position, ispraise, paraiseCount, type);
                adapter.chaneCommentNum(position, commentCount);
                boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                if (isRefresh) {
                    this.data.clear();
                    isShowLoad = false;
                    loadHelpInfo(mNextId = null);
                }

            } else if (requestCode == 2000) {

                boolean refresh = data.getBooleanExtra("backRefresh", false);

                if (refresh) {
                    this.data.clear();
                    isShowLoad = false;
                    loadHelpInfo(mNextId = null);
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
