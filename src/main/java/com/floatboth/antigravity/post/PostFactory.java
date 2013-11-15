package com.floatboth.antigravity.post;

import com.floatboth.antigravity.data.*;

public interface PostFactory {
  public boolean canUseFile(File file);
  public Post makePost(File file, String text);
  public String factoryName(File file);
}
