package com.vgtech.vantop.ui.salary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.utils.Des3Util;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ViewPagerAdapter;
import com.vgtech.vantop.moudle.SalaryYearlyReportData;
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
 * 工资按年查询
 * Created by scott on 2016/7/14.
 */
public class SalaryYearQueryFragment extends BaseFragment implements HttpListener, ViewPager.OnPageChangeListener, OnVerifyPswListenner {

    private String mPsw;
    private String mYear;
    private String mYearlySalaries;
    private List<SalaryYearlyReportData> mDatas;

    private final int CALLBACK_LOADDATA = 0X001;
    private final String TAG = "SalaryYear";

    private ViewPager mVpger;
    private List<Fragment> mFragments;
    private ViewPagerAdapter mVpAdapter;
    private TextView mTvAllSalaries;
//    private TextView mTvTitle;
//    private ImageView mIvNext;
//    private ImageView mIvLast;
    private LinearLayout mLlContainer;

    @Override
    protected int initLayoutId() {
        return R.layout.salary_years_fragment;
    }

    @Override
    protected void initView(View view) {
        showLoadingDialog(getActivity(), "", false);
        mVpger = (ViewPager) view.findViewById(R.id.vp_salary);
        mTvAllSalaries = (TextView) view.findViewById(R.id.tv_salaryall);
//        mIvNext = (ImageView) view.findViewById(R.id.iv_next);
//        mIvLast = (ImageView) view.findViewById(R.id.iv_last);
//        mTvTitle = (TextView) view.findViewById(R.id.tv_title);
//        mIvNext.setOnClickListener(this);
//        mIvLast.setOnClickListener(this);
        mVpger.setOnPageChangeListener(this);
//        mTvTitle.setText(getString(R.string.vantop_yearsalarydefault));
        mLlContainer = (LinearLayout) view.findViewById(R.id.ll_container);
    }

    @Override
    protected void initData() {

        Bundle bundle = getArguments();
        mPsw = bundle.getString(SalaryMainActivity.BUNDLE_PSW, "");
        mYear = bundle.getString(SalaryMainActivity.BUNDLE_DATE, "");
        Log.i(TAG, "psw=" + mPsw + ",year:" + mYear);
        loadData();
    }

    /***
     * 加载数据
     */
    private void loadData() {

        String url = VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_SALARY_YEAR_PRICES);
//        String url = "http://192.168.1.129:8080/vantopapp/"+UrlAddr.URL_SALARY_YEAR_PRICES;
        Map<String, String> params = new HashMap<>();
        params.put("year", mYear);
        params.put("password", mPsw);
        params.put("loginUserCode", PrfUtils.getStaff_no(getActivity()));
        params.put("token", PrfUtils.getToken(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        String vsign = ApiUtils.getSign(params, mPsw);
        params.put("vsign", vsign);
        NetworkPath np = new NetworkPath(url, params, getActivity(), true);
        NetworkManager nm = getApplication().getNetworkManager();
        nm.load(CALLBACK_LOADDATA, np, this);

    }

    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        switch (callbackId) {

            case CALLBACK_LOADDATA: {
                if (rootData.getJson() != null) {
                    if (rootData.getJson().has("code") && rootData.getJson().optInt("code") == -3) {
                        ((SalaryMainActivity) getActivity()).showVerifyDialog();
                        ((SalaryMainActivity) getActivity()).setOnVerifyListenner(this);
                    } else if (rootData.getJson().has("_code") && rootData.getJson().optInt("_code") == -3) {
                        ((SalaryMainActivity) getActivity()).showVerifyDialog();
                        ((SalaryMainActivity) getActivity()).setOnVerifyListenner(this);
                    } else {
                        boolean safe = VanTopActivityUtils.prehandleNetworkData1(getActivity(), this, callbackId, path, rootData, true);
                        if (!safe) {
                            String msg = rootData.getMsg();
                            if (TextUtils.isEmpty(msg)) {
                                msg = getString(R.string.vantop_nosalaries);
                            }
                            ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
                            return;
                        }
                        onLoadFinished(rootData);
                    }
                } else
                    ((SalaryMainActivity) getActivity()).showErrorView();
            }
            break;
        }
    }

    private void initFragments() {
        Log.i(TAG, "fragments:" + mFragments);
        mFragments = new ArrayList<>();
        //页首为最后一页显示的数据
        //mFragments.add(getFragment(mDatas.size() - 2));
        //每次显示俩列数据
        for (int i = 0; i < mDatas.size() - 1; i += 2) {
            try {
                mFragments.add(getFragment(i));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.print(e.getMessage());
            }
        }
        //页末尾位第一页显示的数据
        //mFragments.add(getFragment(0));
        Log.i(TAG, "fragments:" + mFragments);
        mVpAdapter = new ViewPagerAdapter(getChildFragmentManager(), mFragments);
        mVpger.setAdapter(mVpAdapter);
        mVpger.setCurrentItem(0, false);
        mVpAdapter.notifyDataSetChanged();
    }

