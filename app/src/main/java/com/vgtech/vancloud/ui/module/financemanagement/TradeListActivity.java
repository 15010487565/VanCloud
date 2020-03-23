package com.vgtech.vancloud.ui.module.financemanagement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.BalanceInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.TradeListItem;
import com.vgtech.common.api.TradeTypeListItem;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.TradeListAdapter;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nick on 2016/1/4.
 */
public class TradeListActivity extends SearchBaseActivity implements HttpListener<String>, AbsListView.OnScrollListener, View.OnClickListener {

    private final int GET_TRADE_LIST = 431;
    private PullToRefreshListView listView;

    private TradeListAdapter adapter;
    private List<TradeListItem> listData;

    private boolean mSafe;
    private String mNextId = "0";
    private boolean mHasData;
    private String mLastId;
    private View mWaitView;
    private TextView tipTv;
    private RelativeLayout mDefaultlayout;
    private boolean isRefresh = false;

    private boolean scrollEable = false;

    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;
    private TextView balance;
    //private TextView timesContent;

    private boolean isShowLoad = true;

    private static final int CALLBACK_BALANCEINFO = 1;
    private NetworkManager mNetworkManager;
    private BalanceInfo balanceInfo;

//    private TextView tvRight;

    @Override
    protected int getContentView() {
        return R.layout.trade_list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
        setListener();
        setTitleText(getString(R.string.admin_list));
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.add:
                break;
            default:
                super.onClick(v);
                break;

        }
    }

    public void getBalanceInfo() {
        showLoadingView();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ACCOUNTS_BALANCE), params, this);
        mNetworkManager.load(CALLBACK_BALANCEINFO, path, this);
    }

    public void initView() {

        listView = (PullToRefreshListView) findViewById(R.id.listview);
        mWaitView = getLayoutInflater().inflate(R.layout.progress, null);
        tipTv = (TextView) findViewById(R.id.nodetailview);
        mDefaultlayout = (RelativeLayout) findViewById(R.id.default_layout);
        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
        balance = (TextView) findViewById(R.id.balance);
//        tvRight = (TextView) findViewById(R.id.tv_right);
//        tvRight.setVisibility(View.VISIBLE);
//        tvRight.setDettailText(getString(R.string.screen));
        //timesContent = (TextView) findViewById(R.id.times_content);
        findViewById(R.id.trade_type_view).setVisibility(View.VISIBLE);
        findViewById(R.id.add).setVisibility(View.GONE);
//        View search = findViewById(R.id.search);
//        RelativeLayout.LayoutParams lp =new RelativeLayout.LayoutParams(search.getWidth(),search.getHeight());
//        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        search.setLayoutParams(lp);
        initTitleLayout();
    }

    private void initData() {
        listData = new ArrayList<TradeListItem>();
        adapter = new TradeListAdapter(this, listData);
        listView.setAdapter(adapter);
        //String balanced = getIntent().getStringExtra("balance");
        //String formatResult = NumberFormat.getCurrencyInstance().format(Double.parseDouble(balanced));
        //balance.setDettailText(formatResult);
        getBalanceInfo();
        loadTradeInfo(mNextId);
    }

    private void setListener() {
        listView.setOnScrollListener(this);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                startTime = "";
                endTime = "";
                isRefresh = true;
                mLastId = "0";
                mNextId = "0";
                isShowLoad = false;
                scrollEable = false;
                listData.clear();
                ifSearch = false;
                loadTradeInfo(mNextId);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TradeListActivity.this, TradeDetail.class);
                intent.putExtra("data", listData.get(position - 1));
                startActivity(intent);
            }
        });
