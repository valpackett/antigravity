package com.floatboth.antigravity.ui;

import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;

@EActivity(R.layout.file_activity)
public class FileActivity extends Activity
  implements Callback {
  @Bean ADNClientFactory adnClientFactory;
  @ViewById ImageView fullimage;
  @Extra File file;
  @Pref ADNPrefs_ adnPrefs;
  ADNClient adnClient;
  Menu menu;
  MenuItem shareItem;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    setProgressBarIndeterminateVisibility(true);
    if (!adnPrefs.accessToken().exists()) {
      finish();
    } else {
      adnClient = adnClientFactory.getClient(adnPrefs.accessToken().get());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;
    getMenuInflater().inflate(R.menu.file, menu);
    shareItem = menu.findItem(R.id.share);
    updateShareIntent();
    return true;
  }

  private void updateShareIntent() {
    ShareActionProvider shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_TEXT, file.shortUrl);
    shareIntent.setType("text/plain");
    shareActionProvider.setShareIntent(shareIntent);
  }

  private void updateResultIntent() {
    Intent returnIntent = new Intent();
    returnIntent.putExtra("file", file);
    setResult(RESULT_OK, returnIntent);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (file.isPublic) {
      menu.findItem(R.id.share).setVisible(true);
    } else {
      menu.findItem(R.id.make_public).setVisible(true);
    }
    return true;
  }

  public void updateFile(File file) {
    this.file = file;
    updateShareIntent();
    updateResultIntent();
  }

  @OptionsItem(R.id.make_public)
  public void makePublic() {
    setProgressBarIndeterminateVisibility(true);
    final FileActivity self = this;
    File pubDelta = new File();
    pubDelta.isPublic = true;
    adnClient.updateFile(file.id, pubDelta, new retrofit.Callback<ADNResponse<File>>() {
      public void success(ADNResponse<File> adnResponse, Response rawResponse) {
        self.updateFile(adnResponse.data);
        self.setProgressBarIndeterminateVisibility(false);
        menu.findItem(R.id.make_public).setVisible(false);
        menu.findItem(R.id.share).setVisible(true);
        Toast.makeText(self, "File is now public.", Toast.LENGTH_SHORT).show();
      }

      public void failure(RetrofitError err) {
        self.setProgressBarIndeterminateVisibility(false);
        Toast.makeText(self, "Network error :-(", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @AfterViews
  public void setUpViews() {
    Picasso.with(this).load(file.url).into(fullimage, this);
  }

  @Override
  public void onSuccess() {
    setProgressBarIndeterminateVisibility(false);
  }

  @Override
  public void onError() {
    setProgressBarIndeterminateVisibility(false);
  }

  @Override
  public void onDestroy() {
    Picasso.with(this).cancelRequest(fullimage);
    super.onDestroy();
  }

  @OptionsItem(android.R.id.home)
  public void goUp() {
    NavUtils.navigateUpFromSameTask(this);
  }
}
