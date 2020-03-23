package com.vgtech.vancloud.ui.module.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

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
import com.vgtech.common.utils.CommonUtils;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.NotificationExtension;
import com.vgtech.vancloud.reciver.GetuiGTIntentService;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.NoticeCenterAdapter;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.ui.view.pullswipemenulistview.PullToRefreshSwipeMenuListView;
import com.vgtech.vancloud.utils.NoticeUtils;
import com.vgtech.vancloud.utils.NotificationUtils;
import com.vgtech.vancloud.utils.SwipeMenuFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 消息提醒列表
 * Created by Duke on 2016/9/6.
 */
public class MessageListActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    PullToRefreshSwipeMenuListView listView;
    private VancloudLoadingLayout loadingLayout;
    private NoticeCenterAdapter adapter;
    private boolean backRefresh = false;
    private View mDelActionView;

    @Override
    protected int getContentView() {
        return R.layout.activity_message_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrfUtils.setMessageCountCount(this, PrfUtils.MESSAGE_MSG, 0);
        NoticeUtils.updateAppNum(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommonUtils.ACTION_APPROVAL_PROCESS);
        intentFilter.addAction(GetuiGTIntentService.RECEIVER_PUSH);
        registerReceiver(mReceiver, intentFilter);
        initView();
        loadMessageData();
        updateCommentView();
    }

    @Override
    public void finish() {
        super.finish();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CommonUtils.ACTION_APPROVAL_PROCESS.equals(action)) {
                int vantopDeletPosition = intent.getIntExtra("position", -1);
                if (vantopDeletPosition >= 0) {
                    MessageDB todoNotification = adapter.getList().get(vantopDeletPosition);
                    deleteTodoVantop(todoNotification);
                }
            } else if (GetuiGTIntentService.RECEIVER_PUSH.equals(action)) {
                loadMessageData();
                updateCommentView();
            }
        }
    };

    /**
     * 手动删除待办信息
     */
    public void deleteTodoVantop(MessageDB messageDB) {

        PushMessage pushMessage = null;
        try {
            pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetworkManager mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("res_id", pushMessage.resId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_TODO_INDEX_VANTOP_DELETE), params, this);
        mNetworkManager.load(1, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {

            }
        });

    }

    private TextView mTvSelectAll;
    private TextView mTvDelete;
    private TextView mTvCancel;

    public void initView() {
        setTitle(getString(R.string.message_alert));
        mDelActionView = findViewById(R.id.del_action);
        mTvSelectAll = (TextView) findViewById(R.id.btn_select_all);
        mTvSelectAll.setOnClickListener(this);
        mTvDelete = (TextView) findViewById(R.id.btn_delete);
        mTvDelete.setOnClickListener(this);
        mTvCancel = (TextView) findViewById(R.id.btn_cancel);
        mTvCancel.setOnClickListener(this);
        findViewById(R.id.btn_comment).setOnClickListener(this);
        listView = (PullToRefreshSwipeMenuListView) findViewById(R.id.pull_list);
        listView.setMode(PullToRefreshBase.Mode.DISABLED);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        adapter = new NoticeCenterAdapter(this, new ArrayList<MessageDB>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        // step 2. listener item click event
        listView.getRefreshableView().setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu,
                                           int index) {
                switch (index) {
                    case 0: {
                        final MessageDB messageDB = adapter.getList().get(position);
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                messageDB.deleteThis(MessageListActivity.this);
                                return null;
                            }
                        }.execute();
                        backRefresh = true;
                        adapter.deleteItem(position);
                        if (adapter.getList().size() <= 0) {
                            loadingLayout.showEmptyView(listView, getString(R.string.no_notice_info), true, true);
                            listView.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                }
                return false;
            }
        });
        // set creator
        listView.getRefreshableView().setMenuCreator(SwipeMenuFactory.getSwipeMenuCreator(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCommentView();
    }

    private void updateCommentView() {
        final View message_comment_new = findViewById(R.id.message_comment_new);
        int count = PrfUtils.getMessageCount(this, PrfUtils.MESSAGE_COMMENT);
        message_comment_new.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
    }

    public void loadMessageData() {
        new AsyncTask<Void, Void, List<MessageDB>>() {
            @Override
            protected void onPreExecute() {
//                Log.e("TAG_消息列表","onPreExecute");
                loadingLayout.showLoadingView(listView, "", true);
            }

            @Override
            protected List<MessageDB> doInBackground(Void... params) {
                try {
//                    Log.e("TAG_消息列表","doInBackground");
                    ArrayList<MessageDB> messageDBS = MessageDB.queryAllMessage(MessageListActivity.this);
                    return messageDBS;
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(1);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<MessageDB> messageDBs) {
//                Log.e("TAG_消息列表","onPostExecute");
                loadingLayout.dismiss(listView);
//                Log.e("TAG_消息列表","messageDBs="+messageDBs.toString());
                adapter.myNotifyDataSetChanged(messageDBs);
                if (messageDBs.size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_notice_info), true, true);
                    listView.setVisibility(View.VISIBLE);
                }
            }

        }.executeOnExecutor(Executors.newCachedThreadPool());
        updateCommentView();
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1 :{
                    loadingLayout.dismiss(listView);
                    break;
                }
            }
        }
    };
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final MessageDB messageDB = adapter.getList().get(position - listView.getRefreshableView().getHeaderViewsCount());
//        Log.e("TAG_消息列表","adapter.isSelected()="+adapter.isSelected());
        if (adapter.isSelected()) {
            if (adapter.getSelectedData().contains(messageDB)) {
                adapter.getSelectedData().remove(messageDB);
                mTvSelectAll.setText(R.string.totle_choose);
            } else {
                adapter.getSelectedData().add(messageDB);
                if (adapter.getSelectedData().containsAll(adapter.getList())) {
                    mTvSelectAll.setText(R.string.totle_choose_cancle);
                }
            }
            if (adapter.getSelectedData().size() > 0) {
                mTvDelete.setText(getString(R.string.delete) + "(" + adapter.getSelectedData().size() + ")");
                mTvDelete.setEnabled(true);
                mTvDelete.setTextColor(getResources().getColor(R.color.bg_title));
            } else {
                mTvDelete.setText(getString(R.string.delete));
                mTvDelete.setEnabled(false);
                mTvDelete.setTextColor(Color.parseColor("#636363"));
            }
            adapter.notifyDataSetChanged();
            return;
        }
