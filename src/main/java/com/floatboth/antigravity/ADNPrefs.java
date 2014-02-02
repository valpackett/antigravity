package com.floatboth.antigravity;

import org.androidannotations.annotations.sharedpreferences.*;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface ADNPrefs {
  String accessToken();

  boolean refreshFlag();
  long lastUrlExpires();

  int postTextLimit();
}
