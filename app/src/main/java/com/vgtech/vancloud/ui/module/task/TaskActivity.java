package com.vgtech.vancloud.ui.module.task;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.Task;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.TaskAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务列表
 * Created by Duke on 2015/9/8.
 */
public class TaskActivity extends SearchBaseActivity implements View.OnClickListener, HttpListener<String> {

    View leftShadeView;
    View rightShadeView;
    LinearLayout leftTypeGroup;
    TextView leftTextView;
    RelativeLayout leftCLickLayout;
    ImageView leftArrowView;

    LinearLayout rightTypeGroup;
    TextView rightTextView;
    RelativeLayout rightCLickLayout;
    ImageView rightArrowView;

    PullToRefreshListView listView;

    LinearLayout typeGroup;

    View titleShadeView;

    //1-自己，2-下属.
    private String permission = "1";
    // 1-我执行的，2-我发布的，3-抄送我的，默认0-全部
    private String type = "0";
    //完成-1，未完成-2，默认0-全部
    private String state = "0";
    //分页的大小
    private int n = 10;

    private String nextId = "0";

    private NetworkManager mNetworkManager;

    private TaskAdapter adapter;

    private int listViewRefreshType;//0,默认;1,上滑刷新；2,下拉刷新;3,广播刷新

    private static final int CALLBACK_TASKLIST = 1;

    String stime;
    String etime;


    public static final String RECEIVER_DRAFT = "RECEIVER_DRAFT";
    public static final String RECEIVER_PUSH = "RECEIVER_PUSH";
    private String RECEIVER_ERROR = "RECEIVER_ERROR";

//    private LinearLayout loadingLayout;

    private RelativeLayout myButtonLayout;
    private RelativeLayout subordinateButtonLayout;

    private RelativeLayout rightAllLayout;
    private RelativeLayout rightFinishLayout;
    private RelativeLayout rightNotFinishLayout;

    private RelativeLayout leftAllLayout;
    private RelativeLayout leftExecuteLayout;
    private RelativeLayout leftPublishLayout;
    private RelativeLayout leftCpeLayout;

    private VancloudLoadingLayout loadingLayout;

//    private NoticeLayout noticeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initEvent();
        initData();
    }

    @Override
    protected int getContentView() {
        return R.layout.task_activity_layout;
    }


    private void initView() {

        initTitleLayout();
        setTitleText(getString(R.string.my_task));
        arrowView.setVisibility(View.VISIBLE);
        leftShadeView = findViewById(R.id.left_shade);
        rightShadeView = findViewById(R.id.right_shade);
        leftTypeGroup = (LinearLayout) findViewById(R.id.left_type_group);
        leftTextView = (TextView) findViewById(R.id.left_text);
        leftCLickLayout = (RelativeLayout) findViewById(R.id.left_layout);
        leftArrowView = (ImageView) findViewById(R.id.left_arrow);

        rightTypeGroup = (LinearLayout) findViewById(R.id.right_type_group);
        rightTextView = (TextView) findViewById(R.id.right_text);
        rightCLickLayout = (RelativeLayout) findViewById(R.id.right_layout);
        rightArrowView = (ImageView) findViewById(R.id.right_arrow);


        listView = (PullToRefreshListView) findViewById(R.id.listview);
        typeGroup = (LinearLayout) findViewById(R.id.type_group);
        titleShadeView = findViewById(R.id.title_shade);


        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading_layout);

        typeGroup.setTag(true);
        leftTypeGroup.setTag(true);
        rightTypeGroup.setTag(true);

        myButtonLayout = (RelativeLayout) findViewById(R.id.my_button);
        subordinateButtonLayout = (RelativeLayout) findViewById(R.id.subordinate_button);

        myButtonLayout.setOnClickListener(this);
        subordinateButtonLayout.setOnClickListener(this);
        myButtonLayout.setSelected(true);

        rightAllLayout = (RelativeLayout) findViewById(R.id.right_all);
        rightFinishLayout = (RelativeLayout) findViewById(R.id.right_finish);
        rightNotFinishLayout = (RelativeLayout) findViewById(R.id.right_not_finish);
        rightAllLayout.setOnClickListener(this);
        rightFinishLayout.setOnClickListener(this);
        rightNotFinishLayout.setOnClickListener(this);
        rightAllLayout.setSelected(true);

        leftAllLayout = (RelativeLayout) findViewById(R.id.left_all);
        leftExecuteLayout = (RelativeLayout) findViewById(R.id.left_execute);
        leftPublishLayout = (RelativeLayout) findViewById(R.id.left_publish);
        leftCpeLayout = (RelativeLayout) findViewById(R.id.left_cope);

        leftAllLayout.setOnClickListener(this);
        leftExecuteLayout.setOnClickListener(this);
        leftPublishLayout.setOnClickListener(this);
        leftCpeLayout.setOnClickListener(this);
        leftAllLayout.setSelected(true);

