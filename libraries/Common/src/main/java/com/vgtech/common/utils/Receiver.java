package com.vgtech.common.utils;

/**
 * Data:  2018/9/27
 * Auther: 陈占洋
 * Description: 接收器
 */

public interface Receiver<T> {

    void onReceived(T data);
}
