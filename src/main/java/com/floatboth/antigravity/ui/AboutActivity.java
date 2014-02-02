package com.floatboth.antigravity.ui;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.support.v4.app.NavUtils;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;

@EActivity(R.layout.about_activity)
public class AboutActivity extends BaseActivity {
  @ViewById TextView app_info;
  @ViewById ImageView app_logo;
  @DrawableRes Drawable ic_logo;
  @StringRes String app_info_text;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @AfterViews
  public void setUpViews() {
    app_info.setText(Html.fromHtml(app_info_text));
    app_info.setMovementMethod(LinkMovementMethod.getInstance());
    app_logo.setImageDrawable(ic_logo);
  }

  @OptionsItem(android.R.id.home)
  public void goUp() {
    NavUtils.navigateUpFromSameTask(this);
  }
}
