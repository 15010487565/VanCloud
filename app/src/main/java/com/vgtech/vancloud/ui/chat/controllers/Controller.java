package com.vgtech.vancloud.ui.chat.controllers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.igexin.sdk.PushManager;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.UserAccount;
import com.vgtech.vancloud.ui.LoginActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.chat.UsersMessagesFragment;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.net.NetMapAsyncTask;
import com.vgtech.vancloud.ui.chat.net.NetSilentAsyncTask;
import com.vgtech.vancloud.utils.NoticeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import roboguice.inject.ContextScopedProvider;
import roboguice.inject.ContextSingleton;
import roboguice.util.Ln;
import roboguice.util.Strings;

/**
 * @author xuanqiang
 */
@SuppressWarnings("UnusedDeclaration")
@ContextSingleton
public class Controller {

    public void logoutDialog() {
        new AlertDialog.Builder(context).setTitle(context.getString(R.string.app_logout))
                .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        FileUtils.writeString(context,"Controller -> 用户主动退出，到登录界面！\r\n");
                        logout();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void logout() {
        UserAccount account = prefController.getAccount();
//    account.uid = null;
//    if(!account.remember){
//      account.logname = null;
//      account.pwd = null;
//      account.getuiClientId = null;
//    }
        prefController.storageAccount(account);
        stopPush();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        getActivity().finish();
    }

    public View createActionBar(final int layoutResID) {
        return createActionBar(R.layout.actionbar, layoutResID);
    }

    public View createActionBar(final int actionBarResID, final int layoutResID) {
        View actionBar = getActivity().getLayoutInflater().inflate(actionBarResID, null);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setClickable(true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        assert actionBar != null;
        linearLayout.addView(actionBar, layoutParams);

        View content = getActivity().getLayoutInflater().inflate(layoutResID, null);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        assert content != null;
        linearLayout.addView(content, layoutParams);

        return linearLayout;
    }

    public void pushFragment(final Fragment fragment) {
        pushFragment(fragment, null);
    }

    public void pushFragment(final Fragment fragment, final String tag) {
        pushFragment(fragment, true, tag);
    }

    public void pushFragment(final Fragment fragment, final boolean isAdd, final String tag) {
        FragmentTransaction ft = ftAnimations();
        if (isAdd) {
            Fragment frag = fragment();
            if (frag != null) {
                frag.setUserVisibleHint(false);
            }
            ftAdd(ft, fragment, tag);
        } else {
            ft.replace(R.id.container, fragment, tag);
        }
        ft.addToBackStack(null).commitAllowingStateLoss();
    }

    public FragmentTransaction ftAdd(final FragmentTransaction ft, final Fragment fragment, final String tag) {
        return ft.add(R.id.container, fragment, tag);
    }

    @SuppressLint("CommitTransaction")
    public FragmentTransaction ft() {
        return fm().beginTransaction();
    }

    public FragmentTransaction ftAnimationsExcludeEnter() {
        return ft().setCustomAnimations(0, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
    }

    public FragmentTransaction ftAnimations() {
        return ft().setCustomAnimations(R.anim.push_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
    }

    public FragmentTransaction ftFadeAnimations() {
        return ft().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void replaceFragment(final Fragment fragment) {
        replaceFragment(R.id.container, fragment);
    }

    public void replaceFragment(final int resId, final Fragment fragment) {
        ft().replace(resId, fragment).commitAllowingStateLoss();
    }

    public void removeFragmentByHandler(final int resId) {
        if (fmProvider != null) {
            try {
                removeFragmentByHandler(fmProvider.get(context).findFragmentById(resId));
            } catch (Exception e) {

            }
        }
    }

    public void removeFragmentByHandler(final Fragment fragment) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    removeFragment(fragment);
                } catch (Exception ignored) {
                }
            }
        });
    }

    public void removeFragment(final int resId) {
        removeFragment(fm().findFragmentById(resId));
    }

    public void removeFragment(final Fragment fragment) {
        if (fragment != null) {
            ft().remove(fragment).commitAllowingStateLoss();
        }
    }

    public FragmentManager fm() {
        return fmProvider.get(context);
    }

    public Fragment fragment() {
        return fm().findFragmentById(R.id.container);
    }

    public void setFragmentUserVisibleHint(boolean isVisibleToUser) {
        Fragment fragment = fragment();
        if (fragment != null) {
            fragment.setUserVisibleHint(isVisibleToUser);
        }
    }

    public boolean isFastDoubleClick() {
        return isFastDoubleClick(500);
    }

    private long lastClickTime;

