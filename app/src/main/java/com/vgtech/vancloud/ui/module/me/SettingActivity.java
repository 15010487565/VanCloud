package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.vgtech.common.ACache;
import com.vgtech.common.Constants;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.LoginActivity;
import com.vgtech.vancloud.ui.SplashActivity;
import com.vgtech.vancloud.ui.chat.UsersMessagesFragment;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.fragment.BaseSwipeBackFragment;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.utils.AppModulePresenterVantop;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by zhangshaofang on 2015/10/9.
 */
public class SettingActivity extends BaseSwipeBackFragment implements View.OnClickListener {

    private TextView tvLanguage;
    private View mChangePhoneNum;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.setting, null);
        mView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        TextView titleTv = (TextView) mView.findViewById(android.R.id.title);
        titleTv.setText(R.string.settings);
        mView.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        initView();
        return attachToSwipeBack(mView);
    }


    private View mView;

    private View findViewById(int resId) {
        return mView.findViewById(resId);
    }

    protected void initView() {
        tvLanguage = (TextView) findViewById(R.id.tv_language);
        findViewById(R.id.set_language_layout).setOnClickListener(this);
        findViewById(R.id.btn_update_pwd).setOnClickListener(this);
        findViewById(R.id.btn_logout_out).setOnClickListener(this);
        findViewById(R.id.btn_about_vancloud).setOnClickListener(this);
        findViewById(R.id.btn_bar_code).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.feedback_item).setOnClickListener(this);
        findViewById(R.id.btn_vancloud_coustom_service).setOnClickListener(this);

        mChangePhoneNum = findViewById(R.id.btn_change_phone_number);
        mChangePhoneNum.setOnClickListener(this);

        boolean openShowPhoneNum = AppModulePresenterVantop.isOpenPermission(this.getContext(), AppModulePresenterVantop.Type.qita, "qita:showxiugaishouji");
        if (openShowPhoneNum) {
            if (mChangePhoneNum.getVisibility() != View.VISIBLE) {
                mChangePhoneNum.setVisibility(View.VISIBLE);
            }
        }else {
            if (mChangePhoneNum.getVisibility() != View.GONE) {
                mChangePhoneNum.setVisibility(View.GONE);
            }
        }
        tv_cache = (TextView) findViewById(R.id.tv_cache);
        SharedPreferences preferences = PrfUtils.getSharePreferences(getActivity());
        int bell = preferences.getInt("PREF_TIP_BELL", 1);
        int shock = preferences.getInt("PREF_TIP_SHOCK", 0);
        int msg = preferences.getInt("PREF_TIP_MSG", 1);
        CheckBox rb_msg = (CheckBox) findViewById(R.id.rb_msg);
        CheckBox rb_voice = (CheckBox) findViewById(R.id.rb_voice);
        CheckBox rb_vibration = (CheckBox) findViewById(R.id.rb_vibration);
        rb_msg.setChecked(msg != 0);
        rb_voice.setChecked(bell != 0);
        rb_vibration.setChecked(shock != 0);
        rb_msg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("PREF_TIP_MSG", isChecked ? 1 : 0);
                editor.commit();
            }
        });
        rb_voice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("PREF_TIP_BELL", isChecked ? 1 : 0);
                editor.commit();
            }
        });
        rb_vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("PREF_TIP_SHOCK", isChecked ? 1 : 0);
                editor.commit();
            }
        });
        if (PrfUtils.isChineseForAppLanguage(getActivity())) {
            tvLanguage.setText(getString(R.string.language_chinese));
        } else {
            tvLanguage.setText(getString(R.string.language_english));
        }
    }

    private TextView tv_cache;
    @Inject
    public Controller controller;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear:
                new AlertDialog(getActivity()).builder().setTitle(getString(R.string.clear_cache))
                        .setMsg(getString(R.string.clear_cache_confirm))
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            FileCacheUtils.clearCache(FileCacheUtils.getCacheDir(getActivity()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        ACache.get(getActivity()).clear();
                                    }
                                }).start();
                                Toast.makeText(getActivity(), getResources().getString(R.string.clear_cache_success), Toast.LENGTH_SHORT).show();
                                tv_cache.setText("0.00KB");
                            }
                        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.btn_about_vancloud://关于万客
                Intent aboutVanCloud = new Intent(getActivity(), AboutVanCloudActivity.class);
                aboutVanCloud.putExtra("style", "company");
                startActivity(aboutVanCloud);
                break;
            case R.id.feedback_item: {
                Intent intent = new Intent(getActivity(), NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FEEDBACK);
                startActivity(intent);
            }
            break;
            case R.id.btn_vancloud_coustom_service: {
                List<Staff> contactses = new ArrayList<Staff>();
                Staff staff = new Staff(Constants.SERVICE_USERID, Constants.SERVICE_USERID, getString(R.string.vancloud_coustom_service), "", PrfUtils.getTenantId(getActivity()));
                contactses.add(staff);
                UsersMessagesFragment fragment = UsersMessagesFragment.newInstance(
                        ChatGroup.fromStaff(contactses.get(0), PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity())), null);
                controller.pushFragment(fragment);
            }
            break;
            case R.id.btn_update_pwd:
                Intent intentSett = new Intent(getActivity(), UpdatePwdActivity.class);
                startActivity(intentSett);
                break;
            case R.id.btn_logout_out:
                new AlertDialog(getActivity()).builder().setTitle(getString(R.string.logout_current_account))
                        .setMsg(getString(R.string.logout_current_account_confirm))
                        .setPositiveButton(getString(R.string.logout_current_account_sure), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ShortcutBadger.with(getActivity()).count(0);
//                                FileUtils.writeString("SettingActivity -> 点击了退出登录,退出到登录界面！\r\n");
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                Utils.clearUserInfo(getActivity());
                                ApplicationProxy applicationProxy = (ApplicationProxy) getActivity().getApplication();
                                applicationProxy.clear();
                                startActivityForResult(intent, 11);
                                Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                                getActivity().sendBroadcast(reveiverIntent);
                            }
                        }).setNegativeButton(getString(R.string.new_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.set_language_layout: //中英文切换
                new ActionSheetDialog(getActivity())
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem(getString(R.string.language_chinese), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        if (PrfUtils.isChineseForAppLanguage(getActivity())) {
                                            return;
                                        }
                                        VanCloudApplication vanCloudApplication = (VanCloudApplication) getActivity().getApplication();
                                        vanCloudApplication.getApiUtils().setLanguage("zh");
                                        PrfUtils.savePrfparams(getActivity(), "is_language", "zh");
                                        PrfUtils.getInstance(getActivity()).saveLanguage( 1);

                                        Intent mainIntent = new Intent(getActivity(), SplashActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getActivity().startActivity(mainIntent);
                                        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        getActivity().finish();
                                    }
                                })

                        .addSheetItem(getString(R.string.language_english), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        if (!PrfUtils.isChineseForAppLanguage(getActivity())) {
                                            return;
                                        }
                                        VanCloudApplication vanCloudApplication = (VanCloudApplication) getActivity().getApplication();
                                        vanCloudApplication.getApiUtils().setLanguage("en");
                                        PrfUtils.savePrfparams(getActivity(), "is_language", "en");
                                        PrfUtils.getInstance(getActivity()).saveLanguage( 3);

                                        Intent mainIntent = new Intent(getActivity(), SplashActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getActivity().startActivity(mainIntent);
                                        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        getActivity().finish();
                                    }
                                }).show();
                break;

            case R.id.btn_change_phone_number:
                Intent intent = new Intent(getActivity(), ChangePhoneNumberActivity.class);
                startActivity(intent);
                break;
        }
    }

}
