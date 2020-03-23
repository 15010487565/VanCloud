package com.vgtech.common.api;

/**
 * Created by Duke on 2016/1/15.
 */
public class OrderResume extends AbsApiData {

    public long resume_id;//简历编号
    public String name;//姓名
    public String gender;//性别
    public String degree;//学历
    public String photo;//头像
    public String work_city;//期望工作地区
    public String position;//期望从事职业
    public String salary_month;//期望月薪
    public double price;//价格
    public String creator_time;//推送时间
    public String job_type;//工作性质
}
