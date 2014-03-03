package com.floatboth.antigravity.ui;

import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import android.content.DialogInterface;
import android.view.Window;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.FrameLayout;
import android.widget.ArrayAdapter;
import android.provider.MediaStore;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.*;
import org.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.R;
import com.floatboth.antigravity.CanHasCamera;
import com.floatboth.antigravity.ADNPrefs_;
import com.floatboth.antigravity.data.File;

@EActivity(R.layout.main_activity)
public class MainActivity extends BaseActivity
  implements ActionBar.OnNavigationListener {
  @StringRes String pick_chooser_title;
  @StringRes String log_out_confirm_title;
  @Pref ADNPrefs_ adnPrefs;
  @ViewById FrameLayout fragment_frame;
  FileListFragment_ file_list;
  MenuItem cameraToUploadItem;
  Uri camImageUri;

  public static final int REQUEST_CODE_PICK_FILE = 2;
  public static final int REQUEST_CODE_CAMERA = 3;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);
    if (!adnPrefs.accessToken().exists()) {
      startLogin();
      return;
    }
    getActionBar().setDisplayShowTitleEnabled(false);
    getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.tabs, R.layout.actionbar_spinner);
    list.setDropDownViewResource(R.layout.actionbar_spinner_dropdown);
    getActionBar().setListNavigationCallbacks(list, this);
    file_list = new FileListFragment_();
    getFragmentManager().beginTransaction().replace(R.id.fragment_frame, file_list).commit();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    cameraToUploadItem = menu.findItem(R.id.camera_to_upload);
    cameraToUploadItem.setVisible(CanHasCamera.isAvailable(this));
    return true;
  }

  public void startLogin() {
    LoginActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
    finish();
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    camImageUri = (Uri) savedInstanceState.getParcelable("camImageUri");
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable("camImageUri", camImageUri);
  }

  @Override
  public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    return true;
  }

  @OptionsItem(R.id.pick_to_upload)
  public void pickToUpload() {
    Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
    pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
    pickIntent.setType("*/*");
    startActivityForResult(Intent.createChooser(pickIntent, pick_chooser_title), REQUEST_CODE_PICK_FILE);
  }

  @OnActivityResult(REQUEST_CODE_PICK_FILE)
  public void onPickedFile(Intent resultIntent) {
    if (resultIntent == null) return;
    Intent uploadIntent = new Intent(Intent.ACTION_SEND);
    uploadIntent.setClass(this, UploadActivity_.class);
    uploadIntent.putExtra(Intent.EXTRA_STREAM, resultIntent.getData());
    startActivity(uploadIntent);
  }

  @OptionsItem(R.id.camera_to_upload)
  public void cameraToUpload() {
    Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    camImageUri = CanHasCamera.getImageUri();
    camIntent.putExtra(MediaStore.EXTRA_OUTPUT, camImageUri);
    startActivityForResult(camIntent, REQUEST_CODE_CAMERA);
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

  @OptionsItem(R.id.log_out)
  public void logOut() {
    final MainActivity self = this;
    new AlertDialog.Builder(this)
      .setTitle(log_out_confirm_title)
      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          adnPrefs.clear();
          file_list.deleteData();
          self.startLogin();
        }
      })
      .setNegativeButton(R.string.cancel, null)
      .show();
  }

  @OptionsItem(R.id.support)
  public void openSupport() {
    PostActivity_.intent(this).postType(PostActivity_.POST_TYPE_SUPPORT).start();
  }

  @OptionsItem(R.id.about)
  public void openAbout() {
    AboutActivity_.intent(this).start();
  }

}
