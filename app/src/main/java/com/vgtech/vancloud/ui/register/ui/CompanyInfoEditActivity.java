package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.FileInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.DictSelectActivity;
import com.vgtech.common.ui.InputActivity;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.common.MapLocationActivity;
import com.vgtech.vancloud.ui.common.image.ClipActivity;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.vancloud.ui.module.me.SelfInfoActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by brook on 2015/10/22.
 */
public class CompanyInfoEditActivity extends BaseActivity implements HttpListener<String> {

    @InjectView(R.id.iv_company_name)
    SimpleDraweeView ivCompanyName;
    @InjectView(R.id.rl_company_QRcode)
    View rlCompanyQRcode;
    @InjectView(R.id.tv_company_name)
    TextView tvCompanyName;
    @InjectView(R.id.tv_compay_address)
    TextView tvCompayAddress;
    @InjectView(R.id.tv_company_host)
    TextView tvCompanyHost;
    @InjectView(R.id.tv_company_contact)
    TextView tvCompanyContact;
    @InjectView(R.id.tv_company_legal)
    TextView tvCompanyLegal;
    @InjectView(R.id.tv_company_license)
    TextView tvCompanyLicense;
    @InjectView(R.id.tv_gs_wz)
    TextView tv_gs_wz;

    @InjectView(R.id.rl_company_name)
    View rlCompanyName;
    @InjectView(R.id.rl_compay_address)
    View rlCompayAddress;
    @InjectView(R.id.rl_company_host)
    View rlCompanyHost;
    @InjectView(R.id.rl_company_contact)
    View rlCompanyContact;
    @InjectView(R.id.rl_company_legal)
    View rlCompanyLegal;
    @InjectView(R.id.rl_company_license)
    View rlCompanyLicense;

    @InjectView(R.id.iv_arrow1)
    ImageView ivArrow1;
    @InjectView(R.id.iv_arrow2)
    ImageView ivArrow2;
    @InjectView(R.id.iv_arrow4)
    ImageView ivArrow4;
    @InjectView(R.id.iv_arrow5)
    ImageView ivArrow5;
    @InjectView(R.id.iv_arrow6)
    ImageView ivArrow6;
    @InjectView(R.id.iv_arrow7)
    ImageView ivArrow7;

    private NetworkManager mNetworkManager;
    private EditText editTextInput;
    private TextView textView;
    private String qrCodeUri;
    private String subCompany;
    private String createSubCompany;
    private static final int CALLBACK_GET_COMPANY_INFO = 1;
    private static final int CALLBACK_SET_COMPANY_INFO = 2;
    private static final int CALLBACK_UPLOAD_COMPANY_LOGO = 3;
    private static final int CALLBACK_SET_COMPANY_LOGO = 4;
    private static final int CALLBACK_CRATE_SUBCOMPANY = 5;
    private boolean mEdit;
    private boolean mCreate;
    private boolean isUpdate;

    @Override
    protected int getContentView() {
        return R.layout.activity_company_info_edit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.inject(this);
        mNetworkManager = getAppliction().getNetworkManager();
        mLoadingView = findViewById(R.id.loading);
        setTitle(getString(R.string.company_info));
        Intent intent = getIntent();
        createSubCompany = intent.getStringExtra("createSubCompany");
        subCompany = intent.getStringExtra("subCompany");

        if (TextUtil.isEmpty(createSubCompany)) {   //判断是否为创建新公司
            getCompanyInfo();
        } else {
            mCreate = true;
        }
        if (!TextUtils.isEmpty(subCompany) || !TextUtils.isEmpty(createSubCompany))
            rlCompanyQRcode.setVisibility(View.GONE);

        rlCompayAddress.setOnClickListener(this);
        rlCompanyQRcode.setOnClickListener(this);
        isEditPermission();
    }

