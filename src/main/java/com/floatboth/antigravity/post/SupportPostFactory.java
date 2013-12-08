package com.floatboth.antigravity.post;

import java.util.Arrays;

import com.floatboth.antigravity.data.*;

public class SupportPostFactory implements PostFactory {
  public final String supportAppId;
  public final String supportTypeAnn;
  public final String supportTypeTitle;

  public SupportPostFactory(String id, String ann, String title) {
    supportAppId = id;
    supportTypeAnn = ann;
    supportTypeTitle = title;
  }

  public boolean canUseFile(File file) {
    return true;
  }

  public Post makePost(File file, String text) {
    Post p = new Post();
    p.text = text;
    p.replyTo = supportAppId;

    SupportAnnotationValue sav = new SupportAnnotationValue();
    sav.type = supportTypeAnn;
    Annotation ann = new Annotation();
    ann.type = "com.floatboth.supportadn.entry";
    ann.value = sav;
    p.annotations = Arrays.asList(ann);
    return p;
  }

  public String factoryName(File file) {
    return supportTypeTitle;
  }
}
