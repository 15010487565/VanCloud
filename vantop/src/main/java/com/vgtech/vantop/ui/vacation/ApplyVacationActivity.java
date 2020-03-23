package com.vgtech.vantop.ui.vacation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.image.ImageUtility;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.utils.CommonUtils;
import com.vgtech.common.utils.Emiter;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.utils.ImageCacheManager;
import com.vgtech.common.utils.wheel.WheelUtil;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.CcUserGridAdapter;
import com.vgtech.vantop.adapter.CcUserRecyAdapter;
import com.vgtech.vantop.moudle.StaffInfo;
import com.vgtech.vantop.moudle.VacationAppliesNew;
import com.vgtech.vantop.moudle.VacationApplyDetails;
import com.vgtech.vantop.moudle.VacationCode;
import com.vgtech.vantop.moudle.Vacations;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.approval.ApprovalStaffsActivity;
import com.vgtech.vantop.utils.GsonUtils;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 申请假期
 * Created by Duke on 2016/9/13.
 */
public class ApplyVacationActivity extends BaseActivity
        implements HttpListener<String>
        , CompoundButton.OnCheckedChangeListener
        , CcUserRecyAdapter.OnDeleteClickListener {

    private EditText durationSumView;
    private View headerView;
    private TextView typeView;
    private LinearLayout picsView;
    private EditText notesView;

    private View rootView;
    private View picContainer;
    private ImageView addPicImg;
    private TextView remarkView;
    private View selectTypeViewContainer;
    private TextView selectTypeView;
    private ToggleButton allDaySwitch;

    private TextView unitView;
    private View originContainer;
    private TextView originTypeView;
    private TextView originDurationSumView;
    private TextView timeView;
    private TextView originRemarkView;
    private TextView statusView;


    private NetworkManager networkManager;
    private List<VacationCode> vacationCodes = new ArrayList<>();
    private StaffInfo staffInfo;
    private String staffInfoListJson;

    private Vacations vacations;
    private String taskId;
    private String vacationsJson;

    private static final int CALLBACK_GET_CODES = 1;
    private static final int CALLBACK_GET_NEWS = 2;
    private static final int CALLBACK_GET_DURATION = 3;
    private static final int CALLBACK_APPLIES = 4;
    private static final int TAKE_PICTURE = 10;
    private static final int FROM_PHOTO = 11;
    private static final int PHOTO_CLIP = 12;

    private String path = "";
    private String imagePath = "";

    private boolean noteRequire;
    private boolean ccRequire;
    private boolean mPicRequire;
    private boolean isDurationModify;//是否可手动修改加班时长
    private float durationMin;

    private Button appButton;

    private TextView selectStartTimeTextView;
    private TextView selectEndTimeTextView;

    private HorizontalScrollView picScrollview;

    private RelativeLayout selectApprovalerClickView;
    private ImageView selectApprovalerImgView;
    private LinearLayout selectApprovalerView;
    private TextView approvalerNameView;
    //抄送人低于10时，使用的布局
    private NoScrollGridview ccGridView;
    private CcUserGridAdapter ccUserGridAdapter;
    //抄送人大于10时使用的布局
    private RecyclerView rc;
    private CcUserRecyAdapter rcAdapter;
    //抄送人数据源
    private  ArrayList<Node> userSelectList;

    private VacationApplyDetails details;
    private TextView startTimeView;
    private TextView endTimeView;

    private HorizontalScrollView originPicsScrollView;
    private LinearLayout originPicsView;

    private TextView ccUserCountView;
    private RelativeLayout mRlTimeType;


    @Override
    protected int getContentView() {
        return R.layout.apply_vacation_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.vantop_leave_apply));

        initView();
        initEvent();
        showView();
    }


    private void initView() {
        durationSumView = (EditText) findViewById(R.id.durationSumView);
        durationSumView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String etDurationSum = durationSumView.getText().toString();
                if (etDurationSum.length() == 1 && etDurationSum.equals(".")) {
                    durationSumView.setText("");
                }
            }


            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //设置不可编辑状态；
