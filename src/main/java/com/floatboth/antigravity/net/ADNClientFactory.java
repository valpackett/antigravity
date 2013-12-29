package com.floatboth.antigravity.net;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import android.content.Context;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.mimecraft.FormEncoding;
import com.googlecode.androidannotations.annotations.*;
import com.google.gson.Gson;

import com.floatboth.antigravity.R;
import com.floatboth.antigravity.data.ADNAuthResponse;
import com.floatboth.antigravity.data.ADNAuthError;

@EBean
public class ADNClientFactory {
  @RootContext Context context;
  OkHttpClient client = new OkHttpClient();
  Gson gson = new Gson();

  private static final String API_URL = "https://alpha-api.app.net/stream/0";
  private static final String OAUTH_URL = "https://account.app.net/oauth/access_token";

  public ADNClient getClient(String token) {
    return new RestAdapter.Builder()
      .setClient(new OkClient(client))
      .setRequestInterceptor(new ADNAuthInterceptor(token))
      .setServer(API_URL)
      .build().create(ADNClient.class);
  }

  public String getAccessToken(String username, String password, String scopes)
    throws ADNAuthError, IOException {
    InputStream in = null;
    OutputStream out = null;
    try {
      HttpURLConnection conn = client.open(new URL(OAUTH_URL));
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      out = conn.getOutputStream();
      new FormEncoding.Builder()
        .add("client_id", context.getString(R.string.client_id))
        .add("password_grant_secret", context.getString(R.string.password_secret))
        .add("grant_type", "password")
        .add("username", username)
        .add("password", password)
        .add("scopes", scopes)
        .build().writeBodyTo(out);
      out.close();
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
