package com.vgtech.vancloud.ui.module.me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.vgtech.common.api.Draft;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuCreator;
import com.vgtech.common.view.swipemenu.SwipeMenuItem;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.utils.SwipeMenuFactory;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/9/7.
 */
public class DraftActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ApiDataAdapter<Draft> mDraftAdapter;
    private VancloudLoadingLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.lable_draft));
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.default_layout);
        SwipeMenuListView listView = (SwipeMenuListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu,
                                           int index) {
                switch (index) {
                    case 0: {
                        Draft draft = mDraftAdapter.remove(position);
                        PublishTask task = (PublishTask) draft.obj;
                        task.delete(getApplicationContext());
                        Intent intent = new Intent(MainActivity.RECEIVER_DRAFT);
                        sendBroadcast(intent);
                    }
                    break;
                }
                return false;
            }
        });
        // set creator
        listView.setMenuCreator(SwipeMenuFactory.getSwipeMenuCreator(this));
        mDraftAdapter = new ApiDataAdapter<Draft>(this);
        listView.setAdapter(mDraftAdapter);
        refreshData();
        registerDraftReceiver();
    }

    private void registerDraftReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        registerReceiver(mReceiver, intentFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                refreshData();
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        getAppliction().release();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private void refreshData() {
        List<PublishTask> taskList = PublishTask.queryTask(this);
        List<Draft> drafts = new ArrayList<Draft>();
        for (PublishTask t : taskList) {
            Draft draft = new Draft();
            draft.obj = t;
            drafts.add(draft);
        }
        if (drafts != null && drafts.size() > 0) {
            loadingLayout.dismiss(findViewById(android.R.id.list));
            mDraftAdapter.clear();
            mDraftAdapter.add(drafts);
        } else {
            loadingLayout.showEmptyView(findViewById(android.R.id.list), getString(R.string.no_list_data), true, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected int getContentView() {
        return R.layout.draft_list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof Draft) {
            Draft draft = (Draft) obj;
            if (draft.obj instanceof PublishTask) {
//                if (!TenantPresenter.isVanTop(this)) {
                PublishTask task = (PublishTask) draft.obj;
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra("publishTask", task);
                startActivity(intent);
//                }
            }
        }
    }
}
