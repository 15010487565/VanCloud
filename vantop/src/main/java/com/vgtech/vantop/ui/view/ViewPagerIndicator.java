package com.vgtech.vantop.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.vgtech.vantop.R;

import java.util.List;

/***
 * viewpager 导航
 * create by scott
 */
public class ViewPagerIndicator extends LinearLayout {

	private int mTrangleHeight;

	private int mTrangleWidth;

	private int mInitTranglePosition;

	private int mTranglePosition;

	private Path mPath = null;

	private Paint mPaint = null;

	private final String TAG = "ViewPagerIndicator";

	private static final float RADIO_TRANGLE_WIDTH = 1 / 8F;
	private final int DIMESSION_TRANGLE_MAX_WIDTH = (int) ( (getScreenWidth() / 3) * RADIO_TRANGLE_WIDTH);

	private static final int COUNT_DEFAULT_TAB = 4;

	private int mTabVisisbleCount = COUNT_DEFAULT_TAB;
	
	private ViewPager mPager;
	
	//默认状态下标题字体颜色
	private static final int COLOR_DEFAULT_NORMAL = 0x77ffffff;
	//高亮
	private static final int COLOR_DEFAULT_HIGHLIGHT = 0xffffffff;
	
	private int mPosition = 0;

	//////////////////////////
	private int mLineHeight = 7;
	private int mHighLightColor = COLOR_DEFAULT_HIGHLIGHT;
	private int mNormalColor = COLOR_DEFAULT_NORMAL;
	private int mLineColor = COLOR_DEFAULT_HIGHLIGHT;

	public void setLineColor(int color) {
		mLineColor = color;
	}

	public void setHighLightColor(int color) {
		mHighLightColor = color;
	}
	public void setNormalColor(int color) {
		mNormalColor = color;
	}

