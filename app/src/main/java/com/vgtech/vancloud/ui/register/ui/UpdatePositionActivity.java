package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Position;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.PrfUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by brook on 2015/10/13.
 */
public class UpdatePositionActivity extends BaseActivity implements HttpListener<String> {

    @InjectView(R.id.set_lv_tv)
    LinearLayout setLvTv;
    @InjectView(R.id.set_lv)
    LinearLayout setLv;
    @InjectView(R.id.company_tv)
    TextView companyTv;
    private NetworkManager mNetworkManager;
    private ListView listView;
    private TextView textView;
    private Button btn_add_position;
    private List<Position> showPositionList; //页面所要显示的职位信息
    private String goActiivty;
    private EditText editText;
    private String tenant_id;
    private String user_id;
    private GroupAdapter groupAdapter;
    private static final int CALLBACK_UPDATE_POSITION = 1;
    private static final int CALLBACK_GET_COMPANY_POSITION = 2;
    private static final int CALLBACK_DELETE_POSITION = 3;
    private static final int CALLBACK_SET_POSITION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initview();
        initData();
        initEvent();
    }

    private void initEvent() {
        btn_add_position.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = this.getIntent();
        goActiivty = intent.getStringExtra("goActivity");//获取判断从哪个Activity跳转过的信息
        if ("CompanyInfoActivity".equals(goActiivty)) {           //初始化从我要设置页面跳转过来的数据
            setTitle(getString(R.string.vocation_admin));
            ImageView rightIv = (ImageView) findViewById(R.id.btn_right);
            rightIv.setVisibility(View.VISIBLE);
            rightIv.setImageResource(R.drawable.btn_add);
            rightIv.setOnClickListener(this);
            getComPanyPosition();
        } else {
//            findViewById(R.id.iv_back).setVisibility(View.GONE);
//            findViewById(R.id.title_right).setVisibility(View.VISIBLE);
            setTitle(getString(R.string.update_vocation));
            btn_add_position.setVisibility(View.VISIBLE);
            textView = initRightTv(getString(R.string.btn_finish));
            setLv.setVisibility(View.VISIBLE);
            setLvTv.setVisibility(View.VISIBLE);
            getComPanyPosition();
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    savaPosition();   //调用保存职位设置,改变状态
                }
            });
        }
        companyTv.setText(new PreferencesController().getAccount().tenant_name);
    }


    private void savaPosition() {
        /*progressDialog = LoadingProgressDialog.createDialog(this, 0);
        progressDialog.show();*/
        showLoadingDialog(this, getString(R.string.saving));
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", tenant_id);
        params.put("user_id", user_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SAVE_COMPANY__POSITION), params, this);
        mNetworkManager.load(CALLBACK_SET_POSITION, path, this);
    }


    private void getComPanyPosition() {  //获取已保存的的信息
       /* progressDialog = LoadingProgressDialog.createDialog(this, 0);
        progressDialog.show();*/
        showLoadingDialog(this, getString(R.string.loading));
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", tenant_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GET_COMPANY_POSITION), params, this);
        mNetworkManager.load(CALLBACK_GET_COMPANY_POSITION, path, this);
    }

    private void initview() {
        ButterKnife.inject(this);
        mNetworkManager = getAppliction().getNetworkManager();
        btn_add_position = (Button) this.findViewById(R.id.btn_add_positio);
        listView = (ListView) findViewById(android.R.id.list);
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        user_id = preferences.getString("uid", "");
        tenant_id = preferences.getString("tenantId", "");
        showPositionList = new ArrayList<>();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_positio:
                showAddDialog();
                break;
            case R.id.btn_right:
                showAddDialog();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void addVocaion(String positionName) {   //添加职位
        boolean isNameRepeat = false;
        for (Position position : showPositionList) {
            if (position.value.equals(positionName.replace(" ", ""))) {
                isNameRepeat = true;
                break;
            }
        }

        if (isNameRepeat) {
            Toast.makeText(UpdatePositionActivity.this, getString(R.string.has_position_not_add), Toast.LENGTH_SHORT).show();
        } else {
            showLoadingDialog(this, getString(R.string.adding_position));
            Map<String, String> params = new HashMap<String, String>();
            params.put("tenant_id", tenant_id);
            params.put("user_id", user_id);
            params.put("positionname", positionName);
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_UPDATE_POSITION), params, this);
            mNetworkManager.load(CALLBACK_UPDATE_POSITION, path, this);
        }

    }

    private void updataVocation(int select_positions_index, String positionName) {   //修改职位
       /* progressDialog = LoadingProgressDialog.createDialog(this, 0);
        progressDialog.show();*/
        showLoadingDialog(this, getString(R.string.updating_position));
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", tenant_id);
        params.put("user_id", user_id);
        params.put("positionname", positionName);
        params.put("position_id", showPositionList.get(select_positions_index).key);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_UPDATE_POSITION), params, this);
        mNetworkManager.load(CALLBACK_UPDATE_POSITION, path, this);
    }

    private void deleteVocation(int select_positions_index) {    //删除职位
        /*progressDialog = LoadingProgressDialog.createDialog(this, 0);
        progressDialog.show();*/
        showLoadingDialog(this, getString(R.string.deleting_position));
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", tenant_id);
        params.put("user_id", user_id);
        params.put("position_id", showPositionList.get(select_positions_index).key);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_DELETE_POSITION), params, this);
        mNetworkManager.load(CALLBACK_DELETE_POSITION, path, this);
    }


    public void showAddDialog() {
        AlertDialog dialog = new AlertDialog(this).builder().setTitle(getString(R.string.input_add_position_info));
        editText = dialog.setEditer();
        dialog.setPositiveButton(getString(R.string.ok), new OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputString = editText.getText().toString();
                if (!TextUtil.isEmpty(inputString)) {
                    addVocaion(inputString);
                }
            }
        }).setNegativeButton(getString(R.string.cancel), new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).show();

    }


    public void showUpdateDialog(final int index) {
        AlertDialog dialog = new AlertDialog(this).builder().setTitle(getString(R.string.hint_input_info));
        editText = dialog.setEditer();
        editText.setText(showPositionList.get(index).value);
        editText.setSelection(showPositionList.get(index).value.length());
        dialog.setPositiveButton(getString(R.string.ok), new OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputString = editText.getText().toString();
                if (!TextUtil.isEmpty(inputString)) {
                    updataVocation(index, inputString);
                }
            }
        }).setNegativeButton(getString(R.string.cancel), new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).show();
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_updatepositio;
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
            case CALLBACK_GET_COMPANY_POSITION:
                updataListview(rootData);
                break;
            case CALLBACK_UPDATE_POSITION:

                if (!"CompanyInfoActivity".equals(goActiivty)) {
                    SharedPreferences preference = PrfUtils.getSharePreferences(this);
                    SharedPreferences.Editor editors = preference.edit();
                    editors.putString("step", "" + "3");
                    editors.commit();
                }
                Toast.makeText(UpdatePositionActivity.this,
                        getString(R.string.add_update_success), Toast.LENGTH_SHORT).show();
                updataListview(rootData);
                break;
            case CALLBACK_DELETE_POSITION:
                Toast.makeText(UpdatePositionActivity.this,
                        getString(R.string.shared_delete_success), Toast.LENGTH_SHORT).show();
                updataListview(rootData);
                break;
            case CALLBACK_SET_POSITION:
                SharedPreferences preferences = PrfUtils.getSharePreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("step", "" + "4");
                editor.commit();
                Toast.makeText(UpdatePositionActivity.this,
                        getString(R.string.position_msg_saved), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(UpdatePositionActivity.this, InvitationStaffActivity.class);
//                startActivity(intent);
                Intent  intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
//                Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
//                sendBroadcast(reveiverIntent);
        }

    }

    private void updataListview(RootData rootData) {
        JSONObject jsonObject = rootData.getJson();
        try {
            //从服务器获取设置的职位信息
            JSONObject resutObject = jsonObject.getJSONObject("data");    //获取data下对象
            showPositionList = JsonDataFactory.getDataArray(Position.class, resutObject.getJSONArray("positions"));
            if (showPositionList != null && showPositionList.size() < 0) {
                SharedPreferences preference = PrfUtils.getSharePreferences(this);
                SharedPreferences.Editor editors = preference.edit();
                editors.putString("step", "" + "2");
                editors.commit();
            }

            if (groupAdapter == null) {
                groupAdapter = new GroupAdapter();
                listView.setAdapter(groupAdapter);
            } else {
                groupAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
    }

    @Override
    public void onResponse(String response) {
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
                convertView = LayoutInflater.from(UpdatePositionActivity.this).inflate(R.layout.vocation_item, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.groupItem = (TextView) convertView.findViewById(R.id.tv_item);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.groupItem.setText(showPositionList.get(position).value);
            holder.groupItem.setOnClickListener(new UpdataListener(position));
            holder.iv_delete.setOnClickListener(new DeleteListener(position));


            return convertView;
        }
    }


    class UpdataListener implements OnClickListener {
        int index;

        public UpdataListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            showUpdateDialog(index);
        }
    }

    class DeleteListener implements OnClickListener {
        int index;

        public DeleteListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            deleteVocation(index);
        }
    }

    static class ViewHolder {
        TextView groupItem;
        ImageView iv_delete;
    }
}
