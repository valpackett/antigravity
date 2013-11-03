package com.floatboth.antigravity.ui;

import java.util.Date;
import android.content.Context;
import android.text.format.DateFormat;
import com.floatboth.antigravity.data.File;

public class FileDescriptionHelper {
  public static String shortDescription(Context context, File file) {
    String dateS = dateTime(context, file.createdAt);
    String sizeS = size(file.size, false);
    return dateS + "\n" + sizeS + " " + file.kind;
  }

  public static String dateTime(Context context, Date date) {
    return DateFormat.getMediumDateFormat(context).format(date) +
      " " + DateFormat.getTimeFormat(context).format(date);
  }

  // http://stackoverflow.com/a/3758880
  public static String size(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }
}
