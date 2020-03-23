package com.vgtech.common.network;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.network.android.FilePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/8/5.
 */
public class NetworkPath {
    public static final int TYPE_JSONARRAY = 1;
    private String url;
    private Map<String, String> postValues;
    private Object extraData;
    private boolean cache;
    private int type;
    private boolean isVantop;
    private Object tag;
    private Request<String> mRequest;

    public NetworkPath(String url) {
        this.url = url;
    }

    public NetworkPath(String url, Map<String, String> postValues, Context context) {
        this(url, postValues, context, false);
    }

    /**
     * vantop api used
     *
     * @param url
     * @param params
     * @param context
     * @param isVantop always true
     */
    public NetworkPath(String url, Map<String, String> params, Context context, boolean isVantop) {
        this.url = url;
        this.isVantop = isVantop;
        if (!url.contains("/user/login")) {
            if (!TextUtils.isEmpty(PrfUtils.getToken(context))) {
                if (params == null)
                    params = new HashMap<>();
                if (!params.containsKey("token"))
                    params.put("token", PrfUtils.getToken(context));
            }
        }
        this.postValues = params;
        if (isVantop) {
            if (postValues == null)
                postValues = new HashMap<>();
            if (!postValues.containsKey("loginUserCode"))
                postValues.put("loginUserCode", PrfUtils.getStaff_no(context));
            if (!postValues.containsKey("tenant_id"))
                postValues.put("tenant_id", PrfUtils.getTenantId(context));
        }
    }

    public NetworkPath(String url, Map<String, String> postValues, FilePair obj, Context context, boolean isVantop) {
        this(url, postValues, context, isVantop);
        this.extraData = obj;
    }

    public NetworkPath(String url, Map<String, String> postValues, List<FilePair> obj, Context context, boolean isVantop) {
        this(url, postValues, context, isVantop);
        this.extraData = obj;
    }

    public NetworkPath(String url, Map<String, String> postValues, FilePair obj) {
        this.url = url;
        this.postValues = postValues;
        this.extraData = obj;
    }

    public NetworkPath(String url, Map<String, String> postValues, List<FilePair> obj) {
        this.url = url;
        this.postValues = postValues;
        this.extraData = obj;
    }

    public boolean isCache() {

        return cache;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public String getPath() {
        return "NetworkPath{" +
                "url='" + url + '\'' +
                ", postValues=" + postValues +
                '}';
    }

    public boolean isVantop() {
        return isVantop;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getPostValues() {
//        Map<String, String> newPostValues = new HashMap<>();
//        for (Map.Entry<String, String> entry : postValues.entrySet()) {
//            if (entry.getValue()==null){
//                Log.e("TAG_成功", "null=" + entry.getKey());
//            }else if (entry.getValue().equals("")){
//                Log.e("TAG_成功", "空字符=" + entry.getKey());
//            }
//            newPostValues.put(entry.getKey(), entry.getValue()==null ? "11" : entry.getValue());
//
//        }
        return postValues;
    }

    public void setPostValues(Map<String, String> postValues) {
        this.postValues = postValues;
    }

    public Object getExtraData() {
        return extraData;
    }

    public void setExtraData(Object extraData) {
        this.extraData = extraData;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void setRequest(Request<String> request) {
        mRequest = request;
    }

    public Request<String> getRequest() {
        return mRequest;
    }
}