//        noticeLayout = (NoticeLayout) findViewById(R.id.layout_notice);
//        noticeLayout = new NoticeLayout(TaskActivity.this);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
    }


    private void initData() {

        nextId = "0";
        adapter = new TaskAdapter(this, new ArrayList<Task>());
        listView.setAdapter(adapter);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listViewRefreshType = 0;
        initDate(permission, type, state, nextId, stime, etime);

    }

    private void initEvent() {

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                listViewRefreshType = 2;
                nextId = "0";
                initDate(permission, type, state, nextId, stime, etime);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                listViewRefreshType = 1;
                initDate(permission, type, state, nextId, stime, etime);
            }
        });


        leftCLickLayout.setOnClickListener(this);
        rightCLickLayout.setOnClickListener(this);
        leftTypeGroup.setOnClickListener(this);
        rightTypeGroup.setOnClickListener(this);
        typeGroup.setOnClickListener(this);
        titleShadeView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.add:
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case R.id.left_layout:
                if (leftShadeView.getVisibility() == View.VISIBLE) {

                    hideRightTypeselectView();

                } else {

                    if (leftTypeGroup.getVisibility() == View.INVISIBLE) {
                        showLeftTypeselectView();
                    } else {
                        hideLeftTypeselectView();
                    }
                }

                break;

            case R.id.right_layout:
                if (rightShadeView.getVisibility() == View.VISIBLE) {

                    hideLeftTypeselectView();

                } else {

                    if (rightTypeGroup.getVisibility() == View.INVISIBLE) {
                        showRightTypeselectView();
                    } else {
                        hideRightTypeselectView();
                    }

                }

                break;

            case R.id.shade_view:

                if (leftTypeGroup.getVisibility() == View.VISIBLE) {
                    hideLeftTypeselectView();
                }
                if (rightTypeGroup.getVisibility() == View.VISIBLE) {
                    hideRightTypeselectView();
                }
                if (typeGroup.getVisibility() == View.VISIBLE) {
                    hideTopTypeselectView();
                }
                if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
                    hideAdvancedSearchLayout();
                }
                break;

            case R.id.top_type_click:

                if (typeGroup.getVisibility() == View.VISIBLE) {
                    hideTopTypeselectView();
                } else {
                    showTopTypeselectView();
                }
                break;

            case R.id.title_shade:
                if (leftTypeGroup.getVisibility() == View.VISIBLE) {
                    hideLeftTypeselectView();

                }
                if (rightTypeGroup.getVisibility() == View.VISIBLE) {
                    hideRightTypeselectView();
                }

                if (typeGroup.getVisibility() == View.VISIBLE) {
                    hideTopTypeselectView();
                }

                break;

            case R.id.my_button:
                if (!myButtonLayout.isSelected()) {
                    nextId = "0";
                    subordinateButtonLayout.setSelected(false);
                    myButtonLayout.setSelected(true);
                    setTitleText(getResources().getString(R.string.my_task));
                    if (leftCLickLayout.getVisibility() == View.GONE) {
                        leftCLickLayout.setVisibility(View.VISIBLE);
                    }
                    hideTopTypeselectView();
                    permission = "1";
                    initDate(permission, type, state, nextId, stime, etime);
                }

                break;

            case R.id.subordinate_button:
                if (!subordinateButtonLayout.isSelected()) {
                    nextId = "0";
                    myButtonLayout.setSelected(false);
                    subordinateButtonLayout.setSelected(true);
                    setTitleText(getResources().getString(R.string.subordinate_task));
                    if (leftCLickLayout.getVisibility() == View.VISIBLE) {
                        leftCLickLayout.setVisibility(View.GONE);
                    }
                    hideTopTypeselectView();
                    permission = "2";
                    initDate(permission, type, state, nextId, stime, etime);
                }
                break;

            //完成-1，未完成-2，默认0-全部
            case R.id.right_all:
                if (!rightAllLayout.isSelected()) {
                    nextId = "0";
                    rightFinishLayout.setSelected(false);
                    rightNotFinishLayout.setSelected(false);
                    rightAllLayout.setSelected(true);
                    rightTextView.setText(getResources().getString(R.string.all));
                    hideRightTypeselectView();
                    state = "0";
                    initDate(permission, type, state, nextId, stime, etime);
                }
                break;

            case R.id.right_finish:
                if (!rightFinishLayout.isSelected()) {
                    nextId = "0";
                    rightNotFinishLayout.setSelected(false);
                    rightAllLayout.setSelected(false);
                    rightFinishLayout.setSelected(true);
                    rightTextView.setText(getResources().getString(R.string.finish_task));
                    hideRightTypeselectView();
                    state = "1";
                    initDate(permission, type, state, nextId, stime, etime);
                }


                break;
            case R.id.right_not_finish:
                if (!rightNotFinishLayout.isSelected()) {
                    nextId = "0";
                    rightAllLayout.setSelected(false);
                    rightFinishLayout.setSelected(false);
                    rightNotFinishLayout.setSelected(true);

                    rightTextView.setText(getResources().getString(R.string.not_finish));
                    hideRightTypeselectView();
                    state = "2";
                    initDate(permission, type, state, nextId, stime, etime);
                }
                break;

            // 1-我执行的，2-我发布的，3-抄送我的，默认0-全部
            case R.id.left_all:
                if (!leftAllLayout.isSelected()) {
                    nextId = "0";
                    leftExecuteLayout.setSelected(false);
                    leftPublishLayout.setSelected(false);
                    leftCpeLayout.setSelected(false);
                    leftAllLayout.setSelected(true);
                    leftTextView.setText(getResources().getString(R.string.all));
                    hideLeftTypeselectView();
                    type = "0";
                    initDate(permission, type, state, nextId, stime, etime);
                }
                break;
            case R.id.left_execute:

                if (!leftExecuteLayout.isSelected()) {
                    nextId = "0";
                    leftPublishLayout.setSelected(false);
                    leftCpeLayout.setSelected(false);
                    leftAllLayout.setSelected(false);
                    leftExecuteLayout.setSelected(true);
                    leftTextView.setText(getResources().getString(R.string.execute_my));
                    hideLeftTypeselectView();
                    type = "1";
                    initDate(permission, type, state, nextId, stime, etime);
                }
                break;

            case R.id.left_publish:
                if (!leftPublishLayout.isSelected()) {
                    nextId = "0";
                    leftCpeLayout.setSelected(false);
                    leftAllLayout.setSelected(false);
                    leftExecuteLayout.setSelected(false);
                    leftPublishLayout.setSelected(true);
                    leftTextView.setText(getResources().getString(R.string.publish_my));
                    hideLeftTypeselectView();
                    type = "2";
                    initDate(permission, type, state, nextId, stime, etime);
                }
                break;
            case R.id.left_cope:
                if (!leftCpeLayout.isSelected()) {
                    nextId = "0";
                    leftPublishLayout.setSelected(false);
                    leftAllLayout.setSelected(false);
                    leftExecuteLayout.setSelected(false);
                    leftCpeLayout.setSelected(true);
                    leftTextView.setText(getResources().getString(R.string.copek_my));
                    hideLeftTypeselectView();
                    type = "3";
                    initDate(permission, type, state, nextId, stime, etime);
                }
                break;


