package com.vgtech.vancloud.ui.module.todo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.vgtech.common.api.AppModule;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.XmlDataAdapter;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.XMLResParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 待办通知
 * Created by Duke on 2016/9/5.
 */
public class ToDoNotificationActivity extends BaseActivity {
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected int getContentView() {
        return R.layout.todo_list;
    }

    public void initView() {
        setTitle(getString(R.string.information_todo));
        initRightTv(getString(R.string.recruit_filtrate));
        viewPager = (ViewPager) findViewById(R.id.vp);
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFrag(ToDoNotificationListFragment.create("pending", "0", 0), getString(R.string.need_handle));
        adapter.addFrag(ToDoNotificationNewListFragment.create("pending", "0", 0), getString(R.string.need_handle));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void clearList() {
            mFragmentList.clear();
            mFragmentTitleList.clear();
        }

    }

    private int dpToPx(int dps) {
        return Math.round(this.getResources().getDisplayMetrics().density * (float) dps);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                showPop(v);
                break;
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                super.onClick(v);
                break;

        }
    }

    private PopupWindow popupWindow;

    public void showPop(final View v) {
        if (popupWindow == null) {


            List<XMLResParser.MenuItem> list = new ArrayList<>();
                XMLResParser.MenuItem menuItem = new XMLResParser.MenuItem();
                menuItem.setLabel(R.string.all);
                menuItem.setIcon(R.mipmap.icon_todo_all);
                menuItem.setId("action_menu_all");
            list.add(menuItem);

            //审批模块
            List<AppModule> appModulesApprove = AppModulePresenter.getApproveModules(this);
            if (appModulesApprove != null && appModulesApprove.size() > 0){
                XMLResParser.MenuItem menuItem1 = new XMLResParser.MenuItem();
                menuItem1.setLabel(R.string.todo_menu_leave);
                menuItem1.setIcon(R.mipmap.icon_todo_leave);
                menuItem1.setId("action_menu_leave");
                list.add(menuItem1);


                XMLResParser.MenuItem menuItem2 = new XMLResParser.MenuItem();
                menuItem2.setLabel(R.string.todo_menu_overtime);
                menuItem2.setIcon(R.mipmap.icon_todo_overtime);
                menuItem2.setId("action_menu_overtime");
                list.add(menuItem2);


                XMLResParser.MenuItem menuItem3 = new XMLResParser.MenuItem();
                menuItem3.setLabel(R.string.change_sign);
                menuItem3.setIcon(R.mipmap.icon_todo_signcard);
                menuItem3.setId("action_menu_signcard");
                list.add(menuItem3);

            }

            XMLResParser.MenuItem[] todoModules = AppModulePresenter.getTodoModules(this, list);

            XmlDataAdapter menuAdapter = new XmlDataAdapter<>(this);
            menuAdapter.add(todoModules);

            View popView = getLayoutInflater().inflate(R.layout.action_pop_layout, null);
            NoScrollListview listView = (NoScrollListview) popView.findViewById(R.id.listview);
            listView.setAdapter(menuAdapter);
            listView.setItemClick(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    XMLResParser.MenuItem menuItem = (XMLResParser.MenuItem) parent.getItemAtPosition(position);
                    String type = "0";
                    Log.e("TAG_筛选","弹窗="+menuItem.getId());
                    switch (menuItem.getId()) {
                        case "action_menu_all":
                            type = "0";
                            break;
                        case "action_menu_leave":
                            type = TypeUtils.APPROVAL_LEAVE;
                            break;
                        case "action_menu_overtime":
                            type = TypeUtils.APPROVAL_OVERTIME;
                            break;
                        case "action_menu_signcard":
                            type = TypeUtils.APPROVAL_SIGNCARD;
                            break;
                        case "calendar":
                            type = TypeUtils.SCHEDULE;
                            break;
                        case "task":
                            type = TypeUtils.TSAK;
                            break;
                        case "work_reportting":
                            type = TypeUtils.WORKREPORT;
                            break;
                        case "flow":
                            type = TypeUtils.APPROVAL_FLOW;
                            break;
                        case "vote"://问卷/
                            type = TypeUtils.VOTE;
                            break;
                        case "entryapprove":
                            type = TypeUtils.ENTRYAPPROVE;
                            break;
                    }
                    viewPager.setCurrentItem(0);
                    ViewPagerAdapter viewPagerAdapter = (ViewPagerAdapter) viewPager.getAdapter();
                    ToDoNotificationNewListFragment fragment = (ToDoNotificationNewListFragment) viewPagerAdapter.getItem(0);
                    fragment.setType(type);
//                    ToDoNotificationListFragment fragment2 = (ToDoNotificationListFragment) viewPagerAdapter.getItem(1);
//                    fragment2.setType(type);

                }
            });
            popupWindow = new PopupWindow(popView, Utils.convertDipOrPx(this, 210),
                    ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
            popupWindow.setFocusable(true);// 使其聚集
            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
            popupWindow.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.abc_popup_background_mtrl_mult));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.update();
        }
        popupWindow.showAsDropDown(v, 0 - Utils.convertDipOrPx(this, 10), 0 - Utils.convertDipOrPx(this, 8));

    }

}
