package com.chenzhanyang.behaviorstatisticslibrary;

import com.chenzhanyang.behaviorstatisticslibrary.handler.BehaviorHandler;

/**
 * Data:  2017/7/10
 * Auther: 陈占洋
 * Description: 行为统计处理
 */

public class BehaviorStatistics {
    /**
     * 默认行为统计处理器
     */
    private BehaviorHandler mDefaultBehaviorHandler;

    private BehaviorStatistics() {
    }

    private static BehaviorStatistics sInstance;

    public static BehaviorStatistics getInstance() {
        if (sInstance == null) {
            synchronized (BehaviorStatistics.class) {
                if (sInstance == null) {
                    sInstance = new BehaviorStatistics();
                }
            }
        }
        return sInstance;
    }

    /**
     * 设置默认的行为统计处理器
     *
     * @param defaultBehaviorHandler 默认行为统计处理器
     */
    public void setDefaultBehaviorHandler(BehaviorHandler defaultBehaviorHandler) {
        if (defaultBehaviorHandler == null) {
            throw new RuntimeException("行为统计处理器不能为空.");
        }
        mDefaultBehaviorHandler = defaultBehaviorHandler;
    }

    /**
     * 开始行为统计
     *
     * @param params
     */
    public void startBehavior(Object params) {
        if (mDefaultBehaviorHandler == null) {
            throw new RuntimeException("默认行为统计处理器为空，请先设置默认处理器，或者调用startBehavior(BehaviorHandler behaviorHandler)");
        }
        mDefaultBehaviorHandler.behaviorHandle(params);
    }

    /**
     * 开始行为统计
     *
     * @param behaviorHandler
     */
    public void startBehavior(BehaviorHandler behaviorHandler, Object params) {
        if (behaviorHandler == null) {
            throw new RuntimeException("行为统计处理器不能为空.");
        }
        behaviorHandler.behaviorHandle(params);
    }
}
