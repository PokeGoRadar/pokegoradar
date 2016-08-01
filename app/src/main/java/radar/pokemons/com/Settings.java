package radar.pokemons.com;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import radar.pokemons.com.model.Account;

public class Settings {

  private static final String KEY_ACCOUNTS = "KEY_ACCOUNTS";
  private static final String KEY_USERNAME = "KEY_USERNAME";
  private static final String KEY_PASSWORD = "KEY_PASSWORD";
  public static Set<Account> getAccounts(Context context) {
    try {
      String jsonAccount = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ACCOUNTS, "[]");
      return new HashSet<>(Arrays.asList(new Gson().fromJson(jsonAccount, Account[].class)));
    } catch (Exception e) {
      return new HashSet<>();
    }
  }

  public static void saveAccounts(Context context, Set<Account> accounts) {
    String jsonAccount = new Gson().toJson(accounts);
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_ACCOUNTS, jsonAccount).commit();
  }

  public static void saveLoginAccount(Context context, String username, String password) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_USERNAME, username).commit();
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_PASSWORD, password).commit();
  }

  public static String getLoginUsername(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_USERNAME, "");
  }

  public static String getLoginPassword(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PASSWORD, "");
  }

  public static void updateAccount(Context context, Account account) {
    Set<Account> accounts = getAccounts(context);
    for (Account a : accounts) {
      if (a.equals(account)) {
        a.setUsername(account.getUsername());
        a.setPassword(account.getPassword());
        a.setToken(account.getPassword());
        saveAccounts(context, accounts);
      }
    }
  }

  private static Account getAccount(Context context, Account account) {
    for (Account a : getAccounts(context)) {
      if (a.equals(account)) {
        return a;
      }
    }
    return null;
  }
}
