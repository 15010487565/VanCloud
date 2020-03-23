package com.vgtech.vancloud.ui.group;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.OrganizationAdapter;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * vancloud 组织架构
 * Created by vic on 2016/10/8.
 */
public class OrganizationVanCloudActivity extends BaseActivity implements Animation.AnimationListener, AdapterView.OnItemClickListener, HttpListener<String> {
    private ViewFlipper mCategoryFlipper;
    private Animation mNextInAnimation;
    private Animation mNextOutAnimation;
    private Animation mPreviousInAnimation;
    private Animation mPreviousOutAnimation;
    private View mCategoryWait;
    private OrganizationAdapter mCategoryAdapter;
    private ListView mCategoryListView;
    private LinearLayout mLuncherLayout;
    private HorizontalScrollView mLuncherScrollView;
    private View searchBtn;
    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;
    private ListView listView;
    private View nodetailview;
    private OrganizationAdapter mSearchAdapter;
    private boolean mHasGroupEditPermisstion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.organization));
        mHasGroupEditPermisstion = AppPermissionPresenter.hasPermission(this, AppPermission.Type.settings, AppPermission.Setting.organization.toString());
        if (mHasGroupEditPermisstion)
            initRightTv(getString(R.string.edit));
        mCategoryHistory = new ArrayList<CategoryInfo>();
        mLuncherLayout = (LinearLayout) findViewById(R.id.luncher_layout);
        mLuncherScrollView = (HorizontalScrollView) findViewById(R.id.luncher_scrollview);
        searchBtn = findViewById(R.id.btn_action_search);
        searchBtn.setOnClickListener(this);

        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
        listView = (ListView) findViewById(R.id.user_list);
        listView.setOnItemClickListener(this);
        nodetailview = findViewById(R.id.nodetailview);

        mSearchAdapter = new OrganizationAdapter(this);
        listView.setAdapter(mSearchAdapter);
        setCatTitle(null);
        initView();
    }

    private String mRootDepartId;

    private void initView() {
        final LayoutInflater inflater = getLayoutInflater();
        mCategoryFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        mNextInAnimation = AnimationUtils.loadAnimation(this,
                R.anim.in_righttoleft);
        mNextOutAnimation = AnimationUtils.loadAnimation(this,
                R.anim.out_righttoleft);
        mNextOutAnimation.setAnimationListener(this);
        mPreviousInAnimation = AnimationUtils.loadAnimation(this,
                R.anim.in_lefttoright);
        mPreviousOutAnimation = AnimationUtils.loadAnimation(this,
                R.anim.out_lefttoright);
        mPreviousOutAnimation.setAnimationListener(this);
        ListView categoryListView = (ListView) findViewById(R.id.category_list);
        setupCategoryListView(inflater, categoryListView);
        String url = ApiUtils.generatorUrl(this, URLAddr.URL_GROUP_LIST);
        List<Department> departments = Department.queryChildDepartment(this, "null");
        if (!departments.isEmpty()) {
            mRootDepartId = departments.get(0).did;
            Uri uri = Uri.parse(url).buildUpon().appendQueryParameter("code", mRootDepartId).build();
            updateCategories(uri, mCategoryListView, mCategoryAdapter);
        }

    }

    private void setupCategoryListView(LayoutInflater inflater,
                                       ListView listView) {
        mCategoryWait = inflater.inflate(R.layout.progress_black, null);
        mCategoryWait.findViewById(R.id.btn_retry).setVisibility(View.GONE);
        listView.addFooterView(mCategoryWait);
        mCategoryAdapter = new OrganizationAdapter(this);
        mCategoryListView = listView;
        listView.setAdapter(mCategoryAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_organization;
    }

    private ArrayList<CategoryInfo> mCategoryHistory;
    private Uri mCurrentCategories;

    private boolean updateCategories(Uri uri, ListView oldListView,
                                     OrganizationAdapter oldAdapter) {
        if (uri == null
                || (mCurrentCategories != null && uri
                .compareTo(mCurrentCategories) == 0)) {
            return false;
        }
        String cat = uri.getQueryParameter("level");
        if (TextUtils.isEmpty(cat)) {
            mCategoryHistory.clear();
        } else if (oldAdapter != null && !oldAdapter.isEmpty()) {
            // save status
            Parcelable state = oldListView.onSaveInstanceState();
            mCategoryHistory.add(new CategoryInfo(mCurrentCategories,
                    mCurrentCategories.getQueryParameter("lable") == null ? "root" : mCurrentCategories.getQueryParameter("lable"), state, oldAdapter));
        }
        // clean data
        mCategoryAdapter.clear();
        mCategoryWait.setVisibility(View.VISIBLE);
        mCurrentCategories = uri;
        showProgress(mCategoryWait, true);
        mCategoryWait.postDelayed(new Runnable() {
            @Override
            public void run() {
                String level = mCurrentCategories.getQueryParameter("level");
                if (TextUtils.isEmpty(level)) {
                    mCategoryAdapter.addAllData(getOrganizations(1, null));
                } else {
                    int lev = Integer.parseInt(level);
                    mCategoryAdapter.addAllData(getOrganizations(lev + 1, mCurrentCategories.getQueryParameter("code")));
                }
                mCategoryAdapter.notifyDataSetChanged();
                mCategoryListView.removeFooterView(mCategoryWait);
            }
        }, 500);
        initActionView();
        return true;
    }

    public List<Node> nodes;

    public List<Organization> getOrganizations(int level, String pid) {
        if (nodes == null)
            nodes = getAppliction().getTreeNode();
        List<Organization> departs = new ArrayList<Organization>();
        List<Organization> users = new ArrayList<Organization>();
        for (Node node : nodes) {
            if (level == 1 && node.getLevel() == level || node.getLevel() == level && (!TextUtils.isEmpty(pid) && pid.equals(node.getpId()) || TextUtils.isEmpty(pid) && TextUtils.isEmpty(node.getpId()))) {
                if (!node.isUser()) {
                    Organization organization = new Organization("" + TreeHelper.getChildCount(node), node.getId(), node.getName());
                    organization.node = node;
                    organization.level = "" + level;
                    organization.isBranch = node.isBranch;
                    organization.pcodes = node.getpId();
                    departs.add(organization);
                } else {
                    Organization organization = new Organization(node.getJob(), node.getName(), node.getId(), node.getPhoto());
                    organization.node = node;
                    organization.level = "" + level;
                    organization.pcodes = node.getpId();
                    users.add(organization);
                }
            }
        }
        departs.addAll(users);
        return departs;
    }

    private void showLoadingView() {
        listView.setVisibility(View.GONE);
        nodetailview.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMagView.setVisibility(View.VISIBLE);
        loadingMagView.setText(getString(R.string.dataloading));
        findViewById(R.id.searchuser_view).setVisibility(View.VISIBLE);
        findViewById(R.id.viewFlipper).setVisibility(View.GONE);
        findViewById(R.id.luncher_scrollview).setVisibility(View.GONE);

    }

    private void hideLoadingView() {
        loadingLayout.setVisibility(View.GONE);
        nodetailview.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingMagView.setVisibility(View.GONE);
        loadingMagView.setText(null);
        listView.setVisibility(View.VISIBLE);

    }

    private TextView mSearchTv;

    private boolean mEdit;

    private void initActionView() {
        View btn_addcompany = findViewById(R.id.btn_addcompany);
        View btn_adddepartment = findViewById(R.id.btn_adddepartment);
        View btn_setting = findViewById(R.id.btn_setting);
        btn_addcompany.setOnClickListener(this);
        btn_adddepartment.setOnClickListener(this);
        btn_setting.setOnClickListener(this);
        String isBranch = mCurrentCategories.getQueryParameter("isBranch");
        if (TextUtils.isEmpty(isBranch)) {
            //根节点
            btn_addcompany.setVisibility(View.VISIBLE);
            btn_adddepartment.setVisibility(View.VISIBLE);
            btn_setting.setVisibility(View.GONE);
        } else if (Integer.parseInt(isBranch) == 1) {
            //分公司
            btn_addcompany.setVisibility(View.GONE);
            btn_adddepartment.setVisibility(View.VISIBLE);
            btn_setting.setVisibility(View.VISIBLE);
        } else {
            //部门
            btn_addcompany.setVisibility(View.GONE);
            btn_adddepartment.setVisibility(View.VISIBLE);
            btn_setting.setVisibility(View.VISIBLE);
        }

    }

    private EditText mNameEt;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addcompany: {
                AlertDialog dialog = new AlertDialog(OrganizationVanCloudActivity.this).builder().setTitle(getString(R.string.add_company_name));
                mNameEt = dialog.setEditer();
                mNameEt.setSelection(mNameEt.getText().length());
                dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cn = mNameEt.getText().toString().trim();
                        if (TextUtils.isEmpty(cn)) {
                            showToast(getString(R.string.input_name));
                            return;
                        }
                        String code = mCurrentCategories.getQueryParameter("code");
                        updatedepartnameAction(1, code, "", cn, "");
                    }
                }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
            break;
            case R.id.btn_adddepartment:
                AlertDialog dialog = new AlertDialog(OrganizationVanCloudActivity.this).builder().setTitle(getString(R.string.child_partment_name));
                mNameEt = dialog.setEditer();
                mNameEt.setSelection(mNameEt.getText().length());
                dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cn = mNameEt.getText().toString().trim();
                        if (TextUtils.isEmpty(cn)) {
                            showToast(getString(R.string.input_name));
                            return;
                        }
                        String code = mCurrentCategories.getQueryParameter("code");
                        updatedepartnameAction(1, code, "", cn, "");
                    }
                }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.btn_setting:
                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(OrganizationVanCloudActivity.this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);
                actionSheetDialog.addSheetItem(getString(R.string.edit_partment), ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                View childView = mLuncherLayout.getChildAt(mLuncherLayout.getChildCount() - 1);
                                TextView childLableTv = (TextView) childView.findViewById(R.id.tv_lable);
                                String lable = childLableTv.getText().toString();
                                AlertDialog dialog = new AlertDialog(OrganizationVanCloudActivity.this).builder().setTitle(getString(R.string.partment_name));
                                mNameEt = dialog.setEditer();
                                mNameEt.setSelection(mNameEt.getText().length());
                                mNameEt.setText(lable);
                                dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String cn = mNameEt.getText().toString().trim();
                                        if (TextUtils.isEmpty(cn)) {
                                            showToast(getString(R.string.input_name));
                                            return;
                                        }
                                        String code = mCurrentCategories.getQueryParameter("code");
                                        String pcodes = mCurrentCategories.getQueryParameter("pcodes");
                                        updatedepartnameAction(0, pcodes, code, cn, "");
                                    }
                                }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                            }
                        });
                if (mCategoryAdapter.getCount() == 0) {
                    actionSheetDialog.addSheetItem(getString(R.string.del_partment), ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    AlertDialog alertDialog = new AlertDialog(OrganizationVanCloudActivity.this)
                                            .builder().setTitle(getString(R.string.edit_department_title))
                                            .setMsg(getString(R.string.isdel_group_activity_toast));
                                    alertDialog.setPositiveButton(getString(R.string.yes), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String code = mCurrentCategories.getQueryParameter("code");
                                            String pcodes = mCurrentCategories.getQueryParameter("pcodes");
                                            deletedepartAction(pcodes, code);
                                        }
                                    });
                                    alertDialog.setNegativeButton(getString(R.string.no), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    });
                                    alertDialog.show();
                                }
                            });
                }
                actionSheetDialog.show();
                break;
            case R.id.tv_right:
                mEdit = !mEdit;
                if (mEdit) {
                    initRightTv(getString(R.string.cancel));
                    findViewById(R.id.other_action).setVisibility(View.VISIBLE);
                } else {
                    initRightTv(getString(R.string.edit));
                    findViewById(R.id.other_action).setVisibility(View.GONE);
                }

                break;
            case R.id.btn_action_search: {
                findViewById(R.id.tv_right).setVisibility(View.GONE);
                LinearLayout btn_back = (LinearLayout) findViewById(R.id.btn_back);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(0, 0, 0, 0);
                btn_back.setLayoutParams(lp);
                getTitleTv().setVisibility(View.GONE);
                searchBtn.setVisibility(View.GONE);
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
                            showLoadingView();
                            mSearchAdapter.clear();
                            List<User> userList = User.queryUserWithKeyWord(OrganizationVanCloudActivity.this, s);
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
                        }
                        return false;
                    }
                });
            }
            break;
            case R.id.tv_lable:
                v.setEnabled(false);
                int index = (int) v.getTag();
                int count = mLuncherLayout.getChildCount();
                if (index == 0 && count == 1)
                    return;
                previousCategories(index);
                for (int i = count - 1; i > index; i--) {
                    mLuncherLayout.removeViewAt(i);
                }
                View childView = mLuncherLayout.getChildAt(index);
                TextView childLableTv = (TextView) childView.findViewById(R.id.tv_lable);
                childLableTv.setTextColor(getResources().getColor(R.color.text_black));
                mLuncherScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLuncherScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * 删除组织结构部门
     */
    public void deletedepartAction(String parent_id, String department_id) {
        showLoadingDialog(this, getString(R.string.dataloading));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("parent_id", parent_id);
        params.put("department_id", department_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_DELETEDEPART), params, this);
        getAppliction().getNetworkManager().load(DELETEDEPART, path, this);
    }

    /**
     * 新增,修改组织机构部门
     */
    public void updatedepartnameAction(int form, String parent_id, String department_id, String departname, String changed_department_id) {
        showLoadingDialog(this, getString(R.string.dataloading));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("parent_id", parent_id);
        if (form == 0) { // 修改部门名称
            params.put("type", "0");
            params.put("department_id", department_id);
            params.put("departname", departname);
        }
        if (form == 1) { // 增加部门
            params.put("type", "1");
            params.put("departname", departname);
        }
        if (form == 2) { // 修改部门关系
            params.put("type", "2");
            params.put("department_id", department_id);
            params.put("changed_department_id", changed_department_id);
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_UPDATEDEPARTNAME), params, this);
        getAppliction().getNetworkManager().load(UPDATEDEPARTNAME, path, this);
    }

    private final int GET_INDUSDEPARTRINFO = 1;
    private final int DELETEDEPART = 2;
    private final int UPDATEDEPARTNAME = 3;

    @Override
    public void finish() {
        if (mSearchTv != null) {
            closeSearch();
            return;
        }
        super.finish();
    }

    private void closeSearch() {
        if (mSearchTv != null) {
            if (mHasGroupEditPermisstion)
                findViewById(R.id.tv_right).setVisibility(View.VISIBLE);
            final SearchView searchView = (SearchView) findViewById(R.id.searchview);
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(
                    mSearchTv.getWindowToken(), 0);
            searchView.setVisibility(View.GONE);
            searchBtn.setVisibility(View.VISIBLE);
            getTitleTv().setVisibility(View.VISIBLE);
            LinearLayout btn_back = (LinearLayout) findViewById(R.id.btn_back);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, Utils.convertDipOrPx(OrganizationVanCloudActivity.this, 50), 0);
            btn_back.setLayoutParams(lp);

            findViewById(R.id.searchuser_view).setVisibility(View.GONE);
            findViewById(R.id.viewFlipper).setVisibility(View.VISIBLE);
            findViewById(R.id.luncher_scrollview).setVisibility(View.VISIBLE);
            mSearchTv = null;
        }

    }

    private void setCatTitle(Organization organization) {
        if (mLuncherLayout.getChildCount() == 0) {
            View view = getLayoutInflater().inflate(R.layout.luncher_text, null);
            view.findViewById(R.id.ic_arrow).setVisibility(View.GONE);
            TextView lableTv = (TextView) view.findViewById(R.id.tv_lable);
            lableTv.setTag(0);
            lableTv.setText(TenantPresenter.getCurrentTenant(this).tenant_name);
            mLuncherLayout.addView(view);
            mLuncherScrollView.setVisibility(View.VISIBLE);
        }
        if(organization==null)
            return;;
        View view = getLayoutInflater().inflate(R.layout.luncher_text, null);
        TextView lableTv = (TextView) view.findViewById(R.id.tv_lable);
        lableTv.setText(organization.label);
        lableTv.setTag(R.string.app_name, organization);
        lableTv.setTag(mLuncherLayout.getChildCount());
        mLuncherLayout.addView(view);
        for (int i = 0; i < mLuncherLayout.getChildCount() - 1; i++) {
            View childVIew = mLuncherLayout.getChildAt(i);
            TextView childLableTv = (TextView) childVIew.findViewById(R.id.tv_lable);
            childLableTv.setTextColor(getResources().getColor(R.color.bg_title));
            childLableTv.setOnClickListener(this);
            childLableTv.setEnabled(true);
        }
        mLuncherScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLuncherScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mCategoryFlipper.post(new Runnable() {
            @Override
            public void run() {
                if (mCategoryFlipper.getChildCount() > 1) {
                    mCategoryFlipper.setInAnimation(null);
                    mCategoryFlipper.setOutAnimation(null);
                    mCategoryFlipper.removeViewAt(0);
                }
            }

        });
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case DELETEDEPART: {
                int index = mLuncherLayout.getChildCount() - 2;
                previousCategories(index);
                String department_id = path.getPostValues().get("department_id");
                Node node = new Node();
                node.setId(department_id);
                nodes.remove(node);
                for (int i = 0; i < mCategoryAdapter.getCount(); i++) {
                    if (!mCategoryAdapter.getItem(i).isUser() && mCategoryAdapter.getItem(i).code.equals(path.getPostValues().get("department_id")))
                        mCategoryAdapter.remove(mCategoryAdapter.getItem(i));
                }
                mLuncherLayout.removeViewAt(index + 1);
                View childView = mLuncherLayout.getChildAt(index);
                TextView childLableTv = (TextView) childView.findViewById(R.id.tv_lable);
                childLableTv.setTextColor(getResources().getColor(R.color.text_black));
                childLableTv.setEnabled(false);
                mLuncherScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLuncherScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100);
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
            }
            break;
            case UPDATEDEPARTNAME:
                //修改成功
                String typeStr = path.getPostValues().get("type");
                switch (Integer.parseInt(typeStr)) {
                    case 0: {// 修改部门名称
                        String departname = path.getPostValues().get("departname");
                        View itemView = mLuncherLayout.getChildAt(mLuncherLayout.getChildCount() - 1);
                        TextView lableTv = (TextView) itemView.findViewById(R.id.tv_lable);
                        lableTv.setText(departname);
                        Organization organization = (Organization) lableTv.getTag(R.string.app_name);
                        organization.label = departname;
                        organization.node.setName(departname);
                        mCategoryAdapter.notifyDataSetChanged();
                        mLuncherScrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mLuncherScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                            }
                        }, 100);
                    }
                    break;
                    case 1:
                        try {// 添加部门
                            String department_id = rootData.getJson().getJSONObject("data").getString("department_id");
                            String departname = path.getPostValues().get("departname");
                            if (mLuncherLayout.getChildCount() == 0 || mLuncherLayout.getChildCount() == 1) {

                                Node node = new Node(department_id, mRootDepartId, departname, "", "", "", false, "");
                                node.type = 2;
                                node.setLevel(2);
                                nodes.add(node);
                                Organization newOrganization = new Organization("0", node.getId(), node.getName());
                                newOrganization.node = node;
                                newOrganization.level = "" + node.getLevel();
                                newOrganization.pcodes = node.getpId();
                                int index = -1;
                                for (int i = 0; i < mCategoryAdapter.getCount(); i++) {
                                    Organization org = mCategoryAdapter.getItem(i);
                                    if (org.isUser()) {
                                        index = i;
                                        break;
                                    }
                                }
                                if (index == -1)
                                    index = mCategoryAdapter.getCount();
                                mCategoryAdapter.getData().add(index, newOrganization);
                                mCategoryAdapter.notifyDataSetChanged();
                            } else {
                                View itemView = mLuncherLayout.getChildAt(mLuncherLayout.getChildCount() - 1);
                                TextView lableTv = (TextView) itemView.findViewById(R.id.tv_lable);
                                Organization organization = (Organization) lableTv.getTag(R.string.app_name);
                                Node node = new Node(department_id, organization.node.getId(), departname, "", "", "", false, "");
                                node.type = 2;
                                node.setLevel(organization.node.getLevel() + 1);
                                nodes.add(node);
                                Organization newOrganization = new Organization("0", node.getId(), node.getName());
                                newOrganization.node = node;
                                newOrganization.level = "" + node.getLevel();
                                newOrganization.pcodes = node.getpId();
                                int index = -1;
                                for (int i = 0; i < mCategoryAdapter.getCount(); i++) {
                                    Organization org = mCategoryAdapter.getItem(i);
                                    if (org.isUser()) {
                                        index = i;
                                        break;
                                    }
                                }
                                if (index == -1)
                                    index = mCategoryAdapter.getCount();
                                mCategoryAdapter.getData().add(index, newOrganization);
                                mCategoryAdapter.notifyDataSetChanged();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        break;
                }
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1002:
                String departId = data.getStringExtra("departId");
                if (!mUserOrganization.pcodes.equals(departId)) {
                    mUserOrganization.node.getParent().getChildren().remove(mUserOrganization.node);
                    for (Node node : nodes) {
                        if (node.getId().equals(departId)) {
                            mUserOrganization.node.setpId(departId);
                            mUserOrganization.node.setParent(node);
                            mUserOrganization.node.setLevel(node.getLevel() + 1);
                            node.getChildren().add(mUserOrganization.node);
                        }
                    }
                    mCategoryAdapter.remove(mUserOrganization);
                    mCategoryAdapter.notifyDataSetChanged();
                }
                break;
            case 1001:
                String name = data.getStringExtra("name");
                String id = data.getStringExtra("id");
                Node node = new Node(id, mRootDepartId, name, "", "", "", false, "");
                node.type = 2;
                node.isBranch = true;
                node.setLevel(2);
                nodes.add(node);
                Organization newOrganization = new Organization("0", node.getId(), node.getName());
                newOrganization.node = node;
                newOrganization.level = "" + node.getLevel();
                newOrganization.pcodes = node.getpId();
                int index = -1;
                for (int i = 0; i < mCategoryAdapter.getCount(); i++) {
                    Organization org = mCategoryAdapter.getItem(i);
                    if (org.isUser()) {
                        index = i;
                        break;
                    }
                }
                if (index != -1)
                    index = mCategoryAdapter.getCount();
                mCategoryAdapter.getData().add(index, newOrganization);
                mCategoryAdapter.notifyDataSetChanged();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    public static class CategoryInfo {
        public final Uri cat;
        public final CharSequence title;
        public final Parcelable state;
        public final OrganizationAdapter adapter;

        public CategoryInfo(Uri cat, CharSequence title, Parcelable state,
                            OrganizationAdapter adapter) {
            super();
            this.cat = cat;
            this.title = title;
            this.state = state;
            this.adapter = adapter;
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private Organization mUserOrganization;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Object object = parent.getItemAtPosition(position);
        if (object instanceof Organization) {
            closeSearch();
            Organization organization = (Organization) object;
            if (!organization.isUser())
                nextCategories(organization);
            else {
                mUserOrganization = organization;
                UserUtils.enterUserInfo(this, organization.staff_no, organization.staff_name, organization.photo);
            }
        }
    }

    private void nextCategories(Organization item) {
        // Uri oldUri = mCurrentCategories;
        ListView oldView = mCategoryListView;
        OrganizationAdapter oldAdapter = mCategoryAdapter;
        View oldCategoryWait = mCategoryWait;

        final LayoutInflater inflater = getLayoutInflater();
        ViewFlipper viewFlipper = mCategoryFlipper;

        ListView layout = (ListView) inflater.inflate(R.layout.organization_list,
                null);
        setupCategoryListView(inflater, layout);
        String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_ORGS);
        Uri uri = Uri.parse(url)
                .buildUpon()
                .appendQueryParameter("level", item.level)
                .appendQueryParameter("code", item.code)
                .appendQueryParameter("lable", item.label)
                .appendQueryParameter("pcodes", item.pcodes)
                .appendQueryParameter("isBranch", String.valueOf(item.isBranch ? 1 : 0))
                .build();
        boolean updated = updateCategories(uri, oldView, oldAdapter);
        if (updated) {
            setCatTitle(item);
            viewFlipper.addView(layout);
            viewFlipper.setInAnimation(mNextInAnimation);
            viewFlipper.setOutAnimation(mNextOutAnimation);
            viewFlipper.showNext();
        } else {
            mCategoryListView = oldView;
            mCategoryAdapter = oldAdapter;
            mCategoryWait = oldCategoryWait;
        }
    }

    private void previousCategories(int index) {
        final LayoutInflater inflater = getLayoutInflater();
        ViewFlipper viewFlipper = mCategoryFlipper;

        ListView layout = (ListView) inflater.inflate(R.layout.organization_list,
                null);
        setupCategoryListView(inflater, layout);
        final int size = mCategoryHistory.size();
        if (size > 0) {
            for (int i = mCategoryHistory.size() - 1; i > index; i--) {
                mCategoryHistory.remove(i);
            }
            CategoryInfo info = mCategoryHistory.remove(index);
            //  setCatTitle(info.title);
            mCategoryAdapter = info.adapter;
            mCurrentCategories = info.cat;
            layout.setAdapter(info.adapter);
            mCategoryListView.removeFooterView(mCategoryWait);
            layout.onRestoreInstanceState(info.state);
        } else {
            String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_ORGS);
            Uri uri = Uri.parse(url);
            updateCategories(uri, null, null);
            //  setCatTitle(getText(R.string.app_name));
        }

        viewFlipper.addView(layout);
        viewFlipper.setInAnimation(mPreviousInAnimation);
        viewFlipper.setOutAnimation(mPreviousOutAnimation);
        viewFlipper.showNext();
        initActionView();
    }
}
