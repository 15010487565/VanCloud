package com.vgtech.common.image;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vgtech.common.R;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;


public class PhotoActivity extends BaseActivity {

    private ArrayList<View> listViews = null;
    private ViewPager pager;
    private MyPageAdapter adapter;
    private int count;

    public List<String> drr = new ArrayList<String>();
    public List<String> del = new ArrayList<String>();

    RelativeLayout photo_relativeLayout;
    private ArrayList<Integer> removeList = new ArrayList<>();

    @Override
    protected int getContentView() {
        return R.layout.activity_photo;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
        photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
        photo_relativeLayout.setBackgroundColor(0x70000000);

        for (int i = 0; i < Bimp.drr.size(); i++) {
            drr.add(Bimp.drr.get(i));
        }

        Button photo_bt_exit = (Button) findViewById(R.id.photo_bt_exit);
        photo_bt_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                finish();
            }
        });
        Button photo_bt_del = (Button) findViewById(R.id.photo_bt_del);
        photo_bt_del.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (listViews.size() == 1) {
                    View view = listViews.remove(count);
                    removeList.add((Integer) view.getTag());
                    Bimp.drr.clear();
//					FileUtils.deleteDir();
                    Intent intent = new Intent();
                    intent.putIntegerArrayListExtra("remove", removeList);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    String newStr = drr.get(count).substring(
                            drr.get(count).lastIndexOf("/") + 1,
                            drr.get(count).lastIndexOf("."));
                    drr.remove(count);
                    del.add(newStr);
                    pager.removeAllViews();
                    View view = listViews.remove(count);
                    removeList.add((Integer) view.getTag());
                    adapter.setListViews(listViews);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        Button photo_bt_enter = (Button) findViewById(R.id.photo_bt_enter);
        photo_bt_enter.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Bimp.drr = drr;
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra("remove", removeList);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setOnPageChangeListener(pageChangeListener);
        for (int i = 0; i < drr.size(); i++) {
            initListViews(i);//
        }

        adapter = new MyPageAdapter(listViews);// 构造adapter
        pager.setAdapter(adapter);// 设置适配器
        Intent intent = getIntent();
        int id = intent.getIntExtra("ID", 0);
        pager.setCurrentItem(id);
    }
    private void initListViews(int i) {
        if (listViews == null)
            listViews = new ArrayList<View>();
        SimpleDraweeView img = new SimpleDraweeView(this);// 构造textView对象
        img.setBackgroundColor(0xff000000);
        String path = Bimp.drr.get(i);
        if (ImageInfo.isLocal(path)) {
            ImageLoader.getInstance().displayImage("file://"+path,img);
        } else {
            ImageOptions.setImage(img,path);
        }
        img.setTag(i);
        img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        listViews.add(img);// 添加view
    }
    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {// 页面选择响应函数
            count = arg0;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {// 滑动中。。。

        }

        public void onPageScrollStateChanged(int arg0) {// 滑动状态改变

        }
    };

    class MyPageAdapter extends PagerAdapter {

        private ArrayList<View> listViews;// content

        private int size;// 页数

        public MyPageAdapter(ArrayList<View> listViews) {// 构造函数
            // 初始化viewpager的时候给的一个页面
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public void setListViews(ArrayList<View> listViews) {// 自己写的一个方法用来添加数据
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public int getCount() {// 返回数量
            return size;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {// 销毁view对象
            ((ViewPager) arg0).removeView(listViews.get(arg1 % size));
        }

        public void finishUpdate(View arg0) {
        }

        public Object instantiateItem(View arg0, int arg1) {// 返回view对象
            try {
                ((ViewPager) arg0).addView(listViews.get(arg1 % size), 0);

            } catch (Exception e) {
            }
            return listViews.get(arg1 % size);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }
}
