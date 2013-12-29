package com.floatboth.antigravity.ui;

import java.util.Map;
import java.util.HashMap;
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
import android.widget.CompoundButton;
import android.widget.Toast;
import android.view.Window;
import android.text.Html;
import org.apache.tika.Tika;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.*;
import org.apache.commons.io.IOUtils;
import com.samskivert.mustache.*;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;
import com.googlecode.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;
import com.floatboth.antigravity.net.*;

@EActivity(R.layout.upload_activity)
public class UploadActivity extends Activity {
  @StringRes String invalid_intent;
  @StringRes String network_error;
  @StringRes String file_error;
  @StringRes String io_error;
  @StringRes String copied;
  @StringRes String upload_description_template;
  @StringRes String not_logged_in;
  Template descTpl;

  @Bean ADNClientFactory adnClientFactory;
  @Pref ADNPrefs_ adnPrefs;
  @ViewById TextView upload_desc;
  @ViewById ImageView image_upload_preview;
  @ViewById Button cancel_upload;
  @ViewById Button ok_upload;
  @ViewById CompoundButton post_after_upload_switch;
  ContentResolver rslv;
  ClipboardManager clipboardManager;
  ADNClient adnClient;
  Uri uri;
  String mimeType = "";
  String fileName = "";
  long fileSize;
  boolean doPostAfterUpload = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    descTpl = Mustache.compiler().compile(upload_description_template);
    clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    rslv = getContentResolver();
    if (!adnPrefs.accessToken().exists()) {
      Toast.makeText(this, not_logged_in, Toast.LENGTH_LONG).show();
      finish();
    } else {
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      adnClient = adnClientFactory.getClient(adnPrefs.accessToken().get());
      Intent intent = getIntent();
      String action = intent.getAction();
      String type = intent.getType();
      uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
      boolean isSend = action.equals(Intent.ACTION_SEND);
      if (isSend && "text/plain".equals(type)) {
        PostActivity_.intent(this).postType(PostActivity_.POST_TYPE_PLAIN)
          .text(intent.getStringExtra(Intent.EXTRA_TEXT)).start();
        finish();
      } else if (isSend && uri != null) {
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
          Toast.makeText(this, file_error + ": " + uri.toString(), Toast.LENGTH_SHORT).show();
          finish();
        }
      } else {
        Toast.makeText(this, invalid_intent, Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  @AfterViews
  public void setUpViews() {
    Map<String, String> desc = new HashMap<String, String>();
    desc.put("name", fileName);
    desc.put("type", mimeType);
    if (fileSize != -1) desc.put("size", FileDescriptionHelper.size(fileSize, false));
    upload_desc.setText(Html.fromHtml(descTpl.execute(desc)));
    if (mimeType.startsWith("image")) {
      Picasso.with(this).load(uri).into(image_upload_preview);
    }
    final UploadActivity self = this;
    post_after_upload_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        self.doPostAfterUpload = isChecked;
      }
    });
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
              String url = adnResponse.data.shortUrl;
              clipboardManager.setPrimaryClip(ClipData.newPlainText(url, url));
              self.adnPrefs.refreshFlag().put(true);
              self.setProgressStatus(false);
              Toast.makeText(self, copied + ": " + url, Toast.LENGTH_LONG).show();
              if (doPostAfterUpload)
                PostActivity_.intent(self).postType(PostActivity_.POST_TYPE_FILE)
                  .file(adnResponse.data).start();
              self.finish();
            }
            public void failure(RetrofitError err) {
              self.setProgressStatus(false);
              Toast.makeText(self, network_error, Toast.LENGTH_LONG).show();
            }
          });
    } catch (IOException ex) {
      self.setProgressStatus(false);
      Toast.makeText(self, io_error, Toast.LENGTH_LONG).show();
    }
  }

  public void setProgressStatus(boolean p) {
    setProgressBarIndeterminateVisibility(p);
    cancel_upload.setEnabled(!p);
    ok_upload.setEnabled(!p);
  }
}
