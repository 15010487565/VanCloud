package com.vgtech.vancloud.ui.group;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;
import com.vgtech.common.utils.StringUtils;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.OrganizationAdapter;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vic on 2016/10/10.
 */
public class CreateWorkGroupActivity extends BaseActivity implements AdapterView.OnItemClickListener, TextWatcher, HttpListener<String>, OrganizationSearchListener {

    private View searchBtn;
    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;
    private ListView listView;
    private View nodetailview;
    private OrganizationAdapter mSearchAdapter;
    private TextView mUserSelectedTv;
    private TextView btn_finish;
    private Node mNode;
    private boolean mVantop;
    private OrganizationAdapterLister organizationVanCloudFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVantop = TenantPresenter.isVanTop(this);
        mNode = getIntent().getParcelableExtra("node");
        Intent intent = getIntent();
        String workgroupId = intent.getStringExtra("workgroupId");
        String workgroupName = intent.getStringExtra("workgroupName");
        if (!TextUtils.isEmpty(workgroupName)) {
            setTitle(workgroupName);
        } else {
            setTitle(getString(R.string.title_create_work_group));
        }
        if (!TextUtils.isEmpty(workgroupId)) {
            mNode = new Node();
            mNode.setName(workgroupName);
            mNode.setId(workgroupId);
        }
        btn_finish = (TextView) findViewById(R.id.tv_right);
        btn_finish.setOnClickListener(this);
        if (mVantop) {
            OrganizationFragment organizationVanCloudFragment = new OrganizationFragment();
            organizationVanCloudFragment.setSelectedChangeListener(selectedListener);
            this.organizationVanCloudFragment = organizationVanCloudFragment;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_layout, organizationVanCloudFragment);
            transaction.commitAllowingStateLoss();
        } else {
            OrganizationVanCloudFragment organizationVanCloudFragment = new OrganizationVanCloudFragment();
            organizationVanCloudFragment.setSelectedChangeListener(selectedListener);
            this.organizationVanCloudFragment = organizationVanCloudFragment;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_layout, organizationVanCloudFragment);
            transaction.commitAllowingStateLoss();
        }
        searchBtn = findViewById(R.id.btn_action_search);
        searchBtn.setOnClickListener(this);
        mUserSelectedTv = (TextView) findViewById(R.id.tv_selected_user);
        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
        listView = (ListView) findViewById(R.id.user_list);
        listView.setOnItemClickListener(this);
        nodetailview = findViewById(R.id.nodetailview);
        mSearchAdapter = new OrganizationAdapter(this, selectedListener);
        listView.setAdapter(mSearchAdapter);
        if (mNode == null) {
            mNode = new Node();
        } else {
            loadSelectData();
        }
    }

    private void loadSelectData() {
        new AsyncTask<Void, Void, List<Organization>>() {
            @Override
            protected List<Organization> doInBackground(Void... params) {
                List<User> userList = TenantPresenter.isVanTop(CreateWorkGroupActivity.this) ? WorkRelation.queryVantopWorkGroupByWgId(CreateWorkGroupActivity.this, mNode.getId()) : WorkRelation.queryWorkGroupByWgId(getApplicationContext(), mNode.getId());
                List<Organization> users = new ArrayList<Organization>();
                for (User user : userList) {
                    Organization organization = new Organization(user.job, user.getName(), user.userId, user.photo);
                    users.add(organization);
                }
                return users;
            }

            @Override
            protected void onPostExecute(List<Organization> userOrganization) {
                if (!isFinishing())
                    selectedListener.add(userOrganization);
            }
        }.execute();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String name = s.toString();
        mNode.setName(name);
    }

    private TextView mSearchTv;
    private EditText mNameEt;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_action_search: {
                LinearLayout btn_back = (LinearLayout) findViewById(R.id.btn_back);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(0, 0, 0, 0);
                btn_back.setLayoutParams(lp);
                getTitleTv().setVisibility(View.GONE);
                searchBtn.setVisibility(View.GONE);
                btn_finish.setVisibility(View.GONE);
                final SearchView searchView = (SearchView) findViewById(R.id.searchview);
                searchView.setVisibility(View.VISIBLE);
                int id = searchView.getContext()
                        .getResources()
                        .getIdentifier("android:id/search_src_text", null, null);
                int closeId = searchView.getContext()
                        .getResources()
                        .getIdentifier("android:id/search_close_btn", null, null);
                final ImageView closeView = (ImageView) searchView.findViewById(closeId);
                closeView.setBackgroundResource(R.drawable.btn_actionbar);
                final TextView textView = (TextView) searchView.findViewById(id);
                textView.setTextColor(Color.WHITE);
                textView.setHintTextColor(Color.parseColor("#CCFFFFFF"));
                searchView.setIconified(false);
                mSearchTv = textView;
                searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        closeSearch();
                        return true;
                    }
                });
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {

                        if (!TextUtils.isEmpty(s)) {
                            if (!mVantop) {
                                showLoadingView();
                                mSearchAdapter.clear();
                                List<User> userList = User.queryUserWithKeyWord(CreateWorkGroupActivity.this, s);
                                List<Organization> users = new ArrayList<Organization>();
                                for (User user : userList) {
                                    Organization organization = new Organization(user.job, user.getName(), user.userId, user.photo);
                                    users.add(organization);
                                }
                                hideLoadingView();
                                if (users.isEmpty()) {
                                    nodetailview.setVisibility(View.VISIBLE);
                                    mSearchAdapter.clear();
                                } else {
                                    mSearchAdapter.addAllData(users);
                                    mSearchAdapter.notifyDataSetChanged();
                                }
                            } else {
                                s = StringUtils.sqlValidate(s);
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("loginUserCode", PrfUtils.getStaff_no(CreateWorkGroupActivity.this));
                                params.put("q", s);
                                showLoadingView();
                                String url = VanTopUtils.generatorUrl(CreateWorkGroupActivity.this, UrlAddr.URL_ORGS_SEARCH);
                                NetworkPath path = new NetworkPath(url, params, CreateWorkGroupActivity.this, true);
                                getAppliction().getNetworkManager().load(CALLBACK_SEARCH, path, CreateWorkGroupActivity.this);
                            }
                        }
                        return false;
                    }
                });
            }
            break;
            case R.id.tv_right: {
                List<Organization> organizations = selectedListener.getSeleced();
                getAllOrganization(organizations);
            }
            break;
            default:
                super.onClick(v);
                break;
        }
    }

    private static final int CALLBACK_UPDATE = 2;
    private static final int CALLBACK_SEARCH = 10;
    private static final int CALLBACK_STAFFS = 11;

    private void updateGroup(String userIds) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        if (!TextUtils.isEmpty(mNode.getId()))
            params.put("groupid", String.valueOf(mNode.getId()));
        params.put("userids", userIds);
        params.put("name", mNode.getName());
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WORKGROUP_INSERT), params, this);
        getAppliction().getNetworkManager().load(CALLBACK_UPDATE, path, this);
    }

    private void showLoadingView() {
        listView.setVisibility(View.GONE);
        nodetailview.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMagView.setVisibility(View.VISIBLE);
        loadingMagView.setText(getString(R.string.dataloading));
        findViewById(R.id.searchuser_view).setVisibility(View.VISIBLE);
        findViewById(R.id.organization_view).setVisibility(View.GONE);

    }

    private void hideLoadingView() {
        loadingLayout.setVisibility(View.GONE);
        nodetailview.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingMagView.setVisibility(View.GONE);
        loadingMagView.setText(null);
        listView.setVisibility(View.VISIBLE);

    }

    private boolean finish;

    @Override
    public void finish() {
        if (mSearchTv != null) {
            closeSearch();
            if (!finish)
                return;
        }
        super.finish();
    }

    public void closeSearch() {
        if (mSearchTv != null) {
            final SearchView searchView = (SearchView) findViewById(R.id.searchview);
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(
                    mSearchTv.getWindowToken(), 0);
            searchView.setVisibility(View.GONE);
            searchBtn.setVisibility(View.VISIBLE);
            getTitleTv().setVisibility(View.VISIBLE);
            LinearLayout btn_back = (LinearLayout) findViewById(R.id.btn_back);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, Utils.convertDipOrPx(this, 50), 0);
            btn_back.setLayoutParams(lp);

            findViewById(R.id.searchuser_view).setVisibility(View.GONE);
            findViewById(R.id.organization_view).setVisibility(View.VISIBLE);
            btn_finish.setVisibility(View.VISIBLE);
            mSearchTv = null;
        }

    }

    @Override
    protected int getContentView() {
        return R.layout.create_workgroup;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Object object = parent.getItemAtPosition(position);
        if (object instanceof Organization) {
            Organization organization = (Organization) object;
            if (selectedListener.contains(organization)) {
                selectedListener.remove(organization);
            } else {
                selectedListener.add(organization);
            }
            mSearchAdapter.notifyDataSetChanged();
            if (organizationVanCloudFragment.getCategoryAdapter() != null)
                organizationVanCloudFragment.getCategoryAdapter().notifyDataSetChanged();
        }
    }

    public void selected(List<Organization> organizations) {
        int userCount = 0;
        int departCount = 0;
        for (Organization organization : organizations) {
            if (!"all".equals(organization.code)) {
                if (organization.isUser()) {
                    userCount++;
                } else {
                    departCount++;
                    userCount += Integer.parseInt(organization.num);
                }
            }
        }
        if (userCount != 0) {
            btn_finish.setEnabled(true);
            mUserSelectedTv.setText(getString(R.string.selected_lable) + getString(R.string.selected_lable_user, userCount));
//            if (departCount != 0)
//                mUserSelectedTv.append(getString(R.string.selected_lable_depart, departCount));
        } else {
            btn_finish.setEnabled(false);
            mUserSelectedTv.setText(getString(R.string.selected_lable));
        }
    }

    private List<String> mUserOrganization;

    public void getAllOrganization(final List<Organization> organizations) {
        showLoadingDialog(this, "");
        new AsyncTask<Void, Void, List<Organization>>() {

            @Override
            protected List<Organization> doInBackground(Void... params) {

                List<Organization> userOrganization = new ArrayList<Organization>();
                List<String> orgsCodes = new ArrayList<String>();
                mUserOrganization = new ArrayList<String>();
                mUpdateOrgList = new ArrayList<Organization>();
                for (Organization organization : organizations) {
                    if (!"all".equals(organization.code)) {
                        if (organization.isUser()) {
                            if (!userOrganization.contains(organization))
                                userOrganization.add(organization);
                            if (mVantop) {
                                mUserOrganization.add(organization.user_id);
                                mUpdateOrgList.add(organization);
                            }
                        } else {
                            if (!mVantop) {
                                List<User> allUser = getAlluser(organization.code);
                                for (User user : allUser) {
                                    Organization ori = new Organization(user.job, user.name, user.userId, user.photo);
                                    if (!userOrganization.contains(ori))
                                        userOrganization.add(ori);
                                }
                            } else {
//                                B0010-B0010:B0010
                                orgsCodes.add(organization.code + "-" + organization.pcodes);
                            }
                        }
                    }
                }
                if (!orgsCodes.isEmpty()) {
                    Map<String, String> postValues = new HashMap<String, String>();
                    postValues.put("loginUserCode", PrfUtils.getStaff_no(CreateWorkGroupActivity.this));
                    StringBuffer stringBuffer = new StringBuffer();
                    for (String orgsCode : orgsCodes)
                        stringBuffer.append(orgsCode).append(",");
                    postValues.put("orgsCodes", stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                    String url = VanTopUtils.generatorUrl(CreateWorkGroupActivity.this, UrlAddr.URL_ORGS_STAFFLIST);
                    NetworkPath path = new NetworkPath(url, postValues, CreateWorkGroupActivity.this, true);
                    getAppliction().getNetworkManager().load(CALLBACK_STAFFS, path, CreateWorkGroupActivity.this);
                    return null;
                } else {
                    return userOrganization;
                }
            }

            @Override
            protected void onPostExecute(final List<Organization> userOrganization) {
                if (userOrganization != null) {
                    StringBuilder userIds = new StringBuilder();
                    StringBuilder names = new StringBuilder();
                    int i = 0;
                    for (Organization n : userOrganization) {
                        i++;
                        if (mVantop) {
                            userIds.append(n.user_id).append(",");
                            if (i <= 3)
                                names.append(n.staff_name).append(",");
                        } else {
                            userIds.append(n.staff_no).append(",");
                            if (i <= 3)
                                names.append(n.staff_name).append(",");
                        }
                    }
                    if (TextUtils.isEmpty(mNode.getName())) {
                        names.deleteCharAt(names.length() - 1);
                        mNode.setName(names.toString());
                    }
                    userIds.deleteCharAt(userIds.length() - 1);
                    mUpdateOrgList = userOrganization;
                    updateGroup(userIds.toString());
                }
            }
        }.execute();
    }

    private List<Organization> mUpdateOrgList;

    public void updateAccessTime(final List<Organization> list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> ids = new ArrayList<String>();
                final List<Long> times = new ArrayList<Long>();
                final List<String> jobs = new ArrayList<String>();
                long time = System.currentTimeMillis();
                int count = list.size();
                for (int i = 0; i < count; i++) {
                    Organization organization = list.get(i);
                    User user = User.queryUser(getApplicationContext(), organization.user_id);
                    if (user == null) {
                        user = new User();
                        user.userId = organization.user_id;
                        user.photo = organization.photo;
                        user.name = organization.staff_name;
                        user.job = organization.pos;
                        user.email = organization.eMail;
                        user.accessTime = time + (count - i) * 10;
                        user.insert(getApplicationContext());
                    } else {
                        ids.add(organization.user_id);
                        jobs.add(TextUtils.isEmpty(organization.pos) ? "" : organization.pos);
                        times.add(time + (count - i) * 1000);
                    }
                }
                User.updateUserAccessTimeAndJob(getApplicationContext(), ids, times, jobs);
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
            }
        }).start();
    }

    public List<User> getAlluser(String departId) {
        List<User> allUser = new ArrayList<>();
        List<User> users = User.queryUserByDepart(this, departId);
        allUser.addAll(users);
        List<Department> departments = Department.queryChildDepartment(this, departId);
        for (Department department : departments) {
            allUser.addAll(getAlluser(department.did));
        }
        return allUser;
    }

    private OrganizationSelectedListener selectedListener = new OrganizationSelectedListener() {
        List<Organization> selectedList = new ArrayList<>();
        List<Organization> unSelectedList = new ArrayList<>();

        @Override
        public int getSelectMode() {
            return SELECT_MULTI;
        }

        @Override
        public List<Organization> getSeleced() {
            return selectedList;
        }

        @Override
        public List<Organization> getUnSeleced() {
            return unSelectedList;
        }

        @Override
        public void addUnSelected(List<Organization> list) {
            unSelectedList.addAll(list);
        }

        @Override
        public void add(Organization organization) {
            if (!selectedList.contains(organization))
                selectedList.add(organization);
            selected(selectedList);
        }

        @Override
        public void remove(Organization organization) {
            selectedList.remove(organization);
            selected(selectedList);
        }

        @Override
        public boolean contains(Organization organization) {
            boolean contains = selectedList.contains(organization);
            return contains;
        }

        @Override
        public void add(List<Organization> organizations) {
            for (Organization organization : organizations)
                add(organization);
            selected(selectedList);
        }

        @Override
        public void remove(List<Organization> organizations) {
            for (Organization organization : organizations)
                remove(organization);
            selected(selectedList);
        }
    };

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe;
        if (callbackId == CALLBACK_UPDATE) {
            safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        } else {
            safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        }
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_UPDATE: {
                try {
                    String groupId = rootData.getJson().getJSONObject("data").getString("groupid");
//                    Log.e("TAG_讨论组","groupId="+groupId);
//                    Log.e("TAG_讨论组","mNode.getId()="+mNode.getId());
                    if (TextUtils.isEmpty(mNode.getId())) {
                        WorkGroup workGroup = new WorkGroup();
                        workGroup.wgtoupId = groupId;
                        workGroup.name = mNode.getName();
                        workGroup.insert(this);
                    } else {
                        WorkGroup workGroup = new WorkGroup();
                        workGroup.wgtoupId = mNode.getId();
                        workGroup.name = mNode.getName();
                        workGroup.updateByWgId(this);
                    }
                    mNode.setId(groupId);
//                    Log.e("TAG_讨论组","VanTop="+TenantPresenter.isVanTop(this));
                    if (TenantPresenter.isVanTop(this)) {
//                        Log.e("TAG_讨论组","list="+(mUpdateOrgList != null));
                        if (mUpdateOrgList != null) {
                            updateAccessTime(mUpdateOrgList);
                        } else {
                            sendBroadcast(new Intent(GroupReceiver.REFRESH));
                        }

                    } else {
                        String userIds = path.getPostValues().get("userids");
                        String[] uIds = userIds.split(",");
                        List<WorkRelation> relations = new ArrayList<WorkRelation>();
                        for (String uid : uIds) {
                            WorkRelation wr = new WorkRelation();
                            wr.wgtoupId = mNode.getId();
                            wr.userId = uid;
                            relations.add(wr);
                        }
                        WorkRelation.updateWorkGroupRelationTable(relations, this, mNode.getId());
                    }

                    finish = true;

                    setResult(RESULT_OK);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            break;
            case CALLBACK_SEARCH: {
                hideLoadingView();
                JSONObject jsonObject = rootData.getJson();
                try {
                    if (jsonObject.has("staffs") && jsonObject.get("staffs") instanceof JSONArray) {
                        JSONArray nodeArray = jsonObject.getJSONArray("staffs");
                        List<Organization> list = JsonDataFactory.getDataArray(Organization.class, nodeArray);
                        if (list.isEmpty()) {
                            nodetailview.setVisibility(View.VISIBLE);
                            mSearchAdapter.clear();
                        } else {
                            mSearchAdapter.addAllData(list);
                            mSearchAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case CALLBACK_STAFFS:
                JSONObject jsonObject = rootData.getJson();
                try {
                    if (jsonObject.has("staffs") && jsonObject.get("staffs") instanceof JSONArray) {
                        JSONArray nodeArray = jsonObject.getJSONArray("staffs");
                        List<Organization> list = JsonDataFactory.getDataArray(Organization.class, nodeArray);
                        StringBuilder names = new StringBuilder();
                        int j = 0;
                        for (int i = 0; i < list.size(); i++) {
                            Organization ori = list.get(i);
                            String staff_no = ori.user_id;
                            if (!mUserOrganization.contains(staff_no)) {
                                mUserOrganization.add(staff_no);
                                mUpdateOrgList.add(ori);
                                j++;
                                if (j <= 3) {
                                    names.append(ori.staff_name).append(",");
                                }
                            }
                        }
                        StringBuilder userIds = new StringBuilder();
                        for (String n : mUserOrganization) {
                            userIds.append(n).append(",");
                        }
                        if (TextUtils.isEmpty(mNode.getName())) {
                            if (names.length() > 0)
                                names.deleteCharAt(names.length() - 1);
                            mNode.setName(names.toString());
                        }
                        if (userIds.length() > 0)
                            userIds.deleteCharAt(userIds.length() - 1);
                        updateGroup(userIds.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
}
