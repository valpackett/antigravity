package com.floatboth.antigravity.ui;

import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import com.floatboth.antigravity.data.File;
import com.floatboth.antigravity.R;

public class FileListAdapter extends BaseAdapter {
  private List<File> files;
  private LayoutInflater layoutInflater;
  private Context context;

  public FileListAdapter(Context context, LayoutInflater layoutInflater) {
    this.context = context;
    this.layoutInflater = layoutInflater;
    clearFiles();
  }

  public void clearFiles() {
    this.files = new ArrayList<File>();
    notifyDataSetChanged();
  }

  public void appendFiles(List<File> newFiles) {
    files.addAll(newFiles);
    notifyDataSetChanged();
  }

  public void setFiles(List<File> files) {
    this.files = files;
    notifyDataSetChanged();
  }

  public List<File> getFiles() {
    return files;
  }

  @Override
  public int getCount() {
    return files.size();
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public File getItem(int position) {
    return files.get(position);
  }

  static class ViewHolder {
    public TextView filenameView;
    public TextView descView;
    public ImageView thumbnailView;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    if (convertView == null) {
      convertView = layoutInflater.inflate(R.layout.file_in_list, null);
      holder = new ViewHolder();
      holder.filenameView = (TextView) convertView.findViewById(R.id.filename);
      holder.descView = (TextView) convertView.findViewById(R.id.description);
      holder.thumbnailView = (ImageView) convertView.findViewById(R.id.thumbnail);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    File file = getItem(position);
    holder.filenameView.setText(file.name);
    holder.descView.setText(FileDescriptionHelper.shortDescription(context, file));
    loadImage(file, holder.thumbnailView);
    return convertView;
  }

  private void loadImage(File file, ImageView view) {
    if (file.derivedFiles != null && file.derivedFiles.thumbnailSmall != null) {
      Picasso.with(context)
        .load(file.derivedFiles.thumbnailSmall.url)
        .placeholder(R.drawable.ic_file_basic)
        .fit()
        .into(view);
    } else {
      Picasso.with(context).load(R.drawable.ic_file_basic).into(view);
    }
  }
}
