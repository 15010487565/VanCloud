package com.vgtech.vancloud.ui.beidiao;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vic on 2016/10/14.
 */
public class BdStepTwoFragment extends BaseFragment implements HttpListener, AdapterView.OnItemClickListener {
    @Override
    protected int initLayoutId() {
        return R.layout.beidiao_step_two;
    }

    private TextView check_count_project;
    private TextView check_count_price;
    private CheckItemAdapter checkItemAdapter;

    private BdStepListener stepListener;

    public void setStepListener(BdStepListener stepListener) {
        this.stepListener = stepListener;
    }

    private boolean mAllCheck;
    private CheckBox mAllCheckBox;
    private String[] mProject;

    @Override
    protected void initView(View view) {
        mProject = new String[]{
                "身份信息核实",
                "国内学历信息",
                "民事诉讼/犯罪记录",
                "法院失信记录",
                "航空记录查询",
                "P2P行业黑名单",
                "互联网数据风险评估",
                "腾讯QQ",
                "网络行为数据分析",
                "消费数据分析",
                "在逃人员查询",
                "国外学历验证"
        };
        view.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        mAllCheckBox = (CheckBox) view.findViewById(R.id.cb_all);
        mAllCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAllCheck = !mAllCheck;
                mAllCheckBox.setChecked(mAllCheck);
                if (mAllCheck) {
                    selectedListener.getSeleced().clear();
                    selectedListener.add(checkItemAdapter.getData());
                    checkItemAdapter.notifyDataSetChanged();
                } else {
                    selectedListener.getSeleced().clear();
                    selectedListener.add(new CheckItem(1001,mProject[0], 40));
                    checkItemAdapter.notifyDataSetChanged();
                }
            }
        });
        check_count_project = (TextView) view.findViewById(R.id.check_count_project);
        check_count_price = (TextView) view.findViewById(R.id.check_count_price);
        ListView listView = (ListView) view.findViewById(R.id.listview);
        checkItemAdapter = new CheckItemAdapter(getActivity(), selectedListener);
        checkItemAdapter.getData().add(new CheckItem(1001, mProject[0], 40));
        checkItemAdapter.getData().add(new CheckItem(1002, mProject[1], 20));
        checkItemAdapter.getData().add(new CheckItem(1003, mProject[2], 100));
        checkItemAdapter.getData().add(new CheckItem(1004, mProject[3], 20));
        checkItemAdapter.getData().add(new CheckItem(1005, mProject[4], 150));
        checkItemAdapter.getData().add(new CheckItem(1006, mProject[5], 20));
        checkItemAdapter.getData().add(new CheckItem(1021, mProject[6], 20));
        checkItemAdapter.getData().add(new CheckItem(1023, mProject[7], 20));
        checkItemAdapter.getData().add(new CheckItem(1025, mProject[8], 50));
        checkItemAdapter.getData().add(new CheckItem(1029, mProject[9], 100));
        checkItemAdapter.getData().add(new CheckItem(1031, mProject[10], 50));
        checkItemAdapter.getData().add(new CheckItem(1033, mProject[11], 15));
        listView.setOnItemClickListener(this);
        listView.setAdapter(checkItemAdapter);
    }

    private CheckItemSelectedListener selectedListener = new CheckItemSelectedListener() {
        List<CheckItem> selectedList = new ArrayList<>();
        List<CheckItem> unSelectedList = new ArrayList<>();

        @Override
        public int getSelectMode() {
            return SELECT_MULTI;
        }

        @Override
        public List<CheckItem> getSeleced() {
            return selectedList;
        }

        @Override
        public List<CheckItem> getUnSeleced() {
            return unSelectedList;
        }

        @Override
        public void addUnSelected(List<CheckItem> list) {
            unSelectedList.addAll(list);
        }

        @Override
        public void addUnSelected(CheckItem checkItem) {
            unSelectedList.add(checkItem);
        }

        @Override
        public void add(CheckItem CheckItem) {
            if (!selectedList.contains(CheckItem))
                selectedList.add(CheckItem);
            for (CheckItem checkItem : unSelectedList)
                selectedList.remove(checkItem);
            selected(selectedList);
        }

        @Override
        public void remove(CheckItem CheckItem) {
            selectedList.remove(CheckItem);
            selected(selectedList);
        }

        @Override
        public boolean contains(CheckItem CheckItem) {
            boolean contains = selectedList.contains(CheckItem);
            return contains;
        }

        @Override
        public void add(List<CheckItem> CheckItems) {
            for (CheckItem CheckItem : CheckItems)
                add(CheckItem);
            selected(selectedList);
        }

        @Override
        public void remove(List<CheckItem> CheckItems) {
            for (CheckItem CheckItem : CheckItems)
                remove(CheckItem);
            selected(selectedList);
        }
    };

    public void selected(List<CheckItem> checkItems) {
        if (checkItems.size() == mSelectAllSize) {
            mAllCheck = true;
            mAllCheckBox.setChecked(mAllCheck);
        } else {
            mAllCheck = false;
            mAllCheckBox.setChecked(mAllCheck);
        }
        int price = 0;
        for (CheckItem checkItem : checkItems) {
            price += checkItem.price;
        }
        check_count_project.setText("已选择调查项目："+checkItems.size()+"项");
        check_count_price.setText("总价："+price);
    }

    private Map<String, String> mPostParams;
    private int mSelectAllSize;

    public void setParams(Map<String, String> params) {
        selectedListener.getSeleced().clear();
        selectedListener.getUnSeleced().clear();
        mPostParams = params;
        if (!TextUtils.isEmpty(mPostParams.get("qqNumber"))) { //以下中文无需翻译
            selectedListener.add(new CheckItem(1023, "腾讯QQ", 20));
            selectedListener.add(new CheckItem(1025, "网络行为数据分析", 50));
        } else {
            selectedListener.addUnSelected(new CheckItem(1023, "腾讯QQ", 20));
            selectedListener.addUnSelected(new CheckItem(1025, "网络行为数据分析", 50));
        }
        if (!TextUtils.isEmpty(mPostParams.get("unionPayCard"))) {
            selectedListener.add(new CheckItem(1029, "消费数据分析", 100));
        } else {
            selectedListener.addUnSelected(new CheckItem(1029, "消费数据分析", 100));
        }

        String one = mPostParams.get("educationCodeFir");
        String two = mPostParams.get("educationCodeSec");
        String three = mPostParams.get("educationCodeThi");
        if (!TextUtils.isEmpty(one) && !TextUtils.isEmpty(two) && !TextUtils.isEmpty(three)) {
            selectedListener.add(new CheckItem(1033, "国外学历验证", 15));
        } else {
            selectedListener.addUnSelected(new CheckItem(1033, "国外学历验证", 15));
        }
        if (!TextUtils.isEmpty(mPostParams.get("idCard"))) {
            selectedListener.add(new CheckItem(1001, "身份信息核实", 40));
            selectedListener.add(new CheckItem(1002, "国内学历信息", 20));
            selectedListener.add(new CheckItem(1003, "民事诉讼/犯罪记录", 100));
            selectedListener.add(new CheckItem(1004, "法院失信记录", 20));
            selectedListener.add(new CheckItem(1005, "航空记录查询", 150));
            selectedListener.add(new CheckItem(1006, "P2P行业黑名单", 20));
            selectedListener.add(new CheckItem(1021, "互联网数据风险评估", 20));
            selectedListener.add(new CheckItem(1031, "在逃人员查询", 50));
        }
        mSelectAllSize = selectedListener.getSeleced().size();
        mAllCheck = true;
        mAllCheckBox.setChecked(mAllCheck);
        checkItemAdapter.notifyDataSetChanged();
    }

    private static final int CALLBACK_CHECK = 1;
    private static final int CALLBACK_TOPAYMENT = 2;

    public void submit() {
        showLoadingDialog(getActivity(),"");
        Map<String, String> postValues = new HashMap<>();
        StringBuffer stringBuffer = new StringBuffer();
        for (CheckItem checkItem : selectedListener.getSeleced()) {
            stringBuffer.append(checkItem.id).append(",");
        }
        String itemIds = stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString();
        postValues.putAll(mPostParams);
        postValues.put("items", itemIds);
        postValues.put("tenantId", PrfUtils.getTenantId(getActivity()));
        postValues.put("userId", PrfUtils.getUserId(getActivity()));
        String url = ApiUtils.generatorUrl(getActivity(), URLAddr.URL_INVESTIGATES_CHECK);
        NetworkPath path = new NetworkPath(url, postValues, getActivity());
        getApplication().getNetworkManager().load(CALLBACK_CHECK, path, this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_CHECK:
                if (rootData.code == 1) {
                    new AlertDialog(getActivity()).builder().setTitle(getString(R.string.prompt))
                            .setMsg(rootData.msg)
                            .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                } else if (rootData.code == 2 || rootData.code == 3) {
                    new AlertDialog(getActivity()).builder().setTitle(getString(R.string.prompt))
                            .setMsg(rootData.msg)
                            .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    toPayMent();
                                }
                            }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                } else {
                    toPayMent();
                }
                break;
            case CALLBACK_TOPAYMENT:
                try {
                    JSONObject resultObject = rootData.getJson().getJSONObject("data");
                    String orderId = resultObject.getString("order_id");
                    String description = resultObject.getString("order_name");
                    String amount = resultObject.getString("amount");
                    String order_type = resultObject.getString("order_type");
                    ActivityUtils.toPay(this, orderId, description, amount, order_type, -1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200) {
            //resultCode -1支付成功，-2支付失败。
            if (resultCode == Activity.RESULT_OK) {
                stepListener.stepTwo();
            }
        }
    }

    private void toPayMent() {
        Map<String, String> postValues = new HashMap<>();
        StringBuffer stringBuffer = new StringBuffer();
        for (CheckItem checkItem : selectedListener.getSeleced()) {
            stringBuffer.append(checkItem.id).append(",");
        }
        String itemIds = stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString();
        postValues.putAll(mPostParams);
        postValues.put("items", itemIds);
        postValues.put("tenantId", PrfUtils.getTenantId(getActivity()));
        postValues.put("userId", PrfUtils.getUserId(getActivity()));
        String url = ApiUtils.generatorUrl(getActivity(), URLAddr.URL_INVESTIGATES_TO_PAYMENT);
        NetworkPath path = new NetworkPath(url, postValues, getActivity());
        getApplication().getNetworkManager().load(CALLBACK_TOPAYMENT, path, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof CheckItem) {
            CheckItem checkItem = (CheckItem) obj;
            if (checkItem.id == 1001)
                return;
            if (selectedListener.contains(checkItem)) {
                selectedListener.remove(checkItem);
            } else {
                selectedListener.add(checkItem);
            }
            checkItemAdapter.notifyDataSetChanged();
        }
    }
}
