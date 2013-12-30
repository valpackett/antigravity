package com.floatboth.antigravity.net;

import retrofit.mime.*;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class UploadFileRequest extends RetrofitSpiceRequest<File, ADNClient> {

  private String accessToken;
  private TypedContent content;
  private TypedString type;
  private TypedString isPublic;

  public UploadFileRequest(String accessToken, TypedContent content, TypedString type, TypedString isPublic) {
    super(File.class, ADNClient.class);
    this.accessToken = accessToken;
    this.content = content;
    this.type = type;
    this.isPublic = isPublic;
  }

  @Override
  public File loadDataFromNetwork() {
    return getService().uploadFile(accessToken, content, type, isPublic).data;
  }
}
