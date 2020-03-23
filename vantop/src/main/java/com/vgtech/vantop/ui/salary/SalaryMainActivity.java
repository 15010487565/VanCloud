package com.vgtech.vantop.ui.salary;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.SalaryProjectData;
import com.vgtech.vantop.moudle.SalaryProjectItemData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.view.ArrowRectDrawable;
import com.vgtech.vantop.ui.view.SelectWindow;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工资
 * Created by scott on 2016/9/9.
 */
public class SalaryMainActivity extends BaseActivity implements HttpListener<String> {

    private ImageButton mBtnRight;
    private String mPsw;
    private final int CALLBACK_LOADDATE = 0X002;
    private final int CALLBACK_LOADPROJECTS = 0X003;
    private final int CALLBACK_LOADYEARS = 0X004;

    public static final String BUNDLE_PSW = "psw";
    public static final String BUNDLE_DATE = "date";
    public static final String BUNDLE_YEAR = "years";
    public static final String BUNDLE_ITEM = "items";

    public static final String FRAGMENT_TYPE_DATE = "0";//日期查询
    public static final String FRAGMENT_TYPE_ITEMYEAR = "1";//项目查询
    public static final String FRAGMENT_TYPE_YEAR = "2";//年份查询

    private final String TAG = "SalaryMainActivity";

    public List<String> mDates;
    public List<String> mYears;
    public SalaryProjectData mProjectDatas;
    private boolean mMenuVisiable = false;
    private TextView mTvDate;
    private TextView mTvItems;

    //menu
    private LinearLayout mLLMenu;
    private TextView mTvMenuDate;
    private TextView mTvMenuItem;
    private TextView mTvMenuYear;
    //    private TextView mTvMenuClose;
    private TextView mTvMenuExpand;
    private LinearLayout mLLcontainer;
    private EditText editText;//密码输入框
    private ImageView mIvMenuExpand;
    private VancloudLoadingLayout mLoadingView;
    private View mContentView;

    private Map<String, SelectWindow> mWds;
    private String mJson;//date fragment
    private OnVerifyPswListenner mLisenner;
    private View mVshadow;
    private PopupWindow popupWindow;
    private boolean mIsShowPwdDialog;

    public void setOnVerifyListenner(OnVerifyPswListenner l) {
        mLisenner = l;
    }

    public String getPsw() {
        return mPsw;
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_salary;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vantop_salary_main));
        if (getIntent() != null) {
            mIsShowPwdDialog = getIntent().getBooleanExtra("is_show_pwd_dialog", true);
        }
        initViews();
        initDatas();
        mWds = new HashMap<>();
    }


    private void initPopupWindow() {
        mLLMenu = (LinearLayout) findViewById(R.id.ll_menu);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc_popup_background_mtrl_mult);
//        mLLMenu.setBackgroundDrawable(new ArrowRectDrawable());
        mLLMenu.setBackgroundResource(R.drawable.abc_popup_background_mtrl_mult);


        mTvMenuDate = (TextView) findViewById(R.id.tv_menu_date);
        mTvMenuItem = (TextView) findViewById(R.id.tv_menu_item);
        mTvMenuYear = (TextView) findViewById(R.id.tv_menu_year);
//        mTvMenuClose = (TextView) findViewById(R.id.tv_menu_close);
        mTvMenuExpand = (TextView) findViewById(R.id.tv_expand_menu);
        mVshadow = findViewById(R.id.v_shadow);
        mVshadow.setOnClickListener(this);
        mLLcontainer = (LinearLayout) findViewById(R.id.ll_container_menu);
        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        mContentView = findViewById(R.id.ll_container);

        mTvMenuYear.setOnClickListener(this);
        mTvMenuDate.setOnClickListener(this);
        mTvMenuItem.setOnClickListener(this);
