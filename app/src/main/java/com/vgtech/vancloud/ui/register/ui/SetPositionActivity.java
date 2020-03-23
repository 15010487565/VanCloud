package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.api.Position;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.PrfUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jackson on 2015/10/21.
 * Version : 1
 * Details :
 */
public class SetPositionActivity extends BaseActivity implements HttpListener<String> {
    TextView tv_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.update_vocation));
        tv_right = initRightTv(getString(R.string.finish));
        tv_right.setEnabled(false);
        net();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right:
                if (getIntent().getBooleanExtra("get", false)) {
                    Intent data = new Intent();
                    data.putExtra("id", getCheckedPositionId());
                    data.putExtra("name", getCheckedPositionName());
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    submit();
                }
                break;
        }
    }

    /**
     * 提交选的职位
     */
    private void submit() {
        String positionIds = getCheckedPositionId();
        if (TextUtils.isEmpty(positionIds)) {
            finish();
        } else {
            showLoadingDialog(this, getString(R.string.loading2));
            VanCloudApplication app = (VanCloudApplication) getApplication();
            mNetworkManager = app.getNetworkManager();
            Map<String, String> params = new HashMap<>();

            SharedPreferences preferences = PrfUtils.getSharePreferences(this);
            params.put("userids", getIntent().getStringExtra("userIds"));//TODO 这里要通过上一个页面拿到参数
            params.put("ownid", preferences.getString("uid", ""));
            params.put("tenant_id", preferences.getString("tenantId", ""));
            params.put("position_id", positionIds);
            params.put("n", "1");
            params.put("s", "0");
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this,
                    URLAddr.URL_REGISTER_SET_NEW_STAFFS_POSITION), params, this);
            mNetworkManager.load(CALL_BACK_SET_NEW_STAFFS_POSITION, path, this);
        }
    }

    private String getCheckedPositionId() {
        //遍历所有被选中的角色，将其id存起来
        StringBuffer sb = new StringBuffer();
        if (mPositionList == null) return "";
        for (Position position : mPositionList) {
            if (position.isCheck) {
                sb.append(position.key);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String getCheckedPositionName() {
        //遍历所有被选中的角色，将其id存起来
        StringBuffer sb = new StringBuffer();
        if (mPositionList == null) return "";
        for (Position position : mPositionList) {
            if (position.isCheck) {
                sb.append(position.value);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_role;
   /*     <include
        android:id="@+id/title_bar"
        layout="@layout/title_bar" />*/
    }

    private final int CALL_BACK_GET_POSITION = 0;
    private final int CALL_BACK_SET_NEW_STAFFS_POSITION = 1;//TODO 这里该网络回调
    private NetworkManager mNetworkManager;

    /**
     * 获取企业职位
     */
    public void net() {
        showLoadingDialog(this,getString(R.string.loading2));
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        params.put("tenant_id", preferences.getString("tenantId", ""));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_REGISTER_GET_COMPANY_POSITION_INFO), params, this);
        mNetworkManager.load(CALL_BACK_GET_POSITION, path, this);
    }

    List<Position> mPositionList;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_GET_POSITION:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    //TODO 先这行注释 应该放开用来网络获取数据的
                    mPositionList = JsonDataFactory.getDataArray(Position.class, resutObject.getJSONArray("positions"));
                    if (mPositionList == null) mPositionList = new ArrayList<>();
                    String positionId = getIntent().getStringExtra("positionId");
                    if (!TextUtils.isEmpty(positionId)) {
                        for (int i = 0; i < mPositionList.size(); i++) {
                            Position position = mPositionList.get(i);
                            if (positionId.equals(position.key)) {
                                position.isCheck = true;
                                checkedPosition = i;
                            }
                        }
                    }
                    initListView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALL_BACK_SET_NEW_STAFFS_POSITION:
                // sendBroadcast(new Intent(GroupReceiver.REFRESH));
                finish();
                break;
        }
    }

    private int checkedPosition = -1;
    private PositionAdapter mAdapter;
    private boolean enable = false;

    private void initListView() {
        ListView lv_role = (ListView) findViewById(R.id.lv_role);
        if (mAdapter == null) {
            mAdapter = new PositionAdapter();
            lv_role.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        lv_role.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!enable) {
                    enable = !enable;
                    tv_right.setEnabled(enable);
                }
                //上一条的消失
                if (checkedPosition != -1) {
                    Position position1 = mPositionList.get(checkedPosition);
                    position1.isCheck = !position1.isCheck;
                }
                Position position2 = mPositionList.get(position);
                position2.isCheck = !position2.isCheck;
                BaseAdapter ap = (BaseAdapter) parent.getAdapter();
                ap.notifyDataSetChanged();
                checkedPosition = position;
            }
        });
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    class PositionAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mPositionList.size();
        }

        @Override
        public View getItem(int position) {
            return View.inflate(SetPositionActivity.this,
                    R.layout.item_role, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getItem(position);
                viewHolder = new ViewHolder();
                viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Position position1 = mPositionList.get(position);
            viewHolder.cb.setChecked(position1.isCheck);
            viewHolder.tv_name.setText(position1.value);
            return convertView;
        }
    }

    static class ViewHolder {
        private TextView tv_name;
        private CheckBox cb;
    }

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }
}
