package radar.pokemons.com.model;

import com.pokegoapi.api.PokemonGo;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.OkHttpClient;

public class TaskBundle {
  private Account account;
  private PokemonGo pokemonGo;
  private OkHttpClient okHttpClient;
  private Lock lock = new ReentrantLock();

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public OkHttpClient getOkHttpClient() {
    return okHttpClient;
  }

  public void setOkHttpClient(OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
  }

  public Lock getLock() {
    return lock;
  }

  public PokemonGo getPokemonGo() {
    return pokemonGo;
  }

  public void setPokemonGo(PokemonGo pokemonGo) {
    this.pokemonGo = pokemonGo;
  }
}
