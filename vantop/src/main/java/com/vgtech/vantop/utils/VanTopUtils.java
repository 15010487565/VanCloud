package com.vgtech.vantop.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.vgtech.common.network.ApiUtils;
import com.vgtech.vantop.R;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;


/**
 * Created by vic on 2016/7/13.
 */
public class VanTopUtils {
    /**
     *
     * @param context
     * @param url vantop接口地址
     * @return 中转之后的地址
     */
    public static String generatorUrl(Context context, String url) {
        return ApiUtils.generatorUrl(context, "vantopapp/" + url, false);
    }

    /**
     *
     * @param context
     * @param url vantop 图片原地址
     * @return 中转之后的地址
     */
    public static String generatorImageUrl(Context context,String url)
    {
        return ApiUtils.generatorUrl(context, "vantopapp/images/" + url, false);
    }
    /**
     * 获取审批状态
     *
     * @param status
     * @return
     */
    public static int getStatusResId(final String status) {
        if ("0".equals(status)) {
            return R.string.vantop_approving;
        } else if ("2".equals(status)) {
            return R.string.vantop_refuse;
        } else if ("1".equals(status)) {
            return R.string.vantop_agree;
        }
        return R.string.vantop_approving;
    }

//    public static int getApproveStatusResId(final String status) {
//        if("0".equals(status)) {
//            return R.string.vantop_approving;
//        }else if("2".equals(status)) {
//            return R.string.vantop_refuse;
//        }else if("1".equals(status)) {
//            return R.string.vantop_agree;
//        }
//        return R.string.vantop_approving;
//    }

    public static String arabToIndex(int n) {
        switch (n) {
            case 1:
                return 1 + "st";
            case 2:
                return 2 + "nd";
            case 3:
                return 3 + "rd";
            default:
                return n + "th";
        }
    }


    public static void enterVantopUserInfoByUserId(final Context context, final String userId, View userIv) {
        userIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VantopUserInfoActivity.class);
                intent.putExtra(VantopUserInfoActivity.BUNDLE_USERID, userId);
                context.startActivity(intent);
            }
        });

    }


    public static void enterVantopUserInfoBystaffNo(final Context context, final String staffNo, View userIv) {

        userIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VantopUserInfoActivity.class);
                intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, staffNo);
                context.startActivity(intent);
            }
        });
    }
}