//            case R.id.main_shade:
//
//                if (leftTypeGroup.getVisibility() == View.VISIBLE) {
//                    hideLeftTypeselectView();
//
//                }
//                if (rightTypeGroup.getVisibility() == View.VISIBLE) {
//                    hideRightTypeselectView();
//                }
//
//                if (typeGroup.getVisibility() == View.VISIBLE) {
//                    hideTopTypeselectView();
//                }
//
//                if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
//                    hideAdvancedSearchLayout();
//                }
//                break;

            default:
                super.onClick(v);
                break;
        }

    }


    public void showLeftTypeselectView() {

        leftTextView.setTextColor(getResources().getColor(R.color.comment_blue));
        leftArrowView.setImageResource(R.mipmap.type_arrow_bule);
        rightTextView.setTextColor(getResources().getColor(R.color.comment_grey));
        rightArrowView.setImageResource(R.mipmap.type_arrow_grey);

        openAnimation(leftTypeGroup, leftArrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                leftTypeGroup.setTag(false);
                leftTypeGroup.setVisibility(View.VISIBLE);
                shadeView.setVisibility(View.VISIBLE);
                rightShadeView.setVisibility(View.VISIBLE);
                titleShadeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                leftTypeGroup.setTag(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void hideLeftTypeselectView() {

        closeAnimation(leftTypeGroup, leftArrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                leftTypeGroup.setTag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                leftTypeGroup.setTag(true);
                shadeView.setVisibility(View.GONE);
                rightShadeView.setVisibility(View.GONE);
                titleShadeView.setVisibility(View.GONE);
                leftTypeGroup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public void showRightTypeselectView() {

        rightTextView.setTextColor(getResources().getColor(R.color.comment_blue));
        rightArrowView.setImageResource(R.mipmap.type_arrow_bule);

        leftTextView.setTextColor(getResources().getColor(R.color.comment_grey));
        leftArrowView.setImageResource(R.mipmap.type_arrow_grey);

        openAnimation(rightTypeGroup, rightArrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                rightTypeGroup.setTag(false);
                shadeView.setVisibility(View.VISIBLE);
                rightTypeGroup.setVisibility(View.VISIBLE);
                leftShadeView.setVisibility(View.VISIBLE);
                titleShadeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rightTypeGroup.setTag(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public void hideRightTypeselectView() {


        closeAnimation(rightTypeGroup, rightArrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                rightTypeGroup.setTag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rightTypeGroup.setTag(true);
                shadeView.setVisibility(View.GONE);
                leftShadeView.setVisibility(View.GONE);
                titleShadeView.setVisibility(View.GONE);
                rightTypeGroup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 显示顶部分类选择布局
     */
    public void showTopTypeselectView() {

        openAnimation(typeGroup, arrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                typeGroup.setTag(false);
                typeGroup.setVisibility(View.VISIBLE);
                titleShadeView.setVisibility(View.VISIBLE);
                shadeView.setVisibility(View.VISIBLE);
                leftShadeView.setVisibility(View.VISIBLE);
                rightShadeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                typeGroup.setTag(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 隐藏顶部分类选择布局
     */
    public void hideTopTypeselectView() {


        closeAnimation(typeGroup, arrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                typeGroup.setTag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                typeGroup.setTag(true);
                typeGroup.setVisibility(View.INVISIBLE);
                titleShadeView.setVisibility(View.GONE);
                shadeView.setVisibility(View.GONE);
                leftShadeView.setVisibility(View.GONE);
                rightShadeView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void initDate(String permission, String type, String state, String nextId, String stime, String etime) {

        if (listView.getMode() == PullToRefreshBase.Mode.PULL_FROM_START)
            listView.setMode(PullToRefreshBase.Mode.BOTH);
        if (listViewRefreshType == 0)
            loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("permission", permission);
        params.put("type", type);
        params.put("state", state);

        if (!TextUtils.isEmpty(stime))
            params.put("startdate", stime);
        if (!TextUtils.isEmpty(etime))
            params.put("enddate", etime);

        params.put("n", n + "");
        params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_TASK_LIST), params, this);
        mNetworkManager.load(CALLBACK_TASKLIST, path, this, listViewRefreshType == 0);
    }


    private void initDate(String permission, String type, String state, String nextId, String keyword) {

        if (listViewRefreshType == 0)
            loadingLayout.showLoadingView(listView, "", true);

        nextId = "0";
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("permission", permission);
        params.put("type", type);
        params.put("state", state);
        params.put("n", n + "");
        params.put("s", nextId);
        params.put("keyword", keyword);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_TASK_LIST), params, this);
        mNetworkManager.load(CALLBACK_TASKLIST, path, this, true);
    }


    private void searchDate(String permission, String type, String state, String nextId, String keyword, String startTime, String endTime) {

        loadingLayout.showLoadingView(listView, "", true);
        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        nextId = "0";
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("permission", permission);
        params.put("type", type);
        params.put("state", state);
        params.put("n", "50");
        params.put("s", nextId);
        if (!TextUtils.isEmpty(keyword))
            params.put("keyword", keyword);
        if (!TextUtils.isEmpty(startTime) && !"0".equals(startTime))
            params.put("startdate", startTime);
        if (!TextUtils.isEmpty(endTime) && !"0".equals(endTime))
            params.put("enddate", endTime);

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SEARCH_TASK), params, this);
        mNetworkManager.load(CALLBACK_TASKLIST, path, this, true);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        loadingLayout.dismiss(listView);
        listView.onRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (adapter.getMlist().size() <= 0) {
                loadingLayout.showErrorView(listView, "", true, true);
            }
            return;
        }
        switch (callbackId) {

            case CALLBACK_TASKLIST:
                List<Task> taskList = new ArrayList<Task>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
//                    nextId = resutObject.getString("next_id");
                    String id = resutObject.getString("nextid");
                    if (!TextUtils.isEmpty("id") && !"0".equals(id)) {
                        nextId = id;
                    }
                    taskList = JsonDataFactory.getDataArray(Task.class, resutObject.getJSONArray("rows"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (adapter == null) {
                    adapter = new TaskAdapter(this, taskList);
                    listView.setAdapter(adapter);
                } else {
                    String s = path.getPostValues().get("s");
                    if ("0".equals(s) || TextUtils.isEmpty(s)) {
                        adapter.getMlist().clear();
                    }
                    switch (listViewRefreshType) {
                        case 1:
                            List<Task> list = adapter.getMlist();
                            list.addAll(taskList);
                            adapter.myNotifyDataSetChanged(list);
                            listView.onRefreshComplete();
                            listViewRefreshType = 0;
                            break;
                        case 2:
                            adapter.myNotifyDataSetChanged(taskList);
                            listView.onRefreshComplete();
                            listViewRefreshType = 0;
                            break;
                        default:
                            listViewRefreshType = 0;
                            adapter.myNotifyDataSetChanged(taskList);
                            break;
                    }
                    if (adapter.getMlist().size() <= 0) {
                        loadingLayout.showEmptyView(listView, getString(R.string.no_task_detail), true, true);
                        listView.setVisibility(View.VISIBLE);
                    }
                }

                break;

            default:
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
    public void searchRequest() {
        String starttime = startTimeView.getText().toString();
        String endtime = endTimeView.getText().toString();
        long sTime = 0;
        long eTime = 0;
        if (!TextUtils.isEmpty(starttime) && !getString(R.string.no_time).equals(starttime)) {
            sTime = Utils.dateFormat(starttime, "yyyy-MM-dd");
        }
        if (!TextUtils.isEmpty(endtime) && !getString(R.string.no_time).equals(endtime)) {
            eTime = Utils.dateFormat(endtime, "yyyy-MM-dd");
        }
        nextId = "0";
        Log.e("ceshi", "keyword------" + serchContextView.getText().toString()
                + "------startTime------" + sTime + "/" + starttime
                + "------endTime------" + eTime + "/" + endtime);
        searchDate(permission, type, state, nextId, serchContextView.getText().toString(), sTime + "", eTime + "");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_TASK:
                    case PublishTask.PUBLISH_TASK_CONDUCT:
                    case PublishTask.PUBLISH_RECRUIT_FINISH:
                        listViewRefreshType = 3;
                        nextId = "0";
                        initDate(permission, type, state, nextId, stime, etime);
                        break;

                    case PublishTask.PUBLISH_COMMENT:
                        int position = intent.getIntExtra("position", -1);
                        int commentType = intent.getIntExtra("commentType", -1);
                        if (position >= 0 && commentType == PublishUtils.COMMENTTYPE_TASK) {
                            adapter.chaneCommentNum(position);
                        }
                        break;
                }
            } else if (RECEIVER_PUSH.equals(action)) {

                String infoType = intent.getStringExtra("infotype");
                String infoid = intent.getStringExtra("infoid");
                if ("11".equals(infoType)) {
                    listViewRefreshType = 3;
                    nextId = "0";
                    initDate(permission, type, state, nextId, stime, etime);
                }
            } else if (RECEIVER_ERROR.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_TASK:
                    case PublishTask.PUBLISH_TASK_CONDUCT:
                        String msg = intent.getStringExtra("rootDataMsg");
                        if (!TextUtil.isEmpty(msg)) {
                            listViewRefreshType = 3;
                            nextId = "0";
                            initDate(permission, type, state, nextId, stime, etime);
                            Toast.makeText(TaskActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", -1);
                    if (position >= 0) {
                        String json = data.getStringExtra("json");
                        Task task = null;
                        try {
                            if (!TextUtil.isEmpty(json))
                                task = JsonDataFactory.getData(Task.class, new JSONObject(json));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        adapter.chaneTask(position, task.getJson());
                    }
                }
                break;

            case 2000:
                if (resultCode == Activity.RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("backRefresh", false);
                    if (refresh) {
                        listViewRefreshType = 3;
                        nextId = "0";
                        initDate(permission, type, state, nextId, stime, etime);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_DRAFT);
        intentFilter.addAction(RECEIVER_PUSH);
        intentFilter.addAction(RECEIVER_ERROR);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (adapter != null)
            adapter.destroy();
    }
}
