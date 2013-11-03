package com.floatboth.antigravity.data;

import com.google.gson.annotations.SerializedName;

public class ADNMeta {
  public int code;
  public boolean more;
  @SerializedName("min_id") public String minId;
  @SerializedName("max_id") public String maxId;
}
