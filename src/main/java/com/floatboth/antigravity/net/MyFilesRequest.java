package com.floatboth.antigravity.net;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class MyFilesRequest extends RetrofitSpiceRequest<File.List, ADNClient> {

  private String accessToken;
  private String beforeId;

  public MyFilesRequest(String accessToken, String beforeId) {
    super(File.List.class, ADNClient.class);
    this.accessToken = accessToken;
    this.beforeId = beforeId;
  }

  @Override
  public File.List loadDataFromNetwork() {
    ADNResponse<File.List> ar = getService().myFiles(accessToken, beforeId);
    ar.data.meta = ar.meta;
    return ar.data;
  }
}
