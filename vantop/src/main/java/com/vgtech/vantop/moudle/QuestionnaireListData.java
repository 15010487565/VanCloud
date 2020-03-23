package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

import java.io.Serializable;

/**
 * Created by shilec on 2016/9/12.
 */
public class QuestionnaireListData extends AbsApiData implements Serializable{

    public String endTime;
    public String startTime;
    public String status;
    public String titleId;
    public String url;
    public String url311;//新地址从 url311 里取
    public String title;
}
