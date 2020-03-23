package com.vgtech.vancloud.ui.base;

import android.view.View;

import com.vgtech.vancloud.ui.BaseFragment;

/**
 * Created by Duke on 2015/9/16.
 */
public class LazyLoadFragment extends BaseFragment {

    /**
     * Fragment当前状态是否可见
     */
    protected boolean isVisible;
    /**
     * 是否第一次显示
     */
    protected boolean isFirstVisible = true;


    @Override
    protected int initLayoutId() {
        return 0;
    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    /**
     * 可见
     */
    protected void onVisible() {
        if (isFirstVisible) {
            lazyLoad();
            isFirstVisible = false;
        }
    }


    /**
     * 不可见
     */
    protected void onInvisible() {


    }


    /**
     * 延迟加载
     * 子类必须重写此方法
     */
    protected void lazyLoad() {

    }

}
