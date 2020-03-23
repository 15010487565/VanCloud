package com.vgtech.common.api;

/**
 * Created by app02 on 2015/9/6.
 */
public class Flow extends AbsApiData {

    //    processId	True	Int	流程id
//    leaveInfo	false	JsonArray 请假信息
//    content	true	String	流程内容
//    state	true	int	状态，1同意，2不同意，3待审批
//    processType	true	int	流程类型：1普通审批，2请假审批
//    audio	false	String	语音地址
//    comments	true	Int	评论数量
//    praises	true	Int	点赞数量
//    timestamp	true	long	创建时间戳
    public String processid;
    public String processerid;
    public String leaveinfo;
    public long timestamp;
    public String content;
    public String state;
    public int comments;
    public int praises;
    public int processtype;
    public String audio;
    public String image;
    public String process;
    public boolean ispraise;
    public String repealstate;

    public boolean singleline = true;

    public int resource;//资源类型 1 普通 3招聘计划 4 简历申请
    public String resourceid;//招聘ID/简历ID
    public String resourceinfo;//资源详情 json格式 实例：[{"resume_count": "1","amount": "0.0"}] resume_count 代表简历份数 amount 代表金额

}
