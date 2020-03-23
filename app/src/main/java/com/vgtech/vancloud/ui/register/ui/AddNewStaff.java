package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jackson on 2016/5/20.
 * Version : 1
 * Details :
 */
public class AddNewStaff extends BaseActivity implements View.OnClickListener, HttpListener<String> {
    private TextView tv_phone;
    private TextView tv_role;
    private TextView tv_position;
    private TextView tv_department;
    private EditText et_name;

//<include layout="@layout/title_bar" />

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
        setTitle(R.string.number_add);
    }

    private void initData() {

    }

    private void initListener() {
        findViewById(R.id.rl_phone).setOnClickListener(this);
        findViewById(R.id.rl_role).setOnClickListener(this);
        findViewById(R.id.rl_position).setOnClickListener(this);
        findViewById(R.id.rl_department).setOnClickListener(this);
        findViewById(R.id.add_staff).setOnClickListener(this);
    }

    private void initView() {
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_role = (TextView) findViewById(R.id.tv_role);
        tv_position = (TextView) findViewById(R.id.tv_position);
        tv_department = (TextView) findViewById(R.id.tv_department);
        et_name = (EditText) findViewById(R.id.et_name);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_add_new_staff;
    }

    private static final int ROLE = 1000;
    private static final int POSITION = 1001;
    private static final int DEPART = 1002;
    private static final int PHONE = 1003;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_phone:
                Intent i = new Intent();
                i.setAction(Intent.ACTION_PICK);
                i.setData(ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, PHONE);
                break;
            case R.id.rl_role:
                Intent data = new Intent(this, RoleActivity.class);
                data.putExtra("id", mMap.get("roleId"));
                startActivityForResult(data, ROLE);
                break;
            case R.id.rl_position:
                String positionId = mMap.get("positionId");
                Intent intent = new Intent(this, SetPositionActivity.class);
                intent.putExtra("positionId", positionId);
                intent.putExtra("get", true);
                startActivityForResult(intent, POSITION);
                break;
            case R.id.rl_department:
                startActivityForResult(ChooseSubPartmentActivity.class, DEPART);
                break;
            case R.id.add_staff:
                submit();
                break;
        }
        super.onClick(v);
    }

    public void submit() {
        String name = et_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showToast(R.string.hint_input_name);
            return;
        }
        TextView phoneTv = (TextView) findViewById(R.id.tv_phone);
        String phone = phoneTv.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            showToast(getString(R.string.please_input_phone));
            return;
        }
        if (!Utils.isPhoneNum(phone)) {
            showToast(getString(R.string.vancloud_phonenumber_notlegal));
            return;
        }
        String roleId = mMap.get("roleId");
        if (TextUtils.isEmpty(roleId)) {
            showToast(R.string.please_set_role);
            return;
        }

        String positionId = mMap.get("positionId");
        if (positionId == null)
            positionId = "-1";

        String departid = mMap.get("departid");
        if (TextUtils.isEmpty(departid)) {
            showToast(R.string.please_set_depart);
            return;
        }

        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("area_code", "86");
        params.put("mobile", phone);
        /*String password = phone.substring(5);
        params.put("password", MD5.getMD5(password));*/
        params.put("role_id", roleId);
        params.put("position_id", positionId);
        params.put("depart_id", departid);
        params.put("user_name", name);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ADD_STAFF), params, this);
        mNetworkManager.load(CALL_BACK_SUBMIT, path, this);
    }


    private void startActivityForResult(Class<?> clzz, int requestCode) {
        Intent data = new Intent(this, clzz);
        data.putExtra("get", true);
        startActivityForResult(data, requestCode);
    }

    private Map<String, String> mMap = new HashMap<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHONE:
                if (resultCode != RESULT_OK) return;
                PermissionsChecker mChecker = new PermissionsChecker(this);
                if (mChecker.lacksPermissions(CONTACTS_PERMISSION)) {
                    // 请求权限
                    PermissionsActivity.startActivityForResult(this, CONTACTS_REQUESTCODE, CONTACTS_PERMISSION);
                } else {
                    // 全部权限都已获取
                    String[] contacts = getPhoneContacts(data);
                    String name = contacts[0];
                    String phone = contacts[1];
                    tv_phone.setText(phone);
                    mMap.put("phone", phone);
                }

                break;
            case ROLE:
                if (resultCode != RESULT_OK) return;
                String roleId = data.getStringExtra("id");
                String roleName = data.getStringExtra("name");
                mMap.put("roleId", roleId);
                tv_role.setText(roleName);
                break;
            case POSITION:
                if (resultCode != RESULT_OK) return;
                String positionId = data.getStringExtra("id");
                String positionName = data.getStringExtra("name");
                mMap.put("positionId", positionId);
                tv_position.setText(positionName);
                break;
            case DEPART:
                if (resultCode != RESULT_OK) return;
                String departid = data.getStringExtra("id");
                String departName = data.getStringExtra("name");
                mMap.put("departid", departid);
                tv_department.setText(departName);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    /**
     * 获取手机通许路选中的名字和电话号码
     *
     * @param data
     * @return
     */
    private String[] getPhoneContacts(Intent data) {
        String[] contacts = new String[2];
        if (data == null) {
            return contacts;
        }

        Uri contactData = data.getData();
        if (contactData == null) {
            return contacts;
        }
        Cursor cursor = managedQuery(contactData, null, null, null, null);
        if (cursor.moveToFirst()) {
            contacts[0] = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String hasPhone = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String id = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));

            if (hasPhone.equalsIgnoreCase("1")) {
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                + " = " + id, null, null);
                while (phones.moveToNext()) {
                    contacts[1] = phones
                            .getString(phones
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                }
                phones.close();
            }
        }
        if (Build.VERSION.SDK_INT < 14) {
            cursor.close();
        }
        return contacts;
    }


    private static final int CALL_BACK_SUBMIT = 1000;

    private NetworkManager mNetworkManager;


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_SUBMIT:
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
                finish();
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}