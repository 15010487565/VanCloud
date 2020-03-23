package com.vgtech.common.api;

public class RootData extends AbsApiData {

    public boolean result;
    public int code;
    public String msg;
    public String responce;
    public boolean isSuccess() {
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "RootData{" +
                "result=" + result +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", responce='" + responce + '\'' +
                '}';
    }
}
