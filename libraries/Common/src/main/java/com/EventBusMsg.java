package com;

/**
 * Data:  2019/4/26
 * Auther: xcd
 * Description:
 */
public class EventBusMsg {
    private int code;
    private String message;
    private String oppnId;
    private String weName;
    private boolean isAuthor;
    private String actoin;
    private Class<?> aClassName;

    public Class<?> getaClassName() {
        return aClassName;
    }

    public void setaClassName(Class<?> aClassName) {
        this.aClassName = aClassName;
    }

    public boolean isAuthor() {
        return isAuthor;
    }

    public void setAuthor(boolean author) {
        isAuthor = author;
    }

    public EventBusMsg(){

    }

    public String getOppnId() {
        return oppnId;
    }

    public void setOppnId(String oppnId) {
        this.oppnId = oppnId;
    }

    public String getWeName() {
        return weName;
    }

    public void setWeName(String weName) {
        this.weName = weName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int message) {
        this.code = code;
    }

    public String getActoin() {
        return actoin;
    }

    public void setActoin(String actoin) {
        this.actoin = actoin;
    }
}
