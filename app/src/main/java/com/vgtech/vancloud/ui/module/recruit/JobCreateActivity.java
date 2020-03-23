package com.vgtech.vancloud.ui.module.recruit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.adapter.UserGridAdapter;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.Position;
import com.vgtech.common.api.RecruitmentInfoBean;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.AreaSelectActivity;
import com.vgtech.common.ui.DictSelectActivity;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.api.JobModule;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedListener;
import com.vgtech.vancloud.ui.module.flow.FlowReportNewActivity;
import com.vgtech.vancloud.ui.module.me.DepartSelectedActivity;
import com.vgtech.vancloud.ui.register.ui.CompanyInfoEditActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zhangshaofang on 2016/1/10.
 * 发布职位
 */
public class JobCreateActivity extends BaseActivity implements View.OnClickListener, HttpListener<String> {

    @InjectView(R.id.et_job_name)
    EditText etJobName;
    @InjectView(R.id.tv_depart)
    TextView tvDepart;
    @InjectView(R.id.depart_layout)
    RelativeLayout departLayout;
    @InjectView(R.id.tv_report)
    TextView tvReport;
    @InjectView(R.id.report_layout)
    RelativeLayout reportLayout;
    @InjectView(R.id.tv_city)
    TextView tvCity;
    @InjectView(R.id.city_layout)
    RelativeLayout cityLayout;
    @InjectView(R.id.tv_category)
    TextView tvCategory;
    @InjectView(R.id.category_layout)
    RelativeLayout categoryLayout;
    @InjectView(R.id.tv_type)
    TextView tvType;
    @InjectView(R.id.type_layout)
    RelativeLayout typeLayout;
    @InjectView(R.id.tv_education)
    TextView tvEducation;
    @InjectView(R.id.education_layout)
    RelativeLayout educationLayout;
    @InjectView(R.id.tv_work_experience)
    TextView tvWorkExperience;
    @InjectView(R.id.work_experience_layout)
    RelativeLayout workExperienceLayout;
    @InjectView(R.id.tv_work_city)
    TextView tvWorkCity;
    @InjectView(R.id.work_city_layout)
    RelativeLayout workCityLayout;
    @InjectView(R.id.et_work_city_address)
    EditText etWorkCityAddress;
    @InjectView(R.id.tv_salary)
    TextView tvSalary;
    @InjectView(R.id.salary_layout)
    RelativeLayout salaryLayout;
    @InjectView(R.id.et_choose_model)
    EditText etChooseModel;
    @InjectView(R.id.et_demand)
    EditText etDemand;
    @InjectView(R.id.tv_welfare)
    TextView tvWelfare;
    @InjectView(R.id.welfare_layout)
    RelativeLayout welfareLayout;
    @InjectView(R.id.btn_submit)
    Button btnSubmit;
    @InjectView(R.id.select_user_view)
    LinearLayout selectUserView;

    @InjectView(R.id.job_module_layout)
    RelativeLayout jobModuleLayout;
    @InjectView(R.id.tv_job_module)
    TextView tvJobModule;
    @InjectView(R.id.tv_language_one)
    TextView tvLanguageOne;
    @InjectView(R.id.language_one_layout)
    RelativeLayout languageOneLayout;
    @InjectView(R.id.tv_language_one_mastery_degree)
    TextView tvLanguageOneMasteryDegree;
    @InjectView(R.id.language_one_mastery_degree_layout)
    RelativeLayout languageOneMasteryDegreeLayout;
    @InjectView(R.id.tv_language_two)
    TextView tvLanguageTwo;
    @InjectView(R.id.language_two_layout)
    RelativeLayout languageTwoLayout;
    @InjectView(R.id.tv_language_two_mastery_degree)
    TextView tvLanguageTwoMasteryDegree;
    @InjectView(R.id.language_two_mastery_degree_layout)
    RelativeLayout languageTwoMasteryDegreeLayout;
    @InjectView(R.id.tv_zhuanye_one)
    TextView tvZhuanyeOne;
    @InjectView(R.id.zhuanye_one_layout)
    RelativeLayout zhuanyeOneLayout;
    @InjectView(R.id.tv_zhuanye_two)
    TextView tvZhuanyeTwo;
    @InjectView(R.id.zhuanye_two_layout)
    RelativeLayout zhuanyeTwoLayout;

    private static final int REQUEST_DEPART_ACTION = 0x0001; //目标部门
    private static final int REQUEST_CITY_ACTION = 0x0002; //职位发布地点
    private static final int REQUEST_SORT_ACTION = 0x0003; //职能类别
    private static final int REQUEST_WORKCITY_ACTION = 0x0005; //工作地区
    private static final int REQUEST_WELFARE_ACTION = 0x00060; //福利待遇
    private static final int REQUEST_CODE_CCPEOPLE = 0x0007; //抄送人
    private static final int REQUEST_CODE_APPROVE = 0x0008; //审批人

    private TextView mStartCompleteTimeTv;//开始时间
    private TextView mEndCompleteTimeTv;//结束时间
    private TextView mCcPeopleCountTv;//抄送人数量
    private TextView mApprovalCountTv;//审批人数量
    private GridView mCcPeopleGridView;//抄送人
    private GridView mApprovalGridView;//审批人
    private CheckBox mEmployCb;//是否委托万客
    private EditText mNumEt;//人数

