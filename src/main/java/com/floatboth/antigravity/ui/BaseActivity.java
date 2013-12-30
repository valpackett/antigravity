package com.floatboth.antigravity.ui;

import android.app.Activity;

import com.octo.android.robospice.SpiceManager;
import com.floatboth.antigravity.net.ADNSpiceService;

public abstract class BaseActivity extends Activity {

  private SpiceManager spiceManager = new SpiceManager(ADNSpiceService.class);

  @Override
  protected void onStart() {
    spiceManager.start(this);
    super.onStart();
  }

  @Override
  protected void onStop() {
    spiceManager.shouldStop();
    super.onStop();
  }

  protected SpiceManager getSpiceManager() {
    return spiceManager;
  }

}
