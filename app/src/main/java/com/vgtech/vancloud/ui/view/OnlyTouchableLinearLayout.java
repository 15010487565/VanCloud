package com.vgtech.vancloud.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class OnlyTouchableLinearLayout extends LinearLayout {

	public OnlyTouchableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public OnlyTouchableLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean hasFocusable() {
		return false;
	}
}
