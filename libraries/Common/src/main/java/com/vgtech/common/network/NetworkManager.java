package com.vgtech.common.network;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.vgtech.common.ACache;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.network.android.HttpRequest;
import com.vgtech.common.network.android.MultipartRequest;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by zhangshaofang on 2015/8/5.
 */
public class NetworkManager {
    private RequestQueue mRequestQueue;
    private ApiUtils mApiUtils;
    private ACache mAcache;

    public NetworkManager(RequestQueue requestQueue, ApiUtils apiUtils, ACache aCache) {
        mRequestQueue = requestQueue;
        mApiUtils = apiUtils;
        mAcache = aCache;
    }

    public ApiUtils getApiUtils() {
        return mApiUtils;
    }

    public ACache getAcache() {
        return mAcache;
    }

    public void load(int callbackId, final NetworkPath path, HttpListener<String> httpListener, boolean cache) {
        path.setCache(cache);
        load(callbackId, path, httpListener);
    }

    public JSONObject getCache(NetworkPath path) {
        String oriUrl = path.getUrl();
        final String url = mApiUtils.appendSignInfo(oriUrl);
        path.setUrl(url);
        JSONObject jsonObject = mAcache.getAsJSONObject(path.getPath());
        path.setUrl(oriUrl);
        return jsonObject;
    }

    public void load(int callbackId, final NetworkPath path, HttpListener<String> httpListener) {
        final String url = mApiUtils.appendSignInfo(path.getUrl());
        path.setUrl(url);
        if (path.isCache()) {
            if (path.getType() == NetworkPath.TYPE_JSONARRAY) {
                String response = mAcache.getAsString(path.getPath());
                if (!TextUtils.isEmpty(response)) {
                    RootData rootData = new RootData();
                    rootData.responce = response;
                    httpListener.dataLoaded(callbackId, path, rootData);
                }
            } else {
                JSONObject jsonObject = mAcache.getAsJSONObject(path.getPath());
                if (jsonObject != null) {
                    RootData rootData = JsonDataFactory.getData(jsonObject);
                    rootData.setJson(jsonObject);
                    httpListener.dataLoaded(callbackId, path, rootData);
                }
            }

        }

        Object extraData = path.getExtraData();
        Request<String> request = null;
        if (extraData == null) {
            int method = 0;
            if (path.getPostValues() == null) {
                method = Request.Method.GET;
            } else {
                method = Request.Method.POST;
            }
            request = new HttpRequest(method,
                    url, httpListener, httpListener, callbackId, path, mAcache) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> postValues = path.getPostValues();
                    Log.e("TAG_成功", "getParams=" + postValues.toString());
                    return postValues;
               }

                @Override
                public RetryPolicy getRetryPolicy() {
                    RetryPolicy retryPolicy = new DefaultRetryPolicy(60 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    return retryPolicy;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers= mApiUtils.getSignParams();
//                    Log.e("TAG_请求头","HttpRequest="+headers.toString());
                    return headers;
                }

                @Override
                protected String getParamsEncoding() {
                    return "UTF-8";
                }
            };

        } else {
            request = new MultipartRequest(url, httpListener, httpListener, extraData, callbackId, path) {
                @Override
                public RetryPolicy getRetryPolicy() {
                    RetryPolicy retryPolicy = new DefaultRetryPolicy(60 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    return retryPolicy;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> signParams = mApiUtils.getSignParams();
//                    Log.e("TAG_请求头","MultipartRequest="+signParams.toString());
                    return signParams;
                }
                @Override
                protected String getParamsEncoding() {
                    return "UTF-8";
                }
            };
        }
        request.setRetryPolicy(new DefaultRetryPolicy(
                60 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(httpListener);
        Request<String> stringRequest = mRequestQueue.add(request);
        path.setRequest(stringRequest);
    }

    public void addRequest(Request request) {
        mRequestQueue.add(request);
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public void cancle(Object obj) {
        mRequestQueue.cancelAll(obj);
    }

}
