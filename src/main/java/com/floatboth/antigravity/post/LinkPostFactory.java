package com.floatboth.antigravity.post;

import java.util.List;
import java.util.Arrays;
import com.googlecode.androidannotations.annotations.*;

import com.floatboth.antigravity.data.*;

@EBean
public class LinkPostFactory implements PostFactory {
  public boolean canUseFile(File file) {
    return file.isPublic;
  }

  public Post makePost(File file, String text) {
    Post p = new Post();
    p.text = text;
    p.entities = new Entities();
    LinkEntity le = new LinkEntity();
    le.pos = 0;
    le.len = text.length();
    le.url = file.shortUrl;
    p.entities.links = Arrays.asList(le);
    return p;
  }

  public String factoryName(File file) {
    return "Direct link";
  }
}
