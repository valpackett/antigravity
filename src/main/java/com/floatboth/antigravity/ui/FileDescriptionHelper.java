package com.floatboth.antigravity.ui;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import android.content.Context;
import android.text.format.DateFormat;
import com.samskivert.mustache.*;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.File;

public class FileDescriptionHelper {
  public static String shortDescription(Context context, File file) {
    String dateS = dateTime(context, file.createdAt);
    String sizeS = size(file.size, false);
    return dateS + "\n" + sizeS + " " + file.kind;
  }

  public static String longDescription(Context context, File file) {
    Map<String, String> desc = new HashMap<String, String>();
    desc.put("name", file.name);
    desc.put("type", file.type);
    desc.put("date", dateTime(context, file.createdAt));
    desc.put("size", size(file.size, false));
    return Mustache.compiler().compile(context.getString(R.string.file_description_template)).execute(desc);
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
