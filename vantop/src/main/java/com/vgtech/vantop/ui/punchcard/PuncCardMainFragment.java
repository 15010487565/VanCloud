
package com.vgtech.vantop.ui.punchcard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.huantansheng.easyphotos.EasyPhotos;
import com.igexin.sdk.PushManager;
import com.vgtech.common.Constants;
import com.vgtech.common.NetworkHelpers;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.image.ImageUtility;
import com.vgtech.common.image.ImgGridAdapter;
import com.vgtech.common.image.PhotoActivity;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseFragment;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.permissions.PermissionsUtil;
import com.vgtech.common.ui.zxing.zxing.ToastUtil;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.common.utils.AppPermissionPresenterProxy;
import com.vgtech.common.utils.Des3Util;
import com.vgtech.common.utils.DeviceUtils;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.utils.LogUtils;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.CardArea;
import com.vgtech.vantop.moudle.Coord;
import com.vgtech.vantop.moudle.PunchCardInitData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
import com.vgtech.vantop.utils.AppModulePresenterVantop;
import com.vgtech.vantop.utils.PreferencesController;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 打卡
 * Created by shilec on 2016/7/15.
 */
public class PuncCardMainFragment extends BaseFragment implements HttpListener,
        View.OnClickListener, BDLocationListener, ReLoadFragment, PermissionsUtil.IPermissionsCallback {

    private final String TAG = "PuncCardMainFragment";
    //初始化加载数据
    private final int CALLBACK_INITDATA = 0X001;
    //提交打卡数据
    private final int CALLBACK_POSTDATA = 0X002;
    private PunchCardInitData mDatas;

    private LocationClient mLClient;

    private TextView mTvPcTimes;
    private TextView mTvPcTime;
    private TextView mTvPcAddr;
    private TextView mTvName;
    private LinearLayout mTvPcBtn;
    private EditText mEtDiary;


    //提交的照片列表 最多9张
    private String mLongtitude;
    //纬度
    private String mLatitude;
    //地址
    private String mAddress;
    //说说 最多140字
    private String mRemark;
    private SimpleDraweeView mHead;

    //imgs
    private NoScrollGridview mGridImags;
    private ImgGridAdapter adapter;
    //    private ImageView mIvAd;
    private LinearLayout mDiaryParent;
    private int mAreaId = 0;


    @Override
    protected int initLayoutId() {
        return R.layout.punchcard_main_fragment;
    }

    private static final int TAKE_PICTURE = 0x000000;
    private static final int FROM_PHOTO = 0x000001;
    private static final int DELETE_PICTURE = 0x000002;
    //    private String path = "";
    private Timer mLocalTimer;
    private View mRetryView;
    private int flag;
    /**
     * 相机权限
     */
    protected static final int CAMERA_REQUESTCODE = 20001;
    protected static final String[] CAMERAPERMISSION = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE,
            PermissionsUtil.Permission.Storage.READ_EXTERNAL_STORAGE,
            PermissionsUtil.Permission.Location.ACCESS_FINE_LOCATION
    };
    /**
     * 手机唯一识别码
     */
    protected static final int READPHONESTATE_REQUESTCODE = 20003;
    protected static final String[] READPHONESTATEPERMISSION = {
            android.Manifest.permission.READ_PHONE_STATE,
    };
    /**
     * 定位
     */
    protected static final int LOCATION_REQUESTCODE = 20002;
    /**
     * 打卡全选
     */
    PermissionsUtil permissionsUtil;

    @Override
    protected void initView(View view) {
        //touch hide softinput dialog
        view.findViewById(R.id.ll_touchview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return false;
            }
        });
        if (Constants.DEBUG) {
            view.findViewById(R.id.btn_mapaddress).setOnClickListener(this);
        }
        mRetryView = view.findViewById(R.id.tip_location_error);
        view.findViewById(R.id.btn_request_location).setOnClickListener(this);
        mTvPcTimes = (TextView) view.findViewById(R.id.tv_punchcard_times);
        mTvPcTime = (TextView) view.findViewById(R.id.tv_punchcard_time);
        mTvPcAddr = (TextView) view.findViewById(R.id.tv_punchcard_addr);
        mTvPcBtn = (LinearLayout) view.findViewById(R.id.tv_punchcard);
        mEtDiary = (EditText) view.findViewById(R.id.et_diary);
        mGridImags = (NoScrollGridview) view.findViewById(R.id.grid_imgs);
        mHead = (SimpleDraweeView) view.findViewById(R.id.iv_head);
        mTvName = (TextView) view.findViewById(R.id.tv_name);
        mDiaryParent = (LinearLayout) view.findViewById(R.id.ll_diary_parent);


        //设置头像
        PreferencesController prf = new PreferencesController();
        prf.context = getActivity();
        final UserAccount account = prf.getAccount();
        String url = account.photo;//
        //VanTopUtils.getImageUrl(getActivity(),PrfUtils.getStaff_no(getActivity()));
        if (url != null) {
            ImageOptions.setUserImage(mHead, url);
            mTvName.setText(account.user_name);
            mHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), VantopUserInfoActivity.class);
                    intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, PrfUtils.getStaff_no(getActivity()));
                    startActivity(intent);
                }
            });
        }
        mTvPcBtn.setOnClickListener(this);
        mTvPcTimes.setOnClickListener(this);
        adapter = new ImgGridAdapter(getActivity(), mTvPcBtn);
        adapter.update();
        mGridImags.setAdapter(adapter);
        mGridImags.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                if (arg2 == com.vgtech.common.image.Bimp.drr.size()) {
                    if (view.findViewById(R.id.item_grida_image).getVisibility() == View.VISIBLE) {

                        PermissionsChecker mChecker = new PermissionsChecker(getActivity());
                        if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                            // 请求权限
                            PermissionsActivity.startActivityForResult(getActivity(), CAMERA_REQUESTCODE, CAMERAPERMISSION);
                        } else {
                            // 全部权限都已获取
                            EasyPhotos.createCamera(PuncCardMainFragment.this)
                                    .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                    .start(TAKE_PICTURE);
                        }
                    }
                } else {
                    Intent intent = new Intent(getActivity(),
                            PhotoActivity.class);
                    intent.putExtra("ID", arg2);
                    startActivityForResult(intent, DELETE_PICTURE);
                }
            }
        });
        mGridImags.setItemClick(true);
        //设置时间
        //打卡次数
        mTvPcTimes.setText(Html.fromHtml("<u>" + "0" + "</u>"));
        if (mDatas != null && !TextUtils.isEmpty(mDatas.getTimes())) {
            String content = mDatas.getTimes();
            if (TextUtils.equals("null", mDatas.getTimes()) || TextUtils.equals("NULL", mDatas.getTimes())) {

            } else
                mTvPcTimes.setText(Html.fromHtml("<u>" + content + "</u>"));
        }
        mTvPcBtn.setEnabled(false);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case TAKE_PICTURE:
                showLoadingDialog(getActivity(), "");
                //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                if (resultPaths != null && resultPaths.size() > 0) {
                    String path = resultPaths.get(0);
                    Log.e("TAG_path", "path=" + path);
                    Bitmap bm = Bimp.getimage(path);
                    bm = ImageUtility.checkFileDegree(path, bm);
                    String newStr = "";
                    try {
                        newStr = path.substring(
                                path.lastIndexOf("/") + 1,
                                path.lastIndexOf("."));
                    } catch (Exception e) {
                        newStr = String.valueOf(System.currentTimeMillis());
                    }
