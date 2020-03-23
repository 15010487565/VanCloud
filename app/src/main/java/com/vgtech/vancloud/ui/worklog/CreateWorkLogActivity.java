package com.vgtech.vancloud.ui.worklog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.CostCenter;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.WorkLogBean;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.permissions.PermissionsUtil;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.CostCenterAdapter;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateWorkLogActivity extends SearchBaseActivity implements HttpListener<String> {

    private static final int INTENT_SELECT_PIC = 0;
    private static final int CALLBACK_WORK_LOG_SUBMIT = 1;
    private static final int CALLBACK_WORK_LOG_DELETE = 2;
    private static final int CALLBACK_WORK_LOG_UPDATE = 3;
    private TextView mTitleDate;
    private SelectView mSelectStartTime;//开始时间
    private SelectView mSelectEndTime;//结束时间
    private SelectView mSelectWorkDuration;//工时
    private EditText mEtWorkLocation;//工作地点
    private EditText mEtWorkPerson;//工作相关人
    private EditText mEtWorkDesc;//工作描述
    private EditText mEtWorkContent;//工作详细内容
    private SelectView mSelectCostCenter;//工作部门、成本中心
    private EditText mEtWorkIdea;//工作想法、感想
    //    private GridView mGvEnclosure;//附件
    private Button mBtnSave;
    private String mDate;
    private NetworkManager mNetworkManager;
    private View mContentView;
    private VancloudLoadingLayout mLoadingView;
    private ListView mLvCostCenter;
    private CostCenterAdapter mCostCenterAdapter;
    private View mShadowView;
    private RelativeLayout mRlAttachmentPicContainer;
    private SimpleDraweeView mIvAttachmentPic;
    private ImageView mIvAttachmentPicDel;
    private ImageView mIvAttachmentPicAdd;

    private WorkLogBean mWorkLogBean;
    private Button mBtnDelete;
    private boolean mIsModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_work_log);

        mDate = getIntent().getStringExtra("date");
        mWorkLogBean = (WorkLogBean) getIntent().getSerializableExtra("work_log_bean");
        mIsModify = getIntent().getBooleanExtra("is_modify", false);

        initView();
        initData();
        mNetworkManager = getAppliction().getNetworkManager();
    }

    private void initView() {
        initTitleLayout();

        mTitleDate = (TextView) findViewById(R.id.tv_title_date);
        if (TextUtils.isEmpty(mDate)) {
            String date = DateTimeUtil.getCurrentString("yyyy/MM/dd");
            mDate = DateTimeUtil.getCurrentString("yyyy-MM-dd");
            mTitleDate.setText(date);
        } else {
            mTitleDate.setText(mDate.replace("-", "/"));
        }

        findViewById(R.id.search).setVisibility(View.GONE);
        findViewById(R.id.add).setVisibility(View.GONE);

        mContentView = findViewById(R.id.create_work_log_sv_content);
        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.loading_layout);

        mSelectStartTime = (SelectView) findViewById(R.id.create_work_log_select_start_time);
        mSelectEndTime = (SelectView) findViewById(R.id.create_work_log_select_end_time);
        mSelectWorkDuration = (SelectView) findViewById(R.id.create_work_log_select_work_duration);
        mEtWorkLocation = (EditText) findViewById(R.id.create_work_log_et_work_location);
        mEtWorkPerson = (EditText) findViewById(R.id.create_work_log_et_work_person);
        mEtWorkDesc = (EditText) findViewById(R.id.create_work_log_et_work_desc);
        mEtWorkContent = (EditText) findViewById(R.id.create_work_log_et_work_content);
        mSelectCostCenter = (SelectView) findViewById(R.id.create_work_log_select_work_branch);
        mEtWorkIdea = (EditText) findViewById(R.id.create_work_log_et_work_idea);
