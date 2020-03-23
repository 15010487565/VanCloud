package com.vgtech.common.utils;

/**
 * Created by Duke on 2016/9/20.
 */

public interface TypeUtils {

    //业务类型
    //11-任务，9-日程，2-流程，5-帮帮，3-分享，1-公告，7-工作汇报,69-系统通知

    String TSAK = "11";
    String SCHEDULE = "9";
    String APPROVAL = "2";
    String APPROVAL_OVERTIME = "36";
    String APPROVAL_SIGNCARD = "34";
    String APPROVAL_LEAVE = "35";
    String APPROVAL_FLOW = "37";//审批流程
    String WORKREPORT = "7";
    String SYSTEMNOTIFICATION = "69";
    String NOTICE = "1";
    String MYNOTICE = "105";
    String SHARE = "3";
    String HELP = "5";
    String BACKGROUNDINVESTIGATION = "31";//背景调查
    String VANTOPSIGNCARD = "34";//vantop签卡
    String VANTOPLEAVE = "35";//vantop-休假
    String VANTOPOVERTIME = "36";//vantop-加班
    String MEETING = "28";//会议预约创建 修改 取消

    String JOINCOMPANY = "102";//员工申请

    String NEWEMPLOYEE = "103";//新员工加入

    String VOTE = "39";//问卷
    String ENTRYAPPROVE = "38";//入职审批

    //处理类型
    String OPERATION_COMMENT = "comment";//回复
    String OPERATION_CREATE = "create";//创建
}