//        Log.e("TAG_消息列表","messageDB.messageState="+messageDB.messageState);
        if (0 == messageDB.messageState) {
            adapter.chaneIsRead(position - listView.getRefreshableView().getHeaderViewsCount());

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Looper.prepare();
                    messageDB.changeReadState(MessageListActivity.this);
                    Looper.loop();
                    return null;
                }
            }.execute();
            backRefresh = true;
        }
        PushMessage pushMessage = null;
        NotificationExtension notificationExtension = null;
        try {
            pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
            String extension = pushMessage.getJson().getString("extension");
            if (!TextUtils.isEmpty(extension)) {
                notificationExtension = JsonDataFactory.getData(NotificationExtension.class, new JSONObject(extension));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("chexiao".equals(pushMessage.operationType)) {
            return;
        } else if (TypeUtils.VANTOPLEAVE.equals(pushMessage.msgTypeId) || TypeUtils.VANTOPSIGNCARD.equals(pushMessage.msgTypeId) || TypeUtils.VANTOPOVERTIME.equals(pushMessage.msgTypeId)) {
            NotificationUtils.vantopItemClick(this, pushMessage, notificationExtension, position - listView.getRefreshableView().getHeaderViewsCount());
        } else if (TypeUtils.MEETING.equals(pushMessage.msgTypeId)) {
            long noticeID = messageDB.insert(MessageListActivity.this);
            Intent intent = new Intent();
            intent.setAction("com.vgtech.meeting.detail");
            intent.putExtra("id", pushMessage.resId);
            intent.putExtra("noticeid", noticeID);
            startActivity(intent);
        } else if (TypeUtils.APPROVAL.equals(pushMessage.msgTypeId)) {

            Intent intent = new Intent(this, FlowHandleActivity.class);
            intent.putExtra("processid", pushMessage.resId);
            int type = -1;
            if ("cc".equals(pushMessage.role)) {
                type = 3;
            } else if ("process".equals(pushMessage.role)) {
                type = 2;
            } else {
                type = 1;
            }
            intent.putExtra("typer", type);
            startActivityForResult(intent, 1);

        } else if (TypeUtils.NEWEMPLOYEE.equals(pushMessage.msgTypeId)) {
            if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
                UserUtils.enterUserInfo(this, notificationExtension.content, "", "");
            }
        } else
            NotificationUtils.itemClick(MessageListActivity.this, messageDB.type, pushMessage.resId);
//        if (pushMessage != null && !TextUtils.isEmpty(pushMessage.resId))
//            NotificationUtils.itemClick(MessageListActivity.this, messageDB.type, pushMessage.resId);
    }

    @Override
    public void onBackPressed() {
        if (backRefresh) {
            setResult(RESULT_OK);
            backRefresh = false;
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_all: {
                if (adapter.getSelectedData().containsAll(adapter.getList())) {
                    adapter.getSelectedData().clear();
                    mTvSelectAll.setText(R.string.totle_choose);
                } else {
                    adapter.getSelectedData().clear();
                    adapter.getSelectedData().addAll(adapter.getList());
                    mTvSelectAll.setText(R.string.totle_choose_cancle);
                }
                if (adapter.getSelectedData().size() > 0) {
                    mTvDelete.setText(getString(R.string.delete) + "(" + adapter.getSelectedData().size() + ")");
                    mTvDelete.setEnabled(true);
                    mTvDelete.setTextColor(getResources().getColor(R.color.bg_title));
                } else {
                    mTvDelete.setText(getString(R.string.delete));
                    mTvDelete.setEnabled(false);
                    mTvDelete.setTextColor(Color.parseColor("#636363"));
                }
                adapter.notifyDataSetChanged();
            }
            break;
            case R.id.btn_delete: {

                new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                        .setMsg(getString(R.string.clear_message_confirm))
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<MessageDB> messageDBs = new ArrayList<>();
                                messageDBs.addAll(adapter.getSelectedData());
                                for (final MessageDB messageDB : messageDBs) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            messageDB.deleteThis(MessageListActivity.this);
                                            return null;
                                        }
                                    }.execute();
                                    backRefresh = true;
                                    adapter.getList().remove(messageDB);
                                    adapter.notifyDataSetChanged();
                                }
                                adapter.getSelectedData().clear();
                                if (adapter.getList().size() <= 0) {
                                    loadingLayout.showEmptyView(listView, getString(R.string.no_notice_info), true, true);
                                    listView.setVisibility(View.VISIBLE);
                                }
                                mTvDelete.setText(getString(R.string.delete));
                                mTvDelete.setEnabled(false);
                                mTvDelete.setTextColor(Color.parseColor("#636363"));
                                adapter.setSelected(false);
                                mDelActionView.setVisibility(View.GONE);
                            }
                        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
            break;
            case R.id.btn_cancel:
                adapter.getSelectedData().clear();
                mTvDelete.setText(getString(R.string.delete));
                mTvDelete.setEnabled(false);
                mTvDelete.setTextColor(Color.parseColor("#636363"));
                adapter.setSelected(false);
                mDelActionView.setVisibility(View.GONE);
                break;
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_comment:
                Intent intent = new Intent(MessageListActivity.this, CommentMessageListActivity.class);
                startActivity(intent);
                break;
            default:
                super.onClick(v);
                break;
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (adapter.isSelected())
            return false;
        else {
            adapter.setSelected(true);
            mDelActionView.setVisibility(View.VISIBLE);
        }
        return false;
    }
}
