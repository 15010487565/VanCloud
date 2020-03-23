package com.vgtech.vantop.ui.overtime;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.EventBusMsg;
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
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.image.ImageUtility;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.utils.HttpUtils;
import com.vgtech.common.utils.HttpView;
import com.vgtech.common.utils.ToastUtil;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.Approval;
import com.vgtech.vantop.moudle.OvertimeApplyDetail;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.GsonUtils;
import com.vgtech.vantop.utils.MapUtils;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
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

/**
 * Created by Duke on 2016/7/20.
 * 新建加班申请
 */
public class CreatedOverTimeActivity extends BaseActivity implements View.OnClickListener,
//        HttpListener<String>,
        HttpView {

    private static final int CODE_TIME_SLICE = 3;
    private static final int OVERTIMEAPPLY_NEW = 1;
    private TextView overtimeTypeTxt;
    private TextView shiftTxt;
    private TextView startTimeTxt;
    private TextView endTimeTxt;
    private TextView overtimeNumTxt;
    private TextView deducEatNumTxt;
    private TextView approvalSupervisorTxt;
    private TextView convertLeavecodeTxt;
    private EditText overtimeReasonEdt;
    private EditText convertHoursTxt;
    private EditText convertReasonEdt;
    private ImageView addPicImg;
    private ToggleButton isOutOtTgbtn;
    private RelativeLayout overtimeTypeRl;
    private RelativeLayout shiftRl;
    private LinearLayout convertLl;
    private LinearLayout attachFileLl;
    private ImageView overtime_line;
    private ImageView shift_line;
    private ImageView convert_line;
    private ImageView explain_line;

    private ArrayList<Map<String, String>> shiftValues;
    private Map<String, String> typeValues;
    private List<Map<String, String>> approverValues, conventLvValues;
    private String deductHour = "0.0";
    private String approvalSupervisor = "";
    private String typeKey = "";
    private String shiftKey = "";
    private String startTime = "", startDate = "", endTime = "", endDate = "";
    private String convertLeavecode = "", convertReason = "";
    private String overtime_num = "0.0", overtimeReason = "";
    private String taskId;
    private static final int REQUESTCODE = 9;
    private int selectType = -1;
    boolean isOutOt = false;
    private int deduceatDefault = -1;
    private boolean deductHourStatus = false;
    private float convertResult;

    private static final int TAKE_PICTURE = 10;
    private static final int FROM_PHOTO = 11;
    private static final int PHOTO_CLIP = 12;
    private String path = "";

    private LinearLayout picsView;
    private String shiftJsonText;
    private RelativeLayout startTimeClickView;
    private RelativeLayout endTimeClickView;
    private RelativeLayout deduceatNumClickView;
    private RelativeLayout approvalSupervisorClickView;
    private RelativeLayout convertLeavecodeClickView;

    private String imagePath = "";
    private NetworkManager networkManager;
    private boolean mHasConvertJZ = true;
    private View deduceatNumClickDivider;
    //休息区间集合
    private List<String> mSliceStartTimeList;
    private List<String> mSliceEndTimeList ;
    private boolean mTimeSliceStatus;
    private double mOvertimeMinUnit;
    private double mOvertimeMinShichang;
    private boolean convertnum_onlyread;
    private boolean mIsNeedRestTime;
    private String defaultValue;

    private List<String> mRestStartTimeList = new ArrayList<String>();
    private List<String> mRestEndTimeList = new ArrayList<String>();
    private Button buSubmit;
    private boolean isOvertimeRemark = true;//加班原因是否必填

    @Override
    protected int getContentView() {
        return R.layout.activity_created_overtime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.ot_appcation));
