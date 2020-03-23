package com.vgtech.vancloud.ui.web;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.chat.UsersMessagesFragment;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.fragment.BaseSwipeBackFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/11/24.
 * 产品详情
 */
public class AppInfoWebFragment extends BaseSwipeBackFragment implements SwipeRefreshLayout.OnRefreshListener {
    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Inject
    public Controller controller;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.web_app_info, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        TextView titleTv = (TextView) view.findViewById(android.R.id.title);
        titleTv.setText(R.string.title_product_info);
        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        Bundle bundle = getArguments();
        String json = bundle.getString("appmodule");
        try {
            final AppModule appModule = JsonDataFactory.getData(AppModule.class, new JSONObject(json));
           boolean hasPermission =  AppPermissionPresenter.hasPermission(getActivity(), AppPermission.Type.settings, AppPermission.Setting.open_permission.toString());
            if (!appModule.isOpen() && hasPermission) {
                View applyView = view.findViewById(R.id.btn_apply_open_app);
                applyView.setVisibility(View.VISIBLE);
                applyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferencesController preferencesController = new PreferencesController();
                        preferencesController.context = getActivity();
                        UserAccount userAccount = preferencesController.getAccount();
                        List<Staff> contactses = new ArrayList<Staff>();
                        Staff staff = new Staff(Constants.SERVICE_USERID, Constants.SERVICE_USERID, getString(R.string.vancloud_coustom_service), "", PrfUtils.getTenantId(getActivity()));
                        contactses.add(staff);
                        UsersMessagesFragment fragment = UsersMessagesFragment.newInstanceForward(
                                ChatGroup.fromStaff(contactses.get(0), PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity())), getString(R.string.lable_apply_app_template, TenantPresenter.getCurrentTenant(getActivity()).tenant_name, userAccount.user_name, appModule.name, PrfUtils.getPrfparams(getActivity(), "username")));
                        controller.pushFragment(fragment);
                    }
                });
            }
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.my_swiperefreshlayout);
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setEnabled(false);
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                    android.R.color.holo_orange_light, android.R.color.holo_red_light);
            mWebView = (WebView) view.findViewById(R.id.webview);
            WebSettings settings = mWebView.getSettings();
            mWebView.clearCache(true);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            settings.setJavaScriptEnabled(true);
            settings.setBlockNetworkImage(false); // 解决图片不显示
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            mWebView.setWebViewClient(new MyWebViewClient());
            mWebView.setWebChromeClient(new MyWebChromeClient());
            mWebView.loadUrl(appModule.url);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attachToSwipeBack(view);
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mWebView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!isAdded())
                return;
//            view.getSettings().setJavaScriptEnabled(true);
            swipeRefreshLayout.setRefreshing(false);
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (!isAdded()||isDetached())
                return;
            Toast.makeText(getActivity(), getString(R.string.plans_web_toast), Toast.LENGTH_SHORT).show();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CookieSyncManager.createInstance(getActivity().getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookie();
            cookieManager.flush();
        } else {
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
        WebStorage.getInstance().deleteAllData(); //清空WebView的localStorage
    }
}
