package com.floatboth.antigravity.post;

import com.floatboth.antigravity.data.*;

public interface PostFactory {
  public boolean isAvailable();
  public Post makePost(String text);
  public String factoryName();
}
