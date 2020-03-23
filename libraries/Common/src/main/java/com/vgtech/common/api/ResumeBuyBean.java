package com.vgtech.common.api;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by code on 2015/12/25.
 */
public class ResumeBuyBean extends AbsApiData implements Serializable {

    public String resume_id;
    public String photo;
    public String name;
    public String gender;
    public String degree;
    public String position;
    public String work_city;
    public String salary_month;
    public long creator_time;
    public String price;
    public boolean favorite_status;
    public String resume_status;
    public boolean remove_status;
    public String resource_id;
    public String apply_status;


    public ResumeBuyBean() {

    }

    public ResumeBuyBean(OrderResume orderResume) {

        this.resume_id = orderResume.resume_id + "";
        this.photo = orderResume.photo;
        this.name = orderResume.name;
        this.gender = orderResume.gender;
        this.degree = orderResume.degree;
        this.position = orderResume.position;
        this.work_city = orderResume.work_city;
        this.salary_month = orderResume.salary_month;
        if (TextUtils.isEmpty(orderResume.creator_time)) {
            this.creator_time = 0;
        } else {

        }
        this.price = orderResume.price + "";

    }

}
