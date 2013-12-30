package com.floatboth.antigravity.net;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class ConfigurationRequest extends RetrofitSpiceRequest<Configuration, ADNClient> {

  private String accessToken;

  public ConfigurationRequest(String accessToken) {
    super(Configuration.class, ADNClient.class);
    this.accessToken = accessToken;
  }

  @Override
  public Configuration loadDataFromNetwork() {
    return getService().getConfiguration(accessToken).data;
  }
}
