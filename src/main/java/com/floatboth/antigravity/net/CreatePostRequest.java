package com.floatboth.antigravity.net;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class CreatePostRequest extends RetrofitSpiceRequest<Post, ADNClient> {

  private String accessToken;
  private Post post;

  public CreatePostRequest(String accessToken, Post post) {
    super(Post.class, ADNClient.class);
    this.accessToken = accessToken;
    this.post = post;
  }

  @Override
  public Post loadDataFromNetwork() {
    return getService().createPost(accessToken, post).data;
  }
}
