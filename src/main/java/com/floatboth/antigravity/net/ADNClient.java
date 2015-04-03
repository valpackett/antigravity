package com.floatboth.antigravity.net;

import retrofit.http.*;
import retrofit.mime.*;
import com.floatboth.antigravity.data.*;

public interface ADNClient {
  @GET("/users/me/files?include_incomplete=0")
  ADNResponse<File.List> myFiles(@Query("access_token") String accessToken, @Query("before_id") String beforeId);

  @PUT("/files/{id}")
  ADNResponse<File> updateFile(@Query("access_token") String accessToken, @Path("id") String id, @Body File file);

  @DELETE("/files/{id}")
  ADNResponse<File> deleteFile(@Query("access_token") String accessToken, @Path("id") String id);

  @Multipart
  @POST("/files")
  ADNResponse<File> uploadFile(@Query("access_token") String accessToken, @Part("content") TypedContent content, @Part("type") TypedString type, @Part("public") TypedString isPublic);

  @GET("/posts/stream/unified?include_deleted=0&include_annotations=1&has_oembed_photo=1")
  ADNResponse<Post.List> getPhotoStream(@Query("access_token") String accessToken, @Query("before_id") String beforeId);

  @POST("/posts")
  ADNResponse<Post> createPost(@Query("access_token") String accessToken, @Body Post post);

  @GET("/config")
  ADNResponse<Configuration> getConfiguration(@Query("access_token") String accessToken);
}