//        initRightTv(getString(R.string.vantop_sumbit));
        initView();
        initEvent();
        initData(false);
    }

    private void initView() {
        overtimeTypeTxt = (TextView) findViewById(R.id.overtime_type_txt);
        shiftTxt = (TextView) findViewById(R.id.shift_txt);
        startTimeTxt = (TextView) findViewById(R.id.start_time_txt);
        startTimeClickView = (RelativeLayout) findViewById(R.id.start_time_click);
        endTimeTxt = (TextView) findViewById(R.id.end_time_txt);
        endTimeClickView = (RelativeLayout) findViewById(R.id.end_time_click);
        overtimeNumTxt = (TextView) findViewById(R.id.overtime_num_txt);
        deducEatNumTxt = (TextView) findViewById(R.id.deduceat_num_txt);
        deduceatNumClickView = (RelativeLayout) findViewById(R.id.deduceat_num_click);
        deduceatNumClickDivider = findViewById(R.id.deduceat_num_click_divider);
        approvalSupervisorTxt = (TextView) findViewById(R.id.approval_supervisor_txt);
        approvalSupervisorClickView = (RelativeLayout) findViewById(R.id.approval_supervisor_click);
        convertLeavecodeTxt = (TextView) findViewById(R.id.convert_leavecode_txt);
        convertLeavecodeClickView = (RelativeLayout) findViewById(R.id.convert_leavecode_click);
        overtimeReasonEdt = (EditText) findViewById(R.id.overtime_reason_edt);
        convertHoursTxt = (EditText) findViewById(R.id.convert_hours_txt);
        convertReasonEdt = (EditText) findViewById(R.id.convert_reason_edt);
        addPicImg = (ImageView) findViewById(R.id.add_pic_img);
        isOutOtTgbtn = (ToggleButton) findViewById(R.id.isOutOt_tgbtn);
        isOutOtTgbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buSubmit.setEnabled(false);
                Log.e("TAG_外出加班","isChecked="+isChecked);
                initData(isChecked);
            }
        });

        overtimeTypeRl = (RelativeLayout) findViewById(R.id.overtime_type_rl);
        shiftRl = (RelativeLayout) findViewById(R.id.shift_rl);
        convertLl = (LinearLayout) findViewById(R.id.convert_ll);
        attachFileLl = (LinearLayout) findViewById(R.id.attach_file_ll);

        overtime_line = (ImageView) findViewById(R.id.overtime_line);
        shift_line = (ImageView) findViewById(R.id.shift_line);
        convert_line = (ImageView) findViewById(R.id.convert_line);
        explain_line = (ImageView) findViewById(R.id.explain_line);
        picsView = (LinearLayout) findViewById(R.id.picsView);

        buSubmit = (Button) findViewById(R.id.bu_submit);
        buSubmit.setOnClickListener(this);

        overtimeNumTxt.setText("0.0" + " " + getString(R.string.vantop_hour));

    }

    private void getRestTime(String startTime) {
        showLoadingDialog(this, getString(R.string.dataloading));
        Map<String, String> params = new HashMap<>();
        params.put("datestr", startTime);
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(CreatedOverTimeActivity.this, UrlAddr.TIME_SLICE), params, this, true);
//        networkManager.load(CODE_TIME_SLICE, path, CreatedOverTimeActivity.this);
        HttpUtils.load(this,CODE_TIME_SLICE,path,this);

    }

    public void initData(boolean isoutot) {
        showLoadingDialog(this, getString(R.string.dataloading));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("isoutot",String.valueOf(isoutot));
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(CreatedOverTimeActivity.this, UrlAddr.URL_OVERTIMEAPPLY_NEW), params, this, true);
//        networkManager.load(OVERTIMEAPPLY_NEW, path, CreatedOverTimeActivity.this);
        HttpUtils.load(this,OVERTIMEAPPLY_NEW,path,this);

    }

    private void initEvent() {
        overtimeTypeRl.setOnClickListener(this);
        shiftRl.setOnClickListener(this);
        startTimeClickView.setOnClickListener(this);
        endTimeClickView.setOnClickListener(this);
        deduceatNumClickView.setOnClickListener(this);
        convertLeavecodeClickView.setOnClickListener(this);
        addPicImg.setOnClickListener(this);
        approvalSupervisorClickView.setOnClickListener(this);
        startTimeTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String startTime = startTimeTxt.getText().toString();
                String endTime = endTimeTxt.getText().toString();
                if (!getString(R.string.vantop_please_select).equals(startTime) && !getString(R.string.vantop_please_select).equals(endTime)) {
                    if (isFirstThenSecond(endTime, startTime)) {
                        calculVertimeNum();
                    } else {
                        overtimeNumTxt.setText("0.0" + "  " + getString(R.string.vantop_hour));
                        overtime_num = "0.0";
                    }
                }
            }
        });
        endTimeTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String startTime = startTimeTxt.getText().toString();
                String endTime = endTimeTxt.getText().toString();
                if (!getString(R.string.vantop_please_select).equals(startTime) && !getString(R.string.vantop_please_select).equals(endTime)) {
                    if (isFirstThenSecond(endTime, startTime)) {
                        calculVertimeNum();
                    } else {
                        overtimeNumTxt.setText("0.0" + "  " + getString(R.string.vantop_hour));
                        overtime_num = "0.0";
                    }
                }
            }
        });

        overtimeNumTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (deductHourStatus&&!TextUtils.isEmpty(defaultValue) && deducEatNumTxt.getTag() == null) {
                        deductHour = MapUtils.getdeductHourKeys(CreatedOverTimeActivity.this).get(deduceatDefault);
                    } else {
                        if (deducEatNumTxt.getTag() != null) {
                            deductHour = MapUtils.getdeductHourKeys(CreatedOverTimeActivity.this).get((int) deducEatNumTxt.getTag());
                        }
                    }
                    if (!("0.0" + "  " + getString(R.string.vantop_hour)).equals(s) && !"0.0".equals(deductHour) && !getString(R.string.vantop_please_select).equals(convertLeavecodeTxt.getText().toString()))
                        calculConvertNum("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        deducEatNumTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (deductHourStatus &&!TextUtils.isEmpty(defaultValue)&& deducEatNumTxt.getTag() == null) {
                    deductHour = MapUtils.getdeductHourKeys(CreatedOverTimeActivity.this).get(deduceatDefault);
                } else {
                    if (deducEatNumTxt.getTag() != null) {
                        deductHour = MapUtils.getdeductHourKeys(CreatedOverTimeActivity.this).get((int) deducEatNumTxt.getTag());
                    }
                }
                if (!"".equals(overtime_num) && !getString(R.string.vantop_please_select).equals(s) && !getString(R.string.vantop_please_select).equals(convertLeavecodeTxt.getText().toString()))
                    calculConvertNum("");
            }
        });

        convertLeavecodeTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (!"".equals(overtime_num) && !"0.0".equals(deductHour) && !getString(R.string.vantop_please_select).equals(s.toString())) {
                if (!"".equals(overtime_num) && !getString(R.string.vantop_please_select).equals(s.toString())) {
                    if ("···".equals(s.toString())) {
                        convertResult = 0.0f;
                        convertHoursTxt.setText(convertResult + "");
                    } else {
                        calculConvertNum("");
                    }
                }
            }
        });

    }


    @Override
    public void onClick(View view) {
        if (view == overtimeTypeRl) {
//            showItemPicker(overtimeTypeTxt, MapUtils.getMapValues(typeValues));
            showPositionSelected(overtimeTypeTxt, MapUtils.getMapValues(typeValues));
        } else if (view == shiftRl) {
            Intent intent = new Intent(this, ShiftSelectActivity.class);
            intent.putExtra("json", shiftJsonText);
            intent.putExtra("shiftKey", shiftKey);
            startActivityForResult(intent, REQUESTCODE);
        } else if (view == startTimeClickView) {
            hideKeyboard();
            showDateDialogview(startTimeTxt, false, endTimeTxt);
        } else if (view == endTimeClickView) {
            hideKeyboard();
            showDateDialogview(endTimeTxt, true, startTimeTxt);
        } else if (view == deduceatNumClickView) {
            hideKeyboard();
            showPositionSelected(deducEatNumTxt, MapUtils.getdeductHourValues(this));
        } else if (view == approvalSupervisorClickView) {
            hideKeyboard();
            showPositionSelected(approvalSupervisorTxt, getListForMap(approverValues, "staffName"));
        } else if (view == convertLeavecodeClickView) {
            hideKeyboard();
            showPositionSelected(convertLeavecodeTxt, getListForMap(conventLvValues, "conventLvsValue"));
        } else if (view == addPicImg) {
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
                                    PermissionsChecker mChecker = new PermissionsChecker(CreatedOverTimeActivity.this);
                                    if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                                        // 请求权限
                                        PermissionsActivity.startActivityForResult(CreatedOverTimeActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
                                    } else {
                                        // 全部权限都已获取
                                        EasyPhotos.createCamera(CreatedOverTimeActivity.this)
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
        } else if (view.getId() == R.id.bu_submit) {

            submitovertimeData();
        } else {
            super.onClick(view);
        }
    }

    /**
     * 变更日期
     *
     * @param startTime
     * @param endTime
     */
    private void changeDateInflate(String startTime, String endTime) {
        if (isFirstThenSecond(startTime, endTime)) {
            Long aLong = stringToLong_YMdhm(endTime);
            Long endLong = aLong + 86400000;
            Date date = new Date(endLong);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time = sdf.format(date);
            endTimeTxt.setText(time);
        } else {
            endTimeTxt.setText(endTime);
        }
        startTimeTxt.setText(startTime);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(overtimeReasonEdt.getWindowToken(), 0);
        }
    }

    private List<String> getListForMap(List<Map<String, String>> maps, String str) {
        List<String> list = new ArrayList<>();
        if (maps != null) {
            for (Map<String, String> map : maps) {
                list.add(map.get(str));
            }
        }
        return list;
    }

    private void calculVertimeNum() {                //计算加班时长
        if (mIsNeedRestTime) {
            String startTime = string2YMD(startTimeTxt.getText().toString());
            getRestTime(startTime);
        } else {
            calculVertimeNumFinal();
        }
    }

    private void calculVertimeNumFinal() {
        String startTime = startTimeTxt.getText().toString();
        String endTime = endTimeTxt.getText().toString();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        //请假总天数
        Long daySub = getDaySub(startTime, endTime);

        long startLong = stringToLong_YMdhm(startTime);
        long endLong = stringToLong_YMdhm(endTime);
        Log.e("TAG_加班2输入", "startLong=" + startLong);
        Log.e("TAG_加班2输入", "endLong=" + endLong);

        long different = endLong - startLong;


        if (mTimeSliceStatus) {
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < mSliceStartTimeList.size(); i++) {
                    //休息时间区间
                    Long start_time = DateTimeUtil.stringToLong_YMdhm(DateTimeUtil.longToString_YMd(startLong + j * DateTimeUtil.DAY) + " " + mSliceStartTimeList.get(i));
                    Long end_time = DateTimeUtil.stringToLong_YMdhm(DateTimeUtil.longToString_YMd(startLong + j * DateTimeUtil.DAY) + " " + mSliceEndTimeList.get(i));

                    if (start_time - startLong < 0 && endLong - end_time < 0) {
                        continue;//扣餐不在区间
                    }
                    Log.e("TAG_加班区间休息2===" + i, "start_time=" + DateTimeUtil.formatToMdhm(start_time)+";end_time=" + DateTimeUtil.formatToMdhm(end_time));
                    Log.e("TAG_加班提交2===" + i, "startLong=" + startTime+";endLong=" + endTime);
                    //在区间
                    if (start_time - startLong < 0 && startLong - end_time < 0) {
                        different -= (end_time - startLong);
                    }
                    if (endLong - end_time < 0 && endLong - start_time > 0) {
                        different -= (endLong - start_time);
                    }
                    if (start_time - startLong >= 0 && endLong - end_time >= 0) {
                        different -= (end_time - start_time);
                    }

                }
            }
        }

        if (mIsNeedRestTime) {
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < mRestStartTimeList.size(); i++) {
                    Long start_time = DateTimeUtil.stringToLong_YMdhm(DateTimeUtil.longToString_YMd(startLong + j * DateTimeUtil.DAY) + " " + mRestStartTimeList.get(i));
                    Long end_time = DateTimeUtil.stringToLong_YMdhm(DateTimeUtil.longToString_YMd(startLong + j * DateTimeUtil.DAY) + " " + mRestEndTimeList.get(i));
                    if (Constants.DEBUG) {
                        Log.e("TAG_加班1", "start_time=" + start_time);
                        Log.e("TAG_加班1", "end_time=" + end_time);
                    }

                    if (start_time - startLong < 0 && endLong - end_time < 0) {
                        continue;//扣餐不在区间
                    }
                    //在区间
                    if (start_time - startLong < 0 && startLong - end_time < 0) {
                        different -= (end_time - startLong);
                    }
                    if (endLong - end_time < 0 && endLong - start_time > 0) {
                        different -= (endLong - start_time);
                    }
                    if (start_time - startLong >= 0 && endLong - end_time >= 0) {
                        different -= (end_time - start_time);
                    }
                }
            }
        }
        if (Constants.DEBUG) {
            Log.e("TAG_加班3", "different=" + different);
        }

        if (different <= 0) {
            overtimeNumTxt.setText("0.0 " + getString(R.string.vantop_hour));
            overtime_num = "0.0";
            return;
        }

        float elapsedHours = (different % daysInMilli) / hoursInMilli;

        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;

        float min = (float)(Math.round(elapsedMinutes*100))/60;

        float result = elapsedHours + min / 100;

        //加班计算结果没有超过加班最小时长
        if (Constants.DEBUG)
            Log.e("TAG_加班3", "最小加班时长=" + mOvertimeMinShichang);
        if (mOvertimeMinShichang > 0 && result < mOvertimeMinShichang) {
            result = 0;
        }
        if (Constants.DEBUG)
            Log.e("TAG_加班3", "最小加班单位=" + mOvertimeMinUnit);
        if (mOvertimeMinUnit > 0) {
            int r = (int) (result / mOvertimeMinUnit);
            result = (float) (mOvertimeMinUnit * r);
        }
        if (Constants.DEBUG)
            Log.e("TAG_加班===", "未转化前=" + result);
        BigDecimal bd = new BigDecimal(result);
        result = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        if (Constants.DEBUG) {
            Log.e("TAG_加班", "result=" + result);
        }

        overtimeNumTxt.setText(String.valueOf(result) + " " + getString(R.string.vantop_hour));
        overtime_num = String.valueOf(result);
    }

    private String string2YMD(String time) {
        if ("请选择".equals(time)){
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat dfYMD = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = df.parse(time);
            return dfYMD.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void calculConvertNum(String s) {     //计算转假时长

        float overtimeNum = 0;
        float deducEatNum = 0;
        if (!"".equals(s)) {
            overtimeNum = Float.valueOf(s.split("\\s+")[0]);
        } else {
            overtimeNum = Float.valueOf(overtimeNumTxt.getText().toString().split("\\s+")[0]);
        }
        List<String> list = MapUtils.getdeductHourValues(this);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(deducEatNumTxt.getText().toString())) {
                deducEatNum = Float.valueOf(MapUtils.getdeductHourKeys(this).get(i));
            }
        }
        convertResult = (overtimeNum * 100 - deducEatNum * 100) / 100;
        if (convertResult > 0) {
            convertHoursTxt.setText(convertResult + "");
        } else {
            convertResult = 0;
            convertHoursTxt.setText(convertResult + "");
        }
    }


