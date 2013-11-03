package com.floatboth.antigravity.data;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public final class DerivedFiles implements Serializable {
  @SerializedName("image_thumb_200s") public Thumbnail thumbnailSmall;
  @SerializedName("image_thumb_960r") public Thumbnail thumbnailBig;
}
