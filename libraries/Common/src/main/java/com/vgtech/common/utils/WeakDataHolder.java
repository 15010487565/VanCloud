package com.vgtech.common.utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Data:  2019/12/27
 * Auther: xcd
 * Description:
 */
public class WeakDataHolder {

    public static final String IMAGEID = "1";
    private static WeakDataHolder instance;

    public static WeakDataHolder getInstance(){
        if(instance == null){
            synchronized (WeakDataHolder.class) {
                if (instance == null) {
                    instance = new WeakDataHolder();
                }
            }
        }
        return instance;
    }

    private Map<String, WeakReference<Object>> map = new HashMap<>();

    /**
     * 数据存储
     * @param id
     * @param object
     */
    public void saveData(String id, Object object) {
        map.put(id, new WeakReference<>(object));
    }

    /**
     * 获取数据
     * @param id
     * @return
     */
    public Object getData(String id) {
        WeakReference<Object> weakReference = map.get(id);
        return weakReference.get();
    }
}