//    private void showItemPicker(TextView view, List<String> list) {
//        int select_intex = 0;
//        if (view.getTag() != null) select_intex = (int) view.getTag();
//        ItemPicker itemPicker = new ItemPicker(this, view, list, select_intex);
//        itemPicker.showItemPicker();
//    }

    //TODO
    private void showPositionSelected(final TextView view, final List<String> list) {
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true);

        for (String s : list) {
            actionSheetDialog.addSheetItem(s, ActionSheetDialog.SheetItemColor.Blue,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            String title = list.get(which);
                            view.setText(title);
                            view.setTag(which);
                        }
                    });
        }
        actionSheetDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUESTCODE:
                shiftKey = data.getStringExtra("key");
                String value = data.getStringExtra("value");
                if (TextUtils.isEmpty(value))
                    shiftTxt.setText(getString(R.string.vantop_please_select));
                else
                    shiftTxt.setText(value);
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


    public boolean isFirstThenSecond(String FirstTime, String SecondTime) {
        Long l1 = stringToLong_YMdhm(FirstTime);
        Long l2 = stringToLong_YMdhm(SecondTime);
        if (l1 > l2) {
            return true;
        } else {
            return false;
        }
    }

    public Long stringToLong_YMdhm(String curDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long timeLong = 0;
        try {
            timeLong = format.parse(curDate).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeLong;
    }

    //请假总天数
    public Long getDaySub(String startDateStr, String endDateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date beginDate;
        Date endDate;
        long day = 0;
        try {
            beginDate = format.parse(startDateStr);
            endDate = format.parse(endDateStr);
            Log.e("TAG_加班2", "beginDate=" + beginDate);
            Log.e("TAG_加班2", "endDate=" + endDate);
            day = (endDate.getTime() - beginDate.getTime()) / (24 * 60 * 60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;
    }

//    @Override
//    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
//        dismisLoadingDialog();
//
//
//        switch (callbackId) {
//
//        }
//
//    }
//
//    @Override
//    public void onErrorResponse(VolleyError error) {
//        dismisLoadingDialog();
//    }
//
//    @Override
//    public void onResponse(String response) {
//
//    }

    /**
     * 变更数据的填充
     */
    private void changeDataInflate() {

        String oldDataJson = getIntent().getStringExtra("json");

        OvertimeApplyDetail overtimeApplyDetail;
        if (!TextUtils.isEmpty(oldDataJson)) {
            taskId = getIntent().getStringExtra("taskid");
            try {
                overtimeApplyDetail = JsonDataFactory.getData(OvertimeApplyDetail.class, new JSONObject(oldDataJson));
                List<Approval> approvers = JsonDataFactory.getDataArray(Approval.class, overtimeApplyDetail.getJson().getJSONArray("approval"));

                if (overtimeApplyDetail.typeKey != null && typeValues != null) {
                    String typeKey = overtimeApplyDetail.typeKey;
                    String typeValue = typeValues.get(typeKey);
                    if (typeValue != null) {
                        overtimeTypeTxt.setText(typeValue);
                        this.typeKey = typeKey;
                    }
                }
                if (overtimeApplyDetail.shiftKey != null && shiftValues != null) {
                    String shiftKey = overtimeApplyDetail.shiftKey;
                    for (Map<String, String> shiftMap : shiftValues) {
                        if (shiftKey.equals(shiftMap.get("shiftKey"))) {
                            String shiftValue = shiftMap.get("shiftValue");
                            shiftTxt.setText(shiftValue);
                            this.shiftKey = shiftKey;
                            break;
                        }
                    }
                }
                if (overtimeApplyDetail.convertLeaveCodeKey != null && conventLvValues != null) {
                    String convertLeaveCodeKey = overtimeApplyDetail.convertLeaveCodeKey;
                    String conventLvValue = "";
                    if (convertLeaveCodeKey != null) {
                        for (int i = 0; i < conventLvValues.size(); i++) {
                            if (convertLeaveCodeKey.equals(conventLvValues.get(i).get("conventLvsKey")))
                                conventLvValue = conventLvValues.get(i).get("conventLvsValue");
                        }
                    }

                    if (conventLvValue != null && !"".equals(conventLvValue)) {
                        convertLeavecodeTxt.setText(conventLvValue);
                        this.convertLeavecode = convertLeaveCodeKey;
                    }
                }


                if (approvers.size() > 0 && approvers.get(0).staffNo != null && approverValues != null) {
                    String approvalStaffno = approvers.get(0).staffNo;
                    for (Map<String, String> approvalMap : approverValues) {
                        if (approvalStaffno.equals(approvalMap.get("staffNo"))) {
                            String staffName = approvalMap.get("staffName");
                            approvalSupervisorTxt.setText(staffName);
                            this.approvalSupervisor = approvalStaffno;
                            break;
                        }
                    }
                }
                isOutOtTgbtn.setChecked(overtimeApplyDetail.isOutOt);
                deducEatNumTxt.setText(overtimeApplyDetail.deductHours);
                overtimeReasonEdt.setText(overtimeApplyDetail.remark);
                convertHoursTxt.setText(overtimeApplyDetail.convertHours);
                if (convertnum_onlyread) {
                    convertHoursTxt.setFocusable(false);
                    convertHoursTxt.setFocusableInTouchMode(false);
                }
                convertReasonEdt.setText(overtimeApplyDetail.convertRemark);
                addImageView(null, overtimeApplyDetail.pic);
                String date = overtimeApplyDetail.date;
                String startTime = overtimeApplyDetail.startTime;
                String endTime = overtimeApplyDetail.endTime;
                changeDateInflate(date + " " + startTime, date + " " + endTime);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

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
                String imgUrl = "";
                if (url.contains("?loginUserCode="))
                    imgUrl = url;
                else
                    imgUrl = url + "?loginUserCode=" + PrfUtils.getStaff_no(this);
                imgUrl = VanTopUtils.generatorImageUrl(this, imgUrl);
                imageView.setTag(imgUrl);
                ImageOptions.setImage(imageView, imgUrl);
            }
            imageView.setOnClickListener(photoListener);
            deleteView.setOnClickListener(deletImageListener);
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
        }
    };

//    View.OnLongClickListener photoLongListener = new View.OnLongClickListener() {
//        @Override
//        public boolean onLongClick(final View v) {
//            new AlertDialog.Builder(CreatedOverTimeActivity.this).setTitle(getString(R.string.vantop_confirm_delete)).setPositiveButton(getString(R.string.vantop_confirm),
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            picsView.removeView(v);
//                        }
//                    }
//            ).setNegativeButton(getString(R.string.vantop_cancle), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            }).show();
//            return false;
//        }
//    };

    public void showDateDialogview(TextView textView, boolean type, TextView
            startTimeView) {
        String dateS = textView.getText().toString();
        String startTime = startTimeView.getText().toString();
        Calendar calendar = null;
        Calendar otherCalendar = null;

        if (!TextUtils.isEmpty(startTime) && !getString(R.string.vantop_please_select).equals(startTime)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date parse = dateFormat.parse(startTime);
                otherCalendar = Calendar.getInstance();
                otherCalendar.setTime(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(dateS) && !getString(R.string.vantop_please_select).equals(dateS)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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

        String dialogtype = "full";

        if (!TextUtils.isEmpty(startTime) && !getString(R.string.please_select).equals(startTime)) {

            if (type) {
                dialogtype = "EndTime_full";
            } else {
                dialogtype = "StartTime_full";
            }
        }

        DateFullDialogView dateDialogview = new DateFullDialogView(this,
                textView, dialogtype, "ymdhm", calendar, getResources().getColor(R.color.text_black), otherCalendar);//年月日时分秒 当前日期之后选择
        dateDialogview.show(textView);
    }


    //提交申请
    private void submitovertimeData() {
        String pleaseSelect = getString(R.string.please_select);
        List<String> mapKeys = MapUtils.getMapKeys(typeValues);
        if (overtimeTypeTxt.getTag() != null)
            typeKey = mapKeys.get((int) overtimeTypeTxt.getTag());

        startTime = startTimeTxt.getText().toString();
        endTime = endTimeTxt.getText().toString();
        if (!pleaseSelect.equals(startTime)) {
            String[] splitStart = startTime.split("\\s+");
            startDate = splitStart[0];
            startTime = splitStart[1];
        } else {
            ToastUtil.showToast( getString(R.string.vantop_please_start_time));
            return;
        }

        if (!pleaseSelect.equals(endTime)) {
            String[] splitEnd = endTime.split("\\s+");
            endDate = splitEnd[0];
            endTime = splitEnd[1];
        } else {
            ToastUtil.showToast( getString(R.string.vantop_please_end_time));
            return;
        }

        if (deductHourStatus &&!TextUtils.isEmpty(defaultValue)&& deducEatNumTxt.getTag() == null) {
            deductHour = MapUtils.getdeductHourKeys(this).get(deduceatDefault);
        } else {
            if (deducEatNumTxt.getTag() != null) {
                deductHour = MapUtils.getdeductHourKeys(this).get((int) deducEatNumTxt.getTag());
            }
        }

        if ((Float.parseFloat(overtime_num) - Float.parseFloat(deductHour)) <= 0) {
            showToast("加班时长小于或等于扣餐时长，不符合加班规则！");
            return;
        }

        overtimeReason = overtimeReasonEdt.getText().toString().trim();
        if (isOvertimeRemark && TextUtils.isEmpty(overtimeReason)) {
            showToast("加班原因不能为空！");
            return;
        }

        if (approvalSupervisorTxt.getTag() != null) {
            approvalSupervisor = getListForMap(approverValues, "staffNo").get((int) approvalSupervisorTxt.getTag());
        } else {
            if ("".equals(approvalSupervisor)) {
                ToastUtil.showToast( getString(R.string.vantop_overtime_approver_not));
                return;
            }

        }
        float convertHour = 0;
        String str = convertHoursTxt.getText().toString();
        if (!"".equals(str)) {
            convertHour = Float.valueOf(str);
            if (convertHour > convertResult) {
                ToastUtil.showToast(getString(R.string.vantop_convert_hours_error));
                return;
            }
        } else {
            convertHour = 0;
        }

        if (convertLeavecodeTxt.getTag() != null) {
            convertLeavecode = getListForMap(conventLvValues, "conventLvsKey").get((int) convertLeavecodeTxt.getTag());
        }
        isOutOt = isOutOtTgbtn.isChecked();
        convertReason = convertReasonEdt.getText().toString();
        if (!mHasConvertJZ) {
            if (TextUtils.isEmpty(convertLeavecode)) {
                ToastUtil.showToast( getString(R.string.toast_convert_leave));
                return;
            }
            if (convertHour <= 0) {
                ToastUtil.showToast( getString(R.string.toast_convert_leave));
                return;
            }
        }

        showLoadingDialog(this, getString(R.string.vantop_sending));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(taskId)) {
            params.put("fromTaskId", taskId);
        } else {
            params.put("fromTaskId", "0");
        }
        String isoutot = isOutOt ? "1" : "0";
        params.put("type", typeKey);
        params.put("shift", shiftKey);
        params.put("startDate", startDate);
        params.put("startTime", startTime);
        params.put("endDate", endDate);
        params.put("endTime", endTime);
        params.put("hours", overtime_num);
        params.put("deductHours", deductHour);
        params.put("approver", approvalSupervisor);
        params.put("remark", encode(overtimeReason));
        params.put("isOutOt", isoutot);
        params.put("convertLeaveCode", convertLeavecode);
        params.put("convertHours", convertHour + "");
        params.put("convertBemark", encode(convertReason));
        NetworkPath path = null;
        if (TextUtils.isEmpty(imagePath))
            path = new NetworkPath(VanTopUtils.generatorUrl(CreatedOverTimeActivity.this, UrlAddr.URL_OVERTIMEAPPLY_SUBMIT), params, this, true);
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
            File lastFile = new File(imagePath);
            if (lastFile.exists())
                lastFile.delete();
            imagePath = FileUtils.saveBitmap(this, bm, "" + newStr, "jpg");
            FilePair filePair = new FilePair("pic", new File(imagePath));
            path = new NetworkPath(VanTopUtils.generatorUrl(CreatedOverTimeActivity.this, UrlAddr.URL_OVERTIMEAPPLY_SUBMIT), params, filePair, this, true);

        }
        HttpUtils.load(this,2,path,this);

//        networkManager.load(2, path, CreatedOverTimeActivity.this);
    }

    private String encode(final String pass) {
        try {
            return URLEncoder.encode(pass, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return pass;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkManager != null)
            networkManager.cancle(this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, null, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {

            case 2:
                ToastUtil.showToast(getString(R.string.vantop_overtime_apply_success));
//                Emiter.getInstance().emit("VanTopApprovalListFragment", Constants.REFRESH);
                EventBusMsg messageEvent = new EventBusMsg();
                messageEvent.setCode(Constants.REFRESH);
                EventBus.getDefault().post(messageEvent);
                finish();
                break;
            case OVERTIMEAPPLY_NEW:

                try {

                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");

                    JSONObject typeJson = jsonObject.getJSONObject("types");
                    typeValues = GsonUtils.parseDataToMap(typeJson.getString("values"));
                    if (typeJson.getBoolean("status")) {
                        overtimeTypeRl.setVisibility(View.VISIBLE);
                        overtime_line.setVisibility(View.VISIBLE);
                    }
                    JSONObject shiftJson = jsonObject.getJSONObject("shifts");
                    shiftJsonText = shiftJson.optString("values");
                    shiftValues = GsonUtils.parseDataToArrayList(shiftJson.getString("values"));
                    if (shiftJson.getBoolean("status")) {
                        shiftRl.setVisibility(View.VISIBLE);
                        shift_line.setVisibility(View.VISIBLE);
                    }
                    JSONObject approverJson = jsonObject.getJSONObject("approvers");
                    approverValues = GsonUtils.parseDataToArrayList(approverJson.getString("values"));
                    if (approverValues != null && approverJson.has("defaultValue")) {
                        String approvalStaffno = approverJson.getString("defaultValue");
                        for (Map<String, String> approvalMap : approverValues) {
                            if (approvalStaffno.equals(approvalMap.get("staffNo"))) {
                                String staffName = approvalMap.get("staffName");
                                approvalSupervisorTxt.setText(staffName);
                                CreatedOverTimeActivity.this.approvalSupervisor = approvalStaffno;
                                break;
                            }
                        }
                    }

                    JSONObject conventLvJson = jsonObject.getJSONObject("conventLvs");
                    conventLvValues = GsonUtils.parseDataToArrayList(conventLvJson.getString("values"));
                    if (conventLvJson.getBoolean("status")) {
                        convertLl.setVisibility(View.VISIBLE);
                        convert_line.setVisibility(View.VISIBLE);
                        mHasConvertJZ = conventLvJson.getBoolean("hasConvertJZ");
                    }

                    try {
                        convertnum_onlyread = conventLvJson.getBoolean("convertnum_onlyread");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JSONObject overtimePicJson = jsonObject.getJSONObject("overtimePic");
                    if (overtimePicJson.getBoolean("status")) {
                        attachFileLl.setVisibility(View.VISIBLE);
                        explain_line.setVisibility(View.VISIBLE);
                    }

                    JSONObject deductHourJson = jsonObject.getJSONObject("deductHours");
                    deductHourStatus = deductHourJson.getBoolean("status");
                    if (deductHourStatus) {
                        defaultValue = deductHourJson.getString("defaultValue");
                        try {
                            if (!TextUtils.isEmpty(defaultValue)){
//                                Log.e("TAG_加班","defaultValue="+defaultValue);
                                Double aDouble = Double.valueOf(defaultValue);
                                deduceatDefault = aDouble.intValue();
//                                Log.e("TAG_加班","deduceatDefault="+deduceatDefault);
                                deducEatNumTxt.setText(MapUtils.getdeductHourValues(CreatedOverTimeActivity.this).get(deduceatDefault));
                            }

                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        deduceatDefault = 0;
                        deducEatNumTxt.setText(MapUtils.getdeductHourValues(CreatedOverTimeActivity.this).get(0));
                    }
                    //休息时间
                    JSONObject timeSliceJson = jsonObject.getJSONObject("timeSlice");
                    mTimeSliceStatus = timeSliceJson.getBoolean("status");

                    Log.e("TAG_加班申请", "mTimeSliceStatus=" + mTimeSliceStatus);
                    mSliceStartTimeList = new ArrayList<>();
                    mSliceEndTimeList = new ArrayList<>();
                    if (mTimeSliceStatus) {
                        String sliceTimes = timeSliceJson.getString("values");
                        String[] start_end_times = sliceTimes.split("\\|");
                        for (int i = 0; i < start_end_times.length; i++) {
                            String[] start_end = start_end_times[i].split("-");
                            if (start_end.length != 2) {
                                Log.e("timeSlice", "dataLoaded: 服务器返回的timeSlice时间段错误！！！");
                                continue;
                            }
                            String startTime = start_end[0];
                            mSliceStartTimeList.add(startTime);

                            String endTime = start_end[1];
                            mSliceEndTimeList.add(endTime);
                        }
                        //-------------------------------------------------------
//                        if (deduceatNumClickView.getVisibility() != View.GONE) {
//                            deduceatNumClickView.setVisibility(View.GONE);
//                            deduceatNumClickDivider.setVisibility(View.GONE);
//                        }
                    }
//                    else {
                    if (deductHourStatus) {
                        if (deduceatNumClickView.getVisibility() != View.VISIBLE) {
                            deduceatNumClickView.setVisibility(View.VISIBLE);
                            deduceatNumClickDivider.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (deduceatNumClickView.getVisibility() != View.GONE) {
                            deduceatNumClickView.setVisibility(View.GONE);
                            deduceatNumClickDivider.setVisibility(View.GONE);
                        }
                    }
//                    }
                    //-----------------------------------------
                    JSONObject overtimeMinUnitJson = jsonObject.getJSONObject("overtimeMinUnit");
                    boolean status = overtimeMinUnitJson.getBoolean("status");
                    try {
                        mOvertimeMinUnit = overtimeMinUnitJson.getDouble("values");
                    } catch (Exception e) {
                        e.printStackTrace();
                        mOvertimeMinUnit = 0;
                    }
                    if (!status) {
                        mOvertimeMinUnit = 0;
                    }

                    JSONObject overtimeMinShichangJson = jsonObject.getJSONObject("overtimeMinShichang");
                    boolean shichang_status = overtimeMinShichangJson.getBoolean("status");
                    try {
                        mOvertimeMinShichang = overtimeMinShichangJson.optDouble("values");
                    } catch (Exception e) {
                        e.printStackTrace();
                        mOvertimeMinShichang = 0;
                    }
                    if (!shichang_status) {
                        mOvertimeMinShichang = 0;
                    }
                    changeDataInflate();

                    JSONObject isvisitTimeSlice = jsonObject.getJSONObject("isvisitTimeSlice");
                    mIsNeedRestTime = isvisitTimeSlice.getBoolean("status");
                    //加班原因是否必填
                    JSONObject overtimeRemark = jsonObject.getJSONObject("overtimeRemark");
                    isOvertimeRemark = overtimeRemark.getBoolean("status");
                    buSubmit.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToast("数据请求异常 ！");
                }
                if (!getString(R.string.vantop_please_select).equals(startTime) && !getString(R.string.vantop_please_select).equals(endTime)) {
//                    if (isFirstThenSecond(endTime, startTime)) {
                        calculVertimeNum();
//                    }
                }
                break;

            case CODE_TIME_SLICE:
                mRestStartTimeList.clear();
                mRestEndTimeList.clear();
                try {
                    String restTimes = rootData.getJson().getString("data");
                    String[] start_end_times = restTimes.split("\\|");
                    for (int i = 0; i < start_end_times.length; i++) {
                        String[] start_end = start_end_times[i].split("-");
                        if (start_end.length != 2) {
                            Log.e("timeSlice", "dataLoaded: 服务器返回的timeSlice时间段错误！！！");
                            continue;
                        }
                        String startTime = start_end[0];
                        mRestStartTimeList.add(startTime);

                        String endTime = start_end[1];
                        mRestEndTimeList.add(endTime);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                calculVertimeNumFinal();
                break;
            default:
                break;

        }
    }

    @Override
    public void onFailure(int callbackId, String data) {
        dismisLoadingDialog();
        ToastUtil.showToast(data);
    }
}
