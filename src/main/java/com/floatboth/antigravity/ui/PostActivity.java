package com.floatboth.antigravity.ui;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Toast;
import android.view.Window;
import android.view.View;
import android.text.Html;
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

  @ViewById(R.id.cancel_post) Button cancelButton;
  @ViewById(R.id.ok_post) Button okButton;
  @ViewById(R.id.post_text) EditText postEditText;
  @ViewById(R.id.post_type_spinner) Spinner postTypeSpinner;
  @Bean ADNClientFactory adnClientFactory;
  @Bean LinkPostFactory linkPostFactory;
  @Bean OembedPostFactory oembedPostFactory;
  @Extra File file;
  @Pref ADNPrefs_ adnPrefs;
  ADNClient adnClient;
  PostFactory currentPostFactory;
  HashMap<String, PostFactory> factoriesMap;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!adnPrefs.accessToken().exists()) {
      Toast.makeText(this, not_logged_in, Toast.LENGTH_LONG).show();
      finish();
    } else {
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      adnClient = adnClientFactory.getClient(adnPrefs.accessToken().get());
    }
  }

  @AfterViews
  public void setUpViews() {
    factoriesMap = new HashMap();
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
      return;
    }
    ArrayList factoryNames = new ArrayList(factoriesMap.keySet());
    Collections.sort(factoryNames);
    ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, factoryNames);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    postTypeSpinner.setAdapter(adapter);
    postTypeSpinner.setOnItemSelectedListener(this);
  }

  public void onItemSelected(AdapterView parent, View view, int pos, long id) {
    currentPostFactory = factoriesMap.get(parent.getItemAtPosition(pos));
  }

  public void onNothingSelected(AdapterView parent) { }

  @Click(R.id.cancel_post)
  public void onCancel() {
    finish();
  }

  @Click(R.id.ok_post)
  public void onOk() {
    setProgressStatus(true);
    final PostActivity self = this;
    Post post = currentPostFactory.makePost(file, postEditText.getText().toString());
    adnClient.createPost(post, new Callback<ADNResponse<Post>>() {
      public void success(ADNResponse<Post> adnResponse, Response rawResponse) {
        self.setProgressStatus(false);
        Toast.makeText(self, post_success, Toast.LENGTH_LONG).show();
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
    cancelButton.setEnabled(!p);
    okButton.setEnabled(!p);
  }
}