package radar.pokemons.com.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Set;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;
import okhttp3.OkHttpClient;
import radar.pokemons.com.R;
import radar.pokemons.com.Settings;
import radar.pokemons.com.model.Account;

public class LoginActivity extends AppCompatActivity {

  private ProgressBar progressBar;
  private Button loginBtn;
  private EditText username;
  private EditText password;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    setTitle("PokemonGO Radar - Login");

    username = (EditText) findViewById(R.id.username);
    password = (EditText) findViewById(R.id.password);
    loginBtn = (Button) findViewById(R.id.loginBtn);

    final String usernameS = Settings.getLoginUsername(getApplicationContext());
    final String passwordS = Settings.getLoginPassword(getApplicationContext());
    username.setText(usernameS);
    password.setText(passwordS);
    loginBtn.setEnabled(true);

    loginBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        Settings.saveLoginAccount(getApplicationContext(), usernameStr, passwordStr);
        new LoginProcess(username.getText().toString().trim(), password.getText().toString().trim()).execute();
      }
    });

    MobileAds.initialize(getApplicationContext(), "ca-app-pub-9879577400303327~3054725490");
    AdView mAdView = (AdView) findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder()
        //.addTestDevice("17A4EF47D4DCBE08E3640CA30A42326F")
        .build();

    mAdView.loadAd(adRequest);

    progressBar = (ProgressBar) findViewById(R.id.loading);
  }

  private class MyToken {
    private String token;

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  public class LoginProcess extends AsyncTask<String, String, AuthInfo> {
    private String username;
    private String password;

    public LoginProcess(String username, String password) {
      this.username = username;
      this.password = password;
    }

    private AuthInfo auth;

    @Override
    protected AuthInfo doInBackground(String... uri) {
      final OkHttpClient http = new OkHttpClient();
      try {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        auth = new PTCLogin(http).login(username, password);
        return auth;
      } catch (LoginFailedException e) {
        return null;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }

    @Override
    protected void onPreExecute() {
      progressBar.setVisibility(View.VISIBLE);
      loginBtn.setEnabled(false);
    }

    @Override
    protected void onProgressUpdate(String... values) {
      if (values == null) {
        progressBar.setVisibility(View.GONE);
        loginBtn.setEnabled(true);
        Toast.makeText(LoginActivity.this, "We are having problem logging you in, server might be busy", Toast.LENGTH_LONG).show();
      }
    }

    @Override
    protected void onPostExecute(AuthInfo auth) {
      super.onPostExecute(auth);
      progressBar.setVisibility(View.GONE);
      loginBtn.setEnabled(true);
      if (auth != null && !TextUtils.isEmpty(auth.getToken().getContents())) {
        Set<Account> accountList = Settings.getAccounts(getApplicationContext());
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setToken(auth.getToken().getContents());
        boolean added = accountList.add(account);
        if (added) {
          Settings.saveAccounts(getApplicationContext(), accountList);
        }
        startActivity(MapsActivity.getIntent(getApplicationContext(), auth));
      } else {
        Toast.makeText(LoginActivity.this, "Your pokemon trainer username or password is not valid, or maybe servers are down", Toast.LENGTH_LONG).show();
      }
    }
  }
}
