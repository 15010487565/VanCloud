package com.vgtech.vancloud.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuCreator;
import com.vgtech.common.view.swipemenu.SwipeMenuItem;
import com.vgtech.vancloud.R;

/**
 * Created by vic on 2017/2/24.
 */
public class SwipeMenuFactory {
    public static SwipeMenuCreator getSwipeMenuCreator(final Context context) {
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu, int position) {
                if (context == null)
                    return;
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(context);
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xff, 0x00,
                        0x00)));
                // set item width
                openItem.setWidth(Utils.dp2px(context, 70));
                // set item title
                openItem.setTitle(context.getString(R.string.delete));
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);
            }
        };
        return creator;
    }
}
