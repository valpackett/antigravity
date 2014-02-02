package com.floatboth.antigravity.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.octo.android.robospice.SpiceManager;
import com.floatboth.antigravity.net.ADNSpiceService;
import com.floatboth.antigravity.R;

public abstract class BaseActivity extends Activity {

  private SpiceManager spiceManager = new SpiceManager(ADNSpiceService.class);
  private SystemBarTintManager tintManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    spiceManager.start(this);
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      setTranslucentStatus(true);
    }
    tintManager = new SystemBarTintManager(this);
    tintManager.setStatusBarTintEnabled(true);
    tintManager.setNavigationBarTintEnabled(true);
    tintManager.setStatusBarTintResource(R.color.system_bar_tint);
    tintManager.setNavigationBarTintResource(R.color.system_bar_tint);
  }

  @Override
  protected void onDestroy() {
    spiceManager.shouldStop();
    super.onDestroy();
  }

  protected SpiceManager getSpiceManager() {
    return spiceManager;
  }

  protected SystemBarTintManager getTintManager() {
    return tintManager;
  }

  @TargetApi(19)
  private void setTranslucentStatus(boolean on) {
    Window win = getWindow();
    WindowManager.LayoutParams winParams = win.getAttributes();
    final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    if (on) {
      winParams.flags |= bits;
    } else {
      winParams.flags &= ~bits;
    }
    win.setAttributes(winParams);
  }

}
