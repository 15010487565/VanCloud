package com.vgtech.common.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vgtech.common.R;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.utils.EditionUtils;
import com.vgtech.common.view.IphoneDialog;

/**
 * Created by Duke on 2016/5/26.
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    protected IphoneDialog iphoneDialog = null;
    private Toast mToast;


    // 在Fragment的onCreate中会判断宿主FragmentActivity是否已继承了该接口。
    // 在Fragment的onStart()方法中就会调用该接口
    // 告诉宿主FragmentActivity自己是当前屏幕可见的Fragment。
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MobclickAgent.onEvent(getActivity(), getClass().getName());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_right) {
            EditionUtils.switchEdition(getActivity());
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(initLayoutId(), container, false);
        initView(view);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initEvent();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // 拦截触摸事件，防止泄露下去
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        super.onViewCreated(view, savedInstanceState);

    }

    /**
     * 初始化Fragment布局ID
     *
     * @return Fragment布局ID
     */
    protected abstract int initLayoutId();

    /**
     * 初始化View
     */
    protected abstract void initView(View view);

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 初始化事件
     */
    protected abstract void initEvent();


    /**
     * 返回键处理事件
     */
    protected abstract boolean onBackPressed();

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * @param mContext
     * @param contentStr
     */
    public void showLoadingDialog(Context mContext, String contentStr) {
        if (iphoneDialog == null) {
            iphoneDialog = new IphoneDialog(mContext);
        }
        iphoneDialog.setMessage(contentStr);
        iphoneDialog.show(true);
    }

    public void showLoadingDialog(Context mContext, String contentStr, boolean ifCandismiss) {
        if (iphoneDialog == null) {
            iphoneDialog = new IphoneDialog(mContext);
        }
        iphoneDialog.setMessage(contentStr);
        iphoneDialog.show(ifCandismiss);
    }


    /**
     *
     */
    public void dismisLoadingDialog() {
        if (iphoneDialog != null && iphoneDialog.isShowing()) {
            iphoneDialog.dismiss();
        }
    }

    /**
     * 显示toast
     */
    protected void showToast(String msg) {
        if (isDetached() || TextUtils.isEmpty(msg)) return;
        if (null == mToast) mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        mToast.setText(msg);
        mToast.show();
    }

    /**
     * 显示toast
     */
    protected void showToast(int msg) {
        showToast(getString(msg));
    }

    public void searchRequest(String keyword, String startTime, String endTime) {
    }

    /**
     * 在Activity容器中直接刷新当前显示的Fragment。
     */
    public void toRefresh() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getApplication().getNetworkManager().cancle(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

//    /**
//     * 显示加载过程
//     *
//     * @param infoView       显示数据的View
//     * @param loadingView    加载View
//     * @param msg            加载显示信息
//     * @param showLoadingBar 是否显示加载动画（true显示）
//     */
//    public void showDialog(View infoView, View loadingView, String msg, boolean showLoadingBar) {
//
//        TextView loadingMagView = (TextView) loadingView.findViewById(R.id.loadding_msg);
//        ProgressWheel loadingProgressBar = (ProgressWheel) loadingView.findViewById(R.id.progress_view);
//        ImageView emptyView = (ImageView) loadingView.findViewById(R.id.empty_logo);
//        infoView.setVisibility(View.INVISIBLE);
//        loadingView.setVisibility(View.VISIBLE);
//        if (showLoadingBar) {
//            loadingProgressBar.setVisibility(View.VISIBLE);
//            emptyView.setVisibility(View.GONE);
//        } else {
//            loadingProgressBar.setVisibility(View.GONE);
//            emptyView.setVisibility(View.VISIBLE);
//        }
//        if (TextUtils.isEmpty(msg))
//            loadingMagView.setText(getString(R.string.dataloading));
//        else
//            loadingMagView.setText(msg);
//        loadingMagView.setVisibility(View.VISIBLE);
//    }
//
//
//    public void dismisDialog(View infoView, View loadingView) {
//        TextView loadingMagView = (TextView) loadingView.findViewById(R.id.loadding_msg);
//        ProgressWheel loadingProgressBar = (ProgressWheel) loadingView.findViewById(R.id.progress_view);
//        ImageView emptyView = (ImageView) loadingView.findViewById(R.id.empty_logo);
//        emptyView.setVisibility(View.GONE);
//        loadingView.setVisibility(View.GONE);
//        loadingProgressBar.setVisibility(View.GONE);
//        loadingMagView.setVisibility(View.GONE);
//        loadingMagView.setText(null);
//        infoView.setVisibility(View.VISIBLE);
//    }

    public ApplicationProxy getApplication() {
        return (ApplicationProxy) getActivity().getApplication();
    }
    public RecyclerView.ItemDecoration getRecyclerViewDivider(@DrawableRes int drawableId) {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(drawableId));
        return itemDecoration;
    }
}
