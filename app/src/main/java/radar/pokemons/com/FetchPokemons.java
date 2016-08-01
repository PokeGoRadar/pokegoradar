package radar.pokemons.com;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;
import okhttp3.OkHttpClient;
import radar.pokemons.com.model.Account;
import radar.pokemons.com.model.TaskBundle;


public class FetchPokemons extends AsyncTask<LatLng, List<CatchablePokemon>, String> {

  private List<LatLng> scanPoints;
  private Set<Account> accounts;
  private FetchPokemonsListener fetchPokemonsListener;
  private OkHttpClient okHttpClient;
  private CompletionService completionService;
  private Handler handler;
  private Context context;

  public FetchPokemons(Context context, CompletionService completionService, Handler handler, OkHttpClient okHttpClient, List<LatLng> scanPoints, Set<Account> accounts, FetchPokemonsListener fetchPokemonsListener) {
    this.scanPoints = scanPoints;
    this.accounts = accounts;
    this.fetchPokemonsListener = fetchPokemonsListener;
    this.okHttpClient = okHttpClient;
    this.completionService = completionService;
    this.handler = handler;
    this.context = context;
  }

  public interface FetchPokemonsListener {
    void showProgressBar(boolean show);

    void setProgress(int progress);

    void setMaxProgress(int progress);

    void showMessage(String message);

    void scanComplete();
  }

  @Override
  protected String doInBackground(LatLng... lala) {
    try {

     List<TaskBundle> taskBundles = generateTaskBundles();

      for (int i = 0; i < scanPoints.size(); i++) {
        try {
          LatLng latLng = scanPoints.get(i);
          TaskBundle taskBundle = taskBundles.get(i % taskBundles.size());
          FetchPokemonTask test = new FetchPokemonTask(context, taskBundle, latLng.latitude, latLng.longitude, handler);
          completionService.submit(test);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      for (LatLng latLng : scanPoints) {
        try {
          Future<Callable> future = completionService.take();
          future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (fetchPokemonsListener != null) {
        fetchPokemonsListener.showMessage("An error happend during scan, please contact the developers");
      }
      return null;
    } finally {
      if (fetchPokemonsListener != null) {
        fetchPokemonsListener.scanComplete();
      }
    }

    return "";
  }

  @Override
  protected void onPostExecute(String result) {
    if (fetchPokemonsListener != null) {
      fetchPokemonsListener.showProgressBar(false);
    }
  }

  @Override
  protected void onPreExecute() {
    if (fetchPokemonsListener != null) {
      fetchPokemonsListener.showMessage("!! Scan might take up to 3min to complete !!");
      fetchPokemonsListener.showProgressBar(true);
      fetchPokemonsListener.setProgress(0);
      fetchPokemonsListener.setMaxProgress(scanPoints.size());
    }
  }

  @Override
  protected void onProgressUpdate(List<CatchablePokemon>... objects) {

  }

  private List<TaskBundle> generateTaskBundles() {
    List<TaskBundle> taskBundles = new ArrayList<>();
    // TODO move this somewhere else and check for token expiration...
    for (Account account : accounts) {
      AuthInfo authInfo = account.buildAuth();
      try {
        PokemonGo pokemon = new PokemonGo(authInfo, okHttpClient);
        TaskBundle taskBundle = new TaskBundle();
        taskBundle.setPokemonGo(pokemon);
        taskBundles.add(taskBundle);
        taskBundle.setAccount(account);
        taskBundle.setOkHttpClient(okHttpClient);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return taskBundles;
  }

}
