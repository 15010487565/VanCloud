package com.vgtech.vancloud.ui.module;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.ReciverUserAdapter;
import com.vgtech.common.utils.UserUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新版抄送人列表界面
 * Created by Duke on 2016/10/20.
 */

public class ReciverUserListActivity extends BaseActivity implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener, HttpListener {

    private static final int CALLBACK_LIST = 1;
    private PullToRefreshListView listView;
    private ReciverUserAdapter adapter;
    private VancloudLoadingLayout loadingLayout;
    private boolean mHasData;
    private String mNextId;
    private String biz_id;
    private int type;

    @Override
    protected int getContentView() {
        return R.layout.reciver_userlist_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.recruit_detail_copyer));
        String json = getIntent().getStringExtra("json");
        biz_id = getIntent().getStringExtra("biz_id");
        type = getIntent().getIntExtra("type", 0);
        listView = (PullToRefreshListView) findViewById(R.id.listview);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject receiverObject = new JSONObject(json);
                mNextId = receiverObject.getString("nextid");
                mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                List<NewUser> recivers = JsonDataFactory.getDataArray(NewUser.class, receiverObject.getJSONArray("rows"));
                adapter = new ReciverUserAdapter(recivers, this);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
                load(mNextId, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mNextId = "0";
                load(mNextId, false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (mHasData) {
                    load(mNextId, false);
                } else {
                    loadingLayout.dismiss(listView);
                }
            }
        });
    }

    private String mLastId;

    private void load(String mNextId, boolean show) {
        if (!mNextId.equals("0") && mNextId.equals(mLastId))
            return;
        if (show)
            loadingLayout.showLoadingView(listView, "", true);
        getApplicationProxy().getNetworkManager().cancle(this);
        String url = ApiUtils.generatorUrl(this, URLAddr.URL_CC_LIST);
        Map<String, String> postValues = new HashMap<>();
        postValues.put("user_id", PrfUtils.getUserId(this));
        postValues.put("tenant_id", PrfUtils.getTenantId(this));
        postValues.put("biz_id", biz_id);
        postValues.put("type", String.valueOf(type));
        postValues.put("n", String.valueOf(12));
        postValues.put("s", mNextId);
        mLastId = mNextId;
        NetworkPath path = new NetworkPath(url, postValues, this);
        getApplicationProxy().getNetworkManager().load(CALLBACK_LIST, path, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewUser user = adapter.getList().get(position - listView.getRefreshableView().getHeaderViewsCount());
        UserUtils.enterUserInfo(this, user.userid + "", user.name, user.photo);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listView);
        listView.onNewRefreshComplete();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (adapter.getCount() == 0) {
                loadingLayout.showErrorView(listView);
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_LIST:
                try {
                    String lastNextId = path.getPostValues().get("s");
                    if ("0".equals(lastNextId))
                        adapter.clear();
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    JSONArray jsonArray = jsonObject.getJSONArray("rows");
                    mNextId = jsonObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(mNextId) && !"0".equals(mNextId);
                    List<NewUser> recivers = JsonDataFactory.getDataArray(NewUser.class, jsonArray);
                    adapter.add(recivers);
                    if (mHasData) {
                        listView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else {
                        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }
}
