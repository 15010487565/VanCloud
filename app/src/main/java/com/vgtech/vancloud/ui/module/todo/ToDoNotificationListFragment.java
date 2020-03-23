package com.vgtech.vancloud.ui.module.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
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
import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.TodoNotification;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.ToDoNotificationAdapter;
import com.vgtech.vancloud.ui.base.LazyLoadFragment;
import com.vgtech.vancloud.ui.view.pullswipemenulistview.PullToRefreshSwipeMenuListView;
import com.vgtech.vancloud.utils.NotificationUtils;
import com.vgtech.vancloud.utils.SwipeMenuFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 待办
 * Created by Duke on 2016/9/20.
 */

public class ToDoNotificationListFragment extends LazyLoadFragment implements HttpListener<String>, PullToRefreshBase.OnRefreshListener2 {

    public static final String PUSH_TODO_MESSAGE = "PUSH_TODO_MESSAGE";
    private static String STATUS = "status";
    private static String TYPE = "type";
    private static String NUM = "num";
    private NetworkManager mNetworkManager;
    private final int CALLBACK_TODOLIST = 1;
    private final int CALLBACK_DELETE = 2;
    private final int CALLBACK_DELETE_VANTOP = 4;

    private String n = "12";
    private String nextId = "0";
    private String status;


    private String type;
    private int tab_num;

    PullToRefreshSwipeMenuListView listView;
    private VancloudLoadingLayout loadingLayout;
    private ToDoNotificationAdapter adapter;

    private int deletePosition = -1;//记录删除标记

    boolean backRefresh = false;

    private boolean showErrorLayout = true;