    private Fragment getFragment(int index) {
        ArrayList<SalaryYearlyReportData> data = new ArrayList<>();
        //添加俩行数据
        data.add(mDatas.get(index));
        data.add(mDatas.get(index + 1));
        Bundle bundle = new Bundle();
        bundle.putSerializable(SalaryYearQueryItemFragment.BUNDLE_DATAS, data);
        SalaryYearQueryItemFragment fragment = new SalaryYearQueryItemFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void onLoadFinished(RootData data) {

        //Log.i(TAG,"data=" + data.getJson().toString());
        String dataStr = data.getJson().optString("data");
        String staffNo = PrfUtils.getStaff_no(getActivity());
        Des3Util des3Util = new Des3Util();
        des3Util.setSecretKey(staffNo);
        try {
            if (TextUtils.isEmpty(dataStr))
                dataStr = "";
            String decData = des3Util.decode(dataStr);
            JSONObject jObj = new JSONObject(decData);
            if (jObj == null) {
//                mTvTitle.setVisibility(View.INVISIBLE);
                mLlContainer.setVisibility(View.INVISIBLE);
//                mIvLast.setVisibility(View.INVISIBLE);
//                mIvNext.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), getString(R.string.vantop_nosalaries), Toast.LENGTH_SHORT).show();
                dismisLoadingDialog();
                String msg = data.getMsg();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.vantop_nosalaries);
                }
                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
                return;
            }
            mYearlySalaries = jObj.optString("yearlySalaries");
            JSONArray jArr = jObj.optJSONArray("yearlyReport");
            mDatas = new ArrayList<>();

            for (int i = 0; i < jArr.length(); i++) {
                SalaryYearlyReportData rd = new SalaryYearlyReportData();
                JSONArray jArr2 = jArr.optJSONObject(i).optJSONArray("value");
                rd.id = jArr.optJSONObject(i).optInt("id") + "";
                rd.reportName = jArr.optJSONObject(i).optString("reportName");
                for (int j = 0; j < jArr2.length(); j++) {
                    rd.value.add(jArr2.optString(j));
                }
                mDatas.add(rd);
            }
            //不为偶数时最后一列增加一列空数据用于生成空列
            if (mDatas.size() % 2 != 0) {
                SalaryYearlyReportData rd = new SalaryYearlyReportData();
                rd.reportName = "";
                rd.value = new ArrayList<>();
                for (int i = 0; i < mDatas.get(mDatas.size() - 1).value.size(); i++) {
                    rd.value.add("");
                }
                mDatas.add(rd);
            }
            if (mDatas.isEmpty()) {
                String msg = data.getMsg();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.vantop_nosalaries);
                }
                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
                return;
            }
            Log.i(TAG, "mData:" + mDatas);
            mTvAllSalaries.setText(mYear + getString(R.string.vantop_allsalari_label) + mYearlySalaries);
//            mTvTitle.setText(mYear + getString(R.string.vantop_yearsalary));
            dismisLoadingDialog();
            ((SalaryMainActivity) getActivity()).showEmptyView(false);
            initFragments();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }

//    @Override
//    public void onClick(View v) {
//        super.onClick(v);
//
//        if (mVpger == null || mVpger.getAdapter() == null || mVpger.getAdapter().getCount() == 0) {
//            return;
//        }
////        if (v == mIvNext) {
////            //int index = mVpger.getCurrentItem() == mVpger.getChildCount()? 0 : mVpger.getCurrentItem() + 1;
////            if (mVpger.getCurrentItem() == mVpger.getAdapter().getCount() - 2) {
////                mVpger.setCurrentItem(1, false);
////            } else {
////                mVpger.setCurrentItem(mVpger.getCurrentItem() + 1, false);
////            }
////        }
////        if (v == mIvLast) {
////            if (mVpger.getCurrentItem() == 1) {
////                mVpger.setCurrentItem(mVpger.getAdapter().getCount() - 2, false);
////            } else {
////                mVpger.setCurrentItem(mVpger.getCurrentItem() - 1, false);
////            }
////        }
//    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //无限循环
//        if (position == 0) {
//            mVpger.setCurrentItem(mVpAdapter.getCount() - 2, false);
//        } else if (position == mVpAdapter.getCount() - 1) {
//            mVpger.setCurrentItem(1, false);
//        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public String[] onVerifyFinished() {
        String[] params = new String[2];
        params[0] = SalaryMainActivity.FRAGMENT_TYPE_DATE;
        params[1] = mYear;
        return params;
    }
}
