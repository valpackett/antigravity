package com.floatboth.antigravity.ui;

import android.os.Bundle;
import android.app.Activity;

import com.octo.android.robospice.SpiceManager;
import com.floatboth.antigravity.net.ADNSpiceService;

public abstract class BaseActivity extends Activity {

  private SpiceManager spiceManager = new SpiceManager(ADNSpiceService.class);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    spiceManager.start(this);
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    spiceManager.shouldStop();
    super.onDestroy();
  }

  protected SpiceManager getSpiceManager() {
    return spiceManager;
  }

}
