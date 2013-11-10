package com.floatboth.antigravity.ui;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.support.v4.app.NavUtils;
import com.googlecode.androidannotations.annotations.*;

import com.floatboth.antigravity.*;

@EActivity(R.layout.about_activity)
public class AboutActivity extends Activity {
  @ViewById TextView app_info;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @AfterViews
  public void setUpViews() {
    app_info.setText(Html.fromHtml(getString(R.string.app_info)));
    app_info.setMovementMethod(LinkMovementMethod.getInstance());
  }

  @OptionsItem(android.R.id.home)
  public void goUp() {
    NavUtils.navigateUpFromSameTask(this);
  }
}
