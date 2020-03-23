package com.vgtech.vancloud.ui.module.approval;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Approval;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.SearchLogAdapter;
import com.vgtech.vancloud.ui.adapter.VanCloudApprovalAdapter;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2016/9/25.
 */

public class VanCloudApprovalListFragment extends BaseFragment implements HttpListener<String>, PullToRefreshBase.OnRefreshListener2, AdapterView.OnItemClickListener, View.OnTouchListener, View.OnClickListener {

    private static String STATUS = "status";
    private static String TYPE = "type";
    private static String NUM = "num";
    private NetworkManager mNetworkManager;

    private String n = "12";
    private String nextId = "0";
    private String status;//0全部(抄送我的),3待审批(我发起的&我审批的)，4已审批(我发起的&我审批的)
    private String type;// 1我发起的，2我审批的，3抄送我的
    private int tab_num;

    PullToRefreshListView listView;
    private VancloudLoadingLayout loadingLayout;
    private VanCloudApprovalAdapter adapter;

    private final int CALLBACK_APPROVAL_LIST = 1;

    private TextView clickBtn;
    private ImageView searchCancelView;
    private EditText etKeywordView;
    private LinearLayout searchLayout;

    private TextView noSearchTextView;
    private TextView clearSearchBtn;
    private ListView searchLogListView;

    private SearchLogAdapter searchLogAdapter;
    private String searchType;// 1：我审批的待审批。2:我审批的已审批。3：我发起的。4：抄送我的

    private boolean isSearch = false;
    private String keyword;

    private TextView searchListTitle;

    private boolean showErrorLayout = true;


    public static VanCloudApprovalListFragment create(String status, String type, int tabNum) {
        VanCloudApprovalListFragment fragment = new VanCloudApprovalListFragment();
        Bundle args = new Bundle();
        args.putString(STATUS, status);
        args.putString(TYPE, type);
        args.putInt(NUM, tabNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        status = getArguments().getString(STATUS);
        type = getArguments().getString(TYPE);
        tab_num = getArguments().getInt(NUM, -1);

        if ("1".equals(type))
            searchType = "3";
        else if ("2".equals(type)) {
            if ("3".equals(status))
                searchType = "1";
            else if ("4".equals(status))
                searchType = "2";
        } else
            searchType = "4";


        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    protected int initLayoutId() {
        return R.layout.approval_list_layout;
    }

    @Override
    protected void initView(View view) {
        clickBtn = (TextView) view.findViewById(R.id.click_btn);
        searchCancelView = (ImageView) view.findViewById(R.id.search_cancel);
        etKeywordView = (EditText) view.findViewById(R.id.et_keyword);
        etKeywordView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchLayout = (LinearLayout) view.findViewById(R.id.search_layout);

        noSearchTextView = (TextView) view.findViewById(R.id.search_text);
        clearSearchBtn = (TextView) view.findViewById(R.id.clear_search_btn);
        searchLogListView = (ListView) view.findViewById(R.id.search_log_list);
        searchListTitle = (TextView) view.findViewById(R.id.search_title);

        listView = (PullToRefreshListView) view.findViewById(R.id.pull_list);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);
        adapter = new VanCloudApprovalAdapter(getActivity(), new ArrayList<Approval>(), type);

        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnTouchListener(this);
        searchLogListView.setOnTouchListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        getActivity().registerReceiver(mReceiver, intentFilter);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadingLayout.showLoadingView(listView, "", true);
                if (TextUtils.isEmpty(keyword)) {
                    nextId = "0";
                    getData("0");
                } else {
                    nextId = "0";
                    getSearchData("0", keyword);
                }
            }
        });


    }

    public void initsearchList() {

//        List<String> s = PrfUtils.getApprovalSearchLog(getActivity(), searchType);
        searchLogAdapter = new SearchLogAdapter(getActivity(), PrfUtils.getApprovalSearchLog(getActivity(), searchType), new SearchLogInterface() {
            @Override
            public void listNoData() {
                noSearchTextView.setVisibility(View.VISIBLE);
                clearSearchBtn.setVisibility(View.GONE);
                searchListTitle.setVisibility(View.GONE);
            }
        });

        searchLogListView.setAdapter(searchLogAdapter);
        if (searchLogAdapter.getList().size() == 0) {
            noSearchTextView.setVisibility(View.VISIBLE);
            clearSearchBtn.setVisibility(View.GONE);
            searchListTitle.setVisibility(View.GONE);
        } else {
            noSearchTextView.setVisibility(View.GONE);
            clearSearchBtn.setVisibility(View.VISIBLE);
            searchListTitle.setVisibility(View.VISIBLE);
        }

        searchLogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String s = searchLogAdapter.getList().get(position);
                etKeywordView.setText(s);
                gotoSearch();
            }
        });
    }

    @Override
    protected void initData() {
//        if (tab_num == 0) {
        loadingLayout.showLoadingView(listView, "", true);
        nextId = "0";
        getData("0");
        initsearchList();
//        }
    }

    @Override
    protected void initEvent() {

        etKeywordView.setOnClickListener(this);
        etKeywordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!etKeywordView.getText().toString().equals("")) {
                    searchCancelView.setVisibility(View.VISIBLE);
                    clickBtn.setText(getResources().getString(R.string.search));
                } else {
                    searchCancelView.setVisibility(View.INVISIBLE);
                    clickBtn.setText(getResources().getString(R.string.cancel));
                }
            }
        });

        etKeywordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)) {
                    //do something;
                    gotoSearch();
                    return true;
                }
                return false;
            }
        });
        searchCancelView.setOnClickListener(this);
        clickBtn.setOnClickListener(this);
        searchLayout.setTag(true);

        etKeywordView.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    openAnimation();
                } else {
                    // 此处为失去焦点时的处理内容
                }
            }
        });

        clearSearchBtn.setOnClickListener(this);
        searchLayout.setOnClickListener(this);

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

