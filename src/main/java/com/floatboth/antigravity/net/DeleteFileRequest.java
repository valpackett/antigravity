package com.floatboth.antigravity.net;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class DeleteFileRequest extends RetrofitSpiceRequest<File, ADNClient> {

  private String accessToken;
  private String id;

  public DeleteFileRequest(String accessToken, String id) {
    super(File.class, ADNClient.class);
    this.accessToken = accessToken;
    this.id = id;
  }

  @Override
  public File loadDataFromNetwork() {
    return getService().deleteFile(accessToken, id).data;
  }
}
