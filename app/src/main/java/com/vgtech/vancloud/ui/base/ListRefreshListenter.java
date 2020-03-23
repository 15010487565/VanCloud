package com.vgtech.vancloud.ui.base;

/**
 * Created by Duke on 2015/12/30.
 */
public interface ListRefreshListenter {

    /**
     * 下拉刷新
     */
    void onTopRefresh();

    /**
     * 上滑刷新加载更多
     */
    void onDownRefresh();
}