//                    File lastFile = new File(path);
//                    if (lastFile.exists())
//                        lastFile.delete();
                    path = FileUtils.saveBitmap(getActivity(), bm, "" + newStr, "jpg");
                    File file = new File(path);
                    Log.e("puncard", "----" + file.length());
                    if (com.vgtech.common.image.Bimp.drr.size() < 9 && resultCode == -1) {
                        com.vgtech.common.image.Bimp.drr.add(path);
                    }
                    dismisLoadingDialog();
                    if (adapter != null)
                        adapter.update();
                }
                break;
            case DELETE_PICTURE:
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            case FROM_PHOTO:
                if (adapter != null)
                    adapter.update();
                break;
            case CAMERA_REQUESTCODE:
                //监听跳转到权限设置界面后再回到应用
                permissionsUtil.onActivityResult(requestCode, resultCode, data);

                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PunchCardActivity act = (PunchCardActivity) getActivity();
        act.setSearchVisiable(false);
        loadInitData(true);
        initLocation();
        //开启定位权限
        permissionsUtil = PermissionsUtil
                .with(this)
                .requestCode(20000)
                .isDebug(true)//开启log
                .permissions(PermissionsUtil.Permission.Storage.READ_EXTERNAL_STORAGE,
                        PermissionsUtil.Permission.Location.ACCESS_FINE_LOCATION)
                .request();

    }

    private void loadInitData(boolean show) {
        if (show)
            showLoadingDialog(getActivity(), "");
        String path = ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SCHEDULE_CLOCKINPARAM);
        Map<String, String> params = new HashMap<>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        NetworkPath np = new NetworkPath(path, params, getActivity());
        getApplication().getNetworkManager().load(CALLBACK_INITDATA, np, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getNetTime(mTvPcTime);
        mLClient.start();
        Log.e("chen_zhanyang", "onResume: 开始定位");
        adapter.update();
        boolean isOpenVirLocation = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        if (Build.VERSION.SDK_INT >= 22) {
            isOpenVirLocation = false;
        }
        if (isOpenVirLocation) {
            Toast.makeText(getActivity(), getString(R.string.vantop_forbid_virlocation), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        //检查root权限，手机被root不能打卡
        if (DeviceUtils.checkRoot()) {
            ToastUtil.toast(getActivity(), R.string.toast_close_root);
            getActivity().finish();
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        com.vgtech.common.image.Bimp.drr.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocalTimer.cancel();
        mLClient.stop();
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (resId == R.id.tv_punchcard) {
            //打卡权限申请
            permissionsUtil = PermissionsUtil
                    .with(this)
                    .requestCode(READPHONESTATE_REQUESTCODE)
                    .isDebug(true)//开启log
                    .permissions(READPHONESTATEPERMISSION)
                    .request();

        } else if (resId == R.id.btn_mapaddress) {

            if (mDatas != null && mDatas.getJson() != null) {
                //开启定位权限
                permissionsUtil = PermissionsUtil
                        .with(this)
                        .requestCode(LOCATION_REQUESTCODE)
                        .isDebug(true)//开启log
                        .permissions(PermissionsUtil.Permission.Storage.READ_EXTERNAL_STORAGE,
                                PermissionsUtil.Permission.Location.ACCESS_FINE_LOCATION)
                        .request();
            }
        } else if (resId == R.id.btn_request_location) {
            if (mDatas == null) {
                loadInitData(true);
            } else if (mLClient != null) {
                loadInitData(false);
                mRetryView.setVisibility(View.INVISIBLE);
                mTvPcAddr.setText(R.string.vantop_locationing);
                mLClient.requestLocation();
            }
        } else if (resId == R.id.tv_punchcard_times) {
            if (AppPermissionPresenterProxy.hasPermission(getActivity(), AppPermission.Type.clock_out, AppPermission.ClockOut.punch_record.toString())) {
//                ((PunchCardActivity) getActivity()).setCurrentPage(1);
                Intent intent = new Intent(getActivity(), CardInfoByDayActivity.class);
                startActivity(intent);
            }
        }
        //添加照片
    }

    private boolean mHasNoArea;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        Log.e("TAG_打卡","callbackId="+callbackId+";jsonObject="+rootData.getJson());

        boolean safe = callbackId == CALLBACK_INITDATA ? ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, false) : VanTopActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, false);
        if (!safe) {
            JSONObject jsonObject = rootData.getJson();
            try {
                if (jsonObject != null && jsonObject.has("msg")) {
                    Log.i(TAG, "dataLoaded: mHasNoArea = true");
                    mHasNoArea = true;
                    String msg = jsonObject.getString("msg");
                    if (!TextUtils.isEmpty(msg)) {
                        mTvPcAddr.setText(msg);
                    }
                }
                if (jsonObject != null && jsonObject.has("_code")) {
                    String code = jsonObject.getString("_code");
                    if (!"1001".equals(code) && !"1002".equals(code)) {
                        if (jsonObject.has("_msg")) {
                            String _msg = jsonObject.getString("_msg");
                            Toast.makeText(getActivity(), _msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (!NetworkHelpers.isNetworkAvailable(getActivity())) {
                        Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return;
        }
        switch (callbackId) {
            case CALLBACK_INITDATA: {
                mHasNoArea = false;
                try {
                    onInitData(rootData.getJson().getJSONObject("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;

            case CALLBACK_POSTDATA: {
                getActivity().sendBroadcast(new Intent("sign_card"));
                loadInitData(true);
                Toast.makeText(getActivity(), getString(R.string.vantop_punchcard_successed), Toast.LENGTH_SHORT).show();
                reLoad();
//                if (AppPermissionPresenterProxy.hasPermission(getActivity(), AppPermission.Type.clock_out, AppPermission.ClockOut.punch_record.toString())) {
//                    ((PunchCardActivity) getActivity()).setCurrentPage(1);
//                }
            }
            break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        dismisLoadingDialog();
    }

    @Override
    public void onResponse(Object response) {
        dismisLoadingDialog();
    }

    /**
     * 数据初始化完回掉
     */
    private void onInitData(JSONObject json) {
        mDatas = PunchCardInitData.fromJson(json.toString());
        mDatas.setJson(json);
        if (mDatas == null)
            return;
        Log.e("TAG_定位","mDatas="+mDatas.toString());
        try {
            if (json.has("clockArea") && json.getJSONObject("clockArea").has("coord")) {
                mDatas.isNew = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mDatas.getTimes() != null) {
            mTvPcTimes.setText(Html.fromHtml("<u>" + mDatas.getTimes() + "</u>"));
        } else {
            mTvPcTimes.setText(Html.fromHtml("<u>" + "0" + "</u>"));
        }
        if (!TextUtils.isEmpty(mLatitude) && !TextUtils.isEmpty(mLatitude)) {
            if (isInPuncardRange(mDatas)) {
                mRetryView.setVisibility(View.INVISIBLE);
                mTvPcBtn.setEnabled(true);

                boolean openPermission = AppModulePresenterVantop.isOpenPermission(this.getContext(), AppModulePresenterVantop.Type.clock_out, "qita:showgongsiweizh");
                if (openPermission) {
                    mTvPcAddr.setText(mAddress);
                } else {
                    mTvPcAddr.setText(TenantPresenter.getCurrentTenant(getActivity()).tenant_name);
                }
            } else {
                mRetryView.setVisibility(View.VISIBLE);
                mTvPcBtn.setEnabled(false);
                mTvPcAddr.setText(getString(R.string.vantop_outofrang));
            }
        } else {
            mTvPcBtn.setEnabled(false);
        }
    }

    /***
     * 每10秒刷新时间
     */
    private void getNetTime(TextView tv) {

        final TextView tv1 = tv;
        mLocalTimer = new Timer("getLocalTime");
        mLocalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final long lTime = System.currentTimeMillis();
                final Date date = new Date(lTime);
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv1.setText(sdf.format(date));
                    }
                });

            }
        }, 0, 10000);
    }

    /***
     * 初始化定位服务
     */
    private void initLocation() {

        mLClient = new LocationClient(getActivity().getApplicationContext());
        mLClient.registerLocationListener(this);

        LocationClientOption option = new LocationClientOption();
        option.disableCache(true);
        option.setPriority(LocationClientOption.GpsFirst);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5 * 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        mLClient.setLocOption(option);
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        LogUtils.createLogFile(getActivity(),"BDLocation.log","mHasNoArea》》》》》"+mHasNoArea+"\n");
        if (mHasNoArea) {
            Log.i("chen", "onReceiveLocation: mHasNoArea");
            return;
        }
        int errorCode = bdLocation.getLocType();
        LogUtils.createLogFile(getActivity(),"BDLocation.log","errorCode》》》》》"+errorCode+"\n");
        if (bdLocation != null && bdLocation.hasAddr()) {
            mAddress = bdLocation.getAddrStr();
            mLongtitude = bdLocation.getLongitude() + "";
            mLatitude = bdLocation.getLatitude() + "";
            String address = bdLocation.getAddrStr();
            int locationType = DeviceUtils.checkOp(getActivity(), 1);
            if (TextUtils.isEmpty(address) && locationType == 1) {
                Toast.makeText(getActivity(), com.vgtech.vantop.R.string.vantop_location_refused, Toast.LENGTH_SHORT).show();
            }
            if (!TextUtils.isEmpty(bdLocation.getAddrStr())) {
                if (mDatas != null && isInPuncardRange(mDatas)) {
                    Log.i(TAG, "onReceiveLocation: 在打卡范围内");
                    mRetryView.setVisibility(View.INVISIBLE);
                    mTvPcBtn.setEnabled(true);
                    boolean openPermission = AppModulePresenterVantop.isOpenPermission(this.getContext(), AppModulePresenterVantop.Type.clock_out, "qita:showgongsiweizh");
                    if (openPermission) {
                        mTvPcAddr.setText(mAddress);
                    } else {
                        mTvPcAddr.setText(TenantPresenter.getCurrentTenant(getActivity()).tenant_name);
                    }
                } else {
                    Log.i(TAG, "onReceiveLocation: 不在打卡范围内");
                    mRetryView.setVisibility(View.VISIBLE);
                    mTvPcBtn.setEnabled(false);
//                    if (Constants.DEBUG){
//                        ToastUtil.toast(getActivity(),"不在打卡范围内！");
//                    }
                    mTvPcAddr.setText(getString(R.string.vantop_outofrang));
                }
            } else {
                mTvPcBtn.setEnabled(false);
//                if (Constants.DEBUG){
//                    ToastUtil.toast(getActivity(),"无法定位！");
//                }
                int type = DeviceUtils.checkOp(getActivity(), 1);
                if (type == 1) {
                    Toast.makeText(getActivity(), R.string.vantop_location_refused, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            mLongtitude = null;
            mLatitude = null;
        }
    }


    /***
     * 每个区域以 "#" 号分割，每组坐标以 ",," 号分割,经纬度之间以 "," 分割
     *
     * @return
     */

    public boolean isInPuncardRange(PunchCardInitData data) {
        try {
            if (!data.isNew) {
                String longlat = data.getLonglat();
                List<ArrayList<LatLng>> comLats = new ArrayList<ArrayList<LatLng>>();
                if (TextUtils.isEmpty(longlat) || !longlat.contains(",")) {
                    return false;
                }
                if (longlat.contains("#")) {

                    String[] companies = longlat.split("#");
                    for (String company : companies) {
                        ArrayList<LatLng> latLngs = new ArrayList<>();
                        //每个公司是一个用经纬度列表标识的区域
                        String[] tudes = company.split(",,");
                        for (String tude : tudes) {
                            String lgtude = tude.split(",")[0];
                            String ltude = tude.split(",")[1];
                            LatLng latLng = new LatLng(Double.parseDouble(ltude), Double.parseDouble(lgtude));
                            latLngs.add(latLng);
                        }
                        //将每个公司的经纬度列表添加到一个集合中
                        comLats.add(latLngs);
                    }
                } else {

                    ArrayList<LatLng> latLngs = new ArrayList<>();
                    //每个公司是一个用经纬度列表标识的区域
                    String[] tudes = longlat.split(",,");
                    for (String tude : tudes) {
                        String lgtude = tude.split(",")[0];
                        String ltude = tude.split(",")[1];
                        LatLng latLng = new LatLng(Double.parseDouble(ltude), Double.parseDouble(lgtude));
                        latLngs.add(latLng);
                    }
                    //将每个公司的经纬度列表添加到一个集合中
                    comLats.add(latLngs);
                }
                for (ArrayList<LatLng> latLngs : comLats) {
                    if (SpatialRelationUtil.isPolygonContainsPoint(latLngs, new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongtitude)))) {
                        flag = 0;
                        return true;
                    }
                }
            } else {
                long currentTimeMillis = System.currentTimeMillis();
                List<CardArea> scheduleArea = JsonDataFactory.getDataArray(CardArea.class, data.getJson().getJSONArray("scheduleArea"));
                List<CardArea> leaArea = JsonDataFactory.getDataArray(CardArea.class, data.getJson().getJSONArray("leaArea"));
                long preClockintime = Long.parseLong(data.getJson().getString("preClockintime"));
                long sufclockinTime = Long.parseLong(data.getJson().getString("sufclockinTime"));
                int clockidRadius = Integer.parseInt(data.getJson().getString("clockidRadius"));
                for (CardArea cardArea : scheduleArea) {//日程
                    LatLng pCenter = new LatLng(Double.parseDouble(cardArea.latitude), Double.parseDouble(cardArea.longitude));
                    LatLng locPoint = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongtitude));
                    if (SpatialRelationUtil.isCircleContainsPoint(pCenter, clockidRadius, locPoint)) {
                        if (currentTimeMillis >= cardArea.getStartTime() - preClockintime
                                || currentTimeMillis <= cardArea.getEndTime() + sufclockinTime
                        ) {
                            flag = 2;
                            return true;
                        }
                    }
                }
                for (CardArea cardArea : leaArea) {//休假
                    LatLng pCenter = new LatLng(Double.parseDouble(cardArea.latitude), Double.parseDouble(cardArea.longitude));
                    LatLng locPoint = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongtitude));
                    if (SpatialRelationUtil.isCircleContainsPoint(pCenter, clockidRadius, locPoint)) {
                        if (currentTimeMillis >= cardArea.getStartTime() - preClockintime
                                || currentTimeMillis <= cardArea.getEndTime() + sufclockinTime
                        ) {
                            flag = 1;
                            return true;
                        }
                    }
                }

                JSONObject jsonObject = data.getJson().getJSONObject("clockArea");
                if (jsonObject != null && jsonObject.has("_code") && ("1001".equals(jsonObject.getString("_code")) || "1002".equals(jsonObject.getString("_code")))) {
//                    Log.i("chen", "isInPuncardRange: mHasNoArea = true");
                    mHasNoArea = true;
                    if (jsonObject != null && jsonObject.has("msg")) {
                        String msg = jsonObject.getString("msg");
                        if (!TextUtils.isEmpty(msg)) {
                            mTvPcAddr.setText(msg);
                        }
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
                List<Coord> coords = JsonDataFactory.getDataArray(Coord.class, jsonObject.getJSONArray("coord"));
                for (Coord coord : coords) {
                    if (coord.circle == 0) {
                        LatLng pCenter = new LatLng(coord.ltlatitude, coord.ltlongitufe);
                        LatLng locPoint = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongtitude));
                        if (SpatialRelationUtil.isCircleContainsPoint(pCenter, coord.getRadius(), locPoint)) {
                            flag = 0;
                            mAreaId = coord.areaId;
                            return true;
                        }
                    } else {
                        ArrayList<LatLng> latLngs = new ArrayList<>();
                        latLngs.add(new LatLng(coord.ltlatitude, coord.ltlongitufe));
                        latLngs.add(new LatLng(coord.rtlatitude, coord.rtlongitude));
                        latLngs.add(new LatLng(coord.rblatitude, coord.rblongitude));
                        latLngs.add(new LatLng(coord.lblatitude, coord.lblongitude));
                        if (SpatialRelationUtil.isPolygonContainsPoint(latLngs, new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongtitude)))) {
                            flag = 0;
                            mAreaId = coord.areaId;
                            return true;
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param cardNo
     * @param longtitude 经度
     * @param latitude   纬度
     * @param remark     说说 最多140字
     * @param imgs       提交的照片列表 最多九张
     */
    private void submitPunchCard(String cardNo, String termNo, String longtitude, String address,
                                 String latitude, String remark, List<String> imgs) {

        boolean openUpPicPermission = AppModulePresenterVantop.isOpenPermission(this.getContext(), AppModulePresenterVantop.Type.clock_out, "qita:paizhao");
        if (openUpPicPermission && imgs.size() == 0) {
            showToast("您需要拍照以后才能打卡！");
            dismisLoadingDialog();
            return;
        }
        if (TextUtils.isEmpty(cardNo)) {
            showToast("请先设置考勤卡号！");
            dismisLoadingDialog();
            return;
        }
        if (TextUtils.isEmpty(termNo)) {
            showToast("请先设置卡钟！");
            dismisLoadingDialog();
            return;
        }
        try {
            Map<String, String> params = new HashMap<>();
            params.put("cardNo", cardNo);
            params.put("termNo", termNo);
            params.put("longitude", longtitude);
            params.put("latitude", latitude);
            params.put("address", address);
            params.put("remark", remark);
            params.put("loginUserCode", PrfUtils.getStaff_no(getActivity()));
            String deviceId = ApiUtils.getDeviceId(getActivity());
            params.put("device_id", deviceId);
            String staffNo = PrfUtils.getStaff_no(getActivity());
            Des3Util des3Util = new Des3Util();
            des3Util.setSecretKey(staffNo);

            Map<String, String> postValues = new HashMap<>();
            String timestamp = des3Util.encode("cardNo=" + cardNo + "&termNo=" + termNo);
            String x = des3Util.encode("latitude=" + latitude + "&longitude=" + longtitude + "&address=" + address + "&remark=" + remark);
            String noticestr = des3Util.encode("loginUserCode=" + staffNo + "&device_id=" + deviceId);
            String vsign = ApiUtils.getSign(params, null);
            postValues.put("timestamp", timestamp);
            postValues.put("x", x);
            postValues.put("noticestr", noticestr);
            postValues.put("vsign", vsign);
            params.put("flag", String.valueOf(flag));
            params.put("areaId", mAreaId + "");
//            if (Constants.DEBUG) {
            postValues.putAll(params);
//            }
            String path = VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_PUNCHCARD_POSTDATA);
//        String path = "http://192.168.1.129:8080/vantopapp/"+UrlAddr.URL_PUNCHCARD_POSTDATA;
            List<FilePair> filePairs = null;
            if (!imgs.isEmpty()) {
                filePairs = new ArrayList<>();
                for (String url : imgs) {
                    FilePair pair = new FilePair("pictures", new File(url));
                    filePairs.add(pair);
                }
            }
            NetworkPath np = new NetworkPath(path, postValues, filePairs, getActivity(), true);
            getApplication().getNetworkManager().load(CALLBACK_POSTDATA, np, this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"打卡异常！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void reLoad() {
        Bimp.drr.clear();
        if (adapter != null)
            adapter.notifyDataSetChanged();
        if (mEtDiary != null)
            mEtDiary.setText("");
    }

    @Override
    public void onPermissionsGranted(int requestCode, String... permission) {
        Log.e("TAG_GetuiSdkService", "requestCode----" + requestCode);
        if (requestCode == READPHONESTATE_REQUESTCODE){
            String deviceId = ApiUtils.getDeviceId(getActivity());
            SharedPreferences preferences = PrfUtils.getSharePreferences(getActivity());
            String userId = preferences.getString("uid", "");
            String tenantId = preferences.getString("tenantId", "");
            Log.e("TAG_GetuiSdkService", "userId----" + userId);
            Log.e("TAG_GetuiSdkService", "tenantId----" + tenantId);
            Log.e("TAG_GetuiSdkService", "deviceId----" + deviceId);
            String alias = MD5.getMD5(userId + tenantId + deviceId);
            boolean result = PushManager.getInstance().bindAlias(getActivity(), alias,"android");
            Log.e("TAG_GetuiSdkService", "绑定别名----" + alias + "----结果----" + result);
            boolean pushTurnedOn = PushManager.getInstance().isPushTurnedOn(getActivity());
            Log.e("TAG_GetuiSdkService", "SDK服务状态----" + pushTurnedOn);
            if (mDatas != null && isInPuncardRange(mDatas)) {
                if (TextUtils.isEmpty(mDatas.getCardNo())) {
                    Toast.makeText(getActivity(), getString(R.string.cardno_not_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                showLoadingDialog(getActivity(), getString(R.string.vantop_submitdata), false);
                submitPunchCard(mDatas.getCardNo(), mDatas.getTermNo(),
                        mLongtitude, mAddress, mLatitude, mEtDiary.getText().toString(), adapter.getImage());
            } else {
                if (mDatas == null) {
                    Toast.makeText(getActivity(), getString(R.string.vantop_getdataerror), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.vantop_outofrang), Toast.LENGTH_SHORT).show();
                }
            }

        }else if (requestCode == LOCATION_REQUESTCODE){
            if (mDatas !=null){
                Intent intent = new Intent(getActivity(), PunchMapActivity.class);
                intent.putExtra("json", mDatas.getJson().toString() );
                startActivity(intent);
            }
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, String... permission) {
        //权限被拒绝回调
        Toast.makeText(getActivity(), getString(R.string.permissions_equipment), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //需要调用onRequestPermissionsResult
        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

