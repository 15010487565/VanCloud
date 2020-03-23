package com.vgtech.vancloud.ui.module.recruit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RecruitmentDetails;
import com.vgtech.common.api.RecruitmentInfoBean;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.progressbar.ProgressWheel;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by code on 2015/12/23.
 * 查看招聘详情
 */
public class RecruitmentDetailsActivity extends BaseActivity implements HttpListener<String> {

    @InjectView(R.id.job_tv)
    TextView jobTv;
    @InjectView(R.id.department_tv)
    TextView departmentTv;
    @InjectView(R.id.report_tv)
    TextView reportTv;
    @InjectView(R.id.recruit_city_tv)
    TextView recruitCityTv;
    @InjectView(R.id.job_sort_tv)
    TextView jobSortTv;
    @InjectView(R.id.job_type_tv)
    TextView jobTypeTv;
    @InjectView(R.id.xueli_tv)
    TextView xueliTv;
    @InjectView(R.id.job_num_tv)
    TextView jobNumTv;
    //    @InjectView(R.id.job_city_tv)
//    TextView jobCityTv;
    @InjectView(R.id.job_city_address_tv)
    TextView jobCityAddressTv;
    @InjectView(R.id.pay_tv)
    TextView payTv;
    @InjectView(R.id.job_descriptions_tv)
    TextView jobDescriptionsTv;
    @InjectView(R.id.office_tv)
    TextView officeTv;
    @InjectView(R.id.fuli_tv)
    TextView fuliTv;
    @InjectView(R.id.plan_recruit_num_tv)
    TextView planRecruitNumTv;
    @InjectView(R.id.start_time_tv)
    TextView startTv;
    @InjectView(R.id.finish_time_tv)
    TextView finishTimeTv;
    @InjectView(R.id.is_delegate_tv)
    TextView isdelegateTv;
    @InjectView(R.id.create_user_tv)
    TextView createTv;
    @InjectView(R.id.copy_person_tv)
    TextView copyPersonTv;
    @InjectView(R.id.progress_view)
    ProgressWheel progressView;
    @InjectView(R.id.loadding_msg)
    TextView loaddingMsg;
    @InjectView(R.id.loading)
    LinearLayout loading;
    @InjectView(R.id.content_layout)
    LinearLayout contentLayout;

    @InjectView(R.id.is_delegate_layout)
    LinearLayout isDelegateLayout;
    @InjectView(R.id.is_delegate_view)
    View isDelegateView;

    @InjectView(R.id.processer_user_tv)
    TextView processerView;
    @InjectView(R.id.tv_language_01)
    TextView tvLanguage01;
    @InjectView(R.id.language_01_layout)
    LinearLayout language01Layout;
    @InjectView(R.id.tv_language_01_mastery_degree)
    TextView tvLanguage01MasteryDegree;
    @InjectView(R.id.language_01_mastery_degree_layout)
    LinearLayout language01MasteryDegreeLayout;
    @InjectView(R.id.tv_language_02)
    TextView tvLanguage02;
    @InjectView(R.id.language_02_layout)
    LinearLayout language02Layout;
    @InjectView(R.id.tv_language_02_mastery_degree)
    TextView tvLanguage02MasteryDegree;
    @InjectView(R.id.language_02_mastery_degree_layout)
    LinearLayout language02MasteryDegreeLayout;
    @InjectView(R.id.tv_zhuanye_01)
    TextView tvZhuanye01;
    @InjectView(R.id.zhuanye_01_layout)
    LinearLayout zhuanye01Layout;
    @InjectView(R.id.tv_zhuanye_02)
    TextView tvZhuanye02;
    @InjectView(R.id.zhuanye_02_layout)
    LinearLayout zhuanye02Layout;

    private NetworkManager mNetworkManager;
    private String processid;
    private RecruitmentInfoBean recruitmentInfoBean;
    private static final int CALLBACK_DETAIL = 1;
    private boolean texttype = false;

    private ImageView resumeRecruitingView;