    public static ToDoNotificationListFragment create(String status, String type, int tabNum) {
        ToDoNotificationListFragment fragment = new ToDoNotificationListFragment();
        Bundle args = new Bundle();
        args.putString(STATUS, status);
        args.putString(TYPE, type);
        args.putInt(NUM, tabNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        backRefresh = false;
        status = getArguments().getString(STATUS);
        type = getArguments().getString(TYPE);
        tab_num = getArguments().getInt(NUM, -1);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    protected int initLayoutId() {
        return R.layout.handle_list_layout;
    }

    @Override
    protected void initView(View view) {

        listView = (PullToRefreshSwipeMenuListView) view.findViewById(R.id.pull_list);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadingLayout.showLoadingView(listView, "", true);
                getToDoListData("0");
            }
        });
        adapter = new ToDoNotificationAdapter(getActivity(), new ArrayList<TodoNotification>());
        loadLocalPushDatas();
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TodoNotification todoNotification = adapter.getList().get(position - listView.getRefreshableView().getHeaderViewsCount());
//                NotificationUtils.itemClick(ToDoNotificationListFragment.this, todoNotification.type, todoNotification.res_id, todoNotification.create_user_no, position - listView.getRefreshableView().getHeaderViewsCount());
                NotificationUtils.itemClick(ToDoNotificationListFragment.this, todoNotification.type, todoNotification,position - listView.getRefreshableView().getHeaderViewsCount());
                if ("n".equals(todoNotification.is_read)) {
                    adapter.chaneIsRead(position - listView.getRefreshableView().getHeaderViewsCount());
                    readToDoNotification(todoNotification.todo_id);
                }
            }
        });

        listView.getRefreshableView().setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0: {
                        TodoNotification todoNotification = null;
                        List<TodoNotification> todoNotifications = adapter.getList();
                        if (todoNotifications.size() > 0) {
                            todoNotification = todoNotifications.get(position);
                        }
                        if (todoNotification != null) {
                            deletePosition = position;
                            deleteToDoNotification(todoNotification.todo_id);
                        }
                    }
                    break;
                }
                return false;
            }
        });

        listView.getRefreshableView().setMenuCreator(SwipeMenuFactory.getSwipeMenuCreator(getActivity()));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        intentFilter.addAction(CommonUtils.ACTION_APPROVAL_PROCESS);
        intentFilter.addAction(PUSH_TODO_MESSAGE);
        getActivity().registerReceiver(mReceiver, intentFilter);
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
            loadingLayout.showLoadingView(listView, "", true);
            getToDoListData("0");
        }
    }

    @Override
    protected void lazyLoad() {
        if (tab_num > 0) {
            loadingLayout.showLoadingView(listView, "", true);
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
    }

    @Override
    public void dataLoaded(int callbackId, final NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        loadingLayout.dismiss(listView);
        listView.onRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == CALLBACK_TODOLIST) {

                if (showErrorLayout)
                    loadingLayout.showErrorView(listView, "", true, true);
                else {
                    showErrorLayout = true;
                    if (adapter.getList().size() <= 0) {
                        loadingLayout.showErrorView(listView, "", true, true);
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
                String oldeNextId = path.getPostValues().get("s");
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    String id = jsonObject.getString("nextid");
                    if (!TextUtils.isEmpty(id)) {
                        if (!"0".equals(id)) {
                            nextId = id;
                            if (listView.getMode() == PullToRefreshBase.Mode.PULL_FROM_START) {
                                listView.onNewRefreshComplete();
                                listView.setMode(PullToRefreshBase.Mode.BOTH);
                            }
                        } else {
                            listView.onNewRefreshComplete();
                            listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        }
                    }
                    todoNotifications = JsonDataFactory.getDataArray(TodoNotification.class, jsonObject.getJSONArray("rows"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter == null) {
                    adapter = new ToDoNotificationAdapter(getActivity(), todoNotifications);
                    listView.setAdapter(adapter);
                } else {
                    if ("0".equals(oldeNextId)) {
                        adapter.myNotifyDataSetChanged(todoNotifications);
                    } else {
                        List<TodoNotification> list = adapter.getList();
                        list.addAll(todoNotifications);
                        adapter.myNotifyDataSetChanged(list);
                    }
                }
                if (adapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_information_todo), true, true);
                    listView.setVisibility(View.VISIBLE);
                }
                break;

            case CALLBACK_DELETE:
                Toast.makeText(getActivity(), getString(R.string.del_user_sul), Toast.LENGTH_SHORT).show();
                if (deletePosition >= 0) {
                    adapter.deleteItem(deletePosition);
                    deletePosition = -1;
                    if (adapter.getList().size() <= 0) {
                        loadingLayout.showEmptyView(listView, getString(R.string.no_information_todo), true, true);
                        listView.setVisibility(View.VISIBLE);
                    }
                }

                if ("pending".equals(status)) {
                    backRefresh = true;
                }
                break;
            case CALLBACK_DELETE_VANTOP:
                if (vantopDeletPosition >= 0) {
                    adapter.deleteItem(vantopDeletPosition);
                    vantopDeletPosition = -1;
                    if (adapter.getList().size() <= 0) {
                        loadingLayout.showEmptyView(listView, getString(R.string.no_information_todo), true, true);
                        listView.setVisibility(View.VISIBLE);
                    }
                }

                backRefresh = true;

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
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {

        showErrorLayout = false;
        getToDoListData("0");

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        showErrorLayout = false;
        getToDoListData(nextId);
    }

    public void setType(String type) {
        this.type = type;
        nextId = "0";
        loadingLayout.showLoadingView(listView, "", true);
        getToDoListData("0");
    }

    int vantopDeletPosition;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_WORK_REPORT://工作汇报点评
                    case PublishTask.PUBLISH_TASK_CONDUCT://任务处理
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT://日程处理
                    case PublishTask.PUBLISH_FLOW_CONDUCT://流程处理
                        nextId = "0";
                        loadingLayout.showLoadingView(listView, "", true);
                        getToDoListData("0");
                        break;
                }
            } else if (CommonUtils.ACTION_APPROVAL_PROCESS.equals(action)) {
                if (tab_num == 0) {
                    vantopDeletPosition = intent.getIntExtra("position", -1);
                    if (vantopDeletPosition >= 0) {
                        List<TodoNotification> todoNotificationList = adapter.getList();
                        if (todoNotificationList !=null && todoNotificationList.size() > 0){
                            TodoNotification todoNotification = todoNotificationList.get(vantopDeletPosition);
                            deleteTodoVantop(todoNotification);
                        }
                    }
                }
            } else if (PUSH_TODO_MESSAGE.equals(action)) {
                Map<String, Integer> integerMap = adapter.getLocalPushMessage();
                String resId = intent.getStringExtra("id");
                int c = 0;
                if (integerMap.containsKey(resId)) {
                    c = integerMap.get(resId);
                }
                integerMap.put(resId, ++c);
                adapter.notifyDataSetChanged();
            }
        }
    };

    public boolean isBackRefresh() {
        return backRefresh;
    }

    public void setBackRefresh(boolean backRefresh) {
        this.backRefresh = backRefresh;
    }

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
}
