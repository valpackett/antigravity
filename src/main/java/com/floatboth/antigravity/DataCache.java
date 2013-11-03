package com.floatboth.antigravity;

import java.io.*;
import android.app.Application;
import com.googlecode.androidannotations.annotations.*;

@EBean
public class DataCache {
  @App Application app;
  File cacheDir;

  @AfterInject
  public void initializeCacheDir() {
    cacheDir = app.getCacheDir();
  }

  public boolean set(String filename, Object data) {
    try {
      FileOutputStream file = new FileOutputStream(new File(cacheDir, filename));
      BufferedOutputStream buf = new BufferedOutputStream(file);
      ObjectOutput output = new ObjectOutputStream(buf);
      try {
        output.writeObject(data);
      } finally {
        output.close();
      }
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  public Object get(String filename) {
    try {
      FileInputStream file = new FileInputStream(new File(cacheDir, filename));
      BufferedInputStream buf = new BufferedInputStream(file);
      ObjectInput input = new ObjectInputStream(buf);
      Object r;
      try {
        r = input.readObject();
      } catch (Exception e) {
        return null;
      } finally {
        input.close();
      }
      return r;
    } catch (IOException e) {
      return null;
    }
  }

  public boolean delete(String filename) {
    return new File(cacheDir, filename).delete();
  }
}
