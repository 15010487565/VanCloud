package com.vgtech.vancloud.ui.module.recruit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.vgtech.common.api.AppPermission;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.module.accountmanagement.AccountManagementActivity;

/**
 * Created by code on 2016/7/25.
 */
public class ResumeHomeActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected int getContentView() {
        return R.layout.activity_resume_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.home_recruit));
        if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.zhaopin, AppPermission.Zhaopin.position.toString())) {
            findViewById(R.id.btn_resume_job_manage).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_resume_job_manage).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_resume_job_manage).setVisibility(View.GONE);
        }
        if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.zhaopin, AppPermission.Zhaopin.resume.toString())) {
            findViewById(R.id.btn_resume_manage).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_resume_manage).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_resume_manage).setVisibility(View.GONE);
        }
        if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.zhaopin, AppPermission.Zhaopin.allresume.toString())) {
            findViewById(R.id.btn_resume_ku).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_resume_ku).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_resume_ku).setVisibility(View.GONE);
        }
        if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.zhaopin, AppPermission.Zhaopin.account.toString())) {
            findViewById(R.id.btn_resume_account_manger).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_resume_account_manger).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_resume_account_manger).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_resume_job_manage) {
            startActivity(new Intent(this, RecruitmentInfoActivity.class));
        } else if (v.getId() == R.id.btn_resume_manage) {
            startActivity(new Intent(this, ResumeManageActivity.class));
        } else if(v.getId() == R.id.btn_resume_ku) {
            startActivity(new Intent(this, SearchResumeActivity.class));
        } else if (v.getId() == R.id.btn_resume_account_manger) {
            startActivity(new Intent(this, AccountManagementActivity.class));
        } else {
            super.onClick(v);
        }
    }
}
