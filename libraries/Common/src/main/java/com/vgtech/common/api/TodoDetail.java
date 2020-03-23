package com.vgtech.common.api;

/**
 * Created by Duke on 2015/9/28.
 */
public class TodoDetail extends AbsApiData {

    public int count;
    public int type;//日程--9,任务--11,流程--2,工作汇报--7
    public String jsontext;


    public TodoDetail(int count, int type, String jsontext) {
        this.count = count;
        this.type = type;
        this.jsontext = jsontext;
    }
}
