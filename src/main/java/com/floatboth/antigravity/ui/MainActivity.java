package com.floatboth.antigravity.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.net.Uri;
import android.view.Window;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Button;
import android.widget.AdapterView;
import android.provider.MediaStore;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import eu.erikw.PullToRefreshListView;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;
import com.googlecode.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;

@EActivity(R.layout.main_activity)
public class MainActivity extends Activity
  implements AdapterView.OnItemClickListener,
             PullToRefreshListView.OnRefreshListener {
  @StringRes String network_error;
  @StringRes String chooser_title;
  @StringRes String log_out_confirm_title;

  @Bean ADNClientFactory adnClientFactory;
  @Bean DataCache dataCache;
  @Pref ADNPrefs_ adnPrefs;
  @SystemService LayoutInflater layoutInflater;
  @ViewById PullToRefreshListView filelist;
  ADNClient adnClient;
  FileListAdapter fileadapter;
  String minId;
  Button loadMoreButton;
  MenuItem cameraToUploadItem;
  Uri camImageUri;

  public static final int REQUEST_CODE_UPDATE_FILE = 1;
  public static final int REQUEST_CODE_PICK_FILE = 2;
  public static final int REQUEST_CODE_CAMERA = 3;

  private void startLogin() {
    LoginActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
    finish();
  }

  private void deleteData() {
    dataCache.delete("files");
    dataCache.delete("minId");
    dataCache.delete("more");
  }

  private void cacheData(List<File> files, String minId, boolean more) {
    dataCache.set("files", files);
    dataCache.set("minId", minId);
    dataCache.set("more", (Boolean) more);
  }

  private void applyData(List<File> files, String minId, boolean more) {
    adnPrefs.lastUrlExpires().put(files.get(files.size()-1).urlExpires.getTime() / 1000L);
    fileadapter.appendFiles(files);
    loadMoreButton.setEnabled(more); // YAY :-)
    this.minId = minId;
  }

  private interface FileLoadCallback {
    public void callback();
  }

  private void loadFiles(String beforeId, FileLoadCallback callback) {
    final FileLoadCallback callbackF = callback; // F is for "Fuck you, Java".
    final MainActivity self = this;
    adnClient.myFiles(beforeId, new Callback<ADNResponse<List<File>>>() {
      public void success(ADNResponse<List<File>> adnResponse, Response rawResponse) {
        self.adnPrefs.refreshFlag().put(false);
        applyData(adnResponse.data, adnResponse.meta.minId, adnResponse.meta.more);
        cacheData(fileadapter.getFiles(), adnResponse.meta.minId, adnResponse.meta.more);
        callbackF.callback();
      }

      public void failure(RetrofitError err) {
        Toast.makeText(self, network_error, Toast.LENGTH_SHORT).show();
        callbackF.callback();
        err.printStackTrace();
      }
    });
  }

  private void loadMoreFiles() {
    final Activity self = this;
    loadFiles(minId, new FileLoadCallback() {
      public void callback() {
        self.setProgressBarIndeterminateVisibility(false);
      }
    });
  }

  private void loadInitialFiles() {
    setProgressBarIndeterminateVisibility(true);
    List<File> filesFromCache = (List<File>) dataCache.get("files");
    String minIdFromCache = (String) dataCache.get("minId");
    Boolean moreFromCache = (Boolean) dataCache.get("more");
    boolean cacheExists = filesFromCache != null && minIdFromCache != null && moreFromCache != null;
    if (cacheExists && !filesNeedRefreshing()) {
      applyData(filesFromCache, minIdFromCache, moreFromCache);
      loadMoreButton.setEnabled(true);
      setProgressBarIndeterminateVisibility(false);
    } else {
      loadFiles("", new FileLoadCallback() {
        public void callback() {
          loadMoreButton.setEnabled(true);
          setProgressBarIndeterminateVisibility(false);
        }
      });
    }
  }

  private Button getLoadMoreButton() {
    loadMoreButton = new Button(this);
    loadMoreButton.setEnabled(false);
    loadMoreButton.setText(R.string.load_more);
    loadMoreButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        loadMoreButton.setEnabled(false);
        setProgressBarIndeterminateVisibility(true);
        loadMoreFiles();
      }
    });
    return loadMoreButton;
  }

  public void onItemClick(AdapterView lst, View v, int position, long id) {
    FileActivity_.intent(this)
      .file(fileadapter.getItem(position))
      .startForResult(REQUEST_CODE_UPDATE_FILE);
  }

  @OnActivityResult(REQUEST_CODE_UPDATE_FILE)
  public void updateFileCache(Intent updateIntent) {
    if (updateIntent == null) return;
    File file = (File) updateIntent.getSerializableExtra("file");
    List<File> updatedFiles = new ArrayList<File>();
    for (File f : fileadapter.getFiles()) {
      if (f.id.equals(file.id)) {
        if (file.isDeleted != true) updatedFiles.add(file);
      } else {
        updatedFiles.add(f);
      }
    }
    fileadapter.setFiles(updatedFiles);
    dataCache.set("files", updatedFiles);
  }

  @OnActivityResult(REQUEST_CODE_PICK_FILE)
  public void onPickedFile(Intent resultIntent) {
    if (resultIntent == null) return;
    Intent uploadIntent = new Intent(Intent.ACTION_SEND);
    uploadIntent.setClass(this, UploadActivity_.class);
    uploadIntent.putExtra(Intent.EXTRA_STREAM, resultIntent.getData());
    startActivity(uploadIntent);
  }

  @OnActivityResult(REQUEST_CODE_CAMERA)
  public void onCameraImage(int resultCode) {
    if (resultCode != -1) return;
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    mediaScanIntent.setData(camImageUri);
    sendBroadcast(mediaScanIntent);
    Intent uploadIntent = new Intent(Intent.ACTION_SEND);
    uploadIntent.setClass(this, UploadActivity_.class);
    uploadIntent.putExtra(Intent.EXTRA_STREAM, camImageUri);
    startActivity(uploadIntent);
  }

  public void onRefresh() {
    fileadapter.clearFiles();
    loadMoreButton.setEnabled(false);
    loadFiles("", new FileLoadCallback() {
      public void callback() {
        filelist.onRefreshComplete();
        loadMoreButton.setEnabled(true);
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    if (filesNeedRefreshing()) {
      filelist.setRefreshing();
      onRefresh();
    }
  }

  @OptionsItem(R.id.pick_to_upload)
  public void pickToUpload() {
    Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
    pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
    pickIntent.setType("*/*");
    startActivityForResult(Intent.createChooser(pickIntent, chooser_title), REQUEST_CODE_PICK_FILE);
  }

  @OptionsItem(R.id.camera_to_upload)
  public void cameraToUpload() {
    Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    camImageUri = CanHasCamera.getImageUri();
    camIntent.putExtra(MediaStore.EXTRA_OUTPUT, camImageUri);
    startActivityForResult(camIntent, REQUEST_CODE_CAMERA);
  }

  @OptionsItem(R.id.log_out)
  public void logOut() {
    final MainActivity self = this;
    new AlertDialog.Builder(this)
      .setTitle(log_out_confirm_title)
      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          adnPrefs.clear();
          self.deleteData();
          self.startLogin();
        }
      })
      .setNegativeButton(R.string.cancel, null)
      .show();
  }

  @OptionsItem(R.id.about)
  public void openAbout() {
    AboutActivity_.intent(this).start();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!adnPrefs.accessToken().exists()) {
      startLogin();
    } else {
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
      adnClient = adnClientFactory.getClient(adnPrefs.accessToken().get());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    cameraToUploadItem = menu.findItem(R.id.camera_to_upload);
    cameraToUploadItem.setVisible(CanHasCamera.isAvailable(this));
    return true;
  }

  @AfterViews
  public void setUpFileList() {
    fileadapter = new FileListAdapter(this, layoutInflater);
    filelist.setAdapter(fileadapter);
    filelist.setOnRefreshListener(this);
    filelist.setOnItemClickListener(this);
    filelist.addFooterView(getLoadMoreButton());
    if (adnPrefs.accessToken().exists()) loadInitialFiles();
  }

  private boolean filesNeedRefreshing() {
    return adnPrefs.refreshFlag().getOr(false)
      || (new Date().getTime() / 1000L) > adnPrefs.lastUrlExpires().getOr(Long.MAX_VALUE);
  }
}
