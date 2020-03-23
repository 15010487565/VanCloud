package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.util.Log;
import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.AppRole;
import com.vgtech.common.api.FileInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.Position;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.api.UserInfo;
import com.vgtech.common.api.UserProperty;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.UserPropertyAdapter;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.common.image.ClipActivity;
import com.vgtech.vancloud.ui.common.image.ImageCheckActivity;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedListener;
import com.vgtech.vancloud.ui.register.ui.RoleActivity;
import com.vgtech.vancloud.ui.register.ui.UpdatePositionActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/9/29.
 */
public class SelfInfoActivity extends BaseActivity implements HttpListener<String> {
    private View mLoadingView;
    private NetworkManager mNetworkManager;

    private UserPropertyAdapter mPropertyAdapter;
    private SimpleDraweeView mUserIcon;
    private TextView btn_del_user;
    private String mUserId, mType;
    private boolean mSelf;
    private View mHeaderView;
    private ImageView leaveView;
    private TextView nameTv;
    private boolean mHasEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.userinfo));
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String name = intent.getStringExtra("name");
        String photo = intent.getStringExtra("photo");
        String type = intent.getStringExtra("type");
        mHeaderView = getLayoutInflater().inflate(R.layout.userinfo_self, null);
        mNetworkManager = getAppliction().getNetworkManager();
        PreferencesController preferencesController = new PreferencesController();
        preferencesController.context = this;
        UserAccount userAccount = preferencesController.getAccount();
        if (TextUtil.isEmpty(userId)) {
            userId = PrfUtils.getUserId(this);
            name = userAccount.nickname();
            photo = userAccount.photo;
        }
        mUserId = userId;
        mType = type;
        mSelf = userId.equals(PrfUtils.getUserId(this));
        mHasEmployee = AppPermissionPresenter.hasPermission(this, AppPermission.Type.settings, AppPermission.Setting.employee.toString());
        nameTv = (TextView) mHeaderView.findViewById(R.id.user_name);
        mUserIcon = (SimpleDraweeView) mHeaderView.findViewById(R.id.user_photo);
        if (!TextUtil.isEmpty(photo)) {
            mUserIcon.setTag(photo);
            ImageOptions.setUserImage(mUserIcon, photo);
        } else {
            mUserIcon.setImageResource(R.mipmap.user_photo_default_small);
        }
        mUserIcon.setOnClickListener(this);
        if (!TextUtil.isEmpty(name))
            nameTv.setText(name);
        if (mSelf) {
            mHeaderView.findViewById(R.id.btn_photo).setOnClickListener(this);
            mHeaderView.findViewById(R.id.ll_self_name).setOnClickListener(this);
            mHeaderView.findViewById(R.id.iv_arrow_right).setVisibility(View.VISIBLE);
            mHeaderView.findViewById(R.id.ic_arrow).setVisibility(View.VISIBLE);
        } else {
            setTitle(getString(R.string.staff_detail));
            mHeaderView.setEnabled(false);
        }
        leaveView = (ImageView) findViewById(R.id.iv_leave);
        if (PrfUtils.isChineseForAppLanguage(this))
            leaveView.setImageResource(R.mipmap.icon_leave_ch);
        else
            leaveView.setImageResource(R.mipmap.icon_leave_en);
        btn_del_user = (TextView) findViewById(R.id.btn_del_user);
        btn_del_user.setOnClickListener(this);
        mLoadingView = findViewById(R.id.loading);
        ListView listView = (ListView) findViewById(android.R.id.list);
        mPropertyAdapter = new UserPropertyAdapter(this, this);
        listView.addHeaderView(mHeaderView);
        listView.setAdapter(mPropertyAdapter);
        loadingUserInfo(userId);

        if (TextUtils.equals(mUserId, PrfUtils.getUserId(this))) {
            setTitle(getString(R.string.userinfo));
        } else {
            setTitle(getString(R.string.staff_info));
        }
    }

    private void initActionView() {
        findViewById(R.id.other_action).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_msg).setOnClickListener(this);
        findViewById(R.id.btn_message).setOnClickListener(this);
        findViewById(R.id.btn_phone).setOnClickListener(this);
        findViewById(R.id.btn_phone_save).setOnClickListener(this);
    }

    private static final int CALLBACK_USERINFO = 1;
    private static final int CALLBACK_SAVEUSERINFO = 5;
    private static final int CALLBACK_SETTING_ROLE = 3;
    private static final int CALLBACK_IMAGE = 2;
    private static final int DELETESTAFFMSG = 6;

    private void loadingUserInfo(String userId) {
        mLoadingView.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<String, String>();
        params.put("own_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", userId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_USER_MESSAGE), params, this);
        mNetworkManager.load(CALLBACK_USERINFO, path, this);
    }

    private static final int TAKE_PICTURE = 0x000000;
    private static final int FROM_PHOTO = 0x000001;
    private static final int PHOTO_CLIP = 0x000002;
    private String path = "";
    private EditText editText;
    private UserInfo mUserInfo;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_phone_save: {
                Intent it = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(
                        Uri.parse("content://com.android.contacts"), "contacts"));
                it.setType("vnd.android.cursor.dir/person");
                it.setType("vnd.android.cursor.dir/contact");
                it.setType("vnd.android.cursor.dir/raw_contact");
                it.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, mUserInfo.name);// 公司
                it.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
                        mUserInfo.mobile);
                startActivity(it);
            }
            break;
            case R.id.btn_userproperty: {
                final UserProperty userProperty = (UserProperty) v.getTag();
                switch (userProperty.type) {
                    case sex: {
                        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                                .builder()
                                .setCancelable(true)
                                .setCanceledOnTouchOutside(true);
                        actionSheetDialog.addSheetItem(getString(R.string.vancloud_male), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        userProperty.name = getString(R.string.vancloud_male);
                                        userProperty.id = "1";
                                        saveUserInfo("gender", userProperty.id);
                                        mPropertyAdapter.notifyDataSetChanged();
                                    }
                                });
                        actionSheetDialog.addSheetItem(getString(R.string.vancloud_famale), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        userProperty.name = getString(R.string.vancloud_famale);
                                        userProperty.id = "2";
                                        saveUserInfo("gender", userProperty.id);
                                        mPropertyAdapter.notifyDataSetChanged();
                                    }
                                });
                        actionSheetDialog.show();
                    }
                    break;
                    case birthday: {
                        String startTime = userProperty.name;
                        Calendar calendar = null;
                        if (!TextUtils.isEmpty(startTime)) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                Date parse = dateFormat.parse(startTime);
                                calendar = Calendar.getInstance();
                                calendar.setTime(parse);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        if (calendar == null) {
                            calendar = Calendar.getInstance();
                        }
                        DateFullDialogView dateDialogview = new DateFullDialogView(this,
                                new DateFullDialogView.OnSelectedListener() {
                                    @Override
                                    public void onSelectedListener(long time) {
                                        String date = DataUtils.dateFormat(time, DataUtils.FORMAT_YYYYMMDD);
                                        userProperty.id = "" + time;
                                        userProperty.name = date;
                                        saveUserInfo("birthday", userProperty.id);
                                        mPropertyAdapter.notifyDataSetChanged();
                                    }
                                }, "YMD", "date", calendar, getResources().getColor(R.color.lable_enable), calendar);
                        dateDialogview.show(v);
                    }
                    break;
                    case staffno: {
                        showAlertDialog(userProperty.lable,//title
                                userProperty.name,//content
                                new View.OnClickListener() {//positiveListener
                                    @Override
                                    public void onClick(View v) {
                                        String name = editText.getText().toString();
                                        userProperty.id = name;
                                        userProperty.name = name;
                                        saveUserInfo("staff_no", userProperty.id);
                                        mPropertyAdapter.notifyDataSetChanged();
                                    }
                                }, null);
                    }
                    break;
                    case position: {
                        mPositionProperty = userProperty;
                        if (mPrositionList == null) {
                            loadPositionInfo();
                        } else {
                            showPositionSelected();
                        }
                    }
                    break;
                    case role: {
                        mRoleProperty = userProperty;
                        Intent intent = new Intent(this, RoleActivity.class);
                        intent.putExtra("id", mRoleProperty.id);
                        intent.putExtra("name", mRoleProperty.name);
                        startActivityForResult(intent, 1005);
                    }
                    break;
                    case depart: {
                        mDepartProperty = userProperty;
                        Intent intent = new Intent(this, DepartSelectedActivity.class);
                        startActivityForResult(intent, 1001);
                    }
                    break;
                    case leader:
                        mLeaderProperty = userProperty;
                        Intent intent = new Intent(this, OrganizationSelectedActivity.class);
                        intent.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_SINGLE);
                        intent.putExtra("unselect", getUnSelectNodes());
                        intent.putExtra("selectSelf", true);
                        startActivityForResult(intent, 1003);
                        break;
                    case email: {
                        showAlertDialog(userProperty.lable,//title
                                userProperty.name,//content
                                new View.OnClickListener() {//positiveListener
                                    @Override
                                    public void onClick(View v) {
                                        String name = editText.getText().toString();
                                        if (!Utils.isEmail(name)) {
                                            showToast(getString(R.string.email_not_correct));
                                            return;
                                        }
                                        userProperty.name = name;
                                        mPropertyAdapter.notifyDataSetChanged();
                                        saveUserInfo("email", userProperty.name);
                                    }
                                }, null);
                    }
                    break;
                }
            }
            break;
            case R.id.btn_msg: {
                List<String> ids = new ArrayList<String>();
                List<Long> times = new ArrayList<Long>();
                ids.add(mUserId);
                times.add(System.currentTimeMillis());
                com.vgtech.common.provider.db.User.updateUserAccessTime(getApplicationContext(), ids, times);
                Intent intent = new Intent(MainActivity.RECEIVER_CHAT);
                Intent lastIntent = getIntent();
                String userId = lastIntent.getStringExtra("userId");
                String name = lastIntent.getStringExtra("name");
                String photo = lastIntent.getStringExtra("photo");
                intent.putExtra("userId", userId);
                intent.putExtra("name", name);
                intent.putExtra("photo", photo);
                sendBroadcast(intent);
                finish();
            }
            break;
            case R.id.user_photo: {
                Object obj = v.getTag();
                if (obj != null) {
                    String photo = (String) obj;
                    if (!TextUtils.isEmpty(photo)) {
                        List<ImageInfo> imgInfo = new ArrayList<>();
                        imgInfo.add(new ImageInfo(photo, photo));
                        String json = new Gson().toJson(imgInfo);
                        Intent intent = new Intent(this, ImageCheckActivity.class);
                        intent.putExtra("listjson", json);
                        intent.putExtra("numVisible", false);
                        intent.putExtra("userphoto", true);
                        startActivity(intent);
                    }
                }
            }
            break;
            case R.id.btn_message: {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + mUserInfo.mobile));
                intent.putExtra("sms_body", "");
                startActivity(intent);
            }
            break;
            case R.id.btn_phone: {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" + mUserInfo.mobile);
                intent.setData(data);
                startActivity(intent);
            }
            break;
            case R.id.btn_photo:
                new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem(getString(R.string.take), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
//                                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                        File file = new File(FileCacheUtils.getImageDir(getApplicationContext()), String.valueOf(System.currentTimeMillis())
//                                                + ".jpg");
//                                        path = file.getPath();
//                                        Uri imageUri = Uri.fromFile(file);
//                                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
                                        PermissionsChecker mChecker = new PermissionsChecker(SelfInfoActivity.this);
                                        if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                                            // 请求权限
                                            PermissionsActivity.startActivityForResult(SelfInfoActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
                                        } else {
                                            // 全部权限都已获取
                                            EasyPhotos.createCamera(SelfInfoActivity.this)
                                                    .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                                    .start(TAKE_PICTURE);
                                        }
                                    }
                                })

                        .addSheetItem(getString(R.string.select_from_photo), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        Intent intent = new Intent(getApplicationContext(),
                                                PicSelectActivity.class);
                                        intent.putExtra("single", true);
                                        startActivityForResult(intent, FROM_PHOTO);
                                    }
                                }).show();
                break;
            case R.id.btn_del_user:
