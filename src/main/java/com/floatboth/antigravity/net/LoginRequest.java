package com.floatboth.antigravity.net;

import java.io.*;
import com.google.gson.Gson;
import com.squareup.okhttp.*;
import com.squareup.mimecraft.FormEncoding;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class LoginRequest extends RetrofitSpiceRequest<String, ADNClient> {

  private static final String OAUTH_URL = "https://account.app.net/oauth/access_token";
  private static final MediaType Form = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

  private String clientId;
  private String passwordSecret;
  private String username;
  private String password;
  private String scopes;

  public LoginRequest(String clientId, String passwordSecret, String username, String password, String scopes) {
    super(String.class, ADNClient.class);
    this.clientId = clientId;
    this.passwordSecret = passwordSecret;
    this.username = username;
    this.password = password;
    this.scopes = scopes;
  }

  @Override
  public String loadDataFromNetwork() throws ADNAuthError {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      new FormEncoding.Builder()
        .add("client_id", clientId)
        .add("password_grant_secret", passwordSecret)
        .add("grant_type", "password")
        .add("username", username)
        .add("password", password)
        .add("scopes", scopes)
        .build().writeBodyTo(out);
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder()
        .url(OAUTH_URL)
        .post(RequestBody.create(Form, out.toByteArray()))
        .build();
      Response response = client.newCall(request).execute();
      Gson gson = new Gson();
      InputStreamReader rdr = new InputStreamReader(response.body().byteStream());
      if (response.code() != 200) {
        throw gson.fromJson(rdr, ADNAuthError.class);
      }
      return gson.fromJson(rdr, ADNAuthResponse.class).accessToken;
    } catch (IOException ex) {
      throw new ADNAuthError();
    }
  }
}