//        mGvEnclosure = (GridView) findViewById(R.id.create_work_log_gv_enclosure);
        mRlAttachmentPicContainer = (RelativeLayout) findViewById(R.id.create_worklog_rl_attachment_pic_container);
        mIvAttachmentPic = (SimpleDraweeView) findViewById(R.id.create_worklog_iv_attachment_pic);
        mIvAttachmentPicDel = (ImageView) findViewById(R.id.create_worklog_iv_attachment_pic_delete);
        mIvAttachmentPicAdd = (ImageView) findViewById(R.id.create_worklog_iv_attachment_pic_add);
        mBtnSave = (Button) findViewById(R.id.create_work_log_btn_save);
        mBtnDelete = (Button) findViewById(R.id.create_work_log_btn_delete);

        mShadowView = findViewById(R.id.shade_view);
        mLvCostCenter = (ListView) findViewById(R.id.create_work_log_lv_cost_center);
        mCostCenterAdapter = new CostCenterAdapter();
        mLvCostCenter.setAdapter(mCostCenterAdapter);

        mSelectStartTime.setOnClickListener(this);
        mSelectEndTime.setOnClickListener(this);
        mSelectCostCenter.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mIvAttachmentPicDel.setOnClickListener(this);
        mIvAttachmentPicAdd.setOnClickListener(this);
