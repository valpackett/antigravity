package com.floatboth.antigravity.data;

import java.util.Date;
import java.util.List;
import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public final class Post implements Serializable {
  public String id;
  public String text;
  public List<Annotation> annotations;
  public Entities entities;
  @SerializedName("created_at") public Date createdAt;
}
