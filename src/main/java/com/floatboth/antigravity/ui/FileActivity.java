package com.floatboth.antigravity.ui;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.text.Html;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;
import com.googlecode.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;

@EActivity(R.layout.file_activity)
public class FileActivity extends Activity
  implements Callback {
  @StringRes String make_public_success;
  @StringRes String delete_confirm_title;
  @StringRes String delete_confirm_message;
  @StringRes String delete_confirm_message_if_public;
  @StringRes String delete_success;
  @StringRes String network_error;
  @StringRes String io_error;
  @StringRes String copied;

  @Bean ADNClientFactory adnClientFactory;
  @ViewById ImageView image_preview;
  @ViewById TextView file_description;
  @Extra File file;
  @Pref ADNPrefs_ adnPrefs;
  ADNClient adnClient;
  Menu menu;
  MenuItem shareItem;
  ClipboardManager clipboardManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(true);
    clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    getActionBar().setDisplayHomeAsUpEnabled(true);
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
      menu.findItem(R.id.copy_to_clipboard).setVisible(true);
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
        menu.findItem(R.id.copy_to_clipboard).setVisible(true);
        Toast.makeText(self, make_public_success, Toast.LENGTH_SHORT).show();
      }

      public void failure(RetrofitError err) {
        self.setProgressBarIndeterminateVisibility(false);
        Toast.makeText(self, network_error, Toast.LENGTH_SHORT).show();
      }
    });
  }

  @OptionsItem(R.id.delete)
  public void deleteFile() {
    final FileActivity self = this;
    String deleteConfirmMsg = delete_confirm_message;
    if (file.isPublic) deleteConfirmMsg += " " + delete_confirm_message_if_public;
    new AlertDialog.Builder(this)
      .setTitle(delete_confirm_title)
      .setMessage(deleteConfirmMsg)
      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          self.setProgressBarIndeterminateVisibility(true);
          self.adnClient.deleteFile(self.file.id, new retrofit.Callback<ADNResponse<File>>() {
            public void success(ADNResponse<File> adnResponse, Response rawResponse) {
              self.file.isDeleted = true;
              self.updateResultIntent();
              self.setProgressBarIndeterminateVisibility(false);
              Toast.makeText(self, delete_success, Toast.LENGTH_SHORT).show();
              self.finish();
            }

            public void failure(RetrofitError err) {
              self.setProgressBarIndeterminateVisibility(false);
              Toast.makeText(self, network_error, Toast.LENGTH_SHORT).show();
            }
          });
        }
      })
      .setNegativeButton(R.string.cancel, null)
      .show();
  }

  @OptionsItem(R.id.copy_to_clipboard)
  public void copyToClipboard() {
    clipboardManager.setPrimaryClip(ClipData.newPlainText(file.shortUrl, file.shortUrl));
    Toast.makeText(this, copied + ": " + file.shortUrl, Toast.LENGTH_LONG).show();
  }

  @AfterViews
  public void setUpViews() {
    Picasso.with(this).load(file.url).into(image_preview, this);
    file_description.setText(Html.fromHtml(FileDescriptionHelper.longDescription(this, file)));
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
    Picasso.with(this).cancelRequest(image_preview);
    super.onDestroy();
  }

  @OptionsItem(android.R.id.home)
  public void goUp() {
    NavUtils.navigateUpFromSameTask(this);
  }
}
