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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.*;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;
import com.googlecode.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;
import com.floatboth.antigravity.post.*;

@EActivity(R.layout.post_activity)
public class PostActivity extends Activity
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
  @Bean ADNClientFactory adnClientFactory;
  @Bean LinkPostFactory linkPostFactory;
  @Bean OembedPostFactory oembedPostFactory;
  @Bean PlainPostFactory plainPostFactory;
  @Extra File file;
  @Extra String text;
  @Extra int postType;
  @Pref ADNPrefs_ adnPrefs;
  ADNClient adnClient;
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
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      adnClient = adnClientFactory.getClient(adnPrefs.accessToken().get());
      refreshPostTextLimit();
    }
  }

  private void refreshPostTextLimit() {
    setPostTextLimit(adnPrefs.postTextLimit().getOr(256));
    final long curTime = new Date().getTime() / 1000L;
    final PostActivity self = this;
    if (!adnPrefs.lastConfigFetch().exists() || curTime - adnPrefs.lastConfigFetch().get() > 60*60*24) {
      adnClient.getConfiguration(new Callback<ADNResponse<Configuration>>() {
        public void success(ADNResponse<Configuration> adnResponse, Response rawResponse) {
          self.setPostTextLimit(adnResponse.data.post.maxLength);
          adnPrefs.lastConfigFetch().put(curTime);
        }
        public void failure(RetrofitError err) { }
      });
    }
  }

  public void setPostTextLimit(int lim) {
    postTextLimit = lim;
    adnPrefs.postTextLimit().put(lim);
    try {
      post_chars_left.setText(Integer.toString(postTextLimit - postCharsCurrent));
    } catch (Exception ex) {}
  }

  public void setUpViewsForPlain() {
    PostFactory[] allPostFactories = {plainPostFactory};
    for (PostFactory f : allPostFactories) {
      factoriesMap.put(f.factoryName(null), f);
    }
    post_text.setText(text);
    post_text.setSelection(text.length());
    afterTextChanged();
  }

  public void setUpViewsForFile() {
    PostFactory[] allPostFactories = {oembedPostFactory, linkPostFactory};
    boolean canAnyUseFile = false;
    for (PostFactory f : allPostFactories) {
      if (f.canUseFile(file)) {
        factoriesMap.put(f.factoryName(file), f);
        canAnyUseFile = true;
      }
    }
    if (canAnyUseFile == false) {
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
    for (PostFactory f : allPostFactories) {
      factoriesMap.put(f.factoryName(null), f);
    }
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
    final PostActivity self = this;
    Post post = currentPostFactory.makePost(file, post_text.getText().toString());
    adnClient.createPost(post, new Callback<ADNResponse<Post>>() {
      public void success(ADNResponse<Post> adnResponse, Response rawResponse) {
        self.setProgressStatus(false);
        Toast.makeText(self, post_success, Toast.LENGTH_LONG).show();
        if (self.postType == POST_TYPE_SUPPORT) {
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(supportapp_page + "/" + adnResponse.data.id)));
        }
        self.finish();
      }
      public void failure(RetrofitError err) {
        self.setProgressStatus(false);
        Toast.makeText(self, network_error, Toast.LENGTH_LONG).show();
      }
    });
  }

  public void setProgressStatus(boolean p) {
    setProgressBarIndeterminateVisibility(p);
    cancel_post.setEnabled(!p);
    ok_post.setEnabled(!p);
    post_text.setFocusable(!p);
  }
}
