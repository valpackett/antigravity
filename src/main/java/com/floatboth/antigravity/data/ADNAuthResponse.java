package com.floatboth.antigravity.data;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public class ADNAuthResponse implements Serializable {
  @SerializedName("access_token") public String accessToken;
}
