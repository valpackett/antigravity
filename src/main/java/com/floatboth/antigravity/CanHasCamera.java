package com.floatboth.antigravity;

import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.net.Uri;

public class CanHasCamera {
  public static boolean isAvailable(Context ctx) {
    PackageManager pm = ctx.getPackageManager();
    boolean hasCameraDevice = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    boolean hasCameraApp = pm.queryIntentActivities(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
        PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    return hasCameraDevice && hasCameraApp;
  }

  public static Uri getImageUri() {
    File picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Antigravity");
    picDir.mkdirs();
    String picName = "Photo_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
    return Uri.fromFile(new File(picDir, picName));
  }
}
