package com.vgtech.vancloud.ui.web;

/**
 * Data:  2017/11/29
 * Auther: 陈占洋
 * Description:
 */

public class HttpException {

    public int mCode;
    public String mExmsg;
    public String msg;

    public HttpException(int code, String exmsg) {
        this.mCode = code;
        this.mExmsg = exmsg;
    }

    public HttpException(int code, String exmsg,String msg) {
        this.mCode = code;
        this.mExmsg = exmsg;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "code = " + mCode + " exmsg = " + mExmsg;
    }
}
