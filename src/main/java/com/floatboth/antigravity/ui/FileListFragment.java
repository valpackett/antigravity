package com.floatboth.antigravity.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.text.Html;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.*;
import org.androidannotations.annotations.res.StringRes;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;
import com.floatboth.antigravity.net.*;

@EFragment(R.layout.file_list_fragment)
public class FileListFragment extends Fragment
  implements AdapterView.OnItemClickListener,
             OnRefreshListener {
  @StringRes String network_error;
  @StringRes String no_posts;

  @Bean DataCache dataCache;
  @Pref ADNPrefs_ adnPrefs;
  @SystemService LayoutInflater layoutInflater;
  @SystemService ConnectivityManager connManager;
  @ViewById ListView filelist;
  @ViewById PullToRefreshLayout ptr_layout;
  String adnToken;
  FileListAdapter fileadapter;
  String minId;
  Button loadMoreButton;
  TextView noPostsTextView;
  boolean isShowingWelcome = false;

  public static final int REQUEST_CODE_UPDATE_FILE = 1;

  public void deleteData() {
    dataCache.delete("files");
    dataCache.delete("minId");
    dataCache.delete("more");
  }

  private void cacheData(File.List files, String minId, boolean more) {
    dataCache.set("files", files);
    dataCache.set("minId", minId);
    dataCache.set("more", (Boolean) more);
  }

  private void applyData(File.List files, String minId, boolean more) {
    try {
      adnPrefs.lastUrlExpires().put(files.get(files.size()-1).urlExpires.getTime() / 1000L);
    } catch (ArrayIndexOutOfBoundsException ex) {}
    fileadapter.appendFiles(files);
    loadMoreButton.setEnabled(more); // YAY :-)
    this.minId = minId;
  }

  public class MyFilesListener implements RequestListener<File.List> {
    @Override
    public void onRequestFailure(SpiceException spiceException) {
      loadMoreButton.setEnabled(true); // refresh/loadMore disables loadMoreButton -> error -> applyData not invoked -> loadMoreButton not enabled
      getActivity().setProgressBarIndeterminateVisibility(false);
      Toast.makeText(getActivity(), network_error, Toast.LENGTH_SHORT).show();
      spiceException.printStackTrace();
    }

    @Override
    public void onRequestSuccess(final File.List data) {
      adnPrefs.refreshFlag().put(false);
      applyData(data, data.meta.minId, data.meta.more); // does loadMoreButton.setEnabled(more)
      cacheData(fileadapter.getFiles(), data.meta.minId, data.meta.more);
      boolean isNoFiles = data.size() == 0;
      if (!isShowingWelcome && isNoFiles) {
        filelist.removeFooterView(loadMoreButton);
        filelist.addFooterView(noPostsTextView);
        isShowingWelcome = true;
      } else if (isShowingWelcome && !isNoFiles) {
        filelist.removeFooterView(noPostsTextView);
        filelist.addFooterView(loadMoreButton);
        isShowingWelcome = false;
      }
      getActivity().setProgressBarIndeterminateVisibility(false);
    }
  }

  public class MyFilesRefreshListener extends MyFilesListener {
    @Override
    public void onRequestSuccess(final File.List data) {
      fileadapter.clearFiles();
      super.onRequestSuccess(data);
      ptr_layout.setRefreshComplete();
    }
  }

  @Override
  public void onRefreshStarted(View v) {
    loadMoreButton.setEnabled(false);
    ((BaseActivity) getActivity()).getSpiceManager().execute(new MyFilesRequest(adnToken, ""), new MyFilesRefreshListener());
  }

  private void loadFiles(String beforeId) {
    ((BaseActivity) getActivity()).getSpiceManager().execute(new MyFilesRequest(adnToken, beforeId), new MyFilesListener());
  }

  private void loadInitialFiles() {
    getActivity().setProgressBarIndeterminateVisibility(true);
    try {
      File.List filesFromCache = (File.List) dataCache.get("files");
      String minIdFromCache = (String) dataCache.get("minId");
      Boolean moreFromCache = (Boolean) dataCache.get("more");
      boolean cacheExists = filesFromCache != null && minIdFromCache != null && moreFromCache != null;
      if (cacheExists && !shouldRefreshFiles()) {
        applyData(filesFromCache, minIdFromCache, moreFromCache);
        loadMoreButton.setEnabled(true);
        getActivity().setProgressBarIndeterminateVisibility(false);
      } else {
        loadFiles("");
      }
    } catch (Exception ex) {
      loadFiles("");
    }
  }

  private void setUpLoadMoreButton() {
    loadMoreButton = new Button(getActivity());
    loadMoreButton.setEnabled(false);
    loadMoreButton.setText(R.string.load_more);
    loadMoreButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        loadMoreButton.setEnabled(false);
        getActivity().setProgressBarIndeterminateVisibility(true);
        loadFiles(minId);
      }
    });
  }

  private void setUpNoPostsTextView() {
    noPostsTextView = (TextView) layoutInflater.inflate(R.layout.no_files, null);
    noPostsTextView.setText(Html.fromHtml(no_posts));
  }

  public void onItemClick(AdapterView lst, View v, int position, long id) {
    try {
      FileActivity_.intent(this)
        .file(fileadapter.getItem(position))
        .startForResult(REQUEST_CODE_UPDATE_FILE);
    } catch (IndexOutOfBoundsException ex) {}
  }

  @Override
  public void onResume() {
    super.onResume();
    if (shouldRefreshFiles()) {
      onRefreshStarted(null);
    }
  }

  @OnActivityResult(REQUEST_CODE_UPDATE_FILE)
  public void updateFileCache(Intent updateIntent) {
    if (updateIntent == null) return;
    File file = (File) updateIntent.getSerializableExtra("file");
    File.List updatedFiles = new File.List();
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

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @AfterViews
  public void setUpFileList() {
    adnToken = adnPrefs.accessToken().get();
    setUpLoadMoreButton();
    setUpNoPostsTextView();
    fileadapter = new FileListAdapter(getActivity(), layoutInflater);
    filelist.setAdapter(fileadapter);
    filelist.setOnItemClickListener(this);
    filelist.addFooterView(loadMoreButton);
    ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
      .listener(this).setup(ptr_layout);
    if (adnPrefs.accessToken().exists()) loadInitialFiles();
  }

  private boolean networkIsWiFi() {
    NetworkInfo ni = connManager.getActiveNetworkInfo();
    return ni != null && ni.isAvailable() && ni.getType() == ConnectivityManager.TYPE_WIFI;
  }

  private boolean filesNeedRefreshing() {
    return adnPrefs.refreshFlag().getOr(false) ||
      (new Date().getTime() / 1000L) > adnPrefs.lastUrlExpires().getOr(Long.MAX_VALUE);
  }

  private boolean shouldRefreshFiles() {
    return networkIsWiFi() && filesNeedRefreshing();
  }
}
