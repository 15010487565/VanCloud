package com.vgtech.common.network.android;

import com.android.volley.Response;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;

/**
 * Created by zhangshaofang on 2015/8/5.
 */
public interface HttpListener<T> extends Response.Listener<T>, Response.ErrorListener {

    public void dataLoaded(int callbackId,NetworkPath path,RootData rootData);
}
