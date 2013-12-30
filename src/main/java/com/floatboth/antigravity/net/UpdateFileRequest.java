package com.floatboth.antigravity.net;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class UpdateFileRequest extends RetrofitSpiceRequest<File, ADNClient> {

  private String accessToken;
  private String id;
  private File file;

  public UpdateFileRequest(String accessToken, String id, File file) {
    super(File.class, ADNClient.class);
    this.accessToken = accessToken;
    this.id = id;
    this.file = file;
  }

  @Override
  public File loadDataFromNetwork() {
    return getService().updateFile(accessToken, id, file).data;
  }
}
