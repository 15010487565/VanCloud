package com.vgtech.vantop.ui.questionnaire;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.QustionnaireAdapter;
import com.vgtech.vantop.moudle.QuestionnaireListData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 调查问卷列表
 * create by scott
 */
public class QuestionnaireActivity extends BaseActivity implements HttpListener<String>,
        PullToRefreshBase.OnRefreshListener2, AdapterView.OnItemClickListener {

    private final int CALLBACK_LOADDATA = 0X001;
    private final int LAOD_FLAG_ADD = 0X002;
    private final int LOAD_FLAG_REFRESH = 0X003;
    private PullToRefreshListView mListView;
    private QustionnaireAdapter mAdapter;
    private List<QuestionnaireListData> mDatas;
    private int mNextId = 1;
    private int mLoadFlag = LOAD_FLAG_REFRESH;
    private VancloudLoadingLayout mLoadingView;

    private void initView() {

        mListView = (PullToRefreshListView) findViewById(R.id.list_question);
        mDatas = new ArrayList<>();
        mAdapter = new QustionnaireAdapter(this, mDatas);
        mListView.setAdapter(mAdapter);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(this);
        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);

        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {

                initData();
            }
        });
    }

    private void initData() {
        mLoadingView.showLoadingView(mListView, "", true);
        mLoadFlag = LOAD_FLAG_REFRESH;
        mNextId = 1;
        loadData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vantop_questionnaire_survey));
        initView();
    }

    private void loadData() {
        Uri uri = Uri.parse(VanTopUtils.generatorUrl(this, UrlAddr.URL_QUESTIONNAIRE));/*.
                buildUpon().appendQueryParameter("nextId", mNextId + "").build();*/
        Map<String, String> params = new HashMap<>();
        params.put("nextId", mNextId + "");
        NetworkPath np = new NetworkPath(uri.toString(), params, this, true);
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        nm.load(CALLBACK_LOADDATA, np, this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_questionnaire;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        mLoadingView.dismiss(mListView);
        mListView.onRefreshComplete();
        switch (callbackId) {
            case CALLBACK_LOADDATA: {
                boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (!safe) {
                    mLoadingView.showErrorView(mListView);
                    return;
                }
                JSONArray jArr = rootData.getJson().optJSONArray("data");
                List<QuestionnaireListData> lists = JsonDataFactory.getDataArray(QuestionnaireListData.class, jArr);
                if (mLoadFlag == LOAD_FLAG_REFRESH) {
                    mDatas.clear();
                }
                mDatas.addAll(lists);
                if (mDatas.isEmpty()) {
                    mLoadingView.showEmptyView(mListView, getString(R.string.vantop_nodata), true, true);
                    return;
                }
                mAdapter.notifyDataSetChanged();
            }
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

        mLoadFlag = LOAD_FLAG_REFRESH;
        mNextId = 1;
        loadData();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        mLoadFlag = LAOD_FLAG_ADD;
        mNextId++;
        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mDatas.isEmpty()) return;
        QuestionnaireListData data = mDatas.get(i - 1);
        Intent intent = new Intent(this, QustionnaireDetailActivity.class);
        intent.putExtra(QustionnaireDetailActivity.BUNDLE_KEY, data);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNextId = 1;
        mLoadFlag = LOAD_FLAG_REFRESH;
        loadData();
    }
}
