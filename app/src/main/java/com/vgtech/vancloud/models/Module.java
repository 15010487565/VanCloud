package com.vgtech.vancloud.models;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.DisplayMetrics;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.vgtech.vancloud.ui.chat.controllers.HttpRequestFactory;
import com.vgtech.vancloud.ui.chat.controllers.NetController;
import com.vgtech.vancloud.ui.chat.controllers.PersistentCookieStore;
import com.vgtech.vancloud.ui.chat.controllers.PickerController;
import com.vgtech.vancloud.ui.chat.controllers.PinyinController;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import roboguice.util.Ln;

/**
 * @author xuanqiang
 */
@SuppressWarnings("unused")
public class Module extends AbstractModule{

  @Override
  protected void configure(){
    bindConstant().annotatedWith(Names.named("animTime")).to(400L);
  }

  @Provides
  PinyinController providePinyinController(){
    return new PinyinController();
  }

  @Provides
  PickerController providePickerController(Context context){
    return new PickerController(context);
  }

  @Provides
  NetController provideNetController(ConnectivityManager connectivityManager, Context context,
                                     RestTemplate restTemplate, PreferencesController preferencesController){
    return new NetController(connectivityManager, preferencesController, restTemplate);
  }

  @Provides @Singleton
  RestTemplate provideRestTemplate(Context context){
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
    restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    int connectTimeout = 10 * 1000;
    int readTimeout = 20 * 1000;

//    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//    factory.setConnectTimeout(connectTimeout);
//    factory.setReadTimeout(readTimeout);
//    restTemplate.setRequestFactory(factory);

    BasicHttpContext httpContext = new BasicHttpContext();
    httpContext.setAttribute(ClientContext.COOKIE_STORE, new PersistentCookieStore(context));
    HttpRequestFactory requestFactory = new HttpRequestFactory(httpContext);
    restTemplate.setRequestFactory(requestFactory);

    ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
    if(factory instanceof SimpleClientHttpRequestFactory) {
      SimpleClientHttpRequestFactory simpleFactory = (SimpleClientHttpRequestFactory)factory;
      simpleFactory.setConnectTimeout(connectTimeout);
      simpleFactory.setReadTimeout(readTimeout);
    }else if (factory instanceof HttpComponentsClientHttpRequestFactory) {
      HttpComponentsClientHttpRequestFactory httpClientFactory = (HttpComponentsClientHttpRequestFactory)factory;
      httpClientFactory.setConnectTimeout(connectTimeout);
      httpClientFactory.setReadTimeout(readTimeout);
    }

    return restTemplate;
  }

  @Provides @Singleton @Named("cpuNum")
  int provideCpuNumCores(){
    class CpuFilter implements FileFilter{
      @Override
      public boolean accept(File pathname) {
        return Pattern.matches("cpu[0-9]",pathname.getName());
      }
    }
    try {
      File dir = new File("/sys/devices/system/cpu/");
      File[] files = dir.listFiles(new CpuFilter());
      return files.length;
    } catch(Exception e) {
      Ln.e(e);
      return 0;
    }
  }

  @Provides @Singleton @Named("density")
  float provideDensity(Application app){
    DisplayMetrics dm = app.getResources().getDisplayMetrics();
    return dm.density;
  }

}
