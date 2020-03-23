package com.vgtech.vancloud.ui.module.recruit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.common.URLAddr;
import com.vgtech.common.ui.AreaSelectActivity;
import com.vgtech.common.ui.DataProviderActivity;
import com.vgtech.common.ui.DictSelectActivity;
import com.vgtech.common.ui.JobDictSelectActivity;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by code on 2016/5/19.
 * 简历搜索
 */
public class SearchResumeActivity extends BaseActivity implements View.OnClickListener {

    EditText etSearchWord;
    TextView tvEducation;
    RelativeLayout educationLayout;
    TextView tvWorkExperience;
    RelativeLayout workExperienceLayout;
    TextView tvSex;
    RelativeLayout sexLayout;
    TextView tvStatus;
    RelativeLayout statusLayout;
    TextView tvPlace;
    RelativeLayout placeLayout;
    TextView tvCategory;
    RelativeLayout categoryLayout;
    TextView tvIndustryCategory;
    RelativeLayout industryCategoryLayout;
    Button btnSubmit;

    @Override
    protected int getContentView() {
        return R.layout.activity_search_resume;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.search_resume));
        initView();
        initEvent();
    }

    private void initView() {
        etSearchWord = (EditText) findViewById(R.id.et_search_word);
        tvEducation = (TextView) findViewById(R.id.tv_education);
        educationLayout = (RelativeLayout) findViewById(R.id.education_layout);
        tvWorkExperience = (TextView) findViewById(R.id.tv_work_experience);
        workExperienceLayout = (RelativeLayout) findViewById(R.id.work_experience_layout);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        sexLayout = (RelativeLayout) findViewById(R.id.sex_layout);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        statusLayout = (RelativeLayout) findViewById(R.id.status_layout);
        tvPlace = (TextView) findViewById(R.id.tv_place);
        placeLayout = (RelativeLayout) findViewById(R.id.place_layout);
        tvCategory = (TextView) findViewById(R.id.tv_category);
        categoryLayout = (RelativeLayout) findViewById(R.id.category_layout);
        tvIndustryCategory = (TextView) findViewById(R.id.tv_industry_category);
        industryCategoryLayout = (RelativeLayout) findViewById(R.id.industry_category_layout);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
    }

    private void initEvent() {
        educationLayout.setOnClickListener(this);
        workExperienceLayout.setOnClickListener(this);
        sexLayout.setOnClickListener(this);
        statusLayout.setOnClickListener(this);
        placeLayout.setOnClickListener(this);
        categoryLayout.setOnClickListener(this);
        industryCategoryLayout.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.education_layout:
                Intent intent_ed = new Intent(this, DictSelectActivity.class);
                intent_ed.putExtra("title", getString(R.string.vancloud_select_education_requirements));
                intent_ed.putExtra("style", "company");
                intent_ed.putExtra("id", mIdsMap.get(100));
                intent_ed.setData(Uri.parse(URLAddr.URL_RESUME_DEGREE));
                startActivityForResult(intent_ed, 100);
                break;
            case R.id.work_experience_layout:
                Intent intent_worktime = new Intent(this, DataProviderActivity.class);
                intent_worktime.putExtra("type", DataProviderActivity.GONGZUONIANXIAN);
                intent_worktime.putExtra("style", "company");
                intent_worktime.putExtra("id", mIdsMap.get(101));
                startActivityForResult(intent_worktime, 101);
                break;
            case R.id.sex_layout:
                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);

                for (String option : getResources().getStringArray(R.array.sex_type)) {
                    actionSheetDialog.addSheetItem(option, ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    String type = getResources().getStringArray(R.array.sex_type)[which];
                                    tvSex.setText(type);
                                    tvSex.setTag(which);
                                }
                            });
                }
                actionSheetDialog.show();
                break;
            case R.id.status_layout:
                Intent intent_status = new Intent(this, DictSelectActivity.class);
                intent_status.putExtra("title", getString(R.string.vancloud_select_job_status));
                intent_status.putExtra("style", "company");
                intent_status.putExtra("id", mIdsMap.get(102));
                intent_status.setData(Uri.parse(URLAddr.URL_RESUME_SITUATION));
                startActivityForResult(intent_status, 102);
                break;
            case R.id.place_layout:
                Intent intent_workcity = new Intent(this, AreaSelectActivity.class);
                intent_workcity.putExtra("style", "company");
                intent_workcity.putExtra("id", mIdsMap.get(103));
                startActivityForResult(intent_workcity, 103);
                break;
            case R.id.category_layout:
                Intent intent_category = new Intent(this, JobDictSelectActivity.class);
                intent_category.putExtra("title", getString(R.string.vancloud_select_functional_categories));
                intent_category.putExtra("style", "company");
                intent_category.putExtra("id", mIdsMap.get(104));
                intent_category.putExtra("name", mNameMap.get(104));
                intent_category.putExtra("type", JobDictSelectActivity.ZHINENG);
                intent_category.setData(Uri.parse(URLAddr.URL_FUNCTIONS));
                startActivityForResult(intent_category, 104);
                break;
            case R.id.industry_category_layout:
                Intent intent_in = new Intent(this, JobDictSelectActivity.class);
                intent_in.putExtra("style", "company");
                intent_in.putExtra("id", mIdsMap.get(105));
                intent_in.putExtra("type", JobDictSelectActivity.HANGYE);
                startActivityForResult(intent_in, 105);
                break;
            case R.id.btn_submit:
                if (checkAction()) {
                    Intent intent = new Intent(this, ResumeListActivity.class);
                    intent.putExtra("type", "2");
                    intent.putExtra("word", etSearchWord.getText().toString());
                    intent.putExtra("education", tvEducation.getText().toString());
                    intent.putExtra("worktime", tvWorkExperience.getText().toString());
                    intent.putExtra("sex", tvSex.getText().toString());
                    intent.putExtra("status", tvStatus.getText().toString());
                    intent.putExtra("place", tvPlace.getText().toString());
                    intent.putExtra("sort", tvCategory.getText().toString());
                    intent.putExtra("industry", tvIndustryCategory.getText().toString());
                    startActivity(intent);
                }

                break;
            default:
                super.onClick(v);
                break;
        }
    }

    //至少填写一项搜索条件
    private boolean checkAction() {
        if (TextUtils.isEmpty(etSearchWord.getText().toString()) &&
                TextUtils.isEmpty(tvEducation.getText().toString()) &&
                TextUtils.isEmpty(tvWorkExperience.getText().toString()) &&
                TextUtils.isEmpty(tvSex.getText().toString()) &&
                TextUtils.isEmpty(tvStatus.getText().toString()) &&
                TextUtils.isEmpty(tvPlace.getText().toString()) &&
                TextUtils.isEmpty(tvCategory.getText().toString()) &&
                TextUtils.isEmpty(tvIndustryCategory.getText().toString())) {
            showToast(getString(R.string.vancloud_search_prompt));
            return false;
        }
        return true;
    }

    private Map<Integer, String> mIdsMap = new HashMap<>();
    private Map<Integer, String> mNameMap = new HashMap<>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String id = data.getStringExtra("id");
            String name = data.getStringExtra("name");
            mNameMap.put(requestCode, name);
            mIdsMap.put(requestCode, id);
            switch (requestCode) {
                case 100://学历要求
                    tvEducation.setText(name);
                    break;
                case 101://工作经验
                    tvWorkExperience.setText(name);
                    break;
                case 102://求职状态
                    tvStatus.setText(name);
                    break;
                case 103://居住地
                    tvPlace.setText(name);
                    break;
                case 104://职能类别
                    tvCategory.setText(name);
                    break;
                case 105://行业类别
                    tvIndustryCategory.setText(name);
                    break;
            }
        }
    }
}
