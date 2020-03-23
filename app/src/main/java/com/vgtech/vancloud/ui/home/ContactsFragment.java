package com.vgtech.vancloud.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.inject.Inject;
import com.vgtech.common.api.Draft;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuCreator;
import com.vgtech.common.view.swipemenu.SwipeMenuItem;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.RecentContactAdapter;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.group.OrganizationActivity;
import com.vgtech.vancloud.ui.group.OrganizationVanCloudActivity;
import com.vgtech.vancloud.ui.module.contact.ChatGroupFragment;
import com.vgtech.vancloud.ui.module.me.WorkGroupActivity;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.SwipeMenuFactory;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.utils.AppModulePresenterVantop;

import java.util.List;

import roboguice.RoboGuice;

/**
 * 联系人
 * Created by Duke on 2016/9/2.
 */
public class ContactsFragment extends BaseFragment implements View.OnClickListener {

    private SwipeMenuListView mTree;
    private RecentContactAdapter mAdapter;
    private VancloudLoadingLayout loadingLayout;
    @Inject
    public Controller controller;
    private RecentContactsReceiver rc;
    private View mOrganizationLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!AppModulePresenterVantop.isOpenZuzhijiagou(this.getContext())) {
            if (mOrganizationLayout != null && mOrganizationLayout.getVisibility() != View.GONE) {
                mOrganizationLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected int initLayoutId() {
        return R.layout.contacts_layout;
    }

    @Override
    protected void initView(View view) {
        mTree = (SwipeMenuListView) view.findViewById(R.id.id_tree);
        mTree.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu,
                                           int index) {
                switch (index) {
                    case 0: {
                        User user = mAdapter.dataSource.remove(position);
                        user.accessTime = 0;
                        user.update(getActivity());
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                return false;
            }
        });
        // set creator
        mTree.setMenuCreator(SwipeMenuFactory.getSwipeMenuCreator(getActivity()));

        loadingLayout = (VancloudLoadingLayout) view.findViewById(R.id.loading);
        addHeader();
    }

    private void addHeader() {
        View mHeaderView = getActivity().getLayoutInflater().inflate(R.layout.contacts_header, null);
        mTree.addHeaderView(mHeaderView);
        mHeaderView.findViewById(R.id.rl_chat_group).setOnClickListener(this);
        mHeaderView.findViewById(R.id.rl_work_group).setOnClickListener(this);
        mHeaderView.findViewById(R.id.rl_organization).setOnClickListener(this);
        mOrganizationLayout = mHeaderView.findViewById(R.id.rl_organization);
    }

    @Override
    protected void initData() {
        mAdapter = new RecentContactAdapter(getActivity());
        mTree.setAdapter(mAdapter);
        loadData();
    }

    private boolean registerState;

    @Override
    protected void initEvent() {
        if (!registerState) {
            rc = new RecentContactsReceiver();
            getActivity().registerReceiver(rc, new IntentFilter(Actions.ACTION_RECENTCONTACTS_REFRESH));
            registerState = true;
        }
    }

    public void loadData() {
        loadingLayout.showLoadingView(mTree, "", true);
        AsyncTask asyncTask = new AsyncTask<Void, Void, List<User>>() {
            @Override
            protected List<User> doInBackground(Void... params) {
                try {
                    if (getActivity() == null || getActivity().isFinishing())
                        return null;
                    List<User> users = User.queryAccessUserExceptLoginer(getActivity());
                    return users;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<User> users) {

                loadingLayout.dismiss(mTree);
                mTree.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0 && mAdapter.dataSource.size() > 0) {
                            User user = mAdapter.dataSource.get(position - 1);
                            UserUtils.enterUserInfo(getActivity(), user.userId, user.getName(), user.photo);
                        }
                    }
                });

                if (users != null){
                    mAdapter.dataSource.clear();
                    mAdapter.dataSource.addAll(users);
                    mAdapter.notifyDataSetChanged();
                    if (users.size() == 0){
                        loadingLayout.showEmptyView(mTree, getString(R.string.no_recent_contact), true, true);
                        mTree.setVisibility(View.VISIBLE);
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_chat_group: {
                controller.pushFragment(new ChatGroupFragment());
            }
            break;
            case R.id.rl_work_group: {
                Intent intent = new Intent(getActivity(), WorkGroupActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.rl_organization: {
                if (TenantPresenter.isVanTop(getActivity())) {
                    startActivity(new Intent(getActivity(), OrganizationActivity.class));
                } else {
                    startActivity(new Intent(getActivity(), OrganizationVanCloudActivity.class));
                }
            }
            break;
        }
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(rc);
    }

    //更新最近联系人的广播
    class RecentContactsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData();
        }
    }

}
