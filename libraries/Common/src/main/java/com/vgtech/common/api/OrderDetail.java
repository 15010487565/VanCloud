package com.vgtech.common.api;

/**
 * 订单
 * Created by Duke on 2016/1/10.
 */
public class OrderDetail extends AbsApiData {

    public String order_info_id;//传参使用
    public String order_number;//显示使用
    public String amount;//实付金额
    public String order_description;
    public String creator_time;
    public String payment_time;
    public String payment_status;//(pending-待付款,paid-已付款,canceled-已取消)
    public String order_type;
    public String staff_name;
    public String department;
    public String resume_count;
    public String cancel_time;//取消时间
    public String payment_status_name;
    public String payer;

    public boolean option;
    public String url;
    public String order_type_name;

    public String price;//应付金额
}