    private View approvalStubView;
    private String type; //1,从招聘模块开始创建，2，从流程开始创建
    private String isPublish; //已发布状态
    private List<Position> mPrositionList;
    private static final int CALL_BACK_REPORT = 1; //汇报对象
    private static final int CALL_BACK_SUBMIT = 2; //创建提交
    private static final int CALL_BACK_FINISH = 3; //完成招聘
    private NetworkManager mNetworkManager;
    private String recruit_id;// 修改时传职位的id
    private RecruitmentInfoBean recruitmentInfoBean;
    private ArrayList<Node> cCPeopleList = null;
    private ArrayList<Node> approvalList = null;
    private boolean isEmploy; //是否委托万客

    private ImageView requiredImg;

    @Override
    protected int getContentView() {
        return R.layout.job_create;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        Intent intent = getIntent();
        recruit_id = intent.getExtras().getString("recruit_id");
        type = intent.getExtras().getString("type");
        isPublish = intent.getExtras().getString("is_publish");
        recruitmentInfoBean = (RecruitmentInfoBean) intent.getExtras().getSerializable("resumeInfo");
        mNetworkManager = getAppliction().getNetworkManager();
        initView();
        initListener();
        if (!TextUtils.isEmpty(recruit_id)) {
            setTitle(getResources().getString(R.string.edit_position));
            initJobInfo(recruitmentInfoBean);
            initRightTv(getString(R.string.save));
            if ("publish".equals(isPublish)) {
                btnSubmit.setText(getString(R.string.recruit_finish));
                btnSubmit.setVisibility(View.VISIBLE);
            } else {
                btnSubmit.setVisibility(View.GONE);
            }
        }
        if ("2".equals(type)) { //从流程模块创建
            departLayout.setVisibility(View.VISIBLE);
            reportLayout.setVisibility(View.VISIBLE);
            approvalStubView.setVisibility(View.VISIBLE);
            selectUserView.setVisibility(View.VISIBLE);
            requiredImg.setVisibility(View.VISIBLE);
            mIdMap.put(103, "0");
            mNumEt.setText("1");
            isEmploy = true;
        } else {//默认从招聘模块创建
            departLayout.setVisibility(View.GONE);
            reportLayout.setVisibility(View.GONE);
            approvalStubView.setVisibility(View.GONE);
            selectUserView.setVisibility(View.GONE);
        }
        mEmployCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isEmploy = true;
                    mEmployCb.setBackgroundResource(R.drawable.check_true);
                } else {
                    isEmploy = false;
                    mEmployCb.setBackgroundResource(R.drawable.check_false);
                }
            }
        });
    }

    private void initView() {
        findViewById(R.id.btn_start_time).setOnClickListener(this);
        findViewById(R.id.btn_end_time).setOnClickListener(this);
        mStartCompleteTimeTv = (TextView) findViewById(R.id.start_complete_time);
        mEndCompleteTimeTv = (TextView) findViewById(R.id.end_complete_time);
        mNumEt = (EditText) findViewById(R.id.et_num);
        mCcPeopleCountTv = (TextView) findViewById(R.id.tv_cc_people_count);
        mCcPeopleGridView = (GridView) findViewById(R.id.cc_people_gridview);
        mEmployCb = (CheckBox) findViewById(R.id.cb_employ);
        if (approvalStubView == null) {
            approvalStubView = ((ViewStub) findViewById(R.id.vs_approval)).inflate();
            initApprovalView(approvalStubView);
        } else {
            approvalStubView.setVisibility(View.VISIBLE);
        }
    }

    private void initListener() {
        jobModuleLayout.setOnClickListener(this);
        departLayout.setOnClickListener(this);
        reportLayout.setOnClickListener(this);
        cityLayout.setOnClickListener(this);
        categoryLayout.setOnClickListener(this);
        typeLayout.setOnClickListener(this);
        educationLayout.setOnClickListener(this);
        workExperienceLayout.setOnClickListener(this);
        workCityLayout.setOnClickListener(this);
        salaryLayout.setOnClickListener(this);
        welfareLayout.setOnClickListener(this);
        languageOneLayout.setOnClickListener(this);
        languageOneMasteryDegreeLayout.setOnClickListener(this);
        languageTwoLayout.setOnClickListener(this);
        languageTwoMasteryDegreeLayout.setOnClickListener(this);
        zhuanyeOneLayout.setOnClickListener(this);
        zhuanyeTwoLayout.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        findViewById(R.id.ll_cc_people).setOnClickListener(this);
        findViewById(R.id.ll_approval).setOnClickListener(this);

        mNumEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                int len = s.toString().length();
                if (len == 1 && text.equals("0")) {
                    s.clear();
                }
        }});
    }

    private void initApprovalView(View stubView) {
        stubView.findViewById(R.id.ll_approval).setOnClickListener(this);
        mApprovalCountTv = (TextView) stubView.findViewById(R.id.tv_approval_count);
        mApprovalGridView = (GridView) stubView.findViewById(R.id.approval_gridview);
        requiredImg = (ImageView) stubView.findViewById(R.id.required_img);
    }

    //修改职位初始化
    private void initJobInfo(RecruitmentInfoBean jobInfo) {
        try {
            etJobName.setText(jobInfo.job_name);
            tvCity.setText(jobInfo.job_area);
            tvCategory.setText(jobInfo.func_type);
            tvType.setText(jobInfo.term);
            tvEducation.setText(jobInfo.degree_from);
            tvWorkExperience.setText(jobInfo.work_year);
            tvWorkCity.setText(jobInfo.work_area);
            etWorkCityAddress.setText(jobInfo.work_address);
            tvSalary.setText(jobInfo.salray_range);
            etChooseModel.setText(jobInfo.job_desc);
            etDemand.setText(jobInfo.job_requirement);
            tvWelfare.setText(jobInfo.job_welfare);
            mNumEt.setText(jobInfo.job_num);
            mStartCompleteTimeTv.setText(Utils.dateFormatStr(Long.valueOf(jobInfo.job_start_date)));
            mEndCompleteTimeTv.setText(Utils.dateFormatStr(Long.valueOf(jobInfo.job_end_date)));
            String is_delegate = jobInfo.is_delegate;
            if ("Y".equals(is_delegate) || mEmployCb.isChecked()) {
                mEmployCb.setBackgroundResource(R.drawable.check_true);
                isEmploy = true;
            } else {
                mEmployCb.setBackgroundResource(R.drawable.check_false);
                isEmploy = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        hideInputMethod();
        switch (v.getId()) {
            case R.id.btn_back:
                showExitEdit();
                break;
            case R.id.btn_submit:
                if ("publish".equals(isPublish)) {//完成招聘
                    completeAction();
                } else {
                    submit();
                }
                break;
            case R.id.tv_right://修改时保存
                submit();
                break;
            case R.id.job_module_layout://TODO 职位模板
                Intent intent_jobmodule = new Intent(this, JobModuleActivity.class);
                startActivityForResult(intent_jobmodule, 210);
                break;
            case R.id.depart_layout://目标部门
                Intent intent_depart = new Intent(this, DepartSelectedActivity.class);
                startActivityForResult(intent_depart, REQUEST_DEPART_ACTION);
                break;
            case R.id.report_layout://汇报对象
                if (mPrositionList == null) {
                    loadPositionInfo();
                } else {
                    showPositionSelected();
                }
                break;
            case R.id.city_layout://职位发布地点
                Intent intent_city = new Intent(this, AreaSelectActivity.class);
                intent_city.putExtra("id", mIdMap.get(REQUEST_CITY_ACTION));
                startActivityForResult(intent_city, REQUEST_CITY_ACTION);
                break;
            case R.id.category_layout://职能类别
                Intent intent_category = new Intent(this, DictSelectActivity.class);
                intent_category.putExtra("title", getString(R.string.vancloud_select_functional_categories));
                intent_category.putExtra("style", "company");
                intent_category.putExtra("id", mIdMap.get(REQUEST_SORT_ACTION));
                intent_category.putExtra("type", DictSelectActivity.DICT_TITLE);
                intent_category.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_FUNCTIONS));
                startActivityForResult(intent_category, REQUEST_SORT_ACTION);
                break;
            case R.id.type_layout://工作性质：全职，兼职...
                Intent intent_type = new Intent(this, DictSelectActivity.class);
                intent_type.putExtra("title", getString(R.string.vancloud_select_working_properties));
                intent_type.putExtra("style", "company");
                intent_type.putExtra("id", mIdMap.get(103));
                intent_type.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_TERM));
                startActivityForResult(intent_type, 103);
                break;
            case R.id.education_layout://学历要求
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", getString(R.string.vancloud_select_education));
                intent.putExtra("style", "company");
                intent.putExtra("id", mIdMap.get(100));
                intent.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_DEGREE));
                startActivityForResult(intent, 100);
                break;
            case R.id.work_experience_layout://工作经验
                Intent intent_worktime = new Intent(this, DictSelectActivity.class);
                intent_worktime.putExtra("style", "company");
                intent_worktime.putExtra("title", getString(R.string.vancloud_select_work_experience1));
                intent_worktime.putExtra("id", mIdMap.get(101));
                intent_worktime.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_WORK_YEAR));
                startActivityForResult(intent_worktime, 101);
                break;
            case R.id.work_city_layout://工作地区
                Intent intent_workcity = new Intent(this, AreaSelectActivity.class);
                intent_workcity.putExtra("style", "company");
                intent_workcity.putExtra("id", mIdMap.get(REQUEST_WORKCITY_ACTION));
                startActivityForResult(intent_workcity, REQUEST_WORKCITY_ACTION);
                break;
            case R.id.salary_layout://薪水范围
                Intent intent_salary = new Intent(this, DictSelectActivity.class);
                intent_salary.putExtra("title", getString(R.string.vancloud_select_monthly_salary));
                intent_salary.putExtra("style", "company");
                intent_salary.putExtra("id", mIdMap.get(6));
                intent_salary.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_SALARY));
                startActivityForResult(intent_salary, 6);
