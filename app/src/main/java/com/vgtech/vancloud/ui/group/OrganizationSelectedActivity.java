package com.vgtech.vancloud.ui.group;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.provider.db.WorkRelation;
import com.vgtech.common.utils.StringUtils;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.OrganizationAdapter;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.AppModulePresenterVantop;
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
 * Created by vic on 2016/10/11.
 */
public class OrganizationSelectedActivity extends BaseActivity implements OrganizationSearchListener, HttpListener<String>, OrganizationBackListener, AdapterView.OnItemClickListener {

    private OrganizationAdapter mNearAdapter;

    private TextView mUserSelectedTv;
    private TextView btn_finish;

    private View mNearWaitView;
    private ListView mNearListView;
    private boolean mVantop;
    private View searchBtn;
    private OrganizationAdapter mSearchAdapter;

    private View mNearView;
    private View mOriView;
    private View mSearchView;
    private int mSelectMode;
    private TextView mSearchTv;

    @Override
    protected int getContentView() {
        return R.layout.organization_selected;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVantop = TenantPresenter.isVanTop(this);
        if (Constants.DEBUG){
            Log.e("TAG_类型选择","mVantop="+mVantop);
        }
        searchBtn = findViewById(R.id.btn_action_search);
        searchBtn.setOnClickListener(this);
        setTitle(getString(R.string.title_group_select));
        mUserSelectedTv = (TextView) findViewById(R.id.tv_selected_user);
        btn_finish = (TextView) findViewById(R.id.tv_right);
        btn_finish.setOnClickListener(this);
        Intent intent = getIntent();
        ArrayList<Node> selectNodes = intent.getParcelableArrayListExtra("select");
        ArrayList<Node> unSelectNodes = intent.getParcelableArrayListExtra("unselect");
        boolean canSelectedSelf = intent.getBooleanExtra("selectSelf", false);
        boolean hasPass = intent.getBooleanExtra("HAS_PASS", false);
        if (hasPass) {
            btn_finish.setVisibility(View.GONE);
            View startMeeting = findViewById(R.id.start_meeting_layout);
            startMeeting.setVisibility(View.VISIBLE);
            startMeeting.setOnClickListener(this);
        }
        mSelectMode = getIntent().getIntExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_SINGLE);
        if (unSelectNodes == null) {
            unSelectNodes = new ArrayList<Node>();
        }
        PreferencesController preferencesController = new PreferencesController();
        preferencesController.context = this;
        UserAccount userAccount = preferencesController.getAccount();
        if (!canSelectedSelf) {
            Node node = new Node(userAccount.user_id, userAccount.nickname(), true, userAccount.photo);
            unSelectNodes.add(node);
        }
        if (selectNodes != null) {
            List<Organization> organizations = new ArrayList<>();
            for (Node node : selectNodes) {
                if (node.isUser()) {
                    Organization organization = new Organization(node.getJob(), node.getName(), node.getId(), node.getPhoto());
                    organization.eMail = node.email();
                    organizations.add(organization);
                }
            }
            selectedListener.add(organizations);
        }
        if (unSelectNodes != null) {
            List<Organization> organizations = new ArrayList<>();
            for (Node node : unSelectNodes) {
                if (node.isUser()) {
                    Organization organization = new Organization(node.getJob(), node.getName(), node.getId(), node.getPhoto());
                    organization.eMail = node.email();
                    organizations.add(organization);
                }
            }
            selectedListener.addUnSelected(organizations);
        }

        mNearView = findViewById(R.id.near_view);
        mOriView = findViewById(R.id.organization_view);
        mSearchView = findViewById(R.id.searchuser_view);
        LayoutInflater inflater = getLayoutInflater();
        View headerView = inflater.inflate(R.layout.organization_selected_header, null);
        if (AppModulePresenterVantop.isOpenZuzhijiagou(this)) {
            headerView.findViewById(R.id.btn_selected_organization).setOnClickListener(this);
        } else {
            if (headerView.findViewById(R.id.btn_selected_organization) != null && headerView.findViewById(R.id.btn_selected_organization).getVisibility() != View.GONE) {
                headerView.findViewById(R.id.btn_selected_organization).setVisibility(View.GONE);
            }
        }
        headerView.findViewById(R.id.btn_selected_workgroup).setOnClickListener(this);
        mNearListView = (ListView) findViewById(R.id.near_list);
        mNearListView.addHeaderView(headerView);
        mNearWaitView = inflater.inflate(R.layout.progress_black, null);
        mNearListView.addFooterView(mNearWaitView);
        mNearAdapter = new OrganizationAdapter(this, selectedListener);
        mNearListView.setAdapter(mNearAdapter);
        mNearListView.setOnItemClickListener(this);

        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
        listView = (ListView) findViewById(R.id.user_list);
        listView.setOnItemClickListener(this);
        nodetailview = findViewById(R.id.nodetailview);
        mSearchAdapter = new OrganizationAdapter(this, selectedListener);
        listView.setAdapter(mSearchAdapter);

