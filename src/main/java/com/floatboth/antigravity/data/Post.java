package com.floatboth.antigravity.data;

import java.util.Date;
import java.util.ArrayList;
import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public final class Post implements Serializable {
  public String id;
  public String text;
  public java.util.List<Annotation> annotations;
  public Entities entities;
  @SerializedName("created_at") public Date createdAt;
  @SerializedName("reply_to") public String replyTo;

  @SuppressWarnings("serial")
  public static class List extends ArrayList<Post> {
    public ADNMeta meta;
  }
}
