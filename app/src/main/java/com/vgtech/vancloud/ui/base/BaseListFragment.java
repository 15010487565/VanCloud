package com.vgtech.vancloud.ui.base;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.common.view.listview.LoadMoreListView;
import com.vgtech.common.view.progressbar.ProgressWheel;

/**
 * Created by Duke on 2015/12/29.
 */
public class BaseListFragment extends LazyLoadFragment implements SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.OnLoadMoreListener {

    public SwipeRefreshLayout swipeRefreshLayout;
    public LoadMoreListView loadMoreListView;
    private ListRefreshListenter listRefreshListenter;

    public TextView loadingMagView;
    public ProgressWheel loadingProgressBar;
    public LinearLayout loadingLayout;



    @Override
    public void onRefresh() {

        if (listRefreshListenter != null) {
            listRefreshListenter.onTopRefresh();
        }

    }

    @Override
    public void onLoadMore() {
        if (listRefreshListenter != null) {
            listRefreshListenter.onDownRefresh();
        }
    }


    public void showLoadingView() {

        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMagView.setVisibility(View.VISIBLE);
        loadingMagView.setText(getString(R.string.dataloading));

    }

    public void hideLoadingView() {

        loadingLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingMagView.setVisibility(View.GONE);
        loadingMagView.setText(null);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 重置listview下拉刷新状态
     */
    public void resetListViewTopRefreshState() {

        swipeRefreshLayout.setRefreshing(false);

    }

    /**
     * 重置listview分页刷新状态
     */
    public void resetListViewDownRefreshState() {

        loadMoreListView.hidLoadMoreView();
    }

}
