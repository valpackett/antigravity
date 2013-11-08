package com.floatboth.antigravity.data;

import java.util.Date;
import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public final class File implements Serializable {
  public String name;
  public String url;
  public String id;
  public String kind;
  public String type;
  public long size;
  @SerializedName("public") public boolean isPublic;
  @SerializedName("url_short") public String shortUrl;
  @SerializedName("created_at") public Date createdAt;
  @SerializedName("file_token") public String fileToken;
  @SerializedName("file_token_read") public String fileTokenRead;
  @SerializedName("derived_files") public DerivedFiles derivedFiles;

  public boolean isDeleted; // used by the app, not ADN as a flag
                            // to remove the file from the cache.
}
