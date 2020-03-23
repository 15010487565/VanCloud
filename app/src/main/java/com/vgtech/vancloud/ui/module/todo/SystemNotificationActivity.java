package com.vgtech.vancloud.ui.module.todo;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.d.lib.slidelayout.SlideLayout;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.NotificationExtension;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.NoticeCenterNewAdapter;
import com.vgtech.vancloud.ui.web.WebActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2016/9/7.
 */
public class SystemNotificationActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {


    private RecyclerView recyclerView;

    public VancloudLoadingLayout loadingLayout;
    private NoticeCenterNewAdapter adapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_system_notificon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getData();
    }

    public void initView() {

        findViewById(R.id.top_layout).setVisibility(View.VISIBLE);
        setTitle(getString(R.string.system_notify));

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<MessageDB> todoNotifications = new ArrayList<>();
        adapter = new NoticeCenterNewAdapter(todoNotifications);
        adapter.setOnItemChildClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(getRecyclerViewDivider(R.drawable.inset_recyclerview_divider));

        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);

    }

    public void getData() {
        new AsyncTask<Void, Void, List<MessageDB>>() {

            @Override
            protected void onPreExecute() {
                loadingLayout.showLoadingView(recyclerView, "", true);
            }

            @Override
            protected List<MessageDB> doInBackground(Void... params) {
                return MessageDB.queryAllMessageByTypeId(SystemNotificationActivity.this, TypeUtils.SYSTEMNOTIFICATION);
            }

            @Override
            protected void onPostExecute(List<MessageDB> messageDBs) {
                loadingLayout.dismiss(recyclerView);
                adapter.setNewData(messageDBs);

                if (messageDBs.size() <= 0) {
                    loadingLayout.showEmptyView(recyclerView, getString(R.string.no_system_notify), true, true);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }


    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        List data = adapter.getData();
        if (data != null && data.size() > 0) {
            final MessageDB messageDB = (MessageDB) adapter.getData().get(position);
            switch (view.getId()) {
                case R.id.ll_check://查看

                    if (0 == messageDB.messageState) {
                        messageDB.messageState = 1;
                        adapter.notifyItemChanged(position, R.id.is_read);
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                messageDB.changeReadState(SystemNotificationActivity.this);
                                return null;
                            }
                        }.execute();
                    }
                    PushMessage pushMessage = null;
                    NotificationExtension extension = null;
                    try {
                        pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
                        extension = JsonDataFactory.getData(NotificationExtension.class, new JSONObject(pushMessage.getJson().getString("extension")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //TODO 跳转
                    Intent intent = new Intent(SystemNotificationActivity.this, WebActivity.class);
                    intent.putExtra("title", pushMessage.content);
                    String url = extension.content;
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
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

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            messageDB.deleteThis(SystemNotificationActivity.this);
                            return null;
                        }
                    }.execute();

                    if (adapter.getData().size() <= 0) {
                        loadingLayout.showEmptyView(recyclerView, getString(R.string.no_notice_info), true, true);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    break;
            }
        }
    }
}
