/**
 *
 */
package com.vgtech.common.api;

public class Error extends AbsApiData {
    public static final int ERROR_UNKNOW = 400;
    public int code;
    public String msg;
    public String desc;
}
