package com.vgtech.vancloud.ui.chat.controllers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.utils.MD5;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * @author xuanqiang
 */
public abstract class NetConnection {
    public NetConnection(ConnectivityManager connectivityManager, RestTemplate restTemplate) {
        this.connectivityManager = connectivityManager;
        this.restTemplate = restTemplate;
        setRequestInterceptor();
    }

    <T> T get(String uri, Class<T> responseType, Object... urlVariables) throws RestClientException {
        return restTemplate.getForObject(getUrl(uri), responseType, urlVariables);
    }

    <T> T post(final String uri, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return post(uri, null, responseType, uriVariables);
    }

    <T> T post(final String uri, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        Log.e("ceshiliaotian", getUrl(uri));
        return restTemplate.postForObject(getUrl(uri), request, responseType, uriVariables);
    }

    Map postForm(final String uri, final MultiValueMap<String, String> formData) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(formData, requestHeaders);
        return restTemplate.postForObject(getUrl(uri), requestEntity, Map.class);
    }

    public static String getCachePath(Context activity, String url) {
        String ext = "ing";
        Uri uri = Uri.parse(url);
        String cachePath;
        String namePath = uri.getLastPathSegment();
        int pindex = namePath.indexOf(".");
        String name = namePath.substring(0, pindex);
        if (!TextUtils.isEmpty(ext) && "png".equals(ext) || "ing".equals(ext)) {
            cachePath = FileCacheUtils.getImageDir(activity) + "/" + name + "." + ext;
        } else {
            cachePath = activity.getExternalFilesDir(null) + "/" + name + "." + ext;
        }
        File file = new File(cachePath);
        if (file.exists()) {
            return cachePath;
        } else {
            return null;
        }
    }

    public String download(final String url, final String ext, final Activity activity) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
//  restTemplate.execute(URI.create(url), HttpMethod.GET, ACCEPT_CALLBACK, new FileResponseExtractor(filePath));
        Uri uri = Uri.parse(url);
        String cachePath;
        String namePath = uri.getLastPathSegment();
        int pindex = namePath.indexOf(".");
        String name = namePath.substring(0, pindex);
        if (!TextUtils.isEmpty(ext) && "png".equals(ext) || "ing".equals(ext)) {
            cachePath = FileCacheUtils.getImageDir(activity) + "/" + name + "." + ext;
        } else {
            cachePath =  FileCacheUtils.getCacheDir(activity) + "/" + name + "." + ext;
        }
        File file = new File(cachePath);
        if (file.exists()) {
            return cachePath;
        }
        file = restTemplate.execute(URI.create(url), HttpMethod.GET, null, new FileResponseExtractor(cachePath));
        if (!file.exists()) {
            return null;
        }
        return cachePath;
    }
    public String downloadAttach(final String url, final String ext, final Activity activity) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        Uri uri = Uri.parse(url);
        String cachePath;
        String namePath = uri.getLastPathSegment();
        int pindex = namePath.indexOf(".");
        String name = namePath.substring(0, pindex);
         cachePath = FileCacheUtils.getCacheDir(activity) + "/" + MD5.getMD5(url)+"_"+ext;
        File file = new File(cachePath);
        if (file.exists()) {
            return cachePath;
        }
        file = restTemplate.execute(URI.create(url), HttpMethod.GET, null, new FileResponseExtractor(cachePath));
        if (!file.exists()) {
            return null;
        }
        return cachePath;
    }

    public boolean hasNetwork() {
        if (connectivityManager == null) {
            return true;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isAvailable());
        }
    }

    void setRequestInterceptor() {
        restTemplate.setInterceptors(Collections.<ClientHttpRequestInterceptor>singletonList(new HttpRequestInterceptor()));
    }

    //  private static final RequestCallback ACCEPT_CALLBACK = new RequestCallback() {
    //    @Override
    //    public void doWithRequest(ClientHttpRequest request) throws IOException {
    //      request.getHeaders().set("Accept", "application/json");
    //    }
    //  };

    static class FileResponseExtractor implements ResponseExtractor<File> {
        private final File file;

        private FileResponseExtractor(final String filePath) {
            this.file = new File(filePath);
        }

        @Override
        public File extractData(ClientHttpResponse response) throws IOException {
            InputStream is = response.getBody();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            FileCopyUtils.copy(is, os);
            return file;
        }
    }

    class HttpRequestInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            setHeaders(request.getHeaders());
            response = execution.execute(request, body);
//      Ln.e(Strings.toString(response.getBody()));
            return response;
        }
    }

    abstract void setHeaders(final HttpHeaders headers);

    abstract String getUrl(final String uri);


    public ClientHttpResponse getResponse() {
        return response;
    }

    ClientHttpResponse response;
    ConnectivityManager connectivityManager;
    RestTemplate restTemplate;

}
