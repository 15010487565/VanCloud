package com.vgtech.vancloud.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.inject.Inject;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.module.me.CollectionActivity;
import com.vgtech.vancloud.ui.module.me.DraftActivity;
import com.vgtech.vancloud.ui.module.me.SelfInfoActivity;
import com.vgtech.vancloud.ui.module.me.SettingActivity;
import com.vgtech.vancloud.ui.register.ui.CompanyInfoEditActivity;
import com.vgtech.vancloud.ui.register.ui.UpdatePositionActivity;
import com.vgtech.vancloud.ui.web.WebActivity;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;

/**
 * Created by John on 2015/8/7.
 */
public class MeFragment extends BaseFragment implements View.OnClickListener, HttpListener<String> {
    private static final int RESULT_UPDATE_NAME = 1;
    private TextView nameTv;
    @Inject
    public Controller controller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectViewMembers(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    protected int initLayoutId() {
        return R.layout.me_fragment_layout;
    }

    private TextView mDraftNumTv;
    private View draft_item;
    private SimpleDraweeView mUserIcon;
    private TextView mSignTv;

    @Override
    protected void initView(View view) {
        nameTv = (TextView) view.findViewById(R.id.user_name);
        PreferencesController preferencesController = new PreferencesController();
        preferencesController.context = getActivity();
        UserAccount userAccount = preferencesController.getAccount();
        nameTv.setText(userAccount.user_name);

        mDraftNumTv = (TextView) view.findViewById(R.id.draft_num);
        mUserIcon = (SimpleDraweeView) view.findViewById(R.id.user_photo);
        ImageOptions.setUserImage(mUserIcon, userAccount.photo);
        mSignTv = (TextView) view.findViewById(R.id.text_autograph);
        String phone = PrfUtils.getUserPhone(getActivity());
        if (!TextUtils.isEmpty(phone)) {
            mSignTv.setText(getString(R.string.phone_num) + phone);
        }
        view.findViewById(R.id.company_item).setOnClickListener(this);//公司信息
        view.findViewById(R.id.btn_user).setOnClickListener(this);
        if ((AppPermissionPresenter.hasPermission(getActivity(), AppPermission.Type.settings, AppPermission.Setting.position.toString()))
                && !TenantPresenter.isVanTop(getActivity())) {
            view.findViewById(R.id.post_manger_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.post_manger_layout).setOnClickListener(this);//职位管理
        } else {
            view.findViewById(R.id.post_manger_layout).setVisibility(View.GONE);
        }
        //view.findViewById(R.id.organization_item).setOnClickListener(this);
        //view.findViewById(R.id.contact_item).setOnClickListener(this);
        //view.findViewById(R.id.work_group_item).setOnClickListener(this);
        draft_item = view.findViewById(R.id.draft_item);
        draft_item.setOnClickListener(this);//草稿箱
        view.findViewById(R.id.collection_item).setOnClickListener(this);
        //view.findViewById(R.id.account_item).setOnClickListener(this);
        view.findViewById(R.id.setting_item).setOnClickListener(this);
        view.findViewById(R.id.btn_help).setOnClickListener(this);

    }

    @Override
    protected void initData() {
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferencesController preferencesController = new PreferencesController();
        preferencesController.context = getActivity();
        UserAccount userAccount = preferencesController.getAccountUnCache();
        nameTv.setText(userAccount.user_name);
        ImageOptions.setUserImage(mUserIcon, userAccount.photo);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int taskCount = 0;
                if (getActivity() != null && !getActivity().isFinishing())
                    taskCount = PublishTask.queryPublishCount(getActivity());
                return taskCount;
            }

            @Override
            protected void onPostExecute(Integer taskCount) {
                if (taskCount == 0) {
                    mDraftNumTv.setVisibility(View.GONE);
                    draft_item.setVisibility(View.GONE);
                } else {
                    draft_item.setVisibility(View.VISIBLE);
                    mDraftNumTv.setVisibility(View.VISIBLE);
                    mDraftNumTv.setText(String.valueOf(taskCount));
                }

            }
        }.execute();

        String phone = PrfUtils.getUserPhone(getActivity());
        if (!TextUtils.isEmpty(phone)) {
            mSignTv.setText(getString(R.string.phone_num) + phone);
        }
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.company_item: {
                startActivity(new Intent(getActivity(), CompanyInfoEditActivity.class));
//                startActivity(new Intent(getActivity(), CompanyInfoActivity.class));
            }
            break;
            case R.id.post_manger_layout:
                Intent intent_post = new Intent(getActivity(), UpdatePositionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("goActivity", "CompanyInfoActivity");
                intent_post.putExtras(bundle);
                startActivity(intent_post);
                break;
            case R.id.btn_help: {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                intent.putExtra("title", getString(R.string.lable_help));
                String language = PrfUtils.getAppLanguage(getActivity());
                String path = URLAddr.URL_GUIDE.replace("%2$s", language);
                String url = ApiUtils.generatorUrl(getActivity(), path);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
            break;
//            case R.id.organization_item: {
//                Intent intent = new Intent(getActivity(), GroupActivity.class);
//                startActivity(intent);
//            }
//            break;
            case R.id.btn_user: {//个人信息界面
                if (TenantPresenter.isVanTop(getActivity())) {
                    Intent intent = new Intent(getActivity(), VantopUserInfoActivity.class);
                    intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, PrfUtils.getStaff_no(getActivity()));
                    startActivity(intent);
                } else {//已完成的个人信息界面
                    Intent intentContact = new Intent(getActivity(), SelfInfoActivity.class);
                    startActivityForResult(intentContact, RESULT_UPDATE_NAME);
                }
            }
            break;
            case R.id.draft_item:
                Intent intentDraft = new Intent(getActivity(), DraftActivity.class);
                startActivity(intentDraft);
                break;
            case R.id.collection_item:
                Intent collection = new Intent(getActivity(), CollectionActivity.class);
                startActivity(collection);
                break;
            case R.id.setting_item: {
                Intent broadcIntent = new Intent(GroupReceiver.PERMISSIONS_REFRESH);
                getActivity().sendBroadcast(broadcIntent);
                SettingActivity fragment = new SettingActivity();
                controller.pushFragment(fragment);
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_UPDATE_NAME && resultCode == getActivity().RESULT_OK) {
            String user_name = data.getStringExtra("name");
            nameTv.setText(user_name);
        }
    }

    private static final int CALLBACK_SIGN = 1;
    private NetworkManager mNetWorkManager;

    private void updateSign(String sign) {
        mNetWorkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("content", sign);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_USER_UPDATESIGN), params, getActivity());
        mNetWorkManager.load(CALLBACK_SIGN, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_SIGN:
                PreferencesController preferencesController = new PreferencesController();
                preferencesController.context = getActivity();
                UserAccount userAccount = preferencesController.getAccount();
                userAccount.sign = path.getPostValues().get("content");
                preferencesController.storageAccount(userAccount);
                break;
        }
    }


    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
    
}
