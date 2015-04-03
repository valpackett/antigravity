package com.floatboth.antigravity.ui;

import android.annotation.TargetApi;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.octo.android.robospice.SpiceManager;
import com.floatboth.antigravity.net.ADNSpiceService;
import com.floatboth.antigravity.R;

public abstract class BaseActivity extends ActionBarActivity {

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
