package com.floatboth.antigravity;

import java.util.List;
import retrofit.Callback;
import retrofit.http.*;
import com.floatboth.antigravity.data.ADNResponse;
import com.floatboth.antigravity.data.File;

public interface ADNClient {
  @GET("/users/me/files?include_incomplete=0")
  void myFiles(@Query("before_id") String beforeId, Callback<ADNResponse<List<File>>> cb);

  @PUT("/files/{id}")
  void updateFile(@Path("id") String id, @Body File file, Callback<ADNResponse<File>> cb);
}