//    @Override
//    protected void lazyLoad() {
//        if (tab_num > 0) {
//            showDialog(listView, loadingLayout, "", true);
//            getData("0");
//        }
//    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        showErrorLayout = false;

        if (TextUtils.isEmpty(keyword)) {
            nextId = "0";
            getData("0");
        } else {
            nextId = "0";
            getSearchData("0", keyword);
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        showErrorLayout = false;
        if (TextUtils.isEmpty(keyword)) {
            getData(nextId);
        } else {
            getSearchData(nextId, keyword);
        }
    }


    public void getData(String nextid) {

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("state", status);
        params.put("type", type);
        params.put("n", n);
        params.put("s", nextid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_PROCESS_LIST_VANCLOUD), params, getActivity());
        mNetworkManager.load(CALLBACK_APPROVAL_LIST, path, this, false);
    }


    public void getSearchData(String nextid, String keyword) {

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("state", status);
        params.put("type", type);
        params.put("keyword", keyword);
        params.put("n", n);
        params.put("s", nextid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SEARCH_WORKFLOW_VANCLOUD), params, getActivity());
        mNetworkManager.load(CALLBACK_APPROVAL_LIST, path, this, false);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listView);
        listView.onRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == CALLBACK_APPROVAL_LIST) {

                if (showErrorLayout) {
                    loadingLayout.showErrorView(listView);
                } else {
                    showErrorLayout = true;
                    if (adapter.getList().size() <= 0) {
                        loadingLayout.showErrorView(listView);
                    }
                }
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_APPROVAL_LIST:
                showErrorLayout = true;
                List<Approval> approvalList = new ArrayList<Approval>();
                String oldeNextId = path.getPostValues().get("s");
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    String id = jsonObject.getString("nextid");
                    int count = jsonObject.getInt("count");
                    if ("2".equals(type)) {
                        MyApprovalActivity myApprovalActivity = (MyApprovalActivity) getActivity();
                        myApprovalActivity.chaneTitle(tab_num, count);
                    }
                    if (!TextUtils.isEmpty(id) && !"0".equals(id)) {
                        nextId = id;
                    }
                    approvalList = JsonDataFactory.getDataArray(Approval.class, jsonObject.getJSONArray("rows"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter == null) {
                    adapter = new VanCloudApprovalAdapter(getActivity(), approvalList, type);
                    listView.setAdapter(adapter);
                } else {
                    if ("0".equals(oldeNextId)) {
                        adapter.myNotifyDataSetChanged(approvalList);
                    } else {
                        List<Approval> list = adapter.getList();
                        list.addAll(approvalList);
                        adapter.myNotifyDataSetChanged(list);
                    }
                }
                if (adapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_list_data), true, true);
                    listView.setVisibility(View.VISIBLE);
                }
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null)
            adapter.destroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (mReceiver != null)
            getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Approval approval = adapter.getList().get(position - listView.getRefreshableView().getHeaderViewsCount());
        Intent intent = new Intent(getActivity(), FlowHandleActivity.class);
        intent.putExtra(FlowHandleActivity.TYPER, Integer.valueOf(type));
        intent.putExtra(FlowHandleActivity.FLOWID, approval.processid);
        intent.putExtra(FlowHandleActivity.POSITION, position - listView.getRefreshableView().getHeaderViewsCount());
        startActivityForResult(intent, 1);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideKeyboard();
        return false;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(etKeywordView.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.et_keyword:
                openAnimation();
                break;

            case R.id.click_btn:
                if (getString(R.string.search).equals(clickBtn.getText().toString())) {
                    gotoSearch();
                } else {
                    hideKeyboard();
                    etKeywordView.setText("");
                    if (searchLayout.getVisibility() == View.VISIBLE) {
                        closeAnimation(searchLayout, new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                searchLayout.setTag(false);
                                clickBtn.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                searchLayout.setVisibility(View.INVISIBLE);
                                searchLayout.setTag(true);
                                loadingLayout.showLoadingView(listView, "", true);
                                nextId = "0";
                                keyword = "";
                                getData("0");
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    } else {
                        clickBtn.setVisibility(View.GONE);
                        loadingLayout.showLoadingView(listView, "", true);
                        nextId = "0";
                        keyword = "";
                        getData("0");
                    }
                }


                break;

            case R.id.search_cancel:
                etKeywordView.setText("");
                break;

            case R.id.clear_search_btn:

                PrfUtils.clearApprovalSearchLog(getActivity(), searchType);
                searchLogAdapter.myNotifyDataSetChanged(new ArrayList<String>());
                noSearchTextView.setVisibility(View.VISIBLE);
                clearSearchBtn.setVisibility(View.GONE);
                searchListTitle.setVisibility(View.GONE);
                break;
        }
    }


    /**
     * 展开动画
     */
    public void openAnimation() {

        if ((boolean) searchLayout.getTag() && searchLayout.getVisibility() == View.INVISIBLE) {

            int height = searchLayout.getHeight();
            setListviewMaxHeight();
            Animation translateAnimation = new TranslateAnimation(0, 0,
                    height, 0);
            translateAnimation.setDuration(300);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    clickBtn.setVisibility(View.VISIBLE);
                    searchLayout.setTag(false);
                    searchLayout.setVisibility(View.VISIBLE);
                    searchLogAdapter.myNotifyDataSetChanged(PrfUtils.getApprovalSearchLog(getActivity(), searchType));
                    if (searchLogAdapter.getList().size() == 0) {
                        noSearchTextView.setVisibility(View.VISIBLE);
                        clearSearchBtn.setVisibility(View.GONE);
                        searchListTitle.setVisibility(View.GONE);
                    } else {
                        noSearchTextView.setVisibility(View.GONE);
                        clearSearchBtn.setVisibility(View.VISIBLE);
                        searchListTitle.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    searchLayout.setTag(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            searchLayout.startAnimation(translateAnimation);
        }
    }

    public void closeAnimation(View translateAnimationView, Animation.AnimationListener listener) {

        if ((boolean) translateAnimationView.getTag() && translateAnimationView.getVisibility() == View.VISIBLE) {
            int height = translateAnimationView.getHeight();
            Animation translateAnimation = new TranslateAnimation(0, 0,
                    0, height);
            translateAnimation.setDuration(300);
            translateAnimation.setAnimationListener(listener);
            translateAnimationView.startAnimation(translateAnimation);
        }
    }

    public void gotoSearch() {

        hideKeyboard();
        keyword = etKeywordView.getText().toString();
//        etKeywordView.setFocusable(false);
        if (!TextUtils.isEmpty(keyword)) {

            if (searchLayout.getVisibility() == View.VISIBLE) {

                closeAnimation(searchLayout, new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        searchLayout.setTag(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        searchLayout.setVisibility(View.INVISIBLE);
                        searchLayout.setTag(true);
                        loadingLayout.showLoadingView(listView, "", true);
                        nextId = "0";
                        getSearchData("0", keyword);
                        PrfUtils.setApprovalSearchLog(getActivity(), searchType, keyword);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                loadingLayout.showLoadingView(listView, "", true);
                nextId = "0";
                getSearchData("0", keyword);
                PrfUtils.setApprovalSearchLog(getActivity(), searchType, keyword);
            }
        }
    }

    /**
     * 重新设置listview高度
     */
    public void setListviewMaxHeight() {

        int height = searchLayout.getHeight();
        int h = Utils.sysDpToPx(getResources(), 40);
        int toph = Utils.sysDpToPx(getResources(), 10);

        if ((height - h - toph) / h < searchLogAdapter.getCount()) {
            searchLogListView.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height - h - toph));
        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_FLOW_CONDUCT://流程处理

                        if ("2".equals(type)) {
                            if (TextUtils.isEmpty(keyword)) {
                                loadingLayout.showLoadingView(listView, "", true);
                                nextId = "0";
                                getData("0");
                            } else {
                                gotoSearch();
                            }
                        }
                        break;
                }
            }
        }
    };
}
