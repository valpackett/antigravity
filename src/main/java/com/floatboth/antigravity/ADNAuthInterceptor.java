package com.floatboth.antigravity;

import retrofit.RequestInterceptor;

public class ADNAuthInterceptor implements RequestInterceptor {
  String token;

  public ADNAuthInterceptor(String token) {
    this.token = token;
  }

  public void intercept(RequestFacade request) {
    request.addHeader("Authorization", "Bearer " + token);
  }
}
