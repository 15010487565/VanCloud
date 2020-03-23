package com.vgtech.common.api;

/**
 * Created by John on 2015/9/1.
 */
public class Task extends AbsApiData {

    //    必选	类型及范围	说明
//    taskId	true	String	任务id
//    content	true	String	内容
//    plantTime	True	String	计划完成时间
//    finishTime	false	String	实际完成时间
//    notifyType	true	Int	提醒类型，参加config接口获取，0为自定义
//    notifyTIme	false	long	自定义提醒时间
//    state	false	int	状态，0未完成，1完成，2撤销，默认0
//    audio	false	String	语音地址
//    comments	true	Int	评论数量
//    praises	true	Int	点赞数量
//    timestamp	true	long	创建时间戳
//    isPraise	true	boolean	是否已赞，0,否，1是，默认0
    public String taskid;
    public String content;
    public long plantime;
    public long finishtime;
    public int notifytype;
    public String notifytime;
    public String state;//2代表 未完成  1 代表 已完成
    public int comments;
    public int praises;
    public long timestamp;

    /**
     * 是否可以点击详情
     */
    public String iscanviewdetail;

    public int type;//1我执行的，2我发布的，3抄送我的,4下属的任务和我没关系的

    public boolean ispraise;

    public String repealstate;//"2", 代表撤销  1 代表正常使用

    public int resource;//资源类型 1 普通 2 招聘计划
    public String resourceid;//招聘ID
}
