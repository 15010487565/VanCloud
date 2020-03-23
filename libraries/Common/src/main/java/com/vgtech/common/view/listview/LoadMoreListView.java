package com.vgtech.common.view.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.vgtech.common.R;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;


/**
 * Created by Duke on 2015/12/23.
 */
public class LoadMoreListView extends SwipeMenuListView implements AbsListView.OnScrollListener {

    private LayoutInflater inflater;
    private View footerView;
    private int visibleLastIndex = 0;

    private OnLoadMoreListener onLoadMoreListener;

    /**
     * 是否支持分页刷新（true可以分页刷新，false禁止分页刷新,默认false）
     */
    private boolean loadMoreState = false;
    /**
     * 刷新状态（true正在刷新，false没有处于刷新状态，默认false）
     */
    private boolean loadingState = false;

    private int footerViewHeight;


    public LoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        footerView = inflater.inflate(R.layout.load_more_footer, null, false);
        measureView(footerView);
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, 0, 0, -footerViewHeight);
        footerView.setVisibility(View.GONE);
        this.addFooterView(footerView);
        this.setOnScrollListener(this);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        int lastIndex = getAdapter().getCount();
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == lastIndex && onLoadMoreListener != null && loadMoreState && !loadingState) {
            showLoadMoreView();
            onLoadMoreListener.onLoadMore();
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }


    public void setLoadMoreState(boolean loadMoreState) {
        this.loadMoreState = loadMoreState;
    }


    public void hidLoadMoreView() {
        loadingState = false;
        footerView.setPadding(0, 0, 0, -footerViewHeight);
        footerView.setVisibility(View.GONE);
    }

    public void showLoadMoreView() {
        loadingState = true;
        footerView.setPadding(0, 0, 0, 0);
        footerView.setVisibility(VISIBLE);
        setSelection(getBottom());
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