    public boolean isFastDoubleClick(long duration) {
        long time = System.currentTimeMillis();
        long _duration = time - lastClickTime;
        if (0 < _duration && _duration < duration) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

//  public void setLocale(final Locale locale){
//    UserAccount account = account();
//    Configuration config = context.getResources().getConfiguration();
//    if(locale != null) {
//      config.locale = locale;
//    }else {
//      if(Strings.isEmpty(account.lang)) {
//        config.locale = Locale.getDefault();
//      }else{
//        config.locale = account.locale;
//      }
//    }
//    if(!config.locale.equals(account.locale)){
//      account.locale = config.locale;
//      pref().storageAccount(account);
//    }
//    context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
//  }

    public void navigationToMessageFragment() {
        if (context instanceof MainActivity) {
            fm().popBackStackImmediate(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            MainActivity activity = (MainActivity) getActivity();
            activity.getTabHost().setCurrentTab(0);
        }
    }

    public void clearBackStack(FragmentActivity fragment) {
        int num = fragment.getSupportFragmentManager().getBackStackEntryCount();
        String numString = "++++++++++++++++++++++++++++++++++Fragment回退栈数量：" + num;
        Log.d("Fragment", numString);
//        for (int i = 0; i < num; i++) {
//            FragmentManager.BackStackEntry backstatck = fragment.getSupportFragmentManager().getBackStackEntryAt(i);
//            if(backstatck!=null)
//            Log.d("Fragment", backstatck.getName());
//        }
    }

    public void pushUserMessagesFragment(UsersMessagesFragment fragment) {
        if (fm().getFragments() != null)
            for (Fragment frag : fm().getFragments()) {
                if (frag instanceof UsersMessagesFragment) {
                    ft().remove(frag).commitAllowingStateLoss();
                }
            }
        pushFragment(fragment);
    }

    public Activity getActivity() {
        return ((Activity) context);
    }

//  public void startPush(){
//    UserAccount account = prefController.getAccount();
//    if(Strings.notEmpty(account.uid) && (Strings.isEmpty(account.receivePush) || account.receivePush.equals("yes"))){
//      enablePushService();
//    }else {
//      disablePushService();
//    }
//  }

    public void stopPush() {
        PushManager.getInstance().turnOffPush(context.getApplicationContext());
    }

    public String getBaiduApiKey() {
        return getMetaValue("api_key");
    }

    @SuppressWarnings("ConstantConditions")
    public String getMetaValue(String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Ln.e(e);
        }
        return apiKey;
    }

    public void textViewTopDrawable(final TextView textView, int resId) {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(resId), null, null);
    }

    public void textViewLeftDrawable(final TextView textView, int resId) {
        textView.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(resId), null, null, null);
    }

    public void textViewRightDrawable(final TextView textView, int resId) {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(resId), null);
    }

    public void updateMessagesBarNum(final Collection<ChatGroup> groups) {

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {

                int num = 0;
                for (ChatGroup group : groups) {
                    num += group.unreadNum;
                }
                num += PrfUtils.getMessageCount(context);
                return num;
            }

            @Override
            protected void onPostExecute(Integer num) {
                updateBarNum(0, num);
            }
        }.execute();


    }

    public void updateMessagesBarNum(Context context) {
        new AsyncTask<Void, Void, List<ChatGroup>>() {
            @Override
            protected List<ChatGroup> doInBackground(Void... params) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return null;
                }
                List<ChatGroup> chatGroupList = ChatGroup.findAllbyChat(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()));
                return chatGroupList;
            }

            @Override
            protected void onPostExecute(List<ChatGroup> groups) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                updateMessagesBarNum(groups);
            }
        }.execute();
    }

    @SuppressWarnings("ConstantConditions")
    private void updateBarNum(final int index, final int num) {
        if (context instanceof MainActivity) {
            MainActivity homeActivity = (MainActivity) context;
            if (index == 0)
                NoticeUtils.updateAppNum(context, num);
//            if (Constants.DEBUG){
//                Log.e("TAG_首页Main","num="+num);
//            }
//            homeActivity.updateTabNums(index, num);
        }
    }

    public int getPixels(final float dp) {
        return (int) (dp * density);
    }

    public PreferencesController pref() {
        return prefController;
    }

    public UserAccount account() {
        return pref().getAccount();
    }

    public void upgrade(final boolean manual) {
        new NetSilentAsyncTask<Map>(context) {
            @Override
            protected void onSuccess(Map map) throws Exception {
                if (map != null) {
                    String ver = (String) map.get("verCode");
                    if (Strings.notEmpty(ver)) {
                        String verName = (String) map.get("verName");
                        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString("apiVerCode", ver).putString("apiVerName", verName).commit();
//            UpdateChecker.checkForDialog((FragmentActivity)getActivity(), manual);
                    }
                }
            }

            @Override
            protected Map doInBackground() throws Exception {
                return net().ver();
            }
        }.execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void pwdVerify(final View.OnClickListener listener) {
        final EditText editText = new EditText(getActivity());
        editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setHint(context.getString(R.string.prompt_pass));
        editText.setHeight(getPixels(50));
        editText.setHintTextColor(Color.GRAY);
        int pad = getPixels(8);
        editText.setPadding(pad, pad, pad, pad);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.PickerDialog).setTitle(context.getString(R.string.password_verification)).setView(editText)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.ok), null)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String pass = Strings.toString(editText.getText());
                if (Strings.isEmpty(pass)) {
                    Toast.makeText(getActivity(), context.getString(R.string.prompt_pass), Toast.LENGTH_SHORT).show();
                    return;
                }
                new NetMapAsyncTask<Map>(getActivity()) {
                    @Override
                    protected void success(Map map) throws Exception {
                        dialog.dismiss();
                        imManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                        if (listener != null) {
                            v.setTag(pass);
                            listener.onClick(v);
                        }
                    }

                    @Override
                    protected boolean handlerServerError(Map map) {
                        return super.handlerServerError(map);
                    }

                    @Override
                    protected Map doInBackground() throws Exception {
                        return net().pwdVerify(pass);
                    }
                }.execute();
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                dialog.dismiss();
                getActivity().onBackPressed();
            }
        });
    }

    @Inject
    public Context context;
    @Inject
    ContextScopedProvider<FragmentManager> fmProvider;
    @Inject
    private PreferencesController prefController;
    @Inject
    @Named("density")
    float density;
    @Inject
    InputMethodManager imManager;

}