	public ViewPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 获取显示的tab数
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);

		mTabVisisbleCount = array.getInt(R.styleable.ViewPagerIndicator_visisble_tab_count, COUNT_DEFAULT_TAB);
		mLineColor = array.getColor(R.styleable.ViewPagerIndicator_indicator_color,COLOR_DEFAULT_HIGHLIGHT);
		mNormalColor = array.getColor(R.styleable.ViewPagerIndicator_normal_color,COLOR_DEFAULT_NORMAL);
		mHighLightColor = array.getColor(R.styleable.ViewPagerIndicator_highlight_color,COLOR_DEFAULT_HIGHLIGHT);
		mLineHeight = (int) array.getDimension(R.styleable.ViewPagerIndicator_indicator_height,mLineHeight);
		array.recycle();

		mPaint = new Paint();
		mPaint.setStrokeWidth(5);
		mPaint.setColor(mLineColor);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		mPaint.setPathEffect(new CornerPathEffect(3));
	}

	public ViewPagerIndicator(Context context) {
		this(context, null);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();

		canvas.translate(mInitTranglePosition + mTranglePosition, getHeight());
		canvas.drawPath(mPath, mPaint);

		canvas.restore();
		super.dispatchDraw(canvas);
	}

	/*private int getMaxSize() {

		int index = 0;
		if(mTitles != null) {
			int maxLength = mTitles.get(0).length();
			for(int i = 1; i < mTitles.size(); i++) {

				if(mTitles.get(i).length() > maxLength) {
					maxLength = mTitles.get(i).length();
					index = i;
				}
			}
		}
		return index;
	}

	private float measureLineWidth(int index) {
		if(mTitles != null) {

			String content = mTitles.get(index);
			return mPaint.measureText(content);
		}
		return -1;
	}*/
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		mTrangleWidth = (int) (w / mTabVisisbleCount * RADIO_TRANGLE_WIDTH);
		//超出最大尺寸则为最大尺寸
		//mTrangleWidth = Math.min(mTrangleWidth, DIMESSION_TRANGLE_MAX_WIDTH);
		mTrangleWidth = (w / mTabVisisbleCount * 3)/5;
		mInitTranglePosition = w / mTabVisisbleCount / 2 - mTrangleWidth / 2;
		initTrangle();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * 初始化三角形
	 * 
	 * @param
	 * @param
	 */
	private void initTrangle() {
		mTrangleHeight = mTrangleWidth / 2;

		mPath = new Path();
		mPath.moveTo(0, 0);
		mPath.lineTo(mTrangleWidth, 0);
		//mPath.lineTo(mTrangleWidth / 2, -mTrangleHeight);
		mPath.lineTo(mTrangleWidth,-mLineHeight);
		mPath.lineTo(0,-mLineHeight);
		mPath.close();
	}

	
	//由布局加载item
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		int cCount = getChildCount();
		// 设置每个view的宽度
		for (int i = 0; i < cCount; i++) {
			View v = getChildAt(i);
			LayoutParams lp = (LayoutParams) v.getLayoutParams();
			lp.weight = 0;

			lp.width = getScreenWidth() / mTabVisisbleCount;
			v.setLayoutParams(lp);
		}
		
		//设置点击事件
		setItemClickListenner();
	}

	/***
	 * 获取屏幕宽度
	 * 
	 * @return 屏幕宽度
	 */
	private int getScreenWidth() {

		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	public void scroll(int position, float offset) {
		int tabWidth = getWidth() / mTabVisisbleCount;
		mTranglePosition = (int) (tabWidth * (offset + position));

		//
		if (mTabVisisbleCount > 1) {
			if (position >= (mTabVisisbleCount - 2) && offset > 0 && getChildCount() > mTabVisisbleCount) {
				this.scrollTo((position - (mTabVisisbleCount - 2)) * tabWidth + (int) (tabWidth * offset), 0);
			}
		} else {
			this.scrollTo(position * tabWidth + (int) (offset * tabWidth), 0);
		}
		invalidate();
	}

	private List<String> mTitles;

	/***
	 * 动态添加item
	 * 
	 * @param titles
	 */
	public void setTabItemTitles(List<String> titles) {
		if (titles != null && titles.size() > 0) {
			setTabVisisbleCount(titles.size());
			mTitles = titles;
			removeAllViews();
			for (String title : mTitles) {
				addView(generateView(title));
			}
			
			//初始化高亮
			setTextViewHighLight(0);
			//设置点击事件
			setItemClickListenner();
		}
	}
	
	/***
	 * 根据title生成item
	 * 
	 * @param title
	 * @return
	 */
	private View generateView(String title) {

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		lp.width = getScreenWidth() / mTabVisisbleCount;
		
		TextView tv = new TextView(getContext());
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(COLOR_DEFAULT_NORMAL);
		tv.setGravity(Gravity.CENTER);
		tv.setLayoutParams(lp);
		tv.setText(title);
		return tv;
	}
	
	/***
	 * 设置可见的tab数量
	 * @param count
	 */
	public void setTabVisisbleCount(int count) {
		mTabVisisbleCount = count;
	}
	
	
	/***
	 * 设置关联的viewpager
	 * @param vp
	 */
	@SuppressWarnings("deprecation")
	public void setViewPager(ViewPager vp, int pos) {
		mPager = vp;
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pos) {
				if(mListener != null) {
					mListener.onPageSelected(pos);
				}
				
				setTextViewHighLight(pos);
			}
			
			@Override
			public void onPageScrolled(int position, float offset, int offsetPixels) {
				scroll(position, offset);
				
				if(mListener != null) {
					mListener.onPageScrolled(position, offset, offsetPixels);
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
				if(mListener != null) {
					mListener.onPageScrollStateChanged(arg0);
				}
			}
		});
		
		mPosition = pos;
		mPager.setCurrentItem(pos);
		setTextViewHighLight(pos);
	}
	
	private OnPageChangeListener mListener;
	
	/***
	 * 设置viewpager滚动的监听
	 * @param listener
	 */
	public void setOnPageChangeListenner(OnPageChangeListener listener) {
		mListener = listener;
	}
	
	/***
	 * 设置当前的textView为高亮
	 * @param pos
	 */
	private void setTextViewHighLight(int pos) {
		View v = getChildAt(pos);
		resetTextViewColor();
		
		if(v instanceof TextView) {
			((TextView) v).setTextColor(mHighLightColor);
		} 
	}
	
	/***
	 * 重置所有的textview的颜色
	 */
	private void resetTextViewColor() {
		int cCount = getChildCount();
		
		for(int i = 0 ; i < cCount; i++) {
			
			View v = getChildAt(i);
			if(v instanceof TextView) {
				((TextView) v).setTextColor(mNormalColor);
			} 
		}
	}
	
	/***
	 * 设置title点击事件
	 */
	private void setItemClickListenner() {
		int cCount = getChildCount();
		
		for(int i = 0; i < cCount; i++) {
			
			View v = getChildAt(i);
			
			final int j = i;
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mPager.setCurrentItem(j);
				}
			});
		}
	}
	}