//        tvRight.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final DateFullDialogView dateDialogview = new DateFullDialogView(TradeListActivity.this,
//                        timesContent, "else", "date", Calendar.getInstance(), getResources().getColor(R.color.text_black), Calendar.getInstance());//年月日时分秒 当前日期之后选择
//                dateDialogview.show(timesContent);
//                dateDialogview.setOnSelectedListener(new DateFullDialogView.OnSelectedListener() {
//                    @Override
//                    public void onSelectedListener(long time) {
//                        searchTime=time;
//                        Calendar c =Calendar.getInstance();
//                        c.setTimeInMillis(searchTime);
//                        listData.clear();
//                        adapter.notifyDataSetChanged();
//                        loadTradeInfo("");
//                    }
//                });
//            }
//        });
    }

    String keyword;
    String startTime;
    String endTime;
    String type;
    boolean ifSearch = false;

    @Override
    public void searchRequest() {
        listData.clear();
        keyword = serchContextView.getText().toString();
        startTime = startTimeView.getText().toString();
        endTime = endTimeView.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (!TextUtil.isEmpty(startTime) && !startTime.equals(getString(R.string.no_time)))
                startTime = sdf.parse(startTime).getTime() + "";
            if (!TextUtil.isEmpty(endTime) && !endTime.equals(getString(R.string.no_time)))
                endTime = sdf.parse(endTime).getTime() + 24 * 60 * 60 * 1000 + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }

        scrollEable = false;
        ifSearch = true;
        loadTradeInfo("0");
    }

    //网络请求
    private void loadTradeInfo(String nextId) {
        findViewById(R.id.add).setVisibility(View.GONE);
        if (isShowLoad)
            showLoadingView();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();

        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        if (ifSearch)
            params.put("n", "100");
        else
            params.put("n", "12");
        if (!TextUtils.isEmpty(nextId) && !isRefresh)
            params.put("s", nextId);
        else
            params.put("s", "0");

        if (!TextUtil.isEmpty(startTime) && !startTime.equals(getString(R.string.no_time))) {
            params.put("start", startTime);
        }
        if (!TextUtil.isEmpty(endTime) && !endTime.equals(getString(R.string.no_time)))
            params.put("end", endTime);
        if (!TextUtil.isEmpty(keyword))
            params.put("keyword", keyword);

        if (!TextUtil.isEmpty(type) && !getString(R.string.all).equals(typeResultView.getText().toString()))
            params.put("type", type);

        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_TRANSACTIONS), params, this);
        mNetworkManager.load(GET_TRADE_LIST, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (isShowLoad)
            hideLoadingView();
        isShowLoad = true;
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            hideLoadingView();
            listView.onRefreshComplete();
            return;
        }
        switch (callbackId) {
            case CALLBACK_BALANCEINFO:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    balanceInfo = JsonDataFactory.getData(BalanceInfo.class, resutObject);
                    if (balanceInfo != null && !TextUtils.isEmpty(balanceInfo.balance)) {
                        balance.setText(balanceInfo.balance);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case GET_TRADE_LIST:
                listView.onRefreshComplete();
                mWaitView.setVisibility(View.GONE);
                List<TradeListItem> tradeItems = new ArrayList<TradeListItem>();
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mNextId = jsonObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    tradeItems = JsonDataFactory.getDataArray(TradeListItem.class, jsonObject.getJSONArray("records"));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter != null) {
                    if (isRefresh && !scrollEable) {
                        adapter.clear();
                        isRefresh = false;
                        listData.clear();
                    }
                    if (tradeItems != null) {
                        try {
                            listData.addAll(tradeItems);
                        } catch (Exception e) {

                        }

                        adapter.notifyDataSetChanged();
                    }
                    if (listData != null && listData.size() > 0) {
                        mDefaultlayout.setVisibility(View.GONE);
                    } else {
                        tipTv.setText(R.string.no_trade_detail);
                        mDefaultlayout.setVisibility(View.VISIBLE);
                    }
                }
                if (!ifSearch)
                    scrollEable = true;
                break;

        }
    }

    private HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

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
            ifSearch = false;
            loadTradeInfo(mNextId);
            scrollEable = true;
        }


    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mNetworkManager != null) {
            mNetworkManager.cancle(this);
            mNetworkManager = null;
        }


    }

    private void showLoadingView() {

        listView.setVisibility(View.INVISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMagView.setVisibility(View.VISIBLE);
        loadingMagView.setText(getString(R.string.dataloading));

    }

    private void hideLoadingView() {

        loadingLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingMagView.setVisibility(View.GONE);
        loadingMagView.setText(null);
        listView.setVisibility(View.VISIBLE);

    }

    public static final int CALLBACKID = 3423 << 4;

    public void toChoozeTradeType(View view) {
        startActivityForResult(new Intent(this, TradeTypeListActivity.class), CALLBACKID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case CALLBACKID:
                if (data == null)
                    return;
                TradeTypeListItem item = (TradeTypeListItem) data.getSerializableExtra("data");
                type = item.name;
                typeResultView.setText(item.value);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
