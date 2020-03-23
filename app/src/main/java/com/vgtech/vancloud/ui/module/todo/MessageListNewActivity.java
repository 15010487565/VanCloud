package com.vgtech.vancloud.ui.module.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

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
import com.vgtech.common.utils.CommonUtils;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.NotificationExtension;
import com.vgtech.vancloud.reciver.GetuiGTIntentService;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.NoticeCenterNewAdapter;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.utils.NoticeUtils;
import com.vgtech.vancloud.utils.NotificationUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息提醒列表
 * Created by Duke on 2016/9/6.
 */
public class MessageListNewActivity extends BaseActivity implements
        BaseQuickAdapter.OnItemClickListener,
        BaseQuickAdapter.OnItemChildClickListener {

    private VancloudLoadingLayout loadingLayout;
    private RecyclerView recyclerView;

    private NoticeCenterNewAdapter adapter;

//    private boolean backRefresh = false;
    private View mDelActionView;

    @Override
    protected int getContentView() {
        return R.layout.activity_message_list_new;
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
//        updateCommentView();
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
                    MessageDB todoNotification = adapter.getData().get(vantopDeletPosition);
                    deleteTodoVantop(todoNotification);
                }
            } else if (GetuiGTIntentService.RECEIVER_PUSH.equals(action)) {
                loadMessageData();
//                updateCommentView();
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
//        findViewById(R.id.btn_comment).setOnClickListener(this);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<MessageDB> todoNotifications = new ArrayList<>();
        adapter = new NoticeCenterNewAdapter(todoNotifications);
        adapter.setOnItemChildClickListener(this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(getRecyclerViewDivider(R.drawable.inset_recyclerview_divider));

        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        updateCommentView();
//    }

//    private void updateCommentView() {
//        final View message_comment_new = findViewById(R.id.message_comment_new);
//        int count = PrfUtils.getMessageCount(this, PrfUtils.MESSAGE_COMMENT);
//        message_comment_new.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
//    }

    public void loadMessageData() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("TAG_消息列表","loadMessageData");
                        ArrayList<MessageDB> messageDBS = MessageDB.queryAllMessage(MessageListNewActivity.this);
                        adapter.setNewData(messageDBS);
                        if (messageDBS.size() <= 0) {
                            loadingLayout.showEmptyView(recyclerView, getString(R.string.no_notice_info), true, true);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        }.start();

//        updateCommentView();
    }

//    @Override
//    public void onBackPressed() {
//        if (backRefresh) {
//            setResult(RESULT_OK);
//            backRefresh = false;
//        }
//        finish();
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_back:
                onBackPressed();
                break;
//            case R.id.btn_comment:
//                Intent intent = new Intent(MessageListNewActivity.this, CommentMessageListActivity.class);
//                startActivity(intent);
//                break;
            default:
                super.onClick(v);
                break;
        }
    }

//    @Override
//    public void onRefresh() {
//        loadMessageData();
//        updateCommentView();
//    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        List data = adapter.getData();
        if (data != null && data.size() > 0) {
            final MessageDB messageDB = (MessageDB) adapter.getData().get(position);
            switch (view.getId()) {
                case R.id.ll_check://查看

                    Log.e("TAG_消息列表","onItemChildClick="+messageDB.messageState);
                    if (0 == messageDB.messageState) {
                        messageDB.messageState = 1;
                        adapter.notifyItemChanged(position, R.id.is_read);
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                Log.e("TAG_消息列表new","deleteThis=====doInBackground");
                                Looper.prepare();
                                messageDB.changeReadState(MessageListNewActivity.this);
                                Looper.loop();
                            }
                        }.start();
//                        backRefresh = true;
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
                        NotificationUtils.vantopItemClick(this, pushMessage, notificationExtension, position );
                    } else if (TypeUtils.MEETING.equals(pushMessage.msgTypeId)) {
                        long noticeID = messageDB.insert(MessageListNewActivity.this);
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
                        NotificationUtils.itemClick(MessageListNewActivity.this, messageDB.type, pushMessage.resId,pushMessage.operationType);

                    break;
                case R.id.ll_Del: //删除
                    ViewParent parent = view.getParent();
                    if (parent instanceof SlideLayout){
                        SlideLayout slItem = (SlideLayout) parent;
                        if (slItem.isOpen()) {
                            slItem.close();
                        }
                        if (messageDB != null) {

                            adapter.remove(position);

                        }
                    }
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            Log.e("TAG_消息列表new","deleteThis=====doInBackground");
                            Looper.prepare();
                            messageDB.deleteThis(MessageListNewActivity.this);
                            Looper.loop();
                        }
                    }.start();

                    if (adapter.getData().size() <= 0) {
                        loadingLayout.showEmptyView(recyclerView, getString(R.string.no_notice_info), true, true);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    break;
            }
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        try {
            final MessageDB messageDB = (MessageDB) adapter.getData().get(position);
            Log.e("TAG_消息列表new","onItemClick="+messageDB.messageState);
            if (0 == messageDB.messageState) {
                messageDB.messageState = 1;
                adapter.notifyItemChanged(position, R.id.is_read);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Log.e("TAG_消息列表new","deleteThis=====doInBackground");
                        Looper.prepare();
                        messageDB.changeReadState(MessageListNewActivity.this);
                        Looper.loop();
                    }
                }.start();
//                        backRefresh = true;
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
}
