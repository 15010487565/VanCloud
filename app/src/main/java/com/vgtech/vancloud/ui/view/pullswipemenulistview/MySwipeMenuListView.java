package com.vgtech.vancloud.ui.view.pullswipemenulistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.vgtech.common.view.swipemenu.SwipeMenuListView;

/**
 * Created by Duke on 2016/8/8.
 */
public class MySwipeMenuListView extends SwipeMenuListView {

    public MySwipeMenuListView(Context context) {
        super(context);
        mGestureDetector = new GestureDetector(context, onGestureListener);
    }

    public MySwipeMenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, onGestureListener);
    }

    public MySwipeMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, onGestureListener);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean b = mGestureDetector.onTouchEvent(ev);
        Log.w("MyLog", "-- " + b + " --");
        return super.onTouchEvent(ev);
    }

    private GestureDetector mGestureDetector;
    OnTouchListener mGestureListener;

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Log.w("MyLog","onFling");
//            float x = e2.getX() - e1.getX();
//            float y = e2.getY() - e1.getY();
//            if (Math.abs(y) >= Math.abs(x)){
//                setParentScrollAble(false);
//                return true;
//            }
//            //当手指触到listview的时候，让父ScrollView交出ontouch权限，也就是让父scrollview 停住不能滚动
//            setParentScrollAble(true);
//            return false;
//        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.w("MyLog", "onScroll");
            if (distanceY != 0 && distanceX != 0) {
            }
            if (Math.abs(distanceY) >= Math.abs(distanceX)) {
                return true;
            }
            //当手指触到listview的时候，让父ScrollView交出ontouch权限，也就是让父scrollview 停住不能滚动
            setParentScrollAble(false);
            return false;
        }
    };

    /**
     * 是否把滚动事件交给父scrollview
     *
     * @param flag
     */
    private void setParentScrollAble(boolean flag) {
        //这里的parentScrollView就是listview外面的那个scrollview
        Log.w("MyLog", "setParentScrollAble -- " + flag);
        getParent().requestDisallowInterceptTouchEvent(!flag);
    }
}