//                deluserAction();
                Intent intent = new Intent(this, ResignActivity.class);
                intent.putExtra("userId", mUserId);
                intent.putExtra("name", mUserInfo.name);
                startActivityForResult(intent, 4001);
                break;
            case R.id.ll_self_name://修改自己的姓名
                showAlertDialog(getString(R.string.employee_name),//title
                        nameTv.getText(),//content
                        new View.OnClickListener() {//positiveListener
                            @Override
                            public void onClick(View v) {
                                String name = editText.getText().toString();
                                nameTv.setText(name);
                                saveUserInfo("staff_name", name);
                            }
                        }, null);
                break;
            case R.id.user_name://修改别人的姓名
                showAlertDialog(getString(R.string.employee_name),//title
                        nameTv.getText(),//content
                        new View.OnClickListener() {//positiveListener
                            @Override
                            public void onClick(View v) {
                                String name = editText.getText().toString();
                                nameTv.setText(name);
                                saveUserInfo("staff_name", name);
                            }
                        }, null);
                break;
            case R.id.btn_right:
                saveUserInfo();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void saveUserInfo(String key, String value) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("own_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", mUserId);
        params.put(key, value);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_MESSAGE_EDIT), params, this);
        mNetworkManager.load(CALLBACK_SAVEUSERINFO, path, this);
    }

    private void saveUserRole(String key, String value) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("userids", mUserId);
        params.put(key, value);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SETTINGS_ROLES), params, this);
        mNetworkManager.load(CALLBACK_SETTING_ROLE, path, this);
    }

    private void saveUserInfo() {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("own_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", mUserId);
        if (!TextUtils.isEmpty(mPhotoUrl))
            params.put("photo", mPhotoUrl);
        params.put("staff_name", nameTv.getText().toString());
        List<UserProperty> userPropertys = mPropertyAdapter.getData();
        for (UserProperty up : userPropertys) {
            switch (up.type) {
                case sex:
                    params.put("gender", up.id);
                    break;
                case birthday:
                    params.put("birthday", up.id);
                    break;
                case staffno:
                    params.put("staff_no", up.id);
                    break;
                case position:
                    params.put("position_id", up.id);
                    break;
                case depart:
                    params.put("depart_id", up.id);
                    break;
                case leader:
                    params.put("supervisor_id", up.id);
                    break;
                case email:
                    params.put("email", up.id);
                    break;
            }
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_MESSAGE_EDIT), params, this);
        mNetworkManager.load(CALLBACK_SAVEUSERINFO, path, this);
    }

    private void showAlertDialog(String title, CharSequence content, View.OnClickListener positiveListener, View.OnClickListener negativeListener) {
        AlertDialog dialog = new AlertDialog(this).builder().setTitle(title);
        editText = dialog.setEditer();
        editText.setText(content);
        editText.setSelection(content.length());
        dialog.setPositiveButton(getString(R.string.ok), positiveListener)
                .setNegativeButton(getString(R.string.cancel), negativeListener).show();
    }


    private void showPositionSelected() {
        if (mPrositionList.isEmpty()) {
            if ((AppPermissionPresenter.hasPermission(this, AppPermission.Type.settings, AppPermission.Setting.position.toString()))
                    && !TenantPresenter.isVanTop(this)) {
                mPrositionList = null;
                Intent intent_post = new Intent(this, UpdatePositionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("goActivity", "CompanyInfoActivity");
                intent_post.putExtras(bundle);
                startActivity(intent_post);
                return;
            }
        }
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true);

        for (Position option : mPrositionList) {
            actionSheetDialog.addSheetItem(option.value, ActionSheetDialog.SheetItemColor.Blue,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            Position position = mPrositionList.get(which);
                            String value = position.value;
                            mPositionProperty.name = value;
                            mPositionProperty.id = position.key;
                            saveUserInfo("position_id", mPositionProperty.id);
                            mPropertyAdapter.notifyDataSetChanged();
                        }
                    });
        }
        actionSheetDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1003:
                ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                if (userSelectList != null && !userSelectList.isEmpty()) {
                    Node node = userSelectList.get(0);
                    mLeaderProperty.name = node.getName();
                    mLeaderProperty.id = node.getId();
                    mPropertyAdapter.notifyDataSetChanged();
                    saveUserInfo("supervisor_id", mLeaderProperty.id);
                }
                break;
            case 4001:
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
                if (leaveView != null)
                    leaveView.setVisibility(View.VISIBLE);
                btn_del_user.setVisibility(View.GONE);
                findViewById(R.id.other_action).setVisibility(View.GONE);
                break;
            case 1001: {
                Node node = data.getParcelableExtra("node");
                mDepartProperty.name = node.getName();
                mDepartProperty.id = node.getId();
                saveUserInfo("depart_id", mDepartProperty.id);
                mPropertyAdapter.notifyDataSetChanged();
            }
            break;
            case PHOTO_CLIP: {
                String path = data.getStringExtra("path");
                uploadPhoto(path);
            }
            break;
            case TAKE_PICTURE: {
                //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                if (resultPaths != null && resultPaths.size() > 0) {
                    path = resultPaths.get(0);
                    Intent intent = new Intent(this, ClipActivity.class);
                    intent.putExtra("path", path);
                    startActivityForResult(intent, PHOTO_CLIP);
                }
            }
            break;
            case FROM_PHOTO: {
                String path = data.getStringExtra("path");
                Intent intent = new Intent(this, ClipActivity.class);
                intent.putExtra("path", path);
                startActivityForResult(intent, PHOTO_CLIP);
            }
            break;
            case 1005: {
                String id = data.getStringExtra("id");
                String name = data.getStringExtra("name");
                mRoleProperty.id = id;
                mRoleProperty.name = name;
                saveUserRole("roleid", mRoleProperty.id);
                mPropertyAdapter.notifyDataSetChanged();
            }
            break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void uploadPhoto(String imgPath) {
        mLoadingView.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", mUserId);
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("type", String.valueOf(13));
        FilePair filePair = new FilePair("pic", new File(imgPath));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_IMAGE), params, filePair);
        mNetworkManager.load(CALLBACK_IMAGE, path, this);
    }

    private void loadPositionInfo() {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GET_COMPANY_POSITION), params, this);
        mNetworkManager.load(1002, path, this);
    }

    private List<Position> mPrositionList;
    private UserProperty mDepartProperty;
    private UserProperty mPositionProperty;
    private UserProperty mRoleProperty;
    private UserProperty mLeaderProperty;

    /**
     * 获取被选中的用户id
     *
     * @return
     */
    private ArrayList<Node> getUnSelectNodes() {

        ArrayList<Node> list = new ArrayList<Node>();
        Node node = new Node(mUserInfo.userid, mUserInfo.name, true, mUserInfo.photo);
        list.add(node);
        return list;
    }

    private String mPhotoUrl;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.setVisibility(View.GONE);
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_SETTING_ROLE:

                break;
            case CALLBACK_SAVEUSERINFO: {
                if (mSelf) {
                    PreferencesController preferencesController = new PreferencesController();
                    preferencesController.context = this;
                    UserAccount userAccount = preferencesController.getAccount();
                    String photo = path.getPostValues().get("photo");
                    if (!TextUtils.isEmpty(photo)) {
                        userAccount.photo = photo;
                        preferencesController.storageAccount(userAccount);
                    }
                    String name = path.getPostValues().get("staff_name");
                    if (!TextUtils.isEmpty(name)) {
                        userAccount.user_name = name;
                        preferencesController.storageAccount(userAccount);
                    }
                }
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
            }
            break;
            case 1002:
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mPrositionList = JsonDataFactory.getDataArray(Position.class, jsonObject.getJSONArray("positions"));
                    showPositionSelected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_USERINFO:
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mUserInfo = JsonDataFactory.getData(UserInfo.class, jsonObject);
                    mUserInfo.userid = mUserId;
                    mUserIcon.setTag(mUserInfo.photo);
                    nameTv.setText(mUserInfo.name);
                    ImageOptions.setUserImage(mUserIcon, mUserInfo.photo);
                    if (!mSelf) {
                        initActionView();
                    }
                    List<UserProperty> properties = new ArrayList<UserProperty>();
                    List<AppRole> roles = JsonDataFactory.getDataArray(AppRole.class, jsonObject.getJSONArray("role"));
                    StringBuffer roleStr = new StringBuffer();
                    StringBuffer roleId = new StringBuffer();
                    for (AppRole role : roles) {
                        roleStr.append(role.name).append(",");
                        roleId.append(role.id).append(",");
                    }
                    if (!TextUtil.isEmpty(roleStr.toString())) {
                        roleStr = roleStr.deleteCharAt(roleStr.length() - 1);
                        roleId = roleId.deleteCharAt(roleId.length() - 1);
                    }
                    String sex = "";
                    if ("1".equals(mUserInfo.gender)) {
                        sex = getString(R.string.vancloud_male);
                    } else if ("2".equals(mUserInfo.gender)) {
                        sex = getString(R.string.vancloud_famale);
                    }

                    JSONObject positionObject = jsonObject.getJSONObject("position");
                    JSONObject departObject = jsonObject.getJSONObject("depart");
                    JSONObject supervisorObject = jsonObject.getJSONObject("supervisor");
                    properties.add(new UserProperty(mSelf || mHasEmployee, false, UserProperty.Type.sex, getString(R.string.sex_tv), null, sex));
                    properties.add(new UserProperty(mSelf || mHasEmployee, false, UserProperty.Type.birthday, getString(R.string.birthday), "" + mUserInfo.birthday, DataUtils.dateFormat(mUserInfo.birthday, DataUtils.FORMAT_YYYYMMDD)));
                    properties.add(new UserProperty(mHasEmployee, true, UserProperty.Type.staffno, getString(R.string.staffno), "" + mUserInfo.staffno, mUserInfo.staffno));
                    properties.add(new UserProperty(mHasEmployee, false, UserProperty.Type.position, getString(R.string.position), positionObject.getString("id"), positionObject.getString("name")));
                    properties.add(new UserProperty(!mSelf && mHasEmployee, false, UserProperty.Type.role, getString(R.string.role), roleId.toString(), roleStr.toString()));
                    properties.add(new UserProperty(mHasEmployee, false, UserProperty.Type.depart, getString(R.string.department), departObject.getString("id"), departObject.getString("name")));
                    properties.add(new UserProperty(mHasEmployee, false, UserProperty.Type.leader, getString(R.string.leader), supervisorObject.getString("id"), supervisorObject.getString("name")));
                    properties.add(new UserProperty(false, true, UserProperty.Type.mobile, getString(R.string.mobile_phone), mUserInfo.mobile, mUserInfo.mobile));
                    properties.add(new UserProperty(mSelf || mHasEmployee, false, UserProperty.Type.email, getString(R.string.email), mUserInfo.email, mUserInfo.email));
                    mPropertyAdapter.addAllData(properties);
                    mPropertyAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(mUserInfo.is_leave) && "1".equals(mUserInfo.is_leave)) {
                    if (leaveView != null)
                        leaveView.setVisibility(View.VISIBLE);
                    btn_del_user.setVisibility(View.GONE);
                    findViewById(R.id.other_action).setVisibility(View.GONE);
                } else {
                    if (mHasEmployee) {
                        btn_del_user.setVisibility(View.VISIBLE);
                    } else {
                        btn_del_user.setVisibility(View.GONE);
                    }
                }
                break;
            case CALLBACK_IMAGE: {
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    List<FileInfo> fileInfos = JsonDataFactory.getDataArray(FileInfo.class, dataArray);
                    mPhotoUrl = fileInfos.get(0).url;
                    //  updatePhoto(url);
                    mUserIcon.setTag(mPhotoUrl);
                    saveUserInfo("photo", mPhotoUrl);
                    ImageOptions.setUserImage(mUserIcon, mPhotoUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case DELETESTAFFMSG:
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
//                Toast.makeText(this, R.string.del_user_sul, Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        mNetworkManager.cancle(this);
        if (mDepartProperty != null && !TextUtils.isEmpty(mDepartProperty.id)) {
            Intent intent = new Intent();
            intent.putExtra("departId", mDepartProperty.id);
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    @Override
    protected int getContentView() {
        return R.layout.user_info;
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }


}
