package com.vgtech.vancloud.ui.module.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import com.EventBusMsg;
import com.android.volley.VolleyError;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.d.lib.slidelayout.SlideLayout;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.CommonUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.TodoNotification;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.base.LazyLoadFragment;
import com.vgtech.vancloud.utils.NotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待办
 * Created by Duke on 2016/9/20.
 */

public class ToDoNotificationNewListFragment extends LazyLoadFragment
        implements HttpListener<String>,
        BaseQuickAdapter.OnItemClickListener,
        BaseQuickAdapter.OnItemChildClickListener,
        BaseQuickAdapter.RequestLoadMoreListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String PUSH_TODO_MESSAGE = "PUSH_TODO_MESSAGE";
    public static final String RECEIVER_PUSH = "RECEIVER_PUSH";
    private static String STATUS = "status";
    private static String TYPE = "type";
    private static String NUM = "num";
    private NetworkManager mNetworkManager;
    private final int CALLBACK_TODOLIST = 1;//删除VANTOP待办
    private final int CALLBACK_DELETE = 2;//删除
    private final int CALLBACK_DELETE_TOP_QUESSURVEY = 3;//删除VANTOP问卷


    private final int CALLBACK_DELETE_VANTOP = 4;

    private String n = "12";
    private String nextId = "0";
    private String status;


    private String type;
    private int tab_num;

    private SwipeRefreshLayout swipe_container;
    private VancloudLoadingLayout loadingLayout;
    private RecyclerView recyclerView;

    private ToDoNotificationNewAdapter adapter;

    private int deletePosition = -1;//记录删除标记

    //    boolean backRefresh = false;
    int page = 0;
    private boolean showErrorLayout = true;


    public static ToDoNotificationNewListFragment create(String status, String type, int tabNum) {
        ToDoNotificationNewListFragment fragment = new ToDoNotificationNewListFragment();
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
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    protected int initLayoutId() {
        return R.layout.handle_list_layout_new;
    }

    @Override
    protected void initView(View view) {

        swipe_container = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipe_container.setOnRefreshListener(this);
        //设置样式刷新显示的位置
        swipe_container.setProgressViewOffset(true, -20, 100);
        swipe_container.setColorSchemeResources(R.color.red, R.color.blue, R.color.black);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadingLayout.showLoadingView(recyclerView, "", true);
                getToDoListData("0");
            }
        });
        List<TodoNotification> todoNotifications = new ArrayList<>();
        adapter = new ToDoNotificationNewAdapter(todoNotifications);
        adapter.setOnItemChildClickListener(this);
        adapter.setOnItemClickListener(this);
        adapter.setOnLoadMoreListener(this);

        loadLocalPushDatas();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(getRecyclerViewDivider(R.drawable.inset_recyclerview_divider));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        intentFilter.addAction(CommonUtils.ACTION_APPROVAL_PROCESS);
        intentFilter.addAction(PUSH_TODO_MESSAGE);
        getActivity().registerReceiver(mReceiver, intentFilter);

        EventBus.getDefault().register(this);
    }


    private void loadLocalPushDatas() {
        new AsyncTask<Void, Void, Map<String, Integer>>() {
            @Override
            protected Map<String, Integer> doInBackground(Void... params) {
                List<MessageDB> messageDBList = MessageDB.queryPushTodoMessage(getActivity());
                Map<String, Integer> integerMap = new HashMap<String, Integer>();
                for (MessageDB m : messageDBList) {
                    try {
                        PushMessage pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(m.content));
                        if ("hasten".equals(pushMessage.operationType)) {
                            String resId = pushMessage.resId;
                            int c = 0;
                            if (integerMap.containsKey(resId)) {
                                c = integerMap.get(resId);
                            }
                            integerMap.put(resId, ++c);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                //待办和催办 推送类型一样，估次数减1
//                Set<Map.Entry<String, Integer>> entries = integerMap.entrySet();
//                Iterator<Map.Entry<String, Integer>> iter = entries.iterator();
//                while (iter.hasNext()) {
//                    Map.Entry<java.lang.String, java.lang.Integer> entry = iter
//                            .next();
//                    entry.setValue(entry.getValue() - 1);
//                }
                return integerMap;
            }

            @Override
            protected void onPostExecute(Map<String, Integer> messageDBList) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;
                adapter.addPushMessage(messageDBList);
            }
        }.execute();
    }

    @Override
    protected void initData() {

        if (tab_num == 0) {
            loadingLayout.showLoadingView(recyclerView, "", true);
            getToDoListData("0");
        }
    }

    @Override
    protected void lazyLoad() {
        if (tab_num > 0) {
            loadingLayout.showLoadingView(recyclerView, "", true);
            getToDoListData("0");
        }
    }

    @Override
    protected void initEvent() {

    }

    public void getToDoListData(String nextid) {

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("status", status);
        params.put("type", type);
        params.put("n", n);
        params.put("s", nextid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_TODO_LIST), params, getActivity());
        mNetworkManager.load(CALLBACK_TODOLIST, path, this, false);

    }

    public void deleteToDoNotification(String todoid) {

        showLoadingDialog(getActivity(), getString(R.string.prompt_info_01));
        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("todo_id", todoid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_TODO_DELETE), params, getActivity());
        mNetworkManager.load(CALLBACK_DELETE, path, this, false);

    }

    public void readToDoNotification(String todoid) {

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("todo_id", todoid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_TODO_MARK), params, getActivity());
        mNetworkManager.load(3, path, this, false);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (mReceiver != null)
            getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void dataLoaded(int callbackId, final NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        loadingLayout.dismiss(recyclerView);
        swipe_container.setRefreshing(false);
//        adapter.loadMoreComplete();


        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == CALLBACK_TODOLIST) {

                if (showErrorLayout)
                    loadingLayout.showErrorView(recyclerView, "", true, true);
                else {
                    showErrorLayout = true;
                    if (adapter.getData().size() <= 0) {
                        loadingLayout.showErrorView(recyclerView, "", true, true);
                    }
                }
            } else if (callbackId == CALLBACK_DELETE) {
                deletePosition = -1;
            }

            return;
        }
        switch (callbackId) {
            case CALLBACK_TODOLIST:

                showErrorLayout = true;
                List<TodoNotification> todoNotifications = new ArrayList<TodoNotification>();

                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    String id = jsonObject.getString("nextid");
                    if (!TextUtils.isEmpty(id)) {
                        nextId = id;

                    } else {
                        nextId = "0";
                    }
                    todoNotifications = JsonDataFactory.getDataArray(TodoNotification.class, jsonObject.getJSONArray("rows"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (page == 0) {
                    if ("0".equals(nextId)) {
                        adapter.setNewData(todoNotifications);
                        adapter.loadMoreEnd();
                    } else {
                        adapter.setNewData(todoNotifications);
                        adapter.loadMoreComplete();
                    }
                } else {
                    if ("0".equals(nextId)) {
                        adapter.addData(todoNotifications);
                        adapter.loadMoreEnd();
                    } else {
                        adapter.addData(todoNotifications);
                        adapter.loadMoreComplete();
                    }
                }


                if (adapter.getData().size() <= 0) {
                    loadingLayout.showEmptyView(recyclerView, getString(R.string.no_information_todo), true, true);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                break;

            case CALLBACK_DELETE:
                Toast.makeText(getActivity(), getString(R.string.del_user_sul), Toast.LENGTH_SHORT).show();
                if (deletePosition >= 0) {
                    adapter.remove(deletePosition);
                    deletePosition = -1;
                    if (adapter.getData().size() <= 0) {
                        loadingLayout.showEmptyView(recyclerView, getString(R.string.no_information_todo), true, true);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
//                getToDoListData("0");
                break;
            case CALLBACK_DELETE_VANTOP:
                if (vantopDeletPosition >= 0) {
//                    adapter.deleteItem(vantopDeletPosition);
                    vantopDeletPosition = -1;
                    getToDoListData("0");
//                    if (adapter.getList().size() <= 0) {
//                        loadingLayout.showEmptyView(recyclerView, getString(R.string.no_information_todo), true, true);
//                        recyclerView.setVisibility(View.VISIBLE);
//                    }
                }
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        dismisLoadingDialog();
    }

    @Override
    public void onResponse(String response) {

    }

    public void setType(String type) {
        this.type = type;
        nextId = "0";
        loadingLayout.showLoadingView(recyclerView, "", true);
        getToDoListData("0");
    }

    int vantopDeletPosition;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("TAG_待办", "action=" + action);
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_WORK_REPORT://工作汇报点评
                    case PublishTask.PUBLISH_TASK_CONDUCT://任务处理
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT://日程处理
                    case PublishTask.PUBLISH_FLOW_CONDUCT://流程处理
                        nextId = "0";
                        loadingLayout.showLoadingView(recyclerView, "", true);
                        getToDoListData("0");
                        break;
                }
            } else if (CommonUtils.ACTION_APPROVAL_PROCESS.equals(action)) {
                if (tab_num == 0) {
                    vantopDeletPosition = intent.getIntExtra("position", -1);
                    if (vantopDeletPosition >= 0) {
                        List<TodoNotification> todoNotificationList = adapter.getData();
                        if (todoNotificationList != null && todoNotificationList.size() > 0) {
                            TodoNotification todoNotification = todoNotificationList.get(vantopDeletPosition);
                            deleteTodoVantop(todoNotification);
                        }
                    }
                }
            } else if (PUSH_TODO_MESSAGE.equals(action)||RECEIVER_PUSH.equals(action)) {
//                Map<String, Integer> integerMap = adapter.getLocalPushMessage();
//                String resId = intent.getStringExtra("id");
//                int c = 0;
//                if (integerMap.containsKey(resId)) {
//                    c = integerMap.get(resId);
//                }
//                integerMap.put(resId, ++c);
//                adapter.notifyDataSetChanged();
                showErrorLayout = false;
                page = 0;
                getToDoListData("0");
            }

        }
    };

    /**
     * 手动删除待办信息
     *
     * @param todoNotification
     */
    public void deleteTodoVantop(TodoNotification todoNotification) {

        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("res_id", todoNotification.res_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_TODO_INDEX_VANTOP_DELETE), params, getActivity());
        mNetworkManager.load(CALLBACK_DELETE_VANTOP, path, this, false);

    }


    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        List data = adapter.getData();
        if (data != null && data.size() > 0) {
            TodoNotification todoNotification = (TodoNotification) data.get(position);
            switch (view.getId()) {
                case R.id.ll_check://查看

                    NotificationUtils.itemClick(ToDoNotificationNewListFragment.this, todoNotification.type, todoNotification, position);
                    if ("n".equals(todoNotification.is_read)) {
                        todoNotification.is_read = "y";
                        try {
                            todoNotification.getJson().put("is_read", "y");
                            adapter.notifyItemChanged(position, R.id.is_read);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        readToDoNotification(todoNotification.todo_id);
                    }
                    break;
                case R.id.ll_Del: //删除
                    ViewParent parent = view.getParent();
                    if (parent instanceof SlideLayout) {
                        SlideLayout slItem = (SlideLayout) parent;
                        if (slItem.isOpen()) {
                            slItem.close();
                        }
                        if (todoNotification != null) {
                            deletePosition = position;
                            deleteToDoNotification(todoNotification.todo_id);

                        }
                    }


                    break;
            }
        }
    }

    @Override
    public void onLoadMoreRequested() {
        showErrorLayout = false;
        page++;
        getToDoListData(nextId);
        Log.e("TAG_加载更多", "onLoadMoreRequested");
    }

    @Override
    public void onRefresh() {
        showErrorLayout = false;
        page = 0;
        getToDoListData("0");
        Log.e("TAG_下拉刷新", "onRefresh");
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        List data = adapter.getData();
        if (data != null && data.size() > 0) {
            TodoNotification todoNotification = (TodoNotification) data.get(position);
            if ("n".equals(todoNotification.is_read)) {
                todoNotification.is_read = "y";
                try {
                    todoNotification.getJson().put("is_read", "y");
                    adapter.notifyItemChanged(position, R.id.is_read);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                readToDoNotification(todoNotification.todo_id);
            }

        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMsg event) {
        Log.e("TAG_Event","刷新数据===");

        if ("appQuesSurvey".equals(event.getActoin())) {
            mNetworkManager = getApplication().getNetworkManager();
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", PrfUtils.getUserId(getActivity()));
            params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
            params.put("res_id", event.getMessage());
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_TODO_INDEX_VANTOP_DELETE), params, getActivity());
            mNetworkManager.load(CALLBACK_DELETE_TOP_QUESSURVEY, path, this, false);
        }
    }
}
