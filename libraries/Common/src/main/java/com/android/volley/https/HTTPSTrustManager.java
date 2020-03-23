package com.android.volley.https;

import java.io.InputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class HTTPSTrustManager {
    public static SSLSocketFactory buildSSLSocketFactory(
            InputStream keyStore, String keyStorePassword) {


        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new SsX509TrustManager(keyStore, keyStorePassword)}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext.getSocketFactory();

    }
}
