package com.vgtech.vantop.ui.salary;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.utils.Des3Util;
import com.vgtech.common.utils.HttpUtils;
import com.vgtech.common.utils.HttpView;
import com.vgtech.common.utils.ToastUtil;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.SalaryDateListAdapter;
import com.vgtech.vantop.moudle.SalaryDateData;
import com.vgtech.vantop.moudle.SalaryProjectItemData;
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
 * 工资日期查询
 * Created by shilec on 2016/9/9.
 */
public class SalaryDateFragment extends BaseFragment implements
//        HttpListener<String>,
        OnVerifyPswListenner
        , HttpView {

    public static final String BUNDLE_JSON = "jsonData";
    private ListView mListView;
    private TextView mTvTitle;
    private TextView mTvLabel;
    private String mDate;
    private String mPsw;
    private List<SalaryDateData> mDatas;
    private SalaryDateListAdapter mAdapter;
    private final int CALLBACK_LOADDATA = 0X001;
    private final String TAG = "SalaryDateFragment";
    private String mJson;
    private static Handler mHandler;
    private View mFooterView;
    private TextView mTvNoteContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        mHandler = new Handler(Looper.getMainLooper());
        Bundle bundle = getArguments();
        mPsw = bundle.getString(SalaryMainActivity.BUNDLE_PSW, "");
        mDate = bundle.getString(SalaryMainActivity.BUNDLE_DATE, "");
        mJson = bundle.getString(BUNDLE_JSON, "");
        ///Log.i(TAG, "创建!");
    }

    @Override
    protected int initLayoutId() {
        //Log.i(TAG, "创建!");
        return R.layout.salary_date_frgment;
    }

    @Override
    protected void initView(View view) {
        mDatas = new ArrayList<>();
        mAdapter = new SalaryDateListAdapter(getActivity(), mDatas);
        mListView = (ListView) view.findViewById(R.id.list_salary);
        mListView.setAdapter(mAdapter);
        mFooterView = LayoutInflater.from(this.getContext()).inflate(R.layout.layout_salary_date_footer_view, null);
        mTvNoteContent = (TextView) mFooterView.findViewById(R.id.salary_date_note_content);
        mListView.addFooterView(mFooterView);
        mFooterView.setVisibility(View.GONE);
        mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        mTvLabel = (TextView) view.findViewById(R.id.tv_label);
    }

    @Override
    protected void initData() {

        if (TextUtils.isEmpty(mJson)) {
            loadData();
        } else {
            try {
                RootData data = new RootData();
                data.setJson(new JSONObject(mJson));
                onLoadData(data);
                //showData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        String path = Uri.parse(VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_SALARY_QUERY)).toString();
        String staffNo = PrfUtils.getStaff_no(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("date", mDate);
        params.put("password", mPsw);
        params.put("loginUserCode", staffNo);
        params.put("token", PrfUtils.getToken(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        Log.e("TAG_工资","mPsw="+mPsw);
        String vsign = ApiUtils.getSign(params, mPsw);
        params.put("vsign", vsign);
        NetworkPath np = new NetworkPath(path, params, getActivity(), true);
        NetworkManager nm = getApplication().getNetworkManager();
//        nm.load(CALLBACK_LOADDATA, np, this);
        HttpUtils.load(getActivity(),CALLBACK_LOADDATA,np,this);
        showLoadingDialog(getActivity(), "", false);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

//    @Override
//    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
//
//        dismisLoadingDialog();
//        switch (callbackId) {
//            case CALLBACK_LOADDATA: {
//                if (rootData.getJson() != null) {
//                    if (rootData.getJson().has("code") && rootData.getJson().optInt("code") == -3) {
//                        ((SalaryMainActivity) getActivity()).showVerifyDialog();
//                        ((SalaryMainActivity) getActivity()).setOnVerifyListenner(this);
//                    } else if (rootData.getJson().has("_code") && rootData.getJson().optInt("_code") == -3) {
//                        ((SalaryMainActivity) getActivity()).showVerifyDialog();
//                        ((SalaryMainActivity) getActivity()).setOnVerifyListenner(this);
//                    } else {
//                        boolean safe = VanTopActivityUtils.prehandleNetworkData1(getActivity(), this, callbackId, path, rootData, true);
//                        if (!safe) {
//                            String msg = rootData.getMsg();
//                            if (TextUtils.isEmpty(msg)) {
//                                msg = getString(R.string.vantop_nosalaries);
//                            }
//                            final String finalMsg = msg;
//
//                            if (mHandler != null) {
//                                mHandler.postDelayed(new Runnable() {
//
//                                    @Override
//                                    public void run() {
//                                        ((SalaryMainActivity) getActivity()).showEmptyView(finalMsg, true);
//                                    }
//                                }, 100);
//                            } else {
//                                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
//                            }
//                            return;
//                        }
//                        onLoadData(rootData);
//                        //showData();
//                    }
//                } else {
//                    if (mHandler != null) {
//                        mHandler.postDelayed(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                ((SalaryMainActivity) getActivity()).showErrorView();
//                            }
//                        }, 100);
//                    } else {
//                        ((SalaryMainActivity) getActivity()).showErrorView();
//                    }
//                }
//            }
//            break;
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private void showData() {

        mAdapter.notifyDataSetChanged();
        if (mDatas != null && !mDatas.isEmpty()) {
            mTvTitle.setText(mDatas.get(0).month);
            if (!TextUtils.isEmpty(mDatas.get(0).remark)) {
                mFooterView.setVisibility(View.VISIBLE);
                mTvNoteContent.setText(mDatas.get(0).remark);
            }
        }
        ((SalaryMainActivity) getActivity()).showEmptyView(false);
        dismisLoadingDialog();
    }

    private void onLoadData(RootData rootData) {
        //Log.i(TAG, "数据:" + rootData.getJson().toString());
        String dataStr = rootData.getJson().optString("data");
        String staffNo = PrfUtils.getStaff_no(getActivity());
        Des3Util des3Util = new Des3Util();
        des3Util.setSecretKey(staffNo);
        try {
            if (TextUtils.isEmpty(dataStr))
                dataStr = "";
            String decData = des3Util.decode(dataStr);
            Log.e("TAG_工资","decData="+decData);
            JSONArray jArr = new JSONArray(decData);
            if (jArr == null || jArr.length() == 0) {
                mTvTitle.setVisibility(View.INVISIBLE);
                mTvLabel.setVisibility(View.INVISIBLE);
                dismisLoadingDialog();
                String msg = rootData.getMsg();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.vantop_nosalaries);
                }
                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
                return;
            }
            for (int i = 0; i < jArr.length(); i++) {
                SalaryDateData data = new SalaryDateData();
                JSONObject obj = jArr.optJSONObject(i);
                data.month = obj.optString("month") + " " + getString(R.string.vantop_salary_detail);
                data.remark = obj.optString("remark");
                List<SalaryProjectItemData> list = JsonDataFactory.getDataArray(SalaryProjectItemData.
                        class, obj.optJSONArray("items"));
//                SalaryProjectItemData data_1 = new SalaryProjectItemData();
//                data_1.label = "测试";
//                data_1.value = "0";
//                list.add(data_1);
                data.items.addAll(list);
                mDatas.add(data);
            }
            if (mDatas.isEmpty()) {
                String msg = rootData.getMsg();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.vantop_nosalaries);
                }
                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
                return;
            }
            showData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onErrorResponse(VolleyError error) {
//
//    }
//
//    @Override
//    public void onResponse(String response) {
//
//    }

    @Override
    public String[] onVerifyFinished() {
        //默认跳转首页
        SalaryMainActivity act = (SalaryMainActivity) getActivity();
        String[] params = new String[2];
        params[0] = SalaryMainActivity.FRAGMENT_TYPE_DATE;
        params[1] = act.mDates.get(act.mDates.size() - 1);
        return params;
    }

    @Override
    public void dataLoaded(int callbackId,NetworkPath path, RootData rootData) {
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
                        boolean safe = VanTopActivityUtils.prehandleNetworkData1(getActivity(), null, callbackId, path, rootData, true);
                        if (!safe) {
                            String msg = rootData.getMsg();
                            if (TextUtils.isEmpty(msg)) {
                                msg = getString(R.string.vantop_nosalaries);
                            }
                            final String finalMsg = msg;

                            if (mHandler != null) {
                                mHandler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        ((SalaryMainActivity) getActivity()).showEmptyView(finalMsg, true);
                                    }
                                }, 100);
                            } else {
                                ((SalaryMainActivity) getActivity()).showEmptyView(msg, true);
                            }
                            return;
                        }
                        onLoadData(rootData);
                        //showData();
                    }
                } else {
                    if (mHandler != null) {
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                ((SalaryMainActivity) getActivity()).showErrorView();
                            }
                        }, 100);
                    } else {
                        ((SalaryMainActivity) getActivity()).showErrorView();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onFailure(int callbackId, String data) {
        dismisLoadingDialog();
        ToastUtil.showToast(data);
    }
}
