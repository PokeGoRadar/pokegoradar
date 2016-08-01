package radar.pokemons.com.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;
import radar.pokemons.com.FetchPokemonTask;
import radar.pokemons.com.FetchPokemons;
import radar.pokemons.com.R;
import radar.pokemons.com.Settings;
import radar.pokemons.com.adapter.AccountsAdapter;
import radar.pokemons.com.model.Account;
import radar.pokemons.com.model.TaskBundle;


public class AccountsSettingsActivity extends AppCompatActivity {

  private EditText username;
  private EditText password;
  private RecyclerView mRecyclerView;
  private AccountsAdapter mAdapter;
  private Button addAccountBtn;
  private Toolbar toolbar;
  final OkHttpClient http = new OkHttpClient();
  List<Account> accounts = new ArrayList<>();
  OkHttpClient okHttpClient = new OkHttpClient();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_accounts_settings);

    username = (EditText) findViewById(R.id.username);
    password = (EditText) findViewById(R.id.password);
    mRecyclerView = (RecyclerView) findViewById(R.id.account_list);
    addAccountBtn = (Button) findViewById(R.id.add_account_btn);
    toolbar = (Toolbar) findViewById(R.id.toolbar);

    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(getString(R.string.add_extra_account_title));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    accounts = new ArrayList<>(Settings.getAccounts(getApplicationContext()));

    mRecyclerView.setHasFixedSize(true);

    // use a linear layout manager
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    // create an Object for Adapter
    mAdapter = new AccountsAdapter(accounts, new AccountsAdapter.AccountAdapterListener() {

      @Override
      public void onAccountDelete(int index) {
        accounts.remove(index);
        mAdapter.notifyDataSetChanged();
        Settings.saveAccounts(getApplicationContext(), new HashSet<>(accounts));
      }
    });

    // set the adapter object to the Recyclerview
    mRecyclerView.setAdapter(mAdapter);


    addAccountBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String username = AccountsSettingsActivity.this.username.getText().toString().trim();
        String password = AccountsSettingsActivity.this.password.getText().toString().trim();
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        if (!accounts.contains(account)) {
          String token = checkAccountValidity(username, password);
          if (token == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.creds_not_valid), Toast.LENGTH_LONG).show();
          } else {
            account.setToken(token);
            accounts.add(account);
            mAdapter.notifyDataSetChanged();
            Settings.saveAccounts(getApplicationContext(), new HashSet<Account>(accounts));
          }
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.duplicate_account), Toast.LENGTH_LONG).show();
        }
      }
    });

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
    }
    return true;
  }

  private String checkAccountValidity(String username, String password) {
    try {
      return new LoginTask(username, password).execute().get(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      return null;
    }
  }

  public class LoginTask extends AsyncTask<String, String, String> {
    private String username;
    private String password;

    public LoginTask(String username, String password) {
      this.username = username;
      this.password = password;
    }

    private RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth;

    @Override
    protected String doInBackground(String... uri) {
      try {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        return new PTCLogin(http).login(username, password).getToken().getContents();
      } catch (LoginFailedException e) {
        return null;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... values) {
    }

    @Override
    protected void onPostExecute(String value) {
    }
  }
}
