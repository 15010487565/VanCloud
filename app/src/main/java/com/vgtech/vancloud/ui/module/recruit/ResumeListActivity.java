package com.vgtech.vancloud.ui.module.recruit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.PublishResume;
import com.vgtech.common.api.PublishResumeList;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.common.utils.EditionUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.PublishResumeListAdapter;
import com.vgtech.vancloud.ui.module.resume.ResumeDetail;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by code on 2016/5/19.
 * 简历列表-搜索简历列表，简历列表，已删除简历列表
 */
public class ResumeListActivity extends BaseActivity implements View.OnClickListener, HttpListener<String>, AbsListView.OnScrollListener, PublishResumeListAdapter.OnSelectListener, AdapterView.OnItemClickListener {

    @InjectView(R.id.listview)
    PullToRefreshListView listview;
    VancloudLoadingLayout loading;
    @InjectView(R.id.bottom_layout)
    LinearLayout bottomLayout;
    private String type; //1,简历列表，2，搜索列表，3，已删除列表
    private String word, education, worktime, sex, status, place, sort, industry;
    private String position_id; //职位id
    private static final int CALL_BACK_RESUME = 1; //简历列表
    private static final int CALL_BACK_PAYINFO = 2; //简历购买
    private NetworkManager mNetworkManager;
    private PublishResumeListAdapter publishResumeListAdapter;
    private HashMap<String, PublishResumeList> mIndexer = new HashMap<>();
    private boolean isListViewRefresh = false;
    private Boolean isloading = true;
    private int n = 12;
    private String nextId = "0";
    private String mLastId = "0";
    private boolean mSafe;
    private boolean mHasData;

    @Override
    protected int getContentView() {
        return R.layout.resume_list_publish;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        mNetworkManager = getAppliction().getNetworkManager();
        publishResumeListAdapter = new PublishResumeListAdapter(this, new ArrayList<PublishResumeList>());
        publishResumeListAdapter.setOnSelectListener(this);
        listview.setAdapter(publishResumeListAdapter);
        listview.setOnItemClickListener(this);
        bottomLayout.setOnClickListener(this);

        loading = (VancloudLoadingLayout) findViewById(R.id.loading);
        Intent intent = getIntent();
        type = intent.getExtras().getString("type");
        if ("1".equals(type)) {
            position_id = intent.getExtras().getString("position_id");
            setTitle(getString(R.string.resume_list));
            publishResumeListAdapter.setIsCheckbox(true);
            initEvent();
            initData(position_id);
        } else if ("2".equals(type)) {
            setTitle(getString(R.string.search_resume));
            publishResumeListAdapter.setType(2);
            publishResumeListAdapter.setIsCheckbox(true);
            word = intent.getExtras().getString("word");
            education = intent.getExtras().getString("education");
            worktime = intent.getExtras().getString("worktime");
            sex = intent.getExtras().getString("sex");
            status = intent.getExtras().getString("status");
            place = intent.getExtras().getString("place");
            sort = intent.getExtras().getString("sort");
            industry = intent.getExtras().getString("industry");
            initEvent();
            searchData();
        }
    }