    @Override
    protected int getContentView() {
        return R.layout.activity_recruitment_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        setTitle(getString(R.string.recruit_info));
        Intent intent = getIntent();
        processid = intent.getExtras().getString("processid");
        recruitmentInfoBean = (RecruitmentInfoBean) intent.getSerializableExtra("recruitInfo");
        copyPersonTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyPersonTv.setSingleLine(texttype);
                if (texttype) {
                    texttype = false;
                } else {
                    texttype = true;
                }
            }
        });
        resumeRecruitingView = (ImageView) findViewById(R.id.iv_resume_recruiting);
        if (PrfUtils.isChineseForAppLanguage(this))
            resumeRecruitingView.setImageResource(R.mipmap.resume_recruiting_ch);
        else
            resumeRecruitingView.setImageResource(R.mipmap.resume_recruiting_en);

        if (recruitmentInfoBean != null) {
            findViewById(R.id.depart_layout).setVisibility(View.GONE);
            findViewById(R.id.report_layout).setVisibility(View.GONE);
            findViewById(R.id.dep_view).setVisibility(View.GONE);
            findViewById(R.id.report_view).setVisibility(View.GONE);
            findViewById(R.id.is_delegate_layout).setVisibility(View.GONE);
            findViewById(R.id.create_user_layout).setVisibility(View.GONE);
            findViewById(R.id.processer_user_layout).setVisibility(View.GONE);
            findViewById(R.id.copy_user_layout).setVisibility(View.GONE);

            String job = recruitmentInfoBean.job_name;
            String planRecruitNum = recruitmentInfoBean.job_num;
            String recruitCity = recruitmentInfoBean.job_area;
            String jobSort = recruitmentInfoBean.func_type;
            String jobType = recruitmentInfoBean.term;
            String xueli = recruitmentInfoBean.degree_from;
            String jobNum = recruitmentInfoBean.work_year;
            String jobCityaddress = recruitmentInfoBean.work_address;
            String pay = recruitmentInfoBean.salray_range;
            String jobDescriptions = recruitmentInfoBean.job_desc;
            String office = recruitmentInfoBean.job_requirement;
            String fuli = recruitmentInfoBean.job_welfare;
            String is_delegate = recruitmentInfoBean.is_delegate;
            String language = recruitmentInfoBean.language;
            String language_level = recruitmentInfoBean.language_level;
            String language2 = recruitmentInfoBean.language2;
            String language_level2 = recruitmentInfoBean.language_level2;
            String major1 = recruitmentInfoBean.major1;
            String major2 = recruitmentInfoBean.major2;

            if (!TextUtils.isEmpty(job)) {
                jobTv.setText(job);
            }
            if (!TextUtils.isEmpty(recruitCity)) {
                recruitCityTv.setText(recruitCity);
            }
            if (!TextUtils.isEmpty(jobSort)) {
                jobSortTv.setText(jobSort);
            }
            if (!TextUtils.isEmpty(jobType)) {
                jobTypeTv.setText(jobType);
            }
            if (!TextUtils.isEmpty(xueli)) {
                xueliTv.setText(xueli);
            }
            if (!TextUtils.isEmpty(jobNum)) {
                jobNumTv.setText(jobNum);
            }
            if (!TextUtils.isEmpty(jobCityaddress)) {
                jobCityAddressTv.setText(jobCityaddress);
            }
            if (!TextUtils.isEmpty(pay)) {
                payTv.setText(pay);
            }
            if (!TextUtils.isEmpty(jobDescriptions)) {
                jobDescriptionsTv.setText(jobDescriptions);
            }
            if (!TextUtils.isEmpty(office)) {
                officeTv.setText(office);
            }
            if (!TextUtils.isEmpty(fuli)) {
                fuliTv.setText(fuli);
            }
            if (!TextUtils.isEmpty(planRecruitNum)) {
                planRecruitNumTv.setText(planRecruitNum);
            }
            if (!TextUtils.isEmpty(is_delegate)) {
                if ("Y".equals(is_delegate)) {
                    resumeRecruitingView.setVisibility(View.VISIBLE);
                    isdelegateTv.setText(getString(R.string.yes));
                } else {
                    isdelegateTv.setText(getString(R.string.no));
                }
            }
            isDelegateLayout.setVisibility(View.GONE);
            isDelegateView.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(language)) {
                tvLanguage01.setText(language);
            }
            if (!TextUtils.isEmpty(language_level)) {
                tvLanguage01MasteryDegree.setText(language_level);
            }
            if (!TextUtils.isEmpty(language2)) {
                tvLanguage02.setText(language2);
            }
            if (!TextUtils.isEmpty(language_level2)) {
                tvLanguage02MasteryDegree.setText(language_level2);
            }
            if (!TextUtils.isEmpty(major1)) {
                tvZhuanye01.setText(major1);
            }
            if (!TextUtils.isEmpty(major2)) {
                tvZhuanye02.setText(major2);
            }

        } else {
            findViewById(R.id.depart_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.report_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.is_delegate_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.create_user_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.processer_user_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.copy_user_layout).setVisibility(View.VISIBLE);

            loadingDes();
        }
    }

    public void loadingDes() {
        showLoadingView();
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("processid", processid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_PROCESS_JOB_PREVIEW), params, this);
        mNetworkManager.load(CALLBACK_DETAIL, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        hideLoadingView();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {

            case CALLBACK_DETAIL:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    JSONObject jobInfo = resutObject.getJSONObject("job_info");
                    RecruitmentDetails data = JsonDataFactory.getData(RecruitmentDetails.class, new JSONObject(jobInfo.toString()));

                    String job = jobInfo.getString("job_name");
                    String department = jobInfo.getString("department");
                    String report = jobInfo.getString("report_to_user");
                    String recruitCity = jobInfo.getString("job_area");
                    String jobSort = jobInfo.getString("func_type");
                    String jobType = jobInfo.getString("term");
                    String xueli = jobInfo.getString("degree_from");
                    String jobNum = jobInfo.getString("work_year");
//                    String jobCity = jobInfo.getString("work_area");
                    String jobCityaddress = jobInfo.getString("work_address");
                    String pay = jobInfo.getString("salray_range");
                    String jobDescriptions = jobInfo.getString("job_desc");
                    String office = jobInfo.getString("job_requirement");
                    String fuli = jobInfo.getString("job_welfare");
                    String planRecruitNum = jobInfo.getString("job_num");
                    String startTime = jobInfo.getString("job_start_date");
                    String endTime = jobInfo.getString("job_end_date");
                    String is_delegate = jobInfo.getString("is_delegate");
                    String create_user = jobInfo.getString("create_user");
                    String processer_user = jobInfo.getString("processer_user");
                    String cc_user = jobInfo.getString("cc_user");

                    String language = jobInfo.getString("language");
                    String language_level = jobInfo.getString("language_level");
                    String language2 = jobInfo.getString("language2");
                    String language_level2 = jobInfo.getString("language_level2");
                    String major1 = jobInfo.getString("major1");
                    String major2 = jobInfo.getString("major2");

                    if (!TextUtils.isEmpty(job)) {
                        jobTv.setText(job);
                    }
                    if (!TextUtils.isEmpty(department)) {
                        departmentTv.setText(department);
                    }
                    if (!TextUtils.isEmpty(report)) {
                        reportTv.setText(report);
                    }
                    if (!TextUtils.isEmpty(recruitCity)) {
                        recruitCityTv.setText(recruitCity);
                    }
                    if (!TextUtils.isEmpty(jobSort)) {
                        jobSortTv.setText(jobSort);
                    }
                    if (!TextUtils.isEmpty(jobType)) {
                        jobTypeTv.setText(jobType);
                    }
                    if (!TextUtils.isEmpty(xueli)) {
                        xueliTv.setText(xueli);
                    }
                    if (!TextUtils.isEmpty(jobNum)) {
                        jobNumTv.setText(jobNum);
                    }
//                    if (!TextUtils.isEmpty(jobCity)) {
//                        jobCityTv.setDettailText(jobCity);
//                    }
                    if (!TextUtils.isEmpty(jobCityaddress)) {
                        jobCityAddressTv.setText(jobCityaddress);
                    }
                    if (!TextUtils.isEmpty(pay)) {
                        payTv.setText(pay);
                    }
                    if (!TextUtils.isEmpty(jobDescriptions)) {
                        jobDescriptionsTv.setText(jobDescriptions);
                    }
                    if (!TextUtils.isEmpty(office)) {
                        officeTv.setText(office);
                    }
                    if (!TextUtils.isEmpty(fuli)) {
                        fuliTv.setText(fuli);
                    }
                    if (!TextUtils.isEmpty(planRecruitNum)) {
                        planRecruitNumTv.setText(planRecruitNum);
                    }
                    if (!TextUtils.isEmpty(startTime)) {
                        startTv.setText(Utils.getInstance(this).dateFormat(Long.valueOf(startTime)));
                    }
                    if (!TextUtils.isEmpty(endTime)) {
                        finishTimeTv.setText(Utils.getInstance(this).dateFormat(Long.valueOf(endTime)));
                    }
                    if (!TextUtils.isEmpty(is_delegate)) {
                        if ("Y".equals(is_delegate)) {
                            resumeRecruitingView.setVisibility(View.VISIBLE);
                            isdelegateTv.setText(getString(R.string.yes));
                        } else {
                            isdelegateTv.setText(getString(R.string.no));
                        }
                    }
                    isDelegateLayout.setVisibility(View.GONE);
                    isDelegateView.setVisibility(View.GONE);

                    if (!TextUtils.isEmpty(create_user)) {
                        createTv.setText(create_user);
                    }
                    if (!TextUtils.isEmpty(processer_user)) {
                        processerView.setText(processer_user);
                    }
                    if (!TextUtils.isEmpty(cc_user)) {
                        copyPersonTv.setText(cc_user);
                    }
                    if (!TextUtils.isEmpty(language)) {
                        tvLanguage01.setText(language);
                    }
                    if (!TextUtils.isEmpty(language_level)) {
                        tvLanguage01MasteryDegree.setText(language_level);
                    }
                    if (!TextUtils.isEmpty(language2)) {
                        tvLanguage02.setText(language2);
                    }
                    if (!TextUtils.isEmpty(language_level2)) {
                        tvLanguage02MasteryDegree.setText(language_level2);
                    }
                    if (!TextUtils.isEmpty(major1)) {
                        tvZhuanye01.setText(major1);
                    }
                    if (!TextUtils.isEmpty(major2)) {
                        tvZhuanye02.setText(major2);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null) {
            mNetworkManager.cancle(this);
        }
    }

    private void showLoadingView() {
        contentLayout.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.VISIBLE);
        loaddingMsg.setVisibility(View.VISIBLE);
        loaddingMsg.setText(getString(R.string.dataloading));
    }

    private void hideLoadingView() {
        loading.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);
        loaddingMsg.setVisibility(View.GONE);
        loaddingMsg.setText(null);
        contentLayout.setVisibility(View.VISIBLE);
    }
}
