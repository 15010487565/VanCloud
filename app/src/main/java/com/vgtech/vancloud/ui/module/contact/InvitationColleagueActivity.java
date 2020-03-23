package com.vgtech.vancloud.ui.module.contact;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.utils.ShareUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.ContactBean;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.register.country.CharacterParserUtil;
import com.vgtech.vancloud.ui.register.utils.GetPinyinUtil;
import com.vgtech.vancloud.ui.register.utils.MyLetterListView;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brook on 2016/09/06.
 */
public class InvitationColleagueActivity extends BaseActivity implements OnItemClickListener, MyLetterListView.OnTouchingLetterChangedListener, HttpListener<String> {

    private TextView tv_center;
    private AsyncQueryHandler asyncQueryHandler;
    private Map<Integer, ContactBean> contactIdMap = null;
    private ListView lv;
    private ArrayList<ContactBean> contacts;
    private MyAdatper mAdapter;
    private MyLetterListView letterListView;
    private CharacterParserUtil characterParserUtil;
    private NetworkManager mNetworkManager;
    private static final int INVITATION_ADD = 1;
    private String invitationContent;
    private String invitationUrl;
    private String invitationMessage;
    private VancloudLoadingLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        addHeader();
        initData();
    }

    private void initView() {
        setTitle(getString(R.string.invitation_Colleague));
        tv_center = (TextView) findViewById(R.id.tv_center);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        characterParserUtil = new CharacterParserUtil();
        lv = (ListView) findViewById(R.id.lv);
        lv.setOnItemClickListener(this);
    }

    private void addHeader() {
        View mHeaderView = getLayoutInflater().inflate(R.layout.invitation_header, null);
        lv.addHeaderView(mHeaderView);
        mHeaderView.findViewById(R.id.rl_weixin_invite).setOnClickListener(this);
        mHeaderView.findViewById(R.id.rl_number_invite).setOnClickListener(this);
        mHeaderView.findViewById(R.id.rl_webpage_add).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_weixin_invite: {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                new ShareUtils().shareWebPage(this, SendMessageToWX.Req.WXSceneSession
                        , bitmap, invitationUrl, getString(R.string.app_name), invitationContent);
            }
            break;
            case R.id.rl_number_invite: {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + ""));
                intent.putExtra("sms_body", invitationMessage);
                startActivity(intent);
            }
            break;
            case R.id.rl_webpage_add: {
                startActivity(new Intent(this, WebAddActivity.class));
            }
            break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void initData() {
        mAdapter = new MyAdatper();
        lv.setAdapter(mAdapter);
        asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());//遍历数据库
        try {
            PermissionsChecker mChecker = new PermissionsChecker(this);
            if (mChecker.lacksPermissions(CONTACTS_PERMISSION)) {
                // 请求权限
                PermissionsActivity.startActivityForResult(this, CONTACTS_REQUESTCODE, CONTACTS_PERMISSION);
//                    ActivityCompat.requestPermissions(this, BaseActivity.WRITEREADPERMISSIONS, 11000);
            } else {
                // 全部权限都已获取
                init();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        letterListView = (MyLetterListView) findViewById(R.id.indexBar);
        letterListView.setOnTouchingLetterChangedListener(this);
        loadMsg();
    }

    private void loadMsg() {
        mNetworkManager = getAppliction().getNetworkManager();
        showLoadingDialog(this, getString(R.string.please_wait_a_min));
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("template_flag", "invite_template");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_INVITE_TEMPLATE), params, this);
        mNetworkManager.load(INVITATION_ADD, path, this);

    }


    /**
     * 初始化数据库查询参数
     */
    private void init() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人URI
        // 查询的字段
        String[] projection = {ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY};
        // 按照sort_key升序查詢
        asyncQueryHandler.startQuery(0, null, uri, projection, null, null,
                "sort_key COLLATE LOCALIZED asc");

    }

    @Override
    public void onTouchingLetterChanged(String s) {
        showLetter(s);
        if (contacts != null && contacts.size() > 0) {
            for (int i = 0; i < contacts.size(); i++) {
                ContactBean contact = contacts.get(i);
                String l = GetPinyinUtil.getHeadChar(contact.getDesplayName());
                if (TextUtils.equals(s, l)) {
                    // 头一次匹配成功, 中断循环. 跳转过去
                    lv.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case INVITATION_ADD:
                try {
                    JSONObject resultObject = rootData.getJson().getJSONObject("data");
                    invitationContent = resultObject.getString("content");
                    invitationUrl = resultObject.getString("url");
                    invitationMessage = resultObject.getString("message");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * 数据库查询完成
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            loadingLayout.dismiss(lv);
            if (cursor != null && cursor.getCount() > 0) {
                contactIdMap = new HashMap<Integer, ContactBean>();
                ArrayList<ContactBean> contacts = new ArrayList<ContactBean>();
                cursor.moveToFirst(); // 游标移动到第一项
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    String name = cursor.getString(1);
                    String number = cursor.getString(2);
                    String sortKey = cursor.getString(3);
                    int contactId = cursor.getInt(4);
                    Long photoId = cursor.getLong(5);
                    String lookUpKey = cursor.getString(6);

                    if (contactIdMap.containsKey(contactId)) {
                        // 无操作
                    } else {
                        // 建联系人对象
                        ContactBean contact = new ContactBean();
                        contact.setDesplayName(name);
                        contact.setPhoneNum(number.replace(" ", ""));
                        contact.setSortKey(sortKey);
                        contact.setPhotoId(photoId);
                        contact.setLookUpKey(lookUpKey);
                        contact.setPinyin(GetPinyinUtil.getPingYin(name));
                        String countrySortKey = characterParserUtil.getSelling(name);
                        contact.countrySortKey = countrySortKey;
                        contacts.add(contact);
                        contactIdMap.put(contactId, contact);
                    }
                }
                if (contacts.size() > 0) {
                    setContacts(contacts);
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                loadingLayout.showEmptyView(lv, getString(R.string.no_list_data), true, true);
                lv.setVisibility(View.VISIBLE);
            }
            super.onQueryComplete(token, cookie, cursor);
        }

    }

    private Handler mHandler = new Handler();

    protected void showLetter(String letter) {
        tv_center.setText(letter);
        tv_center.setVisibility(View.VISIBLE);

        // 移除之前所有的延时操作
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv_center.setVisibility(View.GONE);
            }
        }, 2000);

    }


    public void setContacts(ArrayList<ContactBean> contacts) {
        this.contacts = contacts;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0 && contacts != null) {
            ContactBean info = contacts.get(position - 1);
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + info.getPhoneNum()));
            intent.putExtra("sms_body", invitationMessage);
            startActivity(intent);
        }
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_sms_invitationstaff;
    }

    class MyAdatper extends BaseAdapter {

        @Override
        public int getCount() {
            if (contacts != null)
                return contacts.size();
            else
                return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(InvitationColleagueActivity.this,
                        R.layout.item_contacts, null);
                holder = new ViewHolder();
                holder.tv_index = (TextView) convertView
                        .findViewById(R.id.tv_index);
                holder.tv_name = (TextView) convertView
                        .findViewById(R.id.tv_name);
                holder.tv_number = (TextView) convertView
                        .findViewById(R.id.tv_number);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ContactBean contact = contacts.get(position);
            String currentLetter = contact.getPinyin().charAt(0) + "";

            // 根据首字母进行分组
            String letter = null;
            if (position == 0) {
                // 如果是第一个条目
                letter = currentLetter;
            } else {
                // 判断当前首字母 跟 上一个 首字母 是否一致
                String preLetter = contacts.get(position - 1).getPinyin()
                        .charAt(0)
                        + "";
                if (!TextUtils.equals(preLetter, currentLetter)) {
                    // 跟前一个条目 不一致则显示
                    letter = currentLetter;
                }
            }

            ContactBean info = contacts.get(position);

            if (!TextUtil.isEmpty(letter)) {
                holder.tv_index.setText(letter.toUpperCase());
            }

            holder.tv_index.setVisibility(letter == null ? View.GONE
                    : View.VISIBLE);
            holder.tv_name.setText(contact.getDesplayName());
            holder.tv_number.setText(contact.getPhoneNum());
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

    }

    static class ViewHolder {
        private TextView tv_index;
        private TextView tv_name;
        private TextView tv_number;
    }
}