    private void initEvent() {
        listview.setOnScrollListener(this);
        listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isListViewRefresh = true;
                nextId = "0";
                if ("1".equals(type)) {
                    initData(position_id);
                } else {
                    searchData();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        loading.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                isloading = true;
                isListViewRefresh = true;
                nextId = "0";
                if ("1".equals(type)) {
                    initData(position_id);
                } else {
                    searchData();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.bottom_layout:
                if (mIndexer.size() == 0) {
                    showToast(getString(R.string.recruit_unchoose));
                } else {
                    if ("1".equals(type)) {
                        getPayinfoList(getResumeListIds(mIndexer));
                    } else {
                        getPayinfo(getIds(mIndexer));
                    }
                }
                break;
        }
    }

    //简历列表
    private void initData(String job_id) {
        if (isloading) {
            loading.showLoadingView(listview, "", true);
        }
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        if (!TextUtils.isEmpty(job_id)) {
            params.put("job_id", job_id);
        }
        params.put("status", "valid");
        params.put("n", n + "");
        if (!TextUtils.isEmpty(nextId) && !isListViewRefresh) {
            params.put("s", nextId);
        } else {
            params.put("s", "0");
        }
        mLastId = nextId;
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_LIST), params, this);
        mNetworkManager.load(CALL_BACK_RESUME, path, this, isloading);
    }

    //搜索列表
    private void searchData() {
        if (isloading) {
            loading.showLoadingView(listview, "", true);
        }
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("n", n + "");
        if (!TextUtils.isEmpty(nextId) && !isListViewRefresh) {
            params.put("s", nextId);
        } else {
            params.put("s", "0");
        }
        mLastId = nextId;
        if (!TextUtils.isEmpty(word)) {
            params.put("keyword", word);
        }
        if (!TextUtils.isEmpty(education)) {
            params.put("education", education);
        }
        if (!TextUtils.isEmpty(worktime)) {
            params.put("workdata", worktime);
        }
        if (!TextUtils.isEmpty(sex)) {
            params.put("gender", sex);
        }
        if (!TextUtils.isEmpty(sort)) {
            params.put("jobtype", sort);
        }
        if (!TextUtils.isEmpty(industry)) {
            params.put("industry", industry);
        }
        if (!TextUtils.isEmpty(status)) {
            params.put("jobstatus", status);
        }
        if (!TextUtils.isEmpty(place)) {
            params.put("place", place);
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SEARCH_RESUME), params, this);
        mNetworkManager.load(CALL_BACK_RESUME, path, this, isloading);

    }

    //简历列表获取支付信息
    private void getPayinfoList(String ids) {
        loading.showLoadingView(listview, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_type", "tenant");
        params.put("resource_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_BUY), params, this);
        mNetworkManager.load(CALL_BACK_PAYINFO, path, this, isloading);
    }

    //搜索列表获取支付信息
    private void getPayinfo(String ids) {
        loading.showLoadingView(listview, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_type", "tenant");
        params.put("resource_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_RESUME_PAYINFO), params, this);
        mNetworkManager.load(CALL_BACK_PAYINFO, path, this, isloading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200) {
            isListViewRefresh = true;
            nextId = "0";
            if ("1".equals(type)) {
                initData(position_id);
            } else {
                searchData();
            }
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loading.dismiss(listview);
        listview.onRefreshComplete();
        mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            if (callbackId == CALL_BACK_RESUME && publishResumeListAdapter.getCount() == 0) {
                loading.showErrorView(listview);
                bottomLayout.setVisibility(View.GONE);
            }
            return;
        }
        switch (callbackId) {

            case CALL_BACK_RESUME:
                List<PublishResume> publishResumeRows = new ArrayList<>();
                List<PublishResumeList> publishResumeAdapterLists = new ArrayList<PublishResumeList>();
                try {
                    String data = rootData.getJson().getString("data");
                    JSONObject jsonObject = new JSONObject(data);
                    nextId = jsonObject.getString("nextid");
                    mHasData = !TextUtils.isEmpty(nextId) && !"0".equals(nextId);
                    publishResumeRows = JsonDataFactory.getDataArray(PublishResume.class, jsonObject.getJSONArray("rows"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (publishResumeRows != null && publishResumeRows.size() > 0) {
                    for (int i = 0; i < publishResumeRows.size(); i++) {
                        try {
                            PublishResumeList temp = JsonDataFactory.getData(PublishResumeList.class,
                                    new JSONObject(publishResumeRows.get(i).getJson().getString("base_info")));
                            if (temp != null) {
                                PublishResumeList publishResumeList = new PublishResumeList();
                                publishResumeList.resume_id = EditionUtils.fromString(publishResumeRows.get(i).resume_id);
                                publishResumeList.resume_format = EditionUtils.fromString(publishResumeRows.get(i).resume_format);
                                publishResumeList.resume_type = EditionUtils.fromString(publishResumeRows.get(i).resume_type);
                                publishResumeList.is_free = EditionUtils.fromString(publishResumeRows.get(i).is_free);
                                publishResumeList.send_date = EditionUtils.fromString(publishResumeRows.get(i).send_date);
                                publishResumeList.city_base = EditionUtils.fromString(temp.city_base);
                                publishResumeList.degree_base = EditionUtils.fromString(temp.degree_base);
                                publishResumeList.fullname_base = EditionUtils.fromString(temp.fullname_base);
                                publishResumeList.gender_base = EditionUtils.fromString(temp.gender_base);
                                publishResumeList.jobtitle_other = EditionUtils.fromString(temp.jobtitle_other);
                                publishResumeList.price = EditionUtils.fromString(temp.price);
                                publishResumeList.salaryrange_other = EditionUtils.fromString(temp.salaryrange_other);
                                publishResumeList.work_term = EditionUtils.fromString(temp.work_term);
                                publishResumeList.resource_id = EditionUtils.fromString(publishResumeRows.get(i).resource_id);
                                publishResumeAdapterLists.add(publishResumeList);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (publishResumeListAdapter == null) {
                    publishResumeListAdapter = new PublishResumeListAdapter(this, publishResumeAdapterLists);
                    listview.setAdapter(publishResumeListAdapter);
                } else {
                    String page = path.getPostValues().get("s");
                    if ("0".equals(page)) {
                        isListViewRefresh = true;
                    }
                    if (isListViewRefresh) {
                        publishResumeListAdapter.getMlist().clear();
                        isListViewRefresh = false;
                    }
                    List<PublishResumeList> list = publishResumeListAdapter.getMlist();
                    list.addAll(publishResumeAdapterLists);
                    publishResumeListAdapter.myNotifyDataSetChanged(list);
                }
                if (publishResumeListAdapter.getCount() > 0) {
                    bottomLayout.setVisibility(View.VISIBLE);
                } else {
                    loading.showEmptyView(listview, getString(R.string.no_resume_detail), true, true);
                    bottomLayout.setVisibility(View.GONE);
                }
                isloading = false;
                break;

            case CALL_BACK_PAYINFO:
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONObject resultObject = jsonObject.getJSONObject("data");
                    com.vgtech.common.utils.ActivityUtils.toPay(this, resultObject.getString("order_id"), resultObject.getString("order_name"),
                            resultObject.getString("amount"), resultObject.getString("order_type"), -1, PasswordFragment.COMPANYUSER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
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
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null) {
            mNetworkManager.cancle(this);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean flag = false;
        if (!TextUtils.isEmpty(mLastId)) {
            if (mLastId.equals(nextId))
                flag = true;
        }
        if (!flag && mSafe && mHasData && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            if ("1".equals(type)) {
                initData(position_id);
            } else {
                searchData();
            }
        }
    }

    public static String getResumeListIds(HashMap<String, PublishResumeList> map) {
        StringBuffer buffer = new StringBuffer();

        Set<String> keySet = map.keySet();
        for (String keyName : keySet) {
            PublishResumeList item = map.get(keyName);
            buffer.append(item.resume_id).append(",");
        }
        if (buffer.toString().length() > 1) {
            return buffer.toString().substring(0, buffer.toString().length() - 1);
        } else {
            return buffer.toString().substring(0, buffer.toString().length());
        }
    }

    public static String getIds(HashMap<String, PublishResumeList> map) {
        StringBuffer buffer = new StringBuffer();

        Set<String> keySet = map.keySet();
        for (String keyName : keySet) {
            PublishResumeList item = map.get(keyName);
            buffer.append(item.resource_id).append(",");
        }
        if (buffer.toString().length() > 1) {
            return buffer.toString().substring(0, buffer.toString().length() - 1);
        } else {
            return buffer.toString().substring(0, buffer.toString().length());
        }
    }

    @Override
    public void OnSelected(PublishResumeList item) {
        mIndexer.put(item.resume_id, item);
    }

    @Override
    public void OnUnSelected(PublishResumeList item) {
        mIndexer.remove(item.resume_id);
    }

    @Override
    public boolean OnIsSelect(PublishResumeList item) {
        return mIndexer.containsKey(item.resume_id);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof PublishResumeList) {
            PublishResumeList resume = (PublishResumeList) obj;
            Intent intent = new Intent(this, ResumeDetail.class);
            intent.putExtra("resumeType", type);
            if ("1".equals(type)) { //简历列表
                intent.putExtra("is_from", "resume_list");
                intent.putExtra("resume_format", resume.resume_format);
            } else { //搜索简历列表
                intent.putExtra("show_type", false);
                intent.putExtra("is_from", "search_list");
            }
            intent.putExtra("is_free", resume.is_free);
            intent.putExtra("resource_ids", resume.resource_id);
            intent.putExtra("resume_id", resume.resume_id);
            startActivityForResult(intent, 200);
        }
    }
}
