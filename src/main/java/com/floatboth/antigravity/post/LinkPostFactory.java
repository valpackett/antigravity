package com.floatboth.antigravity.post;

import java.util.List;
import java.util.Arrays;

import com.floatboth.antigravity.data.*;

public class LinkPostFactory implements PostFactory {
  public final File file;

  public LinkPostFactory(File file) {
    this.file = file;
  }

  public boolean isAvailable() {
    return file.isPublic;
  }

  public Post makePost(String text) {
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

  public String factoryName() {
    return "Direct link";
  }
}
