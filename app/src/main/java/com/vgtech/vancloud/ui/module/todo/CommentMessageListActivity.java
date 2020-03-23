package com.vgtech.vancloud.ui.module.todo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.CommentMessageAdapter;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.ui.view.pullswipemenulistview.PullToRefreshSwipeMenuListView;
import com.vgtech.vancloud.utils.NotificationUtils;
import com.vgtech.vancloud.utils.SwipeMenuFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息提醒评论我的列表
 * Created by Duke on 2016/9/23.
 */

public class CommentMessageListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    PullToRefreshSwipeMenuListView listView;
    public VancloudLoadingLayout loadingLayout;
    private CommentMessageAdapter adapter;

    @Override
    protected int getContentView() {
        return R.layout.handle_list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrfUtils.setMessageCountCount(this, PrfUtils.MESSAGE_COMMENT, 0);
        initView();
        getData();
    }

    public void initView() {

        findViewById(R.id.top_layout).setVisibility(View.VISIBLE);
        setTitle(getString(R.string.message_comment));
        listView = (PullToRefreshSwipeMenuListView) findViewById(R.id.pull_list);
        listView.setMode(PullToRefreshBase.Mode.DISABLED);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        adapter = new CommentMessageAdapter(this, new ArrayList<MessageDB>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
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
                                messageDB.deleteThis(CommentMessageListActivity.this);
                                return null;
                            }
                        }.execute();
                        adapter.deleteItem(position);
                        if (adapter.getList().size() <= 0) {
                            loadingLayout.showEmptyView(listView, getString(R.string.no_list_data), true, true);
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

    public void getData() {
        new AsyncTask<Void, Void, List<MessageDB>>() {

            @Override
            protected void onPreExecute() {
                loadingLayout.showLoadingView(listView, "", true);
            }

            @Override
            protected List<MessageDB> doInBackground(Void... params) {
                Log.e("TAG_评论","doInBackground");
                return MessageDB.queryAllCommonMessage(CommentMessageListActivity.this);
            }

            @Override
            protected void onPostExecute(List<MessageDB> messageDBs) {
                Log.e("TAG_评论","onPostExecute");
                loadingLayout.dismiss(listView);
                adapter.myNotifyDataSetChanged(messageDBs);

                if (messageDBs.size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_list_data), true, true);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final MessageDB messageDB = adapter.getList().get(position - listView.getRefreshableView().getHeaderViewsCount());
        if (0 == messageDB.messageState) {
            adapter.chaneIsRead(position - listView.getRefreshableView().getHeaderViewsCount());

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    messageDB.changeReadState(CommentMessageListActivity.this);
                    return null;
                }
            }.execute();
        }
        PushMessage pushMessage = null;
        try {
            pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO 跳转

        if (TypeUtils.APPROVAL.equals(pushMessage.msgTypeId)) {

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

        } else {
            NotificationUtils.commentItemClick(CommentMessageListActivity.this, messageDB.type, pushMessage.resId);
        }
    }
}
