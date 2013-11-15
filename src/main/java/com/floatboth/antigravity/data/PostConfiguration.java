package com.floatboth.antigravity.data;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public final class PostConfiguration implements Serializable {
  @SerializedName("text_max_length") public int maxLength;
}
