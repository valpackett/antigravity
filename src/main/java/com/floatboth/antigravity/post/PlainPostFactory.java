package com.floatboth.antigravity.post;

import com.googlecode.androidannotations.annotations.*;

import com.floatboth.antigravity.data.*;

@EBean
public class PlainPostFactory implements PostFactory {
  public boolean canUseFile(File file) {
    return true;
  }

  public Post makePost(File file, String text) {
    Post p = new Post();
    p.text = text;
    return p;
  }

  public String factoryName(File file) {
    return "Plain text";
  }
}
