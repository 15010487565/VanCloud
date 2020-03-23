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
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.StringUtils;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.OrganizationAdapter;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
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
 * vantop 组织架构
 * Created by vic on 2016/10/8.
 */
public class OrganizationActivity extends BaseActivity implements HttpListener, Animation.AnimationListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private static final int CALLBACK_ORGS = 1;
    private ViewFlipper mCategoryFlipper;
    private Animation mNextInAnimation;
    private Animation mNextOutAnimation;
    private Animation mPreviousInAnimation;
    private Animation mPreviousOutAnimation;
    private View mCategoryWait;
    private OrganizationAdapter mCategoryAdapter;
    private ListView mCategoryListView;
    private NetworkManager mNetworkManager;
    private LinearLayout mLuncherLayout;
    private HorizontalScrollView mLuncherScrollView;
    private View searchBtn;
    private TextView loadingMagView;
    private ProgressWheel loadingProgressBar;
    private LinearLayout loadingLayout;
    private ListView listView;
    private View nodetailview;
    private OrganizationAdapter mSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.organization));
        mNetworkManager = getAppliction().getNetworkManager();
        mCategoryHistory = new ArrayList<CategoryInfo>();
        mLuncherLayout = (LinearLayout) findViewById(R.id.luncher_layout);
        mLuncherScrollView = (HorizontalScrollView) findViewById(R.id.luncher_scrollview);
        searchBtn = findViewById(R.id.btn_action_search);
        searchBtn.setOnClickListener(this);

        loadingLayout = (LinearLayout) findViewById(R.id.loading);
        loadingMagView = (TextView) findViewById(R.id.loadding_msg);
        loadingProgressBar = (ProgressWheel) findViewById(R.id.progress_view);
        listView = (ListView) findViewById(R.id.user_list);
        nodetailview = findViewById(R.id.nodetailview);

        mSearchAdapter = new OrganizationAdapter(this);
        listView.setAdapter(mSearchAdapter);
        listView.setOnItemClickListener(this);
        setCatTitle(null);
        initView();
    }

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
        String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_ORGS);
        Uri uri = Uri.parse(url);
        updateCategories(uri, mCategoryListView, mCategoryAdapter);
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
        listView.setOnScrollListener(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_organization;
    }

    private HttpListener<String> mCategoryDataCallback = new HttpListener<String>() {
        @Override
        public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

            mSafe = VanTopActivityUtils.prehandleNetworkData(OrganizationActivity.this, this, callbackId, path, rootData, true);
            if (!mSafe) {
                showProgress(mCategoryWait, false);
                return;
            }
            switch (callbackId) {
                case CALLBACK_ORGS: {
                    JSONObject jsonObject = rootData.getJson();
                    try {
                        Uri uri = Uri.parse(path.getUrl());
                        String code = uri.getQueryParameter("code");
                        String level = uri.getQueryParameter("level");
                        String pcodes = uri.getQueryParameter("pcodes");
                        if (mCurrentCategories != null) {
                            String cur_code = mCurrentCategories.getQueryParameter("code");
                            String cur_level = mCurrentCategories.getQueryParameter("level");
                            String cur_pcodes = mCurrentCategories.getQueryParameter("pcodes");
                            String netParams = "" + code + level + pcodes;
                            String curParams = "" + cur_code + cur_level + cur_pcodes;
                            if (!netParams.equals(curParams))
                                return;
                        }
                        if (jsonObject.has("nodes") && jsonObject.get("nodes") instanceof JSONArray) {
                            JSONArray nodeArray = jsonObject.getJSONArray("nodes");
                            List<Organization> list = JsonDataFactory.getDataArray(Organization.class, nodeArray);
                            boolean end = false;
                            for (Organization ori : list) {
                                if (!TextUtils.isEmpty(code) && code.equals(ori.code) &&
                                        !TextUtils.isEmpty(level) && level.equals(ori.level) &&
                                        !TextUtils.isEmpty(pcodes) && pcodes.equals(ori.pcodes)) {
                                    end = true;
                                    break;
                                }
                            }
                            if (!end) {
                                mCategoryAdapter.addAllData(list);
                                mCategoryAdapter.notifyDataSetChanged();
                            }
                        }
                        if (jsonObject.has("staffs") && jsonObject.get("staffs") instanceof JSONArray) {
                            JSONArray nodeArray = jsonObject.getJSONArray("staffs");
                            List<Organization> list = JsonDataFactory.getDataArray(Organization.class, nodeArray);
                            mCategoryAdapter.addAllData(list);
                            mCategoryAdapter.notifyDataSetChanged();
                        }
                        if (jsonObject.has("hasStaffs") && jsonObject.get("hasStaffs") instanceof Boolean) {
                            mHasData = jsonObject.getBoolean("hasStaffs");
                        }
                        if (!mHasData) {
                            mCategoryListView.removeFooterView(mCategoryWait);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
    };
    private ArrayList<CategoryInfo> mCategoryHistory;
    private Uri mCurrentCategories;

    private Uri mUpdateUri;
    private boolean updateCategories(Uri uri, ListView oldListView,
                                     OrganizationAdapter oldAdapter) {
        if (uri == null
                || (mCurrentCategories != null && uri
                .compareTo(mCurrentCategories) == 0)) {
            return false;
        }

        mNetworkManager.cancle(mCategoryDataCallback);
        String cat = uri.getQueryParameter("code");
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
        mUpdateUri = mCurrentCategories;
        mCategoryWait.postDelayed(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("loginUserCode", PrfUtils.getStaff_no(OrganizationActivity.this));
                NetworkPath path = new NetworkPath(mUpdateUri.toString(), params, OrganizationActivity.this, true);
                mNetworkManager.load(CALLBACK_ORGS, path, mCategoryDataCallback);
            }
        }, 300);
        showProgress(mCategoryWait, true);
        return true;
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

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            addTempData(String.valueOf(callbackId), path);
            return;
        }
        switch (callbackId) {
            case 1:
                hideLoadingView();
                JSONObject jsonObject = rootData.getJson();
                try {
                    if (jsonObject.has("staffs") && jsonObject.get("staffs") instanceof JSONArray) {
                        JSONArray nodeArray = jsonObject.getJSONArray("staffs");
                        List<Organization> list = JsonDataFactory.getDataArray(Organization.class, nodeArray);
                        mSearchAdapter.clear();
                        if (list.isEmpty()) {
                            nodetailview.setVisibility(View.VISIBLE);
                        } else {
                            mSearchAdapter.addAllData(list);
                            mSearchAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private TextView mSearchTv;


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
                            s = StringUtils.sqlValidate(s);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("loginUserCode", PrfUtils.getStaff_no(OrganizationActivity.this));
                            params.put("q", s);
                            showLoadingView();
                            String url = VanTopUtils.generatorUrl(OrganizationActivity.this, UrlAddr.URL_ORGS_SEARCH);
                            NetworkPath path = new NetworkPath(url, params, OrganizationActivity.this, true);
                            mNetworkManager.load(1, path, OrganizationActivity.this);
                        }
                        return false;
                    }
                });
            }
            break;
            case R.id.tv_lable:
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

    @Override
    public void finish() {
        if (mSearchTv != null) {
            closeSearch();
            return;
        }
        if (mNetworkManager != null && mCategoryDataCallback != null) {
            mNetworkManager.cancle(mCategoryDataCallback);
        }
        super.finish();
    }

    private void closeSearch() {
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
            lp.setMargins(0, 0, Utils.convertDipOrPx(OrganizationActivity.this, 50), 0);
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
//            lableTv.setOnClickListener(this);
            lableTv.setText(TenantPresenter.getCurrentTenant(this).tenant_name);
            mLuncherLayout.addView(view);
            mLuncherScrollView.setVisibility(View.VISIBLE);
        }
        if (organization == null)
            return;
        View view = getLayoutInflater().inflate(R.layout.luncher_text, null);
        TextView lableTv = (TextView) view.findViewById(R.id.tv_lable);
        lableTv.setText(organization.label);
//        lableTv.setOnClickListener(this);
        lableTv.setTag(mLuncherLayout.getChildCount());
        mLuncherLayout.addView(view);
        for (int i = 0; i < mLuncherLayout.getChildCount() - 1; i++) {
            View childVIew = mLuncherLayout.getChildAt(i);
            TextView childLableTv = (TextView) childVIew.findViewById(R.id.tv_lable);
            childLableTv.setTextColor(getResources().getColor(R.color.bg_title));
            childLableTv.setOnClickListener(this);
        }
        mLuncherScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLuncherScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

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

    private boolean mHasData;
    private boolean mSafe;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        boolean mInScroll = scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
        boolean flag = false;
        if (mCurrentCategories != null) {
            if (!mCategoryAdapter.getData().isEmpty()) {
                Organization organization = mCategoryAdapter.getData().get(mCategoryAdapter.getCount() - 1);
                String lastStaffNo = mCurrentCategories.getQueryParameter("lastStaffNo");
                if (organization.isUser() && organization.staff_no.equals(lastStaffNo))
                    flag = true;
            }
        }
        if (!flag && mSafe && mHasData
                && view.getLastVisiblePosition() >= (view.getCount() - 2)) {
            showProgress(mCategoryWait, true);
            Organization organization = mCategoryAdapter.getData().get(mCategoryAdapter.getCount() - 1);
            mCategoryWait.setVisibility(View.VISIBLE);
            String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_ORGS_STAFFS);
            Uri uri = Uri.parse(url)
                    .buildUpon()
                    .appendQueryParameter("code", mCurrentCategories.getQueryParameter("code"))
                    .appendQueryParameter("pcodes", mCurrentCategories.getQueryParameter("pcodes"))
                    .appendQueryParameter("lastStaffNo", organization.staff_no)
                    .build();
            appendListData(uri);
        }
    }

    protected void appendListData(Uri uri) {
        if (uri == null)
            return;
        mCurrentCategories = uri;
        Map<String, String> params = new HashMap<String, String>();
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        NetworkPath path = new NetworkPath(mCurrentCategories.toString(), params, this, true);
        mNetworkManager.load(CALLBACK_ORGS, path, mCategoryDataCallback);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Object object = parent.getItemAtPosition(position);
        if (object instanceof Organization) {
            closeSearch();
            mHasData = false;
            Organization organization = (Organization) object;
            if (!organization.isUser()) {
                nextCategories(organization);
            } else {
                Intent intent = new Intent(this, VantopUserInfoActivity.class);
                intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, organization.staff_no);
                startActivity(intent);
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
        mNetworkManager.cancle(mCategoryDataCallback);
        final int size = mCategoryHistory.size();
        if (size > 0) {
            for (int i = mCategoryHistory.size() - 1; i > index; i--) {
                mCategoryHistory.remove(i);
            }
            if (index >= mCategoryHistory.size()) {
                finish();
                return;
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
    }
}
