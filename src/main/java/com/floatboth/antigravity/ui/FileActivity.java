package com.floatboth.antigravity.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.text.Html;
import android.net.Uri;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;
import com.googlecode.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;
import com.floatboth.antigravity.net.*;

@EActivity(R.layout.file_activity)
public class FileActivity extends Activity {
  @StringRes String make_public_success;
  @StringRes String delete_confirm_title;
  @StringRes String delete_confirm_message;
  @StringRes String delete_confirm_message_if_public;
  @StringRes String delete_success;
  @StringRes String network_error;
  @StringRes String io_error;
  @StringRes String copied;
  @StringRes String share_chooser_title;

  @Bean ADNClientFactory adnClientFactory;
  @ViewById WebView file_preview;
  @ViewById TextView file_description;
  @Extra File file;
  @Pref ADNPrefs_ adnPrefs;
  ADNClient adnClient;
  Menu menu;
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
    return true;
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
      menu.findItem(R.id.open_in_browser).setVisible(true);
    } else {
      menu.findItem(R.id.make_public).setVisible(true);
    }
    return true;
  }

  public void updateFile(File file) {
    this.file = file;
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
        menu.findItem(R.id.open_in_browser).setVisible(true);
        Toast.makeText(self, make_public_success, Toast.LENGTH_SHORT).show();
      }

      public void failure(RetrofitError err) {
        self.setProgressBarIndeterminateVisibility(false);
        Toast.makeText(self, network_error, Toast.LENGTH_SHORT).show();
      }
    });
  }

  @OptionsItem(R.id.share)
  public void shareFile() {
    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_TEXT, file.shortUrl);
    shareIntent.setType("text/plain");
    startActivity(Intent.createChooser(shareIntent, share_chooser_title));
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

  @OptionsItem(R.id.create_post)
  public void createPost() {
    PostActivity_.intent(this).postType(PostActivity_.POST_TYPE_FILE).file(file).start();
  }

  @OptionsItem(R.id.copy_to_clipboard)
  public void copyToClipboard() {
    clipboardManager.setPrimaryClip(ClipData.newPlainText(file.shortUrl, file.shortUrl));
    Toast.makeText(this, copied + ": " + file.shortUrl, Toast.LENGTH_LONG).show();
  }

  @OptionsItem(R.id.open_in_browser)
  public void openInBrowser() {
    Intent browseIntent = new Intent();
    browseIntent.setAction(Intent.ACTION_VIEW);
    browseIntent.setData(Uri.parse(file.shortUrl));
    startActivity(browseIntent);
  }

  @AfterViews
  public void setUpViews() {
    WebSettings ws = file_preview.getSettings();
    ws.setLoadWithOverviewMode(true);
    if ("image".equals(file.kind)) {
      ws.setUseWideViewPort(true);
      ws.setBuiltInZoomControls(true);
      ws.setDisplayZoomControls(false);
      file_preview.loadUrl(file.url);
    } else {
      ws.setJavaScriptEnabled(true);
      try {
        file_preview.loadUrl("https://docs.google.com/viewer?url=" + URLEncoder.encode(file.url, "utf-8"));
      } catch (UnsupportedEncodingException ex) {}
    }
    file_description.setText(Html.fromHtml(FileDescriptionHelper.longDescription(this, file)));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @OptionsItem(android.R.id.home)
  public void goUp() {
    NavUtils.navigateUpFromSameTask(this);
  }
}
