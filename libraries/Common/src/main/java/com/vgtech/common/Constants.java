package com.vgtech.common;


/**
 * Created by zhangshaofang on 2015/8/3.
 */
public interface Constants {

    boolean DEBUG = true;

    String PACKAGE_NAME = "com.vgtech.vancloud";
    String COMMON_PACKAGE_NAME = "com.vgtech.common";

    String SERVICE_USERID = "SERVICE_USERID";
    String SERVICE_NAME = "SERVICE_NAME";

    int XMPP_GROUP_USER_MAX = 200;

    int RESPONCE_CODE_UNLOGIN = 1001;

    int RESPONCE_CODE_MULLOGIN = 1003;
    int RESPONCE_CODE_TOKEN_EXPIRED = 1004;

    int TEXT_MAX_VALUE = 4000; //文本输入限制 新建

    int LEADER_SEARCH_CODE = 39;
    int REPORT_FORM_CODE = 41;
    int INTEGRAL_CODE = 42;
    int PI_COIN_CODE = 43;
    int TAX_CODE = 44;//附加扣除
    int ENTRYAPPROVE_CODE = 45;//入职审批
    int BGDIAOCHA_CODE = 46;//背景调查

    String SHARE_SUCCESS = PACKAGE_NAME + "SHARE_SUCCESS";
    String SHARE_CANCLE = PACKAGE_NAME + "SHARE_CANCLE";
    String SHARE_ERROR = PACKAGE_NAME + "SHARE_ERROR";

    int REFRESH = 0;

    String TYPE = "conduct";
    int FINISH = 1;//完成
    int CANCLE = 3;//撤銷
    int AGREE = 1;//同意
    int UNAGREE = 2;//不同意
}
