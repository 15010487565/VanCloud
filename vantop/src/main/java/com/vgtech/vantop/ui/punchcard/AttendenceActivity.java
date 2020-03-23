package com.vgtech.vantop.ui.punchcard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.vgtech.common.api.AppPermission;
import com.vgtech.common.utils.AppPermissionPresenterProxy;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ViewPagerAdapter;
import com.vgtech.vantop.ui.SearchActivity;
import com.vgtech.vantop.ui.clockin.ClockInListFragment;
import com.vgtech.vantop.ui.clockin.SchedulingFragment;
import com.vgtech.vantop.ui.punchcard.PunchCardActivity.OnDoSeachActionListenner;
import com.vgtech.vantop.ui.view.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * 考勤
 * Created by vic on 2017/2/24.
 */
public class AttendenceActivity extends SearchActivity implements
        ViewPager.OnPageChangeListener {
    private ViewPager mVpger;
    private ViewPagerIndicator mIndicator;
    private List<String> mTabs;
    private ViewPagerAdapter mPagerAdapter;
    private List<Fragment> mDatas;
    private OnDoSeachActionListenner mSearchListenner;

    public void setSearchListenner(OnDoSeachActionListenner l) {
        mSearchListenner = l;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        initData();
        initViews();
        if (mDatas.size() == 1) {
            mIndicator.setVisibility(View.GONE);
//            setTitle(mTabs.get(0));
        }
        if (mDatas.size() == 0) {
            mIndicator.setVisibility(View.GONE);
        }
    }

    private void initViews() {

        mIndicator = (ViewPagerIndicator) findViewById(R.id.indicator_vp);
        mVpger = (ViewPager) findViewById(R.id.vp_menu);
        mVpger.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mVpger, 0);
        mIndicator.setTabItemTitles(mTabs);
        mIndicator.setOnPageChangeListenner(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.new_option_content) {
            ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                    .builder()
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true);

            for (String option : getResources().getStringArray(R.array.sign_status)) {
                actionSheetDialog.addSheetItem(option, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                String type = getResources().getStringArray(R.array.sign_status)[which];
                                setOptionItem(getString(R.string.lable_sign_status), type);
                            }
                        });
            }
            actionSheetDialog.show();
        } else {
            super.onClick(v);
        }

    }

    private void initData() {
        mTabs = new ArrayList<>();
        mDatas = new ArrayList<>();
        if (AppPermissionPresenterProxy.hasPermission(this, AppPermission.Type.kaoqin, AppPermission.Kaoqin.wodepaiban.toString())) {
            mTabs.add(getString(R.string.vantop_my_schedule));
            mDatas.add(new SchedulingFragment());
        }
        if (AppPermissionPresenterProxy.hasPermission(this, AppPermission.Type.kaoqin, AppPermission.Kaoqin.wodekaoqin.toString())) {
            mTabs.add(getString(R.string.vantop_punchcard_clockin));
            mDatas.add(new ClockInListFragment());
        }
        setSearchVisiable(true);
        //设置搜索为单时间选择
        setSearchStyle(DIALOG_DATEALL_STYLE);
        //当前页为考勤记录 则由考勤fragment去响应搜索事件
        if (!mDatas.isEmpty())
            setSearchListenner((OnDoSeachActionListenner) mDatas.get(0));
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mDatas);
    }


    @Override
    protected void search(String startTime, String endTime, String option1, String option2) {
        if (mSearchListenner != null) {
            mSearchListenner.onSearch(startTime, endTime, option1, option2);
        }
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_punchcard;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Fragment fragment = mPagerAdapter.getItem(position);
        if (fragment instanceof SchedulingFragment) {
            //设置搜索图标可见
            setSearchVisiable(true);
            //设置搜索为
            setSearchStyle(DIALOG_DATEALL_STYLE);
            setOptionItemGone();
            //当前页为打卡记录 则由打卡记录fragment去响应搜索事件
            setSearchListenner((OnDoSeachActionListenner) mDatas.get(position));
        } else if (fragment instanceof ClockInListFragment) {
            setSearchVisiable(true);
            //设置搜索为单时间选择
            setSearchStyle(DIALOG_DATEMOUNTH_STYLE);
            //当前页为考勤记录 则由考勤fragment去响应搜索事件
            setSearchListenner((OnDoSeachActionListenner) mDatas.get(position));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setCurrentPage(int index) {
        mVpger.setCurrentItem(index);
        Fragment fm = mPagerAdapter.getItem(index);
        if (fm instanceof ReLoadFragment) {
            ((ReLoadFragment) fm).reLoad();
        }
    }
}
