package com.floatboth.antigravity.ui;

import java.io.InputStream;
import java.io.IOException;
import android.app.Activity;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import android.view.Window;
import android.text.Html;
import org.apache.tika.Tika;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.*;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;
import org.apache.commons.io.IOUtils;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;

@EActivity(R.layout.upload_activity)
public class UploadActivity extends Activity {
  @Bean ADNClientFactory adnClientFactory;
  @Pref ADNPrefs_ adnPrefs;
  // @SystemService
  ClipboardManager clipboardManager;
  @ViewById(R.id.upload_desc) TextView descView;
  @ViewById(R.id.image_upload_preview) ImageView imageView;
  @ViewById(R.id.cancel_upload) Button cancelButton;
  @ViewById(R.id.ok_upload) Button okButton;
  ContentResolver rslv;
  ADNClient adnClient;
  Uri uri;
  String mimeType;
  String fileName;
  long fileSize;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    rslv = getContentResolver();
    if (!adnPrefs.accessToken().exists()) {
      Toast.makeText(this, "You're not logged into Antigravity!", Toast.LENGTH_LONG).show();
      finish();
    } else {
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      adnClient = adnClientFactory.getClient(adnPrefs.accessToken().get());
      Intent intent = getIntent();
      String action = intent.getAction();
      uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
      if (action.equals(Intent.ACTION_SEND) && uri != null) {
        try {
          // FUCKING FUCK
          String uriString = uri.toString();
          if (uriString.startsWith("content")) {
            mimeType = rslv.getType(uri);
            Cursor cursor = rslv.query(uri, null, null, null, null);
            cursor.moveToFirst();
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            fileSize = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
          } else {
            InputStream input = rslv.openInputStream(uri);
            mimeType = new Tika().detect(input);
            input.close();
            java.io.File f = new java.io.File(uriString);
            fileName = f.getName();
            fileSize = -1;
          }
        } catch (IOException ex) {
          Toast.makeText(this, "File error: " + uri.toString(), Toast.LENGTH_SHORT).show();
          finish();
        }
      } else {
        Toast.makeText(this, "Invalid intent :-(", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  @AfterViews
  public void setUpViews() {
    String desc = "The following file will be shared to App.net:<br><br>";
    desc += "<b>Name:</b> " + fileName + "<br>";
    desc += "<b>Type:</b> " + mimeType + "<br>";
    if (fileSize != -1) {
      desc += "<b>Size:</b> " + FileDescriptionHelper.size(fileSize, false) + "<br>";
    }
    descView.setText(Html.fromHtml(desc));
    if (mimeType.startsWith("image")) {
      Picasso.with(this).load(uri).into(imageView);
    }
  }

  @Click(R.id.cancel_upload)
  public void onCancel() {
    finish();
  }

  @Click(R.id.ok_upload)
  public void onOk() {
    setProgressStatus(true);
    final UploadActivity self = this;
    try {
      InputStream input = rslv.openInputStream(uri);
      adnClient.uploadFile(
          new TypedContent(fileName, mimeType, input),
          new TypedString("com.floatboth.antigravity.file"),
          new TypedString("true"),
          new Callback<ADNResponse<File>>() {
            public void success(ADNResponse<File> adnResponse, Response rawResponse) {
              self.setProgressStatus(false);
              String url = adnResponse.data.shortUrl;
              clipboardManager.setPrimaryClip(ClipData.newPlainText(url, url));
              Toast.makeText(self, "Copied to clipboard: " + url, Toast.LENGTH_LONG).show();
              self.finish();
            }
            public void failure(RetrofitError err) {
              self.setProgressStatus(false);
              Toast.makeText(self, "Upload error :-(", Toast.LENGTH_LONG).show();
            }
          });
    } catch (IOException ex) {
      self.setProgressStatus(false);
      Toast.makeText(self, "I/O error :-(", Toast.LENGTH_LONG).show();
    }
  }

  private void setProgressStatus(boolean p) {
    setProgressBarIndeterminateVisibility(p);
    cancelButton.setEnabled(!p);
    okButton.setEnabled(!p);
  }
}
