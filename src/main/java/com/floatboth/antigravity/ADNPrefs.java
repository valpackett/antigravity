package com.floatboth.antigravity;

import com.googlecode.androidannotations.annotations.sharedpreferences.*;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface ADNPrefs {
  String accessToken();
  boolean refreshFlag();
  long lastUrlExpires();
}
