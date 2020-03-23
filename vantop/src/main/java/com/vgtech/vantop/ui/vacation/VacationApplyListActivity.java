package com.vgtech.vantop.ui.vacation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.VacationApplyListAdapter;
import com.vgtech.vantop.moudle.VacationApply;
import com.vgtech.vantop.moudle.Vacations;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 假期审批列表
 * Created by Duke on 2016/9/18.
 */
public class VacationApplyListActivity extends BaseActivity implements HttpListener<String>, AdapterView.OnItemClickListener {

    private int type;//1批准0申请中2拒绝
    private ListView listView;
    private VancloudLoadingLayout loadingLayout;
    private NetworkManager mNetworkManager;
    private final int CALLBACK_LIST = 1;

    private VacationApplyListAdapter adapter;

    private Vacations vacation;

    @Override
    protected int getContentView() {
        return R.layout.vacation_apply_list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", -1);
        switch (type) {
            case 0:
                setTitle(getString(R.string.vantop_approving));
                break;
            case 1:
                setTitle(getString(R.string.vantop_apply_approved));
                break;
            case 2:
                setTitle(getString(R.string.vantop_denied_apply));
                break;
        }
        listView = (ListView) findViewById(R.id.list_view);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        initData();
    }


    public void intiView() {

        adapter = new VacationApplyListAdapter(this, new ArrayList<VacationApply>(), vacation);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void initData() {

        String jsonTxt = getIntent().getStringExtra("json");
        try {
            if (!TextUtils.isEmpty(jsonTxt)) {
                mNetworkManager = getApplicationProxy().getNetworkManager();
                vacation = JsonDataFactory.getData(Vacations.class, new JSONObject(jsonTxt));
                intiView();
                getData(vacation.code, type + "");

                loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
                    @Override
                    public void loadAgain() {
                        getData(vacation.code, type + "");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getData(String code, String type) {

        loadingLayout.showLoadingView(listView, "", true);
        String url = VanTopUtils.generatorUrl(VacationApplyListActivity.this, UrlAddr.URL_VACATIONS_BY_CODE_APPLIES);
        Map<String, String> params = new HashMap<String, String>();
        params.put("code", code);
        params.put("status", type);
        NetworkPath path = new NetworkPath(url, params, this, true);
        mNetworkManager.load(CALLBACK_LIST, path, this, false);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listView);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            loadingLayout.showErrorView(listView);
            return;
        }
        switch (callbackId) {
            case CALLBACK_LIST:

                List<VacationApply> list = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    list = JsonDataFactory.getDataArray(VacationApply.class, jsonObject.getJSONArray("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter == null) {
                    adapter = new VacationApplyListAdapter(this, list, vacation);
                    listView.setAdapter(adapter);
                } else {
                    adapter.myNotifyDataSetChanged(list);
                }
                if (adapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
                    listView.setVisibility(View.VISIBLE);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        VacationApply apply = adapter.getList().get(position);
        Intent intent = new Intent(VacationApplyListActivity.this, VacationApplyDetailsActivity.class);
        intent.putExtra("id", apply.task_id);
        intent.putExtra("position", position);
        startActivityForResult(intent, 300);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case 300: {
                int position = data.getIntExtra("position", -1);
                if (position >= 0) {
                    adapter.deleteItem(position);
                    if (adapter.getList().size() <= 0) {
                        loadingLayout.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
                        listView.setVisibility(View.VISIBLE);
                    }
                }
            }
            break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }
}
