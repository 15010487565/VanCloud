package com.vgtech.common.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.upload.FilePart;
import com.vgtech.common.network.upload.MultipartEntity;
import com.vgtech.common.network.upload.Part;
import com.vgtech.common.network.upload.StringPart;

import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/8/6.
 */
public class MultipartRequest extends Request<String> {

    private MultipartEntity entity = null;

    private final Response.Listener<String> mListener;

    private Map<String, String> mParams;
    private List<FilePair> mFileList = null;

    public MultipartRequest(String url, Response.ErrorListener errorListener,
                            Response.Listener<String> listener, Object extraData,
                            Map<String, String> params) {
        super(Method.POST, url, errorListener);
        mFileList = new ArrayList<FilePair>();
        if (extraData != null && extraData instanceof FilePair) {
            FilePair extraFile = (FilePair) extraData;
            mFileList.add(extraFile);
        }
        if (extraData != null && extraData instanceof ArrayList) {
            List<FilePair> extraList = (ArrayList<FilePair>) extraData;
            mFileList.addAll(extraList);
        }
        mListener = listener;
        mParams = params;
        buildMultipartEntity();
    }


    private void buildMultipartEntity() {

        try {
            ArrayList<Part> list = new ArrayList<Part>();
            for (int i = 0; i < mFileList.size(); i++) {
                FilePair f = mFileList.get(i);
                list.add(new FilePart(f.getKey(), f.getFile(),
                        "image/jpg", HTTP.UTF_8));
            }
            if (mParams != null) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    list.add(new StringPart(entry.getKey(), entry.getValue()
                            , HTTP.UTF_8));
                }
            }
            Part[] parts = new Part[list.size()];
            entity = new MultipartEntity(list.toArray(parts));
        } catch (Exception e) {

        }
    }

    @Override
    public String getBodyContentType() {
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            entity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        if (VolleyLog.DEBUG) {
            if (response.headers != null) {
                for (Map.Entry<String, String> entry : response.headers
                        .entrySet()) {
                    VolleyLog.d(entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        String parsed;
        try {
            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }


    /*
     * (non-Javadoc)
     *
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        VolleyLog.d("getHeaders");
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }


        return headers;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}