//        durationSumView.setFocusable(false);
//        durationSumView.setEnabled(false);

        headerView = findViewById(R.id.headerView);
        typeView = (TextView) findViewById(R.id.typeView);
        picsView = (LinearLayout) findViewById(R.id.picsView);
        notesView = (EditText) findViewById(R.id.notesView);

        rootView = findViewById(R.id.rootView);


        picContainer = findViewById(R.id.picContainer);
        remarkView = (TextView) findViewById(R.id.remarkView);
        selectTypeViewContainer = findViewById(R.id.selectTypeViewContainer);
        selectTypeView = (TextView) findViewById(R.id.selectTypeView);
        unitView = (TextView) findViewById(R.id.unitView);
        originContainer = findViewById(R.id.originContainer);

        allDaySwitch = (ToggleButton) findViewById(R.id.all_switch);
        mRlTimeType = (RelativeLayout) findViewById(R.id.rl_time_type_parent);
        addPicImg = (ImageView) findViewById(R.id.add_pic_img);

        appButton = (Button) findViewById(R.id.approval_button);
        selectStartTimeTextView = (TextView) findViewById(R.id.select_start_time_text);
        selectEndTimeTextView = (TextView) findViewById(R.id.select_end_time_text);
        picScrollview = (HorizontalScrollView) findViewById(R.id.pic_scrollview);

        selectApprovalerClickView = (RelativeLayout) findViewById(R.id.select_approvaler_click);
        selectApprovalerImgView = (ImageView) findViewById(R.id.select_approvaler_img);
        selectApprovalerView = (LinearLayout) findViewById(R.id.select_approvaler);
        approvalerNameView = (TextView) findViewById(R.id.approvaler_name);
        //10人抄送人
        ccGridView = (NoScrollGridview) findViewById(R.id.cc_gridview);
        ccUserCountView = (TextView) findViewById(R.id.cc_count);
        //超过10人抄送人
        rc = (RecyclerView) findViewById(R.id.rc);
        GridLayoutManager  linearLayoutManager = new GridLayoutManager(this,2);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rc.setLayoutManager(linearLayoutManager);
        rcAdapter = new CcUserRecyAdapter(this);
        rcAdapter.setDeteleItemClickListener(this);
        rc.setAdapter(rcAdapter);

        ccUserGridAdapter = new CcUserGridAdapter(ApplyVacationActivity.this, new ArrayList<Node>(), new OnCcUserRemoveListener() {
            @Override
            public void onRemove(Node node) {
                if (ccUserGridAdapter.getLists().size() == 0) {
                    ccGridView.setVisibility(View.GONE);
                    rc.setVisibility(View.GONE);
                    ccUserCountView.setText("");
                } else {
                    ccUserCountView.setText(getString(R.string.cc_count, ccUserGridAdapter.getLists().size() + ""));
                }
            }
        });
        ccGridView.setAdapter(ccUserGridAdapter);
    }

    /**
     * 保留两位小数正则
     *
     * @param number
     * @return
     */


    public static boolean isOnlyPointNumber(String number) {
        Pattern pattern = Pattern.compile("^\\d+\\.?\\d{0,2}$");
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    private String mLatlng;
    private String mAddress;
    private String mAddName;
    private TextView mAddressTv;

    public void initEvent() {
        appButton.setOnClickListener(this);
        selectTypeView.setOnClickListener(this);
        allDaySwitch.setOnCheckedChangeListener(this);
        addPicImg.setOnClickListener(this);
        mAddressTv = (TextView) findViewById(R.id.tv_address);
        findViewById(R.id.select_start_time_click).setOnClickListener(this);
        findViewById(R.id.select_end_time_click).setOnClickListener(this);
        findViewById(R.id.btn_location_select).setOnClickListener(this);
        selectApprovalerClickView.setOnClickListener(this);
        selectApprovalerClickView.setClickable(false);
        findViewById(R.id.select_cc_click).setOnClickListener(this);

    }

    public void showView() {

        Intent intent = getIntent();
        String details_json = intent.getStringExtra("detailsJson");
        String json = intent.getStringExtra("json");

        if (!TextUtils.isEmpty(details_json)) {
            headerView.setVisibility(View.GONE);
            originContainer.setVisibility(View.VISIBLE);
            selectTypeViewContainer.setVisibility(View.VISIBLE);
            try {
                VacationApplyDetails details = JsonDataFactory.getData(VacationApplyDetails.class, new JSONObject(details_json));
                List<String> piclist = GsonUtils.parseDataToList(details.getJson().getString("pics"));
                taskId = details.task_id;
                vacations = new Vacations(details);
                setOldData(details, piclist);
                getNews(details.code);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            headerView.setVisibility(View.VISIBLE);
            originContainer.setVisibility(View.GONE);
            selectTypeViewContainer.setVisibility(View.GONE);
            try {
                vacations = JsonDataFactory.getData(Vacations.class, new JSONObject(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
            typeView.setText(vacations.desc);
            getNews(vacations.code);
        }
    }

    public void setOldData(VacationApplyDetails details, List<String> piclist) {

        originTypeView = (TextView) findViewById(R.id.originTypeView);
        startTimeView = (TextView) findViewById(R.id.start_time_tv);
        endTimeView = (TextView) findViewById(R.id.end_time_tv);
        originDurationSumView = (TextView) findViewById(R.id.originDurationSumView);
        timeView = (TextView) findViewById(R.id.timeView);
        originRemarkView = (TextView) findViewById(R.id.originRemarkView);
        originPicsScrollView = (HorizontalScrollView) findViewById(R.id.origin_picsScrollView);
        originPicsView = (LinearLayout) findViewById(R.id.origin_picsView);
        statusView = (TextView) findViewById(R.id.statusView);


        originTypeView.setText(getString(R.string.vantop_old_vacation_type, details.desc));
        startTimeView.setText(details.from);
        endTimeView.setText(details.to);
        originDurationSumView.setText(details.num + " " + details.unit);
        timeView.setText(details.created_at);
        originRemarkView.setText(details.remark);
        if (piclist.size() > 0) {
//            picUrl = pics.get(0);
            originPicsScrollView.setVisibility(View.VISIBLE);
            for (String pic : piclist) {
                SimpleDraweeView imageView = new SimpleDraweeView(this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(CommonUtils.dpToPx(getResources(), 90), CommonUtils.dpToPx(getResources(), 70));
                lp.setMargins(0, 0, CommonUtils.dpToPx(getResources(), 10), 0);
                imageView.setLayoutParams(lp);
                originPicsView.addView(imageView);
                String imgUrl = "";
                if (pic.contains("?loginUserCode="))
                    imgUrl = pic;
                else
                    imgUrl = pic + "?loginUserCode=" + PrfUtils.getStaff_no(this);
                imgUrl = VanTopUtils.generatorImageUrl(this, imgUrl);
                ImageOptions.setImage(imageView, imgUrl);
                imageView.setTag(imgUrl);
                imageView.setOnClickListener(photoListener);
            }
        }
        statusView.setText(VanTopUtils.getStatusResId(details.status));
        selectTypeView.setText(getString(R.string.vantop_vacation_type, details.desc));


    }

    private void loadCodesData() {
        networkManager = getApplicationProxy().getNetworkManager();
        String url = VanTopUtils.generatorUrl(ApplyVacationActivity.this, UrlAddr.URL_VACATIONS_CODES);
        NetworkPath path = new NetworkPath(url, null, this, true);
        networkManager.load(CALLBACK_GET_CODES, path, this, false);
    }

    private void getNews(String code) {

        showLoadingDialog(this, getString(R.string.prompt_info_common));
        networkManager = getApplicationProxy().getNetworkManager();
        String url = VanTopUtils.generatorUrl(ApplyVacationActivity.this, UrlAddr.URL_APPLIES_NEW);
        Map<String, String> params = new HashMap<String, String>();
        params.put("code", code);
        NetworkPath path = new NetworkPath(url, params, this, true);
        networkManager.load(CALLBACK_GET_NEWS, path, this, false);
    }


    private void getDuration(String code, String startDate, String startTime, String endDate, String endTime) {

        showLoadingDialog(this, getString(R.string.prompt_info_common));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("code", code);
        params.put("startDate", startDate);
        params.put("startTime", startTime);
        params.put("endDate", endDate);
        params.put("endTime", endTime);

        String url = VanTopUtils.generatorUrl(ApplyVacationActivity.this, UrlAddr.URL_APPLIES_DURATION);
        NetworkPath path = new NetworkPath(url, params, this, true);
        networkManager.load(CALLBACK_GET_DURATION, path, this, false);
    }


    private void applies(String taskId, String code, String unit, String startDate, String startTime,
                         String endDate, String endTime, String duration, String supervisor,
                         String remark, String cc) {

        showLoadingDialog(this, getString(R.string.prompt_info_common));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        if (!TextUtils.isEmpty(taskId)) {
            params.put("taskId", taskId);
        }
        params.put("code", code);
        params.put("unit", unit);
        params.put("startDate", startDate);
        params.put("startTime", startTime);
        params.put("endDate", endDate);
        params.put("endTime", endTime);
        params.put("duration", duration);
        params.put("supervisor", supervisor);
        params.put("remark", encode(remark));
        params.put("cc", cc);
        params.put("isclock", mAppliesNew.isclockin() ? "1" : "0");
        if (mAppliesNew.isclockin()) {
            if (!TextUtils.isEmpty(mLatlng)) {
                String[] gps = mLatlng.split(",");
                if (gps.length > 1) {
                    params.put("latitude", gps[0]);
                    params.put("longitude", gps[1]);
                    params.put("radius", "200");
                }
            }
        }
        NetworkPath path = null;
        if (TextUtils.isEmpty(imagePath))
            path = new NetworkPath(VanTopUtils.generatorUrl(ApplyVacationActivity.this, UrlAddr.URL_APPLIES), params, this, true);
        else {
            Bitmap bm = Bimp.getimage(imagePath);
            bm = ImageUtility.checkFileDegree(imagePath, bm);
            String newStr = "";
            try {
                newStr = imagePath.substring(
                        imagePath.lastIndexOf("/") + 1,
                        imagePath.lastIndexOf("."));
            } catch (Exception e) {
                newStr = String.valueOf(System.currentTimeMillis());
            }
//            File lastFile = new File(imagePath);
//            if (lastFile.exists())
//                lastFile.delete();
            imagePath = FileUtils.saveBitmap(this, bm, "" + newStr, "jpg");
            FilePair filePair = new FilePair("pic", new File(imagePath));
            String url = VanTopUtils.generatorUrl(ApplyVacationActivity.this, UrlAddr.URL_APPLIES);
//            url = "http://192.168.2.46:8085/applies";
            path = new NetworkPath(url, params, filePair, this, true);

        }
        networkManager.load(CALLBACK_APPLIES, path, this, false);
    }

    private VacationAppliesNew mAppliesNew;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_GET_CODES:
                try {
                    vacationCodes = JsonDataFactory.getDataArray(VacationCode.class, rootData.getJson().getJSONArray("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showCodes();
                break;

            case CALLBACK_GET_NEWS:
                try {
                    mAppliesNew = JsonDataFactory.getData(VacationAppliesNew.class, rootData.getJson());
                    staffInfoListJson = rootData.getJson().getString("supers");
                    List<StaffInfo> staffInfos = JsonDataFactory.getDataArray(StaffInfo.class, new JSONArray(staffInfoListJson));

                    if (!mAppliesNew.enable) {
                        Toast.makeText(this, getString(R.string.vantop_apply_disable), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        return;
                    }
                    //TODO
                    isDurationModify = mAppliesNew.isDurationModify;
                    durationMin = Float.valueOf(String.valueOf(mAppliesNew.durationMin));
                    noteRequire = mAppliesNew.noteRequire;
                    ccRequire = mAppliesNew.ccRequire;
                    if (mAppliesNew.isclockin()) {
                        findViewById(R.id.btn_location_select).setVisibility(View.VISIBLE);
                    }
                    if (mAppliesNew.picRequireNum == 0 && mAppliesNew.picRequire) {
                        mPicRequire = true;
                    }
                    //设置是否可编辑
                    if (!mAppliesNew.isDurationModify) {
                        durationSumView.setEnabled(false);
                        durationSumView.setFocusable(false);
                        durationSumView.requestFocus();
                    }

                    if (!mAppliesNew.picShow) {
                        picContainer.setVisibility(View.GONE);
                    } else {
                        picContainer.setVisibility(View.VISIBLE);
                    }
                    remarkView.setText(Html.fromHtml(mAppliesNew.leave_remark));
                    if (staffInfos.size() > 0) {
                        if (staffInfos.size() == 1) {
                            staffInfo = staffInfos.get(0);
                            selectApprovalerView.setVisibility(View.VISIBLE);
                            SimpleDraweeView approverImg = (SimpleDraweeView) findViewById(R.id.approvaler_img);
                            ImageCacheManager.getImage(this, approverImg, staffInfo.getStaff_no());
                            approvalerNameView.setText(staffInfo.staff_name);
                            selectApprovalerImgView.setVisibility(View.GONE);
                            selectApprovalerClickView.setClickable(false);

                        } else {
                            selectApprovalerClickView.setClickable(true);
                            selectApprovalerImgView.setVisibility(View.VISIBLE);
                        }
                    }
                    appButton.setVisibility(View.VISIBLE);
                    if (TextUtils.isEmpty(mAppliesNew.applyType)) {
                        mRlTimeType.setVisibility(View.VISIBLE);
                    } else if (mAppliesNew.applyType.equals("D")) {
                        mRlTimeType.setVisibility(View.GONE);
                        allDaySwitch.setChecked(true);
                    } else if (mAppliesNew.applyType.equals("H")) {
                        mRlTimeType.setVisibility(View.GONE);
                        allDaySwitch.setChecked(false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_GET_DURATION:
                try {
                    String duration = rootData.getJson().getString("duration");
                    String final_duration = "0";
                    try {
                        float dur = Float.parseFloat(duration);
                        if (mAppliesNew.durationUnit > 0) {
                            int duration_unit_count = (int) (dur / mAppliesNew.durationUnit);
                            Log.e("TAG_大于零","duration_unit_count="+duration_unit_count);
                            Log.e("TAG_大于零","durationUnit="+mAppliesNew.durationUnit);
                            BigDecimal bigDecimal1 = new BigDecimal(duration_unit_count);
                            BigDecimal bigDecimal2 = new BigDecimal(mAppliesNew.durationUnit);
                            Log.e("TAG_大于零1","bigDecimal1="+bigDecimal1);
                            Log.e("TAG_大于零2","bigDecimal2="+bigDecimal2);
                            double multiply = bigDecimal1.multiply(bigDecimal2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            final_duration = String.valueOf(multiply);
//                            final_duration = duration_unit_count * mAppliesNew.durationUnit;
                            Log.e("TAG_大于零","final_duration="+final_duration);
                        } else {
                            final_duration = String.valueOf(dur);
                            Log.e("TAG_小于零","final_duration="+final_duration);
                        }
                        if (Double.valueOf(final_duration) < durationMin) {
                            durationSumView.setText(final_duration);
                            unitView.setText(vacations.unit);
                            unitView.setVisibility(View.VISIBLE);
                            showToast(getString(R.string.duration_min_tip) + durationMin);
                            return;
                        }
                        if (mAppliesNew.picRequireNum == 0 && mAppliesNew.picRequire || mAppliesNew.picRequireNum > 0 && mAppliesNew.picRequire && dur >= mAppliesNew.picRequireNum) {//luck 说的2017/3/23
                            mPicRequire = true;
                        } else {//peter 让这么改的 2017年4月16日
                            mPicRequire = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    durationSumView.setText(final_duration + "");
                    unitView.setText(vacations.unit);
                    unitView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case CALLBACK_APPLIES:
                Toast.makeText(ApplyVacationActivity.this, getString(R.string.vantop_submit_success), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                Emiter.getInstance().emit("VanTopApprovalListFragment", Constants.REFRESH);
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

    private static final int REQUEST_CODE_MAP_POI = 5004;

    @Override
    public void onClick(final View v) {

        if (v.getId() == R.id.approval_button) {
            //TODO 申请
            final String startDate = getDate(selectStartTimeTextView, allDaySwitch.isChecked());
            final String startTime = getTime(selectStartTimeTextView, allDaySwitch.isChecked());
            final String endDate = getDate(selectEndTimeTextView, allDaySwitch.isChecked());
            final String endTime = getTime(selectEndTimeTextView, allDaySwitch.isChecked());
            final String duration = durationSumView.getText().toString();
            final String note = notesView.getText().toString();
            final String cc = getCcEmail(ccUserGridAdapter.getLists());
            final String unitCode = allDaySwitch.isChecked() ? "D" : "T";

            if (TextUtils.isEmpty(startDate)) {
                showToast(getString(R.string.please_select) + getString(R.string.vantop_startTime));
                return;
            }
            if (TextUtils.isEmpty(endDate)) {
                showToast(getString(R.string.please_select) + getString(R.string.vantop_endTime));
                return;
            }
            if (TextUtils.isEmpty(duration)) {
                showToast(getString(R.string.vantop_please_input) + getString(R.string.vantop_duration));
                return;
            }
            Log.e("TAG_申请休假","isDurationModify="+isDurationModify+";duration="+duration+";durationMin="+durationMin);
            if ( Float.valueOf(duration) < durationMin) {
                showToast(getString(R.string.duration_min_tip) + durationMin);
                return;
            }
            if (noteRequire && TextUtils.isEmpty(note)) {
                showToast(getString(R.string.vantop_please_input) + getString(R.string.vantop_remark));
                return;
            }
            if (ccRequire && TextUtils.isEmpty(cc)) {
                showToast(getString(R.string.vantop_please_input) + getString(R.string.vantop_cc));
                return;
            }
            if (staffInfo == null) {
                showToast(getString(R.string.vantop_please_select_approver));
                return;
            }
            if (mPicRequire && picsView.getChildCount() == 0) {
                showToast(getString(R.string.please_select) + getString(R.string.vantop_picture));
                return;
            }
            if (mAppliesNew.isclockin() && TextUtils.isEmpty(mLatlng)) {
                showToast(getString(R.string.toast_clock_in_location));
                return;
            }
//            String taskId, String code, String unit, String startDate, String startTime,
//                    String endDate, String endTime, String duration, String supervisor,
//                    String remark, String cc
            applies(taskId, vacations.code, unitCode, startDate, startTime, endDate, endTime, duration, staffInfo.staff_no, note, cc);

        } else if (v.getId() == R.id.selectTypeView) {
            if (vacationCodes != null && vacationCodes.size() > 0) {
                showCodes();
            } else {
                loadCodesData();
            }
        } else if (v.getId() == R.id.select_approvaler_click) {
            Intent intent = new Intent(this, ApprovalStaffsActivity.class);
            intent.putExtra("json", staffInfoListJson);
            startActivityForResult(intent, 301);
        } else if (v.getId() == R.id.select_cc_click) {//抄送人
            Intent intent = new Intent("com.vgtech.vancloud.intent.action.GroupUserSelectActivity");
            if (ccUserGridAdapter != null)
                intent.putParcelableArrayListExtra("select", ccUserGridAdapter.getLists());
            intent.putExtra("SELECT_MODE", 2);
            startActivityForResult(intent, 302);
        } else if (v.getId() == R.id.add_pic_img) {

            new ActionSheetDialog(this)
                    .builder()
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true)
                    .addSheetItem(getString(R.string.vantop_take), ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
//                                    Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                    File file = new File(FileCacheUtils.getImageDir(getApplicationContext()), String.valueOf(System.currentTimeMillis())
//                                            + ".jpg");
//                                    path = file.getPath();
//                                    Uri imageUri = Uri.fromFile(file);
//                                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                                    startActivityForResult(openCameraIntent, TAKE_PICTURE);
                                    PermissionsChecker mChecker = new PermissionsChecker(ApplyVacationActivity.this);
                                    if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                                        // 请求权限
                                        PermissionsActivity.startActivityForResult(ApplyVacationActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
                                    } else {
                                        // 全部权限都已获取
                                        EasyPhotos.createCamera(ApplyVacationActivity.this)
                                                .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                                .start(TAKE_PICTURE);
                                    }
                                }
                            })

                    .addSheetItem(getString(R.string.vantop_select_photo), ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    Intent intent = new Intent(getApplicationContext(),
                                            PicSelectActivity.class);
                                    intent.putExtra("single", true);
                                    startActivityForResult(intent, FROM_PHOTO);
                                }
                            }).show();

        } else if (v.getId() == R.id.select_start_time_click) {
            hideKeyboard();
            DateFullDialogView dateDialogview = createDateSelect(selectStartTimeTextView, allDaySwitch.isChecked());
            dateDialogview.show(selectStartTimeTextView);
        } else if (v.getId() == R.id.select_end_time_click) {
            hideKeyboard();
            DateFullDialogView dateDialogview = createDateSelect(selectEndTimeTextView, allDaySwitch.isChecked());
            dateDialogview.show(selectEndTimeTextView);
        } else if (v.getId() == R.id.btn_location_select) {
            //MapLocationActivity
            Intent intent = new Intent("com.vgtech.vancloud.intent.action.MapLocation");
            intent.putExtra("latlng", mLatlng);
            intent.putExtra("address", mAddress);
            intent.putExtra("name", mAddName);
            intent.putExtra("edit", true);
            startActivityForResult(intent, REQUEST_CODE_MAP_POI);
        } else {
            super.onClick(v);
        }
    }

    private void showCodes() {
        String[] strCodes = new String[vacationCodes.size()];
        for (int i = 0; i < strCodes.length; i++) {
            VacationCode vacationCode = vacationCodes.get(i);
            strCodes[i] = vacationCode.desc;
        }
        new AlertDialog.Builder(this, R.style.PickerDialog)
                .setTitle(getString(R.string.vantop_select))
                .setItems(strCodes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        appButton.setVisibility(View.INVISIBLE);
                        VacationCode vacationCode = vacationCodes.get(which);
                        vacations.code = vacationCode.code;
                        vacations.desc = vacationCode.desc;
                        vacations.unit = vacationCode.unit;
                        unitView.setText(vacationCode.unit);
                        selectTypeView.setText(getString(R.string.vantop_vacation_type, vacationCode.desc));
                        getNews(vacationCode.code);
                        requestDuration();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    private void requestDuration() {

        String startDate = selectStartTimeTextView.getText().toString();
        String endDate = selectEndTimeTextView.getText().toString();
        String startTime = "";
        String endTime = "";
        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate) || getString(R.string.vantop_please_select).equals(startDate) || getString(R.string.vantop_please_select).equals(endDate))
            return;

        if (allDaySwitch.isChecked()) {
            startTime = "";
            endTime = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                long s = dateFormat.parse(startDate).getTime();
                long e = dateFormat.parse(endDate).getTime();
                if (e < s) {
                    Toast.makeText(this, R.string.datefulldialog_info_endtime, Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date sdate = format.parse(startDate);
                Date edate = format.parse(endDate);
                startTime = timeFormat.format(sdate);
                endTime = timeFormat.format(edate);
                try {
                    long s = format.parse(startDate).getTime();
                    long e = format.parse(endDate).getTime();
                    if (e < s) {
                        Toast.makeText(this, R.string. datefulldialog_info_endtime, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        getDuration(vacations.code, startDate, startTime, endDate, endTime);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_CODE_MAP_POI:
                mLatlng = data.getStringExtra("latlng");
                mAddress = data.getStringExtra("address");
                mAddName = data.getStringExtra("name");
                if (TextUtils.isEmpty(mAddName)) {
                    mAddressTv.setText(mAddress);
                } else {
                    mAddressTv.setText(mAddName);
                }

                break;
            case 301: {
                String jsonTxt = data.getStringExtra("json");
                if (!TextUtils.isEmpty(jsonTxt)) {
                    try {
                        staffInfo = JsonDataFactory.getData(StaffInfo.class, new JSONObject(jsonTxt));
                        SimpleDraweeView approverImg = (SimpleDraweeView) findViewById(R.id.approvaler_img);
                        ImageCacheManager.getImage(this, approverImg, staffInfo.getStaff_no());
                        selectApprovalerView.setVisibility(View.VISIBLE);
                        approvalerNameView.setText(staffInfo.staff_name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            case 302:

                userSelectList = data.getParcelableArrayListExtra("select");
                if (Constants.DEBUG) {
                    Log.e("TAG_选择人员4", "list=" + userSelectList.size());
                    Log.e("TAG_选择人员4", "list=" + userSelectList.toString());
                }

                for (int i = userSelectList.size() - 1; i >= 0; i--) {
                    Node node = userSelectList.get(i);
                    if (TextUtils.isEmpty(node.email()))
                        userSelectList.remove(node);
                }
                if (Constants.DEBUG) {
                    Log.e("TAG_选择人员6", "userSelectList=" + userSelectList.size());
                    Log.e("TAG_选择人员6", "userSelectList=" + userSelectList.toString());
                }
                ccUserGridAdapter.myNotifyDataSetChanged(userSelectList);
                rcAdapter.setData(userSelectList);

                ArrayList<Node> ccUserGridAdapterLists = ccUserGridAdapter.getLists();
                if (Constants.DEBUG) {
                    Log.e("TAG_选择人员5", "list=" + ccUserGridAdapterLists.size());
                    Log.e("TAG_选择人员5", "list=" + ccUserGridAdapterLists.toString());
                }
                if (ccUserGridAdapterLists.size() > 0 && ccUserGridAdapterLists.size() <= 10) {
                    ccGridView.setVisibility(View.VISIBLE);
                    rc.setVisibility(View.GONE);
                    ccUserCountView.setText(getString(R.string.cc_count, ccUserGridAdapterLists.size() + ""));
                }else if ( ccUserGridAdapterLists.size() > 10) {
                    ccGridView.setVisibility(View.GONE);
                    rc.setVisibility(View.VISIBLE);
                    ccUserCountView.setText(getString(R.string.cc_count, rcAdapter.getData().size() + ""));
                } else {
                    ccGridView.setVisibility(View.GONE);
                    rc.setVisibility(View.VISIBLE);
                    ccUserCountView.setText("");
                }
                break;

//            case PHOTO_CLIP: {
//                String path = data.getStringExtra("path");
//                File file = new File(path);
//                Uri.fromFile(file);
////                new ImageInfo(Uri.fromFile(file).toString(), Uri.fromFile(file).toString());
//                if (file.exists()) {
//                    Bitmap bm = BitmapFactory.decodeFile(path);
////                    addPicImg.setImageBitmap(bm);
//                    addImageView(bm, "");
//                }
//                FilePair filePair = new FilePair("pic", new File(path));
//            }
//            break;
            case TAKE_PICTURE: {
                //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                if (resultPaths != null && resultPaths.size() > 0) {
                    String path = resultPaths.get(0);
                    callBackImage(path);

//                Intent intent = new Intent("com.vgtech.vancloud.clipimage");
//                intent.putExtra("path", path);
//                startActivityForResult(intent, PHOTO_CLIP);
                }
            }
            break;
            case FROM_PHOTO: {
                String path = data.getStringExtra("path");
                //截取图片
//                Intent intent = new Intent("com.vgtech.vancloud.clipimage");
//                intent.putExtra("path", path);
//                startActivityForResult(intent, PHOTO_CLIP);
                callBackImage(path);
            }
            break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void callBackImage(String path) {
        imagePath = path;
        addImageView(imagePath, "");
    }

    private void addImageView(String path, final String url) {

        if (!TextUtils.isEmpty(path) || !TextUtils.isEmpty(url)) {
            picsView.removeAllViews();
            View view = getLayoutInflater().inflate(R.layout.item_published_grida, null);
            SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.item_grida_image);
            ImageView deleteView = (ImageView) view.findViewById(R.id.btn_delete);
            picsView.addView(view);
            if (!TextUtils.isEmpty(path)) {
                String uri = Uri.decode(Uri.fromFile(new File(path)).toString());
                imageView.setTag(uri);
                int width = 100, height = 100;
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                        .setResizeOptions(new ResizeOptions(width, height))
                        .setAutoRotateEnabled(true)
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(imageView.getController())
                        .setImageRequest(request)
                        .build();
                imageView.setController(controller);
            } else {
                imageView.setTag(url);
                ImageOptions.setImage(imageView, url);
            }
            imageView.setOnClickListener(photoListener);
            deleteView.setOnClickListener(deletImageListener);
            picScrollview.setVisibility(View.VISIBLE);
            addPicImg.setVisibility(View.GONE);
        }

    }


    View.OnClickListener photoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = (String) v.getTag();
            List<ImageInfo> imgInfo = new ArrayList<>();
            imgInfo.add(new ImageInfo(url, url));
            String json = new Gson().toJson(imgInfo);
            Intent intent = new Intent("com.vgtech.imagecheck");
            intent.putExtra("listjson", json);
            intent.putExtra("numVisible", false);
            startActivity(intent);
        }
    };

    View.OnClickListener deletImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            picsView.removeAllViews();
            imagePath = "";
            addPicImg.setVisibility(View.VISIBLE);
            picScrollview.setVisibility(View.GONE);
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        changeTime(selectStartTimeTextView, isChecked);
        changeTime(selectEndTimeTextView, isChecked);
        requestDuration();
    }

    private String encode(final String pass) {
        try {
            return URLEncoder.encode(pass, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return pass;
    }


    private DateFullDialogView createDateSelect(final TextView dateTv, boolean ifAlldate) {
        String dateS = dateTv.getText().toString();
        Calendar calendar = null;
        String format;
        String type;
        String stytleType;
        if (ifAlldate) {
            format = "yyyy-MM-dd";
            type = "YMD";
            stytleType = "date";
        } else {
            format = "yyyy-MM-dd HH:mm";
            type = DateFullDialogView.DATE_TYPE_MINUTE_SPIT_FIVE;
            stytleType = DateFullDialogView.DATE_TYPE_MINUTE_SPIT_FIVE;
        }

        if (!TextUtils.isEmpty(dateS)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            try {
                Date parse = dateFormat.parse(dateS);
                calendar = Calendar.getInstance();
                calendar.setTime(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        final DateFullDialogView dateDialogview = new DateFullDialogView(this,
                dateTv, type, stytleType, calendar);//年月日时分秒 当前日期之后选择

        dateDialogview.setButtonClickListener(new DateFullDialogView.ButtonClickListener() {
            @Override
            public void sureButtonOnClickListener(String time) {

                Class cls = DateFullDialogView.class;
                try {
                    //通过WheelUtil的方法getDateTime获取当前选中的日期
                    Field mWheel = cls.getDeclaredField("mWheel");
                    mWheel.setAccessible(true);
                    WheelUtil util = (WheelUtil) mWheel.get(dateDialogview);
                    //获取WheelUtil对象
                    cls = WheelUtil.class;
                    //执行getDateTime方法
                    if (allDaySwitch.isChecked()) {
                        Method m = cls.getDeclaredMethod("getDateTime");
                        m.setAccessible(true);
                        String date = (String) m.invoke(util);
                        dateTv.setText(date);
                    } else {
                        Method m1 = cls.getDeclaredMethod("getTime");
                        m1.setAccessible(true);
                        String date = (String) m1.invoke(util);
                        dateTv.setText(date);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                requestDuration();
            }

            @Override
            public void cancelButtonOnClickListener() {

            }
        });
        return dateDialogview;
    }


    public void changeTime(TextView textView, boolean ifAllDate) {

        String time = textView.getText().toString();
        String newTime;
        if (!TextUtils.isEmpty(time) && !getString(R.string.vantop_please_select).equals(time)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (ifAllDate) {
                    Date parse = dateFormat.parse(time);
                    newTime = dateFormat1.format(parse);
                } else {
                    Date parse = dateFormat1.parse(time);
                    newTime = dateFormat.format(parse);
                }
                textView.setText(newTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getTime(TextView textView, boolean ifAllDate) {
        String time = "";
        String text = textView.getText().toString();
        if (TextUtils.isEmpty(text) || getString(R.string.vantop_please_select).equals(text))
            return time;
        if (ifAllDate)
            return time;
        else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            try {
                Date date = format.parse(text);
                time = timeFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return time;
        }
    }

    public String getDate(TextView textView, boolean ifAllDate) {

        String time = "";
        String text = textView.getText().toString();
        if (TextUtils.isEmpty(text) || getString(R.string.vantop_please_select).equals(text))
            return time;
        if (ifAllDate)
            return text;
        else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = format.parse(text);
                time = timeFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return time;
        }
    }

    public String getCcEmail(List<Node> nodes) {
        String email = "";
        for (Node s : nodes) {
            if (!TextUtils.isEmpty(s.email())) {
                email += s.email();
                email += ";";
            }
        }
        return email;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkManager != null)
            networkManager.cancle(this);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(notesView.getWindowToken(), 0);
        }
    }

    @Override
    public void OnDeleteClick(View view, int position) {
        userSelectList.remove(position);
        //处理rc数据
        rcAdapter.setDetele(userSelectList);
        //处理Grid数据
       ccUserGridAdapter.myNotifyDataSetChanged(userSelectList);
    }
}
