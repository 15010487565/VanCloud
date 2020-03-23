package com.vgtech.common.api;

/**
 * Created by brook on 2015/10/23.
 */
public class ChaildCompany extends AbsApiData{

    public String name;        //子公司名称
    public String sub_company_id;  //子公司id
    public String logo;        //子公司logo

    public String address;     //子公司地址
    public String corporation; //子公司法人
    public String port;        //子公司营业执照
    public String qrcode;      //子公司二维码地址
}