//        mTvMenuClose.setOnClickListener(this);
        mTvMenuExpand.setOnClickListener(this);

        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                //TODO
                initDatas();

            }
        });
    }

    private void expandMenu() {

        int height = mLLMenu.getHeight();
        final boolean isExpand = mLLMenu.getVisibility() == View.INVISIBLE ? false : true;
        int fromY = isExpand ? 0 : -height;
        int toY = isExpand ? -height : 0;

        TranslateAnimation tAnim = new TranslateAnimation(0, 0, fromY, toY);
        tAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (!isExpand) {
                    //mTvMenuExpand.setVisibility(View.INVISIBLE);
                    mLLMenu.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (isExpand) {
                    mLLMenu.setVisibility(View.INVISIBLE);
                    ///mTvMenuExpand.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        tAnim.setDuration(200);
        mLLMenu.startAnimation(tAnim);

//        float fromDegress = isExpand ? 180 : 0;
//        float toDegress = isExpand ? 360 : 180;
//        ObjectAnimator.ofFloat(mIvMenuExpand, "rotation", fromDegress, toDegress)
//                .setDuration(200)
//                .start();
    }

    private void initViews() {

        // mBtnRight = (ImageButton) findViewById(R.id.btn_right);
        //mBtnRight.setVisibility(View.VISIBLE);
        //mBtnRight.setImageResource(R.mipmap.type_arrow_down);
        //mBtnRight.setOnClickListener(this);

        mTvDate = (TextView) findViewById(R.id.tv_date);
        mTvItems = (TextView) findViewById(R.id.tv_items);
        mIvMenuExpand = (ImageView) findViewById(R.id.iv_expand_menu);
        mIvMenuExpand.setOnClickListener(this);
        initPopupWindow();
    }

    private void initDatas() {
        //初始化密码验证完成操作
        mLisenner = new OnVerifyPswListenner() {
            @Override
            public String[] onVerifyFinished() {
                String[] params = new String[3];
                //显示日期查询结果界面
                params[0] = FRAGMENT_TYPE_DATE;
                params[1] = "";
                return params;
            }
        };
        mDates = new ArrayList<>();
        mProjectDatas = new SalaryProjectData();
        mYears = new ArrayList<>();
        loadDates();
        loadProjects();
        loadYears();
        if (mIsShowPwdDialog) {
            showVerifyDialog();
        }else {
            String pwd = PrfUtils.getPrfparams(SalaryMainActivity.this, "password");
            mPsw = MD5.getMD5(pwd);
//            String[] params = mLisenner.onVerifyFinished();
//            onVerifiedOperation(params);
            showDateSearchFragment("",mPsw);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == mBtnRight) {
            //showPopupMenu();
        }

        if (v == mTvMenuDate) {
            closeAllWnd();
            showDatePicker();
            mJson = null;
        }

        if (v == mTvMenuItem) {
            closeAllWnd();
            showItemPicker();
        }

        if (v == mTvMenuYear) {
            closeAllWnd();
            showYearsPicker();
        }

      /*  if(mVshadow == v) {
            if(mVshadow.getVisibility() == View.VISIBLE) {
                mVshadow.setVisibility(View.GONE);
            }
        }*/

        expandMenu();
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        dismisLoadingDialog();
        mLoadingView.dismiss(mContentView);

        switch (callbackId) {
            case CALLBACK_LOADDATE: {
                boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (!safe) {
                    mLoadingView.showErrorView(mContentView);
                    return;
                }
                onLoadDateFinished(rootData);
            }

            break;
            case CALLBACK_LOADPROJECTS: {
                boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (!safe) {
                    mLoadingView.showErrorView(mContentView);
                    return;
                }
                onLoadProjectsFinished(rootData);
            }

            break;

            case CALLBACK_LOADYEARS: {
                boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (!safe) {
                    mLoadingView.showErrorView(mContentView);
                    return;
                }
                onLoadYearsFinished(rootData);
            }
            break;
        }
    }

    public void showEmptyView(String msg, boolean flag) {
        if (flag) {
            mLoadingView.showEmptyView(mContentView, msg, true, true);
        } else {
            mLoadingView.dismiss(mContentView);
        }
    }

    public void showEmptyView(boolean flag) {
        if (flag) {
            mLoadingView.showEmptyView(mContentView, getString(R.string.vantop_nosalaries), true, true);
        } else {
            mLoadingView.dismiss(mContentView);
        }
    }

    public void showErrorView() {
        mLoadingView.showErrorView(mContentView);
    }

    public void showVerifyDialog() {
        showAlertDialog(getString(R.string.vantop_verifypsw),//title
                new View.OnClickListener() {//positiveListener
                    @Override
                    public void onClick(View v) {
                        String cn = editText.getText().toString();
                        if (TextUtils.isEmpty(cn)) {
                            showToast(getString(R.string.vantop_please_input_pwd));
                            showVerifyDialog();
                            return;
                        }
                        String pwd = PrfUtils.getPrfparams(SalaryMainActivity.this, "password");
                        if (cn.equals(pwd)) {
                            mPsw = MD5.getMD5(pwd);
                            //密码验证完执行的操作
                            String[] params = mLisenner.onVerifyFinished();
                            onVerifiedOperation(params);
                        } else {
                            showToast(getString(R.string.vantop_pwd_error));
                            showVerifyDialog();
                        }
                    }
                }, new View.OnClickListener() {//negativeListener
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD | InputType.TYPE_CLASS_TEXT);
    }

    private void showAlertDialog(String title, View.OnClickListener positiveListener, View.OnClickListener negativeListener) {
        AlertDialog dialog = new AlertDialog(this).builder().setTitle(title);
        editText = dialog.setEditer();
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setHint(getString(R.string.vantop_please_input_pwd));
        dialog.setPositiveButton(getString(R.string.vantop_confirm), positiveListener)
                .setNegativeButton(getString(R.string.vantop_cancle), negativeListener).show();
        dialog.setCancelable(false);
    }

    //当密码验证通过执行的操作
    private void onVerifiedOperation(String[] params) {

        if (params == null || params.length == 0) {
            return;
        }
        //显示日期查询界面
        if (TextUtils.equals(FRAGMENT_TYPE_DATE, params[0])) {
            String date = params[1];
            showDateSearchFragment(date, mPsw);
        }
        //显示项目查询界面
        if (TextUtils.equals(FRAGMENT_TYPE_ITEMYEAR, params[0])) {
            String year = params[1];
            String item = params[2];
            showItemSearchFragment(year, item, mPsw);
        }
        //显示年份查询界面
        if (TextUtils.equals(FRAGMENT_TYPE_YEAR, params[0])) {
            String year = params[1];
            showYearsSearchFragment(year, mPsw);
        }
        dismisLoadingDialog();
    }

    private void showDateSearchFragment(String date, String psw) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_DATE, date);
        bundle.putString(BUNDLE_PSW, psw);
        boolean ret = true;
        try {
            if (mJson != null) {
                JSONObject jObj = new JSONObject(mJson);
                ret = jObj.isNull("data");
            } else {
                ret = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(mJson) && ret)
            bundle.putString(SalaryDateFragment.BUNDLE_JSON, mJson);
        SalaryDateFragment fragment1 = new SalaryDateFragment();
        fragment1.setArguments(bundle);
        showFragment(fragment1, SalaryDateFragment.class.getSimpleName());
    }

    private void showYearsSearchFragment(String year, String psw) {

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_PSW, psw);
        bundle.putString(BUNDLE_DATE, year);
        SalaryYearQueryFragment fragment = new SalaryYearQueryFragment();
        fragment.setArguments(bundle);
        showFragment(fragment, SalaryYearQueryFragment.class.getSimpleName());
    }

    private void showItemSearchFragment(String year, String item, String psw) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_PSW, psw);
        bundle.putString(BUNDLE_YEAR, year);
        bundle.putString(BUNDLE_ITEM, item);

        SalaryProjectQueryFragment fragment = new SalaryProjectQueryFragment();
        fragment.setArguments(bundle);
        showFragment(fragment, SalaryProjectQueryFragment.class.getSimpleName());
    }

    private void showFragment(Fragment fragment, String flag) {

        android.support.v4.app.FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
        tran.replace(R.id.ll_container, fragment, flag);
        tran.commit();
        Log.i(TAG, "SalaryActivity_showFragment");
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    //加载日期
    private void loadDates() {

        String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_SALARY_DATES);
        NetworkPath np = new NetworkPath(url, null, this, true);
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        nm.load(CALLBACK_LOADDATE, np, this);
    }

    //当日期加载完成调用
    private void onLoadDateFinished(RootData data) {
        Log.i(TAG, data.getJson().toString());
        JSONObject jObj = data.getJson();
        JSONArray jArr = jObj.optJSONArray("dates");
        if (jArr == null) {
            return;
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < jArr.length(); i++) {
            list.add(jArr.optString(i));
        }
        mDates.addAll(list);
        if (mDates.isEmpty()) {
//            mLoadingView.showEmptyView(mContentView, getString(R.string.vantop_nosalaries), true, true);
            mLoadingView.showEmptyView(mContentView, data.getMsg(), true, true);
        }
        dismisLoadingDialog();
        //默认显示最新数据
        //showDateSearchFragment(mDates.get(mDates.size() - 1), mPsw);
    }

    //加载项目
    private void loadProjects() {

        String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_SALARY_ITEMS);
        NetworkPath np = new NetworkPath(url, null, this, true);
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        nm.load(CALLBACK_LOADPROJECTS, np, this);
    }

    //当项目加载完成调用
    private void onLoadProjectsFinished(RootData data) {
        Log.i(TAG, data.getJson().toString());
        JSONObject jObj = data.getJson();
        JSONArray jArr = jObj.optJSONArray("items");
        if (jArr == null) {
            return;
        }
        List<SalaryProjectItemData> items = JsonDataFactory.
                getDataArray(SalaryProjectItemData.class, jArr);
        mProjectDatas.items.addAll(items);

        List<String> years = new ArrayList<>();
        jArr = jObj.optJSONArray("years");
        for (int i = 0; i < jArr.length(); i++) {
            years.add(jArr.optString(i));
        }
        mProjectDatas.years.addAll(years);
        if (mProjectDatas.years.isEmpty()) {
            mLoadingView.showEmptyView(mContentView, getString(R.string.vantop_nosalaries), true, true);
        }
    }

    //加载年份
    private void loadYears() {

        String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_SALARY_YEARS);
        Map<String, String> params = new HashMap<>();
        params.put("1", "2");
        NetworkPath np = new NetworkPath(url, params, this, true);
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        nm.load(CALLBACK_LOADYEARS, np, this);
    }

    //当年份加载完成调用
    private void onLoadYearsFinished(RootData data) {
        Log.i(TAG, data.getJson().toString());
        JSONObject jObj = data.getJson();
        JSONArray jArr = jObj.optJSONArray("data");
        if (jArr == null) {
            return;
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < jArr.length(); i++) {
            list.add(jArr.optString(i));
        }
        mYears.addAll(list);
        if (mYears.isEmpty()) {
            mLoadingView.showEmptyView(mContentView, getString(R.string.vantop_nosalaries), true, true);
        }
    }

    //根据日期搜索
    private void showDatePicker() {

        if (mDates == null || mDates.isEmpty()) {
            Toast.makeText(this, getString(R.string.vantop_nosalaries), Toast.LENGTH_SHORT).show();
            return;
        }
      /*  SelectWindow wd;
        if (mWds.get("date") != null) {
            wd = mWds.get("date");
        } else {
            wd = new SelectWindow(this, mDates);
            wd.setOnConfirmLisenner(new SelectWindow.OnConfirmListenner() {
                @Override
                public void onConfirm(String item, String item1, int index1, int index2) {
                    // Toast.makeText(SalaryMainActivity.this, "item:" + item + "" +
                    // ",item1:" + item1, Toast.LENGTH_LONG).show();
                    showDateSearchFragment(item, mPsw);
                }
            });
            mWds.put("date", wd);
        }
        //默认选中最新
        wd.setCurrentItem(mDates.size() - 1);
        wd.show(mTvDate);*/
        showSheetDialog(true, mDates);
    }

    private void showSheetDialog(boolean isDate, List<String> items) {
        ActionSheetDialog dialog = new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true);
        final List<String> itemss = items;
        final boolean isDaten = isDate;
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            dialog.addSheetItem(items.get(i), ActionSheetDialog.SheetItemColor.Blue,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            if (isDaten) {
                                showDateSearchFragment(itemss.get(index), mPsw);
                            } else {
                                showYearsSearchFragment(itemss.get(index), mPsw);
                            }
                        }
                    });
        }
        dialog.show();
    }

    //显示按年份搜索工资的对话框
    private void showYearsPicker() {

        if (mYears == null || mYears.isEmpty()) {
            Toast.makeText(this, getString(R.string.vantop_nosalaries), Toast.LENGTH_SHORT).show();
            return;
        }
      /*  SelectWindow wd;
        if (mWds.get("years") != null) {
            wd = mWds.get("years");
        } else {
            wd = new SelectWindow(this, mYears);
            wd.setOnConfirmLisenner(new SelectWindow.OnConfirmListenner() {
                @Override
                public void onConfirm(String item, String item1, int index1, int index2) {
                    //Toast.makeText(SalaryMainActivity.this, "item:" + item + "" +
                    //",item1:" + item1, Toast.LENGTH_LONG).show();
                    showYearsSearchFragment(item, mPsw);
                }
            });
            mWds.put("years", wd);
        }
        //默认最新年份
        wd.setCurrentItem(mYears.size() - 1);
        wd.show(mTvDate);*/
        showSheetDialog(false, mYears);
    }


    //显示按项目查询的对话框
    private void showItemPicker() {

        if (mProjectDatas == null || mProjectDatas.items == null || mProjectDatas.items.isEmpty()
                || mProjectDatas.years == null || mProjectDatas.years.isEmpty()) {
            Toast.makeText(this, getString(R.string.vantop_nosalaries), Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> items = new ArrayList<>();
        for (int i = 0; i < mProjectDatas.items.size(); i++) {
            items.add(mProjectDatas.items.get(i).label);
        }
        Collections.sort(mProjectDatas.years, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int sort = lhs.compareTo(rhs);
                return sort;
            }
        });
        SelectWindow wd;
        if (mWds.get("item") != null) {
            wd = mWds.get("item");
        } else {
            wd = new SelectWindow(this, items, mProjectDatas.years);
            wd.setOnConfirmLisenner(new SelectWindow.OnConfirmListenner() {
                @Override
                public void onConfirm(String item, String item1, int index1, int index2) {
                    //Toast.makeText(SalaryMainActivity.this, "item:" + item + "" +
                    // ",item1:" + item1, Toast.LENGTH_LONG).show();
                    showItemSearchFragment(item1, mProjectDatas.items.get(index1).value, mPsw);
                }
            });
            mWds.put("item", wd);
        }
        wd.setCurrentItem1(items.size() - 1);
        wd.getWindow().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mVshadow.setVisibility(View.GONE);
            }
        });
        wd.setCurrentItem2(mProjectDatas.years.size() - 1);
        wd.show(mTvDate);
        mVshadow.setVisibility(View.VISIBLE);
    }

    private void closeAllWnd() {

        for (String key : mWds.keySet()) {

            SelectWindow wd = mWds.get(key);
            wd.dismiss();
        }
    }
}
