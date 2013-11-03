package com.floatboth.antigravity.data;

import com.google.gson.annotations.SerializedName;

public class ADNAuthError extends Exception {
  @SerializedName("error") public String text = "Unknown error.";
  @SerializedName("error_title") public String title = "Authentication error";
}
