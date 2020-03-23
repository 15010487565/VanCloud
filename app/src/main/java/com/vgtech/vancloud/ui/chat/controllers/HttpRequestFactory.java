package com.vgtech.vancloud.ui.chat.controllers;

import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

/**
 * Created by sandy on 14/10/23.
 */
public class HttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
  private final HttpContext httpContext;

  public HttpRequestFactory(HttpContext httpContext) {
    super();
    this.httpContext = httpContext;
  }

  @Override
  protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
    return this.httpContext;
  }

}

