package com.floatboth.antigravity.ui;

import java.io.InputStream;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import android.support.v4.app.NavUtils;
import org.apache.commons.io.IOUtils;
import org.androidannotations.annotations.*;

import com.floatboth.antigravity.*;

@EActivity(R.layout.about_activity)
public class LicenseActivity extends BaseActivity {
  @ViewById TextView app_info;
  Uri uri;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    uri = getIntent().getData();
    if (uri == null) {
      finish();
      return;
    }
  }

  @AfterViews
  public void setUpViews() {
    try {
      InputStream is = getAssets().open(uri.toString().replace("com.floatboth.antigravity.license://", "license_") + ".txt");
      app_info.setText(IOUtils.toString(is, "UTF-8"));
      Linkify.addLinks(app_info, Linkify.WEB_URLS);
      app_info.setMovementMethod(LinkMovementMethod.getInstance());
    } catch (Exception ex) {}
  }

  @OptionsItem(android.R.id.home)
  public void goUp() {
    NavUtils.navigateUpFromSameTask(this);
  }
}
