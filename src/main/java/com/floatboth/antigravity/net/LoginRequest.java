package com.floatboth.antigravity.net;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.mimecraft.FormEncoding;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.floatboth.antigravity.data.*;

public class LoginRequest extends RetrofitSpiceRequest<String, ADNClient> {

  private static final String OAUTH_URL = "https://account.app.net/oauth/access_token";

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
  public String loadDataFromNetwork() throws Exception {
    OkHttpClient client = new OkHttpClient();
    InputStream in = null;
    OutputStream out = null;
    try {
      HttpURLConnection conn = client.open(new URL(OAUTH_URL));
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      out = conn.getOutputStream();
      new FormEncoding.Builder()
        .add("client_id", clientId)
        .add("password_grant_secret", passwordSecret)
        .add("grant_type", "password")
        .add("username", username)
        .add("password", password)
        .add("scopes", scopes)
        .build().writeBodyTo(out);
      out.close();
      Gson gson = new Gson();
      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        in = conn.getErrorStream(); // wtf. "error stream" my ass. http body is fucking http body, always.
        throw gson.fromJson(new InputStreamReader(in), ADNAuthError.class);
      }
      in = conn.getInputStream();
      return gson.fromJson(new InputStreamReader(in), ADNAuthResponse.class).accessToken;
    } finally {
      try { // Fuck you.
        if (out != null) out.close();
        if (in != null) in.close();
      } catch (IOException ex) {
        throw new ADNAuthError();
      }
    }
  }
}
