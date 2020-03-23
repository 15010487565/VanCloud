package com.vgtech.vantop.ui.userinfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.FileInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.utils.ImageCacheManager;
import com.vgtech.common.utils.wheel.WheelUtil;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ItemSelectedAdapter;
import com.vgtech.vantop.adapter.UserInfoAdapter;
import com.vgtech.vantop.moudle.ItemSelectMoudle;
import com.vgtech.vantop.moudle.VantopShowUserInfoData;
import com.vgtech.vantop.moudle.VantopUserInfoFieldsData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.ItemSelectActivity;
import com.vgtech.vantop.utils.PreferencesController;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * vantop个人信息
 * 需要传入参数 staff_no 或 userId
 * 当都为空时获取PrfUtils的staff_no
 * Created by shilec on 2016/9/14.
 */
public class VantopUserInfoActivity extends BaseActivity implements HttpListener<String>, AdapterView.OnItemClickListener {

    private final int CALLBACK_LOAD_SHOW = 0X001;
    private final int CALLBACK_LOAD_EDIT = 0X002;
    private final int CALLBACK_EDIT_INFO = 0X003;
    private final int CALLBACK_UPLOAD_AVATAR = 0X004;
    private final int CALLBACK_SETUSERPHOTO = 0X005;
    private final int CALLBACK_USERID_TO_STAFFNO = 0X006;
    private final int CALLBACK_STAFFNO_TO_UID = 0X007;

    private final int REQUEST_ITEM_SELECT = 0x003;
    private static final int REQUEST_TAKE_PICTURE = 0x000000;
    private static final int REQUEST_FROM_PHOTO = 0x000001;
    private static final int REQUEST_PHOTO_CLIP = 0x000002;
    public static final String BUNDLE_STAFFNO = "staff_no";
    public static final String BUNDLE_USERID = "user_id";
    private String mStaffNo;

    private final String TAG = "TAG_UserInfo";
    private VantopShowUserInfoData mShowData;
    private VantopShowUserInfoData mEditData;
    private List<VantopUserInfoFieldsData> mListData;
    private UserInfoAdapter mAdapter;

    private ListView mListView;
    private View mListHeaderView;
    private int mEditIndex;
    private SimpleDraweeView mHead;
    private EditText mPswEdit;
    private VancloudLoadingLayout mLoadingView;
    private String mUserId;
    private boolean mSelf;

