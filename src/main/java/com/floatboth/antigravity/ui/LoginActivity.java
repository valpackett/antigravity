package com.floatboth.antigravity.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import net.app.adnlogin.ADNPassportUtility;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.*;

import com.floatboth.antigravity.*;
import com.floatboth.antigravity.data.*;

@EActivity(R.layout.login_activity)
public class LoginActivity extends Activity {
  @Bean ADNClientFactory adnClientFactory;
  @Pref ADNPrefs_ adnPrefs;
  @ViewById(R.id.login_with_password) Button loginWithPasswordButton;
  @ViewById(R.id.username) EditText usernameField;
  @ViewById(R.id.password) EditText passwordField;
  @ViewById(R.id.login_with_passport) Button loginWithPassportButton;
  @ViewById(R.id.install_passport) Button installPassportButton;
  @ViewById(R.id.adn_info) TextView adnInfo;

  private static final int REQUEST_CODE_AUTHORIZE = 1;
  private static final String AUTHORIZE_ACTION = "net.app.adnpassport.authorize";
  private static final String SCOPES = "basic,write_post,files";

  // Event listeners

  public void onLoginSuccess(String token) {
    adnPrefs.accessToken().put(token);
    MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
  }

  @UiThread
  public void onLoginWithPasswordSuccess(String token) {
    setProgressBarIndeterminateVisibility(false);
    onLoginSuccess(token);
  }

  @OnActivityResult(REQUEST_CODE_AUTHORIZE)
  public void onLoginWithPassportSuccess(int resultCode, Intent data) {
    if (resultCode == 1) onLoginSuccess(data.getStringExtra("accessToken"));
  }

  private final BroadcastReceiver passportInstallReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      System.out.println("YO GOT TEH BRODCAST");
      if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()) &&
          intent.getDataString().equals(String.format("package:%s", ADNPassportUtility.APP_PACKAGE)))
        loginWithPassport();
    }
  };

  // UI helpers

  public void showError(String title, String message) {
    setProgressBarIndeterminateVisibility(false);
    new AlertDialog.Builder(this).setTitle(title).setMessage(message)
      .setPositiveButton(R.string.ok, null).show();
  }

  @UiThread
  public void showPasswordADNError(ADNAuthError ex) {
    showError(ex.title, ex.text);
  }

  @UiThread
  public void showUnknownError(Exception ex) {
    showError("Unknown error", ex.getMessage());
  }

  // Lifecycle

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    filter.addDataScheme("package");
    registerReceiver(passportInstallReceiver, filter);
  }

  @AfterViews
  public void setUpPassportLogin() {
    adnInfo.setText(Html.fromHtml(getString(R.string.adn_info)));
    adnInfo.setMovementMethod(LinkMovementMethod.getInstance());
    if (ADNPassportUtility.isPassportAuthorizationAvailable(this)) {
      loginWithPassportButton.setVisibility(View.VISIBLE);
    } else {
      installPassportButton.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(passportInstallReceiver);
  }

  // Actions

  @Click(R.id.install_passport)
  public void installPassport() {
    ADNPassportUtility.launchPassportInstallation(this);
  }

  @Click(R.id.login_with_passport)
  public void loginWithPassport() {
    Intent i = ADNPassportUtility.getAuthorizationIntent(getString(R.string.client_id), SCOPES);
    startActivityForResult(i, REQUEST_CODE_AUTHORIZE);
  }

  @Click(R.id.login_with_password)
  public void loginWithPassword() {
    String username = usernameField.getText().toString().trim();
    String password = passwordField.getText().toString();
    if (username.matches("")) {
      showError("Empty field :-(", "Username must not be empty.");
    } else if (password.matches("")) {
      showError("Empty field :-(", "Password must not be empty.");
    } else {
      setProgressBarIndeterminateVisibility(true);
      doLoginWithPasswordRequest(username, password);
    }
  }

  @Background
  public void doLoginWithPasswordRequest(String username, String password) {
    try {
      String token = adnClientFactory.getAccessToken(username, password, SCOPES);
      onLoginWithPasswordSuccess(token);
    } catch (ADNAuthError ex) {
      ex.printStackTrace();
      showPasswordADNError(ex);
    } catch (Exception ex) {
      ex.printStackTrace();
      showUnknownError(ex);
    }
  }
}
