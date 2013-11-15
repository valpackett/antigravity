package com.floatboth.antigravity.data;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public final class FileAnnotationReplacementValue implements Serializable {
  @SerializedName("file_token") public String fileToken;
  @SerializedName("file_id") public String fileId;
  public String format;
}
