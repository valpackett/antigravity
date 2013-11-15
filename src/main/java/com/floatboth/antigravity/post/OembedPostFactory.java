package com.floatboth.antigravity.post;

import java.util.List;
import java.util.Arrays;
import com.googlecode.androidannotations.annotations.*;

import com.floatboth.antigravity.data.*;

@EBean
public class OembedPostFactory implements PostFactory {
  public boolean canUseFile(File file) {
    return file.kind.matches("image");
  }

  public Post makePost(File file, String text) {
    Post p = new Post();
    p.text = text;

    p.entities = new Entities();
    LinkEntity le = new LinkEntity();
    le.pos = 0;
    le.len = text.length();
    le.url = "http://photos.app.net/{post_id}/1";
    p.entities.links = Arrays.asList(le);

    FileAnnotationReplacementValue r = new FileAnnotationReplacementValue();
    r.fileToken = file.fileToken;
    r.fileId = file.id;
    r.format = "oembed";
    FileAnnotationReplacementValueWrapper w = new FileAnnotationReplacementValueWrapper();
    w.value = r;
    Annotation ann = new Annotation();
    ann.type = "net.app.core.oembed";
    ann.value = w;
    p.annotations = Arrays.asList(ann);
    return p;
  }

  public String factoryName(File file) {
    return "Attached image and photos.app.net link";
  }
}
