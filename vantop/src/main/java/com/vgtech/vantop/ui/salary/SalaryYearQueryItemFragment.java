package com.vgtech.vantop.ui.salary;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.common.ui.BaseFragment;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.SalaryYearlyReportData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shilec on 2016/9/13.
 * 创建一个俩列13行的表格
 */
public class SalaryYearQueryItemFragment extends BaseFragment {

    public static final String BUNDLE_DATAS = "datas";
    private ArrayList<SalaryYearlyReportData> mDatas;
    private final String TAG = "SalaryYear";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatas = (ArrayList<SalaryYearlyReportData>) getArguments().getSerializable(BUNDLE_DATAS);
        if(mDatas == null) {
            mDatas = new ArrayList<>();
        }
        Log.i(TAG,"mDatas===>" + mDatas);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.salary_years_item_fragment;
    }

    @Override
    protected void initView(View view) {

        showLoadingDialog(getActivity(),"");
        LinearLayout lcontainer = (LinearLayout) view;
        //添加两列
        LinearLayout l1 = generateVerLinearLayout();
        LinearLayout l2 = generateVerLinearLayout();
        View verLine = generateVerLine();
        //以垂直分割线分开
        lcontainer.addView(l1);
        lcontainer.addView(verLine);
        lcontainer.addView(l2);

        List<LinearLayout> llist = new ArrayList<>();
        llist.add(l1);
        llist.add(l2);

        //数据长度为2
        for (int j = 0; j < mDatas.size(); j++) {
            SalaryYearlyReportData data = mDatas.get(j);
            LinearLayout l = llist.get(j);

            TextView tvReport = generateItemView();
            tvReport.setText(data.reportName);
            //添加标题
            l.addView(tvReport);
            //每个content以水平分割线分开
            l.addView(generateHorLine());
            for (int i = 0; i < data.value.size(); i++) {

                Log.i(TAG,"createTextView");
                String content = data.value.get(i);
                TextView tv = generateItemView();
                //添加内容
                tv.setText(content);
                l.addView(tv);

                if (i != data.value.size() - 1)
                    l.addView(generateHorLine());
            }
        }
        dismisLoadingDialog();
    }

    /***
     * 水平分割线
     * @return
     */
    private View generateHorLine() {
        View v = new View(getActivity());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getDpValue(1));
        v.setBackgroundColor(getResources().getColor(R.color.line_color));
        v.setLayoutParams(lp);
        return v;
    }

    /***
     * 垂直分割线
     * @return
     */
    private View generateVerLine() {
        View v = new View(getActivity());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams((int) getDpValue(1),
                ViewGroup.LayoutParams.MATCH_PARENT);
        v.setLayoutParams(lp);
        v.setBackgroundColor(getResources().getColor(R.color.line_color));
        return v;
    }

    /***
     * 垂直LinearLayout
     * @return
     */
    private LinearLayout generateVerLinearLayout() {

        LinearLayout ll = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(lp);
        return ll;
    }

    /***
     * android:layout_width="match_parent"
     * android:layout_height="0dp"
     * android:layout_weight="1"
     * android:gravity="center"
     * android:paddingBottom="5dp"
     * android:paddingTop="5dp"
     * android:text="小计"
     * android:textColor="@color/black"
     * android:textSize="15sp" />
     *  显示内容的TextView
     * @return
     */
    private TextView generateItemView() {
        TextView tv = new TextView(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int)getDpValue(0));
        lp.weight = 1;
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, (int) getDpValue(5), 0, (int) getDpValue(5));
        tv.setTextColor(getResources().getColor(R.color.black));
        tv.setTextSize(15);
        return tv;
    }

    private float getDpValue(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private float getSpValue(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
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
}