    private void isEditPermission() {       //判断是否有编辑权限
        isUpdate = AppPermissionPresenter.hasPermission(this, AppPermission.Type.settings, AppPermission.Setting.company.toString());
        findViewById(R.id.btn_gs_js).setOnClickListener(this);
        if (isUpdate) {
            mEdit = true;
            ivCompanyName.setVisibility(View.VISIBLE);
            textView = initRightTv(getString(R.string.save));
            if (!TextUtils.isEmpty(subCompany) || !TextUtils.isEmpty(createSubCompany)) {
                ivArrow2.setVisibility(View.VISIBLE);
                rlCompanyName.setOnClickListener(this);
            }
            ivArrow1.setVisibility(View.VISIBLE);
            ivArrow4.setVisibility(View.VISIBLE);
            ivArrow5.setVisibility(View.VISIBLE);
            ivArrow6.setVisibility(View.VISIBLE);
            ivArrow7.setVisibility(View.VISIBLE);
            findViewById(R.id.iv_gs_gm).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_gs_lb).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_gs_wz).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_gs_xz).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_gs_wz).setOnClickListener(this);
            findViewById(R.id.btn_gs_xz).setOnClickListener(this);
            findViewById(R.id.btn_gs_gm).setOnClickListener(this);
            findViewById(R.id.btn_gs_lb).setOnClickListener(this);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtil.isEmpty(tvCompayAddress.getText().toString().replace(" ", ""))) {
                        showToast(R.string.get_address);
                        return;
                    } /*else if (TextUtil.isEmpty(tvCompanyHost.getText().toString())) {
                        showToast(R.string.company_principal_not_null);
                        return;
                    } else if (TextUtil.isEmpty(tvCompanyContact.getText().toString())) {
                        showToast(R.string.company_Contact_not_null);
                        return;
                    }*/
                    setCompanyInfo();
                }
            });
            initEvent();
        }
    }

    private void initEvent() {
        ivCompanyName.setOnClickListener(this);
        rlCompanyHost.setOnClickListener(this);
        rlCompanyContact.setOnClickListener(this);
        rlCompanyLegal.setOnClickListener(this);
        rlCompanyLicense.setOnClickListener(this);
    }

    private void getCompanyInfo() {
        showLoadingDialog(this, getString(R.string.loading));
        Map<String, String> params = new HashMap<>();
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        params.put("user_id", preferences.getString("uid", ""));
        params.put("tenant_id", preferences.getString("tenantId", ""));
        if (!TextUtil.isEmpty(subCompany)) {
            params.put("subCompany", subCompany);
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GET_COMPANY_INFO), params, this);
        mNetworkManager.load(CALLBACK_GET_COMPANY_INFO, path, this);
    }

    public String mCompanyName;

    private void setCompanyInfo() {
        showLoadingDialog(this, getString(R.string.saving));
        Map<String, String> params = new HashMap<>();
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        params.put("user_id", preferences.getString("uid", ""));
        params.put("tenant_id", preferences.getString("tenantId", ""));
        if (!TextUtil.isEmpty(subCompany)) {
            params.put("subCompany", subCompany);
        }
        String name = tvCompanyName.getText().toString();
        mCompanyName = name;
        params.put("name", name);
        String address = tvCompayAddress.getText().toString();
        if (!TextUtil.isEmpty(address))
            params.put("address", address);
        if (!TextUtil.isEmpty(mLatlng))
            params.put("latlng", mLatlng);

        String companyHost = tvCompanyHost.getText().toString();
//        if (!TextUtil.isEmpty(companyHost))
            params.put("principal", companyHost);

        String companyContact = tvCompanyContact.getText().toString();
//        if (!TextUtil.isEmpty(companyContact))
            params.put("contacts", companyContact);

        String legal = tvCompanyLegal.getText().toString();
//        if (!TextUtil.isEmpty(legal))
            params.put("corporation", legal);

        String license = tvCompanyLicense.getText().toString();
//        if (!TextUtil.isEmpty(license))
            params.put("port", license);

        String company_url = tv_gs_wz.getText().toString();
//        if (!TextUtils.isEmpty(company_url)) {
            params.put("tenant_url", company_url);
//        }

        if (!TextUtils.isEmpty(mComTypeName)) {
            params.put("tenant_nature", mComTypeName);
        }
        if (!TextUtils.isEmpty(mGuimoName)) {
            params.put("tenant_scale", mGuimoName);
        }
        if (!TextUtils.isEmpty(mHangyeName)) {
            params.put("tenant_industry", mHangyeName);
        }
        if (!TextUtils.isEmpty(mCompanyDesc)) {
            params.put("tenant_desc", mCompanyDesc);
        }

        if (!TextUtil.isEmpty(createSubCompany)) {          //判断是否为创建分公司
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_CRATE_SUBCOMPANY), params, this);
            mNetworkManager.load(CALLBACK_CRATE_SUBCOMPANY, path, this);
        } else {
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SET_COMPANY_INFO), params, this);
            mNetworkManager.load(CALLBACK_SET_COMPANY_INFO, path, this);
        }
    }

    private static final int TAKE_PICTURE = 10;
    private static final int FROM_PHOTO = 11;
    private static final int PHOTO_CLIP = 12;
    private String path = "";
    public static final int HANGYE = 2;
    public static final int GONGSIXINGZHI = 4;
    public static final int GUIMO = 5;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gs_wz://网址
                showInputDialog(tv_gs_wz, getString(R.string.vancloud_input_company_website),true);
                break;
            case R.id.btn_gs_xz: {
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", getString(R.string.vancloud_select_company_nature));
                intent.putExtra("style", "company");
                intent.putExtra("id", mComTypeId);
                intent.setData(Uri.parse(URLAddr.URL_RESUME_COMPANYTYPE));
                startActivityForResult(intent, GONGSIXINGZHI);
            }
            break;
            case R.id.btn_gs_gm: {
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", getString(R.string.vancloud_select_company_size));
                intent.putExtra("style", "company");
                intent.putExtra("id", mGuimoId);
                intent.setData(Uri.parse(URLAddr.URL_RESUME_COSIZE));
                startActivityForResult(intent, GUIMO);
            }
            break;
            case R.id.btn_gs_lb: {
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", getString(R.string.vancloud_select_industry));
                intent.putExtra("id", mHangyeId);
                intent.putExtra("style", "company");
                intent.putExtra("type", DictSelectActivity.DICT_TITLE);
                intent.setData(Uri.parse(URLAddr.URL_INDUSTRY));
                startActivityForResult(intent, HANGYE);
            }
            break;
            case R.id.btn_gs_js: {
                Intent intent = new Intent(this, InputActivity.class);
                intent.putExtra("style", "tenant");
                intent.putExtra("edit",AppPermissionPresenter.hasPermission(this, AppPermission.Type.settings, AppPermission.Setting.company.toString()));
                intent.putExtra("title", getString(R.string.vancloud_company_introduction));
                intent.putExtra("hint", getString(R.string.vancloud_input_company_introduction));
                intent.putExtra("content", mCompanyDesc);
                startActivityForResult(intent, 101);
            }
            break;
            case R.id.rl_company_name:
                showInputDialog(tvCompanyName, getString(R.string.please_input_company_name),false);
                break;
            case R.id.rl_compay_address: {
                Intent intent = new Intent(this, MapLocationActivity.class);
                intent.putExtra("latlng", mLatlng);
                intent.putExtra("address", mAddress);
                if (isUpdate) {
                    intent.putExtra("edit", mEdit);
                } else {
                    intent.putExtra("edit", false);
                }

                startActivityForResult(intent, 1001);
            }
            break;
            case R.id.rl_company_host:        //公司负责人
                showInputDialog(tvCompanyHost, getString(R.string.vancloud_input_company_principal),true);
                break;
            case R.id.rl_company_contact:     //获取联系人
                showInputDialog(tvCompanyContact, getString(R.string.vancloud_input_company_contacts),true);
                break;
            case R.id.rl_company_legal://法人
                showInputDialog(tvCompanyLegal, getString(R.string.input_company_legal),true);
                break;
            case R.id.rl_company_license://营业执照
                showInputDialog(tvCompanyLicense, getString(R.string.hint_company_number),true);
                break;
            case R.id.rl_company_QRcode: {
                Intent intent = new Intent(CompanyInfoEditActivity.this, QrCodeActivity.class);
                if (TextUtils.isEmpty(qrCodeUri)) {
                    qrCodeUri = ApiUtils.generatorUrl(this, URLAddr.URL_EWM);
                }
                intent.putExtra("qrCodeUri", qrCodeUri);
                startActivity(intent);
            }
            break;
            case R.id.iv_company_name:
                if (mEdit) {
                    new ActionSheetDialog(this)
                            .builder()
                            .setCancelable(true)
                            .setCanceledOnTouchOutside(true)
                            .addSheetItem(getString(R.string.take), ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
//                                            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                            File file = new File(FileCacheUtils.getImageDir(getApplicationContext())
//                                                    , String.valueOf(System.currentTimeMillis()) + ".jpg");
//                                            path = file.getPath();
//                                            Uri imageUri = Uri.fromFile(file);
//                                            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                                            startActivityForResult(openCameraIntent, TAKE_PICTURE);
                                            PermissionsChecker mChecker = new PermissionsChecker(CompanyInfoEditActivity.this);
                                            if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                                                // 请求权限
                                                PermissionsActivity.startActivityForResult(CompanyInfoEditActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
                                            } else {
                                                // 全部权限都已获取
                                                EasyPhotos.createCamera(CompanyInfoEditActivity.this)
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
                } else {
                    if (!TextUtils.isEmpty(mLogoUrl)) {
                        List<ImageInfo> imgInfo = new ArrayList<>();
                        imgInfo.add(new ImageInfo(mLogoUrl, mLogoUrl));
                        String json = new Gson().toJson(imgInfo);
                        Intent intent = new Intent("com.vgtech.imagecheck");
                        intent.putExtra("listjson", json);
                        intent.putExtra("numVisible", false);
                        startActivity(intent);
                    }
                }

                break;
            default:
                super.onClick(v);
                break;
        }
    }


    public void showInputDialog(final TextView view, final String explain,final boolean canEmpty) {
        AlertDialog dialog = new AlertDialog(this).builder().setTitle(explain);
        editTextInput = dialog.setEditer();
        editTextInput.setText(view.getText());
        editTextInput.setSelection(view.getText().length());
        dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputString = editTextInput.getText().toString();
                if (!TextUtil.isEmpty(inputString)||canEmpty) {
                    view.setText(inputString);
                }
            }
        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 101:
                mCompanyDesc = data.getStringExtra("content");
                TextView tv_gs_js = (TextView) findViewById(R.id.tv_gs_js);
                tv_gs_js.setText(mCompanyDesc);
                break;
            case HANGYE: {
                String id = data.getStringExtra("id");
                String name = data.getStringExtra("name");
                String referCode = data.getStringExtra("referCode");
                mHangyeId = id;
                mHangyeName = name;
                TextView nameTv = (TextView) findViewById(R.id.tv_gs_lb);
                nameTv.setText(name);
                nameTv.setTag(referCode);
            }
            break;
            case GONGSIXINGZHI: {
                String id = data.getStringExtra("id");
                String name = data.getStringExtra("name");
                String referCode = data.getStringExtra("referCode");
                mComTypeId = id;
                mComTypeName = name;
                TextView nameTv = (TextView) findViewById(R.id.tv_gs_xz);
                nameTv.setText(name);
                nameTv.setTag(referCode);
            }
            break;
            case GUIMO: {
                String id = data.getStringExtra("id");
                String name = data.getStringExtra("name");
                String referCode = data.getStringExtra("referCode");
                mGuimoId = id;
                mGuimoName = name;
                TextView nameTv = (TextView) findViewById(R.id.tv_gs_gm);
                nameTv.setText(name);
                nameTv.setTag(referCode);
            }
            break;
            case 1001: {
                mLatlng = data.getStringExtra("latlng");
                mAddress = data.getStringExtra("address");
                tvCompayAddress.setText(mAddress);
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
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private View mLoadingView;

    private void uploadPhoto(String imgPath) {
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setBackgroundResource(R.drawable.loading_process);
        AnimationDrawable drawable = (AnimationDrawable) mLoadingView
                .getBackground();
        drawable.start();


        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        if (!TextUtil.isEmpty(subCompany)) {
            params.put("subCompany", subCompany);
        }
        params.put("type", "15");
        FilePair filePair = new FilePair("pic", new File(imgPath));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_IMAGE), params, filePair);
        mNetworkManager.load(CALLBACK_UPLOAD_COMPANY_LOGO, path, this);
    }

    private void updatePhoto(String url) {

        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setBackgroundResource(R.drawable.loading_process);
        AnimationDrawable drawable = (AnimationDrawable) mLoadingView
                .getBackground();
        drawable.start();

        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", preferences.getString("uid", ""));
        params.put("tenant_id", preferences.getString("tenantId", ""));
        if (!TextUtil.isEmpty(subCompany)) {
            params.put("subCompany", subCompany);
        }
        try {
            params.put("logo", url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SET_COPANY_LOGO), params, this);
        mNetworkManager.load(CALLBACK_SET_COMPANY_LOGO, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        mLoadingView.setVisibility(View.GONE);
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_GET_COMPANY_INFO:
                parseCompanyInfo(path, rootData);
                break;
            case CALLBACK_SET_COMPANY_INFO:
                /*sendBroadcast(new Intent(GroupReceiver.REFRESH));
                UserAccount account = new PreferencesController().getAccount();
                account.tenant_name=tvCompanyName.getText().toString();
                new PreferencesController().storageAccount(account);
                setResult(RESULT_OK);*/
                Toast.makeText(CompanyInfoEditActivity.this,
                        R.string.company_info_set_success, Toast.LENGTH_SHORT).show();
                finish();
                break;
            case CALLBACK_UPLOAD_COMPANY_LOGO: {
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    List<FileInfo> fileInfos = JsonDataFactory.getDataArray(FileInfo.class, dataArray);
                    String url = fileInfos.get(0).url;
                    mLogoUrl = url;
                    ImageOptions.setImage(ivCompanyName,mLogoUrl);
                    if (!mCreate) {
                        updatePhoto(url);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case CALLBACK_SET_COMPANY_LOGO:
                if (mCreate) {
                    Toast.makeText(CompanyInfoEditActivity.this,
                            R.string.add_child_company, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("name", mCompanyName);
                    intent.putExtra("id", subCompany);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
            case CALLBACK_CRATE_SUBCOMPANY:
                try {
                    subCompany = rootData.getJson().getJSONObject("data").getString("subCompanyId");
                    sendBroadcast(new Intent(GroupReceiver.REFRESH));
                    if (!TextUtils.isEmpty(mLogoUrl)) {
                        updatePhoto(mLogoUrl);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("name", mCompanyName);
                        intent.putExtra("id", subCompany);
                        setResult(RESULT_OK, intent);
                        Toast.makeText(CompanyInfoEditActivity.this,
                                R.string.add_child_company, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }


    }

    private String mLatlng;
    private String mAddress;
    private String mHangyeId, mComTypeId, mGuimoId;
    private String mHangyeName, mComTypeName, mGuimoName;
    private String mCompanyDesc;
    private String mLogoUrl;

    private void parseCompanyInfo(NetworkPath path, RootData rootData) {
        try {

            JSONObject jsonObject = rootData.getJson();
            JSONObject resutObject = jsonObject.getJSONObject("data");
            String hangye = resutObject.getString("tenant_industry");
            mHangyeName = hangye;
            String type = resutObject.getString("tenant_nature");
            mComTypeName = type;
            String guimo = resutObject.getString("tenant_scale");
            mGuimoName = guimo;
            mCompanyDesc = resutObject.getString("tenant_desc");

            TextView tv_gs_js = (TextView) findViewById(R.id.tv_gs_js);
            TextView tv_gs_xz = (TextView) findViewById(R.id.tv_gs_xz);
            TextView tv_gs_gm = (TextView) findViewById(R.id.tv_gs_gm);
            TextView iv_gs_lb = (TextView) findViewById(R.id.tv_gs_lb);
            tv_gs_wz.setText(resutObject.getString("tenant_url"));
            tv_gs_js.setText(mCompanyDesc);
            tv_gs_xz.setText(type);
            tv_gs_gm.setText(guimo);
            iv_gs_lb.setText(hangye);

            qrCodeUri = resutObject.getString("qrcode");
            mAddress = resutObject.getString("address");
            mLatlng = resutObject.getString("latlng");
            tvCompayAddress.setText(mAddress);
            tvCompanyName.setText(resutObject.getString("name"));
            tvCompanyHost.setText(resutObject.getString("principal"));
            tvCompanyContact.setText(resutObject.getString("contacts"));
            tvCompanyLicense.setText(resutObject.getString("port"));
            tvCompanyLegal.setText(resutObject.getString("corporation"));
            String logoUri = resutObject.getString("logo");
            mLogoUrl = logoUri;
            if (!TextUtil.isEmpty(logoUri)) {
                ImageOptions.setImage(ivCompanyName,logoUri);
                ivCompanyName.setVisibility(View.VISIBLE);
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

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

}
