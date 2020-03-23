package com.vgtech.common.listener;

import com.vgtech.common.network.NetworkManager;

/**
 * Created by zhangshaofang on 2016/3/14.
 */
public interface ApplicationProxy {
    public String getChannelId();
    public NetworkManager getNetworkManager();
    public void clear();
}