//        mGvEnclosure.setOnItemClickListener(this);
        mLvCostCenter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mShadowView.setVisibility(View.GONE);
                mLvCostCenter.setVisibility(View.GONE);
                List<CostCenter> data = mCostCenterAdapter.getData();
                mSelectCostCenter.setSelectValue(data.get(position).getDictionary_name_chn());
                mSelectCostCenter.setTag(data.get(position).getDictionary_code());
            }
        });
    }

    private void initData() {
        if (mIsModify && mWorkLogBean != null) {
            mDate = mWorkLogBean.getDates();
            mTitleDate.setText(mDate.replace("-", "/"));
            mSelectStartTime.setSelectValue(mWorkLogBean.getFromTime());
            mSelectEndTime.setSelectValue(mWorkLogBean.getToTime());
            mSelectWorkDuration.setSelectValue(mWorkLogBean.getDuration());
            mEtWorkLocation.setText(mWorkLogBean.getWorkLocation());
            mEtWorkPerson.setText(mWorkLogBean.getRelatedPerson());
            mEtWorkDesc.setText(mWorkLogBean.getWorkBrief());
            mEtWorkContent.setText(mWorkLogBean.getWorkContent());
            mSelectCostCenter.setSelectValue(mWorkLogBean.getCostName());
            mEtWorkIdea.setText(mWorkLogBean.getMyReflections());
            if (mRlAttachmentPicContainer.getVisibility() != View.VISIBLE)
                mRlAttachmentPicContainer.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(mWorkLogBean.getImageUrl())) {
                mRlAttachmentPicContainer.setVisibility(View.GONE);
            } else {
                mRlAttachmentPicContainer.setVisibility(View.VISIBLE);
                mIvAttachmentPic.setTag(mWorkLogBean.getImageUrl());
                //清空缓存
                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                imagePipeline.evictFromMemoryCache(Uri.parse(mWorkLogBean.getImageUrl()));
                imagePipeline.evictFromDiskCache(Uri.parse(mWorkLogBean.getImageUrl()));
                imagePipeline.evictFromCache(Uri.parse(mWorkLogBean.getImageUrl()));

                ImageOptions.setImage(mIvAttachmentPic, mWorkLogBean.getImageUrl());
            }
            if (mBtnDelete.getVisibility() != View.VISIBLE) {
                mBtnDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_work_log_select_start_time:
                showDateDialog(mSelectStartTime);
                break;
            case R.id.create_work_log_select_end_time:
                showDateDialog(mSelectEndTime);
                break;
            case R.id.create_work_log_select_work_branch:
                showCostCenterList();
                break;
            case R.id.create_work_log_btn_save:
                if (!mIsModify) {
                    submit2Server();
                } else {
                    update2Serer();
                }
                break;
            case R.id.create_work_log_btn_delete:
                delete2Server();
                break;
            case R.id.shade_view:
                mShadowView.setVisibility(View.GONE);
                mLvCostCenter.setVisibility(View.GONE);
                return;
            case R.id.create_worklog_iv_attachment_pic_add:

                Intent intent = new Intent(this, PicSelectActivity.class);
                intent.putExtra("single", true);
                startActivityForResult(intent, INTENT_SELECT_PIC);
                break;
            case R.id.create_worklog_iv_attachment_pic_delete:
                if (mRlAttachmentPicContainer.getVisibility() != View.GONE)
                    mRlAttachmentPicContainer.setVisibility(View.GONE);
                mIvAttachmentPic.setImageBitmap(null);
                mIvAttachmentPic.setTag("");
                break;
        }
        super.onClick(v);
    }

    private void submit2Server() {
        String startTime = "";
        if (!getString(R.string.please_select).equals(mSelectStartTime.getSelectValue())
                || TextUtils.isEmpty(mSelectStartTime.getSelectValue())) {
            startTime = mSelectStartTime.getSelectValue();
        }
        String endTime = "";
        if (!getString(R.string.please_select).equals(mSelectEndTime.getSelectValue())
                || TextUtils.isEmpty(mSelectEndTime.getSelectValue())) {
            endTime = mSelectEndTime.getSelectValue();
        }
        String duration = "";
        if (!TextUtils.isEmpty(mSelectWorkDuration.getSelectValue())) {
            duration = mSelectWorkDuration.getSelectValue();
        }
        String workLocation = "";
        if (!TextUtils.isEmpty(mEtWorkLocation.getText().toString().trim())) {
            workLocation = mEtWorkLocation.getText().toString().trim();
        }
        String workPerson = "";
        if (!TextUtils.isEmpty(mEtWorkPerson.getText().toString().trim())) {
            workPerson = mEtWorkPerson.getText().toString().trim();
        }
        String workDesc = "";
        if (!TextUtils.isEmpty(mEtWorkDesc.getText().toString().trim())) {
            workDesc = mEtWorkDesc.getText().toString().trim();
        }
        String workContent = "";
        if (!TextUtils.isEmpty(mEtWorkContent.getText().toString().trim())) {
            workContent = mEtWorkContent.getText().toString().trim();
        }
        String costCenter = "";
        if (!getString(R.string.please_select).equals(mSelectCostCenter.getSelectValue())) {
            costCenter = ((String) mSelectCostCenter.getTag());
        }
        String workIdea = "";
        if (!TextUtils.isEmpty(mEtWorkIdea.getText().toString().trim())) {
            workIdea = mEtWorkIdea.getText().toString().trim();
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("date", mDate);
        params.put("fromTime", startTime);
        params.put("toTime", endTime);
        params.put("duration", duration);
        params.put("workLocation", workLocation);
        params.put("relatedPerson", workPerson);
        params.put("workBrief", workDesc);
        params.put("workContent", workContent);
        params.put("costCode", costCenter);
        params.put("myReflections", workIdea);

        String url = (String) mIvAttachmentPic.getTag();
        FilePair picPair = null;
        if (TextUtils.isEmpty(url)) {
            picPair = null;
        } else {
            String bmpName = url.substring(
                    url.lastIndexOf("/") + 1,
                    url.lastIndexOf("."));
            Bitmap bitmap = Bimp.getimage(url);
            String picPath = FileUtils.saveBitmap(this, bitmap, bmpName, "jpg");
            picPair = new FilePair("pic", new File(picPath));
        }
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_ADD_WORK_LOG), params, picPair, this, true);
        mNetworkManager.load(CALLBACK_WORK_LOG_SUBMIT, path, this);


    }

    private void update2Serer() {
        String startTime = "";
        if (!getString(R.string.please_select).equals(mSelectStartTime.getSelectValue())
                || TextUtils.isEmpty(mSelectStartTime.getSelectValue())) {
            startTime = mSelectStartTime.getSelectValue();
        }
        String endTime = "";
        if (!getString(R.string.please_select).equals(mSelectEndTime.getSelectValue())
                || TextUtils.isEmpty(mSelectEndTime.getSelectValue())) {
            endTime = mSelectEndTime.getSelectValue();
        }
        String duration = "";
        if (!TextUtils.isEmpty(mSelectWorkDuration.getSelectValue())) {
            duration = mSelectWorkDuration.getSelectValue();
        }
        String workLocation = "";
        if (!TextUtils.isEmpty(mEtWorkLocation.getText().toString().trim())) {
            workLocation = mEtWorkLocation.getText().toString().trim();
        }
        String workPerson = "";
        if (!TextUtils.isEmpty(mEtWorkPerson.getText().toString().trim())) {
            workPerson = mEtWorkPerson.getText().toString().trim();
        }
        String workDesc = "";
        if (!TextUtils.isEmpty(mEtWorkDesc.getText().toString().trim())) {
            workDesc = mEtWorkDesc.getText().toString().trim();
        }
        String workContent = "";
        if (!TextUtils.isEmpty(mEtWorkContent.getText().toString().trim())) {
            workContent = mEtWorkContent.getText().toString().trim();
        }
        String costCenter = "";
        if (!getString(R.string.please_select).equals(mSelectCostCenter.getSelectValue())) {
            costCenter = ((String) mSelectCostCenter.getTag());
            if (TextUtils.isEmpty(costCenter)){
                costCenter = "";
            }
        }
        String workIdea = "";
        if (!TextUtils.isEmpty(mEtWorkIdea.getText().toString().trim())) {
            workIdea = mEtWorkIdea.getText().toString().trim();
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("date", mDate);
        params.put("logId", mWorkLogBean.getLogId());
        params.put("fromTime", startTime);
        params.put("toTime", endTime);
        params.put("duration", duration);
        params.put("workLocation", workLocation);
        params.put("relatedPerson", workPerson);
        params.put("workBrief", workDesc);
        params.put("workContent", workContent);
        params.put("costCode", costCenter);
        params.put("myReflections", workIdea);

        String url = (String) mIvAttachmentPic.getTag();
        if (!TextUtils.isEmpty(url) && url.equals(mWorkLogBean.getImageUrl())) {
            //没有更新图片
            params.put("isUpdatePic", "false");
        } else if (TextUtils.isEmpty(url) && TextUtils.isEmpty(mWorkLogBean.getImageUrl())) {
            params.put("isUpdatePic", "false");
        } else {
            params.put("isUpdatePic", "true");
        }
        FilePair picPair;
        if (TextUtils.isEmpty(url)) {
            picPair = null;
        } else if (!TextUtils.isEmpty(url) && url.equals(mWorkLogBean.getImageUrl())) {
            //没有更新图片
            picPair = null;
        } else {
            String bmpName = url.substring(
                    url.lastIndexOf("/") + 1,
                    url.lastIndexOf("."));
            Bitmap bitmap = Bimp.getimage(url);
            String picPath = FileUtils.saveBitmap(this, bitmap, bmpName, "jpg");
            picPair = new FilePair("pic", new File(picPath));
        }

        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_UPDATE_WORK_LOG), params, picPair, this, true);
        mNetworkManager.load(CALLBACK_WORK_LOG_UPDATE, path, this);
    }

    private void delete2Server() {
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        params.put("date", mDate);
        params.put("logId", mWorkLogBean.getLogId());
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_DEL_WORK_LOG), params, this, true);
        mNetworkManager.load(CALLBACK_WORK_LOG_DELETE, path, this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_SELECT_PIC && resultCode == Activity.RESULT_OK && data != null) {
            String url = data.getStringExtra("path");
            if (mRlAttachmentPicContainer.getVisibility() != View.VISIBLE) {
                mRlAttachmentPicContainer.setVisibility(View.VISIBLE);
            }
            Bitmap bitmap = Bimp.getimage(url);
            mIvAttachmentPic.setImageBitmap(bitmap);
            mIvAttachmentPic.setTag(url);
        }
    }

    /**
     * 显示成本中心列表
     */
    private void showCostCenterList() {
        mShadowView.setVisibility(View.VISIBLE);
        mLvCostCenter.setVisibility(View.VISIBLE);
        if (WorklogActivity.mCostCentersList == null || WorklogActivity.mCostCentersList.size() == 0) {
            Toast.makeText(this, R.string.toast_cost_center_fialed, Toast.LENGTH_SHORT).show();
        } else {
            mCostCenterAdapter.setData(WorklogActivity.mCostCentersList);
        }
        hideKeyboard();
    }

    /**
     * 显示日期对话框
     *
     * @param selectView
     */
    private void showDateDialog(@NonNull final SelectView selectView) {
        Calendar calendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(selectView.getSelectValue()) && !getString(R.string.please_select).equals(selectView.getSelectValue())) {
            long selectTime = DateTimeUtil.stringToLong_YMdhm(mDate + " " + selectView.getSelectValue());
            calendar.setTimeInMillis(selectTime);
        }
        final DateFullDialogView timeDialog = new DateFullDialogView(this, new DateFullDialogView.OnSelectedListener() {
            @Override
            public void onSelectedListener(long time) {

            }
        }, "Hm", "time-hm", calendar, getResources().getColor(com.vgtech.vantop.R.color.text_black), calendar);
        timeDialog.setButtonClickListener(new DateFullDialogView.ButtonClickListener() {
            @Override
            public void sureButtonOnClickListener(String time) {
                if (selectView == mSelectStartTime) {
                    String endTime = mSelectEndTime.getSelectValue();
                    if (!TextUtils.isEmpty(endTime) && !getString(R.string.please_select).equals(endTime)) {
                        long endDT = DateTimeUtil.stringToLong_YMdhm(mDate + " " + endTime);
                        long startDT = DateTimeUtil.stringToLong_YMdhm(mDate + " " + time);
                        if ((endDT - startDT) <= 0) {
                            Toast.makeText(CreateWorkLogActivity.this, getString(R.string.toast_endtime_starttime), Toast.LENGTH_SHORT).show();
                            timeDialog.dismiss();
                            return;
                        } else {
                            double duration = DateTimeUtil.formatDuringH2(endDT - startDT);
                            mSelectWorkDuration.setSelectValue(duration + "");
                        }
                    }
                } else if (selectView == mSelectEndTime) {
                    String startTime = mSelectStartTime.getSelectValue();
                    if (!TextUtils.isEmpty(startTime) && !getString(R.string.please_select).equals(startTime)) {
                        long startDT = DateTimeUtil.stringToLong_YMdhm(mDate + " " + startTime);
                        long endDT = DateTimeUtil.stringToLong_YMdhm(mDate + " " + time);
                        if ((endDT - startDT) <= 0) {
                            Toast.makeText(CreateWorkLogActivity.this, getString(R.string.toast_endtime_starttime), Toast.LENGTH_SHORT).show();
                            timeDialog.dismiss();
                            return;
                        } else {
                            double duration = DateTimeUtil.formatDuringH2(endDT - startDT);
                            mSelectWorkDuration.setSelectValue(duration + "");
                        }
                    }
                }
                selectView.setSelectValue(time);
                timeDialog.dismiss();
            }

            @Override
            public void cancelButtonOnClickListener() {

            }
        });
        timeDialog.show(null);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mContentView);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == CALLBACK_WORK_LOG_SUBMIT) {
                Toast.makeText(this, R.string.toast_save_work_log_failed, Toast.LENGTH_SHORT).show();
            } else if (callbackId == CALLBACK_WORK_LOG_UPDATE) {
                Toast.makeText(this, R.string.toast_update_work_log_failed, Toast.LENGTH_SHORT).show();
            } else if (callbackId == CALLBACK_WORK_LOG_DELETE) {
                Toast.makeText(this, R.string.toast_deleted_work_log_failed, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_WORK_LOG_SUBMIT:
                Toast.makeText(this, R.string.toast_save_work_log_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                break;

            case CALLBACK_WORK_LOG_UPDATE:
                Toast.makeText(this, R.string.toast_update_work_log_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                break;

            case CALLBACK_WORK_LOG_DELETE:
                Toast.makeText(this, R.string.toast_deleted_work_log_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