//
//                Intent intent_salary = new Intent(this, DataProviderActivity.class);
//                intent_salary.putExtra("style","company");
//                intent_salary.putExtra("id",mIdMap.get(102));
//                intent_salary.putExtra("type", DataProviderActivity.XINZIFANWEI);
//                startActivityForResult(intent_salary, 102);
                break;
            case R.id.welfare_layout://福利待遇
                Intent welIntent = new Intent(this, WelfareTreatmentActivity.class);
                welIntent.putExtra("ids", welfare_ids);
                startActivityForResult(welIntent, REQUEST_WELFARE_ACTION);
                break;
            case R.id.language_one_layout://语言1
                Intent intent_language_one = new Intent(this, DictSelectActivity.class);
                intent_language_one.putExtra("title", getString(R.string.personal_add_language_type));
                intent_language_one.putExtra("id", mIdMap.get(203));
                intent_language_one.putExtra("style", "company");
                intent_language_one.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_LANGUAGE));
                startActivityForResult(intent_language_one, 203);
                break;
            case R.id.language_one_mastery_degree_layout://语言1掌握程度
                Intent intent_mastery_degree_one = new Intent(this, DictSelectActivity.class);
                intent_mastery_degree_one.putExtra("title", getString(R.string.personal_add_language_degree));
                intent_mastery_degree_one.putExtra("id", mIdMap.get(205));
                intent_mastery_degree_one.putExtra("style", "company");
                intent_mastery_degree_one.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_LANGUAGE_LEVEL));
                startActivityForResult(intent_mastery_degree_one, 205);
                break;
            case R.id.language_two_layout://语言2
                Intent intent_language_two = new Intent(this, DictSelectActivity.class);
                intent_language_two.putExtra("title", getString(R.string.personal_add_language_type));
                intent_language_two.putExtra("id", mIdMap.get(204));
                intent_language_two.putExtra("style", "company");
                intent_language_two.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_LANGUAGE));
                startActivityForResult(intent_language_two, 204);
                break;
            case R.id.language_two_mastery_degree_layout://语言2掌握程度
                Intent intent_mastery_degree_two = new Intent(this, DictSelectActivity.class);
                intent_mastery_degree_two.putExtra("title", getString(R.string.personal_add_language_degree));
                intent_mastery_degree_two.putExtra("id", mIdMap.get(206));
                intent_mastery_degree_two.putExtra("style", "company");
                intent_mastery_degree_two.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_LANGUAGE_LEVEL));
                startActivityForResult(intent_mastery_degree_two, 206);
                break;
            case R.id.zhuanye_one_layout://专业1
                Intent intent_zhuanye_one = new Intent(this, DictSelectActivity.class);
                intent_zhuanye_one.putExtra("title", getString(R.string.personal_choose_zhuanye));
                intent_zhuanye_one.putExtra("id", mIdMap.get(201));
                intent_zhuanye_one.putExtra("type", DictSelectActivity.DICT_TITLE);
                intent_zhuanye_one.putExtra("style", "company");
                intent_zhuanye_one.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_MAJORS));
                startActivityForResult(intent_zhuanye_one, 201);
                break;
            case R.id.zhuanye_two_layout://专业2
                Intent intent_zhuanye_two = new Intent(this, DictSelectActivity.class);
                intent_zhuanye_two.putExtra("title", getString(R.string.personal_choose_zhuanye));
                intent_zhuanye_two.putExtra("id", mIdMap.get(202));
                intent_zhuanye_two.putExtra("type", DictSelectActivity.DICT_TITLE);
                intent_zhuanye_two.putExtra("style", "company");
                intent_zhuanye_two.setData(Uri.parse(URLAddr.URL_DICT_VANCLOUD_MAJORS));
                startActivityForResult(intent_zhuanye_two, 202);
                break;
            case R.id.btn_start_time:
                DataUtils.showDateSelect(this, mStartCompleteTimeTv, DataUtils.DATE_TYPE_AFTER_TODAY, null);
                break;
            case R.id.btn_end_time:
                DataUtils.showDateSelect(this, mEndCompleteTimeTv, DataUtils.DATE_TYPE_AFTER_TODAY, null);
                break;
            case R.id.ll_cc_people: {
                Intent intent_cc = new Intent(this, OrganizationSelectedActivity.class);

                if (mCCPeopleAdapter != null) {
                    intent_cc.putParcelableArrayListExtra("select", mCCPeopleAdapter.getList());
                }
                ArrayList<Node> unSelectList = new ArrayList<>();

                if (mApprovalAdapter != null) {
                    unSelectList.addAll(mApprovalAdapter.getList());
                }
                intent_cc.putParcelableArrayListExtra("unselect", unSelectList);

                intent_cc.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_MULTI);
                startActivityForResult(intent_cc, REQUEST_CODE_CCPEOPLE);
            }
            break;

            case R.id.ll_approval: {
                Intent intent_ll = new Intent(this, OrganizationSelectedActivity.class);
                if (mApprovalAdapter != null) {
                    intent_ll.putParcelableArrayListExtra("select", mApprovalAdapter.getList());
                }
                ArrayList<Node> unSelectList = new ArrayList<>();

                if (mCCPeopleAdapter != null) {
                    unSelectList.addAll(mCCPeopleAdapter.getList());
                }
                intent_ll.putParcelableArrayListExtra("unselect", unSelectList);
                intent_ll.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_SINGLE);
                startActivityForResult(intent_ll, REQUEST_CODE_APPROVE);
            }
            break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void hideInputMethod() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etJobName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etWorkCityAddress.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etChooseModel.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etDemand.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(mNumEt.getWindowToken(), 0);
        }
    }

    private boolean isEmpty() {
        String jobName = etJobName.getText().toString();
        if (!TextUtils.isEmpty(jobName)) {
            return false;
        }
        String workCity = etWorkCityAddress.getText().toString();
        if (!TextUtil.isEmpty(workCity)) {
            return false;
        }
        String chooseModel = etChooseModel.getText().toString();
        if (!TextUtil.isEmpty(chooseModel)) {
            return false;
        }
        String demand = etDemand.getText().toString();
        if (!TextUtil.isEmpty(demand)) {
            return false;
        }
        if (!TextUtils.isEmpty(tvDepart.getText().toString()) || !TextUtils.isEmpty(tvReport.getText().toString())
                || !TextUtils.isEmpty(tvCity.getText().toString()) || !TextUtils.isEmpty(tvCategory.getText().toString())
                || !TextUtils.isEmpty(tvType.getText().toString()) || !TextUtils.isEmpty(tvEducation.getText().toString())
                || !TextUtils.isEmpty(tvWorkExperience.getText().toString())
                || !TextUtils.isEmpty(tvSalary.getText().toString())
                || !TextUtils.isEmpty(tvLanguageOne.getText().toString()) || !TextUtils.isEmpty(tvLanguageOneMasteryDegree.getText().toString())
                || !TextUtils.isEmpty(tvLanguageTwo.getText().toString()) || !TextUtils.isEmpty(tvLanguageTwoMasteryDegree.getText().toString())
                || !TextUtils.isEmpty(tvZhuanyeOne.getText().toString()) || !TextUtils.isEmpty(tvZhuanyeTwo.getText().toString())) {
            return false;
        }
        String num = mNumEt.getText().toString();
        String startTime = mStartCompleteTimeTv.getText().toString();
        String endTime = mEndCompleteTimeTv.getText().toString();
        if (!TextUtil.isEmpty(num)) {
            return false;
        }

//        if (!TextUtil.isEmpty(startTime)) {
//            return false;
//        }
//        if (!TextUtil.isEmpty(endTime)) {
//            return false;
//        }


//        ArrayList<Node> cCPeopleList = null;
//        if (mCCPeopleAdapter != null) {
//            cCPeopleList = mCCPeopleAdapter.getList();
//        }
//
//        ArrayList<Node> approvalList = null;
//        if (mApprovalAdapter != null) {
//            approvalList = mApprovalAdapter.getList();
//        }
//
//        if (cCPeopleList != null || cCPeopleList.size() != 0) {
//            return false;
//        }
//        if (approvalList != null || approvalList.size() != 0) {
//            return false;
//        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitEdit();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void showExitEdit() {
        if (isEmpty()) {
            finish();
            return;
        }
        new AlertDialog(this).builder() .setTitle(getString(R.string.frends_tip))
                .setMsg(getString(R.string.vancloud_flowplan_prompt))
                .setPositiveButton(getString(R.string.vancloud_flowplan_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }
                ).setNegativeButton(getString(R.string.vancloud_flowplan_back), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }).show();
    }

    private void loadPositionInfo() {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GET_COMPANY_POSITION), params, this);
        mNetworkManager.load(CALL_BACK_REPORT, path, this);
    }

    private String welfare_ids;
    private Map<Integer, String> mIdMap = new HashMap<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_DEPART_ACTION://目标部门
                Node node1 = data.getParcelableExtra("node");
                tvDepart.setText(node1.getName());
                break;
            case REQUEST_CITY_ACTION://职位发布地点
                String name = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvCity.setText(name);
                break;
            case REQUEST_SORT_ACTION://职能类别
                String name1 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvCategory.setText(name1);
                break;

            case 100: //学历要求
                String name2 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvEducation.setText(name2);
                break;

            case 101: //工作经验
                String name3 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvWorkExperience.setText(name3);
                break;

            case 6: //薪水范围
                String name4 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvSalary.setText(name4);
                break;

            case 103: //工作性质
                String name5 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvType.setText(name5);
                break;

            case REQUEST_WORKCITY_ACTION://工作地区
                String name6 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvWorkCity.setText(name6);
                break;

            case REQUEST_WELFARE_ACTION://福利待遇
                String ids = data.getStringExtra("welfare_ids");
                String names = data.getStringExtra("welfare_names");
                welfare_ids = ids;
                tvWelfare.setText(names);
                break;

            case 201:
                mIdMap.put(requestCode, data.getStringExtra("id"));
                String name7 = data.getStringExtra("name");
                tvZhuanyeOne.setText(name7);
                break;
            case 202:
                mIdMap.put(requestCode, data.getStringExtra("id"));
                String name8 = data.getStringExtra("name");
                tvZhuanyeTwo.setText(name8);
                break;
            case 203:
                String name9 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvLanguageOne.setText(name9);
                break;
            case 204:
                String name10 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvLanguageTwo.setText(name10);
                break;
            case 205:
                String name11 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvLanguageOneMasteryDegree.setText(name11);
                break;
            case 206:
                String name12 = data.getStringExtra("name");
                mIdMap.put(requestCode, data.getStringExtra("id"));
                tvLanguageTwoMasteryDegree.setText(name12);
                break;
            case 210:// TODO 职位模板
                JobModule jobModule = (JobModule) data.getSerializableExtra("jobModule");
                tvJobModule.setText(jobModule.job_template_name);
                setViewData(jobModule);
                break;

            case REQUEST_CODE_CCPEOPLE://抄送人
                if (resultCode == RESULT_OK) {
                    cCPeopleList = data.getParcelableArrayListExtra("select");
                    setCcPeopleGridView(cCPeopleList);
                }
                break;
            case REQUEST_CODE_APPROVE://审批人
                if (resultCode == RESULT_OK) {
                    ArrayList<Node> approvalList = data.getParcelableArrayListExtra("select");
                    setApprovalGridView(approvalList);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    UserGridAdapter mCCPeopleAdapter;

    private void setCcPeopleGridView(ArrayList<Node> nodes) {
        if (mCCPeopleAdapter == null) {
            mCCPeopleAdapter = new UserGridAdapter(this,
                    nodes, mCcPeopleCountTv);
            mCcPeopleGridView.setAdapter(mCCPeopleAdapter);
        } else {
            mCCPeopleAdapter.setList(nodes, nodes);
            mCCPeopleAdapter.notifyDataSetChanged();
        }
    }

    UserGridAdapter mApprovalAdapter;

    private void setApprovalGridView(ArrayList<Node> nodes) {
        if (mApprovalAdapter == null) {
            mApprovalAdapter = new UserGridAdapter(this,
                    nodes, mApprovalCountTv);
            mApprovalGridView.setAdapter(mApprovalAdapter);
        } else {
            mApprovalAdapter.setList(nodes, nodes);
        }
    }

    //完成招聘
    private void completeAction() {
        showLoadingDialog(this, "");
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("job_id", recruit_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_JOB_FINISH_JOB), params, this);
        mNetworkManager.load(CALL_BACK_FINISH, path, this);
    }

    //创建职位提交
    private void submit() {

        String jobName = etJobName.getText().toString();//职位名称
        String address = etWorkCityAddress.getText().toString();//上班地址
        String desc = etChooseModel.getText().toString();//岗位职责
        String demand = etDemand.getText().toString();//任职要求
        String num = mNumEt.getText().toString();
        String startTime = mStartCompleteTimeTv.getText().toString();
        String endTime = mEndCompleteTimeTv.getText().toString();

        if (TextUtils.isEmpty(jobName)) {
            Toast.makeText(this, etJobName.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        if ("2".equals(type)) {
            if (TextUtils.isEmpty(tvDepart.getText().toString())) {
                Toast.makeText(this, getString(R.string.tip_item_select, getString(R.string.job_depart)), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(tvReport.getText().toString())) {
                Toast.makeText(this, getString(R.string.tip_item_select, getString(R.string.job_leader)), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (TextUtils.isEmpty(num)) {
            showToast(R.string.toast_please_input_employ_num);
            return;
        }
        if (TextUtils.isEmpty(tvCategory.getText().toString())) {
            Toast.makeText(this, getString(R.string.tip_item_select, getString(R.string.job_category)), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvCity.getText().toString())) {
            Toast.makeText(this, getString(R.string.recruit_detail_hint_city), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, etWorkCityAddress.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvType.getText().toString())) {
            Toast.makeText(this, getString(R.string.tip_item_select, getString(R.string.job_type)), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvEducation.getText().toString())) {
            Toast.makeText(this, getString(R.string.recruit_detail_hint_education), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvWorkExperience.getText().toString())) {
            Toast.makeText(this, getString(R.string.recruit_detail_hint_job_year), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvSalary.getText().toString())) {
            Toast.makeText(this, getString(R.string.recruit_detail_money_hint_pay), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtil.isEmpty(desc)) {
            Toast.makeText(this, getString(R.string.vancloud_jobresponsibilities_prompt1), Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (desc.length() < 20) {
                Toast.makeText(this, getString(R.string.vancloud_jobresponsibilities_prompt2), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (TextUtil.isEmpty(demand)) {
            Toast.makeText(this, getString(R.string.toast_please_input_job_requirements), Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (demand.length() < 20) {
                Toast.makeText(this, getString(R.string.toast_please_input_job_requirements1), Toast.LENGTH_SHORT).show();
                return;
            }
        }


//        if (TextUtils.isEmpty(tvWorkCity.getText().toString())) {
//            Toast.makeText(this, getString(R.string.tip_item_select, getString(R.string.job_work_location)), Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (TextUtils.isEmpty(tvWelfare.getText().toString())) {
//            Toast.makeText(this, getString(R.string.tip_item_select, getString(R.string.recruit_detail_fuli)), Toast.LENGTH_SHORT).show();
//            return;
//        }
        if ("2".equals(type)) {
//            if (mCCPeopleAdapter != null) {
//                cCPeopleList = mCCPeopleAdapter.getList();
//            }
            if (mApprovalAdapter != null) {
                approvalList = mApprovalAdapter.getList();
            }
//            if (cCPeopleList == null || cCPeopleList.size() == 0) {
//                showToast(R.string.toast_please_input_ccpeople);
//                return;
//            }
            if (approvalList == null || approvalList.size() == 0) {
                showToast(R.string.toast_please_input_approve);
                return;
            }
        }
//        if (TextUtils.isEmpty(startTime)) {
//            showToast(R.string.toast_please_input_complete_starttime);
//            return;
//        }
//        if (TextUtils.isEmpty(endTime)) {
//            showToast(R.string.toast_please_input_complete_endtime);
//            return;
//        }

        netSubmit(num, startTime, endTime, cCPeopleList, approvalList, isEmploy);
    }

    private void netSubmit(String num, String start, String end, ArrayList<Node> cCPeopleList, ArrayList<Node> approvalList,
                           boolean isEmploy) {
        showLoadingDialog(this, "");
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        if ("2".equals(type)) { //从流程创建
            Map<String, String> params = new HashMap<>();
            params.put("tenantid", PrfUtils.getTenantId(this));
            params.put("ownid", PrfUtils.getUserId(this));
            params.put("processtype", "5");//招聘计划

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("job_name", etJobName.getText().toString());//职位名称
                jsonObject.put("department", tvDepart.getText().toString());//目标部门名称
                jsonObject.put("report_to_user", tvReport.getText().toString());//汇报对象名称
                jsonObject.put("job_num", mNumEt.getText().toString());//计划招聘人数
                jsonObject.put("func_type", tvCategory.getText().toString());//职能类别名称
                jsonObject.put("func_type_code", mIdMap.get(REQUEST_SORT_ACTION));//职能类别编码
                jsonObject.put("job_area", tvCity.getText().toString());//职位发布地点名称
                jsonObject.put("job_area_code", mIdMap.get(REQUEST_CITY_ACTION));//职位发布地点编码
                jsonObject.put("work_address", etWorkCityAddress.getText().toString());//工作地点
                jsonObject.put("term", tvType.getText().toString());//工作性质名称
                jsonObject.put("term_code", mIdMap.get(103));//工作性质编码
                jsonObject.put("degree_from", tvEducation.getText().toString());//学历要求名称
                jsonObject.put("degree_from_code", mIdMap.get(100));//学历要求编码
                jsonObject.put("work_year", tvWorkExperience.getText().toString());//工作经验名称
                jsonObject.put("work_year_code", mIdMap.get(101));//工作经验编码
                jsonObject.put("salray_range", tvSalary.getText().toString());//薪水范围名称
                jsonObject.put("salray_range_code", mIdMap.get(6));//薪水范围编码
//                jsonObject.put("work_area", tvWorkCity.getText().toString());//工作地区名称
                jsonObject.put("job_desc", etChooseModel.getText().toString()); //岗位描述
                jsonObject.put("job_requirement", etDemand.getText().toString());//任职要求
                jsonObject.put("job_welfare", tvWelfare.getText().toString());//福利待遇名称 （多个逗号分隔）
                jsonObject.put("job_welfare_code", mIdMap.get(REQUEST_WELFARE_ACTION));//福利待遇编码 （多个逗号分隔）
                jsonObject.put("language", tvLanguageOne.getText().toString());//语言1
                jsonObject.put("language_code", mIdMap.get(203));//语言1编码
                jsonObject.put("language_level", tvLanguageOneMasteryDegree.getText().toString());//语言1掌握程度
                jsonObject.put("language_level_code", mIdMap.get(205));//语言1掌握程度编码
                jsonObject.put("language2", tvLanguageTwo.getText().toString());//语言2
                jsonObject.put("language2_code", mIdMap.get(204));//语言2编码
                jsonObject.put("language_level2", tvLanguageTwoMasteryDegree.getText().toString());//语言2掌握程度
                jsonObject.put("language_level2_code", mIdMap.get(206));//语言2掌握程度编码
                jsonObject.put("major1", tvZhuanyeOne.getText().toString());//专业1
                jsonObject.put("major1_code", mIdMap.get(201));//专业1编码
                jsonObject.put("major2", tvZhuanyeTwo.getText().toString());//专业2
                jsonObject.put("major1_code", mIdMap.get(202));//专业2编码
                //jsonObject.put("job_start_date", "" + Utils.dateFormat(start));//开始时间
                //jsonObject.put("job_end_date", "" + Utils.dateFormat(end));//结束时间
                String employ = isEmploy ? "Y" : "N";
                jsonObject.put("is_delegate", employ);//是否委托万客招聘 Y 是 N否
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsonObject != null) {
                params.put("jobinfo", jsonObject.toString());
            }
            String ccuser = TextUtil.splitJoint(cCPeopleList, "id");
            if (!TextUtils.isEmpty(ccuser))
                params.put("receiverids", ccuser);//抄送人
            String approve = TextUtil.splitJoint(approvalList, "id");
            if (!TextUtils.isEmpty(approve))
                params.put("processerid", approve);//审批人
            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_PROCESS_CREATE), params, this);
            mNetworkManager.load(CALL_BACK_SUBMIT, path, this);
        } else {
            Map<String, String> params = new HashMap<>();
            params.put("tenant_id", PrfUtils.getTenantId(this));
            params.put("user_id", PrfUtils.getUserId(this));

            params.put("job_name", etJobName.getText().toString());//职位名称
            params.put("job_area", tvCity.getText().toString());//职位发布地点
            params.put("func_type", tvCategory.getText().toString());//职能类别
            params.put("term", tvType.getText().toString());//职能类型
            params.put("degree_from", tvEducation.getText().toString());//学历要求
            params.put("work_year", tvWorkExperience.getText().toString());//工作经验
            params.put("work_area", tvWorkCity.getText().toString());//工作城市
            params.put("work_address", etWorkCityAddress.getText().toString());//工作地点
            params.put("salray_range", tvSalary.getText().toString());//薪水范围
            params.put("job_desc", etChooseModel.getText().toString());//岗位描述
            params.put("job_requirement", etDemand.getText().toString());//任职要求
            params.put("job_welfare", tvWelfare.getText().toString());//福利待遇

            if (!TextUtils.isEmpty(recruit_id)) {
                params.put("job_id", recruit_id);
            }
            params.put("job_num", num);//计划招聘人数
            params.put("job_start_date", "" + Utils.dateFormat(start));//计划开始时间
            params.put("job_end_date", "" + Utils.dateFormat(end));//计划结束时间
            String employ = isEmploy ? "Y" : "N";
            params.put("is_delegate", employ);//是否委托万客 是 Y 否N
            if (TextUtils.isEmpty(recruit_id)) { // 创建
                NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_JOB_CREATE_JOB), params, this);
                mNetworkManager.load(CALL_BACK_SUBMIT, path, this);
            } else { // 修改
                NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_JOB_UPDATE_JOB), params, this);
                mNetworkManager.load(CALL_BACK_SUBMIT, path, this);
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
            case CALL_BACK_REPORT:
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    mPrositionList = JsonDataFactory.getDataArray(Position.class, jsonObject.getJSONArray("positions"));
                    showPositionSelected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALL_BACK_SUBMIT:
                try {
                    String data = rootData.getJson().getString("data");
                    if ("uncomplete_tenant_info".equals(data)) {
                        showCompanyinfoTip();
                    } else {
                        setResult(RESULT_OK);
                        sendBroadcast(new Intent(FlowReportNewActivity.FLOW_REFRESH));
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALL_BACK_FINISH:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    //租户信息不全提示
    private void showCompanyinfoTip() {
        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.updata_company_info));
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setLeft();
        alertDialog.setPositiveButton(getString(R.string.updata_company_info_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(JobCreateActivity.this, CompanyInfoEditActivity.class));
            }
        });
        alertDialog.setNegativeButton(getString(R.string.updata_company_info_cancle), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        alertDialog.show();
    }

    private void showPositionSelected() {
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
                            tvReport.setText(position.value);
                        }
                    });
        }
        actionSheetDialog.show();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    public void setViewData(JobModule jobModule) {
        //职位名称
        etJobName.setText(jobModule.job_name);
        //招聘人数
        mNumEt.setText(jobModule.job_num);
        //职能类别
        tvCategory.setText(jobModule.func_type);
        mIdMap.put(REQUEST_SORT_ACTION, jobModule.func_type_code);
        //发布城市
        tvCity.setText(jobModule.job_area);
        mIdMap.put(REQUEST_CITY_ACTION, jobModule.job_area_code);
        //上班地址
        etWorkCityAddress.setText(jobModule.work_address);
        //工作性质
        tvType.setText(jobModule.term);
        mIdMap.put(103, jobModule.term_code);
        //最低学历
        tvEducation.setText(jobModule.degree_from);
        mIdMap.put(100, jobModule.degree_from_code);
        //职位月薪
        tvSalary.setText(jobModule.salray_range);
        mIdMap.put(6, jobModule.salray_range_code);
        //岗位职责
        etChooseModel.setText(jobModule.job_desc);
        //任职要求
        etDemand.setText(jobModule.job_requirement);
        //福利待遇
        tvWelfare.setText(jobModule.job_welfare);
        welfare_ids = jobModule.job_welfare_code;
        //语言1
        tvLanguageOne.setText(jobModule.language);
        mIdMap.put(203, jobModule.language_code);
        //掌握程度
        tvLanguageOneMasteryDegree.setText(jobModule.language_level);
        mIdMap.put(205, jobModule.language_level_code);
        //语言2
        tvLanguageTwo.setText(jobModule.language2);
        mIdMap.put(204, jobModule.language2_code);
        //掌握程度
        tvLanguageTwoMasteryDegree.setText(jobModule.language_level2);
        mIdMap.put(206, jobModule.language_level2_code);
        //专业1
        tvZhuanyeOne.setText(jobModule.major1);
        mIdMap.put(201, jobModule.major1_code);
        //专业2
        tvZhuanyeTwo.setText(jobModule.major2);
        mIdMap.put(202, jobModule.major2_code);
        //是否委托万客
        if ("Y".equals(jobModule.is_delegate)) {
            mEmployCb.setBackgroundResource(R.drawable.check_true);
            isEmploy = true;
        } else {
            mEmployCb.setBackgroundResource(R.drawable.check_false);
            isEmploy = false;
        }
    }
}
