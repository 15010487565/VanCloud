package com.vgtech.vancloud.ui.beidiao;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.adapter.TabViewPagerAdapter;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by vic on 2016/10/14.
 */
public class BeidiaoActivity extends BaseActivity implements HttpListener<String>, ViewPager.OnPageChangeListener {
    private ViewPager mViewPager;
    private BdStepOneFragment mOneFragment;
    private BdStepTwoFragment mTwoFragment;
    private BdStepThreeFragment mThreeFragment;

    private View mStepOneLineView, mStepTwoLineView;
    private TextView mStepTwoView, mStepThreeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("背景调查");
        if (!AppPermissionPresenter.hasPermission(this, AppPermission.Type.beidiao, AppPermission.Beidiao.start.toString())) {
            startActivity(new Intent(this, BdListActivity.class));
            finish();
            return;
        }
        if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.beidiao, AppPermission.Beidiao.my.toString())) {
            initRightTv("我的调查记录");
        } else if (AppPermissionPresenter.hasPermission(this, AppPermission.Type.beidiao, AppPermission.Beidiao.all.toString())) {
            initRightTv("调查记录");
        }
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        mStepOneLineView = findViewById(R.id.step_one_line);
        mStepTwoLineView = findViewById(R.id.step_two_line);
        mStepTwoView = (TextView) findViewById(R.id.step_two);
        mStepThreeView = (TextView) findViewById(R.id.step_three);
        List fragmentList = new ArrayList<>();
        mOneFragment = new BdStepOneFragment();
        mTwoFragment = new BdStepTwoFragment();
        mThreeFragment = new BdStepThreeFragment();
        mOneFragment.setStepListener(stepListener);
        mTwoFragment.setStepListener(stepListener);
        mThreeFragment.setStepListener(stepListener);
        fragmentList.add(mOneFragment);
        fragmentList.add(mTwoFragment);
        fragmentList.add(mThreeFragment);
        mViewPager.addOnPageChangeListener(this);
        TabViewPagerAdapter fragmentViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(fragmentViewPagerAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                startActivity(new Intent(this, BdListActivity.class));
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if (mViewPager.getCurrentItem() == 2) {
            stepListener.reset();
            mViewPager.setCurrentItem(0);
        } else if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        } else {
            super.finish();
        }
    }

    private BdStepListener stepListener = new BdStepListener() {
        @Override
        public void reset() {
            mOneFragment.reset();
            mViewPager.setCurrentItem(0);
        }

        @Override
        public void stepOne(Map<String, String> params) {
            mViewPager.setCurrentItem(1);
            mTwoFragment.setParams(params);
        }

        @Override
        public void stepTwo() {
            mViewPager.setCurrentItem(2);
            mThreeFragment.start();
        }
    };

    @Override
    protected int getContentView() {
        return R.layout.activity_beidiao;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                mStepOneLineView.setBackgroundColor(Color.parseColor("#ff5CB85C"));
                mStepTwoLineView.setBackgroundColor(Color.parseColor("#fff0f0f0"));
                mStepTwoView.setEnabled(false);
                mStepThreeView.setEnabled(false);
                setTitle("填写被调人信息");
                break;
            case 1:
                mStepOneLineView.setBackgroundColor(Color.parseColor("#ff5CB85C"));
                mStepTwoLineView.setBackgroundColor(Color.parseColor("#ff5CB85C"));
                mStepTwoView.setEnabled(true);
                mStepThreeView.setEnabled(false);
                setTitle("选择调查项目");
                break;
            case 2:
                mStepOneLineView.setBackgroundColor(Color.parseColor("#ff5CB85C"));
                mStepTwoLineView.setBackgroundColor(Color.parseColor("#ff5CB85C"));
                mStepTwoView.setEnabled(true);
                mStepThreeView.setEnabled(true);
                setTitle("生成调查结果");
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
