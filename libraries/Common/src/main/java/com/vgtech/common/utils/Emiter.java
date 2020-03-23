package com.vgtech.common.utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Data:  2018/9/27
 * Auther: 陈占洋
 * Description:发送器
 */

public class Emiter {

    Map<String,WeakReference<Receiver>> mTargets = new HashMap<>();

    private static volatile Emiter sInstance = null;
    private Emiter(){};
    public static Emiter getInstance(){
        if (sInstance == null){
            synchronized (Emiter.class){
                if (sInstance == null){
                    sInstance = new Emiter();
                }
            }
        }
        return sInstance;
    }

    /**
     * 注册观察者
     * @param receiver
     */
    public void registerReceiver(Receiver receiver){
        WeakReference<Receiver> weakObserver = new WeakReference<>(receiver);
        mTargets.put(receiver.getClass().getSimpleName(),weakObserver);
    }

    /**
     * 反注册观察者
     */
    public void unregisterReceiver(Receiver receiver){
        String key = receiver.getClass().getSimpleName();
        mTargets.remove(key);
    }

    /**
     *
     * @param targetClassName
     * @param data
     */
    public <T>void emit(String targetClassName,T data){
        WeakReference<Receiver> weakObserver = mTargets.get(targetClassName);
        if (weakObserver != null && weakObserver.get() != null){
            weakObserver.get().onReceived(data);
        }
    }
}
