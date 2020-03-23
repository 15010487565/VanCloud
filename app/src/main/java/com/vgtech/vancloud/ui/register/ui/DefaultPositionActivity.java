package com.vgtech.vancloud.ui.register.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.PositionInfoDialogAdapter;
import com.vgtech.common.api.Indus;
import com.vgtech.common.api.Position;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.common.PrfUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by brook on 2015/10/19.
 */
public class DefaultPositionActivity extends BaseActivity implements HttpListener<String> {

    @InjectView(R.id.industry_tv)
    TextView industryTv;
    @InjectView(R.id.arrow)
    ImageView arrow;
    @InjectView(R.id.company_tv)
    TextView companyTv;
    private NetworkManager mNetworkManager;
    private ListView listView;
    private Button btn_default;
    private Button btn_own_setting;
    private String tenant_id;
    private long select_indus_id;
    private String user_id;
    private boolean isShow = false;
    private Dialog allMsg;
    private View allMsgView;
    private GridView gridview;
    private PositionInfoDialogAdapter adapter;
    private GroupAdapter groupsAdapter;
    private List<Position> showPositionList; //页面所要显示的职位信息
    private List<Indus> indusList;  //行业集合
    private List<List<Position>> indusPositionList; //所有行位的职位集合

    private static final int CALLBACK_GET_INDUS_POSITION = 1;
    private static final int CALLBACK_SET_POSITION = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initview();
        getDataFromNet();
        initEvent();
    }

    private void initEvent() {
        btn_default.setOnClickListener(this);
        btn_own_setting.setOnClickListener(this);
        companyTv.setText(new PreferencesController().getAccount().tenant_name);
    }

    private void getDataFromNet() {         //获取行业职位信息
        /*progressDialog = LoadingProgressDialog.createDialog(this, 0);
        progressDialog.show();*/
        showLoadingDialog(this, getString(R.string.loading));
        Map<String, String> params = new HashMap<String, String>();
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GET_INDUS_POSITION), params, this);
        mNetworkManager.load(CALLBACK_GET_INDUS_POSITION, path, this);
    }

    private void initview() {    //初始化页面
        ButterKnife.inject(this);
        setTitle(getString(R.string.company_info));
        groupsAdapter=new GroupAdapter();

        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Actions.ACTION_DEFAULT));

        Intent intent = getIntent();
        try {
            select_indus_id =Long.parseLong(intent.getStringExtra("indus_id"));
        } catch (Exception e){
            e.printStackTrace();
        }



        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        user_id = preferences.getString("uid", "");
        tenant_id = preferences.getString("tenantId", "");

        listView = (ListView) findViewById(android.R.id.list);
        mNetworkManager = getAppliction().getNetworkManager();
        btn_default = (Button) this.findViewById(R.id.btn_default);
        btn_own_setting = (Button) this.findViewById(R.id.btn_own_setting);

        allMsgView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.view_department_dialog, null);
        allMsg = new AlertDialog.Builder(this).create();
        allMsg.setCanceledOnTouchOutside(false);
        gridview = (GridView) allMsgView.findViewById(R.id.grid_view);

    }


    /**
     * 选择行业
     */
    @OnClick(R.id.industry_layout)
    public void chooseindustry() {
        showDialog();
        if (isShow) {
            arrow.setBackgroundResource(R.mipmap.up_icon);
        }
    }

    public void showDialog() {
        allMsg.show();
        allMsg.getWindow().setContentView((RelativeLayout) allMsgView);
        isShow = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_default:
                savaPosition();
                break;
            case R.id.btn_own_setting:
                enterUpdateVocation();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    //
    private void enterInvitationStaff() {               //跳转邀请员工页
//        Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
//        sendBroadcast(reveiverIntent);
        Intent  intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void enterUpdateVocation() {                 //直接跳转到我要直接设置页面
        Intent intent = new Intent(this, UpdatePositionActivity.class);
       /* Bundle bundle = new Bundle();
        bundle.putString("goActivity", "DefaultPosition");
        intent.putExtras(bundle);*/
        startActivity(intent);
        finish();
    }

    private void savaPosition() {
        //progressDialog.show();
        showLoadingDialog(this, getString(R.string.saving));
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", tenant_id);
        params.put("user_id", user_id);
        params.put("indus_id", select_indus_id+"");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SAVE_COMPANY__POSITION), params, this);
        mNetworkManager.load(CALLBACK_SET_POSITION, path, this);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_defaultpositio;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        /*if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();*/
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_GET_INDUS_POSITION:
                indusList = new ArrayList<>();
                indusPositionList = new ArrayList<List<Position>>();
                List<Position> PositionList = new ArrayList<>();//单个行业的职位信息
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONObject resutObject = jsonObject.getJSONObject("data");    //获取data下对象
                    indusList = JsonDataFactory.getDataArray(Indus.class, resutObject.getJSONArray("info"));

                    for (Indus indus : indusList) {
                        PositionList = JsonDataFactory.getDataArray(Position.class, indus.getJson()
                                .getJSONArray("positions"));//获取行业中职位的集合
                        indusPositionList.add(PositionList);
                    }


                    if(select_indus_id!=0) {
                        for (int i = 0; i < indusList.size(); i++) {
                            long lg = indusList.get(i).indus_id;
                            if (lg == select_indus_id) {
                                industryTv.setText(getString(R.string.choose_indus_mode) + indusList.get(i).indus_name);
                                //groupsAdapter=new GroupAdapter();
                                showPositionList = indusPositionList.get(i);
                                listView.setAdapter(groupsAdapter);
                            }
                        }
                    }else{
                        industryTv.setText(getString(R.string.choose_indus_mode) + indusList.get(0).indus_name);
                        showPositionList = indusPositionList.get(0);
                        listView.setAdapter(groupsAdapter);
                    }



                    if (indusList != null && indusList.size() > 0) {
                        adapter = new PositionInfoDialogAdapter(this, indusList);
                        gridview.setAdapter(adapter);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_SET_POSITION:
                SharedPreferences preferences = PrfUtils.getSharePreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("step", "" + "4");
                editor.commit();
                enterInvitationStaff();  //直接使用预设的保存成功跳转到邀请业
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
    public void finish() {
        super.finish();
        mNetworkManager.cancle(this);
    }


    public void closeDialog() {
        allMsg.dismiss();
        isShow = false;
        arrow.setBackgroundResource(R.mipmap.down_icon);
    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mReceiver = null;
        super.onDestroy();
    }
    private Receiver mReceiver;

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACTION_DEFAULT.equals(intent.getAction())) {
                select_indus_id = intent.getExtras().getLong("indus_id");
                for (int i=0;i<indusList.size();i++) {
                    long lg = indusList.get(i).indus_id;
                    if (lg==select_indus_id) {
                        industryTv.setText(getString(R.string.choose_indus_mode) + indusList.get(i).indus_name);
                        showPositionList=indusPositionList.get(i);
                        groupsAdapter.notifyDataSetChanged();
                    }
                }
                closeDialog();
            }
        }
    }
    class GroupAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return showPositionList.size();
        }

        @Override
        public Object getItem(int position) {
            return showPositionList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(DefaultPositionActivity.this).inflate(R.layout.default_vocation_item, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.groupItem = (TextView) convertView.findViewById(R.id.tv_item);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.groupItem.setText(showPositionList.get(position).value);
            return convertView;
        }
    }

    static class ViewHolder {
        TextView groupItem;
    }
}
