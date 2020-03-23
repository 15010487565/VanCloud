package com.vgtech.vantop.ui.salary;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.utils.Des3Util;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.SalaryProjectAdapter;
import com.vgtech.vantop.moudle.SalaryItemChildData;
import com.vgtech.vantop.moudle.SalaryItemData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工资项目查询
 * Created by scott on 2016/7/14.
 */
public class SalaryProjectQueryFragment extends BaseFragment implements HttpListener, OnVerifyPswListenner {

    private String mPsw;
    private String mYear;
    private String mItem;
    private ListView mListView;
    private TextView mTvContent;
    private List<SalaryItemData> mDatas;
    private SalaryProjectAdapter mAdapter;
    private List<SalaryItemChildData> mListData;
    private TextView mTvTtitle;


    private final int CALLBACK_LOADDATA = 0X001;
    private final String TAG = "SalaryProject";

    @Override
    protected int initLayoutId() {
        return R.layout.salary_project_fragment;
    }

    @Override
    protected void initView(View view) {

        mListView = (ListView) view.findViewById(R.id.list_project);
        mListData = new ArrayList<>();
        mAdapter = new SalaryProjectAdapter(getActivity(), mListData);
        mListView.setAdapter(mAdapter);
        mTvContent = (TextView) view.findViewById(R.id.tv_content);
        mTvTtitle = (TextView) view.findViewById(R.id.tv_title);
        showLoadingDialog(getActivity(), "", false);
    }

    @Override
    protected void initData() {

        Bundle bundle = getArguments();
        mPsw = bundle.getString(SalaryMainActivity.BUNDLE_PSW, "");
        mYear = bundle.getString(SalaryMainActivity.BUNDLE_YEAR, "");
        mItem = bundle.getString(SalaryMainActivity.BUNDLE_ITEM, "");
        mDatas = new ArrayList<>();
        loadData();
    }

    /***
     * 加载数据
     */
    private void loadData() {

        String path = VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_SALARY_YEAR_ITEM_PRICE);
//        String path = "http://192.168.1.129:8080/vantopapp/"+UrlAddr.URL_SALARY_YEAR_ITEM_PRICE;
        Map<String, String> params = new HashMap<>();
        params.put("y", mYear);
        params.put("password", mPsw);
        params.put("loginUserCode", PrfUtils.getStaff_no(getActivity()));
        params.put("token", PrfUtils.getToken(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        String vsign = ApiUtils.getSign(params, mPsw);
        params.put("vsign", vsign);
        NetworkPath np = new NetworkPath(path, params, getActivity(), true);
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

    private void onLoadFinished(RootData rootData) {

        String dataStr = rootData.getJson().optString("data");
        String staffNo = PrfUtils.getStaff_no(getActivity());
        Des3Util des3Util = new Des3Util();
        des3Util.setSecretKey(staffNo);
        try {
            if (TextUtils.isEmpty(dataStr))
                dataStr = "";
            String decData = des3Util.decode(dataStr);
            JSONArray jArr = new JSONArray(decData);
            if (jArr == null || jArr.length() == 0) {
                mTvTtitle.setVisibility(View.INVISIBLE);
                dismisLoadingDialog();
                String msg = rootData.getMsg();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.vantop_nosalaries);
                }
                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
                return;
            }
            mDatas = JsonDataFactory.getDataArray(SalaryItemData.class, jArr);
            for (int i = 0; i < mDatas.size(); i++) {
                JSONArray jArr2 = jArr.optJSONObject(i).optJSONArray("items");
                List<SalaryItemChildData> datas = JsonDataFactory.getDataArray(SalaryItemChildData.class, jArr2);
                mDatas.get(i).items.addAll(datas);
            }
            if (mDatas.isEmpty()) {
                String msg = rootData.getMsg();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.vantop_nosalaries);
                }
                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
            }
            dismisLoadingDialog();
            showData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showData() {

        int index = -1;
        for (int i = 0; i < mDatas.size(); i++) {
            SalaryItemData data = mDatas.get(i);
            if (TextUtils.equals(data.itemId, mItem)) {
                index = i;
                break;
            }
        }
        //如果选中的标签没有数据则显示第一项数据
        if (index == -1) {
            try {
                index = Integer.parseInt(mDatas.get(0).itemId);
            } catch (Exception e) {
                index = 1;
            }
        }
        SalaryItemData data = mDatas.get(index);
        mListData.clear();
        mListData.addAll(data.items);
        float sum = 0;
        for (int j = 0; j < data.items.size(); j++) {
            sum += Float.parseFloat(data.items.get(j).value);
        }
        mTvContent.setText(data.label);
        mTvTtitle.setText(mYear + getString(R.string.date_year) + " " + data.label + " " + getString(R.string.vantop_allsalaries) + sum +
                " \n" + data.label + " " + getString(R.string.vantop_times) + getString(R.string.vantop_number) + ": " + data.items.size());
        mAdapter.notifyDataSetChanged();
        ((SalaryMainActivity) getActivity()).showEmptyView(false);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }

    @Override
    public String[] onVerifyFinished() {
        SalaryMainActivity act = (SalaryMainActivity) getActivity();
        String[] params = new String[3];
        params[0] = SalaryMainActivity.FRAGMENT_TYPE_DATE;
        params[1] = act.mDates.get(act.mDates.size() - 1);
        params[2] = mItem;
        return params;
    }
}
