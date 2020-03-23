package com.vgtech.vancloud.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vgtech.common.Constants;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.ViewPager_GV_ItemAdapter;
import com.vgtech.vancloud.ui.adapter.ViewPager_GridView_Adapter;
import com.vgtech.vancloud.utils.XMLResParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by code on 2016/9/19.
 * 自定义可左右滑动的gridview
 */

public class GridViewGallery extends LinearLayout {

    private Context context;
    /** 保存实体对象链表 */
    private List<XMLResParser.AppMenu> list;
    private ViewPager viewPager;
    private LinearLayout ll_dot;
    private ImageView[] dots;
    /** ViewPager当前页 */
    private int currentIndex;
    /** ViewPager页数 */
    private int viewPager_size;
    /** 默认一页6个item */
    private int pageItemCount = 6;

    public ClickTypeListener clickTypeListener;

    public void setClickTypeListener(ClickTypeListener clickTypeListener) {
        this.clickTypeListener = clickTypeListener;
    }

    /** 保存每个页面的GridView视图 */
    private List<View> list_Views;
    public static final String TAG = "GridViewGallery";

    public GridViewGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.list = null;
        initView();
    }


    @SuppressWarnings("unchecked")
    public GridViewGallery(Context context, List<?> list) {
        super(context);
        this.context = context;
        this.list = (List<XMLResParser.AppMenu>) list;
        if (Constants.DEBUG){
            Log.e("TAG_无π币","list="+list.toString());
        }
        initView();
        initDots();
        setAdapter();
    }

    private void setAdapter() {
        list_Views = new ArrayList<View>();
        for (int i = 0; i < viewPager_size; i++) {
            list_Views.add(getViewPagerItem(i));
        }
        viewPager.setAdapter(new ViewPager_GridView_Adapter(list_Views));
    }

    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.channel_activity, null);
        viewPager = (ViewPager) view.findViewById(R.id.vPager);
        ll_dot = (LinearLayout) view.findViewById(R.id.ll_channel_dots);
        addView(view);
    }

    // 初始化底部小圆点
    private void initDots() {
        pageItemCount = 6;  //每一页可装item
        if (list == null || list.size() <= 0) {
            return;
        }
        if (list.size()%6 == 0) {
            viewPager_size = list.size() / pageItemCount;
        } else {
            viewPager_size = list.size() / pageItemCount + 1;
        }

        if (0 < viewPager_size) {
            ll_dot.removeAllViews();
            if (1 == viewPager_size) {
                ll_dot.setVisibility(View.GONE);
            } else if (1 < viewPager_size) {
                ll_dot.setVisibility(View.VISIBLE);
                for (int j = 0; j < viewPager_size; j++) {
                    ImageView image = new ImageView(context);
                    LayoutParams params = new LayoutParams(40, 40);  //dot的宽高
                    params.setMargins(3, 0, 3, 0);
                    image.setBackgroundResource(R.drawable.dot_unselected);
                    ll_dot.addView(image, params);
                }
            }
        }
        if (viewPager_size != 1) {
            dots = new ImageView[viewPager_size];
            for (int i = 0; i < viewPager_size; i++) {
                //从布局中填充dots数组
                dots[i] = (ImageView) ll_dot.getChildAt(i);
                //dots[i].setEnabled(true);
                //dots[i].setTag(i);
            }
            currentIndex = 0;  //当前页
            //dots[currentIndex].setEnabled(false);
            dots[currentIndex].setBackgroundResource(R.drawable.dot_selected);
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int arg0) {
                    setCurDot(arg0);
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {}

                @Override
                public void onPageScrollStateChanged(int arg0) {}
            });
        }
    }

    /** 当前底部小圆点 */
    private void setCurDot(int positon) {
        if (positon < 0 || positon > viewPager_size - 1 || currentIndex == positon) {
            return;
        }
        for(int i=0;i<dots.length;i++){
            dots[i].setBackgroundResource(R.drawable.dot_unselected);
        }
        dots[positon].setBackgroundResource(R.drawable.dot_selected);
        currentIndex = positon;
    }

    //ViewPager中每个页面的GridView布局
    private View getViewPagerItem(int index) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.channel_viewpage_gridview, null);
        GridView gridView = (GridView) layout.findViewById(R.id.vp_gv);
        gridView.setNumColumns(3);

        //每个页面GridView的Adpter
        ViewPager_GV_ItemAdapter adapter = new ViewPager_GV_ItemAdapter(context, list, index, pageItemCount);

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (clickTypeListener != null) {
                    clickTypeListener.typeAction(parent, position);
                }
            }
        });
        return gridView;
    }

    public interface ClickTypeListener {
        void typeAction(AdapterView<?> parent, int position);
    }
}