        loadNearUser();
    }

    private static final int CALLBACK_SEARCH = 10;
    private static final int CALLBACK_STAFFS = 11;
    private int orgVisible;
    private int nearVisible;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_meeting_layout:
            case R.id.tv_right: {
                List<Organization> organizations = selectedListener.getSeleced();
                if (Constants.DEBUG){
                    Log.e("TAG_选择人员1","organizations="+organizations.toString());
                }
                getAllOrganization(organizations);
            }
            break;
            case R.id.btn_action_search: {
                orgVisible = mOriView.getVisibility();
                nearVisible = mNearView.getVisibility();
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
                            Log.e("TAG_数据加载","mVantop="+mVantop);
                            if (!mVantop) {

                                showLoadingView();
                                mSearchAdapter.clear();
                                List<User> userList = User.queryUserWithKeyWord(OrganizationSelectedActivity.this, s);
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
                                params.put("loginUserCode", PrfUtils.getStaff_no(OrganizationSelectedActivity.this));
                                params.put("q", s);
                                showLoadingView();
                                String url = VanTopUtils.generatorUrl(OrganizationSelectedActivity.this, UrlAddr.URL_ORGS_SEARCH);
                                NetworkPath path = new NetworkPath(url, params, OrganizationSelectedActivity.this, true);
                                getAppliction().getNetworkManager().load(CALLBACK_SEARCH, path, OrganizationSelectedActivity.this);
                            }
                        }
                        return false;
                    }
                });
            }
            break;
            case R.id.btn_selected_organization: {//从组织机构
                mOriView.setVisibility(View.VISIBLE);
                mNearView.setVisibility(View.GONE);
                if (mVantop) {
                    OrganizationFragment organizationVanCloudFragment = new OrganizationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("index", true);
                    organizationVanCloudFragment.setArguments(bundle);
                    organizationVanCloudFragment.setSelectedChangeListener(selectedListener);
                    organizationVanCloudFragment.setOrganizationBackListener(this);
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.fragment_layout, organizationVanCloudFragment);
                    transaction.commitAllowingStateLoss();
                } else {
                    OrganizationVanCloudFragment organizationVanCloudFragment = new OrganizationVanCloudFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("index", true);
                    organizationVanCloudFragment.setArguments(bundle);
                    organizationVanCloudFragment.setSelectedChangeListener(selectedListener);
                    organizationVanCloudFragment.setOrganizationBackListener(this);
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.fragment_layout, organizationVanCloudFragment);
                    transaction.commitAllowingStateLoss();
                }
            }
            break;
            case R.id.btn_selected_workgroup://从工作组选择
                mOriView.setVisibility(View.VISIBLE);
                mNearView.setVisibility(View.GONE);
                OrganizationWorkGroupFragment organizationVanCloudFragment = new OrganizationWorkGroupFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("index", true);
                organizationVanCloudFragment.setArguments(bundle);
                organizationVanCloudFragment.setSelectedChangeListener(selectedListener);
                organizationVanCloudFragment.setOrganizationBackListener(this);
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_layout, organizationVanCloudFragment);
                transaction.commitAllowingStateLoss();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;
    private ListView listView;
    private View nodetailview;

    private void showLoadingView() {
        Log.e("TAG_数据加载","showLoadingView");
        listView.setVisibility(View.GONE);
        nodetailview.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMagView.setVisibility(View.VISIBLE);
        loadingMagView.setText(getString(R.string.dataloading));

        mOriView.setVisibility(View.GONE);
        mNearView.setVisibility(View.GONE);
        mSearchView.setVisibility(View.VISIBLE);

    }

    private void hideLoadingView() {
        loadingLayout.setVisibility(View.GONE);
        nodetailview.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingMagView.setVisibility(View.GONE);
        loadingMagView.setText(null);
        listView.setVisibility(View.VISIBLE);

    }

    private List<Organization> mUserOrganization;

    public void getAllOrganization(final List<Organization> organizations) {
        new AsyncTask<Void, Void, List<Organization>>() {

            @Override
            protected void onPreExecute() {
                showLoadingDialog(OrganizationSelectedActivity.this, "");
            }

            @Override
            protected List<Organization> doInBackground(Void... params) {
                List<Organization> userOrganization = new ArrayList<Organization>();
                List<String> orgsCodes = new ArrayList<String>();
                mUserOrganization = new ArrayList<Organization>();
                for (Organization organization : organizations) {
                    if (!"all".equals(organization.code)) {
                        if (organization.isUser()) {
                            if (!userOrganization.contains(organization))
                                userOrganization.add(organization);
                            if (mVantop) {
                                mUserOrganization.add(organization);
                            }
                        } else {
                            if (!mVantop) {
                                if (organization.isWorkGroup) {
                                    List<User> allUser = WorkRelation.queryWorkGroupByWgId(OrganizationSelectedActivity.this, organization.code);
                                    for (User user : allUser) {
                                        Organization ori = new Organization(user.job, user.name, user.userId, user.photo);
                                        if (!userOrganization.contains(ori))
                                            userOrganization.add(ori);
                                    }
                                } else {
                                    List<User> allUser = getAlluser(organization.code);
                                    for (User user : allUser) {
                                        Organization ori = new Organization(user.job, user.name, user.userId, user.photo);
                                        if (!userOrganization.contains(ori))
                                            userOrganization.add(ori);
                                    }
                                }
                            } else {
                                if (organization.isWorkGroup) {
                                    List<User> allUser = WorkRelation.queryVantopWorkGroupByWgId(OrganizationSelectedActivity.this, organization.code);
                                    for (User user : allUser) {
                                        Organization ori = new Organization(user.job, user.name, user.userId, user.photo);
                                        if (!userOrganization.contains(ori))
                                            userOrganization.add(ori);
                                    }
                                } else {
                                    orgsCodes.add(organization.code + "-" + organization.pcodes);
                                }
                            }
                        }
                    }else {
                        break;
                    }
                }
                if (Constants.DEBUG){
                    Log.e("TAG_选择人员2","orgsCodes="+orgsCodes.toString());

                }
                if (!orgsCodes.isEmpty()) {
                    Map<String, String> postValues = new HashMap<String, String>();
                    postValues.put("loginUserCode", PrfUtils.getStaff_no(OrganizationSelectedActivity.this));
                    StringBuffer stringBuffer = new StringBuffer();
                    for (String orgsCode : orgsCodes)
                        stringBuffer.append(orgsCode).append(",");
                    postValues.put("orgsCodes", stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                    String url = VanTopUtils.generatorUrl(OrganizationSelectedActivity.this, UrlAddr.URL_ORGS_STAFFLIST);
                    NetworkPath path = new NetworkPath(url, postValues, OrganizationSelectedActivity.this, true);
                    getAppliction().getNetworkManager().load(CALLBACK_STAFFS, path, OrganizationSelectedActivity.this);
                    return null;
                } else {
                    return userOrganization;
                }
            }

            @Override
            protected void onPostExecute(List<Organization> userOrganization) {
                if (userOrganization != null) {
                    dismisLoadingDialog();
                    onSelectFinished(userOrganization);
                }
            }
        }.execute();
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

    private void loadNearUser() {
        new AsyncTask<Void, Void, List<Organization>>() {
            @Override
            protected List<Organization> doInBackground(Void... params) {
                List<User> userList = User.queryAccessUserExceptLoginer(OrganizationSelectedActivity.this);
                List<Organization> users = new ArrayList<Organization>();
                for (User user : userList) {
                    Organization organization = new Organization(user.job, user.getName(), user.userId, user.photo);
                    organization.eMail = user.email;
                    users.add(organization);
                }
                return users;
            }

            @Override
            protected void onPostExecute(List<Organization> userOrganization) {
                mNearAdapter.addAllDataAndNorify(userOrganization);
                mNearListView.removeFooterView(mNearWaitView);
                findViewById(R.id.near_loading).setVisibility(View.GONE);
            }
        }.execute();
    }

    private OrganizationSelectedListener selectedListener = new OrganizationSelectedListener() {
        List<Organization> selectedList = new ArrayList<>();
        List<Organization> unSelectedList = new ArrayList<>();

        @Override
        public int getSelectMode() {
            return mSelectMode;
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
//            Log.e("TAG_选择人员Unselected","list="+list.toString());
            unSelectedList.addAll(list);
        }

        @Override
        public void add(Organization organization) {
//            Log.e("TAG_选择人员add","organization="+organization.toString());
            if (mSelectMode == SELECT_SINGLE)
                selectedList.clear();
            if (!selectedList.contains(organization))
                selectedList.add(organization);
            for (Organization checkItem : unSelectedList)
                selectedList.remove(checkItem);
            selected(selectedList);
        }

        @Override
        public void remove(Organization organization) {
//            Log.e("TAG_选择人员remove","organization="+organization.toString());
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
//            Log.e("TAG_选择人员addlist","organizations="+organizations.toString());
            for (Organization organization : organizations)
                add(organization);
            selected(selectedList);
        }

        @Override
        public void remove(List<Organization> organizations) {
//            Log.e("TAG_选择人员remove","organizations="+organizations.toString());
            for (Organization organization : organizations)
                remove(organization);
            selected(selectedList);
        }
    };

    public void selected(List<Organization> organizations) {
        int userCount = 0;
        int departCount = 0;
        for (Organization organization : organizations) {
            if (!"all".equals(organization.code)) {
                if (organization.isUser()) {
                    userCount++;
                } else {
                    if (!organization.isWorkGroup)
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

    @Override
    public void finish() {
        if (mSearchTv != null) {
            closeSearch();
            return;
        }
        super.finish();
    }

    public void closeSearch() {
        if (mSearchTv != null) {
            btn_finish.setVisibility(View.VISIBLE);
            mOriView.setVisibility(orgVisible);
            mNearView.setVisibility(nearVisible);
            mSearchView.setVisibility(View.GONE);
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
            mSearchTv = null;
        }

    }

    @Override
    public void onBack() {
        findViewById(R.id.organization_view).setVisibility(View.GONE);
        findViewById(R.id.near_view).setVisibility(View.VISIBLE);
        if (mNearAdapter != null)
            mNearAdapter.notifyDataSetChanged();
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
            if (mSearchTv != null)
                mSearchAdapter.notifyDataSetChanged();
            mNearAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
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
                            mSearchAdapter.clear();
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
                        for (int i = 0; i < list.size(); i++) {
                            Organization org = list.get(i);
                            if (!mUserOrganization.contains(org))
                                mUserOrganization.add(org);
                        }
                        onSelectFinished(mUserOrganization);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void onSelectFinished(final List<Organization> list) {
      if (Constants.DEBUG){
          Log.e("TAG_选择人员3","list="+list.size());
          Log.e("TAG_选择人员3","list="+list.toString());
      }
        ArrayList<Node> nodes = new ArrayList<>();
        int count = list.size();
        for (int i = 0; i < count; i++) {
            Organization organization = list.get(i);
            if (selectedListener.getUnSeleced().contains(organization))
                continue;
            Node node = new Node();
            node.setIsUser(true);
            node.setEmail(organization.eMail);
            node.setId(organization.user_id);
            node.setPhoto(organization.photo);
            node.setName(organization.staff_name);
            node.setJob(organization.pos);
            nodes.add(node);
        }
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
            }
        }).start();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("desplayNodes", nodes);
        intent.putParcelableArrayListExtra("select", nodes);
        if (nodes.size() > Constants.XMPP_GROUP_USER_MAX) {
            Toast.makeText(this, getString(R.string.xmpp_group_user_max, Constants.XMPP_GROUP_USER_MAX), Toast.LENGTH_SHORT).show();
            return;
        }
        if (list != null && list.size() > 0) {//视频会议如果没有选人返回true,则直接进入会议;否则发送push通知。
            intent.putExtra("hasPass", false);
        } else {
            intent.putExtra("hasPass", true);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
