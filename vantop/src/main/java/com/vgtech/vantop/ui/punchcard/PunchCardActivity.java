package com.vgtech.vantop.ui.punchcard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.vgtech.common.api.AppPermission;
import com.vgtech.common.ui.zxing.zxing.ToastUtil;
import com.vgtech.common.utils.AppPermissionPresenterProxy;
import com.vgtech.common.utils.DeviceUtils;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ViewPagerAdapter;
import com.vgtech.vantop.ui.SearchActivity;
import com.vgtech.vantop.ui.clockin.ClockInListFragment;
import com.vgtech.vantop.ui.view.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * 考勤打卡主界面
 * author: scott
 * Created at scott on 2016/9/5.
 */
public class PunchCardActivity extends SearchActivity implements
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
        setTitle(getString(R.string.title_signcard));
        initData();
        initViews();
        if (mDatas.size() == 1) {
            mIndicator.setVisibility(View.GONE);
            setTitle(mTabs.get(0));
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
        boolean visibleSearch = true;
        int position = -1;
        if (AppPermissionPresenterProxy.hasPermission(this, AppPermission.Type.clock_out, AppPermission.ClockOut.punch.toString())) {
            mTabs.add(getString(R.string.vantop_punchcard_operation));
            mDatas.add(new PuncCardMainFragment());
            visibleSearch = false;
        }
        if (AppPermissionPresenterProxy.hasPermission(this, AppPermission.Type.clock_out, AppPermission.ClockOut.punch_record.toString())) {
            mTabs.add(getString(R.string.vantop_punchcard_record));
            mDatas.add(new PunchCardHistoryFragment());
            position = 0;
        }
//        if (AppPermissionPresenterProxy.hasPermission(this, AppPermission.Type.clock_out, AppPermission.ClockOut.attendance_record.toString())) {
//            mTabs.add(getString(R.string.vantop_punchcard_clockin));
//            mDatas.add(new ClockInListFragment());
//            position = position == 0 ? 1 : 0;
//        }
        if (visibleSearch) {
            setSearchVisiable(true);
            //设置搜索为单时间选择
            setSearchStyle(DIALOG_DATEMOUNTH_STYLE);
            //当前页为考勤记录 则由考勤fragment去响应搜索事件
            if (!mDatas.isEmpty())
                setSearchListenner((OnDoSeachActionListenner) mDatas.get(position));
        }
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
        if (fragment instanceof PuncCardMainFragment) {
            setSearchVisiable(false);
            Fragment fm = mDatas.get(position);
            if (fm instanceof ReLoadFragment) {
                ((ReLoadFragment) fm).reLoad();
            }
        } else if (fragment instanceof PunchCardHistoryFragment) {
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

    public interface OnDoSeachActionListenner {
        void onSearch(String startTime, String endTime, String option1, String option2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
