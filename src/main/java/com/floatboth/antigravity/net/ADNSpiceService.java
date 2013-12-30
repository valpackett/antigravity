package com.floatboth.antigravity.net;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import com.squareup.okhttp.OkHttpClient;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

public class ADNSpiceService extends RetrofitGsonSpiceService {

  private static final String API_URL = "https://alpha-api.app.net/stream/0";

  OkHttpClient client = new OkHttpClient();

  @Override
  public void onCreate() {
    super.onCreate();
    addRetrofitInterface(ADNClient.class);
  }

  @Override
  protected String getServerUrl() {
    return API_URL;
  }

  @Override
  protected RestAdapter.Builder createRestAdapterBuilder() {
    return super.createRestAdapterBuilder().setClient(new OkClient(client));
  }

  @Override
  public int getThreadCount() {
    return 3;
  }
}
