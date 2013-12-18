package com.floatboth.antigravity.post;

import com.floatboth.antigravity.data.*;

public class PlainPostFactory implements PostFactory {
  public boolean isAvailable() {
    return true;
  }

  public Post makePost(String text) {
    Post p = new Post();
    p.text = text;
    return p;
  }

  public String factoryName() {
    return "Plain text";
  }
}
