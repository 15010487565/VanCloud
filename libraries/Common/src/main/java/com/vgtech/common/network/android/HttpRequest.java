package com.vgtech.common.network.android;

import android.text.TextUtils;
import android.util.Log;

import com.PackageUtil;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.vgtech.common.ACache;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.utils.Des3Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by zhangshaofang on 2015/8/7.
 */

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class HttpRequest extends Request<String> {
    private HttpListener mListener;
    private int mCallbackId;
    private NetworkPath mPath;
    private ACache mCache;
    //log显示的每段长度
    int logNum = 2000;
    /**
     * Creates a new request with the given method.
     *
     * @param method        the request {@link Method} to use
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public HttpRequest(int method, String url, HttpListener listener,
                       HttpListener errorListener, int callbackId, NetworkPath path, ACache aCache) {
        super(method, url, errorListener);
        mListener = listener;
        mCallbackId = callbackId;
        mPath = path;
        mCache = aCache;

    }


    @Override
    protected void deliverResponse(String response) {
        //   mListener.onResponse(response);
        if (Constants.DEBUG) {
            if (response.length() > logNum) {
                for (int i = 0; i < response.length(); i += logNum) {
                    if (i + logNum < response.length())
                        Log.e("TAG_成功", "第"+(i/logNum)+"段log===" + response.substring(i, i + logNum));
                    else {
                        Log.e("TAG_成功", "第"+(i/logNum)+"段log===" + response.substring(i, response.length()));
                    }
                }
            } else {
                Log.e("TAG_成功", "result=" + response);
            }
            Log.e("TAG_成功", "path=" + mPath.getPath());
        }
        RootData rootData = null;
        boolean success = false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (mPath.isVantop()) {
                rootData = new RootData();
            } else {
                rootData = JsonDataFactory.getData(jsonObject);
            }
            rootData.setJson(jsonObject);
        } catch (Exception e) {
            rootData = new RootData();
            if (mPath.getType() == NetworkPath.TYPE_JSONARRAY) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    success = true;
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
            rootData.msg = e.getMessage();
            rootData.responce = response;
        } finally {
            if (mCache != null && (rootData.isSuccess() || success) && mPath.isCache()) {
                if (mPath.getType() == NetworkPath.TYPE_JSONARRAY) {
                    mCache.put(mPath.getPath(), response);
                } else {
                    mCache.put(mPath.getPath(), rootData.getJson());
                }
            }

            if (rootData != null && rootData.getJson() != null) {
                try {
                    boolean isencode = rootData.getJson().optBoolean("isencode");
                    if (isencode) {
                        String encrypStr = rootData.getJson().optString("data");
                        String staff_no = PrfUtils.getStaff_no(PackageUtil.getAppCtx());
                        Des3Util des3Util = new Des3Util();
                        des3Util.setSecretKey(staff_no);
                        String data = des3Util.decode(encrypStr);
                        //TODO code msg 需要添加
                        rootData.setJson(new JSONObject("{data:" + data + "}"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mListener.dataLoaded(mCallbackId, mPath, rootData);
        }
    }

    @Override
    public void deliverError(VolleyError error) {

        NetworkResponse response = error.networkResponse;
        RootData rootData = new RootData();
        rootData.msg = TextUtils.isEmpty(error.getMessage()) ? error.getCause() != null ? error.getCause().toString()
                : "" : error.getMessage();
        if (response != null) {
            String data = new String(response.data);
            rootData.msg = data;
            if (Constants.DEBUG) {
                String result = rootData.msg;
                if (result.length() > logNum) {
                    for (int i = 0; i < result.length(); i += logNum) {
                        if (i + logNum < result.length())
                            Log.e("TAG_失败", "第"+(i/logNum)+"段log===" + result.substring(i, i + logNum));
                        else {
                            Log.e("TAG_失败", "第"+(i/logNum)+"段log===" + result.substring(i, result.length()));
                        }
                    }
                } else {
                    Log.e("TAG_失败", "result=" + result);
                }
                Log.e("TAG_失败", "mPath=" + mPath.getPath());
            }
            mListener.dataLoaded(mCallbackId, mPath, rootData);
        }else {
            mListener.onResponse(String.valueOf(mCallbackId));
        }

    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
//            if(response.headers!=null)
//            {
//                String cookie = response.headers.get("Set-Cookie");
//                if(!TextUtils.isEmpty(cookie))
//                {
//                    String ystid = cookie.subSequence(cookie.indexOf("=")+1, cookie.indexOf(";")).toString();
//                    if(mPath!=null&&mPath.getPostValues()!=null)
//                        mPath.getPostValues().put("cookie",ystid);
//                }
//            }
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        Response<String> success = Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        return success;
    }
}