    @Override
    protected int getContentView() {
        return R.layout.activity_userinfo;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_phone_save) {
            if (!mShowData.getPhone().status) {
                Toast.makeText(this, getString(R.string.vantop_noautor), Toast.LENGTH_SHORT).show();
            } else {
                Intent it = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(
                        Uri.parse("content://com.android.contacts"), "contacts"));
                it.setType("vnd.android.cursor.dir/person");
                it.setType("vnd.android.cursor.dir/contact");
                it.setType("vnd.android.cursor.dir/raw_contact");
                it.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, mShowData.getStaffName().value);
                it.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
                        mShowData.getPhone().value);
                startActivity(it);
            }
        } else if (v.getId() == R.id.btn_message) {
            if (!mShowData.getPhone().status) {
                Toast.makeText(this, getString(R.string.vantop_noautor), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + mShowData.getPhone().value));
            intent.putExtra("sms_body", "");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_phone) {
            if (!mShowData.getPhone().status) {
                Toast.makeText(this, getString(R.string.vantop_noautor), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" + mShowData.getPhone().value);
                intent.setData(data);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.btn_msg) {
//            if (Constants.DEBUG){
//                Log.e("TAG_mUserId","mUserId="+mUserId+";mStaffNo="+mStaffNo);
//            }
            if (mChat) {
                finish();
            } else {
                if (TextUtils.isEmpty(mUserId)) {
                    getStaffNoOrUserId(mStaffNo, false);
                } else {

                    sendChatMsg(mUserId);
                }
            }

        } else {
            super.onClick(v);
        }

    }

    private void sendChatMsg(String userId) {
        List<String> ids = new ArrayList<String>();
        List<Long> times = new ArrayList<Long>();
        ids.add(userId);
        times.add(System.currentTimeMillis());
        PreferencesController prf = new PreferencesController();
        prf.context = this;
        updateUserAccessTime(userId);
        Intent intent = new Intent("RECEIVER_CHAT");
        intent.putExtra("name", mShowData.getStaffName().value);
        intent.putExtra("photo", ImageCacheManager.staffToPhoto.get(mStaffNo));
        intent.putExtra("userId", userId);
        sendBroadcast(intent);
        finish();
    }

    public void updateUserAccessTime(final String userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> ids = new ArrayList<String>();
                final List<Long> times = new ArrayList<Long>();
                final List<String> jobs = new ArrayList<String>();
                long time = System.currentTimeMillis();
                User user = User.queryUser(getApplicationContext(), userId);
                if (user == null) {
                    user = new User();
                    user.userId = userId;
                    user.photo = ImageCacheManager.staffToPhoto.get(mStaffNo);
                    user.name = mShowData.getStaffName().value;
                    user.job = mShowData.getPosistion() == null ? "" : mShowData.getPosistion().value;
                    user.email = mShowData.getEmail() == null ? "" : mShowData.getEmail().value;
                    user.accessTime = time;
                    user.insert(getApplicationContext());
                } else {
                    ids.add(userId);
                    jobs.add(TextUtils.isEmpty(mShowData.getPosistion() == null ? "" : mShowData.getPosistion().value) ? "" : mShowData.getPosistion() == null ? "" : mShowData.getPosistion().value);
                    times.add(time);
                }
                User.updateUserAccessTimeAndJob(getApplicationContext(), ids, times, jobs);
            }
        }).start();
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            mLoadingView.showErrorView(mListView);
            return;
        }

        switch (callbackId) {
            case CALLBACK_LOAD_SHOW: {
                mShowData = VantopShowUserInfoData.fromJson(PrfUtils.isChineseForAppLanguage(this), rootData.getJson().toString());
                if (mSelf)
                    loadData(false);
                else {
                    onLoadedShowData();
                    mLoadingView.dismiss(mListView);
                }
            }
            break;

            case CALLBACK_LOAD_EDIT: {
                onLoadedEditData(rootData);
                mLoadingView.dismiss(mListView);
            }
            break;

            case CALLBACK_EDIT_INFO: {
                Toast.makeText(this, getString(R.string.vantop_edit_success), Toast.LENGTH_SHORT).show();
                mShowData = null;
                mEditData = null;
                mListData.clear();
                loadData(true);
                loadData(false);
            }
            break;
            case CALLBACK_UPLOAD_AVATAR: {
                onUploadAvatar(rootData);
            }
            break;
            case CALLBACK_SETUSERPHOTO: {
                onSetUserPhoto(rootData);
            }
            break;
            case CALLBACK_USERID_TO_STAFFNO: {
                onGetStaffNo(rootData);
            }
            break;
            case CALLBACK_STAFFNO_TO_UID: {
                JSONObject json = rootData.getJson();
                mUserId = json.optString("data");
                sendChatMsg(mUserId);
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShowData = new VantopShowUserInfoData();
        mListData = new ArrayList<>();
        mAdapter = new UserInfoAdapter(this, mListData);

        initView();
        initData();
    }

    private boolean mChat;

    public void initData() {
        mStaffNo = getIntent().getStringExtra(BUNDLE_STAFFNO);
        mChat = getIntent().getBooleanExtra("GROUPCHAT_TYPE", false);
        if (TextUtils.isEmpty(mStaffNo)) {
            setTitle(getString(R.string.staff_detail));

            mUserId = getIntent().getStringExtra(BUNDLE_USERID);

            if (TextUtils.isEmpty(mUserId)) {//查看自己
                mSelf = true;
                mStaffNo = PrfUtils.getStaff_no(this);

                loadInitData();
            } else {
                getStaffNoOrUserId(mUserId, true);
            }
        } else {
            //本人查看本人显示个人信息，查看其他人为个人详情
            if (TextUtils.equals(mStaffNo, PrfUtils.getStaff_no(this))) {
                mSelf = true;
                setTitle(getString(R.string.vantop_userinfo));
            } else {
                setTitle(getString(R.string.staff_detail));
            }
            loadInitData();
        }
    }


    /***
     * @param userId      员工编号(staff_no)或用户Id(userId)
     * @param uid2staffno true uid_2_staff_no
     */
    private void getStaffNoOrUserId(String userId, boolean uid2staffno) {
        if (!uid2staffno)
            showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        if (uid2staffno) {
            params.put("uids", userId);
            params.put("purpose", "to_staffno");
        } else {
            params.put("staffnos", userId);
            params.put("purpose", "to_uid");
        }
        String path = ApiUtils.generatorUrl(this, UrlAddr.URL_USERID_TO_STAFFNO);
        NetworkPath np = new NetworkPath(path, params, this);
        getApplicationProxy().getNetworkManager().
                load(uid2staffno ? CALLBACK_USERID_TO_STAFFNO : CALLBACK_STAFFNO_TO_UID, np, this);
        //showLoadingDialog(this, "");
    }

    private void onGetStaffNo(RootData rootData) {
        dismisLoadingDialog();
        JSONObject json = rootData.getJson();
        mStaffNo = json.optString("data");

        //本人查看本人显示个人信息，查看其他人为个人详情
        if (TextUtils.equals(mStaffNo, PrfUtils.getStaff_no(this))) {
            //其他人查看时不能编辑
            mSelf = true;
            setTitle(getString(R.string.vantop_userinfo));
            //mListHeaderView.findViewById(R.id.iv_edithead).setVisibility(View.VISIBLE);
            showEditView(true);
        } else {
            //mListHeaderView.findViewById(R.id.iv_edithead).setVisibility(View.INVISIBLE);
            showEditView(false);
            setTitle(getString(R.string.staff_detail));
        }
        loadInitData();
    }

    private void loadInitData() {
        loadData(true);
    }

    /***
     * @param isShow 当为true时获取的是个人信息显示数据
     *               false为b编辑数据
     */
    private void loadData(boolean isShow) {
        if (TextUtils.isEmpty(mStaffNo)) return;
        Uri uri = Uri.parse(VanTopUtils.generatorUrl(this, isShow ? UrlAddr.URL_VANTOP_USERINFO : UrlAddr.URL_VANTOP_USEREDIT_SHOW));
        NetworkPath np;
        //if (isShow) {
        //uri.buildUpon().appendQueryParameter("staff_no", mStaffNo).build();
        // }
        Map<String, String> map = new HashMap<>();
        if (isShow) {
            map.put("staff_no", mStaffNo);
            np = new NetworkPath(uri.toString(), map, this, true);
        } else {
            np = new NetworkPath(uri.toString(), map, this, true);
        }
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        nm.load(isShow ? CALLBACK_LOAD_SHOW : CALLBACK_LOAD_EDIT, np, this);
        //showLoadingDialog(this, "");
        //mLoadingView.setVisibility(View.VISIBLE);
    }

    private void initHeadView() {

        mHead = (SimpleDraweeView) mListHeaderView.findViewById(R.id.iv_header);
        /**
         * 更换头像
         */
        mListHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upDataUserInfoHeader();
            }
        });
        //其他人查看时不能编辑
        if (TextUtils.isEmpty(mStaffNo) || TextUtils.equals(mStaffNo, PrfUtils.getStaff_no(this))) {
            //mListHeaderView.findViewById(R.id.iv_edithead).setVisibility(View.VISIBLE);
            showEditView(true);
        } else {
            //mListHeaderView.findViewById(R.id.iv_edithead).setVisibility(View.INVISIBLE);
            showEditView(false);
        }
        mHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //预览头像
                List<ImageInfo> list = new ArrayList<ImageInfo>();
                String url;
                if (mSelf) {
                    PreferencesController prf = new PreferencesController();
                    prf.context = VantopUserInfoActivity.this;
                    url = prf.getAccount().photo;
                } else {
                    url = ImageCacheManager.staffToPhoto.get(mStaffNo);
                }
                ImageInfo info = new ImageInfo();
                info.url = url;
                list.add(info);
                Intent intent = new Intent("com.vgtech.imagecheck");
                intent.putExtra("listjson", new Gson().toJson(list));
                intent.putExtra("userphoto", true);
                startActivity(intent);
            }
        });
    }

    private void upDataUserInfoHeader() {
        if (TextUtils.equals(mStaffNo, PrfUtils.getStaff_no(VantopUserInfoActivity.this))) {
            if (mListData == null || mListData.isEmpty()) return;
            PermissionsChecker mChecker = new PermissionsChecker(VantopUserInfoActivity.this);
            if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                // 请求权限
                PermissionsActivity.startActivityForResult(VantopUserInfoActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
            } else {
                // 全部权限都已获取
                showEditPhotoDialog();
            }
        }
    }

    /***
     * @param flag 是否设置头像右边距
     */
    private void showEditView(boolean flag) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mHead.getLayoutParams();
        if (flag) {
            mListHeaderView.findViewById(R.id.iv_edithead).setVisibility(View.VISIBLE);
            lp.rightMargin = getDpSize(0);
        } else {
            mListHeaderView.findViewById(R.id.iv_edithead).setVisibility(View.GONE);
            lp.rightMargin = getDpSize(10);
        }
        mHead.setLayoutParams(lp);
    }

    private int getDpSize(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_userinfo);
        View v = LayoutInflater.from(this).inflate(R.layout.userinfo_header_item, null);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(lp);
        mListHeaderView = v;
        initHeadView();
        mListView.addHeaderView(v);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        mLoadingView.showLoadingView(mListView, "", true);
        findViewById(R.id.btn_msg).setOnClickListener(this);
        findViewById(R.id.btn_message).setOnClickListener(this);
        findViewById(R.id.btn_phone).setOnClickListener(this);
        findViewById(R.id.btn_phone_save).setOnClickListener(this);

        mLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });
    }

    private void onLoadedShowData() {
        if (mShowData == null) {
            Toast.makeText(this, getString(R.string.vantop_nouserinfo), Toast.LENGTH_SHORT).show();
            mLoadingView.showEmptyView(mListView, getString(R.string.vantop_nouserinfo), true, true);
            return;
        }
        initListData();
    }

    private void showUserPhoto() {
        PreferencesController prf = new PreferencesController();
        prf.context = this;
        UserAccount account = prf.getAccount();
        //String url = VanTopUtils.getImageUrl(this, mStaffNo);
        if (mSelf) {
            ImageOptions.setUserImage(mHead, account.photo);
        } else {
            mListHeaderView.findViewById(R.id.iv_edithead).setVisibility(View.GONE);
            showEditView(false);
            ImageCacheManager.getImage(this, mHead, mStaffNo);
        }
    }

    private void onLoadedEditData(RootData rootData) {
        mEditData = VantopShowUserInfoData.fromJson(PrfUtils.isChineseForAppLanguage(this), rootData.getJson().toString());
        if (mEditData == null) {
            mLoadingView.showEmptyView(mListView, getString(R.string.vantop_nouserinfo), true, true);
            Toast.makeText(this, getString(R.string.vantop_nouserinfo), Toast.LENGTH_SHORT).show();
            return;
        }
        initListData();
    }

    /***
     * 合并显示数据和编辑数据
     * 1.获取 URL_VANTOP_USERINFO URL_VANTOP_USEREDIT_SHOW 两个接口数据
     * 2.合并数据分为两步 固定字段 和 非固定字段
     * fixedfilds: 如果 mShowEditData中有该字段，mEditData中没有改字段
     * 则该字段不可编辑，如果mEditData.staus 为true，并且type不为空的话是可以修改的的字段
     * fields:如果mShowEditData.staus 为true，并且mShowData和mEditData都有该字段则可以
     * 编辑
     */
    private void initListData() {

        if (mShowData != null && mEditData != null) {
            mListData.clear();
            VantopUserInfoFieldsData sData, eData;
            //Set<String> set = mShowData.fixedFields.keySet();
            //遍历固定属性，按FIXED_KEYS顺序获取
            for (String key : VantopShowUserInfoData.FIXED_KEYS) {
                if (key.equals("english_name")){
                    for (int i = 0; i < mShowData.fields.size(); i++) {
                        VantopUserInfoFieldsData vantopUserInfoFieldsData1 = mShowData.fields.get(i);
                        if (vantopUserInfoFieldsData1.status&&vantopUserInfoFieldsData1.name.equals("english_name")) {
                            VantopUserInfoFieldsData vantopUserInfoFieldsData = vantopUserInfoFieldsData1;
                            addFixedFieldToList(vantopUserInfoFieldsData, vantopUserInfoFieldsData);
                        }
                    }

                }else {
                    sData = mShowData.fixedFields.get(key);
                    eData = mEditData.fixedFields.get(key);
                    addFixedFieldToList(sData, eData);

                }

                //如果查看他人信息员工编号和员工姓名都不可以修改
                /*if (eData != null && (TextUtils.equals(eData.name, "staff_name") ||
                        TextUtils.equals(eData.name, "staff_no"))) {
                    if (sData != null)
                        sData.isEdit = false;
                }*/
            }
            //固定属性分割线
            mAdapter.fixedSize = mListData.size() - 1;
            //是否为本人查看本人的信息
            mAdapter.isSelf = TextUtils.equals(mStaffNo, PrfUtils.getStaff_no(this));
            for (int i = 0; i < mShowData.fields.size(); i++) {
                VantopUserInfoFieldsData vantopUserInfoFieldsData1 = mShowData.fields.get(i);
                if (vantopUserInfoFieldsData1.status) {
                    if (!vantopUserInfoFieldsData1.name.equals("english_name")) {
                        //英文名置顶后不再添加数据
                        mListData.add(mShowData.fields.get(i));
                    }
                }
            }

            //非固定字段 设置是否可编辑
            for (int i = 0; i < mShowData.fields.size(); i++) {

                VantopUserInfoFieldsData data = mShowData.fields.get(i);
                if (isCanEdit(data)) {
                    data.isEdit = true;
                } else {
                    data.isEdit = false;
                }
            }
            // mLoadingView.setVisibility(View.GONE);
            if (mListData.isEmpty()) {
                mLoadingView.showEmptyView(mListView, getString(R.string.vantop_nouserinfo), true, true);
                return;
            }
            //查看其他人信息显示 发送消息 短信 打电话 扩展菜单
            if (mSelf)
                findViewById(R.id.other_action).setVisibility(View.GONE);
            showUserPhoto();
            mAdapter.notifyDataSetChanged();
        } else if (mShowData != null) {
            mListData.clear();
            VantopUserInfoFieldsData sData;
            //Set<String> set = mShowData.fixedFields.keySet();
            //遍历固定属性，按FIXED_KEYS顺序获取
            for (String key : VantopShowUserInfoData.FIXED_KEYS) {
                sData = mShowData.fixedFields.get(key);
                addFixedFieldToList(sData, null);
                //如果查看他人信息员工编号和员工姓名都不可以修改
                /*if (eData != null && (TextUtils.equals(eData.name, "staff_name") ||
                        TextUtils.equals(eData.name, "staff_no"))) {
                    if (sData != null)
                        sData.isEdit = false;
                }*/
            }
            //固定属性分割线
            mAdapter.fixedSize = mListData.size() - 1;
            //是否为本人查看本人的信息
            mAdapter.isSelf = TextUtils.equals(mStaffNo, PrfUtils.getStaff_no(this));
            for (int i = 0; i < mShowData.fields.size(); i++) {
                if (mShowData.fields.get(i).status) {
                    mListData.add(mShowData.fields.get(i));
                }
            }
            findViewById(R.id.other_action).setVisibility(View.VISIBLE);
            showUserPhoto();
            mAdapter.notifyDataSetChanged();
        }
    }

    /***
     * 添加固定属性到list
     *
     * @param sData
     * @param eData
     */
    private void addFixedFieldToList(VantopUserInfoFieldsData sData,
                                     VantopUserInfoFieldsData eData) {

        if (sData != null) {
            if (!mListData.contains(sData))
                mListData.add(sData);
            if (eData == null) {
                sData.isEdit = false;
            } else {
                if (eData.status && !TextUtils.isEmpty(eData.type)) {
                    sData.isEdit = true;
                    sData.values = eData.values;
                    sData.type = eData.type;
                    //sData.value = eData.value;
                    sData.name = eData.name;
                }
            }
            //禁用编辑
            if (!mSelf) {
                //查看他人所有字段编辑
                sData.isEdit = false;
            } else {
                //查看自己员工编号和员工姓名不能编辑
                if (TextUtils.equals(sData.name, "staff_name")
                        || TextUtils.equals(sData.name, "staff_no")) {
                    sData.isEdit = false;
                }
            }
        }
    }

    /***
     * 判断是否可以编辑
     * 当可见的字段中和可编辑的list中都有该字段，并且status为true时可以编辑
     *
     * @param data
     * @return
     */
    private boolean isCanEdit(VantopUserInfoFieldsData data) {
        //如果查看其他人信息不能修改
        if (!mSelf) {
            return false;
        }

        //TODO 可优化  indexFilds
        for (int i = 0; i < mEditData.fields.size(); i++) {

            if (mEditData.fields.get(i).status && (TextUtils.equals(data.label, mEditData.fields.get(i).label)
                    || TextUtils.equals(data.name, mEditData.fields.get(i).name))) {
                if (data.values.isEmpty())
                    data.values = mEditData.fields.get(i).values;
                if (TextUtils.isEmpty(data.type))
                    data.type = mEditData.fields.get(i).type;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_ITEM_SELECT: {
                if (data == null) return;
                ArrayList<ItemSelectMoudle> items = (ArrayList<ItemSelectMoudle>) data.getSerializableExtra(ItemSelectActivity.EXTRA_RESAULT);
                if (items == null || items.isEmpty()) {
                    return;
                }
                ItemSelectMoudle item = items.get(0);
                VantopUserInfoFieldsData listdata = mListData.get(mEditIndex);
                if (!TextUtils.equals(listdata.value, item.value)) {
                    listdata.value = item.value;
                    submit(listdata.name, item.code);
                }
                //mAdapter.notifyDataSetChanged();
            }
            break;
            case REQUEST_PHOTO_CLIP: {
                String path = data.getStringExtra("path");
                if (!TextUtils.isEmpty(path)) {
                    uploadPhoto(path);
                }
            }
            break;
            case REQUEST_TAKE_PICTURE: {
                //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                if (resultPaths !=null&&resultPaths.size()>0){
                    mPath = resultPaths.get(0);
                    Intent intent = new Intent("com.vgtech.vancloud.clipimage");
                    intent.putExtra("path", mPath);
                    startActivityForResult(intent, REQUEST_PHOTO_CLIP);
                }
//                if (!TextUtils.isEmpty(mPath)) {
//                    Intent intent = new Intent("com.vgtech.vancloud.clipimage");
//                    intent.putExtra("path", mPath);
//                    startActivityForResult(intent, REQUEST_PHOTO_CLIP);
//                }
            }
            break;
            case REQUEST_FROM_PHOTO: {
                String path = data.getStringExtra("path");
                if (!TextUtils.isEmpty(path)) {
                    Intent intent = new Intent("com.vgtech.vancloud.clipimage");
                    intent.putExtra("path", path);
                    startActivityForResult(intent, REQUEST_PHOTO_CLIP);
                }
            }
            break;
//            case CAMERA_REQUESTCODE://修改头像
//                upDataUserInfoHeader();
//                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void submit(String name, String code) {

        Map<String, String> map = new HashMap<>();
        map.put(name, code);
        NetworkPath np = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_VANTOP_USEREDIT), map, this, true);
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        nm.load(CALLBACK_LOAD_EDIT, np, this);
        showLoadingDialog(this, getString(R.string.vantop_submitdata), false);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


        //E P C N 为输入对话框
        //S 为选择activity
        //D 为date选择对话框
        VantopUserInfoFieldsData data = mListData.get(i - 1);
        //boolean isSelf = TextUtils.equals(mStaffNo, PrfUtils.getStaff_no(this));
        if (i == 0 || !data.isEdit) return;
        //Toast.makeText(this, "点击type:" + data.type, Toast.LENGTH_LONG).show();
        if (TextUtils.equals(data.type, "S")) {
            showSelectActivity(i, data);
        }
        if ("EPCN".contains(data.type)) {
            /*if(TextUtils.equals(data.name,"staff_name")) {
                return;
            }*/
            if (verifyContent(data.type, data.value)) {
                showEditDialog(data.value, i, data.type);
            }
        }
        if (TextUtils.equals("D", data.type)) {
            showDatePicker(i - 1);
        }
    }

    private boolean verifyText(String type, String content) {
        boolean ret = true;
        switch (type) {
            case "E": {
                ret = Pattern.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$", content);
                if (!ret) {
                    Toast.makeText(this, getString(R.string.vantop_emailerror), Toast.LENGTH_SHORT).show();
                    return ret;
                }
            }
            case "P": {
                ret = Pattern.matches("[0-9]{11}", content);
                if (!ret) {
                    Toast.makeText(this, getString(R.string.vantop_phonerror), Toast.LENGTH_SHORT).show();
                    return ret;
                }
            }
            case "N": {
                ret = TextUtils.isDigitsOnly(content);
                if (!ret) {
                    Toast.makeText(this, getString(R.string.vantop_digitserror), Toast.LENGTH_SHORT).show();
                    return ret;
                }
            }
        }
        return ret;
    }


    private void showSelectActivity(int index, VantopUserInfoFieldsData data) {
        ArrayList<ItemSelectMoudle> items = convertMapToData(data.values, data.value);
        Intent intent = new Intent(this, ItemSelectActivity.class);
        intent.putExtra(ItemSelectActivity.EXTRA_DATA, items);
        intent.putExtra(ItemSelectActivity.EXTRA_MODE, ItemSelectedAdapter.SELECTED_MODE_RADIOBTN);
        startActivityForResult(intent, REQUEST_ITEM_SELECT);
        mEditIndex = index - 1;
    }

    private void showEditDialog(String value, final int index, final String type) {

        mEditIndex = index - 1;
        showAlertDialog(getString(R.string.vantop_change) + mListData.get(index - 1).label + "",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.equals(mListData.get(mEditIndex).value, mPswEdit.getText().toString())) {
                            if (!verifyContent(mPswEdit.getText().toString(), type)) {
                                Toast.makeText(VantopUserInfoActivity.this, getString(R.string.vantop_text_error), Toast.LENGTH_SHORT).show();
                            } else {
                                mListData.get(mEditIndex).value = mPswEdit.getText().toString();
                                submit(mListData.get(mEditIndex).name,
                                        mListData.get(mEditIndex).value);
                                mListData.get(mEditIndex).value = mPswEdit.getText().toString();
                            }
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
        setInputType(type, mPswEdit);
        mPswEdit.setText(mListData.get(mEditIndex).value);
    }

    private boolean verifyContent(String content, String type) {
        switch (type) {
            case "E": {

                return content.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
            }
            case "N": {
                return content.matches("[0-9]+");
            }
            case "P": {
                return content.matches("[0-9]{8,11}");
            }
            default: {
                return true;
            }
        }
    }

    private void setInputType(String type, EditText editText) {
        switch (type) {
            case "E": {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            }
            break;
            case "N": {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            }
            break;
            case "P": {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            }
            break;
            default: {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        }
    }

    private void showAlertDialog(String title, View.OnClickListener positiveListener, View.OnClickListener negativeListener) {
        AlertDialog dialog = new AlertDialog(this).builder().setTitle(title);
        mPswEdit = dialog.setEditer();
        dialog.setPositiveButton(getString(R.string.vantop_confirm), positiveListener)
                .setNegativeButton(getString(R.string.vantop_cancle), negativeListener).show();
    }

    private ArrayList<ItemSelectMoudle> convertMapToData(Map<String, String> map, String value) {

        ArrayList<ItemSelectMoudle> datas = new ArrayList<>();
        Set<String> set = map.keySet();
        for (String key : set) {
            ItemSelectMoudle item = new ItemSelectMoudle();
            item.code = key;
            item.value = map.get(key);
            if (TextUtils.equals(value, item.value)) {
                item.isSelected = true;
            } else {
                item.isSelected = false;
            }
            datas.add(item);
        }
        return datas;
    }

    private void showDatePicker(int index) {

        final int i = index;
        final DateFullDialogView dialog = new DateFullDialogView(this,
                ((TextView) findViewById(R.id.tv_right)), "YMD", "date");
        dialog.setButtonClickListener(new DateFullDialogView.ButtonClickListener() {
            @Override
            public void sureButtonOnClickListener(String time) {

                Class cls = DateFullDialogView.class;
                try {
                    //通过WheelUtil的方法getDateTime获取当前选中的日期
                    Field mWheel = cls.getDeclaredField("mWheel");
                    mWheel.setAccessible(true);
                    WheelUtil util = (WheelUtil) mWheel.get(dialog);
                    //获取WheelUtil对象
                    cls = WheelUtil.class;
                    Method m = cls.getDeclaredMethod("getDateTime");
                    m.setAccessible(true);
                    //执行getDateTime方法
                    String date = (String) m.invoke(util);
                    if (!TextUtils.equals(mListData.get(i).value, date)) {
                        mListData.get(i).value = date;
                        submit(mListData.get(i).name, mListData.get(i).value);
                    }
                    //mAdapter.notifyDataSetChanged();
                    //Toast.makeText(VantopUserInfoActivity.this,"date:" + date,Toast.LENGTH_SHORT).show();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void cancelButtonOnClickListener() {
                dialog.dismiss();
            }
        });
        dialog.show(findViewById(R.id.tv_right));
    }

    private String mPath;

    private void showEditPhotoDialog() {

        new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem(getString(R.string.take), ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
//                                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                File file = new File(FileCacheUtils.getImageDir(getApplicationContext()), String.valueOf(System.currentTimeMillis())
//                                        + ".jpg");
//                                mPath = file.getPath();
//                                Uri imageUri = Uri.fromFile(file);
//                                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                                startActivityForResult(openCameraIntent, REQUEST_TAKE_PICTURE);

                                EasyPhotos.createCamera(VantopUserInfoActivity.this)
                                        .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                        .start(REQUEST_TAKE_PICTURE);
                            }
                        })

                .addSheetItem(getString(R.string.select_from_photo), ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                Intent intent = new Intent(getApplicationContext(),
                                        PicSelectActivity.class);
                                intent.putExtra("single", true);
                                startActivityForResult(intent, REQUEST_FROM_PHOTO);
                            }
                        }).show();
    }

    private void uploadPhoto(String path) {
        try {
            //mLoadingView.setVisibility(View.VISIBLE);
            showLoadingDialog(this, "", false);
            Map<String, String> params = new HashMap<String, String>();
            params.put("ownid", PrfUtils.getUserId(this));
            params.put("tenantid", PrfUtils.getTenantId(this));
            params.put("type", String.valueOf(13));
            FilePair filePair = new FilePair("pic", new File(path));
            NetworkPath np = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_IMAGE), params, filePair, this, true);
            getApplicationProxy().getNetworkManager().load(CALLBACK_UPLOAD_AVATAR, np, this);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void onUploadAvatar(RootData rootData) {

        try {
            JSONArray dataArray = rootData.getJson().getJSONArray("data");
            List<FileInfo> fileInfos = JsonDataFactory.getDataArray(FileInfo.class, dataArray);
            String path = fileInfos.get(0).url;
            PreferencesController prf = new PreferencesController();

            prf.context = this;
            UserAccount account = prf.getAccount();
            account.photo = path;
            prf.storageAccount(account);
            ImageOptions.setUserImage(mHead, path);
            setUserPhoto(path);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUserPhoto(String path) {

        String url = ApiUtils.generatorUrl(this, URLAddr.URL_UPDATEPHOTO);
        Map<String, String> params = new HashMap<>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("photo", path);
        NetworkPath np = new NetworkPath(url, params, this);
        getApplicationProxy().getNetworkManager().load(CALLBACK_SETUSERPHOTO, np, this);
    }

    private void onSetUserPhoto(RootData rootData) {
        //dismisLoadingDialog();
    }

}
