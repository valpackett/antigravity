package com.floatboth.antigravity.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.net.Uri;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Toast;
import android.view.Window;
import android.view.View;
import android.text.Html;
import android.text.Editable;
import android.text.TextWatcher;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.*;
import org.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;
import com.floatboth.antigravity.post.*;
import com.floatboth.antigravity.net.*;

@EActivity(R.layout.post_activity)
public class PostActivity extends BaseActivity
  implements AdapterView.OnItemSelectedListener {
  @StringRes String not_logged_in;
  @StringRes String network_error;
  @StringRes String post_success;
  @StringRes String post_no_way;
  @StringRes String post_cancel_confirm_title;
  @StringRes String post_cancel_confirm_message;
  @StringRes String supportapp_username;
  @StringRes String supportapp_id;
  @StringRes String supportapp_page;

  @ViewById Button cancel_post;
  @ViewById Button ok_post;
  @ViewById EditText post_text;
  @ViewById TextView post_chars_left;
  @ViewById Spinner post_type_spinner;
  @ViewById SmoothProgressBar post_progress;
  @Extra File file;
  @Extra String text;
  @Extra int postType;
  @Pref ADNPrefs_ adnPrefs;
  String adnToken;
  PostFactory currentPostFactory;
  HashMap<String, PostFactory> factoriesMap;
  int postTextLimit;
  int postCharsCurrent = 0;

  public static final int POST_TYPE_PLAIN = 0;
  public static final int POST_TYPE_FILE = 1;
  public static final int POST_TYPE_SUPPORT = 2;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!adnPrefs.accessToken().exists()) {
      Toast.makeText(this, not_logged_in, Toast.LENGTH_LONG).show();
      finish();
    } else {
      adnToken = adnPrefs.accessToken().get();
      refreshPostTextLimit();
    }
  }

  private void refreshPostTextLimit() {
    setPostTextLimit(adnPrefs.postTextLimit().getOr(256));
    getSpiceManager().execute(new ConfigurationRequest(adnToken),
        "config", DurationInMillis.ONE_DAY, new ConfigurationListener());
  }

  public final class ConfigurationListener implements RequestListener<Configuration> {
    @Override
    public void onRequestFailure(SpiceException spiceException) { }

    @Override
    public void onRequestSuccess(final Configuration data) {
      setPostTextLimit(data.post.maxLength);
    }
  }

  public void setPostTextLimit(int lim) {
    postTextLimit = lim;
    adnPrefs.postTextLimit().put(lim);
    try {
      afterTextChanged();
    } catch (Exception ex) {}
  }

  public boolean setFactories(PostFactory[] allPostFactories) {
    boolean isAnyFactoryAvailable = false;
    for (PostFactory f : allPostFactories) {
      if (f.isAvailable()) {
        factoriesMap.put(f.factoryName(), f);
        isAnyFactoryAvailable = true;
      }
    }
    return isAnyFactoryAvailable;
  }

  public void setUpViewsForPlain() {
    PostFactory[] allPostFactories = {new PlainPostFactory()};
    setFactories(allPostFactories);
    if (text != null) {
      post_text.setText(text);
      post_text.setSelection(text.length());
    }
    afterTextChanged();
  }

  public void setUpViewsForFile() {
    PostFactory[] allPostFactories = {
      new OembedPostFactory(file),
      new LinkPostFactory(file)
    };
    if (setFactories(allPostFactories) == false) {
      Toast.makeText(this, post_no_way, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  public void setUpViewsForSupport() {
    PostFactory[] allPostFactories = {
      new SupportPostFactory(supportapp_id, "praise", "Praise"),
      new SupportPostFactory(supportapp_id, "ideas", "Idea"),
      new SupportPostFactory(supportapp_id, "bugs", "Bug"),
    };
    setFactories(allPostFactories);
    post_text.setText("@" + supportapp_username + " ");
    post_text.setSelection(supportapp_username.length() + 2);
  }

  @AfterViews
  public void setUpViews() {
    factoriesMap = new HashMap();
    ok_post.setEnabled(false);
    switch (postType) {
      case POST_TYPE_PLAIN: setUpViewsForPlain();
                            break;
      case POST_TYPE_FILE: setUpViewsForFile();
                           break;
      case POST_TYPE_SUPPORT: setUpViewsForSupport();
                              break;
    }
    ArrayList factoryNames = new ArrayList(factoriesMap.keySet());
    Collections.sort(factoryNames);
    ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, factoryNames);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    post_type_spinner.setAdapter(adapter);
    post_type_spinner.setOnItemSelectedListener(this);
    post_chars_left.setText(Integer.toString(postTextLimit - post_text.getText().length()));
    final PostActivity self = this;
    post_text.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        self.afterTextChanged(s);
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      public void onTextChanged(CharSequence s, int start, int before, int count) { }
    });
    post_progress.setVisibility(View.INVISIBLE);
  }

  public void afterTextChanged() {
    afterTextChanged(post_text.getText());
  }

  public void afterTextChanged(Editable s) {
    postCharsCurrent = s.length();
    post_chars_left.setText(Integer.toString(postTextLimit - postCharsCurrent));
    ok_post.setEnabled(postCharsCurrent != 0 && postCharsCurrent <= postTextLimit);
  }

  public void onItemSelected(AdapterView parent, View view, int pos, long id) {
    currentPostFactory = factoriesMap.get(parent.getItemAtPosition(pos));
  }

  public void onNothingSelected(AdapterView parent) { }

  @Click(R.id.cancel_post)
  public void onCancel() {
    if (post_text.getText().length() != 0) {
      final PostActivity self = this;
      new AlertDialog.Builder(this)
        .setTitle(post_cancel_confirm_title)
        .setMessage(post_cancel_confirm_message)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            self.finish();
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
    } else {
      finish();
    }
  }

  @Click(R.id.ok_post)
  public void onOk() {
    setProgressStatus(true);
    Post post = currentPostFactory.makePost(post_text.getText().toString());
    getSpiceManager().execute(new CreatePostRequest(adnToken, post), new CreatePostListener());
  }

  public class CreatePostListener implements RequestListener<Post> {
    @Override
    public void onRequestFailure(SpiceException spiceException) {
      setProgressStatus(false);
      Toast.makeText(PostActivity.this, network_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestSuccess(final Post data) {
      setProgressStatus(false);
      Toast.makeText(PostActivity.this, post_success, Toast.LENGTH_LONG).show();
      if (postType == POST_TYPE_SUPPORT) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(supportapp_page + "/" + data.id)));
      }
      finish();
    }
  }

  public void setProgressStatus(boolean p) {
    post_progress.setVisibility(p ? View.VISIBLE : View.INVISIBLE);
    cancel_post.setEnabled(!p);
    ok_post.setEnabled(!p);
    post_text.setFocusable(!p);
  }
}
